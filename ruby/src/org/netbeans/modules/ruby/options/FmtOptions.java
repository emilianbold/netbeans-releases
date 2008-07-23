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
package org.netbeans.modules.ruby.options;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.ComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.ruby.RubyFormatter;
import org.netbeans.modules.ruby.lexer.RubyTokenId;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.Exceptions;

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
    public static final String expandTabToSpaces = SimpleValueNames.EXPAND_TABS;
    public static final String tabSize = SimpleValueNames.TAB_SIZE;
    public static final String spacesPerTab = SimpleValueNames.SPACES_PER_TAB;
    public static final String indentSize = SimpleValueNames.INDENT_SHIFT_WIDTH;
    public static final String rightMargin = SimpleValueNames.TEXT_LIMIT_WIDTH; //NOI18N
    public static final String continuationIndentSize = "continuationIndentSize"; //NOI18N
    public static final String reformatComments = "reformatComments"; //NOI18N
    public static final String indentHtml = "indentHtml"; //NOI18N
    
    public static CodeStyleProducer codeStyleProducer;
        
    public static Preferences lastValues;
    
    private static Class<? extends EditorKit> kitClass;

    static final String CODE_STYLE_PROFILE = "CodeStyle"; // NOI18N
    private static final String DEFAULT_PROFILE = "default"; // NOI18N
    static final String PROJECT_PROFILE = "project"; // NOI18N
    static final String usedProfile = "usedProfile"; // NOI18N
    
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
        return NbPreferences.forModule(CodeStyle.class).node(CODE_STYLE_PROFILE).node(profileId);
    }
    
    public static Preferences getPreferences(Project project) {
        if (project != null) {
            Preferences root = ProjectUtils.getPreferences(project, IndentUtils.class, true).node(CODE_STYLE_PROFILE);
            String profile = root.get(usedProfile, DEFAULT_PROFILE);
            if (PROJECT_PROFILE.equals(profile))
                return root.node(PROJECT_PROFILE).node(RubyInstallation.RUBY_MIME_TYPE); //NOI18N
        }
        return MimeLookup.getLookup(RubyInstallation.RUBY_MIME_TYPE).lookup(Preferences.class);
    }

    public static Class<? extends EditorKit> getKitClass() {
        if (kitClass == null) {
            EditorKit kit = MimeLookup.getLookup(MimePath.get(RubyInstallation.RUBY_MIME_TYPE)).lookup(EditorKit.class); //NOI18N
            kitClass = kit != null ? kit.getClass() : EditorKit.class;
        }
        return kitClass;
    }
    
    public static String getCurrentProfileId() {
        return DEFAULT_PROFILE;
    }
    
    public static CodeStyle createCodeStyle(Preferences p) {
        CodeStyle.getDefault(null);
        return codeStyleProducer.create(p);
    }

    public static boolean getGlobalExpandTabToSpaces() {
        Preferences prefs = MimeLookup.getLookup(RubyInstallation.RUBY_MIME_TYPE).lookup(Preferences.class);
        return prefs.getBoolean(SimpleValueNames.EXPAND_TABS, getDefaultAsBoolean(expandTabToSpaces));
    }
    
    public static int getGlobalTabSize() {
        Preferences prefs = MimeLookup.getLookup(RubyInstallation.RUBY_MIME_TYPE).lookup(Preferences.class);
        return prefs.getInt(SimpleValueNames.TAB_SIZE, getDefaultAsInt(tabSize));
    }
    
    public static int getGlobalSpacesPerTab() {
        Preferences prefs = MimeLookup.getLookup(RubyInstallation.RUBY_MIME_TYPE).lookup(Preferences.class);
        return prefs.getInt(SimpleValueNames.SPACES_PER_TAB, getDefaultAsInt(spacesPerTab));
    }

    public static int getGlobalIndentSize() {
        Preferences prefs = MimeLookup.getLookup(RubyInstallation.RUBY_MIME_TYPE).lookup(Preferences.class);
        return prefs.getInt(SimpleValueNames.INDENT_SHIFT_WIDTH, -1);
    }

    public static int getGlobalRightMargin() {
        Preferences prefs = MimeLookup.getLookup(RubyInstallation.RUBY_MIME_TYPE).lookup(Preferences.class);
        return prefs.getInt(SimpleValueNames.TEXT_LIMIT_WIDTH, getDefaultAsInt(rightMargin));
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
            { tabSize, "2"}, //NOI18N
            { spacesPerTab, "4"}, //NOI18N
            { indentSize, "2"}, //NOI18N
            { continuationIndentSize, "2"}, //NOI18N
            { rightMargin, "120"}, //NOI18N
            { reformatComments, FALSE }, //NOI18N
            { indentHtml, TRUE }, //NOI18N
        };
        
        defaults = new HashMap<String,String>();
        
        for (java.lang.String[] strings : defaultValues) {
            defaults.put(strings[0], strings[1]);
        }
    }
 
    
    // Support section ---------------------------------------------------------
      
    public static class CategorySupport extends OptionsPanelController implements ActionListener, DocumentListener, HierarchyListener {

        public static final String OPTION_ID = "org.netbeans.modules.ruby.options.FormatingOptions.ID";
                
        private static final int LOAD = 0;
        private static final int STORE = 1;
        private static final int ADD_LISTENERS = 2;
        
//        private static final ComboItem  bracePlacement[] = new ComboItem[] {
//                new ComboItem( BracePlacement.SAME_LINE.name(), "LBL_bp_SAME_LINE" ), // NOI18N
//                new ComboItem( BracePlacement.NEW_LINE.name(), "LBL_bp_NEW_LINE" ), // NOI18N
//                new ComboItem( BracePlacement.NEW_LINE_HALF_INDENTED.name(), "LBL_bp_NEW_LINE_HALF_INDENTED" ), // NOI18N
//                new ComboItem( BracePlacement.NEW_LINE_INDENTED.name(), "LBL_bp_NEW_LINE_INDENTED" ) // NOI18N
//            };
//        private static final ComboItem  bracesGeneration[] = new ComboItem[] {
//                new ComboItem( BracesGenerationStyle.GENERATE.name(), "LBL_bg_GENERATE" ), // NOI18N
//                new ComboItem( BracesGenerationStyle.LEAVE_ALONE.name(), "LBL_bg_LEAVE_ALONE" ), // NOI18N
//                new ComboItem( BracesGenerationStyle.ELIMINATE.name(), "LBL_bg_ELIMINATE" ) // NOI18N       
//            };
//        
//        private static final ComboItem  wrap[] = new ComboItem[] {
//                new ComboItem( WrapStyle.WRAP_ALWAYS.name(), "LBL_wrp_WRAP_ALWAYS" ), // NOI18N
//                new ComboItem( WrapStyle.WRAP_IF_LONG.name(), "LBL_wrp_WRAP_IF_LONG" ), // NOI18N
//                new ComboItem( WrapStyle.WRAP_NEVER.name(), "LBL_wrp_WRAP_NEVER" ) // NOI18N
//            };
        
        private String previewText = NbBundle.getMessage(FmtOptions.class, "SAMPLE_Default");
        private String forcedOptions[][];
        
        private boolean changed = false;
        private boolean loaded = false;
        private JPanel panel;
        private List<JComponent> components = new LinkedList<JComponent>();                
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        private JEditorPane previewPane;
        
        protected Preferences preferences;
    
        public CategorySupport(JPanel panel, String previewText, String[]... forcedOptions) {
            this.panel = panel;
            panel.addHierarchyListener(this);
            scan(panel, components);
            this.previewText = previewText == null ? this.previewText : previewText;
            this.forcedOptions = forcedOptions;
            addListeners();
        }
        
        protected void addListeners() {
            scan(ADD_LISTENERS, null);
        }
        
        public void update() {
            loaded = true;
            scan(LOAD, preferences);
            loaded = false;
            changed = false;
        }

        public void applyChanges() {
            storeTo(preferences);
            // Apply syncing of textlimit etc. which requires immediate repaint
            RubyFormatter.syncCurrentOptions();
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
            this.preferences = masterLookup.lookup(Preferences.class);
            this.previewPane = masterLookup.lookup(JEditorPane.class);
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
        
        protected void storeTo(Preferences p) {
            scan(STORE, p);
        }
        
        void changed() {
            if (loaded)
                return;
            if (!changed) {
                changed = true;
                pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
            }
            pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
            refreshPreview();
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
                
        // HierarchyListener implementation -------------------------------------
        
        public void hierarchyChanged(HierarchyEvent e) {
            if (panel.isShowing()) {
                refreshPreview();
            }
        }
        
        // Private methods -----------------------------------------------------

        private void refreshPreview() {
            Preferences p = new PreviewPreferences();
            storeTo(p);
            for (String[] option : forcedOptions) {
                p.put( option[0], option[1]);
            }
            try {
                int rm = p.getInt(rightMargin, getDefaultAsInt(rightMargin));
                previewPane.putClientProperty("TextLimitLine", rm);
            }
            catch( NumberFormatException e ) {
                // Ignore it
            }
            try {
                Class.forName(CodeStyle.class.getName(), true, CodeStyle.class.getClassLoader());
            } catch (ClassNotFoundException cnfe) {}
            CodeStyle codeStyle = codeStyleProducer.create(p);
            

            int rm = 30;
            try {
                rm = p.getInt(rightMargin, getDefaultAsInt(rightMargin));

                // Estimate text line in preview pane
                
                JComponent pc = previewPane;
                if (previewPane.getParent() instanceof JViewport) {
                    pc = (JViewport)previewPane.getParent();
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
            
            previewPane.setIgnoreRepaint(true);
            try {
                BaseDocument doc = new BaseDocument(null, false);
                doc.putProperty("mimeType", RubyInstallation.RUBY_MIME_TYPE); // NOI18N
                doc.putProperty(org.netbeans.api.lexer.Language.class, RubyTokenId.language());

                doc.insertString(0, previewText, null);

                RubyFormatter formatter = new RubyFormatter(codeStyle, rm);
                formatter.reformat(doc, 0, doc.getLength(), null);

                String formatted = doc.getText(0, doc.getLength());
                previewPane.setText(formatted);
            }
            catch (Exception ex){
                Exceptions.printStackTrace(ex);
            }
            
            previewPane.setIgnoreRepaint(false);
            previewPane.scrollRectToVisible(new Rectangle(0,0,10,10) );
            previewPane.repaint(100);           
        }
        
        private void performOperation(int operation, JComponent jc, String optionID, Preferences p) {
            switch(operation) {
            case LOAD:
                loadData(jc, optionID, p);
                break;
            case STORE:
                storeData(jc, optionID, p);
                break;
            case ADD_LISTENERS:
                addListener(jc);
                break;
            }
        }

        private void scan(int what, Preferences p ) {
            for (JComponent jc : components) {
                Object o = jc.getClientProperty(OPTION_ID);
                if (o instanceof String) {
                    performOperation(what, jc, (String)o, p);
                } else if (o instanceof String[]) {
                    for(String oid : (String[])o) {
                        performOperation(what, jc, oid, p);
                    }
                }
            }
        }

        private void scan(Container container, List<JComponent> components) {
            for (Component c : container.getComponents()) {
                if (c instanceof JComponent) {
                    JComponent jc = (JComponent)c;
                    Object o = jc.getClientProperty(OPTION_ID);
                    if (o instanceof String || o instanceof String[])
                        components.add(jc);
                }                    
                if (c instanceof Container)
                    scan((Container)c, components);
            }
        }

        /** Very smart method which tries to set the values in the components correctly
         */ 
        private void loadData( JComponent jc, String optionID, Preferences node ) {
            
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

        private void storeData( JComponent jc, String optionID, Preferences node ) {
            
            if ( jc instanceof JTextField ) {
                JTextField field = (JTextField)jc;
                
                String text = field.getText();
                
                // XXX test for numbers
                if ( isInteger(optionID) ) {
                    try {
                        int i = Integer.parseInt(text);                        
                    } catch (NumberFormatException e) {
                        return;
                    }
                }

                // XXX: watch out, tabSize, spacesPerTab, indentSize and expandTabToSpaces
                // fall back on getGlopalXXX() values and not getDefaultAsXXX value,
                // which is why we must not remove them. Proper solution would be to
                // store formatting preferences to MimeLookup and not use NbPreferences.
                // The problem currently is that MimeLookup based Preferences do not support subnodes.
                if (!optionID.equals(tabSize) &&
                    !optionID.equals(spacesPerTab) && !optionID.equals(indentSize) &&
                    getDefaultAsString(optionID).equals(text)
                ) {
                    node.remove(optionID);
                } else {
                    node.put(optionID, text);
                }
            }
            else if ( jc instanceof JCheckBox ) {
                JCheckBox checkBox = (JCheckBox)jc;
                if (!optionID.equals(expandTabToSpaces) && getDefaultAsBoolean(optionID) == checkBox.isSelected())
                    node.remove(optionID);
                else
                    node.putBoolean(optionID, checkBox.isSelected());
            } 
            else if ( jc instanceof JComboBox) {
                JComboBox cb  = (JComboBox)jc;
                // Logger.global.info( cb.getSelectedItem() + " " + optionID);
                String value = ((ComboItem) cb.getSelectedItem()).value;
                if (getDefaultAsString(optionID).equals(value))
                    node.remove(optionID);
                else
                    node.put(optionID,value);
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
            
//            // is it braces placement?            
//            for (ComboItem comboItem : bracePlacement) {
//                if ( value.equals( comboItem.value) ) {
//                    return new DefaultComboBoxModel( bracePlacement );
//                }
//            }
//            
//            // is it braces generation?
//            for (ComboItem comboItem : bracesGeneration) {
//                if ( value.equals( comboItem.value) ) {
//                    return new DefaultComboBoxModel( bracesGeneration );
//                }
//            }
//            
//            // is it wrap
//            for (ComboItem comboItem : wrap) {
//                if ( value.equals( comboItem.value) ) {
//                    return new DefaultComboBoxModel( wrap );
//                }
//            }
            
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
   
    public static class PreviewPreferences extends AbstractPreferences {
        
        private Map<String,Object> map = new HashMap<String, Object>();

        public PreviewPreferences() {
            super(null, ""); // NOI18N
        }
        
        protected void putSpi(String key, String value) {
            map.put(key, value);            
        }

        protected String getSpi(String key) {
            return (String)map.get(key);                    
        }

        protected void removeSpi(String key) {
            map.remove(key);
        }

        protected void removeNodeSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        protected String[] keysSpi() throws BackingStoreException {
            String array[] = new String[map.keySet().size()];
            return map.keySet().toArray( array );
        }

        protected String[] childrenNamesSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        protected AbstractPreferences childSpi(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        protected void syncSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        protected void flushSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public static interface CodeStyleProducer {
        
        public CodeStyle create( Preferences preferences );
    
    }
}
