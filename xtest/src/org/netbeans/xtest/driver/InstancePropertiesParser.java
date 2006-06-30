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
 * InstancePropertiesParser.java
 *
 * Created on November 8, 2001, 5:23 PM
 */

package org.netbeans.xtest.driver;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import java.util.StringTokenizer;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;

/**
 *
 * @author lm97939
 */
public class InstancePropertiesParser extends Task {

    private String output_property;

    public static final String CVS_WORKDIR = "xtest.instance.cvs.workdir";
    public static final String TEST_ROOT = "xtest.instance.testroot";
    public static final String INSTANCE = "xtest.instance.location";
    public static final String CVS_ROOT = "xtest.instance.cvs.root";
    public static final String CONFIG = "xtest.instance.config";
    public static final String MASTER_CONFIG = "xtest.instance.master-config";
    public static final String MODULE_BRANCHES = "xtest.instance.re.conf.prefix";
    
    public void setProperty(String p) {
        output_property = p;
    }
    
    public void execute() throws BuildException {
        
        if (output_property == null) throw new BuildException("Property 'property' is empty!");
        
        HashSet instances = getPostfixes(getProject());
        
        StringBuffer buff = new StringBuffer();
        Iterator it = instances.iterator(); 
        while (it.hasNext()) { 
           String o = (String) it.next();
           if (buff.length()>0) buff.append(",");
           buff.append(o);
        }
        getProject().setProperty(output_property,buff.toString());
    }
    
    protected static HashSet getPostfixes(Project project) {
        HashSet instances = new HashSet();
        Hashtable props = project.getProperties();
        Enumeration keys = props.keys();
        boolean onlyone = false;
        while (keys.hasMoreElements()) {
            String name = (String)keys.nextElement(); 
            String prefix = null; 
            if (name.startsWith(CVS_WORKDIR)) { prefix = CVS_WORKDIR; }
            if (name.startsWith(TEST_ROOT)) { prefix = TEST_ROOT; }
            if (name.startsWith(INSTANCE)) { prefix = INSTANCE; }
            if (name.startsWith(CVS_ROOT)) { prefix = CVS_ROOT; }
            if (name.startsWith(CONFIG)) { prefix = CONFIG; }
            if (name.startsWith(MASTER_CONFIG)) { prefix = MASTER_CONFIG; }
            if (name.startsWith(MODULE_BRANCHES)) { prefix = MODULE_BRANCHES; }
            if (prefix != null) {
              if (name.equals(prefix)) {
                onlyone = true;
                instances.add("");
              }
              else {
                String postfix = name.substring(prefix.length());
                instances.add(postfix);
              }
            }
        }
        if (onlyone && (instances.size()!=1)) 
                  throw new BuildException("Only one set of instance properties can be without postfix!");
        return instances;
    }
}
