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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.netbeans.api.debugger.*;
import org.netbeans.api.debugger.jpda.*;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.modules.j2ee.deployment.devmodules.api.*;
import org.netbeans.modules.web.api.webmodule.URLCookie;
import org.netbeans.modules.web.project.ui.NoSelectedServerWarning;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.modules.web.project.ui.ServletUriPanel;
import org.netbeans.modules.web.project.ui.SetExecutionUriAction;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.web.project.parser.ParserWebModule;
import org.netbeans.modules.web.project.parser.JspNameUtil;

import org.netbeans.api.web.dd.DDProvider;
import org.netbeans.api.web.dd.WebApp;
import org.netbeans.api.web.dd.Servlet;
import org.netbeans.api.java.classpath.ClassPath;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.netbeans.modules.web.api.webmodule.WebModule;

import org.netbeans.modules.web.api.webmodule.WebProjectConstants;

import org.netbeans.modules.javacore.JMManager;
import org.netbeans.jmi.javamodel.*;
import org.netbeans.modules.javacore.internalapi.JavaMetamodel;
import java.lang.reflect.Modifier;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.jsps.parserapi.JspParserFactory;

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
        COMMAND_TEST, 
        COMMAND_TEST_SINGLE, 
        COMMAND_DEBUG_TEST_SINGLE, 
        JavaProjectConstants.COMMAND_DEBUG_FIX,
        COMMAND_COMPILE,
    };
    
    // Project
    WebProject project;
    
    // Ant project helper of the project
    private UpdateHelper updateHelper;

    // Ant project helper of the project
//    private AntProjectHelper antProjectHelper;
//    private ReferenceHelper refHelper;
        
    /** Map from commands to ant targets */
    Map/*<String,String[]>*/ commands;
    
    public WebActionProvider(WebProject project, UpdateHelper updateHelper) {
        
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
            // the target name is debug, except for Java files with main method, where it is debug-single-main
            commands.put(COMMAND_DEBUG_SINGLE, new String[] {"debug"}); // NOI18N
            commands.put(JavaProjectConstants.COMMAND_JAVADOC, new String[] {"javadoc"}); // NOI18N
            commands.put(COMMAND_TEST, new String[] {"test"}); // NOI18N
            commands.put(COMMAND_TEST_SINGLE, new String[] {"test-single"}); // NOI18N
            commands.put(COMMAND_DEBUG_TEST_SINGLE, new String[] {"debug-test"}); // NOI18N
            commands.put(JavaProjectConstants.COMMAND_DEBUG_FIX, new String[] {"debug-fix"}); // NOI18N
            commands.put(COMMAND_COMPILE, new String[] {"compile"}); // NOI18N
        
        this.updateHelper = updateHelper;
        this.project = project;
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
                        String raw = updateHelper.getAntProjectHelper().getStandardPropertyEvaluator ().getProperty (WebProjectProperties.COMPILE_JSPS);
                        boolean compile = decodeBoolean(raw);
                        if (!compile) {
                            setAllPropertiesForSingleJSPCompilation(p, files);
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
//                p.setProperty("client.urlPart", project.getWebModule().getUrl());
            } else { //COMMAND_DEBUG_SINGLE
                FileObject[] files = findJsps( context );
                if ((files != null) && (files.length>0)) {
                    // debug jsp
                    try {
                        // possibly compile the JSP, if we are not compiling all of them
                        String raw = updateHelper.getAntProjectHelper().getStandardPropertyEvaluator ().getProperty (WebProjectProperties.COMPILE_JSPS);
                        boolean compile = decodeBoolean(raw);
                        if (!compile) {
                            setAllPropertiesForSingleJSPCompilation(p, files);
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
                } else {
                    // debug HTML file
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
                        // debug Java
                        // debug servlet
                        FileObject[] javaFiles = findJavaSources(context);
                        if ((javaFiles != null) && (javaFiles.length>0)) {
                            FileObject javaFile = javaFiles[0];

                            if (hasMainMethod(javaFile)) {
                                // debug Java with Main method
                                String clazz = FileUtil.getRelativePath(project.getSourceDirectory(), javaFile);
                                p = new Properties();
                                p.setProperty("javac.includes", clazz); // NOI18N
                                // Convert foo/FooTest.java -> foo.FooTest
                                if (clazz.endsWith(".java")) { // NOI18N
                                    clazz = clazz.substring(0, clazz.length() - 5);
                                }
                                clazz = clazz.replace('/','.');

                                p.setProperty("debug.class", clazz); // NOI18N
                                targetNames = new String [] {"debug-single-main"};
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
        } else if (command.equals(JavaProjectConstants.COMMAND_DEBUG_FIX)) {
            FileObject[] files = findJavaSources(context);
            String path = null;
            p = new Properties();
            if (files != null) {
                path = FileUtil.getRelativePath(project.getSourceDirectory(), files[0]);
                targetNames = new String[] {"debug-fix"}; // NOI18N
            } else {
                return;
            }
            // Convert foo/FooTest.java -> foo/FooTest
            if (path.endsWith(".java")) { // NOI18N
                path = path.substring(0, path.length() - 5);
            }
            p.setProperty("fix.includes", path); // NOI18N
            
        //COMPILATION PART
        } else if ( command.equals( COMMAND_COMPILE_SINGLE ) ) {
            FileObject[] files = findJavaSourcesAndPackages( context );
            p = new Properties();
            if (files != null) {
                p.setProperty("javac.includes", ActionUtils.antIncludesList(files, project.getSourceDirectory())); // NOI18N
            } else {
                files = findJsps (context);
                if (files != null) {
                    for (int i=0; i < files.length; i++) {
                        FileObject jsp = files[i];
                        if (areIncludesModified(jsp)) {
                            invalidateClassFile(project, jsp);
                        }
                    }
                    setAllPropertiesForSingleJSPCompilation(p, files);
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

    /* Deletes translated class/java file to force recompilation of the page with all includes
     */
    public void invalidateClassFile(WebProject wp, FileObject jsp) {
        String dir = updateHelper.getAntProjectHelper().getStandardPropertyEvaluator ().getProperty (WebProjectProperties.BUILD_GENERATED_DIR);
        if (dir == null) {
            return;
        }
        dir = dir + "/src"; //NOI18N
        WebModule wm = WebModule.getWebModule(jsp);
        if (wm == null) {
            return;
        }
        String name = JspNameUtil.getServletName(wm.getDocumentBase(), jsp);
        if (name == null) {
            return;
        }
        String filePath = name.substring(0, name.lastIndexOf('.')).replace('.', '/');
        
        String fileClass = dir + '/' + filePath + ".class"; //NOI18N
        String fileJava = dir + '/' + filePath + ".java"; //NOI18N
        
        File fC = updateHelper.getAntProjectHelper().resolveFile(fileClass);
        File fJ = updateHelper.getAntProjectHelper().resolveFile(fileJava);
        if ((fJ != null) && (fJ.exists())) {
            fJ.delete();
        }
        if ((fC != null) && (fC.exists())) {
            fC.delete();
        }
    }
    
    /* checks if timestamp of any of the included pages is higher than the top page
     */
    public boolean areIncludesModified(FileObject jsp){
        boolean modified = false;
        WebModule wm = WebModule.getWebModule(jsp);
        JspParserAPI jspParser = JspParserFactory.getJspParser();
        JspParserAPI.ParseResult result = jspParser.analyzePage(jsp, new ParserWebModule(wm), JspParserAPI.ERROR_IGNORE);
        if (!result.isParsingSuccess()) {
            modified = true;
        } else {
            List includes = result.getPageInfo().getDependants();
            if ((includes != null) && (includes.size() > 0)) {
                long jspTS = jsp.lastModified().getTime();
                int size = includes.size();
                for (int i=0; i<size; i++) {
                    String filename = (String)includes.get(i);
                    filename = FileUtil.toFile(wm.getDocumentBase()).getPath() + filename;
                    File f = new File(filename);
                    if (f != null) {
                        long incTS = f.lastModified();
                        if (incTS > jspTS) {
                            modified = true;
                            break;
                        }
                    }
                }
            }
        }
        return modified;
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
    
    private void setAllPropertiesForSingleJSPCompilation(Properties p, FileObject[] files) {
        p.setProperty("jsp.includes", getBuiltJspFileNamesAsPath(files)); // NOI18N
         /*ActionUtils.antIncludesList(files, project.getWebModule ().getDocumentBase ())*/
        
        p.setProperty("javac.jsp.includes", getCommaSeparatedGeneratedJavaFiles(files)); // NOI18N
        
    }
    
    public String getCommaSeparatedGeneratedJavaFiles(FileObject[] jspFiles) {
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < jspFiles.length; i++) {
            String jspRes = getJspResource(jspFiles[i]);
            if (i > 0) {
                b.append(',');
            }
            b.append(Utils.getGeneratedJavaResource(jspRes));
        }
        return b.toString();
    }
    
    /** Returns a resource name for a given JSP separated by / (does not start with a /).
     */
    private String getJspResource(FileObject jsp) {
        ProjectWebModule pwm = project.getWebModule ();
        FileObject webDir = pwm.getDocumentBase ();
        return FileUtil.getRelativePath(webDir, jsp);
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
    
    
    // THIS METHOD IS (almost) COPIED FROM org.netbeans.modules.java.j2seproject.J2SEProjectUtil
    /** Checks if given file object contains the main method.
     *
     * @param classFO file object represents java 
     * @return false if parameter is null or doesn't contain SourceCookie
     * or SourceCookie doesn't contain the main method
     */    
    final public static boolean hasMainMethod (FileObject fo) {
        // support for unit testing
        /*if (MainClassChooser.unitTestingSupport_hasMainMethodResult != null) {
            return MainClassChooser.unitTestingSupport_hasMainMethodResult.booleanValue ();
        }
        */
        if (fo == null) {
            // ??? maybe better should be thrown IAE
            return false;
        }
        Resource res = JavaMetamodel.getManager ().getResource (fo);
        assert res != null : "Resource found for FileObject " + fo;
        return hasMainMethod (res);
    }
    
    // copied from JavaNode.hasMain
    private static boolean hasMainMethod (Resource res) {
        if (res != null && res.containsIdentifier ("main")) { //NOI18N
            for (Iterator i = res.getClassifiers ().iterator (); i.hasNext (); ) {
                JavaClass clazz = (JavaClass) i.next ();
                // now it is only important top-level class with the same 
                // name as file. Continue if the file name differs
                // from top level class name.
                if (!clazz.getSimpleName ().equals (((JMManager)JMManager.getManager ()).getFileObject (res).getName ()))
                    continue;

                for (Iterator j = clazz.getFeatures ().iterator(); j.hasNext ();) {
                    Object o = j.next ();
                    // if it is not a method, continue with next feature
                    if (!(o instanceof Method))
                        continue;

                    Method m = (Method) o;
                    int correctMods = (Modifier.PUBLIC | Modifier.STATIC);
                    // check that method is named 'main' and has set public 
                    // and static modifiers! Method has to also return
                    // void type.
                    if (!"main".equals (m.getName()) || // NOI18N
                       ((m.getModifiers () & correctMods) != correctMods) ||
                       (!"void".equals (m.getType().getName ())))
                       continue;

                    // check parameters - it has to be one of type String[]
                    // or String...
                    if (m.getParameters ().size ()==1) {
                        Parameter par = ((Parameter) m.getParameters ().get (0));
                        String typeName = par.getType ().getName ();
                        if (par.isVarArg () && ("java.lang.String".equals (typeName) || "String".equals (typeName))) { // NOI18N
                            // Main methods written with variable arguments parameter:
                            // public static main(String... args) {
                            // }
                            return true; 
                        } else if (typeName.equals ("String[]") || typeName.equals ("java.lang.String[]")) { // NOI18N
                            // Main method written with array parameter:
                            // public static main(String[] args) {
                            // }
                            return true;
                        }

                    } // end if parameters
                } // end features cycle
            }
        }
        return false;
    }
    
    public boolean isActionEnabled( String command, Lookup context ) {
        
        if ( findBuildXml() == null ) {
            return false;
        }
        if ( command.equals( COMMAND_DEBUG_SINGLE ) ) {
            return findJavaSources(context) != null || findJsps(context) != null || findHtml(context) != null;
        }
        if ( command.equals( COMMAND_COMPILE_SINGLE ) ) {
            return findJavaSourcesAndPackages( context ) != null || findJsps (context) != null;
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
    
    private FileObject[] findJavaSourcesAndPackages (Lookup context) {
        FileObject srcDir = project.getSourceDirectory();
        if (srcDir != null) {
            FileObject[] files = ActionUtils.findSelectedFiles(context, srcDir, null, true); // NOI18N
            //Check if files are either packages of java files
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    if (!files[i].isFolder() && !"java".equals(files[i].getExt())) {
                        return null;
                    }
                }
            }
            return files;
        } else {
            return null;
        }
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
                        String shmem = attCookie.getSharedMemoryName();
                        if (shmem == null) {
                            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Shared memory name is null.");
                            return false;
                        }
                        if (shmem.equalsIgnoreCase(sdi.getShmemName())) {
                            return true;
                        }
                    } else {
                        String hostname = attCookie.getHostName();
                        if (hostname == null) {
                            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Hostname is null.");
                            return false;
                        }
                        if (hostname.equalsIgnoreCase(sdi.getHost())) {
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
        String instance = updateHelper.getAntProjectHelper().getStandardPropertyEvaluator ().getProperty (WebProjectProperties.J2EE_SERVER_INSTANCE);
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
        
        // try to use the default server instance
        instance = Deployment.getDefault().getDefaultServerInstanceID();
        if (instance != null) {
            setServerInstance(instance);
            return true;
        }
        
        // no selected server => warning
        String server = updateHelper.getAntProjectHelper().getStandardPropertyEvaluator ().getProperty (WebProjectProperties.J2EE_SERVER_TYPE);
        NoSelectedServerWarning panel = new NoSelectedServerWarning (server);

        Object[] options = new Object[] {
            DialogDescriptor.OK_OPTION,
            DialogDescriptor.CANCEL_OPTION
        };
        final DialogDescriptor desc = new DialogDescriptor (panel,
                NbBundle.getMessage (NoSelectedServerWarning.class, "CTL_NoSelectedServerWarning_Title"), // NOI18N
            true, options, options[0], DialogDescriptor.DEFAULT_ALIGN, null, null);
        Dialog dlg = DialogDisplayer.getDefault ().createDialog (desc);
        panel.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals(NoSelectedServerWarning.OK_ENABLED)) {
                        Object newvalue = evt.getNewValue();
                        if ((newvalue != null) && (newvalue instanceof Boolean)) {
                            desc.setValid(((Boolean)newvalue).booleanValue());
                        }
                    }
                }
            }
        );
        desc.setValid(panel.getSelectedInstance() != null);
        dlg.setVisible (true);
        if (desc.getValue() != options[0]) {
            selected = false;
        } else {
            instance = panel.getSelectedInstance ();
            selected = instance != null;
            if (selected) {
                setServerInstance(instance);
            }
        }
        dlg.dispose();            

        return selected;
    }
    
    private void setServerInstance(String serverInstanceId) {
        WebProjectProperties wpp = new WebProjectProperties (project, updateHelper, project.evaluator(), project.getReferenceHelper());
        wpp.put (WebProjectProperties.J2EE_SERVER_INSTANCE, serverInstanceId);
        wpp.store ();
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
