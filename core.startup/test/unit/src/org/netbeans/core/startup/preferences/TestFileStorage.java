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

package org.netbeans.core.startup.preferences;

import java.io.IOException;
import java.util.Properties;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Radek Matous
 */
public class TestFileStorage extends NbPreferencesTest.TestBasicSetup {
    protected NbPreferences.FileStorage instance;
    
    
    public TestFileStorage(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        instance = getInstance();
    }
    
    protected NbPreferences.FileStorage getInstance() {
        return PropertiesStorage.instance("/FileStorageTest/" + getName());//NOI18N);
    }
    
    public void testBasic() throws IOException {
        //preconditions
        noFileRepresentationAssertion();
        
        //load doesn't change file layout
        Properties p = instance.load();
        p.put("key", "value");//NOI18N
        noFileRepresentationAssertion();
        
        //save doesn't change file layout if not marked modified
        instance.save(p);
        noFileRepresentationAssertion();
        
        //marked modified but not saved
        instance.markModified();
        noFileRepresentationAssertion();
        
        if (!instance.isReadOnly()) {
            //saved after marked modified
            instance.save(p);            
            fileRepresentationAssertion();
        } else {
            try {
                //saved after marked modified
                instance.save(p);                
                fail();
            } catch (IOException ex) {}
            noFileRepresentationAssertion();
        }
    }
    
    void noFileRepresentationAssertion() throws IOException {
        assertFalse(instance.existsNode());
    }
    
    void fileRepresentationAssertion() throws IOException {
        assertTrue(instance.existsNode());
    }
}
