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
//import org.apache.tools.ant.types.PatternSet;

/**
 *
 * @author  mk97936
 * @version 
 */
public class MConfig {
    
    private Hashtable mcfg;
    
    /** Creates new Config */
    public MConfig(Hashtable cfg) {        
        this.mcfg = cfg;
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
}
