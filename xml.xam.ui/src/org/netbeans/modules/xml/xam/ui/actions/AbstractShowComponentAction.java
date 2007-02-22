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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.xam.ui.actions;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ui.cookies.GetComponentCookie;
import org.netbeans.modules.xml.xam.ui.cookies.ViewComponentCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.actions.CookieAction;

/**
 *
 * @author Ajit Bhate
 */
public abstract class AbstractShowComponentAction extends CookieAction {
    static final long serialVersionUID = 1L;
    
    public AbstractShowComponentAction() {
        super();
    }
    
    
    /**
     *
     * Runs in same thread.
     */
    protected boolean asynchronous() {
        return false;
    }
    
    
    final protected boolean enable(Node[] nodes) {
        return nodes!=null && super.enable(nodes) &&
                getViewCookie(getComponent(nodes[0])) != null;
    }
    
    /**
     *
     *
     */
    protected void performAction(Node[] nodes) {
        Component c = getComponent(nodes[0]);
        ViewComponentCookie cake = getViewCookie(c);
        if(cake!=null) cake.view(getView(),c);
    }
    
    protected abstract ViewComponentCookie.View getView();
    
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }
    
    protected Class[] cookieClasses() {
        return new Class[]{GetComponentCookie.class,Component.class};
    }
    
    protected Component getComponent(Node n) {
        GetComponentCookie cake = (GetComponentCookie)n.
                getCookie(GetComponentCookie.class);
        Component component = null;
        try {
            if(cake!=null) component = cake.getComponent();
        } catch (IllegalStateException ise) {
            // Happens if the component is no longer in the model.
            // Ignore this here since the subclass will deal with it.
        }
        if(component==null)
            component = (Component) n.getLookup().lookup(Component.class);
        return component;
    }
    
    private ViewComponentCookie getViewCookie(Component c) {
        if(c==null) return null;
        try {
            Model model = c.getModel();
            if (model != null) {
                FileObject fobj = (FileObject) model.getModelSource().
                        getLookup().lookup(FileObject.class);
                if (fobj != null) {
                    DataObject dobj = DataObject.find(fobj);
                    if(dobj!=null) {
                        ViewComponentCookie cake = (ViewComponentCookie) dobj.
                                getCookie(ViewComponentCookie.class);
                        if(cake!=null && cake.canView(getView(), c))
                            return cake;
                    }
                }
            }
        } catch (DataObjectNotFoundException donfe) {
            // fall through to return null
        }
        return null;
    }
}
