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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.vmd.midpnb.components.svg.form;

import org.netbeans.modules.vmd.api.inspector.InspectorOrderingController;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.vmd.api.inspector.InspectorFolder;
import org.netbeans.modules.vmd.api.inspector.InspectorFolderPath;
import org.netbeans.modules.vmd.api.inspector.InspectorFolderPresenter;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignEvent;
import org.netbeans.modules.vmd.api.model.DesignEventFilter;
import org.netbeans.modules.vmd.api.model.PresenterEvent;
import org.netbeans.modules.vmd.api.model.presenters.actions.AddAction;

/**
 *
 * @author Karol Harezlak
 */
public final class MidpInspectorSVGButtonSupport {

    // TODO - these TypeIDs are (and should) not valid and not registered in registry - why they are defined here
    // HINT - do not use it anywhere, if possible
    public static final TypeID TYPEID_CATEGORY_SVG_BUTTONS = new TypeID(TypeID.Kind.PRIMITIVE, "#CategorySVGButtons");  //NOI18N
    
    private static final String ICON_PATH_CATEGORY = "org/netbeans/modules/vmd/midp/resources/components/category_elements_16.png"; // NOI18N                                         
    private static final Image ICON_CATEGORY = ImageUtilities.loadImage(ICON_PATH_CATEGORY);
    
    
    
    //Ordering inside of category by array property defined by propertyName
    private static Presenter createComponentCategory(String categoryName, TypeID folderTypeID, Image icon, List<InspectorOrderingController> orderingControllers, TypeID parentTypeID, TypeID... filtersTypeID) {
        
        return new InspectorSVGButtonFolderCategoryPresenter(categoryName, 
                                               folderTypeID,
                                               icon,
                                               filtersTypeID,
                                               parentTypeID,
                                               orderingControllers.toArray(new InspectorOrderingController[orderingControllers.size()]));
    } 
    
    //Default Ordering inside
    public static Presenter createCategory() {
        
        return createComponentCategory(NbBundle.getMessage(MidpInspectorSVGButtonSupport.class, "DISP_FlowCategory_SVGButtons"), // NOI18N
                                       TYPEID_CATEGORY_SVG_BUTTONS,
                                       ICON_CATEGORY,
                                       Collections.<InspectorOrderingController>emptyList (),
                                       null,
                                       SVGButtonCD.TYPEID); 
    }
    
}
