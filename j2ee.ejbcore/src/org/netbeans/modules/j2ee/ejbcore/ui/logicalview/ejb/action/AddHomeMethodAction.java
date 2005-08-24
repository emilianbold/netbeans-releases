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
import org.openide.util.HelpCtx;


/**
 * Action that can always be invoked and work procedurally.
 * @author cwebster
 */
public class AddHomeMethodAction extends AbstractAddMethodAction {
    
    public AddHomeMethodAction(org.openide.util.Lookup ctx) {
        super(ctx, new AddHomeMethodStrategy());
    }
    
    public AddHomeMethodAction(org.openide.util.Lookup ctx, String name) {
        super(ctx, new AddHomeMethodStrategy(name));
    }
    
    public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup actionContext) {
        return new AddHomeMethodAction(actionContext, getName());
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new HelpCtx(AddCreateMethodAction.class);
    }
}
