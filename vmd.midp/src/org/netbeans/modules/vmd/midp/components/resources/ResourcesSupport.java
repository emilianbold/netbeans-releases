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
package org.netbeans.modules.vmd.midp.components.resources;

import org.netbeans.modules.vmd.api.model.ComponentDescriptor;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignEventFilter;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter.Resolver;
import org.netbeans.modules.vmd.midp.codegen.InstanceNameResolver;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;
import org.netbeans.modules.vmd.midp.components.general.ClassSupport;

import java.awt.*;


/**
 * @author Karol Harezlak
 */
public final class ResourcesSupport {
    
    private static final Resolver RESOURCE_RESOLVER = new ResourceResolver();
    
    public static InfoPresenter createResourceInfoResolver() {
        return InfoPresenter.create(RESOURCE_RESOLVER);
    }
    
    private ResourcesSupport() {
    }
    
    private static class ResourceResolver implements Resolver {
        
        public String getDisplayName(DesignComponent component, InfoPresenter.NameType nameType) {
            switch (nameType) {
            case PRIMARY:
                return ClassSupport.resolveDisplayName(component);
            case SECONDARY:
                return MidpTypes.getSimpleClassName(component.getType());
            case TERTIARY:
                return "<HTML>" + InfoPresenter.getHtmlDisplayName(component);
            default:
                throw new IllegalStateException();
            }
        }
        
        public DesignEventFilter getEventFilter(DesignComponent component) {
            return new DesignEventFilter().addComponentFilter(component, false);
        }
        
        public boolean isEditable(DesignComponent component) {
            return true;
        }
        
        public String getEditableName(DesignComponent component) {
            if (component == null)
                throw Debug.error ("Component cannot be null"); // NOI18N
            return (String) component.readProperty(ClassCD.PROP_INSTANCE_NAME).getPrimitiveValue();
        }
        
        public void setEditableName(DesignComponent component, String name) {
            if (component == null || name == null)
                throw Debug.error ("Component or name cannot be null"); // NOI18N
            component.writeProperty(ClassCD.PROP_INSTANCE_NAME, InstanceNameResolver.createFromSuggested(component, name));
        }
        
        public Image getIcon(DesignComponent component, InfoPresenter.IconType iconType) {
            if (InfoPresenter.IconType.COLOR_16x16.equals(iconType)) {
                ComponentDescriptor descriptor = component.getComponentDescriptor();
                while (descriptor != null) {
                    Image image = MidpTypes.getRegisteredIcon(descriptor.getTypeDescriptor().getThisType());
                    if (image != null)
                        return image;
                    descriptor = descriptor.getSuperDescriptor();
                }
            }
            return null;
        }
    }
    
}

