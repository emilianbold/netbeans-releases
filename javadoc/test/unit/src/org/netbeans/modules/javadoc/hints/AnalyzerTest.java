/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javadoc.hints;

import com.sun.javadoc.Doc;
import com.sun.javadoc.MethodDoc;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.HintContext;

/**
 *
 * @author Jan Pokorsky
 */
public class AnalyzerTest extends JavadocTestSupport {

    public AnalyzerTest(String name) {
        super(name);
    }

    public void testInheritance() throws Exception {
        String code =
                "package test;\n" +
                "/***/" +
                "class ZimaImpl implements Zima {\n" +
                "    public int leden() {\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n" +
                "/***/" +
                "interface Zima {\n" +
                "   /** leden doc */" +
                "   int leden();\n" +
                "}\n";
        
        prepareTest(code);

        TypeElement zimaImpl = info.getTopLevelElements().get(0);
        assertNotNull(zimaImpl);
        ExecutableElement leden = (ExecutableElement) zimaImpl.getEnclosedElements().get(1);
        assertEquals("leden", leden.getSimpleName().toString());
        
        assertTrue(JavadocUtilities.hasInheritedDoc(info, leden));
        
        TreePath zimapath = info.getTrees().getPath(leden);
        assertNotNull(zimapath);
        Analyzer an = new Analyzer(info, doc, zimapath, Severity.WARNING, Access.PRIVATE, new Cancel() {

            @Override
            public boolean isCanceled() {
                return false;
            }
        });
        List<ErrorDescription> errs = an.analyze();
        assertNotNull(errs);
        assertTrue(errs.toString(), errs.isEmpty());
    }
    
    public void testParamInheritance() throws Exception {
        String code =
                "package test;\n" +
                "class ZimaImpl implements Zima {\n" +
                "    /**\n" +
                "     * \n" +
                "     */\n" +
                "    public <T> void leden(T prvniho) {\n" +
                "    }\n" +
                "}\n" +
                "interface Zima {\n" +
                "    /**\n" +
                "     * \n" +
                "     * @param prvniho \n" +
                "     * @param <T> \n" +
                "     */\n" +
                "    <T> void leden(T prvniho);\n" +
                "}\n";
        
        prepareTest(code);
        
        TypeElement zimaImpl = info.getTopLevelElements().get(0);
        assertNotNull(zimaImpl);
        ExecutableElement leden = (ExecutableElement) zimaImpl.getEnclosedElements().get(1);
        assertEquals("leden", leden.getSimpleName().toString());
        Doc ledenDoc = info.getElementUtilities().javaDocFor(leden);
        assertTrue(ledenDoc instanceof MethodDoc);
        MethodDoc mLedenDoc = (MethodDoc) ledenDoc;
        
        assertNotNull(JavadocUtilities.findParamTag(info, mLedenDoc, "prvniho", false, true));
        assertNotNull(JavadocUtilities.findParamTag(info, mLedenDoc, "T", true, true));
    }
    
    public void testThrows() throws Exception {
        String code =
                "package test;\n" +
                "import java.io.IOException;\n" +
                "class ZimaImpl {\n" +
                "    /**\n" +
                "     * @throws NullPointerException reason1\n" +
                "     * @throws IOException reason2\n" +
                "     * @throws InternalError reason3\n" +
                "     */\n" +
                "    public void leden() {\n" +
                "    }\n" +
                "}\n";

        prepareTest(code);

        TypeElement zimaImpl = info.getTopLevelElements().get(0);
        assertNotNull(zimaImpl);
        ExecutableElement leden = (ExecutableElement) zimaImpl.getEnclosedElements().get(1);
        assertEquals("leden", leden.getSimpleName().toString());

        TreePath zimapath = info.getTrees().getPath(leden);
        assertNotNull(zimapath);
        Analyzer an = new Analyzer(info, doc, zimapath, Severity.WARNING, Access.PRIVATE, new Cancel() {

            @Override
            public boolean isCanceled() {
                return false;
            }
        });
        List<ErrorDescription> errs = an.analyze();
        assertNotNull(errs);
        List<String> errorsAsStrings = new ArrayList<String>(errs.size());
        for (ErrorDescription ed : errs) {
            errorsAsStrings.add(ed.toString());
        }
        assertEquals(Arrays.asList("5:7-5:34:warning:Unknown throwable: @throws java.io.IOException"), errorsAsStrings);
    }
}
