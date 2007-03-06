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
package org.netbeans.jellytools;

import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
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
    
    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        try {
            Properties props=new Properties();
            props.load(BundleKeysTest.class.getClassLoader().getResourceAsStream("org/netbeans/jellytools/BundleKeysTest.properties"));
            Enumeration bundles=props.keys();
            String bundle;
            while (bundles.hasMoreElements()) {
                bundle=(String)bundles.nextElement();
                suite.addTest(new BundleKeysTest(bundle, props.getProperty(bundle)));
            }
        } catch (Exception e) {}
        return suite;
    }
    
    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public BundleKeysTest(String bundleName, String keys) {
        super(bundleName);
        this.keys=keys;
    }
    
    protected void runTest() throws Throwable {
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
