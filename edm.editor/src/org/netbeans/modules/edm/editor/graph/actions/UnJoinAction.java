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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.edm.editor.graph.actions;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import org.netbeans.modules.edm.editor.dataobject.MashupDataObject;
import org.netbeans.modules.edm.editor.utils.ImageConstants;
import org.netbeans.modules.edm.editor.utils.MashupGraphUtil;
import org.netbeans.modules.edm.editor.utils.TagParserUtility;
import org.netbeans.modules.edm.model.DBTable;
import org.netbeans.modules.edm.model.SQLConstants;
import org.netbeans.modules.edm.model.SQLDefinition;
import org.netbeans.modules.edm.model.SQLJoinView;
import org.netbeans.modules.edm.model.SourceTable;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author Shankari
 */
public class UnJoinAction extends AbstractAction {

    private MashupDataObject mObj;

    public UnJoinAction(MashupDataObject dObj, String name) {
        super(name, new ImageIcon(MashupGraphUtil.getImage(ImageConstants.EDITJOIN)));
        this.putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(UnJoinAction.class, "TOOLTIP_Remove_Join"));
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('U', InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_MASK));
        mObj = dObj;
    }

    public void actionPerformed(ActionEvent e) {
        JPanel panel = new JPanel();
        String dlgTitle = NbBundle.getMessage(UnJoinAction.class, "MSG_Unjoin_Tables");
        panel.add(new JLabel(dlgTitle));
        int response = JOptionPane.showConfirmDialog(
                WindowManager.getDefault().getMainWindow(), panel, NbBundle.getMessage(UnJoinAction.class, "MSG_Unjoin_Tables"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (JOptionPane.OK_OPTION == response) {
            try {
                mObj.getGraphManager().unJoin();
                SQLJoinView[] joinViews = (SQLJoinView[]) mObj.getModel().getSQLDefinition().getObjectsOfType(
                        SQLConstants.JOIN_VIEW).toArray(new SQLJoinView[0]);
                SQLJoinView joinView = null;
                if (joinViews != null && joinViews.length != 0) {
                    joinView = joinViews[0];
                }
                SQLDefinition sqlDefn = TagParserUtility.getAncestralSQLDefinition(joinView);
                List<DBTable> srcTables = joinView.getSourceTables();
                for (DBTable table : srcTables) {
                    if (table instanceof SourceTable) {
                        SourceTable srcTable = (SourceTable) table;
                        srcTable.setUsedInJoin(false);
                    }
                }
                sqlDefn.removeJoinViewOnly(joinView);
                //mObj.getMashupDataEditorSupport().synchDocument();
                mObj.getModel().setDirty(true);
                mObj.setModified(true);
                mObj.getGraphManager().validateScene();
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
