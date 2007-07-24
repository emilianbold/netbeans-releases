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
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

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
        for (Presenter presenter : presenters.toArray(new Presenter[presenters.size()])) {
            if (presenter instanceof ActionsPresenter)
                presenters.remove(presenter);
             if (presenter instanceof ActionsPresenter)
                presenters.remove(presenter);
        }
        MidpActionsSupport.addCommonActionsPresenters(presenters, true, true, true, true, false);
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
            String name = (String) refComponent.readProperty(ClassCD.PROP_INSTANCE_NAME).getPrimitiveValue ();
            
            return name;
        }

        public void setEditableName (DesignComponent component, String enteredName) {
            DesignComponent refComponent = component.readProperty(PROP_COMMAND).getComponent();
            PropertyValue newName = InstanceNameResolver.createFromSuggested(refComponent, enteredName);
            refComponent.writeProperty(ClassCD.PROP_INSTANCE_NAME, newName);
        }

        public Image getIcon (DesignComponent component, InfoPresenter.IconType iconType) {
            return Utilities.loadImage (CommandCD.ICON_PATH);
        }

    }

}
