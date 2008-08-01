package org.netbeans.modules.mashup.tables.wizard;

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
import org.netbeans.modules.etl.ui.ETLEditorSupport;
import org.netbeans.modules.mashup.db.common.PropertyKeys;
import org.netbeans.modules.mashup.db.model.FlatfileDBTable;
import org.netbeans.modules.mashup.db.model.FlatfileDatabaseModel;
import org.netbeans.modules.mashup.db.model.impl.FlatfileDBTableImpl;
import org.netbeans.modules.mashup.db.ui.wizard.ParseContentPanel;
import org.netbeans.modules.mashup.db.ui.wizard.SelectDatabasePanel;
import org.netbeans.modules.mashup.db.ui.wizard.TableDefinitionPanel;
import org.netbeans.modules.sql.framework.common.utils.DBExplorerUtil;
import org.openide.WizardDescriptor;

public final class MashupTableWizardIterator implements WizardDescriptor.Iterator {

    private WizardDescriptor desc;
    private List<String> tables = new ArrayList<String>();
    public static final String TABLE_INDEX = "tableIndex";
    public static final String TABLE_LIST = "tableList";
    public static final String URL_LIST = "urlList";
    public static final String TABLE_MAP = "tableMap";
    public static final String CONNECTION = "connection";
    /** Property key: current flatfile */
    public static final String PROP_CURRENTTABLE = "CurrentTable"; // NOI18N
    /** Property key: FlatfileDBModel instances */
    public static final String PROP_FLATFILEDBMODEL = "FlatfileDBModel"; // NOI18N
    private int index;
    private int tracker = -1;
    private WizardDescriptor.Panel[] panels;
    //private Deque<Integer> stack = new ArrayDeque<Integer>();
    private Stack<Integer> stack = new Stack<Integer>();

    public MashupTableWizardIterator() {
        super();
        addToStack(0);
    }

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
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
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i));
                    // Sets steps names for a panel
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE);
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE);
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
        } else if (current instanceof SpreadsheetChooserPanel || current instanceof ChooseTablePanel) {
            index = 5;
        } else if (current instanceof TableDetailsPanel) {
            FlatfileDBTable tbl = (FlatfileDBTable) desc.getProperty(
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
            FlatfileDatabaseModel model = (FlatfileDatabaseModel) desc.getProperty(MashupTableWizardIterator.PROP_FLATFILEDBMODEL);
            FlatfileDBTable tempTable = (FlatfileDBTable) desc.getProperty(PROP_CURRENTTABLE);
            if (tempTable != null) {
                if (!model.deleteTable(tempTable.getTableName())) {
                    tempTable.setName("TEMPTBL");
                    model.deleteTable(tempTable.getTableName());
                }
            }
            FlatfileDBTable table = ((Map<String, FlatfileDBTable>) desc.getProperty(
                    TABLE_MAP)).get(name);
            if (table != null) {
                ((FlatfileDBTableImpl) table).setOrPutProperty(PropertyKeys.FILENAME, urls.get(tracker));
                ((FlatfileDBTableImpl) table).setOrPutProperty(PropertyKeys.URL, urls.get(tracker));
                desc.putProperty(MashupTableWizardIterator.PROP_CURRENTTABLE, table);
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

    public static void setProjectInfo(String name,String prjInfo, boolean value) {        
        IS_PROJECT_CALL = value;
        ETLEditorSupport.PRJ_NAME = name;
        ETLEditorSupport.PRJ_PATH = DBExplorerUtil.unifyPath(prjInfo);
    }
    public static boolean IS_PROJECT_CALL = false;   
}
