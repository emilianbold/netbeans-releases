/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
 */
package org.netbeans.modules.etl.ui.view.graph.actions;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.netbeans.modules.etl.ui.ETLDataObject;
import org.netbeans.modules.etl.ui.view.ConfigParamsTreeView;
import org.netbeans.modules.etl.ui.view.ConfigureParametersPanel;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.impl.SQLDefinitionImpl;
import org.netbeans.modules.sql.framework.ui.editor.property.IPropertySheet;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.DialogDescriptor;
import org.openide.util.actions.CookieAction;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import com.sun.sql.framework.exception.BaseException;
import net.java.hulp.i18n.Logger;
import com.sun.sql.framework.utils.XmlUtil;
import org.netbeans.modules.etl.logger.Localizer;
import org.openide.awt.StatusDisplayer;

/**
 * Action class used for configuring ETL Collaborations.
 *
 * @author karthik
 */
public final class ConfigureParametersAction extends CookieAction {

    private final String PATH_SUFFIX = "\\..\\..\\nbproject\\config\\";
    private final String CONF_FILE = ".conf";
    private static transient final Logger mLogger = Logger.getLogger(ConfigureParametersAction.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    private File configFile;
    ETLDataObject mObj;

    protected void performAction(Node[] activatedNodes) {
        ETLDataObject dObj = activatedNodes[0].getCookie(ETLDataObject.class);
        String msg = null;
        boolean syncReqd = true;
        if (dObj != null) {
            this.mObj = dObj;
            dObj.getPrimaryFile().refresh();
            String path = dObj.getPrimaryFile().getPath() + PATH_SUFFIX;
            configFile = new File(path);
            if (!configFile.exists()) {
                configFile.mkdir();
            }
            path = path + dObj.getName() + CONF_FILE;
            configFile = new File(path);
            FileWriter writer = null;
            if (!configFile.exists()) {
                try {
                    // This one doesn't work if ETL Collaboration is not yet opened. Only after opening
                    // the ETL Collab in the editor, the db models are updated into the SQL definition.
                    //writer.write(dObj.getETLDefinition().getSQLDefinition().toXMLString(""));
                    writeToConfigFile(configFile, createSQLDefinition(dObj));

                    // SQL Definition is not necessary. Probably should be using dbModels directly.
                    // for simplicity sake, using sql defn as of now.
                    syncReqd = false;
                } catch (Exception ex) {
                    // ignore
                }
            }
            if (configFile.exists()) {
                if (syncReqd) {
                    syncConfigWithCollab(configFile, dObj);
                }
                showConfigPanel();
            } else {
                StatusDisplayer.getDefault().setStatusText("\nFailed to create the config file.");
            }
        } else {
            StatusDisplayer.getDefault().setStatusText("\nFailed to initialize.");
        }
    }

    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }

    public String getName() {
        String nbBundle1 = mLoc.t("BUND017: Configure Parameters");
        return nbBundle1.substring(15);
    }

    protected Class[] cookieClasses() {
        return new Class[]{ETLDataObject.class};
    }

    @Override
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE);
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    /**
     *
     *
     */
    public void showConfigPanel() {
        String nbBundle2 = mLoc.t("BUND018: Configure Deployment Parameters");
        JLabel panelTitle = new JLabel(nbBundle2.substring(15));
        panelTitle.getAccessibleContext().setAccessibleName(nbBundle2.substring(15));
        panelTitle.setDisplayedMnemonic(nbBundle2.substring(15).charAt(0));
        panelTitle.setFont(panelTitle.getFont().deriveFont(Font.BOLD));
        panelTitle.setFocusable(false);
        panelTitle.setHorizontalAlignment(SwingConstants.LEADING);

        ConfigureParametersPanel editPanel = new ConfigureParametersPanel(mObj);

        JPanel contentPane = new JPanel();
        contentPane.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        contentPane.setLayout(new BorderLayout());
        contentPane.add(panelTitle, BorderLayout.NORTH);
        contentPane.add(editPanel, BorderLayout.CENTER);


        DialogDescriptor dd = new DialogDescriptor(contentPane, "Configure Deployment Parameters");
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
        dlg.getAccessibleContext().setAccessibleDescription("This is a dialog to configure Deployment Parameters");
        dlg.setSize(new Dimension(600, 450));
        dlg.setVisible(true);
        if (NotifyDescriptor.OK_OPTION.equals(dd.getValue())) {
            ConfigParamsTreeView configModelTreeView = editPanel.getConfigModelTreeView();
            if (configModelTreeView != null) {
                IPropertySheet propSheet = configModelTreeView.getPropSheet();
                if (propSheet != null) {
                    propSheet.commitChanges();
                }
                SQLDefinition defn = configModelTreeView.getData();
                if (defn != null) {
                    this.configFile.delete();
                    try {
                        writeToConfigFile(this.configFile, defn);
                        StatusDisplayer.getDefault().setStatusText("\nDeployment parameters successfully updated.");
                    } catch (IOException ex) {
                        StatusDisplayer.getDefault().setStatusText("\nFailed to update changes.");
                    } catch (BaseException baseEx) {
                        StatusDisplayer.getDefault().setStatusText("\nFailed to read SQL Definition.");
                    }
                }
            }
        } else {
            StatusDisplayer.getDefault().setStatusText("\nAll the edits are discarded.");
        }
    }

    private void syncConfigWithCollab(File configFile, ETLDataObject dObj) {
        SQLDefinition srcDefn = createSQLDefinition(dObj);
        SQLDefinition confDefn = getConfigData(configFile);

        // sync Source db Models.
        confDefn = compareAndSync(srcDefn, confDefn, true);
        // sync Target db Models.
        confDefn = compareAndSync(srcDefn, confDefn, false);
        try {

            // write new config data into config file.
            writeToConfigFile(configFile, confDefn);
        } catch (Exception ex) {
            mLogger.infoNoloc(mLoc.t("EDIT022: ConfigureParametersAction.class.getName(){0}", ex.getMessage()));
        }
    }

    private SQLDefinition compareAndSync(SQLDefinition srcDefn, SQLDefinition tgtDefn, boolean isSource) {
        // sync the Models.
        Iterator it = null;
        if (isSource) {
            it = srcDefn.getSourceDatabaseModels().iterator();
        } else {
            it = srcDefn.getTargetDatabaseModels().iterator();
        }
        while (it.hasNext()) {
            boolean exists = false;
            SQLDBModel match = null;
            SQLDBModel srcModel = (SQLDBModel) it.next();
            Iterator confIterator = null;
            if (isSource) {
                confIterator = tgtDefn.getSourceDatabaseModels().iterator();
            } else {
                confIterator = tgtDefn.getTargetDatabaseModels().iterator();
            }
            while (confIterator.hasNext()) {
                SQLDBModel tgtModel = (SQLDBModel) confIterator.next();
                if (tgtModel.getModelName().equals(srcModel.getModelName())) {
                    exists = true;
                    match = tgtModel;
                    break;
                }
            }
            if (exists) {
                try {
                    tgtDefn.removeObject(match);
                    match = syncTables(srcModel, match);
                    tgtDefn.addObject(match);
                } catch (BaseException ex) {
                    // ignore
                }
            } else {
                try {
                    tgtDefn.addObject(srcModel);
                } catch (BaseException ex) {
                    // ignore
                }
            }
        }
        return tgtDefn;
    }

    private SQLDBModel syncTables(SQLDBModel srcModel, SQLDBModel tgtModel) {
        Iterator it = srcModel.getTables().iterator();
        while (it.hasNext()) {
            boolean exists = false;
            SQLDBTable srcTbl = (SQLDBTable) it.next();
            Iterator confIterator = tgtModel.getTables().iterator();
            while (confIterator.hasNext()) {
                SQLDBTable tgtTbl = (SQLDBTable) confIterator.next();
                if (tgtTbl.getName().equals(srcTbl.getName())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                tgtModel.addTable(srcTbl);
            }
        }
        return tgtModel;
    }

    private SQLDefinition getConfigData(File configFile) {
        org.w3c.dom.Node rootNode = null;
        SQLDefinition sqlDefn = null;
        try {
            Element element = XmlUtil.loadXMLFile(new BufferedReader(new FileReader(configFile)));
            rootNode = (org.w3c.dom.Node) element;
        } catch (Exception ex) {
            mLogger.infoNoloc(mLoc.t("EDIT022: ConfigureParametersAction.class.getName(){0}", ex.getMessage()));
        }
        if (rootNode != null) {
            org.w3c.dom.Node sqlNode = rootNode.getFirstChild();
            try {
                sqlDefn = new SQLDefinitionImpl((Element) sqlNode);
            } catch (Exception ex) {
                mLogger.infoNoloc(mLoc.t("EDIT022: ConfigureParametersAction.class.getName(){0}", ex.getMessage()));
            }
        }
        return sqlDefn;
    }

    private SQLDefinition createSQLDefinition(ETLDataObject dObj) {
        Element element = null;
        try {
            element = XmlUtil.loadXMLFile(new BufferedReader(new FileReader(FileUtil.toFile(dObj.getPrimaryFile()))));
        } catch (FileNotFoundException ex) {
            // ignore
        }
        SQLDefinition sqlDefn = null;
        if (element != null) {
            NodeList list = element.getElementsByTagName("sqlDefinition");
            Element sqlElement = (Element) list.item(0);
            try {
                sqlDefn = new SQLDefinitionImpl(sqlElement);
            } catch (BaseException ex) {
                // ignore
            }
        }
        return sqlDefn;
    }

    private void writeToConfigFile(File configFile, SQLDefinition confDefn) throws IOException, BaseException {
        if (configFile.exists()) {
            configFile.delete();
        }
        configFile.createNewFile();
        FileWriter writer = new FileWriter(configFile);
        writer.write("<ETLConfig>");
        writer.write(confDefn.toXMLString(""));
        writer.write("</ETLConfig>");
        writer.close();
    }
}
