package org.netbeans.modules.mashup.tables.wizard;

import java.awt.Component;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.netbeans.modules.mashup.db.bootstrap.TemplateFactory;
import org.netbeans.modules.mashup.db.common.PropertyKeys;
import org.netbeans.modules.mashup.db.model.FlatfileDBTable;
import org.netbeans.modules.mashup.db.model.FlatfileDatabaseModel;
import org.netbeans.modules.mashup.db.model.impl.FlatfileDBTableImpl;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 * Panel for configuring table details such as table type,etc.
 * @author karthik
 */
public class TableDetailsPanel implements WizardDescriptor.Panel {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private Component component;
    private int currentIndex = -1;
    private WizardDescriptor wd;

    public Component getComponent() {
        if (component == null) {
            component = new TableDetailsVisualPanel(this);
        }
        return component;
    }

    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    public boolean isValid() {
        return canAdvance();
    }
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);

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

    public void readSettings(Object settings) {
        if (settings instanceof WizardDescriptor) {
            wd = (WizardDescriptor) settings;
            FlatfileDatabaseModel model = (FlatfileDatabaseModel) wd.getProperty(
                    MashupTableWizardIterator.PROP_FLATFILEDBMODEL);
            int index = Integer.parseInt((String) wd.getProperty(MashupTableWizardIterator.TABLE_INDEX));
            currentIndex = index;
            String jdbcUrl = (String) wd.getProperty("url");
            List tables = (List) wd.getProperty(MashupTableWizardIterator.TABLE_LIST);
            List urls = (List) wd.getProperty(MashupTableWizardIterator.URL_LIST);
            Map map = (Map) wd.getProperty(MashupTableWizardIterator.TABLE_MAP);
            FlatfileDBTable tbl = (FlatfileDBTable) map.get(tables.get(index));
            FlatfileDBTable table = null;
            String fileName = null;
            if (tbl == null) {
                table = new FlatfileDBTableImpl();
                File f = new File((String) tables.get(index));
                if (f.exists()) {
                    table.setFileName(f.getName());
                    table.setLocalFilePath(f.getAbsoluteFile());
                    fileName = f.getName();
                } else {
                    fileName = (String) urls.get(index);
                }
            } else {
                table = tbl;
                fileName = (String) urls.get(index);
            }
            ((FlatfileDBTableImpl) table).setOrPutProperty(PropertyKeys.URL, urls.get(index));
            ((TableDetailsVisualPanel) getComponent()).setCurrentTable(table);
            ((TableDetailsVisualPanel) getComponent()).setFileName(fileName);
            ((TableDetailsVisualPanel) getComponent()).setJDBCUrl(jdbcUrl);
            ((TableDetailsVisualPanel) getComponent()).setResourceUrl((String) urls.get(index));
            ((TableDetailsVisualPanel) getComponent()).guessParserType(table);
            ((TableDetailsVisualPanel) getComponent()).setDBModel(model);
            wd.putProperty(MashupTableWizardIterator.PROP_CURRENTTABLE, table);
        }
    }

    public void storeSettings(Object settings) {
        if (settings instanceof WizardDescriptor) {
            WizardDescriptor wizDes = (WizardDescriptor) settings;
            FlatfileDatabaseModel model = (FlatfileDatabaseModel) wd.getProperty(
                    MashupTableWizardIterator.PROP_FLATFILEDBMODEL);
            int index = Integer.parseInt((String) wd.getProperty(MashupTableWizardIterator.TABLE_INDEX));
            List tables = (List) wizDes.getProperty(MashupTableWizardIterator.TABLE_LIST);
            Map map = (Map) wizDes.getProperty(MashupTableWizardIterator.TABLE_MAP);
            if (map == null) {
                map = new HashMap<String, FlatfileDBTable>();
            }
            List urls = (List) wd.getProperty(MashupTableWizardIterator.URL_LIST);
            if (currentIndex == index) {
                FlatfileDBTableImpl table = (FlatfileDBTableImpl) wd.getProperty(MashupTableWizardIterator.PROP_CURRENTTABLE);
                String tblName = ((TableDetailsVisualPanel) getComponent()).getTableName();
                String type = ((TableDetailsVisualPanel) getComponent()).getTableType();
                if (tblName != null && type != null && !tblName.equals("") && !type.equals("")) {
                    table.setName(tblName);
                    table.setParseType(type);
                    table.setEncodingScheme(((TableDetailsVisualPanel) getComponent()).getEncoding());
                    if (type.equals(PropertyKeys.DELIMITED) ||
                            type.equals(PropertyKeys.FIXEDWIDTH)) {
                        File f = new File((String) urls.get(index));
                        if (f.exists()) {
                            table.setLocalFilePath(f.getAbsoluteFile());
                            table.setFileName(f.getName());
                        } else {
                            table.setOrPutProperty(PropertyKeys.FILENAME, urls.get(index));
                        }
                    } else {
                        table.setOrPutProperty(PropertyKeys.FILENAME, urls.get(index));
                    }
                    tables.set(index, table.getName());
                    map.put(tblName, table);
                    wizDes.putProperty(MashupTableWizardIterator.TABLE_LIST, tables);
                    wizDes.putProperty(MashupTableWizardIterator.TABLE_MAP, map);
                    table.setProperties(TemplateFactory.getProperties(type));
                    table.setOrPutProperty(PropertyKeys.URL, urls.get(index));
                    model.addTable(table);
                    currentIndex = -1;
                }
            }
        }
    }

    private boolean canAdvance() {
        return ((TableDetailsVisualPanel) getComponent()).canAdvance();
    }
}
