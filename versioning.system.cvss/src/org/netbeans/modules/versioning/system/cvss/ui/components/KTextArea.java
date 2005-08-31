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

package org.netbeans.modules.versioning.system.cvss.ui.components;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/**
 * JTextArea with patched focus cycling policy that
 * binds to usual tab/shift-tab.
 *
 * @author Petr Kuzel
 */
public class KTextArea extends JTextArea {
    
    private final static KeyStroke tab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
    private final static KeyStroke shiftTab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK);
    
    /** Creates a new instance of KTextArea */
    public KTextArea() {        
        Set forward = new HashSet();
        forward.add(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0));
        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forward);

        Set backward = new HashSet();
        backward.add(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK));
        setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, backward);        
    }
    
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
        if (tab.equals(ks) || shiftTab.equals(ks)) return false;
        return super.processKeyBinding(ks, e, condition, pressed);
    }
    
}
