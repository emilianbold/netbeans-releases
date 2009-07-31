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
import org.netbeans.modules.vmd.api.screen.actions.DesignerEditAction;
import org.netbeans.modules.vmd.api.screen.actions.DesignerEditParentAction;
import org.netbeans.modules.vmd.midp.codegen.ui.InstanceRenameAction;
import org.openide.actions.PropertiesAction;
import org.openide.util.actions.SystemAction;

/**
 * @author David Kaspar
 */
public final class MidpActionsSupport {
    
    public static void addCommonActionsPresenters(List<Presenter> presenters,
            boolean allowEdit, boolean allowGoToSource,
            boolean allowRename, boolean allowDelete,
            boolean allowProperties)
    {
        if (allowEdit)
            presenters.add(ActionsPresenter.create(20,SystemAction.get(DesignerEditAction.class)));
        if (allowGoToSource)
            presenters.add(ActionsPresenter.create(20, SystemAction.get(GoToSourceAction.class)));
        if (allowRename)
            presenters.add(ActionsPresenter.create(30, SystemAction.get(RenameAction.class)));
        if (allowDelete)
            presenters.add(ActionsPresenter.create(40, SystemAction.get(DeleteAction.class)));
        if (allowProperties)
            presenters.add(ActionsPresenter.create(60, SystemAction.get(PropertiesAction.class)));
         
    }

    public static void addCommonClassActionsPresenters(List<Presenter> presenters, 
            boolean allowEdit,boolean allowGoToSource, boolean allowRename,
            boolean allowDelete, boolean allowProperties)
    {
        if (allowEdit)
            presenters.add(ActionsPresenter.create(20,SystemAction.get(DesignerEditAction.class)));
        if (allowGoToSource)
            presenters.add(ActionsPresenter.create(20, SystemAction.get(GoToSourceAction.class)));
        if (allowRename)
            presenters.add(ActionsPresenter.create(30, SystemAction.get(InstanceRenameAction.class)));
        if (allowDelete)
            presenters.add(ActionsPresenter.create(40, SystemAction.get(DeleteAction.class)));
        if (allowProperties)
            presenters.add(ActionsPresenter.create(60, SystemAction.get(PropertiesAction.class)));
    }
    
    public static void addCommonActionsPresentersParentEditAction(List<Presenter> presenters, boolean allowEdit, boolean allowGoToSource, boolean allowRename, boolean allowDelete, boolean allowProperties) {
        if (allowEdit)
            presenters.add(ActionsPresenter.create(20,SystemAction.get(DesignerEditParentAction.class)));
        if (allowGoToSource)
            presenters.add(ActionsPresenter.create(20, SystemAction.get(GoToSourceAction.class)));
        if (allowRename)
            //presenters.add(ActionsPresenter.create(30, SystemAction.get(RenameAction.class)));
            presenters.add(ActionsPresenter.create(30, SystemAction.get(InstanceRenameAction.class)));
        if (allowDelete)
            presenters.add(ActionsPresenter.create(40, SystemAction.get(DeleteAction.class)));
        if (allowProperties)
            presenters.add(ActionsPresenter.create(60, SystemAction.get(PropertiesAction.class)));
    }
    
    public static void addMoveActionPresenter(List<Presenter> presenters, String propertyName) {
        presenters.add(ActionsPresenter.create(50, MoveAction.createMoveUpAction(propertyName), MoveAction.createMoveDownAction(propertyName)));
    }
    
    public static void addNewActionPresenter(List<Presenter> presenters, TypeID... types) {
        presenters.add(ActionsPresenter.create(10, AddAction.getInstance()));
        presenters.add(AddActionPresenter.create(AddActionPresenter.ADD_ACTION, 10, types));
    }
    
    public static void addUnusedCommandsAddActionForDisplayable(List<Presenter> presenters) {
        presenters.add(UnusedCommandsAddActionPresenter.createForDisplayable(UnusedCommandsAddActionPresenter.DISPLAY_NAME_ADD, 20));
    }
    
    public static void addUnusedCommandsAddActionForItem(List<Presenter> presenters) {
        presenters.add(UnusedCommandsAddActionPresenter.createForItem(UnusedCommandsAddActionPresenter.DISPLAY_NAME_ADD, 20));
    }
    
    public static void addPropertiesActionPresenter(List<Presenter> presenters) {
        presenters.add(ActionsPresenter.create(Integer.MAX_VALUE, SystemAction.get(PropertiesAction.class)));
    }
    
}
