/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.jsf.editor.el;

import java.util.Iterator;
import java.util.SortedSet;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.jsf.editor.TestBase;

/**
 *
 * @author marekfukala
 */
public class JsfVariablesModelTest extends TestBase {

    public JsfVariablesModelTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        JsfVariablesModel.inTest = true; //do not require JsfSupport since we have no project
    }

    public void testCreateModel() throws ParseException {
        HtmlParserResult result = getHtmlParserResult("testfiles/test.xhtml");
        JsfVariablesModel model = JsfVariablesModel.getModel(result);
        assertNotNull(model);
    }

    public void testModel() throws ParseException {
        HtmlParserResult result = getHtmlParserResult("testfiles/test.xhtml");
        JsfVariablesModel model = JsfVariablesModel.getModel(result);
        assertNotNull(model);

        SortedSet<JsfVariableContext> contextsList = model.getContexts();
        assertNotNull(contextsList);

        for(JsfVariableContext context : contextsList) {
            System.out.println(context);
        }

        assertEquals(3, contextsList.size());

        Iterator<JsfVariableContext> contexts = contextsList.iterator();
        JsfVariableContext first = contexts.next();
        JsfVariableContext second = contexts.next();
        JsfVariableContext third = contexts.next();

        assertEquals(385, first.getFrom());
        assertEquals(1161, first.getTo());

        assertEquals(674, second.getFrom());
        assertEquals(1114, second.getTo());

        assertEquals(1170, third.getFrom());
        assertEquals(1527, third.getTo());

        //test get element by offset

        //out of the contexts regions
        assertNull(model.getContext(0));
        assertNull(model.getContext(1600));
        assertNull(model.getContext(1165));

        //inside
        assertEquals(first, model.getContext(390));
        assertEquals(first, model.getContext(673));
        assertEquals(first, model.getContext(1150));

        assertEquals(second, model.getContext(700));
        assertEquals(second, model.getContext(1100));

        assertEquals(third, model.getContext(1200));

        //boundaries - start is inclusive, end exclusive
        assertEquals(first, model.getContext(385));
        assertEquals(first, model.getContext(1114));
        assertNull(model.getContext(1161));
        

    }

}