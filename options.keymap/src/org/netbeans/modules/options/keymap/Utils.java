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

package org.netbeans.modules.options.keymap;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import org.openide.ErrorManager;
import org.openide.util.Utilities;


/**
 *
 * @author Jan Jancura
 */
class Utils {
    
    
    static String getKeyStrokesAsText (KeyStroke[] keyStrokes, String delim) {
        if (keyStrokes == null) return "";
        if (keyStrokes.length == 0) return "";
        StringBuffer sb = new StringBuffer (getKeyStrokeAsText (keyStrokes [0]));
        int i, k = keyStrokes.length;
        for (i = 1; i < k; i++)
            sb.append (delim).append (getKeyStrokeAsText (keyStrokes [i]));
        return new String (sb);
    }

    static KeyStroke getKeyStroke (String keyStroke) {
        int modifiers = 0;
        if (keyStroke.startsWith ("Ctrl+")) {
            modifiers |= InputEvent.CTRL_DOWN_MASK;
            keyStroke = keyStroke.substring (5);
        }
        if (keyStroke.startsWith ("Alt+")) {
            modifiers |= InputEvent.ALT_DOWN_MASK;
            keyStroke = keyStroke.substring (4);
        }
        if (keyStroke.startsWith ("Shift+")) {
            modifiers |= InputEvent.SHIFT_DOWN_MASK;
            keyStroke = keyStroke.substring (6);
        }
        if (keyStroke.startsWith ("Meta+")) {
            modifiers |= InputEvent.META_DOWN_MASK;
            keyStroke = keyStroke.substring (5);
        }
        KeyStroke ks = Utilities.stringToKey (keyStroke);
        if (ks == null) 
            ErrorManager.getDefault ().notify (
                new IllegalArgumentException (keyStroke)
            );
        KeyStroke result = KeyStroke.getKeyStroke (ks.getKeyCode (), modifiers);
        return result;
    }
    
    static String getKeyStrokeAsText (KeyStroke keyStroke) {
        int modifiers = keyStroke.getModifiers ();
        StringBuffer sb = new StringBuffer ();
        if ((modifiers & InputEvent.CTRL_DOWN_MASK) > 0)
            sb.append ("Ctrl+");
        if ((modifiers & InputEvent.ALT_DOWN_MASK) > 0)
            sb.append ("Alt+");
        if ((modifiers & InputEvent.SHIFT_DOWN_MASK) > 0)
            sb.append ("Shift+");
        if ((modifiers & InputEvent.META_DOWN_MASK) > 0)
            sb.append ("Meta+");
        if (keyStroke.getKeyCode () != KeyEvent.VK_SHIFT &&
            keyStroke.getKeyCode () != KeyEvent.VK_CONTROL &&
            keyStroke.getKeyCode () != KeyEvent.VK_META &&
            keyStroke.getKeyCode () != KeyEvent.VK_ALT &&
            keyStroke.getKeyCode () != KeyEvent.VK_ALT_GRAPH
        )
            sb.append (Utilities.keyToString (
                KeyStroke.getKeyStroke (keyStroke.getKeyCode (), 0)
            ));
        return sb.toString ();
    }
}
