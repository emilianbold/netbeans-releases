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

package org.netbeans.modules.cnd.discovery.projectimport;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.SharedClassObject;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Alexander Simon
 */
public class ReconfigureAction extends NodeAction {

    private final static boolean TEST_ACTION = true;

    private JMenuItem presenter;
    private boolean inited = false;

    public ReconfigureAction() {
    }

    public static Action getReconfigureAction() {
        return SharedClassObject.findObject(ReconfigureAction.class, true);
    }

    private static boolean running = false;

    @Override
    public String getName() {
        return NbBundle.getMessage(getClass(), "CTL_ReconfigureAction"); //NOI18N
    }

    @Override
    public JMenuItem getMenuPresenter() {
        return getPresenter();
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return getPresenter();
    }

    private JMenuItem getPresenter() {
        if (!inited) {
            presenter = new JMenuItem();
            org.openide.awt.Actions.connect(presenter, (Action) this, true);
            inited = true;
        }
        final Project p = getProject(getActivatedNodes());
        if (TEST_ACTION) {
            if (p == null) {
                setEnabled(!running);
                presenter.setVisible(false);
            } else {
                try {
                    presenter.setVisible(true);
                    setEnabled(!running);
                } catch (Throwable thr) {
                    thr.printStackTrace();
                    setEnabled(false);
                }
            }
        } else {
            presenter.setVisible(false);
        }
        return presenter;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean enable(Node[] activatedNodes) {
        Project p = getProject(activatedNodes);
        if (p == null) {
            return false;
        }
        ConfigurationDescriptorProvider pdp = p.getLookup().lookup(ConfigurationDescriptorProvider.class);
        if (pdp == null || !pdp.gotDescriptor()){
            return false;
        }
        MakeConfiguration configuration = pdp.getConfigurationDescriptor().getActiveConfiguration();
        if (configuration == null || configuration.getConfigurationType().getValue() !=  MakeConfiguration.TYPE_MAKEFILE){
            return false;
        }
        return true;
    }

    public void performAction(final Node[] activatedNodes) {
        running = true;
        Project p = getProject(activatedNodes);
        ReconfigureProject reconfigurator = new ReconfigureProject(p);
        String cFlags;
        String cxxFlags;
        if (reconfigurator.isSunCompiler()){
            cFlags = "-g"; // NOI18N
            cxxFlags = "-g"; // NOI18N
        } else {
            cFlags = "-g3 -gdwarf-2"; // NOI18N
            cxxFlags = "-g3 -gdwarf-2"; // NOI18N
        }
        ReconfigurePanel panel = new ReconfigurePanel(cFlags, cxxFlags);
        JButton runButton = new JButton(NbBundle.getMessage(getClass(), "ReconfigureButton")); // NOI18N
        runButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(getClass(), "ReconfigureButtonAD")); // NOI18N
        Object options[] =  new Object[]{runButton, DialogDescriptor.CANCEL_OPTION};
        DialogDescriptor dialogDescriptor = new DialogDescriptor(
                panel,
                NbBundle.getMessage(getClass(), "ReconfigureDialogTitle"), // NOI18N
                true,
                options,
                runButton,
                DialogDescriptor.BOTTOM_ALIGN,
                null,
                null);
        Object ret = DialogDisplayer.getDefault().notify(dialogDescriptor);
        if (ret == runButton) {
            reconfigurator.reconfigure(panel.getCFlags(), panel.getCppFlags());
        }
        running = false;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    protected Project getProject(Node[] nodes) {
        if (nodes.length != 1) {
            return null;
        }
        for (int i = 0; i < nodes.length; i++) {
            Project p = nodes[i].getLookup().lookup(Project.class);
            if (p != null) {
                return p;
            }
        }
        return null;
    }
}
