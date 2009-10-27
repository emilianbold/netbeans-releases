/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.tha.support;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.modules.cnd.makeproject.api.wizards.ReconfigureProvider;
import org.netbeans.modules.cnd.tha.THAServiceInfo;
import org.netbeans.modules.dlight.api.execution.DLightTargetListener;
import org.netbeans.modules.dlight.api.execution.DLightToolkitManagement;
import org.netbeans.modules.dlight.api.execution.DLightToolkitManagement.DLightSessionHandler;
import org.netbeans.modules.dlight.perfan.tha.api.THAConfiguration;
import org.netbeans.modules.dlight.perfan.tha.api.THAInstrumentationSupport;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

public final class THAProjectSupport implements PropertyChangeListener {

    private static final String MODIFY_PROJECT_CAPTION = loc("THA_ModifyProjectCaption"); // NOI18N
    private static final String MODIFY_PROJECT_MSG = loc("THA_ModifyProjectMsg"); // NOI18N
    private final static Map<Project, THAProjectSupport> cache = new HashMap<Project, THAProjectSupport>();
    private final Collection<PropertyChangeListener> listeners = new CopyOnWriteArrayList<PropertyChangeListener>();
    private final Project project;
    private DLightSessionHandler session;
    private final List<DLightTargetListener> targetListeners = new ArrayList<DLightTargetListener>();

    private THAProjectSupport(Project project) {
        this.project = project;
    }

    public List<DLightTargetListener> getTargetListeners(){
        return targetListeners;
    }

    public void addDLightTargetListener(DLightTargetListener l){
        synchronized(this){
            if (!targetListeners.contains(l)){
                targetListeners.add(l);
            }
        }
    }

    public void removeDLightTargetListener(DLightTargetListener l){
        synchronized(this){
            targetListeners.remove(l);
        }
    }

    public final void setDLigthSessionHandler(DLightSessionHandler session){
        this.session = session;
    }
    
    public void stop(){
        if (session != null){
            DLightToolkitManagement.getInstance().stopSession(session);
        }
    }

    public static final synchronized THAProjectSupport getSupportFor(Project project) {
        if (project == null) {
            return null;
        }

        if (cache.containsKey(project)) {
            return cache.get(project);
        }

        if (!isSupported(project)) {
            return null;
        }

        THAProjectSupport support = new THAProjectSupport(project);

        ProjectConfigurationProvider<?> pcp = project.getLookup().lookup(ProjectConfigurationProvider.class);

        if (pcp != null) {
            pcp.addPropertyChangeListener(support);
        }

        cache.put(project, support);

        return support;
    }

    /**
     * Returns true if and only if:
     *   - project is not NULL
     *   - project is NativeProject with SunStudio active toolchain
     *   - SunStudio is up-to-date enough (>= 7.6 (mars))
     *
     * @param project
     * @return
     */
    public boolean canInstrument() {
        MakeConfigurationDescriptor mcd = MakeConfigurationDescriptor.getMakeConfigurationDescriptor(project);
        if (mcd == null) {
            return false;
        }
        MakeConfiguration mc = mcd.getActiveConfiguration();
        if (mc == null) {
            return false;
        }
        CompilerSet compilerSet = mc.getCompilerSet().getCompilerSet();

        if (compilerSet == null || !compilerSet.isSunCompiler()) {
            return false;
        }

        THAInstrumentationSupport instrSupport = getInstrumentationSupport(false);

        if (instrSupport == null || !instrSupport.isSupported()) {
            return false;
        }

        return true;
    }

    public boolean isConfiguredForInstrumentation(THAConfiguration configuration) {
        THAInstrumentationSupport instrSupport = getInstrumentationSupport(true);

        if (instrSupport == null){// || !instrSupport.isSupported()) {
            // should be be possible call method in UI thread
            return false;
        }

        //if it is sparc it means we do not need option
        MakeConfigurationDescriptor mcd = MakeConfigurationDescriptor.getMakeConfigurationDescriptor(project);
        if (mcd == null) {
            return false;
        }
        MakeConfiguration mc = mcd.getActiveConfiguration();
        if (mc == null) {
            return false;
        }
        if (mc.isMakefileConfiguration()){
            return isConfiguredForInstrumentationMakefile();
        }
        if (configuration != null) {
            if (!instrSupport.isInstrumentationNeeded(mc.getDevelopmentHost().getExecutionEnvironment(), configuration)){
                return true;
            }
        }
	if ((mc.getCRequired().getValue() && !mc.getCCompilerConfiguration().getCommandLineConfiguration().getValue().contains(instrSupport.getCompilerOptions()))) {
	    return false;
	}
	if ((mc.getCppRequired().getValue() && !mc.getCCCompilerConfiguration().getCommandLineConfiguration().getValue().contains(instrSupport.getCompilerOptions()))) {
	    return false;
	}
	if ((mc.getFortranRequired().getValue() && !mc.getFortranCompilerConfiguration().getCommandLineConfiguration().getValue().contains(instrSupport.getCompilerOptions()))) {
	    return false;
	}
	if (!(mc.getLinkerConfiguration().getCommandLineConfiguration().getValue().contains(instrSupport.getLinkerOptions()))) {
	    return false;
	}
        return true;
    }

    private boolean isConfiguredForInstrumentationMakefile() {
        THAInstrumentationSupport instrSupport = getInstrumentationSupport(false);
        String args = ReconfigureProvider.getDefault().getLastFlags(project);
        if (args != null && args.indexOf(instrSupport.getCompilerOptions())>=0) {
            return true;
        }
        return false;
    }


    public boolean isInstrumented() {
        if (!isSupported(project)) {
            return false;
        }

        // First - check for required options.

        if (!activeCompilerIsSunStudio()) {
            return false;
        }

        MakeConfigurationDescriptor mcd = MakeConfigurationDescriptor.getMakeConfigurationDescriptor(project);
        if (mcd == null) {
            return false;
        }
        MakeConfiguration mc = mcd.getActiveConfiguration();
        if (mc == null) {
            return false;
        }
        CompilerSet compilerSet = mc.getCompilerSet().getCompilerSet();
        if (compilerSet == null) {
            return false;
        }

        Tool ccTool = compilerSet.getTool(Tool.CCCompiler);
        String ccPath = ccTool.getPath();
        String sunstudioBinDir = ccPath.substring(0, ccPath.length() - ccTool.getName().length());

        THAInstrumentationSupport instrSupport = THAInstrumentationSupport.getSupport(mc.getDevelopmentHost().getExecutionEnvironment(), sunstudioBinDir);

        boolean result = false;

        try {
            result = instrSupport.isInstrumented(mc.getAbsoluteOutputValue()).get();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }

        return result;
    }


    public boolean doInstrumentation() {
        NativeProject nativeProject = project.getLookup().lookup(NativeProject.class);

        assert nativeProject != null;

        String projectName = nativeProject.getProjectDisplayName();

        if (!activeCompilerIsSunStudio()) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    loc("THA_ReconfigureProjectWithSunStudio", projectName), NotifyDescriptor.INFORMATION_MESSAGE)); // NOI18N
            return false;
        }

        String caption = MessageFormat.format(MODIFY_PROJECT_CAPTION, new Object[]{projectName});
        String message = MessageFormat.format(MODIFY_PROJECT_MSG, new Object[]{projectName, "build-before-profiler.xml"}); // NOI18N

        if (DialogDisplayer.getDefault().notify(new NotifyDescriptor(message, caption, NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.INFORMATION_MESSAGE, new Object[]{NotifyDescriptor.OK_OPTION,
                    NotifyDescriptor.CANCEL_OPTION}, NotifyDescriptor.OK_OPTION)) != NotifyDescriptor.OK_OPTION) {
            return false;
        }

        MakeConfigurationDescriptor mcd = MakeConfigurationDescriptor.getMakeConfigurationDescriptor(project);
        if (mcd == null) {
            return false;
        }
        MakeConfiguration mc = mcd.getActiveConfiguration();
        if (mc == null) {
            return false;
        }
        if (mc.isMakefileConfiguration()){
            return doInstrumentationMakefile();
        }

        THAInstrumentationSupport instrSupport = getInstrumentationSupport(false);

        String linkerOptions = mc.getLinkerConfiguration().getCommandLineConfiguration().getValue();

        if (!linkerOptions.contains(instrSupport.getLinkerOptions())) {
            mc.getLinkerConfiguration().getCommandLineConfiguration().setValue(linkerOptions + " " + instrSupport.getLinkerOptions()); // NOI18N
        }

        if (mc.getCRequired().getValue()) {
            String cOptions = mc.getCCompilerConfiguration().getCommandLineConfiguration().getValue();
            if (!cOptions.contains(instrSupport.getCompilerOptions())) {
                mc.getCCompilerConfiguration().getCommandLineConfiguration().setValue(cOptions + " " + instrSupport.getCompilerOptions()); // NOI18N
            }
        }

        if (mc.getCppRequired().getValue()) {
            String ccOptions = mc.getCCCompilerConfiguration().getCommandLineConfiguration().getValue();
            if (!ccOptions.contains(instrSupport.getCompilerOptions())) {
                mc.getCCCompilerConfiguration().getCommandLineConfiguration().setValue(ccOptions + " " + instrSupport.getCompilerOptions()); // NOI18N
            }
        }

	if (mc.getFortranRequired().getValue()) {
            String fortranOptions = mc.getFortranCompilerConfiguration().getCommandLineConfiguration().getValue();
            if (!fortranOptions.contains(instrSupport.getCompilerOptions())) {
                mc.getFortranCompilerConfiguration().getCommandLineConfiguration().setValue(fortranOptions + " " + instrSupport.getCompilerOptions()); // NOI18N
            }
        }

        setModified();

        return true;
    }

    private boolean doInstrumentationMakefile() {
        THAInstrumentationSupport instrSupport = getInstrumentationSupport(false);
        ReconfigureProvider.getDefault().reconfigure(project, "-g "+instrSupport.getCompilerOptions(), // NOI18N
                "-g "+instrSupport.getCompilerOptions(), instrSupport.getLinkerOptions()); // NOI18N
        return true;
    }

    public List<String> undoInstrumentation() {
        THAInstrumentationSupport instrSupport = getInstrumentationSupport(true);

        if (instrSupport == null){// || !instrSupport.isSupported()) {
            return Collections.<String>emptyList();
        }

        boolean changed = false;

        MakeConfigurationDescriptor mcd = MakeConfigurationDescriptor.getMakeConfigurationDescriptor(project);
        if (mcd == null) {
            return Collections.<String>emptyList();
        }
        MakeConfiguration mc = mcd.getActiveConfiguration();
        if (mc == null) {
            return Collections.<String>emptyList();
        }
        if (mc.isMakefileConfiguration()){
            return undoInstrumentationMakefile();
        }

        String linkerOptions = mc.getLinkerConfiguration().getCommandLineConfiguration().getValue();
        String linkerInstrOption = instrSupport.getLinkerOptions();
        int idx = linkerOptions.indexOf(linkerInstrOption);

        if (idx >= 0) {
            mc.getLinkerConfiguration().getCommandLineConfiguration().setValue(linkerOptions.replaceAll(linkerInstrOption, "")); // NOI18N
            changed = true;
        }

        if (mc.getCRequired().getValue()) {
            String cOptions = mc.getCCompilerConfiguration().getCommandLineConfiguration().getValue();
            String cInstrOption = instrSupport.getCompilerOptions();
            idx = cOptions.indexOf(cInstrOption);
            if (idx >= 0) {
                mc.getCCompilerConfiguration().getCommandLineConfiguration().setValue(cOptions.replaceAll(cInstrOption, "")); // NOI18N
                changed = true;
            }
        }

        if (mc.getCppRequired().getValue()) {
            String ccOptions = mc.getCCCompilerConfiguration().getCommandLineConfiguration().getValue();
            String ccInstrOption = instrSupport.getCompilerOptions();
            idx = ccOptions.indexOf(ccInstrOption);
            if (idx >= 0) {
                mc.getCCCompilerConfiguration().getCommandLineConfiguration().setValue(ccOptions.replaceAll(ccInstrOption, "")); // NOI18N
                changed = true;
            }
        }

	if (mc.getFortranRequired().getValue()) {
            String fortranOptions = mc.getFortranCompilerConfiguration().getCommandLineConfiguration().getValue();
            String fortranInstrOption = instrSupport.getCompilerOptions();
            idx = fortranOptions.indexOf(fortranInstrOption);
            if (idx >= 0) {
                mc.getFortranCompilerConfiguration().getCommandLineConfiguration().setValue(fortranOptions.replaceAll(fortranInstrOption, "")); // NOI18N
                changed = true;
            }
        }

        if (changed) {
            RunProfile profile = (RunProfile) mc.getAuxObject(RunProfile.PROFILE_ID);
            if (profile != null) {
                profile.setBuildFirst(true);
            }
            setModified();
        }
        if (changed) {
            List<String> res = new ArrayList<String>(3);
            res.add("save"); // NOI18N
            res.add("clean"); // NOI18N
            res.add("build"); // NOI18N
            return res;
        }
        return Collections.<String>emptyList();
    }

    private List<String> undoInstrumentationMakefile() {
        List<String> res = new ArrayList<String>(1);
        res.add("configure"); // NOI18N
        return res;
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(THAProjectSupport.class, key, params);
    }

    public boolean activeCompilerIsSunStudio() {
        boolean result = false;
        try {
            MakeConfigurationDescriptor mcd = MakeConfigurationDescriptor.getMakeConfigurationDescriptor(project);
            if (mcd == null) {
                return false;
            }
            MakeConfiguration mc = mcd.getActiveConfiguration();
            if (mc == null) {
                return false;
            }
            CompilerSet compilerSet = mc.getCompilerSet().getCompilerSet();
            if (compilerSet != null) {
                result = compilerSet.isSunCompiler();
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return result;
    }

    public static boolean isSupported(Project project) {
        if (project == null) {
            return false;
        }

        NativeProject nativeProject = project.getLookup().lookup(NativeProject.class);

        if (nativeProject == null) {
            return false;
        }
        MakeConfigurationDescriptor mcd = MakeConfigurationDescriptor.getMakeConfigurationDescriptor(project);
        if (mcd == null){
            return false;
        }
        MakeConfiguration mc = mcd.getActiveConfiguration();
        if (mc == null) {
            return false;
        }
        int configurationType = mc.getConfigurationType().getValue();
        return THAServiceInfo.isPlatformSupported(mc.getDevelopmentHost().getBuildPlatformDisplayName()) &&
                (configurationType == MakeConfiguration.TYPE_MAKEFILE ||
                configurationType == MakeConfiguration.TYPE_APPLICATION || configurationType == MakeConfiguration.TYPE_QT_APPLICATION);

    }

    public void addProjectConfigurationChangedListener(final PropertyChangeListener listener) {
        listeners.add(listener);
    }

    public void removeProjectConfigurationChangedListener(final PropertyChangeListener listener) {
        listeners.remove(listener);
    }

    private THAInstrumentationSupport getInstrumentationSupport(boolean force) {
        if (!force) {
            if (!activeCompilerIsSunStudio()) {
                return null;
            }
        }

        MakeConfigurationDescriptor mcd = MakeConfigurationDescriptor.getMakeConfigurationDescriptor(project);
        if (mcd == null) {
            return null;
        }
        MakeConfiguration mc = mcd.getActiveConfiguration();
        if (mc == null) {
            return null;
        }
        CompilerSet compilerSet = mc.getCompilerSet().getCompilerSet();
        if (compilerSet == null || compilerSet.isGnuCompiler()) {
            return null;
        }
        String sunstudioBinDir = compilerSet.getDirectory();
        ExecutionEnvironment execEnv = mc.getDevelopmentHost().getExecutionEnvironment();
        return THAInstrumentationSupport.getSupport(execEnv, sunstudioBinDir);
    }

    private void setModified() {
        MakeConfigurationDescriptor mcd = MakeConfigurationDescriptor.getMakeConfigurationDescriptor(project);
        mcd.setModified(true);
        propertyChange(null);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        for (PropertyChangeListener l : listeners) {
            l.propertyChange(evt);
        }
    }
}
