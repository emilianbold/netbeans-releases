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

import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import junit.framework.TestCase;

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

    public void testAddRemoveClipboardListener () {
        
        class L implements ClipboardListener {
            public int cnt;
            public ClipboardEvent ev;
            public void clipboardChanged (ClipboardEvent ev) {
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
