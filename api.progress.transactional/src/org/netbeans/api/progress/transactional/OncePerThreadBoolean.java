/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.api.progress.transactional;

import java.util.Set;
import org.openide.util.WeakSet;

/**
 * A wrapper for a thread-safe boolean which, once set, will return true
 * exactly once for any thread.
 *
 * The contract is as follows:
 * <p/>
 * Any thread may call <code>set()</code> to set the value to true.  Each thread
 * which calls <code>get()</code> afterwards will be returned a value of true
 * <i>exactly once</i>, after which, subsequent calls to <code>get()</code>
 * will return false.  Subsequent calls to <code>set()</code> after it has
 * been called once from any thread will have no effect.
 * <p/>
 * A call to <code>clear()</code> from any thread resets the object to its
 * initial state.  After that, a subsequent call to <code>set()</code> will
 * invoke the same behavior described above.
 * <p/>
 * Used to implement parallel cancellation semantics across multiple threads.
 * If multiple threads are concurrently running a transactions over the same
 * TransactionController in parallel, user cancellation will set the controller's
 * cancelled flag.  After that, the next call to <code>TransactionController.checkCancelled()</code>
 * will throw a <code>TransactionException</code> <i>only the first time
 * <code>checkCancelled()</code> from that particular thread.  This will
 * trigger a rollback on that thread, without additional TransactionExceptions
 * being thrown.
 *
 * @author Tim Boudreau
 */
final class OncePerThreadBoolean {
    private final Set<Thread> threads = new WeakSet<Thread>();
    private volatile boolean isSet;

    void clear() {
        synchronized (threads) {
            threads.clear();
            isSet = false;
        }
    }

    void set(Thread thread) {
        synchronized (threads) {
            if (!isSet) {
                isSet = true;
                threads.clear();
                threads.add(thread);
            }
        }
    }

    void set() {
        synchronized (threads) {
            if (!isSet) {
                isSet = true;
                threads.clear();
            }
        }
    }

    boolean get() {
        synchronized (threads) {
            if (isSet && !threads.contains(Thread.currentThread())) {
                threads.add(Thread.currentThread());
                return true;
            }
        }
        return false;
    }
}

