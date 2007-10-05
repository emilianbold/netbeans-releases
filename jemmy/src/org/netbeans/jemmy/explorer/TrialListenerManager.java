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
 * Contributor(s): Alexandre Iline.
 *
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
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
 *
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
