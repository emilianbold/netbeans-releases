/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.profiler.nbimpl;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import javax.swing.SwingUtilities;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.lib.profiler.common.AttachSettings;
import org.netbeans.lib.profiler.common.CommonUtils;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.lib.profiler.common.ProfilingSettings;
import org.netbeans.modules.profiler.HeapDumpWatch;
import org.netbeans.modules.profiler.ProfilerModule;
import org.netbeans.modules.profiler.actions.RerunAction;
import org.netbeans.modules.profiler.spi.LoadGenPlugin;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Hurka
 */
@ServiceProvider(service=Profiler.class)
public class NetBeansProfiler extends org.netbeans.modules.profiler.NetBeansProfiler {

    // remembered values for rerun and modify actions
    private ProfilerControlPanel2Support actionSupport;

    public void runTarget(FileObject buildScriptFO, String target, Properties props) {
        getActionSupport().setAll(buildScriptFO, target, props);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
                public void run() {
                    ((RerunAction) RerunAction.get(RerunAction.class)).updateAction();
                }
            });

        doRunTarget(buildScriptFO, target, props);
    }

    /**
     * Runs an target in Ant script with properties context.
     *
     * @param buildScript The build script to run the target from
     * @param target The name of target to run
     * @param props The properties context to run the task in
     * @return ExecutorTask to track the running Ant process
     */
    public static ExecutorTask doRunTarget(final FileObject buildScript, final String target, final Properties props) {
        try {
            String oomeenabled = props.getProperty(HeapDumpWatch.OOME_PROTECTION_ENABLED_KEY);

            if ((oomeenabled != null) && oomeenabled.equals("yes")) { // NOI18N
                HeapDumpWatch.getDefault().monitor(props.getProperty(HeapDumpWatch.OOME_PROTECTION_DUMPPATH_KEY));
            }

            return ActionUtils.runTarget(buildScript, new String[] { target }, props);
        } catch (IOException e) {
            Profiler.getDefault().notifyException(Profiler.EXCEPTION, e);
        }

        return null;
    }
    
    @Override
    public String getLibsDir() {
        final File dir = InstalledFileLocator.getDefault()
                                             .locate(ProfilerModule.LIBS_DIR + "/jfluid-server.jar",
                                                     "org.netbeans.lib.profiler", false); //NOI18N

        if (dir == null) {
            return null;
        } else {
            return dir.getParentFile().getPath();
        }
    }    


    @Override
    public boolean rerunAvailable() {
        return getActionSupport().isActionAvailable();
    }

    @Override
    public boolean modifyAvailable() {
        return getProfilingMode()==MODE_ATTACH || getActionSupport().isActionAvailable();
    }

    @Override
    public void rerunLastProfiling() {
        String target = getActionSupport().getTarget();
        if (target!=null) {
            doRunTarget(getActionSupport().getScript(), target, getActionSupport().getProperties());
        }
    }

    @Override
    public boolean attachToApp(ProfilingSettings profilingSettings, AttachSettings attachSettings) {
        // clear rerun
        getActionSupport().nullAll();
        CommonUtils.runInEventDispatchThread(new Runnable() {
            @Override
            public void run() {
                CallableSystemAction.get(RerunAction.class).updateAction();
            }
        });
        return super.attachToApp(profilingSettings, attachSettings);
    }

    @Override
    public void modifyCurrentProfiling(ProfilingSettings profilingSettings) {
        Properties properties = getActionSupport().getProperties();
        
        if (properties!=null) {
            profilingSettings.store(properties); // Fix for http://www.netbeans.org/issues/show_bug.cgi?id=95651, update settings for ReRun
        }
        super.modifyCurrentProfiling(profilingSettings);
    }

    Properties getCurrentProfilingProperties() {
        return getActionSupport().getProperties();
    }

    @Override
    protected void cleanupAfterProfiling() {
        stopLoadGenerator();
        super.cleanupAfterProfiling();
    }
    
    private void stopLoadGenerator() {
        Properties profilingProperties = getCurrentProfilingProperties();

        if (profilingProperties != null) {
            LoadGenPlugin plugin = Lookup.getDefault().lookup(LoadGenPlugin.class);

            if (plugin != null) {
                String scriptPath = profilingProperties.getProperty("profiler.loadgen.path"); // TODO factor out the "profiler.loadgen.path" constant; also used ing J2EEProjectTypeProfiler

                if (scriptPath != null) {
                    plugin.stop(scriptPath);
                }
            }
        }
    }

    private synchronized ProfilerControlPanel2Support getActionSupport() {
        if (actionSupport == null) {
            actionSupport = new ProfilerControlPanel2Support();
        }
        return actionSupport;
    }
}
