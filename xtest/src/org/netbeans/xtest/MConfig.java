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
 * Config.java
 *
 * Created on May 2, 2001, 9:54 PM
 */

package org.netbeans.xtest;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.HashSet;
import java.io.File;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Ant;

//import org.apache.tools.ant.types.PatternSet;

/**
 *
 * @author  mk97936
 * @version 
 */
public class MConfig {
    
    private Hashtable mcfg;
    private Hashtable prop;
    private Setup setup;
    private Hashtable setuptable;
    
    /** Creates new Config */
    public MConfig(Hashtable cfg) {        
        this.mcfg = cfg;
    }

    public void setProperties(Hashtable t) {
        prop = t;
    }
    
    public Hashtable getProperties(String module) {
        Hashtable properties = (Hashtable) prop.get(module);
        if (null != properties)
            return properties;
        return null;
    } 
    
    public Enumeration getModules() {
        return mcfg.keys();
    }
    
    public Enumeration getTests(String module) {
        Vector types = (Vector) mcfg.get(module);
        
        if (null != types)
            return types.elements();
        
        return null;
    }
    
    public void setConfigSetup(Setup setup) {
        this.setup = setup;
    }
    
    public Setup getConfigSetup() {
        return setup;
    }
    
    public void setSetups(Hashtable setuptable) {
        this.setuptable = setuptable;
    }
     
    public Setup getSetup(String module) {
        Setup msetup = (Setup) setuptable.get(module); 
        return msetup;
    }
    
    public static class Test {
        private String [] attribs;
        private String    type;
        
        public Test(String type) {
            this.type = type;
        }
        
        public String [] getAttributes() {
            return attribs;
        }
        
        public String getAttributesAsString() {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < attribs.length; i++) {
                sb.append(attribs[i]);
                sb.append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        }
        
        public void setAttributes(String [] attr) {
            attribs = attr;
        }
 
        public String getType() {
            return type;
        }
    }
    
    public static class Setup {
        private File startDir, stopDir;
        private String startAntFile, stopAntFile;
        private String startTarget, stopTarget;
        
        public void setStart(File startDir, String startAntFile, String startTarget) {
            this.startDir = startDir;
            this.startAntFile = startAntFile;
            this.startTarget = startTarget;
        }
        
        public void setStop(File stopDir, String stopAntFile, String stopTarget) {
            this.stopDir = stopDir;
            this.stopAntFile = stopAntFile;
            this.stopTarget = stopTarget;
        }
        
        public File getStartDir() {
            return startDir;
        }
        
        public File getStopDir() {
            return stopDir;
        }
        
        public String getStartAntfile() {
            return startAntFile;
        }
        
        public String getStopAntfile() {
            return stopAntFile;
        }
        
        public String getStartTarget() {
            return startTarget;
        }
        
        public String getStopTarget() {
            return stopTarget;
        }
    }
    
    
     
        
}
