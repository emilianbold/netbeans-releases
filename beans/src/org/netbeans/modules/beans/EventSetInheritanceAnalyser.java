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

package org.netbeans.modules.beans;

import java.text.MessageFormat;
import java.text.Format;
import java.util.List;
import java.util.Iterator;
import java.lang.reflect.Modifier;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.netbeans.jmi.javamodel.*;
import org.netbeans.modules.java.ui.nodes.SourceNodes;

import javax.jmi.reflect.JmiException;

/**
 *
 * @author  Petr Suchomel
 * @version 0.1
 * utility class, try to detect if given ClassElement has parent which contains given event set
 */
final class EventSetInheritanceAnalyser extends Object {
    
    /** Used to test if PropertyChangeSupport exists
     * @param clazz Class which be tested for PropertyChangeSupport
     * @return Class in which PropertySupport exist, or null
     */    
    static ClassMember detectPropertyChangeSupport(JavaClass clazz) throws JmiException {
        return findSupport(clazz, "java.beans.PropertyChangeSupport" ); // NOI18N
    }

    /** Used to test if VetoableChangeSupport exists
     * @param clazz Class which be tested for VetoableChangeSupport
     * @return Class in which VetoableSupport exist, or null
     */    
    static ClassMember detectVetoableChangeSupport(JavaClass clazz) throws JmiException {
        return findSupport(clazz, "java.beans.VetoableChangeSupport" ); // NOI18N
    }
    
    /** Used to test if given ChangeSupport exists
     * @param clazz Class which be tested for ChangeSupport
     * @param supportName full name of ChangeSupport
     * @return Class in which ChangeSupport exist, or null
     */    
    private static ClassMember findSupport(JavaClass clazz, String supportName) throws JmiException {
        assert JMIUtils.isInsideTrans();
        String propertyChangeField = supportName;
        
        if( clazz == null || "java.lang.Object".equals(clazz.getName())) //NOI18N
            return null;    //no super class given or super class is Object
        
        JavaClass superClass = clazz.getSuperClass();
        if( superClass == null || superClass instanceof UnresolvedClass) //no extends or implements clause
            return null;
        
        List/*<Method>*/ methods = JMIUtils.getMethods(superClass);
        for (Iterator it = methods.iterator(); it.hasNext();) {
            Method method = (Method) it.next();
            if( !Modifier.isPrivate(method.getModifiers()) && method.getParameters().isEmpty() ){
                Type returnType = method.getType();
                if( propertyChangeField.equals(returnType.getName()) ){
                    return method;
                }
            }            
        }            
        List/*<Field>*/ fields = JMIUtils.getFields(superClass);
        for (Iterator it = fields.iterator(); it.hasNext();) {
            Field field = (Field) it.next();
            if( !Modifier.isPrivate(field.getModifiers()) ){
                if (propertyChangeField.equals(field.getType().getName())) {
                    return field;
                }
            }            
        }            
        return findSupport(superClass, supportName);    //Try to search recursively            
    }

    static String showInheritanceEventDialog( ClassMember me , String supportTypeName) throws JmiException {        
        assert JMIUtils.isInsideTrans();
        String supportName = getInheritanceEventSupportName(me, supportTypeName);
        if( me != null ){
            Object msgfields[] = new Object[] {me.getDeclaringClass().getName(), supportTypeName };
            String msg = MessageFormat.format(PatternNode.getString("MSG_Inheritance_Found"), msgfields);
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation ( msg , NotifyDescriptor.YES_NO_OPTION );
            DialogDisplayer.getDefault().notify( nd );
            if( nd.getValue().equals( NotifyDescriptor.YES_OPTION ) ) {     
                return supportName;
            }
        }        
        return null;
    }
    
    static String getInheritanceEventSupportName( ClassMember me , String supportTypeName) throws JmiException {
        assert JMIUtils.isInsideTrans();
        Format format = SourceNodes.createElementFormat("{n}({p})"); // NOI18N
        String supportName = null;
        if( me != null ){
            if( me instanceof Method )
                supportName = format.format(me);
            else
                supportName = me.getName();   //prepare for later usage            
        }        
        return supportName;
    }
}
