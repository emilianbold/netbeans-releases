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

package org.netbeans.modules.j2ee.jboss4.config;

import java.beans.PropertyChangeEvent;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.spi.DConfigBeanRoot;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.exceptions.BeanNotFoundException;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.jboss4.config.gen.Datasources;
import org.netbeans.modules.j2ee.jboss4.config.gen.LocalTxDatasource;
import org.netbeans.modules.schema2beans.BaseBean;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;

/** 
 * Base for JBoss DeploymentConfiguration implementations.
 *
 * @author  Pavel Buzek, lkotouc
 */
public abstract class JBDeploymentConfiguration implements DeploymentConfiguration {
    
    protected static final String JBOSS4_DATASOURCE_JNDI_PREFIX = "java:"; // NOI18N
    protected static final String JBOSS4_MAIL_SERVICE_JNDI_NAME = "java:Mail"; // NOI18N
    protected static final String JBOSS4_CONN_FACTORY_JNDI_NAME = "ConnectionFactory"; // NOI18N
    protected static final String JBOSS4_EJB_JNDI_PREFIX = "java:comp/env/"; // NOI18N

    static final String JBOSS4_MSG_QUEUE_JNDI_PREFIX = "queue/"; // NOI18N
    static final String JBOSS4_MSG_TOPIC_JNDI_PREFIX = "topic/"; // NOI18N

    private static final String DS_RESOURCE_NAME = "jboss-ds.xml"; // NOI18N
    
    //JSR-88 deployable object - initialized when instance is constructed
    protected DeployableObject deplObj;
    
    //cached data object for the server-specific configuration file (initialized by the subclasses)
    protected DataObject deploymentDescriptorDO;
    
    //the directory with resources - supplied by the configuration support in the construction time
    private File resourceDir;

    //model of the data source file
    private Datasources datasources;
    //data source file (placed in the resourceDir)
    private File datasourcesFile;
    //cached data object for the data source file
    private DataObject datasourcesDO;
    
    /** Creates a new instance of JBDeploymentConfiguration */
    public JBDeploymentConfiguration (DeployableObject deplObj) {
        this.deplObj = deplObj;
    }

    /**
     * Initialization of the common data used by the subclasses.
     * 
     * @param resourceDir   directory containing definition for enterprise resources.
     */
    protected void init(File resourceDir) {
        this.resourceDir = resourceDir;
        datasourcesFile = new File(resourceDir, DS_RESOURCE_NAME);
        if (datasourcesFile.exists()) {
            try {
                ensureDatasourcesDOExists();
            } catch (DataObjectNotFoundException donfe) {
                ErrorManager.getDefault().notify(donfe);
            }
        }
    }
            
    // JSR-88 methods ---------------------------------------------------------
    
    public DeployableObject getDeployableObject () {
        return deplObj;
    }
    
    // JSR-88 methods empty implementation ------------------------------------
    
    public DConfigBeanRoot getDConfigBeanRoot (DDBeanRoot dDBeanRoot) 
    throws ConfigurationException {
        return null;
    }
    
    public void removeDConfigBean (DConfigBeanRoot dConfigBeanRoot) 
    throws BeanNotFoundException {
        throw new BeanNotFoundException ("bean not found "+dConfigBeanRoot); // NOI18N
    }
    
    public void restore (InputStream is) 
    throws ConfigurationException {
    }
    
    public DConfigBeanRoot restoreDConfigBean (InputStream is, DDBeanRoot dDBeanRoot) 
    throws ConfigurationException {
        return null;
    }
    
    public void saveDConfigBean (OutputStream os, DConfigBeanRoot dConfigBeanRoot) 
    throws ConfigurationException {
    }
    
    // helper methods -------------------------------------------------
    
    protected void writefile(final File file, final BaseBean bean) throws ConfigurationException {
        try {
            FileObject cfolder = FileUtil.toFileObject(file.getParentFile());
            if (cfolder == null) {
                File parentFile = file.getParentFile();
                try {
                    cfolder = FileUtil.toFileObject(parentFile.getParentFile()).createFolder(parentFile.getName());
                } catch (IOException ioe) {
                    throw new ConfigurationException(NbBundle.getMessage(JBDeploymentConfiguration.class, "MSG_FailedToCreateConfigFolder", parentFile.getAbsolutePath()));
                }
            }
            final FileObject folder = cfolder;
            FileSystem fs = folder.getFileSystem();
            fs.runAtomicAction(new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    OutputStream os = null;
                    FileLock lock = null;
                    try {
                        String name = file.getName();
                        FileObject configFO = folder.getFileObject(name);
                        if (configFO == null) {
                            configFO = folder.createData(name);
                        }
                        lock = configFO.lock();
                        os = new BufferedOutputStream (configFO.getOutputStream(lock), 4086);
                        // TODO notification needed
                        if (bean != null) {
                            bean.write(os);
                        }
                    } finally {
                        if (os != null) {
                            try { os.close(); } catch(IOException ioe) {}
                        }
                        if (lock != null) 
                            lock.releaseLock();
                    }
                }
            });
        } catch (IOException e) {
            throw new ConfigurationException (e.getLocalizedMessage ());
        }
    }
    
    // ---------------------------- resource generation ----------------------------------------

    private abstract class DSResourceModifier {
        String jndiName;
        String url;
        String username;
        String password;
        String driver;

        DSResourceModifier(String jndiName, String  url, String username, String password, String driver) {
            this.jndiName = jndiName;
            this.url = url;
            this.username = username;
            this.password = password;
            this.driver = driver;
        }
        
        abstract JBossDatasource modify(Datasources datasources) throws DatasourceAlreadyExistsException;
    }
    
    protected Set<Datasource> getDatasources() {
        
        HashSet<Datasource> projectDS = new HashSet<Datasource>();
        Datasources dss = getDatasourcesGraph();
        if (dss != null) {
            LocalTxDatasource ltxds[] = datasources.getLocalTxDatasource();
            for (int i = 0; i < ltxds.length; i++) {
                if (ltxds[i].getJndiName().length() > 0) {
                    projectDS.add(new JBossDatasource(
                                    ltxds[i].getJndiName(),
                                    ltxds[i].getConnectionUrl(),
                                    ltxds[i].getUserName(),
                                    ltxds[i].getPassword(),
                                    ltxds[i].getDriverClass()));
                }
            }
        }
        
        return projectDS;
    }
    
    public JBossDatasource createDatasource(String jndiName, String  url, String username, String password, String driver) 
    throws OperationUnsupportedException, ConfigurationException, DatasourceAlreadyExistsException
    {
        JBossDatasource ds = modifyDSResource(new DSResourceModifier(jndiName, url, username, password, driver) {
            JBossDatasource modify(Datasources datasources) throws DatasourceAlreadyExistsException {
               
                LocalTxDatasource ltxds[] = datasources.getLocalTxDatasource();
                for (int i = 0; i < ltxds.length; i++) {
                    String jndiName = ltxds[i].getJndiName();
                    if (this.jndiName.equals(jndiName)) {
                        //already exists
                        JBossDatasource ds = new JBossDatasource(
                                jndiName,
                                ltxds[i].getConnectionUrl(),
                                ltxds[i].getUserName(),
                                ltxds[i].getPassword(),
                                ltxds[i].getDriverClass());
                        
                        throw new DatasourceAlreadyExistsException(ds);
                    }
                }
                
                LocalTxDatasource lds = new LocalTxDatasource();
                lds.setJndiName(jndiName);
                lds.setConnectionUrl(url);
                lds.setDriverClass(driver);
                lds.setUserName(username);
                lds.setPassword(password);
                lds.setMinPoolSize("5");
                lds.setMaxPoolSize("20");
                lds.setIdleTimeoutMinutes("5");

                datasources.addLocalTxDatasource(lds);
                
                return new JBossDatasource(jndiName, url, username, password, driver);
           }
        });
        
        return ds;
    }
    
    /**
     * Return Datasources graph. If it was not created yet, load it from the file
     * and cache it. If the file does not exist, generate it.
     *
     * @return Datasources graph or null if the jboss-ds.xml file is not parseable.
     */
    private synchronized Datasources getDatasourcesGraph() {
        
        try {
            if (datasourcesFile.exists()) {
                // load configuration if already exists
                try {
                    if (datasources == null)
                        datasources = Datasources.createGraph(datasourcesFile);
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ioe);
                } catch (RuntimeException re) {
                    // jboss-ds.xml is not parseable, do nothing
                }
            } else {
                // create jboss-ds.xml if it does not exist yet
                datasources = new Datasources();
                writefile(datasourcesFile, datasources);
            }
        } catch (ConfigurationException ce) {
            ErrorManager.getDefault().notify(ce);
        }

        return datasources;
    }
    
    private void ensureResourceDirExists() {
        if (!resourceDir.exists())
            resourceDir.mkdir();
    }

    private void ensureDatasourcesFilesExists() {
        if (!datasourcesFile.exists())
            getDatasourcesGraph();
    }
    
    /**
     * Listener of jboss-ds.xml document changes.
     */
    private class DatasourceFileListener extends FileChangeAdapter {
        
        public void fileChanged(FileEvent fe) {
            assert(fe.getSource() == datasourcesDO.getPrimaryFile());
            datasources = null;
        }

        public void fileDeleted(FileEvent fe) {
            assert(fe.getSource() == datasourcesDO.getPrimaryFile());
            datasources = null;
        }
    } 
    
    private void ensureDatasourcesDOExists() throws DataObjectNotFoundException {
        if (datasourcesDO == null || !datasourcesDO.isValid()) {
            FileObject datasourcesFO = FileUtil.toFileObject(datasourcesFile);
            assert(datasourcesFO != null);
            datasourcesDO = DataObject.find(datasourcesFO);
            datasourcesDO.getPrimaryFile().addFileChangeListener(new DatasourceFileListener());
        }
    }
    
    public synchronized void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == DataObject.PROP_MODIFIED &&
                evt.getNewValue() == Boolean.FALSE) {
            
            if (evt.getSource() == datasourcesDO) // dataobject has been modified, datasources graph is out of sync
                datasources = null;
        }
    }    
    /**
     * Perform datasources graph changes defined by the jbossWeb modifier. Update editor
     * content and save changes, if appropriate.
     *
     * @param modifier
     */
    private JBossDatasource modifyDSResource(DSResourceModifier modifier) 
    throws ConfigurationException, DatasourceAlreadyExistsException {

        JBossDatasource ds = null;
        
        try {
            ensureResourceDirExists();
            ensureDatasourcesFilesExists();
            ensureDatasourcesDOExists();

            EditorCookie editor = (EditorCookie)datasourcesDO.getCookie(EditorCookie.class);
            StyledDocument doc = editor.getDocument();
            if (doc == null)
                doc = editor.openDocument();

            Datasources newDatasources = null;
            try {  // get the up-to-date model
                // try to create a graph from the editor content
                byte[] docString = doc.getText(0, doc.getLength()).getBytes();
                newDatasources = Datasources.createGraph(new ByteArrayInputStream(docString));
            } catch (RuntimeException e) {
                Datasources oldDatasources = getDatasourcesGraph();
                if (oldDatasources == null) {
                    // neither the old graph is parseable, there is not much we can do here
                    // TODO: should we notify the user?
                    throw new ConfigurationException(
                            NbBundle.getMessage(JBDeploymentConfiguration.class, "MSG_datasourcesXmlCannotParse", DS_RESOURCE_NAME)); // NOI18N
                }
                // current editor content is not parseable, ask whether to override or not
                NotifyDescriptor notDesc = new NotifyDescriptor.Confirmation(
                        NbBundle.getMessage(JBDeploymentConfiguration.class, "MSG_datasourcesXmlNotValid", DS_RESOURCE_NAME),
                        NotifyDescriptor.OK_CANCEL_OPTION);
                Object result = DialogDisplayer.getDefault().notify(notDesc);
                if (result == NotifyDescriptor.CANCEL_OPTION) {
                    // keep the old content
                    return null;
                }
                // use the old graph
                newDatasources = oldDatasources;
            }

            // perform changes
            ds = modifier.modify(newDatasources);

            // save, if appropriate
            boolean modified = datasourcesDO.isModified();
            replaceDocument(doc, newDatasources);
            if (!modified) {
                SaveCookie cookie = (SaveCookie)datasourcesDO.getCookie(SaveCookie.class);
                cookie.save();
            }

            datasources = newDatasources;

        } catch(DataObjectNotFoundException donfe) {
            ErrorManager.getDefault().notify(donfe);
        } catch (BadLocationException ble) {
            throw (ConfigurationException)(new ConfigurationException().initCause(ble));
        } catch (IOException ioe) {
            throw (ConfigurationException)(new ConfigurationException().initCause(ioe));
        }
        
        return ds;
    }

    /**
     * Replace the content of the document by the graph.
     */
    protected void replaceDocument(final StyledDocument doc, BaseBean graph) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            graph.write(out);
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
        NbDocument.runAtomic(doc, new Runnable() {
            public void run() {
                try {
                    doc.remove(0, doc.getLength());
                    doc.insertString(0, out.toString(), null);
                } catch (BadLocationException ble) {
                    ErrorManager.getDefault().notify(ble);
                }
            }
        });
    }
    
    
}
