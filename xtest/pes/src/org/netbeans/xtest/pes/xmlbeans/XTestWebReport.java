/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
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
