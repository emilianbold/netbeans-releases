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
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/**
 * @author Chris Webster
 */
public class AddFinderMethodAction extends AbstractAddMethodAction {
    
    public AddFinderMethodAction(org.openide.util.Lookup ctx) {
        super(ctx, new AddFinderMethodStrategy());
    }

    public AddFinderMethodAction(org.openide.util.Lookup ctx, String name) {
        super(ctx, new AddFinderMethodStrategy(name));
    }
    
    public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup actionContext) {
        return new AddFinderMethodAction(actionContext, getName());
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
