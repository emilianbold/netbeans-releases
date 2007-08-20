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


package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.entity;
import javax.swing.Action;
import org.netbeans.modules.j2ee.api.ejbjar.EjbReference;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.AddActionGroup;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.GenerateDTOAction;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.DeleteEJBDialog;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.GoToSourceActionGroup;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.OpenAction;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;


/**
 * @author Chris Webster
 * @author Ludovic Champenois
 * @author Martin Adamek
 */
public class EntityNode extends AbstractNode implements OpenCookie {
    
    private final PropertyChangeListener nameChangeListener;
    private final EjbViewController controller;
    
    public static EntityNode create(String ejbClass, EjbJar ejbModule, Project project) {
        // XXX what is the best parameter to send? classpath, javasource, ...?
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
        try {
            return new EntityNode(new InstanceContent(), javaSource, ejbClass, ejbModule, project);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        return null;
    }

    private EntityNode(InstanceContent content, JavaSource javaSource, final String ejbClass, EjbJar ejbModule, Project project) throws IOException {
        super(new EntityChildren(javaSource, ejbClass, ejbModule), new AbstractLookup(content));
        setIconBaseWithExtension("org/netbeans/modules/j2ee/ejbcore/ui/logicalview/ejb/entity/EntityNodeIcon.gif");
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
        controller = new EjbViewController(ejbClass, ejbModule);
        setDisplayName();
        nameChangeListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent pce) {
                setDisplayName();
            }
        };
        //TODO: RETOUCHE listening on model for logical view
//        model.addPropertyChangeListener(WeakListeners.propertyChange(nameChangeListener, model));
        content.add(this);
        content.add(controller.getBeanClass());
        if (controller.getBeanDo() != null) {
            content.add(controller.getBeanDo());
        }
        try {
            content.add(controller.createEjbReference());
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }
    
    private void setDisplayName() {
        setDisplayName(controller.getDisplayName());
    }
    
    // Create the popup menu:
    public Action[] getActions(boolean context) {
        Collection<? extends Node> nodes = Utilities.actionsGlobalContext().lookup(new Lookup.Template<Node>(Node.class)).allInstances();
        List<SystemAction> list = new ArrayList<SystemAction>();
        list.add(SystemAction.get(OpenAction.class));
        if (nodes.size() == 1) {
            list.add(null);
            list.add(SystemAction.get(AddActionGroup.class));
            list.add(null);
            list.add(SystemAction.get(GoToSourceActionGroup.class));
            list.add(SystemAction.get(GenerateDTOAction.class));
        }
        return list.toArray(new SystemAction[0]);
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // TODO
        // return new HelpCtx(EntityNode.class);
    }
    
    public boolean canDestroy() {
        return true;
    }
    
    public void destroy() throws java.io.IOException {
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
        StringBuilder ejbRefString = new StringBuilder("");
        ejbRefString.append(controller.getLocalStringRepresentation("Entity"));
        return new EjbTransferable(ejbRefString.toString(), ejbRef);
    }
    
    public Transferable clipboardCut() throws IOException {
        return clipboardCopy();
    }
    
    public void open() {
        DataObject dataObject = controller.getBeanDo();
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
