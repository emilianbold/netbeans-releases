/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.parsing.lucene;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.NativeFSLockFactory;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
class RecordOwnerLockFactory extends NativeFSLockFactory {
    
    private final Set</*@GuardedBy("this")*/RecordOwnerLock> locked = Collections.newSetFromMap(new IdentityHashMap<RecordOwnerLock, Boolean>());
    //@GuardedBy("this")
    private Thread owner;
    //@GuardedBy("this")
    private Exception caller;

    RecordOwnerLockFactory() throws IOException {
        super();
    }
    
    @CheckForNull
    Thread getOwner() {
        return owner;
    }

    @CheckForNull
    Exception getCaller() {
        return caller;
    }

    /**
     * Force freeing of lock file.
     * Lucene IndexWriter.closeInternal does not free lock file when exception
     * happens in it. This method tries to do the best to do free it.
     * @throws IOException if lock(s) cannot be freed.
     */
    synchronized void forceRemoveLock() throws IOException {
        final Collection<? extends RecordOwnerLock> safeIt = new ArrayList<RecordOwnerLock>(locked);
        Throwable cause = null;
        for (RecordOwnerLock l : safeIt) {
            try {
                l.release();
            } catch (Throwable t) {
                if (t instanceof ThreadDeath) {
                    throw (ThreadDeath) t;
                } else if (cause == null) {
                    cause = t;
                }
            }
        }
        if (cause != null) {
            throw new IOException(cause);
        }
    }
    
    
    @Override
    public Lock makeLock(String lockName) {
        return new RecordOwnerLock(super.makeLock(lockName));
    }
    
    @Override
    public void clearLock(String lockName) throws IOException {
        super.clearLock(lockName);
        synchronized (this) {
            owner = null;
            caller = null;
        }
    }


    private synchronized void recordOwner(
        @NonNull final Thread t,
        @NonNull final RecordOwnerLock l) {
        Parameters.notNull("t", t); //NOI18N
        Parameters.notNull("l", l); //NOI18N
        if (owner != t) {
            owner = t;
            caller = new Exception();
        }
        locked.add(l);
    }

    private synchronized void clearOwner(
        @NonNull final RecordOwnerLock l) {
        Parameters.notNull("l", l); //NOI18N
        locked.remove(l);
        owner = null;
        caller = null;
    }
    
    private class RecordOwnerLock extends Lock {
        
        private final Lock delegate;
        
        private RecordOwnerLock(@NonNull final Lock delegate) {
            assert delegate != null;
            this.delegate = delegate;
        }

        @Override
        public boolean obtain() throws IOException {
            final boolean result = delegate.obtain();
            if (result) {
                recordOwner(Thread.currentThread(), this);
            }
            return result;
        }

        @Override
        public void release() throws IOException {
            delegate.release();
            clearOwner(this);
        }

        @Override
        public boolean isLocked() throws IOException {
            return delegate.isLocked();
        }
    }    
}
