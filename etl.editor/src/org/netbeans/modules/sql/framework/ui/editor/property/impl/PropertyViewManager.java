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
package org.netbeans.modules.sql.framework.ui.editor.property.impl;

import java.awt.Dialog;
import java.beans.PropertyEditorManager;
import java.io.InputStream;
import java.util.Map;

import org.netbeans.modules.sql.framework.ui.editor.property.IPropertySheet;
import org.netbeans.modules.sql.framework.ui.editor.property.IResource;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.nodes.Node;


/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class PropertyViewManager {

    static {
        // Ensure string properties are edited without
        // the default multiline custom editor.
        PropertyEditorManager.registerEditor(String.class, DefaultPropertyEditor.SingleLineTextEditor.class);
    }

    private TemplateManager tMgr;

    /** Creates a new instance of PropertyViewManager */
    public PropertyViewManager(InputStream in, IResource resource) {
        tMgr = new TemplateManager(in, resource);
    }

    /**
     * get a property node for a given template name
     * 
     * @return node
     */
    public PropertyNode getNodeForTemplateName(String templateName) {
        return tMgr.getNodeForTemplateName(templateName);
    }

    /**
     * get IPropertySheet which has gui property panels
     * 
     * @param map map of property name as key and property value as value for which a
     *        property sheet needs to be created
     * @param customizerMap map of property name as key and IPropertyCustomizer as value
     * @param templateName name of the template in xml descriptor
     */
    public IPropertySheet getPropertySheet(Map map, Map customizerMap, String templateName) {
        PropertyNode pNode = tMgr.getNodeForTemplateName(templateName);
        if (pNode == null) {
            throw new IllegalArgumentException("Can not load template for template " + templateName);
        }
        PropUtil.setInitialPropertyValues(map, customizerMap, pNode);
        BasicPropertySheet sheet = new BasicPropertySheet(pNode);
        return sheet;
    }

    /**
     * get IPropertySheet which has gui property panels
     * 
     * @param map map of property name as key and property value as value for which a
     *        property sheet needs to be created
     * @param templateName name of the template in xml descriptor
     */
    public IPropertySheet getPropertySheet(Map map, String templateName) {
        return getPropertySheet(map, null, templateName);
    }

    /**
     * get IPropertySheet which has gui property panels
     * 
     * @param bean bean to whose properties are reflected and a property sheet is created
     * @param customizerMap map of property name as key and IPropertyCustomizer as value
     * @param templateName name of the template in xml descriptor
     */
    public IPropertySheet getPropertySheet(Object bean, Map customizerMap, String templateName) {
        return getPropertySheet(bean, templateName);
    }

    /**
     * get IPropertySheet which has gui property panels
     * 
     * @param bean bean to whose properties are reflected and a property sheet is created
     * @param templateName name of the template in xml descriptor
     */
    public IPropertySheet getPropertySheet(Object bean, String templateName) {
        PropertyNode pNode = tMgr.getNodeForTemplateName(templateName);
        if (pNode == null) {
            throw new IllegalArgumentException("Can not load template for template " + templateName);
        }
        PropUtil.setInitialPropertyValues(bean, null, pNode);
        BasicPropertySheet sheet = new BasicPropertySheet(bean, pNode);
        return sheet;
    }

    /**
     * show a property sheet dialog with ok and cancel button which reflect on given bean
     * for property values and set property value on the given bean Property will be
     * sorted by name. This the only diff between this and showNBDialog method
     * 
     * @param bean bean to whose properties are reflected and a property sheet is created
     * @param templateName name of the template in xml descriptor
     * @param isModal if true the sheet dialog will be modal
     */
    public void showDialog(Object bean, String templateName, boolean isModal) {
        PropertyNode pNode = tMgr.getNodeForTemplateName(templateName);
        if (pNode == null) {
            throw new IllegalArgumentException("Cannot load template for template " + templateName);
        }

        PropertySheet pSheet = new PropertySheet();
        PropUtil.setInitialPropertyValues(bean, null, pNode);
        // remove the tool bar from netbeans property dialog
        pSheet.add(new javax.swing.JLabel(""), java.awt.BorderLayout.NORTH);
        pSheet.setNodes(new Node[] { pNode});
        launchDialog(bean, pNode, pSheet, isModal);
    }

    /**
     * show a property sheet dialog with ok and cancel button which reflect on given bean
     * for property values and set property value on the given bean
     * 
     * @param bean bean to whose properties are reflected and a property sheet is created
     * @param customizerMap map of property name as key and IPropertyCustomizer as value
     * @param templateName name of the template in xml descriptor
     * @param isModal if true the sheet dialog will be modal
     */
    public void showNBDialog(Object bean, Map customizerMap, String templateName, boolean isModal) {
        PropertyNode pNode = tMgr.getNodeForTemplateName(templateName);
        if (pNode == null) {
            throw new IllegalArgumentException("Can not load template for template " + templateName);
        }

        PropertySheet pSheet = new PropertySheet();
        // do not show tab view if there is only one tab
        pSheet.putClientProperty("TabPolicy", "HideWhenAlone");
        PropUtil.setInitialPropertyValues(bean, customizerMap, pNode);
        // set sorting mode to unsorted so that we get properties sorted as defined by
        // position attribute in xml
        try {
            pSheet.setSortingMode(PropertySheet.UNSORTED);
        } catch (Exception ex) {
        }

        pSheet.setNodes(new Node[] { pNode});
        launchDialog(bean, pNode, pSheet, isModal);
    }

    /**
     * show a property sheet dialog with ok and cancel button which reflect on given bean
     * for property values and set property value on the given bean
     * 
     * @param bean bean to whose properties are reflected and a property sheet is created
     * @param templateName name of the template in xml descriptor
     * @param isModal if true the sheet dialog will be modal
     */
    public void showNBDialog(Object bean, String templateName, boolean isModal) {
        showNBDialog(bean, null, templateName, isModal);
    }

    private void launchDialog(Object bean, PropertyNode pNode, PropertySheet innerPane, boolean isModal) {
        DialogDescriptor dd = new DialogDescriptor(innerPane, "Properties", isModal, null);
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
        dlg.pack();
        dlg.setSize(350, 400);

        dlg.setVisible(true);

        // User clicked ok so we need to save the information
        if (dd.getValue() == NotifyDescriptor.OK_OPTION) {
            PropUtil.setModifiedPropertyValues(bean, pNode);
        }
    }
}

