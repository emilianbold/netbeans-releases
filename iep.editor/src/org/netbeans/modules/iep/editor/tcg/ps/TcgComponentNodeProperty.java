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

import org.netbeans.modules.iep.editor.share.SharedConstants;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.iep.model.lib.I18nException;
import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;
import java.util.*;
import javax.swing.ListModel;
import javax.swing.DefaultListModel;

import org.openide.nodes.Node;

import java.util.logging.Level;
import org.netbeans.modules.iep.model.lib.TcgProperty;
import org.netbeans.modules.iep.model.lib.TcgPropertyType;
import org.netbeans.modules.iep.model.lib.TcgType;
import org.openide.util.NbBundle;


public class TcgComponentNodeProperty extends Node.Property {
    private static final Logger mLog = Logger.getLogger(TcgComponentNodeProperty.class.getName());

    protected TcgPropertyType mPropertyType;
    protected OperatorComponent mComp;
    protected IEPModel mModel;
    
    public static TcgComponentNodeProperty 
            newCustomPropertyEditorInstance(OperatorComponent component, IEPModel model) throws I18nException {
        Property property = model.getFactory().createProperty(model);
        property.setName(SharedConstants.PROPERTY_EDITOR_KEY);
        TcgPropertyType propertyType = component.getComponentType().getPropertyType(SharedConstants.PROPERTY_EDITOR_KEY);
        return newInstance(propertyType, component, model);
        //return newPropertyInstance(property, node);
    }
    
    public static TcgComponentNodeProperty newInstance(TcgPropertyType propertyType,
                                                       OperatorComponent component,
                                                       IEPModel model) throws I18nException {
        return newPropertyInstance(propertyType, component, model);
    }
    
    private static TcgComponentNodeProperty newPropertyInstance(TcgPropertyType propertyType,
                                                       OperatorComponent component,
                                                       IEPModel model) {
        return new TcgComponentNodeProperty(Object.class, propertyType, component, model);
    }

    private TcgComponentNodeProperty(Class valueType,
                                     TcgPropertyType propertyType,
                                     OperatorComponent component,
                                     IEPModel model) {
        super (valueType);
        mPropertyType = propertyType;
        mComp = component;
        mModel = model;
        setName(propertyType.getName());
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
        return "";
        //rit commented below
//        // Note that the return of this method is not used by 
//        // TcgComponentNodePropertyEditor and its children at all.
//        // It is used by Netbean's default property editors
//        TcgType type = mPropertyType.getType();
//        if (type == TcgType.OBJECT && !mPropertyType.isWritable()) {
//            return mProperty.getValue().toString();
//        }
//        if (type == TcgType.BOOLEAN_LIST ||
//            type == TcgType.INTEGER_LIST ||
//            type == TcgType.LONG_LIST ||
//            type == TcgType.DOUBLE_LIST ||
//            type == TcgType.STRING_LIST) 
//        {
//            DefaultListModel listModel = new DefaultListModel();
//            List list = new ArrayList();//ritmProperty.getListValue();
//            String value = mProperty.getValue();
//            if(value != null) {
//                list = (List) mProperty.getPropertyType().getType().parse(value);
//            }
//            for (int i = 0, I = list.size(); i < I; i++) {
//                listModel.addElement(list.get(i));
//            }
//            return listModel;
//        }
//        return mProperty.getValue();
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
        //rit commented
//        // Note that TcgComponentNodePropertyEditor and its children will only
//        // pass back following types of value:
//        // Boolean, Integer, Double, String, and 
//        // Lists whose elements are of above type.
//        if (val instanceof ListModel) { 
//            ArrayList list = new ArrayList();
//            ListModel listModel = (ListModel)val;
//            for (int i = 0, I = listModel.getSize(); i < I; i++) {
//                list.add(listModel.getElementAt(i));
//            }
//            String value = mProperty.getPropertyType().getType().format(list);
//            mProperty.setValue(value);
//            return;
//        }
//        if (val instanceof String) {
//            mProperty.setValue((String)val);
//            return;
//        }
//        mProperty.setValue(val.toString());
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
            ComponentPropertyEditorConfig editor = (ComponentPropertyEditorConfig)Class.forName(editorName).newInstance();
            //editor.setProperty(this);
            editor.setPropertyType(this.mPropertyType);
            editor.setOperatorComponent(this.mComp);
            return editor;
        } catch (Exception e) {
            mLog.log(Level.SEVERE, 
                     NbBundle.getMessage(TcgComponentNodeProperty.class, "TcgComponentNodeProperty.Editor_not_found", editorName),
                     e);
        }
        return null;
    }

   
    public IEPModel getModel() {
        return this.mModel;
    }
    
    public OperatorComponent getModelComponent() {
        return this.mComp;
    }
    
    public TcgPropertyType getPropertyType() {
        return mPropertyType;
    }
}

