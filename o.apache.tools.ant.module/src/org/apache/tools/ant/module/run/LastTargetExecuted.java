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

package org.apache.tools.ant.module.run;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * Records the last Ant target(s) that was executed.
 * @author Jesse Glick
 */
public class LastTargetExecuted {
    
    private LastTargetExecuted() {}
    
    private static File buildScript;
    private static int verbosity;
    private static String[] targets;
    private static Map<String,String> properties;
    
    /** Called from {@link TargetExecutor}. */
    static void record(File buildScript, int verbosity, String[] targets, Map<String,String> properties) {
        LastTargetExecuted.buildScript = buildScript;
        LastTargetExecuted.verbosity = verbosity;
        LastTargetExecuted.targets = targets;
        LastTargetExecuted.properties = properties;
        fireChange();
    }
    
    /**
     * Get the last build script to be run.
     * @return the last-run build script, or null if nothing has been run yet (or the build script disappeared etc.)
     */
    public static AntProjectCookie getLastBuildScript() {
        if (buildScript != null && buildScript.isFile()) {
            FileObject fo = FileUtil.toFileObject(buildScript);
            assert fo != null;
            try {
                return DataObject.find(fo).getCookie(AntProjectCookie.class);
            } catch (DataObjectNotFoundException e) {
                assert false : e;
            }
        }
        return null;
    }
    
    /**
     * Get the last target names to be run.
     * @return a list of one or more targets, or null for the default target
     */
    public static String[] getLastTargets() {
        return targets;
    }
    
    /**
     * Get a display name (as it would appear in the Output Window) for the last process.
     * @return a process display name, or null if nothing has been run yet
     */
    public static String getProcessDisplayName() {
        AntProjectCookie apc = getLastBuildScript();
        if (apc != null) {
            return TargetExecutor.getProcessDisplayName(apc, targets != null ? Arrays.asList(targets) : null);
        } else {
            return null;
        }
    }
    
    /**
     * Try to rerun the last task.
     */
    public static ExecutorTask rerun() throws IOException {
        AntProjectCookie apc = getLastBuildScript();
        if (apc == null) {
            // Can happen in case the build script was deleted (similar to #84874).
            // Also make sure to disable RunLastTargetAction.
            fireChange();
            return null;
        }
        TargetExecutor t = new TargetExecutor(apc, targets);
        t.setVerbosity(verbosity);
        t.setProperties(properties);
        return t.execute();
    }
    
    private static final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    
    public static void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    public static void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    private static void fireChange() {
        ChangeEvent ev = new ChangeEvent(LastTargetExecuted.class);
        ChangeListener[] ls;
        synchronized (listeners) {
            ls = listeners.toArray(new ChangeListener[listeners.size()]);
        }
        for (ChangeListener l : ls) {
            l.stateChanged(ev);
        }
    }
    
}
