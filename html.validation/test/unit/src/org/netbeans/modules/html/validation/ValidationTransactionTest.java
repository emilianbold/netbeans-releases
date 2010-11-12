package org.netbeans.modules.html.validation;

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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
import java.util.Collection;
import java.io.IOException;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.editor.ext.html.parser.api.HtmlVersion;
import org.netbeans.editor.ext.html.parser.api.ParseException;
import org.netbeans.editor.ext.html.parser.api.ProblemDescription;
import org.netbeans.junit.NbTestCase;
import org.xml.sax.SAXException;

/**
 *
 * @author marekfukala
 */
public class ValidationTransactionTest extends NbTestCase {

    public ValidationTransactionTest(String name) {
        super(name);
    }

    public static Test xsuite() {
//        ValidationTransaction.enableDebug();

        String testName = "testFragment";
        System.err.println("Running only following test: " + testName);
        TestSuite suite = new TestSuite();
        suite.addTest(new ValidatorImplTest(testName));
        return suite;
    }

    public void testBasic() throws SAXException, IOException, ParseException {
//        ValidationTransaction.enableDebug();

        validate("<!doctype html> <html><head><title>hello</title></head><body><div>ahoj!</div></body></html>", true);
        validate("<!doctype html> chybi open tag</div>", false);
        validate("<!doctype html> <div> chybi close tag", false);

        validate("<!doctype html>\n"
                + "<html><head><title>hello</title></head>\n"
                + "<body>\n"
                + "<div>ahoj!</Xiv>\n"
                + "</body></html>\n", false);

        validate("1\n"
                + "23\n"
                + "345\n"
                + "<!doctype html>\n"
                + "<html><head><title>hello</title></head>\n"
                + "<body>\n"
                + "<div>ahoj!</Xiv>\n"
                + "</body></html>\n", false);

    }

    public void testErrorneousSources() throws SAXException {
        //IIOBE from LinesMapper.getSourceOffsetForLocation(LinesMapper.java:129)
        validate("<!doctype html> "
                + "<html>    "
                + "<title>dd</title>"
                + "<b"
                + "a"
                + "</body>"
                + "</html>    ", false);
    }

    public void testMathML() throws SAXException {
        validate("<!doctype html> "
                + "<html>    "
                + "<title>dd</title>"
                + "<body>"
                + "  <math>"
                + "     <mi>x</mi>"
                + "     <mo>=</mo>"
                + "  </math>"
                + "</body>"
                + "</html>    ", true);
    }

    public void testXhtml() throws SAXException {
        validate("<?xml version='1.0' encoding='UTF-8' ?>"
                + "<html xmlns=\"http://www.w3.org/1999/xhtml\">"
                + "<head><title>title</title></head>"
                + "<body>"
                + "</body>"
                + "</html>    ", true, HtmlVersion.XHTML5);
    }

    public void testHtml4() throws SAXException {
        validate("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n"
                + "<html>"
                + "     <head>"
                + "         <title>hello</title>"
                + "     </head>"
                + "     <body>"
                + "         <div>ahoj!</div>"
                + "     </body>"
                + "</html>", true, HtmlVersion.HTML41_TRANSATIONAL);
    }

//    public void testFragment() throws SAXException {
//        String code = "<div>aaa</div>";
//        ValidationTransaction vt = ValidationTransaction.create(HtmlVersion.HTML5);
//        vt.setBodyFragmentContextMode(true);
//        vt.validateCode(code);
//        for (ProblemDescription pd : vt.getFoundProblems()) {
//                System.err.println(pd);
//            }
//        assertTrue(vt.isSuccess());
//    }

    private void validate(String code, boolean expectedPass) throws SAXException {
        validate(code, expectedPass, HtmlVersion.HTML5);
    }

    private void validate(String code, boolean expectedPass, HtmlVersion version) throws SAXException {
        System.out.print("Validating code " + code.length() + " chars long...");
        ValidationTransaction vt = ValidationTransaction.create(version);
        vt.validateCode(code);

        Collection<ProblemDescription> problems = vt.getFoundProblems(ProblemDescription.WARNING);

        if (expectedPass && !problems.isEmpty()) {
            System.err.println("There are some unexpected problems:");
            for (ProblemDescription pd : problems) {
                System.err.println(pd);
            }
        }

        assertEquals(expectedPass, vt.isSuccess());
        assertEquals(expectedPass, problems.isEmpty());

        System.out.println("done in " + vt.getValidationTime() + " ms with " + problems.size() + " problems.");
    }
}
