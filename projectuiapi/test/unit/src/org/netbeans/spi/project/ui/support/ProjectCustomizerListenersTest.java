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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.spi.project.ui.support;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.junit.MockServices;

import org.netbeans.junit.NbTestCase;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CategoryComponentProvider;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;

/**
 * Test of OK and Store listeners of ProjectCustomzier dialog
 * 
 * @author Milan Kubec
 */
public class ProjectCustomizerListenersTest extends NbTestCase {
    
    private List<EventRecord> events = new ArrayList<EventRecord>();
    private enum LType { OK, STORE };
    
    public ProjectCustomizerListenersTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(TestDialogDisplayer.class);
        events.clear();
    }
    
    public void testOKAndStoreListeners() {
        
        Category testCat1 = Category.create("test1", "test1", null);
        final Category testCat2 = Category.create("test2", "test2", null, testCat1);
        final Category testCat3 = Category.create("test3", "test3", null);
        
        testCat1.setOkButtonListener(new Listener(LType.OK, "testCat1", true));
        testCat1.setStoreListener(new Listener(LType.STORE, "testCat1", false));
        testCat2.setOkButtonListener(new Listener(LType.OK, "testCat2", true));
        testCat2.setStoreListener(new Listener(LType.STORE, "testCat2", false));
        testCat3.setOkButtonListener(new Listener(LType.OK, "testCat3", true));
        testCat3.setStoreListener(new Listener(LType.STORE, "testCat3", false));
        
        final Listener mainOKListener = new Listener(LType.OK, "Properties", true);
        final Listener mainStoreListener = new Listener(LType.STORE, "Properties", false);
        
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    ProjectCustomizer.createCustomizerDialog(new Category[]{ testCat2, testCat3 }, 
                        new CategoryComponentProviderImpl(), null, mainOKListener, mainStoreListener, 
                        HelpCtx.DEFAULT_HELP);
                }
            });
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        // wait until all events are delivered
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
             ex.printStackTrace();
        }
        
//        for (EventRecord er : events) {
//            System.out.println(er);
//        }
        
        assertEquals(8, events.size());
        assertEquals(new EventRecord(LType.OK, "Properties", 0), events.get(0));
        assertEquals(new EventRecord(LType.OK, "testCat2", 0), events.get(1));
        assertEquals(new EventRecord(LType.OK, "testCat1", 0), events.get(2));
        assertEquals(new EventRecord(LType.OK, "testCat3", 0), events.get(3));
        assertEquals(new EventRecord(LType.STORE, "Properties", 0), events.get(4));
        assertEquals(new EventRecord(LType.STORE, "testCat2", 0), events.get(5));
        assertEquals(new EventRecord(LType.STORE, "testCat1", 0), events.get(6));
        assertEquals(new EventRecord(LType.STORE, "testCat3", 0), events.get(7));
        
    }
    
    public void testOKListener() {
        
        Category testCat1 = Category.create("test1", "test1", null);
        final Category testCat2 = Category.create("test2", "test2", null, testCat1);
        final Category testCat3 = Category.create("test3", "test3", null);
        
        testCat1.setOkButtonListener(new Listener(LType.OK, "testCat1", true));
        testCat2.setOkButtonListener(new Listener(LType.OK, "testCat2", true));
        testCat3.setOkButtonListener(new Listener(LType.OK, "testCat3", true));
        
        final Listener mainOKListener = new Listener(LType.OK, "Properties", true);
        
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    ProjectCustomizer.createCustomizerDialog(new Category[]{ testCat2, testCat3 }, 
                        new CategoryComponentProviderImpl(), null, mainOKListener, 
                        HelpCtx.DEFAULT_HELP);
                }
            });
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        // wait until all events are delivered
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
             ex.printStackTrace();
        }
        
//        for (EventRecord er : events) {
//            System.out.println(er);
//        }
        
        assertEquals(4, events.size());
        assertEquals(new EventRecord(LType.OK, "Properties", 0), events.get(0));
        assertEquals(new EventRecord(LType.OK, "testCat2", 0), events.get(1));
        assertEquals(new EventRecord(LType.OK, "testCat1", 0), events.get(2));
        assertEquals(new EventRecord(LType.OK, "testCat3", 0), events.get(3));
        
    }
    
    private class Listener implements ActionListener {
        
        private LType type;
        private String id;
        private long when;
        private boolean inEQ;
        
        public Listener(LType type, String id, boolean inEQ) {
            this.type = type;
            this.id = id;
            this.inEQ = inEQ;
        }
        
        public void actionPerformed(ActionEvent e) {
            when = System.nanoTime();
            events.add(new EventRecord(type, id, when));
            if (inEQ) {
                assertTrue(SwingUtilities.isEventDispatchThread());
            } else {
                assertFalse(SwingUtilities.isEventDispatchThread());
            }
        }
        
    }
    
    public static final class TestDialogDisplayer extends DialogDisplayer {
        
        public Object notify(NotifyDescriptor descriptor) {
            return null;
        }
        
        public Dialog createDialog(DialogDescriptor descriptor) {
            Object[] options = descriptor.getOptions();
            if (options[0] instanceof JButton) {
                ((JButton) options[0]).doClick();
            }
            return new JDialog();
        }
        
    }
    
    private static final class CategoryComponentProviderImpl implements CategoryComponentProvider {
        public JComponent create(Category category) {
            return new JPanel();
        }
    }
    
    private static final class EventRecord {
        
        public LType type;
        public String id;
        public long when;
        
        public EventRecord(LType type, String id, long when) {
            this.type = type;
            this.id = id;
            this.when = when;
        }
        
        @Override
        public String toString() {
            return type + ", " + id + ", " + when;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof EventRecord) {
                return (type.equals(((EventRecord) obj).type)) && 
                       (id.equals(((EventRecord) obj).id));
            }
            return false;
        }
        
    }
    
}
