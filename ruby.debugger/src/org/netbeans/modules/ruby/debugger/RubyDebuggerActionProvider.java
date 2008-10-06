/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.ruby.debugger;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.modules.ruby.debugger.breakpoints.RubyBreakpointManager;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.rubyforge.debugcommons.RubyDebugEventListener;
import org.rubyforge.debugcommons.RubyDebugEvent;
import org.rubyforge.debugcommons.RubyDebuggerException;

public final class RubyDebuggerActionProvider extends ActionsProviderSupport implements RubyDebugEventListener {
    
    private static final Set<Object> ACTIONS;
    
    static {
        Set<Object> s = new HashSet<Object>();
        s.add(ActionsManager.ACTION_KILL);
        s.add(ActionsManager.ACTION_CONTINUE);
        s.add(ActionsManager.ACTION_STEP_INTO);
        s.add(ActionsManager.ACTION_STEP_OVER);
        s.add(ActionsManager.ACTION_STEP_OUT);
        s.add(ActionsManager.ACTION_RUN_TO_CURSOR);
        ACTIONS = Collections.unmodifiableSet(s);
    }
    
    private final Semaphore frontEndSemaphore;
    private final Semaphore backEndSemaphore;
    
    private final ContextProviderWrapper contextProvider;
    private final RubyDebuggerEngineProvider engineProvider;
    private final RubySession rubySession;
    private Boolean terminated;
    
    public RubyDebuggerActionProvider(final ContextProvider contextProvider) {
        engineProvider = (RubyDebuggerEngineProvider) contextProvider.
                lookupFirst(null, DebuggerEngineProvider.class);
        this.contextProvider = new ContextProviderWrapper(contextProvider);
        rubySession = this.contextProvider.getRubySession();
        frontEndSemaphore = new Semaphore(1, true);
        backEndSemaphore = new Semaphore(1, true);
        terminated = false;
        
        // backends comes first
        boolean acquired = frontEndSemaphore.tryAcquire();
        assert acquired;
        
        // init actions
        for (Object action : ACTIONS) {
            setEnabled(action, true);
        }
    }
    
    @Override
    public Set<Object> getActions() {
        return ACTIONS;
    }
    
    @Override
    public void doAction(final Object action) {
        Util.finer("Performing \"" + action + '"');
        if (action == ActionsManager.ACTION_KILL) {
            finish(true);
            return;
        }
        if (frontEndSemaphore.getQueueLength() > 10) {
            Util.info("Too much pending events (> 10). Action \"" + action + "\" is rejected."); // NOI18N
            return;
        }
        try {
            frontEndSemaphore.acquire();
        } catch (InterruptedException e) {
            Util.severe(e);
            return;
        }
        synchronized (terminated) {
            if (terminated) {
                Util.info("Flushing cached actions: " + action + ", process is terminated.");
                frontEndSemaphore.release();
                return; // ignore cached actions
            }
        }
        if (action == ActionsManager.ACTION_CONTINUE) {
            rubySession.resume();
            contextProvider.fireModelChanges();
        } else if (action == ActionsManager.ACTION_STEP_INTO) {
            rubySession.stepInto();
        } else if (action == ActionsManager.ACTION_RUN_TO_CURSOR) {
            rubySession.runToCursor();
        } else if (action == ActionsManager.ACTION_STEP_OUT) {
            if (rubySession.getFrames().length == 1) {
                StatusDisplayer.getDefault().setStatusText(
                        NbBundle.getMessage(RubyDebuggerActionProvider.class,
                        "RubyDebuggerActionProvider.stepout.outermost.frame")); // NOI18N
                frontEndSemaphore.release();
                return;
            }
            rubySession.stepReturn();
        } else if (action == ActionsManager.ACTION_STEP_OVER) {
            rubySession.stepOver();
        }
        ContextProviderWrapper.getSessionsModel().fireChanges();
        backEndSemaphore.release();
    }
    
    /**
     * Fed by backends.
     *
     * @param event backend event
     */
    public void onDebugEvent(final RubyDebugEvent event) {
        if (event.isTerminateType()) {
            finish(false);
            return;
        }
        try {
            backEndSemaphore.acquire();
        } catch (InterruptedException e) {
            Util.severe(e);
            return;
        }
        synchronized (terminated) {
            if (terminated) {
                Util.info("Flushing pending event: " + event + ", process is terminated."); // NOI18N
                backEndSemaphore.release();
                return;
            }
        }
        if (event.isSuspensionType() || event.isExceptionType()) {
            String path = event.getFilePath();
            // HACK, do not try to trace the 'eval-code'. Cf. #106115, #146894
            if ("(eval)".equals(path)) { // NOI18N
                rubySession.stepOver(true);
                ContextProviderWrapper.getSessionsModel().fireChanges();
                backEndSemaphore.release();
                return;
            }
            String absPath = rubySession.resolveAbsolutePath(path);
            if (absPath != null) {
                File file = new File(absPath);
                FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(file));
                boolean shouldStop = event.isExceptionType() ||
                        event.isStepping() || rubySession.isRunningTo(file, event.getLine()) ||
                         (fo != null && RubyBreakpointManager.isBreakpointOnLine(fo, event.getLine()));
                if (shouldStop) {
                    stopHere(event);
                } else {
                    event.getRubyThread().resume();
                    backEndSemaphore.release();
                    return;
                }
            } else {
                try {
                    // trying to step into file which we are not able to resolve, step back
                    event.getRubyThread().stepReturn();
                    backEndSemaphore.release();
                    return;
                } catch (RubyDebuggerException e) {
                    Util.severe(e);
                }
            }
        } else {
            assert false : "Unkown event type: " + event;
        }
        frontEndSemaphore.release();
    }
    
    /**
     * @param terminate whether termination of the underlaying process should be
     *        forced.
     */
    private void finish(boolean terminate) {
        synchronized (terminated) {
            Util.finer("Finishing session: " + rubySession.getName());
            if (terminated) {
                Util.warning("Finish is not supposed to be called when a process is already terminated");
                return;
            }
            terminated = true;
        }
        rubySession.finish(this, terminate);
        EditorUtil.unmarkCurrent();
        engineProvider.getDestructor().killEngine();
        // release semaphores -> all backend and frontend actions are flushed since now
        frontEndSemaphore.release();
        backEndSemaphore.release();
    }
    
    private void stopHere(final RubyDebugEvent suspensionEvent) {
        rubySession.suspend(suspensionEvent.getRubyThread(), contextProvider);
        setEnabled(ActionsManager.ACTION_STEP_OUT, rubySession.getFrames().length != 1);
    }

}
