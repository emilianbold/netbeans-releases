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

import java.awt.Component;
import javax.swing.Icon;

/**
 * <p>
 *
 * Title: IPaletteViewItem </p> <p>
 *
 * Description: Describe a view on each palette item </p> <p>
 *
 * @author    Un Seng Leong
 * @created   January 2, 2003
 */
public interface IPaletteViewItem {

    /**
     * Retrun the icon of the palette item
     *
     * @return   the icon of the palette item
     */
    public Icon getIcon();

    /**
     * Return the tooptip text of the palette item
     *
     * @return   the tooptip text of the palette item
     */
    public String getToolTipText();

    /**
     * Return the palette item in another form of object repersentation
     *
     * @return   the palette item in another form of object repersentation
     */
    public Object getItemObject();

    /**
     * Return the transferable object for drag and drop opertaion.
     *
     * @return   the transferable object for drag and drop opertaion.
     */
    public Object getTransferableObject();

    /**
     * Set the transferable object for drag and drop opertaion.
     *
     * @param obj  the transferable object for drag and drop opertaion.
     */
    public void setTransferableObject(Object obj);

    /**
     * Return true if the palette item is visible, false otherwise.
     *
     * @return   true if the palette item is visible, false otherwise.
     */
    public boolean isVisible();

    /**
     * Set the visiblility of this palette item.
     *
     * @param isVisible  true if this palette item is visible, false otherwise.
     */
    public void setVisible(boolean isVisible);

    /**
     * Return the Java AWT component as the viewiable object of this palette
     * view item.
     *
     * @return   the Java AWT component as the viewiable object of this palette
     *      view item.
     */
    public Component getViewComponent();
}
