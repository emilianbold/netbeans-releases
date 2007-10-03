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

package jemmyI18NWizard.wizardSupport;

import org.netbeans.test.oo.gui.jam.Jemmy;
import org.netbeans.test.oo.gui.jello.JelloOKCancelDialog;
import org.netbeans.test.oo.gui.jam.JamComboBox;
import org.netbeans.jemmy.operators.JTreeOperator;
import javax.swing.tree.TreePath;


public class SelectSourcesDialog extends JelloOKCancelDialog {

    protected JamComboBox filesystemCombo;
    protected JTreeOperator treeOperator;

    /** Creates new SelectSourcesDialog */
    public SelectSourcesDialog(String title) {
        super(title);
        filesystemCombo = this.getJamComboBox(0);
        treeOperator = new JTreeOperator(Jemmy.getOp(this));
    }
    
    public void expandRow(int index) {      //index beginning witgh 1
        treeOperator.expandRow(index);
    }
    
    public void selectRow(int index) {
        treeOperator.setSelectionRow(index);
    }
    
    public void collapseRow(int index) {
        treeOperator.collapseRow(index);
    }
    
    public boolean selectFilesystem(String name) {
        for(int i=0;i<filesystemCombo.getItemCount();i++) {
            String selectedName = filesystemCombo.getItemAt(i).toString();
            StringBuffer buffer = new StringBuffer(selectedName);
            int startingAt = selectedName.indexOf("displayName=");
            buffer.delete(0, startingAt+"displayName=".length());
            buffer.deleteCharAt(buffer.length()-1);
            if(name.equals(buffer.toString())) {
                filesystemCombo.setSelectedItem(i);
                return true;
            }
        }
        return false;
    }
    
    public void setSelectedItem(int index) {
        filesystemCombo.setSelectedItem(index);
    }
    
    public String getSelectedFilesystem() {
        return filesystemCombo.getSelectedItem();
    }
    
    public String findPath(String name) {
        return null;
    }
    
    public void expandPath(String[] pathString) {
        TreePath path = treeOperator.findPath(pathString, false, false);
        treeOperator.expandPath(path);
    }
    
    public void selectPath(String[] pathString) {
        TreePath path = treeOperator.findPath(pathString, false, false);
        treeOperator.selectPath(path);
    }
    
}
