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

package org.netbeans.nbbuild;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import org.apache.tools.ant.BuildException;

/**
 * Moves classpath from properties to project.xml
 * @author pzajac
 */
public class FixTestDependencies extends org.apache.tools.ant.Task {
    
    /** Creates a new instance of FixTestClassPath */
    public FixTestDependencies() {
    }
    
    
    String cnb;
    
    private String nbAll;
    
    File projectXmlFile;
    public void setProjectXml(File projectXml) {
        projectXmlFile = projectXml;
        
    }
    File propertiesFile;
    public void setPropertiesFile(File properties) {
        this.propertiesFile = properties;
    }
    public void execute() throws BuildException {
        // read project xml
        try {
            if (projectXmlFile == null || !projectXmlFile.isFile()) {
                throw new BuildException("project.xml file doesn't exist.");
            }
            byte xmlBytes [] = new byte[(int)projectXmlFile.length()];
            FileInputStream prjFis = new FileInputStream(projectXmlFile);
            try {
                prjFis.read(xmlBytes);
            } finally {
                prjFis.close();
            }
            String xml = new String(xmlBytes);
            int projectType = ParseProjectXml.TYPE_NB_ORG;
            if (xml.contains("<suite-component/>")) {
                projectType = ParseProjectXml.TYPE_SUITE;
            } else if (xml.contains("<standalone/>")) {
                projectType = ParseProjectXml.TYPE_NB_ORG;
            } 
            //grrr
            int typeStart = xml.indexOf("<code-name-base>");
            int typeEnd = xml.indexOf("</code-name-base>");
            if (typeStart <= 0 || typeEnd <= 0 || typeEnd <= typeStart) {
                throw new BuildException("Parsing of project.xml failed.");
            }
            cnb = xml.substring(typeStart + "<code-name-base>".length(), typeEnd).trim();
            if (cnb.length() <= 0) {
                throw new BuildException("Invalid codename base:" + cnb);
            }
            // test if project.xml contains test-deps
            if (xml.contains("<test-dependencies>")) {
                // yes -> exit
                log("<test-dependencies> already exists.");
                return ;
            }
            // no :
            // scan for all modules
            ModuleListParser listParser;
            listParser = new ModuleListParser(getProject().getProperties(), projectType, getProject());
            Set/*<ModuleListParser.Entry>*/ entries =  listParser.findAll();
            Set/*<String>*/ allCnbs = getCNBsFromEntries(entries);
            // read properties
            
            // remove modules and test from properties and put it to project.xml
            
            // unittest
            //
            Set/*<String>*/ compileCNB = new HashSet();
            Set/*<String>*/ compileTestCNB = new HashSet();
            Set/*<String>*/ runtimeCNB = new HashSet();
            Set/*<String>*/ runtimeTestCNB = new HashSet();
            
            Properties projectProperties = getTestProperties();
            readCodeNameBases(compileCNB,compileTestCNB,projectProperties,"test.unit.cp",allCnbs,entries);
            readCodeNameBases(compileCNB,compileTestCNB,projectProperties,"test.unit.cp.extra",allCnbs,entries);
            
            readCodeNameBases(runtimeCNB,runtimeTestCNB,projectProperties,"test.unit.run.cp",allCnbs,entries);
            readCodeNameBases(runtimeCNB,runtimeTestCNB,projectProperties,"test.unit.run.cp.extra",allCnbs,entries);
            
            updateProperties(projectProperties,new String[]{"test.unit.cp","test.unit.cp.extra","test.unit.run.cp","test.unit.run.cp.extra"});
            
            StringBuffer buffer = new StringBuffer();
            buffer.append("\n          <test-dependencies>\n");
            buffer.append("              <test-type>\n");
            buffer.append("                  <name>unit</name>\n");
            addDependency(buffer,cnb,true,true,false);
            
            runtimeCNB.removeAll(compileCNB);
            //compileCNB.removeAll(runtimeCNB);
            compileCNB.addAll(compileTestCNB);
            runtimeTestCNB.removeAll(compileTestCNB);
            runtimeCNB.addAll(runtimeTestCNB);
            addDependencies(buffer,compileCNB,compileTestCNB,true,false);
            addDependencies(buffer,runtimeCNB,runtimeTestCNB,false,false);
            buffer.append("              </test-type>\n");
            
            // qa functional tests
            compileCNB.clear();
            runtimeCNB.clear();
            compileTestCNB.clear();
            runtimeTestCNB.clear();
            
            buffer.append("              <test-type>\n");
            buffer.append("                  <name>qa-functional</name>\n");
            
            readCodeNameBases(compileCNB,compileTestCNB,projectProperties,"test.qa-functional.cp",allCnbs,entries);
            readCodeNameBases(compileCNB,compileTestCNB,projectProperties,"test.qa-functional.cp.extra",allCnbs,entries);
            
            readCodeNameBases(runtimeCNB,runtimeTestCNB,projectProperties,"test.qa-functional.runtime.cp",allCnbs,entries);
            readCodeNameBases(runtimeCNB,runtimeTestCNB,projectProperties,"test.qa-functional.runtime.extra",allCnbs,entries);
            
            addDependencies(buffer,compileCNB,compileTestCNB,true,false);
            addDependencies(buffer,runtimeCNB,runtimeTestCNB,false,false);
            buffer.append("              </test-type>\n");
            buffer.append("            </test-dependencies>\n");
            updateProperties(projectProperties,new String[]{"test.qa-functional.cp","test.qa-functional.cp","test.qa-functional.runtime.cp","test.qa-functional.runtime.extra"});
            
            // merge project properties
            String MODULE_DEP_END = "</module-dependencies>";
            int moduleDepEnd = xml.indexOf(MODULE_DEP_END);
            if (moduleDepEnd == -1) {
                throw new BuildException("No module dependency found.");
            }
            moduleDepEnd += MODULE_DEP_END.length();
            StringBuffer resultXml = new StringBuffer();
            resultXml.append(xml.substring(0,moduleDepEnd));
            resultXml.append(buffer);
            resultXml.append(xml.substring(moduleDepEnd + 1, xml.length()));
            
           PrintStream ps = new PrintStream(projectXmlFile);
           ps.print(resultXml);
           ps.close();
            
        } catch (IOException ex) {
            throw new BuildException(ex);
        }
        
        // store project.properties and project.xml
    }
    
    private Set/*<String>*/ getCNBsFromEntries(Set/*<ModuleListParser.Entry>*/ entries) {
        Set /*<String>*/ cnbs = new HashSet();
        for (Iterator it = entries.iterator() ; it.hasNext();) {
            cnbs.add(((ModuleListParser.Entry)it.next()).getCnb());
        }
        return cnbs;
    }
    /** parses all codenamebases from path
     */
    private void readCodeNameBases(Set/*<String>*/ compileCNB,
            Set /*<String>*/ testsCNB,
            Properties projectPropertis,
            String property,
            Set /*<String>*/ allCnbs,
            Set /*<ModuleListParser.Entry>*/ entries) {
        String prop = projectPropertis.getProperty(property);
        StringBuffer newProp = new StringBuffer();
        if (prop !=   null) {
            StringTokenizer tokenizer = new StringTokenizer(prop,";:\\");
            while(tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken().trim();
                boolean found = false;
                if (token.length() > 1) {
                    int lastSlash = token.lastIndexOf("/");
                    int lastDot = token.lastIndexOf(".");
                    // check if the file is module
                    //
                    if (lastSlash != -1 && lastDot != -1) {
                        String codeBaseName = token.substring(lastSlash + 1, lastDot);
                        codeBaseName = codeBaseName.replace('-','.');
                        if (allCnbs.contains(codeBaseName)) {
                            compileCNB.add(codeBaseName);
                            found = true;
                        }
                        // check if the dependency is dependency on test
                    } else {
                        int prjEnd = token.indexOf("/build/test/unit/class");
                        if (prjEnd != -1) {
                            // get the project folder
                            int firstSlash = token.indexOf("/");
                            if (firstSlash != -1 ) {
                                String  prjFolder = token.substring(firstSlash + 1, prjEnd);
                                String  codebaseName = getCNBForFolder(prjFolder,entries);
                                testsCNB.add(codebaseName);
                                found = true;
                            }
                        }
                    }
                    // check if the file is file
                    //
                    if (found == false) {
                        if (newProp.length() > 0) {
                            newProp.append(":");
                        }
                        newProp.append(token);
                    }
                }
            } // while
            projectPropertis.setProperty(property,newProp.toString());
        }
    }
    
    private void addDependencies(StringBuffer buffer, Set moduleCNB,Set testCNB, boolean compile, boolean recursive) {
        for (Iterator it = moduleCNB.iterator() ; it.hasNext() ;) {
            String cnb = (String)it.next();
            addDependency(buffer,cnb,compile,recursive,testCNB.contains(cnb));
        }
    }
    
    private void addDependency(StringBuffer buffer, String cnb, boolean compile, boolean recursive, boolean test) {
        buffer.append("                  <test-dependency>\n");
        buffer.append("                      <code-name-base>" + cnb + "</code-name-base>\n");
        if (recursive) {
            buffer.append("                      <recursive/>\n");
        }
        if (compile) {
            buffer.append("                      <compile-dependency/>\n");
        }
        if (test) {
            buffer.append("                      <test/>\n");
        }
        buffer.append("                  </test-dependency>\n");
        
    }
    
    private String getNbAll() {
        if (nbAll == null) {
            nbAll = getProject().getProperty("nb_all");
        }
        return nbAll;
    }
    
    private Properties getTestProperties() throws IOException {
        if (propertiesFile == null || !propertiesFile.isFile()) {
            throw new BuildException("Property file doesn't exist");
        }
        Properties props = new Properties();
        FileInputStream fis = new FileInputStream(propertiesFile);
        try {
            props.load(fis);
        } finally {
            fis.close();
        }
        return props;
    }
    
    /** @return codeNameBase of project for relative folder prjFolder
     */
    private String getCNBForFolder(String prjFolder, Set entries) {
        String cnb = null;
        for (Iterator it = entries.iterator(); it.hasNext();) {
            ModuleListParser.Entry elem = (ModuleListParser.Entry) it.next();
            if (prjFolder.equals(elem.getNetbeansOrgPath())) {
                return elem.getCnb();
            }
        }
        return null;
    }
    private void updateProperties(Properties projectProperties,String names[]) {
        try {
            
            // read properties
            BufferedReader reader = new BufferedReader (new FileReader(propertiesFile));
            List/*<String>*/ lines = new ArrayList();
            String line = null;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            reader.close();
            
            // merge properties
            for (int i = 0; i < names.length; i++) {
               String propName = names[i];
               String value = projectProperties.getProperty(propName);
               lines = replaceProperty(propName,value,lines);    
            }
            // store properties
            PrintStream ps = new PrintStream(propertiesFile);
            for (int i = 0 ; i < lines.size() ; i++) {
                ps.println(lines.get(i));
            }
            ps.close();
            
        } catch (IOException ioe) {
            throw new BuildException(ioe);
        }
        
    }

    private List replaceProperty(String name,String value,List/*<String>*/ lines) {
        List retLines = new ArrayList();
        for (int i = 0 ; i < lines.size() ; i++) {
            String line = (String)lines.get(i);
            String trimmedLine = line.trim();
            int eqIdx = trimmedLine.indexOf("=");
            if (eqIdx != -1) {
                String pName = line.substring(0,eqIdx).trim();
                if (pName.equals(name)) {
                    // skip property
                    for (; i < lines.size() && ((String)lines.get(i)).trim().endsWith("\\") ; i++) ;
                    // append new property 
                    if (value != null) {
                        retLines.add(name + "=" + value);
                    }
                    continue;
                }
            }
            // either empty line, comment or other property
            retLines.add(line);
        }
        return retLines;
    } 
}
