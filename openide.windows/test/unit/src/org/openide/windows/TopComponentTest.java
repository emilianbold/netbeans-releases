/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.windows;

import java.io.ObjectInputStream;
import junit.framework.TestCase;
import org.openide.util.io.NbMarshalledObject;

/**
 *
 * @author mkleint
 */
public class TopComponentTest extends TestCase {
    
    public TopComponentTest(String testName) {
        super(testName);
    }
    
    /**
     * Test of readExternal method, of class org.openide.windows.TopComponent.
     */
    public void testReadExternal() throws Exception {
        // first try tc with displayname
        TopComponent tc = new TopComponent();
        tc.setName("testName");
        tc.setDisplayName("testdisplayName");
        tc.setToolTipText("testTooltip");
        NbMarshalledObject obj = new NbMarshalledObject(tc);
        tc.close();
        
        tc = (TopComponent)obj.get();
        assertNotNull("One again", tc);
        assertEquals("testName", tc.getName());
        assertEquals("testTooltip", tc.getToolTipText());
        assertEquals("testdisplayName", tc.getDisplayName());
        
        // now try tc withOUT displayname
        tc = new TopComponent();
        tc.setName("testName");
        tc.setToolTipText("testTooltip");
        obj = new NbMarshalledObject(tc);
        tc.close();
        
        tc = (TopComponent)obj.get();
        assertNotNull("One again", tc);
        assertEquals("testName", tc.getName());
        assertEquals("testTooltip", tc.getToolTipText());
        assertNull(tc.getDisplayName());
        
    }
    
    /**
     * Test of readExternal method, of class org.openide.windows.TopComponent.
     */
    public void testOldReadExternal() throws Exception {
        TopComponent tc = null;
        try {
            ObjectInputStream stream = new ObjectInputStream(
                    getClass().getResourceAsStream("data/oldTcWithoutDisplayName.ser"));
            tc = (TopComponent)stream.readObject();
            stream.close();
        } catch (Exception exc) {
            exc.printStackTrace();
            fail("Cannot read tc");
        }
        
        
        assertNotNull("One again", tc);
        assertEquals("testName", tc.getName());
        assertEquals("testTooltip", tc.getToolTipText());
        assertEquals("If the old component does not have a display name, then keep it null", null, tc.getDisplayName());
    }
    
    
    
    //    public void testXXX() {
    //        File fil = new File("/Users/mkleint/oldTcWithoutDisplayName.ser");
    //        TopComponent tc = new TopComponent();
    //        tc.setName("testName");
    //        tc.setDisplayName("testdisplayName");
    //        tc.setToolTipText("testTooltip");1
    //        System.out.println("file=" + fil);
    //        try {
    //            ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(fil));
    //            stream.writeObject(tc);
    //            stream.close();
    //        } catch (Exception exc) {
    //            System.out.println("exception");
    //        }
    //        // TODO code application logic here
    //    }
    
    
    
}
