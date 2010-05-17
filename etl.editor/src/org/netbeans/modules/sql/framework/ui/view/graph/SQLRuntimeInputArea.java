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

package org.netbeans.modules.sql.framework.ui.view.graph;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

import org.netbeans.modules.etl.ui.DataObjectProvider;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.ui.graph.ICommand;
import org.netbeans.modules.sql.framework.ui.graph.impl.GradientBrush;

import com.nwoods.jgo.JGoBrush;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;


/**
 * @author Ritesh Adval
 */
public class SQLRuntimeInputArea extends SQLBasicTableArea {

    private static URL runtimeInputImgUrl = SQLBasicTableArea.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/RuntimeInput.png");

    private static URL propertiesUrl = SQLBasicTableArea.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/properties.png");

    private static final Color DEFAULT_BG_COLOR = new Color(204, 213, 241);
    
    private static final Color DEFAULT_BG_COLOR_DARK = new Color(165, 193, 249);
    
    private static final JGoBrush DEFAULT_TITLE_BRUSH = new GradientBrush(DEFAULT_BG_COLOR_DARK, DEFAULT_BG_COLOR);    

    private JMenuItem editRuntimeItem;
    
    private static transient final Logger mLogger = Logger.getLogger(SQLRuntimeInputArea.class.getName());
    
    private static transient final Localizer mLoc = Localizer.get();

    /**
     * Creates a new instance of SQLRuntimeInputArea
     */
    public SQLRuntimeInputArea() {
        super();
    }

    /**
     * Creates a new instance of SQLRuntimeInputArea
     * 
     * @param table the table to render
     */
    public SQLRuntimeInputArea(SQLDBTable table) {
        super(table);
    }

    protected void initializePopUpMenu() {
        ActionListener aListener = new TableActionListener();
        //      edit runtime
        String nbBundle1 = mLoc.t("BUND451: Edit");
        String lbl = nbBundle1.substring(15);
        editRuntimeItem = new JMenuItem(lbl, new ImageIcon(propertiesUrl));
        editRuntimeItem.addActionListener(aListener);
        popUpMenu.add(editRuntimeItem);

        addSelectVisibleColumnsPopUpMenu(aListener);
        popUpMenu.addSeparator();
        addRemovePopUpMenu(aListener);

    }

    Icon createIcon() {
        return new ImageIcon(runtimeInputImgUrl);
    }

    private class TableActionListener implements ActionListener {
        /**
         * Invoked when an action occurs.
         * 
         * @param e ActionEvent to handle
         */
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();

            if (source == editRuntimeItem) {
                EditRuntime_ActionPerformed(e);
            } else {
                handleCommonActions(e);
            }
        }
    }

    private void EditRuntime_ActionPerformed(ActionEvent e) {
        Object[] args = new Object[] { new Integer(table.getObjectType())};
        this.getGraphView().execute(ICommand.ADD_RUNTIME_CMD, args);
        if(DataObjectProvider.getProvider().getActiveDataObject().getModel().isDirty()) {
            DataObjectProvider.getProvider().getActiveDataObject().getETLEditorSupport().synchDocument();
        }
    }

    public void setConditionIcons() {
        //do nothing
    }
    
    /**
     * @see org.netbeans.modules.sql.framework.ui.view.graph.SQLBasicTableArea#getDefaultTitleBrush()
     */
    protected JGoBrush getDefaultTitleBrush() {
        return DEFAULT_TITLE_BRUSH;
    }

    /**
     * @see org.netbeans.modules.sql.framework.ui.view.graph.SQLBasicTableArea#getDefaultBackgroundColor()
     */
    protected Color getDefaultBackgroundColor() {
        return DEFAULT_BG_COLOR;
    }
}

