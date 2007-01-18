/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared;

import javax.swing.Action;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.AddActionGroup;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.GoToSourceAction;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Represents Local/Remote Methods node under Session and Entity nodes 
 * in EJB logical view
 *
 * @author Martin Adamek
 */
public class MethodsNode extends AbstractNode implements OpenCookie {
    
    private final EjbViewController controller;
    private EntityAndSession model;
    private ClassPath srcPath;
    private boolean local;

    public MethodsNode(EntityAndSession model, EjbJar module, ClassPath srcPath, Children children, boolean local) {
        this(new InstanceContent(), model, module, srcPath, children, local);
    }
    
    private MethodsNode(InstanceContent content, EntityAndSession model, EjbJar module, ClassPath srcPath, Children children, boolean local) {
        super(children, new AbstractLookup(content));
        controller = new EjbViewController(model, module, srcPath);
        this.model = model;
        this.srcPath = srcPath;
        this.local = local;
        content.add(this);
        if (controller.getBeanDo() != null) {
            content.add(controller.getBeanDo());
        }
    }
    
    public Action[] getActions(boolean context) {
        return new Action[] {
                new GoToSourceAction(srcPath, local ? model.getLocal() : model.getRemote(), 
                        NbBundle.getMessage(MethodsNode.class, "LBL_GoToSourceGroup")),
            SystemAction.get(AddActionGroup.class),
        };
    }

    public Action getPreferredAction() {
        return new GoToSourceAction(srcPath, local ? model.getLocal() : model.getRemote(), 
                        NbBundle.getMessage(MethodsNode.class, "LBL_GoToSourceGroup"));
    }

    public void open() {
        DataObject dataObject = controller.getBeanDo();
        if (dataObject != null) {
            OpenCookie cookie = (OpenCookie) dataObject.getCookie(OpenCookie.class);
            if(cookie != null){
                cookie.open();
            }
        }
    }
    
    public boolean isLocal() {
	return local;
    }
}
