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

import java.awt.Image;
import java.util.Collections;
import org.netbeans.modules.vmd.api.inspector.InspectorFolderPresenter;
import org.netbeans.modules.vmd.api.inspector.InspectorOrderingController;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.openide.util.Utilities;
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
    public static final Presenter createComponentCategory(String categoryName, TypeID folderTypeID, Image icon, List<InspectorOrderingController> orderingControllers, TypeID... filtersTypeID) {
        
        return InspectorFolderPresenter.create(categoryName, 
                                               folderTypeID,
                                               icon,
                                               filtersTypeID,
                                               orderingControllers.toArray(new InspectorOrderingController[orderingControllers.size()]));
    } 
    
    //Defauld Ordering inside
    public static final Presenter createComponentCommandsCategory(TypeID ... filtersTypeID) {
        
        return createComponentCategory("Assigned Commands",
                                       TYPEID_COMMANDS,
                                       ICON_ELEMENTS,
                                       Collections.EMPTY_LIST,
                                       filtersTypeID); 
    }
    //Ordering inside of category by array property defined by propertyName
    public static final Presenter createComponentCommandsCategory(List<InspectorOrderingController> orderingControllers, TypeID... filtersTypeID) {
        
        return createComponentCategory("Assigned Commands", 
                                       TYPEID_COMMANDS,
                                       ICON_COMMANDS,
                                       orderingControllers,
                                       filtersTypeID); 
    }
    
    //Default Sorting
    public static final Presenter createComponentElementsCategory(String displayName, TypeID... filtersTypeID) {
        
        return createComponentCategory(displayName,
                                       TYPEID_ELEMENTS,
                                       ICON_ELEMENTS,
                                       Collections.EMPTY_LIST,
                                       filtersTypeID); 
    }
    
    //Ordering inside of category by array property defined by propertyName
    public static final Presenter createComponentElementsCategory(String displayName, List<InspectorOrderingController> orderingControllers, TypeID... filtersTypeID) {
        
        return createComponentCategory( displayName, 
                                       TYPEID_ELEMENTS,
                                       ICON_ELEMENTS,
                                       orderingControllers,
                                       filtersTypeID); 
    }
    

}
