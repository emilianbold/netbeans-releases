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
    String modules      = null;
    String prefix       = "xtest.";
    String keyParam     = null;
    String valueParam   = null;
    String defaultValue = null;
    
    public void setTargetName(String name) {
        this.targetName = name;
    }
    
    public void setModules(String modules) {
        this.modules = modules;
    }
    
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    
    public void setKeyParam(String keyParam) {
        this.keyParam = keyParam;
    }
    
    public void setValueParam(String valueParam) {
        this.valueParam = valueParam;
    }
    
    public void setDefaultValue(String value) {
        this.defaultValue = value;
    }

    public void execute () throws BuildException {
        if (null == targetName || 0 == targetName.length())
            throw new BuildException("Attribute 'targetname' has to be set.");
        if (null == modules || 0 == modules.length())
            throw new BuildException("Attribute 'modules' has to be set.");
        if (null == keyParam || 0 == keyParam.length())
            throw new BuildException("Attribute 'keyparam' has to be set.");
        if (null == valueParam || 0 == valueParam.length())
            throw new BuildException("Attribute 'valueparam' has to be set.");
        
        
        StringTokenizer t = new StringTokenizer(modules, ",");
        
        while(t.hasMoreTokens()) {
            String          key = t.nextToken();
            String          value = getProperty(prefix + key.trim());
            CallTarget      callee = (CallTarget)getProject().createTask("antcall");
            
            callee.setTarget(targetName);
            callee.setOwningTarget(target);
            callee.init();
            
            Property keyPrm = callee.createParam();
            Property valuePrm = callee.createParam();
            
            keyPrm.setName(keyParam);
            keyPrm.setValue(key);
            valuePrm.setName(valueParam);
            valuePrm.setValue(value);
            
            callee.execute();
        }
    }

    private String getProperty(String name) throws BuildException {
        String value;
        
        if (null == (value = getProject().getProperty(name))) {
            if (null == defaultValue)
                throw new BuildException("Property '" + name + "' not found.");
            else
                value = defaultValue;
        }
        
        return value;
    }
}
