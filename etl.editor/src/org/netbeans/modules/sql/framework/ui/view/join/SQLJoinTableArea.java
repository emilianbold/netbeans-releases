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
package org.netbeans.modules.sql.framework.ui.view.join;

import com.nwoods.jgo.JGoLink;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.sql.framework.model.SQLJoinView;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.ui.graph.IGraphNode;
import org.netbeans.modules.sql.framework.ui.view.graph.SQLSourceTableArea;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import com.sun.sql.framework.exception.BaseException;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;


/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class SQLJoinTableArea extends SQLSourceTableArea {

    private static transient final Logger mLogger = Logger.getLogger(SQLJoinTableArea.class.getName());
    
    private static transient final Localizer mLoc = Localizer.get();
    /** Creates a new instance of SQLJoinTableArea
     * @param table
     */
    public SQLJoinTableArea(SourceTable table) {
        super(table);
    }

    @Override
    public void setShowHeader(boolean show) {
        super.setShowHeader(show);
    }

    @Override
    protected void Remove_ActionPerformed(ActionEvent e) {
        SourceTable sTable = (SourceTable) this.getDataObject();
        JoinViewGraphNode joinViewNode = (JoinViewGraphNode) this.getParent();
        if (joinViewNode != null && sTable != null) {
            SQLJoinView joinView = (SQLJoinView) joinViewNode.getDataObject();

            if (joinView.getSourceTables().size() <= 2) {
                String nbBundle1 = mLoc.t("BUND475: Cannot remove table {0} from join view.A join view always requires at least two tables.",sTable.getName());
                NotifyDescriptor d = new NotifyDescriptor.Message(nbBundle1.substring(15), NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
                return;
            }

            try {
                if (joinViewNode.isTableColumnMapped(sTable)) {
                    String nbBundle2 = mLoc.t("BUND476: Table {0} has some mappings defined which will be lost.Do you really want to remove this table?",sTable.getName());
                    NotifyDescriptor d = new NotifyDescriptor.Confirmation(nbBundle2.substring(15), NotifyDescriptor.WARNING_MESSAGE);
                    Object response = DialogDisplayer.getDefault().notify(d);
                    if (response.equals(NotifyDescriptor.OK_OPTION)) {
                        joinViewNode.removeTable(sTable);
                    }
                } else {
                    joinViewNode.removeTable(sTable);
                }
            } catch (BaseException ex) {
                NotifyDescriptor d = new NotifyDescriptor.Message(ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            }
        }
    }

    /**
     * Extends parent implementation to signal this table's enclosing join view that it
     * should also update itself.
     *
     * @param e ActionEvent to be handled
     * @return true if column visibilities were updated; false otherwise
     */
    @Override
    protected boolean selectVisibleColumnsActionPerformed(ActionEvent e) {
        boolean response = super.selectVisibleColumnsActionPerformed(e);
        if (response) {
            IGraphNode parent = this.getParentGraphNode();
            if (parent instanceof JoinViewGraphNode) {
                JoinViewGraphNode joinNode = (JoinViewGraphNode) parent;
                joinNode.setHeight(joinNode.getMaximumHeight());
                joinNode.layoutChildren();
            }
        }

        return response;
    }

    /**
     * Gets the parent node.
     *
     * @return parent
     */
    @Override
    public IGraphNode getParentGraphNode() {
        return (IGraphNode) this.getParent();
    }

    /**
     * is this node can be deleted
     *
     * @return true if node can be deleted
     */
    @Override
    public boolean isDeleteAllowed() {
        Remove_ActionPerformed(null);
        return false;
    }

    /**
     * get a list of all input and output links
     *
     * @return list of input links
     */
    @Override
    public List<JGoLink> getAllLinks() {
        ArrayList<JGoLink> links = new ArrayList<JGoLink>();
        links.addAll(super.getAllLinks());

        JoinViewGraphNode joinNode = (JoinViewGraphNode) this.getParentGraphNode();
        if (joinNode != null) {
            Iterator it = joinNode.getAllTableAreas().iterator();
            while (it.hasNext()) {
                SQLJoinTableArea tableArea1 = (SQLJoinTableArea) it.next();
                if (tableArea1 != this) {
                    links.addAll(tableArea1.getTableLinks());
                }
            }
        }

        return links;
    }

    public List<JGoLink> getTableLinks() {
        return super.getAllLinks();
    }
}
