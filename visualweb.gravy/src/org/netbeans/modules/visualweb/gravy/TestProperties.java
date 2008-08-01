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
* Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.visualweb.gravy;

import java.io.*;
import java.util.Properties;

/**
 * This class is used as storage of various test properties, which can be loaded 
 * from a file and saved to a file.
 */
public class TestProperties {
    Properties props;
    String propFile;
    
    /**
     * Creates a new instance of this class and loads properties from a file.
     */
    public TestProperties() {
        props = new Properties();
        loadProps();
    }
    
    /**
     * Returns current set of test properties.
     * @return an object Properties
     */
    protected Properties getTestProperties() {
        return(props);
    }
    
    /**
     * Returns a value of required property.
     * @param key a name of required property
     * @return a value of required property (an object String)
     */
    public String getTestProperty(String key) {
        return(props.getProperty(key));
    }
    
    /**
     * Returns a value of required property.
     * @param key a name of required property
     * @return a value of required property (an object String)
     */
    public Object setTestProperty(String key, String value) {
        Object retValue = props.setProperty(key, value);
        saveProps();
        return(retValue);
    }
    
    /**
     * Loads test properties from file.
     */
    private void loadProps() {
        try{
            propFile = System.getProperty("xtest.workdir") + File.separator + "TestProperties";
            props.load(new FileInputStream(propFile));
        } catch(IOException e) {
            //do nothing
        }
    }
    
    /**
     * Stores all test properties to file.
     */
    private void saveProps() {
        try{
            props.store(new FileOutputStream(propFile), "Rave test properties");
        } catch(IOException e) {
            //do nothing
        }
    }
}
