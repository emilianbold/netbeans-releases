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

package org.apache.tools.ant.module.run;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.tools.ant.module.AntModule;
import org.openide.awt.Actions;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;

/**
 * An action to run the last Ant build.
 * @see "#47925"
 */
public final class RunLastTargetAction extends AbstractAction implements ChangeListener, Presenter.Menu, Presenter.Toolbar {
    
    public RunLastTargetAction() {
        super(NbBundle.getMessage(RunLastTargetAction.class, "LBL_RunLastTargetAction_general")/*,
                new ImageIcon(Utilities.loadImage("org/apache/tools/ant/module/resources/AntIcon.gif", true))*/);
        LastTargetExecuted.addChangeListener(WeakListeners.change(this, LastTargetExecuted.class));
    }
    
    @Override
    public boolean isEnabled() {
        return LastTargetExecuted.getLastBuildScript() != null;
    }
    
    @Override
    public Object getValue(String key) {
        if (key.equals(Action.SHORT_DESCRIPTION)) {
            String display = LastTargetExecuted.getProcessDisplayName();
            if (display != null) {
                return NbBundle.getMessage(RunLastTargetAction.class, "TIP_RunLastTargetAction_specific", display);
            } else {
                return null;
            }
        } else {
            return super.getValue(key);
        }
    }
    
    public void actionPerformed(ActionEvent e) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    LastTargetExecuted.rerun();
                } catch (IOException ioe) {
                    AntModule.err.notify(ioe);
                }
            }
        });
    }

    public void stateChanged(ChangeEvent e) {
        firePropertyChange("enabled", null, Boolean.valueOf(isEnabled())); // NOI18N
        firePropertyChange(Action.SHORT_DESCRIPTION, null, null);
    }

    public JMenuItem getMenuPresenter() {
        class SpecialMenuItem extends JMenuItem implements DynamicMenuContent {
            public SpecialMenuItem() {
                super(RunLastTargetAction.this);
            }
            public JComponent[] getMenuPresenters() {
                String label;
                String display = LastTargetExecuted.getProcessDisplayName();
                if (display != null) {
                    label = NbBundle.getMessage(RunLastTargetAction.class, "LBL_RunLastTargetAction_specific", display);
                } else {
                    label = (String) getValue(Action.NAME);
                }
                Mnemonics.setLocalizedText(this, label);
                return new JComponent[] {this};
            }
            public JComponent[] synchMenuPresenters(JComponent[] items) {
                return getMenuPresenters();
            }
        }
        return new SpecialMenuItem();
    }

    public Component getToolbarPresenter() {
        JButton button = new JButton();
        Actions.connect(button, this);
        return button;
    }

}
