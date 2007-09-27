/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
