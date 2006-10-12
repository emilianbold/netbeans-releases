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
import org.openide.util.NbBundle;

/**
 * Web module deployment configuration handles creation and updating of the 
 * jboss-web.xml configuration file.
 *
 * @author sherold, lkotouc
 */
public class WarDeploymentConfiguration extends JBDeploymentConfiguration 
        implements PropertyChangeListener, XpathListener {
    
    private static final String RESOURCE_REF = "/web-app/resource-ref"; // NOI18N
    private static final String EJB_REF = "/web-app/ejb-ref"; // NOI18N
    private static final String MSG_DEST_REF = "/web-app/message-destination-ref"; // NOI18N
    
    private File jbossWebFile;
    private JbossWeb jbossWeb;
    
    /**
     * Creates a new instance of WarDeploymentConfiguration 
     */
    public WarDeploymentConfiguration(DeployableObject deployableObject) {
        super(deployableObject);
    }
    
    /**
     * WarDeploymentConfiguration initialization. This method should be called before
     * this class is being used.
     * 
     * @param file jboss-web.xml file.
     * @param resourceDir   directory containing definition for enterprise resources.
     */
    public void init(File file, File resourceDir) {
        super.init(resourceDir);
        this.jbossWebFile = file;
        getJbossWeb();
        if (deploymentDescriptorDO == null) {
            try {
                deploymentDescriptorDO = deploymentDescriptorDO.find(FileUtil.toFileObject(jbossWebFile));
                deploymentDescriptorDO.addPropertyChangeListener(this);
            } catch(DataObjectNotFoundException donfe) {
                ErrorManager.getDefault().notify(donfe);
            }
        }

        if (deplObj != null && deplObj.getDDBeanRoot() != null ) {
            //listen on the resource-ref element
            DDBeanRoot root = deplObj.getDDBeanRoot();
            root.addXpathListener(RESOURCE_REF, this);
            root.addXpathListener(EJB_REF, this);
            root.addXpathListener(MSG_DEST_REF, this);
        }
    }
    
    /**
     * Return context path.
     * 
     * @return context path or null, if the file is not parseable.
     */
    public String getContextPath() throws ConfigurationException {
        JbossWeb jbossWeb = getJbossWeb();
        if (jbossWeb == null) { // graph not parseable
            throw new ConfigurationException("jboss-web.xml is not parseable, cannot read the context path value."); // NOI18N
        }
        return jbossWeb.getContextRoot();
    }
    
    /**
     * Set context path.
     */
    public void setContextPath(String contextPath) throws ConfigurationException {
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
    public synchronized void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == DataObject.PROP_MODIFIED &&
                evt.getNewValue() == Boolean.FALSE) {

            if (evt.getSource() == deploymentDescriptorDO) // dataobject has been modified, jbossWeb graph is out of sync
                jbossWeb = null;
            else
                super.propertyChange(evt);
        }
    }
   
    public void fireXpathEvent(XpathEvent xpe) {
        if (!xpe.isAddEvent())
            return;

        DDBean eventDDBean = xpe.getBean();
        if (RESOURCE_REF.equals(eventDDBean.getXpath())) { //a new resource reference added
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
        }
        else if (EJB_REF.equals(eventDDBean.getXpath())) { // a new ejb reference added
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
        }
        else if (MSG_DEST_REF.equals(eventDDBean.getXpath())) { //a new message destination reference added
            String[] name = eventDDBean.getText("message-destination-ref-name"); // NOI18N
            String[] type = eventDDBean.getText("message-destination-type"); // NOI18N
            if (name.length > 0) {
                
                String destPrefix = "";
                if (type.length > 0) {
                    if (type[0].equals("javax.jms.Queue")) // NOI18N
                        destPrefix = JBOSS4_MSG_QUEUE_JNDI_PREFIX;
                    else
                    if (type[0].equals("javax.jms.Topic")) // NOI18N
                        destPrefix = JBOSS4_MSG_TOPIC_JNDI_PREFIX;
                }
                
                try {
                    addMsgDestReference(name[0], destPrefix);
                } catch (ConfigurationException ce) {
                    ErrorManager.getDefault().notify(ce);
                }
            }
        }
        
    }

    /**
     * Add a new resource reference.
     * 
     * @param desc description
     * @param name resource reference name
     */
    private void addResReference(final String desc, final String name) throws ConfigurationException {
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
                newRR.setJndiName(JBOSS4_DATASOURCE_JNDI_PREFIX + name);
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
    private void addMsgDestReference(final String name, final String destPrefix) throws ConfigurationException {
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
                String jndiName = name;
                if (name.startsWith("jms/")) // prefix automatically prepended to the selected message destination during 'Send JMS Message' action
                    jndiName = destPrefix + name.substring("jms/".length()); //replace 'jms/' with the correct prefix
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
                    writefile(jbossWebFile, jbossWeb);
                }
            } catch (ConfigurationException ce) {
                ErrorManager.getDefault().notify(ce);
            }
        }
        return jbossWeb;
    }
    
    // JSR-88 methods ---------------------------------------------------------
    
    public void save(OutputStream os) throws ConfigurationException {
        JbossWeb jbossWeb = getJbossWeb();
        if (jbossWeb == null) {
            throw new ConfigurationException("Cannot read configuration, it is probably in an inconsistent state."); // NOI18N
        }
        try {
            jbossWeb.write(os);
        } catch (IOException ioe) {
            throw new ConfigurationException(ioe.getLocalizedMessage());
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
                    throw new ConfigurationException("Configuration data are not parseable cannot perform changes."); // NOI18N
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
                cookie.save();
            }
            jbossWeb = newJbossWeb;
        } catch (BadLocationException ble) {
            throw (ConfigurationException)(new ConfigurationException().initCause(ble));
        } catch (IOException ioe) {
            throw (ConfigurationException)(new ConfigurationException().initCause(ioe));
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
