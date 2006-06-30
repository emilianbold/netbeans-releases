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

package org.netbeans.modules.junit.output;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.accessibility.AccessibleContext;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 *
 * @author Marian Petras
 */
final class ResultPanelOutput extends JScrollPane
                              implements ActionListener {
    
    private static final boolean LOG = false;
    
    static final Color selectedFg;
    static final Color unselectedFg;
    static final Color selectedErr;
    static final Color unselectedErr;
    
    private static final int UPDATE_DELAY = 300;         //milliseconds
    
    static {
        
        /*
         * The property names and colour value constants were copied from class
         * org.netbeans.core.output2.WrappedTextView.
         */
        
        Color color;
        
        color = UIManager.getColor("nb.output.foreground.selected");    //NOI18N
        if (color == null) {
            color = UIManager.getColor("textText");                     //NOI18N
            if (color == null) {
                color = Color.BLACK;
            }
        }
        selectedFg = color;
        
        color = UIManager.getColor("nb.output.foreground");             //NOI18N
        if (color == null) {
            color = selectedFg;
        }
        unselectedFg = color;

        color = UIManager.getColor("nb.output.err.foreground.selected");//NOI18N
        if (color == null) {
            color = new Color(164, 0, 0);
        }
        selectedErr = color;
        
        color = UIManager.getColor("nb.output.err.foreground");         //NOI18N
        if (color == null) {
            color = selectedErr;
        }
        unselectedErr = color;
    }
    
    /** */
    private final Style outputStyle, errOutputStyle;
    //private final Style headingStyle;
    
    /** */
    private final JTextPane textPane;
    /** */
    private final StyledDocument doc;
    /** */
    private final ResultDisplayHandler displayHandler;
    
    /** */
    boolean newLinePending = false;
    
    private Timer timer = null;
    
    /*
     * accessed from multiple threads but accessed only from blocks
     * synchronized with the ResultDisplayHandler's output queue lock
     */
    private volatile boolean timerRunning = false;
    
    /**
     * Creates a new instance of ResultPanelOutput
     */
    ResultPanelOutput(ResultDisplayHandler displayHandler) {
        super();
        if (LOG) {
            System.out.println("ResultPanelOutput.<init>");
        }
        
        textPane = new JTextPane();
        doc = textPane.getStyledDocument();
        textPane.setEditable(false);
        setViewportView(textPane);
        
        AccessibleContext accessibleContext = textPane.getAccessibleContext();
        accessibleContext.setAccessibleName(
                NbBundle.getMessage(getClass(), "ACSN_OutputTextPane"));//NOI18N
        accessibleContext.setAccessibleDescription(
                NbBundle.getMessage(getClass(), "ACSD_OutputTextPane"));//NOI18N
        
        Style defaultStyle = StyleContext.getDefaultStyleContext()
                             .getStyle(StyleContext.DEFAULT_STYLE);
        
        outputStyle = doc.addStyle("output", defaultStyle);             //NOI18N
        StyleConstants.setFontFamily(outputStyle, "Monospaced");        //NOI18N
        StyleConstants.setForeground(outputStyle, unselectedFg);
        
        errOutputStyle = doc.addStyle("error", outputStyle);
        StyleConstants.setForeground(errOutputStyle, unselectedErr);
        
        //headingStyle = doc.addStyle("heading", outputStyle);          //NOI18N
        //StyleConstants.setUnderline(headingStyle, true);
        
        this.displayHandler = displayHandler;
    }
    
    /**
     */
    public void addNotify() {
        super.addNotify();
        
        final Object[] pendingOutput;
        
        if (LOG) {
            System.out.println("ResultPanelOutput.addNotify()");
        }
        
        /*
         * We must make the following block synchronized using the output queue
         * lock to prevent a scenario that some new output would be delivered to
         * the display handler and the output listener would not be set yet.
         */
        synchronized (displayHandler.getOutputQueueLock()) {
            pendingOutput = displayHandler.consumeOutput();
            if (pendingOutput.length == 0) {
                displayHandler.setOutputListener(this);
            }
        }
        
        if (pendingOutput.length != 0) {
            displayOutput(pendingOutput);
            startTimer();
        }
    }
    
    /**
     */
    void outputAvailable() {

        /* Called from the AntLogger's thread */

        if (LOG) {
            System.out.println("ResultOutputPanel.outputAvailable() - called by the AntLogger");
        }
        //synchronized (displayHandler.getOutputQueueLock()):
        final Object[] pendingOutput = displayHandler.consumeOutput();
        assert pendingOutput.length != 0;
        new OutputDisplayer(pendingOutput).run();
        displayHandler.setOutputListener(null);
        if (!timerRunning) {
            startTimer();
        }
    }

    final class OutputDisplayer implements Runnable {
        private final Object[] output;
        OutputDisplayer(Object[] output) {
            this.output = output;
        }
        public void run() {
            if (!EventQueue.isDispatchThread()) {
                EventQueue.invokeLater(this);
                return;
            }
            displayOutput(output);
        }
    }
    
    /**
     * This method is called by a Swing timer (in the dispatch thread).
     */
    public void actionPerformed(ActionEvent e) {
        
        /* Called by the Swing timer (in the EventDispatch thread) */
        
        assert EventQueue.isDispatchThread();
        
        if (LOG) {
            System.out.println("ResultOutputPanel.actionPerformed(...) - called by the timer");
        }
        final Object[] pendingOutput = displayHandler.consumeOutput();
        if (pendingOutput.length != 0) {
            displayOutput(pendingOutput);
        } else {
            synchronized (displayHandler.getOutputQueueLock()) {
                stopTimer();
                displayHandler.setOutputListener(this);
            }
        }
    }
    
    /**
     */
    private void startTimer() {
        if (LOG) {
            System.out.println("ResultPanelOutput.startTimer()");
        }
        if (timer == null) {
            timer = new Timer(UPDATE_DELAY, this);
        }
        timerRunning = true;
        timer.start();
    }
    
    /**
     */
    private void stopTimer() {
        if (LOG) {
            System.out.println("ResultPanelOutput.stopTimer()");
        }
        if (timer != null) {
            timer.stop();
            timerRunning = false;
        }
    }
    
    /**
     */
    void displayOutput(final Object[] output) {
        assert EventQueue.isDispatchThread();
        
        if (LOG) {
            System.out.println("ResultPanelOutput.displayOutput(...):");
            for (int i = 0; output[i] != null; i++) {
                System.out.println("    " + output[i]);
            }
        }
        Object o;
        int index = 0;
        while ((o = output[index++]) != null) {
            boolean errOutput = false;
            if (o == Boolean.TRUE) {
                o = output[index++];
                errOutput = true;
            }
            displayOutputLine(o.toString(), errOutput);
        }
    }
    
    /**
     */
    private void displayOutputLine(final String text, final boolean error) {
        final Style textStyle = error ? errOutputStyle : outputStyle;
        
        try {
            if (newLinePending) {
                doc.insertString(doc.getLength(), "\n", outputStyle);   //NOI18N
                newLinePending = false;
            }
            doc.insertString(doc.getLength(), text, textStyle);
            newLinePending = true;
        } catch (BadLocationException ex) {
            ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
        }
    }
    
    //<editor-fold defaultstate="collapsed" desc="displayReport(Report)">
    /* *
     * /
    private void displayReport(final Report report) {
        if (report == null) {
            clear();
            return;
        }
        
        try {
            doc.insertString(
                    0,
                    NbBundle.getMessage(getClass(), "MSG_StdOutput"),   //NOI18N
                    headingStyle);
            doc.insertString(
                    doc.getLength(),
                    "\n",                                               //NOI18N
                    headingStyle);
            if ((report.outputStd != null) && (report.outputStd.length != 0)) {
                displayText(report.outputStd);
            }
            doc.insertString(
                    doc.getLength(),
                    "\n\n",                                             //NOI18N
                    outputStyle);
            doc.insertString(
                    doc.getLength(),
                    NbBundle.getMessage(getClass(), "MSG_ErrOutput"),   //NOI18N
                    headingStyle);
            if ((report.outputErr != null) && (report.outputErr.length != 0)) {
                doc.insertString(
                        doc.getLength(),
                        "\n",                                           //NOI18N
                        headingStyle);
                displayText(report.outputErr);
            }
        } catch (BadLocationException ex) {
            ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
        }
    }
    */
    //</editor-fold>
    
    /**
     */
    private void clear() {
        assert EventQueue.isDispatchThread();
        
        try {
            doc.remove(0, doc.getLength());
        } catch (BadLocationException ex) {
            ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
        }
    }
    
    //<editor-fold defaultstate="collapsed" desc="displayText(String[])">
    /* *
     * /
    private void displayText(final String[] lines) throws BadLocationException {
        final int limit = lines.length - 1;
        for (int i = 0; i < limit; i++) {
            doc.insertString(doc.getLength(),
                             lines[i] + '\n',
                             outputStyle);
        }
        doc.insertString(doc.getLength(),
                         lines[limit],
                         outputStyle);
    }
    */
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="display(DisplayContents)">
    /* *
     * /
    private void display(ResultDisplayHandler.DisplayContents display) {
        assert EventQueue.isDispatchThread();
        
        Report report = display.getReport();
        String msg = display.getMessage();
        if (report != null) {
            displayReport(report);
        } else {
            clear();
        }
    }
    //</editor-fold>

    /* *
     * /
    //<editor-fold defaultstate="collapsed" desc="updateDisplay()">
    private void updateDisplay() {
        ResultDisplayHandler.DisplayContents display
                                                = displayHandler.getDisplay();
        if (display != null) {
            display(display);
        }
    }
    */
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="stateChanged(ChangeEvent)">
    /* *
     * /
    public void stateChanged(ChangeEvent e) {
        updateDisplay();
    }
     */
    //</editor-fold>
    
    /**
     */
    public boolean requestFocusInWindow() {
        return textPane.requestFocusInWindow();
    }

}
