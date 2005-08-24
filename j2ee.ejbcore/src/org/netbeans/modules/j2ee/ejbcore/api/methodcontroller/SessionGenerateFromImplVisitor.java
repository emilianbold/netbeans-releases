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

import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Method;
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
class SessionGenerateFromImplVisitor implements MethodType.MethodTypeVisitor, AbstractMethodController.GenerateFromImpl {
    private Method intfMethod;
    private JavaClass destination;
    private JavaClass home;
    private JavaClass component;
    
    public void getInterfaceMethodFromImpl(MethodType m, 
                                           JavaClass home,
                                           JavaClass component) {
        this.home = home;
        this.component = component;
        m.accept(this);
    }
    
    public Method getInterfaceMethod() {
        return intfMethod;
    }
    
    public JavaClass getDestinationInterface() {
        return destination;
    }
    
    public void visit(BusinessMethodType bmt) {
        intfMethod = JMIUtils.duplicate(bmt.getMethodElement());
        destination = component;
    }
       
    public void visit(CreateMethodType cmt) {
        intfMethod = JMIUtils.duplicate(cmt.getMethodElement());
        String origName = intfMethod.getName();
        String newName = chopAndUpper(origName,"ejb"); //NOI18N
        intfMethod.setName(newName);
        intfMethod.setType(JMIUtils.resolveType(home.getName()));
        destination = home;
    }
    
    public void visit(HomeMethodType hmt) {
        assert false: "session beans do not have home methods";
    }
    
    public void visit(FinderMethodType fmt) {
        assert false: "session beans do not have finder methods";
    }
    
    private String chopAndUpper(String fullName, String chop) {
         StringBuffer sb = new StringBuffer(fullName);
         sb.delete(0, chop.length());
         sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
         return sb.toString();
    }
}
