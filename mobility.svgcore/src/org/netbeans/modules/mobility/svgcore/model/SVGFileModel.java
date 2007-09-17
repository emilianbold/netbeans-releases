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

import com.sun.perseus.util.SVGConstants;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.GZIPInputStream;
import javax.microedition.m2g.SVGImage;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseDocumentEvent;
import org.netbeans.editor.CharSeq;
import org.netbeans.modules.editor.structure.api.DocumentElement;
import org.netbeans.modules.editor.structure.api.DocumentModel;
import org.netbeans.modules.editor.structure.api.DocumentModelException;
import org.netbeans.modules.editor.structure.api.DocumentModelListener;
import org.netbeans.modules.editor.structure.api.DocumentModelStateListener;
import org.netbeans.modules.mobility.svgcore.SVGDataLoader;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.modules.mobility.svgcore.composer.PerseusController;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;
import org.netbeans.modules.mobility.svgcore.model.ElementMapping;
import org.netbeans.modules.mobility.svgcore.view.source.SVGSourceMultiViewElement;
import org.netbeans.modules.xml.multiview.XmlMultiViewEditorSupport;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Pavel Benes
 */
public final class SVGFileModel {
    protected static final String    XML_TAG            = "tag";  //NOI18N
    protected static final String    XML_EMPTY_TAG      = "empty_tag"; //NOI18N
    protected static final String    XML_ERROR_TAG      = "error"; //NOI18N
    protected static final String [] ANIMATION_TAGS     = { "animate", "animateTransform", "animateMotion", "animateColor"}; //NOI18N
    protected static final String    TRANSACTION_TOKEN  = "transaction";
    protected static final String    MODEL_UPDATE_TOKEN = "update";
        
    public interface ModelListener {
        public void modelChanged();
    }
    
    public interface TransactionCommand {
        public Object execute( Object userData);        
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
            //System.out.println("Starting transaction ...");
            
            if (incrementTransactionCounter() == 1) {
                getSceneManager().setBusyState( TRANSACTION_TOKEN, true);
            }
            
            synchronized( getTransactionMonitor()) {
                //System.out.println("Transaction started.");
                try {
                    updateModel();                

                    //assert SwingUtilities.isEventDispatchThread() : "Transaction must be called in AWT thread.";                
                    getDoc().atomicLock();
                    //checkModel();
                    m_model.readLock();
                    transaction();
                } catch(Exception e) {
                    //System.out.println("Transaction failed!");
                    e.printStackTrace();
                    getDoc().atomicUndo();
                } finally {
                    m_model.readUnlock();
                    getDoc().atomicUnlock();
                    //System.out.println("Transaction completed.");
                    //verifyModel();
                    if ( decrementTransactionCounter() == 0) {
                        getSceneManager().setBusyState( TRANSACTION_TOKEN, false);
                    }
                }
            }
            
            if ( m_fireUpdate) {
                getDataObject().fireContentChanged();
            } else {
                m_model.forceUpdate();
            }            
        }
        
        protected abstract void transaction() throws Exception;        
    }
    
    private final XmlMultiViewEditorSupport m_edSup;
    private final ElementMapping            m_mapping;
    private final List<ModelListener>       m_modelListeners   = new ArrayList<ModelListener>();
    private final Object                    m_lock             = new Object();
    private final Object                    m_transactionLock  = new Object();
    private volatile BaseDocument           m_bDoc;
    private       DocumentModel             m_model;
    private       boolean                   m_isChanged        = true;
    private volatile boolean                m_eventInProgress  = false;
    private volatile boolean                m_sourceChanged    = false;
    private volatile boolean                m_updateInProgress = false;
    private volatile boolean                m_updateInProcess    = false;
    private volatile int []                 m_transactionCounter = new int[] {0};
    
    private final DocumentListener          docListener = new DocumentListener() {
        public void removeUpdate(DocumentEvent e) {documentModified(e);}
        
        public void insertUpdate(DocumentEvent e) {documentModified(e);}
        
        public void changedUpdate(DocumentEvent e) {documentModified(e);}
    };
    
    private final DocumentModelListener    modelListener = new DocumentModelListener() {
        public void documentElementRemoved(DocumentElement de) {
            if (isTagElement(de)) {
                //System.out.println("Element removed " + de.getName() + " " + de.toString()  + "[" + de.getElementCount() + "]");
                m_mapping.remove(de);

                //if ( de.getStartOffset() > 0 && de.getStartOffset() >= de.getEndOffset()) {
                //    System.out.println("Element removed, offset problem: " + de);
                //}
                
                fireModelChange();
            }
        }

        public void documentElementChanged(DocumentElement de) {
            if (isTagElement(de)) {
                //System.out.println("Element changed " + de.getName() + " " + de.toString()  + "[" + de.getElementCount() + "]");

                //if ( de.getStartOffset() > 0 && de.getStartOffset() >= de.getEndOffset()) {
                //    System.out.println("Element changed, offset problem: " + de);
                //}
                
                fireModelChange();
            }
        }

        public void documentElementAttributesChanged(DocumentElement de) {
            if (isTagElement(de)) {
                //System.out.println("Element attrs changed " + de.getName() + " " + de.toString()  + "[" + de.getElementCount() + "]");
                //if ( de.getStartOffset() > 0 && de.getStartOffset() >= de.getEndOffset()) {
                //    System.out.println("Element attributes changed, offset problem: " + de);
                //}
                fireModelChange();
            }
        }

        public void documentElementAdded(DocumentElement de) {
            if (isTagElement(de)) {
                m_mapping.add( de);
                //System.out.println("Element added " + de.getName() + " " + de.toString() + "[" + de.getElementCount() + "]");
                //if ( de.getStartOffset() > 0 && de.getStartOffset() >= de.getEndOffset()) {
                //    System.out.println("Element added, offset problem: " + de);
                //}
                fireModelChange();
            }
        }
    };
        
    private final DocumentModelStateListener modelStateListener = new DocumentModelStateListener() {    
        public void sourceChanged() {
            synchronized(m_lock) {
                //System.out.println("Event: source-changed");
                m_sourceChanged = true;
                m_lock.notifyAll();
            }
        }

        public void scanningStarted() {
            synchronized(m_lock) {
                getSceneManager().setBusyState( MODEL_UPDATE_TOKEN, true);                
                //System.out.println("Event: update-started");
                m_updateInProgress = true;
                m_sourceChanged = false;
                m_lock.notifyAll();
            }            
        }
        
        public void updateStarted() {
        }           

        public void updateFinished() {
            synchronized(m_lock) {
                //System.out.println("Event: update-finished");
                m_updateInProgress = false;
                m_lock.notifyAll();
                getSceneManager().setBusyState( MODEL_UPDATE_TOKEN, false);                
            }
        }
    };

    private final Runnable updateTask = new Runnable() {
        public void run() {
            //System.out.println("Updating ...");
            try {
                synchronized( m_modelListeners) {
                    for (int i = 0; i < m_modelListeners.size(); i++) {
                        ((ModelListener) m_modelListeners.get(i)).modelChanged();
                    }
                }
            } finally {
                //System.out.println("Update completed");
                m_eventInProgress = false;
            }
        }
    };
    
    /** Creates a new instance of SVGFileModel */
    public SVGFileModel(XmlMultiViewEditorSupport edSup) {
        m_edSup   = edSup;
        m_model   = null;
        m_mapping = new ElementMapping( this);
    }        

    public synchronized void attachToOpenedDocument() {
        assert SwingUtilities.isEventDispatchThread() : "Model initialisation must be called from AWT thread!";
        if (m_bDoc == null) {
            m_bDoc = getOpenedDoc();
            //System.out.println("Using already opened document.");
            assert m_bDoc != null;
            m_bDoc.addDocumentListener(docListener);
        } else {
            assert m_bDoc == getOpenedDoc() : "Model mismatch";
        }
    }
    
    public synchronized void detachDocument() {
        m_model = null;
        m_bDoc  = null;
        m_modelListeners.clear();
        //System.out.println("Removing the document.");
    }
    
    private BaseDocument getOpenedDoc() {
        JEditorPane[] panes = m_edSup.getOpenedPanes();
        if (panes != null && panes.length > 0) {
            return (BaseDocument) panes[0].getDocument();
        } else {
            return null;
        }
    }
    
    private SceneManager getSceneManager() {
        return ((SVGDataObject) m_edSup.getDataObject()).getSceneManager();
    }
    
    public Object getTransactionMonitor() {
        return m_transactionLock;
    }
    
    private int incrementTransactionCounter() {
        synchronized( m_transactionCounter) {
            m_transactionCounter[0]++;
            return m_transactionCounter[0];
        }
    }

    private int decrementTransactionCounter() {
        synchronized( m_transactionCounter) {
            m_transactionCounter[0]--;
            return m_transactionCounter[0];
        }
    }
    
    private int getTransactionCounter() {
        synchronized( m_transactionCounter) {
            return m_transactionCounter[0];
        }
    }
    
    private synchronized BaseDocument getDoc() {
        if ( m_bDoc == null) {
            try {
                //System.out.println("Opening new document.");
                m_bDoc = (BaseDocument) m_edSup.openDocument();
                assert m_bDoc != null;
                m_bDoc.addDocumentListener(docListener);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return m_bDoc;        
    }
    
    private synchronized void checkModel() {
        if (m_model == null) {
            try {
                m_model = DocumentModel.getDocumentModel(getDoc());
                m_model.addDocumentModelListener(modelListener);
                m_model.addDocumentModelStateListener(modelStateListener);
            } catch (DocumentModelException ex) {
                Exceptions.printStackTrace(ex);
            }
        }    
    }    
/*
    private void verifyModel() {
        updateModel();
        System.out.println("Veryfying model ...");
        if ( !verifyModel( m_model.getRootElement())) {
            System.out.println("Model OK.");
        }
    }
*/    
    private boolean verifyModel(DocumentElement de) {
        List<DocumentElement> children = de.getChildren();
        
        for ( DocumentElement d : children) {
            if ( isTagElement(d) && d.getStartOffset() >= d.getEndOffset()) {
                System.out.println("ERROR: Invalid element offset: " + d);
                return true;
            }
            if ( d.getParentElement() != de) {
                System.out.println("ERROR: Invalid parent relation: " + d);
                return true;
            }
            if (verifyModel(d)) {
                return true;
            }
        }
        return false;
    }
    
    public SVGDataObject getDataObject() {
        return (SVGDataObject) m_edSup.getDataObject();
    }
    
    public SVGImage parseSVGImage() throws IOException, BadLocationException {
        checkModel();
        SVGImage svgImage = m_mapping.parseDocument(true);
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
    
    protected void documentModified(DocumentEvent e) {
        if ( e instanceof BaseDocumentEvent) {                    
            BaseDocumentEvent bde = (BaseDocumentEvent) e;
            if ( bde.isInRedo() || 
                 bde.isInUndo() || 
                 getTransactionCounter() == 0) {
                synchronized(this) {
                    if ( !m_updateInProcess) {
                        m_updateInProcess = true;
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                m_updateInProcess = false;
                                ((SVGDataObject) m_edSup.getDataObject()).fireContentChanged();
                            }
                        });
                    }
                }
            }
        }
        setChanged(true);
    }
    
    public synchronized DocumentModel getModel() {
        checkModel();
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
        String id = (String) attrs.getAttribute(SVGConstants.SVG_ID_ATTRIBUTE);
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
        //assert !SwingUtilities.isEventDispatchThread() : "getElementID cannot be called from AWT thread";
        checkModel();
/*        
        System.out.println("Asking for id: " + id);

        //wait for stable model
        synchronized(m_lock) {
            while( m_updateInProgress) {
                try {
                    m_lock.wait();
                } catch( InterruptedException e) {}
            }
        }
*/
//        System.out.println("Update in progress: " + m_updateInProgress);
        DocumentElement elem = m_mapping.id2element(id);
        return elem;
    }
/*
    public void storeSelection(String id) {
        m_mapping.storeSelection(id);
    }
*/    
    public String getElementAsText(String id) throws BadLocationException {
        synchronized( getTransactionMonitor()) {
            updateModel();
            try {
                m_model.readLock();
                DocumentElement de = getElementById(id);
                if (de != null) {
                    BaseDocument doc = getDoc();
                    int startOffset = de.getStartOffset();
                    int endOffset   = de.getEndOffset();
                    return doc.getText(startOffset, endOffset - startOffset + 1);
                } else {
                    return null;
                }
            } finally {
                m_model.readUnlock();
            }
        }
    }
    
    public String getElementId(DocumentElement de) {
        String id = m_mapping.element2id(de);
        assert id != null : "Element " + de + " could not be found!";
        return id;
    }
    
    public String createUniqueId(String prefix, boolean isWrapper) {
        return m_mapping.generateId(prefix, isWrapper, null);
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

    public int firstIndexOf( String str) {
        CharSeq content = getDoc().getText();
        int charNum = content.length();
        int strLen  = str.length();
        if (strLen > 0) {
            int j = 0;
            for (int i = 0; i < charNum; i++) {
                if ( content.charAt(i) == str.charAt(j)) {
                    if ( ++j == strLen) {
                        return i - j + 1;
                    } 
                } else {
                    j = 0;
                }
            }
        }
        return -1;
    }
    
    public int [] getPositionByOffset(int offset) {
        Element root  = getDoc().getDefaultRootElement();
        int lineCount = root.getElementCount();
        for (int i = 0; i < lineCount; i++) {
            Element el = root.getElement(i);
            if ( el.getEndOffset() > offset) {
                assert  el.getStartOffset() <= offset;
                return new int[] { i+1, offset - el.getStartOffset() + 1};
            }
        }
        return null;
    }
    
    public int getOffsetByPosition(int line, int column) {
        Element root = getDoc().getDefaultRootElement();
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
                sb.append( "=\""); //NOI18N
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
    
    private List<DocumentElement> collectFragmentsToDelete(CharSequence chars, DocumentElement de, List<DocumentElement> toDelete, List<String> removedIds) {
        //List<String> newIds = new ArrayList<String>();
        assert( !toDelete.contains(de));
        toDelete.add(de);
        List<String> deletedIds = collectIds(de, new ArrayList<String>());
        if (removedIds != null) {
            removedIds.addAll(deletedIds);
        }
        DocumentElement root = m_model.getRootElement();
        List<ChangeDescriptor> references = new ArrayList<ChangeDescriptor>();
        
        for ( String id: deletedIds) {
            resolveChanges(chars, root, id, id, references);
        }
        
        //List<DocumentElement> refElems = new ArrayList<DocumentElement>();
        for (ChangeDescriptor reference : references) {
            DocumentElement elem = reference.m_elem;
            assert elem != null;
            if ( !toDelete.contains(elem)) {
                collectFragmentsToDelete( chars, elem, toDelete, null);
            }
        }
        
        /*
        if (!refElems.isEmpty()) {
            for ( Iterator idIter = refElems.iterator(); idIter.hasNext(); ) {
                DocumentElement dee = (DocumentElement) idIter.next();
                if ( toDelete.contains(dee)) {
                    System.out.println("Hej");
                }
                assert( !toDelete.contains(dee));
            }
        } */ 
        return toDelete;
    }
    
    public void deleteElement(final String id, final TransactionCommand cmd)  {
        runTransaction(new FileModelTransaction() {
            protected void transaction() throws BadLocationException {
                final DocumentElement de = checkIntegrity(id);
                BaseDocument      doc   = getDoc();
                List<String> deletedIds = new ArrayList<String>();
                CharSequence      chars = (CharSequence) doc.getProperty(CharSequence.class);
                List<DocumentElement> elemsToDelete = collectFragmentsToDelete(
                        chars, de, new ArrayList<DocumentElement>(), deletedIds);
                
                main_loop: for (int i = elemsToDelete.size() - 1; i >= 0; i--) {
                    DocumentElement di = elemsToDelete.get(i);
                    int start = di.getStartOffset();
                    int end   = di.getEndOffset();
                    
                    for ( int j = elemsToDelete.size() - 1; j >= 0; j--) {
                        if ( j != i) {
                            DocumentElement dj = elemsToDelete.get(j);
                            if ( dj.getStartOffset() <= start &&
                                 dj.getEndOffset() >= end) {
                                 //System.out.println("Droping contained element " + di);
                                 elemsToDelete.remove(i);
                                 continue main_loop;
                            }
                        }
                    }
                }
                
                if ( elemsToDelete.size() > 1) {
                    // some references found; is it OK to delete?
                    StringBuilder sb = new StringBuilder();
                    int count = 0;
                    for ( String deletedId : deletedIds) {
                        sb.append('\t');
                        if ( ++count > 10) {
                            sb.append("...\n");
                            break;
                        } else {
                            sb.append(deletedId);
                            sb.append( '\n');
                        }
                    }
                    Object response = DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(
                        NbBundle.getMessage(SVGFileModel.class, "WARNING_IDReferences", sb.toString()),  //NOI18N
                        NotifyDescriptor.YES_NO_CANCEL_OPTION,
                        NotifyDescriptor.Confirmation.WARNING_MESSAGE
                    ));
                    if ( response == NotifyDescriptor.NO_OPTION) {
                        Collections.sort(elemsToDelete,new Comparator() {
                            public int compare(Object o1, Object o2) {
                                return ((DocumentElement)o2).getStartOffset() - ((DocumentElement)o1).getStartOffset();
                            }
                        });
                    } else if ( response == NotifyDescriptor.YES_OPTION) {
                        elemsToDelete.clear();
                        elemsToDelete.add(de);
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                SVGSourceMultiViewElement.selectPosition(getDataObject(), de.getStartOffset(), true);
                            }
                        });
                    } else if ( response == NotifyDescriptor.CANCEL_OPTION ||
                                response == NotifyDescriptor.CLOSED_OPTION) {
                        return;
                    } else {
                        assert false : "Unknown option: " + response;
                    }
                }
                int lastStartOff = Integer.MAX_VALUE;
                
                List<String> idsToDelete = new ArrayList<String>(elemsToDelete.size());
                
                for (DocumentElement elemToDelete : elemsToDelete) {
                    int startOff = elemToDelete.getStartOffset();
                    int endOff   = elemToDelete.getEndOffset();
                    
                    assert startOff >= 0;
                    assert endOff > startOff;
                    assert endOff <= lastStartOff;
                    lastStartOff = startOff;
                    
                    String id = getIdAttribute(elemToDelete);
                    if ( id == null) {
                        id = getElementId(elemToDelete);
                    }
                    if (id != null) {
                        idsToDelete.add(id);
                    }
                    //System.out.println("Removing DE: " + elemToDelete);
                    //doc.remove(startOff, endOff - startOff + 1);
                    removeFragment( doc, chars, startOff, endOff);
                }
                
                if ( !idsToDelete.isEmpty()) {
                    cmd.execute(idsToDelete);
                }
            }
        });
    }

    private static void removeFragment( BaseDocument doc, CharSequence chars, int startOff, int endOff) throws BadLocationException {
        char c;
        int origStart = startOff;
        int origEnd   = endOff;
        boolean newLineFound = false;
        
        while( startOff > 0 && (c=chars.charAt(startOff-1)) <= ' ') {
            if ( c == '\n') {
                newLineFound = true;
            }
            startOff--;
        }     
        int length = chars.length() - 1;
        while( endOff < length && (c=chars.charAt(endOff+1)) <= ' ') {
            if ( c == '\n') {
                newLineFound = true;
            }
            endOff++;
        } 
        
        int i = startOff;
        int j = endOff;
        
        while (newLineFound) {
            if ( i < origStart) {
                if ( chars.charAt(i++) == '\n') {
                    startOff = i;
                    break;
                }
            }
            if ( j > origEnd) {
                if ( chars.charAt(j--) == '\n') {
                    endOff = j;
                    break;
                }
            }
        }
        doc.remove(startOff, endOff - startOff + 1);
    }
    
    public void appendElement( final String insertString) {
        runTransaction(new FileModelTransaction(true) {
            protected void transaction() throws BadLocationException {
                DocumentElement svgRoot = getSVGRoot(m_model);

                if (svgRoot != null) {
                    BaseDocument doc = getDoc();
                    DocumentElement lastChild = getLastTagChild(svgRoot);
                    int    insertPosition;
                    if (lastChild != null) {
                        //insert new text after last child
                        insertPosition = lastChild.getEndOffset()+1;
                        doc.insertString(insertPosition, insertString, null);
                    } else {
                        String docText  = doc.getText(0, doc.getLength());
                        int    startOff = svgRoot.getStartOffset();
                        int    c = 0;
                        
                        insertPosition = svgRoot.getEndOffset() - 1;
                        while( insertPosition > startOff &&
                                (c=docText.charAt(insertPosition--)) != '/') {}
                        
                        if (c == '/') {
                            if (docText.charAt(insertPosition) == '<') {
                                doc.insertString(insertPosition, insertString, null);
                            } else {
                                StringBuilder sb = new StringBuilder( docText.substring(startOff, insertPosition+1));
                                sb.append(">");  //NOI18N
                                sb.append(insertString);
                                sb.append("\n</svg>"); //NOI18N
                                doc.replace(startOff, svgRoot.getEndOffset() - startOff + 1, sb.toString(), null);
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
    
    private static String getElemTextWithId( BaseDocument doc, DocumentElement de, String id) throws BadLocationException {
        String elemText;
        int    startOffset = de.getStartOffset();
        
        if (de.getAttributes().isDefined( SVGConstants.SVG_ID_ATTRIBUTE)) {
            elemText = doc.getText(startOffset, de.getEndOffset() - startOffset + 1);
        } else {
            String tag = de.getName();
            startOffset += 1 + tag.length();  
            StringBuilder sb = new StringBuilder("<");
            sb.append(tag);
            sb.append( ' ');
            injectId( sb, id);
            sb.append( doc.getText(startOffset, de.getEndOffset() - startOffset + 1));
            elemText = sb.toString();
        }
        return elemText;
    }
    
    private static void injectId( StringBuilder sb, String id) {
        sb.append( SVGConstants.SVG_ID_ATTRIBUTE);
        sb.append("=\""); //NOI18N
        sb.append( id);
        sb.append( "\" "); //NOI18N
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
                boolean injectId = !elem.getAttributes().isDefined( SVGConstants.SVG_ID_ATTRIBUTE);
                                    
                BaseDocument doc      = getDoc();
                String       fragment = doc.getText(startOff, endOff - startOff + 1);
                int p;
                if ( (p=fragment.indexOf(attrName)) != -1) {
                    p += attrName.length();
                    while( ++p < fragment.length()) {
                        if (fragment.charAt(p) =='"') {
                            int q = p;

                            while( ++q < fragment.length()) {
                                if (fragment.charAt(q) =='"') {
                                    p++;
                                    if ( injectId) {
                                        StringBuilder sb = new StringBuilder(attrValue);
                                        sb.append("\" ");
                                        injectId(sb, id);
                                        doc.replace(startOff + p, q-p + 1, sb.toString(), null);
                                    } else {
                                        doc.replace(startOff + p, q-p , attrValue, null);
                                    }
                                    return;
                                }
                            }
                        }
                    }
                    //TODO
                    System.err.println("Attribute " + attrName + " not changed: \"" + fragment + "\"");
                } else {
                    StringBuilder sb = new StringBuilder(" ");
                    if (injectId) {
                        injectId(sb, id);
                    }
                    sb.append(attrName);
                    sb.append("=\""); //NOI18N
                    sb.append(attrValue);
                    sb.append( "\" "); //NOI18N
                    doc.insertString(startOff, sb.toString(), null);
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
                    BaseDocument doc = getDoc();
                    int    startOffset = de.getStartOffset();
                    int    length      = de.getEndOffset() - startOffset + 1;
                    //String elemText    = getElementText(de, id);
                    String elemText = getElemTextWithId(doc, de, id);

                    int insertOffset = firstChild.getStartOffset();
                    assert insertOffset < startOffset : "Offset overlap #1" + insertOffset + "," + startOffset;

                    doc.remove(startOffset, length);
                    doc.insertString(insertOffset, elemText, null);
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
                    BaseDocument doc         = getDoc();
                    int          startOffset = de.getStartOffset();
                    int          length      = de.getEndOffset() - startOffset + 1;
                    //String       elemText    = getElementText(de, id);
                    String elemText = getElemTextWithId(doc, de, id);

                    int insertOffset = lastChild.getEndOffset() + 1;
                    assert startOffset < insertOffset : "Offset overlap #2" + insertOffset + "," + startOffset;

                    doc.insertString(insertOffset, elemText, null);
                    doc.remove(startOffset, length);
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
                    BaseDocument doc         = getDoc();
                    int          startOffset = de.getStartOffset();
                    int          length      = de.getEndOffset() - startOffset + 1;
                    //String       elemText    = doc.getText(startOffset, length);
                    String elemText = getElemTextWithId(doc, de, id);

                    int insertOffset = nextChild.getEndOffset() + 1;
                    assert startOffset < insertOffset : "Offset overlap #3" + insertOffset + "," + startOffset;

                    doc.insertString(insertOffset, elemText, null);
                    doc.remove(startOffset, length);
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
                    BaseDocument doc         = getDoc();
                    
                    int          startOffset = de.getStartOffset();
                    int          length      = de.getEndOffset() - startOffset + 1;
                    //String       elemText    = doc.getText(startOffset, length);

                    String elemText = getElemTextWithId(doc, de, id);
                    int insertOffset = previousChild.getStartOffset();
                    assert startOffset > insertOffset : "Offset overlap #4" + insertOffset + "," + startOffset;

                    doc.remove(startOffset, length);
                    doc.insertString(insertOffset, elemText, null);
                }
            }
        });
    }
    
    public static DocumentElement getSVGRoot(final DocumentModel model) {
        DocumentElement root = model.getRootElement();
        for (DocumentElement de : root.getChildren()) {
            if (isTagElement(de) && "svg".equals(de.getName())) {  //NOI18N
                return de;
            }
        }
        return null;
    }
    
    protected synchronized void fireModelChange() {
        if ( !m_eventInProgress) {
            //System.out.println("Asking for update");
            SwingUtilities.invokeLater( updateTask);
        }
    }
        
    public void runTransaction( final FileModelTransaction transaction) {
        new Thread("TransactionWrapper") {  //NOI18N
            public void run() {
                transaction.run();
                //SwingUtilities.invokeLater(transaction);
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
        checkModel();
        assert SwingUtilities.isEventDispatchThread() == false : "Model update cannot be called in AWT thread.";
        
        synchronized(m_lock) {
            if (m_sourceChanged) {
                //System.out.println("Forcing model update");
                m_model.forceUpdate();
            } else if (!m_updateInProgress) {
                //System.out.println("Model already up to date.");
                return;
            } 
            
            while( m_sourceChanged || m_updateInProgress) {
                //System.out.print( " Waiting for model update...");
                try {
                    m_lock.wait();
                    //System.out.println( " Wait ended."); 
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }            
        }    
        //System.out.println("Model update completed.");
    }
            
    public String mergeImage(File file) throws FileNotFoundException, IOException, DocumentModelException, BadLocationException {
        //System.out.println("Loading document from file...");
        DocumentModel   modelToInsert = loadDocumentModel(file);
        //System.out.println("Document loaded.");
        return mergeImage(modelToInsert, file.getName(), true, true);
    } 
    
    public String mergeImage(String str, boolean wrap) throws IOException, DocumentModelException, BadLocationException {
        StringBufferInputStream in = new StringBufferInputStream(str);
        return mergeImage(loadDocumentModel(in), "", false, wrap);
    }

    protected String mergeImage( DocumentModel docModel, String name, boolean isSvgRoot, boolean wrap) throws BadLocationException {
        String wrapperId    = null;
        String textToInsert = null;
        
        if (wrap) {
            wrapperId    = createUniqueId(name.replace('.', '_'), true);
            textToInsert = m_mapping.getWithUniqueIds(docModel, wrapperId, isSvgRoot, null);
            textToInsert = wrapText( wrapperId, textToInsert);
        } else {
            String [] rootId = new String[1];
            textToInsert = m_mapping.getWithUniqueIds(docModel, wrapperId, isSvgRoot, rootId);
            wrapperId = rootId[0];
        }
        //System.out.println("Appending element ...");
        appendElement(textToInsert);
        //System.out.println("Element appended");
        return wrapperId;
    }
    
    protected static String wrapText(String wrapperId, String textToWrap) {
        //TODO indent the wrapped text
        StringBuilder sb = new StringBuilder();
        sb.append( "<g id=\""); //NOI18N
        sb.append(wrapperId);
        sb.append("\">\n"); //NOI18N
        sb.append(textToWrap);
        sb.append("\n</g>"); //NOI18N
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
    
    public static class ChangeDescriptor implements Comparable {
        private final int             m_startOffset;
        private final int             m_length;
        private final String          m_newValue;
        private final DocumentElement m_elem;
        
        public ChangeDescriptor( int startOffset, int length, String newValue,
                DocumentElement elem) {
            m_startOffset  = startOffset;
            m_length       = length;
            m_newValue     = newValue;
            m_elem         = elem;
        }

        public ChangeDescriptor( int startOffset, int length, String newValue) {
            this( startOffset, length, newValue, null);
        }
        
        public void replace(StringBuilder sb) {
            sb.replace( m_startOffset, m_startOffset + m_length, m_newValue);
        }

        public int compareTo(Object o) {
            return ((ChangeDescriptor) o).m_startOffset - m_startOffset;
        }
    }
    
    private static boolean isElementIdChar(char c, boolean isTrailing) {
        return (!isTrailing || c != '.') && PerseusController.isElementIdChar(c);
    }
    
    private static int indexOf(CharSequence chars, String str, int from, int to) {
        int index = from;
        int strLen = str.length();
        if ( strLen > 0) {
            main_loop: while( index < to) {
                int i = 0;
                while( chars.charAt(index++) == str.charAt(i++)) {
                    if (i == strLen) {
                        return index - i;
                    } else if (index > to) {
                        break main_loop;
                    }
                }
            }
        } 
        return -1;
    }
    
    public static void resolveChanges( CharSequence sb, DocumentElement elem, String oldId,
        String newId, List<ChangeDescriptor> changes) {

        //TODO implement faster and more robust id replacement
        AttributeSet          attrs    = elem.getAttributes();
        List<DocumentElement> children = elem.getChildren();
        
        for ( Enumeration attrNames = attrs.getAttributeNames(); attrNames.hasMoreElements(); ) {
            String name  = (String) attrNames.nextElement();
            String value = (String) attrs.getAttribute(name);
            
            if ( value != null && value.length() > 0) {
                int p;
                if ((p=value.indexOf(oldId)) != -1) {
                    int q;
                    if ( (p==0 || !isElementIdChar( value.charAt(p-1), false)) &&
                         ((q=p+oldId.length()) >= value.length() || !isElementIdChar( value.charAt(q), true))) {                        
                        int startOff  = elem.getStartOffset();
                        int endOff = children.isEmpty() ? elem.getEndOffset() : children.get(0).getStartOffset();
                        
                        if ( (q=indexOf(sb, name, startOff, endOff)) != -1) {
                            if ( (q=indexOf(sb, oldId, q + name.length(), endOff)) != -1) {
                                changes.add( new ChangeDescriptor(q, oldId.length(), newId, elem));
                                continue;
                            }
                        }
                             
                        System.err.println("Attribute value " + value + " not found at the DE " + elem);
                    }
                }                
            }
        }
        
        for ( DocumentElement de : children) {
            if (SVGFileModel.isTagElement(de)) {                
                resolveChanges(sb, de, oldId, newId, changes);
            }
        }            
    }
    
    private static List<String> collectIds( DocumentElement elem, List<String> ids) {
        String id = getIdAttribute(elem);
        
        if ( id != null) {
            ids.add(id);
        }
        List<DocumentElement> children = elem.getChildren();
        for ( DocumentElement de : children) {
            if (isTagElement(de)) {                
                collectIds(de, ids);
            }
        }   
        return ids;
    }
    
    public static DocumentModel loadDocumentModel( File file) throws FileNotFoundException, IOException, DocumentModelException {
        InputStream in = null;
        
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
    /*
    private String _getElementText( DocumentElement de, String id) throws BadLocationException {
        int    startOffset = de.getStartOffset();
        int    length      = de.getEndOffset() - startOffset + 1;

        String elemText = getDoc().getText(startOffset, length);

        if (id != null && !de.getAttributes().isDefined("id")) {
            StringBuilder sb = new StringBuilder(elemText);
            sb.insert(de.getName().length() + 1, " id=\"" + id + "\""); //NOI18N
            elemText = sb.toString();
        }
        
        return elemText;
    }
    */
    
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
        checkModel();
        assert de != null;
        assert de.getDocument() == getDoc() : "Element is not part of the current document";
        assert de.getDocumentModel() == m_model : "Element is not part of the current document model";
        if ( isTagElement(de) && de.getEndOffset() <= de.getStartOffset()) {
            System.out.println("ERROR: Empy tag element: " + de);
        }
    }    
    
    private DocumentElement checkIntegrity(String id) {
        DocumentElement de = getElementById(id);
        assert de != null : "No element with id: " + id;
        checkIntegrity(de);
        return de;
    }    
}
