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

package org.openide.loaders;

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.NbTestCase;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.HelpCtx;
/** Checks the testable behaviour of TemplateWizard
 * @author Jaroslav Tulach, Jiri Rechtacek
 */
public class TemplateWizardTest extends NbTestCase {
    
    public TemplateWizardTest (String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
         FileObject fo = Repository.getDefault ().getDefaultFileSystem ().getRoot ();
         FileUtil.createFolder (fo, "Templates");
    }

    /** Does getIterator honours DataObject's cookies?
     */
    public void testGetIteratorHonoursDataObjectsCookies () throws Exception {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        DataObject obj;
        Loader l = Loader.findObject (Loader.class, true);
        try {
            AddLoaderManuallyHid.addRemoveLoader (l, true);
            obj = DataObject.find (fs.getRoot ());
        } finally {
            AddLoaderManuallyHid.addRemoveLoader (l, false);
        }
        
        TemplateWizard.Iterator it = TemplateWizard.getIterator (obj);
        
        assertEquals ("Iterator obtained from the object's cookie", obj, it);
    }
    
    public void testIteratorBridge() throws Exception {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject fo = fs.getRoot().createData("x");
        final FileObject a = fs.getRoot().createData("a");
        final FileObject b = fs.getRoot().createData("b");
        final FileObject c = fs.getRoot().createData("c");
        final FileObject d = fs.getRoot().createData("d");
        fo.setAttribute("instantiatingIterator", new WizardDescriptor.InstantiatingIterator() {
            public Set instantiate() throws IOException {
                return new LinkedHashSet(Arrays.asList(new FileObject[] {
                    d,
                    c,
                    a,
                    b,
                }));
            }
            public void removeChangeListener(ChangeListener l) {}
            public void addChangeListener(ChangeListener l) {}
            public void uninitialize(WizardDescriptor wizard) {}
            public void initialize(WizardDescriptor wizard) {}
            public void previousPanel() {}
            public void nextPanel() {}
            public String name() {return null;}
            public boolean hasPrevious() {return false;}
            public boolean hasNext() {return false;}
            public WizardDescriptor.Panel current() {return null;}
        });
        System.out.println("natural order:" + new HashSet(Arrays.asList(new DataObject[] {
            DataObject.find(d),
            DataObject.find(c),
            DataObject.find(a),
            DataObject.find(b),
        })));
        assertEquals("order preserved (#64760)", Arrays.asList(new DataObject[] {
            DataObject.find(d),
            DataObject.find(c),
            DataObject.find(a),
            DataObject.find(b),
        }), new ArrayList(TemplateWizard.getIterator(DataObject.find(fo)).instantiate(new TemplateWizard())));
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
        public WizardDescriptor.Panel<WizardDescriptor> current() {
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
        public Set instantiate(TemplateWizard wiz) throws IOException {
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
        class I extends WizardDescriptor.ArrayIterator<WizardDescriptor> implements TemplateWizard.Iterator {
            public I () {
                super (arr);
            }
            public Set<DataObject> instantiate (TemplateWizard wiz) throws IOException {
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
        
        public void removeChangeListener (ChangeListener l) {
        }

        public void addChangeListener (ChangeListener l) {
        }

        public void storeSettings (Object settings) {
        }

        public void readSettings (Object settings) {
        }

        public boolean isValid () {
            return true;
        }

        public HelpCtx getHelp () {
            return null;
        }

        public Component getComponent () {
            return new JPanel ();
        }
        
        public String toString () {
            return Integer.toString (index);
        }
        
    }

}


