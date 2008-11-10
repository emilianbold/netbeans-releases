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

package org.netbeans.modules.groovy.editor.api.parser;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.groovy.editor.api.AstUtilities;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.modules.gsf.api.Parser;
import org.netbeans.modules.gsf.api.ParserFile;
import org.netbeans.modules.gsf.api.SourceFileReader;
import org.netbeans.modules.gsf.api.TranslatedSource;
import org.netbeans.modules.gsf.spi.DefaultParseListener;
import org.netbeans.modules.gsf.spi.DefaultParserFile;
import org.openide.filesystems.FileObject;

/**
 * Helper class to get acces to parse result
 *
 * @author Martin Adamek
 */
public class SourceUtils {

    // package private
    static void runUserActionTask(final FileObject fileObject, final CancellableTask<GroovyParserResult> task, boolean waitJavaScanFinisihed) throws Exception {
        ParserFile parserFile = new DefaultParserFile(fileObject, null, false);
        if (parserFile != null) {
            List<ParserFile> files = Collections.singletonList(parserFile);
            SourceFileReader reader =
                new SourceFileReader() {
                    public CharSequence read(ParserFile file)
                        throws IOException {
                        Document doc = AstUtilities.getBaseDocument(fileObject, true);

                        if (doc == null) {
                            return ""; // NOI18N
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

            DefaultParseListener listener = new DefaultParseListener();

            // TODO - embedding model?
            TranslatedSource translatedSource = null; // TODO - determine this here?
            Parser.Job job = new Parser.Job(files, listener, reader, translatedSource);
            GroovyParser parser = new GroovyParser();
            parser.setWaitJavaScanFinished(waitJavaScanFinisihed);
            parser.parseFiles(job);

            GroovyParserResult result = (GroovyParserResult) listener.getParserResult();
            task.run(result);
        }
    }
    
    public static void runUserActionTask(final FileObject fileObject, final CancellableTask<GroovyParserResult> task) throws Exception {
        runUserActionTask(fileObject, task, true);
    }

}
