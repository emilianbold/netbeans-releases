/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * MTestConfigTask.java
 *
 * Created on 27. bøezen 2003, 15:19
 */

package org.netbeans.xtest.harness;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.taskdefs.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import org.netbeans.xtest.xmlserializer.XMLSerializeException;

/**
 *
 * @author  lm97939
 */
public class MTestConfigTask extends Task {
    
    private String config;
    private String testtype;    
    private static Vector singletest = new Vector();
    private HashSet passed_attribset = new HashSet();
    private HashSet passed_patternattribs = new HashSet();
    private HashSet passed_executors = new HashSet();
    private HashSet passed_testbags = new HashSet();
    
    private Vector additionalPatterns = new Vector();
    
    private static MTestConfig mconfig;
    
    /** Creates a new instance of MTestConfigTask */
    public MTestConfigTask() {
    }
    
        // xml element
    public void setTesttype (String s) {
        testtype = s;
    }       
    
    // xml element
    public void setTestattributes (String s) {        
        StringTokenizer stt = new StringTokenizer(s, ",");
        while ( stt.hasMoreTokens() ) {
            passed_attribset.add( stt.nextToken() );
        }
    }
    
    // xml element
    public void setPatternsetattribs (String s) {        
        StringTokenizer stt = new StringTokenizer(s, ",");
        while ( stt.hasMoreTokens() ) {
            passed_patternattribs.add( stt.nextToken() );
        }
    }
    
    // xml element
    public void setTestexecutors (String s) {
        if (s == null || s.equals("")) return;
        StringTokenizer stt = new StringTokenizer(s, ",");
        while ( stt.hasMoreTokens() ) {
            passed_executors.add( stt.nextToken() );
        }
    } 
    
    // xml element
    public void setTestbags (String s) {
        if (s == null || s.equals("")) return;
        StringTokenizer stt = new StringTokenizer(s, ",");
        while ( stt.hasMoreTokens() ) {
            passed_testbags.add( stt.nextToken() );
        }
    }
    
    // xml element
    public void setTestconfig (String config) {
        this.config = config;
    }
    
    // xml element
    public void setSingleTest (String s) {
        if (s == null || s.equals("")) return;
        StringTokenizer stt = new StringTokenizer(s, ",");
        while ( stt.hasMoreTokens() ) {
            singletest.add( stt.nextToken() );
        }
    }
    
    public PatternSet createPatternSet() {
        PatternSet patterns = new PatternSet();
        additionalPatterns.addElement(patterns);
        return patterns;
    }
    
    public static MTestConfig getMTestConfig() {
        return mconfig;
    }
    
    public static Testbag[] getTestbags() throws BuildException {
        if (!singletest.isEmpty()) {
            try {
                return new Testbag[] { mconfig.createSingleTestbag(mconfig.getTesttype()+"/src", singletest) };
            }
            catch (XMLSerializeException e) {
                throw new BuildException(e.getMessage(), e);
            }
        } else {
            return mconfig.getFilteredTestbags();
        }
    }
    
    
    public void execute() throws BuildException {
        File configfile;
        if (testtype == null)
            throw new BuildException("Attribute testtype is required.");
        if (passed_attribset.isEmpty() && passed_testbags.isEmpty())
            throw new BuildException("Either attribute testattributes or testbags is required.");
        if (config != null && !config.equals("")) {
            configfile = project.resolveFile(config);
            if (!configfile.exists() || !configfile.isFile())
                throw new BuildException("Config file "+configfile.getAbsolutePath()+" doesn't exist or is not a file.");
        } else {
            configfile = new File(project.getBaseDir(), "cfg-" + testtype + ".xml");
        }
        try {
            mconfig = MTestConfig.loadConfig(configfile, passed_patternattribs, passed_attribset, passed_executors, passed_testbags);
            mconfig.setTesttype(testtype);
            ArrayList includes_list = new ArrayList();
            ArrayList excludes_list = new ArrayList();
            Enumeration enum = additionalPatterns.elements();
            while (enum.hasMoreElements()) {
                PatternSet pattern = (PatternSet)enum.nextElement();
                String include[] = pattern.getIncludePatterns(project);
                String exclude[] = pattern.getExcludePatterns(project);
                if (include != null)
                    includes_list.addAll(Arrays.asList(include));
                if (exclude != null)
                    excludes_list.addAll(Arrays.asList(exclude));
            }
            mconfig.setAdditionalIncludes((String[])includes_list.toArray(new String[0]));
            mconfig.setAdditionalExcludes((String[])excludes_list.toArray(new String[0]));
        }
        catch (XMLSerializeException e) {
            throw new BuildException(e.getMessage(), e);
        }
    }
}
