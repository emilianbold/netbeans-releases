/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
                return "<HTML>" + InfoPresenter.getHtmlDisplayName(component); // NOI18N
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

