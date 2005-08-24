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
import org.netbeans.modules.j2ee.common.JMIUtils;
import org.netbeans.modules.j2ee.common.ui.nodes.MethodCollectorFactory;
import org.netbeans.modules.j2ee.common.ui.nodes.MethodCustomizer;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EjbMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType;
import org.openide.util.HelpCtx;


/**
 * @author Chris Webster
 * @author Martin Adamek
 */
public class AddBusinessMethodAction extends AbstractAddMethodAction {
    
    public AddBusinessMethodAction(org.openide.util.Lookup ctx) {
        super(ctx, new AddBusinessMethodStrategy());
    }
    
    public AddBusinessMethodAction(org.openide.util.Lookup ctx, String name) {
        super(ctx, new AddBusinessMethodStrategy(name));
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup actionContext) {
        return new AddBusinessMethodAction(actionContext, getName());
    }
}
