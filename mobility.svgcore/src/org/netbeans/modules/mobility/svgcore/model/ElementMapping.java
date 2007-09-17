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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.microedition.m2g.SVGImage;
import javax.microedition.m2g.SVGImage;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.CharSeq;
import org.netbeans.modules.editor.structure.api.DocumentElement;
import org.netbeans.modules.editor.structure.api.DocumentModel;
import org.netbeans.modules.mobility.svgcore.model.SVGFileModel;
import org.netbeans.modules.mobility.svgcore.model.SVGFileModel.ChangeDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Pavel Benes
 */
public final class ElementMapping {    
    private static final String ID_COUPLING_PREFIX = "_";  //NOI18N
    private static final String ID_WRAPPER_PREFIX  = "w_"; //NOI18N
    
    private final SVGFileModel                m_fileModel;
    private final Map<String,DocumentElement> m_ids = new HashMap<String, DocumentElement>();
    //private final Set<String>                 m_extIds = new HashSet<String>();
    private       boolean                     m_refreshNeeded = false;
    //private       ElementDescriptor           m_selectedDescr = null;
    /*
    private static class ElementDescriptor {
        private final String               m_id;
        private final int                  m_startOff;
        private final String               m_tag;
        private final Map<String, Integer> m_attribs;
        
        public ElementDescriptor(String id, DocumentElement de) {
            m_id       = id;
            m_tag      = de.getName();
            m_startOff = de.getStartOffset();
            m_attribs  = new HashMap<String, Integer>();            

            AttributeSet attrSet = de.getAttributes();
            
            if ( attrSet != null) {
                for (Enumeration attrNames = attrSet.getAttributeNames(); attrNames.hasMoreElements(); ) {
                    String attrName = (String) attrNames.nextElement();
                    Object attrValue = attrSet.getAttribute(attrName);
                    if ( attrValue != null && 
                         !SVGConstants.SVG_TRANSFORM_ATTRIBUTE.equals(attrName)) {
                        m_attribs.put( attrName, attrValue.hashCode());
                    }
                }
            }
            return;
        }
        
        public boolean isSameElement(DocumentElement de, boolean checkStartOff) {
            if ( (!checkStartOff || de.getStartOffset() == m_startOff) &&
                  m_tag != null && m_tag.equals( de.getName())) {
                AttributeSet attrSet    = de.getAttributes();
                int          attrCount1 = attrSet != null ? attrSet.getAttributeCount() : 0;
                int          attrCount2 = m_attribs.size();

                if ( attrCount1 > 0) {
                    if ( attrSet.getAttribute( SVGConstants.SVG_TRANSFORM_ATTRIBUTE) != null) {
                        attrCount1--;
                    } 
                }

                if ( attrCount1 == attrCount2) {
                    for ( Iterator<Entry<String, Integer>> iter = m_attribs.entrySet().iterator(); iter.hasNext();) {
                        Entry<String, Integer> e = iter.next();
                        if ( e.getValue().intValue() != attrSet.getAttribute( e.getKey()).hashCode()) {
                            return false;
                        }
                    }
                    return true;
                }
            }
            return false;
        }
    }
    */
    
    public ElementMapping(SVGFileModel fileModel) {
        m_fileModel = fileModel;
        //m_encoding = obj.getEncodingHelper().getEncoding();
    }

    //TODO Move to the SVGFileModel
    public String getWithUniqueIds(DocumentModel docModel, String wrapperId, boolean isRootSvg, String [] rootId) throws BadLocationException {
        try {
            docModel.readLock();
            
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
                    Set<String>  newIds    = new HashSet<String>();
                    List<String> conflicts = new ArrayList<String>();
                    collectConflictingElements(rootElem, conflicts, newIds);

                    docText = docModel.getDocument().getText(startOff, endOff - startOff + 1);
                    if (!conflicts.isEmpty()) {
                        StringBuilder           sb      = new StringBuilder(docText);
                        List<SVGFileModel.ChangeDescriptor>  changes = new ArrayList<SVGFileModel.ChangeDescriptor>();

                        for (String oldId : conflicts) {
                            String newId;
                            if (wrapperId != null) {
                                newId = generateId(wrapperId + '_' + oldId, false, newIds);
                            } else {
                                newId = generateId(oldId, false, newIds);
                            }
                            newIds.add(newId);
                            if (rootId != null && oldId.equals(rootId[0])) {
                                rootId[0] = newId;
                            }
                            SVGFileModel.resolveChanges( (CharSequence)sb, rootElem, oldId, newId, changes);
                        }

                        Collections.sort(changes);

                        for ( ChangeDescriptor change : changes) {
                            change.replace(sb);
                        }        

                        /*
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
                         */
                        docText = sb.toString();
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                            NbBundle.getMessage(ElementMapping.class, "WARNING_IDConflicts"),  //NOI18N
                            NotifyDescriptor.Message.WARNING_MESSAGE
                        ));
                    }
                }
            }
            return docText;
        } finally {
            docModel.readUnlock();
        }
    }
           
    public SVGImage parseDocument(boolean update) throws BadLocationException, IOException {
        StringBuilder sb;
        
        synchronized( m_fileModel.getTransactionMonitor()) {
            if (update) {
                m_fileModel.updateModel();
            }
            DocumentModel docModel = m_fileModel.getModel();
            try {
                docModel.readLock();
            
                //long time = System.currentTimeMillis();
                BaseDocument  doc       = (BaseDocument) docModel.getDocument();
                CharSeq       charSeq   = doc.getText();            
                int           docLength = charSeq.length();

                sb = new StringBuilder(docLength);

                for (int i = 0; i < docLength; i++) {
                    sb.append(charSeq.charAt(i));
                }

                m_ids.clear();
                m_refreshNeeded = false;
                List <DocumentElement> elemsWithoutID = new ArrayList<DocumentElement>();
                // store all elements with ID into map and collect all anonymous
                // elements for further processing
                collectAnonymousElements(docModel.getRootElement(), elemsWithoutID);
                int serial = 0;

                // generate unique ID for all annonymous elements
                for (DocumentElement elem : elemsWithoutID) {
                    String id;
                    do {
                        id = ID_COUPLING_PREFIX + serial++;
                    } while( m_ids.containsKey(id));
                    
                    int startOffset = elem.getStartOffset();
                    startOffset += elem.getName().length() + 1;
                    String str = " id=\"" + id + "\" ";
                    sb.insert( startOffset, str);

                    checkRemovedElement(elem);
                    if ( m_ids.put(id, elem) != null) {
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
            SVGImage svgImage = (SVGImage) PerseusController.createImage( in);
            return svgImage;
        } finally {
            in.close();
        }        
    }
    
    public synchronized void add(DocumentElement newDe) {
        String id = SVGFileModel.getIdAttribute(newDe);
//        System.out.println("Adding mapping " + id + " <--> " + newDe);
        checkRemovedElement(newDe);
        
        if (id != null) {
            m_ids.put(id, newDe);
        } else {
            m_refreshNeeded = true;
        }
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
            //System.out.println("Refreshing mapping");
            //long time = System.currentTimeMillis();
            SVGImage img = parseDocument(false);
            m_fileModel.getDataObject().getSceneManager().setImage(img);
            //time = System.currentTimeMillis() - time;
            //System.out.println("Mapping refreshed in " + time + "[ms]");
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    synchronized DocumentElement id2element(String id) {
        DocumentElement elem = m_ids.get(id);
        if ( elem == null) {
            System.out.println("No element for id: " + id);
            if ( m_refreshNeeded) {
                refresh();
                elem = m_ids.get(id);
            }
        }
        checkRemovedElement(elem);

        return elem;
    }
    
    
    public synchronized String element2id(DocumentElement elem) {
        String id;
        
        if ( (id=element2idImpl(elem)) == null) {
            //TODO possibly use some small scale refresh, that 
            // tries to refresh only part of the document
            //System.out.println("Refreshing");
            //refresh();
            //id = element2idImpl(elem);
            System.out.println("Element not found: " + elem);
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
    
    synchronized String generateId(String prefix, boolean isWrapper, Set<String> extIds) {
        String id;
        int    serial = 0;
        if ( isWrapper) {
            prefix = ID_WRAPPER_PREFIX + prefix;
        }
        do {
             id = prefix + "_" + serial++; //NOI18N
        } while( m_ids.containsKey(id) || (extIds != null && extIds.contains(id)));
        
        return id;        
    }

    static boolean isWrapperId(String id) {
        return id != null && id.startsWith(ID_WRAPPER_PREFIX);
    }

    static protected void checkRemovedElement(DocumentElement de) {        
        assert de == null || de.getStartOffset() < de.getEndOffset() : "Deleted element found: " + de;
    }

    protected void collectConflictingElements( DocumentElement elem, List<String> conflicts, Set<String> newIds) {
        assert elem != null;

        List<DocumentElement> children = elem.getChildren();

        for (int i = children.size() - 1; i >= 0; i--) {
            DocumentElement de = children.get(i);
            
            if (SVGFileModel.isTagElement(de)) {                
                collectConflictingElements(de, conflicts, newIds);
            }
        }

        if ( !"ROOT_ELEMENT".equals(elem.getType())) {  //NOI18N
            String id = SVGFileModel.getIdAttribute(elem);

            if (id != null) {
                if ( m_ids.containsKey(id)) {
                    conflicts.add(id);
                } else {
                    newIds.add(id);
                }
            }
        }
    }
    
    protected void collectAnonymousElements( DocumentElement elem, List<DocumentElement> elems) {
        assert elem != null;
        assert elem.getDocumentModel() == m_fileModel.getModel() : "Element " + elem + " has incorrect model " + elem.getDocumentModel();

        List<DocumentElement> children = elem.getChildren();

        for (int i = children.size() - 1; i >= 0; i--) {
            DocumentElement de = children.get(i);
            
            if (SVGFileModel.isTagElement(de)) {
                if ( de.getStartOffset() < de.getEndOffset()) {
                    collectAnonymousElements(de, elems);
                } else {
                    System.out.println("Deleted element found in the model: " + de);
                }
            }
        }

        if ( !"ROOT_ELEMENT".equals(elem.getType())) { //NOI18N
            String id = SVGFileModel.getIdAttribute(elem);

            if (id != null) {
                checkRemovedElement(elem);                
                if ( m_ids.put(id, elem) != null) {
                    System.err.println("Duplicated id: " + id);
                }
            } else {
                elems.add(elem);
            }
        }
    }
}
