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

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.netbeans.modules.sql.framework.ui.editor.property.IProperty;
import org.netbeans.modules.sql.framework.ui.editor.property.IPropertyGroup;
import org.netbeans.modules.sql.framework.ui.editor.property.IPropertySheet;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.nodes.Node;


/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class BasicPropertySheet extends JPanel implements IPropertySheet {

    private Object bean;
    private PropertyNode pNode;

    /** Creates a new instance of PropertySheet */
    public BasicPropertySheet(Object bean, PropertyNode node) {
        this(node);
        this.bean = bean;
    }

    /** Creates a new instance of PropertySheet */
    public BasicPropertySheet(PropertyNode node) {
        this.pNode = node;
        init();
    }

    /**
     * commit the changes in property to the bean
     */
    public void commitChanges() {
        if (bean != null) {
            PropUtil.setModifiedPropertyValues(bean, pNode);
        }
    }

    /**
     * get a list of IProperty objects
     * 
     * @return list of IProperty objects
     */
    public List getProperties() {
        ArrayList list = new ArrayList();

        Node.PropertySet[] pSets = pNode.getPropertySets();
        for (int i = 0; i < pSets.length; i++) {
            Node.PropertySet pSet = pSets[i];
            Node.Property[] properties = pSet.getProperties();
            for (int j = 0; j < properties.length; j++) {
                Node.Property property = properties[j];
                IProperty p = (IProperty) property;
                list.add(p);
            }
        }
        return list;
    }

    /**
     * get a property group based on its name
     * 
     * @return property group
     */
    public IPropertyGroup getPropertyGroup(String groupName) {
        return pNode.getPropertyGroup(groupName);
    }

    /**
     * get thr ui component used for displaying properties
     * 
     * @return ui component
     */
    public Component getPropertySheet() {
        return this;
    }

    /**
     * get current property values
     * 
     * @return map of property name and values
     */
    public HashMap getPropertyValues() {
        return getPropertyValues(pNode);
    }

    /**
     * set the bean whose properties are reflected set initialze value from bean to noe
     * 
     * @param bean bean
     */
    public void setBean(Object bean) {
        this.bean = bean;
        PropUtil.setInitialPropertyValues(bean, null, pNode);
    }

    /**
     * set the bean whose properties are reflected. set the template node too.
     * 
     * @param bean bean
     * @param node xml desc node
     */
    public void setBean(Object bean, Node node) {
        this.pNode = (PropertyNode) node;
        // set initialze value from bean to noe
        setBean(bean);
        init();
    }

    private void addPropertyValues(HashMap map, Node.PropertySet pSet) {
        Node.Property[] properties = pSet.getProperties();
        for (int i = 0; i < properties.length; i++) {
            Node.Property property = properties[i];
            IProperty p = (IProperty) property;
            if (p.isReadOnly()) {
                continue;
            }

            try {
                map.put(property.getName(), property.getValue());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private HashMap getPropertyValues(PropertyNode pNode1) {
        HashMap map = new HashMap();
        Node.PropertySet[] pSets = pNode1.getPropertySets();
        for (int i = 0; i < pSets.length; i++) {
            Node.PropertySet pSet = pSets[i];
            addPropertyValues(map, pSet);
        }
        return map;
    }

    private void init() {
        this.setLayout(new BorderLayout());
        PropertySheet pSheet = new PropertySheet();
        // hack don't show tool bar
        pSheet.add(new JLabel(), BorderLayout.NORTH);
        this.add(pSheet, BorderLayout.CENTER);
        pSheet.setNodes(new Node[] { pNode});

        // set sorting mode to unsorted so that we get properties sorted as defined by
        // position attribute in xml
        try {
            pSheet.setSortingMode(PropertySheet.UNSORTED);
        } catch (Exception ex) {
        }
    }
}

