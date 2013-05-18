/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.knockout;

import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.html.editor.embedding.JsEmbeddingProviderTest;
import org.netbeans.modules.html.editor.gsf.HtmlLanguage;
import org.openide.filesystems.FileObject;

/**
 *
 * @author marekfukala
 */
public class KOJsEmbeddingProviderPluginTest extends CslTestBase {

    public KOJsEmbeddingProviderPluginTest(String testName) {
        super(testName);
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new HtmlLanguage();
    }

    @Override
    protected String getPreferredMimeType() {
        return "text/html";
    }

    public void testSimple() {
        FileObject index = getTestFile("KOTestProject/public_html/simple.html");
        BaseDocument document = getDocument(index);
        JsEmbeddingProviderTest.assertEmbedding(document,
                "__netbeans_import__('js/libs/knockout-2.2.1/knockout-min.js');\n"
                + "\n"
                + "__netbeans_import__('js/simple_model.js');\n"
                + "\n"
                + "(function(){\n"
                + "var $root = ko.$bindings;\n"
                + "var $data = $root;\n"
                + "var $parent = undefined;\n"

                + "(function(){\n"
                + "addSeat;\n"
                + "});\n"
                + "(function(){\n"
                + "seats().length < 5;\n"
                + "});\n"
                + "});\n");
    }
}
