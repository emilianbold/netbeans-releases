/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.ruby.options;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.ComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.EditorKit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.gsf.FormattingPreferences;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.modules.ruby.Formatter;
import org.netbeans.modules.ruby.lexer.RubyTokenId;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.Exceptions;

import static org.netbeans.modules.ruby.options.CodeStyle.*;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author phrebejk
 * 
 * @todo Add an RHTML options category, such that I can see the effects of
 *   switching the RHTML toggles?
 */
public class FmtOptions {

    public static final String expandTabToSpaces = "expandTabToSpaces"; //NOI18N
    public static final String tabSize = "tabSize"; //NOI18N
    public static final String indentSize = "indentSize"; //NOI18N
    public static final String continuationIndentSize = "continuationIndentSize"; //NOI18N
    public static final String reformatComments = "reformatComments"; //NOI18N
    public static final String indentHtml = "indentHtml"; //NOI18N
    public static final String rightMargin = "rightMargin"; //NOI18N
    
    public static CodeStyleProducer codeStyleProducer;
        
    public static Preferences lastValues;
    
    private static Class<? extends EditorKit> kitClass;

    private static final String DEFAULT_PROFILE = "default"; // NOI18N
    
    private FmtOptions() {}

    public static int getDefaultAsInt(String key) {
        return Integer.parseInt(defaults.get(key));
    }
    
    public static boolean getDefaultAsBoolean(String key) {
        return Boolean.parseBoolean(defaults.get(key));
    }
        
    public static String getDefaultAsString(String key) {
        return defaults.get(key);
    }
    
    public static Preferences getPreferences(String profileId) {
        return NbPreferences.forModule(CodeStyle.class).node("CodeStyle").node(profileId);
    }
    
    public static boolean getGlobalExpandTabToSpaces() {
        org.netbeans.editor.Formatter f = (org.netbeans.editor.Formatter)Settings.getValue(getKitClass(), "formatter");
        if (f != null)
            return f.expandTabs();
        return getDefaultAsBoolean(expandTabToSpaces);
    }
    
    public static int getGlobalTabSize() {
        Integer i = (Integer)Settings.getValue(getKitClass(), SettingsNames.TAB_SIZE);
        return i != null ? i.intValue() : getDefaultAsInt(tabSize);
    }

    // Ruby needs its own indent size; the global "4" isn't a good match
    //    public static int getGlobalIndentSize() {
    //        org.netbeans.editor.Formatter f = (org.netbeans.editor.Formatter)Settings.getValue(getKitClass(), "formatter");
    //        if (f != null)
    //            return f.getShiftWidth();
    //        return getDefaultAsInt(indentSize);
    //    }
    
    public static int getGlobalRightMargin() {
        Integer i = (Integer)Settings.getValue(getKitClass(), SettingsNames.TEXT_LIMIT_WIDTH);
        return i != null ? i.intValue() : getDefaultAsInt(rightMargin);
    }
    
    public static Class<? extends EditorKit> getKitClass() {
        if (kitClass == null) {
            EditorKit kit = MimeLookup.getLookup(MimePath.get(RubyInstallation.RUBY_MIME_TYPE)).lookup(EditorKit.class); //NOI18N
            kitClass = kit != null ? kit.getClass() : EditorKit.class;
        }
        return kitClass;
    }
    
    
    public static void flush() {
        try {
            getPreferences( getCurrentProfileId()).flush();
        }
        catch(BackingStoreException e) {
            Exceptions.printStackTrace(e);
        }
    }
    
    public static String getCurrentProfileId() {
        return DEFAULT_PROFILE;
    }
    
    public static CodeStyle createCodeStyle(Preferences p) {
        CodeStyle.getDefault(null);
        return codeStyleProducer.create(p);
    }
    
    public static boolean isInteger(String optionID) {
        String value = defaults.get(optionID);
        
        try {
            Integer.parseInt(value);
            return true;            
        } catch (NumberFormatException numberFormatException) {
            return false;
        }
    }
    
    public static String getLastValue(String optionID) {
        Preferences p = lastValues == null ? getPreferences(getCurrentProfileId()) : lastValues;
        return p.get(optionID, getDefaultAsString(optionID));
    }
 
    // Private section ---------------------------------------------------------
    
    private static final String TRUE = "true";      // NOI18N
    private static final String FALSE = "false";    // NOI18N
    
    private static Map<String,String> defaults;
    
    static {
        createDefaults();
    }
    
    private static void createDefaults() {
        String defaultValues[][] = {
            { expandTabToSpaces, TRUE}, //NOI18N
            { tabSize, "8"}, //NOI18N
            { indentSize, "2"}, //NOI18N
            { continuationIndentSize, "2"}, //NOI18N
            { reformatComments, FALSE }, //NOI18N
            { indentHtml, TRUE }, //NOI18N
            { rightMargin, "80"}, //NOI18N
        };
        
        defaults = new HashMap<String,String>();
        
        for (java.lang.String[] strings : defaultValues) {
            defaults.put(strings[0], strings[1]);
        }

    }
 
    
    // Support section ---------------------------------------------------------
      
    public static class CategorySupport extends FormatingOptionsPanel.Category implements ActionListener, DocumentListener {

        public static final String OPTION_ID = "org.netbeans.modules.ruby.options.FormatingOptions.ID";
                
        private static final int LOAD = 0;
        private static final int STORE = 1;
        private static final int ADD_LISTENERS = 2;
        
        private String previewText = NbBundle.getMessage(FmtOptions.class, "SAMPLE_Default");
        private String forcedOptions[][];
        
        private boolean changed = false;
        private JPanel panel;
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
        public CategorySupport(String nameKey, JPanel panel, String previewText, String[]... forcedOptions) {
            super(nameKey);
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

        public void refreshPreview(JEditorPane pane, Preferences p ) {
            
            for (String[] option : forcedOptions) {
                p.put( option[0], option[1]);
            }
            

            int rm = 30;
            try {
                rm = p.getInt(rightMargin, getDefaultAsInt(rightMargin));

                // Estimate text line in preview pane
                
                JComponent pc = pane;
                if (pane.getParent() instanceof JViewport) {
                    pc = (JViewport)pane.getParent();
                }
                Font font = pc.getFont();
                FontMetrics metrics = pc.getFontMetrics(font);
                int cw = metrics.charWidth('x');
                if (cw > 0) {
                    int nrm = pc.getWidth() / cw;
                    if (nrm > 3) {
                        rm = nrm-2;
                    }
                }

                //pane.putClientProperty("TextLimitLine", rm); // NOI18N
            }
            catch( NumberFormatException e ) {
                // Ignore it
            }
            
            CodeStyle codeStyle = FmtOptions.createCodeStyle(p);
            
            try {
                BaseDocument doc = new BaseDocument(null, false);
                doc.putProperty(org.netbeans.api.lexer.Language.class, RubyTokenId.language());

                doc.insertString(0, previewText, null);

                Formatter formatter = new Formatter(codeStyle, rm);
                FormattingPreferences prefs = null;  // obsolete
                formatter.reformat(doc, 0, doc.getLength(), null, prefs);

                String formatted = doc.getText(0, doc.getLength());
                pane.setText(formatted);
            }
            catch (Exception ex){
                Exceptions.printStackTrace(ex);
            }
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
        
        private void scan( Container container, int what, Preferences p ) {
            for (Component c : container.getComponents() ) {
                if (c instanceof JComponent ) {
                    JComponent jc = (JComponent)c;
                    Object o = jc.getClientProperty(OPTION_ID);
                    if ( o != null && o instanceof String ) {
                        switch( what ) {
                        case LOAD:
                            loadData( jc, (String)o );
                            break;
                        case STORE:
                            storeData( jc, (String)o, p );
                            break;
                        case ADD_LISTENERS:
                            addListener( jc );
                            break;
                        }
                    }                    
                }
                if ( c instanceof Container ) {
                    scan((Container)c, what, p);
                }
            }

        }

        /** Very smart method which tries to set the values in the components correctly
         */ 
        private void loadData( JComponent jc, String optionID ) {
            
            Preferences node = getPreferences(getCurrentProfileId());
            
            if ( jc instanceof JTextField ) {
                JTextField field = (JTextField)jc;                
                field.setText( node.get(optionID, getDefaultAsString(optionID)) );
            }
            else if ( jc instanceof JCheckBox ) {
                JCheckBox checkBox = (JCheckBox)jc;
                boolean df = getDefaultAsBoolean(optionID);
                checkBox.setSelected( node.getBoolean(optionID, df));                
            } 
            else if ( jc instanceof JComboBox) {
                JComboBox cb  = (JComboBox)jc;
                String value = node.get(optionID, getDefaultAsString(optionID) );
                ComboBoxModel model = createModel(value);
                cb.setModel(model);
                ComboItem item = whichItem(value, model);
                cb.setSelectedItem(item);
            }
            
        }
        
        private void storeData( JComponent jc, String optionID, Preferences p ) {
            Preferences node = p == null ? getPreferences(getCurrentProfileId()) : p;
            
            if ( jc instanceof JTextField ) {
                JTextField field = (JTextField)jc;
                
                String text = field.getText();
                
                if ( isInteger(optionID) ) {
                    try {
                        int i = Integer.parseInt(text);
                    } catch (NumberFormatException e) {
                        text = getLastValue(optionID);
                    }
                }
                
                // XXX test for numbers
                node.put(optionID, text);  
            }
            else if ( jc instanceof JCheckBox ) {
                JCheckBox checkBox = (JCheckBox)jc;
                node.putBoolean(optionID, checkBox.isSelected());
            } 
            else if ( jc instanceof JComboBox) {
                JComboBox cb  = (JComboBox)jc;
                // Logger.global.info( cb.getSelectedItem() + " " + optionID);
                node.put(optionID, ((ComboItem)cb.getSelectedItem()).value);
            }         
        }
        
        private void addListener( JComponent jc ) {
            if ( jc instanceof JTextField ) {
                JTextField field = (JTextField)jc;
                field.addActionListener(this);
                field.getDocument().addDocumentListener(this);
            }
            else if ( jc instanceof JCheckBox ) {
                JCheckBox checkBox = (JCheckBox)jc;
                checkBox.addActionListener(this);
            } 
            else if ( jc instanceof JComboBox) {
                JComboBox cb  = (JComboBox)jc;
                cb.addActionListener(this);
            }         
        }
        
            
        private ComboBoxModel createModel( String value ) {
            return null;
        }
        
        private static ComboItem whichItem(String value, ComboBoxModel model) {
            
            for (int i = 0; i < model.getSize(); i++) {
                ComboItem item = (ComboItem)model.getElementAt(i);
                if ( value.equals(item.value)) {
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
                this.displayName = NbBundle.getMessage(FmtOptions.class, key);
            }

            @Override
            public String toString() {
                return displayName;
            }
            
        }
    }
   
    public static interface CodeStyleProducer {
        
        public CodeStyle create( Preferences preferences );
    
    }
}
