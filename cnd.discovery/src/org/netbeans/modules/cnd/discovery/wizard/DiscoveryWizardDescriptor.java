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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.discovery.wizard;

import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProvider;
import org.netbeans.modules.cnd.discovery.wizard.api.DiscoveryDescriptor;
import org.netbeans.modules.cnd.discovery.wizard.api.ProjectConfiguration;
import org.openide.WizardDescriptor;
import org.openide.util.Utilities;


/**
 *
 * @author Alexander Simon
 */
@SuppressWarnings("unchecked") // NOI18N
public class DiscoveryWizardDescriptor extends WizardDescriptor implements DiscoveryDescriptor{
    public static final String PROJECT = "DW:project"; // NOI18N
    public static final String PRIVIDER = "DW:provider"; // NOI18N
    public static final String ROOT_FOLDER = "DW:rootFolder"; // NOI18N
    public static final String BUILD_RESULT = "DW:buildResult"; // NOI18N
    public static final String ADDITIONAL_LIBRARIES = "DW:libraries"; // NOI18N
    public static final String CONSOLIDATION_STRATEGY = "DW:consolidationLevel"; // NOI18N
    public static final String CONFIGURATIONS = "DW:configurations"; // NOI18N
    public static final String INCLUDED = "DW:included"; // NOI18N
    public static final String INVOKE_PROVIDER = "DW:invokeProvider"; // NOI18N
    
    private boolean stateChanged = true;
    private boolean simple = true;
    private boolean cutResult = false;
    
    public DiscoveryWizardDescriptor(WizardDescriptor.Iterator panels){
        super(panels);
    }
    
    public static DiscoveryDescriptor adaptee(Object wizard){
        if (wizard instanceof DiscoveryDescriptor) {
            return (DiscoveryDescriptor) wizard;
        } else if (wizard instanceof WizardDescriptor) {
            return new DiscoveryWizardDescriptorAdapter((WizardDescriptor)wizard);
        } else if (wizard instanceof Map){
            return new DiscoveryWizardClone((Map)wizard);
        }
        return null;
    }
    
    public Project getProject(){
        return (Project) getProperty(PROJECT);
    }
    public void setProject(Project project){
        putProperty(PROJECT, project);
    }
    
    public String getRootFolder(){
        return (String) getProperty(ROOT_FOLDER);
    }
    
    public void setRootFolder(String root){
        stateChanged = true;
        if (root != null && Utilities.isWindows()) {
            root = root.replace('\\','/');
        }
        putProperty(ROOT_FOLDER, root);
    }
    
    public String getBuildResult() {
        return (String) getProperty(BUILD_RESULT);
    }
    
    public void setBuildResult(String binaryPath) {
        putProperty(BUILD_RESULT, binaryPath);
    }
    
    public String getAditionalLibraries() {
        return (String) getProperty(ADDITIONAL_LIBRARIES);
    }
    
    public void setAditionalLibraries(String binaryPath) {
        putProperty(ADDITIONAL_LIBRARIES, binaryPath);
    }
    
    public DiscoveryProvider getProvider(){
        return (DiscoveryProvider) getProperty(PRIVIDER);
    }
    public String getProviderID(){
        DiscoveryProvider provider =(DiscoveryProvider) getProperty(PRIVIDER);
        if (provider != null){
            return provider.getID();
        }
        return null;
    }
    public void setProvider(DiscoveryProvider provider){
        stateChanged = true;
        putProperty(PRIVIDER, provider);
    }
    
    public String getLevel(){
        return (String) getProperty(CONSOLIDATION_STRATEGY);
    }
    public void setLevel(String level){
        putProperty(CONSOLIDATION_STRATEGY, level);
    }
    
    public List<ProjectConfiguration> getConfigurations(){
        return (List<ProjectConfiguration>) getProperty(CONFIGURATIONS);
    }
    public void setConfigurations(List<ProjectConfiguration> configuration){
        putProperty(CONFIGURATIONS, configuration);
    }
    
    public List<String> getIncludedFiles(){
        return (List<String>) getProperty(INCLUDED);
    }
    public void setIncludedFiles(List<String> includedFiles){
        putProperty(INCLUDED, includedFiles);
    }
    
    public boolean isInvokeProvider(){
        return stateChanged;
    }
    
    public void setInvokeProvider(boolean invoke){
        stateChanged = invoke;
    }
    
    public void setMessage(String message) {
        putProperty("WizardPanel_errorMessage", message); // NOI18N
    }
    
    public void clean() {
        setProject(null);
        setProvider(null);
        setRootFolder(null);
        setBuildResult(null);
        setAditionalLibraries(null);
        setLevel(null);
        setConfigurations(null);
        setIncludedFiles(null);
    }
    
    public boolean isSimpleMode() {
        return simple;
    }
    
    public void setSimpleMode(boolean simple) {
        this.simple = simple;
    }

    public boolean isCutResult() {
        return cutResult;
    }

    public void setCutResult(boolean cutResult) {
        this.cutResult = cutResult;
    }
   
    private static class DiscoveryWizardDescriptorAdapter implements DiscoveryDescriptor{
        private WizardDescriptor wizard;
        public DiscoveryWizardDescriptorAdapter(WizardDescriptor wizard){
            this.wizard = wizard;
        }
        
        public Project getProject(){
            return (Project) wizard.getProperty(PROJECT);
        }
        public void setProject(Project project){
            wizard.putProperty(PROJECT, project);
        }
        
        public String getRootFolder(){
            String root = (String) wizard.getProperty(ROOT_FOLDER);
            if (root == null) {
                // field in project wizard
                root = (String)wizard.getProperty("buildCommandWorkingDirTextField"); // NOI18N
                if (root != null && Utilities.isWindows()) {
                    root = root.replace('\\','/');
                }
            }
            return root;
        }
        public void setRootFolder(String root){
            wizard.putProperty(INVOKE_PROVIDER, Boolean.TRUE);
            if (root != null && Utilities.isWindows()) {
                root = root.replace('\\','/');
            }
            wizard.putProperty(ROOT_FOLDER, root);
        }
        
        public String getBuildResult() {
            return (String) wizard.getProperty(BUILD_RESULT);
        }
        
        public void setBuildResult(String binaryPath) {
            wizard.putProperty(BUILD_RESULT, binaryPath);
        }
        
        public String getAditionalLibraries() {
            return (String) wizard.getProperty(ADDITIONAL_LIBRARIES);
        }
        
        public void setAditionalLibraries(String binaryPath) {
            wizard.putProperty(ADDITIONAL_LIBRARIES, binaryPath);
        }
        
        public DiscoveryProvider getProvider(){
            return (DiscoveryProvider) wizard.getProperty(PRIVIDER);
        }
        public String getProviderID(){
            DiscoveryProvider provider =(DiscoveryProvider) wizard.getProperty(PRIVIDER);
            if (provider != null){
                return provider.getID();
            }
            return null;
        }
        public void setProvider(DiscoveryProvider provider){
            wizard.putProperty(INVOKE_PROVIDER, Boolean.TRUE);
            wizard.putProperty(PRIVIDER, provider);
        }
        
        public String getLevel(){
            return (String) wizard.getProperty(CONSOLIDATION_STRATEGY);
        }
        public void setLevel(String level){
            wizard.putProperty(CONSOLIDATION_STRATEGY, level);
        }
        
        public List<ProjectConfiguration> getConfigurations(){
            return (List<ProjectConfiguration>) wizard.getProperty(CONFIGURATIONS);
        }
        public void setConfigurations(List<ProjectConfiguration> configuration){
            wizard.putProperty(CONFIGURATIONS, configuration);
        }
        
        public List<String> getIncludedFiles(){
            return (List<String>) wizard.getProperty(INCLUDED);
        }
        public void setIncludedFiles(List<String> includedFiles){
            wizard.putProperty(INCLUDED, includedFiles);
        }
        
        public boolean isInvokeProvider(){
            Boolean res = (Boolean)wizard.getProperty(INVOKE_PROVIDER);
            if (res == null) {
                return true;
            }
            return res.booleanValue();
        }
        
        public void setInvokeProvider(boolean invoke){
            if (invoke) {
                wizard.putProperty(INVOKE_PROVIDER, Boolean.TRUE);
            } else {
                wizard.putProperty(INVOKE_PROVIDER, Boolean.FALSE);
            }
        }
        
        public boolean isSimpleMode() {
            return true;
        }
        
        public void setSimpleMode(boolean simple) {
        }

        public boolean isCutResult() {
            return false;
        }

        public void setCutResult(boolean cutResult) {
        }
        
        public void setMessage(String message) {
            wizard.putProperty("WizardPanel_errorMessage", message); // NOI18N
        }
        
        public void clean() {
            setProject(null);
            setProvider(null);
            setRootFolder(null);
            setBuildResult(null);
            setAditionalLibraries(null);
            setLevel(null);
            setConfigurations(null);
            setIncludedFiles(null);
        }
    }

    private static class DiscoveryWizardClone implements DiscoveryDescriptor{
        private Map<String, Object> map;
        
        public DiscoveryWizardClone(Map<String, Object> map){
            this.map = map;
        }
        
        public Project getProject(){
            return (Project) map.get(PROJECT);
        }
        public void setProject(Project project){
            map.put(PROJECT, project);
        }
        
        public String getRootFolder(){
            String root = (String) map.get(ROOT_FOLDER);
            if (root == null) {
                // field in project wizard
                root = (String)map.get("buildCommandWorkingDirTextField"); // NOI18N
                if (root != null && Utilities.isWindows()) {
                    root = root.replace('\\','/');
                }
            }
            return root;
        }
        public void setRootFolder(String root){
            map.put(INVOKE_PROVIDER, Boolean.TRUE);
            if (root != null && Utilities.isWindows()) {
                root = root.replace('\\','/');
            }
            map.put(ROOT_FOLDER, root);
        }
        
        public String getBuildResult() {
            return (String) map.get(BUILD_RESULT);
        }
        
        public void setBuildResult(String binaryPath) {
            map.put(BUILD_RESULT, binaryPath);
        }
        
        public String getAditionalLibraries() {
            return (String) map.get(ADDITIONAL_LIBRARIES);
        }
        
        public void setAditionalLibraries(String binaryPath) {
            map.put(ADDITIONAL_LIBRARIES, binaryPath);
        }
        
        public DiscoveryProvider getProvider(){
            return (DiscoveryProvider) map.get(PRIVIDER);
        }
        public String getProviderID(){
            DiscoveryProvider provider =(DiscoveryProvider) map.get(PRIVIDER);
            if (provider != null){
                return provider.getID();
            }
            return null;
        }
        public void setProvider(DiscoveryProvider provider){
            map.put(INVOKE_PROVIDER, Boolean.TRUE);
            map.put(PRIVIDER, provider);
        }
        
        public String getLevel(){
            return (String) map.get(CONSOLIDATION_STRATEGY);
        }
        public void setLevel(String level){
            map.put(CONSOLIDATION_STRATEGY, level);
        }
        
        public List<ProjectConfiguration> getConfigurations(){
            return (List<ProjectConfiguration>) map.get(CONFIGURATIONS);
        }
        public void setConfigurations(List<ProjectConfiguration> configuration){
            map.put(CONFIGURATIONS, configuration);
        }
        
        public List<String> getIncludedFiles(){
            return (List<String>) map.get(INCLUDED);
        }
        public void setIncludedFiles(List<String> includedFiles){
            map.put(INCLUDED, includedFiles);
        }
        
        public boolean isInvokeProvider(){
            Boolean res = (Boolean)map.get(INVOKE_PROVIDER);
            if (res == null) {
                return true;
            }
            return res.booleanValue();
        }
        
        public void setInvokeProvider(boolean invoke){
            if (invoke) {
                map.put(INVOKE_PROVIDER, Boolean.TRUE);
            } else {
                map.put(INVOKE_PROVIDER, Boolean.FALSE);
            }
        }
        
        public boolean isSimpleMode() {
            return true;
        }
        
        public void setSimpleMode(boolean simple) {
        }

        public boolean isCutResult() {
            return false;
        }

        public void setCutResult(boolean cutResult) {
        }
        
        public void setMessage(String message) {
            map.put("WizardPanel_errorMessage", message); // NOI18N
        }
        
        public void clean() {
            setProject(null);
            setProvider(null);
            setRootFolder(null);
            setBuildResult(null);
            setAditionalLibraries(null);
            setLevel(null);
            setConfigurations(null);
            setIncludedFiles(null);
        }
    }

}
