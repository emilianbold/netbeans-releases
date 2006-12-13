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

package org.netbeans.modules.editor.hints;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.highlights.spi.Highlight;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jan Lahoda
 */
public class AnnotationHolderTest extends NbTestCase {
    
    private FileObject file;
    private Document doc;
    
    /** Creates a new instance of AnnotationHolderTest */
    public AnnotationHolderTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        
        file = fs.getRoot().createData("test.txt");
        
        writeIntoFile(file, "01234567890123456789");
        
        DataObject od = DataObject.find(file);
        EditorCookie ec = (EditorCookie) od.getCookie(EditorCookie.class);
        
        doc = ec.openDocument();
    }
    
    public void testComputeHighlightsOneLayer1() throws Exception {
        ErrorDescription ed1 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "1", file, 1, 3);
        ErrorDescription ed2 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "2", file, 5, 6);
        
        List<ErrorDescription> errors = Arrays.asList(ed1, ed2);
        List<Highlight> highlights = new ArrayList<Highlight>();
        
        AnnotationHolder.computeHighlights(doc, 0, errors, highlights);
        
        assertEquals(2, highlights.size());
        
        Highlight h1 = highlights.get(0);
        Highlight h2 = highlights.get(1);
        
        assertEquals(1, h1.getStart());
        assertEquals(3, h1.getEnd());
        assertEquals(5, h2.getStart());
        assertEquals(6, h2.getEnd());
    }
    
    public void testComputeHighlightsOneLayer2() throws Exception {
        ErrorDescription ed1 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "1", file, 1, 7);
        ErrorDescription ed2 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "2", file, 5, 6);
        
        List<ErrorDescription> errors = Arrays.asList(ed1, ed2);
        List<Highlight> highlights = new ArrayList<Highlight>();
        
        AnnotationHolder.computeHighlights(doc, 0, errors, highlights);
        
        assertEquals(1, highlights.size());
        
        Highlight h1 = highlights.get(0);
        
        assertEquals(1, h1.getStart());
        assertEquals(7, h1.getEnd());
    }
    
    public void testComputeHighlightsOneLayer3() throws Exception {
        ErrorDescription ed1 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "1", file, 1, 5);
        ErrorDescription ed2 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "2", file, 3, 7);
        
        List<ErrorDescription> errors = Arrays.asList(ed1, ed2);
        List<Highlight> highlights = new ArrayList<Highlight>();
        
        AnnotationHolder.computeHighlights(doc, 0, errors, highlights);
        
        assertEquals(1, highlights.size());
        
        Highlight h1 = highlights.get(0);
        
        assertEquals(1, h1.getStart());
        assertEquals(7, h1.getEnd());
    }
    
    public void testComputeHighlightsOneLayer4() throws Exception {
        ErrorDescription ed1 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "1", file, 3, 5);
        ErrorDescription ed2 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "2", file, 1, 7);
        
        List<ErrorDescription> errors = Arrays.asList(ed1, ed2);
        List<Highlight> highlights = new ArrayList<Highlight>();
        
        AnnotationHolder.computeHighlights(doc, 0, errors, highlights);
        
        assertEquals(1, highlights.size());
        
        Highlight h1 = highlights.get(0);
        
        assertEquals(1, h1.getStart());
        assertEquals(7, h1.getEnd());
    }
    
    public void testComputeHighlightsTwoLayers1() throws Exception {
        ErrorDescription ed1 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "1", file, 1, 3);
        ErrorDescription ed2 = ErrorDescriptionFactory.createErrorDescription(Severity.WARNING, "2", file, 5, 7);
        
        List<ErrorDescription> errors = Arrays.asList(ed1, ed2);
        List<Highlight> highlights = new ArrayList<Highlight>();
        
        AnnotationHolder.computeHighlights(doc, 0, errors, highlights);
        
        assertEquals(2, highlights.size());
        
        Highlight h1 = highlights.get(0);
        Highlight h2 = highlights.get(1);
        
        assertEquals(1, h1.getStart());
        assertEquals(3, h1.getEnd());
        assertEquals(5, h2.getStart());
        assertEquals(7, h2.getEnd());
    }
    
    public void testComputeHighlightsTwoLayers2() throws Exception {
        ErrorDescription ed1 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "1", file, 1, 7);
        ErrorDescription ed2 = ErrorDescriptionFactory.createErrorDescription(Severity.WARNING, "2", file, 3, 5);
        
        List<ErrorDescription> errors = Arrays.asList(ed1, ed2);
        List<Highlight> highlights = new ArrayList<Highlight>();
        
        AnnotationHolder.computeHighlights(doc, 0, errors, highlights);
        
        assertEquals(1, highlights.size());
        
        Highlight h1 = highlights.get(0);
        
        assertEquals(1, h1.getStart());
        assertEquals(7, h1.getEnd());
        
        assertEquals(AnnotationHolder.COLORINGS.get(Severity.ERROR), h1.getColoring());
    }
    
    public void testComputeHighlightsTwoLayers3() throws Exception {
        ErrorDescription ed1 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "1", file, 3, 5);
        ErrorDescription ed2 = ErrorDescriptionFactory.createErrorDescription(Severity.WARNING, "2", file, 4, 7);
        
        List<ErrorDescription> errors = Arrays.asList(ed1, ed2);
        List<Highlight> highlights = new ArrayList<Highlight>();
        
        AnnotationHolder.computeHighlights(doc, 0, errors, highlights);
        
        assertEquals(2, highlights.size());
        
        Highlight h1 = highlights.get(0);
        Highlight h2 = highlights.get(1);
        
        assertEquals(3, h1.getStart());
        assertEquals(5, h1.getEnd());
        assertEquals(6, h2.getStart());
        assertEquals(7, h2.getEnd());
        
        assertEquals(AnnotationHolder.COLORINGS.get(Severity.ERROR), h1.getColoring());
        assertEquals(AnnotationHolder.COLORINGS.get(Severity.WARNING), h2.getColoring());
    }
    
    public void testComputeHighlightsTwoLayers4() throws Exception {
        ErrorDescription ed1 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "1", file, 3, 5);
        ErrorDescription ed2 = ErrorDescriptionFactory.createErrorDescription(Severity.WARNING, "2", file, 1, 4);
        
        List<ErrorDescription> errors = Arrays.asList(ed1, ed2);
        List<Highlight> highlights = new ArrayList<Highlight>();
        
        AnnotationHolder.computeHighlights(doc, 0, errors, highlights);
        
        assertEquals(2, highlights.size());
        
        Highlight h1 = highlights.get(0);
        Highlight h2 = highlights.get(1);
        
        assertEquals(3, h1.getStart());
        assertEquals(5, h1.getEnd());
        assertEquals(1, h2.getStart());
        assertEquals(2, h2.getEnd());
        
        assertEquals(AnnotationHolder.COLORINGS.get(Severity.ERROR), h1.getColoring());
        assertEquals(AnnotationHolder.COLORINGS.get(Severity.WARNING), h2.getColoring());
    }
    
    public void testComputeHighlightsTwoLayers5() throws Exception {
        ErrorDescription ed1 = ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, "1", file, 3, 5);
        ErrorDescription ed2 = ErrorDescriptionFactory.createErrorDescription(Severity.WARNING, "2", file, 1, 7);
        
        List<ErrorDescription> errors = Arrays.asList(ed1, ed2);
        List<Highlight> highlights = new ArrayList<Highlight>();
        
        AnnotationHolder.computeHighlights(doc, 0, errors, highlights);
        
        assertEquals(3, highlights.size());
        
        Highlight h1 = highlights.get(0);
        Highlight h2 = highlights.get(1);
        Highlight h3 = highlights.get(2);
        
        assertEquals(3, h1.getStart());
        assertEquals(5, h1.getEnd());
        assertEquals(1, h3.getStart());
        assertEquals(2, h3.getEnd());
        assertEquals(6, h2.getStart());
        assertEquals(7, h2.getEnd());
        
        assertEquals(AnnotationHolder.COLORINGS.get(Severity.ERROR), h1.getColoring());
        assertEquals(AnnotationHolder.COLORINGS.get(Severity.WARNING), h2.getColoring());
        assertEquals(AnnotationHolder.COLORINGS.get(Severity.WARNING), h3.getColoring());
    }
    
    private void writeIntoFile(FileObject file, String what) throws Exception {
        FileLock lock = file.lock();
        OutputStream out = file.getOutputStream(lock);
        
        try {
            out.write(what.getBytes());
        } finally {
            out.close();
            lock.releaseLock();
        }
    }
    
}
