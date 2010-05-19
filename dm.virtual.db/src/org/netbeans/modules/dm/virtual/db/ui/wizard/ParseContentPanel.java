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
package org.netbeans.modules.dm.virtual.db.ui.wizard;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.ChangeListener;
import jxl.Sheet;
import jxl.Workbook;

import org.netbeans.modules.dm.virtual.db.bootstrap.VirtualTableBootstrapParser;
import org.netbeans.modules.dm.virtual.db.bootstrap.VirtualTableBootstrapParserFactory;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBException;
import org.netbeans.modules.dm.virtual.db.api.Property;
import org.netbeans.modules.dm.virtual.db.bootstrap.PropertyKeys;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBColumn;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBTable;
import org.netbeans.modules.dm.virtual.db.ui.property.impl.VirtualDBResource;
import org.netbeans.modules.dm.virtual.db.ui.property.IPropertySheet;
import org.netbeans.modules.dm.virtual.db.ui.property.impl.PropertyViewManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import jxl.Cell;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Captures information needed to determine the parsing configuration of a file to be
 * imported.
 *
 * @author Ahimanikya Satapathy
 */
public class ParseContentPanel implements PropertyChangeListener, VetoableChangeListener, WizardDescriptor.Panel {

    private static transient final Logger mLogger = Logger.getLogger(ParseContentPanel.class.getName());
    private static final String LOG_CATEGORY = ParseContentPanel.class.getName();
    private Map currentPropertyMap;
    private VirtualDBTable currentTable;
    private transient String parseErrors;
    private IPropertySheet propertySheet;
    private int currentIndex = -1;
    private Component component;
    private ParseContentVisualPanel panel;

    public ParseContentPanel() {
    }

    private boolean canAdvance() {
        if (parseErrors != null) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(parseErrors.trim(), NotifyDescriptor.WARNING_MESSAGE));
            parseErrors = null;  // Blank out the (consumed) error message.
            return false;
        }
        return true;
    }

    public boolean hasValidData() {
        return (propertySheet != null) ? propertySheet.getPropertyGroup(NbBundle.getMessage(ParseContentPanel.class, "LBL_layout_recordtable_default")).isValid() : false;
    }

    public boolean isRefreshRequired(VirtualDBTable table, Map newProperties) {
        final Map tableProps = table.getProperties();
        Iterator iter = tableProps.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            Property property = (Property) tableProps.get(key);

            Object newObj = newProperties.get(key);
            Object oldObj = currentPropertyMap.get(key);

            if (newObj != null && oldObj == null) {
                return true;
            }
            if ((!oldObj.equals(newObj)) && property.isRefreshRequired()) {
                return true;
            }
        }
        return false;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        fireChangeEvent();
    }

    public void readSettings(Object settings) {
        if (settings instanceof WizardDescriptor) {
            WizardDescriptor wd = (WizardDescriptor) settings;

            currentTable = (VirtualDBTable) wd.getProperty(VirtualDBTableWizardIterator.PROP_CURRENTTABLE);
            if (currentTable == null) {
                throw new IllegalStateException(NbBundle.getMessage(ParseContentPanel.class, "MSG_NoRef_ToFile"));
            }

            panel.removeAll();
            JLabel instr = new JLabel(NbBundle.getMessage(ParseContentPanel.class, "LBL_instr_import_parse"));
            instr.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ParseContentPanel.class, "LBL_instr_import_parse"));
            instr.setAlignmentX(Component.LEFT_ALIGNMENT);
            instr.setDisplayedMnemonic(NbBundle.getMessage(ParseContentPanel.class, "LBL_instr_import_parse").charAt(0));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weighty = 0;
            panel.add(instr, gbc);
            panel.setPreferredSize(new Dimension(150, 150));
            // do the guess work for the 1st time
            if (0 == currentTable.getColumnList().size()) {
                try {
                    VirtualTableBootstrapParser parser = VirtualTableBootstrapParserFactory.getInstance().
                            getBootstrapParser(currentTable.getParserType());
                    if (parser != null) {
                        parser.makeGuess(currentTable);
                    }
                } catch (VirtualDBException se) {
                    // ignore
                }
            }

            PropertyViewManager pvMgr = getPropertyViewManager();

            String fieldSep = currentTable.getProperty(PropertyKeys.WIZARDCUSTOMFIELDDELIMITER);
            if (!VirtualDBUtil.isNullString(fieldSep)) {
                currentTable.setProperty(PropertyKeys.FIELDDELIMITER, NbBundle.getMessage(ParseContentPanel.class, "LBL_userdefined"));
            }

            currentPropertyMap = Property.createKeyValueMapFrom(currentTable.getProperties());

            propertySheet = pvMgr.getPropertySheet(currentPropertyMap, currentTable.getParserType());

            propertySheet.getPropertyGroup(NbBundle.getMessage(ParseContentPanel.class, "LBL_layout_recordtable_default")).addPropertyChangeListener(this);
            propertySheet.getPropertyGroup(NbBundle.getMessage(ParseContentPanel.class, "LBL_layout_recordtable_default")).addVetoableChangeListener(this);
            JLabel tmp = new JLabel("");
            tmp.setVisible(false);
            tmp.setLabelFor(propertySheet.getPropertySheet());
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.gridx = 0;
            gbc.gridy = GridBagConstraints.RELATIVE;
            gbc.weightx = 50.0;
            gbc.weighty = 55.0;
            panel.add(propertySheet.getPropertySheet(), gbc);

            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.gridx = 0;
            gbc.gridy = GridBagConstraints.RELATIVE;
            gbc.weightx = 50.0;
            gbc.weighty = 45.0;
            panel.add(getPreviewPanel(wd), gbc);

            parseErrors = null;

            currentIndex = Integer.parseInt(
                    (String) wd.getProperty(VirtualDBTableWizardIterator.TABLE_INDEX));
        }
    }

    private static PropertyViewManager getPropertyViewManager() {
        InputStream stream = ParseContentPanel.class.getClassLoader().getResourceAsStream(
                "org/netbeans/modules/dm/virtual/db/ui/property/impl/parse_properties.xml");
        return new PropertyViewManager(stream, new VirtualDBResource());
    }

    public void storeSettings(Object settings) {
        if (settings instanceof WizardDescriptor) {
            WizardDescriptor wd = (WizardDescriptor) settings;

            // Don't commit if user didn't click next.
            if (wd.getValue() != WizardDescriptor.NEXT_OPTION) {
                return;
            }

            if (currentTable == null) {
                currentTable = (VirtualDBTable) wd.getProperty(VirtualDBTableWizardIterator.PROP_CURRENTTABLE);
                if (currentTable == null) {
                    throw new IllegalStateException(NbBundle.getMessage(ParseContentPanel.class, "MSG_NoRef_ToFile"));
                }
            }

            // get url.
            int index = Integer.parseInt((String) wd.getProperty(VirtualDBTableWizardIterator.TABLE_INDEX));
            List<String> urls = (List<String>) wd.getProperty(VirtualDBTableWizardIterator.URL_LIST);

            if (index == currentIndex) {
                currentTable.updateProperties(propertySheet.getPropertyValues());
                currentTable.setProperty("FILENAME", urls.get(index));
                Map newPropertyMap = Property.createKeyValueMapFrom(currentTable.getProperties());

                // If any properties change, rebuild set of fields.
                if (0 == currentTable.getColumnList().size() ||
                        !newPropertyMap.equals(currentPropertyMap)) {
                    try {
                        final String loadType = (String) currentPropertyMap.get(PropertyKeys.LOADTYPE);

                        if (isRefreshRequired(currentTable, newPropertyMap)) {
                            currentTable.deleteAllColumns();
                        }

                        VirtualTableBootstrapParser parser = VirtualTableBootstrapParserFactory.getInstance().
                                getBootstrapParser(currentTable.getParserType());
                        ((VirtualDBTable) currentTable).setOrPutProperty(PropertyKeys.URL,
                                urls.get(index));
                        ((VirtualDBTable) currentTable).setOrPutProperty(PropertyKeys.FILENAME,
                                urls.get(index));
                        List colList = parser.buildVirtualDBColumns(currentTable);
                        if (colList != null && !colList.isEmpty()) {
                            currentTable.deleteAllColumns();
                            Iterator iter = colList.iterator();
                            try {
                                while (iter.hasNext()) {
                                    currentTable.addColumn((VirtualDBColumn) iter.next());
                                }
                            } catch (IllegalArgumentException e) {
                                // ignore
                            }
                        }

                        try {

                            final String sqlType = (String) newPropertyMap.get(
                                    PropertyKeys.WIZARDDEFAULTSQLTYPE);
                            final String oldsqlType = (String) currentPropertyMap.get(
                                    PropertyKeys.WIZARDDEFAULTSQLTYPE);

                            Iterator iter = currentTable.getColumnList().iterator();
                            while (iter.hasNext() && sqlType != null) {
                                VirtualDBColumn newFld = (VirtualDBColumn) iter.next();

                                if (!sqlType.equals(oldsqlType)) {
                                    newFld.setJdbcType(VirtualDBUtil.getStdJdbcType(sqlType));
                                }

                                if (loadType.equals(PropertyKeys.DELIMITED)) {
                                    final Integer precision = (Integer) newPropertyMap.get(
                                            PropertyKeys.WIZARDDEFAULTPRECISION);
                                    final Integer oldPrecision = (Integer) currentPropertyMap.get(
                                            PropertyKeys.WIZARDDEFAULTPRECISION);

                                    if (!precision.equals(oldPrecision)) {
                                        newFld.setPrecision(precision.intValue());
                                    }
                                }
                            }
                        } catch (Exception ignore) {
                            // do nothing; current fields will use their default values.
                        }
                        parseErrors = null;
                        currentIndex = -1;
                        wd.putProperty(VirtualDBTableWizardIterator.PROP_CURRENTTABLE, currentTable);
                    } catch (Exception e) {
                        parseErrors = e.getMessage();
                    }
                } else {
                    parseErrors = null;
                }
            }
        }
    }

    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
    }

    private JComponent getPreviewPanel(WizardDescriptor wd) {
        JPanel previewPanel = new JPanel();
        previewPanel.setLayout(new GridBagLayout());
        previewPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(NbBundle.getMessage(ParseContentPanel.class, "LBL_preview_import_parse")), BorderFactory.createEmptyBorder(4, 4, 4, 4)));
        previewPanel.setPreferredSize(new Dimension(150, 100));
        JLabel lbl = new JLabel("");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 50.0;
        gbc.weighty = 0.0;
        previewPanel.add(lbl, gbc);

        JTextArea txtArea = new JTextArea();
        txtArea.setEditable(false);
        txtArea.setFont(new Font(NbBundle.getMessage(ParseContentPanel.class, "DEFAULT_Font"), Font.PLAIN, 12));
        txtArea.setText(readPreviewText(wd));

        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.weightx = 50.0;
        gbc.weighty = 100.0;
        JScrollPane sp = new JScrollPane(txtArea);
        previewPanel.add(sp, gbc);

        // Ensure scrollpane text area starts at top of document.
        txtArea.setCaretPosition(0);
        return previewPanel;
    }

    private String readPreviewText(WizardDescriptor wd) {
        String record = "";
        BufferedReader br = null;
        VirtualDBTable table = (VirtualDBTable) wd.getProperty(VirtualDBTableWizardIterator.PROP_CURRENTTABLE);
        List<String> urls = (List<String>) wd.getProperty(VirtualDBTableWizardIterator.URL_LIST);
        int index = Integer.parseInt((String) wd.getProperty(VirtualDBTableWizardIterator.TABLE_INDEX));
        String encoding = table.getEncodingScheme();
        final int maxCharsToRead = 1024;
        final int maxCharsToDisplay = 2048;
        try {
            File repFile = new File(urls.get(index));
            InputStream is = null;
            if (repFile.exists()) {
                is = new FileInputStream(repFile);
            } else {
                is = new URL(urls.get(index)).openStream();
            }
            if (!table.getParserType().equals(PropertyKeys.SPREADSHEET)) {

                br = new BufferedReader(new InputStreamReader(is, encoding), maxCharsToRead * 5);

                StringBuilder strBuf = new StringBuilder(maxCharsToRead);
                int sz = 0;
                int ct = 0;

                char[] charBuf = new char[maxCharsToRead];
                while ((sz = br.read(charBuf)) != -1 && ((ct += sz) < maxCharsToDisplay)) {
                    strBuf.append(charBuf, 0, sz);
                }

                record = strBuf.toString();

            } else {
                Workbook spreadSheetData = Workbook.getWorkbook(is);
                Sheet sheet = spreadSheetData.getSheet(table.getProperty(NbBundle.getMessage(ParseContentPanel.class, "LBL_SpreadSheet")));
                StringBuilder buf = new StringBuilder();
                for (int i = 0; i < sheet.getRows(); i++) {
                    if (i != 0) {
                        buf.append("\r\n");
                    }
                    Cell[] cells = sheet.getRow(i);
                    for (int j = 0; j < cells.length; j++) {
                        if (j != 0) {
                            buf.append(",");
                        }
                        buf.append(cells[j].getContents());
                    }
                }
                record = buf.toString();
                spreadSheetData.close();
            }
        } catch (Exception ioe) {
            mLogger.log(Level.SEVERE, NbBundle.getMessage(ParseContentPanel.class, "LOG_Failed_FileParsing", LOG_CATEGORY), ioe);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignore) {
                    // ignore
                }
            }

            if (record == null) {
                record = "";
            }
        }
        return record;
    }

    public Component getComponent() {
        if (component == null) {
            panel = new ParseContentVisualPanel(this);
            component = (Component) panel;
            panel.setLayout(new GridBagLayout());
            parseErrors = null;
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
}

