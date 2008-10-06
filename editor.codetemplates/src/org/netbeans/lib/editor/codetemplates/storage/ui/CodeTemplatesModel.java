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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.lib.editor.codetemplates.storage.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.CodeTemplateDescription;
import org.netbeans.lib.editor.codetemplates.storage.CodeTemplateSettingsImpl;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.openide.util.NbBundle;


final class CodeTemplatesModel {

    // A mime type is something like text/x-java and a language is a localized name of
    // the programming language denoted by a mime type (e.g. Java).
    
    private final List<String> languages = new ArrayList<String>();
    private final Map<String, String> languageToMimeType = new HashMap<String, String>();
    private final Map<String,TM> languageToModel = new HashMap<String,TM>();
    private final Map<TM, String> modelToLanguage = new HashMap<TM, String>();
    
    CodeTemplatesModel () {
        
        Vector<String> columns = new Vector<String>();
        columns.add(loc("Abbreviation_Title")); //NOI18N
        columns.add(loc("Expanded_Text_Title")); //NOI18N
        columns.add(loc("Description_Title")); //NOI18N

        Set mimeTypes = EditorSettings.getDefault().getAllMimeTypes();
        for(Iterator i = mimeTypes.iterator(); i.hasNext(); ) {
            String mimeType = (String) i.next();
            
            // Load the code templates
            MimePath mimePath = MimePath.parse(mimeType);
            Map<String, CodeTemplateDescription> abbreviationsMap = 
                CodeTemplateSettingsImpl.get(mimePath).getCodeTemplates();

            // Skip compound mime types (e.g. text/x-ant+xml), they inherit
            // code templates from their base mime type
            if (abbreviationsMap.isEmpty() && isCompoundMimeType(mimeType)) {
                continue;
            }
            
            // Add the language and its mime type to the map
            String language = EditorSettings.getDefault().getLanguageName(mimeType);
            if (language.equals (mimeType))
                continue;
            languages.add(language);
            Collections.sort(languages);
            languageToMimeType.put(language, mimeType);
            
            // Load the table
            List<Vector<String>> table = new ArrayList<Vector<String>>();
            for(String abbreviation : abbreviationsMap.keySet()) {
                CodeTemplateDescription ctd = abbreviationsMap.get(abbreviation);
                Vector<String> line =  new Vector<String>(2);
                line.add(abbreviation);
                line.add(ctd.getParametrizedText());
                line.add(ctd.getDescription());
                table.add(line);
            }
            Collections.sort(table, new MComparator());
            
            // Create the code templates table model for this language
            TM tableModel = new TM(abbreviationsMap, columns, table);
            
            modelToLanguage.put(tableModel, language);
            languageToModel.put(language, tableModel);
        }
        
        expander = CodeTemplateSettingsImpl.get(MimePath.EMPTY).getExpandKey();
    }
    
    private boolean isCompoundMimeType(String mimeType) {
        int idx = mimeType.lastIndexOf('+');
        return idx != -1 && idx < mimeType.length() - 1;
    }
    
    List<String> getLanguages () {
        return Collections.unmodifiableList(languages);
    }
    
    String findLanguage(String mimeType) {
        for(String lang : languageToMimeType.keySet()) {
            String mt = languageToMimeType.get(lang);
            if (mt.equals(mimeType)) {
                return lang;
            }
        }
        return null;
    }
    
    String getMimeType(String language) {
        return languageToMimeType.get(language);
    }
    
    TM getTableModel(String language) {
        return languageToModel.get(language);
    }
    
    void saveChanges () {
        // Save modified code templates
        for(String language : languageToModel.keySet()) {
            TM tableModel = languageToModel.get(language);
            
            if (!tableModel.isModified()) {
                continue;
            }
            
            // Get the code templates from the model
            String mimeType = languageToMimeType.get(language);
            Map<String, CodeTemplateDescription> newMap = new HashMap<String, CodeTemplateDescription>();
            for (int idx = 0; idx < tableModel.getRowCount(); idx++) {
                String abbreviation = tableModel.getAbbreviation(idx);
                CodeTemplateDescription ctd = new CodeTemplateDescription(
                    abbreviation,
                    tableModel.getDescription(idx),
                    tableModel.getText(idx),
                    tableModel.getContexts(idx),
                    tableModel.getUniqueId(idx),
                    mimeType
                );
                
                newMap.put(abbreviation, ctd);
            }
            
            // Save the code templates
            MimePath mimePath = MimePath.parse(mimeType);
            CodeTemplateSettingsImpl.get(mimePath).setCodeTemplates(newMap);
        }

        // Save modified expander key
        if (expander != null) {
            CodeTemplateSettingsImpl.get(MimePath.EMPTY).setExpandKey(expander);
        }
    }
    
    boolean isChanged() {
        if (!CodeTemplateSettingsImpl.get(MimePath.EMPTY).getExpandKey().equals(expander)) {
            return true;
        }

        for(String l : languageToModel.keySet()) {
            TM tableModel = languageToModel.get(l);
            if (tableModel.isModified()) {
                return true;
            }
        }
        
        return false;
    }
    
    private static String loc (String key) {
        return NbBundle.getMessage (CodeTemplatesModel.class, key);
    }
    
    KeyStroke getExpander () {
        return expander;
    }
    
    private KeyStroke expander;
    void setExpander (KeyStroke expander) {
        this.expander = expander;
    }
    
    private static class MComparator implements Comparator<Vector<String>> {
        public int compare(Vector<String> o1, Vector<String> o2) {
            String s1 = o1.get(0);
            String s2 = o2.get(0);
            return s1.compareTo(s2);
        }
    } // End of MComparator class
    
    /* package */ static class TM extends DefaultTableModel {

        private final Map<String, CodeTemplateDescription> codeTemplatesMap;
        private boolean modified = false;
        
        public TM(
            Map<String, CodeTemplateDescription> codeTemplatesMap, 
            Vector<String> headers,
            List<Vector<String>> data
        ) {
            super(new Vector<Object>(data), headers);
            this.codeTemplatesMap = codeTemplatesMap;
        }

        public @Override boolean isCellEditable(int row, int column) {
            return false;
        }
        
        public String getAbbreviation(int row) {
            return (String) getValueAt(row, 0);
        }

        public String getDescription(int row) {
            return (String) getValueAt(row, 2);
        }

        public void setDescription(int row, String description) {
            if (compareTexts(description, getDescription(row))) {
                return;
            }
            
            setValueAt(description, row, 2);
            this.modified = true;
        }

        public String getText(int row) {
            return (String) getValueAt(row, 1);
        }

        public void setText(int row, String text) {
            if (compareTexts(text, getText(row))) {
                return;
            }
            
            setValueAt(text, row, 1);
            this.modified = true;
        }

        public List<String> getContexts(int row) {
            CodeTemplateDescription ctd = codeTemplatesMap.get(getAbbreviation(row));
            return ctd == null ? null : ctd.getContexts();
        }
        
        public String getUniqueId(int row) {
            CodeTemplateDescription ctd = codeTemplatesMap.get(getAbbreviation(row));
            return ctd == null ? null : ctd.getUniqueId();
        }
        
        public int addCodeTemplate(String abbreviation) {
            addRow(new Object [] { abbreviation, "", null }); //NOI18N
            this.modified = true;
            return getRowCount() - 1;
        }
        
        public void removeCodeTemplate(int row) {
            removeRow(row);
            this.modified = true;
        }
        
        public boolean isModified() {
            return modified;
        }
        
        private static boolean compareTexts(String t1, String t2) {
            if (t1 == null || t1.length() == 0) {
                t1 = null;
            }
            if (t2 == null || t2.length() == 0) {
                t2 = null;
            }
            if (t1 != null && t2 != null) {
                return t1.equals(t2);
            } else {
                return t1 == null && t2 == null;
            }
        }
    } // End of TableModel class
}


