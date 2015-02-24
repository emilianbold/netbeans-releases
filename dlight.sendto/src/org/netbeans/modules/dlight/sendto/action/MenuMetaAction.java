/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.sendto.action;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;

/**
 * Meta action to be registered in Tools menu....
 * @author akrasny
 */
@ActionID(id = "org.netbeans.modules.dlight.sendto.action.MenuMetaAction", category = "Tools/SendTo")
@ActionRegistration(displayName = "#SendToMenuName", lazy = false)
@ActionReferences(value = {
    @ActionReference(path = "Editors/Popup", name = "sendToAction", position = 1917),
    @ActionReference(path = "Editors/TabActions", name = "sendToAction", position = 1917),
    @ActionReference(path = "UI/ToolActions", name = "sendToAction"/*, position = 1917*/)
})
public class MenuMetaAction extends SystemAction implements ContextAwareAction {

    @Override
    public boolean isEnabled() {
        // Do not show this item in Tools main menu
        // In other cases Context-aware instance will be created
        // TODO: how to make this correctly?
        return false;
    }

    @Override
    public String getName() {
        return "unused-name"; // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("sendto-execute"); // NOI18N
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new MenuWrapperAction(MenuConstructor.constructMenu(actionContext));
    }

    private static class MenuWrapperAction extends AbstractAction implements Presenter.Popup {

        private final JMenu menu;

        public MenuWrapperAction(JMenu menu) {
            this.menu = menu;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
        }

        @Override
        public JMenuItem getPopupPresenter() {
            return menu;
        }
    }
}
