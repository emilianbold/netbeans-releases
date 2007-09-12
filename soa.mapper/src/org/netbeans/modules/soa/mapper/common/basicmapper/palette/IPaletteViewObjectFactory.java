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

package org.netbeans.modules.soa.mapper.common.basicmapper.palette;

import org.netbeans.modules.soa.mapper.common.ui.palette.IPaletteCategory;
import org.netbeans.modules.soa.mapper.common.ui.palette.IPaletteItem;

/**
 * <p>
 *
 * Title: </p> IPaletteViewObjectFactory<p>
 *
 * Description: </p> IPaletteViewObjectFactory provides interfaces to define the
 * implemenation of the IPaletteViewItem for the mapper palette view.<p>
 *
 * @author    Un Seng Leong
 * @created   January 6, 2003
 */

public interface IPaletteViewObjectFactory {

    /**
     * Return a newly create Palette view item by the specified palette item
     * from Palette manager.
     *
     * @param item  the palette item model
     * @return      a newly create Palette view item by the specified palette
     *      item from Palette manager.
     */
    public IPaletteViewItem createViewablePaletteItem(IPaletteItem item);

    /**
     * Return a newly create palette category view item by the specified palette
     * category item from Palette manager.
     *
     * @param category  Description of the Parameter
     * @return          a newly create palette category view item by the
     *      specified palette category item from Palette manager.
     */
    public IPaletteViewItem createViewablePaletteCategoryItem(IPaletteCategory category);

    /**
     * Return a palette view item that performs auto layout for this mapper.
     *
     * @return   a palette view item that performs auto layout for this mapper.
     */
    public IPaletteViewItem createAutoLayoutItem();

    /**
     * Return a palette view item that performs expand all group nodes for this mapper.
     *
     * @return   a palette view item that performs expand all group nodes for this mapper.
     */
    public IPaletteViewItem createExpandAllNodesItem();

    /**
     * Return a palette view item that performs collapse all group nodes for this mapper.
     *
     * @return   a palette view item that performs collapse all group nodes for this mapper.
     */
    public IPaletteViewItem createCollapseAllNodesItem();
    
    /**
     * Return a palette view item that performs collapse all group nodes for this mapper.
     *
     * @return   a palette view item that performs collapse all group nodes for this mapper.
     */
    public IPaletteViewItem createDeleteSelectedNodesItem();
}
