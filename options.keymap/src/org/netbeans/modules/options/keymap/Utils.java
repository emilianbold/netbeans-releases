/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.options.keymap;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
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
        if (ks == null) { // Return null to indicate an invalid keystroke
            return null;
        } else {
            KeyStroke result = KeyStroke.getKeyStroke (ks.getKeyCode (), modifiers);
            return result;
        }
    }
    
    static String getKeyStrokeAsText (KeyStroke keyStroke) {
        int modifiers = keyStroke.getModifiers ();
        StringBuilder sb = new StringBuilder ();
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
