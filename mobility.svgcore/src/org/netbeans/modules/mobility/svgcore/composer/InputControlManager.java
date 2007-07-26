/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt. 
  * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 */

package org.netbeans.modules.mobility.svgcore.composer;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 *
 * @author Pavel Benes
 */
public class InputControlManager {
    private final SceneManager       m_sceneMgr;
    private final MouseController    m_mouseCtrl;
    private final KeyboardController m_keyCtrl;

    class MouseController implements MouseListener, MouseMotionListener {
        public void mouseClicked(MouseEvent e) {
            m_sceneMgr.processEvent(e);
        }

        public void mouseDragged(MouseEvent e) {
            m_sceneMgr.processEvent(e);
        }

        public void mouseEntered(MouseEvent e) {
            m_sceneMgr.processEvent(e);
        }

        public void mouseExited(MouseEvent e) {
            m_sceneMgr.processEvent(e);
        }

        public void mouseMoved(MouseEvent e) {
            m_sceneMgr.processEvent(e);
        }

        public void mousePressed(MouseEvent e) {
            m_sceneMgr.processEvent(e);
        }

        public void mouseReleased(MouseEvent e) {
            m_sceneMgr.processEvent(e);
        }           
    }

    private class KeyboardController implements KeyListener {
        public void keyTyped(KeyEvent e) {
            //System.out.println("keyTyped");
            m_sceneMgr.processEvent(e);
        }
        public void keyPressed(KeyEvent e) {
            //System.out.println("keyPressed");
            m_sceneMgr.processEvent(e);
        }
        public void keyReleased(KeyEvent e) {
            //System.out.println("keyReleased");
            m_sceneMgr.processEvent(e);
        }
    } 
    
    public InputControlManager(SceneManager sceneMgr) {
        m_sceneMgr  = sceneMgr;
        m_mouseCtrl = new MouseController();
        m_keyCtrl   = new KeyboardController();
    }

    void initialize() {        
        m_sceneMgr.getScreenManager().registerMouseController(m_mouseCtrl);
        m_sceneMgr.getScreenManager().registerKeyController(m_keyCtrl);
    }
}
