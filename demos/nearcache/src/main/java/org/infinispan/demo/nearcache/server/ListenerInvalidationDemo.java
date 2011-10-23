/*
 * Copyright 2011 Red Hat, Inc. and/or its affiliates.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA
 */

package org.infinispan.demo.nearcache.server;

import org.hornetq.integration.bootstrap.HornetQBootstrapServer;

import java.io.InputStream;

import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryRemoved;
import org.infinispan.notifications.cachelistener.event.CacheEntryEvent;
import org.infinispan.server.core.CacheValue;
import org.infinispan.server.core.Main;
import org.infinispan.server.core.ProtocolServer;
import org.infinispan.util.ByteArrayKey;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * // TODO: Document this
 *
 * @author Galder Zamarreño
 * @since // TODO
 */
public class ListenerInvalidationDemo {

   public static void main(String[] args) throws Exception {
      final InputStream in = System.in;

      // Start HornetQ server
      HornetQBootstrapServer.main(new String[]{"hornetq-beans.xml"});
      // Start Infinispan remote data grid
      Main.main(new String[]{"-r", "hotrod"});

      ProtocolServer server = Main.getServer();
      EmbeddedCacheManager cm = Main.getCacheManager();
      Context ctx = new InitialContext();
      Connection con = null;
      try {
         con = ((ConnectionFactory) ctx.lookup("/ConnectionFactory"))
               .createConnection();
         // Add a message producer to send invalidation messages to near cache clients
         Session s = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
         Topic topic = (Topic) ctx.lookup("/topic/datagrid");
         cm.addListener(new InvalidationProducer(s, topic));

      } finally {
         while (in.read() != -1) {}
         ctx.close();
         if (con != null) con.close();
         server.stop();
         cm.stop();
         System.exit(0);
      }
   }

   @Listener
   public static class InvalidationProducer {

      final MessageProducer msgProducer;
      final Session s;
      final Topic topic;

      public InvalidationProducer(Session s, Topic topic) throws JMSException {
         this.s = s;
         this.topic = topic;
         msgProducer = s.createProducer(topic);
      }

      @CacheEntryModified
      @CacheEntryRemoved
      public void invalidate(CacheEntryEvent<ByteArrayKey, CacheValue> e) throws JMSException {
         byte[] keyBytes = e.getKey().getData();
         BytesMessage msg = s.createBytesMessage();
         // Create a message with the key that's been modified or removed
         msg.writeBytes(keyBytes);
         msgProducer.send(msg);
      }

   }


}
