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

package org.netbeans.upgrade.systemoptions;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Radek Matous
 */
public abstract class BasicTestForImport extends NbTestCase {
    private FileObject f;
    private String fileName;
    
    
    public BasicTestForImport(String testName, String fileName) {
        super(testName);
        this.fileName = fileName;
    }
    
    protected void setUp() throws Exception {
        URL u = getClass().getResource(getFileName());
        File ff = new File(u.getFile());//getDataDir(),getFileName()
        f = FileUtil.toFileObject(ff);
        assert f != null;
    }
    
    private final String getFileName() {
        return fileName;
    }
    

    /**
     * overload this test in your TestCase see <code>IDESettingsTest</code>
     */
    public void testPropertyNames() throws Exception {
        assertPropertyNames(new String[] {"just_cause_fail"
        });
    }
    
    public void testPreferencesNodePath() throws Exception {
        assertPreferencesNodePath("just_cause_fail");
    }
    
    
    private DefaultResult readSystemOption(boolean types) throws IOException, ClassNotFoundException {
        return SystemOptionsParser.parse(f, types);
    }

    public void assertPropertyNames(final String[] propertyNames) throws IOException, ClassNotFoundException {
        assertEquals(new TreeSet<String>(Arrays.asList(propertyNames)).toString(),
                new TreeSet<String>(Arrays.asList(readSystemOption(false).getPropertyNames())).toString());
    }
    
    public void assertProperty(final String propertyName, final String expected) throws IOException, ClassNotFoundException {
        Result support = readSystemOption(false);
        
        List parsedPropNames = Arrays.asList(support.getPropertyNames());
        
        String parsedPropertyName = null;
        boolean isFakeName = !parsedPropNames.contains(propertyName);
        if (isFakeName) {
            assertTrue(propertyName+" (alias: "+parsedPropertyName + ") not found in: " + parsedPropNames,parsedPropNames.contains(parsedPropertyName));
        } else {
            parsedPropertyName = propertyName;
        }
        
        assertNotNull(parsedPropertyName);
        Class expectedClass = null;
        String actual = support.getProperty(parsedPropertyName);
        if (actual == null) {
            assertNull(expectedClass);
            assertEquals(expected, actual);
        } else {
            assertEquals(expected, actual);
        }
    }    
    
    public void assertPropertyType(final String propertyName, final String expected) throws IOException, ClassNotFoundException {
        Result support = readSystemOption(true);
        List parsedPropNames = Arrays.asList(support.getPropertyNames());        
        String parsedPropertyName = null;
        boolean isFakeName = !parsedPropNames.contains(propertyName);
        if (isFakeName) {
            assertTrue(propertyName+" (alias: "+parsedPropertyName + ") not found in: " + parsedPropNames,parsedPropNames.contains(parsedPropertyName));
        } else {
            parsedPropertyName = propertyName;
        }
        
        assertNotNull(parsedPropertyName);
        String actual = support.getProperty(parsedPropertyName);
        if (actual == null) {
            assertNull(expected);
        } else {
            Class expectedClass = null;
            try {
                expectedClass = Class.forName(expected);
            } catch (ClassNotFoundException ex) {
            }
            if (expectedClass != null) {
                Class cls = Class.forName(actual);
                assertTrue(expectedClass + " but : " + cls,expectedClass.isAssignableFrom(cls));
            } else {
                assertEquals(expected, actual);
            }
            assertEquals(expected, actual);
        }
    }
    
    public void assertPropertyTypeAndValue(String propertyName, String expectedType, String expectedValue) throws Exception {
        assertPropertyType(propertyName, expectedType);
        assertProperty(propertyName, expectedValue);
    }
    
    public void assertPreferencesNodePath(final String expectedInstanceName) throws IOException, ClassNotFoundException {
        DefaultResult support = readSystemOption(true);
        assertEquals(expectedInstanceName,"/"+support.getModuleName());//NOI18N
    }        
}
