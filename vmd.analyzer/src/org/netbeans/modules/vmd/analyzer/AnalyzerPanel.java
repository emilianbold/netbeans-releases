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
 *
 */

package org.netbeans.modules.vmd.analyzer;

import org.netbeans.modules.vmd.api.analyzer.Analyzer;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * @author David Kaspar
 */
public class AnalyzerPanel extends JPanel {

    private static final Font LABEL_FONT = new Font("Dialog", Font.BOLD, 16); // NOI18N
//    private static final Color LABEL_COLOR = new Color (0x88A3CF);
    private static final Color LABEL_COLOR = new Color (0x868686);
    private static final Color BORDER_COLOR = new Color (0xCBC9C1);
    private static final Color GRADIENT_COLOR = new Color (0xD5D5D5);
    private static final Insets EMPTY_INSETS = new Insets (0, 0, 0, 0);
    private static final Insets INSETS = new Insets (10, 10, 10, 10);
    
    public AnalyzerPanel (Analyzer analyzer, JComponent visualRepresentation) {
        setLayout (new GridBagLayout ());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.insets = EMPTY_INSETS;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = GridBagConstraints.REMAINDER;
        constraints.gridy = GridBagConstraints.RELATIVE;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        
        Image image = analyzer.getIcon ();
        JLabel label = new JLabel (analyzer.getDisplayName (), image != null ? new ImageIcon (image) : null, JLabel.CENTER) {
            public void paint (Graphics g) {
                Graphics2D gr = (Graphics2D) g;
                gr.setPaint (new GradientPaint (0.0f, 0.0f, Color.WHITE, 0.0f, getHeight (), GRADIENT_COLOR));
                gr.fill (new Rectangle (getWidth (), getHeight ()));
                super.paint (g);
            }

            public Dimension getPreferredSize () {
                Dimension dimension = super.getPreferredSize ();
                dimension.width += 8;
                dimension.height += 8;
                return dimension;
            }
        };
        label.setBorder (null);
        label.setFont(LABEL_FONT);
        label.setToolTipText(analyzer.getToolTip ());
        label.setBackground (null);
        label.setForeground (LABEL_COLOR);
        label.setOpaque (false);
        add (label, constraints);

        JPanel separator = new JPanel ();
        separator.setOpaque (true);
        separator.setBackground (BORDER_COLOR);
        separator.setPreferredSize (new Dimension (0, 1));
        add (separator, constraints);

        constraints.insets = INSETS;
        add (visualRepresentation, constraints);
    }

    public void paint (Graphics g) {
        Graphics2D gr = (Graphics2D) g;
        Shape previousClip = gr.getClip ();
        gr.clip (new RoundRectangle2D.Float (0, 0, getWidth (), getHeight (), 8, 8));

        super.paint (g);

        gr.setClip (previousClip);
    }


    protected void paintChildren (Graphics g) {
        super.paintChildren (g);
        Graphics2D gr = (Graphics2D) g;
        gr.setColor (BORDER_COLOR);
        gr.draw (new RoundRectangle2D.Float (0, 0, getWidth () - 1, getHeight () - 1, 8, 8));
    }

}
