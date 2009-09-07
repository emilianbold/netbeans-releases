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
package org.netbeans.modules.html.editor.xhtml;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeListener;
import org.netbeans.api.lexer.Language;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;

public class XhtmlElLanguage extends DefaultLanguageConfig {

    public XhtmlElLanguage() {
    }

    @Override
    public Language getLexerLanguage() {
        return XhtmlElTokenId.language();
    }

    @Override
    public Parser getParser() {
        return new XhtmlELParser();
    }

    @Override
    public String getDisplayName() {
        return "XHTML";
    }

    @Override
    public StructureScanner getStructureScanner() {
        return new Scanner();
    }

    @Override
    public boolean hasStructureScanner() {
        return super.hasStructureScanner();
    }

    @Override
    public String getPreferredExtension() {
        return "xhtml"; // NOI18N
    }

    private static class XhtmlELParser extends Parser {

        private HtmlParserResult lastResult;
        private ParserResult instance;

        public
        @Override
        void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
            instance = new FakeResult(snapshot);
        }

        public
        @Override
        Result getResult(Task task) throws ParseException {
            return instance;
        }

        @Override
        public void cancel() {
        }

        @Override
        public void addChangeListener(ChangeListener changeListener) {
        }

        @Override
        public void removeChangeListener(ChangeListener changeListener) {
        }

        private static final class FakeResult extends ParserResult {

            public FakeResult(Snapshot snapshot) {
                super(snapshot);
            }

            @Override
            public List<? extends Error> getDiagnostics() {
                return Collections.emptyList();
            }

            @Override
            protected void invalidate() {
            }
        }
    }

    public static class Scanner implements StructureScanner {

        @Override
        public Map<String, List<OffsetRange>> folds(ParserResult info) {
            return Collections.emptyMap();
        }

        @Override
        public List<? extends StructureItem> scan(ParserResult info) {
            return Collections.emptyList();
        }

        @Override
        public Configuration getConfiguration() {
            return new Configuration(false, false, 0);
        }
    }
}
