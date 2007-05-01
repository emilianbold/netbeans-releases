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
package org.netbeans.modules.sql.framework.ui.graph.impl;

import java.awt.Color;

import org.netbeans.modules.sql.framework.ui.graph.IHighlightConfigurator;

import com.nwoods.jgo.JGoBrush;

/**
 * Implementation of HighlightConfigurator interface.
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public class HighlightConfiguratorImpl implements IHighlightConfigurator {
    private JGoBrush brush = null;
    private JGoBrush hoverBrush = null;

    /**
     * Creates a new default instance of HighlightConfiguratorImpl using the same color ({@link IHighlightConfigurator#DEFAULT_BASIC_COLOR}
     * for both normal and highlighted cases.
     */
    public HighlightConfiguratorImpl() {
        this(IHighlightConfigurator.DEFAULT_BASIC_COLOR, IHighlightConfigurator.DEFAULT_BASIC_COLOR);
    }

    /**
     * Creates a new instance of HighlightConfiguratorImpl using the given Colors for
     * normal and highlighted cases.
     * 
     * @param normalColor Color to use when rendering background under normal
     *        circumstances
     * @param highlightedColor Color to use when rendering background during highlighted
     *        conditions
     */
    public HighlightConfiguratorImpl(Color normalColor, Color highlightedColor) {
        this(JGoBrush.makeStockBrush(normalColor), JGoBrush.makeStockBrush(highlightedColor));
    }

    /**
     * Creates a new instance of HighlightConfiguratorImpl using the given JGoBrushes for
     * normal and highlighted cases.
     * 
     * @param normal JGoBrush to use when rendering background under normal circumstances
     * @param highlighted JGoBrush to use when rendering background during highlighted
     *        conditions
     */
    public HighlightConfiguratorImpl(JGoBrush normal, JGoBrush highlighted) {
        brush = normal;
        hoverBrush = highlighted;
    }

    /**
     * @see org.netbeans.modules.sql.framework.ui.graph.IHighlightConfigurator#getNormalBrush()
     */
    public JGoBrush getNormalBrush() {
        return (brush != null) ? brush : JGoBrush.makeStockBrush(IHighlightConfigurator.DEFAULT_BASIC_COLOR);
    }

    /**
     * @see org.netbeans.modules.sql.framework.ui.graph.IHighlightConfigurator#setNormalBrush(com.nwoods.jgo.JGoBrush)
     */
    public void setNormalBrush(JGoBrush newBrush) {
        brush = newBrush;
    }

    /**
     * @see org.netbeans.modules.sql.framework.ui.graph.IHighlightConfigurator#getHoverBrush()
     */
    public JGoBrush getHoverBrush() {
        return (hoverBrush != null) ? hoverBrush : JGoBrush.makeStockBrush(IHighlightConfigurator.DEFAULT_HOVER_COLOR);
    }

    /**
     * @see org.netbeans.modules.sql.framework.ui.graph.IHighlightConfigurator#setHoverBrush(com.nwoods.jgo.JGoBrush)
     */
    public void setHoverBrush(JGoBrush newBrush) {
        hoverBrush = newBrush;
    }
}
