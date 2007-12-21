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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.iep.editor.tcg.exception;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


import org.netbeans.modules.iep.model.lib.I18n;

/**
 * This class is meant to provide a consistent means of presenting a
 * Exception to the user. It is intended as a replacement for JOptionPane
 * in error conditions. The defect of JOptionPane is that it provides room
 * only for a message, when in error conditions both a message and a stack
 * trace are desired. In ErrorDisplay, click on the middle "more/less" button
 * either exposes or conceals the stack trace.
 *
 * @author Bing Lu
 */
public class ErrorDisplay
    extends JDialog
    implements ActionListener {

    /**
     * String for the color to get from the UI manager for the text area.
     */
    private static final String UI_MANAGER_TEXT_AREA_COLOR_STRING = "control";

    /**
     * This forces the ErrorDisplay to be of a certain width.
     */
    private static final int NUMBER_OF_TEXT_AREA_COLUMNS = 60;

    /**
     * Our logger
     */
    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(ErrorDisplay.class.getName());

    /**
     * Handles the internationalization bundle.
     */
    private static I18n mTranslator = new I18n("org.netbeans.modules.iep.editor.util.Bundle");

    /**
     * String for the details button in its "less" state.
     */
    private static String mLessString;

    /**
     * String for the details button in its "more" state.
     */
    private static String mMoreString;

    /**
     * String to display if we cannot display the stack trace for some reason.
     */
    private static String mNoTraceAvailableString;

    /**
     * String for OK button.
     */
    private static String mOkString;

    /**
     * Title of our error display
     */
    private static String mTitleString;

    static {
        mTranslator = new I18n("org.netbeans.modules.iep.editor.tcg.exception.Bundle");

        try {
            mOkString = mTranslator.i18n("ErrorDisplay.OK", "Ok", null);
            mMoreString = mTranslator.i18n("ErrorDisplay.MORE", "More", null);
            mLessString = mTranslator.i18n("ErrorDisplay.LESS", "Less", null);
            mNoTraceAvailableString = mTranslator.i18n("ErrorDisplay.NO_TRACE_AVAILABLE",
                                                       "No trace available",
                                                       null);
            mTitleString = mTranslator.i18n("ErrorDisplay.ERROR_DISPLAY_TITLE_STRING",
                                            "Error", 
                                            null);
        } catch (Exception e) {
            mLog.warning(e.getMessage());
        }
    }

    /**
     * The Exception we are trying to display.
     */
    private Exception mException;
    private java.awt.Dimension mFirstSize;
    private java.awt.Dimension mSecondSize;

    /**
     * A label to hold message contents.
     */
    private ErrorDisplayTextArea mMessageJta = new ErrorDisplayTextArea();

    /**
     * The text area to hold the stack trace.
     */
    private ErrorDisplayTextArea mStackTraceJta = new ErrorDisplayTextArea();

    /**
     * The button that expose/conceals stack traces.
     */
    private JButton mDetailsButton;

    /**
     * The OK button.
     */
    private JButton mOk;

    /**
     * A panel for the buttons.
     */
    private JPanel mButtonPanel = new JPanel();

    /**
     * The content pane.
     */
    private JPanel mPanel = new JPanel();

    /**
     * A scroll pane to hold the stack trace text area in.
     */
    private JScrollPane mStackTraceScrollPane;

    /**
     * The String we are displaying in lieu of an exception.
     */
    private String mMessage;

    /**
     * Constructor for the ErrorDisplay object
     *
     * @param d Description of the Parameter
     * @param s Description of the Parameter
     */
    public ErrorDisplay(JDialog d, String s) {

        super(d, true);

        mMessage = s;

        init(d);
    }

    /**
     * Constructor for the ErrorDisplay object
     *
     * @param d Description of the Parameter
     * @param dap Description of the Parameter
     */
    public ErrorDisplay(JDialog d, Exception dap) {

        super(d, true);

        mException = dap;

        init(d);
    }

    /**
     * Constructor for the ErrorDisplay object
     *
     * @param c Description of the Parameter
     * @param s Description of the Parameter
     */
    public ErrorDisplay(java.awt.Frame f, String s) {

        super(f, true);

        mMessage = s;

        init(f);
    }

    /**
     * Constructor for the ErrorDisplay object
     *
     * @param c Description of the Parameter
     * @param s Description of the Parameter
     */
    public ErrorDisplay(Component c, String s) {

        super(new JFrame(), true);

        mMessage = s;

        init((Window) findOwner(c));
    }

    /**
     * Constructor for the ErrorDisplay object
     *
     * @param c Description of the Parameter
     * @param s Description of the Parameter
     * @param modal Description of the Parameter
     */
    public ErrorDisplay(Component c, String s, boolean modal) {

        super(new JFrame(), modal);

        mMessage = s;

        init((Window) findOwner(c));
    }

    /**
     * Constructor for the ErrorDisplay object
     *
     * @param c Description of the Parameter
     * @param dap Description of the Parameter
     */
    public ErrorDisplay(Component c, Exception dap) {

        super(new JFrame(), true);

        mException = dap;

        init((Window) findOwner(c));
    }

    /**
     * Constructor for the ErrorDisplay object
     *
     * @param c Description of the Parameter
     * @param dap Description of the Parameter
     * @param modal Description of the Parameter
     */
    public ErrorDisplay(Component c, Exception dap, boolean modal) {

        super(new JFrame(), modal);

        mException = dap;

        init((Window) findOwner(c));
    }

    /**
     * The main program for the ErrorDisplay class
     *
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        // Try it with the long messy untranslated exception message.
        Exception dap = new Exception();

        try {
            throw dap;
        } catch (Exception de) {
            new ErrorDisplay(new JFrame(), dap);
        }
    }

    /**
     * Handles actions for the buttons. The "OK" button disposes the dialog.
     * The "more/less" button hides or displays the stack trace.
     *
     * @param e our action command.
     */
    public void actionPerformed(ActionEvent e) {

        if (e.getActionCommand().equals(mMoreString)) {
            showMore();
        } else if (e.getActionCommand().equals(mLessString)) {
            showLess();
        } else {
            dispose();
        }
    }

    /**
     * Finds the top-level owner of a component. Used for centering the dialog.
     *
     * @param c The component instantiating an ErrorDisplay.
     *
     * @return The first Window found, or a new JFrame if a Window could not be
     *         found.
     */
    private static Object findOwner(Component c) {

        Component candidate = c;

        while (true) {
            if (candidate instanceof Window) {
                return candidate;
            } else if (candidate == null) {
                return new JFrame();
            } else {
                candidate = candidate.getParent();
            }
        }
    }

    /**
     * Get the stack trace text out of an exception. There is no way to get a
     * stack trace into a string using any of the exception methods. For this
     * reason, we need to create new StringWriter, BufferedWriter, and
     * PrintWriter classes. These are used, one after the other, to extract
     * the stack trace from the output stream.
     *
     * @param e Description of the Parameter
     *
     * @return The stack trace text.
     */
    private String getStackTrace(Throwable e) {

        StringWriter writer = new StringWriter();
        BufferedWriter pws = new BufferedWriter(writer);
        PrintWriter out = new PrintWriter(pws);

        // Print the stack trace using the created PrintWriter
        e.printStackTrace(out);

        // Flush the text into the BufferedWriter
        try {
            pws.flush();
        } catch (Throwable ex) {
            mLog.warning(ex.getMessage());

            return mNoTraceAvailableString;
        }

        // Return the string from the writer's text buffer!
        return (writer.getBuffer().toString());
    }

    /**
     * Description of the Method
     *
     * @param w Description of the Parameter
     */
    private void init(Window w) {

        setTitle(mTitleString);

        // setBackground(UIManager.getColor(ErrorDisplay.UI_MANAGER_TEXT_AREA_COLOR_STRING));
        mOk = new JButton(mOkString);

        mPanel = new JPanel();

        if (mException != null) {
            mMessageJta.append(mException.getMessage());
        } else {
            mMessageJta.append(mMessage);
        }

        mMessageJta.setEditable(false);
        mMessageJta.setBackground(mPanel.getBackground());
        mPanel.setLayout(new BorderLayout());
        mPanel.add(BorderLayout.CENTER, mMessageJta);
        mOk.addActionListener(this);
        mButtonPanel.add(mOk);

        // If a constructor with a Exception was used, provide the means to
        // access the stack trace.
        if (mException != null) {
            mDetailsButton = new JButton(mMoreString);

            mDetailsButton.addActionListener(this);
            mButtonPanel.add(mDetailsButton);
            mStackTraceJta.append(getStackTrace(mException));
            mStackTraceJta.setEditable(false);

            mStackTraceScrollPane = new JScrollPane(mStackTraceJta);
        }

        mPanel.add(BorderLayout.SOUTH, mButtonPanel);
        setContentPane(mPanel);
        mOk.requestFocus(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();

        // This extra pack is necessary for mysterious reasons.
        pack();

        mFirstSize = getPreferredSize();

        setLocationRelativeTo(w);
        setVisible(true);
    }

    /**
     * Hides the stack trace.
     */
    private void showLess() {

        mDetailsButton.setText(mMoreString);
        mPanel.removeAll();
        mPanel.setLayout(new BorderLayout());
        mMessageJta.setColumns(ErrorDisplay.NUMBER_OF_TEXT_AREA_COLUMNS);
        mPanel.add(BorderLayout.NORTH, mMessageJta);
        mPanel.add(BorderLayout.SOUTH, mButtonPanel);
        setContentPane(mPanel);
        mPanel.setMinimumSize(mFirstSize);
        mPanel.setPreferredSize(mFirstSize);
        mPanel.setMaximumSize(mFirstSize);
        setContentPane(mPanel);
        pack();
        validate();
        repaint();
    }

    /**
     * Shows the stack trace.
     */
    private void showMore() {

        mDetailsButton.setText(mLessString);
        mPanel.removeAll();
        mPanel.setLayout(new BorderLayout());
        mMessageJta.setColumns(90);
        mPanel.add(BorderLayout.NORTH, mMessageJta);
        mPanel.add(BorderLayout.CENTER, mStackTraceScrollPane);
        mPanel.add(BorderLayout.SOUTH, mButtonPanel);

        if (mSecondSize == null) {
            mSecondSize = mPanel.getPreferredSize();
        } else {
            mPanel.setMinimumSize(mSecondSize);
            mPanel.setPreferredSize(mSecondSize);
            mPanel.setMaximumSize(mSecondSize);
        }

        setContentPane(mPanel);
        pack();
        validate();
        repaint();
    }

    /**
     * A text area with hard coded attributes for use by both the message and
     * the stack trace.
     *
     * @author Bing Lu
     */
    private static class ErrorDisplayTextArea
        extends JTextArea {

        /**
         * Constructor for the ErrorDisplayTextArea object
         */
        public ErrorDisplayTextArea() {

            super();

            setColumns(ErrorDisplay.NUMBER_OF_TEXT_AREA_COLUMNS);
            setWrapStyleWord(true);
            setLineWrap(true);
        }

        /**
         * Sets the columns attribute of the ErrorDisplayTextArea object
         *
         * @param i The new columns value
         */
        public void setColumns(int i) {
            super.setColumns(i);
        }
    }
}


/*--- Formatted in SeeBeyond Java Convention Style on Thu, Dec 5, '02 ---*/


/*------ Formatted by Jindent 3.24 Gold 1.02 --- http://www.jindent.de ------*/
