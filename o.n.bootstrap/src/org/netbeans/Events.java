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

package org.netbeans;

import java.util.Arrays;
import org.openide.ErrorManager;

/** Track events that occur in the module system abstractly.
 * A concrete implementation can provide localized messages
 * for these, notify the user visually, track performance, etc.
 * Only events relevant to the user or to performance need to
 * be logged; detailed low-level descriptions of what is
 * happening can simply be logged to the ErrorManager.
 * @author Jesse Glick
 */
public abstract class Events {
    
    /** String message */
    public static final String PERF_START = "perfStart"; // NOI18N
    /** String message */
    public static final String PERF_TICK = "perfTick"; // NOI18N
    /** String message */
    public static final String PERF_END = "perfEnd"; // NOI18N
    
    /** File jar */
    public static final String START_CREATE_REGULAR_MODULE = "startCreateRegularModule"; // NOI18N
    /** File jar */
    public static final String FINISH_CREATE_REGULAR_MODULE = "finishCreateRegularModule"; // NOI18N
    /** Object history */
    public static final String START_CREATE_BOOT_MODULE = "startCreateBootModule"; // NOI18N
    /** Object history */
    public static final String FINISH_CREATE_BOOT_MODULE = "finishCreateBootModule"; // NOI18N
    /** no args */
    public static final String CREATED_MODULE_SYSTEM = "createdModuleSystem"; // NOI18N
    /** no args */
    public static final String START_LOAD_BOOT_MODULES = "startLoadBootModules"; // NOI18N
    /** no args */
    public static final String FINISH_LOAD_BOOT_MODULES = "finishLoadBootModules"; // NOI18N
    /** File jar */
    public static final String START_DEPLOY_TEST_MODULE = "startDeployTestModule"; // NOI18N
    /** File jar */
    public static final String FINISH_DEPLOY_TEST_MODULE = "finishDeployTestModule"; // NOI18N
    /** Module toDelete */
    public static final String DELETE_MODULE = "deleteModule"; // NOI18N
    /** List<Module> toEnable */
    public static final String START_ENABLE_MODULES = "startEnableModules"; // NOI18N
    /** List<Module> toEnable */
    public static final String FINISH_ENABLE_MODULES = "finishEnableModules"; // NOI18N
    /** List<Module> toDisable */
    public static final String START_DISABLE_MODULES = "startDisableModules"; // NOI18N
    /** List<Module> toDisable */
    public static final String FINISH_DISABLE_MODULES = "finishDisableModules"; // NOI18N
    /** Module prepared */
    public static final String PREPARE = "prepare"; // NOI18N
    /** List<Module> loaded */
    public static final String START_LOAD = "startLoad"; // NOI18N
    /** List<Module> loaded */
    public static final String FINISH_LOAD = "finishLoad"; // NOI18N
    /** List<Module> unloaded */
    public static final String START_UNLOAD = "startUnload"; // NOI18N
    /** List<Module> unloaded */
    public static final String FINISH_UNLOAD = "finishUnload"; // NOI18N
    /** Module installed */
    public static final String INSTALL = "install"; // NOI18N
    /** Module uninstalled */
    public static final String UNINSTALL = "uninstall"; // NOI18N
    /** Module restored */
    public static final String RESTORE = "restore"; // NOI18N
    /** Module updated */
    public static final String UPDATE = "update"; // NOI18N
    /** Module home, ManifestSection section */
    public static final String LOAD_SECTION = "loadSection"; // NOI18N
    /** no args */
    public static final String CLOSE = "close"; // NOI18N
    /** no args */
    public static final String START_READ = "startRead"; // NOI18N
    /** Set<Module> found */
    public static final String FINISH_READ = "finishRead"; // NOI18N
    /** FileObject */
    public static final String MODULES_FILE_PROCESSED = "modulesFileProcessed"; // NOI18N
    /** Set<Module> toInstall */
    public static final String START_AUTO_RESTORE = "startAutoRestore"; // NOI18N
    /** Set<Module> toInstall */
    public static final String FINISH_AUTO_RESTORE = "finishAutoRestore"; // NOI18N
    /** Set<Module> notInstalled */
    public static final String FAILED_INSTALL_NEW = "failedInstallNew"; // NOI18N
    /** Module notInstalled, InvalidException problem */
    public static final String FAILED_INSTALL_NEW_UNEXPECTED = "failedInstallNewUnexpected"; // NOI18N
    /** Set<Module> modules */
    public static final String LOAD_LAYERS = "loadLayers"; // NOI18N
    /** Set<Module> modules */
    public static final String UNLOAD_LAYERS = "unloadLayers"; // NOI18N
    /** Module culprit, Class offending, ClassLoader expected */
    public static final String WRONG_CLASS_LOADER = "wrongClassLoader"; // NOI18N
    /** File extension, Set<File> owners */
    public static final String EXTENSION_MULTIPLY_LOADED = "extensionMultiplyLoaded"; // NOI18N
    /** File nonexistentJar */
    public static final String MISSING_JAR_FILE = "missingJarFile"; // NOI18N
    /** Module autoload (or since org.netbeans.core/1 1.3, eager) */
    public static final String CANT_DELETE_ENABLED_AUTOLOAD = "cantDeleteEnabledAutoload"; // NOI18N
    /** Module afflicted, String propname, Object valueOnDisk, Object actualValue */
    public static final String MISC_PROP_MISMATCH = "miscPropMismatch"; // NOI18N
    /** File patchfile */
    public static final String PATCH = "patch"; // NOI18N
    
    /** Constructor for subclasses to use. */
    protected Events() {
    }
    
    /** Log an event.
     * You must pass a fixed event type string, and a list
     * of arguments (meaning varies according to event type).
     * Note that the event type string must be the exact String
     * listed as the constant in this class, not a copy.
     */
    public final void log(String message, Object[] args) {
        if (Util.err.isLoggable(ErrorManager.INFORMATIONAL) &&
                message != PERF_START && message != PERF_TICK && message != PERF_END) {
            Util.err.log("EVENT -> " + message + " " + Arrays.asList(args));
        }
        try {
            logged(message, args);
        } catch (RuntimeException re) {
            // If there is any problem logging, it should not kill the system
            // which called the logger.
            Util.err.notify(re);
        }
    }
    
    /** Log an event with no arguments.
     * Convenience method.
     */
    public final void log(String message) {
        log(message, new Object[0]);
    }
    
    /** Log an event with a single argument.
     * Convenience method.
     */
    public final void log(String message, Object arg1) {
        log(message, new Object[] {arg1});
    }
    
    /** Log an event with two arguments.
     * Convenience method.
     */
    public final void log(String message, Object arg1, Object arg2) {
        log(message, new Object[] {arg1, arg2});
    }
    
    /** Log an event with three arguments.
     * Convenience method.
     */
    public final void log(String message, Object arg1, Object arg2, Object arg3) {
        log(message, new Object[] {arg1, arg2, arg3});
    }
    
    /** Log an event with four arguments.
     * Convenience method.
     */
    public final void log(String message, Object arg1, Object arg2, Object arg3, Object arg4) {
        log(message, new Object[] {arg1, arg2, arg3, arg4});
    }
    
    /** Report an event.
     */
    protected abstract void logged(String message, Object[] args);
    
}
