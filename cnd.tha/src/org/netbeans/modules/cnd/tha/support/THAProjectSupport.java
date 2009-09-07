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
import org.netbeans.modules.cnd.makeproject.api.wizards.ReconfigureProvider;
import org.netbeans.modules.cnd.tha.THAServiceInfo;
import org.netbeans.modules.dlight.api.execution.DLightTargetListener;
import org.netbeans.modules.dlight.api.execution.DLightToolkitManagement;
import org.netbeans.modules.dlight.api.execution.DLightToolkitManagement.DLightSessionHandler;
import org.netbeans.modules.dlight.perfan.tha.api.THAInstrumentationSupport;
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

        ProjectConfigurationProvider pcp = project.getLookup().lookup(ProjectConfigurationProvider.class);

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
        MakeConfiguration mc = mcd.getActiveConfiguration();
        CompilerSet compilerSet = mc.getCompilerSet().getCompilerSet();

        if (!compilerSet.isSunCompiler()) {
            return false;
        }

        THAInstrumentationSupport instrSupport = getInstrumentationSupport();

        if (instrSupport == null || !instrSupport.isSupported()) {
            return false;
        }

        return true;
    }

    public boolean isConfiguredForInstrumentation() {
        THAInstrumentationSupport instrSupport = getInstrumentationSupport();

        if (instrSupport == null || !instrSupport.isSupported()) {
            return false;
        }

        //if it is sparc it means we do not need option
        MakeConfigurationDescriptor mcd = MakeConfigurationDescriptor.getMakeConfigurationDescriptor(project);
        MakeConfiguration mc = mcd.getActiveConfiguration();
        if (mc.isMakefileConfiguration()){
            return isConfiguredForInstrumentationMakefile();
        }
        if (!instrSupport.isInstrumentationNeeded(mc.getDevelopmentHost().getExecutionEnvironment())){
            return true;
        }
        if (mc.getLinkerConfiguration().getCommandLineConfiguration().getValue().contains(instrSupport.getLinkerOptions())) {
            return true;
        }

        return false;
    }

    private boolean isConfiguredForInstrumentationMakefile() {
        THAInstrumentationSupport instrSupport = getInstrumentationSupport();
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
        MakeConfiguration mc = mcd.getActiveConfiguration();
        CompilerSet compilerSet = mc.getCompilerSet().getCompilerSet();

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
        MakeConfiguration mc = mcd.getActiveConfiguration();
        if (mc.isMakefileConfiguration()){
            return doInstrumentationMakefile();
        }

        THAInstrumentationSupport instrSupport = getInstrumentationSupport();

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

        setModified();

        return true;
    }

    private boolean doInstrumentationMakefile() {
        THAInstrumentationSupport instrSupport = getInstrumentationSupport();
        ReconfigureProvider.getDefault().reconfigure(project, "-g "+instrSupport.getCompilerOptions(), // NOI18N
                "-g "+instrSupport.getCompilerOptions(), instrSupport.getLinkerOptions()); // NOI18N
        return false;
    }

    public boolean undoInstrumentation() {
        THAInstrumentationSupport instrSupport = getInstrumentationSupport();

        if (instrSupport == null || !instrSupport.isSupported()) {
            return false;
        }

        boolean changed = false;

        MakeConfigurationDescriptor mcd = MakeConfigurationDescriptor.getMakeConfigurationDescriptor(project);
        MakeConfiguration mc = mcd.getActiveConfiguration();
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
            String ccOptions = mc.getCCompilerConfiguration().getCommandLineConfiguration().getValue();
            String ccInstrOption = instrSupport.getCompilerOptions();
            idx = ccOptions.indexOf(ccInstrOption);
            if (idx >= 0) {
                mc.getCCCompilerConfiguration().getCommandLineConfiguration().setValue(ccOptions.replaceAll(ccInstrOption, "")); // NOI18N
                changed = true;
            }
        }

        if (changed) {
            setModified();
        }

        return changed;
    }

    private boolean undoInstrumentationMakefile() {
        ReconfigureProvider.getDefault().reconfigure(project, "-g", "-g", ""); // NOI18N
        return false;
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(THAProjectSupport.class, key, params);
    }

    private boolean activeCompilerIsSunStudio() {
        boolean result = false;
        try {
            MakeConfigurationDescriptor mcd = MakeConfigurationDescriptor.getMakeConfigurationDescriptor(project);
            MakeConfiguration mc = mcd.getActiveConfiguration();
            CompilerSet compilerSet = mc.getCompilerSet().getCompilerSet();
            result = compilerSet.isSunCompiler();
        } catch (Throwable th) {
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
        MakeConfiguration mc = mcd.getActiveConfiguration();
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

    private THAInstrumentationSupport getInstrumentationSupport() {
        if (!activeCompilerIsSunStudio()) {
            return null;
        }

        MakeConfigurationDescriptor mcd = MakeConfigurationDescriptor.getMakeConfigurationDescriptor(project);
        MakeConfiguration mc = mcd.getActiveConfiguration();
        CompilerSet compilerSet = mc.getCompilerSet().getCompilerSet();
        Tool ccTool = compilerSet.getTool(Tool.CCCompiler);
        String ccPath = ccTool.getPath();
        String sunstudioBinDir = ccPath.substring(0, ccPath.length() - ccTool.getName().length());

        return THAInstrumentationSupport.getSupport(mc.getDevelopmentHost().getExecutionEnvironment(), sunstudioBinDir);
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
