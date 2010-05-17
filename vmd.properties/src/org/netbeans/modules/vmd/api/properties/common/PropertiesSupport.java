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

package org.netbeans.modules.vmd.api.properties.common;

import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.properties.DesignPropertyEditor;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyPanel;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Node.PropertySet;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;

import java.lang.reflect.InvocationTargetException;
import org.netbeans.modules.vmd.api.io.ActiveViewSupport;
import org.netbeans.modules.vmd.api.io.DataEditorView;
import org.netbeans.modules.vmd.properties.PropertiesNodesManager;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Karol Harezlak
 */
/**
 * This class contains satic support methods to help maintaince Visual Designer properties shown
 * in the Properties Window.
 */
public final class PropertiesSupport {
    /**
     * DataEditorView Tag. This tag hides Netbeans Property Window
     */ 
    public static final String DO_NOT_OPEN_PROPERTIES_WINDOW_TAG = "DO_NOT_OPEN_PROPERTIES_WINDOW"; //NOI18N
    
    private PropertiesSupport() {
    }
    /**
     * Returns propertie's Sheet object for given view and DesignComponent.
     * @param view DataEditor view for searching properties Sheet
     * @param component given DesignComponent
     * @return instance of properties Sheet
     */
    public static Sheet getSheet(DataEditorView view, DesignComponent component) {
        return PropertiesNodesManager.getInstance(view).getSheet(component);
    }
    
    /**
     * This method connects DataEditorView with InstnceContect. It means that Instance contect with 
     * the set of Nodes (PropertiesNode) can be share throug DataEditorViews (TopComponents).
     * This way developer can share the same InstanteContent (set of Nodes) through more that one DataEditorView.
     * @param view instance of DataEditorView to connect with given InstanceContent
     * @param ic InstanceContent to connect with given DataEditorView
     */
    public static void addInstanceContent(DataEditorView view, InstanceContent ic) {
        PropertiesNodesManager.getInstance(view).add(ic);
    }
    
    /**
     * It shows custom property editor window for given DesignComponent and the name of the property.
     * NOTE: Multi selection is not supported.
     * WARNING: Do NOT invoke this method from inside of the read transaction.
     * @param component instance of DesignComponent 
     * @param propertyName property name of the property which contains custom porperty editor to show 
     */
    public synchronized static void showCustomPropertyEditor(DesignComponent component, String propertyName) {
        boolean propertyEditorExists = false;
        if (component.getDocument().getTransactionManager().isWriteAccess())
            Debug.warning("Calling PropertiesSupport.showPropertyEditorForCurrentComponent form write transaction may generate problems"); //NOI18N
        DataEditorView view = ActiveViewSupport.getDefault().getActiveView();
        assert (view != null);
        Sheet sheet = PropertiesNodesManager.getInstance(view).getSheet(component);
        for (PropertySet propertySet : sheet.toArray()) {
            for (Property property : propertySet.getProperties()) {
                if(propertyName.equals(property.getName())) {
                    PropertyPanel propertyPanel = new PropertyPanel(property, PropertyPanel.PREF_CUSTOM_EDITOR);
                    propertyEditorExists = true;
                    propertyPanel.setChangeImmediate(false);
                    DialogDescriptor dd = new DialogDescriptor(propertyPanel, property.getDisplayName(), true, null); // NOI18N
                    Object helpID = property.getValue(ExPropertyEditor.PROPERTY_HELP_ID);
                    if (helpID != null) {
                        assert helpID instanceof String;
                        HelpCtx helpCtx = new HelpCtx((String)helpID);
                        dd.setHelpCtx(helpCtx);
                    }
                    property.getPropertyEditor().getCustomEditor(); // initialization of CustomEditor, issue #113195
                    Object res = DialogDisplayer.getDefault().notify(dd);
                    
                    if (res == DialogDescriptor.OK_OPTION) {
                        ((DesignPropertyEditor) property.getPropertyEditor()).customEditorOKButtonPressed();
                        try {
                            property.setValue(property.getPropertyEditor().getValue());
                        } catch (IllegalAccessException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (IllegalArgumentException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (InvocationTargetException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                    return;
                }
            }
        }
        if (!propertyEditorExists) {
            throw new IllegalArgumentException("PropertyEditor for " + propertyName + " not found in the component " + component); //NOI18N
        }
    }
    
}
