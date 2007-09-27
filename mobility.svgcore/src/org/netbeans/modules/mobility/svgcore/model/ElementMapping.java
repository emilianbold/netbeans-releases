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
 *
 */

package org.netbeans.modules.mobility.svgcore.model;

import org.netbeans.modules.mobility.svgcore.composer.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.microedition.m2g.SVGImage;
import javax.microedition.m2g.SVGImage;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.CharSeq;
import org.netbeans.modules.editor.structure.api.DocumentElement;
import org.netbeans.modules.editor.structure.api.DocumentModel;

/**
 *
 * @author Pavel Benes
 */
public final class ElementMapping {
    private static final String ID_COUPLING_PREFIX = "_"; //NOI18N
    private static final String ID_WRAPPER_PREFIX  = "w_"; //NOI18N
    
    private final SVGFileModel                 m_fileModel;
    private final Map<String, DocumentElement> m_ids = new HashMap<String, DocumentElement>();
    private boolean                            m_refreshNeeded = false;

    public ElementMapping(SVGFileModel fileModel) {
        m_fileModel = fileModel;
    }

    public SVGImage parseDocument(boolean update) throws BadLocationException, IOException {
        StringBuilder sb;

        synchronized (m_fileModel.getTransactionMonitor()) {
            if (update) {
                m_fileModel.updateModel();
            }
            DocumentModel docModel = m_fileModel.getModel();
            try {
                docModel.readLock();

                //long time = System.currentTimeMillis();
                BaseDocument doc = (BaseDocument) docModel.getDocument();
                CharSeq charSeq = doc.getText();
                int docLength = charSeq.length();

                sb = new StringBuilder(docLength);

                for (int i = 0; i < docLength; i++) {
                    sb.append(charSeq.charAt(i));
                }

                m_ids.clear();
                m_refreshNeeded = false;
                List<DocumentElement> elemsWithoutID = new ArrayList<DocumentElement>();
                // store all elements with ID into map and collect all anonymous
                // elements for further processing
                collectAnonymousElements(docModel.getRootElement(), elemsWithoutID);
                int serial = 0;

                // generate unique ID for all annonymous elements
                for (DocumentElement elem : elemsWithoutID) {
                    String id;
                    do {
                        id = ID_COUPLING_PREFIX + serial++;
                    } while (m_ids.containsKey(id));

                    int startOffset = elem.getStartOffset();
                    startOffset += elem.getName().length() + 1;
                    String str = " id=\"" + id + "\" ";
                    sb.insert(startOffset, str);

                    checkRemovedElement(elem);
                    if (m_ids.put(id, elem) != null) {
                        System.err.println("Duplicated id: " + id);
                    }
                }
            } finally {
                docModel.readUnlock();
            }
        }

        String encoding = m_fileModel.getDataObject().getEncodingHelper().getEncoding();
        InputStream in = new EncodingInputStream(sb, encoding);

        try {
            SVGImage svgImage = PerseusController.createImage(in);
            return svgImage;
        } finally {
            in.close();
        }
    }

    public synchronized void add(DocumentElement newDe) {
        String id = SVGFileModel.getIdAttribute(newDe);
        
        if (SceneManager.isEnabled(Level.FINEST)) {
            SceneManager.log(Level.FINEST, "Adding mapping " + id + " <--> " + newDe); //NOI18N
        }
        
        checkRemovedElement(newDe);

        if (id != null) {
            m_ids.put(id, newDe);
        } else {
            m_refreshNeeded = true;
        }
    }

    public synchronized void remove(DocumentElement oldDe) {
        String id = SVGFileModel.getIdAttribute(oldDe);
        if (id == null) {
            //TODO Revisit - maybe it takes too much time
            Iterator iter = m_ids.values().iterator();
            while (iter.hasNext()) {
                if (iter.next() == oldDe) {
                    if (SceneManager.isEnabled(Level.FINEST)) {
                        SceneManager.log(Level.FINEST, "Removing mapping " + id + " <--> " + oldDe); //NOI18N
                    }
                    iter.remove();
                    return;
                }
            }
        } else {
            m_ids.remove(id);
        }
    }

    private void refresh() {
        try {
            SceneManager.log(Level.INFO, "Refreshing mapping"); //NOI18N
            SVGImage img = parseDocument(false);
            m_fileModel.getDataObject().getSceneManager().setImage(img);
        } catch (Exception ex) {
            SceneManager.error("Mapping refresh failed", ex); //NOI18N
        }
    }

    synchronized DocumentElement id2element(String id) {
        if ( SceneManager.isEnabled(Level.FINER)) {
            SceneManager.log(Level.FINER, "Looking for element with id: " + id); //NOI18N
        }
        
        DocumentElement elem = m_ids.get(id);
        
        if (elem == null) {
            SceneManager.log(Level.FINER, "No element found."); //NOI18N
            if (m_refreshNeeded) {
                refresh();
                elem = m_ids.get(id);
            }
        }
        checkRemovedElement(elem);
        if ( SceneManager.isEnabled(Level.FINE)) {
            SceneManager.log(Level.FINE, "Mapping found: " + id + " -> " + elem); //NOI18N
        }
        return elem;
    }

    public synchronized String element2id(DocumentElement elem) {
        String id = null;

        for (Map.Entry e : m_ids.entrySet()) {
            if (e.getValue() == elem) {
                id = (String) e.getKey();
                break;
            }
        }

        if ( SceneManager.isEnabled(Level.FINE)) {
            SceneManager.log(Level.FINE, "Mapping found: " + elem + " -> " + id); //NOI18N
        }
        
        return id;
    }

    synchronized String generateId(String prefix, boolean isWrapper, Set<String> extIds) {
        String id;
        int serial = 0;
        if (isWrapper) {
            prefix = ID_WRAPPER_PREFIX + prefix;
        }
        do {
            id = prefix + "_" + serial++; //NOI18N
        } while (m_ids.containsKey(id) || (extIds != null && extIds.contains(id)));

        return id;
    }

    static boolean isWrapperId(String id) {
        return id != null && id.startsWith(ID_WRAPPER_PREFIX);
    }

    protected static void checkRemovedElement(DocumentElement de) {
        assert de == null || de.getStartOffset() < de.getEndOffset() : "Deleted element found: " + de; //NOI18N 
    }

    synchronized void collectConflictingElements(DocumentElement elem, List<String> conflicts, Set<String> newIds) {
        assert elem != null;

        List<DocumentElement> children = elem.getChildren();

        for (int i = children.size() - 1; i >= 0; i--) {
            DocumentElement de = children.get(i);

            if (SVGFileModel.isTagElement(de)) {
                collectConflictingElements(de, conflicts, newIds);
            }
        }

        if (!"ROOT_ELEMENT".equals(elem.getType())) { //NOI18N            
            String id = SVGFileModel.getIdAttribute(elem);

            if (id != null) {
                if (m_ids.containsKey(id)) {
                    conflicts.add(id);
                } else {
                    newIds.add(id);
                }
            }
        }
    }

    protected void collectAnonymousElements(DocumentElement elem, List<DocumentElement> elems) {
        assert elem != null;
        assert elem.getDocumentModel() == m_fileModel.getModel() : "Element " + elem + " has incorrect model " + elem.getDocumentModel(); //NOI18N 

        List<DocumentElement> children = elem.getChildren();

        for (int i = children.size() - 1; i >= 0; i--) {
            DocumentElement de = children.get(i);

            if (SVGFileModel.isTagElement(de)) {
                if (de.getStartOffset() < de.getEndOffset()) {
                    collectAnonymousElements(de, elems);
                } else {
                    SceneManager.log(Level.SEVERE, "Deleted element found in the model: " + de); //NOI18N
                }
            }
        }

        if (!"ROOT_ELEMENT".equals(elem.getType())) { //NOI18N 
            String id = SVGFileModel.getIdAttribute(elem);

            if (id != null) {
                checkRemovedElement(elem);
                if (m_ids.put(id, elem) != null) {
                    SceneManager.log(Level.SEVERE, "Duplicated id: " + id); //NOI18N
                }
            } else {
                elems.add(elem);
            }
        }
    }
}