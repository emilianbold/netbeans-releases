/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.groovy.editor.actions;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.MissingResourceException;
import javax.swing.AbstractAction;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.gsf.api.EditorAction;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.netbeans.modules.groovy.editor.NbUtilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import javax.swing.text.Document;
import org.netbeans.modules.groovy.editor.parser.GroovyParserResult;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import java.util.ArrayList;
import java.util.Collections;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import java.util.Set;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.TypeElement;
import javax.swing.Icon;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.groovy.editor.AstUtilities;
import org.netbeans.modules.groovy.editor.hints.spi.EditList;
import org.netbeans.modules.groovy.editor.lexer.LexUtilities;
import org.netbeans.modules.groovy.editor.lexer.GroovyTokenId;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.modules.groovy.editor.parser.GroovyParser;
import org.netbeans.modules.gsf.api.Parser;
import org.netbeans.modules.gsf.api.ParserFile;
import org.netbeans.modules.gsf.api.SourceFileReader;
import org.netbeans.modules.gsf.api.TranslatedSource;
import org.netbeans.modules.gsf.spi.DefaultParseListener;
import org.netbeans.modules.gsf.spi.DefaultParserFile;

/**
 *
 * @author schmidtm
 */
public class FixImportsAction extends AbstractAction implements EditorAction, Runnable {

    private final Logger LOG = Logger.getLogger(FixImportsAction.class.getName());
    String MENU_NAME = NbBundle.getMessage(FixImportsAction.class, "FixImportsActionMenuString");
    Document doc = null;

    public FixImportsAction() {
        super(NbBundle.getMessage(FixImportsAction.class, "FixImportsActionMenuString"));
        putValue("PopupMenuText", MENU_NAME);
        // LOG.setLevel(Level.FINEST);
    }

    @Override
    public boolean isEnabled() {
        // here should go all the logic whether there are in fact missing 
        // imports we're able to fix.
        return true;
    }


    void actionPerformed(final JTextComponent comp) {
        LOG.log(Level.FINEST, "actionPerformed(final JTextComponent comp)");

        assert comp != null;
        doc = comp.getDocument();

        if (doc != null) {
            RequestProcessor.getDefault().post(this);
        }
    }
    
    GroovyParserResult getParserResult(final FileObject fo) {
        
        DefaultParseListener listener = new DefaultParseListener();
        TranslatedSource translatedSource = null;
        ParserFile parserFile = new DefaultParserFile(fo, null, false);
        List<ParserFile> files = Collections.singletonList(parserFile);
        
        SourceFileReader reader =
                new SourceFileReader() {

                    public CharSequence read(ParserFile file)
                            throws IOException {
                        Document doc = NbUtilities.getBaseDocument(fo, true);

                        if (doc == null) {
                            return "";
                        }

                        try {
                            return doc.getText(0, doc.getLength());
                        } catch (BadLocationException ble) {
                            IOException ioe = new IOException();
                            ioe.initCause(ble);
                            throw ioe;
                        }
                    }

                    public int getCaretOffset(ParserFile fileObject) {
                        return -1;
                    }
                };
        
        
        Parser.Job job = new Parser.Job(files, listener, reader, translatedSource);
        new GroovyParser().parseFiles(job);

        return (GroovyParserResult) listener.getParserResult();

        
//        Lookup lkp = Lookup.getDefault();
//        if (lkp != null) {
//            GroovyParserManager parserManager = lkp.lookup(GroovyParserManager.class);
//            if (parserManager != null) {
//                result = parserManager.getParsingResultByFileObject(fo);
//                if (result == null) {
//                    LOG.log(Level.FINEST, "Couldn't get GroovyParserResult");
//                }
//            } else {
//                LOG.log(Level.FINEST, "Couldn't get GroovyParserManager from global lookup");
//            }
//        } else {
//            LOG.log(Level.FINEST, "Couldn't get global lookup");
//        }

    }
    
    
    
    public void run() {
        DataObject dob = NbEditorUtilities.getDataObject(doc);
        
        if (dob == null) {
            LOG.log(Level.FINEST, "Could not get DataObject for document");
            return;
        }
        
        FileObject fo = dob.getPrimaryFile();
        GroovyParserResult result = getParserResult(fo);

        if (result == null) {
            LOG.log(Level.FINEST, "Could not get GroovyParserResult");
            return;
        }
        
        ErrorCollector errorCollector = result.getErrorCollector();
        List errList = errorCollector.getErrors();
        
        if (errList == null) {
            LOG.log(Level.FINEST, "Could not get list of errors");
            return;
        }

        List<String> missingNames = new ArrayList<String>();
        
        // loop over the list of errors, remove duplicates and 
        // populate list of missing imports.
        
        for (Object error : errList) {
            if (error instanceof SyntaxErrorMessage) {
                SyntaxException se = ((SyntaxErrorMessage) error).getCause();
                if (se != null) {
                    String missingClassName = getMissingClassName(se.getMessage());

                    if (missingClassName != null) {
                        if(!missingNames.contains(missingClassName)){
                            missingNames.add(missingClassName);
                        }
                    }
                }
            }
        }
        
        // go over list of missing imports, fix it - if there is only one 
        // candidate or populate choosers input list.
        
        Map<String,List> multipleCandidates = new HashMap<String,List>();

        for (String name : missingNames) {
            List<ImportCandidate> importCandidates = getImportCandidate(fo, name);
            
            if (importCandidates.isEmpty()) {
                // nothing to import
                return;
                }
            
            int size = importCandidates.size();
            
            if (size == 1) {
                doImport(fo, importCandidates.get(0).getFqnName());
            } else {
                LOG.log(Level.FINEST, "Adding to multipleCandidates: " + name);
                multipleCandidates.put(name, importCandidates);
            }
        }
        
        // do we have multiple candidate? In this case we need to present a
        // chooser
        
        List<String> listToFix = null;
        
        if(!multipleCandidates.isEmpty()){
            LOG.log(Level.FINEST, "multipleCandidates.size(): " + multipleCandidates.size());
            listToFix = presentChooser(multipleCandidates);
        }
        
        if(listToFix != null && !listToFix.isEmpty()){
            LOG.log(Level.FINEST, "listToFix.size(): " + listToFix.size());
            for (String fqn : listToFix) {
                doImport(fo, fqn);
            }
        }
        
        return;
    }

    
    private List<String> presentChooser(Map<String,List> multipleCandidates){
        LOG.log(Level.FINEST, "presentChooser()");
        List<String> result = new ArrayList<String>();
        ImportChooserInnerPanel panel = new ImportChooserInnerPanel();

        panel.initPanel(multipleCandidates);

        DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(FixImportsAction.class, "FixImportsDialogTitle")); //NOI18N
        Dialog d = DialogDisplayer.getDefault().createDialog(dd);

        d.setVisible(true);
        d.setVisible(false);
        d.dispose();

        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            result = panel.getSelections();
        }
        
        return result;
    }
    
    
    public void actionPerformed(ActionEvent e) {
        LOG.log(Level.FINEST, "actionPerformed(ActionEvent e)");

        JTextComponent pane = NbUtilities.getOpenPane();
        
        LOG.log(Level.FINEST, "NAME               : " + NAME + " : " + getValue(NAME));
        LOG.log(Level.FINEST, "ACCELERATOR_KEY    : " + ACCELERATOR_KEY + " : " + getValue(ACCELERATOR_KEY));
        LOG.log(Level.FINEST, "ACTION_COMMAND_KEY : " + ACTION_COMMAND_KEY + " : " + getValue(ACTION_COMMAND_KEY));

        if (pane != null) {
            actionPerformed(pane);
        }

        return;
    }

    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        LOG.log(Level.FINEST, "actionPerformed(ActionEvent evt, JTextComponent target)");
        return;
    }

    public String getActionName() {
        return NbBundle.getMessage(FixImportsAction.class, "FixImportsActionName");
    }

    public Class getShortDescriptionBundleClass() {
        return FixImportsAction.class;
    }
    
    public class ImportCandidate {
        String Name;
        String fqnName;
        Icon icon;
        int importantsLevel;

        public ImportCandidate(String Name, String fqnName, Icon icon, int importantsLevel) {
            this.Name = Name;
            this.fqnName = fqnName;
            this.icon = icon;
            this.importantsLevel = importantsLevel;
        }

        public String getName() {
            return Name;
        }

        public void setName(String Name) {
            this.Name = Name;
        }

        public String getFqnName() {
            return fqnName;
        }

        public void setFqnName(String fqnName) {
            this.fqnName = fqnName;
        }

        public Icon getIcon() {
            return icon;
        }

        public void setIcon(Icon icon) {
            this.icon = icon;
        }

        public int getImportantsLevel() {
            return importantsLevel;
        }

        public void setImportantsLevel(int importantsLevel) {
            this.importantsLevel = importantsLevel;
        }

        
        
        
    }
    

    List<ImportCandidate> getImportCandidate(FileObject fo, String missingClass) {
        LOG.log(Level.FINEST, "Looking for class: " + missingClass);

        List<ImportCandidate> result = new ArrayList<ImportCandidate>();

        ClassPath bootPath = ClassPath.getClassPath(fo, ClassPath.BOOT);
        ClassPath compilePath = ClassPath.getClassPath(fo, ClassPath.COMPILE);
        ClassPath srcPath = ClassPath.getClassPath(fo, ClassPath.SOURCE);

        if (bootPath == null || compilePath == null || srcPath == null) {
            LOG.log(Level.FINEST, "bootPath    : " + bootPath);
            LOG.log(Level.FINEST, "compilePath : " + compilePath);
            LOG.log(Level.FINEST, "srcPath     : " + srcPath);
            return result;
        }

        ClasspathInfo pathInfo = ClasspathInfo.create(bootPath, compilePath, srcPath);

        Set<org.netbeans.api.java.source.ElementHandle<javax.lang.model.element.TypeElement>> typeNames;

        typeNames = pathInfo.getClassIndex().getDeclaredTypes(missingClass, NameKind.SIMPLE_NAME,
                EnumSet.allOf(ClassIndex.SearchScope.class));

        for (org.netbeans.api.java.source.ElementHandle<TypeElement> typeName : typeNames) {
            javax.lang.model.element.ElementKind ek = typeName.getKind();

            if (ek == javax.lang.model.element.ElementKind.CLASS ||
                    ek == javax.lang.model.element.ElementKind.INTERFACE) {
                String fqnName = typeName.getQualifiedName();
                LOG.log(Level.FINEST, "Found     : " + fqnName);
                
                Icon icon = ElementIcons.getElementIcon(ek, null);
                int level = getImportanceLevel(fqnName);
                
                ImportCandidate candidate = new ImportCandidate(missingClass, fqnName, icon, level);
                result.add(candidate);
            }

        }

        return result;

    }
    
     public static int getImportanceLevel(String fqn) {
        int weight = 50;
        if (fqn.startsWith("java.lang") || fqn.startsWith("java.util")) // NOI18N
            weight -= 10;
        else if (fqn.startsWith("org.omg") || fqn.startsWith("org.apache")) // NOI18N
            weight += 10;
        else if (fqn.startsWith("com.sun") || fqn.startsWith("com.ibm") || fqn.startsWith("com.apple")) // NOI18N
            weight += 20;
        else if (fqn.startsWith("sun") || fqn.startsWith("sunw") || fqn.startsWith("netscape")) // NOI18N
            weight += 30;
        return weight;
    }
    
    
    
    String getMissingClassName(String errorMessage){
        String ERR_PREFIX = "unable to resolve class "; // NOI18N
        String missingClass = null;
        
        if (errorMessage.startsWith(ERR_PREFIX)) { 

            missingClass = errorMessage.substring(ERR_PREFIX.length());
            int idx = missingClass.indexOf(" ");
            
            if(idx != -1){
                return missingClass.substring(0, idx);
            }
        }
        
        return missingClass;
    }
    
    int getImportPosition(BaseDocument doc){
        TokenSequence<?> ts = LexUtilities.getGroovyTokenSequence(doc, 1);
        
        // LOG.setLevel(Level.FINEST);
        
        int importEnd       = -1;
        int packageOffset   = -1;
        
        while (ts.moveNext()) {
            Token t = ts.token();
            int offset = ts.offset();
            
            if(t.id() == GroovyTokenId.LITERAL_import) {
                LOG.log(Level.FINEST, "GroovyTokenId.LITERAL_import found");
                importEnd = offset;                
            } 
            else if (t.id() == GroovyTokenId.LITERAL_package){
                LOG.log(Level.FINEST, "GroovyTokenId.LITERAL_package found");
                packageOffset = offset;
            }
        }
        
        int useOffset = 0;
        
        // sanity check: package *before* import
        if(importEnd != -1 && packageOffset > importEnd) {
            LOG.log(Level.FINEST, "packageOffset > importEnd");
            return -1;
        }
        
        // nothing set:
        if(importEnd == -1 && packageOffset == -1){
            // place imports in the first line
            LOG.log(Level.FINEST, "importEnd == -1 && packageOffset == -1");
            return 0;
        
        }
        // only package set:
        else if(importEnd == -1 && packageOffset != -1){
            // place imports behind package statement
            LOG.log(Level.FINEST, "importEnd == -1 && packageOffset != -1");
            useOffset = packageOffset;
        }
        
        // only imports set:
        else if(importEnd != -1 && packageOffset == -1){
            // place imports after the last import statement
            LOG.log(Level.FINEST, "importEnd != -1 && packageOffset == -1");
            useOffset = importEnd;
        }
        
        // both package & import set:
        else if(importEnd != -1 && packageOffset != -1){
            // place imports right after the last import statement
            LOG.log(Level.FINEST, "importEnd != -1 && packageOffset != -1");
            useOffset = importEnd;
            
        }
        
        int lineOffset = 0;
        
        try {
            lineOffset = Utilities.getLineOffset(doc, useOffset);
        } catch (BadLocationException ex) {
            LOG.log(Level.FINEST, "BadLocationException for : " + useOffset);
            return -1;
        }
        
        return Utilities.getRowStartFromLineOffset(doc, lineOffset + 1);              

    }

    private void doImport(FileObject fo, String fqnName) throws MissingResourceException {
        int firstFreePosition = 0;
        BaseDocument baseDoc = AstUtilities.getBaseDocument(fo, true);

        firstFreePosition = getImportPosition(baseDoc);

        if (firstFreePosition != -1) {
            if (baseDoc == null) {
                return;
            }
            
            EditList edits = new EditList(baseDoc);
            LOG.log(Level.FINEST, "Importing here: " + firstFreePosition);

            edits.replace(firstFreePosition, 0, "import " + fqnName + "\n", false, 0);
            edits.apply();
        }
    }

}
