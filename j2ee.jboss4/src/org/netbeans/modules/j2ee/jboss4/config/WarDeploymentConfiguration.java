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

package org.netbeans.modules.j2ee.jboss4.config;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ContextRootConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DatasourceConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DeploymentPlanConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;
import org.netbeans.modules.j2ee.jboss4.config.gen.EjbRef;
import org.netbeans.modules.j2ee.jboss4.config.gen.JbossWeb;
import org.netbeans.modules.j2ee.jboss4.config.gen.MessageDestinationRef;
import org.netbeans.modules.j2ee.jboss4.config.gen.ResourceRef;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * Web module deployment configuration handles creation and updating of the 
 * jboss-web.xml configuration file.
 *
 * @author Stepan Herold, Libor Kotouc
 */
public class WarDeploymentConfiguration extends JBDeploymentConfiguration 
implements ModuleConfiguration, ContextRootConfiguration, DatasourceConfiguration, 
        DeploymentPlanConfiguration, PropertyChangeListener {
    
    private File jbossWebFile;
    private JbossWeb jbossWeb;
    
    /**
     * Creates a new instance of WarDeploymentConfiguration 
     */
    public WarDeploymentConfiguration(J2eeModule j2eeModule) {
        super(j2eeModule);
        jbossWebFile = j2eeModule.getDeploymentConfigurationFile("WEB-INF/jboss-web.xml"); // NOI18N
        getJbossWeb();
        if (deploymentDescriptorDO == null) {
            try {
                deploymentDescriptorDO = deploymentDescriptorDO.find(FileUtil.toFileObject(jbossWebFile));
                deploymentDescriptorDO.addPropertyChangeListener(this);
            } catch(DataObjectNotFoundException donfe) {
                ErrorManager.getDefault().notify(donfe);
            }
        }
        WebApp webApp = (WebApp) j2eeModule.getDeploymentDescriptor(J2eeModule.WEB_XML);
        if (webApp != null) {
            webApp.addPropertyChangeListener(this);
        }
    }
    
    public Lookup getLookup() {
        return Lookups.fixed(this);
    }
    

    public void dispose() {
        WebApp webApp = (WebApp) j2eeModule.getDeploymentDescriptor(J2eeModule.WEB_XML);
        if (webApp != null) {
            webApp.removePropertyChangeListener(this);
        }
    }

    public boolean supportsCreateDatasource() {
        return true;
    }
    
    public boolean supportsCreateMessageDestination() {
        return true;
    }

    /**
     * Return context path.
     * 
     * @return context path or null, if the file is not parseable.
     */
    public String getContextRoot() throws ConfigurationException {
        JbossWeb jbossWeb = getJbossWeb();
        if (jbossWeb == null) { // graph not parseable
            String msg = NbBundle.getMessage(WarDeploymentConfiguration.class, "MSG_CannotReadContextRoot", jbossWebFile.getAbsolutePath());
            throw new ConfigurationException(msg);
        }
        return jbossWeb.getContextRoot();
    }
    
    /**
     * Set context path.
     */
    public void setContextRoot(String contextPath) throws ConfigurationException {
        // TODO: this contextPath fix code will be removed, as soon as it will 
        // be moved to the web project
        if (!isCorrectCP(contextPath)) {
            String ctxRoot = contextPath;
            java.util.StringTokenizer tok = new java.util.StringTokenizer(contextPath,"/"); //NOI18N
            StringBuffer buf = new StringBuffer(); //NOI18N
            while (tok.hasMoreTokens()) {
                buf.append("/"+tok.nextToken()); //NOI18N
            }
            ctxRoot = buf.toString();
            NotifyDescriptor desc = new NotifyDescriptor.Message(
                    NbBundle.getMessage (WarDeploymentConfiguration.class, "MSG_invalidCP", contextPath),
                    NotifyDescriptor.Message.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
            contextPath = ctxRoot;
        }
        final String newContextPath = contextPath;
        modifyJbossWeb(new JbossWebModifier() {
            public void modify(JbossWeb jbossWeb) {
                jbossWeb.setContextRoot(newContextPath);
            }
        });
    }
    
    /**
     * Listen to jboss-web.xml document changes.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        Object newValue = evt.getNewValue();
        if (evt.getPropertyName() == DataObject.PROP_MODIFIED && newValue == Boolean.FALSE) {
            if (evt.getSource() == deploymentDescriptorDO) { // dataobject has been modified, jbossWeb graph is out of sync
                synchronized (this) {
                    jbossWeb = null;
                }
            } else {
                super.propertyChange(evt);
            }
        } else if (evt.getOldValue() == null) {
            if (newValue instanceof org.netbeans.modules.j2ee.dd.api.common.ResourceRef) {
                //a new resource reference added
                org.netbeans.modules.j2ee.dd.api.common.ResourceRef resourceRef = (org.netbeans.modules.j2ee.dd.api.common.ResourceRef) newValue;
                try {
                    String resType = resourceRef.getResType();
                    if ("javax.sql.DataSource".equals(resType)) { // NOI18N
                        addResReference(resourceRef.getResRefName(), JBOSS4_DATASOURCE_JNDI_PREFIX + resourceRef.getResRefName());
                    } else if ("javax.mail.Session".equals(resType)) { // NOI18N
                        addMailReference(resourceRef.getResRefName());
                    } else if ("javax.jms.ConnectionFactory".equals(resType)) { // NOI18N
                        addConnectionFactoryReference(resourceRef.getResRefName());
                    }
                } catch (ConfigurationException ce) {
                    ErrorManager.getDefault().notify(ce);
                }
            } else if (newValue instanceof org.netbeans.modules.j2ee.dd.api.common.EjbRef) {
                // a new ejb reference added
                org.netbeans.modules.j2ee.dd.api.common.EjbRef ejbRef = (org.netbeans.modules.j2ee.dd.api.common.EjbRef) newValue;
                try {
                    String ejbRefType = ejbRef.getEjbRefType();
                    if ("Session".equals(ejbRefType) || "Entity".equals(ejbRefType)) { // NOI18N
                        addEjbReference(ejbRef.getEjbRefName());
                    }
                } catch (ConfigurationException ce) {
                    ErrorManager.getDefault().notify(ce);
                }
            } else if (newValue instanceof org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef) {
                //a new message destination reference added
                org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef messageDestinationRef = (org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef) newValue;
                try {
                    String messageDestinationType = messageDestinationRef.getMessageDestinationType();
                    String destPrefix = "javax.jms.Queue".equals(messageDestinationType) // NOI18N
                                            ? JBOSS4_MSG_QUEUE_JNDI_PREFIX : JBOSS4_MSG_TOPIC_JNDI_PREFIX;
                    addMsgDestReference(messageDestinationRef.getMessageDestinationRefName(), destPrefix);
                } catch (ConfigurationException ce) {
                    ErrorManager.getDefault().notify(ce);
                }                 
            }       
        }
    }
    
    public void bindDatasourceReference(String referenceName, String jndiName) throws ConfigurationException {
        addResReference(referenceName, jndiName);
    }
    
    public String findDatasourceJndiName(String referenceName) throws ConfigurationException {
        
        ResourceRef resourceRefs[] = getJbossWeb().getResourceRef();
        for (ResourceRef resourceRef : resourceRefs) {
            String rrn = resourceRef.getResRefName();
            if (referenceName.equals(rrn)) {
                return resourceRef.getJndiName();
            }
        }
        
        return null;
    }

    /**
     * Add a new resource reference.
     * 
     * @param name resource reference name
     */
    private void addResReference(final String name, final String jndiName) throws ConfigurationException {
        modifyJbossWeb(new JbossWebModifier() {
            public void modify(JbossWeb modifiedJbossWeb) {

                // check whether resource not already defined
                ResourceRef resourceRefs[] = modifiedJbossWeb.getResourceRef();
                for (int i = 0; i < resourceRefs.length; i++) {
                    String rrn = resourceRefs[i].getResRefName();
                    if (name.equals(rrn)) {
                        // already exists
                        return;
                    }
                }

                //if it doesn't exist yet, create a new one
                ResourceRef newRR = new ResourceRef();
                newRR.setResRefName(name);
                newRR.setJndiName(jndiName);
                modifiedJbossWeb.addResourceRef(newRR);
            }
        });
    }
    
    /**
     * Add a new mail service reference.
     * 
     * @param name mail service name
     */
    private void addMailReference(final String name) throws ConfigurationException {
        modifyJbossWeb(new JbossWebModifier() {
            public void modify(JbossWeb modifiedJbossWeb) {

                // check whether mail service not already defined
                ResourceRef resourceRefs[] = modifiedJbossWeb.getResourceRef();
                for (int i = 0; i < resourceRefs.length; i++) {
                    String rrn = resourceRefs[i].getResRefName();
                    if (name.equals(rrn)) {
                        // already exists
                        return;
                    }
                }

                //if it doesn't exist yet, create a new one
                ResourceRef newRR = new ResourceRef();
                newRR.setResRefName(name);
                newRR.setJndiName(JBOSS4_MAIL_SERVICE_JNDI_NAME);
                modifiedJbossWeb.addResourceRef(newRR);
            }
        });
    }
    
    public void bindMessageDestinationReference(String referenceName, String connectionFactoryName, 
            String destName, MessageDestination.Type type) throws ConfigurationException {

        addConnectionFactoryReference(connectionFactoryName);
        
        String jndiName = null;
        if (MessageDestination.Type.QUEUE.equals(type)) {
            jndiName = "queue/" + destName;
        }
        else
        if (MessageDestination.Type.TOPIC.equals(type)) {
            jndiName = "topic/" + destName;
        }

        addMsgDestReference(referenceName, jndiName);
    }
    
    /**
     * Add a new connection factory reference.
     * 
     * @param name connection factory name
     */
    private void addConnectionFactoryReference(final String name) throws ConfigurationException {
        modifyJbossWeb(new JbossWebModifier() {
            public void modify(JbossWeb modifiedJbossWeb) {

                // check whether connection factory not already defined
                ResourceRef resourceRefs[] = modifiedJbossWeb.getResourceRef();
                for (int i = 0; i < resourceRefs.length; i++) {
                    String rrn = resourceRefs[i].getResRefName();
                    if (name.equals(rrn)) {
                        // already exists
                        return;
                    }
                }

                //if it doesn't exist yet, create a new one
                ResourceRef newRR = new ResourceRef();
                newRR.setResRefName(name);
                newRR.setJndiName(JBOSS4_CONN_FACTORY_JNDI_NAME);
                modifiedJbossWeb.addResourceRef(newRR);
            }
        });
    }
    
    /**
     * Add a new message destination reference.
     * 
     * @param name message destination name
     * @param destPrefix MDB destination prefix
     */
    private void addMsgDestReference(final String name, final String jndiName) throws ConfigurationException {
        modifyJbossWeb(new JbossWebModifier() {
            public void modify(JbossWeb modifiedJbossWeb) {

                // check whether message destination not already defined
                MessageDestinationRef mdRefs[] = modifiedJbossWeb.getMessageDestinationRef();
                for (int i = 0; i < mdRefs.length; i++) {
                    String mdrn = mdRefs[i].getMessageDestinationRefName();
                    if (name.equals(mdrn)) {
                        // already exists
                        return;
                    }
                }

                //if it doesn't exist yet, create a new one
                MessageDestinationRef mdr = new MessageDestinationRef();
                mdr.setMessageDestinationRefName(name);
                mdr.setJndiName(jndiName);
                modifiedJbossWeb.addMessageDestinationRef(mdr);
            }
        });
    }
    
    /**
     * Add a new ejb reference.
     * 
     * @param name ejb reference name
     */
    private void addEjbReference(final String name) throws ConfigurationException {
        modifyJbossWeb(new JbossWebModifier() {
            public void modify(JbossWeb modifiedJbossWeb) {

                // check whether resource not already defined
                EjbRef ejbRefs[] = modifiedJbossWeb.getEjbRef();
                for (int i = 0; i < ejbRefs.length; i++) {
                    String ern = ejbRefs[i].getEjbRefName();
                    if (name.equals(ern)) {
                        // already exists
                        return;
                    }
                }

                //if it doesn't exist yet, create a new one
                EjbRef newER = new EjbRef();
                newER.setEjbRefName(name);
                String jndiName = name;
                if (jndiName.indexOf('/') != -1) {
                    jndiName = jndiName.substring(jndiName.lastIndexOf('/') + 1);
                }
                newER.setJndiName(jndiName);
                modifiedJbossWeb.addEjbRef(newER);
            }
        });
    }
    
    /**
     * Return JbossWeb graph. If it was not created yet, load it from the file
     * and cache it. If the file does not exist, generate it.
     *
     * @return JbossWeb graph or null if the jboss-web.xml file is not parseable.
     */
    public synchronized JbossWeb getJbossWeb() {
        if (jbossWeb == null) {
            try {
                if (jbossWebFile.exists()) {
                    // load configuration if already exists
                    try {
                        jbossWeb = JbossWeb.createGraph(jbossWebFile);
                    } catch (IOException ioe) {
                        ErrorManager.getDefault().notify(ioe);
                    } catch (RuntimeException re) {
                        // jboss-web.xml is not parseable, do nothing
                    }
                } else {
                    // create jboss-web.xml if it does not exist yet
                    jbossWeb = generateJbossWeb();
                    writeFile(jbossWebFile, jbossWeb);
                }
            } catch (ConfigurationException ce) {
                ErrorManager.getDefault().notify(ce);
            }
        }
        return jbossWeb;
    }
    
    public void save(OutputStream os) throws ConfigurationException {
        JbossWeb jbossWeb = getJbossWeb();
        if (jbossWeb == null) {
            String msg = NbBundle.getMessage(WarDeploymentConfiguration.class, "MSG_cannotSaveNotParseableConfFile", jbossWebFile.getAbsolutePath());
            throw new ConfigurationException(msg);
        }
        try {
            jbossWeb.write(os);
        } catch (IOException ioe) {
            String msg = NbBundle.getMessage(WarDeploymentConfiguration.class, "MSG_CannotUpdateFile", jbossWebFile.getAbsolutePath());
            throw new ConfigurationException(msg, ioe);
        }
    }
    
    // private helper methods -------------------------------------------------
    
    /**
     * Perform jbossWeb changes defined by the jbossWeb modifier. Update editor
     * content and save changes, if appropriate.
     *
     * @param modifier
     */
    private void modifyJbossWeb(JbossWebModifier modifier) throws ConfigurationException {
        assert deploymentDescriptorDO != null : "DataObject has not been initialized yet"; // NIO18N
        try {
            // get the document
            EditorCookie editor = (EditorCookie)deploymentDescriptorDO.getCookie(EditorCookie.class);
            StyledDocument doc = editor.getDocument();
            if (doc == null) {
                doc = editor.openDocument();
            }
            
            // get the up-to-date model
            JbossWeb newJbossWeb = null;
            try {
                // try to create a graph from the editor content
                byte[] docString = doc.getText(0, doc.getLength()).getBytes();
                newJbossWeb = JbossWeb.createGraph(new ByteArrayInputStream(docString));
            } catch (RuntimeException e) {
                JbossWeb oldJbossWeb = getJbossWeb();
                if (oldJbossWeb == null) {
                    // neither the old graph is parseable, there is not much we can do here
                    // TODO: should we notify the user?
                    String msg = NbBundle.getMessage(WarDeploymentConfiguration.class, "MSG_jbossXmlCannotParse", jbossWebFile.getAbsolutePath());
                    throw new ConfigurationException(msg);
                }
                // current editor content is not parseable, ask whether to override or not
                NotifyDescriptor notDesc = new NotifyDescriptor.Confirmation(
                        NbBundle.getMessage(WarDeploymentConfiguration.class, "MSG_jbossWebXmlNotValid"),
                        NotifyDescriptor.OK_CANCEL_OPTION);
                Object result = DialogDisplayer.getDefault().notify(notDesc);
                if (result == NotifyDescriptor.CANCEL_OPTION) {
                    // keep the old content
                    return;
                }
                // use the old graph
                newJbossWeb = oldJbossWeb;
            }
            
            // perform changes
            modifier.modify(newJbossWeb);
            
            // save, if appropriate
            boolean modified = deploymentDescriptorDO.isModified();
            replaceDocument(doc, newJbossWeb);
            if (!modified) {
                SaveCookie cookie = (SaveCookie)deploymentDescriptorDO.getCookie(SaveCookie.class);
                if (cookie != null) {
                    cookie.save();
                }
            }
            synchronized (this) {
                jbossWeb = newJbossWeb;
            }
        } catch (BadLocationException ble) {
            // this should not occur, just log it if it happens
            ErrorManager.getDefault().notify(ble);
        } catch (IOException ioe) {
            String msg = NbBundle.getMessage(WarDeploymentConfiguration.class, "MSG_CannotUpdateFile", jbossWebFile.getAbsolutePath());
            throw new ConfigurationException(msg, ioe);
        }
    }
    
    /**
     * Generate JbossWeb graph.
     */
    private JbossWeb generateJbossWeb() {
        JbossWeb jbossWeb = new JbossWeb();
        jbossWeb.setContextRoot(""); // NOI18N
        return jbossWeb;
    }
    
    // TODO: this contextPath fix code will be removed, as soon as it will 
    // be moved to the web project
    private boolean isCorrectCP(String contextPath) {
        boolean correct=true;
        if (!contextPath.equals("") && !contextPath.startsWith("/")) correct=false; //NOI18N
        else if (contextPath.endsWith("/")) correct=false; //NOI18N
        else if (contextPath.indexOf("//")>=0) correct=false; //NOI18N
        return correct;
    }
    
    
    // private helper interface -----------------------------------------------
     
    private interface JbossWebModifier {
        void modify(JbossWeb modifiedJbossWeb);
    }
}
