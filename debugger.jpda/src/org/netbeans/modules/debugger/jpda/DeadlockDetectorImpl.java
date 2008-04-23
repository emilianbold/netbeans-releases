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

package org.netbeans.modules.debugger.jpda;

import com.sun.jdi.ThreadReference;
import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.netbeans.api.debugger.jpda.DeadlockDetector;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.models.ThreadsCache;
import org.openide.util.WeakListeners;
import org.openide.util.WeakSet;

/**
 *
 * @author martin
 */
public class DeadlockDetectorImpl extends DeadlockDetector implements PropertyChangeListener {
    
    private JPDADebugger debugger;
    private Set<JPDAThread> suspendedThreads = new WeakSet<JPDAThread>();

    DeadlockDetectorImpl(JPDADebugger debugger) {
        this.debugger = debugger;
        debugger.addPropertyChangeListener(this);
        List<JPDAThread> threads = debugger.getAllThreads();
        for (JPDAThread thread : threads) {
            ((Customizer) thread).addPropertyChangeListener(WeakListeners.propertyChange(this, thread));
            if (thread.isSuspended()) {
                synchronized (suspendedThreads) {
                    suspendedThreads.add(thread);
                }
            }
        }
    }
    
    private Set<Deadlock> findDeadlockedThreads(Collection<JPDAThread> threads) {
        // TODO: Implement this
        return null;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if (JPDADebugger.PROP_THREAD_STARTED.equals(propName)) {
            JPDAThread thread = (JPDAThread) evt.getNewValue();
            ((Customizer) thread).addPropertyChangeListener(WeakListeners.propertyChange(this, thread));
        } else if (JPDAThread.PROP_SUSPENDED.equals(propName)) {
            JPDAThread thread = (JPDAThread) evt.getSource();
            boolean suspended = (Boolean) evt.getNewValue();
            Set<Deadlock> deadlocks = null;
            synchronized (suspendedThreads) {
                if (suspended) {
                    suspendedThreads.add(thread);
                    deadlocks = findDeadlockedThreads(suspendedThreads);
                } else {
                    suspendedThreads.remove(thread);
                }
            }
            if (deadlocks != null) {
                setDeadlocks(deadlocks);
            }
        }
    }

}
