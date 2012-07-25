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
package org.netbeans.modules.web.inspect.webkit.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

/**
 * WebKit-based CSS Styles view.
 *
 * @author Jan Stola
 */
public class CSSStylesPanel extends JPanel {
    /** Action command for switching to document view. */
    static final String DOCUMENT_ACTION_COMMAND = "document"; // NOI18N
    /** Action command for switching to selection view. */
    static final String SELECTION_ACTION_COMMAND = "selection"; // NOI18N
    /** The default instance of this class. */
    private static final CSSStylesPanel DEFAULT = new CSSStylesPanel();

    private CSSStylesPanel() {
        setLayout(new BorderLayout());
        add(createToolbar(), BorderLayout.PAGE_START);
    }

    private JToolBar createToolbar() {
        // The toolbar itself
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        // Button group for document and source buttons
        ButtonGroup buttonGroup = new ButtonGroup();

        // Document button
        JToggleButton documentButton = new JToggleButton("D");
        documentButton.setActionCommand(DOCUMENT_ACTION_COMMAND);
        initToolbarButton(documentButton, toolBar, buttonGroup);

        // Selection button
        JToggleButton selectionButton = new JToggleButton("S");
        selectionButton.setActionCommand(SELECTION_ACTION_COMMAND);
        initToolbarButton(selectionButton, toolBar, buttonGroup);

        return toolBar;
    }

    private void initToolbarButton(AbstractButton button, JToolBar toolBar, ButtonGroup buttonGroup) {
        button.setFocusPainted(false);
        button.addActionListener(getListener());
        buttonGroup.add(button);
        toolBar.add(button);
    }

    public static CSSStylesPanel getDefault() {
        return DEFAULT;
    }

    private Listener listener;
    private Listener getListener() {
        if (listener == null) {
            listener = new Listener();
        }
        return listener;
    }

    class Listener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (DOCUMENT_ACTION_COMMAND.equals(command)) {
                // PENDING
            } else if (SELECTION_ACTION_COMMAND.equals(command)) {
                // PENDING
            }
        }

    }

}
