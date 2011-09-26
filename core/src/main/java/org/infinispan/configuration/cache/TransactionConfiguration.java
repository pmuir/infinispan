package org.infinispan.configuration.cache;

import org.infinispan.transaction.LockingMode;
import org.infinispan.transaction.TransactionMode;
import org.infinispan.transaction.lookup.TransactionManagerLookup;

public class TransactionConfiguration {

   private final boolean autoCommit;
   private final int cacheStopTimeout;
   private final boolean eagerLockingSingleNode;
   private final LockingMode lockingMode;
   private final boolean syncCommitPhase;
   private final boolean syncRollbackPhase;
   private final TransactionManagerLookup transactionManagerLookup;
   private final TransactionMode transactionMode;
   private final boolean useEagerLocking;
   private final boolean useSynchronization;
   private final RecoveryConfiguration recovery;
   
   TransactionConfiguration(boolean autoCommit, int cacheStopTimeout, boolean eagerLockingSingleNode, LockingMode lockingMode,
         boolean syncCommitPhase, boolean syncRollbackPhase, TransactionManagerLookup transactionManagerLookup,
         TransactionMode transactionMode, boolean useEagerLocking, boolean useSynchronization, RecoveryConfiguration recovery) {
      this.autoCommit = autoCommit;
      this.cacheStopTimeout = cacheStopTimeout;
      this.eagerLockingSingleNode = eagerLockingSingleNode;
      this.lockingMode = lockingMode;
      this.syncCommitPhase = syncCommitPhase;
      this.syncRollbackPhase = syncRollbackPhase;
      this.transactionManagerLookup = transactionManagerLookup;
      this.transactionMode = transactionMode;
      this.useEagerLocking = useEagerLocking;
      this.useSynchronization = useSynchronization;
      this.recovery = recovery;
   }

   public boolean isAutoCommit() {
      return autoCommit;
   }

   public int getCacheStopTimeout() {
      return cacheStopTimeout;
   }

   public boolean isEagerLockingSingleNode() {
      return eagerLockingSingleNode;
   }

   public LockingMode getLockingMode() {
      return lockingMode;
   }

   public boolean isSyncCommitPhase() {
      return syncCommitPhase;
   }

   public boolean isSyncRollbackPhase() {
      return syncRollbackPhase;
   }

   public TransactionManagerLookup getTransactionManagerLookup() {
      return transactionManagerLookup;
   }

   public TransactionMode getTransactionMode() {
      return transactionMode;
   }

   public boolean isUseEagerLocking() {
      return useEagerLocking;
   }

   public boolean isUseSynchronization() {
      return useSynchronization;
   }
   
   public RecoveryConfiguration getRecovery() {
      return recovery;
   }
   
}
