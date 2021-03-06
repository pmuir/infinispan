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

package org.infinispan.container.versioning;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.VersioningScheme;
import org.infinispan.test.MultipleCacheManagersTest;
import org.infinispan.test.fwk.CleanupAfterMethod;
import org.infinispan.test.fwk.TestCacheManagerFactory;
import org.infinispan.transaction.LockingMode;
import org.infinispan.util.concurrent.IsolationLevel;
import org.testng.annotations.Test;

import javax.transaction.RollbackException;
import javax.transaction.Transaction;

@Test(testName = "container.versioning.ReplWriteSkewTest", groups = "functional")
@CleanupAfterMethod
public class ReplWriteSkewTest extends MultipleCacheManagersTest {
   @Override
   protected void createCacheManagers() throws Throwable {
      ConfigurationBuilder builder = TestCacheManagerFactory.getDefaultCacheConfiguration(true);

      builder
            .clustering()
               .cacheMode(CacheMode.REPL_SYNC)
            .versioning()
               .enable()
               .scheme(VersioningScheme.SIMPLE)
            .locking()
               .isolationLevel(IsolationLevel.REPEATABLE_READ)
               .writeSkewCheck(true)
            .transaction()
               .lockingMode(LockingMode.OPTIMISTIC)
               .syncCommitPhase(true);

      createCluster(builder, 2);
   }

   public void testWriteSkew() throws Exception {
      Cache<Object, Object> cache0 = cache(0);
      Cache<Object, Object> cache1 = cache(1);

      // Auto-commit is true
      cache0.put("hello", "world 1");

      tm(0).begin();
      assert "world 1".equals(cache0.get("hello"));
      Transaction t = tm(0).suspend();

      // Induce a write skew
      cache1.put("hello", "world 3");

      assert cache0.get("hello").equals("world 3");
      assert cache1.get("hello").equals("world 3");

      tm(0).resume(t);
      cache0.put("hello", "world 2");

      try {
         tm(0).commit();
         assert false : "Transaction should roll back";
      } catch (RollbackException re) {
         // expected
      }

      assert "world 3".equals(cache0.get("hello"));
      assert "world 3".equals(cache1.get("hello"));
   }

   public void testWriteSkewMultiEntries() throws Exception {
      Cache<Object, Object> cache0 = cache(0);
      Cache<Object, Object> cache1 = cache(1);

      tm(0).begin();
      cache0.put("hello", "world 1");
      cache0.put("hello2", "world 1");
      tm(0).commit();

      tm(0).begin();
      cache0.put("hello2", "world 2");
      assert "world 2".equals(cache0.get("hello2"));
      assert "world 1".equals(cache0.get("hello"));
      Transaction t = tm(0).suspend();

      // Induce a write skew
      // Auto-commit is true
      cache1.put("hello", "world 3");

      assert cache0.get("hello").equals("world 3");
      assert cache0.get("hello2").equals("world 1");
      assert cache1.get("hello").equals("world 3");
      assert cache1.get("hello2").equals("world 1");

      tm(0).resume(t);
      cache0.put("hello", "world 2");

      try {
         tm(0).commit();
         assert false : "Transaction should roll back";
      } catch (RollbackException re) {
         // expected
      }

      assert cache0.get("hello").equals("world 3");
      assert cache0.get("hello2").equals("world 1");
      assert cache1.get("hello").equals("world 3");
      assert cache1.get("hello2").equals("world 1");
   }

   public void testNullEntries() throws Exception {
      Cache<Object, Object> cache0 = cache(0);
      Cache<Object, Object> cache1 = cache(1);

      // Auto-commit is true
      cache0.put("hello", "world");

      tm(0).begin();
      assert "world".equals(cache0.get("hello"));
      Transaction t = tm(0).suspend();

      cache1.remove("hello");

      assert null == cache0.get("hello");
      assert null == cache1.get("hello");

      tm(0).resume(t);
      cache0.put("hello", "world2");

      try {
         tm(0).commit();
         assert false : "This transaction should roll back";
      } catch (RollbackException expected) {
         // expected
      }

      assert null == cache0.get("hello");
      assert null == cache1.get("hello");
   }
}
