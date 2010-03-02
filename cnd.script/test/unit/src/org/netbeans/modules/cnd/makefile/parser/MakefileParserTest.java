/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makefile.parser;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.cnd.makefile.lexer.MakefileTokenId;
import org.netbeans.modules.cnd.makefile.model.AbstractMakefileElement;
import org.netbeans.modules.cnd.makefile.model.MakefileAssignment;
import org.netbeans.modules.cnd.makefile.model.MakefileRule;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import static org.junit.Assert.*;

/**
 *
 * @author Alexey Vladykin
 */
public class MakefileParserTest extends NbTestCase {

    public MakefileParserTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        MimePath mimePath = MimePath.parse(MIMENames.MAKEFILE_MIME_TYPE);
        MockMimeLookup.setInstances(mimePath, MakefileTokenId.language());
    }

    public void testSample() throws Exception {
        MakefileParseResult result = parseFile(new File(getDataDir(), "Makefile1"));
        assertNotNull(result);
        List<? extends AbstractMakefileElement> elements = result.getElements();
        assertNotNull(elements);

        MakefileAssignment rm = (MakefileAssignment) elements.get(0);
        assertEquals(ElementKind.VARIABLE, rm.getKind());
        assertEquals("RM", rm.getName());
        assertEquals("rm", rm.getValue());

        MakefileAssignment cc = (MakefileAssignment) elements.get(1);
        assertEquals(ElementKind.VARIABLE, cc.getKind());
        assertEquals("CC", cc.getName());
        assertEquals("gcc", cc.getValue());

        MakefileRule buildConf = (MakefileRule) elements.get(2);
        assertEquals(ElementKind.RULE, buildConf.getKind());
        assertEquals(Collections.singletonList(".build-conf"), buildConf.getTargets());
        assertEquals(Arrays.asList("$(BUILD_SUBPROJECTS)", "dist/Debug/GNU-Solaris-x86/quote_1"), buildConf.getPrerequisites());

        MakefileRule cleanConf = (MakefileRule) elements.get(3);
        assertEquals(ElementKind.RULE, cleanConf.getKind());
        assertEquals(Collections.singletonList(".clean-conf"), cleanConf.getTargets());
        assertEquals(Collections.emptyList(), cleanConf.getPrerequisites());

        MakefileRule done = (MakefileRule) elements.get(4);
        assertEquals(ElementKind.RULE, done.getKind());
        assertEquals(Collections.singletonList(".DONE"), done.getTargets());
        assertEquals(Collections.emptyList(), done.getPrerequisites());

        assertEquals(5, elements.size());
    }

    private MakefileParseResult parseFile(File file) throws ParseException {
        FileObject fobj = FileUtil.toFileObject(file);
        MakefileParser parser = new MakefileParser();
        parser.parse(Source.create(fobj).createSnapshot(), null, null);
        return parser.getResult(null);
    }
}
