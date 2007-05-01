/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.schema.completion;

import java.util.List;
import junit.framework.*;
import javax.swing.text.Document;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Samaresh
 */
public class CompletionPerfTest extends AbstractTestCase {
    
    static final String COMPLETION_TEST_DOCUMENT = "resources/OTA_TravelItinerary.xsd";
    
    public CompletionPerfTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(CompletionPerfTest.class);
        return suite;
    }

    /**
     * Queries elements on OTA.
     */
    public void testPerformance() throws Exception {
        long startTime = System.currentTimeMillis();
        setupCompletion(COMPLETION_TEST_DOCUMENT, null);
        List<CompletionResultItem> items = query(819236);
        long endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime);
        String[] expectedResult = null; //{"po:shipTo", "po:billTo", "po:comment", "po:items"};
        //assertResult(items, expectedResult);
        assert(true);
    }
    
}
