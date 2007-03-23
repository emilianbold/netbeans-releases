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
package org.netbeans.modules.vmd.midp.screen;

import org.netbeans.modules.vmd.api.screen.resource.ScreenResourceCategoryDescriptor;
import org.netbeans.modules.vmd.api.screen.resource.ScreenResourceOrderingController;
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCD;
import org.netbeans.modules.vmd.midp.components.displayables.FormCD;
import org.openide.util.Utilities;

/**
 * @author breh
 */
public class ScreenResourceCategoryDescriptorSupport {
    //TODO Add tOrdering
    public static ScreenResourceCategoryDescriptor ASSIGNED_COMMANDS = new ScreenResourceCategoryDescriptor (
            "Assigned Commands",
            Utilities.loadImage ("org/netbeans/modules/vmd/midp/resources/components/command_16.png"),
            "Commands assigned to the edited displayable",
            -100,
            ScreenResourceOrderingController.getArrayOrdering(DisplayableCD.PROP_COMMANDS)
    );

    public static ScreenResourceCategoryDescriptor OTHER_DESIGN_RESOURCES = new ScreenResourceCategoryDescriptor (
            "Resources",
            Utilities.loadImage ("org/netbeans/modules/vmd/midp/resources/components/resource_16.png"),
            "Resources available in the design",
            100,
            ScreenResourceOrderingController.getDefaultOrdering()
    );
    
    public static ScreenResourceCategoryDescriptor ASSIGNED_ITEM_COMMANDS = new ScreenResourceCategoryDescriptor (
            "Assigned Item Commands",
            Utilities.loadImage ("org/netbeans/modules/vmd/midp/resources/components/command_16.png"),
            "Assigned Item Commands",
            50,
            ScreenResourceOrderingController.getArrayOrdering(FormCD.PROP_ITEMS)
    );
}
