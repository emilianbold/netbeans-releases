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

/**
 * @author Radek Matous
 */
public class TaskListSettingsTest extends BasicTestForImport {
    public TaskListSettingsTest(String testName) {
        super(testName, "org-netbeans-modules-tasklist-docscan-Settings.settings");
    }

    public void testPreferencesNodePath() throws Exception {
        assertPreferencesNodePath("/org/netbeans/modules/tasklist/docscan");
    }
    
    public void testPropertyNames() throws Exception {
        assertPropertyNames(new String[] {"Tag:<<<<<<<",
                "Tag:@todo",
                "Tag:FIXME",
                "Tag:PENDING",
                "Tag:TODO",
                "Tag:XXX",
                "skipComments",
                "modificationTime",
                "usabilityLimit"
        });
    }
    
    public void testModificationTime() throws Exception {
        assertPropertyType("modificationTime", "java.lang.Long");
        assertProperty("modificationTime", "0");
    }
    public void testSkipComments() throws Exception {
        assertPropertyType("skipComments","java.lang.Boolean");
        assertProperty("skipComments","false");
    }
    public void testUsabilityLimit() throws Exception {
        assertPropertyType("usabilityLimit","java.lang.Integer");
        assertProperty("usabilityLimit","300");
    }
    
    public void testTaskTagsTypes() throws Exception {
        assertProperty("Tag:<<<<<<<", "1");
        assertProperty("Tag:@todo","3");
        assertProperty("Tag:FIXME","3");
        assertProperty("Tag:PENDING","3");
        assertProperty("Tag:TODO","3");
        assertProperty("Tag:XXX","3");
    } 
}
