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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.swing.Action;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer;
import org.netbeans.modules.j2ee.common.DatasourceUIHelper;
import org.netbeans.modules.j2ee.common.EventRequestProcessor;
import org.netbeans.modules.j2ee.common.EventRequestProcessor.AsynchronousAction;
import org.netbeans.modules.j2ee.common.EventRequestProcessor.Context;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore._RetoucheUtil;
import org.netbeans.modules.j2ee.ejbcore.action.UseDatabaseGenerator;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;


/**
 * Provide action for using a data source.
 * @author Chris Webster
 * @author Martin Adamek
 */
public class UseDatabaseAction extends NodeAction {
    
    protected void performAction(Node[] nodes) {
        if (nodes == null || nodes.length != 1) {
            return;
        }
        FileObject fileObject = nodes[0].getLookup().lookup(FileObject.class);
        try {
            ElementHandle<TypeElement> elementHandle = _RetoucheUtil.getJavaClassFromNode(nodes[0]);
            generate(fileObject, elementHandle);
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
    }
    
    private boolean generate(FileObject fileObject, ElementHandle<TypeElement> elementHandle) throws IOException {
        Project project = FileOwnerQuery.getOwner(fileObject);
        //make sure configuration is ready
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        j2eeModuleProvider.getConfigSupport().ensureConfigurationReady();
        EnterpriseReferenceContainer enterpriseReferenceContainer = project.getLookup().lookup(EnterpriseReferenceContainer.class);

        // get all the resources
        ResourcesHolder holder = getResources(j2eeModuleProvider, fileObject);
        
        SelectDatabasePanel selectDatabasePanel = new SelectDatabasePanel(
                j2eeModuleProvider,
                enterpriseReferenceContainer.getServiceLocatorName(),
                holder.getReferences(),
                holder.getModuleDataSources(),
                holder.getServerDataSources());
        final DialogDescriptor dialogDescriptor = new DialogDescriptor(
                selectDatabasePanel,
                NbBundle.getMessage(UseDatabaseAction.class, "LBL_ChooseDatabase"), //NOI18N
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(SelectDatabasePanel.class),
                null
                );
        //#73163: disable OK button when no db connections are available
        dialogDescriptor.setValid(checkConnections());
        selectDatabasePanel.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(SelectDatabasePanel.IS_VALID)) {
                    Object newvalue = evt.getNewValue();
                    if ((newvalue != null) && (newvalue instanceof Boolean)) {
                        dialogDescriptor.setValid(((Boolean)newvalue).booleanValue() && checkConnections());
                    }
                }
            }
        });
        
        Object option = DialogDisplayer.getDefault().notify(dialogDescriptor);
        if (option == NotifyDescriptor.OK_OPTION) {
            String refName = selectDatabasePanel.getDatasourceReference();
            
            UseDatabaseGenerator generator = new UseDatabaseGenerator();
            try {
                generator.generate(
                        fileObject,
                        elementHandle,
                        j2eeModuleProvider,
                        refName,
                        selectDatabasePanel.getDatasource(),
                        selectDatabasePanel.createServerResources(),
                        selectDatabasePanel.getServiceLocator()
                        );
            }
            catch (ConfigurationException ex) {
                //TODO
            }
        }
        return false;
    }
    
    /** Get references, module- and server datasources. */
    private ResourcesHolder getResources(final J2eeModuleProvider j2eeModuleProvider, final FileObject fileObject) {
        
        final ResourcesHolder holder = new ResourcesHolder();
        
        // fetch references & datasources asynchronously
        Collection<EventRequestProcessor.Action> asyncActions = new ArrayList<EventRequestProcessor.Action>(1);
        asyncActions.add(new AsynchronousAction() {

            public void run(Context actionContext) {
                String msg = NbBundle.getMessage(DatasourceUIHelper.class, "MSG_retrievingDS"); //NOI18N
                actionContext.getProgress().progress(msg);
                try {
                    populateDataSourceReferences(holder, j2eeModuleProvider, fileObject);
                } catch (ConfigurationException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
        });
        
        EventRequestProcessor erp = new EventRequestProcessor();
        erp.invoke(asyncActions);
        
        return holder;
    }
    
    // this method has to called asynchronously!
    private void populateDataSourceReferences(final ResourcesHolder holder, final J2eeModuleProvider j2eeModuleProvider,
            final FileObject fileObject) throws ConfigurationException {
        
        HashMap<String, Datasource> references = new HashMap<String, Datasource>();
        holder.setReferences(references);
        holder.setModuleDataSources(j2eeModuleProvider.getModuleDatasources());
        holder.setServerDataSources(j2eeModuleProvider.getServerDatasources());
        
        if (j2eeModuleProvider.getJ2eeModule().getModuleType().equals(J2eeModule.EJB)) {
            EjbJar dd = findEjbDDRoot(fileObject);
            if (dd == null) {
                return;
            }
            EnterpriseBeans beans = dd.getEnterpriseBeans();
            if (beans == null) {
                return;
            }
            
            Ejb[] ejbs = beans.getEjbs();
            for (Ejb ejb : ejbs) {
                ResourceRef[] refs = ejb.getResourceRef();
                for (ResourceRef ref : refs) {
                    String refName = ref.getResRefName();
                    Datasource ds = findDatasourceForReferenceForEjb(holder, j2eeModuleProvider, refName, ejb.getEjbName());
                    if (ds != null) {
                        references.put(refName, ds);
                    }
                }
            }
        }
        else
        if (j2eeModuleProvider.getJ2eeModule().getModuleType().equals(J2eeModule.WAR)) {
            WebApp dd = findWebDDRoot(fileObject);
            if (dd == null) {
                return;
            }
            ResourceRef[] refs = dd.getResourceRef();
            for (ResourceRef ref : refs) {
                String refName = ref.getResRefName();
                Datasource ds = findDatasourceForReference(holder, j2eeModuleProvider, refName);
                if (ds != null) {
                    references.put(refName, ds);
                }
            }
        }
    }

    private Datasource findDatasourceForReference(final ResourcesHolder holder, J2eeModuleProvider j2eeModuleProvider, String referenceName) throws ConfigurationException {
        String jndiName = j2eeModuleProvider.getConfigSupport().findDatasourceJndiName(referenceName);
        if (jndiName == null) {
            return null;
        }
        return findDataSource(holder, jndiName);
    }
    
    public Datasource findDatasourceForReferenceForEjb(final ResourcesHolder holder, J2eeModuleProvider j2eeModuleProvider, String referenceName, String ejbName) throws ConfigurationException {
        String jndiName = j2eeModuleProvider.getConfigSupport().findDatasourceJndiNameForEjb(ejbName, referenceName);
        if (jndiName == null) {
            return null;
        }
        return findDataSource(holder, jndiName);
    }
    
    // this is faster implementation than in API (@see ConfigSupportImpl#findDatasource())
    // TODO this method (as well as API method) should not use <code>equals()</code>
    private Datasource findDataSource(ResourcesHolder holder, String jndiName) {
        
        assert holder != null;
        assert jndiName != null;
        
        // project ds
        for (Datasource ds : holder.getModuleDataSources()) {
            if (jndiName.equals(ds.getJndiName())) {
                return ds;
            }
        }
        for (Datasource ds : holder.getServerDataSources()) {
            if (jndiName.equals(ds.getJndiName())) {
                return ds;
            }
        }
        
        return null;
    }
    
    private EjbJar findEjbDDRoot(FileObject fileObject) throws ConfigurationException {
        org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbJar = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(fileObject);
        assert ejbJar != null;
        try {
            return org.netbeans.modules.j2ee.dd.api.ejb.DDProvider.getDefault().getMergedDDRoot(ejbJar.getMetadataUnit());
        }
        catch (IOException ioe) {
            String msg = NbBundle.getMessage(UseDatabaseAction.class, "ERR_CannotReadEjbDD");
            throw new ConfigurationException(msg, ioe);
        }
    }
    
    private WebApp findWebDDRoot(FileObject fileObject) throws ConfigurationException {
        WebModule mod = WebModule.getWebModule(fileObject);
        try {
            return org.netbeans.modules.j2ee.dd.api.web.DDProvider.getDefault().getMergedDDRoot(mod);
        }
        catch (IOException ioe) {
            String msg = NbBundle.getMessage(UseDatabaseAction.class, "ERR_CannotReadWebDD");
            throw new ConfigurationException(msg, ioe);
        }
    }
        
    protected boolean enable(Node[] nodes) {
        if (nodes == null || nodes.length != 1) {
            return false;
        }
        FileObject fileObject = nodes[0].getLookup().lookup(FileObject.class);
        if (fileObject == null) {
            return false;
        }
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        final boolean[] isInterface = new boolean[1];
        try {
            final ElementHandle<TypeElement> elementHandle = _RetoucheUtil.getJavaClassFromNode(nodes[0]);
            if (elementHandle == null || javaSource == null) {
                return false;
            }
            javaSource.runUserActionTask(new AbstractTask<CompilationController>() {
                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    TypeElement typeElement = elementHandle.resolve(controller);
                    isInterface[0] = ElementKind.INTERFACE == typeElement.getKind();
                }
            }, true);
            return elementHandle == null ? false : !isInterface[0];
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
        return false;
    }
    
    private boolean checkConnections() {
        return ConnectionManager.getDefault().getConnections().length > 0;
    }
    
    public String getName() {
        return NbBundle.getMessage(UseDatabaseAction.class, "LBL_UseDbAction"); // NOI18N
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    protected void initialize() {
        super.initialize();
        putProperty(Action.SHORT_DESCRIPTION, NbBundle.getMessage(UseDatabaseAction.class, "HINT_UseDbAction")); // NOI18N
    }
    
    /**
     * Just holder for few properties.
     */
    private static final class ResourcesHolder {
        private Map<String, Datasource> references;
        private Set<Datasource> moduleDataSources;
        private Set<Datasource> serverDataSources;
        
        public ResourcesHolder() {
        }

        public void setReferences(final Map<String, Datasource> references) {
            this.references = references;
        }

        public void setModuleDataSources(final Set<Datasource> moduleDataSources) {
            this.moduleDataSources = moduleDataSources;
        }

        public void setServerDataSources(final Set<Datasource> serverDataSources) {
            this.serverDataSources = serverDataSources;
        }

        public Map<String, Datasource> getReferences() {
            if (references == null) {
                references = new HashMap<String, Datasource>();
            }
            return references;
        }

        public Set<Datasource> getModuleDataSources() {
            if (moduleDataSources == null) {
                moduleDataSources = new HashSet<Datasource>();
            }
            return moduleDataSources;
        }

        public Set<Datasource> getServerDataSources() {
            if (moduleDataSources == null) {
                moduleDataSources = new HashSet<Datasource>();
            }
            return serverDataSources;
        }
        
    }
}
