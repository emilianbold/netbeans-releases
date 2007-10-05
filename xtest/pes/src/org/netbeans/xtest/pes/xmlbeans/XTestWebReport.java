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
/*
 * TestedBuild.java
 *
 * Created on December 5, 2001, 5:04 PM
 */

package org.netbeans.xtest.pes.xmlbeans;

import org.netbeans.xtest.pe.xmlbeans.*;
import java.util.*;

/**
 *
 * @author  mb115822
 * @version
 */
public class XTestWebReport extends XMLBean {

    /** Creates new TestedBuild */
    public XTestWebReport() {
    }


    public String xmlat_oldBuilds;

    public ManagedReport[] xmlel_ManagedReport;



    // business methods
    public Collection  getUniqueProjects() {
        HashSet uniqueProjectsSet = new HashSet();
        if (xmlel_ManagedReport != null) {
            for (int i=0; i< xmlel_ManagedReport.length; i++) {
                String currentProject = xmlel_ManagedReport[i].xmlat_project;
                if (!uniqueProjectsSet.contains(currentProject)) {
                    uniqueProjectsSet.add(currentProject);
                }
            }
        }
        return uniqueProjectsSet;
    }
    
    public Collection getUniqueHosts() {
        HashSet uniqueHostsSet = new HashSet();
        if (xmlel_ManagedReport != null) {
            for (int i=0; i< xmlel_ManagedReport.length; i++) {
                String currentHost = xmlel_ManagedReport[i].xmlat_host;
                if (!uniqueHostsSet.contains(currentHost)) {
                    uniqueHostsSet.add(currentHost);
                }
            }
        }
        return uniqueHostsSet;
    }
    
    public Collection getUniqueHosts(String inProject) {
        HashSet uniqueHostsSet = new HashSet();
        if (xmlel_ManagedReport != null) {
            for (int i=0; i< xmlel_ManagedReport.length; i++) {
                if (inProject != null) {
                    if (!inProject.equals(xmlel_ManagedReport[i].xmlat_project)) {
                        // this is not our project !!!
                        continue;
                    }                
                }
                String currentHost = xmlel_ManagedReport[i].xmlat_host;
                if (!uniqueHostsSet.contains(currentHost)) {
                    uniqueHostsSet.add(currentHost);
                }
            }
        }
        return uniqueHostsSet;        
    }
    
    public Collection getUniqueBuilds() {
        return getUniqueBuilds(null,null,null,null);
    }
    
    public Collection getUniqueBuilds(String inProject) {
        HashSet uniqueBuildsSet = new HashSet();
        if (xmlel_ManagedReport != null) {
            for (int i=0; i< xmlel_ManagedReport.length; i++) {
                if (inProject != null) {
                    if (!inProject.equals(xmlel_ManagedReport[i].xmlat_project)) {
                        // this is not our project !!!
                        continue;
                    }                
                }
                String currentBuild = xmlel_ManagedReport[i].xmlat_build;
                if (!uniqueBuildsSet.contains(currentBuild)) {
                    uniqueBuildsSet.add(currentBuild);
                }
            }
        }
        return uniqueBuildsSet; 
    }
    
    public Collection getUniqueBuilds(String inProject, String fromHost) {
        HashSet uniqueBuildsSet = new HashSet();
        if (xmlel_ManagedReport != null) {
            for (int i=0; i< xmlel_ManagedReport.length; i++) {
                if (inProject != null) {
                    if (!inProject.equals(xmlel_ManagedReport[i].xmlat_project)) {
                        // this is not our project !!!
                        continue;
                    }                
                }
                if (fromHost != null) {
                    if (!fromHost.equals(xmlel_ManagedReport[i].xmlat_host)) {
                        // this is not our host !!!
                        continue;
                    }                
                }
                String currentBuild = xmlel_ManagedReport[i].xmlat_build;
                if (!uniqueBuildsSet.contains(currentBuild)) {
                    uniqueBuildsSet.add(currentBuild);
                }
            }
        }
        return uniqueBuildsSet; 
    }
    
    public Collection getUniqueTestingGroups(String inProject) {
        HashSet uniqueGroupsSet = new HashSet();
        if (xmlel_ManagedReport != null) {
            for (int i=0; i< xmlel_ManagedReport.length; i++) {
                if (inProject != null) {
                    if (!inProject.equals(xmlel_ManagedReport[i].xmlat_project)) {
                        // this is not our project !!!
                        continue;
                    }                
                }
                String currentGroup = xmlel_ManagedReport[i].xmlat_testingGroup;
                if (!uniqueGroupsSet.contains(currentGroup)) {
                    uniqueGroupsSet.add(currentGroup);
                }
            }
        }
        return uniqueGroupsSet;
    }
    
    public Collection getUniqueTestedTypes(String inProject, String inGroup) {     
        HashSet uniqueTypesSet = new HashSet();        
        if (xmlel_ManagedReport != null) {
            for (int i=0; i< xmlel_ManagedReport.length; i++) {
                if (inProject != null) {
                    if (!inProject.equals(xmlel_ManagedReport[i].xmlat_project)) {
                        // this is not our project !!!
                        continue;
                    }
                }                
                if (inGroup != null) {
                    if (!inGroup.equals((xmlel_ManagedReport[i].xmlat_testingGroup))) {
                        continue;
                    }
                }
                String currentType = xmlel_ManagedReport[i].xmlat_testedType;
                if (!uniqueTypesSet.contains(currentType)) {
                    uniqueTypesSet.add(currentType);
                }
            }
        }
        return uniqueTypesSet;
    }
    
    public Collection getUniqueHosts(String inProject, String inGroup, String inType) {
        HashSet uniqueHostsSet = new HashSet();
        if (xmlel_ManagedReport != null) {
            for (int i=0; i< xmlel_ManagedReport.length; i++) {
                if (inProject != null) {
                    if (!inProject.equals(xmlel_ManagedReport[i].xmlat_project)) {
                        // this is not our project !!!
                        continue;
                    }                
                }
                if (inGroup != null) {
                    if (!inGroup.equals((xmlel_ManagedReport[i].xmlat_testingGroup))) {
                        continue;
                    }
                }
                if (inType != null) {
                    if (!inGroup.equals((xmlel_ManagedReport[i].xmlat_testingGroup))) {
                        continue;
                    }
                }
                String currentHost = xmlel_ManagedReport[i].xmlat_host;
                if (!uniqueHostsSet.contains(currentHost)) {
                    uniqueHostsSet.add(currentHost);
                }
            }
        }
        return uniqueHostsSet;        
    }
    
    
    
    public Collection getUniqueBuilds(String inProject, String inGroup, String inType) {
        return getUniqueBuilds(inProject,inGroup,inType,null);
    }
    
    
    public Collection getUniqueBuilds(String inProject, String inGroup, String inType, String fromHost) {
        HashSet uniqueBuildsSet = new HashSet();
        if (xmlel_ManagedReport != null) {
            for (int i=0; i< xmlel_ManagedReport.length; i++) {
                if (inProject != null) {
                    if (!inProject.equals(xmlel_ManagedReport[i].xmlat_project)) {
                        // this is not our project !!!
                        continue;
                    }                
                }
                if (inGroup != null) {
                    if (!inGroup.equals((xmlel_ManagedReport[i].xmlat_testingGroup))) {
                        continue;
                    }
                }
                if (inType != null) {
                    if (!inGroup.equals((xmlel_ManagedReport[i].xmlat_testingGroup))) {
                        continue;
                    }
                }
                if (fromHost != null) {
                    if (!fromHost.equals(xmlel_ManagedReport[i].xmlat_host)) {
                        // this is not our host !!!
                        continue;
                    }                
                }
                String currentBuild = xmlel_ManagedReport[i].xmlat_build;
                if (!uniqueBuildsSet.contains(currentBuild)) {
                    uniqueBuildsSet.add(currentBuild);
                }
            }
        }
        return uniqueBuildsSet; 
    }
    
    
    
    
    public ManagedReport[] filterBy(String project, String build, String testingGroup, String testedType, String host) {
        if (this.xmlel_ManagedReport == null) {
            return null;
        }
        ManagedReport[] filteredReport = new ManagedReport[this.xmlel_ManagedReport.length];
        int newSize = 0;
        for (int i=0; i<xmlel_ManagedReport.length; i++) {
            if (xmlel_ManagedReport[i].isEqualTo(project,build,testingGroup,testedType, host)) {
                filteredReport [newSize] = xmlel_ManagedReport[i];
                newSize++;
            }
        }
        ManagedReport[] resultingReport = new ManagedReport[newSize];
        for (int i=0; i<newSize; i++) {
            resultingReport[i] = filteredReport[i];
        }

        return resultingReport;
    }
    
    
    // select only projects listed in projects array
    public ManagedReport[] filterByProjects(String projects[]) {
        if (this.xmlel_ManagedReport == null) {
            return null;
        }
        if (projects == null) {
            throw new IllegalArgumentException("argument cannot be null");            
        }
        
        ManagedReport[] filteredReport = new ManagedReport[this.xmlel_ManagedReport.length];
        int newSize = 0;
        for (int i=0; i<xmlel_ManagedReport.length; i++) {
            for ( int j=0; j < projects.length; j++) {
                if (projects[j].equals(xmlel_ManagedReport[i].xmlat_project)) {
                    filteredReport [newSize] = xmlel_ManagedReport[i];
                    newSize++;
                    break;
                }   
            }
        }
        ManagedReport[] resultingReport = new ManagedReport[newSize];
        for (int i=0; i<newSize; i++) {
            resultingReport[i] = filteredReport[i];
        }

        return resultingReport;
    }
    
    // select only projects not listed in the array
    public ManagedReport[] filterProjects(String projects[]) {
        if (this.xmlel_ManagedReport == null) {
            return null;
        }
        if (projects == null) {
            throw new IllegalArgumentException("argument cannot be null");            
        }
        
        ManagedReport[] filteredReport = new ManagedReport[this.xmlel_ManagedReport.length];
        int newSize = 0;
        for (int i=0; i<xmlel_ManagedReport.length; i++) {
            int j = 0;
            for ( j=0; j < projects.length; j++) {
                if (projects[j].equals(xmlel_ManagedReport[i].xmlat_project)) {
                    break;
                }
            }
            if ( j == projects.length ) {
                // this report is not of any given project - add it to result
                filteredReport [newSize] = xmlel_ManagedReport[i];
                newSize++;
            }
        }
        ManagedReport[] resultingReport = new ManagedReport[newSize];
        for (int i=0; i<newSize; i++) {
            resultingReport[i] = filteredReport[i];
        }

        return resultingReport;
    }
        
}
