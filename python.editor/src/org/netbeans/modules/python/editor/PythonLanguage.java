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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.python.editor;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.python.editor.hints.PythonHintsProvider;
import org.netbeans.api.lexer.Language;
import org.netbeans.modules.python.editor.lexer.PythonTokenId;
import org.netbeans.modules.gsf.api.CodeCompletionHandler;
import org.netbeans.modules.gsf.api.DeclarationFinder;
import org.netbeans.modules.gsf.api.Formatter;
import org.netbeans.modules.gsf.api.HintsProvider;
import org.netbeans.modules.gsf.api.IndexSearcher;
import org.netbeans.modules.gsf.api.Indexer;
import org.netbeans.modules.gsf.api.InstantRenamer;
import org.netbeans.modules.gsf.api.KeystrokeHandler;
import org.netbeans.modules.gsf.api.OccurrencesFinder;
import org.netbeans.modules.gsf.api.Parser;
import org.netbeans.modules.gsf.api.SemanticAnalyzer;
import org.netbeans.modules.gsf.api.StructureScanner;
import org.netbeans.modules.gsf.spi.DefaultLanguageConfig;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;

/**
 *
 * @author alley
 * @author Tor Norbye
 */
public class PythonLanguage extends DefaultLanguageConfig {
    private static FileObject jsStubsFO;

    @Override
    public String getLineCommentPrefix() {
        return "#"; //NOI18N
    }

    @Override
    public boolean isIdentifierChar(char c) {
        return (c >= 'a') && (c <= 'z') ||
                (c >= 'A') && (c <= 'Z') ||
                (c >= '0') && (c <= '9') ||
                c == '_';
    }

    public Language getLexerLanguage() {
        return PythonTokenId.language();
    }

    public String getDisplayName() {
        return "Python";
    }

    @Override
    public String getPreferredExtension() {
        return "py";
    }

    @Override
    public Parser getParser() {
        return new PythonParser();
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean hasStructureScanner() {
        return true;
    }

    @Override
    public StructureScanner getStructureScanner() {
        return new PythonStructureScanner();
    }

    @Override
    public SemanticAnalyzer getSemanticAnalyzer() {
        return new PythonSemanticHighlighter();
    }

    @Override
    public KeystrokeHandler getKeystrokeHandler() {
        return new PythonKeystrokeHandler();
    }

    @Override
    public boolean hasOccurrencesFinder() {
        return true;
    }

    @Override
    public OccurrencesFinder getOccurrencesFinder() {
        return new PythonOccurrencesMarker();
    }

    @Override
    public CodeCompletionHandler getCompletionHandler() {
        return new PythonCodeCompleter();
    }

    @Override
    public InstantRenamer getInstantRenamer() {
        return new PythonInstantRename();
    }

    @Override
    public Indexer getIndexer() {
        return new PythonIndexer();
    }

    @Override
    public boolean hasHintsProvider() {
        return true;
    }

    @Override
    public HintsProvider getHintsProvider() {
        return new PythonHintsProvider();
    }

    @Override
    public DeclarationFinder getDeclarationFinder() {
        return new PythonDeclarationFinder();
    }

    @Override
    public IndexSearcher getIndexSearcher() {
        return new PythonIndexSearcher();
    }

    @Override
    public boolean hasFormatter() {
        return true;
    }

    @Override
    public Formatter getFormatter() {
        return new PythonFormatter();
    }

    @Override
    public Collection<FileObject> getCoreLibraries() {
        return Collections.singletonList(getPythonStubs());
    }


    // TODO - add classpath recognizer for these ? No, don't need go to declaration inside these files...
    public static FileObject getPythonStubs() {
        if (jsStubsFO == null) {
            // Core classes: Stubs generated for the "builtin" Ruby libraries.
            File clusterFile = InstalledFileLocator.getDefault().locate(
                    "modules/org-netbeans-modules-python-editor.jar", null, false); // NOI18N

            if (clusterFile != null) {
                File jsStubs =
                        new File(clusterFile.getParentFile().getParentFile().getAbsoluteFile(),
                        "pythonstubs"); // NOI18N
                assert jsStubs.exists() && jsStubs.isDirectory() : "No stubs found";
                jsStubsFO = FileUtil.toFileObject(jsStubs);
            }
        }

        return jsStubsFO;
    }
}
