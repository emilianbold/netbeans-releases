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
            return new HelpCtx("projrave_ui_elements_options_visual_editor"); // NOI18N
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
