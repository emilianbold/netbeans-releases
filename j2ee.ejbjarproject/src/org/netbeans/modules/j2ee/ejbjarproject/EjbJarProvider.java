/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbjarproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.*;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.*;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.common.api.EjbChangeDescriptor;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.j2ee.deployment.devmodules.api.*;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.EjbJarProjectProperties;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarImplementation;
import org.openide.filesystems.FileUtil;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.modules.websvc.spi.webservices.WebServicesSupportImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.netbeans.modules.j2ee.dd.api.webservices.ServiceImplBean;
import org.netbeans.modules.j2ee.ejbjarproject.ejb.wizard.session.SessionGenerator;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.w3c.dom.Node;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.spi.project.support.ant.PropertyUtils;

/** A ejb module implementation on top of project.
 *
 * @author  Pavel Buzek
 */
public final class EjbJarProvider extends J2eeModuleProvider implements EjbJarImplementation, J2eeModule, ModuleChangeReporter, EjbChangeDescriptor, PropertyChangeListener, WebServicesSupportImpl {
      
    public static final String FILE_DD        = "ejb-jar.xml";//NOI18N
    public static final String WEBSERVICES_DD = "webservices";//NOI18N
    
    private EjbJarProject project;
    private AntProjectHelper helper;
    private Set versionListeners = null;
    
    EjbJarProvider(EjbJarProject project, AntProjectHelper helper) {
        this.project = project;
        this.helper = helper;
    }
    
    public FileObject getDeploymentDescriptor() {
        FileObject metaInfFo = getMetaInf();
        if (metaInfFo==null) {
            DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message(NbBundle.getMessage(EjbJarProject.class,"MSG_WebInfCorrupted"),
                                             NotifyDescriptor.ERROR_MESSAGE));
            return null;
        }
        return metaInfFo.getFileObject (FILE_DD);
    }

    public ClassPath getJavaSources () {
        ClassPathProvider cpp = (ClassPathProvider) project.getLookup ().lookup (ClassPathProvider.class);
        if (cpp != null) {
            return cpp.findClassPath (getFileObject (EjbJarProjectProperties.SRC_DIR), ClassPath.SOURCE);
        }
        return null;
    }
    
    public FileObject getMetaInf () {
        return getFileObject (EjbJarProjectProperties.META_INF);
    }
    
    public FileObject findDeploymentConfigurationFile(String name) {
        return getMetaInf().getFileObject(name);
    }
    
    public File getDeploymentConfigurationFile(String name) {
        FileObject moduleFolder = getMetaInf();
        File configFolder = FileUtil.toFile(moduleFolder);
        return new File(configFolder, name);
    }
    
    public ClassPathProvider getClassPathProvider () {
        return (ClassPathProvider) project.getLookup ().lookup (ClassPathProvider.class);
    }
    
    public FileObject getArchive () {
        return getFileObject (EjbJarProjectProperties.DIST_JAR); //NOI18N
    }
    
    private FileObject getFileObject(String propname) {
        String prop = helper.getStandardPropertyEvaluator().getProperty(propname);
        if (prop != null) {
            return helper.resolveFileObject(prop);
        } 
        
        return null;
    }
    
    private File getFile(String propname) {
        String prop = helper.getStandardPropertyEvaluator().getProperty(propname);
        if (prop != null) {
            return helper.resolveFile(prop);
        } 
        return null;
    }
    
    public org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule getJ2eeModule () {
        return this;
    }
    
    public org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter getModuleChangeReporter () {
        return this;
    }
    
    public boolean useDefaultServer () {
        return true;
    }
    
    public String getServerID () {
        return helper.getStandardPropertyEvaluator ().getProperty (EjbJarProjectProperties.J2EE_SERVER_TYPE);
    }

    public String getServerInstanceID () {
        return helper.getStandardPropertyEvaluator ().getProperty (EjbJarProjectProperties.J2EE_SERVER_INSTANCE);
    }
        
    public Iterator getArchiveContents () throws java.io.IOException {
        return new IT (getContentDirectory ());
    }

    public FileObject getContentDirectory() {
        return getFileObject (EjbJarProjectProperties.BUILD_CLASSES_DIR); //NOI18N
    }

    public FileObject getBuildDirectory() {
        return getFileObject (EjbJarProjectProperties.BUILD_DIR); //NOI18N
    }

    public File getContentDirectoryAsFile() {
        return getFile (EjbJarProjectProperties.BUILD_CLASSES_DIR); //NOI18N
    }

    public org.netbeans.modules.schema2beans.BaseBean getDeploymentDescriptor (String location) {
        if (! J2eeModule.EJBJAR_XML.equals(location))
            return null;
        
        EjbJar webApp = getEjbJar();
        if (webApp != null) {
            //PENDING find a better way to get the BB from WApp and remove the HACK from DDProvider!!
            return DDProvider.getDefault ().getBaseBean (webApp);
        }
        return null;
    }

    private EjbJar getEjbJar () {
        try {
            return DDProvider.getDefault ().getDDRoot (getDeploymentDescriptor ());
        } catch (java.io.IOException e) {
            org.openide.ErrorManager.getDefault ().log (e.getLocalizedMessage ());
        }
        return null;
    }
    
    public org.netbeans.modules.j2ee.deployment.common.api.EjbChangeDescriptor getEjbChanges (long timestamp) {
        return this;
    }

    public Object getModuleType () {
        return J2eeModule.EJB;
    }

    public String getModuleVersion () {
        EjbJar ejbJar = getEjbJar ();
        return ejbJar.getVersion ().toString();
    }

    private Set versionListeners() {
        if (versionListeners == null) {
            versionListeners = new HashSet();
            org.netbeans.modules.j2ee.dd.api.ejb.EjbJar ejbJar = getEjbJar();
            if (ejbJar != null) {
                PropertyChangeListener l = (PropertyChangeListener) org.openide.util.WeakListeners.create(PropertyChangeListener.class, this, ejbJar);
                ejbJar.addPropertyChangeListener(l);
            }
        }
        return versionListeners;
    }

    public void addVersionListener(J2eeModule.VersionListener vl) {
        versionListeners().add(vl);
    }

    public void removeVersionListener(J2eeModule.VersionListener vl) {
        if (versionListeners != null)
            versionListeners.remove(vl);
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(org.netbeans.modules.j2ee.dd.api.ejb.EjbJar.PROPERTY_VERSION)) {
            for (Iterator i=versionListeners.iterator(); i.hasNext();) {
                J2eeModule.VersionListener vl = (J2eeModule.VersionListener) i.next();
                String oldVersion = (String) evt.getOldValue();
                String newVersion = (String) evt.getNewValue();
                vl.versionChanged(oldVersion, newVersion);
            }
        }
    }
        
    public String getUrl () {
        EditableProperties ep =  helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String name = ep.getProperty(EjbJarProjectProperties.JAR_NAME);
        return name == null ? "" : ("/"+name); //NOI18N
    }

    public boolean isManifestChanged (long timestamp) {
        return false;
    }

    public void setUrl (String url) {
        throw new UnsupportedOperationException ("Cannot customize URL of web module");
    }

    public boolean ejbsChanged () {
        return false;
    }

    public String[] getChangedEjbs () {
        return new String[] {};
    }

    public String getJ2eePlatformVersion () {
        return helper.getStandardPropertyEvaluator ().getProperty (EjbJarProjectProperties.J2EE_PLATFORM);
    }
    
    private static class IT implements Iterator {
        java.util.Enumeration ch;
        FileObject root;
        
        private IT (FileObject f) {
            this.ch = f.getChildren (true);
            this.root = f;
        }
        
        public boolean hasNext () {
            return ch.hasMoreElements ();
        }
        
        public Object next () {
            FileObject f = (FileObject) ch.nextElement ();
            return new FSRootRE (root, f);
        }
        
        public void remove () {
            throw new UnsupportedOperationException ();
        }
        
    }

    private static final class FSRootRE implements J2eeModule.RootedEntry {
        FileObject f;
        FileObject root;
        
        FSRootRE (FileObject root, FileObject f) {
            this.f = f;
            this.root = root;
        }
        
        public FileObject getFileObject () {
            return f;
        }
        
        public String getRelativePath () {
            return FileUtil.getRelativePath (root, f);
        }
    }
    
    //implementation of WebServicesSupportImpl
    //FIX-ME: move these constants to a central place
    public static final String WEB_SERVICES =     "web-services";//NOI18N
    public static final String WEB_SERVICE  =     "web-service";//NOI18N
    public static final String WEB_SERVICE_NAME = "web-service-name";//NOI18N
    public static final String CONFIG_PROP_SUFFIX = ".config.name";//NOI18N
    public static final String MAPPING_PROP_SUFFIX = ".mapping";//NOI18N
    public static final String MAPPING_FILE_SUFFIX = "-mapping.xml";//NOI18N

    private static final String WSCOMPILE_CLASSPATH="wscompile.classpath"; //NOI18N
    private static final String WSCOMPILE_TOOLS_CLASSPATH="wscompile.tools.classpath"; //NOI18N
    private static final String WEBSVC_GENERATED_DIR="websvc.generated.dir"; // NOI18N
    private static final String [] WSCOMPILE_JARS = {
		"${libs.j2ee14.classpath}",
		"${libs.jaxrpc11.classpath}",
		"${libs.saaj12.classpath}",
		"${wscompile.tools.classpath}"
    };
    public String generateImplementationBean(String wsName, FileObject pkg, Project project)throws java.io.IOException
    {
	SessionGenerator sessionGenerator = new SessionGenerator();
	return sessionGenerator.generateWebServiceImplBean(wsName, pkg, project);
    }
	
    public void addServiceImpl(String serviceName, String serviceEndpointInterface, String servantClassName, FileObject configFile) {

         //Add properties to project.properties file
         //FIX-ME: Move this to websvc
         EditableProperties ep =  helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
         String packageName = getPackageName(configFile);
         ep.put(serviceName + CONFIG_PROP_SUFFIX, packageName +
               (packageName.equals("") ? "" : "/") + configFile.getNameExt()); //NOI18N
         ep.put(serviceName + MAPPING_PROP_SUFFIX, serviceName + MAPPING_FILE_SUFFIX); //NOI18N
         helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);

         //Add web-services information in project.xml
         Element data = helper.getPrimaryConfigurationData(true);
         Document doc = data.getOwnerDocument();
         NodeList nodes = data.getElementsByTagName(WEB_SERVICES); //NOI18N
         Element webservices = null;
         if(nodes.getLength() == 0){
            webservices = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, WEB_SERVICES); //NOI18N
            data.appendChild(webservices);
         }
         else{
             webservices = (Element)nodes.item(0);
         }
         Element webservice = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, WEB_SERVICE); //NOI18N
         webservices.appendChild(webservice);
         Element webserviceName = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, WEB_SERVICE_NAME); //NOI18N
         webservice.appendChild(webserviceName);
         webserviceName.appendChild(doc.createTextNode (serviceName));
         helper.putPrimaryConfigurationData(data, true);

	// Update wscompile related properties.  boolean return indicates whether
	// any changes were made.
	updateWsCompileProperties(serviceName);

         try
         {
              ProjectManager.getDefault().saveProject(project);
         }catch(java.io.IOException ioe){
             throw new RuntimeException(ioe.getMessage());
         }

         addServiceImplEntry(serviceName, serviceEndpointInterface, servantClassName);
    }

     private void addServiceImplEntry(String serviceName, String serviceEndpointInterface, String servantClassName)
     {
         //add service endpoint entry to ejb-jar.xml
	 DDProvider provider = DDProvider.getDefault();
	 EjbJarImplementation ejbJarImpl = (EjbJarImplementation)project.getLookup().lookup(EjbJarImplementation.class);
         org.netbeans.modules.j2ee.dd.api.ejb.EjbJar ejbJar = null;
         try
	 {
 	    ejbJar = provider.getDDRoot(ejbJarImpl.getDeploymentDescriptor());
         }
	 catch(java.io.IOException e)
	 {
            //FIX-ME: handle this
	    throw new RuntimeException(e.getMessage());
	 }
		 
         EjbJarProvider pwm = (EjbJarProvider) project.getLookup ().lookup (EjbJarProvider.class);
         pwm.getConfigSupport().ensureConfigurationReady();
         EnterpriseBeans beans = ejbJar.getEnterpriseBeans();
	 Session s = null;
	 if(beans == null)
	 {
	    beans = ejbJar.newEnterpriseBeans();
	    ejbJar.setEnterpriseBeans(beans);
	 }
	 s = beans.newSession();
	 s.setEjbName(serviceName);
         s.setDisplayName(serviceName + "SB");
         s.setEjbClass(servantClassName);
         try
	 {
	     s.setServiceEndpoint(serviceEndpointInterface);
         }
	 catch(org.netbeans.api.web.dd.common.VersionNotSupportedException e)
	 { 
             //FIX-ME: handle this 
	     throw new RuntimeException(e.getMessage());
	 }
         s.setSessionType("Stateless");
         s.setTransactionType("Container");
         beans.addSession(s);
         try
	 {
	     ejbJar.write(ejbJarImpl.getDeploymentDescriptor());
             //Hack to save any defaults put in vendor-specific DD
             //Need a better way to save selectively from server plugins(an api that allows
             //server plugins to save server configuration in selective manner)
              org.openide.LifecycleManager.getDefault().saveAll();
         }
	 catch(java.io.IOException e)
	 {
             //FIX-ME: handle this 
	     throw new RuntimeException(e.getMessage());
	 }
 
         //Hack to save any defaults put in vendor-specific DD
         //Need a better way to save selectively from server plugins(an api that allows
         //server plugins to save server configuration in selective manner)
         //FIX-ME: Do we still need this?: org.openide.LifecycleManager.getDefault().saveAll();
     }

     public void addServiceImplLinkEntry(ServiceImplBean serviceImplBean, String wsName)
     {
	 serviceImplBean.setEjbLink(wsName);
     } 

    /**
     * Get the webservices.xml file object
     * TO-DO: Misleading method name, change to something more
     * descriptive in interface, e.g., getWebserviceDD
     */
    public FileObject getDD() {
	FileObject metaInfFo = getMetaInf();
        if (metaInfFo==null) {
            DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message(NbBundle.getMessage(EjbJarProject.class,"MSG_WebInfCorrupted"),
                                             NotifyDescriptor.ERROR_MESSAGE));
            return null;
        }
        return getMetaInf ().getFileObject (WEBSERVICES_DD, "xml");
    }

    /**
     *  Returns the directory that contains webservices.xml in the project
     */
    public FileObject getWsDDFolder()
    {
       return getMetaInf();
    }

     /**
     * Returns the name of the directory that contains the webservices.xml in
     * the archive
     */
     public String getArchiveDDFolderName()
     {
	return "META-INF";
     }

     /**
     * Returns the name of the implementation bean class
     * given the ejb-link name
     */
     public String getImplementationBean(String linkName)
     {
        EjbJar ejbJar = getEjbJar ();
        EnterpriseBeans beans = ejbJar.getEnterpriseBeans();
        Session[] sessionBeans = beans.getSession();
	for(int i = 0; i < sessionBeans.length; i++)
	{
	   Session sessionBean = sessionBeans[i];
           if(sessionBean.getEjbName().equals(linkName))
	   {
	     return sessionBean.getEjbClass();
	   }
			
	}
		return null;
     }

     public void removeServiceEntry(String serviceName, String linkName)
     {
        //remove ejb  entry in ejb-jar.xml
         EjbJarImplementation ejbJarImpl = (EjbJarImplementation)project.getLookup().lookup(EjbJarImplementation.class); 
	 EjbJar ejbJar = getEjbJar();
	 EnterpriseBeans beans = ejbJar.getEnterpriseBeans();
	 Session[] sessionBeans = beans.getSession();
	 for(int i = 0; i < sessionBeans.length; i++)
	 {
	    Session sessionBean = sessionBeans[i];
            if(sessionBean.getEjbName().equals(linkName))
	    {
	       beans.removeSession(sessionBean);
               break;
	    }
	 }	 
         try
	 {
	    ejbJar.write(ejbJarImpl.getDeploymentDescriptor());
         }
	 catch(java.io.IOException e)
	 {
            NotifyDescriptor ndd = 
             new NotifyDescriptor.Message(NbBundle.getMessage(this.getClass(), "MSG_Unable_WRITE_EJB_DD"), 
              NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(ndd); 
         }

         boolean needsSave = false;

         //Remove entries in the project.properties file 
         //FIX-ME:we should move this to websvc 
         EditableProperties ep =  helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH); 
         String configProperty = serviceName + CONFIG_PROP_SUFFIX;
	 String mappingProperty = serviceName + MAPPING_PROP_SUFFIX;
         if(ep.getProperty(configProperty) != null)
	 {
	     ep.remove(configProperty);
             needsSave = true;
         }
	 if(ep.getProperty(mappingProperty) != null)
	 {
	     ep.remove(mappingProperty);
             needsSave = true;
         }

         if(needsSave){
           helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
         }
         //Remove entry in the project.xml file (we should move this to websvc)
         Element data = helper.getPrimaryConfigurationData(true);
         Document doc = data.getOwnerDocument();
         NodeList nodes = data.getElementsByTagNameNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,
                                                    WEB_SERVICES); //NOI18N
         Element webservices = null; 
         Element wsNameNode = null;
         if(nodes.getLength() == 1){
            webservices = (Element)nodes.item(0);
            NodeList wsNodes = webservices.getElementsByTagNameNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,
                                                      WEB_SERVICE); //NOI18N
            for(int j = 0; j < wsNodes.getLength(); j++)
	    {
	       Element wsNode = (Element)wsNodes.item(j);
               NodeList wsNameNodes = wsNode.getElementsByTagNameNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,
                                                     WEB_SERVICE_NAME); //NOI18N
               if(wsNameNodes.getLength() == 1)
	       {
                  wsNameNode = (Element)wsNameNodes.item(0);
                  NodeList nl = wsNameNode.getChildNodes();
                  if(nl.getLength() == 1)
		  {
		     Node n = nl.item(0);
	             if(n.getNodeType() == Node.TEXT_NODE)
		     {
			 if(serviceName.equals(n.getNodeValue()))
			 {
		            webservices.removeChild(wsNode);
                            //if there are no more children, remove the web-services node
                            NodeList children = webservices.getChildNodes();
			    if(children.getLength() == 0)
			    {
			        data.removeChild(webservices);
                            }
                            needsSave = true;
                            break;
			 }
		     }
		  }
		}
	    }
         }
         if(needsSave) {
            helper.putPrimaryConfigurationData(data, true);
	    try {
	 	   ProjectManager.getDefault().saveProject(project);
	    } catch(java.io.IOException ex) {
		String mes = NbBundle.getMessage(this.getClass(), "MSG_ErrorSavingOnWSRemove") + serviceName 
                                   + "'\r\n" + ex.getMessage();
		NotifyDescriptor desc = new NotifyDescriptor.
                Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
	        DialogDisplayer.getDefault().notify(desc);			}
	    }
       }

   public AntProjectHelper getAntProjectHelper()
   {
      return helper;
   }

  private boolean updateWsCompileProperties(String serviceName) {
  /** Ensure wscompile.classpath and wscompile.tools.classpath are
   *  properly defined.
   *
   *  wscompile.classpath goes in project properties and includes
   *  jaxrpc and qname right now.
   *
   *  wscompile.tools.classpath is for tools.jar which is needed when
   *  running under the Sun JDK to invoke javac.  It is placed in
   *  user.properties so that if we compute it incorrectly (say on a mac)
   *  the user can change it and we will not blow away the change.
   *  Hopefully we can do this better for release.
  */
  boolean globalPropertiesChanged = false;

  EditableProperties globalProperties = PropertyUtils.getGlobalProperties();
  if(globalProperties.getProperty(WSCOMPILE_TOOLS_CLASSPATH) == null) {
	globalProperties.setProperty(WSCOMPILE_TOOLS_CLASSPATH, "${java.home}\\..\\lib\\tools.jar");

	try {
		PropertyUtils.putGlobalProperties(globalProperties);
	} catch(java.io.IOException ex) {
	String mes = "Error saving global properties when adding wscompile.tools.classpath for service '" + serviceName + "'\r\n" + ex.getMessage();
	NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
		DialogDisplayer.getDefault().notify(desc);
	}

	globalPropertiesChanged = true;
  }

  boolean projectPropertiesChanged = false;
  EditableProperties projectProperties = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);

  { // Block that adjusts wscompile.client.classpath as necessary.
    HashSet wscJars = new HashSet();
    boolean newWscJars = false;
    String wscClientClasspath = projectProperties.getProperty(WSCOMPILE_CLASSPATH);
    if(wscClientClasspath != null) {
       String [] libs = PropertyUtils.tokenizePath(wscClientClasspath);
       for(int i = 0; i < libs.length; i++) {
	  wscJars.add(libs[i]);
       }
    }

    for(int i = 0; i < WSCOMPILE_JARS.length; i++) {
      if(!wscJars.contains(WSCOMPILE_JARS[i])) {
        wscJars.add(WSCOMPILE_JARS[i]);
        newWscJars = true;
     }
   }

   if(newWscJars) {
       StringBuffer newClasspathBuf = new StringBuffer(256);
       for(Iterator iter = wscJars.iterator(); iter.hasNext(); ) {
	 newClasspathBuf.append(iter.next().toString());
	 if(iter.hasNext()) {
	   newClasspathBuf.append(":");
	}
      }
      projectProperties.put(WSCOMPILE_CLASSPATH, newClasspathBuf.toString());
      projectPropertiesChanged = true;
   }
}
   // Set websvc.generated.dir property, if not set.
   if(projectProperties.getProperty(WEBSVC_GENERATED_DIR) == null) {
	projectProperties.setProperty(WEBSVC_GENERATED_DIR, "${build.generated.dir}/wssrc");
	projectPropertiesChanged = true;
   }

   if(projectPropertiesChanged) {
	helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProperties);
   }
	return globalPropertiesChanged || projectPropertiesChanged;
   }


   private String getPackageName(FileObject file){
	FileObject parent = file.getParent();
	Sources sources = ProjectUtils.getSources(project);
	SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
	String packageName = null;
	for (int i = 0; i < groups.length && packageName == null; i++) {
		packageName = FileUtil.getRelativePath(groups [i].getRootFolder(), parent);
	}
	return packageName + "";
  }

}
