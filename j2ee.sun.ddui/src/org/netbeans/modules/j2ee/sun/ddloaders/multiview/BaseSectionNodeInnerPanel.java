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
package org.netbeans.modules.j2ee.sun.ddloaders.multiview;

import java.awt.Dimension;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;


/**
 *
 * @author Peter Williams
 */
public class BaseSectionNodeInnerPanel extends SectionNodeInnerPanel {

    public static final ResourceBundle commonBundle = ResourceBundle.getBundle(
       "org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.Bundle"); // NOI18N
    
    public static final ResourceBundle customizerBundle = ResourceBundle.getBundle(
       "org.netbeans.modules.j2ee.sun.share.configbean.customizers.Bundle"); // NOI18N

    
    // Current descriptor version.
    protected final ASDDVersion version;
    
    // true if AS 8.0+ fields are visible.
    protected final boolean as80FeaturesVisible;
    
    // true if AS 8.1+ fields are visible.
    protected final boolean as81FeaturesVisible;

    // true if AS 9.0+ fields are visible.
    protected final boolean as90FeaturesVisible;
    

    public BaseSectionNodeInnerPanel(SectionNodeView sectionNodeView, final ASDDVersion version) {
        super(sectionNodeView);
        
        this.version = version;
        this.as80FeaturesVisible = ASDDVersion.SUN_APPSERVER_8_0.compareTo(version) <= 0;
        this.as81FeaturesVisible = ASDDVersion.SUN_APPSERVER_8_1.compareTo(version) <= 0;
        this.as90FeaturesVisible = ASDDVersion.SUN_APPSERVER_9_0.compareTo(version) <= 0;
    }
    
    // ------------------------------------------------------------------------
    //
    //  Abstract methods from SectionNodeInnerPanel
    //
    // ------------------------------------------------------------------------
    public void setValue(JComponent source, Object value) {
    }

    public void linkButtonPressed(Object ddBean, String ddProperty) {
    }

    public JComponent getErrorComponent(String errorId) {
        return null;
    }

    
    // ------------------------------------------------------------------------
    //
    //  Nicer default sizing behavior.
    //
    // ------------------------------------------------------------------------
    /** Return reasonable maximum size.  Usage of GridBagLayout + stretch fields
     *  in this panel cause the default maximum size behavior to too wide.
     */
    @Override
    public Dimension getMaximumSize() {
        return new Dimension(CustomSectionNodePanel.MAX_WIDTH, super.getMaximumSize().height);
    }
    
    /** Return correct preferred size.  Multiline JLabels cause the default
     *  preferred size behavior to too wide.
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(getMinimumSize().width, super.getPreferredSize().height);
    }
    
    
}
