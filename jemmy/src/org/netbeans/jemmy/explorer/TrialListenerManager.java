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
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
 *
 * Contributor(s): Alexandre Iline.
 *
 * $Id$ $Revision$ $Date$
 *
 */

package org.netbeans.jemmy.explorer;

import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Outputable;
import org.netbeans.jemmy.TestOut;

import java.awt.AWTEvent;
import java.awt.Component;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import org.netbeans.jemmy.QueueTool;

/**
 * Auxiliary class to find an event sequence which should be posted
 * to reproduce user actions.
 *	
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *
 */

public class TrialListenerManager implements Outputable {

    Component comp;
    TrialMouseListener mListener;
    TrialMouseMotionListener mmListener;
    TrialKeyListener kListener;
    TestOut output;

    /**
     * Contructor.
     * @param comp Component to display event sequence for.
     */
    public TrialListenerManager(Component comp) {
	this.comp = comp;
	mListener = new TrialMouseListener();
	mmListener = new TrialMouseMotionListener();
	kListener = new TrialKeyListener();
	output = JemmyProperties.getCurrentOutput();
    }

    public void setOutput(TestOut output) {
	this.output = output;
    }

    public TestOut getOutput() {
	return(output);
    }

    /**
     * Removes mouse listener.
     * @see #addMouseListener
     */
    public void removeMouseListener() {
	comp.removeMouseListener(mListener);
    }
    
    /**
     * Adds mouse listener.
     * @see #removeMouseListener
     */
    public void addMouseListener() {
	removeMouseListener();
	comp.addMouseListener(mListener);
    }

    /**
     * Removes mouse motion listener.
     * @see #addMouseMotionListener
     */
    public void removeMouseMotionListener() {
	comp.removeMouseMotionListener(mmListener);
    }
    
    /**
     * Adds mouse motion listener.
     * @see #removeMouseMotionListener
     */
    public void addMouseMotionListener() {
	removeMouseMotionListener();
	comp.addMouseMotionListener(mmListener);
    }

    /**
     * Removes key listener.
     * @see #addKeyListener
     */
    public void removeKeyListener() {
	comp.removeKeyListener(kListener);
    }
    
    /**
     * Adds key listener.
     * @see #removeKeyListener
     */
    public void addKeyListener() {
	removeKeyListener();
	comp.addKeyListener(kListener);
    }

    void printEvent(final AWTEvent event) {
        // if event != null run toString in dispatch thread
        String eventToString = (String)new QueueTool().invokeSmoothly(
            new QueueTool.QueueAction("event.toString()") {
                public Object launch() {
                    return event.toString();
                }
            }
        );
	output.printLine(eventToString);
    }

    private class TrialMouseListener implements MouseListener {
	public void mouseClicked(MouseEvent e) {
	    printEvent(e);
	}
	public void mouseEntered(MouseEvent e) {
	    printEvent(e);
	}
	public void mouseExited(MouseEvent e) {
	    printEvent(e);
	}
	public void mousePressed(MouseEvent e) {
	    printEvent(e);
	}
	public void mouseReleased(MouseEvent e) {
	    printEvent(e);
	}
    }

    private class TrialMouseMotionListener implements MouseMotionListener {
	public void mouseDragged(MouseEvent e) {
	    printEvent(e);
	}
	public void mouseMoved(MouseEvent e) {
	    printEvent(e);
	}
    }

    private class TrialKeyListener implements KeyListener {
	public void keyPressed(KeyEvent e) {
	    printEvent(e);
	}
	public void keyReleased(KeyEvent e) {
	    printEvent(e);
	}
	public void keyTyped(KeyEvent e) {
	    printEvent(e);
	}
    }
}
