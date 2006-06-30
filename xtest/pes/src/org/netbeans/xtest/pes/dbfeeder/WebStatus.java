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

package org.netbeans.xtest.pes.dbfeeder;

import org.netbeans.xtest.xmlserializer.*;
import java.io.*;
import java.util.*;
import org.netbeans.xtest.pes.PESLogger;
import org.netbeans.xtest.pes.xmlbeans.*;

public class WebStatus implements XMLSerializable {

    static ClassMappingRegistry classMappingRegistry = new ClassMappingRegistry(WebStatus.class);
    static {
        try {
            classMappingRegistry.registerContainerField("projects","project",ClassMappingRegistry.DIRECT);
            //classMappingRegistry.registerSimpleField("team", ClassMappingRegistry.ATTRIBUTE, "team");
            classMappingRegistry.registerSimpleField("webURL", ClassMappingRegistry.ATTRIBUTE, "webURL");
            
        } catch (MappingException me) {
            me.printStackTrace();
            classMappingRegistry = null;
        }
    }
    

    
    public ClassMappingRegistry registerXMLMapping() {
        return classMappingRegistry;
    }
    
    // empty constructor - required by XMLSerializer
    public WebStatus() {}

    public WebStatus(PESWeb pesWeb) {
        this.pesWeb = pesWeb;
        this.webURL = pesWeb.getWebURL();
    }
    
    
    public String getWebURL() {
        return webURL;
    }
    
    // webURL of the processeed web
    private String webURL;
    
    // team name who owns the web
    //private String team;
    // getter for team
    /*
    public String getTeam() {
        return team;
    }
     **/    

    
    // the pes web which is going to be taken care of 
    private PESWeb pesWeb;
    
    /** Getter for property projectStatus.
     * @return Value of property projectStatus.
     *
     */
    public ProjectStatus[] getProjectStatus() {
        return this.projects;
    }    
    
    
    private ProjectStatus[] projects;
    
    /* the only bussiness method of this class - it scans through this web metadata,
     * finds all unique projects and sends the information about them
     * I really don't like this method - it must use the old XMLBean stuff
     * which is no longer being developed (use xmlserializer instead)
     */
    public boolean scanWebMetadata() {
        try {
            // for each project group - load metadata            
            ArrayList projectList = new ArrayList();
            for (int i=0; i < pesWeb.xmlel_PESProjectGroup.length; i++) {
                // only if this project group is uploaded to web
                if (pesWeb.isUploadToDatabase()) {
                    ManagedGroup managedGroup = ManagedGroup.loadManagedGroup(pesWeb, pesWeb.xmlel_PESProjectGroup[i]);
                    Collection uniqueProjects = managedGroup.getUniqueProjects();
                    Iterator projectIterator = uniqueProjects.iterator();
                    while (projectIterator.hasNext()) {
                        String projectName = (String)projectIterator.next();
                        // get the last build available in this project
                        Collection builds = managedGroup.getUniqueBuilds(projectName);
                        String lastBuild = (String)Collections.min(builds);
                        // get the last build available in with the new status (i.e. builds with full details)
                        builds = managedGroup.getUniqueBuildsWithStatus(projectName, ManagedReport.NEW);
                        String lastBuildWithFullDetails = (String)Collections.min(builds);
                        // now get all teams contributing to this project
                        Collection projectTeams = managedGroup.getUniqueTeams(projectName);
                        if (projectTeams.size() > 0) {
                            Iterator teamIterator = projectTeams.iterator();
                            while (teamIterator.hasNext()) {
                                String teamName = (String)teamIterator.next();
                                projectList.add(new ProjectStatus(projectName, teamName, lastBuild, lastBuildWithFullDetails));
                            }
                        } else {
                            // looks like no team contributes to this project ... strange
                            String teamName = pesWeb.getTeam(); ////
                            if (teamName != null) {
                                PESLogger.logger.fine("No teams found to be contributing to project "+projectName+", assiging default team name:"+teamName);
                                projectList.add(new ProjectStatus(projectName, teamName, lastBuild, lastBuildWithFullDetails));
                            } else {
                                PESLogger.logger.warning("Cannot determine default team for PES. Please add team attribute to PESConfig element in the config file"); 
                            }
                        }
                    /*
                    // get number of unique builds available
                    int buildsAvailable = managedGroup.getUniqueBuilds(projectName).size();
                    // get the number of builds with full Details (do we need this ?)
                    int fullDetailsBuildsAvailable = managedGroup.getUniqueBuildsWithStatus(projectName, ManagedReport.NEW).size();
                    // ergwelrgwoeinrg owinrg
                    projectList.add(new ProjectStatus(projectName, buildsAvailable, fullDetailsBuildsAvailable));
                     **/
                    }
                }
            }

            // convert project list to array
            projects = (ProjectStatus[])projectList.toArray(new ProjectStatus[0]);
            // done ...
        } catch (Exception e) {
            System.err.println("VWERVWERVWERV EXCEPTION:");
            e.printStackTrace(System.err);
        }
        return true;        
    }

}
