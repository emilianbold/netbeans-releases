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

package org.netbeans.modules.languages.yaml;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.gsf.GsfTestCompilationInfo;
import org.netbeans.modules.gsf.api.Error;
import org.netbeans.modules.gsf.api.ParseEvent;
import org.netbeans.modules.gsf.api.ParseListener;
import org.netbeans.modules.gsf.api.Parser.Job;
import org.netbeans.modules.gsf.api.ParserFile;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.SourceFileReader;
import org.netbeans.modules.gsf.api.TranslatedSource;
import org.netbeans.modules.gsf.spi.DefaultParserFile;
import org.netbeans.modules.ruby.RubyParser;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tor Norbye
 */
public class YamlParserTest extends YamlTestBase {
    public YamlParserTest(String testName) {
        super(testName);
    }

    public void testErrors1() throws Exception {
        checkErrors("testfiles/error.yaml");
    }

    public void testErrors2() throws Exception {
        checkErrors("testfiles/error2.yaml");
    }

    public void testErrors3() throws Exception {
        checkErrors("testfiles/error3.yaml");
    }

    public void testHuge() throws Exception {
        StringBuilder sb = new StringBuilder();
        String s = readFile(getTestFile("testfiles/database.yml"));
        while (sb.length() < 1024*1024) {
            sb.append(s);
        }
        String huge = sb.toString();
        String relFilePath = "generated-huge.yml";
        GsfTestCompilationInfo info = getInfoForText(huge, relFilePath);
        String text = info.getText();
        assertNotNull(text);

        ParserResult pr = info.getEmbeddedResult(info.getPreferredMimeType(), 0);
        assertNotNull(pr);

        List<Error> diagnostics = pr.getDiagnostics();
        String annotatedSource = annotateErrors(text, diagnostics);
        assertDescriptionMatches("testfiles/" + relFilePath, annotatedSource, false, ".errors", false);
        // Make sure we actually skipped parsing this large document!
        assertNull(((YamlParserResult)pr).getObject());
    }

    public void testValidResult() throws Exception {
        // Make sure we get a valid parse result out of an aborted parse
        FileObject fo = getTestFile("testfiles/error3.yaml");
        ParserFile file = new DefaultParserFile(fo, null, false);
        List<ParserFile> files = Collections.<ParserFile>singletonList(file);
        final ParserResult[] resultHolder = new ParserResult[1];
        final Exception[] exceptionHolder = new Exception[1];

        ParseListener listener = new ParseListener() {

            public void started(ParseEvent e) {
            }

            public void finished(ParseEvent e) {
                resultHolder[0] = e.getResult();
            }

            public void error(Error e) {
            }

            public void exception(Exception e) {
                exceptionHolder[0] = e;
            }

        };
        TranslatedSource ts = null;
        SourceFileReader reader = new SourceFileReader() {

            public CharSequence read(ParserFile file) throws IOException {
                throw new IOException("Simulate failure");
            }

            public int getCaretOffset(ParserFile file) {
                return -1;
            }

        };
        Job job = new Job(files, listener, reader, ts);
        new YamlParser().parseFiles(job);

        assertNotNull("Parser result must be nonnull", resultHolder[0]);
        assertNotNull("Expected to have the listener notified of a failure", exceptionHolder[0]);
    }
}
