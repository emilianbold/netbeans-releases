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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared;

import javax.swing.Action;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.AddActionGroup;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Represents Local/Remote Methods node under Session and Entity nodes 
 * in EJB logical view
 *
 * @author Martin Adamek
 */
public class MethodsNode extends AbstractNode {
    
    private final EjbViewController controller;

    public MethodsNode(Ejb model, EjbJar module, ClassPath srcPath, Children children) {
        this(new InstanceContent(), model, module, srcPath, children);
    }
    
    private MethodsNode(InstanceContent content, Ejb model, EjbJar module, ClassPath srcPath, Children children) {
        super(children, new AbstractLookup(content));
        controller = new EjbViewController(model, module, srcPath);
        content.add(this);
        content.add(controller.getBeanDo());
    }
    
    public Action[] getActions(boolean context) {
        return new SystemAction[] {
            SystemAction.get(AddActionGroup.class),
        };
    }

}
