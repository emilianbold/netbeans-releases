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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dm.virtual.db.ui.wizard;

import java.awt.Component;
import java.util.Stack;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.dm.virtual.db.bootstrap.PropertyKeys;
import org.netbeans.modules.dm.virtual.db.model.VirtualDatabaseModel;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBTable;

import org.openide.WizardDescriptor;

public final class VirtualDBTableWizardIterator implements WizardDescriptor.Iterator {

    private WizardDescriptor desc;
    private List<String> tables = new ArrayList<String>();
    public static final String TABLE_INDEX = "tableIndex";
    public static final String TABLE_LIST = "tableList";
    public static final String URL_LIST = "urlList";
    public static final String TABLE_MAP = "tableMap";
    public static final String CONNECTION = "connection";
    public static final String PROP_CURRENTTABLE = "CurrentTable"; // NOI18N
    public static final String PROP_VIRTUALDBMODEL = "VirtualDBModel"; // NOI18N
    private int index;
    private int tracker = -1;
    private WizardDescriptor.Panel[] panels;

    private Stack<Integer> stack = new Stack<Integer>();

    public VirtualDBTableWizardIterator() {
        super();
        addToStack(0);
    }


    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            panels = new WizardDescriptor.Panel[]{
                new SelectDatabasePanel(),
                new FileSelectionPanel(),
                new TableDetailsPanel(),
                new SpreadsheetChooserPanel(),
                new ChooseTablePanel(),
                new ParseContentPanel(),
                new TableDefinitionPanel()
            };
            String[] steps = new String[panels.length];
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                // Default step name to component name of panel.
                steps[i] = c.getName();
                if (c instanceof JComponent) {
                    // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));
                    // Sets steps names for a panel
                    jc.putClientProperty("WizardPanel_contentData", steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE);
                }
            }
        }
        return panels;
    }

    public WizardDescriptor.Panel current() {
        return getPanels()[index];
    }

    public String name() {
        return index + 1 + ". from " + getPanels().length;
    }

    public boolean hasNext() {
        return findNext();
    }

    public boolean hasPrevious() {
        return index > 0 && !stack.isEmpty();
    }

    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        WizardDescriptor.Panel current = current();
        if (current instanceof FileSelectionPanel) {
            tables = (List<String>) desc.getProperty(TABLE_LIST);
            tracker = 0;
            index++;
        } else if (current instanceof TableDefinitionPanel) {
            if (++tracker < tables.size()) {
                index = 2;
            }
        } else if (current instanceof JDBCTablePanel) {
            if (++tracker < tables.size()) {
                index = 2;
            }
        }
        else if (current instanceof SpreadsheetChooserPanel || current instanceof ChooseTablePanel) {
            index = 5;
        } else if (current instanceof TableDetailsPanel) {
            VirtualDBTable tbl = (VirtualDBTable) desc.getProperty(
                    PROP_CURRENTTABLE);
            String type = tbl.getParserType();
            if (type.equals("WEB")) {
                index = 4;
            } else if (type.equals("SPREADSHEET")) {
                index = 3;
            } else {
                index = 5;
            }
        } else {
            index++;
        }
        if (current() instanceof TableDetailsPanel) {
            desc.putProperty(TABLE_INDEX, String.valueOf(tracker));
        }
        addToStack(index);
    }

    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        if (stack.elementAt(stack.size() - 1) != null) {
            if (index == Integer.valueOf(stack.elementAt(stack.size() - 1))) {
                stack.removeElementAt(stack.size() - 1);
            }
            index = Integer.valueOf(stack.elementAt(stack.size() - 1));
        } else {
            index--;
        }
        WizardDescriptor.Panel current = current();
        if (current instanceof TableDefinitionPanel) {
            setNewTable();
        } else if (current instanceof FileSelectionPanel) {
            tracker = -1;
            tables.clear();
        }
    }

    public void setDescriptor(WizardDescriptor wiz) {
        desc = wiz;
    }
    private Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);

    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }

    private boolean findNext() {
        if (tracker != -1) {
            if ((tracker + 1) == tables.size() && index >= 6) {
                return false;
            }
        }
        return true;
    }

    private void setNewTable() {
        tracker--;
        if (tracker < 0) {
            return;
        } else {
            String name = ((List<String>) desc.getProperty(TABLE_LIST)).get(tracker);
            desc.putProperty(TABLE_INDEX, String.valueOf(tracker));
            List<String> urls = (List<String>) desc.getProperty(URL_LIST);
            VirtualDatabaseModel model = (VirtualDatabaseModel) desc.getProperty(VirtualDBTableWizardIterator.PROP_VIRTUALDBMODEL);
            VirtualDBTable tempTable = (VirtualDBTable) desc.getProperty(PROP_CURRENTTABLE);
            if (tempTable != null) {
                if (!model.deleteTable(tempTable.getTableName())) {
                    tempTable.setName("TEMPTBL");
                    model.deleteTable(tempTable.getTableName());
                }
            }
            VirtualDBTable table = ((Map<String, VirtualDBTable>) desc.getProperty(
                    TABLE_MAP)).get(name);
            if (table != null) {
                ((VirtualDBTable) table).setOrPutProperty(PropertyKeys.FILENAME, urls.get(tracker));
                ((VirtualDBTable) table).setOrPutProperty(PropertyKeys.URL, urls.get(tracker));
                desc.putProperty(VirtualDBTableWizardIterator.PROP_CURRENTTABLE, table);
            }
        }
    }

    private void addToStack(int i) {
        try {
            int stackSize = stack.size();
            if (stackSize == 0 && i == 0) {
                stack.add(i);
            } else {
                if (stack.elementAt(stackSize - 1) != null && !stack.elementAt(stackSize - 1).equals(i)) {
                    int last = stack.elementAt(stackSize - 1);
                    if (i > last) {
                        stack.add(i);
                    } else if (i < last && last >= 6) {
                        stack.add(i);
                    }
                }
            }
        } finally {
        }
    }

//    public static void setProjectInfo(String name,String prjInfo, boolean value) {        
//        IS_PROJECT_CALL = value;
//        ETLEditorSupport.PRJ_NAME = name;
//        ETLEditorSupport.PRJ_PATH = DBExplorerUtil.unifyPath(prjInfo);
//    }
//    public static boolean IS_PROJECT_CALL = false;   
}
