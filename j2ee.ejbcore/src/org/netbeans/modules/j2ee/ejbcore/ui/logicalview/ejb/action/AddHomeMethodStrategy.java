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
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.modules.j2ee.common.JMIUtils;
import org.netbeans.modules.j2ee.common.ui.nodes.MethodCollectorFactory;
import org.netbeans.modules.j2ee.common.ui.nodes.MethodCustomizer;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.Utils;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EjbMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType;
import org.openide.util.NbBundle;


/**
 * Action that can always be invoked and work procedurally.
 * @author cwebster
 */
public class AddHomeMethodStrategy extends AbstractAddMethodStrategy {
    
    public AddHomeMethodStrategy(String name) {
        super (name);
    }
    public AddHomeMethodStrategy () {
        super(NbBundle.getMessage(AddHomeMethodAction.class, "LBL_AddHomeMethodAction"));
    }
    
    protected MethodType getPrototypeMethod(JavaClass jc) {
        Method me = JMIUtils.createMethod(jc);
        me.setName("homeMethod"); //NOI18N
        return new MethodType.HomeMethodType(me);
    }

    protected MethodCustomizer createDialog(MethodType pType, EjbMethodController c) {
        Method[] methodElements = Utils.getMethods(c, true, false);
        return MethodCollectorFactory.homeCollector(pType.getMethodElement(), c.hasRemote(), c.hasLocal(), methodElements);
    }
    
    public int prototypeMethod() {
        return MethodType.METHOD_TYPE_HOME;
    }
}
