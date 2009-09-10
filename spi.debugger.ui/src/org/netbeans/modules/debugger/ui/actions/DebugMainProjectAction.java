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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.debugger.ui.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.MainProjectSensitiveActions;
import org.openide.awt.Actions;
import org.openide.awt.DropDownButtonFactory;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Martin Entlicher
 */
public class DebugMainProjectAction implements Action, Presenter.Toolbar {
    
    private Action delegate;
    
    /** Creates a new instance of DebugMainProjectAction */
    public DebugMainProjectAction() {
        delegate = MainProjectSensitiveActions.mainProjectCommandAction(
                ActionProvider.COMMAND_DEBUG,
                NbBundle.getMessage(DebugMainProjectAction.class, "LBL_DebugMainProjectAction_Name" ),ImageUtilities.loadImageIcon("org/netbeans/modules/debugger/resources/debugProject.png", false)); // NOI18N
        delegate.putValue("iconBase","org/netbeans/modules/debugger/resources/debugProject.png"); //NOI18N
    }
    
    public Object getValue(String arg0) {
        return delegate.getValue(arg0);
    }

    public void putValue(String arg0, Object arg1) {
        delegate.putValue(arg0, arg1);
    }

    public void setEnabled(boolean arg0) {
        delegate.setEnabled(arg0);
    }

    public boolean isEnabled() {
        return delegate.isEnabled();
    }

    public void addPropertyChangeListener(PropertyChangeListener arg0) {
        delegate.addPropertyChangeListener(arg0);
    }

    public void removePropertyChangeListener(PropertyChangeListener arg0) {
        delegate.removePropertyChangeListener(arg0);
    }

    public void actionPerformed(ActionEvent arg0) {
        Project p = OpenProjects.getDefault().getMainProject();
        GestureSubmitter.logDebugProject(p);
        delegate.actionPerformed(arg0);
    }

    public Component getToolbarPresenter() {
        JPopupMenu menu = new JPopupMenu();
        JButton button = DropDownButtonFactory.createDropDownButton(
                new ImageIcon(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)), menu);
        final JMenuItem item = new JMenuItem(Actions.cutAmpersand((String) delegate.getValue("menuText")));
        item.setEnabled(delegate.isEnabled());

        delegate.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                String propName = evt.getPropertyName();
                if ("enabled".equals(propName)) {
                    item.setEnabled((Boolean)evt.getNewValue());
                } else if ("menuText".equals(propName)) {
                    item.setText(Actions.cutAmpersand((String) evt.getNewValue()));
                }
            }
        });

        menu.add(item);
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DebugMainProjectAction.this.actionPerformed(e);
            }
        });
        try {
            FileObject fo = FileUtil.getConfigFile("Actions/Debug/org-netbeans-modules-debugger-ui-actions-ConnectAction.instance");
            DataObject obj = DataObject.find(fo);
            InstanceCookie ic = obj.getLookup().lookup(InstanceCookie.class);
            ConnectAction ca = (ConnectAction) ic.instanceCreate();
            JMenuItem item2 = new JMenuItem(Actions.cutAmpersand((String) ca.getValue(NAME)));
            Actions.connect(item2, ca);
            menu.add(item2);
        } catch (Exception nsee) {
            Exceptions.printStackTrace(nsee);
        }
        Actions.connect(button, this);
        return button;
    }

}
