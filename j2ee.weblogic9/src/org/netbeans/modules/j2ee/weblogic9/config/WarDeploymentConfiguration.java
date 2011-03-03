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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ContextRootConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DeploymentDescriptorConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DeploymentPlanConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ServerLibraryConfiguration;
import org.netbeans.modules.j2ee.weblogic9.config.gen.JspDescriptorType;
import org.netbeans.modules.j2ee.weblogic9.config.gen.LibraryRefType;
import org.netbeans.modules.j2ee.weblogic9.config.gen.WeblogicWebApp;
import org.netbeans.modules.schema2beans.AttrProp;
import org.netbeans.modules.schema2beans.BaseBean;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.NbDocument;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.lookup.Lookups;

/**
 * Web module deployment configuration handles creation and updating of the 
 * weblogic.xml configuration file.
 *
 * @author sherold
 */
public class WarDeploymentConfiguration extends WLDeploymentConfiguration
        implements ServerLibraryConfiguration, ModuleConfiguration,
        ContextRootConfiguration, DeploymentPlanConfiguration, PropertyChangeListener, DeploymentDescriptorConfiguration {


    private final ChangeSupport serverLibraryChangeSupport = new ChangeSupport(this);

    private final File file;

    private final J2eeModule j2eeModule;

    private final DataObject dataObject;

    private final FileChangeListener weblogicXmlListener = new WeblogicXmlListener();

    private WeblogicWebApp webLogicWebApp;

    private Set<ServerLibraryDependency> originalDeps;
    
    /**
     * Creates a new instance of WarDeploymentConfiguration 
     */
    public WarDeploymentConfiguration(J2eeModule j2eeModule) {
        super(j2eeModule);
        this.j2eeModule = j2eeModule;
        file = j2eeModule.getDeploymentConfigurationFile("WEB-INF/weblogic.xml"); // NOI18N
        FileUtil.addFileChangeListener(weblogicXmlListener, file);

        getWeblogicWebApp();
        DataObject dataObject = null;
        try {
            dataObject = DataObject.find(FileUtil.toFileObject(file));
            dataObject.addPropertyChangeListener(this);
        } catch(DataObjectNotFoundException donfe) {
            Exceptions.printStackTrace(donfe);
        }
        this.dataObject = dataObject;
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

    @Override
    public boolean isDescriptorRequired() {
        return true;
    }
    
    /**
     * Listen to weblogic.xml document changes.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == DataObject.PROP_MODIFIED &&
                evt.getNewValue() == Boolean.FALSE) {
            // dataobject has been modified, webLogicWebApp graph is out of sync
            synchronized (this) {
                webLogicWebApp = null;
            }
        }
    }
   
    /**
     * Return WeblogicWebApp graph. If it was not created yet, load it from the file
     * and cache it. If the file does not exist, generate it.
     *
     * @return WeblogicWebApp graph or null if the weblogic.xml file is not parseable.
     */
    public final synchronized WeblogicWebApp getWeblogicWebApp() {
        if (webLogicWebApp == null) {
            try {
                if (file.exists()) {
                    // load configuration if already exists
                    try {
                        webLogicWebApp = WeblogicWebApp.createGraph(file);
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    } catch (RuntimeException re) {
                        // weblogic.xml is not parseable, do nothing
                    }
                } else {
                    // create weblogic.xml if it does not exist yet
                    webLogicWebApp = generateWeblogicWebApp();
                    ConfigUtil.writefile(file, webLogicWebApp);
                }
            } catch (ConfigurationException ce) {
                Exceptions.printStackTrace(ce);
            }
        }
        return webLogicWebApp;
    }
    
    // FIXME this is not a proper implementation - deployment PLAN should be saved
    // not a deployment descriptor
    public void save(OutputStream os) throws ConfigurationException {
        WeblogicWebApp webLogicWebApp = getWeblogicWebApp();
        if (webLogicWebApp == null) {
            String msg = NbBundle.getMessage(WarDeploymentConfiguration.class, "MSG_cannotSaveNotParseableConfFile", file.getPath());
            throw new ConfigurationException(msg);
        }
        try {
            webLogicWebApp.write(os);
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
                    String msg = NbBundle.getMessage(WarDeploymentConfiguration.class, "MSG_configFileCannotParse", file.getPath());
                    throw new ConfigurationException(msg);
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
            synchronized (this) {
                webLogicWebApp = newWeblogicWebApp;
            }
        } catch (BadLocationException ble) {
            // this should not occur, just log it if it happens
            Exceptions.printStackTrace(ble);
        } catch (IOException ioe) {
            String msg = NbBundle.getMessage(WarDeploymentConfiguration.class, "MSG_CannotUpdateFile", file.getPath());
            throw new ConfigurationException(msg, ioe);
        }
    }
    
    /**
     * Genereate Context graph.
     */
    private WeblogicWebApp generateWeblogicWebApp() {
        WeblogicWebApp webLogicWebApp = new WeblogicWebApp();
        webLogicWebApp.createAttribute("xmlns:j2ee", "xmlns:j2ee", AttrProp.CDATA | AttrProp.IMPLIED, null, null);
        webLogicWebApp.setAttributeValue("xmlns:j2ee", "http://java.sun.com/xml/ns/j2ee");
        webLogicWebApp.setAttributeValue("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"); // NOI18N
        webLogicWebApp.setAttributeValue("xsi:schemaLocation", "http://www.bea.com/ns/weblogic/90 http://www.bea.com/ns/weblogic/90/weblogic-web-app.xsd"); // NOI18N
        webLogicWebApp.setContextRoot(new String [] {""}); // NOI18N
        JspDescriptorType jspDescriptor = new JspDescriptorType();
        jspDescriptor.setKeepgenerated(true);
        webLogicWebApp.setJspDescriptor(new JspDescriptorType[] {jspDescriptor});
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
    
    // TODO: this contextPath fix code will be removed, as soon as it will 
    // be moved to the web project
    private boolean isCorrectCP(String contextPath) {
        boolean correct=true;
        if (!contextPath.equals("") && !contextPath.startsWith("/")) correct=false; //NOI18N
        else if (contextPath.endsWith("/")) correct=false; //NOI18N
        else if (contextPath.indexOf("//")>=0) correct=false; //NOI18N
        return correct;
    }
    
    public String getContextRoot() throws ConfigurationException {
        WeblogicWebApp webLogicWebApp = getWeblogicWebApp();
        if (webLogicWebApp == null) { // graph not parseable
            String msg = NbBundle.getMessage(WarDeploymentConfiguration.class, "MSG_CannotReadContextRoot", file.getPath());
            throw new ConfigurationException(msg);
        }
        return webLogicWebApp.getContextRoot(0);
    }

    public void setContextRoot(String contextRoot) throws ConfigurationException {
        // TODO: this contextPath fix code will be removed, as soon as it will 
        // be moved to the web project
        if (!isCorrectCP(contextRoot)) {
            String ctxRoot = contextRoot;
            java.util.StringTokenizer tok = new java.util.StringTokenizer(contextRoot,"/"); //NOI18N
            StringBuffer buf = new StringBuffer(); //NOI18N
            while (tok.hasMoreTokens()) {
                buf.append("/"+tok.nextToken()); //NOI18N
            }
            ctxRoot = buf.toString();
            NotifyDescriptor desc = new NotifyDescriptor.Message(
                    NbBundle.getMessage (WarDeploymentConfiguration.class, "MSG_invalidCP", contextRoot),
                    NotifyDescriptor.Message.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
            contextRoot = ctxRoot;
        }
        final String newContextPath = contextRoot;
        modifyWeblogicWebApp(new WeblogicWebAppModifier() {
            public void modify(WeblogicWebApp webLogicWebApp) {
                webLogicWebApp.setContextRoot(new String [] {newContextPath});
            }
        });
    }

    @Override
    public void configureLibrary(@NonNull final ServerLibraryDependency library) throws ConfigurationException {
        assert library != null;

        modifyWeblogicWebApp(new WeblogicWebAppModifier() {
            public void modify(WeblogicWebApp webLogicWebApp) {
                for (LibraryRefType libRef : webLogicWebApp.getLibraryRef()) {
                    ServerLibraryDependency lib = getServerLibraryRange(libRef);
                    if (library.specificationEquals(lib)) {
                        return;
                    }
                }

                addServerLibraryRange(webLogicWebApp, library);
            }
        });
    }

    @Override
    public Set<ServerLibraryDependency> getLibraries() throws ConfigurationException {
        WeblogicWebApp webLogicWebApp = getWeblogicWebApp();
        if (webLogicWebApp == null) { // graph not parseable
            String msg = NbBundle.getMessage(WarDeploymentConfiguration.class, "MSG_CannotReadServerLibraries", file.getPath());
            throw new ConfigurationException(msg);
        }

        Set<ServerLibraryDependency> ranges = new HashSet<ServerLibraryDependency>();
        for (LibraryRefType libRef : webLogicWebApp.getLibraryRef()) {
            ranges.add(getServerLibraryRange(libRef));
        }

        return ranges;
    }

    @Override
    public void addLibraryChangeListener(@NonNull ChangeListener listener) {
        Parameters.notNull("listener", listener);

        boolean load = false;
        synchronized (this) {
            load = originalDeps == null;
        }
        if (load) {
            Set<ServerLibraryDependency> deps = null;
            try {
                deps = getLibraries();
            } catch(ConfigurationException ex) {
                deps = Collections.emptySet();
            }
            synchronized (this) {
                if (originalDeps == null) {
                    originalDeps = deps;
                }
            }
        }

        serverLibraryChangeSupport.addChangeListener(listener);
    }

    @Override
    public void removeLibraryChangeListener(@NonNull ChangeListener listener) {
        Parameters.notNull("listener", listener);
        serverLibraryChangeSupport.removeChangeListener(listener);
    }

    private void fireChange() {
        Set<ServerLibraryDependency> oldDeps = null;
        synchronized (this) {
            if (originalDeps == null) {
                // nobody is listening
                return;
            }
            oldDeps = new HashSet<ServerLibraryDependency>(originalDeps);
        }

        Set<ServerLibraryDependency> deps = new HashSet<ServerLibraryDependency>();
        try {
            deps.addAll(getLibraries());
        } catch (ConfigurationException ex) {
            // noop - empty set
        }
        boolean fire = false;
        for (ServerLibraryDependency old : oldDeps) {
            if (!deps.remove(old)) {
                fire = true;
                break;
            }
        }
        if (!deps.isEmpty()) {
            fire = true;
        }
        if (fire) {
            serverLibraryChangeSupport.fireChange();
        }
    }

    private ServerLibraryDependency getServerLibraryRange(LibraryRefType libRef) {
        String name = libRef.getLibraryName();
        String specVersionString = libRef.getSpecificationVersion();
        String implVersionString = libRef.getImplementationVersion();
        boolean exactMatch = libRef.isExactMatch();

        Version spec = specVersionString == null ? null : Version.fromJsr277NotationWithFallback(specVersionString);
        Version impl = implVersionString == null ? null : Version.fromJsr277NotationWithFallback(implVersionString);
        if (exactMatch) {
            return ServerLibraryDependency.exactVersion(name, spec, impl);
        } else {
            return ServerLibraryDependency.minimalVersion(name, spec, impl);
        }
    }

    private void addServerLibraryRange(WeblogicWebApp webApp, ServerLibraryDependency library) {
        LibraryRefType libRef = new LibraryRefType();
        libRef.setLibraryName(library.getName());
        if (library.isExactMatch()) {
            libRef.setExactMatch(true);
        }
        if (library.getSpecificationVersion() != null) {
            libRef.setSpecificationVersion(library.getSpecificationVersion().toString());
        }
        if (library.getImplementationVersion() != null) {
            libRef.setImplementationVersion(library.getImplementationVersion().toString());
        }
        
        LibraryRefType[] refs = webApp.getLibraryRef();
        LibraryRefType[] updated = new LibraryRefType[refs.length + 1];
        System.arraycopy(refs, 0, updated, 1, refs.length);
        updated[0] = libRef;
        webApp.setLibraryRef(updated);
    }

    // private helper interface -----------------------------------------------

    private class WeblogicXmlListener implements FileChangeListener {

        @Override
        public void fileFolderCreated(FileEvent fe) {
            // noop
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            fireChange();
        }

        @Override
        public void fileChanged(FileEvent fe) {
            fireChange();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            fireChange();
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            fireChange();
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            // noop
        }
    }

    private interface WeblogicWebAppModifier {
        void modify(WeblogicWebApp context);
    }
}
