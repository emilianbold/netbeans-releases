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
 * UserPropertyWriter.java
 *
 * Created on November 12, 2001, 4:30 PM
 */

package org.netbeans.xtest;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import java.util.Properties;
import java.io.File;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.StringTokenizer;

/** This task writes all properties which correspond to criteria described below
 * to file which name is in property 'file'. Properties which is writen has to have 
 * prefix specified in attribute 'propertyPrefix' and after the prefix they has to 
 * have character '|'. After prefix and before '|' property may have word in parenthesis
 * and this word has to be one of the words in attribute 'attribs'. Only part 
 * after '|' of the property is written to file.
 * 
 * Example: 
 *
 * <property name="xtest.userdata|property1 value="value1"/>
 * <property name="xtest.userdata(oracle)|property2 value="value2"/>
 * <property name="xtest.userdata(pointbase)|property3 value="value3"/>
 * <write-property propertyPrefix="xtest.userdata" attribs="pointbase,mssql" file="propertyfile.txt"/>
 *
 * Result will be propertyfile.txt with content:
 * property1=value1
 * property3=value3
 *
 * @author lm97939
 */
public class UserPropertyWriter extends Task {

    private String prefix;
    private File file;
    private String attribs;
    
    public void setPropertyPrefix(String p) {
        prefix = p;
    }
    
    public void setFile(File f) {
        file = f;
    }
    
    public void setAttribs(String a) {
        attribs = a;
    }

    public void execute() throws BuildException {
        final String HEADER = "Properties passed to test";
        final String PREFIX = "xtest.userdata";
        Properties properties = new Properties();
        
        if (file == null) throw new BuildException("Attribute 'file' is empty.", getLocation());
        if (attribs == null) throw new BuildException("Attribute 'attribs' is empty.", getLocation());
        if (prefix == null) 
            log("No propertyPrefix set. All properties will be written to file.");
        
        if (!file.getParentFile().exists())
            file.getParentFile().mkdirs();
        
        Hashtable table = getProject().getProperties();
        Enumeration en = table.keys();
        while (en.hasMoreElements()) {
            String key = (String) en.nextElement();
            StringTokenizer attrtokens = new StringTokenizer(attribs,","); 
            while (attrtokens.hasMoreTokens()) {
                String attr = attrtokens.nextToken();
                if (prefix == null || key.startsWith(prefix+"|") || key.startsWith(PREFIX+"("+attr+")|")) {
                      int i = key.indexOf("|");
                      if (prefix == null || i == -1)
                          properties.setProperty(key,getProject().getProperty(key));
                      else 
                          properties.setProperty(key.substring(i+1),getProject().getProperty(key));
                      break;
                }
            }
        }
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            properties.store(bos,HEADER);
            bos.close();
        }
        catch (java.io.IOException e) { throw new BuildException(e, getLocation()); }

    }

}
