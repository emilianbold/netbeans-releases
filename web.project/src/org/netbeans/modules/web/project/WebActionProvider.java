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

package org.netbeans.modules.web.project;

import java.awt.Dialog;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.NbBundle;
import org.openide.src.*;
import org.openide.cookies.SourceCookie;
import org.netbeans.modules.j2ee.deployment.impl.projects.*;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.netbeans.modules.j2ee.deployment.execution.*;
import org.netbeans.api.debugger.*;
import org.netbeans.api.debugger.jpda.*;
import javax.enterprise.deploy.spi.Target;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.modules.j2ee.deployment.impl.*;
import org.netbeans.modules.j2ee.deployment.devmodules.api.*;
import org.netbeans.modules.web.api.webmodule.URLCookie;
import org.netbeans.modules.web.project.ui.NoSelectedServerWarning;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.modules.web.project.ui.ServletUriPanel;
import org.netbeans.modules.web.project.ui.SetExecutionUriAction;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.*;
import org.netbeans.api.project.ProjectInformation;

import org.netbeans.api.web.dd.DDProvider;
import org.netbeans.api.web.dd.WebApp;
import org.netbeans.api.web.dd.Servlet;
import org.netbeans.api.web.dd.ServletMapping;
import org.netbeans.api.java.classpath.ClassPath;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.netbeans.modules.web.api.webmodule.WebModule;

import org.netbeans.modules.web.api.webmodule.WebProjectConstants;



/** Action provider of the Web project. This is the place where to do
 * strange things to Web actions. E.g. compile-single.
 */
class WebActionProvider implements ActionProvider {
    
    // Definition of commands
    
    private static final String COMMAND_COMPILE = "compile"; //NOI18N
        
    // Commands available from Web project
    private static final String[] supportedActions = {
        COMMAND_BUILD, 
        COMMAND_CLEAN, 
        COMMAND_REBUILD, 
        COMMAND_COMPILE_SINGLE, 
        COMMAND_RUN, 
        COMMAND_RUN_SINGLE, 
        COMMAND_DEBUG, 
        WebProjectConstants.COMMAND_REDEPLOY,
        COMMAND_DEBUG_SINGLE, 
        JavaProjectConstants.COMMAND_JAVADOC, 
        JavaProjectConstants.COMMAND_DEBUG_FIX,
        COMMAND_COMPILE,
    };
    
    // Project
    WebProject project;
    
    // Ant project helper of the project
    private AntProjectHelper antProjectHelper;
    private ReferenceHelper refHelper;
        
    /** Map from commands to ant targets */
    Map/*<String,String[]>*/ commands;
    
    public WebActionProvider(WebProject project, AntProjectHelper antProjectHelper, ReferenceHelper refHelper) {
        
        commands = new HashMap();
            commands.put(COMMAND_BUILD, new String[] {"dist"}); // NOI18N
            commands.put(COMMAND_CLEAN, new String[] {"clean"}); // NOI18N
            commands.put(COMMAND_REBUILD, new String[] {"clean", "dist"}); // NOI18N
            // the target name is compile-single, except for JSPs, where it is compile-single-jsp
            commands.put(COMMAND_COMPILE_SINGLE, new String[] {"compile-single"}); // NOI18N
            commands.put(COMMAND_RUN, new String[] {"run"}); // NOI18N
            // the target name is run, except for Java files with main method, where it is run-main
            commands.put(COMMAND_RUN_SINGLE, new String[] {"run"}); // NOI18N
            commands.put(WebProjectConstants.COMMAND_REDEPLOY, new String[] {"run"}); // NOI18N
            commands.put(COMMAND_DEBUG, new String[] {"debug"}); // NOI18N
            commands.put(COMMAND_DEBUG_SINGLE, new String[] {"debug"}); // NOI18N
            commands.put(JavaProjectConstants.COMMAND_JAVADOC, new String[] {"javadoc"}); // NOI18N
            commands.put(JavaProjectConstants.COMMAND_DEBUG_FIX, new String[] {"debug-fix"}); // NOI18N
            commands.put(COMMAND_COMPILE, new String[] {"compile"}); // NOI18N
        
        this.antProjectHelper = antProjectHelper;
        this.project = project;
        this.refHelper = refHelper;
    }
    
    private FileObject findBuildXml() {
        return project.getProjectDirectory().getFileObject(project.getBuildXmlName ());
    }
    
    public String[] getSupportedActions() {
        return supportedActions;
    }
    
    public void invokeAction( String command, Lookup context ) throws IllegalArgumentException {
        Properties p;
        String[] targetNames = (String[])commands.get(command);
        //EXECUTION PART
        if (command.equals (COMMAND_RUN) || command.equals (COMMAND_RUN_SINGLE) || command.equals (WebProjectConstants.COMMAND_REDEPLOY)) {
            if (!isSelectedServer ()) {
                return;
            }
            if (isDebugged()) {
                NotifyDescriptor nd;
                String text;
                if (command.equals (COMMAND_RUN)) {
                    ProjectInformation pi = (ProjectInformation)project.getLookup().lookup(ProjectInformation.class);
                    text = pi.getDisplayName();
                } else { //COMMAND_RUN_SINGLE
                    FileObject[] files = ActionUtils.findSelectedFiles(context, null, null, false);
                    text = (files == null) ? "?" : files[0].getNameExt(); // NOI18N
                }
                nd = new NotifyDescriptor.Confirmation(
                            NbBundle.getMessage(WebActionProvider.class, "MSG_SessionRunning", text),
                            NotifyDescriptor.OK_CANCEL_OPTION);
                Object o = DialogDisplayer.getDefault().notify(nd);
                if (o.equals(NotifyDescriptor.OK_OPTION)) {            
                    DebuggerManager.getDebuggerManager().getCurrentSession().kill();
                } else {
                    return;
                }
            }
            p = new Properties();
            if (command.equals (WebProjectConstants.COMMAND_REDEPLOY)) {
                p.setProperty("forceRedeploy", "true"); //NOI18N
            } else {
                p.setProperty("forceRedeploy", "false"); //NOI18N
            }
            if (command.equals (COMMAND_RUN_SINGLE)) {
                // run a JSP
                FileObject[] files = findJsps( context );
                if (files!=null && files.length>0) {
                    try {
                        // possibly compile the JSP, if we are not compiling all of them
                        String raw = antProjectHelper.getStandardPropertyEvaluator ().getProperty (WebProjectProperties.COMPILE_JSPS);
                        boolean compile = decodeBoolean(raw);
                        if (!compile) {
                            p.setProperty("jsp.includes", getBuiltJspFileNamesAsPath(files)); // NOI18N
                        }
                        
                        URLCookie uc = (URLCookie) DataObject.find (files [0]).getCookie (URLCookie.class);
                        if (uc != null) {
                            p.setProperty("client.urlPart", uc.getURL ()); //NOI18N
                        } else {
                            return;
                        }
                    } catch (DataObjectNotFoundException e) {
                        ErrorManager.getDefault ().notify (e);
                        return;
                    }
                } else {
                    // run HTML file
                    FileObject[] htmlFiles = findHtml(context);
                    if ((htmlFiles != null) && (htmlFiles.length>0)) {
                        String url = "/" + FileUtil.getRelativePath(WebModule.getWebModule (htmlFiles[0]).getDocumentBase (), htmlFiles[0]); // NOI18N
                        if (url != null) {
                            url = org.openide.util.Utilities.replaceString(url, " ", "%20");
                            p.setProperty("client.urlPart", url); //NOI18N
                        } else {
                            return;
                        }
                    } else {
                        // run Java
                        FileObject[] javaFiles = findJavaSources(context);
                        if ((javaFiles != null) && (javaFiles.length>0)) {
                            FileObject javaFile = javaFiles[0];
                            
                            if (hasMainMethod(javaFile)) {
                                // run Java with Main method
                                String clazz = FileUtil.getRelativePath(project.getSourceDirectory(), javaFile);
                                p = new Properties();
                                p.setProperty("javac.includes", clazz); // NOI18N
                                // Convert foo/FooTest.java -> foo.FooTest
                                if (clazz.endsWith(".java")) { // NOI18N
                                    clazz = clazz.substring(0, clazz.length() - 5);
                                }
                                clazz = clazz.replace('/','.');
                                
                                p.setProperty("run.class", clazz); // NOI18N
                                targetNames = new String [] {"run-main"};
                            }
                            else {
                                // run servlet
                                // PENDING - what about servlets with main method? servlet should take precedence
                                String executionUri = (String)javaFile.getAttribute(SetExecutionUriAction.ATTR_EXECUTION_URI);
                                if (executionUri!=null) {
                                    p.setProperty("client.urlPart", executionUri); //NOI18N
                                } else {
                                    WebModule webModule = WebModule.getWebModule(javaFile);
                                    String[] urlPatterns = SetExecutionUriAction.getServletMappings(webModule,javaFile);
                                    if (urlPatterns!=null && urlPatterns.length>0) {
                                        ServletUriPanel uriPanel = new ServletUriPanel(urlPatterns,null,true);
                                        DialogDescriptor desc = new DialogDescriptor(uriPanel,
                                            NbBundle.getMessage (SetExecutionUriAction.class, "TTL_setServletExecutionUri"));
                                        Object res = DialogDisplayer.getDefault().notify(desc);
                                        if (res.equals(NotifyDescriptor.YES_OPTION)) {
                                            p.setProperty("client.urlPart", uriPanel.getServletUri()); //NOI18N
                                            try {
                                                javaFile.setAttribute(SetExecutionUriAction.ATTR_EXECUTION_URI,uriPanel.getServletUri());
                                            } catch (IOException ex){}
                                        } else return;
                                    } else {
                                        String mes = java.text.MessageFormat.format (
                                                NbBundle.getMessage (SetExecutionUriAction.class, "TXT_missingServletMappings"),
                                                new Object [] {javaFile.getName()});
                                        NotifyDescriptor desc = new NotifyDescriptor.Message(mes,NotifyDescriptor.Message.ERROR_MESSAGE);
                                        DialogDisplayer.getDefault().notify(desc);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }

            }
        //DEBUGGING PART
        } else if (command.equals (COMMAND_DEBUG) || command.equals(COMMAND_DEBUG_SINGLE)) {
            if (!isSelectedServer ()) {
                return;
            }
            if (isDebugged()) {
                NotifyDescriptor nd;
                nd = new NotifyDescriptor.Confirmation(
                            NbBundle.getMessage(WebActionProvider.class, "MSG_FinishSession"),
                            NotifyDescriptor.OK_CANCEL_OPTION);
                Object o = DialogDisplayer.getDefault().notify(nd);
                if (o.equals(NotifyDescriptor.OK_OPTION)) {            
                    DebuggerManager.getDebuggerManager().getCurrentSession().kill();
                } else {
                    return;
                }
            }
            J2eeModuleProvider jmp = (J2eeModuleProvider)project.getLookup().lookup(J2eeModuleProvider.class);
            ServerDebugInfo sdi = jmp.getServerDebugInfo ();
            String h = sdi.getHost();
            String transport = sdi.getTransport();
            String address = "";                                                //NOI18N
            
            if (transport.equals(ServerDebugInfo.TRANSPORT_SHMEM)) {
                address = sdi.getShmemName();
            } else {
                address = Integer.toString(sdi.getPort());
            }
            
            p = new Properties();
            p.setProperty("jpda.transport", transport);
            p.setProperty("jpda.host", h);
            p.setProperty("jpda.address", address);
        
            if (command.equals (COMMAND_DEBUG)) {
                p.setProperty("client.urlPart", project.getWebModule().getUrl());
            } else { //COMMAND_DEBUG_SINGLE
                FileObject[] files = findJsps( context );
                // debug jsp
                if ((files != null) && (files.length>0)) {
                    try {
                        // possibly compile the JSP, if we are not compiling all of them
                        String raw = antProjectHelper.getStandardPropertyEvaluator ().getProperty (WebProjectProperties.COMPILE_JSPS);
                        boolean compile = decodeBoolean(raw);
                        if (!compile) {
                            p.setProperty("jsp.includes", getBuiltJspFileNamesAsPath(files)); // NOI18N
                        }
                        
                        URLCookie uc = (URLCookie) DataObject.find (files [0]).getCookie (URLCookie.class);
                        if (uc != null) {
                            p.setProperty("client.urlPart", uc.getURL ());
                        } else {
                            return;
                        }
                    } catch (DataObjectNotFoundException e) {
                        ErrorManager.getDefault ().notify (e);
                        return;
                    }
                // debug servlet
                } else {
                    FileObject[] javaFiles = findJavaSources(context);
                    FileObject servlet = javaFiles[0];
                    String executionUri = (String)servlet.getAttribute(SetExecutionUriAction.ATTR_EXECUTION_URI);
                    if (executionUri!=null) {
                        p.setProperty("client.urlPart", executionUri); //NOI18N
                    } else {
                        WebModule webModule = WebModule.getWebModule(servlet);
                        String[] urlPatterns = SetExecutionUriAction.getServletMappings(webModule,servlet);
                        if (urlPatterns!=null && urlPatterns.length>0) {
                            ServletUriPanel uriPanel = new ServletUriPanel(urlPatterns,null,true);
                            DialogDescriptor desc = new DialogDescriptor(uriPanel,
                                NbBundle.getMessage (SetExecutionUriAction.class, "TTL_setServletExecutionUri"));
                            Object res = DialogDisplayer.getDefault().notify(desc);
                            if (res.equals(NotifyDescriptor.YES_OPTION)) {
                                p.setProperty("client.urlPart", uriPanel.getServletUri()); //NOI18N
                                try {
                                    servlet.setAttribute(SetExecutionUriAction.ATTR_EXECUTION_URI,uriPanel.getServletUri());
                                } catch (IOException ex){}
                            } else return;
                        } else {
                            String mes = java.text.MessageFormat.format (
                                    NbBundle.getMessage (SetExecutionUriAction.class, "TXT_missingServletMappings"),
                                    new Object [] {servlet.getName()});
                            NotifyDescriptor desc = new NotifyDescriptor.Message(mes,NotifyDescriptor.Message.ERROR_MESSAGE);
                            DialogDisplayer.getDefault().notify(desc);
                            return;
                        }
                    }
                }
            }            
            
        //COMPILATION PART
        } else if ( command.equals( COMMAND_COMPILE_SINGLE ) ) {
            FileObject[] files = findJavaSources( context );
            p = new Properties();
            if (files != null) {
                p.setProperty("javac.includes", ActionUtils.antIncludesList(files, project.getSourceDirectory())); // NOI18N
            } else {
                files = findJsps (context);
                if (files != null) {
                    p.setProperty("jsp.includes", getBuiltJspFileNamesAsPath(files) /*ActionUtils.antIncludesList(files, project.getWebModule ().getDocumentBase ())*/); // NOI18N
                    targetNames = new String [] {"compile-single-jsp"};
                } else {
                    return;
                }
            }
        } else {
            p = null;
            if (targetNames == null) {
                throw new IllegalArgumentException(command);
            }
        }

        try {
            ActionUtils.runTarget(findBuildXml(), targetNames, p);
        } 
        catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
        
    // PENDING - should not this be in some kind of an API?
    private boolean decodeBoolean(String raw) {
        if ( raw != null ) {
           String lowecaseRaw = raw.toLowerCase();
               
           if ( lowecaseRaw.equals( "true") || // NOI18N
                lowecaseRaw.equals( "yes") || // NOI18N
                lowecaseRaw.equals( "enabled") ) // NOI18N
               return true;
        }
            
        return false;
    }    
    
    public File getBuiltJsp(FileObject jsp) {
        ProjectWebModule pwm = project.getWebModule ();
        FileObject webDir = pwm.getDocumentBase ();
        String relFile = FileUtil.getRelativePath(webDir, jsp).replace('/', File.separatorChar);
        File webBuildDir = pwm.getContentDirectoryAsFile();
        return new File(webBuildDir, relFile);
    }
    
    public String getBuiltJspFileNamesAsPath(FileObject[] files) {
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < files.length; i++) {
            String path = getBuiltJsp(files[i]).getAbsolutePath();
            if (i > 0) {
                b.append(File.pathSeparator);
            }
            b.append(path);
        }
        return b.toString();
    }
    
    // THIS METHOD IS COPIED FROM org.netbeans.modules.java.j2seproject.ui.customizer.MainClassChooser
    private static String getMainMethod (Object obj, String expectedName) {
        if (obj == null || !(obj instanceof SourceCookie)) {
            return null;
        }
        SourceCookie cookie = (SourceCookie) obj;
        // check the main class
        String fullName = null;
        SourceElement source = cookie.getSource ();
        ClassElement[] classes = source.getClasses();
        boolean hasMain = false;
        for (int i = 0; i < classes.length; i++) {
          if (expectedName == null || classes[i].getName().getName().equals (expectedName)) {
            if (classes[i].hasMainMethod()) {
                hasMain = true;
                fullName = classes[i].getName ().getFullName ();
                break;
            }
          }
        }
        if (hasMain) {
            return fullName;
        }
        return null;
    }
    
    // THIS METHOD IS COPIED FROM org.netbeans.modules.java.j2seproject.ui.customizer.MainClassChooser
    /** Checks if given file object contains the main method.
     *
     * @param classFO file object represents java 
     * @return false if parameter is null or doesn't contain SourceCookie
     * or SourceCookie doesn't contain the main method
     */    
    public static boolean hasMainMethod (FileObject classFO) {
        if (classFO == null) {
            return false;
        }
        try {
            DataObject classDO = DataObject.find (classFO);
            return getMainMethod (classDO.getCookie (SourceCookie.class), null) != null;
        } catch (DataObjectNotFoundException ex) {
            // can ignore it, classFO could be wrongly set
            return false;
        }
        
    }

    public boolean isActionEnabled( String command, Lookup context ) {
        
        if ( findBuildXml() == null ) {
            return false;
        }
        if ( command.equals( COMMAND_DEBUG_SINGLE ) ) {
            return findJavaSources(context) != null || findJsps(context) != null;
        }
        if ( command.equals( COMMAND_COMPILE_SINGLE ) ) {
            return findJavaSources( context ) != null || findJsps (context) != null;
        }
        if ( command.equals( COMMAND_RUN_SINGLE ) ) {
            // test for jsps
            FileObject files [] = findJsps (context);
            if (files != null && files.length >0) return true;
            // test for html pages
            files = findHtml(context);
            if (files != null && files.length >0) return true;
            // test for servlets
            FileObject[] javaFiles = findJavaSources(context);
            if (javaFiles!=null && javaFiles.length > 0) {
                if (javaFiles[0].getAttribute(SetExecutionUriAction.ATTR_EXECUTION_URI)!=null)
                    return true;
                else if (Boolean.TRUE.equals(javaFiles[0].getAttribute("org.netbeans.modules.web.IsServletFile"))) //NOI18N
                    return true;
                else if (isDDServlet(context, javaFiles[0])) {
                    try {
                        javaFiles[0].setAttribute("org.netbeans.modules.web.IsServletFile",Boolean.TRUE); //NOI18N
                    } catch (IOException ex){}
                    return true;
                } else return true; /* because of java main classes, otherwise we would return false */
            } else return false;
        }
        else {
            // other actions are global
            return true;
        }

        
    }
    
    // Private methods -----------------------------------------------------
    
    /*
     * copied from ActionUtils and reworked so that it checks for mimeType of files, and DOES NOT include files with suffix 'suffix'
     */
    private static FileObject[] findSelectedFilesByMimeType(Lookup context, FileObject dir, String mimeType, String suffix, boolean strict) {
        if (dir != null && !dir.isFolder()) {
            throw new IllegalArgumentException("Not a folder: " + dir); // NOI18N
        }
        List/*<FileObject>*/ files = new ArrayList();
        Iterator it = context.lookup(new Lookup.Template(DataObject.class)).allInstances().iterator();
        while (it.hasNext()) {
            DataObject d = (DataObject)it.next();
            FileObject f = d.getPrimaryFile();
            boolean matches = FileUtil.toFile(f) != null;
            if (dir != null) {
                matches &= (FileUtil.isParentOf(dir, f) || dir == f);
            }
            if (mimeType != null) {
                matches &= f.getMIMEType().equals(mimeType);
            }
            if (suffix != null) {
                matches &= !f.getNameExt().endsWith(suffix);
            }
            // Generally only files from one project will make sense.
            // Currently the action UI infrastructure (PlaceHolderAction)
            // checks for that itself. Should there be another check here?
            if (matches) {
                files.add(f);
            } else if (strict) {
                return null;
            }
        }
        if (files.isEmpty()) {
            return null;
        }
        return (FileObject[])files.toArray(new FileObject[files.size()]);
    }
    
    
    private static final Pattern SRCDIRJAVA = Pattern.compile("\\.java$"); // NOI18N
    
    /** Find selected java sources 
     */
    private FileObject[] findJavaSources(Lookup context) {
        FileObject srcDir = project.getSourceDirectory ();
        FileObject[] files = null;
        if (srcDir != null) {
            files = ActionUtils.findSelectedFiles(context, srcDir, ".java", true);
        }
        return files;
    }
    
    private FileObject[] findHtml(Lookup context) {
        FileObject webDir = project.getWebModule ().getDocumentBase ();
        FileObject[] files = null;
        if (webDir != null) {
            files = findSelectedFilesByMimeType(context, webDir, "text/html", null, true);
        }
        return files;
    }
    
    /** Find selected jsps
     */
    private FileObject[] findJsps(Lookup context) {
        FileObject webDir = project.getWebModule ().getDocumentBase ();
        FileObject[] files = null;
        if (webDir != null) {
            files = findSelectedFilesByMimeType(context, webDir, "text/x-jsp", ".jspf", true);
        }
        return files;
    }
    private boolean isDebugged() {
        
        J2eeModuleProvider jmp = (J2eeModuleProvider)project.getLookup().lookup(J2eeModuleProvider.class);
        ServerDebugInfo sdi = jmp.getServerDebugInfo ();
        if (sdi == null) {
            return false;
        }
//        server.getServerInstance().getStartServer().getDebugInfo(null);
        Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
        
        for (int i=0; i < sessions.length; i++) {
            Session s = sessions[i];
            if (s != null) {
                Object o = s.lookupFirst(null, AttachingDICookie.class);
                if (o != null) {
                    AttachingDICookie attCookie = (AttachingDICookie)o;
                    if (sdi.getTransport().equals(ServerDebugInfo.TRANSPORT_SHMEM)) {
                        if (attCookie.getSharedMemoryName().equalsIgnoreCase(sdi.getShmemName())) {
                            return true;
                        }
                    } else {
                        if (attCookie.getHostName().equalsIgnoreCase(sdi.getHost())) {
                            if (attCookie.getPortNumber() == sdi.getPort()) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    
    private boolean isSelectedServer () {
        String instance = antProjectHelper.getStandardPropertyEvaluator ().getProperty (WebProjectProperties.J2EE_SERVER_INSTANCE);
        boolean selected;
        if (instance != null) {
            J2eeModuleProvider jmp = (J2eeModuleProvider)project.getLookup().lookup(J2eeModuleProvider.class);
            String sdi = jmp.getServerInstanceID();
            if (sdi != null) {
                String id = Deployment.getDefault().getServerID(sdi);
                if (id != null) {
                    return true;
                }
            }
        }
            
        // no selected server => warning
        String server = antProjectHelper.getStandardPropertyEvaluator ().getProperty (WebProjectProperties.J2EE_SERVER_TYPE);
        NoSelectedServerWarning panel = new NoSelectedServerWarning (server);

        Object[] options = new Object[] {
            DialogDescriptor.OK_OPTION,
            DialogDescriptor.CANCEL_OPTION
        };
        DialogDescriptor desc = new DialogDescriptor (panel,
                NbBundle.getMessage (NoSelectedServerWarning.class, "CTL_NoSelectedServerWarning_Title"), // NOI18N
            true, options, options[0], DialogDescriptor.DEFAULT_ALIGN, null, null);
        Dialog dlg = DialogDisplayer.getDefault ().createDialog (desc);
        dlg.setVisible (true);
        if (desc.getValue() != options[0]) {
            selected = false;
        } else {
            instance = panel.getSelectedInstance ();
            selected = instance != null;
            if (selected) {
                WebProjectProperties wpp = new WebProjectProperties (project, antProjectHelper, refHelper);
                wpp.put (WebProjectProperties.J2EE_SERVER_INSTANCE, instance);
                wpp.store ();
            }
        }
        dlg.dispose();            

        return selected;
    }
    
    private boolean isDDServlet(Lookup context, FileObject javaClass) {
        FileObject webDir = project.getWebModule ().getDocumentBase ();
        if (webDir==null) return false;
        FileObject fo = webDir.getFileObject("WEB-INF/web.xml"); //NOI18N
        ClassPath classPath = ClassPath.getClassPath(project.getSourceDirectory (),ClassPath.SOURCE);
        String className = classPath.getResourceName(javaClass,'.',false);
        if (fo==null) return false;
        try {
            WebApp webApp = DDProvider.getDefault().getDDRoot(fo);
            Servlet servlet = (Servlet)webApp.findBeanByName("Servlet","ServletClass",className); //NOI18N
            if (servlet!=null) return true;
            else return false;
        } catch (IOException ex) {return false;}  
    }   
}
