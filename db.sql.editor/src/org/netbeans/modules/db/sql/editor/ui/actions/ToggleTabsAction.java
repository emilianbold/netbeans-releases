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

package org.netbeans.modules.db.sql.editor.ui.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.ButtonModel;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.db.api.sql.execute.SQLExecuteOptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.BooleanStateAction;

/**
 * This action lets you toggle between creating a new tab for each execution
 * or reusing the same tab each time.
 *
 * @author David Van Couvering
 */
public class ToggleTabsAction extends BooleanStateAction {
    private static final Logger LOGGER = Logger.getLogger(ToggleTabsAction.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);

    private static final String TOGGLE_TAB_ICON_PATH = "org/netbeans/modules/db/sql/editor/resources/toggleremovetabs.png"; // NOI18N

    public ToggleTabsAction() {
        putValue(Action.SMALL_ICON, new ImageIcon(Utilities.loadImage(TOGGLE_TAB_ICON_PATH)));
        putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(ToggleTabsAction.class, "LBL_ToggleRemoveTabsActionDisplayName")); // NOI18N
    }

    @Override
    public boolean getBooleanState() {
        return SQLExecuteOptions.getDefault().isKeepOldResultTabs();
    }

    @Override
    public void setBooleanState(boolean value) {
        super.setBooleanState(value);
        SQLExecuteOptions.getDefault().setKeepOldResultTabs(value);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ToggleTabsAction.class);
    }


    @Override
    public void actionPerformed(ActionEvent ev) {
        boolean keepOldTabs = SQLExecuteOptions.getDefault().isKeepOldResultTabs();
        SQLExecuteOptions.getDefault().setKeepOldResultTabs(!keepOldTabs);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ToggleTabsAction.class, "LBL_ToggleRemoveTabsActionName");
    }
    
    @Override
     public Component getToolbarPresenter() {
        JToggleButton b = new ToggleTabsButton();
        b.setModel(new HighlightButtonModel());
        b.putClientProperty("hideActionText", Boolean.TRUE); //NOI18N
        b.setAction(this);

        return b;
    }

    private static final class HighlightButtonModel extends JToggleButton.ToggleButtonModel implements PropertyChangeListener {

        public HighlightButtonModel() {
            SQLExecuteOptions options = SQLExecuteOptions.getDefault();
            options.addPropertyChangeListener(WeakListeners.propertyChange(this, options));
            propertyChange(null);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            SQLExecuteOptions options = SQLExecuteOptions.getDefault();
            if (evt == null || evt.getPropertyName() == null || evt.getPropertyName().equals(SQLExecuteOptions.PROP_KEEP_OLD_TABS)) {
                setSelected(options.isKeepOldResultTabs());
            }
        }
    } // End of HighlightButtonModel class

    private static final class ToggleTabsButton extends JToggleButton implements ChangeListener {

        public ToggleTabsButton() {

        }

        @Override
        public void setModel(ButtonModel model) {
            ButtonModel oldModel = getModel();
            if (oldModel != null) {
                oldModel.removeChangeListener(this);
            }

            super.setModel(model);

            ButtonModel newModel = getModel();
            if (newModel != null) {
                newModel.addChangeListener(this);
            }

            stateChanged(null);
        }

        public void stateChanged(ChangeEvent evt) {
            boolean selected = isSelected();
            super.setContentAreaFilled(selected);
            super.setBorderPainted(selected);
        }

        @Override
        public void setBorderPainted(boolean arg0) {
            if (!isSelected()) {
                super.setBorderPainted(arg0);
            }
        }

        @Override
        public void setContentAreaFilled(boolean arg0) {
            if (!isSelected()) {
                super.setContentAreaFilled(arg0);
            }
        }
    }
}
