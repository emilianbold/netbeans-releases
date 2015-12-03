/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.mixeddev.ui;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.modules.cnd.mixeddev.MixedDevUtils;
import org.netbeans.modules.cnd.mixeddev.wizard.GenerateProjectAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Petr Kudryavtsev <petrk@netbeans.org>
 */
@ActionID(id = "org.netbeans.modules.cnd.mixeddev.ui.MixedDevToolsMetaAction", category = "MixedDevelopment")
@ActionRegistration(displayName = "unused-name", lazy = false)
@ActionReferences(value = {
    @ActionReference(path = "UI/ToolActions")
})
public class MixedDevToolsMetaAction extends SystemAction implements ContextAwareAction {
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
        return new HelpCtx("MixedDevelopment"); // NOI18N
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        JMenu menu = new JMenu();
        menu.setText(NbBundle.getMessage(MixedDevUtils.class, "Editors/text/x-java/Popup/MixedDevelopment")); // NOI18N
        menu.add(GenerateProjectAction.INSTANCE);
        return new MenuWrapperAction(menu);
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
