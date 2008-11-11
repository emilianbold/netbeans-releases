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
package org.netbeans.napi.gsfret.source;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.modules.csl.api.EmbeddingModel;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.ParseEvent;
import org.netbeans.modules.csl.api.ParseListener;
import org.netbeans.modules.csl.api.Parser;
import org.netbeans.modules.csl.api.ParserFile;
import org.netbeans.modules.csl.api.ParserResult;
import org.netbeans.modules.csl.api.SourceFileReader;
import org.netbeans.modules.csl.api.TranslatedSource;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.source.parsing.SourceFileObject;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;


public class ParserTaskImpl {
    private ParseListener listener;
    private Language language;

    public ParserTaskImpl(Language language) {
        this.language = language;
    }

    public void setParseListener(ParseListener listener) {
        this.listener = listener;
    }

    public Iterable<ParserResult> parse(ParserFile... files) throws IOException {
        List<ParserResult> results = new ArrayList<ParserResult>(files.length);

        for (ParserFile file : files) {
            if (file == null) {
                continue;
            }

            List<ParserFile> sourceFiles = new ArrayList<ParserFile>(1);
            sourceFiles.add(file);

            FileObject fo = file.getFileObject();
            if (fo == null) {
                // See for example #131671 - some short lived file has been deleted in the meantime.
                Logger.getLogger(ParserTaskImpl.class.getName()).log(Level.WARNING, "No fileobject for " + file);
                continue;
            }

            // We're parsing only one language here!
            assert language != null;
            Parser parser = language.getParser(); // Todo - call createParserTask here?

            if (parser != null) {
                //ParserResult result = parser.parseBuffer(currentInfo.getFileObject(), buffer, errorHandler);
                final ParserResult[] resultHolder = new ParserResult[1];
                ParseListener delegatingListener = // TODO make innerclass
                    new ParseListener() {
                        public void started(ParseEvent e) {
                            ParserTaskImpl.this.listener.started(e);
                        }

                        public void error(Error e) {
                            ParserTaskImpl.this.listener.error(e);
                        }

                        public void exception(Exception e) {
                            ParserTaskImpl.this.listener.exception(e);
                        }

                        public void finished(ParseEvent e) {
                            // TODO - check state
                            if (e.getKind() == ParseEvent.Kind.PARSE) {
                                resultHolder[0] = e.getResult();
                            }
                            ParserTaskImpl.this.listener.finished(e);
                        }
                    };

                String mimeType = file.getFileObject().getMIMEType();
                LanguageRegistry registry = LanguageRegistry.getInstance();
                EmbeddingModel model = registry.getEmbedding(language.getMimeType(), mimeType);

                if (model != null) {
                    FileObject bufferFo = file.getFileObject();
                    Document document = GsfUtilities.getDocument(bufferFo, true, true);
                    if (document == null) {
                        continue;
                    }

                    Collection<? extends TranslatedSource> translations = model.translate(document);
                    for (TranslatedSource translatedSource : translations) {
                        String buffer = translatedSource.getSource();
                        SourceFileReader reader = new StringSourceFileReader(buffer, bufferFo);
                        Parser.Job job = new Parser.Job(sourceFiles, delegatingListener, reader, translatedSource);
                        parser.parseFiles(job);


                        ParserResult result = resultHolder[0];
                        result.setTranslatedSource(translatedSource);
                        assert result != null;
                        results.add(result);
                    }
                } else {
                    SourceFileReader reader =
                        new SourceFileReader() {
                            public CharSequence read(ParserFile file) throws IOException {
                                //assert fileObject == file;
                                //assert file.getFileObject() != null : file.getNameExt();
                                // #100618: Get more info but don't blow up
                                if (file.getFileObject() == null) {
                                    ErrorManager.getDefault().log("Null fileobject for " + file.getNameExt());
                                    return "";
                                }
                                return SourceFileObject.create(file.getFileObject()).getCharContent(false).toString();
                            }
                            public int getCaretOffset(ParserFile fileObject) {
                                return -1;
                            }
                        };

                    Parser.Job job = new Parser.Job(sourceFiles, delegatingListener, reader, null);
                    parser.parseFiles(job);
                    ParserResult result = resultHolder[0];
                    assert result != null;
                    results.add(result);
                }
            }
        }

        return results;
    }
}
