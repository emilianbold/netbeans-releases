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


package org.netbeans.modules.iep.editor.tcg.ps;

import org.netbeans.modules.iep.editor.tcg.exception.I18nException;
import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;
import java.util.*;
import javax.swing.ListModel;
import javax.swing.DefaultListModel;

import org.openide.nodes.Node;

import org.netbeans.modules.iep.editor.tcg.model.TcgProperty;
import org.netbeans.modules.iep.editor.tcg.model.TcgPropertyType;
import org.netbeans.modules.iep.editor.tcg.model.TcgType;
import java.util.logging.Level;
import org.openide.util.NbBundle;


public class TcgComponentNodeProperty extends Node.Property {
    private static final Logger mLog = Logger.getLogger(TcgComponentNodeProperty.class.getName());

    protected TcgProperty mProperty;
    protected TcgPropertyType mPropertyType;
    protected TcgComponentNode mNode;
    
    public static TcgComponentNodeProperty newInstance(String propName, TcgComponentNode node) throws I18nException {
        TcgProperty property = node.getComponent().getProperty(propName);
        return newInstance(property, node);
    }

    public static TcgComponentNodeProperty newInstance(TcgProperty property, TcgComponentNode node) {
        TcgPropertyType pt = property.getType();
        TcgType type = pt.getType();
        if (type == TcgType.BOOLEAN) {
            return new TcgComponentNodeProperty(property, Boolean.class, node);
        }
        if (type == TcgType.INTEGER) {
            return new TcgComponentNodeProperty(property, Integer.class, node);
        }
        if (type == TcgType.LONG) {
            return new TcgComponentNodeProperty(property, Long.class, node);
        }
        if (type == TcgType.DOUBLE) {
            return new TcgComponentNodeProperty(property, Double.class, node);
        }
        if (type == TcgType.STRING) {
            return new TcgComponentNodeProperty(property, String.class, node);
        }
        if (type == TcgType.DATE) {
            return new TcgComponentNodeProperty(property, Date.class, node);
        }
        if (type == TcgType.OBJECT && !pt.isWritable()) {
            return new TcgComponentNodeProperty(property, String.class, node);
        }
        if (type == TcgType.BOOLEAN_LIST ||
            type == TcgType.INTEGER_LIST ||
            type == TcgType.LONG_LIST ||
            type == TcgType.DOUBLE_LIST ||
            type == TcgType.STRING_LIST) 
        {
            TcgComponentNodeProperty p = new TcgComponentNodeProperty(property, ListModel.class, node);
            // Disable inplace editing.
            p.setValue("canEditAsText", Boolean.FALSE);
            return p;
        }
        return new TcgComponentNodeProperty(property, Object.class, node);
    }

    private TcgComponentNodeProperty(TcgProperty property, Class valueType, TcgComponentNode node) {
        super (valueType);
        mProperty = property;
        mPropertyType = mProperty.getType();
        mNode = node;
        setName(mProperty.getName());
        setDisplayName(TcgPsI18n.getDisplayName(mPropertyType));
        setShortDescription(TcgPsI18n.getToolTip(mPropertyType));
    }


    /* Can read the value of the property.
    * @return <CODE>true</CODE> if the read of the value is supported
    */
    public boolean canRead () {
        return mPropertyType.isReadable();
    }

    /* Getter for the value.
    * @return the value of the property
    * @exception IllegalAccessException cannot access the called method
    * @exception IllegalArgumentException wrong argument
    * @exception InvocationTargetException an exception during invocation
    */
    public Object getValue () 
        throws IllegalAccessException, IllegalArgumentException, InvocationTargetException 
    {
        // Note that the return of this method is not used by 
        // TcgComponentNodePropertyEditor and its children at all.
        // It is used by Netbean's default property editors
        TcgType type = mPropertyType.getType();
        if (type == TcgType.OBJECT && !mPropertyType.isWritable()) {
            return mProperty.getValue().toString();
        }
        if (type == TcgType.BOOLEAN_LIST ||
            type == TcgType.INTEGER_LIST ||
            type == TcgType.LONG_LIST ||
            type == TcgType.DOUBLE_LIST ||
            type == TcgType.STRING_LIST) 
        {
            DefaultListModel listModel = new DefaultListModel();
            List list = mProperty.getListValue();
            for (int i = 0, I = list.size(); i < I; i++) {
                listModel.addElement(list.get(i));
            }
            return listModel;
        }
        return mProperty.getValue();
    }

    /* Can write the value of the property.
    * @return <CODE>true</CODE> if the read of the value is supported
    */
    public boolean canWrite () {
        return mPropertyType.isWritable();
    }

    /** 
     * Setter for the value. Called when PropertyEditor updates this property's value
     * @param val the value of the property
     * @exception IllegalAccessException cannot access the called method
     * @exception IllegalArgumentException wrong argument
     * @exception InvocationTargetException an exception during invocation
     */
    public void setValue (Object val) 
        throws IllegalAccessException, IllegalArgumentException, InvocationTargetException 
    {
        if (!mPropertyType.isWritable()) {
            throw new IllegalAccessException ();
        }
        // Note that TcgComponentNodePropertyEditor and its children will only
        // pass back following types of value:
        // Boolean, Integer, Double, String, and 
        // Lists whose elements are of above type.
        if (val instanceof ListModel) { 
            ArrayList list = new ArrayList();
            ListModel listModel = (ListModel)val;
            for (int i = 0, I = listModel.getSize(); i < I; i++) {
                list.add(listModel.getElementAt(i));
            }
            mProperty.setValue(list);
            return;
        }
        if (val instanceof String) {
            mProperty.setStringValue((String)val);
            return;
        }
        mProperty.setValue(val);
    }

    /**
     * Returns property editor for this property.
     * @return the property editor or <CODE>null</CODE> if there should not be any editor.
     */
    public PropertyEditor getPropertyEditor () {
        String editorName = mPropertyType.getEditorName();
        if (editorName.equals("default")) {
            // Use Netbeans' default property editors.
            return super.getPropertyEditor();
        }
        try {
            TcgComponentNodePropertyEditor editor = (TcgComponentNodePropertyEditor)Class.forName(editorName).newInstance();
            editor.setProperty(this);
            return editor;
        } catch (Exception e) {
            mLog.log(Level.SEVERE, 
                     NbBundle.getMessage(TcgComponentNodeProperty.class, "TcgComponentNodeProperty.Editor_not_found", editorName),
                     e);
        }
        return null;
    }

    public TcgComponentNode getNode() {
        return mNode;
    }
    
    public TcgProperty getProperty() {
        return mProperty;
    }
    
    public TcgPropertyType getPropertyType() {
        return mPropertyType;
    }
}

