/*
 * Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 * 
 * The Original Code is the Jemmy library.
 * The Initial Developer of the Original Code is Alexandre Iline.
 * All Rights Reserved.
 * 
 * Contributor(s): Alexandre Iline.
 * 
 * $Id$ $Revision$ $Date$
 * 
 */

package org.netbeans.jemmy.demo;

/**
 *
 * Interface implementation defines a way to display step comments
 * during demo or test debug.
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 * 
 *	
 */

public interface CommentWindow {
    /**
     * Defines either test(demo) has been stopped or not.
     * @return true if test (demo) execution has been stopped.
     * Like when user is reading step comments.
     * false if test execution can be continued.
     */
    public boolean isStopped();

    /**
     * Defines either test execution should be interrupted or not.
     * @return If true, execution will be interrupted.
     */
    public boolean isInterrupted();

    /**
     * Defines window title.
     * @param title Title to display.
     */
    public void setTitle(String title);

    /**
     * Should display next step comment.
     * @param stepComment Comments to be displayed.
     */
    public void nextStep(String stepComment);

    /**
     * Method is invoked at the end of test(demo).
     * @param stepComment Comment to be displayed.
     */
    public void showFinalComment(String stepComment);

    /**
     * Closes the window.
     */
    public void close();

    /**
     * Returns a message for a case when test needs to be interrupted.
     * @return Interrupted message if test should be interrupted.
     */
    public String getInterruptMessage();
}
