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

import com.sun.perseus.model.ModelNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
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
import org.netbeans.modules.mobility.svgcore.composer.PerseusController;
import org.netbeans.modules.mobility.svgcore.composer.prototypes.PatchedGroup;
import org.netbeans.modules.xml.multiview.XmlMultiViewEditorSupport;
import org.w3c.dom.svg.SVGElement;

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
        public void modelChanged( int [] path);
    }

    public interface SelectionListener {
        public void selectionChanged( int [] path);
    }

    private final XmlMultiViewEditorSupport edSup;
    private       List<ModelListener>       modelListeners     = new ArrayList<ModelListener>();
    private       List<SelectionListener>   selectionListeners = new ArrayList<SelectionListener>();
    private       BaseDocument              bDoc;
    private       EditorKit                 kit;
    private       DocumentModel             m_model;
    private       boolean                   isChanged = false;
    
    private final DocumentListener          docListener = new DocumentListener() {
        public void removeUpdate(DocumentEvent e) {documentModified();}
        
        public void insertUpdate(DocumentEvent e) {documentModified();}
        
        public void changedUpdate(DocumentEvent e) {documentModified();}
    };
    
    private final DocumentModelListener    modelListener = new DocumentModelListener() {
        public void documentElementRemoved(DocumentElement de) {
            if (isTagElement(de)) {
                System.out.println("Element removed " + de.getName() + " " + de.toString()  + "[" + de.getElementCount() + "]");
                fireModelChange(null);
            }
        }

        public void documentElementChanged(DocumentElement de) {
            if (isTagElement(de)) {
                System.out.println("Element changed " + de.getName() + " " + de.toString()  + "[" + de.getElementCount() + "]");
                fireModelChange( getIndexedPath(de));
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
                fireModelChange( getIndexedPath(de));
            }
        }

        public void documentElementAdded(DocumentElement de) {
            if (isTagElement(de)) {
                System.out.println("Element added " + de.getName() + " " + de.toString() + "[" + de.getElementCount() + "]");
                fireModelChange( getIndexedPath(de));
            }
        }        
    };

    /** Creates a new instance of SVGFileModel */
    public SVGFileModel(XmlMultiViewEditorSupport edSup) {
        this.edSup      = edSup;
        m_model      = null;
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
    
    public void setSelected( int [] path) {
        synchronized(selectionListeners) {
            for (int i = 0; i < selectionListeners.size(); i++) {
                ((SelectionListener) selectionListeners.get(i)).selectionChanged(path);
            }
        }
    }
    
    protected void documentModified() {
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
        //TODO should be called from AWT Thread
        JEditorPane [] panes = edSup.getOpenedPanes();
        
        if (panes != null && panes.length > 0) {
            return panes[0];
        } else {
            return null;
        }
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
    
    public synchronized String writeToString() throws Exception {
        checkDocument();
        return bDoc.getText(0, bDoc.getLength());
    }
    
    public synchronized int getElementStartOffset( DocumentElement del) throws IOException, DocumentModelException {
        return del.getStartOffset();
    }
    
    public synchronized int getElementStartOffset( int [] pathIndexes) throws IOException, DocumentModelException {
        checkDocument();
        int             offset;
        DocumentElement elem = findElement(pathIndexes);
        
        if ( elem != null) {
            offset = elem.getStartOffset();
        } else {
            offset = -1;
        }
        
        return offset;
    }
    
    static boolean isTagElement(DocumentElement elem) {
        return elem != null && (elem.getType().equals(XML_TAG) ||
               elem.getType().equals(XML_EMPTY_TAG));
    }

    static boolean isError(DocumentElement elem) {
        return elem.getType().equals(XML_ERROR_TAG);
    }
    
    public DocumentElement findElement(int [] pathIndexes) {
        DocumentElement element = m_model.getRootElement();
        
        main_loop: for (int i = 0; i < pathIndexes.length; i++)  {
            int childNum = element.getElementCount();
            int index = 0;
            for (int j = 0; j < childNum; j++) {
                DocumentElement child = element.getElement(j);
                
                if ( isTagElement(child)) {
                    if ( pathIndexes[i] == index) {
                        element = child;
                        continue main_loop;
                    } else {
                        index++;
                    }
                }
            }
            element = null;
            break;
        }
        
        return element;
    }
    
    public int [] getIndexedPath( DocumentElement de) {
        assert isTagElement(de);
        assert de.getDocumentModel() == m_model : "The element is no longer in the current document";
        
        int [] path = null;
        
        try {
            List<DocumentElement> objectPath = new ArrayList<DocumentElement>();
            do {
                objectPath.add(de);
                de = de.getParentElement();
            } while( de != null);

            de = m_model.getRootElement();
            assert de == objectPath.get(objectPath.size()-1);

            path = new int[objectPath.size()-1];

            main_loop: for (int i = 0; i < path.length; i++)  {
                int    childNum = de.getElementCount();
                int    index    = 0;
                Object o        = objectPath.get( path.length - i - 1);

                for (int j = 0; j < childNum; j++) {
                    DocumentElement child = de.getElement(j);

                    if ( isTagElement(child)) {
                        if (child == o) {
                            path[i] = index;
                            de      = child;
                            continue main_loop;
                        } else {
                            index++;
                        }
                    }
                }
                path = null;
                break;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        return path;       
    }
    
    public DocumentModel getDocumentModel() {
        return m_model;
    }

    public DocumentElement findElement(String id) {
        DocumentElement element = m_model.getRootElement();
        int idLength = id.length();
        int pos = 0;
        
        main_loop: while( pos < idLength) {
            int elemIndex = 0;
            char c;
            while ( Character.isDigit(c=id.charAt(pos))) {
                elemIndex = (elemIndex * 10) + (c - '0');
                pos++;
            }
            int childNum = element.getElementCount();
            int index = 0;
            for (int j = 0; j < childNum; j++) {
                DocumentElement child = element.getElement(j);    
                if ( isTagElement(child)) {
                    if (index == elemIndex) {
                        String name       = child.getName();
                        int    nameLength = name.length();
                        
                        if ( isEqual(id, name, pos, 0, nameLength)) {
                            pos += nameLength;
                            element = child;
                            continue main_loop;
                        }
                    } else {
                        index++;
                    }
                }
            }
            return null;
        }
        
        return element;        
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
        DocumentElement elem = findElement(id); 
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
        DocumentElement parent = findElement(id);
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
    
    public synchronized String describeElement(int [] path, boolean showTag, boolean showAttributes, String lineSep) {
        DocumentElement el = findElement(path);
        if (el != null) {
            return describeElement( el, showTag, showAttributes, lineSep);
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
                        ((ModelListener) modelListeners.get(i)).modelChanged(null);
                    }
                }
            } finally {
                eventInProgress = false;
            }
        }
    };
    
    protected synchronized void fireModelChange(int [] path) {
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
    
    public void deleteElement(int [] path) throws BadLocationException {
        assert path != null : "Element not found";
        DocumentElement de = findElement(path);
        assert de != null : "No element at path: " + PerseusController.toString(path);
        //TODO any locking??
        int startOff = de.getStartOffset();
        bDoc.remove(startOff, de.getEndOffset() - startOff + 1);
    }
    
    public void wrapElement(int [] path, String text) throws BadLocationException {
        assert path != null : "Element not found";
        DocumentElement de = findElement(path);
        assert de != null : "No element at path: " + PerseusController.toString(path);
        checkIntegrity(de);
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
    public void moveToBottom(int [] path) throws BadLocationException {
        assert path != null : "Element not found";
        DocumentElement de = findElement(path);
        assert de != null : "No element at path: " + PerseusController.toString(path);
        checkIntegrity(de);
        
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
    public void moveToTop(int [] path) throws BadLocationException {
        assert path != null : "Element not found";
        DocumentElement de = findElement(path);
        assert de != null : "No element at path: " + PerseusController.toString(path);
        checkIntegrity(de);
        
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
    public void moveForward(int [] path) throws BadLocationException {
        assert path != null : "Element not found";
        DocumentElement de = findElement(path);
        assert de != null : "No element at path: " + PerseusController.toString(path);
        checkIntegrity(de);
        
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
    public void moveBackward(int [] path) throws BadLocationException {
        assert path != null : "Element not found";
        DocumentElement de = findElement(path);
        assert de != null : "No element at path: " + PerseusController.toString(path);
        checkIntegrity(de);
        
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
    
    public void synchronize(ModelNode perseusRoot) throws BadLocationException {
        DocumentElement element = m_model.getRootElement();
        //TODO lock document?
        synchronize( element, perseusRoot);
    }
    
    public void synchronize(DocumentElement docElem, ModelNode perseusNode) throws BadLocationException {
        assert docElem != null;
        assert perseusNode != null;
        
        //TODO check if works for multiple synchronisation changes
        if ( perseusNode instanceof PatchedGroup) {
            PatchedGroup pg = (PatchedGroup) perseusNode;
            if (pg.isChanged()) {
                int startOff = docElem.getStartOffset();
                if ( docElem.getElementCount() > 0) {
                    int endOff = docElem.getElement(0).getStartOffset();
                    String text = ((PatchedGroup)perseusNode).getText();
                    bDoc.replace(startOff, endOff - startOff, text, null);
                } else {
                    System.err.println("Invalid wrapper");
                }
                pg.setChanged(false);
            }
        }
        
        int childElemNum = docElem.getElementCount();
        ModelNode childNode = perseusNode.getFirstChildNode();
        for (int i = 0; i < childElemNum; i++) {
            DocumentElement childElem = docElem.getElement(i);
            if ( isTagElement(childElem)) {
                if (childNode != null) {
                    synchronize(childElem, childNode);
                    childNode = childNode.getNextSiblingNode();
                } else {
                    System.err.println("Inconsistent tree structure");
                    break;
                }
            }
        }
        
        if (childNode != null) {
            System.err.println("Inconsistent tree structure");
        }
    }
    
    private void checkIntegrity(DocumentElement de) {
        
        if ( de.getDocumentModel() != m_model ||
             de.getDocument() != bDoc) {
            System.out.println("Element is not part of the current document");
            throw new RuntimeException("Element is not part of the current document");
        }
    }    
}
