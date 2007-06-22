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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.microedition.m2g.SVGImage;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.structure.api.DocumentElement;
import org.netbeans.modules.editor.structure.api.DocumentModel;
import org.netbeans.modules.editor.structure.api.DocumentModelException;
import org.netbeans.modules.editor.structure.api.DocumentModelListener;
import org.netbeans.modules.mobility.svgcore.SVGDataLoader;
import org.netbeans.modules.mobility.svgcore.model.ElementMapping;
import org.netbeans.modules.xml.multiview.XmlMultiViewEditorSupport;
import org.openide.util.Exceptions;

/**
 *
 * @author Pavel Benes
 */
public class SVGFileModel {
    protected static final String XML_TAG       = "tag";
    protected static final String XML_EMPTY_TAG = "empty_tag";
    protected static final String XML_ERROR_TAG = "error";
    protected static String [] ANIMATION_TAGS = { "animate", "animateTransform", "animateMotion", "animateColor"};
    
    
    public interface ModelListener {
        public void modelChanged();
    }

    public interface SelectionListener {
        public void selectionChanged( String id);
    }

    private final XmlMultiViewEditorSupport edSup;
    private final ElementMapping            m_mapping;
    private       List<ModelListener>       modelListeners     = new ArrayList<ModelListener>();
    private       List<SelectionListener>   selectionListeners = new ArrayList<SelectionListener>();
    private       BaseDocument              bDoc;
    private       EditorKit                 kit;
    private       DocumentModel             m_model;
    private       boolean                   isChanged = true;
    
    private final DocumentListener          docListener = new DocumentListener() {
        public void removeUpdate(DocumentEvent e) {documentModified();}
        
        public void insertUpdate(DocumentEvent e) {documentModified();}
        
        public void changedUpdate(DocumentEvent e) {documentModified();}
    };
    
    private final DocumentModelListener    modelListener = new DocumentModelListener() {
        public void documentElementRemoved(DocumentElement de) {
            if (isTagElement(de)) {
                System.out.println("Element removed " + de.getName() + " " + de.toString()  + "[" + de.getElementCount() + "]");
                fireModelChange();
            }
        }

        public void documentElementChanged(DocumentElement de) {
            if (isTagElement(de)) {
                System.out.println("Element changed " + de.getName() + " " + de.toString()  + "[" + de.getElementCount() + "]");
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
                System.out.println("Element attrs changed " + de.getName() + " " + de.toString()  + "[" + de.getElementCount() + "]");
                fireModelChange();
            }
        }

        public void documentElementAdded(DocumentElement de) {
            if (isTagElement(de)) {
                String id = getIdAttribute(de);
                if (id != null) {
                    m_mapping.add( id, de);
                }
                System.out.println("Element added " + de.getName() + " " + de.toString() + "[" + de.getElementCount() + "]");
                fireModelChange();
            }
        }        
    };

    /** Creates a new instance of SVGFileModel */
    public SVGFileModel(XmlMultiViewEditorSupport edSup) {
        this.edSup = edSup;
        m_model    = null;
        m_mapping  = new ElementMapping();
    }        

    public SVGImage parseSVGImage() throws IOException, BadLocationException {
        SVGImage svgImage = m_mapping.parseDocument(this, m_model);
        return svgImage;
    }
        
    public boolean isChanged() {
        return isChanged;
    }
    
    public void setChanged(boolean isChanged) {
        this.isChanged = isChanged;
    }
    
    public void addModelListener( ModelListener listener) {
        synchronized( modelListeners) {
            modelListeners.add(listener);
        }
    }

    public void removeModelListener( ModelListener listener) {
        synchronized( modelListeners) {
            modelListeners.remove(listener);
        }
    }

    public void addSelectionListener( SelectionListener listener) {
        synchronized( selectionListeners) {
            selectionListeners.add(listener);
        }
    }

    public void removeSelectionListener( SelectionListener listener) {
        synchronized( selectionListeners) {
            selectionListeners.remove(listener);
        }
    }
    
    public void setSelected(String selectedId) {
        synchronized(selectionListeners) {
            for (int i = 0; i < selectionListeners.size(); i++) {
                ((SelectionListener) selectionListeners.get(i)).selectionChanged(selectedId);
            }
        }
    }
    
    private long m_changedTime;
    
    protected void documentModified() {
        m_changedTime = System.currentTimeMillis();
        setChanged(true);
    }
    
    public boolean isModelStable() {
        return System.currentTimeMillis() - m_changedTime > 2000;
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
            JEditorPane [] temp = edSup.getOpenedPanes();
            if (temp != null && temp.length > 0) {
                panes[0] = temp[0];
            }
        } else {
            try {
                SwingUtilities.invokeAndWait( new Runnable() {
                    public void run() {
                        JEditorPane [] temp = edSup.getOpenedPanes();
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
        
        if (bDoc == null) {
            if (editor != null) {
                // reuse document from opened editor, if exists
                kit = editor.getEditorKit();
                createModel( (BaseDocument) editor.getDocument(), true);
            } else {
                // otherwise create the document from scratch
                kit = JEditorPane.createEditorKitForContentType(SVGDataLoader.REQUIRED_MIME);
                //Use in NB 6.0
                //Editor kit = CloneableEditorSupport.getEditorKit( SVGDataLoader.REQUIRED_MIME);
                
                //TODO Read file content in another thread
                createModel( (BaseDocument) kit.createDefaultDocument(), false);
                try {
                    kit.read( edSup.getInputStream(), bDoc, 0);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }
        } else {
            if (editor != null) {
                BaseDocument opened = (BaseDocument) editor.getDocument();
                if ( opened != bDoc) {
                    bDoc.removeDocumentListener(docListener);
                    kit   = editor.getEditorKit();
                    createModel(opened, true);
                }
            }
        }
        assert m_model != null;
        assert kit   != null;
        assert bDoc  != null;
    }

    private void createModel( BaseDocument doc, boolean addListener) throws DocumentModelException {
        bDoc  = doc;
        m_model = DocumentModel.getDocumentModel(bDoc);
        
        if (addListener) {
            bDoc.addDocumentListener(docListener);
            m_model.addDocumentModelListener(modelListener);
        }
    }
    
    public synchronized DocumentModel _getModel() throws Exception {
        checkDocument();
        return m_model;
    }
    
    public synchronized int getElementStartOffset( DocumentElement del) throws IOException, DocumentModelException {
        return del.getStartOffset();
    }
    
    public synchronized int getElementStartOffset( String id) throws IOException, DocumentModelException {
        checkDocument();
        int             offset;
        DocumentElement elem = getElementById(id);
        
        if ( elem != null) {
            offset = elem.getStartOffset();
        } else {
            offset = -1;
        }
        
        return offset;
    }
    
    public static boolean isTagElement(DocumentElement elem) {
        return elem != null && (elem.getType().equals(XML_TAG) ||
               elem.getType().equals(XML_EMPTY_TAG));
    }

    public static boolean isError(DocumentElement elem) {
        return elem.getType().equals(XML_ERROR_TAG);
    }
    
    public static String getIdAttribute(DocumentElement de) {
        AttributeSet attrs = de.getAttributes();
        String id = (String) attrs.getAttribute("id");
        return id;
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
    
    public DocumentModel _getDocumentModel() {
        return m_model;
    }
    
    protected static boolean isEqual(String str1, String str2, int pos1, int pos2, int length) {
        if ( pos1 + length <= str1.length() &&
             pos2 + length <= str2.length()) {
            for (int i = 0; i < length; i++) {
                if ( str1.charAt(pos1 + i) != str2.charAt(pos2+i)) {
                    return false;
                }
            }
            return true;
        }
        
        return false;
    }
    
    public boolean isLeaf(String id) {
        DocumentElement elem = getElementById(id); 
        return elem != null ? elem.isLeaf() : true;
    }

    public List getChildElements( DocumentElement parent) {
        int  elemCount = parent.getElementCount();  
        List<DocumentElement> elemList  = new ArrayList<DocumentElement>(elemCount/2);
        for (int i = 0; i < elemCount; i++) {
            DocumentElement deChild = parent.getElement(i);
            if ( isTagElement(deChild)) {
                elemList.add(deChild);
            }
        }
        return elemList;
    }
        
    public List getChildElements( String id) {
        DocumentElement parent = getElementById(id);
        if (parent != null) {
            StringBuilder sb = new StringBuilder(id);
            int size = sb.length();
            
            int  elemCount = parent.getElementCount();  
            List<String> elemList  = new ArrayList<String>(elemCount/2);
            int index = 0;
            for (int i = 0; i < elemCount; i++) {
                DocumentElement deChild = parent.getElement(i);
                if ( isTagElement(deChild)) {
                    sb.append(index++);
                    sb.append(deChild.getName());
                    elemList.add(sb.toString());
                    sb.setLength(size);
                }
            }  
            return elemList;
        } else {
            return null;
        }
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

    private boolean eventInProgress = false;

    private final Runnable updateTask = new Runnable() {
        public void run() {
            System.out.println("Updating ...");
            try {
                synchronized( modelListeners) {
                    for (int i = 0; i < modelListeners.size(); i++) {
                        ((ModelListener) modelListeners.get(i)).modelChanged();
                    }
                }
            } finally {
                System.out.println("Update completed");
                eventInProgress = false;
            }
        }
    };
    
    protected synchronized void fireModelChange() {
        if ( !eventInProgress) {
            System.out.println("Asking for update");
            SwingUtilities.invokeLater( updateTask);
            eventInProgress = true;
        }
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
    
    public void deleteElement(String id) throws BadLocationException {
        DocumentElement de = checkIntegrity(id);
        
        //TODO any locking??
        int startOff = de.getStartOffset();
        bDoc.remove(startOff, de.getEndOffset() - startOff + 1);
    }
    
    public void wrapElement(String id, String text) throws BadLocationException {
        DocumentElement de = checkIntegrity(id);
        
        //TODO any locking??
        int startOff = de.getStartOffset();
        int length   = de.getEndOffset() - startOff + 1;

        bDoc.replace(startOff, length,
                     wrapText(text, bDoc.getText(startOff, length)), null);
    }
    
    public static DocumentElement getSVGRoot(final DocumentModel model) {
        DocumentElement root = model.getRootElement();
        for (int i = root.getElementCount() - 1; i >= 0; i--) {
            DocumentElement de = root.getElement(i);
            if (isTagElement(de) && "svg".equals(de.getName())) {
                return de;
            }
        }
        return null;
    }
    
    private static String wrapText(String group, String text) {
        //TODO indent the wrapped text
        StringBuilder sb = new StringBuilder();
        sb.append( "\n");
        sb.append(group);
        sb.append("\n");
        sb.append(text);
        sb.append("\n</g>");
        return sb.toString();
    }
    
    public void appendElement( String groupHeader, String childXMLText) throws BadLocationException {
        DocumentElement svgRoot = getSVGRoot(m_model);
        
        if (svgRoot != null) {
            int svgRootChildNum = svgRoot.getElementCount();
            int insertPosition;

            if (svgRootChildNum > 0) {
                // insert new text after last child
                insertPosition = svgRoot.getElement(svgRootChildNum-1).getEndOffset();
                bDoc.insertString(insertPosition, wrapText(groupHeader, childXMLText), null);
            } else {
                //TODO implement
                throw new RuntimeException("Insert into empty document not implemented.");
            }
        } else {
            //TODO handle situation when no SVG root element is found
        }
    }

    /**
     * Make the selected element to become the first child of its parent.
     */
    public void moveToBottom(String id) throws BadLocationException {
        DocumentElement de = checkIntegrity(id);
        
        DocumentElement parent = de.getParentElement();
        DocumentElement firstChild = parent.getElement(0);
        if (de != firstChild) {
            //TODO lock document??
            int startOffset = de.getStartOffset();
            int length      = de.getEndOffset() - startOffset + 1;
            String elemText = bDoc.getText(startOffset, length);
            
            int insertOffset = firstChild.getStartOffset();
            assert insertOffset < startOffset;
            
            bDoc.remove(startOffset, length);
            bDoc.insertString(insertOffset, elemText, null);
        }
    }
    
    /**
     * Make the selected element to become the last child of its parent.
     */
    public void moveToTop(String id) throws BadLocationException {
        DocumentElement de = checkIntegrity(id);
        
        DocumentElement parent = de.getParentElement();
        DocumentElement lastChild = parent.getElement(parent.getElementCount() - 1);
        if (de != lastChild) {
            //TODO lock document??
            int startOffset = de.getStartOffset();
            int length      = de.getEndOffset() - startOffset + 1;
            String elemText = bDoc.getText(startOffset, length);
            
            int insertOffset = lastChild.getEndOffset();
            assert startOffset < insertOffset;
            
            bDoc.insertString(insertOffset, elemText, null);
            bDoc.remove(startOffset, length);
        }
    }

    /**
     * Move the selected element one position to the end of a list of its siblings.
     */
    public void moveForward(String id) throws BadLocationException {
        DocumentElement de = checkIntegrity(id);
        
        DocumentElement parent = de.getParentElement();
        DocumentElement lastChild = parent.getElement(parent.getElementCount() - 1);
        if (de != lastChild) {
            //TODO lock document??
            int startOffset = de.getStartOffset();
            int length      = de.getEndOffset() - startOffset + 1;
            String elemText = bDoc.getText(startOffset, length);
            
            int index = getDocumentElementIndex(de);
            DocumentElement nextChild = parent.getElement(index+1);
                        
            int insertOffset = nextChild.getEndOffset() + 1;
            assert startOffset < insertOffset;
            
            bDoc.insertString(insertOffset, elemText, null);
            bDoc.remove(startOffset, length);
        }
    }

    /**
     * Move the selected element one position to the beginning of a list of its siblings.
     */
    public void moveBackward(String id) throws BadLocationException {
        DocumentElement de = checkIntegrity(id);
        
        DocumentElement parent = de.getParentElement();
        DocumentElement firstChild = parent.getElement(0);
        if (de != firstChild) {
            //TODO lock document??
            int startOffset = de.getStartOffset();
            int length      = de.getEndOffset() - startOffset + 1;
            String elemText = bDoc.getText(startOffset, length);
            
            int index = getDocumentElementIndex(de);
            DocumentElement previousChild = parent.getElement(index-1);
                        
            int insertOffset = previousChild.getStartOffset();
            assert startOffset > insertOffset;
            
            bDoc.remove(startOffset, length);
            bDoc.insertString(insertOffset, elemText, null);
        }
    }
    
    private static int getDocumentElementIndex(DocumentElement de) {
        DocumentElement parent = de.getParentElement();
        int childNum = parent.getElementCount();
        for (int i = 0; i < childNum; i++) {
            if (parent.getElement(i) == de) {
                return i;
            }
        }
        throw new RuntimeException("The document element " + de + " is no longer part of the document");
    }
    
    /*
    public void synchronize(ModelNode perseusRoot) throws BadLocationException {
        System.out.println("Synchronization started ...");
        DocumentElement element = m_model.getRootElement();
        //TODO lock document?
        synchronize( element, perseusRoot, 0);
        System.out.println("Synchronization ended.");
    }
    */
    
    public void setAttributeLater(final String id, final String attrName, final String attrValue) throws BadLocationException {
        DocumentElement de = m_mapping.id2element(id);
        if (de != null) {
            setAttribute(id, attrName, attrValue);
        } else {
            m_mapping.scheduleTask(id,new Runnable() {
                public void run() {
                    try {
                        setAttribute(id, attrName, attrValue);
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        }
    }
    
    private void setAttribute( String id, String attrName, String attrValue) throws BadLocationException {
        DocumentElement elem = checkIntegrity(id);
        assert isTagElement(elem) : "Attribute change allowed only for tag elements";
        
        int startOff = elem.getStartOffset() + 1 + elem.getName().length();        
        int endOff;
        
        if ( elem.getElementCount() > 0) {
            endOff = elem.getElement(0).getStartOffset() - 1;
        } else {
            endOff = elem.getEndOffset() - 1;
        }
        String fragment = bDoc.getText(startOff, endOff - startOff + 1);
        int p;
        if ( (p=fragment.indexOf(attrName)) != -1) {
            p += attrName.length();
            while( ++p < fragment.length()) {
                if (fragment.charAt(p) =='"') {
                    int q = p;

                    while( ++q < fragment.length()) {
                        if (fragment.charAt(q) =='"') {
                            p++;
                            bDoc.replace(startOff + p, q-p , attrValue, null);
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
            bDoc.insertString(startOff, sb.toString(), null);
        }
    }
    /*
    private void synchronize(DocumentElement docElem, ModelNode perseusNode, int level) throws BadLocationException {
        assert docElem != null;
        assert perseusNode != null;
        
        //TODO check if works for multiple synchronisation changes
        if ( perseusNode instanceof PatchedGroup) {
            PatchedGroup pg = (PatchedGroup) perseusNode;
            
            if (pg.isChanged()) {
                changeAttribute(docElem, "transform", ((PatchedGroup)perseusNode).getTransformAsText());
                pg.setChanged(false);
            }
        }
        
        int childElemNum = docElem.getElementCount();
        ModelNode childNode = perseusNode.getFirstChildNode();
        System.out.println(level+"synchronizing " + perseusNode + "->" + docElem);
        for (int i = 0; i < childElemNum; i++) {
            DocumentElement childElem = docElem.getElement(i);
            if ( isTagElement(childElem)) {
                if (childNode != null) {
                    synchronize(childElem, childNode, level + 1);
                    childNode = childNode.getNextSiblingNode();
                } else {
                    System.err.println("Extra element:" + childElem);
                    break;
                }
            }
        }
        
        if (childNode != null &&
            !(childNode instanceof SVGSVGElement) &&    
            !PerseusController.isViewBoxMarker(childNode)) {
            System.err.println("Inconsistent tree structure");
        }
    }
    */
    private void checkIntegrity(DocumentElement de) {        
        assert de != null;
        assert de.getDocument() == bDoc : "Element is not part of the current document";
        assert de.getDocumentModel() == m_model : "Element is not part of the current document model";
    }    
    
    private DocumentElement checkIntegrity(String id) {
        DocumentElement de = getElementById(id);
        assert de != null : "No element with id: " + id;
        checkIntegrity(de);
        return de;
    }    
}
