/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.terminal.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.netbeans.modules.dlight.terminal.ui.TerminalContainerTopComponent;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.actions.Presenter;
import org.openide.windows.IOContainer;
import org.openide.windows.IOProvider;

/**
 *
 */
public abstract class TerminalAction extends AbstractAction implements Presenter.Toolbar {
    
    public static final String TERMINAL_ACTIONS_PATH = "Terminal/Actions"; // NOI18N

    public TerminalAction(String name, String descr, ImageIcon icon) {
        putValue(Action.NAME, name);
        putValue(Action.SHORT_DESCRIPTION, descr);
        putValue(Action.SMALL_ICON, icon);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final TerminalContainerTopComponent instance = TerminalContainerTopComponent.findInstance();
        instance.open();
        instance.requestActive();
        final IOContainer ioContainer = instance.getIOContainer();
        final IOProvider term = IOProvider.get("Terminal"); // NOI18N
        if (term != null) {
            final ExecutionEnvironment env = getEnvironment();
            if (env != null) {
                TerminalSupportImpl.openTerminalImpl(ioContainer, env.getDisplayName(), env, null, TerminalContainerTopComponent.SILENT_MODE_COMMAND.equals(e.getActionCommand()), true, 0);
            }
        }
    }

    @Override
    public Component getToolbarPresenter() {
        return TerminalSupportImpl.getToolbarPresenter(this);
    }

    protected abstract ExecutionEnvironment getEnvironment();
}
