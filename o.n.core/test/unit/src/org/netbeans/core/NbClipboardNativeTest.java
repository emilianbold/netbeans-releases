/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core;

import java.awt.datatransfer.*;
import java.io.IOException;
import java.util.*;
import org.netbeans.junit.*;
import junit.textui.TestRunner;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.*;

/** Test NbClipboard, in "native" mode (e.g. Windows).
 * @author Jesse Glick
 * @see "#30923"
 */
public class NbClipboardNativeTest extends NbTestCase implements org.openide.util.datatransfer.ClipboardListener {
    private NbClipboard ec;
    private int listenerCalls;
    
    public NbClipboardNativeTest(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(NbClipboardNativeTest.class));
    }
    
    protected void setUp() throws Exception {
        // Use MemoryFileSystem:
        Properties p = System.getProperties();
        p.remove("system.dir");
        System.setProperties(p);
        super.setUp();
        //System.setProperty("org.netbeans.core.NbClipboard", "-5");
        System.setProperty("netbeans.slow.system.clipboard.hack", String.valueOf(slowClipboardHack()));
        Object ec = Lookup.getDefault().lookup(ExClipboard.class);
        assertEquals("found right ExClipboard", NbClipboard.class, ec.getClass());
        this.ec = (NbClipboard)ec;
        this.ec.addClipboardListener(this);
    }
    
    protected void tearDown () throws Exception {
        super.tearDown ();
        if (ec != null) {
            this.ec.removeClipboardListener(this);
        }
    }
    
    protected boolean slowClipboardHack() {
        return false;
    }
    
    public void testWhenCallingGetContentsItChecksSystemClipboardFirstTimeAfterActivation () throws Exception {
        assertEquals ("No changes yet", 0, listenerCalls);
        
        
        Clipboard sc = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection ss = new StringSelection ("oldvalue");
        sc.setContents(ss, ss);
        
        assertEquals ("No changes still", 0, listenerCalls);
        
        // just simulate initial switch to NetBeans main window
        ec.eventDispatched(new java.awt.event.WindowEvent (
            new javax.swing.JFrame (),
            java.awt.event.WindowEvent.WINDOW_ACTIVATED
        ));
        ec.waitFinished ();
        
        assertEquals ("This generated a change", 1, listenerCalls);
        
        StringSelection s2 = new StringSelection ("data2");
        sc.setContents (s2, s2);
        
        assertEquals ("No change notified", 1, listenerCalls);

        // we need to wait longer time than the value in NbClipboard
        Thread.sleep (200);
        
        Transferable t = this.ec.getContents(this);
        assertTrue ("String flavor is there", t.isDataFlavorSupported(DataFlavor.stringFlavor));
        
        String s = (String)t.getTransferData(DataFlavor.stringFlavor);
        assertEquals ("The getContents rechecked the system clipboard first time after window activated", "data2", s);
        
        sc.setContents (ss, ss);
        
        t = this.ec.getContents(this);
        s = (String)t.getTransferData(DataFlavor.stringFlavor);
        if (slowClipboardHack ()) {
            assertEquals ("The getContents rechecked the clipboard just for the first time, not now, so the content is the same", "data2", s);
            
            ec.eventDispatched(new java.awt.event.WindowEvent (
                new javax.swing.JFrame (),
                java.awt.event.WindowEvent.WINDOW_ACTIVATED
            ));

            t = this.ec.getContents(this);
            s = (String)t.getTransferData(DataFlavor.stringFlavor);
            assertEquals ("The WINDOW_ACTIVATED rechecks the clipboard", "oldvalue", s);
        } else {
            assertEquals ("without slow hack it gets the value immediatelly", "oldvalue", s);
        }
        
        
    }
    
    public void testClipboard() throws Exception {
        FileSystem sfs = Repository.getDefault().getDefaultFileSystem();
        FileObject f = sfs.findResource("Services");
        if (f == null) f = sfs.getRoot().createFolder("Services");
        Lookup.getDefault().lookup(ModuleInfo.class);
        // XXX crude but the easiest thing to do here:
        NbTopManager.Lkp.modulesClassPathInitialized();
        DataFolder d = DataFolder.findFolder(f);
        InstanceDataObject.create(d, null, Cnv.class);
        /*
        List cnvs = new ArrayList(Lookup.getDefault().lookup(new Lookup.Template(Cnv.class)).allInstances());
        assertEquals("one Cnv registered in " + Lookup.getDefault(), 1, cnvs.size());
        cnvs = new ArrayList(Lookup.getDefault().lookup(new Lookup.Template(ExClipboard.Convertor.class)).allInstances());
        assertEquals("one convertor registered", 1, cnvs.size());
        assertEquals("right convertor type", Cnv.class, cnvs.get(0).getClass());
         */
        Clipboard c = (Clipboard)Lookup.getDefault().lookup(Clipboard.class);
        /*
        assertEquals("found right Clipboard", NbClipboard.class, c.getClass());
         */
        ExClipboard ec = (ExClipboard)Lookup.getDefault().lookup(ExClipboard.class);
        /*
        assertEquals("found right ExClipboard", NbClipboard.class, c.getClass());
         */
        assertEquals("Clipboard == ExClipboard", c, ec);
        c.setContents(new ExTransferable.Single(DataFlavor.stringFlavor) {
            protected Object getData() throws IOException, UnsupportedFlavorException {
                return "17";
            }
        }, null);
        Transferable t = c.getContents(null);
        assertTrue("still supports stringFlavor", t.isDataFlavorSupported(DataFlavor.stringFlavor));
        assertEquals("correct string in clipboard", "17", t.getTransferData(DataFlavor.stringFlavor));
        assertTrue("support Integer too", t.isDataFlavorSupported(MYFLAV));
        assertEquals("correct Integer", new Integer(17), t.getTransferData(MYFLAV));
    }
    
    private static final DataFlavor MYFLAV = new DataFlavor("text/x-integer", "Integer"); // data: java.lang.Integer
    public static final class Cnv implements ExClipboard.Convertor {
        public Transferable convert(Transferable t) {
            System.err.println("converting...");//XXX
            if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                final ExTransferable t2 = ExTransferable.create(t);
                if (t2.isDataFlavorSupported(DataFlavor.stringFlavor) && !t2.isDataFlavorSupported(MYFLAV)) {
                    t2.put(new ExTransferable.Single(MYFLAV) {
                        protected Object getData() throws IOException, UnsupportedFlavorException {
                            String s = (String)t2.getTransferData(DataFlavor.stringFlavor);
                            try {
                                return new Integer(s);
                            } catch (NumberFormatException nfe) {
                                throw new IOException(nfe.toString());
                            }
                        }
                    });
                }
                return t2;
            } else {
                return t;
            }
        }
    }


    // #25537
    public void testOwnershipLostEvent() throws Exception {
        final int[] holder = new int[] { 0 };
        ExTransferable transferable = ExTransferable.create (new StringSelection("A"));

        // listen on ownershipLost
        transferable.addTransferListener (new TransferListener () {
            public void accepted (int action) {}
            public void rejected () {}
            public void ownershipLost () { holder[0]++; }
        });

        Clipboard c = (Clipboard)Lookup.getDefault().lookup(Clipboard.class);

        c.setContents(transferable, null);

        assertTrue("Still has ownership", holder[0] == 0);

        c.setContents(new StringSelection("B"), null);

        assertTrue("Exactly one ownershipLost event have happened.", holder[0] == 1);
    }

    public void clipboardChanged(ClipboardEvent ev) {
        listenerCalls++;
    }
}
