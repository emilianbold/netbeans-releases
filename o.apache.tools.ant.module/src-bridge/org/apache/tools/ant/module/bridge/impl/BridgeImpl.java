/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.apache.tools.ant.module.bridge.impl;

import java.io.*;
import java.util.*;
import org.apache.tools.ant.*;
import org.apache.tools.ant.input.InputHandler;

import org.apache.tools.ant.module.AntModule;
import org.apache.tools.ant.module.AntSettings;
import org.apache.tools.ant.module.api.IntrospectedInfo;
import org.apache.tools.ant.module.bridge.*;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.*;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Implements the BridgeInterface using the current version of Ant.
 * @author Jesse Glick
 */
public class BridgeImpl implements BridgeInterface {
    
    public BridgeImpl() {
    }
    
    public String getAntVersion() {
        try {
            return Main.getAntVersion();
        } catch (BuildException be) {
            AntModule.err.notify(ErrorManager.INFORMATIONAL, be);
            return NbBundle.getMessage(BridgeImpl.class, "LBL_ant_version_unknown");
        }
    }
    
    public IntrospectionHelperProxy getIntrospectionHelper(Class clazz) {
        return new IntrospectionHelperImpl(clazz);
    }
    
    public boolean toBoolean(String val) {
        return Project.toBoolean(val);
    }
    
    public String[] getEnumeratedValues(Class c) {
        if (EnumeratedAttribute.class.isAssignableFrom(c)) {
            try {
                return ((EnumeratedAttribute)c.newInstance()).getValues();
            } catch (Exception e) {
                AntModule.err.notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return null;
    }
    
    public boolean run(File buildFile, final FileObject buildFileObject, List targets, PrintStream out, PrintStream err, Properties properties, int verbosity, boolean useStatusLine) {
        boolean ok = false;
        
        // Make sure "main Ant loader" is used as context loader for duration of the
        // run. Otherwise some code, e.g. JAXP, will accidentally pick up NB classes,
        // which can cause various undesirable effects.
        ClassLoader oldCCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(Project.class.getClassLoader());
        try {
        
        Project project = null;
        
        // first use the ProjectHelper to create the project object
        // from the given build file.
        BuildLogger logger;
        try {
            project = new Project();
            project.init();
            Map customDefs = AntBridge.getCustomDefs();
            Iterator defs = ((Map)customDefs.get("task")).entrySet().iterator(); // NOI18N
            while (defs.hasNext()) {
                Map.Entry entry = (Map.Entry)defs.next();
                project.addTaskDefinition((String)entry.getKey(), (Class)entry.getValue());
            }
            defs = ((Map)customDefs.get("type")).entrySet().iterator(); // NOI18N
            while (defs.hasNext()) {
                Map.Entry entry = (Map.Entry)defs.next();
                project.addDataTypeDefinition((String)entry.getKey(), (Class)entry.getValue());
            }
            project.setUserProperty("ant.file", buildFile.getAbsolutePath()); // NOI18N
            // #14993:
            project.setUserProperty("ant.version", Main.getAntVersion()); // NOI18N
            project.setUserProperty("ant.home", AntSettings.getDefault().getAntHome().getAbsolutePath()); // NOI18N
            Iterator it = properties.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                project.setUserProperty((String) entry.getKey(), (String) entry.getValue());
            }
            logger = new NbBuildLogger(useStatusLine);
            logger.setMessageOutputLevel(verbosity);
            logger.setOutputPrintStream(out);
            logger.setErrorPrintStream(err);
            //writer.println("#2"); // NOI18N
            project.addBuildListener(logger);
            ProjectHelper.configureProject(project, buildFile);
            //writer.println("#3"); // NOI18N
            
            String inputHandlerName = AntSettings.getDefault().getInputHandler();
            InputHandler inputHandler = null;
            if (inputHandlerName != null && inputHandlerName.length() > 0) {
                try {
                    ClassLoader l = AntBridge.createUserClassLoader(buildFileObject);
                    Class clazz = Class.forName(inputHandlerName, true, l);
                    inputHandler = (InputHandler)clazz.newInstance();
                } catch (Exception ex) {
                    throw new BuildException(NbBundle.getMessage(BridgeImpl.class, "MSG_input_handler_exception", inputHandlerName), ex); // NOI18N
                }
            }
            if (inputHandler == null) {
                inputHandler = new NbInputHandler();
            }
            project.setInputHandler(inputHandler);
        }
        catch (BuildException be) {
            // Write errors to the output window, since
            // alot of errors could be annoying as dialogs
            if (verbosity >= Project.MSG_VERBOSE) {
                be.printStackTrace(err);
            } else {
                err.println(be);
            }
            return false;
        }
        
        // Interesting fact: Project.build{Started,Finished} is protected!
        // So it must be fired directly on the listener. Poor API design IMHO.
        logger.buildStarted(new BuildEvent(project));
        
        // Save & restore system output streams.
        PrintStream sysout = System.out;
        PrintStream syserr = System.err;
        System.setOut(new PrintStream(new DemuxOutputStream(project, false)));
        System.setErr(new PrintStream(new DemuxOutputStream(project, true)));

        try {
            // Execute the configured project
            //writer.println("#4"); // NOI18N
            Vector targs;
            if (targets != null) {
                targs = new Vector(targets);
            } else {
                targs = new Vector(1);
                targs.add(project.getDefaultTarget());
            }
            project.executeTargets(targs);
            //writer.println("#5"); // NOI18N
            logger.buildFinished(new BuildEvent(project));
            ok = true;
        } catch (ThreadDeath td) {
            if (useStatusLine) {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(BridgeImpl.class, "MSG_target_failed_status"));
            }
            // don't throw ThreadDeath, just return. ThreadDeath sometimes
            // generated when killing process in Execution Window
            //throw td;
        } catch (Exception e) {
            BuildEvent ev = new BuildEvent(project);
            ev.setException(e);
            logger.buildFinished(ev);
        } catch (LinkageError e) {
            BuildEvent ev = new BuildEvent(project);
            ev.setException(e);
            logger.buildFinished(ev);
        } finally {
            System.setOut(sysout);
            System.setErr(syserr);
        }
        
        // Now check to see if the Project defined any cool new custom tasks.
        final Project p2 = project;
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                IntrospectedInfo custom = AntSettings.getDefault().getCustomDefs();
                Map defs = new HashMap(); // Map<String,Map<String,Class>>
                defs.put("task", p2.getTaskDefinitions());
                defs.put("type", p2.getDataTypeDefinitions());
                custom.scanProject(defs);
                // #8993: also try to refresh FS that script was on...
                if (buildFileObject != null) {
                    try {
                        FileSystem fs = buildFileObject.getFileSystem();
                        fs.refresh(false);
                    } catch (FileStateInvalidException e) {
                        AntModule.err.notify(ErrorManager.WARNING, e);
                    }
                }
            }
        }, 1000); // a bit later; the target can finish first!
        
        } finally {
            Thread.currentThread().setContextClassLoader(oldCCL);
        }
        
        return ok;
    }
    
}
