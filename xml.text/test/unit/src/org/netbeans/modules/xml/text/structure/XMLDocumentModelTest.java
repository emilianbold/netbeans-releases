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
        
        doc1.remove(30,"<c/>".length());
        Thread.sleep(1000); //wait for the model update (started after 500ms)
        
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
        
        doc1.remove(38,"<b>text</b>".length());
        Thread.sleep(1000); //wait for the model update (started after 500ms)
        
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
    
    public void testRemoveAndAddEntireDocumentContent() throws DocumentModelException, BadLocationException, InterruptedException {
        //initialize documents used in tests
        initDoc1();
        //set the document content
        DocumentModel model = DocumentModel.getDocumentModel(doc1);
        
        DocumentElement root = model.getRootElement();

        System.out.println("AFTER INIT:::");
        DocumentModelUtils.dumpElementStructure(root);
        DocumentModelUtils.dumpModelElements(model);
        
        
        //listen to model
        final Vector removedElements = new Vector();
        model.addDocumentModelListener(new DocumentModelListenerAdapter() {
            public void documentElementRemoved(DocumentElement de) {
                removedElements.add(de);
            }
        });
        
        //listen to element
        final Vector removedElements2 = new Vector();
        root.addDocumentElementListener(new DocumentElementListenerAdapter() {
            public void elementRemoved(DocumentElementEvent e) {
                removedElements2.add(e.getChangedChild());
            }
        });
        
        //remove entire document content
        doc1.remove(0,doc1.getLength());
        Thread.sleep(1000); //wait for the model update (started after 500ms)
        
        System.out.println("AFTER REMOVE:::");
        DocumentModelUtils.dumpElementStructure(root);
        DocumentModelUtils.dumpModelElements(model);
        
        assertEquals(6, removedElements.size()); //all elements removed
        
        //XXX probably should be only one element removed, but because of the
        //elements removal mechanism, when entire document is erased and 
        //where all empty elements (startoffset == endoffset)
        //are considered as children of root element the event is fired 6-times.
        assertEquals(6, removedElements2.size()); //<root> removed
        
        assertEquals(0, root.getElementCount()); //has not children
        
        //insert the document content back
        
        //listen to model
        final Vector addedElements = new Vector();
        model.addDocumentModelListener(new DocumentModelListenerAdapter() {
            public void documentElementRemoved(DocumentElement de) {
                addedElements.add(de);
            }
        });
        
        //listen to element
        final Vector addedElements2 = new Vector();
        root.addDocumentElementListener(new DocumentElementListenerAdapter() {
            public void elementRemoved(DocumentElementEvent e) {
                addedElements2.add(e.getChangedChild());
            }
        });

        doc1.insertString(0,"<?xml version='1.0'?><root><a><c/></a><b>text</b></root>",null); 
        Thread.sleep(1000); //wait for the model update (started after 500ms)
        
        System.out.println("AFTER ADD:::");
        DocumentModelUtils.dumpElementStructure(root);
        DocumentModelUtils.dumpModelElements(model);
        
        //check events
        //#63348 -  [50cat][navigator] Replacing whole document screws up its tree
        //eval: no events are fired to neither model nor root element
        //see that the elements order (from debugElements()) has wrong order of
        //first two elements!!!
        assertEquals(1, addedElements2.size()); //<root> added
        assertEquals(6, addedElements.size()); //all elements added
        
        assertEquals(1, root.getElementCount()); //has <root> child
        
        DocumentElement rootTag = root.getElement(0);
        assertNotNull(rootTag);
        
        assertEquals(2, rootTag.getElementCount()); //has A and B children
        
        //check basic properties of the root tag
        assertEquals("root", rootTag.getName());
        assertEquals(XMLDocumentModelProvider.XML_TAG, rootTag.getType());
        
    }
    
    //inserts a character into <root> tag element (e.g. <roXot>) so the element is not valid
    //the ROOT element should be destroyed and its children (A, B) should be moved to its parent (document root element)
    public void testInvalidateTagElement() throws DocumentModelException, BadLocationException, InterruptedException {
        //initialize documents used in tests
        initDoc1();
        //set the document content
        DocumentModel model = DocumentModel.getDocumentModel(doc1);
        
        DocumentElement root = model.getRootElement();
        DocumentElement rootTag = root.getElement(1); //get <root> element
        DocumentElement aTag = rootTag.getElement(0);
        DocumentElement bTag = rootTag.getElement(1);
        
        //add-listen to model
        final Vector addedElements = new Vector();
        model.addDocumentModelListener(new DocumentModelListenerAdapter() {
            public void documentElementAdded(DocumentElement de) {
                addedElements.add(de);
            }
        });
        
        //add-listen to element
        final Vector addedElements2 = new Vector();
        root.addDocumentElementListener(new DocumentElementListenerAdapter() {
            public void elementAdded(DocumentElementEvent e) {
                addedElements2.add(e.getChangedChild());
            }
        });
        
        //remove-listen to model
        final Vector removedElements = new Vector();
        model.addDocumentModelListener(new DocumentModelListenerAdapter() {
            public void documentElementRemoved(DocumentElement de) {
                removedElements.add(de);
            }
        });
        
        //remove-listen to element
        final Vector removedElements2 = new Vector();
        rootTag.addDocumentElementListener(new DocumentElementListenerAdapter() {
            public void elementRemoved(DocumentElementEvent e) {
                System.out.println("removed " + e.getChangedChild());
                removedElements2.add(e.getChangedChild());
            }
        });
        
        assertEquals(2, root.getElementCount()); //has PI and ROOT child
        
        //DocumentModelUtils.dumpElementStructure(root);
        
        doc1.insertString(24,"X",null);
        Thread.sleep(1000); //wait for the model update (started after 500ms)
        
        //System.out.println(doc1.getText(0, doc1.getLength()));
        //DocumentModelUtils.dumpElementStructure(root);
        
        assertEquals(3, root.getElementCount()); //has PI, A, B children
        
        //check events
        assertEquals(0, addedElements.size());
        assertEquals(2, addedElements2.size());
        
        assertEquals(1, removedElements.size()); 
        assertEquals(2, removedElements2.size());//A,B from ROOT
        
        //test children
        assertEquals("b", bTag.getName());
        assertEquals(XMLDocumentModelProvider.XML_TAG, bTag.getType());
        assertEquals(1, bTag.getElementCount());
        assertEquals(root, bTag.getParentElement());
        
        assertEquals("a", aTag.getName());
        assertEquals(XMLDocumentModelProvider.XML_TAG, aTag.getType());
        assertEquals(1, aTag.getElementCount());
        assertEquals(root, aTag.getParentElement());
        
        //check content and offsets
        assertEquals(28, aTag.getStartOffset());
        assertEquals(38, aTag.getEndOffset());
        
        //check if the ROOT element has been really removed
        try {
            rootTag.getParentElement(); //should throw the IAE
            assertTrue("The removed element still can obtain its parent!?!?!", false);
        } catch(IllegalArgumentException iae) {
            //OK
        }
        
        assertEquals(0, rootTag.getChildren().size()); //has not children
    }
    
    
    
    
    private void initDoc1() throws BadLocationException {
        /*
          supposed structure:
            ROOT
             |
             +--<?xml version='1.0'?>
             +--<root>
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
