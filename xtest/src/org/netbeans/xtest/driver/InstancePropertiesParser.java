/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
