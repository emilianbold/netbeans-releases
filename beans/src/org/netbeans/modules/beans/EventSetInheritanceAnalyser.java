/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * EventSetInheritanceAnalyser.java
 *
 * Created on 24. leden 2001, 9:14
 */

package org.netbeans.modules.beans;

import java.lang.reflect.*;
import java.text.MessageFormat;

import org.openide.src.*;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;
/**
 *
 * @author  Petr Suchomel
 * @version 0.1
 * utility class, try to detect if given ClassElement has parent which contains given event set
 */
class EventSetInheritanceAnalyser extends Object {
    
    /** Used to test if PropertyChangeSupport exists
     * @param clazz Class which be tested for PropertyChangeSupport
     * @return Class in which PropertySupport exist, or null
     */    
    static MemberElement detectPropertyChangeSupport(ClassElement clazz){
        return findSupport(clazz, "java.beans.PropertyChangeSupport" ); // NOI18N
    }

    /** Used to test if VetoableChangeSupport exists
     * @param clazz Class which be tested for VetoableChangeSupport
     * @return Class in which VetoableSupport exist, or null
     */    
    static MemberElement detectVetoableChangeSupport(ClassElement clazz){
        return findSupport(clazz, "java.beans.VetoableChangeSupport" ); // NOI18N
    }
    
    /** Used to test if given ChangeSupport exists
     * @param clazz Class which be tested for ChangeSupport
     * @param supportName full name of ChangeSupport
     * @return Class in which ChangeSupport exist, or null
     */    
    private static MemberElement findSupport(ClassElement clazz, String supportName){
        Identifier propertyChangeField = null;
        
        if( clazz == null || clazz.getName().getFullName().equals("java.lang.Object")) //NOI18N
            return null;    //no super class given or super class is Object
        
        ClassElement parent = ClassElement.forName( clazz.getSuperclass().getFullName() );
        if( parent == null )
            return parent; 
        else {
            if( propertyChangeField == null )
                propertyChangeField = Identifier.create( supportName ); // NOI18N                

            MethodElement methods[] = parent.getMethods();
            for( int i = 0; i < methods.length; i++ ) {
                if( !Modifier.isPrivate(methods[i].getModifiers()) && methods[i].getParameters().length == 0 ){
                    if( !methods[i].getReturn().isPrimitive() && !methods[i].getReturn().isArray() && methods[i].getReturn().getClassName().compareTo(propertyChangeField, false )  ){
                        return methods[i];
                    }
                }            
            }            
            FieldElement fields[] = parent.getFields();
            for( int i = 0; i < fields.length; i++ ) {
                if( !Modifier.isPrivate(fields[i].getModifiers()) ){                    
                    if( !fields[i].getType().isPrimitive() && !fields[i].getType().isArray() && fields[i].getType().getClassName().compareTo(propertyChangeField, false )  ){
                        return fields[i];
                    }
                }            
            }            
        }
        return findSupport(parent, supportName);    //Try to search recursively            
    }

    static String showInheritanceEventDialog( MemberElement me , String supportTypeName){
        ElementFormat format = new ElementFormat("{n}({p})");
        String supportName = null;
        if( me != null ){
            if( me instanceof MethodElement )
                supportName = format.format(((MethodElement)me));
            else
                supportName = me.getName().getFullName();   //prepare for later usage
            Object msgfields[] = new Object[] {me.getDeclaringClass().getName().getFullName(), supportTypeName };
            String msg = MessageFormat.format(PatternNode.getString("MSG_Inheritance_Found"), msgfields);
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation ( msg , NotifyDescriptor.YES_NO_OPTION );
            TopManager.getDefault().notify( nd );
            if( nd.getValue().equals( NotifyDescriptor.YES_OPTION ) ) {     
                return supportName;
            }
        }        
        return null;
    }
}
