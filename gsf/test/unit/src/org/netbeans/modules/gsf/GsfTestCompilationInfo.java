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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.gsf;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.EmbeddingModel;
import org.netbeans.modules.gsf.api.Error;
import org.netbeans.modules.gsf.api.Index;
import org.netbeans.modules.gsf.api.ParseEvent;
import org.netbeans.modules.gsf.api.ParseListener;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.napi.gsfret.source.ClasspathInfo;
import org.netbeans.napi.gsfret.source.Source;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.api.EditHistory;
import org.netbeans.modules.gsf.api.IncrementalParser;
import org.netbeans.modules.gsf.api.Indexer;
import org.netbeans.modules.gsf.api.Parser;
import org.netbeans.modules.gsf.api.ParserFile;
import org.netbeans.modules.gsf.api.SourceFileReader;
import org.netbeans.modules.gsf.api.TranslatedSource;
import org.netbeans.modules.gsf.spi.DefaultParserFile;
import org.netbeans.modules.gsfpath.api.classpath.ClassPath;
import org.netbeans.modules.gsfpath.spi.classpath.support.ClassPathSupport;
import org.netbeans.modules.gsfret.source.GlobalSourcePath;
import org.netbeans.modules.gsfret.source.usages.ClassIndexImpl;
import org.netbeans.modules.gsfret.source.usages.ClassIndexManager;
import org.netbeans.napi.gsfret.source.ClassIndex;
import org.netbeans.napi.gsfret.source.ClasspathInfo.PathKind;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author tor
 */
public final class GsfTestCompilationInfo extends CompilationInfo {
    protected String text;
    protected Document doc;
    protected Source source;
    protected int caretOffset = -1;
    protected Map<String,ParserResult> embeddedResults = new HashMap<String,ParserResult>();
    protected GsfTestBase test;
    protected Index index;
    protected ParserResult previousResult;
    protected EditHistory editHistory;

    public GsfTestCompilationInfo(GsfTestBase test, FileObject fileObject, BaseDocument doc, String text) throws IOException {
        super(fileObject);
        this.test = test;
        this.text = text;
        assert text != null;
        this.doc = doc;
        if (fileObject != null) {
            //source = Source.forFileObject(fileObject);
            //ClasspathInfo cpInfo = ClasspathInfo.create(fileObject);
            ClasspathInfo cpInfo = createForTest(fileObject);
            source = Source.create(cpInfo, Collections.singletonList(fileObject));
        }
    }
    
    public void setCaretOffset(int caretOffset) {
        this.caretOffset = caretOffset;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        embeddedResults.clear(); // Make sure we recompute if necessary
    }

    public Source getSource() {
        return source;
    }

    static Index mostRecentIndex;

    public Index getIndex(String mimeType) {
        if (index == null) {
            test.initializeRegistry();
            ClasspathInfo cpi = source.getClasspathInfo();
            if (cpi != null) {
                index = cpi.getClassIndex(mimeType);
                ClassIndex classIndex = (ClassIndex)index;
                updateIndexForTests(cpi, classIndex, source);
                mostRecentIndex = index;
            }
        }
        
        return index;
    }
    
    @Override
    public Document getDocument() {
        return this.doc;
    }

    public void setPreviousResult(ParserResult previousResult) {
        this.previousResult = previousResult;
    }

    public void setEditHistory(EditHistory editHistory) {
        this.editHistory = editHistory;
    }
    
    @Override
    public Collection<? extends ParserResult> getEmbeddedResults(String mimeType) {
        ParserResult result = getEmbeddedResult(mimeType, 0);
        if (result != null) {
            return Collections.singletonList(result);
        } else {
            return Collections.emptyList();
        }
    }

    public String getPreferredMimeType() {
        return test.getPreferredMimeType();
    }
    
    @Override
    public ParserResult getEmbeddedResult(String embeddedMimeType, int offset) {
        if (embeddedResults.get(embeddedMimeType) == null) {
            String mimeType = getPreferredMimeType();


            Collection<? extends TranslatedSource> translatedSources = null;
            TranslatedSource translatedSource = null;

            if (!embeddedMimeType.equals(mimeType)) {
                // Embedding model?
                EmbeddingModel model = LanguageRegistry.getInstance().getEmbedding(embeddedMimeType, mimeType);
                if (model != null) {
                    translatedSources = model.translate(doc);
                    if (translatedSources.size() > 0) {
                        translatedSource = translatedSources.iterator().next();
                        text = translatedSource.getSource();
                    }
                }
            }

            if (translatedSource == null) {
                GsfTestBase.assertEquals(mimeType, embeddedMimeType);
            }

            GsfTestParseListener listener = new GsfTestParseListener();
            List<ParserFile> sourceFiles = new ArrayList<ParserFile>(1);
            ParserFile file = new DefaultParserFile(getFileObject(), null, false);
            sourceFiles.add(file);

            Parser parser;
            Language language = LanguageRegistry.getInstance().getLanguageByMimeType(embeddedMimeType);
            if (language != null) {
                parser = language.getParser();
            } else {
                parser = test.getParser();
            }
            GsfTestBase.assertNotNull(parser);

            SourceFileReader reader = new SourceFileReader() {

                public CharSequence read(ParserFile file) throws IOException {
                    if (text.length() == 0) {
                        try {
                            text = doc.getText(0, doc.getLength());
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                            GsfTestBase.fail(ex.toString());
                        }
                    }
                    return text;
                }

                public int getCaretOffset(ParserFile file) {
                    return caretOffset;
                }
            };

            ParserResult parserResult = null;
            if (editHistory != null && previousResult != null && parser instanceof IncrementalParser) {
                IncrementalParser incrementalParser = (IncrementalParser)parser;
                parserResult = incrementalParser.parse(file, reader, translatedSource, editHistory, previousResult);
            }

            if (parserResult == null) {
                Parser.Job request = new Parser.Job(sourceFiles, listener, reader, translatedSource);
                parser.parseFiles(request);

                parserResult = listener.getParserResult();
            }
            if (parserResult != null) {
                for (Error error : listener.getErrors()) {
                    parserResult.addError(error);
                }
                embeddedResults.put(embeddedMimeType, parserResult);
                parserResult.setInfo(this);
                parserResult.setTranslatedSource(translatedSource);
            }
            test.validateParserResult(parserResult);
        }

        return embeddedResults.get(embeddedMimeType);
    }

    @Override
    public List<Error> getErrors() {
        // Force initialization
        getEmbeddedResult(getPreferredMimeType(), 0);

        List<Error> errors = new ArrayList<Error>();
        for (ParserResult result : embeddedResults.values()) {
            errors.addAll(result.getDiagnostics());
        }

        return errors;
    }

    private static final ClassPath EMPTY_PATH = ClassPathSupport.createClassPath(new URL[0]);
    
    public static ClasspathInfo createForTest(FileObject fo) {
        ClassPath bootPath = ClassPath.getClassPath(fo, ClassPath.BOOT);
        if (bootPath == null) {
            bootPath = EMPTY_PATH;
        }
        ClassPath compilePath = ClassPath.getClassPath(fo, ClassPath.COMPILE);
        if (compilePath == null) {
            compilePath = EMPTY_PATH;
        }
        ClassPath srcPath = ClassPath.getClassPath(fo, ClassPath.SOURCE);
        if (srcPath == null) {
            //srcPath = EMPTY_PATH;
            srcPath = ClassPathSupport.createClassPath(fo.getParent());
        }
        return ClasspathInfo.create(bootPath, compilePath, srcPath);
    }

    // Ensure that we have a proper index for the given source
    public void updateIndexForTests(ClasspathInfo cpInfo, ClassIndex classIndex, Source js) { // TESTS ONLY
        ClassPath sourcePath = cpInfo.getClassPath(PathKind.SOURCE);
        Language language = LanguageRegistry.getInstance().getLanguageByMimeType(getPreferredMimeType());
        GsfTestBase.assertNotNull(language);
        //final Iterable<? extends ClassIndexImpl> queries = this.getQueries(EnumSet.of(SearchScope.SOURCE));
        HashSet<ClassIndexImpl> sourceIndeces = new HashSet<ClassIndexImpl>();
        createQueriesForTest (language, sourcePath, true, sourceIndeces);
        for (ClassIndexImpl query : sourceIndeces) {
            query.setDirty(js);
        }
        classIndex.initForTest(sourceIndeces);
    }

    // Copied from createQueriesForRoot in ClassIndex, tweaked to create query unconditionally
    private static void createQueriesForTest(final Language language, final ClassPath cp, final boolean sources, final Set<? super ClassIndexImpl> queries) {
        final GlobalSourcePath gsp = GlobalSourcePath.getDefault();
        List<ClassPath.Entry> entries = cp.entries();
        Indexer indexer = language.getIndexer();
        if (indexer == null) {
            return;
        }

        for (ClassPath.Entry entry : entries) {
            try {
                if (!indexer.acceptQueryPath(entry.getURL().toExternalForm())) {
                    continue;
                }
                URL[] srcRoots;
                if (!sources) {
                    URL srcRoot = org.netbeans.modules.gsfret.source.usages.Index.getSourceRootForClassFolder(language, entry.getURL());
                    if (srcRoot != null) {
                        srcRoots = new URL[]{srcRoot};
                    } else {
                        srcRoots = gsp.getSourceRootForBinaryRoot(entry.getURL(), cp, true);
                        if (srcRoots == null) {
                            srcRoots = new URL[]{entry.getURL()};
                        }
                    }
                //End to be removed
                } else {
                    srcRoots = new URL[]{entry.getURL()};
                }
                for (URL srcRoot : srcRoots) {
                    //ClassIndexImpl ci = ClassIndexManager.get(language).getUsagesQuery(srcRoot);
                    ClassIndexImpl ci = ClassIndexManager.get(language).createUsagesQuery(srcRoot, true);
                    if (ci != null) {
                        queries.add(ci);
                    }
                }
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
    }

    public static class GsfTestParseListener implements ParseListener {
        private final List<Error> errors = new ArrayList<Error>();
        private ParserResult result;

        public void started(ParseEvent e) {
            errors.clear();
        }

        public void error(Error error) {
            errors.add(error);
        }

        public void exception(Exception exception) {
            Exceptions.printStackTrace(exception);
        }

        public void finished(ParseEvent e) {
            result = e.getResult();
        }
        
        public List<Error> getErrors() {
            return errors;
        }
        
        public ParserResult getParserResult() {
            return result;
        }
    }
    
}
