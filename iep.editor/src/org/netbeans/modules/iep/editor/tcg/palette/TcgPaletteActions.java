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


package org.netbeans.modules.iep.editor.tcg.palette;

import javax.swing.Action;
import org.netbeans.spi.palette.PaletteActions;
import org.openide.util.Lookup;

/*
 * TcgPaletteActions.java
 *
 * Created on November 20, 2005, 1:54 PM
 *
 * @author Bing Lu
 */
public class TcgPaletteActions extends PaletteActions {
    
    /** Creates a new instance of FormPaletteProvider */
    public TcgPaletteActions() {
    }

    /**
     * @return An array of action that will be used to construct buttons for import
     * of new palette item in palette manager window.
     *
     */
    public Action[] getImportActions() {
        return new Action[0]; //TODO implement this
    }

    /**
     * @param category Lookup representing palette's category.
     *
     * @return Custom actions to be added to the top of default popup menu for the given category.
     */
    public Action[] getCustomCategoryActions(Lookup category) {
        return new Action[0]; //TODO implement this
    }

    /**
     * @param item Lookup representing palette's item.
     *
     * @return Custom actions to be added to the top of the default popup menu for the given palette item.
     */
    public Action[] getCustomItemActions(Lookup item) {
        return new Action[0]; //TODO implement this
    }

    /**
     * @return Custom actions to be added to the top of palette's default popup menu.
     */
    public Action[] getCustomPaletteActions() {
        return new Action[0]; //TODO implement this
    }

    /**
     * @param item Lookup representing palette's item.
     *
     * @return An action to be invoked when user double-clicks the item in the
     * palette (e.g. insert item at editor's default location).
     * Return null to disable preferred action for this item.
     */
    public Action getPreferredAction( Lookup item ) {
        return null;
    }
}