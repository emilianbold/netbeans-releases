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
 * Version.java
 *
 * Created on March 5, 2003, 4:06 PM
 */

package org.netbeans.xtest;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import java.util.*;
import java.util.jar.*;
import java.io.*;

/**
 *
 * @author  mb115822
 */
public class XTestVersion  extends Task {

    public XTestVersion() {
    }

    private static String UNKNOWN="Unknown";

    private String xtestHomeProperty;

    public static Manifest getManifest(String xtestHome) {
        try {
            File xtestHomeFile = new File(xtestHome);
            File xtestJar = new File(xtestHome,"lib/xtest.jar");
            JarFile xtestJarFile = new JarFile(xtestJar);
            Manifest man = xtestJarFile.getManifest();
            if (man == null) {
                throw new MissingResourceException("Cannot find manifest in xtest.jar",null,null);
            } else {
                return man;
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
            throw new MissingResourceException("Version info not available",null,null);
        } 
    }
    
    private Manifest getManifest() {
        return getManifest(xtestHomeProperty);
    }
    
    private void printoutAttributes(Attributes atts) {
        Iterator keys = atts.keySet().iterator();
        while (keys.hasNext()) {
            Attributes.Name key = (Attributes.Name)keys.next();
            System.err.println("Attr:"+key+":"+atts.getValue(key));
        }
    }
    
    
    public static String getMajorVersion(Manifest man) {
            return man.getMainAttributes().getValue("XTest-MajorVersion");
    }
    
    public static String getMinorVersion(Manifest man) {
        return man.getMainAttributes().getValue("XTest-MinorVersion");
    }    
    
    public static String getBranch(Manifest man) {
        return man.getMainAttributes().getValue("XTest-Branch");
    }        
        
    
    public String getMajorVersion() {
        return getMajorVersion(getManifest());
    }
    
    public String getMinorVersion() {
        return getMinorVersion(getManifest());
    }

    public String getBranch() {
        return getBranch(getManifest());
    }    
    
    public void execute() throws BuildException {
        xtestHomeProperty = this.getProject().getProperty("xtest.home");
        if (xtestHomeProperty == null) {
            throw new BuildException("Cannot provide version when xtest.home property is not set. "
                        +"Please use -Dxtest.home=${your-xtest-home} to run the command");
        }
        String version = "unknown";
        try {
            version = getMajorVersion()+"."+getMinorVersion()+" "+getBranch();
        } catch (Exception mre) {
            // cannot find resource --- unkonwn version
        }
        log("XTest version: "+version);        
    }
    
    
}
