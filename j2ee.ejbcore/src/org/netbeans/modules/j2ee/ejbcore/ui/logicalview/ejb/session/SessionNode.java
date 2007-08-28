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
import org.netbeans.modules.j2ee.api.ejbjar.EjbReference;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.DeleteEJBDialog;
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
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.EjbTransferable;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.EjbViewController;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.AddActionGroup;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.GoToSourceActionGroup;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.actions.OpenAction;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
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
    
    public static SessionNode create(String ejbClass, EjbJar ejbModule, Project project) {
        JavaSource javaSource = null;
        FileObject[] javaSources = ejbModule.getJavaSources();
        if (javaSources.length > 0) {
            ClasspathInfo cpInfo = ClasspathInfo.create(
                    ClassPath.getClassPath(javaSources[0], ClassPath.BOOT),
                    ClassPath.getClassPath(javaSources[0], ClassPath.COMPILE),
                    ClassPath.getClassPath(javaSources[0], ClassPath.SOURCE)
                    );
            javaSource = JavaSource.create(cpInfo);
        }
        assert javaSource != null;
        return new SessionNode(new InstanceContent(), javaSource, ejbClass, ejbModule);
    }
    
    private SessionNode(InstanceContent instanceContent, JavaSource javaSource, final String ejbClass, EjbJar ejbModule) {
        super(new SessionChildren(javaSource, ejbClass, ejbModule), new AbstractLookup(instanceContent));
        setIconBaseWithExtension("org/netbeans/modules/j2ee/ejbcore/ui/logicalview/ejb/session/SessionNodeIcon.gif");
        String ejbName = null;
        try {
            ejbName = ejbModule.getMetadataModel().runReadAction(new MetadataModelAction<EjbJarMetadata, String>() {
                public String run(EjbJarMetadata metadata) throws Exception {
                    Ejb ejb = metadata.findByEjbClass(ejbClass);
                    return ejb == null ? null : ejb.getEjbName();
                }
            });
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        setName(ejbName + "");
        ejbViewController = new EjbViewController(ejbClass, ejbModule);
        setDisplayName();
        nameChangeListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent pce) {
                setDisplayName();
            }
        };
        //TODO: RETOUCHE listening on model for logical view
//        session.addPropertyChangeListener(WeakListeners.propertyChange(nameChangeListener, session));
        instanceContent.add(this);
        instanceContent.add(ejbViewController.getBeanClass());
        if (ejbViewController.getBeanDo() != null) {
            instanceContent.add(ejbViewController.getBeanDo().getPrimaryFile());
        }
        try {
            instanceContent.add(ejbViewController.createEjbReference());
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }
    
    private void setDisplayName() {
        setDisplayName(ejbViewController.getDisplayName());
    }
    
    public Action[] getActions(boolean context) {
        int nodesCount = Utilities.actionsGlobalContext().lookup(new Lookup.Template<Node>(Node.class)).allInstances().size();
        List<SystemAction> list = new ArrayList<SystemAction>();
        list.add(SystemAction.get(OpenAction.class));
        if (nodesCount == 1) {
            list.add(null);
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
        ejbRefString.append(ejbViewController.getLocalStringRepresentation("Session"));
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
