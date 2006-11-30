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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.tools.ant.*;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * It scans test distribution ant sets to property with name defined by param 
 * 'testListProperty'path with filtered test. The TestDistFilter is used for running tests 
 * in test distribution.
 * <br>
 * 
 * Parameters :
 * <ul>
 *    <li>harness - type of harness (junit,xtest). For junit harness are 
 *     scanned only tests with unit tes type and 'code' executor name inside 
 *     cfg-unit.xml.
 *    <li>testtype - unit|qa-functional|all testtype  
 *    <li>attribs - xtest.attribs filter (attribs are declared in cfg-<xxx>.xml file  
 *    <li>testlistproperty  - store to property path with test folders (separated by ':')
 *    <li>testdistdir - root folder with test distribution
 *    <li>requiredmodules - list of module names required on runtime classpath example:
 *             org-netbeans-modules-masterfs.jar,org-openide-loaders.jar. Only tests 
 *             which contains masterfs and loaders will be stored to testlistproperty value.
 * </ul>
 */
public class TestDistFilter extends Task {
    public static final String TYPE_ALL = "all";
    public static final String TYPE_UNIT = "unit";
    public static final String TYPE_QA_FUNCTIONAL = "qa-functional";
  
    public static final String HARNESS_JUNIT = "junit";
    public static final String HARNESS_XTEST = "xtest";

    private File testDistDir;
    Set<TestConf> possibleTests = new HashSet();
    // "unit|qa_functional|all
    // default value is all
    private String testtype = TYPE_ALL;
    private String harness = HARNESS_XTEST;
    // xtest attribs
    private String attribs ;
    private String testListProperty;
    private String requiredModules;
    // TODO customize method names to match custom task
    // property and type (handled by inner class) names
    
    /** represents a test directory
     */
    private static class TestConf {
        File moduleDir;
        boolean unit;
        TestConf(File moduleDir,boolean unit) {
            this.moduleDir = moduleDir;
            this.unit = unit;
        }

        public int hashCode() {
            return moduleDir.hashCode();
        }
        public boolean  equals(Object obj) {
            return  (obj instanceof TestConf) && moduleDir.equals(((TestConf)obj).moduleDir); 
        }
      
        
        /** check if cfg-<testtype>.xml contains xtest.attribs, 
         *  ide executor is ignored for junit
         */
        boolean matchAttribs(String harness,String attribs) {
            Element config;
            try {
                config = getConfig();
            } catch (SAXException ex) {
                throw new BuildException("Error in parsing " + getConfigFile(),ex);
            } catch (ParserConfigurationException ex) {
                throw new BuildException("Error in parsing " + getConfigFile(),ex);
            } catch (IOException ex) {
                throw new BuildException("Error in parsing " + getConfigFile(),ex);
            }
            if (config == null) {
                return false;
            }
            boolean junit = HARNESS_JUNIT.equals(harness);
            NodeList elements = config.getElementsByTagName("testbag");
            for (int n  = 0 ; n < elements.getLength() ; n++) {
                Element testbag = (Element) elements.item(n);
                if (junit && "ide".equals(testbag.getAttribute("executor"))) {
                    continue;
                }
                if (testAttr(testbag.getAttribute("testattribs"),attribs)) {
                    return true;
                }
            } 
            return false;
        }
        private static boolean testAttr(String xmlAttr,String userAttr) {
            if (userAttr == null) {
                return true;
            }
            if (xmlAttr == null) {
                return false;
            }
            StringTokenizer tokenizer = new StringTokenizer(xmlAttr,"&|, ");
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken().trim();
                if (token.equals(userAttr)) {
                    return true;
                } 
            }
            return false;
        }
        File getModuleDir() {
            return moduleDir;
        }
        
        private File getConfigFile () {
            String name = (unit) ? "cfg-unit.xml" : "cfg-qa-functional.xml";
            return  new File(getModuleDir(),name);
        }
        private Element getConfig() throws ParserConfigurationException, SAXException, IOException {
            File xml = getConfigFile();
            if (!xml.exists()) {
                return null;
            }
            return getDocumentBuilder().parse(xml).getDocumentElement();
        }
        
    }

    
    public void execute() throws BuildException {
          possibleTests.clear();
          if (getTestListProperty() == null) {
              throw new BuildException("Param testlistproperty is not defined.");
          }
          if (getTestDistDir() == null || !getTestDistDir().exists()) {
              throw new BuildException("Param testdistdir is not defined.");
          }
          if ("".equals(attribs)) {
              attribs = null;
          }
          String tt = getTesttype();
          if (getHarness().equals(HARNESS_JUNIT)) { 
              findCodeTests(HARNESS_JUNIT,TYPE_UNIT,getAttribs());
          } else {
              if (tt.equals(TYPE_QA_FUNCTIONAL) || tt.equals(TYPE_ALL)) {
                  findCodeTests(HARNESS_XTEST,TYPE_QA_FUNCTIONAL,getAttribs());
              }
              if (tt.equals(TYPE_UNIT) || tt.equals(TYPE_ALL)) {
                  findCodeTests(HARNESS_XTEST,TYPE_UNIT,getAttribs());
              }
          }
          define(getTestListProperty(),getTestList());
    }
    /** get path with test dirs separated by :
     */
    private String getTestList() {
        StringBuffer path = new StringBuffer();
        for (Iterator it = possibleTests.iterator() ; it.hasNext() ; ) {
            TestConf tc = (TestConf)it.next();
            if (!matchRequiredModule(tc.getModuleDir())) {
                continue;
            }
            if (path.length() > 0) {
                path.append(':');
            }
            path.append(tc.getModuleDir().getAbsolutePath());
        }
        return path.toString();
    }
    private void define(String prop, String val) {
        log("Setting " + prop + "=" + val, Project.MSG_VERBOSE);
        String old = getProject().getProperty(prop);
        if (old != null && !old.equals(val)) {
            getProject().log("Warning: " + prop + " was already set to " + old, Project.MSG_WARN);
        }
        getProject().setNewProperty(prop, val);
    }


    public String getTesttype() {
        return testtype;
    }

    public void setTesttype(String testtype) {
        this.testtype = testtype;
    }

    public String getHarness() {
        return harness;
    }

    public void setHarness(String harness) {
        this.harness = harness;
    }

    public String getAttribs() {
        return attribs;
    }

    public void setAttribs(String attribs) {
        this.attribs = attribs;
    }

    public String getTestListProperty() {
        return testListProperty;
    }

    public void setTestListProperty(String testListProperty) {
        this.testListProperty = testListProperty;
    }

    private void findCodeTests(String harness, String type, String string) {
          List tests = getTestList(type);
          for (int i = 0 ; i < tests.size() ; i++) {
              TestConf test = (TestConf)tests.get(i);
              if (test.matchAttribs(harness,attribs)) {
                  possibleTests.add(test);
              }
          }
    }

    private List getTestList(String testtype) {
        File root = new File (getTestDistDir(),testtype);
        List <TestConf> testList = new ArrayList();
        if (!root.exists()) {
            return Collections.EMPTY_LIST;
        }
        File clusters[] = root.listFiles();
        for (int c = 0 ; c < clusters.length ; c++) {
            File cluster = clusters[c];
            if (cluster.isDirectory()) {
                File modules[] = cluster.listFiles();
                for (int m = 0 ; m < modules.length ; m++) {
                    File module = modules[m];
                    if (module.isDirectory()) {
                        testList.add(new TestConf(module,testtype.equals(TYPE_UNIT)));
                    }
                }
            }
        }
        return testList;
    }

    
    // create document builder with empty EntityResolver
    //
    private static DocumentBuilder db;
    private static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        if (db == null) {
           db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
           db.setEntityResolver(new EntityResolver() {
               public InputSource resolveEntity(String publicId, String systemId) throws SAXException,IOException {
                   return new InputSource(new StringReader(""));
               }
            
           });
        }
        return db;
    }

    public File getTestDistDir() {
        return testDistDir;
    }

    public void setTestDistDir(File testDistDir) {
        this.testDistDir = testDistDir;
    }

    public String getRequiredModules() {
        return requiredModules;
    }

    public void setRequiredModules(String requiredModules) {
        this.requiredModules = requiredModules;
    }

    private boolean matchRequiredModule(File path) {
       if (requiredModules == null || requiredModules.trim().length() == 0) {
           return true;
       }
       File pfile = new File(path,"test.properties");
       if (pfile.exists()) {
           Properties props = new Properties();
            try {
                FileInputStream fis = new FileInputStream(pfile);
                try { 
                  props.load(fis);
                  
                  String runCp = props.getProperty("test.unit.run.cp");
                  if (runCp != null) {
                      String paths[] = runCp.split(":");
                      Set reqModules = getRequiredModulesSet();
                      if (reqModules.size() == 0) {
                          return true;
                      }
                      for (int i = 0 ; i < paths.length ; i++) {
                          String p = paths[i];
                          int lastSlash = p.lastIndexOf('/');
                          if (lastSlash != -1) {
                              p = p.substring(lastSlash + 1);
                          } 
                          if (reqModules.contains(p)) {
                              return true;
                          }
                      }
                  }
                } finally {
                  fis.close();  
                }
            } catch(IOException ioe){
                throw new BuildException(ioe);
            }
       }
       return false;        
    }

    private Set getRequiredModulesSet() {
        String names[] = getRequiredModules().split(",");
        return new HashSet(Arrays.asList(names));
    }
}
