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

package org.netbeans.modules.j2ee.earproject;

import java.io.File;
import java.io.IOException;

import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.modules.InstalledFileLocator;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectGenerator;

import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.api.common.J2eeProjectConstants;
import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProjectGenerator;
import org.netbeans.modules.web.project.WebProjectGenerator;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.Module;
import org.netbeans.modules.j2ee.dd.api.application.DDProvider;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 * Create a fresh EarProject from scratch.
 * Importing an exisitng enterprise application 
 * in j2ee blueprint recommended layout format 
 * using existing sources subprojects
 *
 * @see WebProjectGenerator
 *
 * @author vince kraemer
 */
public class EarProjectGenerator {
    
    private static final String DEFAULT_DOC_BASE_FOLDER = "conf"; //NOI18N
    private static final String DEFAULT_SRC_FOLDER = "src"; //NOI18N
    private static final String DEFAULT_BUILD_DIR = "build"; //NOI18N
    private static final String DEFAULT_RESOURCE_FOLDER = "setup"; //NOI18N
    
    private static final String META_INF = "META-INF"; //NOI18N
    
    private static final String SOURCE_ROOT_REF = "${" + EarProjectProperties.SOURCE_ROOT + "}"; //NOI18N

    private EarProjectGenerator() {}

    /**
     * Create a new empty J2SE project.
     * @param dir the top-level directory (need not yet exist but if it does it must be empty)
     * @param name the code name for the project
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    public static AntProjectHelper createProject(File dir, String name, String j2eeLevel, String serverInstanceId, String contextPath) throws IOException {
        dir.mkdirs();
        // XXX clumsy way to refresh, but otherwise it doesn't work for new folders
        File rootF = dir;
        while (rootF.getParentFile() != null) {
            rootF = rootF.getParentFile();
        }
        FileObject fo = FileUtil.toFileObject (FileUtil.normalizeFile(rootF));
        assert fo != null : "At least disk roots must be mounted! " + rootF;
        fo.getFileSystem().refresh(false);
        fo = FileUtil.toFileObject (FileUtil.normalizeFile(dir));
        assert fo != null : "No such dir on disk: " + dir;
        assert fo.isFolder() : "Not really a dir: " + dir;
        AntProjectHelper h = setupProject (fo, name, j2eeLevel, serverInstanceId);
        fo = fo.createFolder(DEFAULT_SRC_FOLDER); // NOI18N
        FileObject webInfFO = fo.createFolder(DEFAULT_DOC_BASE_FOLDER); // NOI18N
        
        //create a default manifest
        FileUtil.copyFile(Repository.getDefault().getDefaultFileSystem().findResource("org-netbeans-modules-j2ee-earproject/MANIFEST.MF"), webInfFO, "MANIFEST"); //NOI18N
        
        //FileObject webInfFO = webFO.createFolder(META_INF); // NOI18N
        // create web.xml
        // PENDING : should be easier to define in layer and copy related FileObject (doesn't require systemClassLoader)
//        else if (J2eeProjectConstants.J2EE_13_LEVEL.equals(j2eeLevel))
//            FileUtil.copyFile(Repository.getDefault().getDefaultFileSystem().findResource("org-netbeans-modules-j2ee-ejbjarproject/ejb-jar-2.0.xml"), webInfFO, "ejb-jar"); //NOI18N
        
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.put (EarProjectProperties.SOURCE_ROOT, "."); //NOI18N
        ep.setProperty(EarProjectProperties.META_INF, DEFAULT_SRC_FOLDER+"/"+DEFAULT_DOC_BASE_FOLDER); //NOI18N
        ep.setProperty(EarProjectProperties.SRC_DIR, DEFAULT_SRC_FOLDER); //NOI18N
        ep.setProperty(EarProjectProperties.RESOURCE_DIR, DEFAULT_RESOURCE_FOLDER);
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        
        Project p = ProjectManager.getDefault().findProject(h.getProjectDirectory ());
        ProjectManager.getDefault().saveProject(p);
        
        // Make this a bit unit test friendlier.
        // The j2eeserver code is pretty convinced that it is running inside the
        // IDE at all times. Those assumptions don't hold up in the 
        FileObject tfo = Repository.getDefault().getDefaultFileSystem().findResource("org-netbeans-modules-j2ee-earproject/ear-1.4.xml");
        if (null != tfo) {
            if (J2eeProjectConstants.J2EE_14_LEVEL.equals(j2eeLevel)) 
                FileUtil.copyFile(tfo, webInfFO, "application"); //NOI18N
        
            ((EarProject)p).getAppModule().getConfigSupport ().createInitialConfiguration();
        }
            
        

        //create default index.jsp
        //createIndexJSP(webFO);
        
//        ProjectEjbJar pwm = (ProjectEjbJar) p.getLookup ().lookup (ProjectEjbJar.class);
//        if (pwm != null) //should not be null
//            pwm.setContextPath(contextPath);
        
        return h;
    }
    
    public static AntProjectHelper importProject (File pDir, File sDir, String name, String j2eeLevel, String serverInstanceID) throws IOException {
//        wmFO = dir;
//        File docRoot = 
        File top = sDir;
        File metaInf = new File(top,"src/conf");
        //dir = new File(dir,"nbimport");
        pDir.mkdirs();
        // XXX clumsy way to refresh, but otherwise it doesn't work for new folders
        File rootF = pDir;
        while (rootF.getParentFile() != null) {
            rootF = rootF.getParentFile();
        }
        FileObject fo = FileUtil.toFileObject (rootF);
        assert fo != null : "At least disk roots must be mounted! " + rootF;
        fo.getFileSystem().refresh(false);
        fo = FileUtil.toFileObject (FileUtil.normalizeFile(pDir));
        FileObject docBase = FileUtil.toFileObject (FileUtil.normalizeFile(metaInf));
        FileObject appRootFO = FileUtil.toFileObject (FileUtil.normalizeFile(top));
        assert fo != null : "No such dir on disk: " + pDir;
        assert fo.isFolder() : "Not really a dir: " + pDir;
        AntProjectHelper h = setupProject (fo, name, j2eeLevel, serverInstanceID);
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ReferenceHelper referenceHelper = new ReferenceHelper(h,
                h.createAuxiliaryConfiguration(), h.getStandardPropertyEvaluator());
//        EditableProperties ep = new EditableProperties();
//        if (FileUtil.isParentOf(fo, wmFO) || fo.equals(wmFO)) {
//            ep.setProperty(WebProjectProperties.SOURCE_ROOT, "."); //NOI18N
//        } else {
            ep.setProperty(EarProjectProperties.SOURCE_ROOT,
                    referenceHelper.createForeignFileReference(top, null));
//        }
        ep.setProperty(EarProjectProperties.META_INF, createFileReference(referenceHelper, fo, appRootFO, docBase));
//        if (FileUtil.isParentOf (fo, wmFO) || fo.equals (wmFO)) {
//            ep.put (EarProjectProperties.SOURCE_ROOT, "."); //NOI18N
//            ep.setProperty(EarProjectProperties.META_INF, relativePath (fo, docBase)); //NOI18N
            ep.setProperty(EarProjectProperties.SRC_DIR, "${"+EarProjectProperties.SOURCE_ROOT+"}/src"); //NOI18N
//            if (libFolder != null) {
  //              ep.setProperty(EarProjectProperties.LIBRARIES_DIR, relativePath (fo, libFolder)); //NOI18N
    //        }
      //  } else {
        //    File wmRoot = FileUtil.toFile (wmFO);
          //  ep.put (EarProjectProperties.SOURCE_ROOT, wmRoot.getAbsolutePath ());
            //String docPath = relativePath (wmFO, docBase);
//            docPath = docPath.length () > 0 ? "${"+EarProjectProperties.SOURCE_ROOT+"}/" + docPath : "${"+EarProjectProperties.SOURCE_ROOT+"}"; //NOI18N
  //          ep.setProperty(EarProjectProperties.META_INF, docPath);
    //        String javaPath = relativePath (wmFO, javaRoot);
      //      javaPath = javaPath.length () > 0 ? "${"+EarProjectProperties.SOURCE_ROOT+"}/" + javaPath : "${"+EarProjectProperties.SOURCE_ROOT+"}"; //NOI18N
        //    ep.setProperty(EarProjectProperties.SRC_DIR, javaPath);
          //  if (libFolder != null) {
            //    String libPath = relativePath (wmFO, libFolder);
              //  libPath = libPath.length () > 0 ? "${"+EarProjectProperties.SOURCE_ROOT+"}/" + libPath : "${"+EarProjectProperties.SOURCE_ROOT+"}"; //NOI18N
                //ep.setProperty(EarProjectProperties.LIBRARIES_DIR, libPath);
            //}
        //}
//        if (! GeneratedFilesHelper.BUILD_XML_PATH.equals (buildfile)) {
//            ep.setProperty (EarProjectProperties.BUILD_FILE, buildfile);
//        }
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        
        FileObject pd = h.getProjectDirectory ();
        Project p = ProjectManager.getDefault().findProject(pd);
        
        EarProject earProject = (EarProject) p.getLookup().lookup(EarProject.class);
        if (null != earProject) {
            Application app = null;
            try {
                FileObject backup, orig = earProject.getAppModule().getDeploymentDescriptor();
                if (null != orig) {
                    // make a backup copy of the application.xml
                    backup = FileUtil.copyFile(orig, orig.getParent(), "original_application", "xml");
                    if (null != backup) {
                        app = DDProvider.getDefault().getDDRoot(orig);
                        Module m[] = app.getModule();
                        if (null != m && m.length > 0) {
                            // notify the user here....
                            DialogDisplayer.getDefault().notify(
                                    new NotifyDescriptor(NbBundle.getMessage(EarProjectGenerator.class, "MESSAGE_CheckContextRoots"),
                                        NbBundle.getMessage(EarProjectGenerator.class, "TITLE_CheckContextRoots"),NotifyDescriptor.OK_CANCEL_OPTION,
                                        NotifyDescriptor.WARNING_MESSAGE, (Object[]) null, (Object) null));
                            // delete the modules
                            for (int k = 0; k < m.length; k++) {
                                app.removeModule(m[k]);
                            }
                        }
                //app.setDisplayName(name);
                //kids.add(new Node[] { new LogicalViewNode(app) });
                        app.write(earProject.getAppModule().getDeploymentDescriptor());
                    }
                }
            } catch (java.io.IOException ioe) {
                org.openide.ErrorManager.getDefault().log(ioe.getLocalizedMessage());
            }
        }
            AuxiliaryConfiguration aux = h.createAuxiliaryConfiguration();
            ReferenceHelper refHelper = new ReferenceHelper(h, aux, h.getStandardPropertyEvaluator ());
            EarProjectProperties epp = new EarProjectProperties((EarProject) p, h, refHelper, new EarProjectType());
        // detect the j2ee blueprint sub projects....
        // get the children
        
        FileObject[] children = appRootFO.getChildren();

        // for each child
        // -- if the child is a directory
                int webSubCount = 0;
                int ejbSubCount = 0;
        for (int i = 0; i < children.length; i++) {
            if (children[i].isFolder()) {
                FileObject subprojectRoot = children[i];
                // ---- test to see if it is a web module and trigger import
                FileObject webDotXml = 
                    subprojectRoot.getFileObject("web/WEB-INF/web.xml");
                FileObject javaRoot = 
                    subprojectRoot.getFileObject("src/java");
                FileObject ejbJarDotXml =
                    subprojectRoot.getFileObject("src/conf/ejb-jar.xml");
                AntProjectHelper subProjHelper = null;
                File subProjDir = new File(pDir,subprojectRoot.getName());
                subProjDir = FileUtil.normalizeFile(subProjDir);
                File srcFolders[] = null;
                if (null != javaRoot) {
                    srcFolders = new File[] { FileUtil.toFile(javaRoot)};
                } else {
                    srcFolders = new File[0];
                }
                if (null != webDotXml) {
                    subProjHelper = WebProjectGenerator.importProject(subProjDir,
                        subprojectRoot.getName(), subprojectRoot, srcFolders, new File[0],
                        subprojectRoot.getFileObject("web"), null, j2eeLevel, serverInstanceID, "build.xml");
                }

                // ---- test to see if it is an ejb jar project and trigger the import
                if (null != ejbJarDotXml) {
                    subProjHelper = EjbJarProjectGenerator.importProject(
			subProjDir, subprojectRoot.getName(),
			new File[] {FileUtil.toFile(javaRoot)},
			new File[0], FileUtil.toFile(ejbJarDotXml.getParent()),
			 null, j2eeLevel, serverInstanceID);
//                    subProjHelper = EjbJarProjectGenerator.importProject(subProjDir, 
//                        subprojectRoot.getName(), subprojectRoot, javaRoot, ejbJarDotXml.getParent(), j2eeLevel, serverInstanceID, "build.xml");
                }
                    
                // XXX ---- test to see if it is an app client and figure out how to import it.
                
                if (null != subProjHelper) {
                    pd = subProjHelper.getProjectDirectory();
                    Project subp = ProjectManager.getDefault().findProject(pd);
                    epp.addJ2eeSubprojects(new Project[] { subp });                    
                }
                
            }
        }
                
                // XXX all web module URI-to-ContextRoot mapping should happen here
        
        ProjectManager.getDefault().saveProject(p);
            ((EarProject)p).getAppModule().getConfigSupport ().createInitialConfiguration();
        
        return h;
    }
    
    private static String generateSubprojName(String base, String type, int count) {
        return base+type+count;
    }
    
    private static String relativePath (FileObject parent, FileObject child) {
        if (child.equals (parent))
            return "";
        if (!FileUtil.isParentOf (parent, child))
            throw new IllegalArgumentException ("Cannot find relative path, " + parent + " is not parent of " + child);
        return child.getPath ().substring (parent.getPath ().length () + 1);
    }
    
    private static AntProjectHelper setupProject (FileObject dirFO, String name, String j2eeLevel, String serverInstanceID) throws IOException {
        AntProjectHelper h = ProjectGenerator.createProject(dirFO, EarProjectType.TYPE);
        Element data = h.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element nameEl = doc.createElementNS(EarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
        nameEl.appendChild(doc.createTextNode(name));
        data.appendChild(nameEl);
        Element minant = doc.createElementNS(EarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "minimum-ant-version"); // NOI18N
        minant.appendChild(doc.createTextNode("1.6")); // NOI18N
        data.appendChild(minant);
        
        Element wmLibs = doc.createElementNS (EarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "web-module-libraries"); //NOI18N
        
//        if (J2eeProjectConstants.J2EE_14_LEVEL.equals(j2eeLevel)) {
//            Element servletLib = doc.createElementNS (EarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "library"); //NOI18N
//            Element servletLibName = doc.createElementNS (EarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "file"); //NOI18N
//            servletLibName.appendChild (doc.createTextNode ("${libs.j2ee14.classpath}")); //NOI18N
//            servletLib.appendChild (servletLibName);
//            wmLibs.appendChild (servletLib);
//
//            Element jspLib = doc.createElementNS (EarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "library"); //NOI18N
//            Element jspLibName = doc.createElementNS (EarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "file"); //NOI18N
//            jspLibName.appendChild (doc.createTextNode ("${libs.jsp20.classpath}")); //NOI18N
//            jspLib.appendChild (jspLibName);
//            wmLibs.appendChild (jspLib);
//            // XXX determine better way to handle the 1.4/1.5 switching
////        } else if (EjbJar.J2EE_13_LEVEL.equals(j2eeLevel)) {
////            Element servletLib = doc.createElementNS (EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "library"); //NOI18N
////            Element servletLibName = doc.createElementNS (EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "file"); //NOI18N
////            servletLibName.appendChild (doc.createTextNode ("${libs.servlet23.classpath}")); //NOI18N
////            servletLib.appendChild (servletLibName);
////            wmLibs.appendChild (servletLib);
//        }
        
        data.appendChild (wmLibs);
        
        Element addLibs = doc.createElementNS(EarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "web-module-additional-libraries"); //NOI18N
        data.appendChild(addLibs);
        
        h.putPrimaryConfigurationData(data, true);
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        // XXX the following just for testing, TBD:
        ep.setProperty(EarProjectProperties.DIST_DIR, "dist");
        ep.setProperty(EarProjectProperties.DIST_JAR, "${"+EarProjectProperties.DIST_DIR+"}/" + name.toLowerCase() + ".ear");
        
//        if (J2eeProjectConstants.J2EE_14_LEVEL.equals(j2eeLevel))
//            ep.setProperty(EarProjectProperties.JAVAC_CLASSPATH, "${libs.j2ee14.classpath}");
//        else if (EjbJar.J2EE_13_LEVEL.equals(j2eeLevel))
//            ep.setProperty(EarProjectProperties.JAVAC_CLASSPATH, "${libs.servlet23.classpath}");
        
        ep.setProperty(EarProjectProperties.J2EE_PLATFORM, j2eeLevel);
        
        ep.setProperty(EarProjectProperties.JAR_NAME, name + ".ear");
        ep.setProperty(EarProjectProperties.JAR_COMPRESS, "false");
        ep.setProperty(EarProjectProperties.JAR_CONTENT_ADDITIONAL, "");
        
        ep.setProperty(EarProjectProperties.CLIENT_MODULE_URI, "");
        ep.setProperty(EarProjectProperties.LAUNCH_URL_RELATIVE, "");
        //ep.setProperty(EarProjectProperties.LAUNCH_URL_FULL, "");
        ep.setProperty(EarProjectProperties.DISPLAY_BROWSER, "true");
        Deployment deployment = Deployment.getDefault ();
        ep.setProperty(EarProjectProperties.J2EE_SERVER_TYPE, deployment.getServerID (serverInstanceID));
        ep.setProperty(EarProjectProperties.JAVAC_SOURCE, "1.4");
        ep.setProperty(EarProjectProperties.JAVAC_DEBUG, "true");
        ep.setProperty(EarProjectProperties.JAVAC_DEPRECATION, "false");
        
        //xxx Default should be 1.2
        //http://projects.netbeans.org/buildsys/j2se-project-ui-spec.html#Build_Compiling_Sources
        ep.setProperty(EarProjectProperties.JAVAC_TARGET, "1.4");
        
        ep.setProperty(EarProjectProperties.BUILD_DIR, DEFAULT_BUILD_DIR);
        ep.setProperty(EarProjectProperties.BUILD_ARCHIVE_DIR, "${"+EarProjectProperties.BUILD_DIR+"}/jar");
        ep.setProperty(EarProjectProperties.BUILD_GENERATED_DIR, "${"+EarProjectProperties.BUILD_DIR+"}/generated");
        ep.setProperty(EarProjectProperties.BUILD_CLASSES_DIR, "${"+EarProjectProperties.BUILD_ARCHIVE_DIR+"}");
        ep.setProperty(EarProjectProperties.BUILD_CLASSES_EXCLUDES, "**/*.java,**/*.form,**/.nbattrs");
        ep.setProperty(EarProjectProperties.DIST_JAVADOC_DIR, "${"+EarProjectProperties.DIST_DIR+"}/javadoc");
        ep.setProperty(EarProjectProperties.NO_DEPENDENCIES, "false");
        ep.setProperty(EarProjectProperties.JAVA_PLATFORM, "default_platform");
        ep.setProperty(EarProjectProperties.DEBUG_CLASSPATH, 
            "${"+EarProjectProperties.JAVAC_CLASSPATH+"}:${"+
            EarProjectProperties.BUILD_CLASSES_DIR+"}:${"+
            EarProjectProperties.JAR_CONTENT_ADDITIONAL+"}:${"+
            EarProjectProperties.RUN_CLASSPATH+"}");
        
        ep.setProperty(EarProjectProperties.JAVADOC_PRIVATE, "false"); // NOI18N
        ep.setProperty(EarProjectProperties.JAVADOC_NO_TREE, "false"); // NOI18N
        ep.setProperty(EarProjectProperties.JAVADOC_USE, "true"); // NOI18N
        ep.setProperty(EarProjectProperties.JAVADOC_NO_NAVBAR, "false"); // NOI18N
        ep.setProperty(EarProjectProperties.JAVADOC_NO_INDEX, "false"); // NOI18N
        ep.setProperty(EarProjectProperties.JAVADOC_SPLIT_INDEX, "true"); // NOI18N
        ep.setProperty(EarProjectProperties.JAVADOC_AUTHOR, "false"); // NOI18N
        ep.setProperty(EarProjectProperties.JAVADOC_VERSION, "false"); // NOI18N
        ep.setProperty(EarProjectProperties.JAVADOC_WINDOW_TITLE, ""); // NOI18N
        ep.setProperty(EarProjectProperties.JAVADOC_ENCODING, ""); // NOI18N
        ep.setProperty(EarProjectProperties.JAVADOC_PREVIEW, "true"); // NOI18N        

//        ep.setProperty(EarProjectProperties.COMPILE_JSPS, "false"); // NOI18N        
        
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        
        ep = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        ep.setProperty(EarProjectProperties.J2EE_SERVER_INSTANCE, serverInstanceID);
        
        h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
        Project p = ProjectManager.getDefault().findProject(dirFO);
        ProjectManager.getDefault().saveProject(p);
        return h;
    }

    private static void createIndexJSP(FileObject webFolder) throws IOException {
        FileObject jspTemplate = Repository.getDefault().getDefaultFileSystem().findResource( "Templates/JSP_Servlet/JSP.jsp" );

        if (jspTemplate == null)
            return; // Don't know the template
                
        DataObject mt = DataObject.find(jspTemplate);        
        DataFolder webDf = DataFolder.findFolder(webFolder);        
        mt.createFromTemplate(webDf, "index");
    }

    private static String createFileReference(ReferenceHelper refHelper, FileObject projectFO,
            FileObject sourceprojectFO, FileObject referencedFO) {
        if (FileUtil.isParentOf(projectFO, referencedFO)) {
            return relativePath(projectFO, referencedFO);
        } else if (FileUtil.isParentOf(sourceprojectFO, referencedFO)) {
            String s = relativePath(sourceprojectFO, referencedFO);
            return s.length() > 0 ? SOURCE_ROOT_REF + "/" + s : SOURCE_ROOT_REF; //NOI18N
        } else {
            return refHelper.createForeignFileReference(FileUtil.toFile(referencedFO), null);
        }
    }

//    private static String relativePath (FileObject parent, FileObject child) {
//        if (child.equals (parent))
//            return ""; // NOI18N
//        if (!FileUtil.isParentOf (parent, child))
//            throw new IllegalArgumentException ("Cannot find relative path, " + parent + " is not parent of " + child);
//        return child.getPath ().substring (parent.getPath ().length () + 1);
//    }
//    
}