/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.errorstripe;
import java.util.HashMap;
import java.util.Map;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import junit.framework.*;
import org.netbeans.editor.AnnotationDesc;
import org.netbeans.editor.AnnotationType;
import org.netbeans.editor.AnnotationType.Severity;
import org.netbeans.editor.AnnotationTypes;
import org.netbeans.editor.AnnotationTypes.Loader;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;


/**
 *
 * @author Jan Lahoda
 */
public class AnnotationTestUtilities extends TestCase {
    
    /*package private*/ static final String NAME_TEST_ANNOTATION_DESC1 = "test-annotation-1";
    /*package private*/ static final String NAME_TEST_ANNOTATION_DESC2 = "test-annotation-2";
    
    /*package private*/ static final String SD_TEST_ANNOTATION_DESC1 = "Test1";
    /*package private*/ static final String SD_TEST_ANNOTATION_DESC2 = "Test2";
    
    
    /*package private*/ static class TestAnnotationDesc1 extends AnnotationDesc {
        
        private BaseDocument doc;
        private Position position;
        
        public TestAnnotationDesc1(BaseDocument bd, Position position) {
            super(position.getOffset(), -1);
            this.doc      = bd;
            this.position = position;
        }
        
        public String getShortDescription() {
            return SD_TEST_ANNOTATION_DESC1;
        }

        public String getAnnotationType() {
            return NAME_TEST_ANNOTATION_DESC1;
        }

        public int getOffset() {
            return position.getOffset();
        }

        public int getLine() {
            try {
                return Utilities.getLineOffset(doc, getOffset());
            } catch (BadLocationException e) {
                IllegalStateException exc = new IllegalStateException();
                
                exc.initCause(e);
                
                throw exc;
            }
        }
        
    }
    
    /*package private*/ static class TestAnnotationDesc2 extends AnnotationDesc {
        
        private BaseDocument doc;
        private Position position;
        
        public TestAnnotationDesc2(BaseDocument bd, Position position) {
            super(position.getOffset(), -1);
            this.doc      = bd;
            this.position = position;
        }
        
        public String getShortDescription() {
            return SD_TEST_ANNOTATION_DESC2;
        }

        public String getAnnotationType() {
            return NAME_TEST_ANNOTATION_DESC2;
        }

        public int getOffset() {
            return position.getOffset();
        }

        public int getLine() {
            try {
                return Utilities.getLineOffset(doc, getOffset());
            } catch (BadLocationException e) {
                IllegalStateException exc = new IllegalStateException();
                
                exc.initCause(e);
                
                throw exc;
            }
        }
        
    }
    
}
