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
