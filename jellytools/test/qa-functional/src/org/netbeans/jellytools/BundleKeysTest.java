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
package org.netbeans.jellytools;

import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import junit.framework.*;
import org.netbeans.junit.*;

/**
 * Test of all resurce bundle keys used in Jelly2
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public class BundleKeysTest extends NbTestCase {

    String keys;

    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    private static Properties props;
    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(BundleKeysTest.class);
        try {
            props=new Properties();
            props.load(BundleKeysTest.class.getClassLoader().getResourceAsStream("org/netbeans/jellytools/BundleKeysTest.properties"));
            Set bundles=props.keySet();
            for(Object bundle : bundles) {
                conf = conf.addTest((String) bundle);
            }
        } catch (Exception e) {}
        return NbModuleSuite.create(conf.clusters(".*").enableModules(".*"));
        /*
        NbTestSuite suite = new NbTestSuite();
        try {
            props=new Properties();
            props.load(BundleKeysTest.class.getClassLoader().getResourceAsStream("org/netbeans/jellytools/BundleKeysTest.properties"));
            Enumeration bundles=props.keys();
            String bundle;
            while (bundles.hasMoreElements()) {
                bundle=(String)bundles.nextElement();
                suite.addTest(new BundleKeysTest(bundle, props.getProperty(bundle)));
            }
        } catch (Exception e) {}
        return suite;
         */
    }
    
    public BundleKeysTest(String bundleName) {
        this(bundleName, null);
    }
    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public BundleKeysTest(String bundleName, String keys) {
        super(bundleName);
        this.keys=keys;
    }
    
    protected void runTest() throws Throwable {
        if(keys == null) {
            if(props == null) {
                props=new Properties();
                props.load(BundleKeysTest.class.getClassLoader().getResourceAsStream("org/netbeans/jellytools/BundleKeysTest.properties"));
            }
            keys = props.getProperty(getName());
        }
        ResourceBundle bundle=ResourceBundle.getBundle(getName());
        StringTokenizer tok=new StringTokenizer(keys, ",");
        String key="";
        String missing="";
        int mis=0;
        while (tok.hasMoreTokens()) try {
            key=tok.nextToken();
            bundle.getObject(key);
        } catch (MissingResourceException mre) {
            missing+=key+" ";
            mis++;
        }
        if (mis>0)
            throw new AssertionFailedError("Missing "+String.valueOf(mis)+" key(s): "+missing);
    }
   
}
