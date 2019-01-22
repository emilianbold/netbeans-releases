/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.vmd.analyzer;

import org.netbeans.modules.vmd.api.analyzer.Analyzer;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * 
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
            @Override
            public void paint (Graphics g) {
                Graphics2D gr = (Graphics2D) g;
                gr.setPaint (new GradientPaint (0.0f, 0.0f, Color.WHITE, 0.0f, getHeight (), GRADIENT_COLOR));
                gr.fill (new Rectangle (getWidth (), getHeight ()));
                super.paint (g);
            }

            @Override
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

    @Override
    public void paint (Graphics g) {
        Graphics2D gr = (Graphics2D) g;
        Shape previousClip = gr.getClip ();
        gr.clip (new RoundRectangle2D.Float (0, 0, getWidth (), getHeight (), 8, 8));

        super.paint (g);

        gr.setClip (previousClip);
    }


    @Override
    protected void paintChildren (Graphics g) {
        super.paintChildren (g);
        Graphics2D gr = (Graphics2D) g;
        gr.setColor (BORDER_COLOR);
        gr.draw (new RoundRectangle2D.Float (0, 0, getWidth () - 1, getHeight () - 1, 8, 8));
    }

}
