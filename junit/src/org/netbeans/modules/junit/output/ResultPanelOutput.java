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

package org.netbeans.modules.junit.output;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
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
final class ResultPanelOutput extends JScrollPane {
    
    /** */
    private final Style outputStyle, headingStyle;
    
    /** */
    private final JTextPane textPane;
    /** */
    private final StyledDocument doc;
    
    /**
     * Creates a new instance of ResultPanelOutput
     */
    ResultPanelOutput(final Report report) {
        super();
        
        textPane = new JTextPane();
        doc = textPane.getStyledDocument();
        textPane.setEditable(false);
        setViewportView(textPane);
        
        
        Style defaultStyle = StyleContext.getDefaultStyleContext()
                             .getStyle(StyleContext.DEFAULT_STYLE);
        outputStyle = doc.addStyle("output", defaultStyle);             //NOI18N
        StyleConstants.setFontFamily(outputStyle, "Monospaced");        //NOI18N
        headingStyle = doc.addStyle("heading", outputStyle);            //NOI18N
        StyleConstants.setUnderline(headingStyle, true);
        
        
        displayReport(report);
    }
    
    /**
     */
    private void displayReport(final Report report) {
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
    
    /**
     */
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
    
}
