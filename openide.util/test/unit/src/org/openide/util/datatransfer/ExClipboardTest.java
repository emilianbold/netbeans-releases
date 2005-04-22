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

package org.openide.util.datatransfer;

import junit.framework.*;
import java.awt.datatransfer.*;
import javax.swing.event.EventListenerList;

/** 
 *
 * @author Jaroslav Tulach
 */
public class ExClipboardTest extends TestCase {
    private ExClipboard clipboard;
    
    private ExClipboard.Convertor[] convertors = new ExClipboard.Convertor[0];
    
    public ExClipboardTest (String testName) {
        super (testName);
    }

    protected void setUp () throws Exception {
        clipboard = new ExClipboard ("test clipboard") {
            protected ExClipboard.Convertor[] getConvertors () {
                return convertors;
            }
        };
    }

    protected void tearDown () throws Exception {
    }

    public static Test suite () {
        TestSuite suite = new TestSuite(ExClipboardTest.class);
        
        return suite;
    }

    public void testAddRemoveClipboardListener () {
        
        class L implements org.openide.util.datatransfer.ClipboardListener {
            public int cnt;
            public org.openide.util.datatransfer.ClipboardEvent ev;
            public void clipboardChanged (org.openide.util.datatransfer.ClipboardEvent ev) {
                cnt++;
                this.ev = ev;
            }
        }
        L listener = new L ();
        
        clipboard.addClipboardListener (listener);
        clipboard.fireClipboardChange ();
        assertEquals ("One event", 1, listener.cnt);
        assertNotNull ("An event", listener.ev);
        assertEquals ("source is right", clipboard, listener.ev.getSource ());
        
        clipboard.removeClipboardListener (listener);
        clipboard.fireClipboardChange ();
        
        assertEquals ("no new change", 1, listener.cnt);
    }

    public void testConvert () {
        class WillNotGetNull implements ExClipboard.Convertor {
            public Transferable convert (Transferable t) {
                assertNotNull ("Never get null", t);
                return null;
            }
        }
        
        convertors = new ExClipboard.Convertor[] {
            new WillNotGetNull (),
            new WillNotGetNull (),
            new WillNotGetNull (),
        };
        
        Transferable ret = clipboard.convert (new StringSelection ("Ahoj"));
        assertNull ("Correctly returned null", ret);
        assertNull ("Handle also null parameter", clipboard.convert (null));
    }

}
