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

package org.netbeans.jemmy.demo;

import org.netbeans.jemmy.EventDispatcher;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Timeouts;

import org.netbeans.jemmy.operators.JButtonOperator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * Default org.netbeans.jemmy.demo.CommentWindow implementation.
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 * 
 */

public class DefaultCommentWindow extends JDialog implements CommentWindow {

    JTextArea comments;
    JButton finishButton;
    JButton nextStepButton;
    JButton contButton;

    boolean stopped = true;
    boolean continual = false;
    boolean finished = false;
    boolean interrupted = false;

    long readCommentTimeout = 1000;

    /**
     * Constructs a DefaultCommentWindow object.
     * @param modal Display as modal dialog.
     */
    public DefaultCommentWindow(boolean modal) {

	super();

	nextStepButton = new JButton("One step");
	nextStepButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		hideWindow();
		nextStepButton.setEnabled(false);
		contButton.setEnabled(false);
		finishButton.setEnabled(false);
		setStopped(false);
	    }
	});

	contButton = new JButton("All steps");
	contButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		hideWindow();
		nextStepButton.setEnabled(false);
		contButton.setEnabled(false);
		finishButton.setEnabled(false);
		continual = true;
		setStopped(false);
	    }
	});

	finishButton = new JButton("Interrupt");
	finishButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		hideWindow();
		if(!finished) {
		    setInterrupted(true);
		}
		continual = false;
		setStopped(false);
	    }
	});

	JPanel prepreNavPane = new JPanel();
	prepreNavPane.setLayout(new BorderLayout());
	prepreNavPane.add(finishButton, BorderLayout.NORTH);

	JPanel preNavPane = new JPanel();
	preNavPane.setLayout(new BorderLayout());
	preNavPane.add(contButton, BorderLayout.NORTH);
	preNavPane.add(prepreNavPane, BorderLayout.CENTER);

	JPanel navPane = new JPanel();
	navPane.setLayout(new BorderLayout());
	navPane.add(nextStepButton, BorderLayout.NORTH);
	navPane.add(preNavPane, BorderLayout.CENTER);

	comments = new JTextArea("");
	comments.setEditable(false);
	comments.setLineWrap(true);

	getContentPane().setLayout(new BorderLayout());
	getContentPane().add(navPane, BorderLayout.WEST);
	getContentPane().add(new JScrollPane(comments), BorderLayout.CENTER);

	Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
	setSize((int)screen.getWidth(), 200);
	setLocation(0, (int)screen.getHeight() - 200);

	setModal(modal);
    }

    /**
     * Constructs a DefaultCommentWindow object.
     */
    public DefaultCommentWindow() {
	this(false);
    }

    /**
     * Specifies the time to display comment.
     * @param timeout lond value.
     */
    public void setCommentTimeout(long timeout) {
	readCommentTimeout = timeout;
    }

    public boolean isStopped() {
	return(stopped);
    }

    public void nextStep(String stepComment) {
	comments.setText(stepComment);
	nextStepButton.setEnabled(true);
	contButton.setEnabled(true);
	finishButton.setEnabled(true);
	setStopped(true);
	if(!continual) {
	    new Mover(nextStepButton).enter();
	} else {
	    new Mover(nextStepButton).push();
	}
	showWindow();
    }

    public void showFinalComment(String stepComment) {
	setStopped(true);
	finished = true;
	continual = false;
	finishButton.setEnabled(true);
	finishButton.setText("Finish");
	comments.setText(stepComment);
	new Mover(finishButton).enter();
	showWindow();
    }

    public boolean isInterrupted() {
	return(interrupted);
    }

    public String getInterruptMessage() {
	return("Step comments: \"" + comments.getText() + "\"");
    }

    public void close() {
	setVisible(false);
    }

    /**
     * Perform a mouse action at the end of test step.
     * If demo is executed in continual mode 
     * (i.e. "All Steps" button has been pushed), 
     * performs "Next" button pushing.
     * Otherwise simply moves mouse to the "Next" button.
     */
    public class Mover extends Thread {
	JButtonOperator bo;
	boolean toPush = false;
        /**
         * Creates a Mover object.
         * @param button a Button
         */
	public Mover(JButton button) {
	    super();
	    bo = new JButtonOperator(button);
	    Timeouts times = JemmyProperties.getCurrentTimeouts();
	    times.setTimeout("AbstractButton.PushButtonTimeout",
			     readCommentTimeout);
	    bo.setTimeouts(times);
	}
        /**
         * Pushes the button.
         */
	public void push() {
	    toPush = true;
	    start();
	}
        /**
         * Moves mouse pointer to the button.
         */
	public void enter() {
	    toPush = false;
	    start();
	}
	public void run() {
	    try {
		while(!bo.getSource().isShowing()) {
		    Thread.currentThread().sleep(100);
		}
		EventDispatcher.waitQueueEmpty(TestOut.getNullOutput(),
					       JemmyProperties.getCurrentTimeouts());
		bo.enterMouse();
		if(toPush) {
		    Thread.currentThread().sleep(readCommentTimeout);
		    bo.push();
		}
	    } catch(InterruptedException e) {
		e.printStackTrace();
	    } catch(TimeoutExpiredException e) {
		e.printStackTrace();
	    }
	}
    }

    private void hideWindow() {
	if(isModal()) {
	    hide();
	} else {
	    toBack();
	}
    }

    private void showWindow() {
	show();
	if(!isModal()) {
	    toFront();
	}
    }

    private void setStopped(boolean value) {
	stopped = value;
    }

    private void setInterrupted(boolean value) {
	interrupted = value;
    }
}
