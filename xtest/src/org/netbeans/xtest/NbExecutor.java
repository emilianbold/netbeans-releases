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

package org.netbeans.xtest;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.taskdefs.*;

import java.util.*;

/**
 *
 * @author  vs124454
 * @version 
 */
public class NbExecutor extends Task {
    
    String targetName   = null;
    String targetParamModule     = null;
    String targetParamTestType   = null;
    String targetParamIncludes   = null;
    String targetParamExcludes   = null;
    
    public void setTargetName(String name) {
        this.targetName = name;
    }
    
    public void setTargetParamModule(String param) {
        this.targetParamModule = param;
    }
    
    public void setTargetParamTesttype(String param) {
        this.targetParamTestType = param;
    }
    
    public void setTargetParamIncludes(String param) {
        this.targetParamIncludes = param;
    }

    public void setTargetParamExcludes(String param) {
        this.targetParamExcludes = param;
    }

    public void execute () throws BuildException {
        if (null == targetName || 0 == targetName.length())
            throw new BuildException("Attribute 'targetname' has to be set.");
        if (null == targetParamModule || 0 == targetParamModule.length())
            throw new BuildException("Attribute 'targetParamModule' has to be set.");
        if (null == targetParamTestType || 0 == targetParamTestType.length())
            throw new BuildException("Attribute 'targetParamTestType' has to be set.");
        if (null == targetParamIncludes || 0 == targetParamIncludes.length())
            throw new BuildException("Attribute 'targetParamIncludes' has to be set.");
        if (null == targetParamExcludes || 0 == targetParamExcludes.length())
            throw new BuildException("Attribute 'targetParamExcludes' has to be set.");
        
        XConfig cfg = NbTestConfig.getXConfig();
        if (null == cfg)
            throw new BuildException("XTest configuration wasn't chosen, use call xtestconfig task first (org.netbeans.xtest.NbTestConfig).", getLocation());
        
        Enumeration modules = cfg.getModules();
        while(modules.hasMoreElements()) {
            String module = (String)modules.nextElement();
            Enumeration tests = cfg.getTests(module);

            while(tests.hasMoreElements()) {
                XConfig.Test test = (XConfig.Test)tests.nextElement();
                CallTarget   callee = (CallTarget)getProject().createTask("antcall");
                String       pattern;
                
                callee.setTarget(targetName);
                callee.setOwningTarget(target);
                callee.init();

                Property paramModule = callee.createParam();
                Property paramTestType = callee.createParam();
                Property paramIncludes = callee.createParam();
                Property paramExcludes = callee.createParam();

                paramModule.setName(targetParamModule);
                paramModule.setValue(module);
                paramTestType.setName(targetParamTestType);
                paramTestType.setValue(test.getType());
                
                pattern = listPatterns(test.getPattern().getIncludePatterns(project));
                if (pattern.length () > 0) {
                    paramIncludes.setName(targetParamIncludes);
                    paramIncludes.setValue(pattern);
                }
                
                pattern = listPatterns(test.getPattern().getExcludePatterns(project));
                if (pattern.length () > 0) {
                    paramExcludes.setName(targetParamExcludes);
                    paramExcludes.setValue(pattern);
                }
                
                callee.execute();
            }
        }
    }
    
    private String listPatterns(String [] patterns) {
        if (null == patterns)
            return "";
        
        StringBuffer buf = new StringBuffer();
        for(int i = 0; i < patterns.length; i++) {
            if (0 != i)
                buf.append(",");
                
            buf.append(patterns[i]);
        }
        
        return buf.toString();
    }
}
