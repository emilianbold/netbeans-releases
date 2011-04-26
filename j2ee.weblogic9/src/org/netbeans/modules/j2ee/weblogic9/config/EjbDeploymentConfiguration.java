/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.j2ee.weblogic9.config;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DeploymentPlanConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;
import org.netbeans.modules.j2ee.weblogic9.dd.model.EjbJarModel;
import org.netbeans.modules.j2ee.weblogic9.dd.model.WebApplicationModel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;


/**
 * EJB module deployment configuration handles weblogic-ejb-jar.xml configuration file creation.
 *
 * @author sherold
 */
public class EjbDeploymentConfiguration extends WLDeploymentConfiguration
        implements ModuleConfiguration, DeploymentPlanConfiguration, PropertyChangeListener {

    private final File file;
    private final J2eeModule j2eeModule;
    private final DataObject dataObject;
    
    private final Version serverVersion;
    
    private final boolean isWebProfile;    

    private EjbJarModel weblogicEjbJar;
    
    public EjbDeploymentConfiguration(J2eeModule j2eeModule) {
        this(j2eeModule, null, false);
    }

    /**
     * Creates a new instance of EjbDeploymentConfiguration 
     */
    public EjbDeploymentConfiguration(J2eeModule j2eeModule, Version serverVersion,
            boolean isWebProfile) {

        super(j2eeModule);
        this.j2eeModule = j2eeModule;
        this.serverVersion = serverVersion;
        this.isWebProfile = isWebProfile;
        file = j2eeModule.getDeploymentConfigurationFile("META-INF/weblogic-ejb-jar.xml"); // NOI18N
        getWeblogicEjbJar();
        DataObject dataObject = null;
        try {
            dataObject = DataObject.find(FileUtil.toFileObject(file));
            dataObject.addPropertyChangeListener(this);
        } catch(DataObjectNotFoundException donfe) {
            Exceptions.printStackTrace(donfe);
        }
        this.dataObject = dataObject;
    }
       
    /**
     * Return WeblogicEjbJar graph. If it was not created yet, load it from the file
     * and cache it. If the file does not exist, generate it.
     *
     * @return WeblogicEjbJar graph or null if the weblogic-ejb-jar.xml file is not parseable.
     */
    public synchronized EjbJarModel getWeblogicEjbJar() {
        if (weblogicEjbJar == null) {
            try {
                if (file.exists()) {
                    // load configuration if already exists
                    try {
                        weblogicEjbJar = EjbJarModel.forFile(file);
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    } catch (RuntimeException re) {
                        // weblogic-ejb-jar.xml is not parseable, do nothing
                    }
                } else {
                    // create weblogic-ejb-jar.xml if it does not exist yet
                    weblogicEjbJar = genereateWeblogicEjbJar();
                    weblogicEjbJar.write(file);
                }
            } catch (ConfigurationException ce) {
                Exceptions.printStackTrace(ce);
            }
        }
        return weblogicEjbJar;
    }
    
    public Lookup getLookup() {
        return Lookups.fixed(this);
    }
    

    public J2eeModule getJ2eeModule() {
        return j2eeModule;
    }

    public void dispose() {
        if (dataObject != null) {
            dataObject.removePropertyChangeListener(this);
        }        
    }
    
    /**
     * Listen to weblogic-ejb-jar.xml document changes.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == DataObject.PROP_MODIFIED &&
                evt.getNewValue() == Boolean.FALSE) {
            // dataobject has been modified, webLogicWebApp graph is out of sync
            synchronized (this) {
                weblogicEjbJar = null;
            }
        }
    }    
    
    // FIXME this is not a proper implementation - deployment PLAN should be saved
    // not a deployment descriptor    
    public void save(OutputStream os) throws ConfigurationException {
        EjbJarModel weblogicEjbJar = getWeblogicEjbJar();
        if (weblogicEjbJar == null) {
            String msg = NbBundle.getMessage(WarDeploymentConfiguration.class, "MSG_cannotSaveNotParseableConfFile", file.getPath());
            throw new ConfigurationException(msg);
        }
        try {
            weblogicEjbJar.write(os);
        } catch (IOException ioe) {
            String msg = NbBundle.getMessage(WarDeploymentConfiguration.class, "MSG_CannotUpdateFile", file.getPath());
            throw new ConfigurationException(msg, ioe);
        }
    }
    
    // private helper methods -------------------------------------------------
    
    /**
     * Perform webLogicWebApp changes defined by the webLogicWebApp modifier. Update editor
     * content and save changes, if appropriate.
     *
     * @param modifier
     */
    private void modifyWeblogicEjbJar(WeblogicEjbJarModifier modifier) throws ConfigurationException {
        assert dataObject != null : "DataObject has not been initialized yet"; // NIO18N
        try {
            // get the document
            EditorCookie editor = (EditorCookie)dataObject.getCookie(EditorCookie.class);
            StyledDocument doc = editor.getDocument();
            if (doc == null) {
                doc = editor.openDocument();
            }
            
            // get the up-to-date model
            EjbJarModel newWeblogicEjbJar = null;
            try {
                // try to create a graph from the editor content
                byte[] docString = doc.getText(0, doc.getLength()).getBytes();
                newWeblogicEjbJar = EjbJarModel.forInputStream(new ByteArrayInputStream(docString));
            } catch (RuntimeException e) {
                EjbJarModel oldWeblogicEjbJar = getWeblogicEjbJar();
                if (oldWeblogicEjbJar == null) {
                    // neither the old graph is parseable, there is not much we can do here
                    // TODO: should we notify the user?
                    String msg = NbBundle.getMessage(EjbDeploymentConfiguration.class, "MSG_configFileCannotParse", file.getPath());
                    throw new ConfigurationException(msg);
                }
                // current editor content is not parseable, ask whether to override or not
                NotifyDescriptor notDesc = new NotifyDescriptor.Confirmation(
                        NbBundle.getMessage(EjbDeploymentConfiguration.class, "MSG_weblogicEjbJarXmlNotValid"),
                        NotifyDescriptor.OK_CANCEL_OPTION);
                Object result = DialogDisplayer.getDefault().notify(notDesc);
                if (result == NotifyDescriptor.CANCEL_OPTION) {
                    // keep the old content
                    return;
                }
                // use the old graph
                newWeblogicEjbJar = oldWeblogicEjbJar;
            }
            
            // perform changes
            modifier.modify(newWeblogicEjbJar);
            
            // save, if appropriate
            boolean modified = dataObject.isModified();
            replaceDocument(doc, newWeblogicEjbJar);
            if (!modified) {
                SaveCookie cookie = (SaveCookie)dataObject.getCookie(SaveCookie.class);
                if (cookie != null) {
                    cookie.save();
                }
            }
            synchronized (this) {
                weblogicEjbJar = newWeblogicEjbJar;
            }
        } catch (BadLocationException ble) {
            // this should not occur, just log it if it happens
            Exceptions.printStackTrace(ble);
        } catch (IOException ioe) {
            String msg = NbBundle.getMessage(EjbDeploymentConfiguration.class, "MSG_CannotUpdateFile", file.getPath());
            throw new ConfigurationException(msg, ioe);
        }
    }

    /**
     * Replace the content of the document by the graph.
     */
    private void replaceDocument(final StyledDocument doc, EjbJarModel graph) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            graph.write(out);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        NbDocument.runAtomic(doc, new Runnable() {
            public void run() {
                try {
                    doc.remove(0, doc.getLength());
                    doc.insertString(0, out.toString(), null);
                } catch (BadLocationException ble) {
                    Exceptions.printStackTrace(ble);
                }
            }
        });
    }

    @Override
    public void bindDatasourceReferenceForEjb(final String ejbName, final String ejbType,
            final String referenceName, final String jndiName) throws ConfigurationException {
        if (ejbName == null || ejbName.length() == 0
                || referenceName == null || referenceName.length() == 0
                || jndiName == null || jndiName.length() == 0) {
            return;
        }

        modifyWeblogicEjbJar(new WeblogicEjbJarModifier() {
            @Override
            public void modify(EjbJarModel webLogicEjbJar) {
                webLogicEjbJar.setReference(ejbName, ejbType, referenceName, jndiName);
            }
        });
    }

    @Override
    public String findDatasourceJndiNameForEjb(String ejbName, String referenceName) throws ConfigurationException {
        EjbJarModel webLogicEjbJar = getWeblogicEjbJar();
        if (webLogicEjbJar == null) { // graph not parseable
            String msg = NbBundle.getMessage(EjbDeploymentConfiguration.class, "MSG_CannotReadReferenceName", file.getPath());
            throw new ConfigurationException(msg);
        }
        return webLogicEjbJar.getReferenceJndiName(ejbName, referenceName);
    }

    /**
     * Genereate WeblogicEjbJar graph.
     */
    private EjbJarModel genereateWeblogicEjbJar() {
        return EjbJarModel.generate(serverVersion);
    }

    private interface WeblogicEjbJarModifier {
        void modify(EjbJarModel context);
    }    
}
