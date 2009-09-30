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
package org.netbeans.modules.mashup.db.ui.wizard;

import java.awt.BorderLayout;
import java.util.MissingResourceException;

import javax.swing.JPanel;

import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.mashup.db.model.FlatfileDatabaseModel;
import org.netbeans.modules.mashup.db.ui.FlatfileTreeTableView;
import org.netbeans.modules.mashup.db.ui.model.FlatfileTreeTableModel;



/**
 * Descriptor for single panel to select tables and columns to be included in an Flatfile
 * Database definition instance.
 * 
 * @author Jonathan Giron
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class PreviewDatabaseVisualPanel extends JPanel {

    private static transient final Logger mLogger = Logger.getLogger(PreviewDatabaseVisualPanel.class.getName());
    
    private static transient final Localizer mLoc = Localizer.get();
    /* Container to hold configuration components */
    private JPanel contentPanel;

    /* Model used to supply content to FlatfileTreeTableView instance */
    private FlatfileTreeTableModel mTreeModel;

    /* Component which displays content to be configured. */
    private FlatfileTreeTableView mTreeView;

    /**
     * Constructs a new instance of PreviewDatabaseVisualPanel with the given
     * PreviewDatabasePanel instance as its owner.
     * 
     * @param panelHost owner of this new instance
     */
    public PreviewDatabaseVisualPanel(PreviewDatabasePanel panelHost) {
        mTreeModel = new FlatfileTreeTableModel();
        mTreeView = new FlatfileTreeTableView();

        setLayout(new BorderLayout());
        String nbBundle1 = mLoc.t("BUND226: Preview Flat File Database Definition");
        try {
            setName(nbBundle1.substring(15));
        } catch (MissingResourceException e) {
            setName("*** Preview Flatfile Database ***");
        }
    }
    
     /**
     * Constructs a new instance of PreviewDatabaseVisualPanel
     * 
     * @param panelHost owner of this new instance
     */
    public PreviewDatabaseVisualPanel() {
        mTreeModel = new FlatfileTreeTableModel();
        mTreeView = new FlatfileTreeTableView();

        setLayout(new BorderLayout());
        String nbBundle2 = mLoc.t("BUND226: Preview Flat File Database Definition");
        try {
            setName(nbBundle2.substring(15));
        } catch (MissingResourceException e) {
            setName("*** Preview Flatfile Database ***");
        }
    }

    /**
     * Gets FlatfileDatabaseModel representing current contents of this visual component.
     * 
     * @return FlatfileDatabaseModel representing the contents of this visual component
     */
    public FlatfileDatabaseModel getModel() {
        if (mTreeModel != null) {
            FlatfileDatabaseModel modFolder = mTreeModel.getModel();
            return modFolder;
        }
        return null;
    }

    /**
     * Indicates whether the controls in this panel all have sufficient valid data to
     * advance the wizard to the next panel.
     * 
     * @return true if data are valid, false otherwise
     */
    public boolean hasValidData() {
        return true;
    }

    /**
     * Sets data model of this visual component to the contents of the given
     * FlatfileDatabaseModel.
     * 
     * @param newModel FlatfileDatabaseModel whose contents will be rendered by this
     *        component
     */
    public void setModel(FlatfileDatabaseModel newModel) {
        mTreeModel.configureModel(newModel);
        mTreeView.setModel(mTreeModel);

        if (contentPanel == null) {
            contentPanel = createContentPanel(newModel, mTreeView);
            add(contentPanel, BorderLayout.CENTER);
        }

        mTreeView.revalidate();
        mTreeView.repaint();
    }

    private JPanel createContentPanel(FlatfileDatabaseModel folder, FlatfileTreeTableView view) {
        JPanel outermost = new JPanel(new BorderLayout());

        mTreeView.setModel(mTreeModel);
        mTreeView.setDividerLocation(190);
        outermost.add(mTreeView, BorderLayout.CENTER);
        outermost.setSize(300, 200);
        return outermost;
    }
}

