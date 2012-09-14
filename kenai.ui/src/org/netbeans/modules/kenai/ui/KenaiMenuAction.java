/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.kenai.ui;

import java.awt.event.ActionEvent;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiManager;
import org.netbeans.modules.kenai.collab.chat.ChatTopComponent;
import org.netbeans.modules.kenai.collab.chat.WhoIsOnlineAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Actions;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.NbBundle;

public final class KenaiMenuAction extends AbstractAction implements DynamicMenuContent {
    private static final String CATEGORY_TEAM = "Team";
    private static KenaiMenuAction inst;

    @ActionID(category = "Team", id = "org.netbeans.modules.kenai.ui.KenaiMenuAction")
    @ActionRegistration(displayName = "#CTL_KenaiMenuAction", lazy = false)
    @ActionReference(path = "Menu/Versioning/Team", position = 220)
    public static synchronized KenaiMenuAction getDefault () {
        if (inst == null) {
            inst = new KenaiMenuAction();
        }
        return inst;
    }
    
    @Override
    public void actionPerformed (ActionEvent e) {
    }

    @Override
    public JComponent[] getMenuPresenters () {
        return new JComponent[0];
    }

    @Override
    public JComponent[] synchMenuPresenters (JComponent[] items) {
        JMenu kenaiMenu = new JMenu(getName());
        Action action = Actions.forID(CATEGORY_TEAM, NewKenaiProjectAction.ID);
        JMenuItem item = new JMenuItem();
        Actions.connect(item, action, false);
        kenaiMenu.add(item);

        action = Actions.forID(CATEGORY_TEAM, OpenKenaiProjectAction.ID);
        item = new JMenuItem();
        Actions.connect(item, action, false);
        kenaiMenu.add(item);

        action = Actions.forID(CATEGORY_TEAM, GetSourcesFromKenaiAction.ID);
        item = new JMenuItem();
        Actions.connect(item, action, false);
        kenaiMenu.add(item);

        kenaiMenu.add(new JSeparator());

        action = Actions.forID(CATEGORY_TEAM, ChatTopComponent.ACTION_ID);
        item = new JMenuItem();
        Actions.connect(item, action, false);
        kenaiMenu.add(item);

        action = Actions.forID(CATEGORY_TEAM, WhoIsOnlineAction.ID);
        item = new JMenuItem();
        Actions.connect(item, action, false);
        kenaiMenu.add(item);
        return new JComponent[] { kenaiMenu };
    }

    @NbBundle.Messages({
        "CTL_MenuName_java_net=java.net",
        "CTL_MenuName_other=Others"
    })
    private String getName () {
        String name;
        Collection<Kenai> kenais = KenaiManager.getDefault().getKenais();
        name = Bundle.CTL_MenuName_other();
        if (kenais.size() == 1 && kenais.iterator().next().getUrl().toString().startsWith("https://java.net")) { //NOI18N
            name = Bundle.CTL_MenuName_java_net();
        }
        return name;
    }

}
