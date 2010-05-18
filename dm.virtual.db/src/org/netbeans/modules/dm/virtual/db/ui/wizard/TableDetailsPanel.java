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
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.netbeans.modules.dm.virtual.db.bootstrap.TemplateFactory;
import org.netbeans.modules.dm.virtual.db.bootstrap.PropertyKeys;
import org.netbeans.modules.dm.virtual.db.model.VirtualDatabaseModel;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBTable;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 * Panel for configuring table details such as table type,etc.
 * @author karthik
 */
public class TableDetailsPanel implements WizardDescriptor.Panel {

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
            VirtualDatabaseModel model = (VirtualDatabaseModel) wd.getProperty(
                    VirtualDBTableWizardIterator.PROP_VIRTUALDBMODEL);
            int index = Integer.parseInt((String) wd.getProperty(VirtualDBTableWizardIterator.TABLE_INDEX));
            currentIndex = index;
            String jdbcUrl = (String) wd.getProperty("url");
            List tables = (List) wd.getProperty(VirtualDBTableWizardIterator.TABLE_LIST);
            List urls = (List) wd.getProperty(VirtualDBTableWizardIterator.URL_LIST);
            Map map = (Map) wd.getProperty(VirtualDBTableWizardIterator.TABLE_MAP);
            VirtualDBTable tbl = (VirtualDBTable) map.get(tables.get(index));
            VirtualDBTable table = null;
            String fileName = null;
            if (tbl == null) {
                table = new VirtualDBTable();
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
            ((VirtualDBTable) table).setOrPutProperty(PropertyKeys.URL, urls.get(index));
            ((TableDetailsVisualPanel) getComponent()).setCurrentTable(table);
            ((TableDetailsVisualPanel) getComponent()).setFileName(fileName);
            ((TableDetailsVisualPanel) getComponent()).setJDBCUrl(jdbcUrl);
            ((TableDetailsVisualPanel) getComponent()).setResourceUrl((String) urls.get(index));
            ((TableDetailsVisualPanel) getComponent()).guessParserType(table);
            ((TableDetailsVisualPanel) getComponent()).setDBModel(model);
            wd.putProperty(VirtualDBTableWizardIterator.PROP_CURRENTTABLE, table);
        }
    }

    public void storeSettings(Object settings) {
        if (settings instanceof WizardDescriptor) {
            WizardDescriptor wizDes = (WizardDescriptor) settings;
            VirtualDatabaseModel model = (VirtualDatabaseModel) wd.getProperty(
                    VirtualDBTableWizardIterator.PROP_VIRTUALDBMODEL);
            int index = Integer.parseInt((String) wd.getProperty(VirtualDBTableWizardIterator.TABLE_INDEX));
            List tables = (List) wizDes.getProperty(VirtualDBTableWizardIterator.TABLE_LIST);
            Map map = (Map) wizDes.getProperty(VirtualDBTableWizardIterator.TABLE_MAP);
            if (map == null) {
                map = new HashMap<String, VirtualDBTable>();
            }
            List urls = (List) wd.getProperty(VirtualDBTableWizardIterator.URL_LIST);
            if (currentIndex == index) {
                VirtualDBTable table = (VirtualDBTable) wd.getProperty(VirtualDBTableWizardIterator.PROP_CURRENTTABLE);
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
                    wizDes.putProperty(VirtualDBTableWizardIterator.TABLE_LIST, tables);
                    wizDes.putProperty(VirtualDBTableWizardIterator.TABLE_MAP, map);
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
