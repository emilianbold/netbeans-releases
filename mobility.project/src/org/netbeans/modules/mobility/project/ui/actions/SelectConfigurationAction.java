/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.mobility.project.ui.actions;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.netbeans.spi.project.ProjectConfiguration;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

public final class SelectConfigurationAction extends AbstractAction implements ContextAwareAction, Presenter.Popup {

    private final Lookup context;

    private SelectConfigurationAction() {
        this(Utilities.actionsGlobalContext());
        putValue(NAME, NbBundle.getMessage(SelectConfigurationAction.class, "LBL_SelConfigurationAction")); //NO18N
    }

    private SelectConfigurationAction(Lookup context) {
        super();
        this.context = context;
    }

    public static Action getStaticInstance() {
        return new SelectConfigurationAction();
    }

    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Should never be called"); //NO18N
    }

    public Action createContextAwareInstance(Lookup actionContext) {
        return new SelectConfigurationAction(actionContext);
    }

    @Override
    public boolean isEnabled() {
        return context.lookupItem(new Lookup.Template(J2MEProject.class)) != null;
    }

    public JMenuItem getPopupPresenter() {
        J2MEProject project = context.lookup(J2MEProject.class);
        assert project != null;
        ProjectConfigurationsHelper helper = project.getConfigurationHelper();
        JMenu result = new JMenu(NbBundle.getMessage(SelectConfigurationAction.class, "LBL_SelConfigurationAction")); //NO18N
        ProjectConfiguration active = helper.getActiveConfiguration();
        for (ProjectConfiguration c : helper.getConfigurations()) {
            OneConfigurationAction cfgAction = new OneConfigurationAction(c);
            JMenuItem item = new JMenuItem(cfgAction);
            if (active.equals(c)) {
                Font f = item.getFont();
                f = f.deriveFont(Font.BOLD);
                item.setFont(f);
            }
            result.add(item);
        }
        return result;
    }

    private final class OneConfigurationAction extends AbstractAction {

        private final ProjectConfiguration config;

        OneConfigurationAction(ProjectConfiguration config) {
            super();
            putValue(NAME, config.getDisplayName());
            this.config = config;
        }

        public void actionPerformed(ActionEvent e) {
            J2MEProject project = context.lookup(J2MEProject.class);
            assert project != null;
            try {
                project.getConfigurationHelper().setActiveConfiguration(config);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
