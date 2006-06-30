/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
    
    //if the node itself contains an error
    private boolean containsError = false;
    //if one of its descendants contains an error
    private int childrenErrorCount = 0;
    
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
    
    public boolean containsError() {
        return containsError;
    }
    
    //returns a number of ancestors with error
    public int getChildrenErrorCount() {
        return childrenErrorCount;
    }
    
    public String toString() {
        return getText(false);
    }
    
    public String getText(boolean html) {
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
            
            StringBuffer text = new StringBuffer();
            text.append(html ? "<html>" : "");
            text.append(html && containsError ? "<font color=FF0000><b>": ""); //red
            text.append(getDocumentElement().getName());
            text.append(html && containsError ? "</b></font>": "");
            text.append(html ? "<font color=888888>" : "");//gray
            if(attribsVisibleText.trim().length() > 0) {
                text.append(" ");
                text.append(attribsVisibleText);
            }
            text.append(html ? "</font>" : "");
            if(contentText.trim().length() > 0) {
                text.append(" (");
                text.append(contentText);
                text.append(")");
            }
            text.append(html ? "</html>" : "");
            
            return text.toString();
            
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
            int index = "<?".length() + de.getName().length();
            if(index > (documentText.length() - 1)) index = documentText.length() - 1;
            if(documentText.length() > 0) documentText = documentText.substring(index, documentText.length() - 1).trim();
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
            while(attrNames.hasMoreElements()) {
                String aname = (String)attrNames.nextElement();
                String value = (String)getDocumentElement().getAttributes().getAttribute(aname);
                attribsText.append(aname);
                attribsText.append("=\"");
                attribsText.append(value);
                attribsText.append("\"");
                if(attrNames.hasMoreElements()) attribsText.append(", ");
            }
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
        } else if(ade.getType().equals(XMLDocumentModelProvider.XML_ERROR)) {
            //handle error element
            markNodeAsErrorInAWT(this);
        } else if(ade.getType().equals(XMLDocumentModelProvider.XML_COMMENT)) {
            //do nothing for comments
        } else {
            TreeNode tn = new TreeNodeAdapter(ade, tm, tree, this, true); //do not create children adapters here!!!
            int insertIndex = getVisibleChildIndex(ade);
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
    
    private void markNodeAsErrorInAWT(final TreeNodeAdapter tna) {
        if(!SwingUtilities.isEventDispatchThread()) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        markNodeAsError(tna);
                    }
                });
            }catch(Exception ie) {
                ie.printStackTrace(); //XXX handle somehow better
            }
        } else markNodeAsError(tna);
    }
    
    private void markNodeAsError(final TreeNodeAdapter tna) {
        tna.containsError = true;
        //mark all its ancestors as "childrenContainsError"
        TreeNodeAdapter parent = tna;
        tm.nodeChanged(tna);
        while((parent = (TreeNodeAdapter)parent.getParent()) != null) {
            if(parent.getParent() != null) parent.childrenErrorCount++; //do not fire for root element
            tm.nodeChanged(parent);
        }
    }
    
    private int getVisibleChildIndex(DocumentElement de) {
        int index = 0;
        Iterator children = getDocumentElement().getChildren().iterator();
        while(children.hasNext()) {
            DocumentElement child = (DocumentElement)children.next();
            if(child.equals(de)) return index;
            
            //skip text and error tokens
            if(!child.getType().equals(XMLDocumentModelProvider.XML_CONTENT)
            && !child.getType().equals(XMLDocumentModelProvider.XML_ERROR)
            && !child.getType().equals(XMLDocumentModelProvider.XML_COMMENT)) index++;
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
        } else if(rde.getType().equals(XMLDocumentModelProvider.XML_ERROR)) {
            unmarkNodeAsErrorInAWT(this);
        } else if(rde.getType().equals(XMLDocumentModelProvider.XML_COMMENT)) {
            //do nothing for comments
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
    
    private void unmarkNodeAsErrorInAWT(final TreeNodeAdapter tna) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    //handle error element
                    tna.containsError = false;
                    //unmark all its ancestors as "childrenContainsError"
                    TreeNodeAdapter parent = tna;
                    tm.nodeChanged(tna);
                    while((parent = (TreeNodeAdapter)parent.getParent()) != null) {
                        if(parent.getParent() != null) parent.childrenErrorCount--; //do not fire for root element
                        tm.nodeChanged(parent);
                    }
                }
            });
        }catch(Exception ie) {
            ie.printStackTrace(); //XXX handle somehow better
        }
    }
    
    public void attributesChanged(DocumentElementEvent e) {
        if(debug)System.out.println("Attributes of treenode " + this + " has changed.");
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    tm.nodeChanged(TreeNodeAdapter.this);
                }
            });
        }catch(Exception ie) {
            ie.printStackTrace(); //XXX handle somehow better
        }
    }
    
    public void contentChanged(DocumentElementEvent e) {
        if(debug) System.out.println("treenode " + this + " changed.");
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    tm.nodeChanged(TreeNodeAdapter.this);
                }
            });
        }catch(Exception ie) {
            ie.printStackTrace(); //XXX handle somehow better
        }
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
            if(chde.getType().equals(XMLDocumentModelProvider.XML_CONTENT)) {
                //create a text node listener
                textElements.add(new TextElementWrapper(chde));
                //update text text content of the node
                childTextElementChanged();
            } else if(chde.getType().equals(XMLDocumentModelProvider.XML_ERROR)) {
                markNodeAsErrorInAWT(this);
            } else if(chde.getType().equals(XMLDocumentModelProvider.XML_COMMENT)) {
                //do nothing for comments
            } else {
                //add the adapter only when there isn't any
                if(getChildTreeNode(chde) == null) children.add(new TreeNodeAdapter(chde, tm, tree, this));
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
                    //the endoffset if increased by +1 due to still not yet resolved issue with element boundaries
                    //should be removed once it is properly fixed. On the other hand the issue has no user impact now.
                    int endOfs = del.getEndOffset() - del.getStartOffset() + 1;
                    //check document boundary - the condition should never be true
                    endOfs = endOfs > del.getDocument().getLength() ? del.getDocument().getLength() : endOfs;
                    
                    buf.append((del.getDocument().getText(del.getStartOffset(), endOfs)).trim());
                }catch(BadLocationException e) {
                    buf.append("???");
                }
            }
        }
        textContent = buf.toString();
        //fire a change event for this node
        try {
            if(!SwingUtilities.isEventDispatchThread()) {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        tm.nodeChanged(TreeNodeAdapter.this);
                    }
                });
            } else tm.nodeChanged(TreeNodeAdapter.this);
        }catch(Exception ie) {
            ie.printStackTrace(); //XXX handle somehow better
        }
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
