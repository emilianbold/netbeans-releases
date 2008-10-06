/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.projectimport.eclipse.core;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import org.netbeans.api.project.Project;
import org.openide.awt.Actions;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.Mnemonics;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

public final class UpdateProjectAction extends AbstractAction implements ContextAwareAction, Presenter.Popup {
    
    private Lookup context;
    private UpgradableProject upgradable;
    private boolean initialized;
    
    public UpdateProjectAction() {
        this(null);
    }

    public UpdateProjectAction(Lookup actionContext) {
        super(NbBundle.getMessage(UpdateProjectAction.class, "UpdateProjectAction.Name"));
        this.context = actionContext;
    }

    public void actionPerformed(ActionEvent ignore) {
        new UpdateAllProjects().update(false);
    }

    @Override
    public boolean isEnabled() {
        assert context != null;
        return getUpgradableProject() != null && getUpgradableProject().isUpgradable();
    }
    
    private UpgradableProject getUpgradableProject() {
        if (!initialized) {
            Project p = context.lookup(Project.class);
            if (p != null) {
                upgradable = p.getLookup().lookup(UpgradableProject.class);
            }
            initialized = true;
        }
        return upgradable;
    }
    
    
    public Action createContextAwareInstance(Lookup actionContext) {
        return new UpdateProjectAction(actionContext);
    }

    public JMenuItem getPopupPresenter() {
        return new Menu();
    }

    private class Menu extends JMenuItem implements DynamicMenuContent {

        public Menu() {
            Actions.connect(this, UpdateProjectAction.this);
            Mnemonics.setLocalizedText(this, (String) getValue(NAME));
        }

        public JComponent[] getMenuPresenters() {
            if (UpdateProjectAction.this.isEnabled()) {
                return new JComponent[] {this};
            } else {
                return new JComponent[0];
            }
        }

        public JComponent[] synchMenuPresenters(JComponent[] items) {
            return getMenuPresenters();
        }

    }

}
