/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.discovery.wizard;

import java.util.List;
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
public class DiscoveryWizardDescriptor extends WizardDescriptor implements DiscoveryDescriptor{
    private static final String PROJECT = "project"; // NOI18N
    private static final String PRIVIDER = "provider"; // NOI18N
    private static final String ROOT_FOLDER = "rootFolder"; // NOI18N
    private static final String CONSOLIDATION_STRATEGY = "consolidationLevel"; // NOI18N
    private static final String CONFIGURATIONS = "configurations"; // NOI18N
    private static final String INCLUDED = "included"; // NOI18N
    private static final String ADDITIONAL = "additionalFiles"; // NOI18N
    private static final String INVOKE_PROVIDER = "invokeProvider"; // NOI18N
    
    private boolean stateChanged = true;
    
    public DiscoveryWizardDescriptor(WizardDescriptor.Iterator panels){
        super(panels);
    }
    
    public static DiscoveryDescriptor adaptee(Object wizard){
        if (wizard instanceof DiscoveryDescriptor) {
            return (DiscoveryDescriptor) wizard;
        }
        return new DiscoveryWizardDescriptorAdapter((WizardDescriptor)wizard);
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
    
    public DiscoveryProvider getProvider(){
        return (DiscoveryProvider) getProperty(PRIVIDER);
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
    
    public List<String> getAdditionalFiles(){
        return (List<String>) getProperty(ADDITIONAL);
    }
    public void setAdditionalFiles(List<String> additionalFiles){
        putProperty(ADDITIONAL, additionalFiles);
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
        setLevel(null);
        setConfigurations(null);
        setIncludedFiles(null);
        setAdditionalFiles(null);
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
        
        public DiscoveryProvider getProvider(){
            return (DiscoveryProvider) wizard.getProperty(PRIVIDER);
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
        
        public List<String> getAdditionalFiles(){
            return (List<String>) wizard.getProperty(ADDITIONAL);
        }
        public void setAdditionalFiles(List<String> additionalFiles){
            wizard.putProperty(ADDITIONAL, additionalFiles);
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
        
        public void setMessage(String message) {
            wizard.putProperty("WizardPanel_errorMessage", message); // NOI18N
        }

        public void clean() {
            setProject(null);
            setProvider(null);
            setRootFolder(null);
            setLevel(null);
            setConfigurations(null);
            setIncludedFiles(null);
            setAdditionalFiles(null);
        }
    }
}