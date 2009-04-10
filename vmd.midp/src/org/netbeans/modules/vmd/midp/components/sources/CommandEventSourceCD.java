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

import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeleteDependencyPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeletePresenter;
import org.netbeans.modules.vmd.api.model.support.ArraySupport;
import org.netbeans.modules.vmd.api.properties.PropertiesPresenterForwarder;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.midp.actions.GoToSourcePresenter;
import org.netbeans.modules.vmd.midp.actions.SecondaryGoToSourcePresenter;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.commands.CommandCD;
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCD;
import org.netbeans.modules.vmd.midp.components.listeners.CommandListenerCD;
import org.netbeans.modules.vmd.midp.flow.FlowDisplayableCommandPinOrderPresenter;
import org.netbeans.modules.vmd.midp.flow.FlowEventSourcePinPresenter;
import org.netbeans.modules.vmd.midp.screen.CommandEventSourceSRItemPresenter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.vmd.api.model.presenters.actions.ActionsPresenter;
import org.netbeans.modules.vmd.midp.actions.MidpActionsSupport;
import org.netbeans.modules.vmd.midp.codegen.ui.RenameCommandAction;
import org.openide.util.actions.SystemAction;


/**
 * @author David Kaspar
 */

public final class CommandEventSourceCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "#CommandEventSource"); // NOI18N

    // HINT this is redunance property, could be resolved from parent
    public static final String PROP_DISPLAYABLE = "displayable"; // NOI18N
    public static final String PROP_COMMAND = "command"; // NOI18N

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (EventSourceCD.TYPEID, CommandEventSourceCD.TYPEID, true, true);
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.MIDP;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return Arrays.asList(
            new PropertyDescriptor (CommandEventSourceCD.PROP_DISPLAYABLE, DisplayableCD.TYPEID, PropertyValue.createNull (), false, false, Versionable.FOREVER),
            new PropertyDescriptor (CommandEventSourceCD.PROP_COMMAND, CommandCD.TYPEID, PropertyValue.createNull (), false, false, Versionable.FOREVER)
        );
    }

    public static DesignComponent getCommandComponent (DesignComponent commandEventSourceComponent) {
        return commandEventSourceComponent.readProperty (PROP_COMMAND).getComponent ();
    }
    
    @Override
    protected void gatherPresenters(ArrayList<Presenter> presenters) {
        EventSourceSupport.addActionsPresentres(presenters, false);
        presenters.add(ActionsPresenter.create(30,
                SystemAction.get(RenameCommandAction.class)));
        super.gatherPresenters(presenters);
    }
    
    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList (
            // info
            InfoPresenter.create (EventSourceSupport.createCommandEventSourceInfoResolver ()),
            // general
            new GoToSourcePresenter() {
                protected boolean matches (GuardedSection section) {
                    DesignComponent listener = MidpDocumentSupport.getCommandListener (getComponent ().getDocument (), CommandListenerCD.TYPEID);
                    return MultiGuardedSection.matches (section, listener.getComponentID () + "-commandAction", getComponent ().getComponentID () + "-postAction"); // NOI18N
                }
            },
            SecondaryGoToSourcePresenter.createForwarder(PROP_COMMAND),
            // flow
            new CommandEventSourceFlowPinPresenter (),
            // properties
            PropertiesPresenterForwarder.createByReference(PROP_COMMAND),
            // delete
            DeleteDependencyPresenter.createDependentOnPropertyPresenter (PROP_DISPLAYABLE),
            DeleteDependencyPresenter.createDependentOnPropertyPresenter (PROP_COMMAND),
            new DeletePresenter () {
                protected void delete () {
                    DesignComponent component = getComponent ();
                    DesignComponent displayable = component.readProperty (PROP_DISPLAYABLE).getComponent ();
                    ArraySupport.remove (displayable, DisplayableCD.PROP_COMMANDS, component);
                }
            },
            // screen
            new CommandEventSourceSRItemPresenter ()
        );
    }

    
    public static class CommandEventSourceFlowPinPresenter extends FlowEventSourcePinPresenter {

        protected DesignComponent getComponentForAttachingPin () {
            return getComponent ().readProperty (PROP_DISPLAYABLE).getComponent ();
        }

        protected String getDisplayName () {
            DesignComponent command = getCommandComponent (getComponent ());
            if(command == null || command.getParentComponent() == null)
                return null;
            InfoPresenter presenter = command.getPresenter (InfoPresenter.class);
            return presenter.getDisplayName (InfoPresenter.NameType.PRIMARY);
        }

        protected String getOrder () {
            return FlowDisplayableCommandPinOrderPresenter.CATEGORY_ID;
        }

        @Override
        protected boolean canRename () {
            DesignComponent command = getCommandComponent (getComponent ());
            if (command == null)
                return false;
            InfoPresenter presenter = command.getPresenter (InfoPresenter.class);
            return presenter.isEditable ();
        }

        @Override
        protected String getRenameName () {
            DesignComponent command = getCommandComponent (getComponent ());
            InfoPresenter presenter = command.getPresenter (InfoPresenter.class);
            return presenter.getEditableName ();
        }

        @Override
        protected void setRenameName (String name) {
            DesignComponent command = getCommandComponent (getComponent ());
            InfoPresenter presenter = command.getPresenter (InfoPresenter.class);
            presenter.setEditableName (name);
        }

        @Override
        protected DesignEventFilter getEventFilter () {
            return super.getEventFilter ().addDescentFilter (getComponent (), PROP_COMMAND).addDescentFilter (getComponent (), PROP_DISPLAYABLE);
        }

    }

}
