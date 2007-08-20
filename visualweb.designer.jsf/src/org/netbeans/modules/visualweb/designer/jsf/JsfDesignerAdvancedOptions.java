/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.designer.jsf;

import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import org.netbeans.modules.visualweb.dataconnectivity.DataconnectivitySettings;

import org.netbeans.spi.options.AdvancedOption;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;


/**
 * @author Pavel Buzek
 */
public class JsfDesignerAdvancedOptions extends AdvancedOption {
    public String getTooltip() {
        return getDisplayName();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(JsfDesignerAdvancedOptions.class, "LBL_OptionsPanelName");
    }

    public OptionsPanelController create() {
        return new Controller();
    }

    private static final class Controller extends OptionsPanelController {
        private JsfDesignerAdvancedOptionsPanel component;

        public JComponent getComponent(Lookup masterLookup) {
            if (component == null) {
                component = new JsfDesignerAdvancedOptionsPanel();
            }

            return component;
        }

        public void removePropertyChangeListener(PropertyChangeListener l) {
        }

        public void addPropertyChangeListener(PropertyChangeListener l) {
        }

        public void update() {
            Mutex.EVENT.readAccess(new Runnable() {
                    public void run() {
                        JsfDesignerPreferences designer = JsfDesignerPreferences.getInstance();
                        component.setDefaultFontSize(designer.getDefaultFontSize());
                        component.setShowGrid(designer.getGridShow());
                        component.setSnapToGrid(designer.getGridSnap());
                        component.setGridHeight(designer.getGridHeight());
                        component.setGridWidth(designer.getGridWidth());
                        component.setResolution(designer.getPageSize());
                        
                        DataconnectivitySettings dataConnectivity = DataconnectivitySettings.getInstance();
                        component.setDataProviderSuffix(dataConnectivity.getDataProviderSuffixProp());
                        component.setRowsetSuffix(dataConnectivity.getRowSetSuffixProp());
                        component.setRowsetDuplicate(dataConnectivity.getCheckRowSetProp());
                        component.setRowsetInSession(dataConnectivity.getMakeInSession());
                        component.setPromptForName(dataConnectivity.getPromptForName());
                    }
                });
        }

        public boolean isValid() {
            return true;
        }

        public boolean isChanged() {
            return false;
        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.findHelp("projrave_ui_elements_options_visual_editor"); // NOI18N
        }

        public void cancel() {
        }

        public void applyChanges() {
            Mutex.EVENT.readAccess(new Runnable() {
                    public void run() {
                        JsfDesignerPreferences designer = JsfDesignerPreferences.getInstance();
                        designer.setDefaultFontSize(component.getDefaultFontSize());
                        designer.setGridShow(component.isShowGrid());
                        designer.setGridSnap(component.isSnapToGrid());
                        designer.setGridHeight(component.getGridHeight());
                        designer.setGridWidth(component.getGridWidth());
                        designer.setPageSize(component.getResolution());
                        
                        DataconnectivitySettings dataConnectivity = DataconnectivitySettings.getInstance();
                        dataConnectivity.setDataProviderSuffixProp(component.getDataProviderSuffix());
                        dataConnectivity.setRowSetSuffixProp(component.getRowsetSuffix());
                        dataConnectivity.setCheckRowSetProp(component.isRowsetDuplicate());
                        dataConnectivity.setMakeInSession(component.isRowsetInSession());
                        dataConnectivity.setPromptForName(component.isPromptForName());
                    }
                });
        }
    }
}
