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
 * NbExecutor.java
 *
 * Created on March 28, 2001, 6:57 PM
 */

package org.netbeans.xtest.harness;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.taskdefs.*;

import java.util.*;
import java.io.File;
import java.io.IOException;
import org.netbeans.junit.Filter;
import org.netbeans.xtest.testrunner.*;

/**
 *
 * @author  mk97936
 * @version
 */
public class MTestExecutor extends Task {
    
    String targetName = null;
    String targetParamClasspathProp = null;
    String targetParamTestConfigProp = "tbag.testtype";
    String targetParamNameProp = null;
    String targetParamExecutorProp = "tbag.executor";
    String targetParamTimeoutProp = "xtest.timeout";
    
    File resultDir;
    File workDir;
    
    public void setTargetName(String name) {
        this.targetName = name;
    }
    
    public void setParamClasspathProp(String param) {
        this.targetParamClasspathProp = param;
    }
    
    public void setParamTestConfigProp(String param) {
        this.targetParamTestConfigProp = param;
    }
    
    public void setParamNameProp(String param) {
        this.targetParamNameProp = param;
    }
    
    public void setParamExecutorProp(String param) {
        this.targetParamExecutorProp = param;
    }
    
    public void setParamTimeoutProp(String param) {
        this.targetParamTimeoutProp = param;
    }
    
    public void setResultDir(File s) {
        resultDir = s;
    }
    
    public void setWorkDir(File s) {
        workDir = s;
    }

    public void execute() throws BuildException {
        
        if (null == targetParamClasspathProp || 0 == targetParamClasspathProp.length())
            throw new BuildException("Attribute 'targetParamClasspathProp' has to be set.");
        
        if (resultDir == null) {
            throw new BuildException("ResultDir is not set");
        }
        if (workDir == null) {
            throw new BuildException("WorkDir is not set");
        }
        
        Testbag testbags[] = MTestConfigTask.getTestbags();
        if (null == testbags)
            throw new BuildException("TestBag configuration wasn't chosen.", getLocation());

        for (int i=0; i<testbags.length; i++) {

                // get TestBag 
                Testbag testbag = testbags[i];
                
                MTestConfig.AntExecType exec = testbag.getExecutor();
                if (exec == null) throw new BuildException("Testbag "+testbag.getName()+" has not a executor.");

                Ant ant_new = (Ant) getProject().createTask( "ant" );
                ant_new.setOwningTarget( target ); 
                ant_new.setAntfile( exec.getAntFile() );
                ant_new.setTarget( exec.getTarget() );
                if (exec.getDir() != null) ant_new.setDir( project.resolveFile ( exec.getDir()));
                ant_new.init();
                
                /// ??????? add xtest.userdata| prefix ?????
                
                // add all test properties for given testbag
                Testbag.TestProperty properties[] = testbag.getTestProperties();
                if (properties != null)
                    for (int j=0; j<properties.length; j++) {
                        Property ant_prop = ant_new.createProperty();
                        ant_prop.setName(  properties[j].getName() );
                        ant_prop.setValue( properties[j].getValue() );
                    }
                
                // Set classpath property for given testbag
                Property clspth_prop = ant_new.createProperty();
                clspth_prop.setName( targetParamClasspathProp );
                
                StringBuffer stb = new StringBuffer();
                
                // if any of the testset contains setup dir, it does not
                // have to be added to classpath
                boolean testsetContainsSetupDir = false;
                if (testbag.getSetupDir() == null) {
                    testsetContainsSetupDir = true;
                }
                
                for (int j=0; j<testbag.getTestsets().length; j++) {
                    // name of this property should be passed by atribute
                    // and also it's not clear where it's resolved !!!!!!
                    stb.append( ant_new.getProject().getProperty( "tbag.classpath.root" ) );
                    // don't need it :-)
                    
                     /*
                    stb.append( "/" );
                    stb.append( MTestConfigurator.getCurrentTestType() );
                    */
                    stb.append( "/" );
                    stb.append( ant_new.getProject().getProperty( "tbag.classpath.work" ) );
                    stb.append( "/" );
                    String testsetDir = testbag.getTestsets()[j].getDir();
                    stb.append( testsetDir );
                    stb.append( ";" );
                    // check if this testset contains setup dir
                    if ( ! testsetContainsSetupDir) {
                        if (testbag.getSetupDir().equals(testsetDir)) {
                            testsetContainsSetupDir = true;
                        }
                    }
                }
                // add setup/teardown dir if available
                if ( ! testsetContainsSetupDir ) {
                    stb.append(testbag.getSetupDir());
                    stb.append( ";" );
                }
               
                if ( stb.length() > 1 ) {
                    stb.deleteCharAt( stb.length() - 1 );                
                }
                clspth_prop.setValue( stb.toString() );
                
                // set name of executed test config
                Property cttprop = ant_new.createProperty();
                cttprop.setName( targetParamTestConfigProp );
                cttprop.setValue( MTestConfigTask.getMTestConfig().getTesttype() );
                
                // set name of executed testbag
                Property nameprop = ant_new.createProperty();
                nameprop.setName( targetParamNameProp );
                nameprop.setValue( testbag.getName() );
                
                // set name of executor for executed testbags
                Property execprop = ant_new.createProperty();
                execprop.setName( targetParamExecutorProp );
                execprop.setValue( testbag.getExecutor().getName() );
                
                if (testbag.getTimeout() != null) {
                    Property timeoutprop = ant_new.createProperty();
                    timeoutprop.setName( targetParamTimeoutProp );
                    timeoutprop.setValue( testbag.getTimeout().toString() );
                }
                
                createJUnitTestRunnerPropertyFile(testbag);
                
                // execute tests
                ant_new.execute();
                
                // run results processor
                MTestConfig.AntExecType proc = testbag.getResultsprocessor();
                Ant ant_proc = (Ant) getProject().createTask( "ant" );
                ant_proc.setOwningTarget( target );
                ant_proc.setAntfile( proc.getAntFile() );
                ant_proc.setTarget( proc.getTarget() );
                if (proc.getDir() != null) ant_new.setDir( project.resolveFile ( proc.getDir()));
                ant_proc.init();
                
                // set name of executed test config
                Property cttprop1 = ant_proc.createProperty();
                cttprop1.setName( targetParamTestConfigProp );
                cttprop1.setValue( MTestConfigTask.getMTestConfig().getTesttype() );
                
                // set name of executor for executed testbags
                Property execprop1 = ant_proc.createProperty();
                execprop1.setName( targetParamExecutorProp );
                execprop1.setValue( testbag.getExecutor().getName() );
                
                // execute results processing
                ant_proc.execute();

            }
        
    }
    
    private JUnitTestRunnerProperties createJUnitTestRunnerPropertyFile(Testbag testbag) {
        
       ArrayList testsWithFilters = new ArrayList();
       try {
       
         for (int i=0; i<testbag.getTestsets().length; i++) {
            Testbag.Testset testset = testbag.getTestsets()[i];
            TestScanner ts = new TestScanner();
            
            ts.setBasedir(project.resolveFile(testset.getDir()));
            //fs.setBasePath(path);

            /*
            for (int i=0; i<additionalPatterns.size(); i++) {
                Object o = additionalPatterns.elementAt(i);
                defaultPatterns.append((PatternSet) o, p);
            } */
            
            System.out.println("Testbag "+testbag.getName());
            System.out.println("testset = "+testset);
            System.out.println("Testset includes "+testset.getIncludes());

            ts.setIncludes(testset.getIncludes());
            ts.setExcludes(testset.getExcludes());
                
            // scan directories for test classes and associated test patterns
            ts.scan();
           
            TestFilter[] filter;
                filter = ts.getIncludedFiles();
           
            // go through all found test classes and prepare the JUnitTest instances
            for(int j = 0;  j < filter.length; j++) {
                String testName = fileToClassname(filter[j].getFile());
                log("Adding test class "+testName,Project.MSG_VERBOSE);
                TestWithFilter test = new TestWithFilter();
                test.testName = testName;
                test.includeFilter = filter[j].getFilter().getIncludes();
                test.excludeFilter = filter[j].getFilter().getExcludes();
                // add test to array
                testsWithFilters.add(test);
            }
         }
       } catch (IOException e) {
            throw new BuildException(e.getMessage(),e);
       }
       
        log("Preparing test runner property file",Project.MSG_DEBUG);
        JUnitTestRunnerProperties props = new JUnitTestRunnerProperties();
        // results dir
        props.setResultsDirName(resultDir.getAbsolutePath());
        // setup teardown
        if ((testbag.getSetUpClassName() != null) && (testbag.getSetUpMethodName() != null)) {
            props.setTestbagSetup(testbag.getSetUpClassName(), testbag.getSetUpMethodName());
        }
        if ((testbag.getTearDownClassName() != null) && (testbag.getTearDownMethodName() != null)) {
            props.setTestbagTeardown(testbag.getTearDownClassName(), testbag.getTearDownMethodName());
        }        
        // tests
        for (int i=0; i < testsWithFilters.size(); i++) {
            TestWithFilter test = (TestWithFilter)testsWithFilters.get(i);
            props.addTestNameWithFilter(test.testName,test.includeFilter,
                            test.excludeFilter);                
        }
        // save it to workdir
        try {
            File testRunnerPropertyFile = new File(workDir, JUnitTestRunner.TESTRUNNER_PROPERTY_FILENAME);
            log("Saving test runner property to "+testRunnerPropertyFile,Project.MSG_DEBUG);
            props.save(testRunnerPropertyFile);
        } catch (IOException ioe) {
            throw new BuildException("Cannot save test runner property file",ioe);
        }
        return props;
        
    }
    
    // convert file to class name
    private String fileToClassname(String file) {
        if (file.endsWith(".java")) {
            file = file.substring(0, file.length() - 5); // ".java".length() equals 5
        }
        else if (file.endsWith(".class")) {
            file = file.substring(0, file.length() - 6); // ".class".length() equals 6
        }
        return javaToClass(file);
    }    
    
    /**
     * convenient method to convert a pathname without extension to a
     * fully qualified classname. For example <tt>org/apache/Whatever</tt> will
     * be converted to <tt>org.apache.Whatever</tt>
     * @param filename the filename to "convert" to a classname.
     * @return the classname matching the filename.
     */
    public final static String javaToClass(String filename){
        return filename.replace(File.separatorChar, '.');
    }
    
    // this is rather struct than class :-(
    // this represents test (testname) with include and exclude filters
    static class TestWithFilter {
        String   testName;
        Filter.IncludeExclude[] includeFilter;
        Filter.IncludeExclude[] excludeFilter;
    }
    
}
