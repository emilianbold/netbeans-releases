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
/*
 * Project.java
 *
 * Created on May 14, 2002, 9:33 AM
 */

package org.netbeans.xtest.pes.xmlbeans;

import org.netbeans.xtest.pe.xmlbeans.*;
import java.util.regex.*;


/**
 *
 * @author  breh
 */
public class PESProject extends XMLBean {

    //public static final String EVERYTHING = "*everything*";


    /** Creates a new instance of Project */
    public PESProject() {
    }

    // attributes
    // name of the project  - serves as project ID
    public String xmlat_name;
    // description of the project
    public String xmlat_description;
    // name of the project, which is being managed (serves as link to real data)
    public String xmlat_project;
    // department which tests this project
    public String xmlat_department;
    // test type 
    public String xmlat_testType;
    // buildFrom and buildTo Strings - if only a subset of builds
    // is used in this project from the managed project
    public String xmlat_fromBuild;
    public String xmlat_toBuild;
    
    
    // getters/setters

        /** Getter for property xmlat_department.
     * @return Value of property xmlat_department.
     */
    public java.lang.String getDepartment() {
        return xmlat_department;
    }
    
    /** Setter for property xmlat_department.
     * @param xmlat_department New value of property xmlat_department.
     */
    public void setDepartment(java.lang.String xmlat_department) {
        this.xmlat_department = xmlat_department;
        if (xmlat_department != null) {
            departmentPattern = Pattern.compile(xmlat_department,Pattern.CASE_INSENSITIVE);
        } else {
            departmentPattern = null;
        }        
    }
    
    /** Getter for property xmlat_project.
     * @return Value of property xmlat_project.
     */
    public java.lang.String getProject() {
        return xmlat_project;
    }
    
    /** Setter for property xmlat_project.
     * @param xmlat_project New value of property xmlat_project.
     */
    public void setProject(java.lang.String xmlat_project) {
        this.xmlat_project = xmlat_project;
        if (xmlat_project != null) {
            projectPattern = Pattern.compile(xmlat_project,Pattern.CASE_INSENSITIVE);
        } else {
            projectPattern = null;
        }
        
    }
    /** Getter for property xmlat_description.
     * @return Value of property xmlat_description.
     */
    public java.lang.String getDescription() {
        return xmlat_description;
    }    

    /** Setter for property xmlat_description.
     * @param xmlat_description New value of property xmlat_description.
     */
    public void setDescription(java.lang.String xmlat_description) {
        this.xmlat_description = xmlat_description;
    }    
    
    /** Getter for property xmlat_fromBuild.
     * @return Value of property xmlat_fromBuild.
     */
    public java.lang.String getFromBuild() {
        return xmlat_fromBuild;
    }
    
    /** Setter for property xmlat_fromBuild.
     * @param xmlat_fromBuild New value of property xmlat_fromBuild.
     */
    public void setFromBuild(java.lang.String xmlat_fromBuild) {
        this.xmlat_fromBuild = xmlat_fromBuild;
    }
    
    /** Getter for property xmlat_name.
     * @return Value of property xmlat_name.
     */
    public java.lang.String getName() {
        return xmlat_name;
    }
    
    /** Setter for property xmlat_name.
     * @param xmlat_name New value of property xmlat_name.
     */
    public void setName(java.lang.String xmlat_name) {
        this.xmlat_name = xmlat_name;
    }
    
    /** Getter for property xmlat_testType.
     * @return Value of property xmlat_testType.
     */
    public java.lang.String getTestType() {
        return xmlat_testType;
    }
    
    /** Setter for property xmlat_testType.
     * @param xmlat_testType New value of property xmlat_testType.
     */
    public void setTestType(java.lang.String xmlat_testType) {
        this.xmlat_testType = xmlat_testType;
        if (xmlat_testType != null) {
            testTypePattern = Pattern.compile(xmlat_testType,Pattern.CASE_INSENSITIVE);
        } else {
            testTypePattern = null;
        }
    }
    
    /** Getter for property xmlat_toBuild.
     * @return Value of property xmlat_toBuild.
     */
    public java.lang.String getToBuild() {
        return xmlat_toBuild;
    }
    
    /** Setter for property xmlat_toBuild.
     * @param xmlat_toBuild New value of property xmlat_toBuild.
     */
    public void setToBuild(java.lang.String xmlat_toBuild) {
        this.xmlat_toBuild = xmlat_toBuild;
    }    
    
    

    // validator
    public void checkValidity() throws PESConfigurationException {
        if (xmlat_project == null) throw new PESConfigurationException("PESConfig: PESWeb: PESProjectGroup: PESProject: project is not set");        
    }
    
    // business methods
    
    
    public static PESProject getDefaultPESProject() {
        PESProject project = new PESProject();
        return project;
    }
    
    // 
    public void finishInitialization() {
        this.setProject(xmlat_project);
        this.setDepartment(xmlat_department);
        this.setTestType(xmlat_testType);        
    }
    
    
    
    // does the supplied report belong to 
    public boolean isThisReportMine(XTestResultsReport xtr) {
        
        // if argument is null - do not add it :-)
        if (xtr == null) {
            return false;
        }
        
        // project        
        if (xmlat_project == null) {
            // this means this is project which is default
            // include everything 
            return true;
        } else {
            // use regex
            if (!projectMatches(xtr.getProject())) {
                return false;
            }
        }
        
        // testing group/department
        if (xmlat_department != null) {
            if (!departmentMatches(xtr.getTestingGroup())) {
                return false;
            }
        }
        
        // tested type
        if (xmlat_testType != null) {
            if (!testTypeMatches(xtr.getTestedType())) {
                return false;
            }
        }
        
        // compare build numbers
        String build = xtr.getBuild();
        if (build == null) {
            if (xmlat_fromBuild != null) {
                if (xmlat_fromBuild.compareToIgnoreCase(build) > 0 ) {
                    // build is lower that fromBuild
                    return false;
                }
            }
        
            if (xmlat_toBuild != null) {
                if (xmlat_toBuild.compareToIgnoreCase(build) < 0) {
                    // build is greater than toBuild
                    return false;
                }            
            }
        }
        
        // well - it looks like xtr belong to this project
        return true;
        
    }
    

    
    // regex stuff
    
    // project
    private Pattern projectPattern;
    private boolean projectMatches(String project) {
        if (projectPattern != null) {
            Matcher matcher = projectPattern.matcher(project);
            return matcher.matches();
        } else {
            return false;
        }        
    }
    
    // department
    private Pattern departmentPattern;
    private boolean departmentMatches(String department) {
        if (departmentPattern != null) {
            Matcher matcher = departmentPattern.matcher(department);
            return matcher.matches();
        } else {
            return false;
        }        
    }    
    
    // test type
    private Pattern testTypePattern;
    private boolean testTypeMatches(String testType) {
        if (testTypePattern != null) {
            Matcher matcher = testTypePattern.matcher(testType);
            return matcher.matches();
        } else {
            return false;
        }        
    }    
    
    
}
