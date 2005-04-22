/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.loaders;
import javax.swing.JFrame;

import javax.swing.event.ChangeEvent;

import javax.swing.event.ChangeListener;

import org.netbeans.junit.NbTestCase;

/**
 *
 * @author  pzajac
 */
public class NewObjectPanelTest extends NbTestCase{
    
    private static class MyChangeListener implements ChangeListener {
        public void stateChanged(ChangeEvent event) {}
    }
    /** Creates a new instance of NewObjectPanelTest */
    public NewObjectPanelTest(String name) {
        super(name);
    }
    
    public void testNewObjectPanelTest() {
        NewObjectPanel panel = new NewObjectPanel();
        JFrame frame = new JFrame("sss");
        
        frame.getContentPane().add(panel, java.awt.BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
        assertNotNull(panel.getNewObjectName());
        assertNotNull(panel.getPreferredSize());
        MyChangeListener list = new MyChangeListener();
        panel.addChangeListener(list);
        panel.removeChangeListener(list);
        panel.addNotify();
        assertNotNull(panel.defaultNewObjectName());
        frame.dispose();
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }
    
}
