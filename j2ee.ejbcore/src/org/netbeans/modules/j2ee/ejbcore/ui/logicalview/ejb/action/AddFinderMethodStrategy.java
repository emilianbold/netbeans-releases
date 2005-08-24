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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action;
import java.util.Collection;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.Type;
import org.netbeans.modules.j2ee.common.JMIUtils;
import org.netbeans.modules.j2ee.common.ui.nodes.MethodCollectorFactory;
import org.netbeans.modules.j2ee.common.ui.nodes.MethodCustomizer;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.Utils;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EjbMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType;
import org.netbeans.modules.javacore.api.JavaModel;
import org.openide.util.NbBundle;


/**
 * @author Pavel Buzek
 */
public class AddFinderMethodStrategy extends AbstractAddMethodStrategy {
    
    public AddFinderMethodStrategy (String name) {
        super(name);
    }
    public AddFinderMethodStrategy () {
        super (NbBundle.getMessage(AddFinderMethodAction.class, "LBL_AddFinderMethodAction"));
    }
    
    protected MethodType getPrototypeMethod(JavaClass jc) {
        return getFinderPrototypeMethod(jc);
    }

    public static MethodType getFinderPrototypeMethod(JavaClass jc) {
        Method me = JMIUtils.createMethod(jc);
        me.setName("findBy"); //NOI18N
        JMIUtils.addException(me, "javax.ejb.FinderException"); //NOI18N
        return new MethodType.FinderMethodType(me);
    }

    protected MethodCustomizer createDialog(MethodType pType, EjbMethodController c) {
        return createFinderDialog(c, pType);
    }

    public static MethodCustomizer createFinderDialog(EjbMethodController c, MethodType pType) {
        boolean javaImpl = c.hasJavaImplementation(pType);
        Method[] methodElements = Utils.getMethods(c, true, false);
        return MethodCollectorFactory.finderCollector(pType.getMethodElement(), c.hasRemote(), c.hasLocal(), !javaImpl, methodElements);
    }

    protected Type remoteReturnType(EjbMethodController c, Type t, boolean isOneReturn) {
        String fullName = isOneReturn?c.getRemote():Collection.class.getName();
        return JMIUtils.resolveType(fullName);
    }

    protected Type localReturnType(EjbMethodController c, Type t, boolean isOneReturn) {
        String fullName = isOneReturn?c.getLocal():Collection.class.getName();
        return JMIUtils.resolveType(fullName);
    }
    
    public int prototypeMethod() {
        return MethodType.METHOD_TYPE_FINDER;
    }
    
}
