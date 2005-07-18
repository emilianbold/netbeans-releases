/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
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
