/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.api.debugger.jpda;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.Set;

/**
 *
 * @author Martin Entlicher
 */
public abstract class DeadlockDetector {
    
    public static final String PROP_DEADLOCK = "deadlock"; // NOI18N
    
    private Set<Deadlock> deadlocks;
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    public final synchronized Set<Deadlock> getDeadlocks() {
        return deadlocks;
    }
    
    protected final void setDeadlocks(Set<Deadlock> deadlocks) {
        synchronized (this) {
            this.deadlocks = deadlocks;
        }
        firePropertyChange(PROP_DEADLOCK, null, deadlocks);
    }
    
    protected final Deadlock createDeadlock(Collection<JPDAThread> threads) {
        return new Deadlock(threads);
    }
    
    private void firePropertyChange(String name, Object oldValue, Object newValue) {
        pcs.firePropertyChange(name, oldValue, newValue);
    }
    
    public final void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public final void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    public static final class Deadlock {

        private Collection<JPDAThread> threads;
        
        private Deadlock(Collection<JPDAThread> threads) {
            this.threads = threads;
        }
        
        public Collection<JPDAThread> getThreads() {
            return threads;
        }
    }
    
    /*public static final class ThreadInDeadlock {
        
        private JPDAThread thread;
        private MonitorInfo monitor;
        
        public ThreadInDeadlock(JPDAThread thread, MonitorInfo monitor) {
            this.thread = thread;
            this.monitor = monitor;
        }
        
        public JPDAThread getThread() {
            return thread;
        }
        
        public MonitorInfo getMonitor() {
            return monitor;
        }
    }*/
}
