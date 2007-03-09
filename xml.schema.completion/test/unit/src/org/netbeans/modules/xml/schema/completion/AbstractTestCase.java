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
public abstract class AbstractTestCase extends TestCase {
    
    protected String instanceResourcePath;
    protected FileObject instanceFileObject;
    protected Document instanceDocument;
    
    public AbstractTestCase(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }
    
    protected void setupCompletion(String path, StringBuffer buffer) throws Exception {
        this.instanceResourcePath = path;
        this.instanceFileObject = Util.getResourceAsFileObject(path);
        this.instanceDocument = Util.getResourceAsDocument(path);
        instanceDocument.remove(0, instanceDocument.getLength());
        instanceDocument.insertString(0, buffer.toString(), null);
    }
    
    /**
     * Queries and returns a list of completion items.
     * Each unit test is supposed to evaluate this result.
     */
    protected List<CompletionResultItem> query(int caretOffset) throws Exception {
        assert(instanceFileObject  != null && instanceDocument != null);
        CompletionQuery instance = new CompletionQuery(instanceFileObject);
        return instance.getCompletionItems(instanceDocument, caretOffset);
    }
    
    protected void assertResult(List<CompletionResultItem> result,
            String[] expectedResult) {
        assert(result.size() == expectedResult.length);
        for(int i=0; i<expectedResult.length; i++) {
            boolean found = false;
            for(CompletionResultItem item : result) {
                String resultItem = item.getReplacementText();
                if(item instanceof AttributeResultItem)
                    resultItem = resultItem.substring(0, resultItem.indexOf("="));
                if(resultItem.equals(expectedResult[i]))
                    found = true;
            }            
            assert(found);
        }
    }
    
}
