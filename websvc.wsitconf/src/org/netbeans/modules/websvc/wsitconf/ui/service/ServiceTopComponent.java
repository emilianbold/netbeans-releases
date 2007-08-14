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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.wsitconf.ui.service;

import javax.swing.undo.UndoManager;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.xml.multiview.ui.InnerPanelFactory;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import java.awt.*;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;

/**
 * @author Martin Grebac
 */
public class ServiceTopComponent extends TopComponent {

    static final long serialVersionUID=6021472310161712674L;
    private boolean initialized = false;

    private static final Logger logger = Logger.getLogger(ServiceTopComponent.class.getName());
    
    private WSDLModel wsdlModel;
    private UndoManager undoManager;
    private Node node;
    private Service service;
    private boolean serviceOnly;
    private JaxWsModel jaxWsModel;
    private FileObject implClass;
    private Collection<Binding> bindings;
    
    public ServiceTopComponent(Service service, 
                JaxWsModel jaxWsModel, WSDLModel wsdlModel, Node node, UndoManager undoManager) {
        setLayout(new BorderLayout());
        this.wsdlModel = wsdlModel;
        this.undoManager = undoManager;
        this.node = node;
        this.service = service;
        this.jaxWsModel = jaxWsModel;
    }

    public ServiceTopComponent(WSDLModel wsdlModel, UndoManager undoManager, Collection<Binding> bindings, Node node) {
        setLayout(new BorderLayout());
        this.wsdlModel = wsdlModel;
        this.undoManager = undoManager;
        this.bindings = bindings;
        this.node = node;
    }

    public ServiceTopComponent(Service service, JaxWsModel jaxWsModel, WSDLModel wsdlModel, FileObject implClass, UndoManager undoManager, boolean serviceOnly) {
        setLayout(new BorderLayout());
        this.wsdlModel = wsdlModel;
        this.undoManager = undoManager;
        this.service = service;
        this.jaxWsModel = jaxWsModel;
        this.implClass = implClass;
        this.serviceOnly = serviceOnly;
    }
    
    @Override
    protected String preferredID(){
        return "WSITTopComponent";    //NOI18N
    }
    
    /**
     * #38900 - lazy addition of GUI components
     */
    private void doInitialize() {
        initAccessibility();
        ToolBarDesignEditor tb = new ToolBarDesignEditor();
        if (wsdlModel == null) {
            logger.log(Level.INFO, "WSDL Model not ready"); //NOI18N
            return;
        }
        ModelSource ms = wsdlModel.getModelSource();
        FileObject fo = org.netbeans.modules.xml.retriever.catalog.Utilities.getFileObject(ms);
        Project p = (fo != null) ? FileOwnerQuery.getOwner(fo) : null;
        InnerPanelFactory panelFactory = new ServicePanelFactory(tb, node, undoManager, p, jaxWsModel);
        ServiceView mview = new ServiceView(panelFactory, wsdlModel, node, implClass, service, bindings, serviceOnly);
        tb.setContentView(mview);
        add(tb);
        setFocusable(true);
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
    
    private void initAccessibility(){
        getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(ServiceTopComponent.class, "ACS_Tab_DESC")); // NOI18N
    }
    
    /**
     * #38900 - lazy addition of GUI components
     */    
    @Override
    public void addNotify() {
        if (!initialized) {
            initialized = true;
            doInitialize();
        }
        super.addNotify();
    }
    
    /**
     * Called when <code>TopComponent</code> is about to be shown.
     * Shown here means the component is selected or resides in it own cell
     * in container in its <code>Mode</code>. The container is visible and not minimized.
     * <p><em>Note:</em> component
     * is considered to be shown, even its container window
     * is overlapped by another window.</p>
     * @since 2.18
     *
     * #38900 - lazy addition of GUI components
     *
     */
    @Override
    protected void componentShowing() {
        if (!initialized) {
            initialized = true;
            doInitialize();
        }
        super.componentShowing();
    }
}

