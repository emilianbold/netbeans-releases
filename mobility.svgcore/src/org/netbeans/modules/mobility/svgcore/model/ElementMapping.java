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
import java.util.HashMap;
import java.util.Map;
import javax.microedition.m2g.SVGImage;
import javax.microedition.m2g.SVGImage;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.editor.structure.api.DocumentElement;
import org.netbeans.modules.editor.structure.api.DocumentModel;
import org.netbeans.modules.mobility.svgcore.model.SVGFileModel;
import org.openide.util.Exceptions;

/**
 *
 * @author Pavel Benes
 */
public class ElementMapping {    
    private DocumentModel               m_docModel;
    private Map<String,DocumentElement> m_ids = new HashMap<String, DocumentElement>();
    private int                         m_serial = 0;
    private Map<String, Runnable>       m_scheduledTasks = new HashMap<String, Runnable>();
   
    @SuppressWarnings({"deprecation"})
    public synchronized SVGImage parseDocument(SVGFileModel fileModel, DocumentModel docModel) throws BadLocationException, IOException {
        m_ids.clear();
        m_scheduledTasks.clear();
        m_serial     = 0;
        Document doc = docModel.getDocument();
        
        //TODO use a better way for module synchronisation
        while(fileModel != null && !fileModel.isModelStable()) {
            System.out.println("Waiting for model synchronisation");
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        //TODO wait until the DocumentModel is stable
        StringBuilder sb;
        try {
            docModel.readLock();
            DocumentElement root = docModel.getRootElement();
            sb = new StringBuilder(doc.getText(0, doc.getLength()));
            m_docModel   = docModel;
            decorateElement(root, sb);
        } finally {
            docModel.readUnlock();
        }
        
        InputStream in = new java.io.StringBufferInputStream(sb.toString());
        try {
            SVGImage svgImage = (SVGImage) PerseusController.createImage( in);
            return svgImage;
        } finally {
            in.close();
        }        
    }
    
    public synchronized void add(String id, DocumentElement de) {
        System.out.println("Adding mapping " + id + " <--> " + de);
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
            long time = System.currentTimeMillis();
            parseDocument(null, m_docModel);
            time = System.currentTimeMillis() - time;
            System.out.println("Mapping refreshed in " + time + "[ms]");
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
                System.out.println(" refreshing");
                refresh();
                return id2element(id);
            }
        }
        System.out.println("Not found.");
        return null;
    }
    
    public synchronized String element2id(DocumentElement elem) {
        String id;
        
        if ( (id=element2idImpl(elem)) == null) {
            //TODO possibly use some small scale refresh, that 
            // tries to refresh only part of the document
            System.out.println("Refreshing");
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
    
    protected String generateId() {
        String id;
        do {
             id = "$" + m_serial++;
        } while( m_ids.containsKey(id));
        
        return id;        
    }
    
    protected void decorateElement( DocumentElement elem, StringBuilder sb) {
        assert elem != null;
        assert elem.getDocumentModel() == m_docModel : "Element " + elem + " has incorrect model " + elem.getDocumentModel();

        int childElemNum   = elem.getElementCount();

        for (int i = childElemNum - 1; i >= 0; i--) {
            DocumentElement de = elem.getElement(i);
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
}
