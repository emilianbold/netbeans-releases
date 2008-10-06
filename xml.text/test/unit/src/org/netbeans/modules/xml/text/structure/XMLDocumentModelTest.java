/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.xml.text.structure;

import java.util.List;
import java.util.Vector;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.structure.api.DocumentElement;
import org.netbeans.modules.editor.structure.api.DocumentElementEvent;
import org.netbeans.modules.editor.structure.api.DocumentElementListener;
import org.netbeans.modules.editor.structure.api.DocumentModel;
import org.netbeans.modules.editor.structure.api.DocumentModelException;
import org.netbeans.modules.editor.structure.api.DocumentModelListener;
import org.netbeans.modules.editor.structure.api.DocumentModelStateListener;
import org.netbeans.modules.xml.text.syntax.XMLKit;
import org.netbeans.modules.xml.text.test.TestBase;


/** XML DocumentModel provider unit tests
 *
 * @author  Marek Fukala
 */
public class XMLDocumentModelTest extends TestBase {

    private static final int MODEL_TIMEOUT = 500; //ms
    BaseDocument doc = null;

    public XMLDocumentModelTest() {
        super("xml-document-model-test");
    }
    
    public XMLDocumentModelTest(String testName) {
        super(testName);
    }
    
    public void setUp() throws BadLocationException {
    }

    protected void runTest() throws Throwable {
        //System.out.println(getName());
        super.runTest();
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new XMLDocumentModelTest("testAddElement"));
        suite.addTest(new XMLDocumentModelTest("testChangeTreeFromElementsWithSameName"));
        suite.addTest(new XMLDocumentModelTest("testCreateAndUpdateCommentElement"));
        suite.addTest(new XMLDocumentModelTest("testDoTwoModificationsOnVariousPlaces"));
        suite.addTest(new XMLDocumentModelTest("testDocumentModelStateListener"));
        //suite.addTest(new XMLDocumentModelTest("testEditElementWithOneCharContent_71596"));
        suite.addTest(new XMLDocumentModelTest("testElementAttributes"));
        suite.addTest(new XMLDocumentModelTest("testInvalidateTagElement"));
        suite.addTest(new XMLDocumentModelTest("testMergeTwoElementsIntoOne"));
        suite.addTest(new XMLDocumentModelTest("testModelBasis"));
        suite.addTest(new XMLDocumentModelTest("testModelLocking"));
        suite.addTest(new XMLDocumentModelTest("testRemoveAndAddEntireDocumentContent"));
        suite.addTest(new XMLDocumentModelTest("testRemoveDocumentContentPartToTheEndOfTheFile"));
        suite.addTest(new XMLDocumentModelTest("testRemoveElementAttributes"));
        suite.addTest(new XMLDocumentModelTest("testRemoveElementsInText"));
        suite.addTest(new XMLDocumentModelTest("testRemoveEmptyTagElement"));
        suite.addTest(new XMLDocumentModelTest("testRemoveNestedElements"));
        suite.addTest(new XMLDocumentModelTest("testRemoveTagElementWithTextContent"));
        suite.addTest(new XMLDocumentModelTest("testRemoveTwoElementsWithSameName"));
        suite.addTest(new XMLDocumentModelTest("testReplaceEntireDocumentContent"));
        return suite;
    }

//--------- test methods -----------
    public void testModelBasis() throws DocumentModelException, BadLocationException {
//initialize documents used in tests
        initDoc1();

//set the document content
        DocumentModel model = DocumentModel.getDocumentModel(doc);

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

        assertEquals("a", a.getName());
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
        DocumentModel model = DocumentModel.getDocumentModel(doc);

        DocumentElement root = model.getRootElement();
        final DocumentElement rootTag = root.getElement(1); //get <root> element
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
        final Object lock = new Object();
        model.addDocumentModelStateListener(new DocumentModelStateListenerAdapter() {
            public void updateFinished() {
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        });

        doc.insertString(27, "<new></new>", null);
        model.forceUpdate();

        synchronized (lock) {
            lock.wait(2000);
        }

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
        DocumentModel model = DocumentModel.getDocumentModel(doc);

        DocumentElement root = model.getRootElement();
        DocumentElement rootTag = root.getElement(1); //get <root> element
        final DocumentElement aTag = rootTag.getElement(0); //get <a> element
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
        final Object lock = new Object();
        model.addDocumentModelStateListener(new DocumentModelStateListenerAdapter() {

            public void updateFinished() {
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        });

        doc.remove(30, "<c/>".length());
        model.forceUpdate();

        synchronized (lock) {
            lock.wait(2000);
        }

        assertEquals(1, removedElements.size());
        assertEquals(1, removedElements2.size());

        assertEquals(0, aTag.getElementCount()); //has B children
    }

    public void testRemoveTagElementWithTextContent() throws DocumentModelException, BadLocationException, InterruptedException {
//initialize documents used in tests
        initDoc1();
//set the document content
        DocumentModel model = DocumentModel.getDocumentModel(doc);

        DocumentElement root = model.getRootElement();
        final DocumentElement rootTag = root.getElement(1); //get <root> element
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
        final Object lock = new Object();
        model.addDocumentModelStateListener(new DocumentModelStateListenerAdapter() {

            public void updateFinished() {
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        });

        doc.remove(38, "<b>text</b>".length());
        model.forceUpdate();

        synchronized (lock) {
            lock.wait(2000);
        }


        assertEquals(2, removedElements.size()); //two events - one for B and one for TEXT
        assertEquals(2, removedElements2.size());

        assertEquals(1, rootTag.getElementCount()); //now has only A child
    }

    public void testRemoveNestedElements() throws DocumentModelException, BadLocationException, InterruptedException {
//initialize documents used in tests
        initDoc1();
//set the document content
        DocumentModel model = DocumentModel.getDocumentModel(doc);

        DocumentElement root = model.getRootElement();
        final DocumentElement rootTag = root.getElement(1); //get <root> element
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
        final Object lock = new Object();
        model.addDocumentModelStateListener(new DocumentModelStateListenerAdapter() {

            public void updateFinished() {
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        });

        doc.remove(27, "<a><c/></a>".length());
        model.forceUpdate();

        synchronized (lock) {
            lock.wait(2000);
        }

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
        final DocumentModel model = DocumentModel.getDocumentModel(doc);
        final DocumentElement root = model.getRootElement();

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
        final Object lock = new Object();
        model.addDocumentModelStateListener(new DocumentModelStateListenerAdapter() {

            public void updateFinished() {
                model.removeDocumentModelStateListener(this);
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        });

//remove entire document content
        doc.remove(0, doc.getLength());
        model.forceUpdate();

//insert the document content back
        synchronized (lock) {
            lock.wait(2000);
        }

        assertEquals(6, removedElements.size()); //all elements removed
//XXX probably should be only one element removed, but because of the
//elements removal mechanism, when entire document is erased and
//where all empty elements (startoffset == endoffset)
//are considered as children of root element the event is fired 6-times.
        assertEquals(6, removedElements2.size()); //<root> removed
        assertEquals(0, root.getElementCount()); //has not children
//listen to model
        final Vector addedElements = new Vector();
        model.addDocumentModelListener(new DocumentModelListenerAdapter() {

            public void documentElementAdded(DocumentElement de) {
                addedElements.add(de);
            }
        });

//listen to element
        final Vector addedElements2 = new Vector();
        root.addDocumentElementListener(new DocumentElementListenerAdapter() {

            public void elementAdded(DocumentElementEvent e) {
                addedElements2.add(e.getChangedChild());
            }
        });

        model.addDocumentModelStateListener(new DocumentModelStateListenerAdapter() {

            public void updateFinished() {
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        });

        doc.insertString(0, "<?xml version='1.0'?><root><a><c/></a><b>text</b></root>", null);
        model.forceUpdate();

        synchronized (lock) {
            lock.wait(2000);
        }

//check events
        assertEquals(6, addedElements.size()); //all elements added
        assertEquals(2, addedElements2.size()); //<root> and PI added
        assertEquals(2, root.getElementCount()); //has <root> and PI child
//check PI tag
        DocumentElement piTag = root.getElement(0);
        assertNotNull(piTag);

//check root tag and its children
        DocumentElement rootTag = root.getElement(1);
        assertNotNull(rootTag);
//check basic properties of the root tag
        assertEquals("root", rootTag.getName());
        assertEquals(XMLDocumentModelProvider.XML_TAG, rootTag.getType());

        assertEquals(2, rootTag.getElementCount()); //has A and B children
        assertEquals(0, piTag.getElementCount()); //has no children
        DocumentElement aTag = rootTag.getElement(0);
        DocumentElement bTag = rootTag.getElement(1);
        assertNotNull(aTag);
        assertEquals("a", aTag.getName());
        assertNotNull(bTag);
        assertEquals("b", bTag.getName());
        assertEquals(1, aTag.getElementCount()); //has C children
        assertEquals(1, bTag.getElementCount()); //has text children
        DocumentElement cTag = aTag.getElement(0);
        assertNotNull(cTag);
        assertEquals("c", cTag.getName());
        assertEquals(0, cTag.getElementCount()); //has no children
        DocumentElement text = bTag.getElement(0);
        assertNotNull(text);
        assertEquals(XMLDocumentModelProvider.XML_CONTENT, text.getType());
        assertEquals(0, text.getElementCount()); //has no children
    }

    public void testReplaceEntireDocumentContent() throws DocumentModelException, BadLocationException, InterruptedException {
//initialize documents used in tests
        initDoc1();
//set the document content
        DocumentModel model = DocumentModel.getDocumentModel(doc);

        final DocumentElement root = model.getRootElement();

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

        final Object lock = new Object();
        model.addDocumentModelStateListener(new DocumentModelStateListenerAdapter() {

            public void updateFinished() {
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        });

//remove entire document content
        doc.remove(0, doc.getLength());
        doc.insertString(0, "xxx", null);
        model.forceUpdate();

        synchronized (lock) {
            lock.wait(2000);
        }

        assertEquals(6, removedElements.size()); //all elements removed
//XXX probably should be only one element removed, but because of the
//elements removal mechanism, when entire document is erased and
//where all empty elements (startoffset == endoffset)
//are considered as children of root element the event is fired 6-times.
        assertEquals(6, removedElements2.size()); //<root> removed
        assertEquals(1, root.getElementCount()); //has one text children
        DocumentElement text = root.getElement(0);
        assertNotNull(text);
        assertEquals(XMLDocumentModelProvider.XML_CONTENT, text.getType());
        assertEquals(0, text.getElementCount()); //has no children
    }

//inserts a character into <root> tag element (e.g. <roXot>) so the element is not valid
//the ROOT element should be destroyed and its children (A, B) should be moved to its parent (document root element)
    public void testInvalidateTagElement() throws DocumentModelException, BadLocationException, InterruptedException {
//initialize documents used in tests
        initDoc1();
//set the document content
        DocumentModel model = DocumentModel.getDocumentModel(doc);

        final DocumentElement root = model.getRootElement();
        final DocumentElement rootTag = root.getElement(1); //get <root> element
        final DocumentElement aTag = rootTag.getElement(0);
        final DocumentElement bTag = rootTag.getElement(1);

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
//                System.out.println("removed " + e.getChangedChild());
                removedElements2.add(e.getChangedChild());
            }
        });

        assertEquals(2, root.getElementCount()); //has PI and ROOT child
        final Object lock = new Object();
        model.addDocumentModelStateListener(new DocumentModelStateListenerAdapter() {

            public void updateFinished() {
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        });

        doc.insertString(24, "X", null);
        model.forceUpdate();

        synchronized (lock) {
            lock.wait(2000);
        }

        assertEquals(3, root.getElementCount()); //has PI, A, B children
//check events
        assertEquals(0, addedElements.size());
        assertEquals(2, addedElements2.size());

        assertEquals(1, removedElements.size());
        assertEquals(2, removedElements2.size()); //A,B from ROOT
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
        } catch (IllegalArgumentException iae) {
            //OK
        }

        assertEquals(0, rootTag.getChildren().size()); //has not children
    }

    public void testCreateAndUpdateCommentElement() throws DocumentModelException, BadLocationException, InterruptedException {
//initialize documents used in tests
        initDoc1();
//set the document content
        final DocumentModel model = DocumentModel.getDocumentModel(doc);

        final DocumentElement root = model.getRootElement();
        final DocumentElement rootTag = root.getElement(1); //get <root> element
        final DocumentElement aTag = rootTag.getElement(0);

//add-listen to model
        final Vector addedElements = new Vector();
        model.addDocumentModelListener(new DocumentModelListenerAdapter() {

            public void documentElementAdded(DocumentElement de) {
                addedElements.add(de);
            }
        });

//add-listen to element
        final Vector addedElementsToATag = new Vector();
        aTag.addDocumentElementListener(new DocumentElementListenerAdapter() {

            public void elementAdded(DocumentElementEvent e) {
                addedElementsToATag.add(e.getChangedChild());
            }
        });

        assertEquals(1, aTag.getElementCount()); //A has only C children
        final Object lock = new Object();

        model.addDocumentModelStateListener(new DocumentModelStateListenerAdapter() {

            public void updateFinished() {

                model.removeDocumentModelStateListener(this);
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        });

        doc.insertString(30, "<!-- xml comment -->", null);
        model.forceUpdate();
//text after:
//                  <?xml version='1.0'?><root><a><!-- xml comment --><c/></a><b>text</b></root>
//                  012345678901234567890123456789012345678901234567890123456789
//                  0         1         2         3         4         5
        synchronized (lock) {
            lock.wait(2000);
        }

//check events
        assertEquals(1, addedElements.size());
        assertEquals(1, addedElementsToATag.size());

//test a tag and its content
        assertEquals("a", aTag.getName());
        assertEquals(XMLDocumentModelProvider.XML_TAG, aTag.getType());
        assertEquals(2, aTag.getElementCount()); //the new commnent and C tag
        assertEquals(rootTag, aTag.getParentElement());

//check content and offsets
        assertEquals(27, aTag.getStartOffset());
        assertEquals(57, aTag.getEndOffset());

//check the comment element
        DocumentElement comment = aTag.getElement(0);
        assertEquals("comment", comment.getName());
        assertEquals(XMLDocumentModelProvider.XML_COMMENT, comment.getType());
        assertEquals(0, comment.getElementCount()); //check has not children
        assertEquals(aTag, comment.getParentElement());
//check boundaries
        assertEquals(30, comment.getStartOffset());
        assertEquals(49, comment.getEndOffset());


//test comment content update
//add-listen to model
        final Vector modifiedEls = new Vector();
        model.addDocumentModelListener(new DocumentModelListenerAdapter() {

            public void documentElementChanged(DocumentElement de) {
                modifiedEls.add(de);
            }
        });
//add-listen to element
        final Vector commentModifications = new Vector();
        comment = aTag.getElement(0);
        assertNotNull(comment);
        comment.addDocumentElementListener(new DocumentElementListenerAdapter() {

            public void contentChanged(DocumentElementEvent de) {
                commentModifications.add(de);
            }
        });

        model.addDocumentModelStateListener(new DocumentModelStateListenerAdapter() {

            public void updateFinished() {

                model.removeDocumentModelStateListener(this);
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        });

        doc.insertString(36, "big ", null);
        model.forceUpdate();

        synchronized (lock) {
            lock.wait(2000);
        }

        assertEquals(1, modifiedEls.size()); //one model change event fired
        assertEquals(1, commentModifications.size()); //one change event fired
    }

    public void testElementAttributes() throws DocumentModelException, BadLocationException, InterruptedException {
//initialize documents used in tests
        initDoc2();
//set the document content
        DocumentModel model = DocumentModel.getDocumentModel(doc);

        DocumentElement root = model.getRootElement();
        final DocumentElement rootTag = root.getElement(1); //get <root> element
        AttributeSet attrs = rootTag.getAttributes();
        assertNotNull(attrs);

        assertEquals(1, attrs.getAttributeCount()); //one attribute
        assertTrue(attrs.containsAttribute("attrname", "value"));
        assertEquals("value", (String) attrs.getAttribute("attrname"));

//listen to model
        final Vector modelAttrsChanges = new Vector();
        model.addDocumentModelListener(new DocumentModelListenerAdapter() {

            public void documentElementAttributesChanged(DocumentElement de) {
                modelAttrsChanges.add(de);
            }
        });

//listen to element
        final Vector elementAttrsChanges = new Vector();
        rootTag.addDocumentElementListener(new DocumentElementListenerAdapter() {

            public void attributesChanged(DocumentElementEvent e) {
                elementAttrsChanges.add(e.getChangedChild());
            }
        });

        final Object lock = new Object();

        model.addDocumentModelStateListener(new DocumentModelStateListenerAdapter() {

            public void updateFinished() {
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        });

        doc.insertString(39, "aaa", null);
        model.forceUpdate();

        synchronized (lock) {
            lock.wait(2000);
        }

        //check events
        assertEquals(1, modelAttrsChanges.size());
        assertEquals(1, elementAttrsChanges.size());

        //check the element
        attrs = rootTag.getAttributes();
        assertNotNull(attrs);

        assertEquals(1, attrs.getAttributeCount()); //one attribute
        assertTrue(attrs.containsAttribute("attrname", "vaaaalue"));
        assertEquals("vaaaalue", (String) attrs.getAttribute("attrname"));
    }

    public void testMergeTwoElementsIntoOne() throws DocumentModelException, BadLocationException, InterruptedException {
        //initialize documents used in tests
        initDoc3();
        //set the document content
        DocumentModel model = DocumentModel.getDocumentModel(doc);
        DocumentElement root = model.getRootElement();

        DocumentElement rootTag = root.getElement(1); //get <wood> element
        final DocumentElement wood1 = rootTag.getElement(0);
        final DocumentElement wood2 = rootTag.getElement(1);

        assertEquals("tree", wood1.getName());
        assertEquals("tree", wood2.getName());

        //listen to model
        final Vector modelChanges = new Vector();
        model.addDocumentModelListener(new DocumentModelListenerAdapter() {

            public void documentElementRemoved(DocumentElement de) {
                modelChanges.add(de);
            }
        });

        //listen to element
        final Vector elementChanges = new Vector();
        rootTag.addDocumentElementListener(new DocumentElementListenerAdapter() {

            public void elementRemoved(DocumentElementEvent e) {
                elementChanges.add(e.getChangedChild());
            }
        });

        final Object lock = new Object();
        model.addDocumentModelStateListener(new DocumentModelStateListenerAdapter() {

            public void updateFinished() {
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        });

        doc.remove(40, "/><tree id=\"2\"".length());
        model.forceUpdate();

        synchronized (lock) {
            lock.wait(2000);
        }

        //check events
        assertEquals(1, modelChanges.size());
        assertEquals(wood2, modelChanges.get(0));
        assertEquals(1, elementChanges.size());
        assertEquals(wood2, elementChanges.get(0));

        //check the element
        AttributeSet attrs = wood1.getAttributes();
        assertNotNull(attrs);

        assertEquals(1, attrs.getAttributeCount()); //one attribute
        assertTrue(attrs.containsAttribute("id", "1"));
    }

    public void testRemoveTwoElementsWithSameName() throws DocumentModelException, BadLocationException, InterruptedException {
        //initialize documents used in tests
        initDoc3();
        //set the document content
        DocumentModel model = DocumentModel.getDocumentModel(doc);
        DocumentElement root = model.getRootElement();

        final DocumentElement rootTag = root.getElement(1); //get <wood> element
        DocumentElement wood1 = rootTag.getElement(0);
        DocumentElement wood2 = rootTag.getElement(1);

        assertEquals("tree", wood1.getName());
        assertEquals("tree", wood2.getName());

        //listen to model
        final Vector modelChanges = new Vector();
        model.addDocumentModelListener(new DocumentModelListenerAdapter() {

            public void documentElementRemoved(DocumentElement de) {
                System.out.println("removed " + de);
                modelChanges.add(de);
            }
        });

        //listen to element
        final Vector elementChanges = new Vector();
        rootTag.addDocumentElementListener(new DocumentElementListenerAdapter() {

            public void elementRemoved(DocumentElementEvent e) {
                System.out.println("removed " + e.getChangedChild());
                elementChanges.add(e.getChangedChild());
            }
        });

        final Object lock = new Object();
        model.addDocumentModelStateListener(new DocumentModelStateListenerAdapter() {

            public void updateFinished() {
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        });


        doc.remove(27, "<tree id=\"1\"/><tree id=\"2\"/>".length());
        model.forceUpdate();

        synchronized (lock) {
            lock.wait(2000);
        }

        //check events
        assertEquals(2, elementChanges.size()); //two elements removed
        assertEquals(2, modelChanges.size()); //two elements removed
        assertEquals(0, rootTag.getElementCount());
    }

    public void testDoTwoModificationsOnVariousPlaces() throws DocumentModelException, BadLocationException, InterruptedException {
        //initialize documents used in tests
        initDoc1();
        //set the document content
        DocumentModel model = DocumentModel.getDocumentModel(doc);
        DocumentElement root = model.getRootElement();

        DocumentElement rootTag = root.getElement(1); //get root
        final DocumentElement aTag = rootTag.getElement(0); //a tag
        final DocumentElement bTag = rootTag.getElement(1); //b tag
        //listen to model
        final Vector modelChanges = new Vector();
        model.addDocumentModelListener(new DocumentModelListenerAdapter() {

            public void documentElementAdded(DocumentElement de) {
                modelChanges.add(de);
            }
        });

        //listen to element
        final Vector aChanges = new Vector();
        aTag.addDocumentElementListener(new DocumentElementListenerAdapter() {

            public void elementAdded(DocumentElementEvent e) {
                aChanges.add(e.getChangedChild());
            }
        });

        final Vector bChanges = new Vector();
        bTag.addDocumentElementListener(new DocumentElementListenerAdapter() {

            public void elementAdded(DocumentElementEvent e) {
                bChanges.add(e.getChangedChild());
            }
        });

        final Object lock = new Object();
        model.addDocumentModelStateListener(new DocumentModelStateListenerAdapter() {

            public void updateFinished() {
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        });

        doc.insertString(30, "<tag1></tag1>", null);
        doc.insertString(41 + "<tag1></tag1>".length(), "<tag2></tag2>", null);

        model.forceUpdate();

        synchronized (lock) {
            lock.wait(2000);
        }

        //check events
        assertEquals(2, modelChanges.size());

        assertEquals(1, aChanges.size());
        assertEquals(1, bChanges.size());

        DocumentElement tag1 = aTag.getElement(0);
        assertNotNull(tag1);
        assertEquals("tag1", tag1.getName());

        DocumentElement tag2 = bTag.getElement(0);
        assertNotNull(tag2);
        assertEquals("tag2", tag2.getName());
    }

    public void testRemoveDocumentContentPartToTheEndOfTheFile() throws DocumentModelException, BadLocationException, InterruptedException {
        //initialize documents used in tests
        initDoc1();
        //set the document content
        DocumentModel model = DocumentModel.getDocumentModel(doc);
        final DocumentElement root = model.getRootElement();

        //listen to model
        final Vector modelChanges = new Vector();
        model.addDocumentModelListener(new DocumentModelListenerAdapter() {

            public void documentElementRemoved(DocumentElement de) {
                System.out.println("removed " + de);
                modelChanges.add(de);
            }
        });

        //listen to element
        final Vector elementChanges = new Vector();
        root.addDocumentElementListener(new DocumentElementListenerAdapter() {

            public void elementRemoved(DocumentElementEvent e) {
                System.out.println("removed " + e.getChangedChild());
                elementChanges.add(e.getChangedChild());
            }
        });

        final Object lock = new Object();
        model.addDocumentModelStateListener(new DocumentModelStateListenerAdapter() {

            public void updateFinished() {
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        });

        doc.remove(21, doc.getLength() - 21);
        model.forceUpdate();

        synchronized (lock) {
            lock.wait(2000);
        }

        //check events
        //the only <root> element should be removed from the main ROOT document element
        //but due to a design flaw in the document model the original elements strucure cannot be
        //determined after a removal of a document part so all removed elements are considered
        //as children of the most top element which contains their start and end offsets.
        //this problem is filtered out in the treenodes so the navigator works correctly.
        assertEquals(5, elementChanges.size()); //<root> should be removed
        assertEquals(5, modelChanges.size()); //ROOT, A,B,C, text should be removed
        assertEquals(1, root.getElementCount()); //has only <?xml...?> element
    }

    public void testRemoveElementAttributes() throws DocumentModelException, BadLocationException, InterruptedException {
        //initialize documents used in tests
        initDoc2();
        //set the document content
        DocumentModel model = DocumentModel.getDocumentModel(doc);

        DocumentElement root = model.getRootElement();
        final DocumentElement rootTag = root.getElement(1); //get <root> element
        AttributeSet attrs = rootTag.getAttributes();
        assertNotNull(attrs);

        assertEquals(1, attrs.getAttributeCount()); //one attribute
        assertTrue(attrs.containsAttribute("attrname", "value"));
        assertEquals("value", (String) attrs.getAttribute("attrname"));

        //listen to model
        final Vector modelAttrsChanges = new Vector();
        model.addDocumentModelListener(new DocumentModelListenerAdapter() {

            public void documentElementAttributesChanged(DocumentElement de) {
                modelAttrsChanges.add(de);
            }
        });

        //listen to element
        final Vector elementAttrsChanges = new Vector();
        rootTag.addDocumentElementListener(new DocumentElementListenerAdapter() {

            public void attributesChanged(DocumentElementEvent e) {
                elementAttrsChanges.add(e.getChangedChild());
            }
        });

        final Object lock = new Object();
        model.addDocumentModelStateListener(new DocumentModelStateListenerAdapter() {

            public void updateFinished() {
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        });


        doc.remove(26, " attrname=\"value\"".length());
        model.forceUpdate();

        synchronized (lock) {
            lock.wait(2000);
        }

        //check events
        assertEquals(1, modelAttrsChanges.size());
        assertEquals(1, elementAttrsChanges.size());

        //check the element
        attrs = rootTag.getAttributes();
        assertNotNull(attrs);
        assertEquals(0, attrs.getAttributeCount()); //one attribute
    }

    public void testChangeTreeFromElementsWithSameName() throws DocumentModelException, BadLocationException, InterruptedException {
        //initialize documents used in tests
        initDoc4();
        //set the document content
        final DocumentModel model = DocumentModel.getDocumentModel(doc);
        final DocumentElement root = model.getRootElement();

        assertEquals(2, root.getElementCount()); //has <?xml ...?> and <tree id="1"> elements
        final DocumentElement tree1 = root.getElement(1); //get <tree id="1"> element
        assertEquals(1, tree1.getElementCount()); //has <tree id="2"> element
        final DocumentElement tree2 = tree1.getElement(0); //<tree id="2"> element
        assertEquals(2, tree2.getElementCount()); //has <tree id="3"> and <tree id="4"> elements
        final DocumentElement tree3 = tree2.getElement(0); //<tree id="3"> element
        final DocumentElement tree4 = tree2.getElement(1); //<tree id="4"> element
        assertEquals("tree", tree3.getName());
        assertEquals("tree", tree4.getName());

        //listen to model
        final Vector modelChanges = new Vector();
        model.addDocumentModelListener(new DocumentModelListenerAdapter() {

            public void documentElementRemoved(DocumentElement de) {
                System.out.println("removed " + de);
                modelChanges.add(de);
            }
        });

        //listen to element
        final Vector tree1Removes = new Vector();
        final Vector tree1Adds = new Vector();
        tree1.addDocumentElementListener(new DocumentElementListenerAdapter() {

            public void elementRemoved(DocumentElementEvent e) {
                System.out.println("removed " + e.getChangedChild());
                tree1Removes.add(e.getChangedChild());
            }

            public void elementAdded(DocumentElementEvent e) {
                System.out.println("added " + e.getChangedChild());
                tree1Adds.add(e.getChangedChild());
            }
        });

        final Object lock = new Object();

        model.addDocumentModelStateListener(new DocumentModelStateListenerAdapter() {

            public void updateFinished() {
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        });

        doc.insertString(36, "BIG", null);
        model.forceUpdate();

        synchronized (lock) {
            lock.wait(2000);
        }

        //check events
        // tree2 removed from model
        assertEquals(1, modelChanges.size());
        assertEquals(tree2, modelChanges.get(0));

        //tree2 removed from tree1
        assertEquals(1, tree1Removes.size());
        assertEquals(tree2, tree1Removes.get(0));

        //tree3 and tree4 added to tree1
        assertEquals(2, tree1Adds.size());
        assertEquals(tree1Adds.get(0), tree3);
        assertEquals(tree1Adds.get(1), tree4);
        assertEquals(tree1.getElement(0), tree3);
        assertEquals(tree1.getElement(1), tree4);

        //OK lets return back to the original state
        //listen to model
        final Vector modelChanges2 = new Vector();
        model.addDocumentModelListener(new DocumentModelListenerAdapter() {

            public void documentElementAdded(DocumentElement de) {
                System.out.println("added " + de);
                modelChanges2.add(de);
            }
        });

        tree1Adds.clear();
        tree1Removes.clear();

        doc.remove(36, "BIG".length());
        model.forceUpdate();

        synchronized (lock) {
            lock.wait(2000);
        }

        //check events
        // tree2 added to the model
        assertEquals(1, modelChanges.size());
        //tree2 added to tree1
        assertEquals(1, tree1Adds.size());
        //tree3 and 4 removed from tree1
        assertEquals(2, tree1Removes.size());

        //so now tree1 has only one children - tree2
        assertEquals(1, tree1.getElementCount());

        //and tree2 has tree3 and tree4
        assertEquals(2, tree2.getElementCount());

        assertEquals(tree3, tree2.getElement(0));
        assertEquals(tree4, tree2.getElement(1));

        assertEquals(2, root.getElementCount()); //has <?xml ...?> and <tree id="1"> elements
    }

    public void testRemoveElementsInText() throws DocumentModelException, BadLocationException, InterruptedException {
        //initialize documents used in tests
        initDoc5();
        //set the document content
        DocumentModel model = DocumentModel.getDocumentModel(doc);

        DocumentElement root = model.getRootElement();
        final DocumentElement rootTag = root.getElement(2); //get <root> element (first is PI, second whitespaces)
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

        final Object lock = new Object();

        model.addDocumentModelStateListener(new DocumentModelStateListenerAdapter() {

            public void updateFinished() {

                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        });

        doc.remove(31, "  <a>   <c/>   </a> ".length());
        model.forceUpdate();

        synchronized (lock) {
            lock.wait(2000);
        }

        assertEquals(6, removedElements.size()); //remove A, C + WHs
        assertEquals(6, removedElements2.size());

        assertEquals(3, rootTag.getElementCount()); //has B children and 2 WS children
    }

    public void testEditElementWithOneCharContent_71596() throws DocumentModelException, BadLocationException, InterruptedException {
        //initialize documents used in tests
        initDoc6();
        //set the document content
        DocumentModel model = DocumentModel.getDocumentModel(doc);
        DocumentElement root = model.getRootElement();

        final DocumentElement rootTag = root.getElement(0); //get <root> element
        assertNotNull(rootTag.getName());

        //listen to model
        final Vector modelChanges = new Vector();
        model.addDocumentModelListener(new DocumentModelListenerAdapter() {

            public void documentElementAdded(DocumentElement de) {
                modelChanges.add(de);
            }
        });

        //listen to element
        final Vector elementChanges = new Vector();
        rootTag.addDocumentElementListener(new DocumentElementListenerAdapter() {

            public void elementAdded(DocumentElementEvent e) {
                elementChanges.add(e.getChangedChild());
            }
        });

        final Object lock = new Object();
        model.addDocumentModelStateListener(new DocumentModelStateListenerAdapter() {

            public void updateFinished() {
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        });

        doc.insertString(5, " ", null);
        model.forceUpdate();

        synchronized (lock) {
            lock.wait(2000);
        }

        assertEquals(1, rootTag.getElementCount());

        //check events
        assertEquals(0, modelChanges.size());
        assertEquals(0, elementChanges.size());
    }

    public void testModelLocking() throws DocumentModelException, BadLocationException, InterruptedException {
        //initialize documents used in tests
        initDoc1();

        final Semaphore semaphore = new Semaphore();
        Runnable r = new Runnable() {

            public void run() {
                //set the document content
                try {
                    DocumentModel model = DocumentModel.getDocumentModel(doc);
                    //get the document element
                    DocumentElement root = model.getRootElement();
                    //make the document dirty
                    doc.insertString(0, " ", null);
                    //force the model to resort the children
                    root.getChildren();

                    semaphore.state = true;
                    synchronized (semaphore) {
                        semaphore.notifyAll();
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                    t.printStackTrace(getLog());
                }
            }
        };

        Thread t = new Thread(r);
        t.start();

        try {
            synchronized (semaphore) {
                semaphore.wait(5000);
            }
        } catch (InterruptedException ioe) {
            ;
        }

        assertTrue("The DocumentElement.getChildren() locked up! (model.writeLock() called under model.readLock())", semaphore.state);
    }

    public void testDocumentModelStateListener() throws DocumentModelException, BadLocationException, InterruptedException {
        //initialize documents used in tests
        initDoc6();
        //set the document content
        DocumentModel model = DocumentModel.getDocumentModel(doc);

        model.addDocumentModelStateListener(new DocumentModelStateListener() {

            private int state = 0;

            public void sourceChanged() {
                assertEquals(state, 0);
                state = 1;
            }

            public void scanningStarted() {
                assertEquals(state, 1);
                state = 2;
            }

            public void updateStarted() {
                assertEquals(state, 2);
                state = 3;
            }

            public void updateFinished() {
                assertEquals(state, 3);
            }
        });

        doc.insertString(0, " ", null);
        model.forceUpdate();
    }

    //Jirka Kovalsky's test - not completed yet hence commented out
    /*public void testEncapsulateValue_73131() throws DocumentModelException, BadLocationException, InterruptedException {
    BaseDocument doc = new BaseDocument(XMLKit.class, false);
    doc.putProperty("mimeType", "text/xml");
    doc.insertString(0, "<root>X</root>", null);
    DocumentModel model = DocumentModel.getDocumentModel(doc);
    assertNotNull(model);
    assertNotNull(model.getDocument());
    DocumentElement root = model.getRootElement();
    System.out.println(doc.getText(0, doc.getLength()));
    Thread.sleep(MODEL_TIMEOUT * 2); //wait for the model update (started after 500ms)
    DocumentModelUtils.dumpElementStructure(root);
    doc.insertString(5, " id=1\"", null);
    Thread.sleep(MODEL_TIMEOUT * 2); //wait for the model update (started after 500ms)
    System.out.println(doc.getText(0, doc.getLength()));
    DocumentModelUtils.dumpElementStructure(root);
    // <root id=1">X</root>
    // 01234567890123456789
    doc.insertString(9, "\"", null);
    Thread.sleep(MODEL_TIMEOUT * 2); //wait for the model update (started after 500ms)
    System.out.println(doc.getText(0, doc.getLength()));
    DocumentModelUtils.dumpElementStructure(root);
    DocumentElement rootTag = root.getElement(0); //get <root> element
    assertEquals(1, rootTag.getElementCount()); // check that root has one element (X)
    }*/
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
        doc = new BaseDocument(true, "text/xml"); //NOI18N
        doc.putProperty("mimeType", "text/xml");

        doc.insertString(0, "<?xml version='1.0'?><root><a><c/></a><b>text</b></root>", null);
        //                  012345678901234567890123456789012345678901234567890123456789
        //                  0         1         2         3         4         5
    }

    private void initDoc2() throws BadLocationException {
        /*
        supposed structure:
        ROOT
        |
        +--<?xml version='1.0'?>
        +--<root attrname="value">
        |
        +---text
         */
        doc = new BaseDocument(true, "text/xml"); //NOI18N
        doc.putProperty("mimeType", "text/xml");

        doc.insertString(0, "<?xml version='1.0'?><root attrname=\"value\">text</root>", null);
        //                  012345678901234567890123456789012345678901234567890123456789
        //                  0         1         2         3         4         5
    }

    private void initDoc3() throws BadLocationException {
        /*
        supposed structure:
        ROOT
        |
        +--<?xml version='1.0'?>
        +--<wood>
        |
        +---<tree id="1">
        +---<tree id="2">
         */
        doc = new BaseDocument(true, "text/xml"); //NOI18N
        doc.putProperty("mimeType", "text/xml");

        doc.insertString(0, "<?xml version=\"1.0\"?><wood><tree id=\"1\"/><tree id=\"2\"/></wood>", null);
        //                  01234567890123 4567 89012345678901234567 89 01234567890123456789012345678901234567890123456789012345678901234567890123456789
        //                  0         1         2         3         4         5         6         7         8
    }

    private void initDoc4() throws BadLocationException {
        /*
        supposed structure:
        ROOT
        |
        +--<?xml version='1.0'?>
        +--<tree id="1">
        |
        +---<tree id="2">
        |
        +-- <tree id="3">                           |
        +-- <tree id="4">
         */
        doc = new BaseDocument(true, "text/xml"); //NOI18N
        doc.putProperty("mimeType", "text/xml");

        doc.insertString(0, "<?xml version=\"1.0\"?><tree id=\"1\"><tree id=\"2\"><tree id=\"3\"></tree><tree id=\"4\"></tree></tree></tree>", null);
        //                  01234567890123 4567 89012345678901234567 89 01234567890123456789012345678901234567890123456789012345678901234567890123456789
        //                  0         1         2         3         4         5         6         7         8
    }

    private void initDoc5() throws BadLocationException {
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
        doc = new BaseDocument(true, "text/xml"); //NOI18N
        doc.putProperty("mimeType", "text/xml");

        doc.insertString(0, "<?xml version='1.0'?>   <root>   <a>   <c/>   </a>   <b>text</b>   </root>", null);
        //                  012345678901234567890123456789012345678901234567890123456789
        //                  0         1         2         3         4         5
    }

    private void initDoc6() throws BadLocationException {
        /*
        supposed structure:
        ROOT
        |
        +--<root>X</root>
         */
        doc = new BaseDocument(true, "text/xml"); //NOI18N
        doc.putProperty("mimeType", "text/xml");

        doc.insertString(0, "<root>X</root>", null);
        //                  0123456789012345
        //                  0         1
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

    private static class DocumentModelStateListenerAdapter implements DocumentModelStateListener {

        public void sourceChanged() {
        }

        public void scanningStarted() {
        }

        public void updateStarted() {
        }

        public void updateFinished() {
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

    private static class Semaphore {

        public boolean state = false;
    }
}