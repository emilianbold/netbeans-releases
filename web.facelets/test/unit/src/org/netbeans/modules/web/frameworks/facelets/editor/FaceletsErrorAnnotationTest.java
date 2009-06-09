/*
 * FaceletsErrorAnnotationTest.java
 * JUnit based test
 *
 * Created on December 2, 2006, 7:21 PM
 */

package org.netbeans.modules.web.frameworks.facelets.editor;

import java.io.InputStream;
import javax.swing.text.StyledDocument;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.web.frameworks.facelets.loaders.FaceletDataObject;
import org.netbeans.modules.web.frameworks.facelets.loaders.FaceletDataObjectTest;
import org.netbeans.modules.web.frameworks.facelets.loaders.FaceletLocalFileSystem;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.text.Line;
import org.openide.text.NbDocument;

/**
 *
 * @author petr
 */
public class FaceletsErrorAnnotationTest extends FaceletLocalFileSystem {
    
    
    public FaceletsErrorAnnotationTest(String testName) {
        super(testName);
    }

    public void testCreatePart() throws Exception {
        /*String resource = "template02.xhtml";
        FaceletDataObject facelet = findDataObject(resource);
        InputStream is = facelet.getPrimaryFile().getInputStream();
        
        
        EditorCookie editorCookie = (EditorCookie)facelet.getCookie(EditorCookie.class);
        System.out.println("editorCookie: " + editorCookie);
        editorCookie.open();
        StyledDocument document = editorCookie.openDocument();
        System.out.println("document: " + document);
        FaceletsEditorError fee = new FaceletsEditorError(20, 13, "", FaceletsEditorError.PARSIN_ERROR);
        
        
        NbEditorDocument doc = new NbEditorDocument(HTMLKit.class);
        doc.insertString(0, document.getText(0, document.getLength()), null);
        FaceletsErrorAnnotation fea = new FaceletsErrorAnnotation (fee, doc);
        
        LineCookie cookie = (LineCookie)facelet.getCookie(LineCookie.class);
        Line.Set lines = cookie.getLineSet();
        
        Line.Part part = fea.createPart(lines);
        
        System.out.println("part: " + part);
        */
    }
    
    
}
