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

package org.netbeans.modules.j2ee.jboss4.config.ds;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.jboss4.config.JBossDatasource;
import org.netbeans.modules.j2ee.jboss4.config.ResourceConfigurationHelper;
import org.netbeans.modules.j2ee.jboss4.config.gen.Datasources;
import org.netbeans.modules.j2ee.jboss4.config.gen.LocalTxDatasource;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Libor Kotouc
 */
public class DatasourceSupport {
    
    private static final String DS_RESOURCE_NAME = "jboss-ds.xml"; // NOI18N
    
    // the directory with resources - supplied by the configuration support in the construction time
    private File resourceDir;

    //model of the data source file
    private Datasources datasources;

    // data source file (placed in the resourceDir)
    private File datasourcesFile;

    //destination service file object
    private FileObject datasourcesFO;
    
    public DatasourceSupport(File resourceDir) {
        this.resourceDir = resourceDir;
        this.datasourcesFile = new File(resourceDir, DS_RESOURCE_NAME);
        ensureDatasourcesFOExists();
    }
    
    /**
     * Listener of jboss-ds.xml document changes.
     */
    private class DatasourceFileListener extends FileChangeAdapter {
        
        public void fileChanged(FileEvent fe) {
            assert(fe.getSource() == datasourcesFO);
            datasources = null;
        }

        public void fileDeleted(FileEvent fe) {
            assert(fe.getSource() == datasourcesFO);
            datasources = null;
        }
    } 

    private void ensureDatasourcesFOExists() {
        if (!datasourcesFile.exists()) {
            return;
        }
        if (datasourcesFO == null || !datasourcesFO.isValid()) {
            datasourcesFO = FileUtil.toFileObject(datasourcesFile);
            assert(datasourcesFO != null);
            datasourcesFO.addFileChangeListener(new DatasourceFileListener());
        }
    }
    
//---------------------------------------- READING --------------------------------------
    
    public Set<Datasource> getDatasources() throws ConfigurationException {
        
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
                    Exceptions.printStackTrace(ioe);
                } catch (RuntimeException re) {
                    // jboss-ds.xml is not parseable, do nothing
                }
            } else {
                // create jboss-ds.xml if it does not exist yet
                datasources = new Datasources();
                ResourceConfigurationHelper.writeFile(datasourcesFile, datasources);
                ensureDatasourcesFOExists();
            }
        } catch (ConfigurationException ce) {
            Exceptions.printStackTrace(ce);
        }

        return datasources;
    }
    
//---------------------------------------- WRITING --------------------------------------
    
    private abstract class DSResourceModifier {
        String rawName;
        String url;
        String username;
        String password;
        String driver;

        DSResourceModifier(String jndiName, String  url, String username, String password, String driver) {
            this.rawName = JBossDatasource.getRawName(jndiName);
            this.url = url;
            this.username = username;
            this.password = password;
            this.driver = driver;
        }
        
        abstract JBossDatasource modify(Datasources datasources) throws DatasourceAlreadyExistsException;
    }
    
    public JBossDatasource createDatasource(String jndiName, String  url, String username, String password, String driver) 
    throws UnsupportedOperationException, ConfigurationException, DatasourceAlreadyExistsException
    {
        JBossDatasource ds = modifyDSResource(new DSResourceModifier(jndiName, url, username, password, driver) {
            JBossDatasource modify(Datasources datasources) throws DatasourceAlreadyExistsException {
               
                LocalTxDatasource ltxds[] = datasources.getLocalTxDatasource();
                for (int i = 0; i < ltxds.length; i++) {
                    String jndiName = ltxds[i].getJndiName();
                    if (rawName.equals(JBossDatasource.getRawName(jndiName))) {
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
                lds.setJndiName(rawName);
                lds.setConnectionUrl(url);
                lds.setDriverClass(driver);
                lds.setUserName(username);
                lds.setPassword(password);
                lds.setMinPoolSize("5");
                lds.setMaxPoolSize("20");
                lds.setIdleTimeoutMinutes("5");

                datasources.addLocalTxDatasource(lds);
                
                return new JBossDatasource(rawName, url, username, password, driver);
           }
        });
        
        return ds;
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
            ensureDatasourcesFileExists();
            
            DataObject datasourcesDO = DataObject.find(datasourcesFO);

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
                            NbBundle.getMessage(DatasourceSupport.class, "MSG_datasourcesXmlCannotParse", DS_RESOURCE_NAME)); // NOI18N
                }
                // current editor content is not parseable, ask whether to override or not
                NotifyDescriptor notDesc = new NotifyDescriptor.Confirmation(
                        NbBundle.getMessage(DatasourceSupport.class, "MSG_datasourcesXmlNotValid", DS_RESOURCE_NAME),       // NOI18N
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

            // save if appropriate
            boolean modified = datasourcesDO.isModified();
            ResourceConfigurationHelper.replaceDocument(doc, newDatasources);
            if (!modified) {
                SaveCookie cookie = (SaveCookie)datasourcesDO.getCookie(SaveCookie.class);
                cookie.save();
            }

            datasources = newDatasources;

        } catch(DataObjectNotFoundException donfe) {
            Exceptions.printStackTrace(donfe);
        } catch (BadLocationException ble) {
            // this should not occur, just log it if it happens
            Exceptions.printStackTrace(ble);
        } catch (IOException ioe) {
            String msg = NbBundle.getMessage(DatasourceSupport.class, "MSG_CannotUpdateFile", datasourcesFile.getAbsolutePath());
            throw new ConfigurationException(msg, ioe);
        }
        
        return ds;
    }
    
//---------------------------------------- HELPERS --------------------------------------

    private void ensureResourceDirExists() {
        if (!resourceDir.exists())
            resourceDir.mkdir();
    }
    
    private void ensureDatasourcesFileExists() {
        if (!datasourcesFile.exists())
            getDatasourcesGraph();
    }

}
