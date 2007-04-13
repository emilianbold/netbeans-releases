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
package org.netbeans.modules.editor.structure.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.structure.api.DocumentModel.DocumentChange;
import org.netbeans.modules.editor.structure.api.DocumentModel.DocumentModelTransactionCancelledException;
import org.netbeans.modules.editor.structure.spi.DocumentModelProvider;


/** DocumentModel unit tests
 *
 * @author  Marek Fukala
 */
public class DocumentModelTest extends NbTestCase {
    
    DocumentModelProvider dmProvider = null;
    
    public DocumentModelTest() {
        super("document-model-test");
    }
    
    public void setUp() throws BadLocationException {
        dmProvider = new FakeDocumentModelProvider();
    }
    
    //--------- test methods -----------
    public void testModelBasis() throws DocumentModelException, BadLocationException {
        //set the document content
        Document doc = new BaseDocument(DefaultEditorKit.class, false);
        doc.insertString(0,"abcde|fgh|ij|k",null); //4 elements should be created
        
        DocumentModel model = new DocumentModel(doc, dmProvider);
        
        assertNotNull(model.getDocument());
        
        DocumentElement root = model.getRootElement();
        assertNotNull(root);
        
        assertNull(root.getParentElement());
        
        //test if the content of the root elemnt equals the document content
        assertTrue(root.getContent().equals(doc.getText(0, doc.getLength())));
        
        List children = root.getChildren();
        assertEquals(4, children.size());
        
        DocumentElement first = root.getElement(0);
        
        //check name and type
        assertEquals("element0", first.getName());
        assertEquals(FakeDocumentModelProvider.FAKE_ELEMENT_TYPE, first.getType());
        
        //check content and offsets
        assertEquals("abcde", first.getContent());
        assertEquals(0, first.getStartOffset());
        assertEquals(5, first.getEndOffset());
        
        //check has no children
        assertEquals(0, first.getElementCount());
        
    }
    
    public void testAddElementEvent() throws DocumentModelException, BadLocationException, InterruptedException {
        Document doc = new BaseDocument(DefaultEditorKit.class, false);
        DocumentModel model = new DocumentModel(doc, dmProvider);
        
        //listen to model
        final Vector addedElements = new Vector();
        model.addDocumentModelListener(new DocumentModelListenerAdapter() {
            public void documentElementAdded(DocumentElement de) {
                addedElements.add(de);
            }
        });
        
        //listen to element
        final Vector addedElements2 = new Vector();
        model.getRootElement().addDocumentElementListener(new DocumentElementListenerAdapter() {
            public void elementAdded(DocumentElementEvent e) {
                addedElements2.add(e.getChangedChild());
            }
        });
        
        doc.insertString(0,"abcde|fgh|ij|k",null); //4 elements should be created
        Thread.sleep(1000); //wait for the model update (started after 500ms)
        
        assertEquals(4, addedElements.size());
        assertEquals(4, addedElements2.size());
        
        assertEquals(4, model.getRootElement().getElementCount());
        
    }
    
     public void testMoreDocumentElementListeners() throws DocumentModelException, BadLocationException, InterruptedException {
        Document doc = new BaseDocument(DefaultEditorKit.class, false);
        DocumentModel model = new DocumentModel(doc, dmProvider);
        
        DocumentElement root = model.getRootElement();
        
        assertNotNull(root);
        
        DocumentElementListener del1 = new DocumentElementListenerAdapter();
        DocumentElementListener del2 = new DocumentElementListenerAdapter();

        assertNull(root.deListener);
        assertNull(root.deListeners);
        
        root.addDocumentElementListener(del1);
        
        assertTrue(root.deListener == del1);
        assertNull(root.deListeners);
        
        root.addDocumentElementListener(del2);
        
        assertNull(root.deListener);
        assertNotNull(root.deListeners);
        assertEquals(2, root.deListeners.size());
        
        //try to add twice
        root.addDocumentElementListener(del2);
        
        assertNull(root.deListener);
        assertNotNull(root.deListeners);
        assertEquals(2, root.deListeners.size());
        
        root.removeDocumentElementListener(del2);
        
        assertNull(root.deListener);
        assertNotNull(root.deListeners);
        assertEquals(1, root.deListeners.size());
        
        root.removeDocumentElementListener(del1);
        
        assertNull(root.deListener);
        assertNotNull(root.deListeners);
        assertEquals(0, root.deListeners.size());
        
    }
    
    public void testRemoveElementEvent() throws DocumentModelException, BadLocationException, InterruptedException {
        Document doc = new BaseDocument(DefaultEditorKit.class, false);
        doc.insertString(0,"abcde|fgh|ij|k",null); //4 elements should be created
        DocumentModel model = new DocumentModel(doc, dmProvider);
        
        DocumentModelUtils.dumpModelElements(model);
        DocumentModelUtils.dumpElementStructure(model.getRootElement());
        
        //listen to model
        final Vector removedElements = new Vector();
        model.addDocumentModelListener(new DocumentModelListenerAdapter() {
            public void documentElementRemoved(DocumentElement de) {
                removedElements.add(de);
            }
        });
        
        //listen to element
        final Vector removedElements2 = new Vector();
        model.getRootElement().addDocumentElementListener(new DocumentElementListenerAdapter() {
            public void elementRemoved(DocumentElementEvent e) {
                removedElements2.add(e.getChangedChild());
            }
        });
        
        doc.remove(0,doc.getLength());
        Thread.sleep(1000); //wait for the model update (started after 500ms)
        

        assertEquals(4,removedElements2.size());
        assertEquals(4, removedElements.size());
        
        assertEquals(0, model.getRootElement().getElementCount());
    }
    
    /**
     * A Simple testing implementation of DocumentModelProvider interface
     * used by DocumentModel unit tests.
     *
     * Creates elements according to the "|" separators: abcde|fghij|k|l|mnopqrstu|vwxyz
     *
     * @author Marek Fukala
     */
    private static class FakeDocumentModelProvider implements DocumentModelProvider {
        
        public void updateModel(DocumentModel.DocumentModelModificationTransaction dtm,
                DocumentModel model, DocumentChange[] changes)
                throws DocumentModelException, DocumentModelTransactionCancelledException {
            try {
                String text = model.getDocument().getText(0, model.getDocument().getLength());
                int lastElementEnd = 0;
                int elCount = 0;
                ArrayList foundElements = new ArrayList();
                for(int i = 0; i < text.length(); i++) {
                    if(text.charAt(i) == '|' || i == text.length() - 1) {
                        //create element if doesn't exist
                        DocumentElement test = model.getDocumentElement(lastElementEnd, i);
                        if( test == null) {
                            foundElements.add(dtm.addDocumentElement("element"+(elCount++), FAKE_ELEMENT_TYPE, Collections.EMPTY_MAP, lastElementEnd, i));
                        } else {
                            foundElements.add(test);
                        }
                        lastElementEnd = i;
                    }
                }
                
                //delete unexisting elements
                ArrayList deleted = new ArrayList(model.getRootElement().getChildren());
                deleted.removeAll(foundElements);
                Iterator i = deleted.iterator();
                while(i.hasNext()) {
                    DocumentElement de = (DocumentElement)i.next();
                    dtm.removeDocumentElement(de, false);
                }
                
            }catch(BadLocationException e) {
                throw new DocumentModelException("error occured when creating elements",e);
            }
        }
        
        public static final String FAKE_ELEMENT_TYPE = "fake element";
        
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
