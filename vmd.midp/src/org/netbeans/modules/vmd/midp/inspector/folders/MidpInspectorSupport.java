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

package org.netbeans.modules.vmd.midp.inspector.folders;

import org.netbeans.modules.vmd.api.inspector.InspectorFolderCategoryPresenter;
import org.netbeans.modules.vmd.api.inspector.InspectorOrderingController;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

import java.awt.*;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Karol Harezlak
 */
public final class MidpInspectorSupport {

    // TODO - these TypeIDs are (and should) not valid and not registered in registry - why they are defined here
    // HINT - do not use it anywhere, if possible
    public static final TypeID TYPEID_ELEMENTS = new TypeID(TypeID.Kind.PRIMITIVE, "#CategoryElements");  //NOI18N
    public static final TypeID TYPEID_COMMANDS = new TypeID(TypeID.Kind.PRIMITIVE, "#CategoryCommands");  //NOI18N
    
    private static final String ICON_PATH_COMMANDS = "org/netbeans/modules/vmd/midp/resources/components/category_commands_16.png"; // NOI18N
    private static final String ICON_PATH_ELEMENTS = "org/netbeans/modules/vmd/midp/resources/components/category_elements_16.png"; // NOI18N
    private static final Image ICON_COMMANDS = Utilities.loadImage(ICON_PATH_COMMANDS);
    private static final Image ICON_ELEMENTS = Utilities.loadImage(ICON_PATH_ELEMENTS);
    
    
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
