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
package org.netbeans.modules.cnd.editor.options;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Formatter;
import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.cnd.editor.api.CodeStyle.BracePlacement;
import org.netbeans.modules.cnd.editor.api.CodeStyle.PreprocessorIndent;
import org.netbeans.modules.cnd.editor.reformat.Reformatter;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *  was cloned from org.netbeans.modules.java.ui.FmtOptions
 * 
 * @author Alexander Simon
 */
public class CategorySupport extends Category implements ActionListener, DocumentListener {

    public static final String OPTION_ID = "org.netbeans.modules.cnd.editor.options.EditorOptions.ID";
    private static final int LOAD = 0;
    private static final int STORE = 1;
    private static final int ADD_LISTENERS = 2;
    private static final ComboItem  bracePlacement[] = new ComboItem[] {
            new ComboItem( BracePlacement.SAME_LINE.name(), "LBL_bp_SAME_LINE" ), // NOI18N
            new ComboItem( BracePlacement.NEW_LINE.name(), "LBL_bp_NEW_LINE" ), // NOI18N
        };

    private static final ComboItem  preprocessorPlacement[] = new ComboItem[] {
            new ComboItem( PreprocessorIndent.START_LINE.name(), "LBL_pi_START_LINE" ), // NOI18N
            new ComboItem( PreprocessorIndent.CODE_INDENT.name(), "LBL_pi_CODE_INDENT" ), // NOI18N
            new ComboItem( PreprocessorIndent.PREPROCESSOR_INDENT.name(), "LBL_pi_PREPROCESSOR_INDENT" ), // NOI18N
        };

    private String previewText = NbBundle.getMessage(EditorOptions.class, "SAMPLE_Default");
    private Map<String, Object> forcedOptions;
    private boolean changed = false;
    private JPanel panel;
    private CodeStyle.Language language;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public CategorySupport(CodeStyle.Language language, 
            String nameKey, JPanel panel, String previewText, Map<String, Object> forcedOptions) {
        super(nameKey);
        this.language = language;
        this.panel = panel;
        this.previewText = previewText == null ? this.previewText : previewText;
        this.forcedOptions = forcedOptions;
        addListeners();
    }

    protected void addListeners() {
        scan(panel, ADD_LISTENERS, null);
    }

    public void update() {
        scan(panel, LOAD, null);
    }

    public void applyChanges() {
        scan(panel, STORE, null);
    }

    public void storeTo(Preferences preferences) {
        scan(panel, STORE, preferences);
    }

    public void refreshPreview(JEditorPane pane, Preferences p) {
        for (Map.Entry<String, Object> option : forcedOptions.entrySet()) {
            Object value = option.getValue();
            if (value instanceof String) {
                p.put(option.getKey(), value.toString());
            } else if (value instanceof Integer) {
                p.putInt(option.getKey(), (Integer) value);
            } else if (value instanceof Boolean) {
                p.putBoolean(option.getKey(), (Boolean) value);
            }
        }
        pane.setText(previewText);
        CodeStyle codeStyle = EditorOptions.createCodeStyle(language, p);
        BaseDocument bd = (BaseDocument) pane.getDocument();
        try {
            new Reformatter(bd, codeStyle).reformat();
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
//        Preferences oldPreferences = ((CodeStyleImpl)CodeStyle.getDefault(language)).getPref();
//        ((CodeStyleImpl)CodeStyle.getDefault(language)).setPref(p);
//        Formatter f = bd.getFormatter();
//        try {
//	    f.reformatLock();
//            f.reformat(bd, 0, bd.getLength());
//            String x = bd.getText(0, bd.getLength());
//            pane.setText(x);
//        } catch (BadLocationException ex) {
//            Exceptions.printStackTrace(ex);
//        } finally {
//            ((CodeStyleImpl)CodeStyle.getDefault(language)).setPref(oldPreferences);
//	    f.reformatUnlock();
//	}
    }
    public void cancel() {
        // Usually does not need to do anything
    }

    public boolean isValid() {
        return true; // Should almost always be OK
    }

    public boolean isChanged() {
        return changed;
    }

    public JComponent getComponent(Lookup masterLookup) {
        return panel;
    }

    public HelpCtx getHelpCtx() {
        return null;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    void changed() {
        if (!changed) {
            changed = true;
            pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }

    // ActionListener implementation ---------------------------------------
    public void actionPerformed(ActionEvent e) {
        changed();
    }

    // DocumentListener implementation -------------------------------------
    public void insertUpdate(DocumentEvent e) {
        changed();
    }

    public void removeUpdate(DocumentEvent e) {
        changed();
    }

    public void changedUpdate(DocumentEvent e) {
        changed();
    }

    // Private methods -----------------------------------------------------
    private void scan(Container container, int what, Preferences p) {
        for (Component c : container.getComponents()) {
            if (c instanceof JComponent) {
                JComponent jc = (JComponent) c;
                Object o = jc.getClientProperty(OPTION_ID);
                if (o != null && o instanceof String) {
                    switch (what) {
                        case LOAD:
                            loadData(jc, (String) o);
                            break;
                        case STORE:
                            storeData(jc, (String) o, p);
                            break;
                        case ADD_LISTENERS:
                            addListener(jc);
                            break;
                    }
                }
            }
            if (c instanceof Container) {
                scan((Container) c, what, p);
            }
        }
    }

    /** Very smart method which tries to set the values in the components correctly
     */
    private void loadData(JComponent jc, String optionID) {
        Preferences node = EditorOptions.getPreferences(EditorOptions.getCurrentProfileId(language));
        if (jc instanceof JTextField) {
            JTextField field = (JTextField)jc;                
            field.setText(node.get(optionID, EditorOptions.getDefault(optionID).toString()));
        } else if (jc instanceof JCheckBox) {
            JCheckBox checkBox = (JCheckBox)jc;
            checkBox.setSelected(node.getBoolean(optionID, (Boolean)EditorOptions.getDefault(optionID)));
        } else if (jc instanceof JComboBox) {
            JComboBox cb  = (JComboBox)jc;
            String value = node.get(optionID,EditorOptions.getDefault(optionID).toString());
            ComboBoxModel model = createModel(value);
            cb.setModel(model);
            ComboItem item = whichItem(value, model);
            cb.setSelectedItem(item);
        }
    }

    private void storeData(JComponent jc, String optionID, Preferences p) {
        Preferences node = p == null ? EditorOptions.getPreferences(EditorOptions.getCurrentProfileId(language)) : p;
        if (jc instanceof JTextField) {
            JTextField field = (JTextField)jc;
            try {
                int i = Integer.parseInt(field.getText());
                node.putInt(optionID,i);
            } catch (NumberFormatException e) {
                node.put(optionID, EditorOptions.getLastValue(language, optionID).toString());
            }
        } else if (jc instanceof JCheckBox) {
            JCheckBox checkBox = (JCheckBox)jc;
            node.putBoolean(optionID, checkBox.isSelected());
        } else if (jc instanceof JComboBox) {
            JComboBox cb  = (JComboBox)jc;
            node.put(optionID, ((ComboItem)cb.getSelectedItem()).value);
        }         
    }

    private void addListener(JComponent jc) {
        if (jc instanceof JTextField) {
            JTextField field = (JTextField) jc;
            field.addActionListener(this);
            field.getDocument().addDocumentListener(this);
        } else if (jc instanceof JCheckBox) {
            JCheckBox checkBox = (JCheckBox) jc;
            checkBox.addActionListener(this);
        } else if (jc instanceof JComboBox) {
            JComboBox cb = (JComboBox) jc;
            cb.addActionListener(this);
        }
    }

    private ComboBoxModel createModel(String value) {
        // is it braces placement?            
        for (ComboItem comboItem : bracePlacement) {
            if ( value.equals( comboItem.value) ) {
                return new DefaultComboBoxModel( bracePlacement );
            }
        }
        for (ComboItem comboItem : preprocessorPlacement) {
            if ( value.equals( comboItem.value) ) {
                return new DefaultComboBoxModel( preprocessorPlacement );
            }
        }
        return null;
    }

    private static ComboItem whichItem(String value, ComboBoxModel model) {
        for (int i = 0; i < model.getSize(); i++) {
            ComboItem item = (ComboItem) model.getElementAt(i);
            if (value.equals(item.value)) {
                return item;
            }
        }
        return null;
    }

    private static class ComboItem {
        String value;
        String displayName;
        public ComboItem(String value, String key) {
            this.value = value;
            this.displayName = NbBundle.getMessage(EditorOptions.class, key);
        }
        @Override
        public String toString() {
            return displayName;
        }
    }
}
