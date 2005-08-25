/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.jboss4.config;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.j2ee.jboss4.config.gen.JbossWeb;
import org.netbeans.modules.schema2beans.BaseBean;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;

/**
 * Web module deployment configuration handles creation and updating of the 
 * jboss-web.xml configuration file.
 *
 * @author sherold
 */
public class WarDeploymentConfiguration extends JBDeploymentConfiguration 
        implements PropertyChangeListener {
    
    private File file;
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
     */
    public void init(File file) {
        this.file = file;
        getJbossWeb();
        if (dataObject == null) {
            try {
                dataObject = dataObject.find(FileUtil.toFileObject(file));
                dataObject.addPropertyChangeListener(this);
            } catch(DataObjectNotFoundException donfe) {
                ErrorManager.getDefault().notify(donfe);
            }
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
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == DataObject.PROP_MODIFIED &&
                evt.getNewValue() == Boolean.FALSE) {
            // dataobject has been modified, jbossWeb graph is out of sync
            jbossWeb = null;
        }
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
                if (file.exists()) {
                    // load configuration if already exists
                    try {
                        jbossWeb = JbossWeb.createGraph(file);
                    } catch (IOException ioe) {
                        ErrorManager.getDefault().notify(ioe);
                    } catch (RuntimeException re) {
                        // jboss-web.xml is not parseable, do nothing
                    }
                } else {
                    // create jboss-web.xml if it does not exist yet
                    jbossWeb = genereateJbossWeb();
                    writefile(file, jbossWeb);
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
        assert dataObject != null : "DataObject has not been initialized yet"; // NIO18N
        try {
            // get the document
            EditorCookie editor = (EditorCookie)dataObject.getCookie(EditorCookie.class);
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
            boolean modified = dataObject.isModified();
            replaceDocument(doc, newJbossWeb);
            if (!modified) {
                SaveCookie cookie = (SaveCookie)dataObject.getCookie(SaveCookie.class);
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
     * Genereate Context graph.
     */
    private JbossWeb genereateJbossWeb() {
        JbossWeb jbossWeb = new JbossWeb();
        jbossWeb.setContextRoot(""); // NOI18N
        return jbossWeb;
    }
    
    /**
     * Replace the content of the document by the graph.
     */
    private void replaceDocument(final StyledDocument doc, BaseBean graph) {
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
        void modify(JbossWeb context);
    }
}
