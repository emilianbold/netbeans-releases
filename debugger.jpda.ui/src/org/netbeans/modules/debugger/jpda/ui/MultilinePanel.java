/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.ui;

import java.awt.Font;
import java.util.StringTokenizer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Panel for displaying multiline labels.
 * Also a title (emphasized first line of the text) may be specified.
 * The text is parsed using <CODE>\n</CODE> as a delimiter.
 * <P>
 * The panel is built from several <TT>JLabel</TT>s, layed out vertically
 * using {@link BoxLayout}. A vertical glue ({@link Box#createVerticalGlue()})
 * is added on the bottom of the panel.
 *
 * @author  Marian Petras
 */
public final class MultilinePanel extends JPanel {
    
    /**
     * Creates a panel containing the specified text.
     *
     * @param  text  text to be displayed
     */
    public MultilinePanel(String text) {
        this(null, text);
    }
    
    /**
     * Creates a panel containing the specified title and text.
     * The title will be displayed as an emphasized (bold) first
     * line of text.
     *
     * @param  title  title of the dialog
     *                (if <CODE>null</CODE>, no title is displayed)
     * @param  text  text to be displayed
     */
    public MultilinePanel(String title, String text) {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        //
        if (title != null) {
            JLabel label = new JLabel(title);
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            add(label);
        }
        //
        StringTokenizer tokenizer = new StringTokenizer(text, "\n", true);  //NOI18N
        boolean lastWasNewline = true;
        for (int i = 0; tokenizer.hasMoreTokens(); ++i) {
            String line = tokenizer.nextToken();
            if (line.equals("\n")) {   //NOI18N
                if (!lastWasNewline) {
                    lastWasNewline = true;
                    continue;   //newline after text - end of line
                }
                //two adjacent newlines - empty line
                line = " ";  //empty JLabels have zero height    //NOI18N
            }
            else {
                lastWasNewline = false;
            }
            add(new JLabel(line));
        }
        add(Box.createVerticalGlue());
    }
    
}
