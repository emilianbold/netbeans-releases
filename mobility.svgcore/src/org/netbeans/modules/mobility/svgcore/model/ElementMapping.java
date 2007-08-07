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
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.microedition.m2g.SVGImage;
import javax.microedition.m2g.SVGImage;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.CharSeq;
import org.netbeans.modules.editor.structure.api.DocumentElement;
import org.netbeans.modules.editor.structure.api.DocumentModel;
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
    private final Map<String, Runnable>       m_scheduledTasks = new HashMap<String, Runnable>();
    private       DocumentModel               m_docModel;
    
    public ElementMapping(String encoding) {
        m_encoding = encoding;
    }
    
    public String getWithUniqueIds(DocumentModel docModel, String wrapperId) throws BadLocationException {
        String          docText = null;
        DocumentElement svgElem = SVGFileModel.getSVGRoot(docModel);

        int childElemNum;
        if (svgElem != null &&
            (childElemNum=svgElem.getElementCount()) > 0) {
            int startOff = svgElem.getElement(0).getStartOffset();
            int endOff   = svgElem.getElement(childElemNum - 1).getEndOffset();

            try {
                m_extIds.clear();

                List<DocumentElement> conflicts = new ArrayList<DocumentElement>();
                collectConflictingElements(docModel.getRootElement(), conflicts);

                docText = docModel.getDocument().getText(startOff, endOff - startOff + 1);
                if (conflicts.size() > 0) {
                    //TODO change also the references to changed ids
                    StringBuilder sb = new StringBuilder(docText);
                    for (DocumentElement conflict : conflicts) {
                        String oldId = SVGFileModel.getIdAttribute(conflict);
                        int p;
                        if ( (p=docText.indexOf('"' + oldId + '"', conflict.getStartOffset() - startOff)) != -1) {
                            String newId = generateId(wrapperId + '.' + oldId, false);
                            m_extIds.add(newId);
                            p++;
                            sb.replace( p, p + oldId.length(), newId);
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
        return docText;
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
            m_scheduledTasks.clear();
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
    
    public synchronized void add(String id, DocumentElement de) {
        //System.out.println("Adding mapping " + id + " <--> " + de);
        m_ids.put(id, de);
        Runnable task;
        if ( (task=m_scheduledTasks.remove(id)) != null) {
            SwingUtilities.invokeLater(task);
        }
    }
    
    public void scheduleTask(String id, Runnable task) {
        m_scheduledTasks.put(id, task);
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
             id = prefix + serial++;
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
