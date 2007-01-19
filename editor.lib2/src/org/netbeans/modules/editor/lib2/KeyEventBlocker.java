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

package org.netbeans.modules.editor.lib2;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

/**
*
* @author Dusan Balek
*/
public class KeyEventBlocker implements KeyListener {

    private LinkedList<KeyEvent> blockedEvents = new LinkedList<KeyEvent>();
    private JTextComponent component;
    private boolean discardKeyTyped = true;
    private static final boolean debugBlockEvent
    = Boolean.getBoolean("netbeans.debug.editor.blocker"); // NOI18N
    

    public KeyEventBlocker(JTextComponent component, boolean discardFirstKeyTypedEvent) {
        this.component = component;
        this.discardKeyTyped = discardFirstKeyTypedEvent;
        if (debugBlockEvent){
            System.out.println(""); //NOI18N
            System.out.println("attaching listener"+this.component.getClass()+" - "+this.component.hashCode()); //NOI18N
        }
        this.component.addKeyListener(this);
    }

    /** Has to be called from AWT event thread to be properly synchronized */
    public void stopBlocking(boolean dispatchBlockedEvents) {
        if (debugBlockEvent){
            System.out.println("removing listener from "+this.component.getClass()+" - "+this.component.hashCode()); //NOI18N
        }
        this.component.removeKeyListener(this);
        if (dispatchBlockedEvents){
            KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            while(!blockedEvents.isEmpty()) {
                KeyEvent e = blockedEvents.removeFirst();
                e = new KeyEvent((Component)e.getSource(), e.getID(), e.getWhen(), e.getModifiers(), e.getKeyCode(), e.getKeyChar(), e.getKeyLocation());
                kfm.dispatchEvent(e);
            }
        }
    }
    
    public void stopBlocking() {
        stopBlocking(true);
    }

    public void keyPressed(KeyEvent e) {
        if (debugBlockEvent){
            System.out.println("consuming keyPressed event:"+KeyEvent.getKeyModifiersText(e.getModifiers())+" + "+KeyEvent.getKeyText(e.getKeyCode())); //NOI18N
        }
        e.consume();
        blockedEvents.add(e);
    }

    public void keyReleased(KeyEvent e) {
        if (debugBlockEvent){
            System.out.println("consuming keyReleased event:"+KeyEvent.getKeyModifiersText(e.getModifiers())+" + "+KeyEvent.getKeyText(e.getKeyCode())); //NOI18N
        }
        e.consume();
        blockedEvents.add(e);
    }

    public void keyTyped(KeyEvent e) {
        if (debugBlockEvent){
            System.out.println("consuming keyTyped event:"+KeyEvent.getKeyModifiersText(e.getModifiers())+" + "+KeyEvent.getKeyText(e.getKeyCode())); //NOI18N
        }
        e.consume();
        if (discardKeyTyped) {
            discardKeyTyped = false;
        } else {
            blockedEvents.add(e);
        }
    }
}
