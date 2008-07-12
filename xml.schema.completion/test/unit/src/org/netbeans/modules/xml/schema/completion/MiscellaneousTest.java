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
package org.netbeans.modules.xml.schema.completion;

import java.util.List;
import junit.framework.*;
import org.netbeans.modules.xml.schema.completion.util.CompletionUtil;
import org.netbeans.modules.xml.schema.completion.util.CompletionUtil.DocRoot;
import org.netbeans.modules.xml.schema.completion.util.CompletionUtil.DocRootAttribute;

/**
 * Tests various utility methods that are used heavily for code completion.
 * 
 * @author Samaresh
 */
public class MiscellaneousTest extends AbstractTestCase {
        
    public MiscellaneousTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new MiscellaneousTest("testGetDocRoot"));
        suite.addTest(new MiscellaneousTest("testIsDTDBasedDocument1"));
        suite.addTest(new MiscellaneousTest("testIsDTDBasedDocument2"));        
        return suite;
    }

    /**
     * Finds the docroot and its attributes.
     */
    public void testGetDocRoot() throws Exception {
        String[] expectedResult = {
            "xmlns:c=http://www.camera.com",
            "xmlns:n=http://www.nikon.com",
            "xmlns:o=http://www.olympus.com",
            "xmlns:p=http://www.pentax.com",
            "xmlns:xsi=http://www.w3.org/2001/XMLSchema-instance",
            "xsi:schemaLocation=http://www.camera.com camera.xsd"
        };
        
        setupCompletion("resources/camera.xml", null);
        DocRoot root = CompletionUtil.getDocRoot(getDocument());
        assert("c:camera".equals(root.getName()));
        List<DocRootAttribute> attributes = root.getAttributes();
        assert(attributes.size() == 6);
        String[] results = new String[attributes.size()];
        for(int i=0; i<attributes.size(); i++) {
            results[i] = attributes.get(i).toString();
        }
        assertResult(results, expectedResult);
    }
    
    /**
     * Tests to see if the document declares any DOCTYPE.
     */
    public void testIsDTDBasedDocument1() throws Exception {
        setupCompletion("resources/Doctype.xml", null);
        assert(CompletionUtil.isDTDBasedDocument(instanceDocument));
    }
    
    /**
     * Tests to see if the document declares any DOCTYPE. Also it serves to
     * test the performance of finding DOCTYPE declaration in a XML.
     */
    public void testIsDTDBasedDocument2() throws Exception {
        long start = System.currentTimeMillis();
        setupCompletion("resources/NFL.xml", null);
        assert(!CompletionUtil.isDTDBasedDocument(instanceDocument));
        long end = System.currentTimeMillis();
        System.out.println("Time taken for isDTDBasedDocument: " + (end-start));
    }
}
