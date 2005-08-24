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
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.Type;
import org.netbeans.modules.j2ee.common.JMIUtils;
import org.netbeans.modules.j2ee.common.ui.nodes.MethodCollectorFactory;
import org.netbeans.modules.j2ee.common.ui.nodes.MethodCustomizer;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EjbMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType;
import org.netbeans.modules.javacore.api.JavaModel;
import org.openide.util.HelpCtx;


/**
 * @author Chris Webster
 * @author Martin Adamek
 */
public class AddCreateMethodAction extends AbstractAddMethodAction {
    
    public AddCreateMethodAction(org.openide.util.Lookup ctx) {
        super(ctx, new AddCreateMethodStrategy());
    }
    
    public AddCreateMethodAction(org.openide.util.Lookup ctx, String name) {
        super(ctx, new AddCreateMethodStrategy(name));
    }
    
    public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup actionContext) {
        return new AddCreateMethodAction(actionContext, getName());
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new HelpCtx(AddBusinessMethodAction.class);
    }
    
    protected MethodType getPrototypeMethod(JavaClass jc) {
        Method me = JMIUtils.createMethod(jc);
        me.setName("create"); //NOI18N
        JMIUtils.addException(me, "javax.ejb.CreateException"); //NOI18N
        return new MethodType.CreateMethodType(me);
    }
    
    protected MethodCustomizer createDialog(MethodType pType, EjbMethodController c) {
        Method[] methodElements = org.netbeans.modules.j2ee.ejbcore.ui.logicalview.Utils.getMethods(c, true, false);
        return MethodCollectorFactory.createCollector(pType.getMethodElement(), c.hasRemote(), c.hasLocal(), methodElements);
    }

    protected Type remoteReturnType(EjbMethodController c, Type t, boolean isOneReturn) {
        return JMIUtils.resolveType(c.getRemote());
    }

    protected Type localReturnType(EjbMethodController c, Type t, boolean isOneReturn) {
        return JMIUtils.resolveType(c.getLocal());
    }
    
    protected int prototypeMethod() {
        return MethodType.METHOD_TYPE_CREATE;
    }
    
}
