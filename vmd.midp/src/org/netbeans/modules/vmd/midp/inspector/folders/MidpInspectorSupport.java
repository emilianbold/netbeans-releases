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

package org.netbeans.modules.vmd.midp.inspector.folders;

import org.netbeans.modules.vmd.api.inspector.InspectorFolderCategoryPresenter;
import org.netbeans.modules.vmd.api.inspector.InspectorOrderingController;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

import java.awt.*;
import java.util.Collections;
import java.util.List;

/**
 *
 * 
 */
public final class MidpInspectorSupport {

    // TODO - these TypeIDs are (and should) not valid and not registered in registry - why they are defined here
    // HINT - do not use it anywhere, if possible
    public static final TypeID TYPEID_ELEMENTS = new TypeID(TypeID.Kind.PRIMITIVE, "#CategoryElements");  //NOI18N
    public static final TypeID TYPEID_COMMANDS = new TypeID(TypeID.Kind.PRIMITIVE, "#CategoryCommands");  //NOI18N
    
    private static final String ICON_PATH_COMMANDS = "org/netbeans/modules/vmd/midp/resources/components/category_commands_16.png"; // NOI18N
    private static final String ICON_PATH_ELEMENTS = "org/netbeans/modules/vmd/midp/resources/components/category_elements_16.png"; // NOI18N
    private static final Image ICON_COMMANDS = ImageUtilities.loadImage(ICON_PATH_COMMANDS);
    private static final Image ICON_ELEMENTS = ImageUtilities.loadImage(ICON_PATH_ELEMENTS);
    
    
    //Ordering inside of category by array property defined by propertyName
    public static Presenter createComponentCategory(String categoryName, TypeID folderTypeID, Image icon, List<InspectorOrderingController> orderingControllers, TypeID parentTypeID, TypeID... filtersTypeID) {
        
        return new InspectorFolderCategoryPresenter(categoryName, 
                                               folderTypeID,
                                               icon,
                                               filtersTypeID,
                                               parentTypeID,
                                               orderingControllers.toArray(new InspectorOrderingController[orderingControllers.size()]));
    } 
    
    //Default Ordering inside
    public static Presenter createComponentCommandsCategory(TypeID ... filtersTypeID) {
        
        return createComponentCategory(NbBundle.getMessage(MidpInspectorSupport.class, "DISP_InspectorCategory_Assigned_Commands"), // NOI18N
                                       TYPEID_COMMANDS,
                                       ICON_ELEMENTS,
                                       Collections.<InspectorOrderingController>emptyList (),
                                       null,
                                       filtersTypeID); 
    }
    //Ordering inside of category by array property defined by propertyName
    public static Presenter createComponentCommandsCategory(List<InspectorOrderingController> orderingControllers, TypeID... filtersTypeID) {
        
        return createComponentCategory(NbBundle.getMessage(MidpInspectorSupport.class, "DISP_InspectorCategory_Assigned_Commands"), // NOI18N
                                       TYPEID_COMMANDS,
                                       ICON_COMMANDS,
                                       orderingControllers,
                                       null,
                                       filtersTypeID); 
    }
    //Ordering inside of category by array property defined by propertyName, with given parentTypeID category would not be visible when 
    // parent compoennt TypeID is the same like given parent TypeID
    public static Presenter createSpecialComponentCommandCategory(List<InspectorOrderingController> orderingControllers, TypeID parentTypeID, TypeID... filtersTypeID) {
        
        return createComponentCategory(NbBundle.getMessage(MidpInspectorSupport.class, "DISP_InspectorCategory_Assigned_Commands"), // NOI18N
                                       TYPEID_COMMANDS,
                                       ICON_COMMANDS,
                                       orderingControllers,
                                       parentTypeID,
                                       filtersTypeID); 
    }
    
    
    //Default Sorting
    public static Presenter createComponentElementsCategory(String displayName, TypeID... filtersTypeID) {
        
        return createComponentCategory(displayName,
                                       TYPEID_ELEMENTS,
                                       ICON_ELEMENTS,
                                       Collections.<InspectorOrderingController>emptyList (),
                                       null,
                                       filtersTypeID); 
    }
    
    //Ordering inside of category by array property defined by propertyName
    public static Presenter createComponentElementsCategory(String displayName, List<InspectorOrderingController> orderingControllers, TypeID... filtersTypeID) {
        
        return createComponentCategory( displayName, 
                                       TYPEID_ELEMENTS,
                                       ICON_ELEMENTS,
                                       orderingControllers,
                                       null,
                                       filtersTypeID); 
    }

}
