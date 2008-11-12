/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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

    @Override
    protected final boolean runInEQ () {
        return true;
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
