/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.ejbcore.api.methodcontroller;

import java.lang.reflect.Modifier;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.PrimitiveType;
import org.netbeans.jmi.javamodel.PrimitiveTypeKindEnum;
import org.netbeans.jmi.javamodel.Type;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.AbstractMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType.BusinessMethodType;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType.CreateMethodType;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType.FinderMethodType;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType.HomeMethodType;
import org.netbeans.modules.j2ee.common.JMIUtils;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
class EntityGenerateFromIntfVisitor implements MethodType.MethodTypeVisitor, AbstractMethodController.GenerateFromIntf {
    private Method implMethod;
    private Method secondaryMethod;
    private Entity dd;
    private static final String TODO = "//TODO implement "; //NOI18N
    
    public EntityGenerateFromIntfVisitor(Entity dd) {
        this.dd = dd;
    }
    
    public void getInterfaceMethodFromImpl(MethodType m) {
        m.accept(this);
    }
    
    public Method getImplMethod() {
        return implMethod;
    }
    
    public Method getSecondaryMethod() {
        return secondaryMethod;
    }
    
    public void visit(BusinessMethodType bmt) {
        implMethod = JMIUtils.duplicate(bmt.getMethodElement());
        implMethod.setModifiers(Modifier.PUBLIC);
        //implMethod.setBody(TODO + implMethod.getName().getName());
        String body = TODO + implMethod.getName();
        Type type= implMethod.getType();
        if ((type instanceof PrimitiveType) && ((PrimitiveType) type).getKind().equals(PrimitiveTypeKindEnum.VOID)) {
            
        } else if ((type instanceof PrimitiveType) && ((PrimitiveType) type).getKind().equals(PrimitiveTypeKindEnum.BOOLEAN)){
            body+="\nreturn false;";
        } else if ((type instanceof PrimitiveType) && ((PrimitiveType) type).getKind().equals(PrimitiveTypeKindEnum.BYTE)){
            body+="\nreturn 0;";
        } else if ((type instanceof PrimitiveType) && ((PrimitiveType) type).getKind().equals(PrimitiveTypeKindEnum.CHAR)){
            body+="\nreturn '0';";
        } else if ((type instanceof PrimitiveType) && ((PrimitiveType) type).getKind().equals(PrimitiveTypeKindEnum.DOUBLE)) {
            body+="\nreturn 0.0;";
        } else if ((type instanceof PrimitiveType) && ((PrimitiveType) type).getKind().equals(PrimitiveTypeKindEnum.FLOAT)) {
            body+="\nreturn 0.0;";
        } else if ((type instanceof PrimitiveType) && ((PrimitiveType) type).getKind().equals(PrimitiveTypeKindEnum.INT)) {
            body+="\nreturn 0;";
        } else if ((type instanceof PrimitiveType) && ((PrimitiveType) type).getKind().equals(PrimitiveTypeKindEnum.LONG)) {
            body+="\nreturn 0;";
        } else if ((type instanceof PrimitiveType) && ((PrimitiveType) type).getKind().equals(PrimitiveTypeKindEnum.SHORT)) {
            body+="\nreturn 0;";
        } else{
            body+="\nreturn null;";
            
        }
        
        implMethod.setBodyText(body);
    }
    
    public void visit(CreateMethodType cmt) {
        implMethod = JMIUtils.duplicate(cmt.getMethodElement());
        String origName = implMethod.getName();
        String newName = prependAndUpper(origName,"ejb"); //NOI18N
        implMethod.setName(newName);
        implMethod.setModifiers(Modifier.PUBLIC);
        implMethod.setType(JMIUtils.resolveType(dd.getPrimKeyClass()));
        implMethod.setBodyText(TODO + newName);

        secondaryMethod = JMIUtils.duplicate(cmt.getMethodElement());
        origName = secondaryMethod.getName();
        newName = prependAndUpper(origName,"ejbPost"); //NOI18N
        secondaryMethod.setName(newName);
        secondaryMethod.setModifiers(Modifier.PUBLIC);
        secondaryMethod.setType(JMIUtils.resolveType("void"));
        secondaryMethod.setBodyText(TODO + newName);
    }
    
    public void visit(HomeMethodType hmt) {
        implMethod = JMIUtils.duplicate(hmt.getMethodElement());
        String origName = implMethod.getName();
        String newName = prependAndUpper(origName,"ejbHome"); //NOI18N
        implMethod.setName(newName);
        implMethod.setModifiers(Modifier.PUBLIC);
        implMethod.setBodyText(TODO + implMethod.getName());
    }
    
    public void visit(FinderMethodType fmt) {
        implMethod = JMIUtils.duplicate(fmt.getMethodElement());
        String origName = implMethod.getName();
        String newName = prependAndUpper(origName,"ejb"); //NOI18N
        implMethod.setName(newName);
        implMethod.setModifiers(Modifier.PUBLIC);
        Type collectionType = JMIUtils.resolveType(java.util.Collection.class.getName());
        if (!collectionType.equals(implMethod.getType())) {
            implMethod.setType(JMIUtils.resolveType(dd.getPrimKeyClass()));
        }
        implMethod.setBodyText(TODO + implMethod.getName());
    }
    
    private String prependAndUpper(String fullName, String prefix) {
        StringBuffer sb = new StringBuffer(fullName);
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        return prefix+sb.toString();
    }
}
