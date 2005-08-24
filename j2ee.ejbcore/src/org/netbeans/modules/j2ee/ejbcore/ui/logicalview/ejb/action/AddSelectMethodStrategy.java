/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action;

import java.lang.reflect.Modifier;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.modules.j2ee.common.JMIUtils;
import org.netbeans.modules.j2ee.common.ui.nodes.MethodCollectorFactory;
import org.netbeans.modules.j2ee.common.ui.nodes.MethodCustomizer;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EntityMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EjbMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType;
import org.netbeans.modules.javacore.api.JavaModel;
import org.openide.util.NbBundle;


/**
 * @author Pavel Buzek
 */
public class AddSelectMethodStrategy extends AbstractAddMethodStrategy {
    
    public AddSelectMethodStrategy() {
        super(NbBundle.getMessage(AddSelectMethodAction.class, "LBL_AddSelectMethodAction"));
    }
    
    public AddSelectMethodStrategy(String name) {
        super(name);
    }
    
    protected MethodType getPrototypeMethod(JavaClass jc) {
        Method method = JMIUtils.createMethod(jc);
        method.setName("ejbSelectBy"); //NOI18N
        method.setType(JMIUtils.resolveType("int"));
        method.setModifiers(Modifier.PUBLIC|Modifier.ABSTRACT);
        JMIUtils.addException(method, "javax.ejb.FinderException");
        return new MethodType.SelectMethodType(method);
    }
    
    protected MethodCustomizer createDialog(MethodType pType, EjbMethodController c) {
        return MethodCollectorFactory.selectCollector(pType.getMethodElement(), JMIUtils.getMethods(c.getBeanClass()));
    }

    protected void okButtonPressed(MethodType pType, MethodCustomizer mc, Method prototypeMethod, EjbMethodController c, JavaClass jc) throws java.io.IOException {
        EntityMethodController emc = (EntityMethodController) c;
        emc.addSelectMethod(prototypeMethod,mc.getEjbQL(), getDDFile(jc));
    }
    
    public int prototypeMethod() {
        return MethodType.METHOD_TYPE_SELECT;
    }
}
