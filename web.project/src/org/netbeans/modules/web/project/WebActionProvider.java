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

import java.io.IOException;
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
import org.openide.loaders.ExecutionSupport;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.netbeans.modules.j2ee.deployment.impl.projects.*;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.netbeans.modules.j2ee.deployment.execution.*;


/** Action provider of the Web project. This is the place where to do
 * strange things to Web actions. E.g. compile-single.
 */
class WebActionProvider implements ActionProvider {
    
    // Definition of commands
    private static final String COMMAND_COMPILE_SINGLE = "compile.single"; /*XXX define somewhere*/ // NOI18N
    private static final String COMMAND_RUN = "run"; /*XXX define somewhere*/ // NOI18N
    private static final String COMMAND_DEBUG = "debug"; /*XXX define somewhere*/ // NOI18N
    private static final String COMMAND_JAVADOC = "javadoc"; /*XXX define somewhere*/ // NOI18N
    private static final String COMMAND_DEBUG_FIX = "debug.fix"; /*XXX define somewhere*/ // NOI18N
    private static final String COMMAND_COMPILE = "compile"; //NOI18N
        
    // Commands available from Web project
    private static final String[] supportedActions = {
        COMMAND_BUILD, 
        COMMAND_CLEAN, 
        COMMAND_REBUILD, 
        COMMAND_COMPILE_SINGLE, 
        COMMAND_RUN, 
        COMMAND_DEBUG, 
        COMMAND_JAVADOC, 
        COMMAND_DEBUG_FIX,
        COMMAND_COMPILE,
    };
    
    // Project
    WebProject project;
    
    // Ant project helper of the project
    private AntProjectHelper antProjectHelper;
        
    /** Map from commands to ant targets */
    Map/*<String,String[]>*/ commands;
    
    public WebActionProvider(WebProject project, AntProjectHelper antProjectHelper) {
        
        commands = new HashMap();
            commands.put(COMMAND_BUILD, new String[] {"jar"}); // NOI18N
            commands.put(COMMAND_CLEAN, new String[] {"clean"}); // NOI18N
            commands.put(COMMAND_REBUILD, new String[] {"clean", "jar"}); // NOI18N
            commands.put(COMMAND_COMPILE_SINGLE, new String[] {"compile-single"}); // NOI18N
            commands.put(COMMAND_RUN, new String[] {"run"}); // NOI18N
            commands.put(COMMAND_DEBUG, new String[] {"debug"}); // NOI18N
            commands.put(COMMAND_JAVADOC, new String[] {"javadoc"}); // NOI18N
            commands.put(COMMAND_DEBUG_FIX, new String[] {"debug-fix"}); // NOI18N
            commands.put(COMMAND_COMPILE, new String[] {"compile"}); // NOI18N
        
        this.antProjectHelper = antProjectHelper;
        this.project = project;
    }
    
    private FileObject findBuildXml() {
        return project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
    }
    
    public String[] getSupportedActions() {
        return supportedActions;
    }
    
    public void invokeAction( String command, Lookup context ) throws IllegalArgumentException {
        Properties p;
        String[] targetNames = (String[])commands.get(command);
        
        if (command.equals (COMMAND_RUN)) {             
            p = new Properties();
            p.setProperty("client.urlPart", project.getWebModule().getUrl());
        } else if (command.equals (COMMAND_DEBUG)) {
            p = new Properties();
            p.setProperty("client.urlPart", project.getWebModule().getUrl());
        } else if ( command.equals( COMMAND_COMPILE_SINGLE ) ) {
            FileObject[] files = findSources( context );
            p = new Properties();
            if (files != null) {
                p.setProperty("javac.includes", ActionUtils.antIncludesList(files, project.getSourceDirectory())); // NOI18N
            } else {
                return;
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
    
    public boolean isActionEnabled( String command, Lookup context ) {
        
        if ( findBuildXml() == null ) {
            return false;
        }
        if ( command.equals( COMMAND_COMPILE_SINGLE ) ) {
            return findSources( context ) != null;
        }
        else {
            // other actions are global
            return true;
        }

        
    }
    
    // Private methods -----------------------------------------------------
    
    
    private static final Pattern SRCDIRJAVA = Pattern.compile("\\.java$"); // NOI18N
    
    /** Find selected sources 
     */
    //PENDING BUILDSYS - ignore JspServletDataObject for compilation
    private FileObject[] findSources(Lookup context) {
        FileObject srcDir = project.getSourceDirectory();
        if (srcDir != null) {
            FileObject[] files = ActionUtils.findSelectedFiles(context, srcDir, ".java", true);
            return files;
        } else {
            return null;
        }
    }
    
//    private FileObject[] findJSPs(Lookup context) {
//        FileObject webDir = project.getWebModule ().getDocumentBase ();
//        if (webDir != null) {
//            FileObject[] files = ActionHelper.findSelectedFiles(context, webDir, ".jsp", true);
//            return files;
//        } else {
//            return null;
//        }
//    }
        
}
