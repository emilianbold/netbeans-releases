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
package org.netbeans.modules.vmd.midp.actions;


import java.util.List;

import org.netbeans.modules.vmd.api.inspector.common.RenameAction;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.presenters.actions.MoveAction;
import org.netbeans.modules.vmd.api.model.presenters.actions.ActionsPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.AddAction;
import org.netbeans.modules.vmd.api.model.presenters.actions.AddActionPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeleteAction;
import org.netbeans.modules.vmd.api.properties.common.PropertiesAction;

import org.openide.util.actions.SystemAction;

/**
 * @author David Kaspar
 */
public final class MidpActionsSupport {

    public static void addCommonActionsPresenters (List<Presenter> presenters, boolean allowEdit, boolean allowGoToSource, boolean allowRename, boolean allowDelete, boolean allowProperties) {
        if (allowEdit)
            presenters.add (ActionsPresenter.create (20, SystemAction.get(EditAction.class)));
        if (allowGoToSource)
            presenters.add (ActionsPresenter.create (20, SystemAction.get(GoToSourceAction.class)));
        if (allowRename)
            presenters.add (ActionsPresenter.create (30, SystemAction.get(RenameAction.class)));
        if (allowDelete)
            presenters.add (ActionsPresenter.create (40, SystemAction.get(DeleteAction.class)));
        if (allowProperties)
            presenters.add (ActionsPresenter.create (60, SystemAction.get(PropertiesAction.class)));
    }
    
    public static void addMoveActionPresenter(List<Presenter> presenters, String propertyName) {
            presenters.add(ActionsPresenter.create (50, MoveAction.createMoveUpAction(propertyName), MoveAction.createMoveDownAction(propertyName)));
    }

    public static void addNewActionPresenter(List<Presenter> presenters, TypeID ... types) {
       presenters.add(ActionsPresenter.create(10, AddAction.getInstance()));
       presenters.add(AddActionPresenter.create(AddActionPresenter.ADD_ACTION, 10, types));
    }
    
    public static void addUnusedCommandsAddActionForDisplayable(List<Presenter> presenters) {
        presenters.add(UnusedCommandsAddActionPresenter.createForDisplayable(UnusedCommandsAddActionPresenter.DISPLAY_NAME_ADD, 20));
    }
    
     public static void addUnusedCommandsAddActionForItem(List<Presenter> presenters) {
        presenters.add(UnusedCommandsAddActionPresenter.createForItem(UnusedCommandsAddActionPresenter.DISPLAY_NAME_ADD, 20));
    }
}
