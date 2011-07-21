/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.lib2.search;

import javax.swing.text.Document;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JTextArea;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Milutin Kristofic
 */
public class DocumentFinderTest {

    public DocumentFinderTest() {
    }

    private Map<String, Object> getDefaultProps() {
        final Map<String, Object> props = new HashMap<String, Object>();
        props.put(EditorFindSupport.FIND_WHAT, "test");
        props.put(EditorFindSupport.FIND_BACKWARD_SEARCH, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_WHOLE_WORDS, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_HIGHLIGHT_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_INC_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_WRAP_SEARCH, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_MATCH_CASE, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_SMART_CASE, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_REG_EXP, Boolean.FALSE);
        props.put(EditorFindSupport.FIND_HISTORY, new Integer(30));
        return props;
    }
    
    private Document getDocument(String str) {
        JTextArea ta = new JTextArea(str);
        ta.setCaretPosition(1);
        Document doc = ta.getDocument();
        return doc;
    }
    /**
     * Test of backward whole words search
     * Bug #177126
     */
    @Test
    public void testBackWardSearchWholeWordsEndOfWords() throws Exception {
        final Map<String, Object> props = getDefaultProps();
        props.put(EditorFindSupport.FIND_WHAT, "PAxx1");
        props.put(EditorFindSupport.FIND_BACKWARD_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_WHOLE_WORDS, Boolean.TRUE);
        final Document doc = getDocument(" PAxx11 ");
        
        int[] finds = DocumentFinder.find(doc, 0, 25, props, false);
        
        final int[] expectedFinds = {-1, 0};
        assertArrayEquals(expectedFinds, finds);
    }

    /**
     * Test of backward whole words search - a word is in the beginning of document
     * reopened Bug #177126
     */
    @Test
    public void testBackWardSearchWholeWordsBeginDocument() throws Exception {
        final Map<String, Object> props = getDefaultProps();
        props.put(EditorFindSupport.FIND_WHAT, "PAxx1");
        props.put(EditorFindSupport.FIND_BACKWARD_SEARCH, Boolean.TRUE);
        props.put(EditorFindSupport.FIND_WHOLE_WORDS, Boolean.TRUE);
        final Document doc = getDocument("PAxx1 ");
        
        int[] finds = DocumentFinder.find(doc, 0, 6, props, false);
        
        final int[] expectedFinds = {0, 5};
        assertArrayEquals(expectedFinds, finds);
    }   
}
