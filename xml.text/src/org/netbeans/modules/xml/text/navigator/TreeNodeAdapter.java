/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.text.navigator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.structure.api.DocumentElement;
import org.netbeans.modules.editor.structure.api.DocumentElementEvent;
import org.netbeans.modules.editor.structure.api.DocumentElementListener;
import org.netbeans.modules.xml.text.folding.XmlFoldTypes;
import org.netbeans.modules.xml.text.structure.XMLDocumentModelProvider;

/**
 * TreeNodeAdapter is an implementation of j.s.t.TreeNode encapsulating a DocumentElement
 * instance and listening on its changes.
 *
 * @author Marek Fukala
 */
public class TreeNodeAdapter implements TreeNode, DocumentElementListener {
    
    private DocumentElement de;
    private DefaultTreeModel tm;
    private TreeNode parent;
    private JTree tree;
    
    private ArrayList children; //a list of non-content nodes
    private ArrayList textElements; //stores wrappers of DocumentElement-s which are children of the DocumentElement held by this TreeNode
    
    private String textContent;
    
    public TreeNodeAdapter(DocumentElement de, DefaultTreeModel tm, JTree tree, TreeNode parent) {
        this(de, tm, tree, parent, true);
    }
    
    public TreeNodeAdapter(DocumentElement de, DefaultTreeModel tm, JTree tree, TreeNode parent, boolean createChildrenAdapters) {
        this.de = de;
        this.tm = tm;
        this.tree = tree;
        this.parent = parent;
        
        children = new ArrayList();
        textElements = new ArrayList();
        
        //init children adapters
        if(createChildrenAdapters) createChildrenAdapters();
        
        //attach myself to the document element as a listener
        de.addDocumentElementListener(this);
        
        //init the text content buffer
        childTextElementChanged();
    }
    
    /**Returns a text content of this node. The content is fetched from all text  document elements which
     * are children of the element held by this tree node.
     *
     * @return the text content of this node.
     */
    public String getDocumentContent() {
        return textContent;
    }
    
    public java.util.Enumeration children() {
        return Collections.enumeration(children);
    }
    
    public boolean getAllowsChildren() {
        return true;
    }
    
    public TreeNode getChildAt(int param) {
        return (TreeNode)children.get(param);
    }
    
    public int getChildCount() {
        return children.size();
    }
    
    public int getIndex(TreeNode treeNode) {
        return children.indexOf(treeNode);
    }
    
    public TreeNode getParent() {
        return parent;
    }
    
    public boolean isLeaf() {
        return getChildCount() == 0;
    }
    
    public DocumentElement getDocumentElement() {
        return de;
    }
    
    public TreeNodeAdapter getChildTreeNode(DocumentElement de) {
        for(int i = 0; i < getChildCount(); i++) {
            TreeNodeAdapter tna = (TreeNodeAdapter)getChildAt(i);
            if(tna.getDocumentElement().equals(de)) return tna;
        }
        return null;
    }
    
    public String toString() {
        if(de.getType().equals(XMLDocumentModelProvider.XML_TAG)
        || de.getType().equals(XMLDocumentModelProvider.XML_EMPTY_TAG)) {
            //XML TAG text
            String attribsVisibleText = "";
            AttributeSet attribs = getDocumentElement().getAttributes();
            
            if(attribs.getAttributeCount() > 0) {
                String attribsText = getAttribsText();
                if(NavigatorContent.showAttributes) {
                    attribsVisibleText = attribsText.length() > ATTRIBS_MAX_LEN ? attribsText.substring(0,ATTRIBS_MAX_LEN) + "..." : attribsText.toString();
                }
            }
            
            String contentText = "";
            String documentText = getDocumentContent();
            if(NavigatorContent.showContent) {
                contentText  = documentText.length() > TEXT_MAX_LEN ? documentText.substring(0,TEXT_MAX_LEN) + "..." : documentText;
            }
            
            String text = getDocumentElement().getName()
            + ((attribsVisibleText.trim().length() > 0) ? " " + attribsVisibleText : "")
            + ((contentText.trim().length() > 0) ? " "+ contentText : "");
            return text;
            
        } else if(de.getType().equals(XMLDocumentModelProvider.XML_PI)) {
            //PI text
            String documentText = getPIText();
            documentText = documentText.length() > TEXT_MAX_LEN ? documentText.substring(0,TEXT_MAX_LEN) + "..." : documentText;
            return documentText;
        } else if(de.getType().equals(XMLDocumentModelProvider.XML_DOCTYPE)) {
            //limit the text length
            String documentText = getDoctypeText();
            String visibleText  = documentText.length() > TEXT_MAX_LEN ? documentText.substring(0,TEXT_MAX_LEN) + "..." : documentText;
            return visibleText;
        } else if(de.getType().equals(XMLDocumentModelProvider.XML_CDATA)) {
            //limit the text length
            String documentText = getCDATAText();
            String visibleText  = documentText.length() > TEXT_MAX_LEN ? documentText.substring(0,TEXT_MAX_LEN) + "..." : documentText;
            return visibleText;
        }
        
        return de.getName() + " [unknown content]";
    }
    
    public String getToolTipText() {
        if(de.getType().equals(XMLDocumentModelProvider.XML_TAG)
                || de.getType().equals(XMLDocumentModelProvider.XML_EMPTY_TAG)) {
            return getAttribsText() + " " + getDocumentContent();
        } else if(de.getType().equals(XMLDocumentModelProvider.XML_PI)) {
            return getPIText();
        } else if(de.getType().equals(XMLDocumentModelProvider.XML_DOCTYPE)) {
            return getDoctypeText();
        } else if(de.getType().equals(XMLDocumentModelProvider.XML_CDATA)) {
            return getCDATAText();
        }
        return "";
    }
    
    private String getPIText() {
        String documentText = null;
        try {
            documentText = de.getDocumentModel().getDocument().getText(de.getStartOffset(), de.getEndOffset() - de.getStartOffset());
            //cut the leading PI name and the <?
            if(documentText.length() > 0) documentText = documentText.substring("<?".length() + de.getName().length(), documentText.length() - 1).trim();
        }catch(BadLocationException e) {
            return "???";
        }
        return documentText;
    }
    
    private String getDoctypeText() {
        String documentText = "???";
        try {
            documentText = de.getDocumentModel().getDocument().getText(de.getStartOffset(), de.getEndOffset() - de.getStartOffset());
            //cut the leading PI name and the <?
            if(documentText.length() > 0) documentText = documentText.substring("<!DOCTYPE ".length() + de.getName().length(), documentText.length() - 1).trim();
        }catch(BadLocationException e) {
            return "???";
        }
        return documentText;
    }
    
    private String getCDATAText() {
        String documentText = "???";
        try {
            documentText = de.getDocumentModel().getDocument().getText(de.getStartOffset(), de.getEndOffset() - de.getStartOffset());
            //cut the leading PI name and the <?
            if(documentText.length() > 0) documentText = documentText.substring("<![CDATA[".length(), documentText.length() - "]]>".length()).trim();
        }catch(BadLocationException e) {
            return "???";
        }
        return documentText;
    }
    
    public void childrenReordered(DocumentElementEvent ce) {
        //notify treemodel - do that in event dispath thread
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    tm.nodeStructureChanged(TreeNodeAdapter.this);
                }
            });
        }catch(Exception ie) {
            ie.printStackTrace(); //XXX handle somehow
        }
    }
    
    public String getAttribsText() {
        StringBuffer attribsText = new StringBuffer();
        Enumeration attrNames = getDocumentElement().getAttributes().getAttributeNames();
        if(attrNames.hasMoreElements()) {
            attribsText.append("(");
            while(attrNames.hasMoreElements()) {
                String aname = (String)attrNames.nextElement();
                String value = (String)getDocumentElement().getAttributes().getAttribute(aname);
                attribsText.append(aname + "=" + value);
                if(attrNames.hasMoreElements()) attribsText.append(", ");
            }
            attribsText.append(")");
        }
        return attribsText.toString();
    }
    
    public void elementAdded(DocumentElementEvent e) {
        DocumentElement ade = e.getChangedChild();
        
        if(debug) System.out.println(">>> +EVENT called on " + hashCode() + " - " + de + ": element " + ade + " is going to be added");
        
        if(ade.getType().equals(XMLDocumentModelProvider.XML_CONTENT)) {
            //create a text node listener
            textElements.add(new TextElementWrapper(ade));
            //update text text content of the node
            childTextElementChanged();
        } else {
            TreeNode tn = new TreeNodeAdapter(ade, tm, tree, this, true); //do not create children adapters here!!!
            int insertIndex = getIndexOfNonTextChildren(ade);
            //add the element only when there isn't such one
            if(getChildTreeNode(ade) == null) {
                children.add(insertIndex, tn);
                final int tnIndex = getIndex(tn);
                //notify treemodel in event dispatch thread
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            tm.nodesWereInserted(TreeNodeAdapter.this, new int[]{tnIndex});
                        }
                    });
                }catch(Exception ie) {
                    ie.printStackTrace(); //XXX handle somehow better
                }
            }
            if(debug)System.out.println("<<<EVENT finished (node " + tn + " added)");
        }
        
        //fix: if a new nodes are added into the root element (set as invisible), the navigator
        //window is empty. So we need to always expand the root element when adding something into
        if(de.equals(de.getDocumentModel().getRootElement())) {
            //expand path
            tree.expandPath(new TreePath(this));
        }
        
    }
    
    private int getIndexOfNonTextChildren(DocumentElement de) {
        int index = 0;
        Iterator children = getDocumentElement().getChildren().iterator();
        while(children.hasNext()) {
            DocumentElement child = (DocumentElement)children.next();
            if(child.equals(de)) return index;
            
            if(!child.getType().equals(XMLDocumentModelProvider.XML_CONTENT)) index++;
            
        }
        return -1;
    }
    
    public void elementRemoved(DocumentElementEvent e) {
        DocumentElement rde = e.getChangedChild();
        
        if(debug) System.out.println(">>> -EVENT on " + hashCode() + " - " + de + ": element " + rde + " is going to be removed ");
        
        if(rde.getType().equals(XMLDocumentModelProvider.XML_CONTENT)) {
            if(debug) System.out.println(">>> removing CONTENT element");
            //remove the text eleemnt listener
            Iterator i = textElements.iterator();
            ArrayList toRemove = new ArrayList();
            while(i.hasNext()) {
                TextElementWrapper tew = (TextElementWrapper)i.next();
                if(rde.equals(tew.getDocumentElement())) toRemove.add(tew);
            }
            textElements.removeAll(toRemove);
            //update text text content of the node
            childTextElementChanged();
        } else {
            if(debug) System.out.println(">>> removing tag element");
            final TreeNode tn = getChildTreeNodeForDE(rde);
            final int tnIndex = getIndex(tn);
            
            if(tn != null) {
                children.remove(tn);
                //notify treemodel - do that in event dispath thread
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            tm.nodesWereRemoved(TreeNodeAdapter.this, new int[]{tnIndex}, new Object[]{tn});
                        }
                    });
                }catch(Exception ie) {
                    ie.printStackTrace(); //XXX handle somehow
                }
                
            } else if(debug) System.out.println("Warning: TreeNode for removed element doesn't exist!!!");
            
        }
        if(debug) System.out.println("<<<EVENT finished (node removed)");
    }
    
    public void attributesChanged(DocumentElementEvent e) {
        if(debug)System.out.println("Attributes of treenode " + this + " has changed.");
        tm.nodeChanged(this);
    }
    
    public void contentChanged(DocumentElementEvent e) {
        if(debug) System.out.println("treenode " + this + " changed.");
        tm.nodeChanged(this);
    }
    
    //---- private -----
    
    private TreeNode getChildTreeNodeForDE(DocumentElement de) {
        Iterator i = children.iterator();
        while(i.hasNext()) {
            TreeNodeAdapter tn = (TreeNodeAdapter)i.next();
            if(tn.getDocumentElement().equals(de)) return tn;
        }
        return null;
    }
    
    private void createChildrenAdapters() {
        Iterator i = de.getChildren().iterator();
        while(i.hasNext()) {
            DocumentElement chde = (DocumentElement)i.next();
            if(!chde.getType().equals(XMLDocumentModelProvider.XML_CONTENT)) {
                //add the adapter only when there isn't any
                if(getChildTreeNode(chde) == null) children.add(new TreeNodeAdapter(chde, tm, tree, this));
            } else {
                //create a text node listener
                textElements.add(new TextElementWrapper(chde));
                //update text text content of the node
                childTextElementChanged();
            }
        }
    }
    
    public void childTextElementChanged() {
        //get all text elements children of the document element held by this node
        Iterator children = getDocumentElement().getChildren().iterator();
        StringBuffer buf = new StringBuffer();
        while(children.hasNext()) {
            DocumentElement del = (DocumentElement)children.next();
            if(del.getType().equals(XMLDocumentModelProvider.XML_CONTENT)) {
                try {
                    buf.append((del.getDocument().getText(del.getStartOffset(), del.getEndOffset() - del.getStartOffset())).trim());
                }catch(BadLocationException e) {
                    buf.append("???");
                }
            }
        }
        textContent = buf.toString();
        //fire a change event for this node
        tm.nodeChanged(this);
    }
    
    private final class TextElementWrapper implements DocumentElementListener {
        
        private DocumentElement de;
        
        public TextElementWrapper(DocumentElement de) {
            this.de = de;
            de.addDocumentElementListener(TextElementWrapper.this);
        }
        
        public DocumentElement getDocumentElement() {
            return de;
        }
        
        public void contentChanged(DocumentElementEvent e) {
            TreeNodeAdapter.this.childTextElementChanged();
        }
        
        //no need to implement these methods
        public void elementAdded(DocumentElementEvent e) {
            //just a test
            System.err.println("????? a child node added into a text element!!!!");
        }
        public void elementRemoved(DocumentElementEvent e) {}
        public void childrenReordered(DocumentElementEvent e) {}
        public void attributesChanged(DocumentElementEvent e) {}
        
    }
    
    private static final boolean debug = Boolean.getBoolean("org.netbeans.modules.xml.text.structure.debug");
    
    private static final int ATTRIBS_MAX_LEN = 30;
    private static final int TEXT_MAX_LEN = ATTRIBS_MAX_LEN;
    
}
