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
package org.netbeans.modules.vmd.midp.components.general;

import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.midp.codegen.InstanceNameResolver;
import org.netbeans.modules.vmd.midp.components.MidpTypes;

import java.awt.*;

/**
 * @author David Kaspar
 */
public class ClassSupport {

    private static final InfoPresenter.Resolver INFO_RESOLVER = new ClassInfoResolver();

    public static boolean isLazyInitialized (DesignComponent classComponent) {
        return MidpTypes.getBoolean (classComponent.readProperty (ClassCD.PROP_LAZY_INIT));
    }

    public static Presenter createInfoPresenter () {
        return InfoPresenter.create (INFO_RESOLVER);
    }

    private static class ClassInfoResolver implements InfoPresenter.Resolver {

        public String getDisplayName (DesignComponent component, InfoPresenter.NameType nameType) {
            switch (nameType) {
                case PRIMARY:
                    return resolveDisplayName (component);
                case SECONDARY:
                    return MidpTypes.getSimpleClassName (component.getType ());
                case TERTIARY:
                    return null;
                default:
                    throw new IllegalStateException ();
            }
        }

        public DesignEventFilter getEventFilter (DesignComponent component) {
            return new DesignEventFilter ().addComponentFilter (component, false);
        }

        public boolean isEditable (DesignComponent component) {
            return true;
        }

        public String getEditableName (DesignComponent component) {
            if (component == null)
                throw new IllegalArgumentException("Component cannot be null"); // NOI18N
            return (String) component.readProperty(ClassCD.PROP_INSTANCE_NAME).getPrimitiveValue ();
        }

        public void setEditableName (DesignComponent component, String name) {
             if (component == null || name == null)
                throw new IllegalArgumentException ("Component or name cannot be null"); // NOI18N
             component.writeProperty(ClassCD.PROP_INSTANCE_NAME, InstanceNameResolver.createFromSuggested(component, name));
        }

        public Image getIcon (DesignComponent component, InfoPresenter.IconType iconType) {
            if (InfoPresenter.IconType.COLOR_16x16.equals (iconType)) {
                ComponentDescriptor descriptor = component.getComponentDescriptor ();
                while (descriptor != null) {
                    Image image = MidpTypes.getRegisteredIcon (descriptor.getTypeDescriptor ().getThisType ());
                    if (image != null)
                        return image;
                    descriptor = descriptor.getSuperDescriptor ();
                }
            }
            return null;
        }

    }

    public static String resolveDisplayName (DesignComponent component) {
        PropertyValue value = component.readProperty (ClassCD.PROP_INSTANCE_NAME);
        if (value.getKind () == PropertyValue.Kind.VALUE && MidpTypes.TYPEID_JAVA_LANG_STRING.equals (value.getType ()))
            return (String) value.getPrimitiveValue ();
        throw Debug.illegalState ("Invalid instance name ", value, "for component", component); // NOI18N
    }

}
