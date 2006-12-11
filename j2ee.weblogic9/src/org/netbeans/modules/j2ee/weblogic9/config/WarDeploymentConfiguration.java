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

package org.netbeans.modules.j2ee.weblogic9.config;

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
import org.netbeans.modules.j2ee.weblogic9.config.gen.WeblogicWebApp;
import org.netbeans.modules.schema2beans.AttrProp;
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
 * weblogic.xml configuration file.
 *
 * @author sherold
 */
public class WarDeploymentConfiguration extends WLDeploymentConfiguration 
        implements PropertyChangeListener {
    
    private File file;
    private WeblogicWebApp webLogicWebApp;
    
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
     * @param file weblogic.xml file.
     */
    public void init(File file) {
        this.file = file;
        getWeblogicWebApp();
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
        WeblogicWebApp webLogicWebApp = getWeblogicWebApp();
        if (webLogicWebApp == null) { // graph not parseable
            throw new ConfigurationException("weblogic.xml is not parseable, cannot read the context path value."); // NOI18N
        }
        return webLogicWebApp.getContextRoot(0);
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
        modifyWeblogicWebApp(new WeblogicWebAppModifier() {
            public void modify(WeblogicWebApp webLogicWebApp) {
                webLogicWebApp.setContextRoot(new String [] {newContextPath});
            }
        });
    }
    
    /**
     * Listen to weblogic.xml document changes.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == DataObject.PROP_MODIFIED &&
                evt.getNewValue() == Boolean.FALSE) {
            // dataobject has been modified, webLogicWebApp graph is out of sync
            webLogicWebApp = null;
        }
    }
   
    /**
     * Return WeblogicWebApp graph. If it was not created yet, load it from the file
     * and cache it. If the file does not exist, generate it.
     *
     * @return WeblogicWebApp graph or null if the weblogic.xml file is not parseable.
     */
    public synchronized WeblogicWebApp getWeblogicWebApp() {
        if (webLogicWebApp == null) {
            try {
                if (file.exists()) {
                    // load configuration if already exists
                    try {
                        webLogicWebApp = WeblogicWebApp.createGraph(file);
                    } catch (IOException ioe) {
                        ErrorManager.getDefault().notify(ioe);
                    } catch (RuntimeException re) {
                        // weblogic.xml is not parseable, do nothing
                    }
                } else {
                    // create weblogic.xml if it does not exist yet
                    webLogicWebApp = genereateWeblogicWebApp();
                    writefile(file, webLogicWebApp);
                }
            } catch (ConfigurationException ce) {
                ErrorManager.getDefault().notify(ce);
            }
        }
        return webLogicWebApp;
    }
    
    // JSR-88 methods ---------------------------------------------------------
    
    public void save(OutputStream os) throws ConfigurationException {
        WeblogicWebApp webLogicWebApp = getWeblogicWebApp();
        if (webLogicWebApp == null) {
            throw new ConfigurationException("Cannot read configuration, it is probably in an inconsistent state."); // NOI18N
        }
        try {
            webLogicWebApp.write(os);
        } catch (IOException ioe) {
            throw new ConfigurationException(ioe.getLocalizedMessage());
        }
    }
    
    // private helper methods -------------------------------------------------
    
    /**
     * Perform webLogicWebApp changes defined by the webLogicWebApp modifier. Update editor
     * content and save changes, if appropriate.
     *
     * @param modifier
     */
    private void modifyWeblogicWebApp(WeblogicWebAppModifier modifier) throws ConfigurationException {
        assert dataObject != null : "DataObject has not been initialized yet"; // NIO18N
        try {
            // get the document
            EditorCookie editor = (EditorCookie)dataObject.getCookie(EditorCookie.class);
            StyledDocument doc = editor.getDocument();
            if (doc == null) {
                doc = editor.openDocument();
            }
            
            // get the up-to-date model
            WeblogicWebApp newWeblogicWebApp = null;
            try {
                // try to create a graph from the editor content
                byte[] docString = doc.getText(0, doc.getLength()).getBytes();
                newWeblogicWebApp = WeblogicWebApp.createGraph(new ByteArrayInputStream(docString));
            } catch (RuntimeException e) {
                WeblogicWebApp oldWeblogicWebApp = getWeblogicWebApp();
                if (oldWeblogicWebApp == null) {
                    // neither the old graph is parseable, there is not much we can do here
                    // TODO: should we notify the user?
                    throw new ConfigurationException("Configuration data are not parseable cannot perform changes."); // NOI18N
                }
                // current editor content is not parseable, ask whether to override or not
                NotifyDescriptor notDesc = new NotifyDescriptor.Confirmation(
                        NbBundle.getMessage(WarDeploymentConfiguration.class, "MSG_weblogicXmlNotValid"),
                        NotifyDescriptor.OK_CANCEL_OPTION);
                Object result = DialogDisplayer.getDefault().notify(notDesc);
                if (result == NotifyDescriptor.CANCEL_OPTION) {
                    // keep the old content
                    return;
                }
                // use the old graph
                newWeblogicWebApp = oldWeblogicWebApp;
            }
            
            // perform changes
            modifier.modify(newWeblogicWebApp);
            
            // save, if appropriate
            boolean modified = dataObject.isModified();
            replaceDocument(doc, newWeblogicWebApp);
            if (!modified) {
                SaveCookie cookie = (SaveCookie)dataObject.getCookie(SaveCookie.class);
                if (cookie != null) {
                    cookie.save();
                }
            }
            webLogicWebApp = newWeblogicWebApp;
        } catch (BadLocationException ble) {
            throw (ConfigurationException)(new ConfigurationException().initCause(ble));
        } catch (IOException ioe) {
            throw (ConfigurationException)(new ConfigurationException().initCause(ioe));
        }
    }
    
    /**
     * Genereate Context graph.
     */
    private WeblogicWebApp genereateWeblogicWebApp() {
        WeblogicWebApp webLogicWebApp = new WeblogicWebApp();
        webLogicWebApp.createAttribute("xmlns:j2ee", "xmlns:j2ee", AttrProp.CDATA | AttrProp.IMPLIED, null, null);
        webLogicWebApp.setAttributeValue("xmlns:j2ee", "http://java.sun.com/xml/ns/j2ee");
        webLogicWebApp.setAttributeValue("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"); // NOI18N
        webLogicWebApp.setAttributeValue("xsi:schemaLocation", "http://www.bea.com/ns/weblogic/90 http://www.bea.com/ns/weblogic/90/weblogic-web-app.xsd"); // NOI18N
        webLogicWebApp.setContextRoot(new String [] {""}); // NOI18N
        return webLogicWebApp;
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
     
    private interface WeblogicWebAppModifier {
        void modify(WeblogicWebApp context);
    }
}
