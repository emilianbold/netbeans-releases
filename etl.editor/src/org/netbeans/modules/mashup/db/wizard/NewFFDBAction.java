/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
 */
package org.netbeans.modules.mashup.db.wizard;

import java.awt.Component;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.mashup.tables.wizard.MashupTableWizardIterator;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author ks161616
 */
public class NewFFDBAction extends AbstractAction implements Presenter.Menu {

    private JMenuItem menuPresenter = null;
    private static transient final Logger mLogger = Logger.getLogger(NewFFDBAction.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    public static String nbBundle1 = mLoc.t("BUND256: Mashup Database");
    public NewFFDBAction() {
        super(nbBundle1.substring(15));
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
    }

    public JMenuItem getMenuPresenter() {
        if (menuPresenter == null) {
            menuPresenter = new MenuPresenter();
        }
        return menuPresenter;
    }

    private final class MenuPresenter extends JMenu implements DynamicMenuContent, MenuListener {

        public MenuPresenter() {
            super((String) getValue(Action.NAME));
            addMenuListener(this);
        }

        public JComponent[] synchMenuPresenters(javax.swing.JComponent[] items) {
            return getMenuPresenters();
        }

        public JComponent[] getMenuPresenters() {
            return new JComponent[]{this};
        }

        public void menuSelected(MenuEvent e) {
            getPopupMenu().removeAll();
            MashupTableWizardIterator.IS_PROJECT_CALL = false;
            JPopupMenu menu = Utilities.actionsToPopup(new Action[]{
                SystemAction.get(NewFlatfileDatabaseWizardAction.class),
                SystemAction.get(NewFlatfileTableAction.class),
                SystemAction.get(NewJDBCTableAction.class),
                SystemAction.get(FlatfileDBViewerAction.class)
            }, Utilities.actionsGlobalContext());
            while (menu.getComponentCount() > 0) {
                Component c = menu.getComponent(0);
                menu.remove(0);
                getPopupMenu().add(c);
            }
        }

        public void menuCanceled(MenuEvent e) {
        }

        public void menuDeselected(MenuEvent e) {
        }
    }
}
