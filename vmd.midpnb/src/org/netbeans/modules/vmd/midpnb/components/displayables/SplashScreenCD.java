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

package org.netbeans.modules.vmd.midpnb.components.displayables;

import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.vmd.api.model.ComponentDescriptor;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.PropertyDescriptor;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeDescriptor;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.VersionDescriptor;
import org.netbeans.modules.vmd.api.model.Versionable;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.displayables.CanvasCD;
import org.netbeans.modules.vmd.midp.components.resources.ImageCD;
import org.netbeans.modules.vmd.midp.components.sources.EventSourceCD;

/**
 *
 * @author Anton Chechel
 */
public class SplashScreenCD extends ComponentDescriptor {
    
    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "org.netbeans.modules.vmd.midpnb.components.displayables.SplashScreen"); // NOI18N

    public static final String ICON_PATH = "org/netbeans/modules/vmd/midpnb/resources/splash_screen16.png"; // NOI18N
    public static final String ICON_LARGE_PATH = "org/netbeans/modules/vmd/midpnb/resources/splash_screen64.png"; // NOI18N

    private String PROP_TIMEOUT = "Timeout"; //NOI18N
    private String PROP_ALLOW_TIMEOUT_INTERRUPT = "allowTimeoutInterrupt"; //NOI18N
    private String PROP_DISSMISS_ACTION = "dissmissAction"; //NOI18N
    private String PROP_IMAGE = "image"; 
    private String PROP_TEXT = "text";

    static {
        MidpTypes.registerIconResource(TYPEID, ICON_PATH);
    }

    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(CanvasCD.TYPEID, TYPEID, true, true);
    }

    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
            new PropertyDescriptor(PROP_TIMEOUT, MidpTypes.TYPEID_INT, PropertyValue.createNull(), false, true, MidpVersionable.MIDP),
            new PropertyDescriptor(PROP_ALLOW_TIMEOUT_INTERRUPT, MidpTypes.TYPEID_BOOLEAN, MidpTypes.createBooleanValue(Boolean.TRUE), false, false, Versionable.FOREVER),
            new PropertyDescriptor(PROP_DISSMISS_ACTION, EventSourceCD.TYPEID, PropertyValue.createNull(), false, false, Versionable.FOREVER),
            new PropertyDescriptor(PROP_IMAGE, ImageCD.TYPEID, PropertyValue.createNull(), true, true, MidpVersionable.MIDP),
            new PropertyDescriptor(PROP_TEXT, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull(), true, true, MidpVersionable.MIDP)

        );
    }
    
    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
        );
    }

}
