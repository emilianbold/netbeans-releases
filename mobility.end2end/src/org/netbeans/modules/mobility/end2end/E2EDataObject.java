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
import org.netbeans.api.java.source.SourceUtils;
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
import org.netbeans.modules.mobility.end2end.classdata.MethodData;
import org.netbeans.modules.mobility.end2end.classdata.OperationData;
import org.netbeans.modules.mobility.end2end.classdata.WSDLService;
import org.netbeans.modules.mobility.end2end.client.config.ClientConfiguration;
import org.netbeans.modules.mobility.end2end.client.config.Configuration;
import org.netbeans.modules.mobility.end2end.client.config.ConfigurationReader;
import org.netbeans.modules.mobility.end2end.client.config.ConfigurationWriter;
import org.netbeans.modules.mobility.end2end.client.config.ServerConfiguration;
import org.netbeans.modules.mobility.end2end.util.Util;
import org.netbeans.modules.xml.multiview.DesignMultiViewDesc;
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


/**
 *
 * @author Michal Skvor
 */
public class E2EDataObject extends XmlMultiViewDataObject {

    public static RequestProcessor rp; //generator
      
    // Configuration
    transient protected Configuration configuration;
    // Project the file is in
    transient private Project clientProject;
    transient private Project serverProject;
        
    transient protected Set<SaveCallback> saveCallbacks;
    
    transient protected boolean generating;
    
    public static final String PROP_GENERATING = "generating"; //NOI18N
    
    final private static String TRUE = "true";
    
    private ModelSynchronizer synchronizer;
    
    /** Creates a new instance of E2EDataObject */
    public E2EDataObject( FileObject file, MultiFileLoader ldr )
    throws DataObjectExistsException {
        super( file, ldr );
        
        synchronizer = new ModelSynchronizer( this );
        
        saveCallbacks = new HashSet<SaveCallback>();
    }
    
    /**
     * Adds callback to callback list
     *
     * @param callBack callback to be called before the file is written to the disc
     */
    public void addSaveCallback( final SaveCallback callBack ) {
        saveCallbacks.add( callBack );
    }
    
    /**
     * Removes callback from the callback list
     *
     * @param callBack callback to be called before the file is written to the disc
     */
    public void removeSaveCallback( final SaveCallback callBack ) {
        saveCallbacks.remove( callBack );
    }
    
    protected Node createNodeDelegate() {
        Node node;
        
        node = new E2EDataNode( this );
        return node;
    }
    
    public void setModified( final boolean modif ) {
        super.setModified( modif );
        if( modif ) {
            synchronizer.requestUpdateData();
        }
    }
    
    public Configuration getConfiguration() {
        if( configuration == null ) {
            try {
                configuration = ConfigurationReader.read( this );
            } catch( Exception e ) {
                configuration = null;
                ErrorManager.getDefault().log( e.getMessage());
            }
        }
        return configuration;
    }
    
    public void setConfiguration(final Configuration configuration) {
        this.configuration = configuration;
        setModified(true);
    }
    
    public Project getClientProject() {
        if (clientProject == null)  clientProject = FileOwnerQuery.getOwner(getPrimaryFile());
        return clientProject;
    }
    
    public Project getServerProject() {
        final Configuration config = getConfiguration();
        if( serverProject == null ) {
            final OpenProjects openProject = OpenProjects.getDefault();
            final Project[] openedProjects = openProject.getOpenProjects();
            if (config.getServerConfigutation() == null) {
                return null;
            }
            final String serverProjectName = config.getServerConfigutation().getProjectName();
            for ( final Project p : openedProjects ) {//
                final ProjectInformation pi = p.getLookup().lookup( ProjectInformation.class );
                final String webProjectName = pi.getName();
                if( serverProjectName.equals( webProjectName )) {
                    serverProject = p;
                    break;
                }
            }
            
            if( serverProject == null ) {
                // TODO: add some dialog to inform user that his Web project is not there ...
                //System.err.println( "Cannot find server node" );
            }
        }
        return serverProject;
    }
    
    // FIXME: this method should be rather in GenerateAction
    public JavonMappingImpl getMapping() throws Exception {
//        //System.err.println(" - GET MAPPING START - ");
//        // run always
        final Configuration config = getConfiguration();
        final List<String> classPath = new ArrayList<String>();
//        
        final ServerConfiguration sc = config.getServerConfigutation();
        final Properties sprops = sc.getProperties();
        final Sources ssources = getServerProject().getLookup().lookup( Sources.class );
        final SourceGroup[] ssg = ssources.getSourceGroups( JavaProjectConstants.SOURCES_TYPE_JAVA );
        List<ClasspathInfo> classpaths = new ArrayList<ClasspathInfo>();
        for( int i = 0; i < ssg.length; i++ ) {
            final ClassPath cp = ClassPath.getClassPath( ssg[i].getRootFolder(), ClassPath.SOURCE );
            final FileObject[] roots = cp.getRoots();
            for( int j = 0; j < roots.length; j++ ) {
                File f;
                if(( f = FileUtil.toFile( roots[j] )) != null )
                    classPath.add( f.getAbsolutePath());
                classpaths.add( ClasspathInfo.create( roots[j] ));
            }
        }
                
//        
//        for (int i = 0; i < ssg.length; i++) {
//            final ClassPath cp = ClassPath.getClassPath(ssg[i].getRootFolder() ,ClassPath.EXECUTE);
//            final FileObject[] roots = cp.getRoots(); //only returns folders. How to handle jars?
//            for (int j = 0; j < roots.length; j++) {
//                File f;
//                if ((f = FileUtil.toFile(roots[j])) != null)
//                    classPath.add(f.getAbsolutePath());
//            }
//        }
//      
        //TODO: Dirty hack
        List<ClasspathInfo> classpathInfos = new ArrayList<ClasspathInfo>();
        classpathInfos.add( ClasspathInfo.create( ssg[0].getRootFolder()));   // TODO: fix this!!!
        if( config.getServiceType().equals( Configuration.WSDLCLASS_TYPE )) {
            FileObject projectFO = getServerProject().getProjectDirectory().getFileObject( "build/generated/wsimport/client" );
            if( projectFO != null ) {
                classpathInfos.add( ClasspathInfo.create( projectFO ));
            }
        }
        // TODO: fix 
        System.err.println(" - classpathInfos: " + classpathInfos.size());
        final ClassDataRegistry registry = ClassDataRegistry.getRegistry( "default", classpathInfos );  
//        final ClassDataRegistry registry =
//                getClassDataRegistryFactory().create( classPath );
//        mapping = new MutableJavonMapping( registry );
//        
//        // FIXME: devel hack
//        final MutableJavonMapping m = new MutableJavonMapping( mapping );
        // Create new mapping
        JavonMappingImpl mapping = new JavonMappingImpl( registry );
        
        // Client part of the mapping
        final ClientConfiguration cc = config.getClientConfiguration();
        final JavonMappingImpl.Client jcc = new JavonMappingImpl.Client();
        final Properties cprops = cc.getProperties();
        jcc.setClassName( cc.getClassDescriptor().getLeafClassName());
        jcc.setPackageName( cc.getClassDescriptor().getPackageName());
        final Sources csources = getClientProject().getLookup().lookup( Sources.class );
        final SourceGroup csg = Util.getPreselectedGroup(
                csources.getSourceGroups( JavaProjectConstants.SOURCES_TYPE_JAVA ),
                cc.getClassDescriptor().getLocation());
//        //System.err.println(" - client path: " + FileUtil.getFileDisplayName( csg.getRootFolder()));
//        m.setClientRootDirectory( FileUtil.toFile( csg.getRootFolder()).getPath());
        jcc.setOutputDirectory( FileUtil.toFile( csg.getRootFolder()).getPath());
//        if( TRUE.equals(cprops.getProperty( "trace" ))) {   // NOI18N
//            m.setClientTraceLevel( 1 );
//        } else {
//            m.setClientTraceLevel( 0 );
//        }
//        m.setDynamicInvocationSupported( false );
//        m.setGroupingSupported( cprops.getProperty( "multipleCall" ).equals( TRUE )); // NOI18N
        mapping.setProperty( "create-stubs", cprops.getProperty( "createStubs" ).equals( TRUE ));     // NOI18N
        mapping.setProperty( "databinding", cprops.getProperty( "DataBinding" ));
//        m.setSynchronousSupported( true );
//        m.setFloatingPointSupported( true );
        mapping.setClientMapping( jcc );
        
        /* Server part of the mapping */
        final ProjectInformation pi = getServerProject().getLookup().lookup( ProjectInformation.class );
        final JavonMappingImpl.Server jsc = new JavonMappingImpl.Server();
        jsc.setProjectName( pi.getName());
        jsc.setClassName( sc.getClassDescriptor().getLeafClassName());
        jsc.setPackageName( sc.getClassDescriptor().getPackageName());
//        //System.err.println(" - server path: " + FileUtil.getFileDisplayName( ssg.getRootFolder()));
        jsc.setOutputDirectory( 
                FileUtil.toFile( Util.getPreselectedGroup(ssg, sc.getClassDescriptor().getLocation()).getRootFolder()).getPath());
//        m.setServerRootDirectory(
//                FileUtil.toFile( Util.getPreselectedGroup(ssg, sc.getClassDescriptor().getLocation()).getRootFolder()).getPath());
//        if( TRUE.equals(sprops.getProperty( "trace" ))) {   // NOI18N
//            m.setClientTraceLevel( 1 );
//        } else {
//            m.setClientTraceLevel( 0 );
//        }
        ;
        jsc.setLocation( Util.getServerLocation( getServerProject()));
        jsc.setPort( Util.getServerPort( getServerProject()) );
        jsc.setServletLocation( configuration.getServerConfigutation().getProjectName() + "/servlet/" + //NOI18N
                configuration.getServerConfigutation().getClassDescriptor().getType());
        mapping.setServerMapping( jsc );
//        
//        final JavonMapping.Service jss = mapping.new Service();
        int methodID = 1;
        final List<AbstractService> services = config.getServices();
//        // there must be one and just one service
        
        final List<ClassData> classes = services.get(0).getData();        
        for( final ClassData ccd : classes ) {
            JavonMappingImpl.Service javonService = new JavonMappingImpl.Service();
            javonService.setPackageName( ccd.getPackageName());
            if( Configuration.WSDLCLASS_TYPE.equals( config.getServiceType())) {
                WSDLService wsdlService = (WSDLService)services.get(0);
                javonService.setClassName( wsdlService.getType());
            } else {
                javonService.setClassName( ccd.getClassName());
            }
            
            String className = ccd.getProxyClassType();
            if( className == null ){
                className = ccd.getType();
            }
            mapping.setProperty( "instance", className );
            
//            while( SourceUtils.isScanInProgress()) {}
            Thread.sleep( 5000 );
            
            registry.updateClassDataTree();
            final org.netbeans.modules.mobility.e2e.classdata.ClassData classData = registry.getClassData( className );
            System.err.println(" - classdata = " + classData);
            if( classData == null ) continue;
            
            final List<OperationData> methods = ccd.getOperations();
            final List<org.netbeans.modules.mobility.e2e.classdata.MethodData> methodsData = classData.getMethods();
            for( int j = 0; j < methods.size(); j++ ) {
                System.err.println(" - method: " + methods.get( j ).getMethodName());
                String methodName = null;
                if( Configuration.WSDLCLASS_TYPE.equals( config.getServiceType())) {
                    methodName = methods.get( j ).getMethodName();
                } else {
                    methodName = methods.get( j ).getName();
                }
                final int methodIndex = findMethodIndex( methodsData, methodName );
                if( methodIndex >= 0 ) {
                    org.netbeans.modules.mobility.e2e.classdata.MethodData mmd = methodsData.get(methodIndex);
                    mmd.setRequestID( methodID++ );
                    javonService.addMethod( mmd );
                }
            }
            mapping.addServiceMaping( javonService );
        }
        mapping.setServletURL( Util.getServerURL( getServerProject(), getConfiguration()));
        
        System.err.println(" - mapping :" + mapping.getServiceMappings().toString());
        return mapping;
    }
    
    private int findMethodIndex( final List<org.netbeans.modules.mobility.e2e.classdata.MethodData> methods, final String methodName ) {
        int result = 0;
        for( org.netbeans.modules.mobility.e2e.classdata.MethodData method : methods ) {
//        for( int i = 0; i < methods.size(); i++ ) {
            // FIXME: check parameters and return types
            if( method.getName().equals( methodName )) {
                return result;
            }
            result++;
        }
        
        return -1;
    }
        
    protected String getPrefixMark() {
        // FIXME: What the heck is this method for?
        return "";
    }
    
    protected boolean parseDocument(@SuppressWarnings("unused")
	final boolean updateModel) {
        // FIXME: devel hack
        return true;
    }
    
    protected boolean isModelCreated() {
        // FIXME: devel hack
        return false;
    }
    
    protected String generateDocumentFromModel() {
        return "";
    }
    
    public synchronized void generate(){
        generating = true;
        firePropertyChange( PROP_GENERATING, Boolean.FALSE, Boolean.TRUE );
        SwingUtilities.invokeLater( new Runnable() {
            @SuppressWarnings("synthetic-access")
                public void run() {
                    if( rp == null ) {
                        rp = new RequestProcessor( "End2EndGenerator", 5 ); //NOI18N
                    }
                    rp.post( new Runnable() {
                        @SuppressWarnings("synthetic-access")
                        public void run() {
                        final Lookup.Result<E2EServiceProvider> result = Lookup.getDefault().lookup(new Lookup.Template<E2EServiceProvider>(E2EServiceProvider.class));
                        for( final E2EServiceProvider elem : result.allInstances() ) {
                            if( E2EDataObject.this.getConfiguration().getServiceType().equals( elem.getServiceType())) {
                                final ServiceGeneratorResult service = elem.generateStubs( Lookups.singleton( E2EDataObject.this ));
//                                        if (generateMidlet && service != null && service.getAccessMethods().length != 0 && Util.isSuitableProjectConfiguration( getClientProject())){
//                                            final VisualDesignGenerator designGenerator = new VisualDesignGenerator(service);
//                                            DataObject generated;
//                                            if ((generated = designGenerator.generateDesign(getFolder())) == null){
//                                                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage( E2EDataObject.class, "MSG_SampleNotGenerated" )); // NOI18N
//                                            } else {
//                                                final Project p = getClientProject();
//                                                final AntProjectHelper h = p.getLookup().lookup(AntProjectHelper.class);
//                                                if (p instanceof J2MEProject  &&  h != null) {
//                                                    // set the server url as a custom prperty in jad
//                                                    setServerUrlInJad((J2MEProject) p, service.getDeploymentUrl());
//                                                    JavaModel.getJavaRepository().beginTrans(false);
//                                                    try {
//                                                        final Resource resource = JavaModel.getResource(generated.getPrimaryFile());
//                                                        final JavaClass jc = (JavaClass) resource.getClassifiers().get(0);
//                                                        
//                                                        J2MEProjectGenerator.addMIDletProperty(p, h,
//                                                                generated.getName(),
//                                                                jc.getName(),
//                                                                ""
//                                                                );
//                                                        ProjectManager.getDefault().saveProject(p);
//                                                    } catch (IOException e) {
//                                                        ErrorManager.getDefault().notify(e);
//                                                    } finally {
//                                                        JavaModel.getJavaRepository().endTrans();
//                                                    }
//                                                }
//                                            }
//                                        }
//                                        generating = false;
//                                        E2EDataObject.this.firePropertyChange(PROP_GENERATING, Boolean.TRUE, Boolean.FALSE);
//                                        return;
                            }
                        }
                    } 
                } );
                generating = false;
                E2EDataObject.this.firePropertyChange(PROP_GENERATING, Boolean.TRUE, Boolean.FALSE);
//                            }
//                            
//                            private void setServerUrlInJad( J2MEProject project, String url ) {
//                                final AntProjectHelper helper = project.getLookup().lookup( AntProjectHelper.class );
//                                final EditableProperties props = helper.getProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH );
//                                final HashMap<String,String> jad = (HashMap<String,String>)DefaultPropertyParsers.MANIFEST_PROPERTY_PARSER.decode(props.getProperty(DefaultPropertiesDescriptor.MANIFEST_JAD), null, null);
//                                jad.put( "serverURL", url );
//                                props.setProperty( DefaultPropertiesDescriptor.MANIFEST_JAD, DefaultPropertyParsers.MANIFEST_PROPERTY_PARSER.encode(jad, null, null));
//                                helper.putProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH, props );
//                            }
//                        });
//                    }
//                }, NbBundle.getMessage(E2EDataObject.class, "MSG_GeneratingStubs"))){ //NOI18N
//                    generating = false;
//                    E2EDataObject.this.firePropertyChange(PROP_GENERATING, Boolean.TRUE, Boolean.FALSE);
//                }
            }
        });
    }
    
    public boolean isGenerating(){
        return generating;
    }
    
    protected DesignMultiViewDesc[] getMultiViewDesc() {
        final Lookup.Result<E2EServiceProvider> result = Lookup.getDefault().lookup(new Lookup.Template<E2EServiceProvider>(E2EServiceProvider.class));
        for ( final E2EServiceProvider elem : result.allInstances() ) {
            if (getConfiguration().getServiceType().equals(elem.getServiceType())){
                return elem.getMultiViewDesc(Lookups.singleton(this));
            }
        }
        return null;
    }
    
    public interface SaveCallback {
        public void save();
    }
    
    private XmlMultiViewEditorSupport editorSupport;
    protected synchronized XmlMultiViewEditorSupport getEditorSupport() {
        if(editorSupport == null) {
            editorSupport = new ValidatedXmlMultiViewEditorSupport(this);
        }
        return editorSupport;
    }
    
    private static class ValidatedXmlMultiViewEditorSupport extends XmlMultiViewEditorSupport {
        
        final protected E2EDataObject dataObject;
        
        public ValidatedXmlMultiViewEditorSupport(XmlMultiViewDataObject dObj) {
            super(dObj);
//            setSuppressXmlView(true);
            this.dataObject = (E2EDataObject)dObj;
        }
        
        @Override
        public void open() {
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
//                    JavaMetamodel.getManager().invokeAfterScanFinished( new Runnable() {
//                        public void run() {
                            final Configuration configuration = dataObject.getConfiguration();
                            if( configuration == null ) {
                                final NotifyDescriptor.Message dd  = new NotifyDescriptor.Message(
                                        NbBundle.getMessage( E2EDataObject.class, "ERR_ConfigurationFileCorrupted" ));
                                DialogDisplayer.getDefault().notify(dd);
                                return;
                            }
                            //resolve broken reference here if this can be broken --> depends on server project
                            if ( dataObject.getConfiguration().getServerConfigutation() != null && dataObject.getServerProject() == null ){
                                final NotifyDescriptor.Message dd  =
                                        new NotifyDescriptor.Message(
                                        NbBundle.getMessage( E2EDataObject.class, "ERR_ServerProjectNotOpened",
                                        configuration.getServerConfigutation().getProjectName()));
                                DialogDisplayer.getDefault().notify(dd);
                                if (Util.openProject(dataObject.getConfiguration().getServerConfigutation().getProjectPath()) == null){
                                    return;
                                }
                                return;
                            }
                            openme();
//                        }
//                    }, NbBundle.getMessage(E2EDataObject.class, "MSG_OpeningConfigFile" ));
                }});
        }
        
        protected void openme() {
            super.open();
        }
    }
    
    private class ModelSynchronizer extends XmlMultiViewDataSynchronizer {
        
        public ModelSynchronizer(XmlMultiViewDataObject dataObject) {
            super(dataObject, 100);
        }
        
        protected boolean mayUpdateData(@SuppressWarnings("unused")
		final boolean allowDialog) {
            return true;
        }
        
        protected void updateDataFromModel(final Object model, final FileLock lock, final boolean modify) {
            if (model == null) {
                return;
            }
            
            for ( final SaveCallback callBack :saveCallbacks ) {
                callBack.save();
            }
            
            try {
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                if( configuration != null ) {
                    try {
                        ConfigurationWriter.write( out , (Configuration)model );
                    } catch( Exception e ) {
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
        
        protected Object getModel() {
            return getConfiguration();
        }
        
        protected void reloadModelFromData() {
            getConfiguration();
        }
    }
}
