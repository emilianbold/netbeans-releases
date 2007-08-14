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
package org.netbeans.modules.j2ee.sun.ddloaders.multiview;

import java.awt.Dimension;
import java.awt.Image;
import javax.swing.ImageIcon;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionNodePanel;

/**
 *
 * @author Peter Williams
 */
public class CustomSectionNodePanel extends SectionNodePanel {

    public static final int MAX_WIDTH = 600;
    
    public CustomSectionNodePanel(final SectionNode node) {
        super(node, false);

        setAlignmentX(LEFT_ALIGNMENT);
//        setBorder(javax.swing.BorderFactory.createTitledBorder("custom section node panel"));
    }
    
    public void setTitleIcon(String iconBase) {
        Image iconImage = org.openide.util.Utilities.loadImage(iconBase, true);
        getTitleButton().setIcon(iconImage != null ? new ImageIcon(iconImage) : null);
    }

//    @Override
//    protected void openInnerPanel() {
//        System.out.println(this.getNode().getClass().getName() + " : SectionNodePanel.openInnerPanel()");
//        super.openInnerPanel();
//    }
//
//    /** Return reasonable maximum size.  Usage of GridBagLayout + stretch fields
//     * in this panel cause the default maximum size behavior to too wide.
//     */
//    @Override
//    public Dimension getMaximumSize() {
//        return new Dimension(MAX_WIDTH, super.getMaximumSize().height);
//    }
    
    /** Return correct preferred size.  The multiline JLabels in this panel cause
     *  the default preferred size behavior to be incorrect (too wide).
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(getMinimumSize().width, super.getPreferredSize().height);
    }
    
}
