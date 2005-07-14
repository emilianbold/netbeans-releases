/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.awt;

import java.awt.Component;
import java.util.ArrayList;
import junit.framework.*;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.actions.Presenter;

/**
 *
 * @author mkleint
 */
public class DynaMenuModelTest extends TestCase {
    
    public DynaMenuModelTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(DynaMenuModelTest.class);
        
        return suite;
    }

    /**
     * Test of loadSubmenu method, of class org.openide.awt.DynaMenuModel.
     */
    public void testLoadSubmenu() {
        System.out.println("loadSubmenu");
        
        List cInstances = new ArrayList();
        cInstances.add(new Act1());
        cInstances.add(new Act2());
        JMenu m = new JMenu();
        DynaMenuModel instance = new DynaMenuModel();
        
        instance.loadSubmenu(cInstances, m);
        Component[] comps = m.getPopupMenu().getComponents();
        assertEquals("0", ((JMenuItem)comps[0]).getText());
        assertEquals("1", ((JMenuItem)comps[1]).getText());
        assertEquals("2", ((JMenuItem)comps[2]).getText());
        
    }

    /**
     * Test of checkSubmenu method, of class org.openide.awt.DynaMenuModel.
     */
    public void testCheckSubmenu() {
        List cInstances = new ArrayList();
        cInstances.add(new Act1());
        cInstances.add(new Act2());
        JMenu m = new JMenu();
        DynaMenuModel instance = new DynaMenuModel();
        
        instance.loadSubmenu(cInstances, m);
        instance.checkSubmenu(m);
        
        Component[] comps = m.getPopupMenu().getComponents();
        assertEquals("0", ((JMenuItem)comps[0]).getText());
        assertEquals("1x", ((JMenuItem)comps[1]).getText());
        assertEquals("2x", ((JMenuItem)comps[2]).getText());
        
    }
    
    private class Act1 extends AbstractAction implements Presenter.Menu {
        public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        }
        
        public JMenuItem getMenuPresenter() {
            return new JMenuItem("0");
        }
    }
    
    private class Act2 extends AbstractAction implements Presenter.Menu {
        public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        }
        
        public JMenuItem getMenuPresenter() {
            return new Dyna();
        }
    }    
    
    private class Dyna extends JMenuItem implements DynamicMenuContent {
        private JMenuItem itm1;
        private JMenuItem itm2;
        public JComponent[] getMenuPresenters() {
            itm1 = new JMenuItem();
            itm1.setText("1");
            itm2 = new JMenuItem();
            itm2.setText("2");
            return new JComponent[] {
                itm1,
                itm2
            };
        }
    
        public JComponent[] synchMenuPresenters(JComponent[] items) {
            ((JMenuItem)items[0]).setText("1x");
            ((JMenuItem)items[1]).setText("2x");
            return items;
        }
    }    
    
}
