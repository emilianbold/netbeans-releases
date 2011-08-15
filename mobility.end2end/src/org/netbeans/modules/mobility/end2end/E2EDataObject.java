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

/*
 * E2EDataObject.java
 *
 * Created on June 27, 2005, 2:51 PM
 *
 */
package org.netbeans.modules.mobility.end2end;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.mobility.e2e.classdata.ClassDataRegistry;
import org.netbeans.modules.mobility.e2e.mapping.JavonMappingImpl;
import org.netbeans.modules.mobility.end2end.classdata.AbstractService;
import org.netbeans.modules.mobility.end2end.classdata.ClassData;
import org.netbeans.modules.mobility.end2end.classdata.OperationData;
import org.netbeans.modules.mobility.end2end.classdata.WSDLService;
import org.netbeans.modules.mobility.end2end.client.config.ClientConfiguration;
import org.netbeans.modules.mobility.end2end.client.config.Configuration;
import org.netbeans.modules.mobility.end2end.client.config.ConfigurationReader;
import org.netbeans.modules.mobility.end2end.client.config.ConfigurationWriter;
import org.netbeans.modules.mobility.end2end.client.config.ServerConfiguration;
import org.netbeans.modules.mobility.end2end.util.Util;
import org.netbeans.modules.mobility.javon.JavonMapping;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;
import org.netbeans.modules.xml.multiview.XmlMultiViewEditorSupport;
import org.netbeans.spi.mobility.end2end.E2EServiceProvider;
import org.netbeans.spi.mobility.end2end.ServiceGeneratorResult;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.modules.xml.multiview.XmlMultiViewElement;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

/**
 *
 * @author Michal Skvor, Anton Chechel
 */
@Messages("CTL_SourceTabCaption=Source") // NOI18N
public class E2EDataObject extends XmlMultiViewDataObject {
    public static final String PROP_GENERATING = "generating"; // NOI18N
    public static final String ICON_BASE = "org/netbeans/modules/j2ee/ddloaders/web/resources/DDDataIcon.gif"; // NOI18N

    public static final String MIME_TYPE_CLASS = "text/x-wsclient.class"; // NOI18N
    public static final String MIME_TYPE_WSDL = "text/x-wsclient.wsdl"; // NOI18N
    public static final String MIME_TYPE_JSR172 = "text/x-wsclient.jsr172"; // NOI18N

    private static final String TRUE = "true"; // NOI18N

    public static RequestProcessor rp; //generator
      
    // Configuration
    protected Configuration configuration;
    // Project the file is in
    private Project clientProject;
    private Project serverProject;
        
    protected Set<SaveCallback> saveCallbacks;
    
    private ModelSynchronizer synchronizer;

    protected volatile boolean generating;
    private volatile XmlMultiViewEditorSupport xmlEditorSupport;
    
    
    /** Creates a new instance of E2EDataObject */
    public E2EDataObject(FileObject file, MultiFileLoader ldr) throws DataObjectExistsException {
        super(file, ldr);
        synchronizer = new ModelSynchronizer(this);
        saveCallbacks = new HashSet<SaveCallback>();
    }
    
    /**
     * Adds callback to callback list
     *
     * @param callBack callback to be called before the file is written to the disc
     */
    public void addSaveCallback(final SaveCallback callBack) {
        saveCallbacks.add(callBack);
    }
    
    /**
     * Removes callback from the callback list
     *
     * @param callBack callback to be called before the file is written to the disc
     */
    public void removeSaveCallback(final SaveCallback callBack) {
        saveCallbacks.remove(callBack);
    }
    
    @Override
    protected Node createNodeDelegate() {
        return new E2EDataNode(this);
    }
    
    @Override
    public void setModified(final boolean modif) {
        super.setModified(modif);
        if (modif) {
            synchronizer.requestUpdateData();
        }
    }
    
    public Configuration getConfiguration() {
        if (configuration == null) {
            try {
                configuration = ConfigurationReader.read(this);
            } catch (Exception e) {
                configuration = null;
                ErrorManager.getDefault().log(e.getMessage());
            }
        }
        return configuration;
    }
    
    public void setConfiguration(final Configuration configuration) {
        this.configuration = configuration;
        setModified(true);
    }
    
    public Project getClientProject() {
        if (clientProject == null) {
            clientProject = FileOwnerQuery.getOwner(getPrimaryFile());
        }
        return clientProject;
    }
    
    public Project getServerProject() {
        final Configuration config = getConfiguration();
        if (serverProject == null) {
            final OpenProjects openProject = OpenProjects.getDefault();
            final Project[] openedProjects = openProject.getOpenProjects();
            if (config.getServerConfigutation() == null) {
                return null;
            }
            final String serverProjectName = config.getServerConfigutation().getProjectName();
            for (final Project p : openedProjects) {
                final ProjectInformation pi = p.getLookup().lookup(ProjectInformation.class);
                final String webProjectName = pi.getName();
                if (serverProjectName.equals(webProjectName)) {
                    serverProject = p;
                    break;
                }
            }
            
            if (serverProject == null) {
                // TODO: add some dialog to inform user that his Web project is not there ...
                //System.err.println( "Cannot find server node" );
            }
        }
        return serverProject;
    }
    
    // FIXME: this method should be rather in GenerateAction
    public JavonMappingImpl getMapping()  {
        final Configuration config = getConfiguration();
        final List<String> classPath = new ArrayList<String>();

        final ServerConfiguration sc = config.getServerConfigutation();
        final Sources ssources = getServerProject().getLookup().lookup(Sources.class);
        final SourceGroup[] ssg = ssources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        List<ClasspathInfo> classpaths = new ArrayList<ClasspathInfo>();

        for (int i = 0; i < ssg.length; i++) {
            final ClassPath cp = ClassPath.getClassPath(ssg[i].getRootFolder(), ClassPath.SOURCE);
            final FileObject[] roots = cp.getRoots();
            for (int j = 0; j < roots.length; j++) {
                File f;
                if ((f = FileUtil.toFile(roots[j])) != null) {
                    classPath.add(f.getAbsolutePath());
                }
                classpaths.add(ClasspathInfo.create(roots[j]));
            }
        }
                
        //TODO: Dirty hack
        List<ClasspathInfo> classpathInfos = new ArrayList<ClasspathInfo>();
        classpathInfos.add(ClasspathInfo.create(ssg[0].getRootFolder()));   // TODO: fix this!!!
        if (config.getServiceType().equals(Configuration.WSDLCLASS_TYPE)) {
            FileObject projectFO = getServerProject().getProjectDirectory().getFileObject("build/generated/wsimport/client"); // NOI18N
            if (projectFO != null) {
                classpathInfos.add(ClasspathInfo.create(projectFO));
            }
        }

        // TODO: fix 
        final ClassDataRegistry registry = ClassDataRegistry.getRegistry(ClassDataRegistry.DEFAULT_PROFILE, classpathInfos);
        // Create new mapping
        JavonMappingImpl mapping = new JavonMappingImpl(registry);
        
        // Client part of the mapping
        final ClientConfiguration cc = config.getClientConfiguration();
        final JavonMappingImpl.Client jcc = new JavonMappingImpl.Client();
        final Properties cprops = cc.getProperties();
        jcc.setClassName(cc.getClassDescriptor().getLeafClassName());
        jcc.setPackageName(cc.getClassDescriptor().getPackageName());
        final Sources csources = getClientProject().getLookup().lookup(Sources.class);
        final SourceGroup csg = Util.getPreselectedGroup(
                csources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA),
                cc.getClassDescriptor().getLocation());
        jcc.setOutputDirectory(FileUtil.toFile(csg.getRootFolder()).getPath());
        mapping.setProperty(JavonMapping.CREATE_STUBS, cprops.getProperty("createStubs").equals(TRUE)); // NOI18N
        mapping.setProperty(JavonMapping.DATABINDING, cprops.getProperty("DataBinding")); // NOI18N
        mapping.setProperty(JavonMapping.FLOATING_POINT_SUPPORT, cprops.getProperty("floatingPoint").equals(TRUE)); // NOI18N
        mapping.setClientMapping(jcc);
        
        /* Server part of the mapping */
        final ProjectInformation pi = getServerProject().getLookup().lookup(ProjectInformation.class);
        final JavonMappingImpl.Server jsc = new JavonMappingImpl.Server();
        jsc.setProjectName(pi.getName());
        jsc.setClassName(sc.getClassDescriptor().getLeafClassName());
        jsc.setPackageName(sc.getClassDescriptor().getPackageName());
        jsc.setOutputDirectory(FileUtil.toFile(Util.getPreselectedGroup(ssg, sc.getClassDescriptor().getLocation()).getRootFolder()).getPath());

        jsc.setLocation(Util.getServerLocation(getServerProject()));
        jsc.setPort(Util.getServerPort(getServerProject()));
        jsc.setServletLocation(configuration.getServerConfigutation().getProjectName() + "/servlet/" + //NOI18N
                configuration.getServerConfigutation().getClassDescriptor().getType());
        mapping.setServerMapping(jsc);

        int methodID = 1;
        final List<AbstractService> services = config.getServices();
//        // there must be one and just one service
        
        final List<ClassData> classes = services.get(0).getData();
        for (final ClassData ccd : classes) {
            JavonMappingImpl.Service javonService = new JavonMappingImpl.Service();
            javonService.setPackageName(ccd.getPackageName());
            if (Configuration.WSDLCLASS_TYPE.equals(config.getServiceType())) {
                WSDLService wsdlService = (WSDLService) services.get(0);
                javonService.setClassName(wsdlService.getType());
            } else {
                javonService.setClassName(ccd.getClassName());
            }

            String className = ccd.getProxyClassType();
            if (className == null) {
                className = ccd.getType();
            }
            mapping.setProperty("instance", className); // NOI18N

            // TODO!!!!!
            while (SourceUtils.isScanInProgress()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // just ignore it 
                }
            }
            
            registry.updateClassDataTree();
            final org.netbeans.modules.mobility.e2e.classdata.ClassData classData = registry.getClassData(className);
            if (classData == null) {
                continue;
            }

            final List<OperationData> methods = ccd.getOperations();
            final List<org.netbeans.modules.mobility.e2e.classdata.MethodData> methodsData = classData.getMethods();
            for (int j = 0; j < methods.size(); j++) {
                String methodName = null;
                if (Configuration.WSDLCLASS_TYPE.equals(config.getServiceType())) {
                    methodName = methods.get(j).getMethodName();
                } else {
                    methodName = methods.get(j).getName();
                }
                final int methodIndex = findMethodIndex(methodsData, methodName);
                if (methodIndex >= 0) {
                    org.netbeans.modules.mobility.e2e.classdata.MethodData mmd = methodsData.get(methodIndex);
                    mmd.setRequestID(methodID++);
                    javonService.addMethod(mmd);
                }
            }
            mapping.addServiceMaping(javonService);
        }
        mapping.setServletURL(Util.getServerURL(getServerProject(), getConfiguration()));

        return mapping;
    }
    
    private int findMethodIndex(final List<org.netbeans.modules.mobility.e2e.classdata.MethodData> methods, final String methodName) {
        int result = 0;
        for (org.netbeans.modules.mobility.e2e.classdata.MethodData method : methods) {
            // FIXME: check parameters and return types
            if (method.getName().equals(methodName)) {
                return result;
            }
            result++;
        }

        return -1;
    }
        
    @Override
    protected String getPrefixMark() {
        return null;
    }
    
    public synchronized void generate() {
        generating = true;
        // Save document before generation of files
        try {
            getEditorSupport().saveDocument();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        firePropertyChange(PROP_GENERATING, Boolean.FALSE, Boolean.TRUE);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (rp == null) {
                    rp = new RequestProcessor("End2EndGenerator", 5); // NOI18N
                }
                rp.post(new Runnable() {
                    @Override
                    public void run() {
                        final Lookup.Result<E2EServiceProvider> result = Lookup.getDefault().lookup(new Lookup.Template<E2EServiceProvider>(E2EServiceProvider.class));
                        for (final E2EServiceProvider elem : result.allInstances()) {
                            if (E2EDataObject.this.getConfiguration().getServiceType().equals(elem.getServiceType())) {
                                final ServiceGeneratorResult service = elem.generateStubs(Lookups.singleton(E2EDataObject.this));
                            }
                        }
                        generating = false;
                        E2EDataObject.this.firePropertyChange(PROP_GENERATING, Boolean.TRUE, Boolean.FALSE);
                    }
                });
            }
        });
    }
    
    public boolean isGenerating(){
        return generating;
    }
    
    @Override
    protected String getEditorMimeType() {
        String serviceType = getConfiguration().getServiceType();
        if (Configuration.WSDLCLASS_TYPE.equals(serviceType)) {
            return MIME_TYPE_WSDL;
        }
        
        if (Configuration.JSR172_TYPE.equals(serviceType)) {
            return MIME_TYPE_JSR172;
        }

        return MIME_TYPE_CLASS;
    }

    @MultiViewElement.Registration(
        mimeType=MIME_TYPE_CLASS,
        iconBase = ICON_BASE,
        persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID = "e2e.source", // NOI18N
        displayName="#CTL_SourceTabCaption", // NOI18N
        position=1
    )
    public static XmlMultiViewElement createClassSourceViewElement(Lookup lookup) {
        return new XmlMultiViewElement(lookup.lookup(XmlMultiViewDataObject.class));
    }

    @MultiViewElement.Registration(
        mimeType=MIME_TYPE_WSDL,
        iconBase = ICON_BASE,
        persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID = "e2e.source", // NOI18N
        displayName="#CTL_SourceTabCaption", // NOI18N
        position=1
    )
    public static XmlMultiViewElement createWsdlSourceViewElement(Lookup lookup) {
        return new XmlMultiViewElement(lookup.lookup(XmlMultiViewDataObject.class));
    }

    @MultiViewElement.Registration(
        mimeType=MIME_TYPE_JSR172,
        iconBase = ICON_BASE,
        persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID = "e2e.source", // NOI18N
        displayName="#CTL_SourceTabCaption", // NOI18N
        position=1
    )
    public static XmlMultiViewElement createJsr172SourceViewElement(Lookup lookup) {
        return new XmlMultiViewElement(lookup.lookup(XmlMultiViewDataObject.class));
    }

    public interface SaveCallback {
        public void save();
    }
    
    @Override
    protected synchronized XmlMultiViewEditorSupport getEditorSupport() {
        if(xmlEditorSupport == null) {
            xmlEditorSupport = new ValidatedXmlMultiViewEditorSupport(this);
        }
        return xmlEditorSupport;
    }
    
    private static class ValidatedXmlMultiViewEditorSupport extends XmlMultiViewEditorSupport {

        final protected E2EDataObject dataObject;

        public ValidatedXmlMultiViewEditorSupport(XmlMultiViewDataObject dObj) {
            super(dObj);
//            setSuppressXmlView(true);
            this.dataObject = (E2EDataObject) dObj;
        }

        @Override
        public void open() {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    final Configuration configuration = dataObject.getConfiguration();
                    if (configuration == null) {
                        final NotifyDescriptor.Message dd = new NotifyDescriptor.Message(
                                NbBundle.getMessage(E2EDataObject.class, "ERR_ConfigurationFileCorrupted")); // NOI18N
                        DialogDisplayer.getDefault().notify(dd);
                        return;
                    }
                    //resolve broken reference here if this can be broken --> depends on server project
                    if (dataObject.getConfiguration().getServerConfigutation() != null && dataObject.getServerProject() == null) {
                        final NotifyDescriptor.Message dd =
                                new NotifyDescriptor.Message(
                                NbBundle.getMessage(E2EDataObject.class, "ERR_ServerProjectNotOpened", // NOI18N
                                configuration.getServerConfigutation().getProjectName()));
                        DialogDisplayer.getDefault().notify(dd);
                        if (Util.openProject(dataObject.getConfiguration().getServerConfigutation().getProjectPath()) == null) {
                            return;
                        }
                        return;
                    }
                    openme();
                }
            });
        }
        
        protected void openme() {
            super.open();
        }
    }
    
    private class ModelSynchronizer extends XmlMultiViewDataSynchronizer {

        public ModelSynchronizer(XmlMultiViewDataObject dataObject) {
            super(dataObject, 100);
        }

        @Override
        protected boolean mayUpdateData(final boolean allowDialog) {
            return true;
        }

        @Override
        protected void updateDataFromModel(final Object model, final FileLock lock, final boolean modify) {
            if (model == null) {
                return;
            }

            for (final SaveCallback callBack : saveCallbacks) {
                callBack.save();
            }

            try {
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                if (configuration != null) {
                    try {
                        ConfigurationWriter.write(out, (Configuration) model);
                    } catch (Exception e) {
                        ErrorManager.getDefault().notify(e);
                    }
                }
                out.close();
                final OutputStream outputStream = getDataCache().createOutputStream(lock, modify);
                try {
                    outputStream.write(out.toByteArray());
                } finally {
                    outputStream.close();
                }
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }

        @Override
        protected Object getModel() {
            return getConfiguration();
        }

        @Override
        protected void reloadModelFromData() {
            getConfiguration();
        }
    }
}
