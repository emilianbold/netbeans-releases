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

package org.netbeans.modules.vmd.screen.resource;

import org.netbeans.modules.vmd.api.screen.resource.ScreenResourceCategoryDescriptor;
import org.netbeans.modules.vmd.api.screen.resource.ScreenResourceItemPresenter;
import org.netbeans.modules.vmd.screen.MainPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;

/**
 *
 * @author David Kaspar
 */
public class ResourceCategoryPanel extends JPanel {

    private static final Color LABEL_COLOR = new Color (0x868686);
    private static final Color BORDER_COLOR = new Color (0xCBC9C1);
    private static final Color GRADIENT_COLOR = new Color (0xD5D5D5);
    private static final Insets EMPTY_INSETS = new Insets (0, 0, 0, 0);

    private JLabel label;

    public ResourceCategoryPanel(ScreenResourceCategoryDescriptor category) {
        setOpaque (false);
        setLayout(new GridBagLayout());
        setBackground(MainPanel.BACKGROUND_COLOR);
        setBorder (BorderFactory.createEmptyBorder (1, 1, 1, 1));
        
        label = new JLabel (category.getTitle(), JLabel.CENTER) {
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
        label.setFont(getFont().deriveFont(Font.BOLD));
        label.setToolTipText(category.getToolTip());
        label.setBackground (null);
        label.setForeground (LABEL_COLOR);
        label.setOpaque (false);
    }
    
    public void reload(ArrayList<ScreenResourceItemPresenter> list) {
        removeAll ();

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.insets = EMPTY_INSETS;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = GridBagConstraints.REMAINDER;
        constraints.gridy = GridBagConstraints.RELATIVE;
        constraints.anchor = GridBagConstraints.NORTHWEST;

        add (label, constraints);

        for (ScreenResourceItemPresenter presenter : list) {
            JPanel filler = new JPanel (); // TODO - cache filler panels
            Dimension FILLER_SIZE = new Dimension (0, 1);
            filler.setPreferredSize (FILLER_SIZE);
            filler.setBackground (BORDER_COLOR);
            add (filler, constraints);

            ResourceItemPanel item = new ResourceItemPanel (presenter.getRelatedComponent ()); // TODO - cache ResourceItemPanels
            
            item.reload ();
            
            add (item, constraints);
        }
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
