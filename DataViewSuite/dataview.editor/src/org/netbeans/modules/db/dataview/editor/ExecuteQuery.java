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
package org.netbeans.modules.db.dataview.editor;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import javax.swing.text.Document;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.openide.util.Exceptions;
import org.netbeans.modules.db.dataview.output.DataViewOutputPanel;

/**
 *
 * @author Nithya Radhakrishnan
 */
public final class ExecuteQuery extends AbstractAction {

    private static final URL runIconUrl = ExecuteQuery.class.getResource("/org/netbeans/modules/db/dataview/editor/images/runCollaboration.png");
    private DataViewSourceMultiViewElement editor;

    public ExecuteQuery(DataViewSourceMultiViewElement e) {
        editor = e;
        //action name
        String nbBundle1 = "Execute Query";
        this.putValue(Action.NAME, nbBundle1);

        //action 
        this.putValue(Action.SMALL_ICON, new ImageIcon(runIconUrl));

        //action tooltip
        String nbBundle2 = "Execute Query (Alt+Shift+N)";
        this.putValue(Action.SHORT_DESCRIPTION, nbBundle2);

        // Acceleratot Shift-N
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('N', InputEvent.SHIFT_DOWN_MASK + InputEvent.ALT_MASK));
    }

    public void actionPerformed(ActionEvent ev) {
        try {

            // get the Query String
            Document doc = editor.getEditorPane().getDocument();
            String queryString = doc.getText(0, doc.getLength());

            // Create DBConnectionDefinition from selected DatabaseConnection
            final DatabaseConnection dbConn = editor.getSelectedConnection();

            // Create DataView and add it to the splipane as tab.
            DataViewOutputPanel dataView = new DataViewOutputPanel(dbConn, queryString, 1);
            dataView.generateResult();
            editor.addResultSet(dataView);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

//    // Not used now...
//    public void showSplitPaneView(DataViewOutputPanel dataView) {
//        ResultSetTabbedPane topComp = ResultSetTabbedPane.findInstance();
//        if (!topComp.isOpened()) {
//            topComp.open();
//        }
//        topComp.setVisible(true);
//        topComp.addPanel(dataView);
//        topComp.requestActive();
//    }
}
