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

package org.netbeans.editor.ext.html;

import java.util.Set;
import org.netbeans.editor.ext.html.dtd.*;
import org.netbeans.editor.ext.html.dtd.DTD.Content;
import org.netbeans.editor.ext.html.dtd.DTD.Element;
import org.netbeans.modules.html.editor.NbReaderProvider;
import org.netbeans.modules.html.editor.test.TestBase;
import static org.junit.Assert.*;

/**
 *
 * @author marekfukala
 */
public class DTDParserTest extends TestBase {

    private static final String FALLBACK_DOCTYPE = "-//W3C//DTD HTML 4.01//EN";  // NOI18N

    public DTDParserTest() {
        super(DTDParserTest.class.getName());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        NbReaderProvider.setupReaders();
    }


    public void testDTDParser_HTML_BODY_Elements() {
        DTD dtd = org.netbeans.editor.ext.html.dtd.Registry.getDTD(FALLBACK_DOCTYPE, null);
        assertNotNull(dtd);

        Element htmlElement = dtd.getElement("HTML");
        assertNotNull(htmlElement);

        Element headElement = dtd.getElement("HEAD");
        assertNotNull(headElement);

        Content c = htmlElement.getContentModel().getContent();
        assertTrue(c.getPossibleElements().contains(headElement));

        c = c.reduce("HEAD");

        Element bodyElement = dtd.getElement("BODY");
        assertNotNull(bodyElement);

        assertTrue(c.getPossibleElements().contains(bodyElement));
    }

      public void testTable() {
        DTD dtd = org.netbeans.editor.ext.html.dtd.Registry.getDTD(FALLBACK_DOCTYPE, null);
        assertNotNull(dtd);

        Element el = dtd.getElement("TABLE");
        assertNotNull(el);
        Content c = el.getContentModel().getContent();

//        dumpContent(c);
//        assertEquals(Content.EMPTY_CONTENT, c.reduce("TR"));

      }

      public void testOption() {
        DTD dtd = org.netbeans.editor.ext.html.dtd.Registry.getDTD(FALLBACK_DOCTYPE, null);
        assertNotNull(dtd);

        Element el = dtd.getElement("OPTION");
        assertNotNull(el);
        assertFalse(el.isEmpty());

      }

//      private void dumpContent(Content c) {
//          for(Object obj : c.getPossibleElements()) {
//            Element e = (Element)obj;
//            System.out.println(e);
//        }
//      }

}