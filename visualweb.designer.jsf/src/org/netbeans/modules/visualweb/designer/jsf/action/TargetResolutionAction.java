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


package org.netbeans.modules.visualweb.designer.jsf.action;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.AbstractAction;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.visualweb.designer.jsf.JsfDesignerPreferences;
import org.openide.awt.Actions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;

/**
 * Action providing target resolutions.
 *
 *
 * @author Peter Zavadsky
 * @author Tor Norbye (old functionality implementation -> performActionAt impl)
 */
public class TargetResolutionAction extends AbstractAction implements Presenter.Menu, Presenter.Popup, Presenter.Toolbar {

    /** Creates a new instance of TargetResolutionAction. */
    public TargetResolutionAction() {
        putValue(NAME, getDisplayName());
    }

    private String getDisplayName() {
        return NbBundle.getMessage(TargetResolutionAction.class, "LBL_TargetResolutionAction");
    }

//    private String getIconBase(Element[] componentRootElements) {
//        return null;
//    }

//    private boolean isEnabled() {
//        return componentRootElements.length > 0;
//    }

//    private void performAction() {
//        // XXX Strange impl of the Actions.SubMenu(action, model, isPopup). If the model provides one item,
//        // it doesn't call the performAt(0), but this method.
////        new ResolutionMenuModel(designBeans).performActionAt(0);
//        RESOLUTIONS[0].performAction();
//    }

    public void actionPerformed(ActionEvent e) {
        RESOLUTIONS[0].performAction();
    }
    
    public JMenuItem getMenuPresenter() {
        return new Actions.SubMenu(this, new ResolutionMenuModel(), false);
    }

    public JMenuItem getPopupPresenter() {
        return new Actions.SubMenu(this, new ResolutionMenuModel(), true);
    }

    public Component getToolbarPresenter() {
        return new TargetResolutionComboBox(this);
    }


    private static class TargetResolutionComboBox extends JComboBox
    implements PropertyChangeListener, PreferenceChangeListener {
        private final Action contextAwareAction;

        public TargetResolutionComboBox(Action contextAwareAction) {
            super(RESOLUTIONS);

            this.contextAwareAction = contextAwareAction;

            // XXX Do better than annonymous class.
            setRenderer(new DefaultListCellRenderer() {
                public Component getListCellRendererComponent(
                    JList list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus)
                {
                    if (value instanceof Resolution) {
                        value = ((Resolution)value).getDisplayName();
                    }
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            });

            contextAwareAction.addPropertyChangeListener(WeakListeners.propertyChange(this, contextAwareAction));
            // #6457867.
//            DesignerSettings.getInstance().addPropertyChangeListener(WeakListeners.propertyChange(this, DesignerSettings.getInstance()));
//            DesignerSettings.getInstance().addWeakPreferenceChangeListener(this);
            JsfDesignerPreferences.getInstance().addWeakPreferenceChangeListener(this);
            
            setSelectedItemForComboBox(this);

            // XXX Do better than annonymous class.
            addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    Resolution resolution = (Resolution)getSelectedItem();
                    if (resolution != null) {
                        resolution.performAction();
                    }
                }
            });

            // XXX Copied from former TargetSizeCombo.
            // Set fixed width to prevent excessive horizontal expansion
            // etc.  Similar to hack I saw in NetBeans NavigationView code
            // where they're putting dropdowns in the toolbar too
            Dimension prefSize = getPreferredSize();
            setPreferredSize(prefSize);
            setMinimumSize(prefSize);
            setMaximumSize(prefSize);
        }

        public void propertyChange(PropertyChangeEvent evt) {
//            if (PROP_ELEMENTS.equals(evt.getPropertyName())) {
////                DesignBean[] designBeans = (DesignBean[])evt.getNewValue();
////                this.designBeans = designBeans;
//                setSelectedItemForComboBox(this);
//            } else 
//            if (DesignerSettings.PROP_PAGE_SIZE.equals(evt.getPropertyName())) {
//                // #6457867 If the change originated from other source, update the combo selection.
//                setSelectedItemForComboBox(this);
//            }
        }
        
        private static void setSelectedItemForComboBox(JComboBox combo) {
            int type = JsfDesignerPreferences.getInstance().getPageSize();
            Object selected = null;
            for (int i = 0; i < RESOLUTIONS.length; i++) {
                if (type == RESOLUTIONS[i].getType()) {
                    selected = RESOLUTIONS[i];
                    break;
                }
            }
            combo.setSelectedItem(selected);
        }

        public void preferenceChange(PreferenceChangeEvent evt) {
            if (JsfDesignerPreferences.PROP_PAGE_SIZE.equals(evt.getKey())) {
                // #6457867 If the change originated from other source, update the combo selection.
                setSelectedItemForComboBox(this);
            }
        }
    }


    // XXX Make an enum once moved to jdk5.0 sources.
    private static class Resolution {
        private final int type;
        private final String displayName;
        public Resolution(int type, String displayName) {
            this.type = type;
            this.displayName = displayName;
        }

        public int getType() {
            return type;
        }
        
        public String getDisplayName() {
            return displayName;
        }

        public void performAction() {
            setResolution(type);
        }
    } // End of Resolution class.

    private static final Resolution RESOLUTION_NONE = new Resolution(
            JsfDesignerPreferences.CONSTRAINTS_NONE, NbBundle.getMessage(TargetResolutionAction.class, "LBL_ResolutionNone"));
    private static final Resolution RESOLUTION_640x480 = new Resolution(
            JsfDesignerPreferences.CONSTRAINTS_640x480, NbBundle.getMessage(TargetResolutionAction.class, "LBL_Resolution640x480"));
    private static final Resolution RESOLUTION_800x600 = new Resolution(
            JsfDesignerPreferences.CONSTRAINTS_800x600, NbBundle.getMessage(TargetResolutionAction.class, "LBL_Resolution800x600"));
    private static final Resolution RESOLUTION_1024x768 = new Resolution(
            JsfDesignerPreferences.CONSTRAINTS_1024x768, NbBundle.getMessage(TargetResolutionAction.class, "LBL_Resolution1024x768"));
    private static final Resolution RESOLUTION_1280x1024 = new Resolution(
            JsfDesignerPreferences.CONSTRAINTS_1280x1024, NbBundle.getMessage(TargetResolutionAction.class, "LBL_Resolution1280x1024"));

    private static final Resolution[] RESOLUTIONS = new Resolution[] {
        RESOLUTION_NONE, RESOLUTION_640x480, RESOLUTION_800x600, RESOLUTION_1024x768, RESOLUTION_1280x1024};

    
    /** Implementation of the actions submenu model. */
    private static class ResolutionMenuModel implements Actions.SubMenuModel {
        
        
        public ResolutionMenuModel() {
        }
        
        
        public int getCount() {
            return RESOLUTIONS.length;
        }

        public String getLabel(int i) {
            return RESOLUTIONS[i].getDisplayName();
        }

        public HelpCtx getHelpCtx(int i) {
            // XXX Implement?
            return null;
        }

        public void performActionAt(int i) {
            RESOLUTIONS[i].performAction();
        }

        public void addChangeListener(ChangeListener changeListener) {
            // dummy, this model is not mutable.
        }

        public void removeChangeListener(ChangeListener changeListener) {
            // dummy, this model is not mutable.
        }
        
    } // End of ResolutionMenuModel.
    

    // XXX Copied from before DesignerActions
    /** Changes the target resolution. */
    private static void setResolution(int type) {
        if (type != JsfDesignerPreferences.getInstance().getPageSize()) {
            JsfDesignerPreferences.getInstance().setPageSize(type);
        }
    }

}
