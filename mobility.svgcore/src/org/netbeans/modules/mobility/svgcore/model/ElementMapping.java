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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.microedition.m2g.SVGImage;
import javax.microedition.m2g.SVGImage;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.CharSeq;
import org.netbeans.modules.editor.structure.api.DocumentElement;
import org.netbeans.modules.editor.structure.api.DocumentModel;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.modules.mobility.svgcore.model.SVGFileModel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Pavel Benes
 */
public class ElementMapping {    
    private static final String ID_COUPLING_PREFIX = "_";  //NOI18N
    private static final String ID_WRAPPER_PREFIX  = "w_"; //NOI18N
    
    private final String                      m_encoding;
    private final Map<String,DocumentElement> m_ids = new HashMap<String, DocumentElement>();
    private final Set<String>                 m_extIds = new HashSet<String>();
    private       DocumentModel               m_docModel;
    private       boolean                     m_refreshNeeded = false;
    
    
    public ElementMapping(SVGDataObject obj) {
        m_encoding = obj.getEncodingHelper().getEncoding();
    }
    
    public String getWithUniqueIds(DocumentModel docModel, String wrapperId, 
            boolean isRootSvg, String [] rootId) throws BadLocationException {
        String          docText  = null;
        DocumentElement rootElem = isRootSvg ? SVGFileModel.getSVGRoot(docModel) : docModel.getRootElement();

        if (rootElem != null) {
            List<DocumentElement> children = rootElem.getChildren();
            int childElemNum;
        
            if ( (childElemNum=children.size()) > 0) {
                DocumentElement firstChild = children.get(0);
                int startOff = firstChild.getStartOffset();
                int endOff   = children.get(childElemNum - 1).getEndOffset();
                if (rootId != null) {
                    rootId[0] = SVGFileModel.getIdAttribute(firstChild);
                }
                try {
                    m_extIds.clear();

                    List<DocumentElement> conflicts = new ArrayList<DocumentElement>();
                    collectConflictingElements(docModel.getRootElement(), conflicts);

                    docText = docModel.getDocument().getText(startOff, endOff - startOff + 1);
                    if (!conflicts.isEmpty()) {
                        //TODO change also the references to changed ids
                        StringBuilder sb = new StringBuilder(docText);
                        
                        for (DocumentElement conflict : conflicts) {
                            String oldId = SVGFileModel.getIdAttribute(conflict);
                            assert oldId != null;
                            int p;
                            if ( (p=docText.indexOf('"' + oldId + '"', conflict.getStartOffset() - startOff)) != -1) {                                
                                String newId;
                                if (wrapperId != null) {
                                    newId = generateId(wrapperId + '.' + oldId, false);
                                } else {
                                    newId = generateId(oldId, false);
                                }
                                m_extIds.add(newId);
                                p++;
                                sb.replace( p, p + oldId.length(), newId);
                                if (rootId != null && oldId.equals(rootId[0])) {
                                    rootId[0] = newId;
                                }
                            }
                        }
                        docText = sb.toString();
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                            NbBundle.getMessage(ElementMapping.class, "WARNING_IDConflicts"),  //NOI18N
                            NotifyDescriptor.Message.WARNING_MESSAGE
                        ));
                    }
                } finally {
                    m_extIds.clear();
                }
            }
        }
        return docText;
    }
            
    private String m_id;
    private int m_startOff;
    private int m_endOff;
    private String m_text;
    
    public void storeSelection(String id) {
        if ( id != null && m_docModel != null) {
            DocumentElement de  = m_ids.get(id);
            if ( de != null && SVGFileModel.getIdAttribute(de) == null) {
                try {
                    BaseDocument    doc = (BaseDocument) m_docModel.getDocument();
                    
                    m_id       = id;
                    m_startOff = de.getStartOffset();
                    m_endOff   = de.getEndOffset();
                    m_text     = doc.getText(m_startOff, m_endOff - m_startOff + 1);
                    return;
                } catch( BadLocationException e) {
                    Exceptions.printStackTrace(e);
                }                    
            } 
        }
        m_id   = null;
        m_text = null;
        m_startOff = m_endOff = -1;
    }
    
    private DocumentElement findSameElement( List<DocumentElement> anonymous) throws BadLocationException {
        if (m_id != null) {
            BaseDocument doc = (BaseDocument) m_docModel.getDocument();
            
            //first pass
            for ( DocumentElement de : anonymous) {
                int startOff = de.getStartOffset();
                if ( startOff == m_startOff) {
                    String text = doc.getText( startOff, de.getEndOffset() - startOff + 1);
                    if ( m_text.equals(text)) {
                        return de;
                    }
                }
            }
            //second pass
            for ( DocumentElement de : anonymous) {
                int startOff = de.getStartOffset();
                String text = doc.getText( startOff, de.getEndOffset() - startOff + 1);
                if ( m_text.equals(text)) {
                    return de;
                }
            }
        }
        return null;
    }
    
    public SVGImage parseDocument(SVGFileModel fileModel, DocumentModel docModel) throws BadLocationException, IOException {
        if (fileModel != null) {
            //wait until the DocumentModel is updated after last changes
            fileModel.updateModel();
        }
            
        synchronized(this) {
            //long time = System.currentTimeMillis();
            BaseDocument  doc    = (BaseDocument) docModel.getDocument();
            CharSeq       charSeq = doc.getText();
            
            int           docLength = charSeq.length();
            StringBuilder sb        = new StringBuilder(docLength);
            
            for (int i = 0; i < docLength; i++) {
                sb.append(charSeq.charAt(i));
            }
            
            m_docModel = docModel;

            m_ids.clear();
            m_refreshNeeded = false;
//            m_scheduledTasks.clear();
            List <DocumentElement> elemsWithoutID = new ArrayList<DocumentElement>();
            // store all elements with ID into map and collect all anonymous
            // elements for further processing
            collectAnonymousElements(docModel.getRootElement(), elemsWithoutID);

            DocumentElement selected = null;
            if ( m_id != null) {
                selected = findSameElement(elemsWithoutID);
            }
            int serial = 0;
            
            // generate unique ID for all annonymous elements
            for (DocumentElement elem : elemsWithoutID) {
                String id;
                if (selected == elem) {
                    id = m_id;
                } else {
                    do {
                        id = ID_COUPLING_PREFIX + serial++;
                    } while( m_ids.containsKey(id) || id.equals(m_id));
                }
                int startOffset = elem.getStartOffset();
                startOffset += elem.getName().length() + 1;
                String str = " id=\"" + id + "\" ";
                sb.insert( startOffset, str);

                if ( m_ids.put(id, elem) != null) {
                    System.err.println("Duplicated id: " + id);
                }
            }
            
            InputStream in = new EncodingInputStream(sb, m_encoding);
            
            try {
                SVGImage svgImage = (SVGImage) PerseusController.createImage( in);
                return svgImage;
            } finally {
                in.close();
            }        
        }
    }
    
    public synchronized void add(DocumentElement newDe) {
        String id = SVGFileModel.getIdAttribute(newDe);
//        System.out.println("Adding mapping " + id + " <--> " + newDe);
        if (id != null) {
            m_ids.put(id, newDe);
        } else {
            m_refreshNeeded = true;
        }
        /*
        Runnable task;
        if ( (task=m_scheduledTasks.remove(id)) != null) {
            SwingUtilities.invokeLater(task);
        }*/
    }
    
    public synchronized void remove(DocumentElement oldDe) {
        String id = SVGFileModel.getIdAttribute(oldDe);
        if ( id == null) {
            //TODO Revisit - maybe it takes too much time
            Iterator iter = m_ids.values().iterator();
            while( iter.hasNext()) {
                if (iter.next() == oldDe) {
//                    System.out.println("Removing mapping null <--> " + oldDe);
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
            //long time = System.currentTimeMillis();
            parseDocument(null, m_docModel);
            //time = System.currentTimeMillis() - time;
            //System.out.println("Mapping refreshed in " + time + "[ms]");
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public synchronized DocumentElement id2element(String id) {
        DocumentElement elem = m_ids.get(id);
        //System.out.print("Looking for id " + id + " ...");
        if ( elem != null) {
            if ( elem.getDocumentModel() == m_docModel) {
                //System.out.println(" found.");
                return elem;
            } else {
                //System.out.println(" refreshing");
                refresh();
                return id2element(id);
            }
        } else {
            if (m_refreshNeeded) {
                refresh();
                return id2element(id);
           }
        }
        System.out.println("No element found for id '" + id + "'");
        return null;
    }
    
    public synchronized String element2id(DocumentElement elem) {
        String id;
        
        if ( (id=element2idImpl(elem)) == null) {
            //TODO possibly use some small scale refresh, that 
            // tries to refresh only part of the document
            //System.out.println("Refreshing");
            refresh();
            id = element2idImpl(elem);
        }
        return id;
    }
    
    protected String element2idImpl(DocumentElement elem) {
        for ( Map.Entry e : m_ids.entrySet()) {
            if ( e.getValue() == elem) {
                return (String) e.getKey();
            }
        }
        return null;
    }
    
    synchronized String generateId(String prefix, boolean isWrapper) {
        String id;
        int    serial = 0;
        if ( isWrapper) {
            prefix = ID_WRAPPER_PREFIX + prefix;
        }
        do {
             id = prefix + "." + serial++;
        } while( m_ids.containsKey(id) || m_extIds.contains(id));
        
        return id;        
    }

    static boolean isWrapperId(String id) {
        return id != null && id.startsWith(ID_WRAPPER_PREFIX);
    }

    protected void collectConflictingElements( DocumentElement elem, List<DocumentElement> conflicts) {
        assert elem != null;

        List<DocumentElement> children = elem.getChildren();

        for (int i = children.size() - 1; i >= 0; i--) {
            DocumentElement de = children.get(i);
            
            if (SVGFileModel.isTagElement(de)) {                
                collectConflictingElements(de, conflicts);
            }
        }

        if ( !"ROOT_ELEMENT".equals(elem.getType())) {  //NOI18N
            String id = SVGFileModel.getIdAttribute(elem);

            if (id != null) {
                if ( m_ids.containsKey(id)) {
                    conflicts.add(elem);
                } else {
                    m_extIds.add(id);
                }
            }
        }
    }
    
    protected void collectAnonymousElements( DocumentElement elem, List<DocumentElement> elems) {
        assert elem != null;
        assert elem.getDocumentModel() == m_docModel : "Element " + elem + " has incorrect model " + elem.getDocumentModel();

        List<DocumentElement> children = elem.getChildren();

        for (int i = children.size() - 1; i >= 0; i--) {
            DocumentElement de = children.get(i);
            
            if (SVGFileModel.isTagElement(de)) {                
                collectAnonymousElements(de, elems);
            }
        }

        if ( !"ROOT_ELEMENT".equals(elem.getType())) { //NOI18N
            String id = SVGFileModel.getIdAttribute(elem);

            if (id != null) {
                if ( m_ids.put(id, elem) != null) {
                    System.err.println("Duplicated id: " + id);
                }
            } else {
                elems.add(elem);
            }
        }
    }
/*    
    protected void decorateElement( DocumentElement elem, StringBuilder sb) {
        assert elem != null;
        assert elem.getDocumentModel() == m_docModel : "Element " + elem + " has incorrect model " + elem.getDocumentModel();

        List<DocumentElement> children = elem.getChildren();

        for (int i = children.size() - 1; i >= 0; i--) {
            DocumentElement de = children.get(i);
            
            if (SVGFileModel.isTagElement(de)) {                
                decorateElement(de, sb);
            }
        }

        if ( !"ROOT_ELEMENT".equals(elem.getType())) {
            AttributeSet attrs = elem.getAttributes();
            String       id    = (String) attrs.getAttribute(PerseusController.ATTR_ID);

            if (id == null) {
                id = generateId();

                int startOffset = elem.getStartOffset();
                startOffset += elem.getName().length() + 1;
                String str = " id=\"" + id + "\" ";
                sb.insert( startOffset, str);
            }

            if ( m_ids.put(id, elem) != null) {
                System.err.println("Duplicated id: " + id);
            }
        }
    }
 */
}
