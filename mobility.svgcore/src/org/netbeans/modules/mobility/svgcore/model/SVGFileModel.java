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
import org.netbeans.modules.xml.multiview.XmlMultiViewEditorSupport;

/**
 *
 * @author Pavel Benes
 */
public class SVGFileModel {
    protected static final String XML_TAG       = "tag";
    protected static final String XML_EMPTY_TAG = "empty_tag";
    protected static final String XML_ERROR_TAG = "error";
    
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
    private       DocumentModel             model;
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
        this.model      = null;
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
        assert model != null;
        assert kit   != null;
        assert bDoc  != null;
    }

    private void createModel( BaseDocument doc, boolean addListener) throws DocumentModelException {
        bDoc  = doc;
        model = DocumentModel.getDocumentModel(bDoc);
        
        if (addListener) {
            bDoc.addDocumentListener(docListener);
            model.addDocumentModelListener(modelListener);
        }
    }
    
/*    
    public static class StreamDescriptor {
        public Thread      th;
        public InputStream in;
        
        public void init(Thread th, InputStream in) {
            this.th = th;
            this.in = in;
        }
    }
    
    public synchronized StreamDescriptor getInputStream() throws IOException, DocumentModelException  {
        checkDocument();
        
        final BaseDocument      bDocCopy = bDoc;
        System.out.println("Locking document");
        bDocCopy.readLock();
        final EditorKit         kitCopy  = kit;        
        final PipedInputStream  in       = new PipedInputStream();
        final PipedOutputStream out      = new PipedOutputStream(in);
        
        final StreamDescriptor descr = new StreamDescriptor();
        Thread th = new Thread(new Runnable() {
            public void run() {
                try {
                    System.out.println("Serializing document ...");
                    kitCopy.write( out, bDocCopy, 0, bDocCopy.getLength());
                    System.out.println("Flushing document...");
                    out.flush();
                    System.out.println("Write ended ...");
                } catch (Exception ex) {
                    System.out.println("Write failed");
                    ex.printStackTrace();
                } finally {
                    try {
                        out.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    System.out.println("Unlocking document");
                    bDocCopy.readUnlock();
                    synchronized(descr) {
                        descr.notifyAll();
                    }
                }
            }
        });
        descr.init( th, in);
        th.start();
        return descr;
    }
*/    
    public synchronized String writeToString() throws Exception {
        checkDocument();
        return bDoc.getText(0, bDoc.getLength());
    }
    
/*    
    public synchronized String getSVGHeader() throws Exception {
        checkDocument();
        return getSVGText( model.getRootElement());
    }
    private String getSVGText(DocumentElement el) throws BadLocationException {
        String text;
        if ("svg".equals(el.getName())) {
            int startOffset = el.getStartOffset();
            int endOffset;
            if (el.getElementCount() > 0) {
                endOffset = el.getElement(0).getStartOffset();
            } else {
                endOffset = el.getEndOffset();
            }
            
            String txt = bDoc.getText( startOffset, endOffset - startOffset);
            return txt;
        } else {
        int elCount = el.getElementCount();
        for (int i = 0; i < elCount; i++) {
            if ( (text=getSVGText(el.getElement(i))) != null) {
                return text;
            }
        }
        return null;
        }
    }
*/    
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
        return elem.getType().equals(XML_TAG) ||
               elem.getType().equals(XML_EMPTY_TAG);
    }

    static boolean isError(DocumentElement elem) {
        return elem.getType().equals(XML_ERROR_TAG);
    }
    
    public DocumentElement findElement(int [] pathIndexes) {
        DocumentElement element = model.getRootElement();
        
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
        assert de.getDocumentModel() == model;
        assert isTagElement(de);
        
        int [] path = null;
        
        try {
            List<DocumentElement> objectPath = new ArrayList<DocumentElement>();
            do {
                objectPath.add(de);
                de = de.getParentElement();
            } while( de != null);

            de = model.getRootElement();
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
    
    /*
    public synchronized boolean updateHierarchy( Node root, NodeFactory nodeFactory) throws IOException, DocumentModelException {
        checkDocument();
        return updateHierarchy(root, model.getRootElement(), nodeFactory);
    }
    
    protected boolean updateHierarchy( Node node, DocumentElement elem, NodeFactory nodeFactory) {
        Node [] nodeChildren = node.getChildren().getNodes();
        int     elemCount    = elem.getElementCount();
        
        boolean changed = false;
        int     index   = 0;
        
        for (int i = 0; i < elemCount; i++) {
            DocumentElement deChild = elem.getElement(i);
            if (isTagElement(deChild)) {
                if (index >= nodeChildren.length ||
                    !nodeChildren[index].getName().equals(deChild.getName())) {
                    changed = true;
                    break;
                }
                index++;
            }            
        }
        
        if (index < nodeChildren.length - 1) {
            changed = true;
        }
        
        if (!changed) {
            index = 0;
            for (int i = 0; i < elemCount; i++) {
                DocumentElement deChild = elem.getElement(i);
                if ( isTagElement(deChild)) {
                    if ( updateHierarchy( nodeChildren[index], deChild, nodeFactory)) {
                        changed = true;
                    }
                    index++;
                }
            }            
        } else {
            node.getChildren().remove(nodeChildren);
            node.getChildren().add( createHiearchy(elem, nodeFactory).getNodes());
        }
        return changed;
    }
    */
    
    /*
    public synchronized String getRootElement() throws IOException, DocumentModelException {
        checkDocument();
        return "";
    }
    */
    
    public DocumentModel getDocumentModel() {
        return model;
    }
/*    
    public synchronized ElementFilterTag getRootElement() throws IOException, DocumentModelException {
        checkDocument();
        return rootFilterTag;
    }
*/    
    public DocumentElement findElement(String id) {
        DocumentElement element = model.getRootElement();
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
/*    
    public synchronized Children createHierarchy(NodeFactory nodeFactory) throws IOException, DocumentModelException  {
        checkDocument();
        return createHiearchy( model.getRootElement(), nodeFactory);
    }
    
    protected Children createHiearchy(DocumentElement elem, NodeFactory nodeFactory) {
        int childNum = elem.getElementCount();
        
        Children.Array array = null;
        
        if (childNum > 0) {
            array = new Children.Array();
            int index = 0;
            
            for (int i = 0; i < childNum; i++) {
                DocumentElement child = elem.getElement(i);
                if ( isTagElement(child)) {
                    Children children = createHiearchy(child, nodeFactory);
                    Node node = nodeFactory.createNode(child.getName(), index, children);
                    array.add( new Node[] {node});
                    index++;
                }
            }
        }
        
        return array;
    }
 */ 
 /*
    public synchronized void visitModel( SVGContentModel.SVGContentVisitor visitor) throws Exception {
        checkDocument();
        visitElement( visitor, model.getRootElement(), null);
    }
     
    private void visitElement(SVGContentModel.SVGContentVisitor visitor,
                               DocumentElement parent, Object parentUserData) {
        int index = 0;
     
        DocumentElement child;
     
        for (int i = 0; i < parent.getElementCount(); i++) {
            child = parent.getElement(i);
     
            if ( isTagElement(child)) {
                Object userData = visitor.visitObject(child, index, parent, parentUserData);
                visitElement( visitor, child, userData);
                index++;
            }
        }
    }
     */
    
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
}
