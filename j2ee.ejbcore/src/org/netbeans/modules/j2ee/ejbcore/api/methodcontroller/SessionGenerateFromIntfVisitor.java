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
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.AbstractMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType.BusinessMethodType;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType.CreateMethodType;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType.FinderMethodType;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType.HomeMethodType;
import org.netbeans.modules.j2ee.common.JMIUtils;
import org.netbeans.modules.javacore.api.JavaModel;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
class SessionGenerateFromIntfVisitor implements MethodType.MethodTypeVisitor, AbstractMethodController.GenerateFromIntf {
    private Method implMethod;
    private static final String TODO = "//TODO implement "; //NOI18N
    
    public void getInterfaceMethodFromImpl(MethodType m) {
        m.accept(this);
    }
    
    public Method getImplMethod() {
        return implMethod;
    }
    
    public Method getSecondaryMethod() {
        return null;
    }
    
    public void visit(BusinessMethodType bmt) {
        implMethod = JMIUtils.duplicate(bmt.getMethodElement());
        implMethod.setModifiers(Modifier.PUBLIC);
        String body = TODO + implMethod.getName();
        Type type= implMethod.getType();
        if ((type instanceof PrimitiveType) && ((PrimitiveType) type).getKind().equals(PrimitiveTypeKindEnum.VOID)){

        }
        else if ((type instanceof PrimitiveType) && ((PrimitiveType) type).getKind().equals(PrimitiveTypeKindEnum.BOOLEAN)){
            body+="\nreturn false;";
        }
        else if ((type instanceof PrimitiveType) && ((PrimitiveType) type).getKind().equals(PrimitiveTypeKindEnum.BYTE)){
            body+="\nreturn 0;";
        }
        else if ((type instanceof PrimitiveType) && ((PrimitiveType) type).getKind().equals(PrimitiveTypeKindEnum.CHAR)){
            body+="\nreturn '0';";
        }
        else if ((type instanceof PrimitiveType) && ((PrimitiveType) type).getKind().equals(PrimitiveTypeKindEnum.DOUBLE)){
            body+="\nreturn 0.0;";
        }
        else if ((type instanceof PrimitiveType) && ((PrimitiveType) type).getKind().equals(PrimitiveTypeKindEnum.FLOAT)){
            body+="\nreturn 0.0;";
        }
        else if ((type instanceof PrimitiveType) && ((PrimitiveType) type).getKind().equals(PrimitiveTypeKindEnum.INT)){
            body+="\nreturn 0;";
        }
        else if ((type instanceof PrimitiveType) && ((PrimitiveType) type).getKind().equals(PrimitiveTypeKindEnum.LONG)){
            body+="\nreturn 0;";
        }
        else if ((type instanceof PrimitiveType) && ((PrimitiveType) type).getKind().equals(PrimitiveTypeKindEnum.SHORT)){
            body+="\nreturn 0;";
        }
        else{
            body+="\nreturn null;";

        }

        implMethod.setBodyText(body);
    }
       
    public void visit(CreateMethodType cmt) {
        implMethod = JMIUtils.duplicate(cmt.getMethodElement());
        String origName = implMethod.getName();
        String newName = prependAndUpper(origName,"ejb"); //NOI18N
        implMethod.setName(newName);
        implMethod.setType(JMIUtils.resolveType("void"));
        implMethod.setModifiers(Modifier.PUBLIC);
        implMethod.setBodyText(TODO);
    }
    
    public void visit(HomeMethodType hmt) {
        assert false: "session beans do not have home methods";
    }
    
    public void visit(FinderMethodType fmt) {
        assert false: "session beans do not have finder methods";
    }
    
    private String prependAndUpper(String fullName, String prefix) {
         StringBuffer sb = new StringBuffer(fullName);
         sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
         return prefix+sb.toString();
    }
}
