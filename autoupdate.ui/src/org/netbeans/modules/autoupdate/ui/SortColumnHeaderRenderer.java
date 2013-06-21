/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.autoupdate.ui;

import java.awt.Component;
import java.util.prefs.Preferences;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.plaf.UIResource;
import javax.swing.table.TableCellRenderer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbPreferences;

/**
 *
 * @author Radek Matous
 */
public class SortColumnHeaderRenderer implements TableCellRenderer, UIResource {
    private UnitCategoryTableModel model;
    private TableCellRenderer textRenderer;
    private String sortColumn;
    private ImageIcon sortDescIcon;
    private static ImageIcon sortAscIcon;    
    private boolean sortAscending;
    
    public SortColumnHeaderRenderer (UnitCategoryTableModel model, TableCellRenderer textRenderer) {
        this.model = model;
        this.textRenderer = textRenderer;
        sortColumn = getPreferences().get(keyForType("SortingColumn"), getDefaultColumnSelected());// NOI18N
        sortAscending = getPreferences().getBoolean(keyForType("SortAscending"), true);// NOI18N
        this.model.sort (sortColumn, sortAscending);
    }
        
    public Component getTableCellRendererComponent (JTable table, Object value,
            boolean isSelected,
            boolean hasFocus, int row,
            int column) {
        Component text = textRenderer.getTableCellRendererComponent (table, value, isSelected, hasFocus, row, column);
        if( text instanceof JLabel ) {
            JLabel label = (JLabel)text;
            if (table.getColumnModel ().getColumn (column).getIdentifier ().equals (sortColumn)) {
                label.setIcon ( sortAscending ? getSortAscIcon () : getSortDescIcon ());
                label.setHorizontalTextPosition ( SwingConstants.LEFT );
            } else {
                label.setIcon ( null);
            }
        }
        return text;
    }
    
    public void setDefaultSorting () {
        setSorting(getDefaultColumnSelected());
    }
    
    private String getDefaultColumnSelected() {
        String retval = null;
        retval = this.model.getColumnName(2); // category
        return retval;
    }
        
    public void setSorting (Object column) {
        if (!column.equals (sortColumn)) {
            sortColumn = (String)column;
            sortAscending = true;
        } else {
            sortAscending = !sortAscending;
        }
        getPreferences().put(keyForType("SortingColumn"), sortColumn);// NOI18N
        getPreferences().putBoolean(keyForType("SortAscending"), sortAscending);// NOI18N
        this.model.sort (column, sortAscending);
    }
        
    private ImageIcon getSortAscIcon () {
        if (sortAscIcon == null) {
            sortAscIcon = ImageUtilities.loadImageIcon("org/netbeans/modules/autoupdate/ui/resources/columnsSortedAsc.gif", false); // NOI18N
        }
        return sortAscIcon;
    }
    
    private ImageIcon getSortDescIcon () {
        if (sortDescIcon == null) {
            sortDescIcon = ImageUtilities.loadImageIcon("org/netbeans/modules/autoupdate/ui/resources/columnsSortedDesc.gif", false); // NOI18N
        }
        return sortDescIcon;
    }
    
    private String keyForType(String key) {
        return key  + model.getType();
    }
    
    private static Preferences getPreferences() {
        return NbPreferences.forModule(SortColumnHeaderRenderer.class);
    }    
}
