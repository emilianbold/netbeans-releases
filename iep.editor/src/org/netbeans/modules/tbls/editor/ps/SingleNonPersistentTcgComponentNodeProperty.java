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

import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.IOType;
import org.netbeans.modules.tbls.model.I18nException;
import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;
import java.util.*;
import javax.swing.ListModel;
import javax.swing.DefaultListModel;

import org.openide.nodes.Node;

import java.util.logging.Level;
import org.netbeans.modules.iep.model.share.SharedConstants;
import org.netbeans.modules.tbls.model.TcgPropertyType;
import org.netbeans.modules.tbls.model.TcgType;
import org.openide.util.NbBundle;


public class SingleNonPersistentTcgComponentNodeProperty extends Node.Property implements SharedConstants {
    private static final Logger mLog = Logger.getLogger(SingleNonPersistentTcgComponentNodeProperty.class.getName());

    protected TcgPropertyType mPropertyType;
    protected OperatorComponent mComp;
    protected IEPModel mModel;
    
    private TcgComponentNode mNode;
    
    
    public static SingleNonPersistentTcgComponentNodeProperty newInstance(TcgPropertyType propertyType, 
                                                       OperatorComponent component,
                                                       IEPModel model) throws I18nException {
        return newPropertyInstance(propertyType, component, model);
    }
    
    private static SingleNonPersistentTcgComponentNodeProperty newPropertyInstance(TcgPropertyType propertyType, 
                                                       OperatorComponent component,
                                                       IEPModel model) {
        TcgPropertyType pt = propertyType;
        TcgType type = pt.getType();
        if (type == TcgType.BOOLEAN) {
            return new SingleNonPersistentTcgComponentNodeProperty(propertyType, Boolean.class, component, model);
        }
        if (type == TcgType.INTEGER) {
            //inputMaxCount
            if(propertyType.getName().equals(PROP_INPUT_MAX_COUNT)) {
                return new InputMaxCountSingleNonPersistentTcgComponentNodeProperty(propertyType, Integer.class, component, model);
            }
            //staticInputMaxCount
            return new SingleNonPersistentTcgComponentNodeProperty(propertyType, Integer.class, component, model);
        }
        if (type == TcgType.LONG) {
            return new SingleNonPersistentTcgComponentNodeProperty(propertyType, Long.class, component, model);
        }
        if (type == TcgType.DOUBLE) {
            return new SingleNonPersistentTcgComponentNodeProperty(propertyType, Double.class, component, model);
        }
        if (type == TcgType.STRING) {
            return new SingleNonPersistentTcgComponentNodeProperty(propertyType, String.class, component, model);
        }
        if (type == TcgType.DATE) {
            return new SingleNonPersistentTcgComponentNodeProperty(propertyType, Date.class, component, model);
        }
        if (type == TcgType.OBJECT && !pt.isWritable()) {
            return new SingleNonPersistentTcgComponentNodeProperty(propertyType, String.class, component, model);
        }
        if (type == TcgType.BOOLEAN_LIST ||
            type == TcgType.INTEGER_LIST ||
            type == TcgType.LONG_LIST ||
            type == TcgType.DOUBLE_LIST ||
            type == TcgType.STRING_LIST) 
        {
            SingleNonPersistentTcgComponentNodeProperty p = new SingleNonPersistentTcgComponentNodeProperty(propertyType, ListModel.class, component, model);
            // Disable inplace editing.
            p.setValue("canEditAsText", Boolean.FALSE);
            return p;
        }
        return new SingleNonPersistentTcgComponentNodeProperty(propertyType, Object.class, component, model);
    }

    private SingleNonPersistentTcgComponentNodeProperty(TcgPropertyType propertyType, 
                                     Class valueType, 
                                     OperatorComponent component,
                                     IEPModel model) {
        super (valueType);
        mComp = component;
        mPropertyType = propertyType;
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
        // Note that the return of this method is not used by 
        // TcgComponentNodePropertyEditor and its children at all.
        // It is used by Netbean's default property editors
        TcgType type = mPropertyType.getType();
        if (type == TcgType.OBJECT && !mPropertyType.isWritable()) {
            return mPropertyType.getDefaultValue().toString();
        }
        if (type == TcgType.BOOLEAN_LIST ||
            type == TcgType.INTEGER_LIST ||
            type == TcgType.LONG_LIST ||
            type == TcgType.DOUBLE_LIST ||
            type == TcgType.STRING_LIST) 
        {
            DefaultListModel listModel = new DefaultListModel();
            List list =(List) mPropertyType.getDefaultValue();
            
            for (int i = 0, I = list.size(); i < I; i++) {
                listModel.addElement(list.get(i));
            }
            return listModel;
        }
        
        Object val = mPropertyType.getDefaultValue();
        
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
            throw new IllegalAccessException ("Property is not writable");
        } 
        
    }

    /**
     * Returns property editor for this property.
     * @return the property editor or <CODE>null</CODE> if there should not be any editor.
     */
    @Override
    public PropertyEditor getPropertyEditor () {
        String editorName = mPropertyType.getEditorName();
        if (editorName.equals("default")) {
            // Use Netbeans' default property editors.
            return super.getPropertyEditor();
        }
        
        try {
            ComponentPropertyEditorConfig editor = new NonPersistentPropertyNoEditEditor();
            editor.setPropertyType(this.mPropertyType);
            editor.setOperatorComponent(this.mComp);
            return editor;
        } catch (Exception e) {
            mLog.log(Level.SEVERE, 
                     NbBundle.getMessage(SingleNonPersistentTcgComponentNodeProperty.class, 
                         "TcgComponentNodeProperty.Editor_not_found", NonPersistentPropertyNoEditEditor.class.getName()),
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
    
    public static class InputMaxCountSingleNonPersistentTcgComponentNodeProperty extends SingleNonPersistentTcgComponentNodeProperty {
        
        InputMaxCountSingleNonPersistentTcgComponentNodeProperty(TcgPropertyType propertyType, 
                 Class valueType, 
                 OperatorComponent component,
                 IEPModel model) {
            super(propertyType, valueType, component, model);
            
            String displayNameKey = mPropertyType.getTitle();
            String descriptionKey = mPropertyType.getDescription();
            
//            if stream is allowed
            if(component.getInputType().equals(IOType.STREAM)) {
                    displayNameKey = "IEP.Model.Entity.inputMaxCountStream.title"; //NOTI18N
                    descriptionKey = "IEP.Model.Entity.inputMaxCountStream.description"; //NOTI18N
            } else if(component.getInputType().equals(IOType.RELATION)) {
                    displayNameKey = "IEP.Model.Entity.inputMaxCountRelation.title"; //NOTI18N
                    descriptionKey = "IEP.Model.Entity.inputMaxCountRelation.description"; //NOTI18N
                
            }
            
            setDisplayName(TcgPsI18n.getI18nString(displayNameKey));
            setShortDescription(TcgPsI18n.getI18nString(descriptionKey));
        }
    }
    
    public static class StaticInputMaxCountSingleNonPersistentTcgComponentNodeProperty extends SingleNonPersistentTcgComponentNodeProperty {
        
        StaticInputMaxCountSingleNonPersistentTcgComponentNodeProperty(TcgPropertyType propertyType, 
                 Class valueType, 
                 OperatorComponent component,
                 IEPModel model) {
            super(propertyType, valueType, component, model);
            
            String displayNameKey = mPropertyType.getTitle();
            String descriptionKey = mPropertyType.getDescription();
            
//            if stream is allowed
            if(component.getInputType().equals(IOType.STREAM)) {
                    displayNameKey = "IEP.Model.Entity.inputMaxCountStream.title"; //NOTI18N
                    descriptionKey = "IEP.Model.Entity.inputMaxCountStream.description"; //NOTI18N
            } else if(component.getInputType().equals(IOType.RELATION)) {
                    displayNameKey = "IEP.Model.Entity.inputMaxCountRelation.title"; //NOTI18N
                    descriptionKey = "IEP.Model.Entity.inputMaxCountRelation.description"; //NOTI18N
                
            }
            
            setDisplayName(TcgPsI18n.getI18nString(displayNameKey));
            setShortDescription(TcgPsI18n.getI18nString(descriptionKey));
        }
    }
    
    
}

