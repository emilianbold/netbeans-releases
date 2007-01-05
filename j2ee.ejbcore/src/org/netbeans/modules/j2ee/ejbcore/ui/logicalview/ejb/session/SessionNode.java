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


package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.session;

import javax.swing.Action;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.api.ejbjar.EjbReference;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.DeleteEJBDialog;
import org.openide.actions.*;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.EjbTransferable;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.EjbViewController;
import org.openide.util.WeakListeners;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.AddActionGroup;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.GoToSourceActionGroup;
import org.openide.cookies.OpenCookie;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;


/**
 * @author Chris Webster
 * @author Ludovic Champenois
 * @author Martin Adamek
 */
public final class SessionNode extends AbstractNode implements OpenCookie {
    
    private final PropertyChangeListener nameChangeListener;
    private final EjbViewController ejbViewController;
    
    public SessionNode(Session session, EjbJar ejbJar, ClassPath classPath) {
        this(new InstanceContent(), session, ejbJar, classPath);
    }
    
    private SessionNode(InstanceContent instanceContent, Session session, EjbJar ejbJar, ClassPath classPath) {
        super(new SessionChildren(session, classPath), new AbstractLookup(instanceContent));
        setIconBaseWithExtension("org/netbeans/modules/j2ee/ejbcore/ui/logicalview/ejb/session/SessionNodeIcon.gif");
        setName(session.getEjbName() + "");
        ejbViewController = new EjbViewController(session, ejbJar, classPath);
        setDisplayName();
        nameChangeListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent pce) {
                setDisplayName();
            }
        };
        session.addPropertyChangeListener(WeakListeners.propertyChange(nameChangeListener, session));
        instanceContent.add(this);
        instanceContent.add(ejbViewController.getBeanClass());
        if (ejbViewController.getBeanDo() != null) {
            instanceContent.add(ejbViewController.getBeanDo());
        }
        EjbReference ejbReference = ejbViewController.createEjbReference();
        if (ejbReference != null) {
            instanceContent.add(ejbReference);
        }
    }
    
    private void setDisplayName() {
        setDisplayName(ejbViewController.getDisplayName());
    }
    
    public Action[] getActions(boolean context) {
        int nodesCount = Utilities.actionsGlobalContext().lookup(new Lookup.Template<Node>(Node.class)).allInstances().size();
        List<SystemAction> list = new ArrayList<SystemAction>();
        list.add(SystemAction.get(OpenAction.class));
        list.add(null);
        list.add(SystemAction.get(DeleteAction.class));
        if (nodesCount == 1) {
            list.add(SystemAction.get(AddActionGroup.class));
            list.add(null);
            list.add(SystemAction.get(GoToSourceActionGroup.class));
        }
        return list.toArray(new SystemAction[list.size()]);
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public boolean canDestroy() {
        return true;
    }
    
    public void destroy() throws IOException {
        super.destroy();
        String deleteOptions = DeleteEJBDialog.open(ejbViewController.getDisplayName());
        if (!deleteOptions.equals(DeleteEJBDialog.DELETE_NOTHING)) {
            if (deleteOptions.equals(DeleteEJBDialog.DELETE_ONLY_DD)) {
                ejbViewController.delete(false);
            } else {
                ejbViewController.delete(true);
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
        EjbReference ejbRef = ejbViewController.createEjbReference();
        StringBuffer ejbRefString = new StringBuffer();
        if (ejbRef.supportsRemoteInvocation()) {
            ejbRefString.append(ejbViewController.getRemoteStringRepresentation("Session"));
        }
        if (ejbRef.supportsLocalInvocation()) {
            ejbRefString.append(ejbViewController.getLocalStringRepresentation("Session"));
        }
        return new EjbTransferable(ejbRefString.toString(), ejbRef);
    }
    
    public Transferable clipboardCut() throws IOException {
        return clipboardCopy();
    }
    
    public void open() {
        DataObject dataObject = ejbViewController.getBeanDo();
        if (dataObject != null) {
            OpenCookie cookie = dataObject.getCookie(OpenCookie.class);
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
        if ("customDelete".equals(attributeName)) {
            retValue = Boolean.TRUE;
        } else {
            retValue = super.getValue(attributeName);
        }
        return retValue;
    }

}
