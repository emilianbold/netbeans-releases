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

import org.netbeans.modules.sql.framework.ui.graph.IHighlightConfigurator;
import org.netbeans.modules.sql.framework.ui.graph.ListAreaCellRenderer;

import com.nwoods.jgo.JGoObject;

/**
 * Extends highlightable basic cell to allow list area components to change color on mouse
 * hover.
 * 
 * @author Ritesh Adval
 * @author Jonathan Giron
 * @version $Revision$
 */
public class DefaultListAreaRenderer extends BasicCellArea.Highlightable implements ListAreaCellRenderer {

    /** Creates a new instance of DefaultListAreaRenderer */
    public DefaultListAreaRenderer() {
    }

    /**
     * Gets the list area cell renderer
     * 
     * @param list list Area
     * @param value value
     * @param index index
     * @param isSelected isSelected
     * @param cellHasFocus cellHasFocus
     * @return list renderer
     */
    public JGoObject getListAreaCellRenderer(JGoObject list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        BasicCellArea.Highlightable tCellArea = new BasicCellArea.Highlightable(BasicCellArea.LEFT_PORT_AREA, value.toString());

        tCellArea.setBrush(this.getHighlightConfigurator().getNormalBrush());
        tCellArea.setTextColor(this.getTextColor());
        tCellArea.setHighlightEnabled(this.isHighlightEnabled());

        IHighlightConfigurator tHC = tCellArea.getHighlightConfigurator();
        tHC.setHoverBrush(this.getHighlightConfigurator().getHoverBrush());

        // Draw bounding rectangle around it
        tCellArea.drawBoundingRect(true);
        return cellArea;
    }
}

