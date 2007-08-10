/*
 * DataObjectHelper.java
 *
 * Created on June 25, 2006, 5:05 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.etl.ui;

import java.awt.Cursor;
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.io.InputStream;
import java.sql.Types;
import java.util.Iterator;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.netbeans.modules.etl.model.impl.ETLDefinitionImpl;
import org.netbeans.modules.etl.ui.model.impl.ETLCollaborationModel;
import org.netbeans.modules.etl.ui.view.ETLCollaborationTopComponent;
import org.netbeans.modules.etl.ui.view.property.ETLResourceManager;
import org.netbeans.modules.etl.ui.view.wizards.ETLWizardHelper;
import org.netbeans.modules.sql.framework.model.RuntimeDatabaseModel;
import org.netbeans.modules.sql.framework.model.RuntimeInput;
import org.netbeans.modules.sql.framework.model.RuntimeOutput;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SQLJoinView;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.model.SourceColumn;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.model.impl.RuntimeDatabaseModelImpl;
import org.netbeans.modules.sql.framework.model.impl.RuntimeOutputImpl;
import org.netbeans.modules.sql.framework.model.utils.SQLObjectUtil;
import org.netbeans.modules.sql.framework.ui.editor.property.impl.PropertyViewManager;
import org.netbeans.modules.sql.framework.ui.view.join.JoinUtility;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.windows.WindowManager;

import com.sun.sql.framework.exception.BaseException;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;

/**
 *
 * @author radval
 */
public class DataObjectHelper {
    
    private static ETLDataObject mDataObject;
    
    private static PropertyViewManager pvMgr;
    
    private static DataObjectHelper manager;
    
    /** Creates a new instance of DataObjectHelper */
    public DataObjectHelper(ETLDataObject dataObject) {
        mDataObject = dataObject;
    }
    
    public DataObjectHelper(){
    }
    
    public void initializeETLDataObject(WizardDescriptor descriptor,
            ETLDataObject mObj,
            ETLEditorSupport editorSupport) throws Exception {
        mDataObject = mObj;
        ETLWizardHelper wHelper = new ETLWizardHelper(descriptor);
        
        List sourceOTDList = wHelper.getSelectedSourceOtds();
        List destinationOTDList = wHelper.getSelectedDestinationOtds();
        SQLJoinView joinView = wHelper.getSQLJoinView();
        List jVisibleColumns = wHelper.getTableColumnNodes();
        ETLDefinitionImpl repModel = (ETLDefinitionImpl) mObj.getETLDefinition();
        
        // for now we need to have an editor top component so that table can be added to
        // it
        final ETLCollaborationTopComponent etlEditor = this.mDataObject.getETLEditorTC();
        ETLCollaborationModel collabModel = mObj.getModel();
        
        // first add join view
        if (joinView != null) {
            JoinUtility.handleNewJoinCreation(joinView, jVisibleColumns, etlEditor.getGraphView());
            
            // WT #67643: Ensure that flatfile tables in join views have filename runtime
            // inputs.
            establishRuntimeInputs(collabModel, joinView.getSourceTables());
        }
        
        if (sourceOTDList != null) {
            addDestinationOTDs(sourceOTDList, collabModel);
        }
        
        if (destinationOTDList != null) {
            addDestinationOTDs(destinationOTDList, collabModel);
        }
        establishRuntimeOutputs(mObj);
        //make sure editor document has etl content when first time created
        try {
            String content = collabModel.getETLDefinition().toXMLString("");
            editorSupport.openDocument();
            editorSupport.getDocument().remove(0, editorSupport.getDocument().getLength());
            editorSupport.getDocument().insertString(0, content, null);
        } catch(Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    public void initializeETLDataObject(WizardDescriptor descriptor, ETLCollaborationModel collabModel, IGraphView graphView) throws Exception {
        ETLWizardHelper wHelper = new ETLWizardHelper(descriptor);
        
        List sourceOTDList = wHelper.getSelectedSourceOtds();
        List destinationOTDList = wHelper.getSelectedDestinationOtds();
        SQLJoinView joinView = wHelper.getSQLJoinView();
        List jVisibleColumns = wHelper.getTableColumnNodes();     
        
        // first add join view
        if (joinView != null) {
            JoinUtility.handleNewJoinCreation(joinView, jVisibleColumns, graphView);
            
            // WT #67643: Ensure that flatfile tables in join views have filename runtime
            // inputs.
            establishRuntimeInputs(collabModel, joinView.getSourceTables());
        }
        
        if (sourceOTDList != null) {
            addDestinationOTDs(sourceOTDList, collabModel);
        }
        
        if (destinationOTDList != null) {
            addDestinationOTDs(destinationOTDList, collabModel);
        }
        establishRuntimeOutputs(collabModel.getSQLDefinition());
    }    
    
    private static void addDestinationOTDs(List otds, ETLCollaborationModel collabModel)
    throws BaseException {
        for (int i = 0; i < otds.size(); i++) {
            SQLDBModel dbModel = (SQLDBModel) otds.get(i);
            
            // Add database model only if at least one table in it was selected by user
            if (dbModel.getTables().size() == 0) {
                continue;
            }
            
            addTables(dbModel, collabModel);
        }
    }
    
    private static void addTables(SQLDBModel dbModel, ETLCollaborationModel collabModel) throws BaseException {
        List tables = dbModel.getTables();
        Iterator it = tables.iterator();
        while (it.hasNext()) {
            SQLDBTable table = (SQLDBTable) it.next();
            collabModel.addObject(table);
        }
        
        establishRuntimeInputs(collabModel, tables);
    }
    
    private static void establishRuntimeInputs(ETLCollaborationModel collabModel,
            List tables) throws BaseException {
        Iterator it = tables.iterator();
        while (it.hasNext()) {
            // if table is source table and is a flat file we auto create a run
            SQLDBTable sTable = (SQLDBTable) it.next();
            SourceColumn runtimeArg = SQLObjectUtil.createRuntimeInput(sTable, collabModel.getSQLDefinition());
            if (runtimeArg != null) {
                RuntimeInput runtimeInput = (RuntimeInput) runtimeArg.getParent();
                
                if (runtimeInput != null) {
                    // if runtime input is not in SQL definition then add it
                    if ((collabModel.getSQLDefinition().isTableExists(runtimeInput)) == null) {
                        collabModel.addObject(runtimeInput);
                    }
                }
            }
        }
    }
    
    /**
     * Updates selected source and target tables for the given eTL Collaboration
     * ProjectElement.
     *
     * @param dObj etldataobject(representing an eTL Collaboration) whose table
     *        selections are to be updated
     * @param sourceOtds List of source table OTDs to be added to <code>element</code>
     * @param targetOtds List of target table OTDs to be added to <code>element</code>
     * @throws Exception if error occurs during update
     */
    public void updateTableSelections(ETLDataObject dObj, List sourceOtds, List targetOtds) throws Exception {
        ETLCollaborationModel collabModel = null;
        collabModel = dObj.getModel();
        if (collabModel == null) {
            return;
        }
        
        if (sourceOtds != null) {
            addDestinationOTDs(sourceOtds, collabModel);
        }
        
        if (targetOtds != null) {
            addDestinationOTDs(targetOtds, collabModel);
        }
    }
    
    /**
     * Gets instance of PropertyViewManager.
     *
     * @return instance of PropertyViewManager
     */
    public static PropertyViewManager getPropertyViewManager() {
        if (pvMgr == null) {
            InputStream stream = DataObjectHelper.class.getClassLoader().getResourceAsStream("org/netbeans/modules/etl/ui/resources/etl_properties.xml");
            pvMgr = new PropertyViewManager(stream, new ETLResourceManager());
        }
        return pvMgr;
    }
    
    /**
     * Displays default cursor in Editor.
     */
    public static void setDefaultCursor() {
        Runnable r = new Runnable() {
            public synchronized void run() {
                Frame mainWindow = WindowManager.getDefault().getMainWindow();
                if (mainWindow instanceof JFrame) {
                    JFrame frame = (JFrame) mainWindow;
                    JPanel glass = (JPanel) frame.getGlassPane();
                    glass.setCursor(Cursor.getDefaultCursor());
                    glass.setVisible(false);
                }
            }
        };
        
        SwingUtilities.invokeLater(r);
    }
    
    /**
     * Displays wait cursor in Editor.
     */
    public static void setWaitCursor() {
        Runnable r = new Runnable() {
            public synchronized void run() {
                Frame mainWindow = WindowManager.getDefault().getMainWindow();
                
                if (mainWindow instanceof JFrame) {
                    JFrame frame = (JFrame) mainWindow;
                    JPanel glass = (JPanel) frame.getGlassPane();
                    glass.addMouseListener(new MouseAdapter() {
                    });
                    glass.addMouseMotionListener(new MouseMotionAdapter() {
                    });
                    glass.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    glass.setVisible(true);
                }
            }
        };
        
        SwingUtilities.invokeLater(r);
    }
    
    private void establishRuntimeOutputs(ETLDataObject mObj) {
        try {
            SQLDefinition sqlDefn = mObj.getETLDefinition().getSQLDefinition();
            RuntimeDatabaseModel rtModel = sqlDefn.getRuntimeDbModel();
            if(rtModel == null) {
                rtModel = new RuntimeDatabaseModelImpl();
            }
            RuntimeOutput rtOutTable = new RuntimeOutputImpl();
            // add STATUS
            SQLDBColumn column = SQLModelObjectFactory.getInstance().createTargetColumn("STATUS", Types.VARCHAR, 0, 0, true);
            column.setEditable(false);
            rtOutTable.addColumn(column);
            
            // add STARTTIME
            column = SQLModelObjectFactory.getInstance().createTargetColumn("STARTTIME", Types.TIMESTAMP, 0, 0, true);
            column.setEditable(false);
            rtOutTable.addColumn(column);
            
            // add ENDTIME
            column = SQLModelObjectFactory.getInstance().createTargetColumn("ENDTIME", Types.TIMESTAMP, 0, 0, true);
            column.setEditable(false);
            rtOutTable.addColumn(column);
            
            Iterator it = sqlDefn.getTargetTables().iterator();
            while(it.hasNext()) {
                TargetTable targetTable = (TargetTable)it.next();
                String argName = SQLObjectUtil.getTargetTableCountRuntimeOutput(targetTable);                
                column = SQLModelObjectFactory.getInstance().createTargetColumn(argName, Types.INTEGER, 0, 0, true);
                column.setEditable(false);
                rtOutTable.addColumn(column);
            }            
            rtModel.addTable(rtOutTable);
            sqlDefn.addObject(rtModel);
        } catch (Exception ex) {
            //ignore
        }
    }
    
    private void establishRuntimeOutputs(SQLDefinition sqlDefn) {
        try {
            RuntimeDatabaseModel rtModel = sqlDefn.getRuntimeDbModel();
            if(rtModel == null) {
                rtModel = new RuntimeDatabaseModelImpl();
            }
            RuntimeOutput rtOutTable = new RuntimeOutputImpl();
            // add STATUS
            SQLDBColumn column = SQLModelObjectFactory.getInstance().createTargetColumn("STATUS", Types.VARCHAR, 0, 0, true);
            column.setEditable(false);
            rtOutTable.addColumn(column);
            
            // add STARTTIME
            column = SQLModelObjectFactory.getInstance().createTargetColumn("STARTTIME", Types.TIMESTAMP, 0, 0, true);
            column.setEditable(false);
            rtOutTable.addColumn(column);
            
            // add ENDTIME
            column = SQLModelObjectFactory.getInstance().createTargetColumn("ENDTIME", Types.TIMESTAMP, 0, 0, true);
            column.setEditable(false);
            rtOutTable.addColumn(column);
            
            Iterator it = sqlDefn.getTargetTables().iterator();
            while(it.hasNext()) {
                TargetTable targetTable = (TargetTable)it.next();
                String argName = SQLObjectUtil.getTargetTableCountRuntimeOutput(targetTable);                
                column = SQLModelObjectFactory.getInstance().createTargetColumn(argName, Types.INTEGER, 0, 0, true);
                column.setEditable(false);
                rtOutTable.addColumn(column);
            }            
            rtModel.addTable(rtOutTable);
            sqlDefn.addObject(rtModel);
        } catch (Exception ex) {
            //ignore
        }
    }
}