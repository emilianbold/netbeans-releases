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
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.model.XpathEvent;
import javax.enterprise.deploy.model.XpathListener;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.j2ee.jboss4.config.gen.EjbRef;
import org.netbeans.modules.j2ee.jboss4.config.gen.JbossClient;
import org.netbeans.modules.j2ee.jboss4.config.gen.ResourceRef;
import org.netbeans.modules.j2ee.jboss4.config.gen.ServiceRef;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;

/**
 *
 * @author jungi
 */
public class CarDeploymentConfiguration extends JBDeploymentConfiguration
        implements PropertyChangeListener, XpathListener {
    
    private static final String EJB_REF = "/application-client/ejb-ref"; // NOI18N
    private static final String SERVICE_REF = "/application-client/service-ref"; // NOI18N
    private static final String RESOURCE_REF = "/application-client/resource-ref"; // NOI18N
    private static final String DISPLAY_NAME = "/application-client/display-name"; // NOI18N
    
    private File jbossClientFile;
    private JbossClient jbossClient;
    
    /** Creates a new instance of CarDeploymentConfiguration */
    public CarDeploymentConfiguration(DeployableObject deployableObject) {
        super(deployableObject);
    }
    
    /**
     * CarDeploymentConfiguration initialization. This method should be called before
     * this class is being used.
     * 
     * @param file jboss-client.xml file.
     * @param resourceDir   directory containing definition for enterprise resources.
     */
    public void init(File file, File resourceDir) {
        super.init(resourceDir);
        this.jbossClientFile = file;
        getJbossClient();
        if (deploymentDescriptorDO == null) {
            try {
                deploymentDescriptorDO = deploymentDescriptorDO.find(FileUtil.toFileObject(jbossClientFile));
                deploymentDescriptorDO.addPropertyChangeListener(this);
            } catch(DataObjectNotFoundException donfe) {
                ErrorManager.getDefault().notify(donfe);
            }
        }
        
        if (deplObj != null && deplObj.getDDBeanRoot() != null ) {
            //listen on the resource-ref element
            DDBeanRoot root = deplObj.getDDBeanRoot();
            root.addXpathListener(DISPLAY_NAME, this);
            root.addXpathListener(RESOURCE_REF, this);
            root.addXpathListener(EJB_REF, this);
            root.addXpathListener(SERVICE_REF, this);
        }
    }
    
    /**
     * Return JbossClient graph. If it was not created yet, load it from the file
     * and cache it. If the file does not exist, generate it.
     *
     * @return JbossWeb graph or null if the jboss-web.xml file is not parseable.
     */
    public synchronized JbossClient getJbossClient() {
        if (jbossClient == null) {
            try {
                if (jbossClientFile.exists()) {
                    // load configuration if already exists
                    try {
                        jbossClient = JbossClient.createGraph(jbossClientFile);
                    } catch (IOException ioe) {
                        ErrorManager.getDefault().notify(ioe);
                    } catch (RuntimeException re) {
                        // jboss-web.xml is not parseable, do nothing
                    }
                } else {
                    // create jboss-web.xml if it does not exist yet
                    jbossClient = generateJbossClient();
                    writefile(jbossClientFile, jbossClient);
                }
            } catch (ConfigurationException ce) {
                ErrorManager.getDefault().notify(ce);
            }
        }
        return jbossClient;
    }

    /**
     * Listen to jboss-web.xml document changes.
     */
    public synchronized void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == DataObject.PROP_MODIFIED &&
                evt.getNewValue() == Boolean.FALSE) {

            if (evt.getSource() == deploymentDescriptorDO) // dataobject has been modified, jbossWeb graph is out of sync
                jbossClient = null;
            else
                super.propertyChange(evt);
        }
    }
   
    public void fireXpathEvent(XpathEvent xpe) {
        if (!xpe.isAddEvent())
            return;

        DDBean eventDDBean = xpe.getBean();
        if (DISPLAY_NAME.equals(eventDDBean.getXpath())) {
            String name = eventDDBean.getText();
            try {
                setJndiName(name);
            } catch (ConfigurationException ce) {
                ErrorManager.getDefault().notify(ce);
            }
        } else if (RESOURCE_REF.equals(eventDDBean.getXpath())) { //a new resource reference added
            String[] desc = eventDDBean.getText("description"); // NOI18N
            String[] name = eventDDBean.getText("res-ref-name"); // NOI18N
            String[] type = eventDDBean.getText("res-type");     // NOI18N
            if (name.length > 0 && type.length > 0) {
                try {
                    if (desc.length > 0  && "javax.sql.DataSource".equals(type[0])) // NOI18N
                        addResReference(desc[0], name[0]);
                    else
                    if ("javax.mail.Session".equals(type[0])) // NOI18N
                        addMailReference(name[0]);
                    if ("javax.jms.ConnectionFactory".equals(type[0])) // NOI18N
                        addConnectionFactoryReference(name[0]);
                } catch (ConfigurationException ce) {
                    ErrorManager.getDefault().notify(ce);
                }
            }
        } else if (EJB_REF.equals(eventDDBean.getXpath())) { // a new ejb reference added
            String[] name = eventDDBean.getText("ejb-ref-name"); // NOI18N
            String[] type = eventDDBean.getText("ejb-ref-type"); // NOI18N
            if (name.length > 0 && type.length > 0 
                    && ("Session".equals(type[0]) || "Entity".equals(type[0]))) { // NOI18N
                try {
                    addEjbReference(name[0]);
                } catch (ConfigurationException ce) {
                    ErrorManager.getDefault().notify(ce);
                }
            }
        } else if (SERVICE_REF.equals(eventDDBean.getXpath())) { //a new message destination reference added
            String[] name = eventDDBean.getText("service-ref-name"); // NOI18N
            if (name.length > 0) {
                try {
                    addServiceReference(name[0]);
                } catch (ConfigurationException ce) {
                    ErrorManager.getDefault().notify(ce);
                }
            }
        }
        
    }

    // JSR-88 methods ---------------------------------------------------------
    
    public void save(OutputStream os) throws ConfigurationException {
        JbossClient jbossClientDD = getJbossClient();
        if (jbossClientDD == null) {
            throw new ConfigurationException("Cannot read configuration, it is probably in an inconsistent state."); // NOI18N
        }
        try {
            jbossClientDD.write(os);
        } catch (IOException ioe) {
            throw new ConfigurationException(ioe.getLocalizedMessage());
        }
    }
    
    // private helper methods -------------------------------------------------
    
    /**
     * Generate JbossWeb graph.
     */
    private JbossClient generateJbossClient() {
        JbossClient jbossClientDD = new JbossClient();
        //jbossClientDD.setContextRoot(""); // NOI18N
        return jbossClientDD;
    }
    
    /**
     * Add a new resource reference.
     * 
     * @param desc description
     * @param name resource reference name
     */
    private void addResReference(final String desc, final String name) throws ConfigurationException {
        modifyJbossClient(new JbossClientModifier() {
            public void modify(JbossClient modifiedJbossClient) {

                // check whether resource not already defined
                ResourceRef resourceRefs[] = modifiedJbossClient.getResourceRef();
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
                newRR.setJndiName(JBOSS4_DATASOURCE_JNDI_PREFIX + name);
                modifiedJbossClient.addResourceRef(newRR);
            }
        });
    }
    
    /**
     * Add a new mail service reference.
     * 
     * @param name mail service name
     */
    private void addMailReference(final String name) throws ConfigurationException {
        modifyJbossClient(new JbossClientModifier() {
            public void modify(JbossClient modifiedJbossClient) {

                // check whether mail service not already defined
                ResourceRef resourceRefs[] = modifiedJbossClient.getResourceRef();
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
                modifiedJbossClient.addResourceRef(newRR);
            }
        });
    }
    
    /**
     * Add a new connection factory reference.
     * 
     * @param name connection factory name
     */
    private void addConnectionFactoryReference(final String name) throws ConfigurationException {
        modifyJbossClient(new JbossClientModifier() {
            public void modify(JbossClient modifiedJbossClient) {

                // check whether connection factory not already defined
                ResourceRef resourceRefs[] = modifiedJbossClient.getResourceRef();
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
                modifiedJbossClient.addResourceRef(newRR);
            }
        });
    }
    
    /**
     * Add a new ejb reference.
     * 
     * @param name ejb reference name
     */
    private void addEjbReference(final String name) throws ConfigurationException {
        modifyJbossClient(new JbossClientModifier() {
            public void modify(JbossClient modifiedJbossClient) {

                // check whether resource not already defined
                EjbRef ejbRefs[] = modifiedJbossClient.getEjbRef();
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
                newER.setJndiName(/*JBOSS4_EJB_JNDI_PREFIX + */name);
                modifiedJbossClient.addEjbRef(newER);
            }
        });
    }
    
    /**
     * Add a new jndi-name.
     * 
     * @param name jndi-name  name
     */
    private void setJndiName(final String jndiName) throws ConfigurationException {
        modifyJbossClient(new JbossClientModifier() {
            public void modify(JbossClient modifiedJbossClient) {
                modifiedJbossClient.setJndiName(jndiName);
            }
        });
    }
    
    /**
     * Add a new service reference.
     * 
     * @param name service reference name
     */
    private void addServiceReference(final String name) throws ConfigurationException {
        modifyJbossClient(new JbossClientModifier() {
            public void modify(JbossClient modifiedJbossClient) {

                // check whether resource not already defined
                ServiceRef serviceRefs[] = modifiedJbossClient.getServiceRef();
                for (int i = 0; i < serviceRefs.length; i++) {
                    String srn = serviceRefs[i].getServiceRefName();
                    if (name.equals(srn)) {
                        // already exists
                        return;
                    }
                }

                //if it doesn't exist yet, create a new one
                ServiceRef newSR = new ServiceRef();
                newSR.setServiceRefName(name);
                modifiedJbossClient.addServiceRef(newSR);
            }
        });
    }
    
    /**
     * Perform jbossWeb changes defined by the jbossWeb modifier. Update editor
     * content and save changes, if appropriate.
     *
     * @param modifier
     */
    private void modifyJbossClient(JbossClientModifier modifier) throws ConfigurationException {
        assert deploymentDescriptorDO != null : "DataObject has not been initialized yet"; // NIO18N
        try {
            // get the document
            EditorCookie editor = (EditorCookie)deploymentDescriptorDO.getCookie(EditorCookie.class);
            StyledDocument doc = editor.getDocument();
            if (doc == null) {
                doc = editor.openDocument();
            }
            
            // get the up-to-date model
            JbossClient newJbossClient = null;
            try {
                // try to create a graph from the editor content
                byte[] docString = doc.getText(0, doc.getLength()).getBytes();
                newJbossClient = JbossClient.createGraph(new ByteArrayInputStream(docString));
            } catch (RuntimeException e) {
                JbossClient oldJbossClient = getJbossClient();
                if (oldJbossClient == null) {
                    // neither the old graph is parseable, there is not much we can do here
                    // TODO: should we notify the user?
                    throw new ConfigurationException("Configuration data are not parseable cannot perform changes."); // NOI18N
                }
                // current editor content is not parseable, ask whether to override or not
                NotifyDescriptor notDesc = new NotifyDescriptor.Confirmation(
                        NbBundle.getMessage(CarDeploymentConfiguration.class, "MSG_jbossClientXmlNotValid"),
                        NotifyDescriptor.OK_CANCEL_OPTION);
                Object result = DialogDisplayer.getDefault().notify(notDesc);
                if (result == NotifyDescriptor.CANCEL_OPTION) {
                    // keep the old content
                    return;
                }
                // use the old graph
                newJbossClient = oldJbossClient;
            }
            
            // perform changes
            modifier.modify(newJbossClient);
            
            // save, if appropriate
            boolean modified = deploymentDescriptorDO.isModified();
            replaceDocument(doc, newJbossClient);
            if (!modified) {
                SaveCookie cookie = (SaveCookie)deploymentDescriptorDO.getCookie(SaveCookie.class);
                cookie.save();
            }
            jbossClient = newJbossClient;
        } catch (BadLocationException ble) {
            throw (ConfigurationException)(new ConfigurationException().initCause(ble));
        } catch (IOException ioe) {
            throw (ConfigurationException)(new ConfigurationException().initCause(ioe));
        }
    }
    
    // private helper interface -----------------------------------------------
     
    private interface JbossClientModifier {
        void modify(JbossClient modifiedJbossClient);
    }
}
