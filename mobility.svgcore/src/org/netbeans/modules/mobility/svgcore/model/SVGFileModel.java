/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt. 
  * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */   
package org.netbeans.modules.mobility.svgcore.model;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.GZIPInputStream;
import javax.microedition.m2g.SVGImage;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseDocumentEvent;
import org.netbeans.modules.editor.structure.api.DocumentElement;
import org.netbeans.modules.editor.structure.api.DocumentModel;
import org.netbeans.modules.editor.structure.api.DocumentModelException;
import org.netbeans.modules.editor.structure.api.DocumentModelListener;
import org.netbeans.modules.editor.structure.api.DocumentModelStateListener;
import org.netbeans.modules.mobility.svgcore.SVGDataLoader;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.modules.mobility.svgcore.model.ElementMapping;
import org.netbeans.modules.xml.multiview.XmlMultiViewEditorSupport;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;

/**
 *
 * @author Pavel Benes
 */
public class SVGFileModel {
    protected static final String    XML_TAG        = "tag";
    protected static final String    XML_EMPTY_TAG  = "empty_tag";
    protected static final String    XML_ERROR_TAG  = "error";
    protected static final String [] ANIMATION_TAGS = { "animate", "animateTransform", "animateMotion", "animateColor"};
        
    public interface ModelListener {
        public void modelChanged();
        public void modelSwitched();
    }

    public abstract class FileModelTransaction implements Runnable {
        private final boolean m_fireUpdate;
        
        public FileModelTransaction(boolean fireUpdate) {
            m_fireUpdate = fireUpdate;
        }

        public FileModelTransaction() {
            this(false);
        }
        
        public void run() {
            System.out.println("Transaction started.");
            try {
                assert SwingUtilities.isEventDispatchThread() : "Transaction must be called in AWT thread.";                
                m_bDoc.atomicLock();
                //m_isTransactionInProgress = true;
                transaction();
            } catch(Exception e) {
                m_bDoc.atomicUndo();
                System.out.println("Transaction failed!");
                e.printStackTrace();
            } finally {
                //m_isTransactionInProgress = false;
                m_bDoc.atomicUnlock();
                System.out.println("Transaction completed.");
                if ( m_fireUpdate) {
                    getDataObject().fireContentChanged();
                } else {
                    m_model.forceUpdate();
                }
            }
        }
        
        protected abstract void transaction() throws Exception;        
    }
    
    private final XmlMultiViewEditorSupport m_edSup;
    private final ElementMapping            m_mapping;
    private       List<ModelListener>       m_modelListeners = new ArrayList<ModelListener>();
    private       BaseDocument              m_bDoc;
    private       EditorKit                 m_kit;
    private       DocumentModel             m_model;
    private       boolean                   m_isChanged = true;
    private volatile boolean                m_eventInProgress = false;
    private volatile boolean                m_sourceChanged = false;
    private volatile boolean                m_updateInProgress = false;
    //private volatile boolean                m_isTransactionInProgress = false;
    private          Object                 m_lock = new Object();
    
    private final DocumentListener          docListener = new DocumentListener() {
        public void removeUpdate(DocumentEvent e) {documentModified(e);}
        
        public void insertUpdate(DocumentEvent e) {documentModified(e);}
        
        public void changedUpdate(DocumentEvent e) {documentModified(e);}
    };
    
    private final DocumentModelListener    modelListener = new DocumentModelListener() {
        public void documentElementRemoved(DocumentElement de) {
            if (isTagElement(de)) {
                //System.out.println("Element removed " + de.getName() + " " + de.toString()  + "[" + de.getElementCount() + "]");
                fireModelChange();
            }
        }

        public void documentElementChanged(DocumentElement de) {
            if (isTagElement(de)) {
                //System.out.println("Element changed " + de.getName() + " " + de.toString()  + "[" + de.getElementCount() + "]");
                fireModelChange();
            }
        }

        public void documentElementAttributesChanged(DocumentElement de) {
            if (isTagElement(de)) {
                //TODO solve the assert problem
                /*java.lang.AssertionError
                    at org.netbeans.modules.editor.structure.api.DocumentModel.getChildren(DocumentModel.java:551)
                    at org.netbeans.modules.editor.structure.api.DocumentElement.getChildren(DocumentElement.java:274)
                    at org.netbeans.modules.editor.structure.api.DocumentElement.getElementCount(DocumentElement.java:145)
                    at org.netbeans.modules.mobility.svgcore.model.SVGFileModel$2.documentElementAttributesChanged(SVGFileModel.java:84)
                 */
                //System.out.println("Element attrs changed " + de.getName() + " " + de.toString()  + "[" + de.getElementCount() + "]");
                fireModelChange();
            }
        }

        public void documentElementAdded(DocumentElement de) {
            if (isTagElement(de)) {
                String id = getIdAttribute(de);
                if (id != null) {
                    m_mapping.add( id, de);
                }
                //System.out.println("Element added " + de.getName() + " " + de.toString() + "[" + de.getElementCount() + "]");
                fireModelChange();
            }
        }
    };
    
    private final DocumentModelStateListener modelStateListener = new DocumentModelStateListener() {    
        public void sourceChanged() {
            synchronized(m_lock) {
                System.out.println("Event: source-changed");
                m_sourceChanged = true;
                m_lock.notifyAll();
            }
        }

        public void scanningStarted() {
            synchronized(m_lock) {
                System.out.println("Event: update-started");
                m_updateInProgress = true;
                m_sourceChanged = false;
                m_lock.notifyAll();
            }            
        }
        
        public void updateStarted() {

        }

        public void updateFinished() {
            synchronized(m_lock) {
                System.out.println("Event: update-finished");
                m_updateInProgress = false;
                m_lock.notifyAll();
            }
        }
    };

    private final Runnable updateTask = new Runnable() {
        public void run() {
            System.out.println("Updating ...");
            try {
                synchronized( m_modelListeners) {
                    for (int i = 0; i < m_modelListeners.size(); i++) {
                        ((ModelListener) m_modelListeners.get(i)).modelChanged();
                    }
                }
            } finally {
                System.out.println("Update completed");
                m_eventInProgress = false;
            }
        }
    };
    
    /** Creates a new instance of SVGFileModel */
    public SVGFileModel(XmlMultiViewEditorSupport edSup) {
        m_edSup   = edSup;
        m_model   = null;
        m_mapping = new ElementMapping();
    }        

    public SVGDataObject getDataObject() {
        return (SVGDataObject) m_edSup.getDataObject();
    }
    
    public SVGImage parseSVGImage() throws IOException, BadLocationException {
        SVGImage svgImage = m_mapping.parseDocument(this, m_model);
        return svgImage;
    }       
    
    public boolean isChanged() {
        return m_isChanged;
    }
    
    public void setChanged(boolean isChanged) {
        m_isChanged = isChanged;
    }
    
    public void addModelListener( ModelListener listener) {
        synchronized( m_modelListeners) {
            m_modelListeners.add(listener);
        }
    }

    public void removeModelListener( ModelListener listener) {
        synchronized( m_modelListeners) {
            m_modelListeners.remove(listener);
        }
    }
    
    private volatile boolean m_undoInProcess = false;
    
    protected void documentModified(DocumentEvent e) {
        if ( e instanceof BaseDocumentEvent) {
            BaseDocumentEvent bde = (BaseDocumentEvent) e;
            if (bde.isInRedo() || bde.isInUndo()) {
                synchronized(this) {
                    if ( !m_undoInProcess) {
                        m_undoInProcess = true;
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                m_undoInProcess = false;
                                ((SVGDataObject) m_edSup.getDataObject()).fireContentChanged();
                            }
                        });
                    }
                }
            }
        }
        setChanged(true);
    }
    
    public boolean containsAnimations() {
        //TODO implement
        return true;
    }
    
    public synchronized void refresh() {
        try {
            checkDocument();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
       
    private JEditorPane getOpenedEditor() {
        final JEditorPane [] panes = new JEditorPane[1];
                
        if ( SwingUtilities.isEventDispatchThread()) {
            JEditorPane [] temp = m_edSup.getOpenedPanes();
            if (temp != null && temp.length > 0) {
                panes[0] = temp[0];
            }
        } else {
            try {
                SwingUtilities.invokeAndWait( new Runnable() {
                    public void run() {
                        JEditorPane [] temp = m_edSup.getOpenedPanes();
                        if (temp != null && temp.length > 0) {
                            panes[0] = temp[0];
                        }
                    }
                });
            } catch( Exception e) {
                e.printStackTrace();
            }
        }
        
        return panes[0];
    }
    
    //TODO synchronize
    private void checkDocument() throws IOException, DocumentModelException {
        JEditorPane editor = getOpenedEditor();
        
        if (m_bDoc == null) {
            if (editor != null) {
                // reuse document from opened editor, if exists
                m_kit = editor.getEditorKit();
                createModel( (BaseDocument) editor.getDocument(), true);
            } else {
                // otherwise create the document from scratch
                //m_kit = JEditorPane.createEditorKitForContentType(SVGDataLoader.REQUIRED_MIME);
                //Use in NB 6.0
                m_kit = CloneableEditorSupport.getEditorKit( SVGDataLoader.REQUIRED_MIME);
                
                //TODO Read file content in another thread
                createModel( (BaseDocument) m_kit.createDefaultDocument(), false);
                try {
                    m_kit.read( m_edSup.getInputStream(), m_bDoc, 0);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }
        } else {
            if (editor != null) {
                BaseDocument opened = (BaseDocument) editor.getDocument();
                if ( opened != m_bDoc) {
                    m_bDoc.removeDocumentListener(docListener);
                    m_kit   = editor.getEditorKit();
                    createModel(opened, true);
                }
            }
        }
        assert m_model != null;
        assert m_kit   != null;
        assert m_bDoc    != null;
    }
    
    private void createModel( BaseDocument doc, boolean addListener) throws DocumentModelException {
        boolean modelChanged = m_model != null;
        m_bDoc  = doc;
        m_model = DocumentModel.getDocumentModel(m_bDoc);
        //System.out.println("******** Creating model");
        //m_testModel = new TestSVGFileModel(doc, edSup.getDataObject());
        //System.out.println("******** dumping model");
        //m_testModel.dump(null, 0);
        
        if (addListener) {
            m_bDoc.addDocumentListener(docListener);
            m_model.addDocumentModelListener(modelListener);
            m_model.addDocumentModelStateListener(modelStateListener);
        }
        
        if (modelChanged) {
            fireModelSwitched();
        }
    }
    
    public synchronized DocumentModel _getModel() throws Exception {
        checkDocument();
        return m_model;
    }
        
    public static boolean isTagElement(DocumentElement elem) {
        return elem != null && (elem.getType().equals(XML_TAG) ||
               elem.getType().equals(XML_EMPTY_TAG));
    }

    public static boolean isError(DocumentElement elem) {
        return elem.getType().equals(XML_ERROR_TAG);
    }
    
    /**
     * Convenience helper method.
     */ 
    public static String getIdAttribute(DocumentElement de) {
        AttributeSet attrs = de.getAttributes();
        String id = (String) attrs.getAttribute("id");
        return id;
    }

    /**
     * Convenience helper method.
     */ 
    public static List<DocumentElement> getParents(DocumentElement elem) {
        List<DocumentElement> list = new ArrayList<DocumentElement>();
        while( elem != null) {
            list.add(elem);
            elem = elem.getParentElement();
        }
        return list;
    }
    
    public DocumentElement getElementById(String id) {
        DocumentElement elem = m_mapping.id2element(id);
        return elem;
    }
    
    
    public String getElementId(DocumentElement de) {
        String id = m_mapping.element2id(de);
        assert id != null : "Element " + de + " could not be found!";
        return id;
    }
    
    public String createUniqueId(String prefix, boolean isWrapper) {
        return m_mapping.generateId(prefix, isWrapper);
    }
    
    public static boolean isWrapperId(String id) {
        return ElementMapping.isWrapperId(id);        
    }
    
    public synchronized String describeElement(String id, boolean showTag, boolean showAttributes, String lineSep) {
        DocumentElement de = getElementById(id);
        if (de != null) {
            checkIntegrity(de);
            return describeElement( de, showTag, showAttributes, lineSep);
        } else {
            return "";
        }
    }

    public int getOffsetByPosition(int line, int column) {
        Element root = m_bDoc.getDefaultRootElement();
        line = Math.max(line, 1);
        line = Math.min(line, root.getElementCount());
        int pos = root.getElement(line - 1).getStartOffset() + column;
        return pos;
    }
            
    public static String describeElement( DocumentElement el, boolean showTag, boolean showAttributes, String lineSep) {
        StringBuilder sb = new StringBuilder();
        
        if ( showTag) {
            sb.append(el.getName());
        }
        
        AttributeSet attrs = el.getAttributes();
        if (showAttributes) {
            for ( Enumeration names = attrs.getAttributeNames(); names.hasMoreElements(); ) {
                String attrName = (String) names.nextElement();
                sb.append( ' ');
                sb.append( attrName);
                sb.append( "=\"");
                sb.append( attrs.getAttribute(attrName));
                sb.append('"');
                if (lineSep != null) {
                    sb.append(lineSep);
                }
            }
        }
        
        return sb.toString();
    }       
    
    public static boolean isAnimation(DocumentElement elem) {
        String tagName = elem.getName();        
        
        for (int i = 0; i < ANIMATION_TAGS.length; i++) {
            if (ANIMATION_TAGS[i].equals(tagName)) {
                return true;
            }
        }
        return false;
    }
    
    public void deleteElement(final String id)  {
        runTransaction(new FileModelTransaction() {
            protected void transaction() throws BadLocationException {
                DocumentElement de       = checkIntegrity(id);
                int             startOff = de.getStartOffset();
                m_bDoc.remove(startOff, de.getEndOffset() - startOff + 1);
            }
        });
    }
    /*
    public void wrapElement(final String id, final String wrapperId) {
        runTransaction(new FileModelTransaction() {
            protected void transaction() throws BadLocationException {
                DocumentElement de       = checkIntegrity(id);
                int             startOff = de.getStartOffset();
                int             length   = de.getEndOffset() - startOff + 1;

                m_bDoc.replace(startOff, length,
                             PatchedGroup.wrapText( wrapperId, m_bDoc.getText(startOff, length)), null);
            }
        });
    }
    */
    public void _appendElement( final String insertString) {
        runTransaction(new FileModelTransaction(true) {
            protected void transaction() throws BadLocationException {
                DocumentElement svgRoot = getSVGRoot(m_model);

                if (svgRoot != null) {
                    DocumentElement lastChild = getLastTagChild(svgRoot);
                    int    insertPosition;
                    if (lastChild != null) {
                        //insert new text after last child
                        insertPosition = lastChild.getEndOffset()+1;
                        m_bDoc.insertString(insertPosition, insertString, null);
                    } else {
                        String docText  = m_bDoc.getText(0, m_bDoc.getLength());
                        int    startOff = svgRoot.getStartOffset();
                        int    c = 0;
                        
                        insertPosition = svgRoot.getEndOffset() - 1;
                        while( insertPosition > startOff &&
                                (c=docText.charAt(insertPosition--)) != '/') {}
                        
                        if (c == '/') {
                            if (docText.charAt(insertPosition) == '<') {
                                m_bDoc.insertString(insertPosition, insertString, null);
                            } else {
                                StringBuilder sb = new StringBuilder( docText.substring(startOff, insertPosition+1));
                                sb.append(">");
                                sb.append(insertString);
                                sb.append("\n</svg>");
                                m_bDoc.replace(startOff, svgRoot.getEndOffset() - startOff + 1, sb.toString(), null);
                            }    
                        } else {
                            //TODO report invalid svg doc
                        }
                    }
                } else {
                    //TODO report invalid svg doc
                }
            }
        });
    }
    
    public void setAttribute( final String id, final String attrName, final String attrValue) {
        runTransaction(new FileModelTransaction() {
            protected void transaction() throws BadLocationException {
                DocumentElement elem = checkIntegrity(id);
                assert isTagElement(elem) : "Attribute change allowed only for tag elements";

                int startOff = elem.getStartOffset() + 1 + elem.getName().length();        
                int endOff;

                List<DocumentElement> children = elem.getChildren();

                if ( children.size() > 0) {
                    endOff = children.get(0).getStartOffset() - 1;
                } else {
                    endOff = elem.getEndOffset() - 1;
                }

                String fragment = m_bDoc.getText(startOff, endOff - startOff + 1);
                int p;
                if ( (p=fragment.indexOf(attrName)) != -1) {
                    p += attrName.length();
                    while( ++p < fragment.length()) {
                        if (fragment.charAt(p) =='"') {
                            int q = p;

                            while( ++q < fragment.length()) {
                                if (fragment.charAt(q) =='"') {
                                    p++;
                                    m_bDoc.replace(startOff + p, q-p , attrValue, null);
                                    return;
                                }
                            }
                        }
                    }
                    System.err.println("Attribute " + attrName + " not changed: \"" + fragment + "\"");
                } else {
                    StringBuilder sb = new StringBuilder(" ");
                    sb.append(attrName);
                    sb.append("=\"");
                    sb.append(attrValue);
                    sb.append( "\" ");
                    m_bDoc.insertString(startOff, sb.toString(), null);
                }            
            }
        });
    }
    
    /**
     * Make the selected element to become the first child of its parent.
     */
    public void moveToBottom(final String id) {
        runTransaction(new FileModelTransaction() {
            protected void transaction() throws BadLocationException {
                DocumentElement de         = checkIntegrity(id);
                DocumentElement firstChild = getFirstTagChild(de.getParentElement());

                if (de != firstChild) {
                    int    startOffset = de.getStartOffset();
                    int    length      = de.getEndOffset() - startOffset + 1;
                    String elemText    = getElementText(de, id);

                    int insertOffset = firstChild.getStartOffset();
                    assert insertOffset < startOffset;

                    m_bDoc.remove(startOffset, length);
                    m_bDoc.insertString(insertOffset, elemText, null);
                }        
            }
        });       
    }
        
    /**
     * Make the selected element to become the last child of its parent.
     */
    public void moveToTop(final String id) {
        runTransaction(new FileModelTransaction() {
            protected void transaction() throws BadLocationException {
                DocumentElement de        = checkIntegrity(id);
                DocumentElement lastChild = getLastTagChild(de.getParentElement());

                if (de != lastChild) {
                    int startOffset = de.getStartOffset();
                    int length      = de.getEndOffset() - startOffset + 1;
                    String elemText = getElementText(de, id);

                    int insertOffset = lastChild.getEndOffset() + 1;
                    assert startOffset < insertOffset;

                    m_bDoc.insertString(insertOffset, elemText, null);
                    m_bDoc.remove(startOffset, length);
                }
            }
        });
    }

    /**
     * Move the selected element one position to the end of a list of its siblings.
     */
    public void moveForward(final String id) {
        runTransaction(new FileModelTransaction() {
            protected void transaction() throws BadLocationException {
                DocumentElement de        = checkIntegrity(id);
                DocumentElement nextChild = getNextTagSibling(de);

                if (nextChild != null) {
                    int startOffset = de.getStartOffset();
                    int length      = de.getEndOffset() - startOffset + 1;
                    String elemText = m_bDoc.getText(startOffset, length);

                    int insertOffset = nextChild.getEndOffset() + 1;
                    assert startOffset < insertOffset;

                    m_bDoc.insertString(insertOffset, elemText, null);
                    m_bDoc.remove(startOffset, length);
                }
            }
        });
    }

    /**
     * Move the selected element one position to the beginning of a list of its siblings.
     */
    public void moveBackward(final String id) {
        runTransaction(new FileModelTransaction() {
            protected void transaction() throws BadLocationException {
                DocumentElement de            = checkIntegrity(id);
                DocumentElement previousChild = getPreviousTagSibling(de);

                if (previousChild != null) {
                    int startOffset = de.getStartOffset();
                    int length      = de.getEndOffset() - startOffset + 1;
                    String elemText = m_bDoc.getText(startOffset, length);

                    int insertOffset = previousChild.getStartOffset();
                    assert startOffset > insertOffset;

                    m_bDoc.remove(startOffset, length);
                    m_bDoc.insertString(insertOffset, elemText, null);
                }
            }
        });
    }
    
    public static DocumentElement getSVGRoot(final DocumentModel model) {
        DocumentElement root = model.getRootElement();
        for (DocumentElement de : root.getChildren()) {
            if (isTagElement(de) && "svg".equals(de.getName())) {
                return de;
            }
        }
        return null;
    }
    
    protected synchronized void fireModelChange() {
        if ( !m_eventInProgress) {
            System.out.println("Asking for update");
            SwingUtilities.invokeLater( updateTask);
        }
    }
    
    protected void fireModelSwitched() {
        synchronized( m_modelListeners) {
            for (int i = 0; i < m_modelListeners.size(); i++) {
                ((ModelListener) m_modelListeners.get(i)).modelSwitched();
            }
        }
    }
    
    public void runTransaction( final FileModelTransaction transaction) {
        new Thread("TransactionWrapper") {
            public void run() {
                updateModel();
                SwingUtilities.invokeLater(transaction);
            }
            
        }.start();
    }    
    
    /*
     * Verify that the DocumentModel is up to date and no change of the
     * source file is being processed. If the method is called called from
     * the AWT thread, a deadlock will occur since DocumentModel update
     * implementation calls SwingUtilitis.inwokeAndWait() method.
     */ 
    void updateModel() {
        System.out.println("Updating model ...");
        assert SwingUtilities.isEventDispatchThread() == false : "Model update cannot be called in AWT thread.";
        
        synchronized(m_lock) {
            if (m_sourceChanged) {
                System.out.println("Forcing model update");
                m_model.forceUpdate();
            } else if (!m_updateInProgress) {
                System.out.println("Model already up to date.");
                return;
            } 
            
            while( m_sourceChanged || m_updateInProgress) {
                System.out.print( " Waiting for model update...");
                try {
                    m_lock.wait();
                    System.out.println( " Wait ended."); 
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }            
        }    
        System.out.println("Model update completed.");
    }
        
    //TODO Show dialog with id conversion mapping
    @SuppressWarnings({"deprecation"})
    public void mergeImage(File file) throws FileNotFoundException, IOException, DocumentModelException, BadLocationException {
        DocumentModel   modelToInsert = loadDocumentModel(file);
        String          wrapperId     = createUniqueId(file.getName().replace('.', '_'), true);
        String          textToInsert  = m_mapping.getWithUniqueIds(modelToInsert, wrapperId);

        textToInsert = wrapText( wrapperId, textToInsert);
        _appendElement(textToInsert);
    } 

    protected static String wrapText(String wrapperId, String textToWrap) {
        //TODO indent the wrapped text
        StringBuilder sb = new StringBuilder();
        sb.append( "<g id=\"");
        sb.append(wrapperId);
        sb.append("\">\n");
        sb.append(textToWrap);
        sb.append("\n</g>");
        return sb.toString();
    }
    
/*            
                    wrapper.attachSVGObject( new SVGObject(m_sceneMgr, wrapper));

                    transferChildren(wrapper, (SVG)sibling);
                    fileModel.appendElement(wrapper.getText(false), insertedText);
            
            DocumentElement svgElem       = getSVGRoot(modelToInsert);
            
            int childElemNum;
            if (svgElem != null &&
                (childElemNum=svgElem.getElementCount()) > 0) {
                int startOff = svgElem.getElement(0).getStartOffset();
                int endOff   = svgElem.getElement(childElemNum - 1).getEndOffset();

                String insertedText = modelToInsert.getDocument().getText(startOff, endOff - startOff + 1);
                
            }
        
        SVGFileModel fileModel = m_sceneMgr.getDataObject().getModel();
        
        if (svgElem != null && ) {
            
            Set<String> oldIds = new HashSet<String>();
            collectIDs(m_svgDoc, oldIds);

            Set<String> newIds = new HashSet<String>();
            collectIDs( svgElem, newIds);

            Set<String> conflicts = new HashSet<String>();
            for (String id  : newIds) {
                if (oldIds.contains(id)) {
                    conflicts.add(id);
                }
            }

            BaseDocument doc  = (BaseDocument) docModel.getDocument();
            if ( !conflicts.isEmpty()) {
                for (String id : conflicts) {
                    String newID = fileModel.createUniqueId(id, false);
                    for (String pattern : REPLACE_PATTERNS) {
                        String oldStr = MessageFormat.format(pattern, id);
                        String newStr = MessageFormat.format(pattern, newID);
                        replaceAllOccurences(doc, oldStr, newStr);
                    }
                }
            }

            String text = doc.getText(0, doc.getLength());
            java.io.StringBufferInputStream strIn = new java.io.StringBufferInputStream(text);
            try {
                ModelBuilder.loadDocument(strIn, m_svgDoc,
                        SVGComposerPrototypeFactory.getPrototypes(m_svgDoc));
            } finally {
                strIn.close();
            }                            

            SVG svgRoot = (SVG)getSVGRootElement();
            System.out.println("Before children transfer");
            printTree(m_svgDoc, 0);

            ModelNode sibling = svgRoot;
            while( (sibling=sibling.getNextSiblingNode()) != null) {
                if (sibling instanceof SVG && 
                    sibling.getFirstChildNode() != null) {

                    PatchedGroup wrapper = (PatchedGroup) m_svgDoc.createElementNS(SVGConstants.SVG_NAMESPACE_URI,
                        SVGConstants.SVG_G_TAG);
                    ((SVG)svgRoot).appendChild(wrapper);
                    //wrapper.setPath( new int[] { 0, getChildrenCount(svgRoot)});
                    wrapper.setId( fileModel.createUniqueId(file.getName(), true));
                    wrapper.attachSVGObject( new SVGObject(m_sceneMgr, wrapper));

                    transferChildren(wrapper, (SVG)sibling);
                    fileModel.appendElement(wrapper.getText(false), insertedText);
                    break;
                }
            }
            
            System.out.println("After children transfer");
            printTree(m_svgDoc, 0);                    
        }       
    }    
    
    
  */  
    
    
    public static DocumentModel loadDocumentModel( File file) throws FileNotFoundException, IOException, DocumentModelException {
        InputStream in = null;
        
        try {
            FileInputStream fin = new FileInputStream(file);
            in = new BufferedInputStream(fin);

            String fileName = file.getName();

            int p;
            if ( (p=fileName.indexOf('.')) != -1) {
                if (SVGDataObject.isSVGZ(fileName.substring(p+1))) {
                    in = new BufferedInputStream(new GZIPInputStream(in));
                }
            }
            
            return loadDocumentModel(in);
        } finally {
            in.close();
        }
    }
    
    protected static DocumentModel loadDocumentModel(InputStream in) throws IOException, DocumentModelException {
        EditorKit    kit = JEditorPane.createEditorKitForContentType(SVGDataLoader.REQUIRED_MIME);
        BaseDocument doc = (BaseDocument) kit.createDefaultDocument();
        try {
            kit.read( in, doc, 0);
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        } finally {
            in.close();
        }
        DocumentModel model = DocumentModel.getDocumentModel(doc);
        return model;
    }    
    
    
    /** 
     * Get text for the given element and inject provided id if no id
     * is defined.
     */
    private String getElementText( DocumentElement de, String id) throws BadLocationException {
        int    startOffset = de.getStartOffset();
        int    length      = de.getEndOffset() - startOffset + 1;

        String elemText = m_bDoc.getText(startOffset, length);

        if (id != null && !de.getAttributes().isDefined("id")) {
            StringBuilder sb = new StringBuilder(elemText);
            sb.insert(de.getName().length() + 1, " id=\"" + id + "\"");
            elemText = sb.toString();
        }
        
        return elemText;
    }
    
    private static DocumentElement getFirstTagChild( DocumentElement parent) {
        assert parent != null;
        for (DocumentElement child : parent.getChildren()) {
            if (isTagElement(child)) {
                return child;
            }
        }
        return null;
    }

    private static DocumentElement getLastTagChild( DocumentElement parent) {
        assert parent != null;
        List<DocumentElement> children = parent.getChildren();
        
        for (int i = children.size() - 1; i >= 0; i--) {
            DocumentElement child = children.get(i);
            if ( isTagElement(child)) {
                return child;
            }
        }
        return null;
    }
    
    private static DocumentElement getPreviousTagSibling( DocumentElement elem) {
        DocumentElement parent   = elem.getParentElement();
        DocumentElement previous = null;
        assert parent != null;
        
        for (DocumentElement child : parent.getChildren()) {
            if (child == elem) {
                return previous;
            } else {
                if ( isTagElement(child)) {
                    previous = child;
                }
            }
        }
        assert false : "The document element " + elem + " is no longer part of the document";
        return null;
    }

    private static DocumentElement getNextTagSibling( DocumentElement elem) {
        DocumentElement parent = elem.getParentElement();
        DocumentElement next   = null;
        assert parent != null;
        List<DocumentElement> children = parent.getChildren();
        
        for ( int i = children.size() - 1; i >= 0; i--) {
            DocumentElement child = children.get(i);
            if (child == elem) {
                return next;
            } else {
                if ( isTagElement(child)) {
                    next = child;
                }
            }
        }
        assert false : "The document element " + elem + " is no longer part of the document";
        return null;
    }
        
    private void checkIntegrity(DocumentElement de) {        
        assert de != null;
        assert de.getDocument() == m_bDoc : "Element is not part of the current document";
        assert de.getDocumentModel() == m_model : "Element is not part of the current document model";
    }    
    
    private DocumentElement checkIntegrity(String id) {
        DocumentElement de = getElementById(id);
        assert de != null : "No element with id: " + id;
        checkIntegrity(de);
        return de;
    }    
}
