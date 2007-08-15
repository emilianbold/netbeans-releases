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

package org.apache.tools.ant.module.bridge.impl;

import java.beans.Introspector;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DemuxOutputStream;
import org.apache.tools.ant.IntrospectionHelper;
import org.apache.tools.ant.Main;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.module.AntModule;
import org.apache.tools.ant.module.AntSettings;
import org.apache.tools.ant.module.api.IntrospectedInfo;
import org.apache.tools.ant.module.bridge.AntBridge;
import org.apache.tools.ant.module.bridge.BridgeInterface;
import org.apache.tools.ant.module.bridge.IntrospectionHelperProxy;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.Path;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;
import org.openide.util.RequestProcessor;
import org.openide.windows.OutputWriter;

/**
 * Implements the BridgeInterface using the current version of Ant.
 * @author Jesse Glick
 */
public class BridgeImpl implements BridgeInterface {
    
    /** Number of milliseconds to wait before forcibly halting a runaway process. */
    private static final int STOP_TIMEOUT = 3000;
    
    private static boolean classpathInitialized = false;
    
    /**
     * Index of loggers by active thread.
     * @see #stop
     */
    private static final Map<Thread,NbBuildLogger> loggersByThread = new WeakHashMap<Thread,NbBuildLogger>();
    
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
    
    public boolean isAnt16() {
        try {
            Class.forName("org.apache.tools.ant.taskdefs.Antlib"); // NOI18N
            return true;
        } catch (ClassNotFoundException e) {
            // Fine, 1.5
            return false;
        }
    }
    
    public IntrospectionHelperProxy getIntrospectionHelper(Class<?> clazz) {
        return new IntrospectionHelperImpl(clazz);
    }
    
    public boolean toBoolean(String val) {
        return Project.toBoolean(val);
    }
    
    public String[] getEnumeratedValues(Class<?> c) {
        if (EnumeratedAttribute.class.isAssignableFrom(c)) {
            try {
                return ((EnumeratedAttribute)c.newInstance()).getValues();
            } catch (Exception e) {
                AntModule.err.notify(ErrorManager.INFORMATIONAL, e);
            }
        } else if (Enum.class.isAssignableFrom(c)) { // Ant 1.7.0 (#41058)
            try {
                Enum<?>[] vals = (Enum<?>[]) c.getMethod("values").invoke(null);
                String[] names = new String[vals.length];
                for (int i = 0; i < vals.length; i++) {
                    names[i] = vals[i].name();
                }
                return names;
            } catch (Exception x) {
                Exceptions.printStackTrace(x);
            }
        }
        return null;
    }
    
    public boolean run(File buildFile, List<String> targets, InputStream in, OutputWriter out, OutputWriter err,
                       Map<String,String> properties, int verbosity, String displayName, Runnable interestingOutputCallback, ProgressHandle handle) {
        if (!classpathInitialized) {
            classpathInitialized = true;
            // #46171: Ant expects this path to have itself and whatever else you loaded with it,
            // or AntClassLoader.getResources will not be able to find anything in the Ant loader.
            Path.systemClasspath = new Path(null, AntBridge.getMainClassPath());
        }
        
        boolean ok = false;
        
        // Important for various other stuff.
        final boolean ant16 = isAnt16();
        
        // Make sure "main Ant loader" is used as context loader for duration of the
        // run. Otherwise some code, e.g. JAXP, will accidentally pick up NB classes,
        // which can cause various undesirable effects.
        ClassLoader oldCCL = Thread.currentThread().getContextClassLoader();
        ClassLoader newCCL = Project.class.getClassLoader();
        if (AntModule.err.isLoggable(ErrorManager.INFORMATIONAL)) {
            AntModule.err.log("Fixing CCL: " + oldCCL + " -> " + newCCL);
        }
        Thread.currentThread().setContextClassLoader(newCCL);
        AntBridge.fakeJavaClassPath();
        try {
        
        final Project project;
        
        // first use the ProjectHelper to create the project object
        // from the given build file.
        final NbBuildLogger logger = new NbBuildLogger(buildFile, out, err, verbosity, displayName, interestingOutputCallback, handle);
        Vector<String> targs;
        try {
            project = new Project();
            project.addBuildListener(logger);
            project.init();
            project.addTaskDefinition("java", ForkedJavaOverride.class); // #56341
            try {
                addCustomDefs(project);
            } catch (IOException e) {
                throw new BuildException(e);
            }
            project.setUserProperty("ant.file", buildFile.getAbsolutePath()); // NOI18N
            // #14993:
            project.setUserProperty("ant.version", Main.getAntVersion()); // NOI18N
            File antHome = AntSettings.getAntHome();
            if (antHome != null) {
                project.setUserProperty("ant.home", antHome.getAbsolutePath()); // NOI18N
            }
            for (Map.Entry<String,String> entry : properties.entrySet()) {
                project.setUserProperty(entry.getKey(), entry.getValue());
            }
            if (in != null && ant16) {
                try {
                    Method m = Project.class.getMethod("setDefaultInputStream", InputStream.class); // NOI18N
                    m.invoke(project, in);
                } catch (Exception e) {
                    AntModule.err.notify(ErrorManager.INFORMATIONAL, e);
                }
            }
            if (AntModule.err.isLoggable(ErrorManager.INFORMATIONAL)) {
                AntModule.err.log("CCL when configureProject is called: " + Thread.currentThread().getContextClassLoader());
            }
            ProjectHelper projhelper = ProjectHelper.getProjectHelper();
            // Cf. Ant #32668 & #32216; ProjectHelper.configureProject undeprecated in 1.7
            project.addReference("ant.projectHelper", projhelper); // NOI18N
            projhelper.parse(project, buildFile);
            
            project.setInputHandler(new NbInputHandler(interestingOutputCallback));
            
            if (targets != null) {
                targs = new Vector<String>(targets);
            } else {
                targs = new Vector<String>(1);
                targs.add(project.getDefaultTarget());
            }
            logger.setActualTargets(targets != null ? targets.toArray(new String[targets.size()]) : null);
        }
        catch (BuildException be) {
            logger.buildInitializationFailed(be);
            logger.shutdown();
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    AntModule.err.notify(e);
                }
            }
            return false;
        }
        
        project.fireBuildStarted();
        
        // Save & restore system output streams.
        InputStream is = System.in;
        if (in != null && ant16) {
            try {
                Class<? extends InputStream> dis = Class.forName("org.apache.tools.ant.DemuxInputStream").asSubclass(InputStream.class); // NOI18N
                Constructor<? extends InputStream> c = dis.getConstructor(Project.class);
                is = c.newInstance(project);
            } catch (Exception e) {
                AntModule.err.notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        AntBridge.pushSystemInOutErr(is,
                                     new PrintStream(new DemuxOutputStream(project, false)),
                                     new PrintStream(new DemuxOutputStream(project, true)));

        Thread currentThread = Thread.currentThread();
        synchronized (loggersByThread) {
            assert !loggersByThread.containsKey(currentThread);
            loggersByThread.put(currentThread, logger);
        }
        try {
            // Execute the configured project
            //writer.println("#4"); // NOI18N
            project.executeTargets(targs);
            //writer.println("#5"); // NOI18N
            project.fireBuildFinished(null);
            ok = true;
        } catch (Throwable t) {
            // Really need to catch everything, else AntClassLoader.cleanup may
            // not be called, resulting in a memory leak and/or locked JARs (#42431).
            project.fireBuildFinished(t);
        } finally {
            AntBridge.restoreSystemInOutErr();
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    AntModule.err.notify(e);
                }
            }
            synchronized (loggersByThread) {
                loggersByThread.remove(currentThread);
            }
        }
        
        // Now check to see if the Project defined any cool new custom tasks.
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                IntrospectedInfo custom = AntSettings.getCustomDefs();
                Map<String,Map<String,Class>> defs = new HashMap<String,Map<String,Class>>();
                defs.put("task", NbCollections.checkedMapByCopy(project.getTaskDefinitions(), String.class, Class.class, true));
                defs.put("type", NbCollections.checkedMapByCopy(project.getDataTypeDefinitions(), String.class, Class.class, true));
                custom.scanProject(defs);
                AntSettings.setCustomDefs(custom);
                logger.shutdown();
                // #85698: do not invoke multiple refreshes at once
                refreshFilesystemsTask.schedule(0);
                gutProject(project);
                if (!ant16) {
                    // #36393 - memory leak in Ant 1.5.
                    RequestProcessor.getDefault().post(new Runnable() {
                        public void run() {
                            hack36393();
                        }
                    });
                }
            }
        });
        
        } finally {
            AntBridge.unfakeJavaClassPath();
            if (AntModule.err.isLoggable(ErrorManager.INFORMATIONAL)) {
                AntModule.err.log("Restoring CCL: " + oldCCL);
            }
            Thread.currentThread().setContextClassLoader(oldCCL);
        }
        
        return ok;
    }

    private static final RequestProcessor.Task refreshFilesystemsTask = RequestProcessor.getDefault().create(new Runnable() {
        public void run() {
            // #8993: also try to refresh masterfs...this is hackish...
            // cf. also RefreshAllFilesystemsAction
            for (FileSystem fs : getFileSystems()) {
                Logger.getLogger(BridgeImpl.class.getName()).log(Level.FINE, "Refreshing filesystem {0}", fs);
                fs.refresh(false);
            }
        }
    });

    public void stop(final Thread process) {
        NbBuildLogger logger;
        synchronized (loggersByThread) {
            logger = loggersByThread.get(process);
        }
        if (logger != null) {
            // Try stopping at a safe point.
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(BridgeImpl.class, "MSG_stopping", logger.getDisplayNameNoLock()));
            logger.stop();
            // But if that doesn't do it, double-check later...
            // Yes Thread.stop() is deprecated; that is why we try to avoid using it.
            RequestProcessor.getDefault().create(new Runnable() {
                public void run () {
                    forciblyStop(process);
                }
            }).schedule(STOP_TIMEOUT);
        } else {
            // Try killing it now!
            forciblyStop(process);
        }
    }
    
    private void forciblyStop(Thread process) {
        if (process.isAlive()) {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(BridgeImpl.class, "MSG_halting"));
            // XXX try using process.interrupt() first, then wait a bit longer
            stopThread(process);
        }
    }

    @SuppressWarnings("deprecation")
    private static void stopThread(Thread process) {
        process.stop();
    }
    
    //copy - paste programming
    //http://ant.netbeans.org/source/browse/ant/src-bridge/org/apache/tools/ant/module/bridge/impl/BridgeImpl.java.diff?r1=1.15&r2=1.16
    //http:/java.netbeans.org/source/browse/java/javacore/src/org/netbeans/modules/javacore/Util.java    
    //http://core.netbeans.org/source/browse/core/ui/src/org/netbeans/core/ui/MenuWarmUpTask.java
    //http://core.netbeans.org/source/browse/core/src/org/netbeans/core/actions/RefreshAllFilesystemsAction.java
    //http://java.netbeans.org/source/browse/java/api/src/org/netbeans/api/java/classpath/ClassPath.java
        
    private static FileSystem[] fileSystems;

    private static FileSystem[] getFileSystems() {
        if (fileSystems != null) {
            return fileSystems;
        }
        File[] roots = File.listRoots();
        Set<FileSystem> allRoots = new LinkedHashSet<FileSystem>();
        assert roots != null && roots.length > 0 : "Could not list file roots"; // NOI18N

        for (File root : roots) {
            FileObject random = FileUtil.toFileObject(root);
            if (random == null) continue;
            
            FileSystem fs;
            try {
                fs = random.getFileSystem();
                allRoots.add(fs);
                
                /*Because there is MasterFileSystem impl. that provides conversion to FileObject for all File.listRoots
                (except floppy drives and empty CD). Then there is useless to convert all roots into FileObjects including
                net drives that might cause performance regression.
                */
                
                if (fs != null) {
                    break;
                }
            } catch (FileStateInvalidException e) {
                throw new AssertionError(e);
            }
        }
        assert !allRoots.isEmpty() : "Could not get any filesystem"; // NOI18N
        return fileSystems = allRoots.toArray(new FileSystem[allRoots.size()]);
    }

    private static void addCustomDefs(Project project) throws BuildException, IOException {
        long start = System.currentTimeMillis();
        if (AntBridge.getInterface().isAnt16()) {
            Map<String,ClassLoader> antlibLoaders = AntBridge.getCustomDefClassLoaders();
            for (Map.Entry<String,ClassLoader> entry : antlibLoaders.entrySet()) {
                String cnb = entry.getKey();
                ClassLoader l = entry.getValue();
                String resource = cnb.replace('.', '/') + "/antlib.xml"; // NOI18N
                URL antlib = l.getResource(resource);
                if (antlib == null) {
                    throw new IOException("Could not find " + antlib + " in ant/nblib/" + cnb.replace('.', '-') + ".jar"); // NOI18N
                }
                // Once with no namespaces.
                NbAntlib.process(project, antlib, null, l);
                // Once with.
                String antlibUri = "antlib:" + cnb; // NOI18N
                NbAntlib.process(project, antlib, antlibUri, l);
            }
        } else {
            // For Ant 1.5, just dump in old-style defs in the simplest manner.
            Map<String,Map<String,Class>> customDefs = AntBridge.getCustomDefsNoNamespace();
            for (Map.Entry<String,Class> entry : customDefs.get("task").entrySet()) { // NOI18N
                project.addTaskDefinition(entry.getKey(), entry.getValue());
            }
            for (Map.Entry<String,Class> entry : customDefs.get("type").entrySet()) { // NOI18N
                project.addDataTypeDefinition(entry.getKey(), entry.getValue());
            }
        }
        if (AntModule.err.isLoggable(ErrorManager.INFORMATIONAL)) {
            AntModule.err.log("addCustomDefs took " + (System.currentTimeMillis() - start) + "msec");
        }
    }
    
    private static boolean doHack36393 = true;
    /**
     * Remove any outstanding ProcessDestroyer shutdown hooks.
     * They should not be left in the JRE static area.
     * Workaround for bug in Ant 1.5.x, fixed already in Ant 1.6.
     */
    private static void hack36393() {
        if (!doHack36393) {
            // Failed last time, skip this time.
            return;
        }
        try {
            Class shutdownC = Class.forName("java.lang.Shutdown"); // NOI18N
            Class wrappedHookC = Class.forName("java.lang.Shutdown$WrappedHook"); // NOI18N
            Field hooksF = shutdownC.getDeclaredField("hooks"); // NOI18N
            hooksF.setAccessible(true);
            Field hookF = wrappedHookC.getDeclaredField("hook"); // NOI18N
            hookF.setAccessible(true);
            Field lockF = shutdownC.getDeclaredField("lock"); // NOI18N
            lockF.setAccessible(true);
            Object lock = lockF.get(null);
            Set<Thread> toRemove = new HashSet<Thread>();
            synchronized (lock) {
                @SuppressWarnings("unchecked")
                Set<Object> hooks = (Set) hooksF.get(null);
                for (Object wrappedHook : hooks) {
                    Thread hook = (Thread)hookF.get(wrappedHook);
                    if (hook.getClass().getName().equals("org.apache.tools.ant.taskdefs.ProcessDestroyer")) { // NOI18N
                        // Don't remove it now - will get ConcurrentModificationException.
                        toRemove.add(hook);
                    }
                }
            }
            for (Thread hook : toRemove) {
                if (!Runtime.getRuntime().removeShutdownHook(hook)) {
                    throw new IllegalStateException("Hook was not really registered!"); // NOI18N
                }
                AntModule.err.log("#36393: removing an unwanted ProcessDestroyer shutdown hook");
                // #36395: memory leak in ThreadGroup if the thread is not started.
                hook.start();
            }
        } catch (Exception e) {
            // Oh well.
            AntModule.err.notify(ErrorManager.INFORMATIONAL, e);
            doHack36393 = false;
        }
    }
    
    private static boolean doGutProject = !Boolean.getBoolean("org.apache.tools.ant.module.bridge.impl.BridgeImpl.doNotGutProject");
    /**
     * Try to break up as many references in a project as possible.
     * Helpful to mitigate the effects of unsolved memory leaks: at
     * least one project will not hold onto all subprojects, and a
     * taskdef will not hold onto its siblings, etc.
     */
    private static void gutProject(Project p) {
        if (!doGutProject) {
            return;
        }
        // XXX should ideally try to wait for all other threads in this thread group
        // to finish - see e.g. #51962 for example of what can happen otherwise.
        try {
            String s = p.getName();
            AntModule.err.log("Gutting extra references in project \"" + s + "\"");
            Field[] fs = Project.class.getDeclaredFields();
            for (int i = 0; i < fs.length; i++) {
                if (Modifier.isStatic(fs[i].getModifiers())) {
                    continue;
                }
                if (fs[i].getType().isPrimitive()) {
                    continue;
                }
                fs[i].setAccessible(true);
                Object o = fs[i].get(p);
                try {
                    if (o instanceof Collection) {
                        ((Collection) o).clear();
                        // #69727: do not null out the field (e.g. Project.listeners) in this case.
                        continue;
                    } else if (o instanceof Map) {
                        ((Map) o).clear();
                        continue;
                    }
                } catch (UnsupportedOperationException e) {
                    // ignore
                }
                if (Modifier.isFinal(fs[i].getModifiers())) {
                    continue;
                }
                fs[i].set(p, null);
            }
            // #43113: IntrospectionHelper can hold strong refs to dynamically loaded classes
            Field helpersF;
            try {
                helpersF = IntrospectionHelper.class.getDeclaredField("helpers");
            } catch (NoSuchFieldException x) { // Ant 1.7.0
                helpersF = IntrospectionHelper.class.getDeclaredField("HELPERS");
            }
            helpersF.setAccessible(true);
            Object helpersO = helpersF.get(null);
            Map helpersM = (Map) helpersO;
            helpersM.clear();
            // #46532: java.beans.Introspector caches not cleared well in all cases.
            Introspector.flushCaches();
        } catch (Exception e) {
            // Oh well.
            AntModule.err.notify(ErrorManager.INFORMATIONAL, e);
            doGutProject = false;
        }
    }
    
}
