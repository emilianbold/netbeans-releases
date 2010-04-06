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
package org.netbeans.modules.xml.clazz;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.lang.reflect.Method;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.openide.DialogDescriptor;
import static org.netbeans.modules.xml.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2009.07.15
 */
public final class ClazzDialog extends Dialog implements TreeSelectionListener {

    public ClazzDialog(List<File> files) {
        myTree = new ClazzTree(new ClazzLoader().getMethods(files));
        myTree.addTreeSelectionListener(this);
    }

    public void valueChanged(TreeSelectionEvent event) {
        setEnabledOkButton(myTree.getSelectedMethod() != null);
    }

    private JPanel createPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;

        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        JScrollPane scrollPanel = new JScrollPane(myTree);
        panel.add(scrollPanel, c);

        Dimension dimension = new Dimension(WIDTH, HEIGHT);
        panel.setMinimumSize(dimension);
        panel.setPreferredSize(dimension);

        return panel;
    }

    @Override
    protected DialogDescriptor createDescriptor() {
        myDescriptor = new DialogDescriptor(
            getResizableXY(createPanel()),
            i18n("LBL_Choose_Method"), // NOI18N
            true,
            getButtons(),
            DialogDescriptor.OK_OPTION,
            DialogDescriptor.DEFAULT_ALIGN,
            null,
            new ActionListener() {
                public void actionPerformed(ActionEvent event) {}
            }
        );
        setEnabledOkButton(false);

        return myDescriptor;
    }

    public Method getMethod() {
        return myTree.getSelectedMethod();
    }

    private Object[] getButtons() {
        return new Object[] { DialogDescriptor.OK_OPTION, DialogDescriptor.CANCEL_OPTION };
    }

    private void setEnabledOkButton(boolean enabled) {
        myDescriptor.setValid(enabled);
    }

    private ClazzTree myTree;
    private DialogDescriptor myDescriptor;
    private static final int WIDTH = 380;
    private static final int HEIGHT = 420;
}
