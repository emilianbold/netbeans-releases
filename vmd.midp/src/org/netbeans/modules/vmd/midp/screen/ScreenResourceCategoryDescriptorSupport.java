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
import org.openide.util.Utilities;
import org.openide.util.NbBundle;

/**
 * @author breh
 */
public class ScreenResourceCategoryDescriptorSupport {

    public static ScreenResourceCategoryDescriptor ASSIGNED_COMMANDS = new ScreenResourceCategoryDescriptor (
            NbBundle.getMessage (ScreenResourceCategoryDescriptorSupport.class, "DISP_AssignedCommands"), // NOI18N
            Utilities.loadImage ("org/netbeans/modules/vmd/midp/resources/components/command_16.png"), // NOI18N
            NbBundle.getMessage (ScreenResourceCategoryDescriptorSupport.class, "TTIP_AssignedCommands"), // NOI18N
            100,
            ScreenResourceOrderingController.getArrayOrdering(DisplayableCD.PROP_COMMANDS)
    );

    public static ScreenResourceCategoryDescriptor ASSIGNED_ITEM_COMMANDS = new ScreenResourceCategoryDescriptor (
            NbBundle.getMessage (ScreenResourceCategoryDescriptorSupport.class, "DISP_AssignedItemCommands"), // NOI18N
            Utilities.loadImage ("org/netbeans/modules/vmd/midp/resources/components/command_16.png"), // NOI18N
            NbBundle.getMessage (ScreenResourceCategoryDescriptorSupport.class, "TTIP_AssignedItemCommands"), // NOI18N
            200,
            ScreenResourceOrderingController.getDefaultOrdering()
    );

    public static ScreenResourceCategoryDescriptor OTHER_DESIGN_RESOURCES = new ScreenResourceCategoryDescriptor (
            NbBundle.getMessage (ScreenResourceCategoryDescriptorSupport.class, "DISP_Resources"), // NOI18N
            Utilities.loadImage ("org/netbeans/modules/vmd/midp/resources/components/resource_16.png"), // NOI18N
            NbBundle.getMessage (ScreenResourceCategoryDescriptorSupport.class, "TTIP_Resources"), // NOI18N
            300,
            ScreenResourceOrderingController.getDefaultOrdering()
    );
    
}
