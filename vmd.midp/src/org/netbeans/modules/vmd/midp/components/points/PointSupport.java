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
package org.netbeans.modules.vmd.midp.components.points;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignEventFilter;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.midp.codegen.InstanceNameResolver;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.openide.util.Utilities;
import org.openide.util.NbBundle;

import java.awt.*;

/**
 * @author David Kaspar
 */
public final class PointSupport {

    static InfoPresenter.Resolver createInfoResolver (final Image icon, final String propertyName, final String typeName) {
        return new InfoPresenter.Resolver() {
            public DesignEventFilter getEventFilter (DesignComponent component) {
                return new DesignEventFilter ().addComponentFilter (component, false);
            }

            public String getDisplayName (DesignComponent component, InfoPresenter.NameType nameType) {
                switch (nameType) {
                    case PRIMARY:
                        return getEditableName (component);
                    case SECONDARY:
                        return typeName;
                    case TERTIARY:
                        return null;
                    default:
                        throw new IllegalStateException ();
                }
            }

            public boolean isEditable (DesignComponent component) {
                return true;
            }

            public String getEditableName (DesignComponent component) {
                return MidpTypes.getString (component.readProperty (propertyName));
            }

            public void setEditableName (DesignComponent component, String enteredName) {
                component.writeProperty (propertyName, InstanceNameResolver.createFromSuggested (component, enteredName));
            }

            public Image getIcon (DesignComponent component, InfoPresenter.IconType iconType) {
                return icon;
            }
        };
    }

    static InfoPresenter.Resolver createCallPointInfoResolver () {
        return new InfoPresenter.Resolver() {
            public DesignEventFilter getEventFilter (DesignComponent component) {
                return new DesignEventFilter ().addComponentFilter (component, false);
            }

            public String getDisplayName (DesignComponent component, InfoPresenter.NameType nameType) {
                switch (nameType) {
                    case PRIMARY:
                        String code = MidpTypes.getJavaCode (component.readProperty (CallPointCD.PROP_ACCESS_CODE));
                        if (code.length () >= 7)
                            code = code.substring (0, 7) + "..."; // NOI18N
                        return code;
                    case SECONDARY:
                        return NbBundle.getMessage (PointSupport.class, "TYPE_Call"); // NOI18N
                    case TERTIARY:
                        return null;
                    default:
                        throw new IllegalStateException ();
                }
            }

            public boolean isEditable (DesignComponent component) {
                return false;
            }

            public String getEditableName (DesignComponent component) {
                throw Debug.illegalState ();
            }

            public void setEditableName (DesignComponent component, String enteredName) {
                throw Debug.illegalState ();
            }

            public Image getIcon (DesignComponent component, InfoPresenter.IconType iconType) {
                return Utilities.loadImage (CallPointCD.ICON_PATH);
            }
        };
    }

}
