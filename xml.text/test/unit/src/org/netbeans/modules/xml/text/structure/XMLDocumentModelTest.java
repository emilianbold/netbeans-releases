/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.text.structure;

import java.util.List;
import java.util.Vector;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.structure.api.DocumentElement;
import org.netbeans.modules.editor.structure.api.DocumentElementEvent;
import org.netbeans.modules.editor.structure.api.DocumentElementListener;
import org.netbeans.modules.editor.structure.api.DocumentModel;
import org.netbeans.modules.editor.structure.api.DocumentModelException;
import org.netbeans.modules.editor.structure.api.DocumentModelListener;
import org.netbeans.modules.editor.structure.api.DocumentModelUtils;
import org.netbeans.modules.editor.structure.spi.DocumentModelProvider;
import org.netbeans.modules.xml.text.syntax.XMLKit;


/** XML DocumentModel provider unit tests
 *
 * @author  Marek Fukala
 */
public class XMLDocumentModelTest extends NbTestCase {
    
    BaseDocument doc1 = null;
    
    public XMLDocumentModelTest() {
        super("xml-document-model-test");
    }
    
    public void setUp() throws BadLocationException {
    }
    
    //--------- test methods -----------
    public void testModelBasis() throws DocumentModelException, BadLocationException {
        //initialize documents used in tests
        initDoc1();

        //set the document content
        DocumentModel model = DocumentModel.getDocumentModel(doc1);
        
        assertNotNull(model);       
        
        assertNotNull(model.getDocument());
        
        DocumentElement root = model.getRootElement();
        assertNotNull(root);
        
        assertNull(root.getParentElement());
        
        List children = root.getChildren();
        assertEquals(2, children.size()); 
        
        DocumentElement rootel = root.getElement(1); //<root> element
        
        //check parent
        assertEquals(root, rootel.getParentElement());
        
        //check name and type
        assertEquals("root", rootel.getName());
        assertEquals(XMLDocumentModelProvider.XML_TAG, rootel.getType());
        
        //check content and offsets
        assertEquals(21, rootel.getStartOffset());
        assertEquals(55, rootel.getEndOffset());
        
        //check children count
        assertEquals(2, rootel.getElementCount());
        
        //test children (A)
        DocumentElement a = rootel.getElement(0);
        //check parent
        assertEquals(rootel, a.getParentElement());
        
        assertEquals( "a", a.getName());
        assertEquals(XMLDocumentModelProvider.XML_TAG, a.getType());
        //check content and offsets
        assertEquals(27, a.getStartOffset());
        assertEquals(37, a.getEndOffset());
        
        //test children (B)
        DocumentElement b = rootel.getElement(1);
        //check parent
        assertEquals(rootel, b.getParentElement());
        
        assertEquals("b", b.getName());
        assertEquals(XMLDocumentModelProvider.XML_TAG, b.getType());
        //check content and offsets
        assertEquals(38, b.getStartOffset());
        assertEquals(48, b.getEndOffset());
        
        //test children of B (T)
        DocumentElement t = b.getElement(0);
        //check parent
        assertEquals(b, t.getParentElement());
        
        assertEquals("...", t.getName());
        assertEquals(XMLDocumentModelProvider.XML_CONTENT, t.getType());
        //check content and offsets
        assertEquals(41, t.getStartOffset());
        assertEquals(44, t.getEndOffset());
        
    }
     
    public void testAddElement() throws DocumentModelException, BadLocationException, InterruptedException {
        //initialize documents used in tests
        initDoc1();
        //set the document content
        DocumentModel model = DocumentModel.getDocumentModel(doc1);
        
        DocumentElement root = model.getRootElement();
        DocumentElement rootTag = root.getElement(1); //get <root> element
        
        //listen to model
        final Vector addedElements = new Vector();
        model.addDocumentModelListener(new DocumentModelListenerAdapter() {
            public void documentElementAdded(DocumentElement de) {
                addedElements.add(de);
            }
        });
        
        //listen to element
        final Vector addedElements2 = new Vector();
        rootTag.addDocumentElementListener(new DocumentElementListenerAdapter() {
            public void elementAdded(DocumentElementEvent e) {
                addedElements2.add(e.getChangedChild());
            }
        });
        
        assertEquals(2, rootTag.getElementCount()); //has A, B children
        
        //DocumentModelUtils.dumpElementStructure(root);
        
        doc1.insertString(27,"<new></new>",null);
        Thread.sleep(1000); //wait for the model update (started after 500ms)
        
        //System.out.println(doc1.getText(0, doc1.getLength()));
        //DocumentModelUtils.dumpElementStructure(root);
        
        assertEquals(3, rootTag.getElementCount()); //has NEW, A, B children
        
        //check events
        assertEquals(1, addedElements.size());
        assertEquals(1, addedElements2.size());
        
        DocumentElement newElement = rootTag.getElement(0);
        //test children (B)
        assertEquals("new", newElement.getName());
        assertEquals(XMLDocumentModelProvider.XML_TAG, newElement.getType());
        //check content and offsets
        assertEquals(27, newElement.getStartOffset());
        assertEquals(37, newElement.getEndOffset());
        
        //test new element has no children
        assertEquals(0, newElement.getChildren().size());
        
        //test new element parent
        DocumentElement newElementParent = newElement.getParentElement();
        assertEquals(rootTag, newElementParent);
        
    }
    
    public void testRemoveEmptyTagElement() throws DocumentModelException, BadLocationException, InterruptedException {
        //initialize documents used in tests
        initDoc1();
        //set the document content
        DocumentModel model = DocumentModel.getDocumentModel(doc1);
        
        DocumentElement root = model.getRootElement();
        DocumentElement rootTag = root.getElement(1); //get <root> element
        DocumentElement aTag = rootTag.getElement(0); //get <a> element
        
        //listen to model
        final Vector removedElements = new Vector();
        model.addDocumentModelListener(new DocumentModelListenerAdapter() {
            public void documentElementRemoved(DocumentElement de) {
                removedElements.add(de);
            }
        });
        
        //listen to element
        final Vector removedElements2 = new Vector();
        aTag.addDocumentElementListener(new DocumentElementListenerAdapter() {
            public void elementRemoved(DocumentElementEvent e) {
                removedElements2.add(e.getChangedChild());
            }
        });
        
        assertEquals(1, aTag.getElementCount()); //has only C children
        
        DocumentModelUtils.dumpElementStructure(root);
        
        doc1.remove(30,"<c/>".length());
        Thread.sleep(1000); //wait for the model update (started after 500ms)
        
        System.out.println(doc1.getText(0, doc1.getLength()));
        DocumentModelUtils.dumpElementStructure(root);
        
        assertEquals(1, removedElements.size());
        assertEquals(1, removedElements2.size());
        
        assertEquals(0, aTag.getElementCount()); //has B children
        
    }
    
    public void testRemoveTagElementWithTextContent() throws DocumentModelException, BadLocationException, InterruptedException {
        //initialize documents used in tests
        initDoc1();
        //set the document content
        DocumentModel model = DocumentModel.getDocumentModel(doc1);
        
        DocumentElement root = model.getRootElement();
        DocumentElement rootTag = root.getElement(1); //get <root> element
        
        //listen to model
        final Vector removedElements = new Vector();
        model.addDocumentModelListener(new DocumentModelListenerAdapter() {
            public void documentElementRemoved(DocumentElement de) {
                removedElements.add(de);
            }
        });
        
        //listen to element
        final Vector removedElements2 = new Vector();
        rootTag.addDocumentElementListener(new DocumentElementListenerAdapter() {
            public void elementRemoved(DocumentElementEvent e) {
                removedElements2.add(e.getChangedChild());
            }
        });
        
        assertEquals(2, rootTag.getElementCount()); //has A and B children
        
        DocumentModelUtils.dumpElementStructure(root);
        
        doc1.remove(38,"<b>text</b>".length());
        Thread.sleep(1000); //wait for the model update (started after 500ms)
        
        System.out.println(doc1.getText(0, doc1.getLength()));
        DocumentModelUtils.dumpElementStructure(root);
        
        assertEquals(2, removedElements.size()); //two events - one for B and one for TEXT
        assertEquals(2, removedElements2.size());
        
        assertEquals(1, rootTag.getElementCount()); //now has only A child
        
    }
    
    public void testRemoveNestedElements() throws DocumentModelException, BadLocationException, InterruptedException {
        //initialize documents used in tests
        initDoc1();
        //set the document content
        DocumentModel model = DocumentModel.getDocumentModel(doc1);
        
        DocumentElement root = model.getRootElement();
        DocumentElement rootTag = root.getElement(1); //get <root> element

        //listen to model
        final Vector removedElements = new Vector();
        model.addDocumentModelListener(new DocumentModelListenerAdapter() {
            public void documentElementRemoved(DocumentElement de) {
                removedElements.add(de);
            }
        });
        
        //listen to element
        final Vector removedElements2 = new Vector();
        rootTag.addDocumentElementListener(new DocumentElementListenerAdapter() {
            public void elementRemoved(DocumentElementEvent e) {
                removedElements2.add(e.getChangedChild());
            }
        });
        
        assertEquals(2, rootTag.getElementCount()); //has A, B children
        
        DocumentModelUtils.dumpElementStructure(root);
        
        doc1.remove(27,"<a><c/></a>".length());
        Thread.sleep(1000); //wait for the model update (started after 500ms)
        
        System.out.println(doc1.getText(0, doc1.getLength()));
        DocumentModelUtils.dumpElementStructure(root);
        
        //#63357 - [navigator] Inconsistece when deleted tag connects other two
        //evaluation: empty elements of <c> and <a> stays in the structure and events are not fired!
        assertEquals(2, removedElements.size());
        assertEquals(2, removedElements2.size());
        
        assertEquals(1, rootTag.getElementCount()); //has B children
        
    }
    
   
    
    private void initDoc1() throws BadLocationException {
        /*
          supposed structure:
            <root>
               |
               +--<a>
               |   |
               |   +---<c>
               |
               +--<b>
                   |
                   +----text
         */
        doc1 = new BaseDocument(XMLKit.class, false);
        doc1.putProperty("mimeType", "text/xml");
        
        doc1.insertString(0,"<?xml version='1.0'?><root><a><c/></a><b>text</b></root>",null); 
        //                  012345678901234567890123456789012345678901234567890123456789
        //                  0         1         2         3         4         5
    }
    
    private static class DocumentModelListenerAdapter implements DocumentModelListener {
        public void documentElementAdded(DocumentElement de) {
        }
        public void documentElementAttributesChanged(DocumentElement de) {
        }
        public void documentElementChanged(DocumentElement de) {
        }
        public void documentElementRemoved(DocumentElement de) {
        }
    }
    
    private static class DocumentElementListenerAdapter implements DocumentElementListener {
        public void attributesChanged(DocumentElementEvent e) {
        }
        public void childrenReordered(DocumentElementEvent e) {
        }
        public void contentChanged(DocumentElementEvent e) {
        }
        public void elementAdded(DocumentElementEvent e) {
        }
        public void elementRemoved(DocumentElementEvent e) {
        }
    }
}
