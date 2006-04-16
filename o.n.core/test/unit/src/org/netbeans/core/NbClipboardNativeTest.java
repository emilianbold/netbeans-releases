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

package org.netbeans.core;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.AssertionFailedErrorException;
import org.netbeans.junit.AssertionFileFailedError;
import org.netbeans.junit.NbTestCase;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.InstanceDataObject;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ClipboardEvent;
import org.openide.util.datatransfer.ClipboardListener;
import org.openide.util.datatransfer.ExClipboard;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.TransferListener;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;

/** Test NbClipboard, in "native" mode (e.g. Windows).
 * @author Jesse Glick
 * @see "#30923"
 */
public class NbClipboardNativeTest extends NbTestCase implements ClipboardListener {
    private NbClipboard ec;
    private int listenerCalls;
    
    public NbClipboardNativeTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        ErrManager.log = getLog();
        
        System.setProperty("org.openide.util.Lookup", Lkp.class.getName());
        assertEquals("Lookup registered", Lkp.class, Lookup.getDefault().getClass());
        
        class EmptyTrans  implements Transferable, ClipboardOwner {
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[0];
            }

            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return false;
            }

            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                throw new IOException("Nothing here");
            }

            public void lostOwnership(Clipboard clipboard, Transferable contents) {
            }
        }

        
        super.setUp();
        //System.setProperty("org.netbeans.core.NbClipboard", "-5");
        System.setProperty("netbeans.slow.system.clipboard.hack", String.valueOf(slowClipboardHack()));
        this.ec = new NbClipboard();

        EmptyTrans et = new EmptyTrans();
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(et, et);

        this.ec.addClipboardListener(this);
    }
    
    protected void tearDown () throws Exception {
        super.tearDown ();
        if (ec != null) {
            this.ec.removeClipboardListener(this);
        }
        
        waitFinished(ec);
    }
    
    protected boolean slowClipboardHack() {
        return false;
    }
    
    public void testGetClipboardWorks() throws Exception {
        class Get implements ClipboardListener {
            Transferable in;
            
            public void clipboardChanged(ClipboardEvent ev) {
                in = ec.getContents(this);
            }
        }
        
        Get get = new Get();
        ec.addClipboardListener(get);
        
        StringSelection ss = new StringSelection("x");
        ec.setContents(ss, ss);

        assertEquals("Inside is the right one", ss.getTransferData(DataFlavor.stringFlavor), get.in.getTransferData(DataFlavor.stringFlavor));
    }
    
    public void testWhenCallingGetContentsItChecksSystemClipboardFirstTimeAfterActivation () throws Exception {
        assertEquals ("No changes yet", 0, listenerCalls);
        
        Clipboard sc = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection ss = new StringSelection ("oldvalue");
        sc.setContents(ss, ss);
        
        
        // just simulate initial switch to NetBeans main window
        ec.activateWindowHack (true);
        waitFinished (ec);
        
        if (listenerCalls == 0) {
            fail("We need at least one call: " + listenerCalls);
        }
        listenerCalls = 0;
        
        StringSelection s2 = new StringSelection ("data2");
        sc.setContents (s2, s2);
        
        waitFinished (ec);

        if (slowClipboardHack()) {
            assertEquals ("No change notified", 0, listenerCalls);
        }
        
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

            ec.activateWindowHack (true);
            Thread.sleep (200);

            t = this.ec.getContents(this);
            s = (String)t.getTransferData(DataFlavor.stringFlavor);
            assertEquals ("The WINDOW_ACTIVATED rechecks the clipboard", "oldvalue", s);
        } else {
            assertEquals ("without slow hack it gets the value immediatelly", "oldvalue", s);
        }
        
        
    }
    
    public void testClipboard() throws Exception {
        Lkp lkp = (Lkp)Lookup.getDefault();
        Object ins = new Cnv();
        lkp.ic.add(ins);

        try {
            Clipboard c = (Clipboard)Lookup.getDefault().lookup(Clipboard.class);
            ExClipboard ec = (ExClipboard)Lookup.getDefault().lookup(ExClipboard.class);
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
        } finally {
            lkp.ic.remove(ins);
        }
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
    
    private void waitFinished(NbClipboard ec) {
        try {
            ec.waitFinished();
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                }
            });
            ec.waitFinished();
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                }
            });
        } catch (InterruptedException ex) {
            throw new AssertionFailedErrorException(ex);
        } catch (InvocationTargetException ex) {
            throw new AssertionFailedErrorException(ex);
        }
    }
    
    //
    // Fake Lookup
    //
    
    public static final class Lkp extends ProxyLookup {
        public InstanceContent ic;
        
        public Lkp() {
            super(new Lookup[0]);
            ic = new InstanceContent();
            AbstractLookup al = new AbstractLookup(ic);
            ic.add(new ErrManager());
            Lookup ml = org.openide.util.lookup.Lookups.metaInfServices(getClass().getClassLoader());
            
            setLookups(new Lookup[] { al, ml });
        }
        
    }
    //
    // Logging support
    //
    public static final class ErrManager extends ErrorManager {
        public static final StringBuffer messages = new StringBuffer ();
        
        private String prefix;

        private static PrintStream log;
        
        public ErrManager () {
            this (null);
        }
        public ErrManager (String prefix) {
            this.prefix = prefix;
        }
        
        public Throwable annotate (Throwable t, int severity, String message, String localizedMessage, Throwable stackTrace, Date date) {
            return t;
        }
        
        public Throwable attachAnnotations (Throwable t, ErrorManager.Annotation[] arr) {
            return t;
        }
        
        public ErrorManager.Annotation[] findAnnotations (Throwable t) {
            return null;
        }
        
        public ErrorManager getInstance (String name) {
            if (
                true
//                name.startsWith ("org.openide.loaders.FolderList")
//              || name.startsWith ("org.openide.loaders.FolderInstance")
            ) {
                return new ErrManager ('[' + name + ']');
            } else {
                // either new non-logging or myself if I am non-logging
                return new ErrManager ();
            }
        }
        
        public void log (int severity, String s) {
            if (prefix != null) {
                messages.append (prefix);
                messages.append (s);
                messages.append ('\n');
                
                if (messages.length() > 30000) {
                    messages.delete(0, 15000);
                }
                
                log.print(prefix);
                log.println(s);
            }
        }
        
        public void notify (int severity, Throwable t) {
            log (severity, t.getMessage ());
        }
        
        public boolean isNotifiable (int severity) {
            return prefix != null;
        }
        
        public boolean isLoggable (int severity) {
            return prefix != null;
        }
        
    } // end of ErrManager
    
}
