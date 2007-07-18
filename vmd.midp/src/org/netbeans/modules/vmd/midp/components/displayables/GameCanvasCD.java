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
 *
 */
package org.netbeans.modules.vmd.midp.components.displayables;

import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;

import java.util.List;

/**
 * @author David Kaspar
 */

public final class GameCanvasCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "javax.microedition.lcdui.game.GameCanvas"); // NOI18N

//    public static final String ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/game_canvas_16.png"; // NOI18N

    public static final Integer VALUE_LEFT_PRESSED = (1 << CanvasCD.VALUE_LEFT);
    public static final Integer VALUE_RIGHT_PRESSED = (1 << CanvasCD.VALUE_RIGHT);
    public static final Integer VALUE_UP_PRESSED = (1 << CanvasCD.VALUE_UP);
    public static final Integer VALUE_DOWN_PRESSED = (1 << CanvasCD.VALUE_DOWN);
    public static final Integer VALUE_FIRE_PRESSED = (1 << CanvasCD.VALUE_FIRE);
    public static final Integer VALUE_GAME_A_PRESSED = (1 << CanvasCD.VALUE_GAME_A);
    public static final Integer VALUE_GAME_B_PRESSED = (1 << CanvasCD.VALUE_GAME_B);
    public static final Integer VALUE_GAME_C_PRESSED = (1 << CanvasCD.VALUE_GAME_C);
    public static final Integer VALUE_GAME_D_PRESSED = (1 << CanvasCD.VALUE_GAME_D);


//    static {
//        MidpTypes.registerIconResource (TYPEID, ICON_PATH);
//    }

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (CanvasCD.TYPEID, TYPEID, false, true);
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.MIDP_2;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return null;
    }

    protected List<? extends Presenter> createPresenters () {
        return null;
    }

}
