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
package org.netbeans.modules.mashup.db.ui.wizard;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.mashup.db.common.FlatfileDBConnectionFactory;
import org.netbeans.modules.mashup.db.model.FlatfileDBTable;
import org.netbeans.modules.mashup.db.model.FlatfileDatabaseModel;
import org.netbeans.modules.mashup.tables.wizard.MashupTableWizardIterator;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;

import com.sun.sql.framework.exception.BaseException;
import java.io.File;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.common.utils.DBExplorerUtil;
import org.netbeans.modules.sql.framework.model.DBConnectionDefinition;

/**
 * Wizard panel to select tables and columns to be included in an Flatfile DB instance.
 *
 * @author Jonathan Giron
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class PreviewDatabasePanel extends AbstractWizardPanel implements 
        ActionListener, WizardDescriptor.FinishablePanel {
    
    private PreviewDatabaseVisualPanel component;
    private static transient final Logger mLogger = Logger.getLogger(PreviewDatabasePanel.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    /** Creates a new instance of PreviewDatabasePanel */
    public PreviewDatabasePanel() {
        component = new PreviewDatabaseVisualPanel(this);
        component.setPreferredSize(new Dimension(400,350));
    }
    
    /**
     * Invoked when an action occurs.
     *
     * @param e ActionEvent to handle
     */
    public void actionPerformed(ActionEvent e) {
    }
    
    /**
     * @see org.openide.WizardDescriptor.Panel#getComponent
     */
    public java.awt.Component getComponent() {
        return component;
    }
    
    /**
     * @see org.openide.WizardDescriptor.Panel#getHelp
     */
    public org.openide.util.HelpCtx getHelp() {
        return null;
    }
    
    public String getStepLabel() {
        String nbBundle1 = mLoc.t("BUND226: Preview Flat File Database Definition");
        return nbBundle1.substring(15);
    }
    
    public String getTitle() {
        return (component != null) ? component.getName() : "*** Preview Flat File Database ***";
    }
    
    public boolean isValid() {
        return component.hasValidData();
    }
    
    /**
     * @see org.openide.WizardDescriptor.Panel#readSettings
     */
    public void readSettings(Object settings) {
        if (settings instanceof WizardDescriptor) {
            WizardDescriptor wd = (WizardDescriptor) settings;
            
            FlatfileDatabaseModel folder = (FlatfileDatabaseModel) wd.getProperty(MashupTableWizardIterator.PROP_FLATFILEDBMODEL);
            if (folder == null || folder.getTables().size() == 0) {
                throw new IllegalStateException("Context must contain a populated FlatfileDatabaseModel.");
            }
            
            component.setModel(folder);
            super.fireChangeEvent();
        }
    }
    
    /**
     * @see org.openide.WizardDescriptor.Panel#storeSettings
     */
    public void storeSettings(Object settings) {
        String dbDir = null;
        if (settings instanceof WizardDescriptor) {
            WizardDescriptor wd = (WizardDescriptor) settings;
            
            // Don't commit if user didn't click next or finish.
            if (wd.getValue() == WizardDescriptor.NEXT_OPTION || wd.getValue() == WizardDescriptor.FINISH_OPTION) {
                Connection conn = null;
                Statement stmt = null;
                try {
                    FlatfileDatabaseModel model = component.getModel();
                    DBConnectionDefinition def = model.getFlatfileDBConnectionDefinition(true);
                    conn = FlatfileDBConnectionFactory.getInstance().getConnection(def.getConnectionURL());
                    dbDir = (DBExplorerUtil.parseConnUrl(def.getConnectionURL()))[1];
                    List tables = model.getTables();
                    Iterator it = tables.iterator();
                    while(it.hasNext()) {
                        FlatfileDBTable table = (FlatfileDBTable)it.next();
                        String sql = table.getCreateStatementSQL(table.getLocalFilePath(), table.getTableName(),
                                null, false, true);
                        try {
                            stmt = conn.createStatement();
                            stmt.execute(sql);
                        } catch (Exception ex) {
                            //ignore
                        }
                    }
                } catch (BaseException ex) {
                    //ignore
                }
                if(conn != null) {
                    try {
                        conn.commit();
                    } catch (Exception ex) {
                        //ignore
                    } finally {
                        try {
                            stmt.execute("shutdown");
                            stmt.close();
                            conn.close();
                            if(dbDir != null){
                                File dbExplorerNeedRefresh = new File(dbDir + "/dbExplorerNeedRefresh");
                                dbExplorerNeedRefresh.createNewFile();
                            }
                        } catch (Exception ex) {
                            //ignore
                        }
                    }
                } else {
                    NotifyDescriptor d =
                            new NotifyDescriptor.Message("Table(s) creation failed.", NotifyDescriptor.INFORMATION_MESSAGE);
                    DialogDisplayer.getDefault().notify(d);
                }
            }
        }
    }

    public boolean isFinishPanel() {
        return true;
    }
}
