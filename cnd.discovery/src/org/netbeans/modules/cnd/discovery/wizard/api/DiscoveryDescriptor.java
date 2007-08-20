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

package org.netbeans.modules.cnd.discovery.wizard.api;

import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProvider;

/**
 *
 * @author Alexander Simon
 */
public interface DiscoveryDescriptor {

    Project getProject();
    void setProject(Project project);
    
    DiscoveryProvider getProvider();
    String getProviderID();
    void setProvider(DiscoveryProvider provider);

    String getRootFolder();
    void setRootFolder(String root);

    String getBuildResult();
    void setBuildResult(String binaryPath);

    String getAditionalLibraries();
    void setAditionalLibraries(String binaryPath);

    String getLevel();
    void setLevel(String level);

    List<ProjectConfiguration> getConfigurations();
    void setConfigurations(List<ProjectConfiguration> configuration);

    List<String> getIncludedFiles();
    void setIncludedFiles(List<String> includedFiles);

    boolean isInvokeProvider();
    void setInvokeProvider(boolean invoke);
    
    boolean isSimpleMode();
    void setSimpleMode(boolean simple);
    
    void setMessage(String message);

    void clean();
}
