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


package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.session;

import javax.swing.Action;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.DeleteEJBDialog;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.GoToSourceActionGroup;
import org.openide.actions.*;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.EjbTransferable;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.EjbViewController;
import org.openide.util.WeakListeners;
import org.netbeans.modules.j2ee.api.ejbjar.EjbReference;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.AddActionGroup;
import org.openide.cookies.OpenCookie;
import org.openide.nodes.AbstractNode;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;


/**
 * @author Chris Webster
 * @author Ludovic Champenois
 * @author Martin Adamek
 */
public class SessionNode extends AbstractNode implements OpenCookie {
    
    private final PropertyChangeListener nameChangeListener;
    private final EjbViewController controller;
    
    public SessionNode(Session model, EjbJar module, ClassPath srcPath) {
        this(new InstanceContent(), model, module, srcPath);
    }
    
    private SessionNode(InstanceContent content, Session model, EjbJar module, ClassPath srcPath) {
        super(new SessionChildren(model, srcPath, module), new AbstractLookup(content));
        setIconBase("org/netbeans/modules/j2ee/ejbcore/ui/logicalview/ejb/session/SessionNodeIcon");
        setName(model.getEjbName()+"");
        controller = new EjbViewController(model, module, srcPath);
        setDisplayName();
        nameChangeListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent pce) {
                setDisplayName();
            }
        };
        model.addPropertyChangeListener(
            WeakListeners.propertyChange(nameChangeListener,model));
        content.add(this);
        content.add(controller.getBeanClass());
        content.add(controller.getBeanDo());
        content.add(controller.createEjbReference());
    }
    
    private void setDisplayName() {
        setDisplayName(controller.getDisplayName());
    }
    
    public Action[] getActions(boolean context) {
        return new SystemAction[] {
            SystemAction.get(OpenAction.class),
            null,
            SystemAction.get(AddActionGroup.class),
            SystemAction.get(DeleteAction.class),
            null,
            SystemAction.get(GoToSourceActionGroup.class)
        };
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // TODO
        // return new HelpCtx(SessionNode.class);
    }
    
    public boolean canDestroy() {
        return true;
    }
    
    public void destroy() throws java.io.IOException {
        super.destroy();
        String deleteOptions = DeleteEJBDialog.open(controller.getDisplayName());
        if (!deleteOptions.equals(DeleteEJBDialog.DELETE_NOTHING)) {
            if (deleteOptions.equals(DeleteEJBDialog.DELETE_ONLY_DD)) {
                controller.delete(false);
            } else {
                controller.delete(true);
            }
        }
    }
    
    public boolean canCopy() {
        return true;
    }
    
    public boolean canCut() {
        return true;
    }
    
    public Transferable clipboardCopy() throws IOException {
        EjbReference ejbRef = controller.createEjbReference();
        String ejbRefString = "";
        if (ejbRef.supportsRemoteInvocation()) {
            ejbRefString += controller.getRemoteStringRepresentation("Session");
        }
        if (ejbRef.supportsLocalInvocation()) {
            ejbRefString += controller.getLocalStringRepresentation("Session");
        }
        return new EjbTransferable(ejbRefString,ejbRef);
    }
    
    public Transferable clipboardCut() throws IOException {
        return clipboardCopy();
    }
    
    public void open() {
        DataObject ce = controller.getBeanDo();
        if (ce != null) {
            OpenCookie cookie = (OpenCookie) ce.getCookie(OpenCookie.class);
            if(cookie != null){
                cookie.open();
            }
        }
    }
    
    public Action getPreferredAction() {
        return SystemAction.get(OpenAction.class);
    }
    
    /**
     * Adds possibility to display custom delete dialog
     */
    public Object getValue(String attributeName) {
        Object retValue;
        if (attributeName.equals("customDelete")) {
            retValue = Boolean.TRUE;
        } else {
            retValue = super.getValue(attributeName);
        }
        return retValue;
    }

}
