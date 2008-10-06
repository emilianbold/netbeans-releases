/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.uml.drawingarea.keymap;

import java.awt.event.KeyEvent;
import java.util.ListResourceBundle;
import java.util.MissingResourceException;
import javax.swing.JComponent;

/**
 *
 * @author thuy
 */
public class InputKeyResources extends ListResourceBundle 
{
    private static final String WHEN_FOCUSED = String.valueOf(JComponent.WHEN_FOCUSED);
    private static final String WHEN_IN_FOCUSED_WINDOW = 
            String.valueOf(JComponent.WHEN_IN_FOCUSED_WINDOW);
    private static final String WHEN_ANCESTOR_OF_FOCUSED_COMPONENT = 
            String.valueOf(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    
    private static final String NO_MODIFIER = String.valueOf(0);
    @Override
    protected Object[][] getContents()
    {
        return (contents);
    }

    // Resource table
    static final Object[][] contents =
    {
//        ----------------------------------------------------------------------
//        Key binding
//
//        This section defines the key bindings. The key binding
//        starts with the keyword "key". The number that follows
//        is the index number which must be increased when you add
//        new key bindings.
//        Each key event has a key code, modifier value (user 0 for no modifier),
//        command name and a focus value.
//        -----------------------------------------------------------------------------
        {"key.1.keyCode", String.valueOf(KeyEvent.VK_ESCAPE)}, 
        {"key.1.modifiers", NO_MODIFIER},
        {"key.1.command", DiagramInputkeyMapper.CANCEL_ACTION }, 
        {"key.1.focus", WHEN_IN_FOCUSED_WINDOW},
        
        {"key.2.keyCode", String.valueOf(KeyEvent.VK_P)}, 
        {"key.2.modifiers", String.valueOf(KeyEvent.CTRL_DOWN_MASK)},
        {"key.2.mac_modifiers", String.valueOf(KeyEvent.META_DOWN_MASK)},
        {"key.2.command", DiagramInputkeyMapper.CONTEXT_PALETTE_FOCUS }, 
        {"key.2.focus", WHEN_IN_FOCUSED_WINDOW},
        
        {"key.3.keyCode", String.valueOf(KeyEvent.VK_ENTER)}, 
        {"key.3.modifiers", String.valueOf(KeyEvent.CTRL_DOWN_MASK)},
        {"key.3.mac_modifiers", String.valueOf(KeyEvent.META_DOWN_MASK)},
        {"key.3.command", DiagramInputkeyMapper.ADD_TO_DIAGRAM }, 
        {"key.3.focus", WHEN_IN_FOCUSED_WINDOW}
        
//     more examples:
//        {"key.4.keyCode", String.valueOf(KeyEvent.VK_UP)},
//        {"key.4.modifiers", String.valueOf(KeyEvent.CTRL_DOWN_MASK)},
//        {"key.4.command", DiagramInputkeyMapper.MOVE_UP }, 
//        {"key.4.focus", WHEN_ANCESTOR_OF_FOCUSED_COMPONENT}, 
//
//        {"key.5.keyCode", String.valueOf(KeyEvent.VK_DOWN)}, 
//        {"key.5.modifiers", String.valueOf(KeyEvent.CTRL_DOWN_MASK)},
//        {"key.5.command", DiagramInputkeyMapper.MOVE_DOWN }, 
//        {"key.5.focus", WHEN_ANCESTOR_OF_FOCUSED_COMPONENT}
        
    };  // end of content
    
    /**
     * This method redefines the standard behavior of the <code>
     * getString</code> method of the ListResourceBundle class.
     * Instead of throwing an exception, it returns null if the given
     * resource is not found.
     */
    public String getStringResource(String key)
    {
        String value = null;
        try
        {
            value = this.getString(key);
        }
        catch (MissingResourceException resourceError)
        {
        }
        
        return value;
    }
}
