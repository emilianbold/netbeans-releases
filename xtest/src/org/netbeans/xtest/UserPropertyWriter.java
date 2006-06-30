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
