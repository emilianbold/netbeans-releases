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

package org.netbeans.modules.websvc.registry.nodes;

//import com.sun.beans2.live.LiveBean;
//import com.sun.rave.palette.BeanPaletteItem;
//import com.sun.rave.project.ProjectManager;
//import com.sun.rave.project.BuildCookie;
//import com.sun.rave.project.model.GenericFolder;
//import com.sun.rave.project.model.Portfolio;
//import com.sun.rave.project.model.Project;
//import com.sun.rave.project.model.LibraryReference;
//import com.sun.rave.project.model.SymbolicPath;
//import com.sun.rave.project.model.WebAppProject;
//import com.sun.rave.project.model.Reference;
//import com.sun.rave.project.model.SymbolicPath;
//import org.netbeans.modules.websvc.registry.jaxrpc.Wsdl2Java;
import org.netbeans.modules.websvc.registry.model.WebServiceData;
//import org.netbeans.modules.websvc.registry.util.DebugMonitor;
//import org.netbeans.modules.websvc.registry.util.JarUtil;
//import org.netbeans.modules.websvc.registry.util.WebProxySetter;
//import org.netbeans.modules.websvc.registry.util.Util;
//import com.sun.xml.rpc.util.JavaCompilerHelper;
import java.io.*;
//import java.net.URL;
//import java.net.MalformedURLException;
//import java.net.URLClassLoader;
//import java.util.Date;
//import java.util.ArrayList;
import javax.swing.ImageIcon;
//import org.netbeans.modules.editor.java.JCUpdater;
//import org.openide.ErrorManager;
//import org.openide.awt.StatusDisplayer;
//import org.openide.compiler.CompilerJob;
//import org.openide.compiler.Compiler;
//import org.openide.cookies.CompilerCookie;
//import org.openide.filesystems.FileLock;
//import org.openide.filesystems.FileObject;
//import org.openide.filesystems.FileUtil;
//import org.openide.filesystems.FileStateInvalidException;
//import org.openide.loaders.DataObject;
//import org.openide.util.NbBundle;
//import org.openide.util.RequestProcessor;
//import org.openide.DialogDescriptor;
//import org.openide.DialogDisplayer;
//import org.openide.NotifyDescriptor;
//import org.openide.windows.TopComponent;
//import com.sun.rave.designer.DesignerTopComp;
//import org.openide.nodes.Node;

// FIXME NEW AFTER COPY-PASTE FROM RAVE
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;

/**
 * Construct a palette like item to drag and drop the webservices on to the web designer
 *
 * @author  Winston Prakash
 */
public class WebServicePaletteItem implements Transferable { // FIXME COPY-PASTE FROM RAVE extends BeanPaletteItem {
    private String proxyName;
    private String wsdlFileName;
    private String wsdlFileNameExt = "wsdl";

    private WebServiceData data;

    public final static String WEB_SERVICE_PREFIX = "Web Service - ";
    public final static String WEB_SERVICE_REFERENCE_NODE="Web Service Support";

    public WebServicePaletteItem(WebServiceData data) {
//        super(null, null, new ImageIcon("org/netbeans/modules/websvc/registry/resources/webservice.png"));
        this.data = data;
        String wsName = data.getName();
        wsdlFileName = wsName.substring(wsName.lastIndexOf('.') + 1, wsName.length());
        proxyName = data.getProxy();
    }

//    public void annotateCreate(LiveBean liveBean) {
//        // NOP
//    }
//
//    /**
//     * Overriding to tell the Designer which class to create
//     */
//    public String getBeanClassName() {
//        return getWebServiceProxyBean();
//    }
//
//    /**
//     * This method will get the first project found from the Project Manager.
//     */
//    public Project getProject(){
//        ProjectManager pMgr = ProjectManager.getDefault();
//        Portfolio folio = pMgr.getPortfolio();
//        if (folio != null) {
//            Project[] projects = folio.getProjects();
//            if (projects.length > 0) {
//                if ((projects[0] instanceof WebAppProject)) {
//                    return projects[0];
//                }else{
//                    StatusDisplayer.getDefault().displayError(NbBundle.getMessage(WebServicesNode.class, NbBundle.getMessage(WebServicePaletteItem.class, "INCORRECT_PROJECT")),2);
//                }
//            } else {
//                DebugMonitor.println(NbBundle.getMessage(WebServicePaletteItem.class, "EMPTY_PORTFOLIO"));
//            }
//        } else {
//            StatusDisplayer.getDefault().displayError(NbBundle.getMessage(WebServicesNode.class, NbBundle.getMessage(WebServicePaletteItem.class, "EMPTY_PORTFOLIO_ERROR")),2);
//        }
//        return null;
//    }
//
//    /** When the user drags and drop the webservice on to the designer, designer requires the class name of a
//     *  bean to create necessary code using insync.
//     *  Steps - Get the project to which the designer belongs to (currently project[0] is used - XXXX
//     *          Get the backing folder of the project
//     *          Copy the webservice client jar to the Portfolio Manager "References" node.
//     *          Extract the webservice wrapper client, the webservice icon file,  and webservice
//     *              beaninfo from the webservice client jar
//     *          Copy the webservice wrapper client, the webservice icon file,  and webservice
//     *              beaninfo to the backing folder
//     *          Change the webservice wrapper client and beaninfo package names to match the project package names
//     *          Return the wrapper client as a bean to designer.
//     *
//     *  Note- for TP stabilization, instead of copying to the "reference" node, extract the jar to the project path and change the
//     *          package names.
//     *
//     *  Rewritten 2/19/2004 by David Botterill
//     */
//    public String getWebServiceProxyBean(){
//        FileObject wsdlFileObject = null;
//        FileObject proxyFileObject = null;
//
//        WebAppProject webAppProject = (WebAppProject)getProject();
//
//        /**
//         * Get the FileObject for the target folder for the web service client wrapper and beaninfo
//         */
//        FileObject clientWrapperTargetFolderObject = this.getClientWrapperFolder(webAppProject);
//
//        File clientWrapperTargetFolderFile = FileUtil.toFile(clientWrapperTargetFolderObject);
//
//
//        if(null == clientWrapperTargetFolderFile) {
//            /**
//             * just return, the method that created the File is responsible for logging why
//             * the file is null.
//             */
//            return null;
//        }
//
//        /**
//         * Form the Web Service client name
//         */
//
//        String displayName = data.getDisplayName();
//        if(null == displayName) {
//            ErrorManager.getDefault().log(WebServicePaletteItem.class.getName() + NbBundle.getMessage(WebServicePaletteItem.class, "WS_NO_WSNAME"));
//            StatusDisplayer.getDefault().displayError(NbBundle.getMessage(WebServicePaletteItem.class, "WS_DROP_ERROR"),2);
//            return null;
//        }
//        String webServiceClientPkgName = data.getPackageName();
//
//        String webServiceClientDisplayName =  displayName.substring(displayName.lastIndexOf('.') + 1, displayName.length()) + "Client";
//        String webServiceClientFullName =  webServiceClientPkgName + "." + webServiceClientDisplayName;
//
//
//        /**
//         * If the web service client has already been added to the page, display a dialog and return null.
//         */
//        if (isWebServiceInPage(displayName)) {
//
//            String msg = data.getDisplayName() + " " + NbBundle.getMessage(WebServicePaletteItem.class, "WS_EXISTS_ERROR");
//            NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
//            DialogDisplayer.getDefault().notify(d);
//
//            return null;
//        }
//        /**
//         * If the web service client has already been added to the project, simply return the webservice name;
//         */
//        if (isWebServiceInProject(displayName)) {
//
//
//            return webServiceClientFullName;
//        }
//
//
//        /**
//         * Get the FileObject for the web services within the project.
//         */
//        FileObject jarTargetFolderObject = this.getProjectSrcFolder(webAppProject);
//
//        if(null == jarTargetFolderObject) {
//            /**
//             * just return, the method that created the FileObject is responsible for logging why
//             * the FileObject is null.
//             */
//            return null;
//        }
//
//
//        /**
//         *  Get the jar file name of the web service client.
//         */
//        String jarFileName = data.getProxyJarFileName();
//
//        /**
//         * We should always get back the jar file name at this point. But, if we
//         * don't, return null, log an error and display a message.
//         */
//        if(null == jarFileName) {
//            /**
//             * If we are here, the user chose a WSDL URL that was preloaded and the jar doesn't exist.
//             * So let's create it.
//             *
//             */
//
//            jarFileName = System.getProperty("netbeans.user") +"/websvc/" + "webservice" + new Date().getTime() + ".jar";
//            if(!Util.createWSJar(data,null,jarFileName)) {
//                /**
//                 * One likely cause is that the HTTP Proxy setting so show a dialog with this hint.
//                 */
//                String msg = data.getDisplayName() + " " + NbBundle.getMessage(WebServicePaletteItem.class, "WS_WSDL2JAVA_ERROR");
//                NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
//                DialogDisplayer.getDefault().notify(d);
//
//                return null;
//            }
//
//        }
//        /**
//         * create a File reference for the jar file created for the web service client
//         */
//
//        File jarFile = new File(jarFileName);
//
//        /**
//         * Add the web service client jar to the Project
//         */
//        addJarToProject(displayName,jarFileName);
//
//        /**
//         * Now copy/change the client wrapper file and beaninfo from the default package to the project package.
//         */
//
//        // changePackages(jarFile,clientWrapperTargetFolderObject, webServiceClientDisplayName, displayName);
//
//        /**
//         * Copy the image file for the tray over to the new package.
//         */
//        //copyImageToProject(clientWrapperTargetFolderObject);
//
//
//        /* For debugging classloader issues
//        URLClassLoader classLoader = (URLClassLoader)this.getProject().getClassLoader();
//
//        URL [] urls = classLoader.getURLs();
//
//        System.out.println(WebServicePaletteItem.class.getName() + "classloader=" + classLoader);
//
//        for(int ii=0; ii < urls.length; ii++) {
//            System.out.println(WebServicePaletteItem.class.getName() + "url[" + ii + "]=" + urls[ii]);
//        }
//         */
//        /*
//        // Run the Java Auto Complete updater on the Project source folder
//        // to autocomplete the just added W/S bean and beaninfo files
//        GenericFolder projectSubFolder = webAppProject.getSrcFolder();
//        final DataObject projectFolderDataObject = projectSubFolder.getDataObject();
//        new RequestProcessor().post(new Runnable() {
//            public void run() {
//                JCUpdater update = new JCUpdater();
//                update.processDataObject(projectFolderDataObject, null);
//            }
//        });
//         **/
//
//
//        return webServiceClientFullName;
//    }
//
//    private boolean isWebServiceInPage(String inDisplayName) {
//        boolean foundNode = false;
//        TopComponent formView = DesignerTopComp.findCurrent();
//        if (formView == null) {
//            return foundNode;
//        }
//
//        /**
//         * We need to find out what nodes are displayed in the tray on the designer.  The designer shows all
//         * the activated nodes in the tray.
//         */
//        Node [] nodes = formView.getActivatedNodes();
//
//        /**
//         * go through each node and check if it's a WebServiceNode and it's name matches the Display Name passed in.
//         **/
//        for(int ii=0; ii < nodes.length; ii++) {
//            if(nodes[ii] instanceof WebServicesNode) {
//                WebServicesNode currentNode = (WebServicesNode)nodes[ii];
//                if(currentNode.getName().equals(inDisplayName)) {
//                    foundNode = true;
//                }
//            }
//        }
//
//
//        return foundNode;
//
//    }
//
//    private boolean isWebServiceInProject(String inDisplayName) {
//        WebAppProject webAppProject = (WebAppProject)getProject();
//        Reference [] references = webAppProject.getReferences();
//        boolean foundReference = false;
//
//        for(int ii=0; ii < references.length; ii++) {
//            if(references[ii] != null && references[ii].getDisplayName() != null) {
//                if(references[ii].getDisplayName().equalsIgnoreCase(this.WEB_SERVICE_PREFIX + inDisplayName))  {
//                    foundReference = true;
//                    break;
//
//                }
//            }
//        }
//
//        return foundReference;
//
//    }
//
//    /**
//     * This method will determine where the source folder is for the project.
//     * exist).
//     * @return FileObject of the project source code folder representing
//     * @param inProject The WebAppProject to look in for the web service source code folder.
//     */
//    private FileObject getProjectSrcFolder(WebAppProject inProject) {
//        FileObject returnFileObject = null;
//        /**
//         * First we need to get the target project backing folder so we know where to
//         * copy the web service client source and beaninfo.
//         */
//        GenericFolder projectSubFolder = inProject.getSrcFolder();
//
//        /**
//         * now we need to know the project folder so we can add the webservice folder to it.
//         */
//        DataObject projectFolderDataObject = projectSubFolder.getDataObject();
//
//        /**
//         * Make sure we got a valid DataObject
//         */
//        if(projectFolderDataObject == null) {
//            ErrorManager.getDefault().log(WebServicePaletteItem.class.getName() + NbBundle.getMessage(WebServicePaletteItem.class, "WS_NO_DATAOBJ"));
//            StatusDisplayer.getDefault().displayError(NbBundle.getMessage(WebServicePaletteItem.class, "WS_NO_DATAOBJ"),2);
//            return null;
//        }
//
//        returnFileObject = projectFolderDataObject.getPrimaryFile();
//        /**
//         * Make sure we have a valid FileObject
//         */
//        if (returnFileObject == null) {
//            ErrorManager.getDefault().log(WebServicePaletteItem.class.getName() + NbBundle.getMessage(WebServicePaletteItem.class, "WS_MISSING_PROJECT_FILE"));
//            StatusDisplayer.getDefault().displayError(NbBundle.getMessage(WebServicePaletteItem.class, "WS_MISSING_PROJECT_FILE"),2);
//            return null;
//        }
//
//
//        return returnFileObject;
//    }
//
//
//    /**
//     * This method will determine the folder for the project source code package.
//     * @param inProject The WebAppProject to look in for the web service source code folder.
//     * @return FileObject of the web service source code folder representing the package of
//     * the generated web service source code.
//     */
//    private FileObject getClientWrapperFolder(WebAppProject inProject) {
//
//        FileObject returnFileObject = null;
//        /**
//         * Get the folder the client wrapper code should go in. This is the same package
//         * as the rest of the web application.
//         */
//        /**
//         * Now get the folder of the project source.
//         */
//        GenericFolder projectSubFolder = inProject.getBackingFolder();
//
//        //        assert projectSubFolder != null;
//        DataObject dObj = projectSubFolder.getDataObject();
//        /**
//         * Make sure we got a valid DataObject
//         */
//        if(dObj == null) {
//            ErrorManager.getDefault().log(WebServicePaletteItem.class.getName() + NbBundle.getMessage(WebServicePaletteItem.class, "WS_NO_DATAOBJ"));
//            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(WebServicePaletteItem.class, "WS_NO_DATAOBJ"));
//            return null;
//        }
//
//        returnFileObject = dObj.getPrimaryFile();
//
//        /**
//         * Make sure we have a valid FileObject
//         */
//        if (returnFileObject == null) {
//            ErrorManager.getDefault().log(WebServicePaletteItem.class.getName() + NbBundle.getMessage(WebServicePaletteItem.class, "WS_MISSING_PROJECT_FILE"));
//            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(WebServicePaletteItem.class, "WS_MISSING_PROJECT_FILE"));
//            return null;
//        }
//
//
//        return returnFileObject;
//
//    }
//    /**
//     * This method adds the jar file for the web service client to the project references.
//     * @param inWebServiceName The web service name to use for the name of the reference
//     * @param inJarFileName The name of the jar file to add to the project resources.
//     */
//    private void addJarToProject(String inWebServiceName, String inJarFileName) {
//
//        Project project = this.getProject();
//        /**
//         * Copy the jar file over to the project.
//         */
//        String projectAbsPath = project.getAbsolutePath();
//        String projectDataRoot = project.getProjectDataRoot();
//
//        File webservicesDataDir = new File(projectAbsPath + File.separator  + projectDataRoot + File.separator + "webservice_clients");
//        /**
//         * Make sure the directory exists.
//         */
//        if(!webservicesDataDir.exists()) {
//            webservicesDataDir.mkdir();
//        }
//
//        /**
//         * strip any leading path information off.
//         */
//        String newJarFileName = null;
//
//        newJarFileName = inJarFileName;
//
//        if(newJarFileName.indexOf("/") != -1) {
//            newJarFileName = inJarFileName.substring(inJarFileName.lastIndexOf("/")+1);
//        }
//        if(newJarFileName.indexOf("\\") != -1) {
//            newJarFileName = inJarFileName.substring(inJarFileName.lastIndexOf("\\")+1);
//        }
//
//        File newJarFile = new File(webservicesDataDir.getAbsolutePath(),newJarFileName);
//
//        File oldJarFile =  new File(inJarFileName);
//
//        try {
//            FileOutputStream outStream = new FileOutputStream(newJarFile);
//            DataInputStream in = new DataInputStream(new FileInputStream(oldJarFile));
//            DataOutputStream out = new DataOutputStream(outStream);
//
//            byte[] bytes = new byte[1024];
//            int byteCount = in.read(bytes);
//
//            while ( byteCount > -1 ) {
//                out.write( bytes, 0, byteCount );
//                byteCount = in.read(bytes);
//            }
//            out.flush();
//            out.close();
//            outStream.close();
//            in.close();
//
//        } catch (IOException ioe) {
//            ErrorManager.getDefault().notify(ioe);
//            StatusDisplayer.getDefault().displayError(NbBundle.getMessage(WebServicesNode.class, "JAR_COPY_ERROR"),2);
//            return;
//        }
//
//        /**
//         * Create the library reference to add to the project.
//         */
//        LibraryReference libraryRef = new LibraryReference(this.WEB_SERVICE_PREFIX + inWebServiceName,project);
//        /**
//         * Setting the DisplayName important because we are going to use this to get the reference for this jar later.
//         */
//        libraryRef.setDisplayName(this.WEB_SERVICE_PREFIX + inWebServiceName);
//
//        /**
//         * Specify where the definition file will live for this reference.
//         */
//        libraryRef.setSymbolicPath(new SymbolicPath("{project.home}/project-data/lib/references/" + inWebServiceName + ".xml"));
//
//        /**
//         * Specify the path to the web services client jar file.
//         */
//        SymbolicPath jarPath = new SymbolicPath("{project.home}/project-data/webservice_clients/" + newJarFileName);
//
//        /**
//         * Tell the project what actions to take with this library reference
//         */
//        libraryRef.addPathAction(jarPath, LibraryReference.ACTION_CLASSPATH);
//        libraryRef.addPathAction(jarPath, LibraryReference.ACTION_COPY);
//
//
//        /**
//         * Add the path to the reference.
//         */
//        libraryRef.addClassPath(jarPath);
//
//        /**
//         * Add the reference to the project.
//         */
//        project.addReference(libraryRef);
//
//
//
//    }
//
//
//
//
//    /**
//     * This method will extract the web service client files to the correct project directory
//     * changing the package statements in the process.  This method will also compile the new
//     * java files putting the classes in the same directory that the java files were copied.
//     * @param inJarFile The jar file to extract the web service client files from
//     * @param toPackage the FileObject to copy the web service client files to
//     * @param webServiceClientName The java name of the web service client
//     * @param shortWSName the web service name given to the element within Creator, the same names that
//     * shows up in the Server Navigator
//     */
//    private void changePackages(File inJarFile, FileObject toPackage,String webServiceClientName, String displayName) {
//        FileObject newClientJavaObject = null;
//        FileObject oldClientJavaObject = null;
//        FileObject newClientBeanInfoJavaObject = null;
//        FileObject oldClientBeanInfoJavaObject = null;
//
//        /**
//         * Get the name of the package where the client code will be copied to.
//         */
//        String toPackageName = toPackage.getPackageName('.');
//
//        JarUtil jarUtil = new JarUtil(inJarFile);
//
//        String wsClientJarFilename = Wsdl2Java.DEFAULT_TARGET_PACKAGE + "/" + webServiceClientName + ".java";
//        String wsClientBeanInfoJarFilename = Wsdl2Java.DEFAULT_TARGET_PACKAGE + "/" + webServiceClientName + "BeanInfo.java";
//
//        try {
//            newClientJavaObject = toPackage.createData(webServiceClientName,"java");
//        }catch(IOException ioe){
//            ErrorManager.getDefault().notify(ioe);
//            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(WebServicePaletteItem.class, "WS_DROP_ERROR"));
//        }
//
//        try{
//
//            PrintWriter out = new PrintWriter(newClientJavaObject.getOutputStream(newClientJavaObject.lock()));
//            BufferedReader in = jarUtil.openFile(wsClientJarFilename);
//
//            /**
//             * Read through the java source for the client wrapper and write it out to the new package, when we hit
//             * the package statement, change it to be the new package.
//             */
//            String currentLine = "";
//            String outLine = "";
//            while((currentLine = in.readLine()) != null) {
//                if(currentLine.indexOf("package ") == 0) {
//                    outLine = "package " + toPackageName + ";";
//                } else {
//                    outLine = currentLine;
//                }
//
//                out.println(outLine);
//            }
//
//            out.close();
//            in.close();
//
//        }catch(IOException ioe){
//            ErrorManager.getDefault().notify(ioe);
//            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(WebServicePaletteItem.class, "WS_DROP_ERROR"));
//        }
//
//        /**
//         * Now copy the BeanInfo
//         */
//        try {
//            newClientBeanInfoJavaObject = toPackage.createData(webServiceClientName + "BeanInfo","java");
//        }catch(IOException ioe){
//            ErrorManager.getDefault().notify(ioe);
//            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(WebServicePaletteItem.class, "WS_DROP_ERROR"));
//        }
//
//        try{
//            PrintWriter out = new PrintWriter(newClientBeanInfoJavaObject.getOutputStream(newClientBeanInfoJavaObject.lock()));
//            BufferedReader in = jarUtil.openFile(wsClientBeanInfoJarFilename);
//
//
//            /**
//             * Read through the java source for the client wrapper and write it out to the new package, when we hit
//             * the package statement, change it to be the new package.
//             */
//            String currentLine = "";
//            String outLine = "";
//            while((currentLine = in.readLine()) != null) {
//                if(currentLine.indexOf("package ") == 0) {
//                    outLine = "package " + toPackageName + ";";
//                } else {
//                    outLine = currentLine;
//                }
//
//                out.println(outLine);
//            }
//
//            out.close();
//            in.close();
//
//
//        }catch(IOException ioe){
//            ErrorManager.getDefault().notify(ioe);
//            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(WebServicePaletteItem.class, "WS_DROP_ERROR"));
//        }
//
//
//        /**
//         * Now we need to compile the new class files
//         */
//
//        /**
//         * Get the to path for the destination of the compile.
//         */
//        File toPackageFile = FileUtil.toFile(toPackage);
//
//        String destinationDir = toPackageFile.getParentFile().getAbsolutePath();
//
//        /**
//         * Create a classpath for the compile
//         */
//        String classPath = "";
//
//        /**
//         * Get the web service support and client jar locations from the project references to add to the classpath.
//         */
//        WebAppProject webAppProject = (WebAppProject)getProject();
//        Reference [] references = webAppProject.getReferences();
//
//        for(int ii=0; ii < references.length; ii++) {
//            if(references[ii] != null && references[ii].getDisplayName() != null) {
//                if(references[ii].getDisplayName().equalsIgnoreCase(this.WEB_SERVICE_REFERENCE_NODE) ||
//                references[ii].getDisplayName().equalsIgnoreCase(this.WEB_SERVICE_PREFIX + displayName))  {
//                    LibraryReference libRef = (LibraryReference)references[ii];
//                    SymbolicPath [] classpaths = libRef.getClassPaths();
//                    for(int jj=0; jj < classpaths.length; jj++) {
//                        classPath += File.pathSeparator + classpaths[jj].getResolvedPath();
//                    }
//                }
//            }
//        }
//
//        /**
//         * Now we have to compile the new web service client wrapper and beaninfo so the classes will be available for the designer.
//         */
//        compileWSClientFiles(destinationDir, classPath, newClientJavaObject, newClientBeanInfoJavaObject);
//
//
//        /**
//         * Now refresh the folder
//         * TODO: determine whether we should ignore the getFileSystem exception
//         */
//        try {
//            toPackage.getFileSystem().refresh(true);
//        } catch(FileStateInvalidException fsie) {
//            //ignore
//        }
//    }
//
//
//    /** Compile the proxy client just created */
//    private void compileWSClientFiles(String inDestDir, String inClasspath,
//    FileObject inWrapperObject, FileObject inWrapperBeanInfoObjectd){
//
//        ArrayList argList = new ArrayList();
//
//        argList.add("-d");
//        argList.add(inDestDir);
//        argList.add("-classpath");
//        argList.add(inClasspath);
//        /**
//         * TODO: get the debug flag for the Creator environment.
//         */
//        argList.add("-g");
//
//        /**
//         * Now add the files to be compiled
//         */
//        File wrapperFile = FileUtil.toFile(inWrapperObject);
//        argList.add(wrapperFile.getAbsolutePath());
//        File wrapperBeanInfoFile = FileUtil.toFile(inWrapperBeanInfoObjectd);
//        argList.add(wrapperBeanInfoFile.getAbsolutePath());
//
//        String [] args = (String [])argList.toArray(new String[0]);
//
//        // ByteArrayOutputStream javacOutput = new ByteArrayOutputStream();
//
//        /**
//         * Define a temp file for the compile results.
//         */
//        String outputDir = System.getProperty("user.home");
//        File outputDirFile = new File(outputDir);
//        File tempFile = null;
//        try{
//            tempFile = File.createTempFile("wstemp","ws",outputDirFile);
//        }catch (IOException ioe){
//            ErrorManager.getDefault().notify(ioe);
//            StatusDisplayer.getDefault().displayError(NbBundle.getMessage(WebServicePaletteItem.class, "WS_DROP_ERROR"),2);
//            return;
//        }
//
//        FileOutputStream out = null;
//
//        try {
//            out = new FileOutputStream(tempFile);
//        } catch(FileNotFoundException fnfe) {
//
//            ErrorManager.getDefault().notify(fnfe);
//            StatusDisplayer.getDefault().displayError(NbBundle.getMessage(WebServicePaletteItem.class, "WS_DROP_ERROR"),2);
//            return;
//        }
//
//        JavaCompilerHelper compilerHelper = new JavaCompilerHelper(out);
//
//        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(WebServicePaletteItem.class, "WS_CLIENTWRAPPER_COMPILING"));
//
//        boolean result = compilerHelper.compile(args);
//        if (!result) {
//            ErrorManager.getDefault().log(WebServicePaletteItem.class.getName() + NbBundle.getMessage(WebServicePaletteItem.class, "WS_CLIENTWRAPPER_COMPILE_ERROR") + tempFile == null ? "" : tempFile.getAbsolutePath());
//            StatusDisplayer.getDefault().displayError(NbBundle.getMessage(WebServicePaletteItem.class, "WS_DROP_ERROR"),2);
//        } else {
//            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(WebServicePaletteItem.class, "WS_CLIENTWRAPPER_COMPILE_OK"));
//            /**
//             * clean up the output file since the compile was successful
//             */
//            tempFile.delete();
//        }
//
//    }

	// FIXME NEW AFTER COPY-PASTE FROM RAVE
	public Object getTransferData(java.awt.datatransfer.DataFlavor dataFlavor) throws java.awt.datatransfer.UnsupportedFlavorException, java.io.IOException {
		return null;
	}

	public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors() {
		return null;
	}

	public boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor dataFlavor) {
		return false;
	}

}
