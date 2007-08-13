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

package org.netbeans.modules.tomcat5.config;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ContextRootConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DatasourceConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DeploymentPlanConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;
import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.schema2beans.Schema2BeansException;
import org.netbeans.modules.tomcat5.TomcatManager.TomcatVersion;
import org.netbeans.modules.tomcat5.config.gen.Context;
import org.netbeans.modules.tomcat5.config.gen.Parameter;
import org.netbeans.modules.tomcat5.config.gen.ResourceParams;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/** 
 * Server specific configuration data related to Tomcat 5 server
 *
 * @author sherold
 */
public class TomcatModuleConfiguration implements ModuleConfiguration, ContextRootConfiguration, 
        DatasourceConfiguration, DeploymentPlanConfiguration, PropertyChangeListener {
    
    private final J2eeModule j2eeModule;
    private final TomcatVersion tomcatVersion;
    
    private DataObject contextDataObject;
    private File contextXml;
    private Context context;
    
    private final String ATTR_PATH = "path"; // NOI18N
    
    private static final Logger LOGGER = Logger.getLogger("org.netbeans.modules.tomcat5"); // NOI18N
    
    /** Creates a new instance of TomcatModuleConfiguration */
    public TomcatModuleConfiguration(J2eeModule j2eeModule, TomcatVersion tomcatVersion) {
        this.j2eeModule = j2eeModule;
        this.tomcatVersion = tomcatVersion;
        contextXml = j2eeModule.getDeploymentConfigurationFile("META-INF/context.xml"); // NOI18N
        init(contextXml);
    }
    
    /**
     * WebappConfiguration initialization. This method should be called before
     * this class is being used.
     *
     * @param contextXml context.xml file.
     */
    private void init(File contextXml) {
        this.contextXml = contextXml;
        try {
            getContext();
        } catch (ConfigurationException e) {
            LOGGER.log(Level.FINE, null, e);
        }
        if (contextDataObject == null) {
            try {
                contextDataObject = DataObject.find(FileUtil.toFileObject(contextXml));
                contextDataObject.addPropertyChangeListener(this);
            } catch(DataObjectNotFoundException donfe) {
                LOGGER.log(Level.FINE, null, donfe);
            }
        }
    }
    
    public Lookup getLookup() {
        return Lookups.fixed(this);
    }

    public void dispose() {
        // no op
    }

    public boolean supportsCreateDatasource() {
        return true;
    }
    
    /**
     * Return Context graph. If it was not created yet, load it from the file
     * and cache it. If the file does not exist, generate it.
     *
     * @return Context graph.
     * 
     * @throws ConfigurationException if the context.xml file is not accessible or not parseable.
     */
    public synchronized Context getContext() throws ConfigurationException {
        if (context == null) {
            if (contextXml.exists()) {
                // load configuration if already exists
                try {
                    context = Context.createGraph(contextXml);
                } catch (IOException e) {
                    String msg = NbBundle.getMessage(TomcatModuleConfiguration.class, "MSG_ContextXmlReadFail", contextXml.getPath());
                    throw new ConfigurationException(msg, e);
                } catch (RuntimeException e) {
                    // context.xml is not parseable
                    String msg = NbBundle.getMessage(TomcatModuleConfiguration.class, "MSG_ContextXmlBroken", contextXml.getPath());
                    throw new ConfigurationException(msg, e);
                }
            } else {
                // create context.xml if it does not exist yet
                context = genereateContext();
                writefile(contextXml);
            }
        }
        return context;
    }
    
    /**
     * Return context path.
     * 
     * @return context path or null, if the file is not parseable.
     */
    public String getContextRoot() throws ConfigurationException {
        return getContext().getAttributeValue(ATTR_PATH);
    }
    
    
    /**
     * Get the module datasources defined in the context.xml file.
     */
    public Set<Datasource> getDatasources() throws ConfigurationException {
        Context context = getContext();
        Set<Datasource> result = new HashSet<Datasource>();
        int length = context.getResource().length;
        if (tomcatVersion != TomcatVersion.TOMCAT_50) {
            // Tomcat 5.5.x or Tomcat 6.0.x
            for (int i = 0; i < length; i++) {
                String type = context.getResourceType(i);
                if ("javax.sql.DataSource".equals(type)) { // NOI18N
                    String name     = context.getResourceName(i);
                    String username = context.getResourceUsername(i);
                    String url      = context.getResourceUrl(i);
                    String password = context.getResourcePassword(i);
                    String driverClassName = context.getResourceDriverClassName(i);
                    if (name != null && username != null && url != null && driverClassName != null) {
                        // return the datasource only if all the needed params are non-null except the password param
                        result.add(new TomcatDatasource(username, url, password, name, driverClassName));
                    }
                }
            }
        } else {
            // Tomcat 5.0.x
            ResourceParams[] resourceParams = context.getResourceParams();
            for (int i = 0; i < length; i++) {
                String type = context.getResourceType(i);
                if ("javax.sql.DataSource".equals(type)) { // NOI18N
                    String name = context.getResourceName(i);
                    // find the resource params for the selected resource
                    for (int j = 0; j < resourceParams.length; j++) {
                        if (name.equals(resourceParams[j].getName())) {
                            Parameter[] params = resourceParams[j].getParameter();
                            HashMap paramNameValueMap = new HashMap(params.length);
                            for (Parameter parameter : params) {
                                paramNameValueMap.put(parameter.getName(), parameter.getValue());
                            }
                            String username = (String) paramNameValueMap.get("username"); // NOI18N
                            String url      = (String) paramNameValueMap.get("url"); // NOI18N
                            String password = (String) paramNameValueMap.get("password"); // NOI18N
                            String driverClassName = (String) paramNameValueMap.get("driverClassName"); // NOI18N
                            if (username != null && url != null && driverClassName != null) {
                                // return the datasource only if all the needed params are non-null except the password param
                                result.add(new TomcatDatasource(username, url, password, name, driverClassName));
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
    
    
    public Datasource createDatasource(final String name, final String url, 
            final String username, final String password, final String driverClassName) 
            throws ConfigurationException, DatasourceAlreadyExistsException {
        // check whether a resource of the given name is not already defined in the module
        List<Datasource> conflictingDS = new ArrayList<Datasource>();
        for (Datasource datasource : getDatasources()) {
            if (name.equals(datasource.getJndiName())) {
                conflictingDS.add(datasource);
            }
        }
        if (conflictingDS.size() > 0) {
            throw new DatasourceAlreadyExistsException(conflictingDS);
        }
        if (tomcatVersion != TomcatVersion.TOMCAT_50) {
            // Tomcat 5.5.x or Tomcat 6.0.x
            modifyContext(new ContextModifier() {
                public void modify(Context context) {
                    int idx = context.addResource(true);
                    context.setResourceName(idx, name);
                    context.setResourceAuth(idx, "Container"); // NOI18N
                    context.setResourceType(idx, "javax.sql.DataSource"); // NOI18N
                    context.setResourceDriverClassName(idx, driverClassName);
                    context.setResourceUrl(idx, url);
                    context.setResourceUsername(idx, username);
                    context.setResourcePassword(idx, password);
                    context.setResourceMaxActive(idx, "20");    // NOI18N
                    context.setResourceMaxIdle(idx, "10");      // NOI18N
                    context.setResourceMaxWait(idx, "-1");      // NOI18N
                }
            });
        } else {
            // Tomcat 5.0.x
            modifyContext(new ContextModifier() {
                public void modify(Context context) {
                    int idx = context.addResource(true);
                    context.setResourceName(idx, name);
                    context.setResourceAuth(idx, "Container"); // NOI18N
                    context.setResourceType(idx, "javax.sql.DataSource"); // NOI18N

                    // check whether resource params not already defined
                    ResourceParams[] resourceParams = context.getResourceParams();
                    for (int i = 0; i < resourceParams.length; i++) {
                        if (name.equals(resourceParams[i].getName())) {
                            // if this happens in means that for this ResourceParams
                            // element was no repspective Resource element - remove it
                            context.removeResourceParams(resourceParams[i]);
                        }
                    }
                    ResourceParams newResourceParams = createResourceParams(
                            name, 
                            new Parameter[] {
                                createParameter("factory", "org.apache.commons.dbcp.BasicDataSourceFactory"), // NOI18N
                                createParameter("driverClassName", driverClassName), // NOI18N
                                createParameter("url", url),                // NOI18N
                                createParameter("username", username),      // NOI18N
                                createParameter("password", password),      // NOI18N
                                createParameter("maxActive", "20"), // NOI18N
                                createParameter("maxIdle", "10"),   // NOI18N
                                createParameter("maxWait", "-1")    // NOI18N
                            }
                    );
                    context.addResourceParams(newResourceParams);
                }
            });
        }
        return new TomcatDatasource(username, url, password, name, driverClassName);
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
                    NbBundle.getMessage (TomcatModuleConfiguration.class, "MSG_invalidCP", contextPath),
                    NotifyDescriptor.Message.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
            contextPath = ctxRoot;
        }
        final String newContextPath = contextPath;
        modifyContext(new ContextModifier() {
            public void modify(Context context) {
                // if Tomcat 5.0.x update also logger prefix
                if (tomcatVersion == TomcatVersion.TOMCAT_50) {
                    String oldContextPath = context.getAttributeValue(ATTR_PATH);
                    String oldPrefix = context.getLoggerPrefix();
                    if (oldPrefix != null 
                            && oldPrefix.equals(computeLoggerPrefix(oldContextPath))) {
                        context.setLoggerPrefix(computeLoggerPrefix(newContextPath));
                    }
                }
                context.setAttributeValue(ATTR_PATH, newContextPath);
            }
        });
    }
    
    /** Context data object */
    public DataObject getContextDataObject() {
        return contextDataObject;
    }
    
    // PropertyChangeListener listener ----------------------------------------
    
    /**
     * Listen to context.xml document changes.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == DataObject.PROP_MODIFIED &&
                evt.getNewValue() == Boolean.FALSE) {
            // dataobject has been modified, context graph is out of sync
            context = null;
        }
    }
        
    public J2eeModule getJ2eeModule() {
        return j2eeModule;
    }
    
    public void save (OutputStream os) throws ConfigurationException {
        Context ctx = getContext();
        try {
            ctx.write(os);
        } catch (IOException ioe) {
            String msg = NbBundle.getMessage(TomcatModuleConfiguration.class, "MSG_ContextXmlWriteFail", contextXml.getPath());
            throw new ConfigurationException(msg, ioe);
        }
    }
        
    // private helper methods -------------------------------------------------
    
    /**
     * Genereate Context graph.
     */
    private Context genereateContext() {
        Context newContext = new Context();
        String path = ""; // NOI18N
        newContext.setAttributeValue(ATTR_PATH, path);

        // if tomcat 5.0.x generate a logger
        if (tomcatVersion == TomcatVersion.TOMCAT_50) {
            // generate default logger
            newContext.setLogger(true);
            newContext.setLoggerClassName("org.apache.catalina.logger.FileLogger"); // NOI18N
            newContext.setLoggerPrefix(computeLoggerPrefix(path));
            newContext.setLoggerSuffix(".log");    // NOI18N
            newContext.setLoggerTimestamp("true"); // NOI18N
        }
        return newContext;
    }
    
    /**
     * Perform context changes defined by the context modifier. Update editor
     * content and save changes, if appropriate.
     *
     * @param modifier
     */
    private void modifyContext(ContextModifier modifier) throws ConfigurationException {
        assert contextDataObject != null : "DataObject has not been initialized yet"; // NIO18N
        try {
            // get the document
            EditorCookie editor = (EditorCookie)contextDataObject.getCookie(EditorCookie.class);
            StyledDocument doc = editor.getDocument();
            if (doc == null) {
                doc = editor.openDocument();
            }
            
            // get the up-to-date model
            Context newContext = null;
            try {
                // try to create a graph from the editor content
                byte[] docString = doc.getText(0, doc.getLength()).getBytes();
                newContext = Context.createGraph(new ByteArrayInputStream(docString));
            } catch (RuntimeException e) {
                Context oldContext = getContext(); // throws an exception if not parseable
                // current editor content is not parseable, ask whether to override or not
                NotifyDescriptor notDesc = new NotifyDescriptor.Confirmation(
                        NbBundle.getMessage(TomcatModuleConfiguration.class, "MSG_ContextXmlNotValid"),
                        NotifyDescriptor.OK_CANCEL_OPTION);
                Object result = DialogDisplayer.getDefault().notify(notDesc);
                if (result == NotifyDescriptor.CANCEL_OPTION) {
                    // keep the old content
                    return;
                }
                // use the old graph
                newContext = oldContext;
            }
            
            // perform changes
            modifier.modify(newContext);
            
            // save, if appropriate
            boolean modified = contextDataObject.isModified();
            replaceDocument(doc, newContext);
            if (!modified) {
                SaveCookie cookie = (SaveCookie)contextDataObject.getCookie(SaveCookie.class);
                if (cookie != null) {
                    cookie.save();
                }
            }
            context = newContext;
        } catch (BadLocationException e) {
            String msg = NbBundle.getMessage(TomcatModuleConfiguration.class, "MSG_ContextXmlWriteFail", contextXml.getPath());
            throw new ConfigurationException(msg, e);
        } catch (IOException e) {
            String msg = NbBundle.getMessage(TomcatModuleConfiguration.class, "MSG_ContextXmlWriteFail", contextXml.getPath());
            throw new ConfigurationException(msg, e);
        }
    }
    
    private Parameter createParameter(String name, String value) {
        Parameter parameter = new Parameter();
        parameter.setName(name);
        parameter.setValue(value);
        return parameter;
    }
    
    private ResourceParams createResourceParams(String name, Parameter[] parameters) {
        ResourceParams resourceParams = new ResourceParams();
        resourceParams.setName(name);
        for (int i = 0; i < parameters.length; i++) {
            resourceParams.addParameter(parameters[i]);
        }
        return resourceParams;
    }
    
    /**
     * Compute logger prefix based on context path. Cut off leading slash and 
     * escape other slashes, use ROOT prefix for empty context path.
     */
    private String computeLoggerPrefix(String contextPath) {
        return contextPath.length() > 0 
                ? contextPath.substring(1).replace('/', '_').concat(".") // NOI18N
                : "ROOT.";   // NOI18N
    }
    
    private void writefile(final File file) throws ConfigurationException {
        try {
            FileObject cfolder = FileUtil.toFileObject(file.getParentFile());
            if (cfolder == null) {
                File parentFile = file.getParentFile();
                try {
                    cfolder = FileUtil.toFileObject(parentFile.getParentFile()).createFolder(parentFile.getName());
                } catch (IOException ioe) {
                    throw new ConfigurationException(NbBundle.getMessage(TomcatModuleConfiguration.class, "MSG_FailedToCreateConfigFolder", parentFile.getAbsolutePath()));
                }
            }
            final FileObject folder = cfolder;
            final ConfigurationException anonClassException[] = new ConfigurationException[]{null};
            FileSystem fs = folder.getFileSystem();
            fs.runAtomicAction(new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    String name = file.getName();
                    FileObject configFO = folder.getFileObject(name);
                    if (configFO == null) {
                        configFO = folder.createData(name);
                    }
                    Context ctx = null;
                    try{
                        ctx = getContext();
                    } catch (ConfigurationException e) {
                        // propagate exception out to the outer class
                        anonClassException[0] = e;
                        return;
                    }
                    FileLock lock = configFO.lock();
                    try {
                        OutputStream os = new BufferedOutputStream(configFO.getOutputStream(lock), 4096);
                        try {
                            if (ctx != null) {
                                ctx.write(os);
                            }
                        } finally {
                            os.close();
                        }
                    } finally {
                        lock.releaseLock();
                    }
                }
            });
            if (anonClassException[0] != null) {
                throw anonClassException[0];
            }
        } catch (IOException e) {
            String msg = NbBundle.getMessage(TomcatModuleConfiguration.class, "MSG_ContextXmlWriteFail", contextXml.getPath());
            throw new ConfigurationException(msg, e);
        }
    }
    
    /**
     * Replace the content of the document by the graph.
     */
    private void replaceDocument(final StyledDocument doc, BaseBean graph) {
        final StringWriter out = new StringWriter();
        try {
            graph.write(out);
        } catch (Schema2BeansException ex) {
            Logger.getLogger(TomcatModuleConfiguration.class.getName()).log(Level.INFO, null, ex);
        } catch (IOException ioe) {
            Logger.getLogger(TomcatModuleConfiguration.class.getName()).log(Level.INFO, null, ioe);
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

    public void bindDatasourceReference(final String referenceName, final String jndiName) throws ConfigurationException {
        Set<Datasource>  datasources = getDatasources();
        // check whether a resource of the given name is not already defined
        for (Datasource ds : datasources) {
            if (referenceName.equals(ds.getJndiName())) { // Tomcat DS JNDI name acts as a reference name
                // do nothing if already exists
                return;
            }
        }
        
        Context context = getContext();
        // check whether a resource link of the given name is not already defined
        int lengthResourceLink = context.getResourceLink().length;
        for (int i = 0; i < lengthResourceLink; i++) {
            if (referenceName.equals(context.getResourceLinkName(i))) {
                // do nothing if already exists
                return;
            }
        }
        
        // try to find a datasource whose resource name equals the given jndiName 
        for (Datasource ds : datasources) {
            if (jndiName.equals(ds.getJndiName())) { // Tomcat DS JNDI name acts as a reference name
                try {
                    createDatasource(referenceName, ds.getUrl(), ds.getUsername(), ds.getPassword(), ds.getDriverClassName());
                } catch (DatasourceAlreadyExistsException ex) {
                    // this should not happen
                    LOGGER.info("Datasource with the '" + referenceName + "' reference name already exists."); // NOI18N
                }
                return;
            }
        }

        // create a resource link to the global resource
        modifyContext(new ContextModifier() {
            public void modify(Context context) {
                int idx = context.addResourceLink(true);
                context.setResourceLinkName(idx, referenceName);
                context.setResourceLinkGlobal(idx, jndiName);
                context.setResourceLinkType(idx, "javax.sql.DataSource"); // NOI18N
            }
        });
        
    }

    public void bindDatasourceReferenceForEjb(String ejbName, String ejbType, String referenceName, String jndiName) throws ConfigurationException {
        // not supported
    }

    public String findDatasourceJndiName(String referenceName) throws ConfigurationException {
        Context context = getContext();
        if (context != null) {
            int lengthResource = context.getResource().length;
                for (int i = 0; i < lengthResource; i++) {
                    if (referenceName.equals(context.getResourceName(i))) {
                        // DS with this name is defined in context.xml, there is no
                        // real JNDI name in Tomcat 'local' DSs, lets return reference name
                        return referenceName;
                    }
                }
                // check whether a resource link of the given name is not already defined
                int lengthResourceLink = context.getResourceLink().length;
                for (int i = 0; i < lengthResourceLink; i++) {
                    if (referenceName.equals(context.getResourceLinkName(i))) {
                        // return global resource name
                        return context.getResourceLinkGlobal(i);
                    }
                }
        }
        // nothing was found
        return null;
    }

    public String findDatasourceJndiNameForEjb(String ejbName, String referenceName) throws ConfigurationException {
        // not supported
        return null;
    }
    
    // private helper interface -----------------------------------------------
     
    private interface ContextModifier {
        void modify(Context context);
    }
}
