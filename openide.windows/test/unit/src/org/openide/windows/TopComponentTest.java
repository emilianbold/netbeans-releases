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

package org.openide.windows;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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

    TopComponent tcOpened = null;
    TopComponent tcClosed = null;
    
    public void testOpenedClosed () throws Exception {
        System.out.println("Testing property firing of TopComponent's registry");
        tcOpened = null;
        tcClosed = null;
                
        TopComponent.getRegistry().addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (TopComponent.Registry.PROP_TC_OPENED.equals(evt.getPropertyName())) {
                            tcOpened = (TopComponent) evt.getNewValue();
                        } 
                        if (TopComponent.Registry.PROP_TC_CLOSED.equals(evt.getPropertyName())) {
                            tcClosed = (TopComponent) evt.getNewValue();
                        } 
                    }
                });
                
        TopComponent tc = new TopComponent();
        
        tc.open();
        assertNotNull("Property change was not fired, tcOpened is null", tcOpened);
        assertEquals("New value in property change is wrong", tc, tcOpened);
                
        tc.close();
        assertNotNull("Property change was not fired, tcClosed is null", tcClosed);
        assertEquals("New value in property change is wrong", tc, tcClosed);
        
    }
    
    
    
}
