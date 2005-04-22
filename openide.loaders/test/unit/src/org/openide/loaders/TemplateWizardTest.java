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

import java.io.IOException;
import javax.swing.event.ChangeListener;
import junit.textui.TestRunner;
import org.netbeans.junit.*;
import javax.swing.JPanel;
import org.openide.WizardDescriptor;
import org.openide.filesystems.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;



/** Checks the testable behaviour of TemplateWizard
 * @author Jaroslav Tulach, Jiri Rechtacek
 */
public class TemplateWizardTest extends NbTestCase {
    
    public TemplateWizardTest (String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(TemplateWizardTest.class));
    }
    

    protected void setUp() throws java.lang.Exception {
         FileObject fo = Repository.getDefault ().getDefaultFileSystem ().getRoot ();
         FileUtil.createFolder (fo, "Templates");
    }

    /** Does getIterator honours DataObject's cookies?
     */
    public void testGetIteratorHonoursDataObjectsCookies () throws Exception {
        LocalFileSystem fs = new LocalFileSystem ();
        DataObject obj;
        Loader l = (Loader)Loader.findObject (Loader.class, true);
        try {
            AddLoaderManuallyHid.addRemoveLoader (l, true);
            obj = DataObject.find (fs.getRoot ());
        } finally {
            AddLoaderManuallyHid.addRemoveLoader (l, false);
        }
        
        TemplateWizard.Iterator it = TemplateWizard.getIterator (obj);
        
        assertEquals ("Iterator obtained from the object's cookie", obj, it);
    }
    
    private static class DO extends DataFolder implements TemplateWizard.Iterator {
        public DO (FileObject fo) throws DataObjectExistsException {
            super (fo);

            getCookieSet ().add (this);
        }

        //
        // Dummy implementation of wizard iterator
        //

        public void addChangeListener(ChangeListener l) {
        }
        public TemplateWizard.Panel current() {
            return null;
        }
        public boolean hasNext() {
            return false;
        }
        public boolean hasPrevious() {
            return false;
        }
        public void initialize(TemplateWizard wiz) {
        }
        public java.util.Set instantiate(TemplateWizard wiz) throws IOException {
            throw new IOException ();
        }
        public String name() {
            return "";
        }
        public void nextPanel() {
        }
        public void previousPanel() {
        }
        public void removeChangeListener(ChangeListener l) {
        }
        public void uninitialize(TemplateWizard wiz) {
        }
    } // end of DO
    private static class Loader extends UniFileLoader {
        public Loader () {
            super (DO.class.getName ());
        }

        protected FileObject findPrimaryFile (FileObject fo) {
            if (fo.isFolder ()) {
                return fo;
            } else {
                return null;
            }
        }

        protected MultiDataObject createMultiObject (FileObject fo) throws IOException {
            return new DO (fo);
        }
    } // end of Loader

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
        assertEquals ("IterImpl returns template chooser.", wizard.templateChooser (), iter.current ());
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
        WizardDescriptor.Panel oldPanel = iter.current ();
        iter.setIterator (newIter, notify);
        iter.nextPanel ();
        assertEquals ("IterImpl returns the first panel of newly delegated iterator, ", arr[0], iter.current ());
        iter.previousPanel ();
        assertEquals ("IterImpl returns the first panel of old iterator on previous, ", oldPanel, iter.current ());
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


