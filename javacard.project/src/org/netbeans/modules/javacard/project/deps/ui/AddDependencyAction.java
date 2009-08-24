/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javacard.project.deps.ui;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.io.IOException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.javacard.GuiUtils;
import org.netbeans.modules.javacard.project.JCProject;
import org.netbeans.modules.javacard.project.deps.DependenciesProvider;
import org.netbeans.modules.javacard.project.deps.ResolvedDependencies;
import org.netbeans.spi.actions.Single;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Tim Boudreau
 */
public class AddDependencyAction extends Single <JCProject> {

    public AddDependencyAction() {
        super (JCProject.class, NbBundle.getMessage(AddDependencyAction.class, 
                "ACTION_ADD_DEPENDENCY"), null); //NOI18N
    }

    @Override
    protected void actionPerformed(JCProject target) {
        DependenciesProvider prov = target.getLookup().lookup(DependenciesProvider.class);
        ProgressHandle handle = ProgressHandleFactory.createHandle(
                NbBundle.getMessage(AddDependencyAction.class,
                "MSG_LOADING_DEPS")); //NOI18N
        WaitRunnable wr = new WaitRunnable(target, prov, handle);
        GuiUtils.showProgressDialogAndRun(handle, wr, false);
    }

    @Override
    protected boolean isEnabled(JCProject target) {
        return target.getLookup().lookup(DependenciesProvider.class) != null;
    }

    private static class WaitRunnable implements Runnable {
        private Cancellable c;
        private final DependenciesProvider prov;
        private final ProgressHandle handle;
        private final JCProject project;
        WaitRunnable (JCProject project, DependenciesProvider prov, ProgressHandle handle) {
            this.project = project;
            this.prov = prov;
            this.handle = handle;
        }

        public void run() {
            handle.start();
            handle.switchToIndeterminate();
            try {
                R r = new R(project);
                c = prov.requestDependencies(r);
                if (c != null) {
                    synchronized (c) {
                        try {
                            while (!r.done) {
                                c.wait();
                            }
                        } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            } finally {
                handle.finish();
            }
        }

    }

    private static class R implements DependenciesProvider.Receiver, Runnable {
        private volatile ResolvedDependencies deps;
        private volatile boolean done;
        private final JCProject project;

        private R(JCProject project) {
            this.project = project;
        }
        public void receive(ResolvedDependencies deps) {
            this.deps = deps;
            done = true;
            if (deps != null) {
                EventQueue.invokeLater(this);
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        }

        public boolean failed(Throwable failure) {
            //XXX handle w/ message
            done = true;
            return false;
        }

        public void run() {
            AddDependencyWizardIterator.show(deps, project);
            if (deps.isModified()) {
                try {
                    deps.save();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
}
