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

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.gsf.api.EditorAction;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.netbeans.modules.groovy.editor.NbUtilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import javax.swing.text.Document;
import org.netbeans.modules.groovy.editor.parser.GroovyParserManager;
import org.netbeans.modules.groovy.editor.parser.GroovyParserResult;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import java.util.ArrayList;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import java.util.Set;
import java.util.EnumSet;
import javax.lang.model.element.TypeElement;
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
import org.openide.util.NbBundle;

/**
 *
 * @author schmidtm
 */
public class FixImportsAction extends AbstractAction implements EditorAction {

    private final Logger LOG = Logger.getLogger(FixImportsAction.class.getName());
    String NAME = NbBundle.getMessage(FixImportsAction.class, "FixImportsActionMenuString");

    public FixImportsAction() {
        super(NbBundle.getMessage(FixImportsAction.class, "FixImportsActionMenuString"));
        putValue("PopupMenuText", NAME);
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
        Document doc = comp.getDocument();
        List<String> importDone = new ArrayList<String>();

        if (doc != null) {
            DataObject dob = NbEditorUtilities.getDataObject(doc);
            if (dob != null) {
                FileObject fo = dob.getPrimaryFile();
                Lookup lkp = Lookup.getDefault();

                if (lkp != null && fo != null) {
                    GroovyParserManager parserManager = lkp.lookup(GroovyParserManager.class);
                    if (parserManager != null) {
                        GroovyParserResult result = parserManager.getParsingResultByFileObject(fo);

                        if (result != null) {
                            
                            ErrorCollector errorCollector = result.getErrorCollector();
                            List errList = errorCollector.getErrors();
                            
                            if (errList != null) {
                                for (Object error : errList) {
                                    if (error instanceof SyntaxErrorMessage) {
                                        SyntaxException se = ((SyntaxErrorMessage)error).getCause();
                                        if (se != null) {
                                            String errorMessage = se.getMessage();
                                            String missingClass = getMissingClassName(errorMessage);
                                            
                                            if (missingClass != null) {
                                                LOG.log(Level.FINEST, "Missing Class " + missingClass);
                                                
                                                List<String> importCandidates = getImportCandidate(fo, missingClass);
                                                
                                                if (!importCandidates.isEmpty()) {
                                                    int firstFreePosition = 0;
                                                    BaseDocument baseDoc = AstUtilities.getBaseDocument(fo, true);

                                                    firstFreePosition = getImportPosition(baseDoc);
                                                    
                                                    if (firstFreePosition != -1) {
                                                        EditList edits = null;
                                                        if (baseDoc != null) {
                                                            edits = new EditList(baseDoc);
                                                        }
                                                        
                                                        LOG.log(Level.FINEST, "Importing here: " + firstFreePosition);
                                                        
                                                        if (importCandidates.size() == 1) {
                                                            LOG.log(Level.FINEST, "Importing class!");
                                                            LOG.log(Level.FINEST, importCandidates.toString());
                                                            
                                                            String fqnName = importCandidates.get(0);
                                                            
                                                            if (!importDone.contains(fqnName)) {
                                                                
                                                                edits.replace(firstFreePosition, 0,
                                                                        "import " + fqnName + ";\n", false, 0);
                                                                
                                                                edits.apply();
                                                                
                                                                importDone.add(fqnName);
                                                            }
                                                            
                                                        } else {
                                                            LOG.log(Level.FINEST, "Present Chooser between: ");
                                                            LOG.log(Level.FINEST, importCandidates.toString());
                                                        }
                                                    }
                                                }
                                                
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        LOG.log(Level.FINEST, "Couldn't get GroovyParserManager from global lookup");
                    }
                } else {
                    LOG.log(Level.FINEST, "Couldn't get global lookup");
                }

            }
        }

        return;
    }

    public void actionPerformed(ActionEvent e) {
        LOG.log(Level.FINEST, "actionPerformed(ActionEvent e)");

        JTextComponent pane = NbUtilities.getOpenPane();

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
        return NAME;
    }

    public Class getShortDescriptionBundleClass() {
        return FixImportsAction.class;
    }

    List<String> getImportCandidate(FileObject fo, String missingClass) {
        LOG.log(Level.FINEST, "Looking for class: " + missingClass);

        List<String> result = new ArrayList<String>();

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
                result.add(fqnName);
            }

        }

        return result;

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
        
        int importEnd       = 0;
        int packageOffset   = 0;
        
        while (ts.moveNext()) {
            Token t = ts.token();
            
            if(t.id() == GroovyTokenId.LITERAL_import) {
                int offset = ts.offset();
                importEnd = offset;                
            } 
            else if (t.id() == GroovyTokenId.LITERAL_package){
                packageOffset = ts.offset();
            }
        }
        
        int useOffset = 0;
        
        // sanity check: package *before* import
        if(packageOffset > importEnd) {
            return -1;
        }
        
        // nothing set:
        if(importEnd == 0 && packageOffset == 0){
            // place imports in the first line
            return 0;
        
        }
        // only package set:
        else if(importEnd == 0 && packageOffset != 0){
            // place imports behind package statement
            useOffset = packageOffset;
        }
        
        // only imports set:
        else if(importEnd != 0 && packageOffset == 0){
            // place imports after the last import statement
            useOffset = importEnd;
        }
        
        // both package & import set:
        else if(importEnd != 0 && packageOffset != 0){
            // place imports right after the last import statement
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
    
    
    
}
