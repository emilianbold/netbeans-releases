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

import java.io.*;
import javax.swing.JPanel;
import junit.framework.*;
import org.openide.WizardDescriptor;
import org.openide.filesystems.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.*;


public class TemplateWizardTest extends TestCase {
    
    public TemplateWizardTest (java.lang.String testName) {
        super (testName);
    }
    
    public static Test suite () {
        TestSuite suite = new TestSuite (TemplateWizardTest.class);
        return suite;
    }

    protected void setUp() throws java.lang.Exception {
         FileObject fo = Repository.getDefault ().getDefaultFileSystem ().getRoot ();
         FileUtil.createFolder (fo, "Templates");
    }

    protected void tearDown() throws java.lang.Exception {
    }
    
    public void testNextOnIterImpl () {
        doNextOnIterImpl (false);
    }
    
    public void testNextOnIterImplWithNotification () {
        doNextOnIterImpl (true);
    }
    
    private void doNextOnIterImpl (boolean notify) {
        TemplateWizard wizard = new TemplateWizard ();
        wizard.initialize ();
        TemplateWizardIterImpl iter = wizard.getIterImpl ();
        assertEquals ("IterImpl returns template chooser.", iter.current (), wizard.templateChooser ());
        final WizardDescriptor.Panel[] arr = {new P(1), new P(2)};
        class I extends WizardDescriptor.ArrayIterator implements TemplateWizard.Iterator {
            public I () {
                super (arr);
            }
            public java.util.Set instantiate (TemplateWizard wiz) throws IOException {
                throw new IOException ();
            }
            public void initialize(TemplateWizard wiz) {}
            public void uninitialize(TemplateWizard wiz) {}
        }
        
        I newIter = new I ();
        iter.setIterator (newIter, notify);
        iter.nextPanel ();
        assertEquals ("IterImpl returns the first panel of newly delegated iterator.", arr[0], iter.current ());
    }
    
    public static class P implements WizardDescriptor.Panel {
        int index;
        public P (int i) {
            index = i;
        }
        
        public void removeChangeListener (javax.swing.event.ChangeListener l) {
        }

        public void addChangeListener (javax.swing.event.ChangeListener l) {
        }

        public void storeSettings (Object settings) {
        }

        public void readSettings (Object settings) {
        }

        public boolean isValid () {
            return true;
        }

        public org.openide.util.HelpCtx getHelp () {
            return null;
        }

        public java.awt.Component getComponent () {
            return new JPanel ();
        }
        
        public String toString () {
            return Integer.toString (index);
        }
        
    }

}
