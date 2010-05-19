/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.etl.ui.view.graph.actions;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.net.URL;
import java.util.List;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.netbeans.modules.etl.ui.DataObjectProvider;
import org.netbeans.modules.etl.ui.model.impl.ETLCollaborationModel;
import org.netbeans.modules.etl.ui.view.ETLCollaborationTopPanel;
import org.netbeans.modules.sql.framework.model.SQLJoinView;
import org.netbeans.modules.sql.framework.ui.graph.actions.GraphAction;
import org.netbeans.modules.sql.framework.ui.view.join.JoinMainDialog;
import org.netbeans.modules.sql.framework.ui.view.join.JoinUtility;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import net.java.hulp.i18n.Logger;
import com.sun.etl.exception.BaseException;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.model.DBTable;

/**
 * This action is to create or edit join
 * 
 * @author Ritesh Adval
 * @version $Revision$
 */
public class JoinAction extends GraphAction {

    private static final URL joinImgUrl = ValidationAction.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/join_view.png");
    private static final String LOG_CATEGORY = JoinAction.class.getName();
    private static transient final Logger mLogger = Logger.getLogger(JoinAction.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    public JoinAction() {
        // action name
        String nbBundle1 = mLoc.t("BUND022: Create New Join...");
        this.putValue(Action.NAME, nbBundle1.substring(15));

        // action icon
        this.putValue(Action.SMALL_ICON, new ImageIcon(joinImgUrl));

        // action tooltip
        String nbBundle2 = mLoc.t("BUND023: Create New Join (Ctrl+Shift+J)");
        this.putValue(Action.SHORT_DESCRIPTION,nbBundle2.substring(15));

        // Acceleratot Shift-J
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('J', InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_MASK));
    }

    /**
     * called when this action is performed in the ui
     * 
     * @param ev event
     */
    public void actionPerformed(ActionEvent ev) {
        ETLCollaborationTopPanel etlEditor = null;
        try {
            etlEditor = DataObjectProvider.getProvider().getActiveDataObject().getETLEditorTopPanel();
        } catch (Exception ex) {
            //ignore
        }

        // first check if user has selected some tables/join view and he wants to create
        // a join

        // if user just selects one join view then he wants to edit that
        // if there is no selection then user wants to create a new join
        ETLCollaborationModel collabModel = DataObjectProvider.getProvider().getActiveDataObject().getModel();

        if (collabModel != null) {
            List<DBTable> sList = collabModel.getSQLDefinition().getJoinSources();
            JoinMainDialog.showJoinDialog(sList, null, etlEditor.getGraphView(), true);

            if (JoinMainDialog.getClosingButtonState() == JoinMainDialog.OK_BUTTON) {
                SQLJoinView joinView = JoinMainDialog.getSQLJoinView();
                try {
                    if (joinView != null) {
                        JoinUtility.handleNewJoinCreation(joinView, JoinMainDialog.getTableColumnNodes(), etlEditor.getGraphView());
                    }
                } catch (BaseException ex) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("Error adding join view.", NotifyDescriptor.INFORMATION_MESSAGE));
                    mLogger.fine(mLoc.t("EDIT025: error adding join view{0}")+ex);
                }
            }
        }
    }
}

