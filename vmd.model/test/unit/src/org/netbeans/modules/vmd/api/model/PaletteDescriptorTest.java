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
package org.netbeans.modules.vmd.api.model;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author Karol Harezlak
 */
public class PaletteDescriptorTest extends TestCase {

    public PaletteDescriptorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(PaletteDescriptorTest.class);
        
        return suite;
    }
    
    /**
     * Complex test of class org.netbeans.modules.vmd.api.model.PaletteDescriptor.
     */
    public void testIsComplexTest() {
        System.out.println("Complex test"); // NOI18N
        
        String displayName = "Test Palette"; // NOI18N
        String categotyID = "Test category ID"; //NOI18N
        String toolTip = "Test tool tip"; //NOI18N
        
        //Implementation of java.awt.image
        String smallIcon =  "small_image";
        String largeIcon = "large_image";
        PaletteDescriptor instance = new PaletteDescriptor(categotyID, displayName, toolTip, smallIcon, largeIcon);
        assertEquals(displayName, instance.getDisplayName());
        assertEquals(largeIcon, instance.getLargeIcon());
        assertEquals(smallIcon, instance.getSmallIcon());
        assertEquals(toolTip, instance.getToolTip());
        assertEquals(categotyID, instance.getCategoryID());
    }
}
