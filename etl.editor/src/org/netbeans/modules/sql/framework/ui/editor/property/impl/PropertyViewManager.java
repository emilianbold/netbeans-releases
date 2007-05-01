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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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

