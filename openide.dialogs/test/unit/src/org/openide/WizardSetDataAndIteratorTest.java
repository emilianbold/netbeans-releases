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
package org.openide;

import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.NbTestCase;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.HelpCtx;

/**
 *
 * @author Jaroslav Tulach
 */
public class WizardSetDataAndIteratorTest extends NbTestCase {

    public WizardSetDataAndIteratorTest(String s) {
        super(s);
    }

    
    public void testSetDataAndIterator() throws Exception {
        MyWizard w = new MyWizard();        
        assertTrue("Finish enabled", w.isFinishEnabled());        
        assertEquals("Right Settings passed", w, MyPanel.set);
    }
    private static class MyWizard extends WizardDescriptor {
        public MyWizard() {
            super();
            setPanelsAndSettings(new MyIter(), this);
        }
    }
    
    private static class MyIter implements WizardDescriptor.Iterator<MyWizard> {
        private MyPanel myPanel = new MyPanel();
        
        public Panel<org.openide.WizardSetDataAndIteratorTest.MyWizard> current() {
            return myPanel;
        }

        public String name() {
            return "OneName";
        }

        public boolean hasNext() {
            return false;
        }

        public boolean hasPrevious() {
            return false;
        }

        public void nextPanel() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void previousPanel() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void addChangeListener(ChangeListener l) {
        }

        public void removeChangeListener(ChangeListener l) {
        }        
    }
    
    private static class MyPanel implements WizardDescriptor.Panel<MyWizard> {
        private static MyWizard set;
        
        private JPanel cmp = new JPanel();
        
        public Component getComponent() {
            return cmp;
        }

        public HelpCtx getHelp() {
            return HelpCtx.DEFAULT_HELP;
        }

        public void readSettings(org.openide.WizardSetDataAndIteratorTest.MyWizard settings) {
            assertNull("Not yet set", set);
            set = settings;
        }

        public void storeSettings(org.openide.WizardSetDataAndIteratorTest.MyWizard settings) {
        }

        public boolean isValid() {
            return true;
        }

        public void addChangeListener(ChangeListener l) {
        }

        public void removeChangeListener(ChangeListener l) {
        }
    }
}
