/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.vmd.midp.components.sources;

import java.awt.Image;
import java.util.List;

import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignEventFilter;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.ActionsPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.ActionsSupport;
import org.netbeans.modules.vmd.midp.actions.MidpActionsSupport;
import org.netbeans.modules.vmd.midp.codegen.InstanceNameResolver;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.commands.CommandCD;
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCD;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;
import org.openide.actions.PropertiesAction;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * @author David Kaspar
 */
public final class EventSourceSupport {

    private static final InfoPresenter.Resolver COMMAND_EVENT_SOURCE_RESOLVER = new EventSourceResolver (CommandEventSourceCD.PROP_COMMAND, true);
    private static final InfoPresenter.Resolver ITEM_COMMAND_EVENT_SOURCE_RESOLVER = new EventSourceResolver (ItemCommandEventSourceCD.PROP_COMMAND, true);
    private static final String PROP_COMMAND = "command"; //NOI18N
    
    static InfoPresenter.Resolver createCommandEventSourceInfoResolver () {
        return COMMAND_EVENT_SOURCE_RESOLVER;
    }

    static InfoPresenter.Resolver createItemCommandEventSourceInfoResolver () {
        return ITEM_COMMAND_EVENT_SOURCE_RESOLVER;
    }
    
    static void addActionsPresentres(List<Presenter> presenters) {
        addActionsPresentres(presenters, true);
    }

    static void addActionsPresentres(List<Presenter> presenters, boolean allowRename)
    {
        for (Presenter presenter : presenters.toArray(new Presenter[presenters.size()])) {
            if (presenter instanceof ActionsPresenter)
                presenters.remove(presenter);
             if (presenter instanceof ActionsPresenter)
                presenters.remove(presenter);
        }
        MidpActionsSupport.addCommonActionsPresenters(presenters, true,
                    true, allowRename, true, false);
        MidpActionsSupport.addMoveActionPresenter(presenters, DisplayableCD.PROP_COMMANDS);
        presenters.addAll(ActionsSupport.createByReference(PROP_COMMAND, PropertiesAction.class)); //NOI18N
    }
    
    private static class EventSourceResolver implements InfoPresenter.Resolver {

        private String propertyName;
        private boolean editable;

        private EventSourceResolver (String propertyName, boolean editable) {
            this.propertyName = propertyName;
            this.editable = editable;
        }

        public DesignEventFilter getEventFilter (DesignComponent component) {
            return new DesignEventFilter ().addDescentFilter (component, propertyName);
        }

        public String getDisplayName (DesignComponent component, InfoPresenter.NameType nameType) {
            switch (nameType) {
                case PRIMARY:
                    return resolveName (component);
                case SECONDARY:
                    return NbBundle.getMessage(EventSourceSupport.class, "TYPE_Command"); // NOI18N
                case TERTIARY:
                    StringBuffer nameWithParent = new StringBuffer();
                    nameWithParent.append("<HTML>"); //NOI18N
                    nameWithParent.append(resolveName(component));
                    nameWithParent.append(" <font color=\"#808080\">["); // NOI18N
                    nameWithParent.append(component.getParentComponent().getPresenter(InfoPresenter.class).getEditableName());
                    nameWithParent.append("]"); // NOI18N
                    return nameWithParent.toString();
                default:
                    throw Debug.illegalState ();
            }
        }

        private String resolveName (DesignComponent component) {
            component = component.readProperty (propertyName).getComponent ();
            if (component == null)
                return null;

            PropertyValue value = component.readProperty (ClassCD.PROP_INSTANCE_NAME);
            if (value.getKind () == PropertyValue.Kind.VALUE && MidpTypes.TYPEID_JAVA_LANG_STRING.equals (value.getType ()))
                return (String) value.getPrimitiveValue ();
            throw Debug.error ("Invalid instance name", value, "for component", component); // NOI18N
        }

        public boolean isEditable (DesignComponent component) {
            return editable;
        }

        public String getEditableName (DesignComponent component) {
            DesignComponent refComponent = component.readProperty(PROP_COMMAND).getComponent();
            if (refComponent == null || refComponent.readProperty(ClassCD.PROP_INSTANCE_NAME) == null) {
                Debug.warning("EventSource referenced " + component.toString() + " is null "); //NOI18N
                return null;
            }
            
            String name = (String) refComponent.readProperty(ClassCD.PROP_INSTANCE_NAME).getPrimitiveValue ();
            
            return name;
        }

        public void setEditableName (DesignComponent component, String enteredName) {
            DesignComponent refComponent = component.readProperty(PROP_COMMAND).getComponent();
            PropertyValue newName = InstanceNameResolver.createFromSuggested(refComponent, enteredName);
            refComponent.writeProperty(ClassCD.PROP_INSTANCE_NAME, newName);
        }

        public Image getIcon (DesignComponent component, InfoPresenter.IconType iconType) {
            return ImageUtilities.loadImage (CommandCD.ICON_PATH);
        }

    }

}
