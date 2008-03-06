/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui.wizards;

import java.awt.Component;
import java.io.File;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.MutableComboBoxModel;
import org.openide.util.NbBundle;

/**
 * Helper class with static methods
 * @author Tomas Mysik
 */
public final class Utils {

    private Utils() {
    }

    public static String browseLocationAction(final Component parent, String path) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(NbBundle.getMessage(ConfigureProjectPanel.class, "LBL_SelectProjectLocation"));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (path != null && path.length() > 0) {
            File f = new File(path);
            if (f.exists()) {
                chooser.setSelectedFile(f);
            }
        }
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(parent)) {
            return chooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }

    public static List getAllItems(final JComboBox comboBox) {
        return new AbstractList() {
            public Object get(int i) {
                return comboBox.getItemAt(i);
            }

            public int size() {
                return comboBox.getItemCount();
            }
        };
    }

    /**
     * Sort {@link MutableComboBoxModel} according to the natural ordering of its items
     * and preserves selected item if any.
     * @param comboBoxModel {@link MutableComboBoxModel} to sort.
     */
    public static void sortComboBoxModel(MutableComboBoxModel comboBoxModel) {
        int size = comboBoxModel.getSize();
        if (size < 2) {
            return;
        }
        Object selected = comboBoxModel.getSelectedItem();
        Object[] items = new Object[size];
        for (int i = size - 1; i >= 0; i--) {
            items[i] = comboBoxModel.getElementAt(i);
            comboBoxModel.removeElementAt(i);
        }
        assert comboBoxModel.getSize() == 0;
        Arrays.sort(items);
        for (int i = 0; i < size; i++) {
            comboBoxModel.addElement(items[i]);
        }
        if (selected != null) {
            comboBoxModel.setSelectedItem(selected);
        }
    }
}
