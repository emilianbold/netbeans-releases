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

    private Vector mcfg;
    private Setup setup;

    /** Creates new Config */
    public MConfig(Vector cfg) {        
        this.mcfg = cfg;
    }

    /** Returns enumeration of TestGroup */
    public Enumeration getAllTests() {
        return mcfg.elements();
    }
    
    public void setConfigSetup(Setup setup) {
        this.setup = setup;
    }
    
    public Setup getConfigSetup() {
        return setup;
    }
    
    public static class TestGroup {
        private Vector tests = new Vector();
        private Setup  setup = null;
        private Hashtable properties = new Hashtable();
        
        public TestGroup() {
        }
        
        public void addTest(Test test) {
            tests.add(test);
        }
        
        public Enumeration getTests() {
            return tests.elements();
        }
        
        public void setSetup(Setup s) {
            setup = s;
        }
        
        public Setup getSetup() {
            return setup;
        }
        
        public void setProperties (Hashtable t) {
            properties = t;
        }
        
        public void addProperties(Hashtable t) {
            properties.putAll(t);   
        }
        
        public Hashtable getProperties () {
            return properties;
        }
    }
    
    public static class Test {
        private String [] attribs;
        private String    type;
        private String    module;
        
        public Test(String module, String type) {
            this.type = type;
            this.module = module;
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
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            return sb.toString();
        }
        
        public void setAttributes(String [] attr) {
            attribs = attr;
        }
 
        public String getType() {
            return type;
        }
        
        public String getModule() {
            return module;
        }
    }
    
    public static class Setup {
        
        public static class StartStop {
            public File dir= null;
            public String antfile = null;
            public String target = null;
            public boolean onBackground = false;
            public int delay = 0;
        }

        private StartStop start = null, stop = null;
        private String name;
        
        public void setName(String n) {
            name =n;
        }
        
        public String getName() {
            return name;
        }
        
        public void setStart(StartStop start) {
            this.start = start;
        }
        
        public void setStop(StartStop stop) {
            this.stop = stop;
        }
        
        public File getStartDir() {
            return start==null?null:start.dir;
        }
        
        public File getStopDir() {
            return stop==null?null:stop.dir;
        }
        
        public String getStartAntfile() {
            return start==null?null:start.antfile;
        }
        
        public String getStopAntfile() {
            return stop==null?null:stop.antfile;
        }
        
        public String getStartTarget() {
            return start==null?null:start.target;
        }
        
        public String getStopTarget() {
            return stop==null?null:stop.target;
        }
        
        public boolean getStartOnBackground() {
            return start==null?false:start.onBackground;
        }
        
        public boolean getStopOnBackground() {
            return stop==null?false:stop.onBackground;
        }
        
        public int getStartDelay() {
            return start==null?0:start.delay;
        }
        
        public int getStopDelay() {
            return stop==null?0:stop.delay;
        }
    }
    
    
     
        
}
