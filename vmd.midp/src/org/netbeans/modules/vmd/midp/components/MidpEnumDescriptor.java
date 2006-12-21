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
package org.netbeans.modules.vmd.midp.components;

import org.netbeans.modules.vmd.api.model.EnumDescriptor;
import org.netbeans.modules.vmd.api.model.EnumDescriptorFactory;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author David Kaspar
 */
// HINT - after making change, update MidpCodeSupport too
public final class MidpEnumDescriptor implements EnumDescriptorFactory {

    static final MidpEnumDescriptor.AlertTypePD alertTypePD = new MidpEnumDescriptor.AlertTypePD ();

    public String getProjectType () {
        return MidpDocumentSupport.PROJECT_TYPE_MIDP;
    }

    public EnumDescriptor getDescriptorForTypeIDString (String string) {
        if (MidpTypes.TYPEID_ALERT_TYPE.getString ().equals (string))
            return MidpEnumDescriptor.alertTypePD;
        //TODO
        return null;
    }

    private static class AlertTypePD implements EnumDescriptor {

        public String serialize (Object value) {
            return value.toString ();
        }

        public Object deserialize (String serialized) {
            return MidpTypes.AlertType.valueOf (serialized);
        }

        public boolean isValidInstance (Object object) {
            return object instanceof MidpTypes.AlertType;
        }

        public Collection<?> values () {
            return Arrays.asList (MidpTypes.AlertType.values ());
        }
    }

}
