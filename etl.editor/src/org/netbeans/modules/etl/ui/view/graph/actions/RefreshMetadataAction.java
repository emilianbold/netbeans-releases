/*
 *
 *          Copyright (c) 2003-2005, SeeBeyond Technology Corporation,
 *          All Rights Reserved
 *
 *          This program, and all the routines referenced herein,
 *          are the proprietary properties and trade secrets of
 *          SEEBEYOND TECHNOLOGY CORPORATION.
 *
 *          Except as provided for by license agreement, this
 *          program shall not be duplicated, used, or disclosed
 *          without  written consent signed by an officer of
 *          SEEBEYOND TECHNOLOGY CORPORATION.
 *
 */

package org.netbeans.modules.etl.ui.view.graph.actions;

import com.sun.sql.framework.exception.DBSQLException;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.TargetTable;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.Action;
import java.util.Iterator;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.etl.ui.DataObjectProvider;
import org.netbeans.modules.sql.framework.model.SQLJoinView;
import org.openide.util.Exceptions;
import org.netbeans.modules.sql.framework.ui.view.graph.MetaTableModel;
import org.netbeans.modules.sql.framework.ui.view.graph.SQLTableArea;
import org.netbeans.modules.sql.framework.model.visitors.SQLDBSynchronizationVisitor;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.netbeans.modules.sql.framework.ui.model.CollabSQLUIModel;
import org.netbeans.modules.sql.framework.ui.view.graph.SQLSourceTableArea;
import org.netbeans.modules.sql.framework.ui.view.graph.SQLTargetTableArea;
import org.netbeans.modules.sql.framework.ui.graph.actions.GraphAction;
import org.netbeans.modules.sql.framework.ui.view.BasicTopView;
import org.netbeans.modules.sql.framework.ui.view.join.JoinViewGraphNode;
import org.openide.windows.WindowManager;

/**
 * @author Nithya Radhakrishnan
 * @version $Revision$
 */
public class RefreshMetadataAction extends GraphAction {

    private static final URL synchroniseImgUrl = RefreshMetadataAction.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/refresh.png");
    private static transient final Logger mLogger = Logger.getLogger(RefreshMetadataAction.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    public String nbBundle1 = mLoc.t("BUND024: Refresh Metadata");
    public RefreshMetadataAction() {
        //action name
        
        this.putValue(Action.NAME,nbBundle1.substring(15));

        //action icon
        this.putValue(Action.SMALL_ICON, new ImageIcon(synchroniseImgUrl));

        //action tooltip
        String nbBundle2 = mLoc.t("BUND025: Refresh Metadata ");
        this.putValue(Action.SHORT_DESCRIPTION,nbBundle2.substring(15));
        // Acceleratot Shift-R
    }

    /**
     * called when this action is performed in the ui
     *
     * @param ev event
     */
    public void actionPerformed(ActionEvent ev) {
        IGraphView graphView = (IGraphView) ev.getSource();
        CollabSQLUIModel model = (CollabSQLUIModel) graphView.getGraphModel();
        List infoList = new ArrayList();
        String nbBundle3 = mLoc.t("BUND026: If columns are deleted or renamed you may lose existing mappings.");
        String dlgMsg = nbBundle3.substring(15);
        String dlgTitle = nbBundle1.substring(15);
        
        int response = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(), dlgMsg, dlgTitle, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (JOptionPane.OK_OPTION == response) {
        Iterator targetTables = model.getSQLDefinition().getTargetTables().iterator();
        while (targetTables.hasNext()) {
            try {
                TargetTable tt = (TargetTable) targetTables.next();
                SQLDBSynchronizationVisitor visitView = new SQLDBSynchronizationVisitor();
                SQLTargetTableArea ttArea = (SQLTargetTableArea) graphView.findGraphNode(tt);
                SQLTableArea tarea = (SQLTableArea) ttArea.getTableArea();
                MetaTableModel metaModel = (MetaTableModel) tarea.getModel();
                visitView.mergeCollabTableWithDatabaseTable((SQLDBTable) tt, metaModel);
                   if (!visitView.infoList.isEmpty()) {
                    ttArea.layoutChildren();
                    // Mark collab as needing to be persisted.
                    DataObjectProvider.getProvider().getActiveDataObject().setModified(true);
                    model.setDirty(true);
                    infoList.addAll(visitView.infoList);
                }
            } catch (DBSQLException ex) {
                Exceptions.printStackTrace(ex);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        Iterator sourceTables = model.getSQLDefinition().getSourceTables().iterator();
        while (sourceTables.hasNext()) {
            try {
                SourceTable st = (SourceTable) sourceTables.next();
                SQLDBSynchronizationVisitor visitView = new SQLDBSynchronizationVisitor();
                SQLSourceTableArea stArea = (SQLSourceTableArea) graphView.findGraphNode(st);
                SQLTableArea tarea = (SQLTableArea) stArea.getTableArea();
                MetaTableModel metaModel = (MetaTableModel) tarea.getModel();
                visitView.mergeCollabTableWithDatabaseTable((SQLDBTable) st, metaModel);
                    if (!visitView.infoList.isEmpty()) {
                    stArea.layoutChildren();
                    SQLJoinView jView = model.getJoinView(st);
                    if (jView != null) {
                        JoinViewGraphNode jViewGraph = (JoinViewGraphNode) graphView.findGraphNode(jView);
                        jViewGraph.layoutChildren();
                        jViewGraph.setHeight(jViewGraph.getMaximumHeight());
                    }
                    // Mark collab as needing to be persisted.
                    DataObjectProvider.getProvider().getActiveDataObject().setModified(true);
                    model.setDirty(true);
                    infoList.addAll(visitView.infoList);
                }
            } catch (DBSQLException ex) {
                Exceptions.printStackTrace(ex);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
          BasicTopView gvMgr = (BasicTopView) graphView.getGraphViewContainer();
          gvMgr.showRefreshMetadataInfo(infoList);
        } // end of jOptionPane
    }
}