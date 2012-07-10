/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.editor.completion.impl;

import java.util.LinkedList;
import java.util.List;
import javax.lang.model.SourceVersion;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.javafx2.editor.completion.model.ImportDecl;
import org.netbeans.modules.xml.text.syntax.SyntaxElement;
import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;
import org.netbeans.modules.xml.text.syntax.dom.ProcessingInstructionImpl;
import org.w3c.dom.Document;

import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.javafx2.editor.completion.model.LanguageDecl;
import org.openide.util.Utilities;

import org.netbeans.modules.csl.api.Error;



/**
 * Produces a FxModel from the given source.
 *
 * @author sdedic
 */
public class ModelFactoryImpl {
    private static final String PROCESSING_TARGET_IMPORT = "import"; // NOI18N
    private static final String PROCESSING_TARGET_INCLUDE = "include"; // NOI18N
    private static final String PROCESSING_TARGET_LANGUAGE = "language"; // NOI18N
            
    private XMLSyntaxSupport    xmlSupport;
    private BaseDocument        baseDoc;
    private int                 offset;
    private TokenHierarchy      tokenHierarchy;
    private ModelAccessor       accessor;
    private LanguageDecl        language;
    
    private List<LanguageDecl>  languages = new LinkedList<LanguageDecl>();
    private List<ImportDecl>    imports = new LinkedList<ImportDecl>();
    
    private List<Error>         errors;
    
    public ModelFactoryImpl(BaseDocument doc, int offset) {
        this.baseDoc = doc;
    }
    
    private void init() {
        if (xmlSupport != null) {
            return;
        }
        xmlSupport = new XMLSyntaxSupport(baseDoc);
    }
    
    private void parse() throws BadLocationException {
        init();
        
        SyntaxElement contextEl = xmlSupport.getElementChain(0);
        if (!(contextEl instanceof Document)) {
            // some weird error, bail out
            return;
        }
    }
    
    private void parseImport(ProcessingInstructionImpl pi) {
        String imported = pi.getData().trim();
        boolean wildcard = false;
        
        if (imported.endsWith(".*")) {
            wildcard = true;
            imported = imported.substring(0, imported.length() - 2);
        }
        ImportDecl decl = accessor.createImport(imported, wildcard);
        imports.add(decl);
    }
    
    private void parseLanguage(ProcessingInstructionImpl pi) {
        String langData = pi.getData();
        LanguageDecl decl = accessor.createLanguage(langData);
        languages.add(decl);
    }
    
    private void parseProcessingInstruction(ProcessingInstructionImpl i) {
        String target = i.getTarget();
        if (PROCESSING_TARGET_IMPORT.equals(target)) {
            
        } else if (PROCESSING_TARGET_INCLUDE.equals(target)) {
            // not now
        } else if (PROCESSING_TARGET_LANGUAGE.equals(target)) {
            parseLanguage(i);
        }            
    }
}
