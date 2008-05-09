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
package org.netbeans.modules.sql.framework.ui.view.property;

import com.sun.sql.framework.exception.BaseException;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.ui.ETLDataObject;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.ui.view.BasicTopView;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.openide.nodes.Node;
import org.netbeans.modules.etl.ui.model.impl.ETLCollaborationModel;
import org.netbeans.modules.etl.ui.DataObjectProvider;
import org.netbeans.modules.etl.ui.view.ETLCollaborationTopPanel;
import org.netbeans.modules.etl.ui.view.EditDBModelPanel;
import org.netbeans.modules.sql.framework.model.SQLConstants;

/**
 * @author Ahimanikya Satapathy
 */
public class SQLCollaborationProperties {

    private static transient final Logger mLogger = Logger.getLogger(SQLCollaborationProperties.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    SQLDefinition sqlDef;
    BasicTopView editor;

    public SQLCollaborationProperties(SQLDefinition def, BasicTopView editor) {
        this.sqlDef = def;
        this.editor = editor;
    }

    public PropertyEditor getCustomEditor(Node.Property property) {
        if (property.getName().equals("targetConnections")) {
            return new DBModelEditor(SQLConstants.TARGET_DBMODEL);
        } else if (property.getName().equals("sourceConnections")) {
            return new DBModelEditor(SQLConstants.SOURCE_DBMODEL);
        }
        return null;
    }
    
    /**
     * Gets display name of this table.
     * 
     * @return table disply name.
     */
    public String getDisplayName() {
        return sqlDef.getDisplayName();
    }

    /**
     * Gets execution stratergy code set for this collaboration.
     * @return execution stratergy code
     */
    public Integer getExecutionStrategyCode() {
        return this.sqlDef.getExecutionStrategyCode();
    }

    /**
     * Sets display name to given value.
     *
     * @param newName new display name
     */
    public void setDisplayName(String newName) {
        this.sqlDef.setDisplayName(newName);
    }

    /**
     * Sets execution stratergy codefor this collaboration.
     * @param code execution stratergy code
     */
    public void setExecutionStrategyCode(Integer code) {
        this.sqlDef.setExecutionStrategyCode(code);
        setDirty(true);
    }

    public void setAxiondbWorkingDirectory(String appDataRoot) {
        this.sqlDef.setAxiondbWorkingDirectory(appDataRoot);
        setDirty(true);
    }

    public void setAxiondbDataDirectory(String dbInstanceName) {
        this.sqlDef.setAxiondbDataDirectory(dbInstanceName);
        setDirty(true);
    }

    public String getAxiondbWorkingDirectory() {
        return this.sqlDef.getAxiondbWorkingDirectory();
    }

    public String getAxiondbDataDirectory() {
        return this.sqlDef.getAxiondbDataDirectory();
    }
    
    public boolean isDynamicFlatFile() {
        return this.sqlDef.isDynamicFlatFile();
    }
    
    public boolean getDynamicFlatFile() {
        return isDynamicFlatFile();
    }
    
    public void setDynamicFlatFile(boolean flag) {
        this.sqlDef.setDynamicFlatFile(flag);
    }
    

    public String getSourceConnections() throws BaseException {
        String conNames = "";
        //Check if the list is zero
        int i = 0;
        for (SQLDBModel dbmodel : sqlDef.getSourceDatabaseModels()) {
            if (i++ > 0) {
                conNames += "," + dbmodel.getModelName();
            } else {
                conNames += dbmodel.getModelName();
            }
        }
        return conNames;
    }

    public String getTargetConnections() {
        String conNames = "";
        //Check if the list is zero
        int i = 0;
        for (SQLDBModel dbmodel : sqlDef.getTargetDatabaseModels()) {
            if (i++ > 0) {
                conNames += "," + dbmodel.getModelName();
            } else {
                conNames += dbmodel.getModelName();
            }
        }
        return conNames;
    }

    protected void setDirty(boolean dirty) {
        editor.setDirty(dirty);
    }

    public static class DBModelEditor extends PropertyEditorSupport {

        int modelType;

        public DBModelEditor(int type) {
            super();
            modelType = type;
        }

        @Override
        public Component getCustomEditor() {
            ETLCollaborationTopPanel etlEditor = null;
            try {
                etlEditor = DataObjectProvider.getProvider().getActiveDataObject().getETLEditorTopPanel();
            } catch (Exception ex) {
                // ignore
            }
            ETLCollaborationModel collabModel = DataObjectProvider.getProvider().getActiveDataObject().getModel();
            if (etlEditor != null && collabModel != null) {
                String nbBundle7 = mLoc.t("BUND163: Modify design-time database properties for this session.");
                JLabel panelTitle = new JLabel(nbBundle7.substring(15));
                panelTitle.getAccessibleContext().setAccessibleName(nbBundle7.substring(15));
                panelTitle.setDisplayedMnemonic(nbBundle7.substring(15).charAt(0));
                panelTitle.setFont(panelTitle.getFont().deriveFont(Font.BOLD));
                panelTitle.setFocusable(false);
                panelTitle.setHorizontalAlignment(SwingConstants.LEADING);
                ETLDataObject dObj = DataObjectProvider.getProvider().getActiveDataObject();

                EditDBModelPanel editPanel = new EditDBModelPanel(dObj, modelType);

                JPanel contentPane = new JPanel();
                contentPane.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
                contentPane.setLayout(new BorderLayout());
                contentPane.add(panelTitle, BorderLayout.NORTH);
                contentPane.add(editPanel, BorderLayout.CENTER);
                return contentPane;
            }
            return null;
        }

        @Override
        public boolean supportsCustomEditor() {
            return true;
        }
    }
}

