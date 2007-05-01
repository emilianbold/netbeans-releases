/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.mashup.db.ui.wizard;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.mashup.db.common.FlatfileDBConnectionFactory;
import org.netbeans.modules.mashup.db.model.FlatfileDBConnectionDefinition;
import org.netbeans.modules.mashup.db.model.FlatfileDBTable;
import org.netbeans.modules.mashup.db.model.FlatfileDatabaseModel;
import org.netbeans.modules.mashup.tables.wizard.MashupTableWizardIterator;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

import com.sun.sql.framework.exception.BaseException;

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
    
    /**
     * @see com.sun.jbi.ui.devtool.flatfile.db.otd.ui.wizard.AbstractWizardPanel#getStepLabel
     */
    public String getStepLabel() {
        return NbBundle.getMessage(PreviewDatabasePanel.class, "STEP_configureotd");
    }
    
    /**
     * @see com.sun.jbi.ui.devtool.flatfile.db.otd.ui.wizard.AbstractWizardPanel#getTitle
     */
    public String getTitle() {
        return (component != null) ? component.getName() : "*** Preview Flat File Database ***";
    }
    
    /**
     * @see com.sun.jbi.ui.devtool.flatfile.db.otd.ui.wizard.AbstractWizardPanel#getTitle
     */
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
        if (settings instanceof WizardDescriptor) {
            WizardDescriptor wd = (WizardDescriptor) settings;
            
            // Don't commit if user didn't click next or finish.
            if (wd.getValue() == WizardDescriptor.NEXT_OPTION || wd.getValue() == WizardDescriptor.FINISH_OPTION) {
                Connection conn = null;
                Statement stmt = null;
                try {
                    FlatfileDatabaseModel model = component.getModel();
                    FlatfileDBConnectionDefinition def = model.getFlatfileDBConnectionDefinition(true);
                    conn = FlatfileDBConnectionFactory.getInstance().getConnection(def.getConnectionURL());
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
