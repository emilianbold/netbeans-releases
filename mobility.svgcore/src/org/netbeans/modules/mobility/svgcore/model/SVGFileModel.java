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
 */package org.netbeans.modules.mobility.svgcore.model;

import com.sun.perseus.util.SVGConstants;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javax.microedition.m2g.SVGImage;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
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
import org.netbeans.modules.mobility.svgcore.view.source.SVGSourceMultiViewElement;
import org.netbeans.modules.xml.multiview.XmlMultiViewEditorSupport;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Pavel Benes
 */
public final class SVGFileModel {
    protected static final int      MAX_TOOLTIP_ATTR_SIZE = 100;
    protected static final String   XML_TAG            = "tag"; //NOI18N
    protected static final String   XML_EMPTY_TAG      = "empty_tag"; //NOI18N
    protected static final String   XML_ERROR_TAG      = "error"; //NOI18N
    protected static final String[] ANIMATION_TAGS     = {"animate", "animateTransform", "animateMotion", "animateColor", "set"}; //NOI18N
    protected static final String   TRANSACTION_TOKEN  = "transaction"; //NOI18N
    protected static final String   MODEL_UPDATE_TOKEN = "update"; //NOI18N

    public interface ModelListener {
        public void modelChanged();
    }

    public interface TransactionCommand {
        public Object execute(Object userData);
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
            SceneManager.log(Level.FINE, "Starting transaction..."); //NOI18N
            if (incrementTransactionCounter() == 1) {
                getSceneManager().setBusyState(TRANSACTION_TOKEN, true);
            }

            synchronized (getTransactionMonitor()) {
                SceneManager.log(Level.FINE, "Transaction started."); //NOI18N
                try {
                    updateModel();
                    //assert SwingUtilities.isEventDispatchThread() : "Transaction must be called in AWT thread.";
                    getDoc().getFormatter().reformatLock();
                    getDoc().atomicLock();
                    //checkModel();
                    m_model.readLock();
                    transaction();
                } catch (Exception e) {
                    SceneManager.error("Transaction failed.", e); //NOI18N
                    getDoc().atomicUndo();
                } finally {
                    m_model.readUnlock();
                    getDoc().atomicUnlock();
                    getDoc().getFormatter().reformatUnlock();
                    SceneManager.log(Level.FINE, "Transaction completed."); //NOI18N
                    if (decrementTransactionCounter() == 0) {
                        getSceneManager().setBusyState(TRANSACTION_TOKEN, false);
                    }
                }
            }

            if (m_fireUpdate) {
                getDataObject().fireContentChanged();
            } else {
                m_model.forceUpdate();
            }
        }

        protected abstract void transaction() throws Exception;
    }
        
    private final XmlMultiViewEditorSupport m_edSup;
    private final ElementMapping      m_mapping;
    private final List<ModelListener> m_modelListeners = new ArrayList<ModelListener>();
    private final Object              m_lock = new Object();
    private final Object              m_transactionLock = new Object();
    private volatile BaseDocument     m_bDoc;
    private DocumentModel             m_model;
    private boolean                   m_isChanged = true;
    private volatile boolean          m_eventInProgress = false;
    private volatile boolean          m_sourceChanged = false;
    private volatile boolean          m_updateInProgress = false;
    private volatile boolean          m_updateInProcess = false;
    private volatile int[]            m_transactionCounter = new int[]{0};
    private final DocumentListener    m_docListener = new DocumentListener() {
        public void removeUpdate(DocumentEvent e) {
            documentModified(e);
        }

        public void insertUpdate(DocumentEvent e) {
            documentModified(e);
        }

        public void changedUpdate(DocumentEvent e) {
            if ( e.getLength() > 0 ||
                 e.getOffset() > 0 ||
                 e.getType() != DocumentEvent.EventType.CHANGE) {
                documentModified(e);
            }
        }
    };
    
    private final DocumentModelListener m_modelListener = new DocumentModelListener() {
        public void documentElementRemoved(DocumentElement de) {
            if (isTagElement(de)) {
                if ( SceneManager.isEnabled(Level.FINEST)) {
                    SceneManager.log(Level.FINEST, "Element removed: " +de); //NOI18N
                }
                m_mapping.remove(de);
                fireModelChange();
            }
        }

        public void documentElementChanged(DocumentElement de) {
            if (isTagElement(de)) {
                if ( SceneManager.isEnabled(Level.FINEST)) {
                    SceneManager.log(Level.FINEST, "Element changed: " +de); //NOI18N
                }
                fireModelChange();
            }
        }

        public void documentElementAttributesChanged(DocumentElement de) {
            if (isTagElement(de)) {
                if ( SceneManager.isEnabled(Level.FINEST)) {
                    SceneManager.log(Level.FINEST, "Element attr changed: " +de); //NOI18N
                }
                fireModelChange();
            }
        }

        public void documentElementAdded(DocumentElement de) {
            if (isTagElement(de)) {
                if ( SceneManager.isEnabled(Level.FINEST)) {
                    SceneManager.log(Level.FINEST, "Element added: " + de); //NOI18N
                }
                m_mapping.add(de);
                fireModelChange();
            }
        }
    };
    
    private final DocumentModelStateListener m_modelStateListener = new DocumentModelStateListener() {
        public void sourceChanged() {
            synchronized (m_lock) {
                SceneManager.log(Level.FINER, "Document source changed."); //NOI18N
                m_sourceChanged = true;
                m_lock.notifyAll();
            }
        }

        public void scanningStarted() {
            synchronized (m_lock) {
                SceneManager.log(Level.FINER, "Document scanning started."); //NOI18N
                getSceneManager().setBusyState(MODEL_UPDATE_TOKEN, true);
                m_updateInProgress = true;
                m_sourceChanged = false;
                m_lock.notifyAll();
            }
        }

        public void updateStarted() {
            SceneManager.log(Level.FINER, "Model update started."); //NOI18N
        }

        public void updateFinished() {
            synchronized (m_lock) {
                SceneManager.log(Level.FINER, "Model update finished."); //NOI18N
                m_updateInProgress = false;
                m_lock.notifyAll();
                getSceneManager().setBusyState(MODEL_UPDATE_TOKEN, false);
            }
        }
    };
    private final Runnable m_updateTask = new Runnable() {
        public void run() {
            SceneManager.log(Level.FINER, "Update task started."); //NOI18N
            try {
                synchronized (m_modelListeners) {
                    for (int i = 0; i < m_modelListeners.size(); i++) {
                        (m_modelListeners.get(i)).modelChanged();
                    }
                }
            } finally {
                SceneManager.log(Level.FINER, "Update task finished."); //NOI18N
                m_eventInProgress = false;
            }
        }
    };

    /** Creates a new instance of SVGFileModel */
    public SVGFileModel(XmlMultiViewEditorSupport edSup) {
        m_edSup = edSup;
        m_model = null;
        m_mapping = new ElementMapping(this);
    }

    public synchronized void attachToOpenedDocument() {
        assert SwingUtilities.isEventDispatchThread() : "Model initialisation must be called from AWT thread!"; //NOI18N
        if (m_bDoc == null) {
            m_bDoc = getOpenedDoc();
            SceneManager.log(Level.INFO, "Using already opened document."); //NOI18N
            assert m_bDoc != null;
            m_bDoc.addDocumentListener(m_docListener);
        } else {
            assert m_bDoc == getOpenedDoc() : "Model mismatch"; //NOI18N
        }
    }

    public synchronized void detachDocument() {
        m_model = null;
        m_bDoc  = null;
        m_modelListeners.clear();
        SceneManager.log(Level.INFO, "Removing the document."); //NOI18N
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
        synchronized (m_transactionCounter) {
            m_transactionCounter[0]++;
            return m_transactionCounter[0];
        }
    }

    private int decrementTransactionCounter() {
        synchronized (m_transactionCounter) {
            m_transactionCounter[0]--;
            return m_transactionCounter[0];
        }
    }

    private int getTransactionCounter() {
        synchronized (m_transactionCounter) {
            return m_transactionCounter[0];
        }
    }

    private synchronized BaseDocument getDoc() {
        if (m_bDoc == null) {
            try {
                SceneManager.log(Level.INFO, "Opening new document."); //NOI18N
                m_bDoc = (BaseDocument) m_edSup.openDocument();
                assert m_bDoc != null;
                m_bDoc.addDocumentListener(m_docListener);
            } catch (IOException ex) {
                SceneManager.error("Could not open the document", ex); //NOI18N
            }
        }
        return m_bDoc;
    }

    private synchronized void checkModel() {
        if (m_model == null) {
            try {
                m_model = DocumentModel.getDocumentModel(getDoc());
                m_model.addDocumentModelListener(m_modelListener);
                m_model.addDocumentModelStateListener(m_modelStateListener);
            } catch (DocumentModelException ex) {
                SceneManager.error("Could not obtain document model", ex); //NOI18N
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
 
    private boolean verifyModel(DocumentElement de) {
        List<DocumentElement> children = de.getChildren();

        for (DocumentElement d : children) {
            if (isTagElement(d) && d.getStartOffset() >= d.getEndOffset()) {
                System.out.println("ERROR: Invalid element offset: " + d);
                return true;
            }
            if (d.getParentElement() != de) {
                System.out.println("ERROR: Invalid parent relation: " + d);
                return true;
            }
            if (verifyModel(d)) {
                return true;
            }
        }
        return false;
    }
*/
    
    public SVGDataObject getDataObject() {
        return (SVGDataObject) m_edSup.getDataObject();
    }

    public SVGImage parseSVGImage() 
            throws IOException, BadLocationException, InterruptedException 
    {
        SceneManager.log(Level.INFO, "Parsing image..."); //NOI18N
        checkModel();
        SVGImage svgImage = m_mapping.parseDocument(true);
        SceneManager.log(Level.INFO, "Image parsed."); //NOI18N
        return svgImage;
    }

    public boolean isChanged() {
        return m_isChanged;
    }

    public void setChanged(boolean isChanged) {
        m_isChanged = isChanged;
    }

    public void addModelListener(ModelListener listener) {
        synchronized (m_modelListeners) {
            m_modelListeners.add(listener);
        }
    }

    public void removeModelListener(ModelListener listener) {
        synchronized (m_modelListeners) {
            m_modelListeners.remove(listener);
        }
    }

    protected void documentModified(DocumentEvent e) {
        if (e instanceof BaseDocumentEvent) {
            BaseDocumentEvent bde = (BaseDocumentEvent) e;
            if (bde.isInRedo() || bde.isInUndo() || getTransactionCounter() == 0) {
                synchronized (this) {
                    if (!m_updateInProcess) {
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
        return elem != null && (elem.getType().equals(XML_TAG) || elem.getType().equals(XML_EMPTY_TAG));
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

    public static boolean isHiddenElement(DocumentElement de) {
        AttributeSet attrs = de.getAttributes();
        String visible = (String) attrs.getAttribute(SVGConstants.SVG_VISIBILITY_ATTRIBUTE);
        if (visible != null && visible.equals(SVGConstants.CSS_HIDDEN_VALUE)) {
            return true;
        }
        return false;
    }

    /**
     * Convenience helper method.
     */
    public static List<DocumentElement> getParents(DocumentElement elem) {
        List<DocumentElement> list = new ArrayList<DocumentElement>();
        while (elem != null) {
            list.add(elem);
            elem = elem.getParentElement();
        }
        return list;
    }

    public DocumentElement getElementById(String id) {
        //assert !SwingUtilities.isEventDispatchThread() : "getElementID cannot be called from AWT thread";
        checkModel();
        DocumentElement elem = m_mapping.id2element(id);
        return elem;
    }

    public String getElementAsText(String id) throws BadLocationException {
        synchronized (getTransactionMonitor()) {
            updateModel();
            try {
                m_model.readLock();
                DocumentElement de = getElementById(id);
                if (de != null) {
                    BaseDocument doc = getDoc();
                    int startOffset = de.getStartOffset();
                    int endOffset = de.getEndOffset();
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
        //assert id != null : "Element " + de + " could not be found!"; //NOI18N
        //Warning if id == null, it could happen when Navigator is not updated fast enough.
        if (id == null) {
            id = ""; //NOI18N
            System.out.println("Element " + de + " could not be found!"); //NOI18N
        }
        return id;
    }

    public String createUniqueId(String prefix, boolean isWrapper) {
        return createUniqueId(prefix, isWrapper, null);
    }

    public String createUniqueId(String prefix, boolean isWrapper, Set<String> extIds) {
        return m_mapping.generateId(prefix, isWrapper, extIds);
    }

    public static boolean isWrapperId(String id) {
        return ElementMapping.isWrapperId(id);
    }

    public synchronized String describeElement(String id) {
        DocumentElement de = getElementById(id);
        if (de != null) {
            checkIntegrity(de);
            return describeElement(de);
        } else {
            return "";
        }
    }

    public int firstIndexOf(String str) {
        CharSeq content = getDoc().getText();
        int charNum = content.length();
        int strLen = str.length();
        if (strLen > 0) {
            int j = 0;
            for (int i = 0; i < charNum; i++) {
                if (content.charAt(i) == str.charAt(j)) {
                    if (++j == strLen) {
                        return i - j + 1;
                    }
                } else {
                    j = 0;
                }
            }
        }
        return -1;
    }

    public int[] getPositionByOffset(int offset) {
        Element root = getDoc().getDefaultRootElement();
        int lineCount = root.getElementCount();
        for (int i = 0; i < lineCount; i++) {
            Element el = root.getElement(i);
            if (el.getEndOffset() > offset) {
                assert el.getStartOffset() <= offset;
                return new int[]{i + 1, offset - el.getStartOffset() + 1};
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

    protected static String describeElement(DocumentElement el) {
        StringBuilder sb = new StringBuilder();

        AttributeSet attrs = el.getAttributes();
        for (Enumeration names = attrs.getAttributeNames(); names.hasMoreElements();) {
            String attrName = (String) names.nextElement();
            sb.append("&nbsp;"); //NOI18N
            sb.append(attrName);
            sb.append("=\""); //NOI18N
            Object o = attrs.getAttribute(attrName);
            if ( o != null) {
                String value = o.toString();
                if (value.length() > MAX_TOOLTIP_ATTR_SIZE) {
                    sb.append(value.substring(0, MAX_TOOLTIP_ATTR_SIZE));
                    sb.append("..."); //NOI18N
                } else {
                    sb.append(value);
                }
            }
            sb.append('"');
            sb.append("&nbsp;<br>"); //NOI18N
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
        assert (!toDelete.contains(de));
        toDelete.add(de);
        List<String> deletedIds = collectIds(de, new ArrayList<String>());
        if (removedIds != null) {
            removedIds.addAll(deletedIds);
        }
        DocumentElement root = m_model.getRootElement();
        List<ChangeDescriptor> references = new ArrayList<ChangeDescriptor>();

        for (String id : deletedIds) {
            resolveChanges(chars, root, id, id, references);
        }

        for (ChangeDescriptor reference : references) {
            DocumentElement elem = reference.m_elem;
            assert elem != null;
            if (!toDelete.contains(elem)) {
                collectFragmentsToDelete(chars, elem, toDelete, null);
            }
        }
        return toDelete;
    }

    public void deleteElement(final String id, final TransactionCommand cmd) {
        runTransaction(new FileModelTransaction() {
            @SuppressWarnings("unchecked")
            protected void transaction() throws BadLocationException {
                final DocumentElement de = checkIntegrity(id);
                BaseDocument doc = getDoc();
                List<String> deletedIds = new ArrayList<String>();
                CharSequence chars = (CharSequence) doc.getProperty(CharSequence.class);
                List<DocumentElement> elemsToDelete = collectFragmentsToDelete(chars, de, new ArrayList<DocumentElement>(), deletedIds);

                main_loop:
                for (int i = elemsToDelete.size() - 1; i >= 0; i--) {
                    DocumentElement di = elemsToDelete.get(i);
                    int start = di.getStartOffset();
                    int end = di.getEndOffset();

                    for (int j = elemsToDelete.size() - 1; j >= 0; j--) {
                        if (j != i) {
                            DocumentElement dj = elemsToDelete.get(j);
                            if (dj.getStartOffset() <= start && dj.getEndOffset() >= end) {
                                elemsToDelete.remove(i);
                                continue main_loop;
                            }
                        }
                    }
                }

                if (elemsToDelete.size() > 1) {
                    // some references found; is it OK to delete?
                    StringBuilder sb = new StringBuilder();
                    int count = 0;
                    for (String deletedId : deletedIds) {
                        sb.append('\t');
                        if (++count > 10) {
                            sb.append("...\n"); //NOI18N
                            break;
                        } else {
                            sb.append(deletedId);
                            sb.append('\n');
                        }
                    }
                    Object response = DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(NbBundle.getMessage(SVGFileModel.class, "WARNING_IDReferences", sb.toString()), NotifyDescriptor.YES_NO_CANCEL_OPTION, NotifyDescriptor.Confirmation.WARNING_MESSAGE));
                    if (response == NotifyDescriptor.NO_OPTION) {
                        Collections.sort(elemsToDelete, new Comparator() {
                            public int compare(Object o1, Object o2) {
                                return ((DocumentElement) o2).getStartOffset() - ((DocumentElement) o1).getStartOffset();
                            }
                        });
                    } else if (response == NotifyDescriptor.YES_OPTION) {
                        elemsToDelete.clear();
                        elemsToDelete.add(de);
                        SwingUtilities.invokeLater(new Runnable() {

                            public void run() {
                                SVGSourceMultiViewElement.selectPosition(getDataObject(), de.getStartOffset(), true);
                            }
                        });
                    } else if (response == NotifyDescriptor.CANCEL_OPTION || response == NotifyDescriptor.CLOSED_OPTION) {
                        return;
                    } else {
                        assert false : "Unknown option: " + response; //NOI18N
                    }
                }
                int lastStartOff = Integer.MAX_VALUE;

                List<String> idsToDelete = new ArrayList<String>(elemsToDelete.size());

                for (DocumentElement elemToDelete : elemsToDelete) {
                    int startOff = elemToDelete.getStartOffset();
                    int endOff = elemToDelete.getEndOffset();

                    assert startOff >= 0;
                    assert endOff > startOff;
                    assert endOff <= lastStartOff;
                    lastStartOff = startOff;

                    String id = getIdAttribute(elemToDelete);
                    if (id == null) {
                        id = getElementId(elemToDelete);
                    }
                    if (id != null) {
                        idsToDelete.add(id);
                    }
                    removeFragment(doc, chars, startOff, endOff);
                }

                if (!idsToDelete.isEmpty()) {
                    cmd.execute(idsToDelete);
                }
            }
        });
    }

    private static void removeFragment(BaseDocument doc, CharSequence chars, int startOff, int endOff) throws BadLocationException {
        char c;
        int origStart = startOff;
        int origEnd = endOff;
        boolean newLineFound = false;

        while (startOff > 0 && (c = chars.charAt(startOff - 1)) <= ' ') {
            if (c == '\n') {
                newLineFound = true;
            }
            startOff--;
        }
        int length = chars.length() - 1;
        while (endOff < length && (c = chars.charAt(endOff + 1)) <= ' ') {
            if (c == '\n') {
                newLineFound = true;
            }
            endOff++;
        }

        int i = startOff;
        int j = endOff;

        while (newLineFound) {
            if (i < origStart) {
                if (chars.charAt(i++) == '\n') {
                    startOff = i;
                    break;
                }
            }
            if (j > origEnd) {
                if (chars.charAt(j--) == '\n') {
                    endOff = j;
                    break;
                }
            }
        }
        doc.remove(startOff, endOff - startOff + 1);
    }

    public void appendElement(final String insertString) {
        runTransaction(new FileModelTransaction(true) {
            protected void transaction() throws BadLocationException {
                DocumentElement svgRoot = getSVGRoot(m_model);

                if (svgRoot != null) {
                    BaseDocument doc = getDoc();
                    List<DocumentElement> children = svgRoot.getChildren();
                    DocumentElement lastChild = getLastTagChild(children);
                    int insertPosition;
                    if (lastChild != null) {
                        CharSequence chars = (CharSequence) doc.getProperty(CharSequence.class);
                        
                        //insert new text before last visible
                        lastChild = getLastVisibleTagChild(children);
                        insertPosition = lastChild.getEndOffset() + 1;
                        String str = insertString;
                        int i = insertPosition;
                        int c;
                        
                        while( i > 0) {
                            if ( (c=chars.charAt(--i)) <= ' ') {
                                if ( c == '\n') {
                                    break;
                                }
                            } else {
                                str = '\n' + str;
                                break;
                            }
                        }
                        doc.insertString(insertPosition, str, null);
                        doc.getFormatter().reformat(doc, insertPosition, insertPosition + str.length()+1);
                    } else {
                        String docText = doc.getText(0, doc.getLength());
                        int startOff = svgRoot.getStartOffset();
                        int c = 0;

                        insertPosition = svgRoot.getEndOffset() - 1;
                        while (insertPosition > startOff && (c = docText.charAt(insertPosition--)) != '/') {
                        }

                        if (c == '/') {
                            if (docText.charAt(insertPosition) == '<') {
                                doc.insertString(insertPosition, insertString, null);
                                doc.getFormatter().reformat(doc, insertPosition, insertPosition + insertString.length()+1);
                            } else {
                                StringBuilder sb = new StringBuilder(docText.substring(startOff, insertPosition + 1));
                                sb.append(">\n"); //NOI18N
                                sb.append(insertString);
                                sb.append("\n</svg>"); //NOI18N
                                doc.replace(startOff, svgRoot.getEndOffset() - startOff + 1, sb.toString(), null);
                                doc.getFormatter().reformat(doc, insertPosition, doc.getLength());
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

    private static String getElemTextWithId(BaseDocument doc, DocumentElement de, String id) throws BadLocationException {
        String elemText;
        int startOffset = de.getStartOffset();

        if (de.getAttributes().isDefined(SVGConstants.SVG_ID_ATTRIBUTE)) {
            elemText = doc.getText(startOffset, de.getEndOffset() - startOffset + 1);
        } else {
            String tag = de.getName();
            startOffset += 1 + tag.length();
            StringBuilder sb = new StringBuilder("<"); //NOI18N
            sb.append(tag);
            sb.append(' ');
            injectId(sb, id);
            sb.append(doc.getText(startOffset, de.getEndOffset() - startOffset + 1));
            elemText = sb.toString();
        }
        return elemText;
    }

    private static StringBuilder injectId(StringBuilder sb, String id) {
        sb.append(SVGConstants.SVG_ID_ATTRIBUTE);
        sb.append("=\""); //NOI18N
        sb.append(id);
        sb.append("\" "); //NOI18N
        return sb;
    }

    public void setAttributes(final String id, final String [] attributes) {
        runTransaction(new FileModelTransaction() {
            protected void transaction() throws BadLocationException {
                DocumentElement elem = checkIntegrity(id);
                assert isTagElement(elem) : "Attribute change allowed only for tag elements"; //NOI18N

                int startOff = elem.getStartOffset() + 1 + elem.getName().length();
                int endOff;

                List<DocumentElement> children = elem.getChildren();

                if (children.size() > 0) {
                    endOff = children.get(0).getStartOffset() - 1;
                } else {
                    endOff = elem.getEndOffset() - 1;
                }
                boolean injectId = !elem.getAttributes().isDefined(SVGConstants.SVG_ID_ATTRIBUTE);

                assert attributes.length % 2 == 0;
                BaseDocument doc     = getDoc();
                
                loop: for ( int i = 0; i < attributes.length; i+=2) {
                    String fragment  = doc.getText(startOff, endOff - startOff + 1);
                    String attrName  = attributes[i];
                    String attrValue = attributes[i+1];
                    
                    int p;
                    if ((p = indexOfAttr(fragment, attrName)) != -1) {
                        int start = p;
                        p += attrName.length();
                        while (++p < fragment.length()) {
                            if (fragment.charAt(p) == '"') {
                                int q = p;

                                while (++q < fragment.length()) {
                                    if (fragment.charAt(q) == '"') {
                                        p++;
                                        
                                        if ( attrValue != null) {
                                            int l;
                                            String txt;
                                            if (injectId) {
                                                StringBuilder sb = new StringBuilder(attrValue);
                                                sb.append("\" "); //NOI18N
                                                injectId(sb, id);
                                                injectId = false;
                                                l = q - p + 1;
                                                txt = sb.toString();
                                                doc.replace(startOff + p, l, txt, null);
                                            } else {
                                                l = q - p;
                                                txt = attrValue;
                                                doc.replace(startOff + p, l, txt, null);
                                            }
                                            endOff = endOff - l + txt.length();
                                        } else {
                                            int l = q - start + 1;
                                            doc.remove(startOff + start, l);
                                            endOff -= l;
                                        }
                                        continue loop;
                                    }
                                }
                            }
                        }
                        SceneManager.log(Level.SEVERE, "Attribute " + attrName + " not changed: \"" + fragment + "\""); //NOI18N
                    } else {
                        if (attrValue != null) {
                            StringBuilder sb = new StringBuilder(" "); //NOI18N
                            if (injectId) {
                                injectId(sb, id);
                                injectId = false;
                            }
                            sb.append(attrName);
                            sb.append("=\""); //NOI18N
                            sb.append(attrValue);
                            sb.append("\" "); //NOI18N
                            String txt = sb.toString();
                            doc.insertString(startOff, txt, null);
                            endOff += txt.length();
                        }
                    }
                }
            }
        });
    }
    
    private static int skipWhite(String fragment, int index) {
        while(index < fragment.length()) {
            if ( fragment.charAt(index) > ' ') {
                return index;
            } else {
                index++;
            }
        }
        return -1;
    }
    
    private static int indexOfAttr( String fragment, String attrName) {
        int index = 0;
        
        while ( (index=fragment.indexOf(attrName, index)) != -1) {
            int q = index;
            index += attrName.length();
            if ( q < 1 || fragment.charAt(q-1) == ' ') {
                int p = skipWhite( fragment, index);
                if ( p != -1 && fragment.charAt(p) == '=') {
                    p = skipWhite(fragment, p+1);
                    if (p != -1 && fragment.charAt(p) == '"') {
                        return q;
                    }
                }
            }
        }
        
        return index;
    }
    public void setAttribute(final String id, final String attrName, final String attrValue) {
        runTransaction(new FileModelTransaction() {
            protected void transaction() throws BadLocationException {
                DocumentElement elem = checkIntegrity(id);
                assert isTagElement(elem) : "Attribute change allowed only for tag elements"; //NOI18N

                int startOff = elem.getStartOffset() + 1 + elem.getName().length();
                int endOff;

                List<DocumentElement> children = elem.getChildren();

                if (children.size() > 0) {
                    endOff = children.get(0).getStartOffset() - 1;
                } else {
                    endOff = elem.getEndOffset() - 1;
                }
                boolean injectId = !elem.getAttributes().isDefined(SVGConstants.SVG_ID_ATTRIBUTE);

                BaseDocument doc = getDoc();
                String fragment = doc.getText(startOff, endOff - startOff + 1);
                int p;
                if ((p = fragment.indexOf(attrName)) != -1) {
                    p += attrName.length();
                    while (++p < fragment.length()) {
                        if (fragment.charAt(p) == '"') {
                            int q = p;

                            while (++q < fragment.length()) {
                                if (fragment.charAt(q) == '"') {
                                    p++;
                                    if (injectId) {
                                        StringBuilder sb = new StringBuilder(attrValue);
                                        sb.append("\" "); //NOI18N
                                        injectId(sb, id);
                                        doc.replace(startOff + p, q - p + 1, sb.toString(), null);
                                    } else {
                                        doc.replace(startOff + p, q - p, attrValue, null);
                                    }
                                    return;
                                }
                            }
                        }
                    }
                    SceneManager.log(Level.SEVERE, "Attribute " + attrName + " not changed: \"" + fragment + "\""); //NOI18N
                } else {
                    StringBuilder sb = new StringBuilder(" "); //NOI18N
                    if (injectId) {
                        injectId(sb, id);
                    }
                    sb.append(attrName);
                    sb.append("=\""); //NOI18N
                    sb.append(attrValue);
                    sb.append("\" "); //NOI18N
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
                DocumentElement de = checkIntegrity(id);
                DocumentElement firstChild = getFirstTagChild(de.getParentElement().getChildren());

                if (de != firstChild) {
                    BaseDocument doc = getDoc();
                    int startOffset = de.getStartOffset();
                    int length = de.getEndOffset() - startOffset + 1;
                    String elemText = getElemTextWithId(doc, de, id);

                    int insertOffset = firstChild.getStartOffset();
                    assert insertOffset < startOffset : "Offset overlap #1" + insertOffset + "," + startOffset; //NOI18N

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
                DocumentElement de = checkIntegrity(id);
                DocumentElement lastChild = getLastTagChild(de.getParentElement().getChildren());

                if (de != lastChild) {
                    BaseDocument doc = getDoc();
                    int startOffset = de.getStartOffset();
                    int length = de.getEndOffset() - startOffset + 1;
                    String elemText = getElemTextWithId(doc, de, id);

                    int insertOffset = lastChild.getEndOffset() + 1;
                    assert startOffset < insertOffset : "Offset overlap #2" + insertOffset + "," + startOffset; //NOI18N

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
                DocumentElement de = checkIntegrity(id);
                DocumentElement nextChild = getNextTagSibling(de);

                if (nextChild != null) {
                    BaseDocument doc = getDoc();
                    int startOffset = de.getStartOffset();
                    int length = de.getEndOffset() - startOffset + 1;
                    String elemText = getElemTextWithId(doc, de, id);

                    int insertOffset = nextChild.getEndOffset() + 1;
                    assert startOffset < insertOffset : "Offset overlap #3" + insertOffset + "," + startOffset; //NOI18N

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
                DocumentElement de = checkIntegrity(id);
                DocumentElement previousChild = getPreviousTagSibling(de);

                if (previousChild != null) {
                    BaseDocument doc = getDoc();

                    int startOffset = de.getStartOffset();
                    int length = de.getEndOffset() - startOffset + 1;
                    String elemText = getElemTextWithId(doc, de, id);
                    int insertOffset = previousChild.getStartOffset();
                    assert startOffset > insertOffset : "Offset overlap #4" + insertOffset + "," + startOffset; //NOI18N

                    doc.remove(startOffset, length);
                    doc.insertString(insertOffset, elemText, null);
                }
            }
        });
    }

    public static DocumentElement getSVGRoot(final DocumentModel model) {
        DocumentElement root = model.getRootElement();
        for (DocumentElement de : root.getChildren()) {
            if (isTagElement(de) && "svg".equals(de.getName())) {   //NOI18N
                return de;
            }
        }
        return null;
    }

    protected synchronized void fireModelChange() {
        if (!m_eventInProgress) {
            //System.out.println("Asking for update");
            SwingUtilities.invokeLater(m_updateTask);
        }
    }

    public void runTransaction(final FileModelTransaction transaction) {
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
        SceneManager.log(Level.FINE, "Updating model..."); //NOI18N
        checkModel();
        assert SwingUtilities.isEventDispatchThread() == false : "Model update cannot be called in AWT thread.";

        synchronized (m_lock) {
            if (m_sourceChanged) {
                SceneManager.log(Level.FINE, "Forcing model update"); //NOI18N
                m_model.forceUpdate();
            } else if (!m_updateInProgress) {
                SceneManager.log(Level.FINE, "Model already up to date."); //NOI18N
                return;
            }

            while (m_sourceChanged || m_updateInProgress) {
                SceneManager.log(Level.FINE, "Waiting for model update..."); //NOI18N
                try {
                    m_lock.wait();
                    SceneManager.log(Level.FINE, "Wait ended."); //NOI18N
                } catch (InterruptedException ex) {
                }
            }
        }
        SceneManager.log(Level.FINE, "Model update completed."); //NOI18N
    }

    public String getSVGBody(File file, StringBuilder wrapperIDHolder) throws FileNotFoundException, IOException, DocumentModelException, DocumentModelException, BadLocationException {
        DocumentModel docModel = loadDocumentModel(file);
        String name = file.getName();
        String wrapperId = createUniqueId(name.replace('.', '_'), true);
        String textToInsert = getWithUniqueIds(docModel, wrapperId, true, null, true);
        
        textToInsert = wrapText(wrapperId, textToInsert);
        if ( wrapperIDHolder != null) {
            wrapperIDHolder.insert(0, wrapperId);
        }
        return textToInsert;
    }

    public String mergeImage(File file) throws FileNotFoundException, IOException, DocumentModelException, BadLocationException {
        SceneManager.log(Level.INFO, "Merging file " + file.getPath()); //NOI18N
        StringBuilder wrapperId = new StringBuilder();
        appendElement( getSVGBody(file, wrapperId));
        return wrapperId.toString();
    }

    public String mergeImage(String str, boolean wrap) throws IOException, DocumentModelException, BadLocationException {
        InputStream in = new EncodingInputStream(str, "UTF-8"); //NOI18N
        return mergeImage(loadDocumentModel(in), wrap);
    }

    protected String mergeImage(DocumentModel docModel, boolean wrap) throws BadLocationException {
        String wrapperId    = null;
        String textToInsert = null;

        if (wrap) {
            wrapperId = createUniqueId( "", true);
            textToInsert = getWithUniqueIds(docModel, wrapperId, false, null, true);
            textToInsert = wrapText(wrapperId, textToInsert);
        } else {
            String[] rootId = new String[1];
            textToInsert = getWithUniqueIds(docModel, wrapperId, false, rootId, false);
            wrapperId = rootId[0];
        }
        appendElement(textToInsert);
        return wrapperId;
    }

    @SuppressWarnings("unchecked")
    protected String getWithUniqueIds(DocumentModel docModel, String wrapperId, 
            boolean isRootSvg, String[] rootId, boolean allowAnonymousRoot) 
            throws BadLocationException 
    {
        return getWithUniqueIds(docModel, wrapperId, isRootSvg, rootId, allowAnonymousRoot, true);
    }
    
    @SuppressWarnings("unchecked")
    protected String getWithUniqueIds(DocumentModel docModel, String wrapperId, 
            boolean isRootSvg, String[] rootId, boolean allowAnonymousRoot, 
            boolean silently) 
            throws BadLocationException 
    {
        try {
            docModel.readLock();

            DocumentElement rootElem = isRootSvg ? getSVGRoot(docModel) : docModel.getRootElement();
            Document        doc      = docModel.getDocument();
            String          docText  = null;
            
            if (rootElem != null) {
                List<DocumentElement> children = rootElem.getChildren();
                DocumentElement firstChild = getFirstTagChild(children);

                if (firstChild != null) {
                    int startOff = firstChild.getStartOffset();
                    int endOff = getLastTagChild(children).getEndOffset();
                    if (rootId != null) {
                        rootId[0] = SVGFileModel.getIdAttribute(firstChild);
                    }
                    
                    Set<String>   newIds      = new HashSet<String>();
                    List<String>  conflicts   = new ArrayList<String>();
                    StringBuilder conflictMsg = new StringBuilder();
                    
                    m_mapping.collectConflictingElements(rootElem, conflicts, newIds);
                    
                    if (!conflicts.isEmpty()) {
                        int length = doc.getLength();
                        StringBuilder sb = new StringBuilder(doc.getText(0, length));
                        List<SVGFileModel.ChangeDescriptor> changes = new ArrayList<SVGFileModel.ChangeDescriptor>();

                        for (int i = 0; i < conflicts.size(); i++) {
                            String oldId = conflicts.get(i);
                            String newId;
                            
                            if (wrapperId != null) {
                                newId = m_mapping.generateId(wrapperId + '_' + oldId, false, newIds);
                            } else {
                                newId = m_mapping.generateId(oldId, false, newIds);
                            }
                            
                            appendToConflictMsg(conflictMsg, oldId, newId, i, silently);
                            
                            newIds.add(newId);
                            if (rootId != null && oldId.equals(rootId[0])) {
                                rootId[0] = newId;
                            }
                            resolveChanges((CharSequence) sb, rootElem, oldId, newId, changes);
                        }

                        Collections.sort(changes);

                        for (ChangeDescriptor change : changes) {
                            change.replace(sb);
                        }

                        // fragment length have changed probably
                        docText = sb.substring(startOff, endOff + (sb.length() - length) + 1);
                        notifyAboutConflictIds(conflictMsg, silently);
                    } else {
                        docText = doc.getText(startOff, endOff - startOff + 1);
                    }
                    
                    if ( !allowAnonymousRoot && getIdAttribute(firstChild) == null &&
                          getTagChildCount(children) == 1) {
                        String name = '<' + firstChild.getName() + ' ';
                        int p;
                        if ( (p=docText.indexOf(name)) != -1) {
                                String id = m_mapping.generateId("", false, newIds); 
                                p += name.length();
                                docText = docText.substring(0, p) + 
                                          injectId( new StringBuilder(), id).toString() + 
                                          docText.substring(p);
                                if (rootId != null) {
                                    rootId[0] = id;
                                }
                        } else {
                            SceneManager.log(Level.SEVERE, "Could not inject id into:\n" + docText); //NOI18N
                        }
                    }
                }
            }
            return docText;
        } finally {
            docModel.readUnlock();
        }
    }
    
    private void appendToConflictMsg(StringBuilder msg, String oldId, String newId,
            int conflictIdx, boolean silently)
    {
        if (silently) {
            return;
        }
            
            if (conflictIdx < 10) {
                msg.append("\t'"); //NOI18N
                msg.append(oldId).append("' -> '").append(newId);//NOI18N
                msg.append("'\n"); //NOI18N
            } else if (conflictIdx == 10) {
                msg.append("\t...\n"); //NOI18N
            }
    }
    
    private void notifyAboutConflictIds(StringBuilder conflictMsg, boolean silently){
        if(silently){
            return;
        }
        String msg = NbBundle.getMessage(ElementMapping.class, 
                "WARNING_IDConflicts", conflictMsg.toString()); //NOI18N
        DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message(msg, NotifyDescriptor.Message.WARNING_MESSAGE));
    }
    
    protected static String wrapText(String wrapperId, String textToWrap) {
        //TODO indent the wrapped text
        StringBuilder sb = new StringBuilder();
        sb.append("<g id=\""); //NOI18N
        sb.append(wrapperId);
        sb.append("\">\n"); //NOI18N
        sb.append(textToWrap);
        sb.append("\n</g>"); //NOI18N
        return sb.toString();
    }

    public static final class ChangeDescriptor implements Comparable {
        private final int             m_startOffset;
        private final int             m_length;
        private final String          m_newValue;
        private final DocumentElement m_elem;

        public ChangeDescriptor(int startOffset, int length, String newValue, DocumentElement elem) {
            m_startOffset = startOffset;
            m_length      = length;
            m_newValue    = newValue;
            m_elem        = elem;
        }

        public ChangeDescriptor(int startOffset, int length, String newValue) {
            this(startOffset, length, newValue, null);
        }

        public void replace(StringBuilder sb) {
            sb.replace(m_startOffset, m_startOffset + m_length, m_newValue);
        }

        public int compareTo(Object o) {
            return ((ChangeDescriptor) o).m_startOffset - m_startOffset;
        }
        
        public boolean equals(Object o) {
            return ((ChangeDescriptor) o).m_startOffset == m_startOffset;
        }
    }

    private static boolean isElementIdChar(char c, boolean isTrailing) {
        return (!isTrailing || c != '.') && PerseusController.isElementIdChar(c);
    }

    private static int indexOf(CharSequence chars, String str, int from, int to) {
        assert to <= chars.length() : "Index out of bounds: " + to + " > " + chars.length(); //NOI18N
        int index = from;
        int strLen = str.length();
        if (strLen > 0) {
            main_loop:
            while (index < to) {
                int i = 0;
                while (chars.charAt(index++) == str.charAt(i++)) {
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

    public static void resolveChanges(CharSequence sb, DocumentElement elem, String oldId, String newId, List<ChangeDescriptor> changes) {
        //TODO implement faster and more robust id replacement
        AttributeSet attrs = elem.getAttributes();
        List<DocumentElement> children = elem.getChildren();

        for (Enumeration attrNames = attrs.getAttributeNames(); attrNames.hasMoreElements();) {
            String name = (String) attrNames.nextElement();
            String value = (String) attrs.getAttribute(name);

            if (value != null && value.length() > 0) {
                int p;
                if ((p = value.indexOf(oldId)) != -1) {
                    int q;
                    if ((p == 0 || !isElementIdChar(value.charAt(p - 1), false)) && ((q = p + oldId.length()) >= value.length() || !isElementIdChar(value.charAt(q), true))) {
                        int startOff = elem.getStartOffset();
                        int endOff = children.isEmpty() ? elem.getEndOffset() : children.get(0).getStartOffset();

                        if ((q = indexOf(sb, name, startOff, endOff)) != -1) {
                            if ((q = indexOf(sb, oldId, q + name.length(), endOff)) != -1) {
                                changes.add(new ChangeDescriptor(q, oldId.length(), newId, elem));
                                continue;
                            }
                        }

                        SceneManager.log(Level.INFO, "Attribute value " + value + " not found at the DE " + elem); //NOI18N
                    }
                }
            }
        }

        for (DocumentElement de : children) {
            if (SVGFileModel.isTagElement(de)) {
                resolveChanges(sb, de, oldId, newId, changes);
            }
        }
    }

    private static List<String> collectIds(DocumentElement elem, List<String> ids) {
        String id = getIdAttribute(elem);

        if (id != null) {
            ids.add(id);
        }
        List<DocumentElement> children = elem.getChildren();
        for (DocumentElement de : children) {
            if (isTagElement(de)) {
                collectIds(de, ids);
            }
        }
        return ids;
    }

    public static DocumentModel loadDocumentModel(File file) throws FileNotFoundException, IOException, DocumentModelException {
        InputStream in = null;

        FileInputStream fin = new FileInputStream(file);
        in = new BufferedInputStream(fin);

        String fileName = file.getName();

        int p;
        if ((p = fileName.indexOf('.')) != -1) {
            if (SVGDataObject.isSVGZ(fileName.substring(p + 1))) {
                in = new BufferedInputStream(new GZIPInputStream(in));
            }
        }

        return loadDocumentModel(in);
    }

    protected static DocumentModel loadDocumentModel(InputStream in) throws IOException, DocumentModelException {
        EditorKit kit = JEditorPane.createEditorKitForContentType(SVGDataLoader.REQUIRED_MIME);
        BaseDocument doc = (BaseDocument) kit.createDefaultDocument();
        try {
            kit.read(in, doc, 0);
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
    private static DocumentElement getFirstTagChild(List<DocumentElement> children) {
        for (DocumentElement child : children) {
            if (isTagElement(child)) {
                return child;
            }
        }
        return null;
    }

    private static DocumentElement getLastTagChild(List<DocumentElement> children) {
        for (int i = children.size() - 1; i >= 0; i--) {
            DocumentElement child = children.get(i);
            if (isTagElement(child)) {
                return child;
            }
        }
        return null;
    }

    private static DocumentElement getLastVisibleTagChild(List<DocumentElement> children) {
        for (int i = children.size() - 1; i >= 0; i--) {
            DocumentElement child = children.get(i);
            if (isTagElement(child) && !isHiddenElement(child)) {
                return child;
            }
        }
        return null;
    }
    
    private static int getTagChildCount(List<DocumentElement> children) {
        int count = 0;
        
        for (DocumentElement child : children) {
            if (isTagElement(child)) {
                count++;
            }
        }
        return count;
    }
    
    private static DocumentElement getPreviousTagSibling(DocumentElement elem) {
        DocumentElement parent = elem.getParentElement();
        DocumentElement previous = null;
        assert parent != null;

        for (DocumentElement child : parent.getChildren()) {
            if (child == elem) {
                return previous;
            } else {
                if (isTagElement(child)) {
                    previous = child;
                }
            }
        }
        assert false : "The document element " + elem + " is no longer part of the document";
        return null;
    }

    private static DocumentElement getNextTagSibling(DocumentElement elem) {
        DocumentElement parent = elem.getParentElement();
        DocumentElement next = null;
        assert parent != null;
        List<DocumentElement> children = parent.getChildren();

        for (int i = children.size() - 1; i >= 0; i--) {
            DocumentElement child = children.get(i);
            if (child == elem) {
                return next;
            } else {
                if (isTagElement(child)) {
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
        if (isTagElement(de) && de.getEndOffset() <= de.getStartOffset()) {
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