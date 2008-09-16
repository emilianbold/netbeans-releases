/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.apache.tools.ant.module.run;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import javax.swing.event.ChangeListener;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.support.AntScriptUtils;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;

/**
 * Records the last Ant target(s) that was executed.
 * @author Jesse Glick
 */
public class LastTargetExecuted {
    
    private LastTargetExecuted() {}
    
    private static File buildScript;
    //private static int verbosity;
    private static String[] targets;
    private static Map<String,String> properties;
    private static String displayName;
    
    /** Called from {@link TargetExecutor}. */
    static void record(File buildScript, String[] targets, Map<String,String> properties, String displayName) {
        LastTargetExecuted.buildScript = buildScript;
        //LastTargetExecuted.verbosity = verbosity;
        LastTargetExecuted.targets = targets;
        LastTargetExecuted.properties = properties;
        LastTargetExecuted.displayName = displayName;
        cs.fireChange();
    }
    
    /**
     * Get the last build script to be run.
     * @return the last-run build script, or null if nothing has been run yet (or the build script disappeared etc.)
     */
    public static AntProjectCookie getLastBuildScript() {
        if (buildScript != null && buildScript.isFile()) {
            FileObject fo = FileUtil.toFileObject(buildScript);
            assert fo != null;
            return AntScriptUtils.antProjectCookieFor(fo);
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
        return displayName;
    }
    
    /**
     * Try to rerun the last task.
     */
    public static ExecutorTask rerun() throws IOException {
        AntProjectCookie apc = getLastBuildScript();
        if (apc == null) {
            // Can happen in case the build script was deleted (similar to #84874).
            // Also make sure to disable RunLastTargetAction.
            cs.fireChange();
            return null;
        }
        TargetExecutor t = new TargetExecutor(apc, targets);
        //t.setVerbosity(verbosity);
        t.setProperties(properties);
        t.setDisplayName(displayName); // #140999: do not recalculate
        return t.execute();
    }
    
    private static final ChangeSupport cs = new ChangeSupport(LastTargetExecuted.class);
    
    public static void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }
    
    public static void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }
    
}
