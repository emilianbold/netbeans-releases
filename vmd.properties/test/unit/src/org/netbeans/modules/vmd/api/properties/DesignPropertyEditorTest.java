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
package org.netbeans.modules.vmd.api.properties;

import java.util.Collection;
import java.util.Map;

import junit.framework.TestCase;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.TypeID;

/**
 *
 * @author Karol Harezlak
 */
public class DesignPropertyEditorTest extends TestCase {
    
    public DesignPropertyEditorTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }

    /**
     * Test of addToCreate method, of class DefaultPropertyEditorValue.
     */
    public void testAddToCreate() {
        System.out.println("addToCreate");
        
        DesignComponent component = null;
        Collection<TypeID> typeIDs = null;
        GroupValue instance = new GroupValue();

        String key1 = "key1"; //NOI18N
        String key2 = "key2"; //NOI18N
        String key3 = "key3"; //NOI18N
        String key4 = "key1"; //NOI18N
        
        instance.getValuesMap().put(key3, "1"); //NOI18N
        instance.getValuesMap().put(key1, "1"); //NOI18N
        instance.getValuesMap().put(key2, "2"); //NOI18N
        instance.getValuesMap().put(key1, "2"); //NOI18N
        instance.getValuesMap().put(key4, "1"); //NOI18N
        instance.getValuesMap().put(key1, "1"); //NOI18N
        instance.getValuesMap().put(key3, "1"); //NOI18N
        System.out.println("Map :" + instance.getValuesMap());
        assertEquals(3, instance.getValuesMap().keySet().size());       
    }
    
}
