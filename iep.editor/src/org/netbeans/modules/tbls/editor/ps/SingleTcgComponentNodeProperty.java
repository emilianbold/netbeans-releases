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


package org.netbeans.modules.tbls.editor.ps;

import org.netbeans.modules.iep.model.share.SharedConstants;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.IOType;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.tbls.model.I18nException;
import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;
import java.util.*;
import javax.swing.ListModel;
import javax.swing.DefaultListModel;

import org.openide.nodes.Node;

import java.util.logging.Level;
import org.netbeans.modules.tbls.model.TcgPropertyType;
import org.netbeans.modules.tbls.model.TcgType;
import org.openide.util.NbBundle;


public class SingleTcgComponentNodeProperty extends Node.Property implements SharedConstants {
    private static final Logger mLog = Logger.getLogger(SingleTcgComponentNodeProperty.class.getName());

    protected Property mProperty;
    protected TcgPropertyType mPropertyType;
    protected OperatorComponent mComp;
    protected IEPModel mModel;
    
    private TcgComponentNode mNode;
    
    public static SingleTcgComponentNodeProperty 
            newCustomPropertyEditorInstance(OperatorComponent component, IEPModel model) throws I18nException {
        Property property = model.getFactory().createProperty(model);
        property.setName(SharedConstants.PROP_PROPERTY_EDITOR);
        
        return new SingleTcgComponentNodeProperty(property, Object.class, component, model);
        //return newPropertyInstance(property, node);
    }
    
    public static SingleTcgComponentNodeProperty newInstance(Property property, 
                                                       OperatorComponent component,
                                                       IEPModel model) throws I18nException {
        return newPropertyInstance(property, component, model);
    }
    
    private static SingleTcgComponentNodeProperty newPropertyInstance(Property property, 
                                                       OperatorComponent component,
                                                       IEPModel model) {
        TcgPropertyType pt = property.getPropertyType();
        TcgType type = pt.getType();
        if (type == TcgType.BOOLEAN) {
            return new SingleTcgComponentNodeProperty(property, Boolean.class, component, model);
        }
        if (type == TcgType.INTEGER) {
            return new SingleTcgComponentNodeProperty(property, Integer.class, component, model);
        }
        if (type == TcgType.LONG) {
            return new SingleTcgComponentNodeProperty(property, Long.class, component, model);
        }
        if (type == TcgType.DOUBLE) {
            return new SingleTcgComponentNodeProperty(property, Double.class, component, model);
        }
        if (type == TcgType.STRING) {
            return new SingleTcgComponentNodeProperty(property, String.class, component, model);
        }
        if (type == TcgType.DATE) {
            return new SingleTcgComponentNodeProperty(property, Date.class, component, model);
        }
        if (type == TcgType.OBJECT && !pt.isWritable()) {
            return new SingleTcgComponentNodeProperty(property, String.class, component, model);
        }
        if (type == TcgType.BOOLEAN_LIST ||
            type == TcgType.INTEGER_LIST ||
            type == TcgType.LONG_LIST ||
            type == TcgType.DOUBLE_LIST ||
            type == TcgType.STRING_LIST) 
        {
            SingleTcgComponentNodeProperty p = null;
            if(property.getName().equals(PROP_INPUT_ID_LIST)) {
                p = new SingleTcgComponentNodeProperty.InputIdListSingleTcgComponentNodeProperty(property, ListModel.class, component, model);
            } else {
                p = new SingleTcgComponentNodeProperty(property, ListModel.class, component, model);
            }
            // Disable inplace editing.
            p.setValue("canEditAsText", Boolean.FALSE);
            return p;
        }
        return new SingleTcgComponentNodeProperty(property, Object.class, component, model);
    }

    private SingleTcgComponentNodeProperty(Property property, 
                                     Class valueType, 
                                     OperatorComponent component,
                                     IEPModel model) {
        super (valueType);
        mProperty = property;
        mComp = component;
        mPropertyType = mProperty.getPropertyType();
        mModel = model;
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
            List list = new ArrayList();//ritmProperty.getListValue();
            String value = mProperty.getValue();
            if(value != null) {
                list = (List) mProperty.getPropertyType().getType().parse(value);
            }
            for (int i = 0, I = list.size(); i < I; i++) {
                listModel.addElement(list.get(i));
            }
            return listModel;
        }
        
        Object val = mProperty.getPropertyType().getType().parse(mProperty.getValue());
        
        return val;
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
            String value = mProperty.getPropertyType().getType().format(list);
            
            mProperty.getModel().startTransaction();
            mProperty.setValue(value);
            mProperty.getModel().endTransaction();
            
            return;
        }
        if (val instanceof String) {
            mProperty.getModel().startTransaction();
            mProperty.setValue((String)val);
            mProperty.getModel().endTransaction();
            
            return;
        }
        String strVal = mProperty.getPropertyType().getType().format(val);
        
        mProperty.getModel().startTransaction();
        mProperty.setValue(strVal);
        mProperty.getModel().endTransaction();
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
            SingleTcgComponentNodePropertyEditor editor = (SingleTcgComponentNodePropertyEditor)Class.forName(editorName).newInstance();
            //editor.setProperty(this);
            editor.setProperty(this.mProperty);
            editor.setPropertyType(this.mPropertyType);
            editor.setOperatorComponent(this.mComp);
            return editor;
        } catch (Exception e) {
            mLog.log(Level.SEVERE, 
                     NbBundle.getMessage(SingleTcgComponentNodeProperty.class, "TcgComponentNodeProperty.Editor_not_found", editorName),
                     e);
        }
        return null;
    }

    public TcgComponentNode getNode() {
        return mNode;
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
    
    public static class InputIdListSingleTcgComponentNodeProperty extends SingleTcgComponentNodeProperty {
        InputIdListSingleTcgComponentNodeProperty(Property property, 
                 Class valueType, 
                 OperatorComponent component,
                 IEPModel model) {
            super(property, valueType, component, model);
            
            String displayNameKey = mPropertyType.getTitle();
            String descriptionKey = mPropertyType.getDescription();
            
            
            //if stream is allowed
            if(component.getInputType().equals(IOType.STREAM)) {
                    displayNameKey = "IEP.Model.Entity.inputIdListStream.title"; //NOTI18N
                    descriptionKey = "IEP.Model.Entity.inputIdListStream.description"; //NOTI18N
            } else if(component.getInputType().equals(IOType.RELATION)) {
                    displayNameKey = "IEP.Model.Entity.inputIdListRelation.title"; //NOTI18N
                    descriptionKey = "IEP.Model.Entity.inputIdListRelation.description"; //NOTI18N
                
            }
            
            setDisplayName(TcgPsI18n.getI18nString(displayNameKey));
            setShortDescription(TcgPsI18n.getI18nString(descriptionKey));
            
        }
        
//        public Object getValue () 
//        throws IllegalAccessException, IllegalArgumentException, InvocationTargetException 
//    {
//        
//        DefaultListModel listModel = new DefaultListModel();
//        List<OperatorComponent> inputs = mComp.getInputOperatorList();
//        Iterator<OperatorComponent> it = inputs.iterator();
//        while(it.hasNext()) {
//            OperatorComponent oc = it.next();
//            String displayName = oc.getDisplayName();
//            listModel.addElement(displayName);
//        }
//        return listModel;
//       
//    }

        
    }
}

