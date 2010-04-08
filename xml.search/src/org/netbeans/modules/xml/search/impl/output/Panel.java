/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.xml.search.impl.output;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.netbeans.api.print.PrintManager;
import static org.netbeans.modules.xml.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2008.02.11
 */
final class Panel extends JPanel {

    Panel(Tree list, Tree tree) {
        myList = list;
        myTree = tree;
        myCurrent = tree;

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;

        // buttons
        add(createButtonPanel(), c);

        // tree
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        myInner = new JPanel(new GridBagLayout());
        add(myInner, c);
        updateView();
    }

    private void updateView() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                myInner.removeAll();
                GridBagConstraints c = new GridBagConstraints();

                c.weightx = 1.0;
                c.weighty = 1.0;
                c.fill = GridBagConstraints.BOTH;
                c.anchor = GridBagConstraints.NORTH;
                JScrollPane scrollPane = new JScrollPane(myCurrent);
                myInner.add(new Navigation(myCurrent, scrollPane), c);

                myInner.revalidate();
                myInner.repaint();
                myCurrent.requestFocus();
            }
        });
    }

    private JToolBar createButtonPanel() {
        JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
        toolBar.setFloatable(false);
        JButton button;

        // collapse / expand
        button = createButton(
            new ButtonAction(
            icon(Panel.class, "expose"), // NOI18N
            i18n(Panel.class, "TLT_Expose")) { // NOI18N
                public void actionPerformed(ActionEvent event) {
                    myCurrent.expose();
                }
            }
        );
        toolBar.add(button);

        // view
        button = createButton(
            new ButtonAction(
            icon(Panel.class, "view"), // NOI18N
            i18n(Panel.class, "TLT_Change_View")) { // NOI18N
                public void actionPerformed(ActionEvent event) {
                    changeView();
                }
            }
        );
        toolBar.add(button);

        // previous occurence
        button = createButton(
            new ButtonAction(
            icon(Panel.class, "previous"), // NOI18N
            i18n(Panel.class, "TLT_Previous_Occurence")) { // NOI18N
                public void actionPerformed(ActionEvent event) {
                    myCurrent.previousOccurence();
                }
            }
        );
        toolBar.add(button);

        // next occurence
        button = createButton(
            new ButtonAction(
            icon(Panel.class, "next"), // NOI18N
            i18n(Panel.class, "TLT_Next_Occurence")) { // NOI18N
                public void actionPerformed(ActionEvent event) {
                    myCurrent.nextOccurence();
                }
            }
        );
        toolBar.add(button);

        // vlv: print
        button = createButton(PrintManager.printAction(this));
        toolBar.add(button);

        // export
        button = createButton(
            new ButtonAction(
            icon(Panel.class, "export"), // NOI18N
            i18n(Panel.class, "TLT_Export")) { // NOI18N
                public void actionPerformed(ActionEvent event) {
                    myCurrent.export();
                }
            }
        );
        toolBar.add(button);

        return toolBar;
    }

    private void changeView() {
        if (myCurrent == myTree) {
            myCurrent = myList;
        } else {
            myCurrent = myTree;
        }
        updateView();
    }

    private Tree myList;
    private Tree myTree;
    private Tree myCurrent;
    private JPanel myInner;
}
