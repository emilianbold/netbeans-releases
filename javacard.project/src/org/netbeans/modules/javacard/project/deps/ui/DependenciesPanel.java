/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

package org.netbeans.modules.javacard.project.deps.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.javacard.project.JCProjectProperties;
import org.netbeans.modules.javacard.project.deps.DependenciesProvider;
import org.netbeans.modules.javacard.project.deps.ResolvedDependencies;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;

/**
 *
 * @author Tim Boudreau
 */
public class DependenciesPanel extends JPanel {
    private final JCProjectProperties props;
    private ResolvedDependencies deps;
    private final Object lock = new Object();
    private Cancellable cancel;
    public DependenciesPanel (JCProjectProperties props) {
        this.props = props;
        setLayout (new BorderLayout());
        JLabel lbl = new JLabel (NbBundle.getMessage(DependenciesPanel.class, "MSG_LOADING")); //NOI18N
        add (lbl, BorderLayout.CENTER);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (deps == null) {
            synchronized (lock) {
                cancel = props.getDependencies(new R());
            }
        }
    }

    @Override
    public void removeNotify() {
        synchronized(lock) {
            if (cancel != null) {
                cancel.cancel();
                cancel = null;
            }
        }
        super.removeNotify();
    }

    void setDependencies (ResolvedDependencies deps) {
        synchronized (lock) {
            this.deps = deps;
            if (cancel == null) {
                //Already removed, don't do any work
                return;
            }
            cancel = null;
        }
        if (deps != null) {
            removeAll();
            add (new DependenciesEditorPanel(props.getProject(), deps), BorderLayout.CENTER);
        } else {
            removeAll();
            add (new JLabel(NbBundle.getMessage(DependenciesPanel.class, "MSG_LOAD_FAILED"))); //NOI18N
        }
        invalidate();
        revalidate();
        repaint();
    }

    ResolvedDependencies getDependencies() {
        synchronized (lock) {
            return deps;
        }
    }

    private class R implements DependenciesProvider.Receiver, Runnable {
        private volatile ResolvedDependencies deps;
        public void receive(ResolvedDependencies deps) {
            this.deps = deps;
            EventQueue.invokeLater(this);
        }

        public boolean failed(Throwable failure) {
            setDependencies(null);
            return true;
        }

        public void run() {
            setDependencies(deps);
        }

    }
}
