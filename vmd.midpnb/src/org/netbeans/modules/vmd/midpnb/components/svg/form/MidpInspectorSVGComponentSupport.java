/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER. Copyright 1997-2007
 * Sun Microsystems, Inc. All rights reserved. The contents of this file are
 * subject to the terms of either the GNU General Public License Version 2 only
 * ("GPL") or the Common Development and Distribution License("CDDL")
 * (collectively, the "License"). You may not use this file except in compliance
 * with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP.
 * See the License for the specific language governing permissions and
 * limitations under the License. When distributing the software, include this
 * License Header Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this particular file as
 * subject to the "Classpath" exception as provided by Sun in the GPL Version 2
 * section of the License file that accompanied this code. If applicable, add
 * the following below the License Header, with the fields enclosed by brackets
 * [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]" Contributor(s): The
 * Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun Microsystems, Inc.
 * All Rights Reserved. If you wish your version of this file to be governed by
 * only the CDDL or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution under the
 * [CDDL or GPL Version 2] license." If you do not indicate a single choice of
 * license, a recipient has the option to distribute your version of this file
 * under either the CDDL, the GPL Version 2 or to extend the choice of license
 * to its licensees as provided above. However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright holder.
 */

package org.netbeans.modules.vmd.midpnb.components.svg.form;

import org.netbeans.modules.vmd.api.inspector.InspectorOrderingController;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

import java.awt.*;
import java.util.Collections;
import java.util.List;

/**
 * @author Karol Harezlak
 */
public final class MidpInspectorSVGComponentSupport {

    // TODO - these TypeIDs are (and should) not valid and not registered in
    // registry - why they are defined here
    // HINT - do not use it anywhere, if possible
    public static final TypeID TYPEID_CATEGORY_SVG_COMPONENTS = new TypeID(
            TypeID.Kind.PRIMITIVE, "#CategorySVGComponents"); // NOI18N

    private static final String ICON_PATH_CATEGORY = 
        "org/netbeans/modules/vmd/midp/resources/components/category_elements_16.png"; // NOI18N

    private static final Image ICON_CATEGORY = ImageUtilities
            .loadImage(ICON_PATH_CATEGORY);

    // Ordering inside of category by array property defined by propertyName
    private static Presenter createComponentCategory( String categoryName,
            TypeID folderTypeID, Image icon,
            List<InspectorOrderingController> orderingControllers,
            TypeID parentTypeID, TypeID... filtersTypeID )
    {

        return new InspectorSVGComponentFolderCategoryPresenter(
                categoryName,
                folderTypeID,
                icon,
                filtersTypeID,
                parentTypeID,
                orderingControllers.toArray(new InspectorOrderingController[orderingControllers.size()]));
    }

    // Default Ordering inside
    public static Presenter createCategory() {

        return createComponentCategory(
                NbBundle.getMessage(MidpInspectorSVGComponentSupport.class,
                        "DISP_InspectorCategory_SVGComponents"), // NOI18N
                TYPEID_CATEGORY_SVG_COMPONENTS, ICON_CATEGORY, Collections
                        .<InspectorOrderingController> emptyList(), null,
                SVGButtonCD.TYPEID, SVGCheckBoxCD.TYPEID, SVGRadioButtonCD.TYPEID,
                SVGSliderCD.TYPEID, SVGTextFieldCD.TYPEID, SVGLabelCD.TYPEID,
                SVGListCD.TYPEID, SVGComboBoxCD.TYPEID, SVGSpinnerCD.TYPEID);
    }

}
