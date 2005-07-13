/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


/*
 * ManagedGroup.java
 *
 * Created on May 27, 2002, 9:49 PM
 */

package org.netbeans.xtest.pes.xmlbeans;

import org.netbeans.xtest.pe.xmlbeans.*;
import org.netbeans.xtest.util.*;
import org.netbeans.xtest.pes.*;
import java.io.*;
import java.util.*;
import javax.xml.transform.*;
import org.w3c.dom.*;

// logging stuff
import org.netbeans.xtest.pes.PESLogger;
import java.util.logging.Level;


/**
 *
 * @author  breh
 */
public class ManagedGroup extends PESProjectGroup {
    
    /** Creates a new instance of ManagedGroup */
    public ManagedGroup() {
    }
    
    
    // elements
    public ManagedReport[] xmlel_ManagedReport;
    
    // private stuff
    private File groupFile;
    
    private IncomingReport[] incomings;
    private PESWeb pesWeb;
    private PESProjectGroup pesProjectGroup;
    
    // bussiness methods
    /*
    public String getGroupFilename() {
        String name = GROUP_PREFIX+xmlat_name+XMLBEAN_FILE_SUFFIX;
        return FileUtils.normalizeName(name);
    }
    */
    
    public static ManagedGroup getDefault(PESWeb pesWeb, PESProjectGroup pesGroup) {
        ManagedGroup group = new ManagedGroup();
        group.xmlel_ManagedReport = new ManagedReport[0];
        group.readPESProjectGroup(pesGroup);
        group.pesWeb = pesWeb;
        group.pesProjectGroup = pesGroup;        
        return group;
    }
    
    public static ManagedGroup loadManagedGroup(PESWeb web, PESProjectGroup group) throws IOException  {
        //String filename =
        File groupData = new File(web.getDataDir(),getDataFilename(group));
        if (groupData.isFile()) {
            PESLogger.logger.finest("loading ManagedGroup from "+groupData);
            XMLBean aBean = null;
            try {
                aBean = XMLBean.loadXMLBean(groupData);
            } catch (ClassNotFoundException cnfe) {
                System.out.println("CNFE:"+cnfe);
            }
            if ((aBean == null) | !(aBean instanceof ManagedGroup)) {
                // this should not happen very often
                PESLogger.logger.finest("ManagedGroup could not be loaded from "+groupData);
                PESLogger.logger.finest("Installing new group");
                ManagedGroup mg =  ManagedGroup.getDefault(web,group);
                mg.groupFile = groupData;
                return mg;
            } else {
                ManagedGroup mg =  (ManagedGroup) aBean;
                mg.readPESProjectGroup(group);
                mg.pesWeb = web;
                mg.pesProjectGroup = group;
                mg.groupFile = groupData;
                return mg;
            }
        } else {
             ManagedGroup mg =  ManagedGroup.getDefault(web,group);
             mg.groupFile = groupData;
             return mg;
        }
    }
    

    
    public void readPESProjectGroup(PESProjectGroup ppg) {
        this.xmlat_name = ppg.xmlat_name;
        this.xmlat_description = ppg.xmlat_description;
        this.xmlat_historyMatrices = ppg.xmlat_historyMatrices;
        this.xmlat_main = ppg.xmlat_main;
        this.xmlat_detailedData = ppg.xmlat_detailedData;
        this.xmlat_deleteAge = ppg.xmlat_deleteAge;
        this.xmlat_currentBuilds = ppg.xmlat_currentBuilds;
        this.xmlel_PESProject = null;        
    }
    

    public void addIncomingReports(IncomingReport[] incomings) {
        PESLogger.logger.fine("adding incoming reports");
        this.incomings = incomings;
        if (incomings == null) {
            return;
        }
        ManagedReport[] newReports = new ManagedReport[incomings.length];
        for (int i=0; i < incomings.length; i++) {
             ManagedReport mr = new ManagedReport();             
             mr.readManagedReport(incomings[i]);
            // are hostnames mapped ?
            if (pesWeb.areHostnamesMapped()) {
                String mappedHostname = pesWeb.getMappedHostname(mr);
                mr.setMappedHostname(mappedHostname);
                PESLogger.logger.finer("Mapping hostname "+mr.getHost()+" to "+mr.getMappedHostname());
            }
            
             if (incomings[i].isReplace()) {
                 if (xmlel_ManagedReport != null) {
                     int index = mr.indexOf(xmlel_ManagedReport);
                     if (index > -1) {
                        xmlel_ManagedReport[index] = mr;
                        continue;
                    }
                 }
                 int index = mr.indexOf(newReports);
                 if (index > -1) {
                     newReports[index] = mr;
                     continue;
                 }                 
             }
             // well, add it
             newReports[i] = mr;             
        }
        // 
        newReports = (ManagedReport[])XMLBean.shrinkArray(newReports);
        // now add the array to the existing one
        if (xmlel_ManagedReport == null) {
            xmlel_ManagedReport = newReports;
        } else {
            xmlel_ManagedReport = (ManagedReport[])XMLBean.addToArray(xmlel_ManagedReport,newReports);
        }
    }
    
    
    // process web   
    public void processWebPages() {
        PESLogger.logger.fine("processing web pages");
        
        // are there any incomings ?
        if ((incomings != null)&(incomings.length > 0))
            // convert main group file
            
            try {
                File groupHTMLFile = getGroupHTMLFile();
                PESLogger.logger.finer("Converting summary to:"+groupFile);
                
                //get webdata file
                File webDataFile = new File(groupFile.getParentFile(),ManagedWeb.getDataFilename());
                // prepare transformer
                Transformer transformer = XSLTransformers.getProjectsSummaryTransformer();
                transformer.setParameter("pesWebDataFile",webDataFile.getAbsolutePath());
                XSLUtils.transform(transformer,this.toDocument(),groupHTMLFile);
            } catch (Exception e) {
                PESLogger.logger.log(Level.WARNING,"Transforming unsucessfull",e);
            }
        
        // convert all projects
        Iterator projects = ManagedGroup.getUniqueProjects(incomings).iterator();
        while (projects.hasNext()) {
            String project = (String)projects.next();            
            
            // overall build summaries/history matrices/navigator frames
            
            Iterator departments = ManagedGroup.getUniqueTestingGroups(incomings,project).iterator();
            while (departments.hasNext()) {
                String department = (String)departments.next();
                Iterator types = ManagedGroup.getUniqueTestedTypes(incomings,project,department).iterator();
                while (types.hasNext()) {
                    String type = (String)types.next();
                    PESLogger.logger.fine("Processing project:"+project+" department:"+department+" type:"+type);
                    try {
                        ManagedGroup filteredGroup = ManagedGroup.getDefault(this.pesWeb,this.pesProjectGroup);
                        filteredGroup.readPESProjectGroup(pesProjectGroup);
                        filteredGroup.xmlel_ManagedReport = this.filterBy(project,null,department,type,null);
                        Document filteredDOM = filteredGroup.toDocument();
                        
                        File projectFile = getProjectHTMLFile(project,department,type,true);     
                        PESLogger.logger.finer("Converting to:"+projectFile);

                        Transformer transformer = XSLTransformers.getBuildsSummaryTransformer();
                        
                        XSLUtils.transform(transformer,filteredDOM,projectFile);

                        if (xmlat_currentBuilds > 0) {
                            projectFile = getProjectHTMLFile(project,department,type,false);
                            PESLogger.logger.finest("As well as to:"+projectFile);
                            transformer.setParameter("oldBuilds","true");                            
                            XSLUtils.transform(transformer,filteredDOM,projectFile);
                        }
                        // here goes the matrix ...
                        if (getHistoryMatricesAge() > 0) {
                            // do history matrices
                            PESLogger.logger.finer("Creating HistoryMatrices, age="+getHistoryMatricesAge());
                            Transformer historyTransformer = XSLTransformers.getBuildsHistoryMatrixTransformer();
                            historyTransformer.setParameter("includeExceptions", ""+pesWeb.includeExceptions());
                            CreateHistoryMatrices matrices = new CreateHistoryMatrices(filteredGroup,historyTransformer);
                            matrices.setBuildHistoryAge(getHistoryMatricesAge());
                            matrices.createMatrices(project);
                            
                        }                        
                        // clean up if applicable
                        if (getDetailedDataAge() >= 0) {
                            // clean up detailed data of some age()
                            PESLogger.logger.finer("Setting trasinet flag with age "+getDetailedDataAge());
                            setManagedReportsFlag(getDetailedDataAge(), project, department, type, ManagedReport.OLD);
                        }
                        // delete old builds if applicable
                        if (getDeleteAge() > 0) {
                            // delete all builds
                            PESLogger.logger.finer("Setting delete flag with age "+getDeleteAge());
                            setManagedReportsFlag(getDeleteAge(), project, department, type, ManagedReport.TO_BE_DELETED);
                        }
                        // create (recreate) build summary pages for all incoming builds
                        
                        Iterator builds = ManagedGroup.getUniqueBuilds(incomings,project, department,type,null).iterator();
                        ManagedGroup buildFilteredGroup = ManagedGroup.getDefault(this.pesWeb,this.pesProjectGroup);
                        buildFilteredGroup.readPESProjectGroup(pesProjectGroup);
                        // for each found build create summary page
                        while (builds.hasNext()) {
                            String build = (String)builds.next();
                            buildFilteredGroup.xmlel_ManagedReport =filteredGroup.filterBy(project,build,department,type,null);
                            Document buildFilteredDOM = buildFilteredGroup.toDocument();
                            
                            File buildFile = getBuildHTMLFile(project,department,type,build);
                            PESLogger.logger.finer("Converting build summary to:"+buildFile);
                            Transformer buildTransformer = XSLTransformers.getBuildSummaryTransformer();                           
                            
                            try {
                                XSLUtils.transform(buildTransformer,buildFilteredDOM,buildFile);
                            } catch (TransformerException te) {
                                PESLogger.logger.log(Level.WARNING,"Build summary transforming unsucessfull",te);
                            } catch (IOException ioe) {
                                PESLogger.logger.log(Level.WARNING,"Build summary transforming unsucessfull",ioe);
                            }
                        }
                        

                        
                    } catch (Exception e) {
                        PESLogger.logger.log(Level.WARNING,"Transforming unsucessfull",e);                        
                    }
                   
                }
            }
        }
        // clean up detailed data in all 'transient' files
        cleanUpAndRetransformTransients();
        // delete all files to be deleted (also remove them from metadata)
        cleanUpAndRemoveToBeDeleted();    
    }
    
    
    // save the group to groupFile
    public void saveManagedGroup() throws IOException {
        if (groupFile != null) {
            this.saveXMLBean(groupFile);
        } else {
            throw new IOException("group file is not specified - cannot save");
        }
    }
   
    
    
    
    
    
    
    // get main HTML file
    public File getGroupHTMLFile() throws IOException {
        String groupFilename;
        if (pesProjectGroup.isMain()) {
            groupFilename = "index.html";
        } else {            
            if (pesProjectGroup.getName() == null) {
                throw new IOException("Cannot create group HTML file, because name of the group is not set");
            }
            groupFilename = FileUtils.normalizeName("group-"+pesProjectGroup.getName()+".html");
        }
        File result = new File(pesWeb.getWebRoot(),groupFilename);
        return result;
    }
    
    // get file for given project in this group
    public File getProjectHTMLFile(String projectName, String department, String testType, boolean current) throws IOException {
        if (projectName == null | department == null | testType == null) {
            throw new IllegalArgumentException("arguments cannot be null");
        }
        File projectDir = new File(pesWeb.getWebRoot(),FileUtils.normalizeName(projectName));
        String prefix = "";
        if (!current) {
            prefix = "old-";
        }
        if (pesProjectGroup.getName() == null) {
            throw new IOException("Cannot create Project HTML file, because name of the group is not set");
        } else {
            prefix = prefix + pesProjectGroup.getName() + "-";
        }
        
        File projectFile = new File(projectDir,FileUtils.normalizeName(prefix+department+"-"+testType+".html"));
        return projectFile;
    }
    
    
    // get file for build summary file
    public File getBuildHTMLFile(String projectName,String department,String testType, String buildNumber) throws IOException {
        if (projectName == null | department == null | testType == null | buildNumber == null) {
            throw new IllegalArgumentException("arguments cannot be null");
        }
        File buildDir = new File(pesWeb.getWebRoot(), FileUtils.normalizeName(projectName+"/"+buildNumber+"/"+department+"-"+testType));
        File buildFile = new File(buildDir, "index.html");
        return buildFile;
    }
    
    // get filename of xml file with ManagedReports for this group
    public static String getDataFilename(PESProjectGroup group) {
        String name = "groupdata-"+group.getName()+".xml";
        return FileUtils.normalizeName(name);
    }
    
    
    // get webroot of this web
    public PESWeb getPESWeb() {
        return pesWeb;
    }
    
    
    
    // cleanup/ transient stuff
    
   public void setManagedReportsFlag(int age, String project, String department, String type, short flag) {
       // find 'age' number of builds in given project/department/type and if anything older than 'age'
       // is still new - set it's status to transient
       // get all builds for this project/department,type                            
       ManagedReport[] reports = this.filterBy(project,null,department,type,null);
       String[] uniqueBuilds = (String[])ManagedGroup.getUniqueBuilds(reports).toArray(new String[0]);              
       PESLogger.logger.finest("uniqueBuilds.length="+uniqueBuilds.length+" reports.length="+reports.length);
       if (uniqueBuilds.length > age) {
           Arrays.sort(uniqueBuilds);           
           String lastBuild = null;
           if (age > 0) {
               lastBuild = uniqueBuilds[uniqueBuilds.length-age];
           }
           PESLogger.logger.finest("last build ="+lastBuild);
           for (int i=0; i<reports.length; i++) {
               // find all builds older than lastBuilds
               if ((lastBuild == null) || (lastBuild.compareToIgnoreCase(reports[i].getBuild()) > 0)) {
                   switch (flag) {
                       case ManagedReport.OLD: 
                           // process only new reports
                           if (reports[i].isNew()) {
                              reports[i].setReportStatus(ManagedReport.IN_TRANSITION);
                           }
                           break;
                       case ManagedReport.TO_BE_DELETED:
                           // process only reports which are not already DELETED
                           if (!reports[i].isToBeDeleted()) {
                               reports[i].setReportStatus(ManagedReport.TO_BE_DELETED);
                           }
                           break;
                   }
               }
           }
       }              
   }
   
   
   public void cleanUpAndRetransformTransients() {
       if (xmlel_ManagedReport == null) {
           // there is nothing to do - return
           return;
       }
       PESLogger.logger.fine("cleaning up detailed data for transients");
       for (int i=0; i < xmlel_ManagedReport.length; i++) {
           ManagedReport report = xmlel_ManagedReport[i];
           if (report != null) {
               if (report.isInTransition()) {
                   try {
                       File reportRoot = new File(pesWeb.getWebRoot(),report.getPathToResultsRoot());
                       if (reportRoot.isDirectory()) {
                           try {
                               PESLogger.logger.finest("deleting detailed data for "+reportRoot);
                               report.deleteDetailedData(reportRoot,true,false);
                               PESLogger.logger.finest("retransforming report to not include links to detailed data");
                               // retransform report
                               XSLTransformers.transformResultsForServer(reportRoot, true, true, true, null, pesProjectGroup, true);
                               report.setReportStatus(ManagedReport.OLD);
                           } catch (IOException ioe) {
                               PESLogger.logger.log(Level.WARNING, "IOExcepting caught when deleting detailed Data at "+reportRoot,ioe);
                           } catch (Exception e) {
                               PESLogger.logger.log(Level.WARNING, "Excepting cautgh when retransforming transient report at "+reportRoot,e);
                           }
                       } else {
                           PESLogger.logger.warning("Report root "+reportRoot+" is not a valid directory");
                       }
                   } catch (IOException ioe) {
                       PESLogger.logger.log(Level.WARNING, "IOExcepting caught when determining report root "+report.getPathToResultsRoot(),ioe);
                   }
               }
           }
       }
   }
    
   public void cleanUpAndRemoveToBeDeleted()  {
       if (xmlel_ManagedReport == null) {
           // there is nothing to do - return
           return;
       }
       PESLogger.logger.fine("cleaning up to be deleted reports");
       for (int i=0; i < xmlel_ManagedReport.length; i++) {
           ManagedReport report = xmlel_ManagedReport[i];
           if (report != null) {
               if (report.isToBeDeleted()) {
                   try {
                       File buildRoot = new File(new File(pesWeb.getWebRoot(), 
                                                          FileUtils.normalizeName(report.getProject())), 
                                                 FileUtils.normalizeName(report.getBuild()));
                        PESLogger.logger.finest("deleting old reports at "+buildRoot);
                        if (FileUtils.delete(buildRoot)) {
                            // remove report from the collection
                            xmlel_ManagedReport[i] = null;
                        } else {
                            PESLogger.logger.warning("Cannot delete directory "+buildRoot);
                        }
                   } catch (IOException ioe) {
                       PESLogger.logger.log(Level.WARNING, "IOExcepting caught when determining web root.", ioe);
                   }
               }
           }
       }
       // now shrink array, so it does not contain any nulls;
       xmlel_ManagedReport = (ManagedReport[])XMLBean.shrinkArray(xmlel_ManagedReport);
   }
    
    
    
    
    
    
    
    
    //
    //
    // unique stuff
    //
       
       
    public static Collection getUniqueProjects(ManagedReport[] reports) {
        HashSet uniqueProjectsSet = new HashSet();
        if (reports != null) {
            for (int i=0; i< reports.length; i++) {
                String currentProject = reports[i].xmlat_project;
                if (!uniqueProjectsSet.contains(currentProject)) {
                    uniqueProjectsSet.add(currentProject);
                }
            }
        }
        return uniqueProjectsSet;
    }
    
   public Collection  getUniqueProjects() {
        return getUniqueProjects(xmlel_ManagedReport);
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
    
    
    public static Collection getUniqueTestingGroups(ManagedReport[] reports, String inProject) {
        HashSet uniqueGroupsSet = new HashSet();
        if (reports != null) {
            for (int i=0; i< reports.length; i++) {
                if (inProject != null) {
                    if (!inProject.equals(reports[i].xmlat_project)) {
                        // this is not our project !!!
                        continue;
                    }
                }
                String currentGroup = reports[i].xmlat_testingGroup;
                if (!uniqueGroupsSet.contains(currentGroup)) {
                    uniqueGroupsSet.add(currentGroup);
                }
            }
        }
        return uniqueGroupsSet;

    }
    
    
    public Collection getUniqueTestingGroups(String inProject) {
        return getUniqueTestingGroups(this.xmlel_ManagedReport, inProject);
    }
    
    
    public static Collection getUniqueTestedTypes(ManagedReport[] reports, String inProject, String inGroup) {     
     HashSet uniqueTypesSet = new HashSet();        
        if ( reports != null) {
            for (int i=0; i< reports.length; i++) {
                if (inProject != null) {
                    if (!inProject.equals(reports[i].xmlat_project)) {
                        // this is not our project !!!
                        continue;
                    }
                }                
                if (inGroup != null) {
                    if (!inGroup.equals((reports[i].xmlat_testingGroup))) {
                        continue;
                    }
                }
                String currentType = reports[i].xmlat_testedType;
                if (!uniqueTypesSet.contains(currentType)) {
                    uniqueTypesSet.add(currentType);
                }
            }
        }
        return uniqueTypesSet;        
    }
    
    
    public Collection getUniqueTestedTypes(String inProject, String inGroup) {
        return getUniqueTestedTypes(this.xmlel_ManagedReport, inProject, inGroup);   
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
                String currentHost = null;
                if (xmlel_ManagedReport[i].getMappedHostname() != null) {
                    currentHost = xmlel_ManagedReport[i].getMappedHostname();
                } else {
                    currentHost = xmlel_ManagedReport[i].getHost();
                }
                if (!uniqueHostsSet.contains(currentHost)) {
                    uniqueHostsSet.add(currentHost);
                }
            }
        }
        return uniqueHostsSet;        
    }
    
    
    
   private Collection getUniqueBuilds() {
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

 
    public Collection getUniqueBuildsWithStatus(String inProject, short status) {
        HashSet uniqueBuildsSet = new HashSet();
        if (xmlel_ManagedReport != null) {
            for (int i=0; i< xmlel_ManagedReport.length; i++) {
                if (inProject != null) {
                    if (!inProject.equals(xmlel_ManagedReport[i].xmlat_project)) {
                        // this is not our project !!!
                        continue;
                    }                
                }
                if (xmlel_ManagedReport[i].xmlat_reportStatus == status) {
                    String currentBuild = xmlel_ManagedReport[i].xmlat_build;
                    if (!uniqueBuildsSet.contains(currentBuild)) {
                        uniqueBuildsSet.add(currentBuild);
                    }
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

    
    
    public Collection getUniqueBuilds(String inProject, String inGroup, String inType) {
        return getUniqueBuilds(inProject,inGroup,inType,null);
    }
    
    
    public Collection getUniqueBuilds(String inProject, String inGroup, String inType, String fromHost) {
        return getUniqueBuilds(this.xmlel_ManagedReport, inProject, inGroup, inType, fromHost);
    }
    
    public static Collection getUniqueBuilds(ManagedReport[] reports) {
        return ManagedGroup.getUniqueBuilds(reports,null,null,null,null);
    }
    
    
    public static Collection getUniqueBuilds(ManagedReport[] reports, String inProject, String inGroup, String inType, String fromHost) {
        HashSet uniqueBuildsSet = new HashSet();
        if (reports != null) {
            for (int i=0; i< reports.length; i++) {
                if (inProject != null) {
                    if (!inProject.equals(reports[i].xmlat_project)) {
                        // this is not our project !!!
                        continue;
                    }                
                }
                if (inGroup != null) {
                    if (!inGroup.equals((reports[i].xmlat_testingGroup))) {
                        continue;
                    }
                }
                if (inType != null) {
                    if (!inType.equals((reports[i].xmlat_testedType))) {
                        continue;
                    }
                }
                if (fromHost != null) {
                    if (!fromHost.equals(reports[i].xmlat_host)) {
                        // this is not our host !!!
                        continue;
                    }                
                }
                String currentBuild = reports[i].xmlat_build;
                if (!uniqueBuildsSet.contains(currentBuild)) {
                    uniqueBuildsSet.add(currentBuild);
                }
            }
        }
        return uniqueBuildsSet; 
    }
    
    // new stuff - get unique teams
    public static Collection getUniqueTeams(ManagedReport[] reports, String inProject) {
        HashSet uniqueTeamSet = new HashSet();
        if (reports != null) {
            for (int i=0; i< reports.length; i++) {
                if (inProject != null) {
                    if (!inProject.equals(reports[i].xmlat_project)) {
                        // this is not our project !!!
                        continue;
                    }
                }
                String currentTeam = reports[i].getTeam();
                if (currentTeam != null) {
                    if (!uniqueTeamSet.contains(currentTeam)) {
                        uniqueTeamSet.add(currentTeam);
                    }
                }
            }
        }
        return uniqueTeamSet;
    }
    
    
    public Collection getUniqueTeams(String inProject) {
        return ManagedGroup.getUniqueTeams(this.xmlel_ManagedReport, inProject);
    }
    
    
    // filters !!!
    
    
    
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
