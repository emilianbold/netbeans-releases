package org.netbeans.modules.mashup.db.wizard;

import java.awt.Dialog;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.mashup.db.common.FlatfileDBConnectionFactory;
import org.netbeans.modules.mashup.db.common.PropertyKeys;
import org.netbeans.modules.mashup.db.model.FlatfileDBTable;
import org.netbeans.modules.mashup.db.model.FlatfileDatabaseModel;
import org.netbeans.modules.mashup.db.model.impl.FlatfileDBTableImpl;
import org.netbeans.modules.mashup.tables.wizard.MashupTableWizardIterator;
import org.netbeans.modules.sql.framework.common.utils.DBExplorerUtil;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;

public final class NewFlatfileTableAction extends CallableSystemAction {
    private static transient final Logger mLogger = Logger.getLogger(NewFlatfileTableAction.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    public String nbBundle1 = mLoc.t("BUND274: Add External Tables...");
    public String nbBundle6 = mLoc.t("BUND874: Add External Tables");
    public void performAction() {
        WizardDescriptor.Iterator iterator = new MashupTableWizardIterator();
        WizardDescriptor wizardDescriptor = new WizardDescriptor(iterator);
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle(nbBundle6.substring(15));
        ((MashupTableWizardIterator)iterator).setDescriptor(wizardDescriptor);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.getAccessibleContext().setAccessibleDescription("This dialog helps to create a flatfile table from xls,csv and txt sources");
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            String error = null;
            boolean status = false;
            String jdbcUrl = (String)wizardDescriptor.getProperty("url");
            FlatfileDatabaseModel model = (FlatfileDatabaseModel) 
                    wizardDescriptor.getProperty(MashupTableWizardIterator.PROP_FLATFILEDBMODEL);
            List<String> urls = (List<String>)wizardDescriptor.getProperty(
                    MashupTableWizardIterator.URL_LIST);
            List<String> tables = (List<String>) wizardDescriptor.getProperty(
                    MashupTableWizardIterator.TABLE_LIST);
            Connection conn = null;
            Statement stmt = null;
            String dbDir = (DBExplorerUtil.parseConnUrl(jdbcUrl))[1];
            try {
                conn = FlatfileDBConnectionFactory.getInstance().getConnection(jdbcUrl);
                if(conn != null) {
                    conn.setAutoCommit(true);
                    stmt = conn.createStatement();
                }
                
                if(model != null) {
                    Iterator tablesIt = model.getTables().iterator();
                    while(tablesIt.hasNext()) {
                        FlatfileDBTable table = (FlatfileDBTable) tablesIt.next();
                        int i = tables.indexOf(table.getName());
                        ((FlatfileDBTableImpl)table).setOrPutProperty(PropertyKeys.FILENAME,
                                urls.get(i));
                        String sql = table.getCreateStatementSQL();
                        stmt.execute(sql);
                    }
                }
                status = true;
            } catch (Exception ex) {
                ErrorManager.getDefault().log(ex.getMessage());
                status = false;
            } finally {
                if(conn != null) {
                    try {
                        if(stmt != null) {
                            stmt.execute("shutdown");
                        }
                        conn.close();
                        File dbExplorerNeedRefresh = new File(dbDir + "/dbExplorerNeedRefresh");
                        dbExplorerNeedRefresh.createNewFile();
                    } catch (SQLException ex) {
                        conn = null;
                    } catch(Exception ex) {
                        // ignore
                    }
                }
            }
            if(status) {
                String nbBundle2 = mLoc.t("BUND503: Table successfully created.");
                NotifyDescriptor d =
                        new NotifyDescriptor.Message(nbBundle2.substring(15), NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            } else {
                String nbBundle3 = mLoc.t("BUND504: Table creation failed.");
                String msg = nbBundle3.substring(15);
                if(error != null) {
                    msg = msg + "CAUSE:" + error;
                }
                NotifyDescriptor d =
                        new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            }
        }
    }
    
    public String getName() {
        return nbBundle1.substring(15);
    }
    
    @Override
    protected void initialize() {
        super.initialize();
        putValue("noIconInMenu", Boolean.TRUE);
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
