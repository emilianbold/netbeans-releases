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
import org.openide.util.HelpCtx;


/**
 * @author Chris Webster
 * @author Martin Adamek
 */
public class AddSelectMethodAction extends AbstractAddMethodAction {
    
    public AddSelectMethodAction(org.openide.util.Lookup ctx) {
        super(ctx, new AddSelectMethodStrategy());
    }
    
    public AddSelectMethodAction(org.openide.util.Lookup ctx, String name) {
        super(ctx, new AddSelectMethodStrategy(name));
    }
    
    public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup actionContext) {
        return new AddSelectMethodAction(actionContext, getName());
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
