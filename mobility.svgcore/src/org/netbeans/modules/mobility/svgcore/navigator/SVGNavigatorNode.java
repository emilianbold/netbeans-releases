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

package org.netbeans.modules.mobility.svgcore.navigator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.AttributeSet;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.modules.editor.structure.api.DocumentElement;
import org.netbeans.modules.editor.structure.api.DocumentElementEvent;
import org.netbeans.modules.editor.structure.api.DocumentElementListener;
import org.netbeans.modules.mobility.svgcore.model.SVGFileModel;
import org.openide.ErrorManager;

/**
 * TreeNodeAdapter is an implementation of j.s.t.TreeNode encapsulating a DocumentElement
 * instance and listening on its changes.
 *
 * @author Pavel Benes (based on the class TreeNodeAdapter by Marek Fukala)
 */
final class SVGNavigatorNode implements TreeNode, DocumentElementListener {
    private final SVGNavigatorTree        m_nodeTree;
    private final DocumentElement         m_de;    
    private final TreeNode                m_parent;    
    private       byte                    m_nodeVisibility;
    private       List<SVGNavigatorNode>  m_children     = null; //a list of non-content nodes
    //if the node itself contains an error
    private       boolean                 m_containsError = false;
    //if one of its descendants contains an error
    private       int                     m_childrenErrorCount = 0;
    
    public SVGNavigatorNode(DocumentElement de, SVGNavigatorTree nodeTree, TreeNode parent, byte nodeVisibility) {
        m_de             = de;
        m_nodeTree       = nodeTree;
        m_parent         = parent;
        m_nodeVisibility = nodeVisibility;
    }   
        
    public java.util.Enumeration<SVGNavigatorNode> children() {
        checkChildrenAdapters();
        return Collections.enumeration(m_children);
    }
    
    public boolean getAllowsChildren() {
        return true;
    }
    
    public TreeNode getChildAt(int param) {
        checkChildrenAdapters();
        return (TreeNode)m_children.get(param);
    }
    
    public int getChildCount() {
        checkChildrenAdapters();
        return m_children.size();
    }
    
    public int getIndex(TreeNode treeNode) {
        checkChildrenAdapters();
        return m_children.indexOf(treeNode);
    }
    
    public TreeNode getParent() {
        return m_parent;
    }
    
    public boolean isLeaf() {
        return getChildCount() == 0;
    }
    
    public DocumentElement getDocumentElement() {
        assert m_de != null;
        return m_de;
    }
    
    public byte getNodeVisibility() {
        return m_nodeVisibility;
    }
    
    SVGNavigatorNode findNode(DocumentElement docElem) {
        SVGNavigatorNode node = null;
        
        if (m_de.equals(docElem)) {
            node = this;
        } else if (m_children != null) {
            for (int i = m_children.size() - 1; i >= 0; i--) {
                if ( (node=((SVGNavigatorNode) m_children.get(i)).findNode(docElem)) != null) {
                    break;
                }
            }
        }
        
        return node;
    }
    
    SVGNavigatorNode getChildByElemenent( DocumentElement de) {
        checkChildrenAdapters();
        if (m_children != null) {
            for (SVGNavigatorNode child : m_children) {
                if (child.getDocumentElement() == de) {
                    return child;
                }
            }
        }
        return null;
    }
    
    TreePath getNodePath() {
        int depth = 0;
        TreeNode node = this;
        do {
            node = node.getParent();
            depth++;
        } while( node != null);
        
        TreeNode [] nodes = new TreeNode[depth];
        node = this;
        for (int i = nodes.length - 1; i >= 0; i--) {
            nodes[i] = node;
            node = node.getParent();
        }
        
        return new TreePath(nodes);
    }
    
    private SVGNavigatorNode getChildTreeNode(DocumentElement de) {
        int index;
        
        if ((index=getChildTreeNodeIndex(de)) != -1) {
            return (SVGNavigatorNode) m_children.get(index);
        }
        return null;
    }
       
    private int getChildTreeNodeIndex(DocumentElement de) {
        checkChildrenAdapters();
        int childNum = m_children.size();
        for (int i = 0; i < childNum; i++) {
            SVGNavigatorNode node = (SVGNavigatorNode) m_children.get(i);
            if(node.getDocumentElement().equals(de)) {
                return i;
            }
        }

        return -1;
    }
    
    public boolean containsError() {
        checkChildrenAdapters();
        return m_containsError;
    }
    
    //returns a number of ancestors with error
    public int getChildrenErrorCount() {
        checkChildrenAdapters();
        return m_childrenErrorCount;
    }
    
    public String toString() {
        return getText(false);
    }
    
    public String getText(boolean html) {
        if(SVGNavigatorTree.isTreeElement(m_de)) {
            //XML TAG text
            String attribsVisibleText = "";
            AttributeSet attribs = getDocumentElement().getAttributes();
            
            if(attribs.getAttributeCount() > 0) {
                String attribsText = getAttribsText();
                if(SVGNavigatorTree.showAttributes) {
                    attribsVisibleText = attribsText.length() > ATTRIBS_MAX_LEN ? attribsText.substring(0,ATTRIBS_MAX_LEN) + "..." : attribsText.toString();
                }
            }
            /*
            String contentText = "";
            String documentText = getDocumentContent();
            if(NavigatorContent.showContent) {
                contentText  = documentText.length() > TEXT_MAX_LEN ? documentText.substring(0,TEXT_MAX_LEN) + "..." : documentText;
            }*/
            
            StringBuffer text = new StringBuffer();
            text.append(html ? "<html>" : ""); //NOI18N
            if (html) {
                if (m_containsError) {
                    text.append("<font color=FF0000><b>");  //NOI18N
                } else if (m_nodeVisibility == SVGNavigatorTree.VISIBILITY_UNDIRECT) {
                    text.append("<font color=888888>");  //NOI18N
                }
            }
            text.append(getDocumentElement().getName());
            if (html) {
                if (m_containsError) {
                    text.append("</b></font>");  //NOI18N
                } else if (m_nodeVisibility == SVGNavigatorTree.VISIBILITY_UNDIRECT) {
                    text.append("</font>");  //NOI18N
                }
            }
                  
            text.append(html ? "<font color=888888>" : ""); //NOI18N
            if(attribsVisibleText.trim().length() > 0) {
                text.append(" ");
                text.append(attribsVisibleText);
            }
            text.append(html ? "</font>" : ""); //NOI18N
            /*
            if(contentText.trim().length() > 0) {
                text.append(" (");
                text.append(HTMLTextEncoder.encodeHTMLText(contentText));
                text.append(")");
            }
             */
            text.append(html ? "</html>" : ""); //NOI18N
            
            return text.toString();
            
        } /*else if(de.getType().equals(XML_PI)) {
            //PI text
            String documentText = getPIText();
            documentText = documentText.length() > TEXT_MAX_LEN ? documentText.substring(0,TEXT_MAX_LEN) + "..." : documentText;
            return documentText;
        } else if(de.getType().equals(XML_DOCTYPE)) {
            //limit the text length
            String documentText = getDoctypeText();
            String visibleText  = documentText.length() > TEXT_MAX_LEN ? documentText.substring(0,TEXT_MAX_LEN) + "..." : documentText;
            return visibleText;
        } else if(de.getType().equals(XML_CDATA)) {
            //limit the text length
            String documentText = getCDATAText();
            String visibleText  = documentText.length() > TEXT_MAX_LEN ? documentText.substring(0,TEXT_MAX_LEN) + "..." : documentText;
            return visibleText;
        }*/
        
        return m_de.getName() + " [unknown content]";
    }
    
    public String getToolTipText() {
        return getAttribsText();
        /*
        if(de.getType().equals(XML_TAG)
        || de.getType().equals(XML_EMPTY_TAG)) {
            return getAttribsText();
        } else if(de.getType().equals(XML_PI)) {
            return getPIText();
        } else if(de.getType().equals(XML_DOCTYPE)) {
            return getDoctypeText();
        } else if(de.getType().equals(XML_CDATA)) {
            return getCDATAText();
        }
        return ""; */
    }
    
/*    
    private String getPIText() {
        String documentText = null;
        try {
            documentText = m_de.getDocumentModel().getDocument().getText(de.getStartOffset(), de.getEndOffset() - de.getStartOffset());
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
  */  
    public void childrenReordered(DocumentElementEvent ce) {
        //notify treemodel - do that in event dispath thread
        m_nodeTree.getTreeModel().nodeStructureChanged(SVGNavigatorNode.this);
    }
    
    public String getAttribsText() {
        StringBuffer attribsText = new StringBuffer();
        Enumeration attrNames = getDocumentElement().getAttributes().getAttributeNames();
        if(attrNames.hasMoreElements()) {
            while(attrNames.hasMoreElements()) {
                String aname = (String)attrNames.nextElement();
                String value = (String)getDocumentElement().getAttributes().getAttribute(aname);
                attribsText.append(aname);
                attribsText.append("=\"");  //NOI18N
                attribsText.append(value);
                attribsText.append("\""); //NOI18N
                if(attrNames.hasMoreElements()) attribsText.append(", ");  //NOI18N
            }
        }
        return attribsText.toString();
    }
    
    public void elementAdded(DocumentElementEvent e) {
        DocumentElement ade = e.getChangedChild();
        
        if(debug) System.out.println(">>> +EVENT called on " + hashCode() + " - " + m_de + ": element " + ade + " is going to be added");  //NOI18N
        
        if (SVGNavigatorTree.isTreeElement(ade)) {
            byte visibility = m_nodeTree.checkVisibility(ade, true);

            //add the element only when there isn't such one
            int index = getChildTreeNodeIndex(ade);
            if(index == -1) {               
                if (visibility != SVGNavigatorTree.VISIBILITY_NO) {
                    SVGNavigatorNode tn = new SVGNavigatorNode(ade, m_nodeTree, this, visibility);
                    int insertIndex = getVisibleChildIndex(ade);

                    //check whether the insert index doesn't go beyond the actual children length (which states an error)
                    if(m_children.size() < insertIndex /*||
                            children.size() + 1 /* it doesn't contain the currently added element != getDocumentElement().getElementCount()*/) {
                        //error => try to recover by refreshing the current node
                        //debugError(e);
                        //notify treemodel
                        m_nodeTree.getTreeModel().nodeStructureChanged(this);
                    } else {
                        m_children.add(insertIndex, tn);
                        final int tnIndex = getIndex(tn);
                        m_nodeTree.getTreeModel().nodesWereInserted(this, new int[]{tnIndex});
                    }
                    if(debug)System.out.println("<<<EVENT finished (node " + tn + " added)"); //NOI18N
                }
            } else {
                if (visibility == SVGNavigatorTree.VISIBILITY_NO) {
                    Object removedNode = m_children.remove(index);
                    m_nodeTree.getTreeModel().nodesWereRemoved(this, new int[] {index}, new Object[] {removedNode});
                } 
            }            
        } else if (SVGFileModel.isError(ade)) {
            markNodeAsError(this);
        }
                
        //fix: if a new nodes are added into the root element (set as invisible), the navigator
        //window is empty. So we need to always expand the root element when adding something into
        if(m_de.equals(m_de.getDocumentModel().getRootElement())) {
            //expand path
            m_nodeTree.expandPath(new TreePath(this));
        }        
    }
    
    private void debugError(DocumentElementEvent e) {
        StringBuffer sb = new StringBuffer();
        sb.append("An inconsistency between XML navigator and XML DocumentModel occured when adding a new element in the XML DocumentModel! Please report the problem and add following debug messages to the issue along with the XML document you are editing.");  //NOI18N
        sb.append("Debug for Node " + this + ":\n"); //NOI18N
        sb.append("Children of current node:\n"); //NOI18N
        Iterator itr = m_children.iterator();
        while(itr.hasNext()) {
            SVGNavigatorNode tna = (SVGNavigatorNode)itr.next();
            sb.append(tna.toString());
            sb.append("\n"); //NOI18N
        }
        sb.append("\nChildren of DocumentElement (" + getDocumentElement() + ") wrapped by the current node:\n"); //NOI18N
        Iterator currChildrenItr = getDocumentElement().getChildren().iterator();
        while(itr.hasNext()) {
            DocumentElement de = (DocumentElement)itr.next();
            sb.append(de.toString());
            sb.append("\n"); //NOI18N
        }
        sb.append("------------"); //NOI18N
        
        ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, sb.toString());
    }
    
    private void markNodeAsError(final SVGNavigatorNode tna) {
        tna.m_containsError = true;
        //mark all its ancestors as "childrenContainsError"
        SVGNavigatorNode parent = tna;
        m_nodeTree.getTreeModel().nodeChanged(tna);
        while((parent = (SVGNavigatorNode)parent.getParent()) != null) {
            if(parent.getParent() != null) parent.m_childrenErrorCount++; //do not fire for root element
            m_nodeTree.getTreeModel().nodeChanged(parent);
        }
    }
    
    private int getVisibleChildIndex(DocumentElement de) {
        int index = 0;
        Iterator children = getDocumentElement().getChildren().iterator();
        while(children.hasNext()) {
            DocumentElement child = (DocumentElement)children.next();
            if(child.equals(de)) return index;
            
            if ( SVGNavigatorTree.isTreeElement(child)) {
                index++;
            }
            /*
            //skip text and error tokens
            if(!child.getType().equals(XML_CONTENT)
            && !child.getType().equals(XML_ERROR)
            && !child.getType().equals(XML_COMMENT)) index++; */
        }
        return -1;
    }
    
    public void elementRemoved(DocumentElementEvent e) {
        DocumentElement rde = e.getChangedChild();
        
        if(debug) System.out.println(">>> -EVENT on " + hashCode() + " - " + m_de + ": element " + rde + " is going to be removed ");  //NOI18N
        
        if (SVGNavigatorTree.isTreeElement(rde)) {
            if(debug) System.out.println(">>> removing tag element");  //NOI18N
            final SVGNavigatorNode tn = getChildTreeNode(rde);
            final int tnIndex = getIndex(tn);
            
            if(tn != null) {
                m_children.remove(tn);
                //notify treemodel - do that in event dispath thread
                m_nodeTree.getTreeModel().nodesWereRemoved(SVGNavigatorNode.this, new int[]{tnIndex}, new Object[]{tn});
            } else if(debug) System.out.println("Warning: TreeNode for removed element doesn't exist!!!");
        } else if (SVGFileModel.isError(rde)) {
            unmarkNodeAsError(this);
        }
        
        if(debug) System.out.println("<<<EVENT finished (node removed)");
    }
        
    private void unmarkNodeAsError(final SVGNavigatorNode tna) {
        //handle error element
        tna.m_containsError = false;
        //unmark all its ancestors as "childrenContainsError"
        SVGNavigatorNode parent = tna;
        m_nodeTree.getTreeModel().nodeChanged(tna);
        while((parent = (SVGNavigatorNode)parent.getParent()) != null) {
            if(parent.getParent() != null) parent.m_childrenErrorCount--; //do not fire for root element
            m_nodeTree.getTreeModel().nodeChanged(parent);
        }
    }
    
    public void attributesChanged(DocumentElementEvent e) {
        if(debug)System.out.println("Attributes of treenode " + this + " has changed.");
        m_nodeTree.getTreeModel().nodeChanged(SVGNavigatorNode.this);
    }
    
    public void contentChanged(DocumentElementEvent e) {
        if(debug) System.out.println("treenode " + this + " changed.");
        m_nodeTree.getTreeModel().nodeChanged(SVGNavigatorNode.this);
    }

    public synchronized void refresh() {
        if (m_children != null) {
            List<DocumentElement> childElems       = m_de.getChildren();
            int                   elemNum          = childElems.size();
            int                   childNum         = m_children.size();
            boolean []            processed        = new boolean[elemNum];
            SVGNavigatorNode []   removedChildren  = new SVGNavigatorNode[childNum];
            int                   removedNum       = 0;
            int                   processedElemNum = 0;

            // skip the elements that are not represented by navigator node
            for (int j = 0; j < elemNum; j++) {
                if (!SVGNavigatorTree.isTreeElement( childElems.get(j))) {
                    processedElemNum++;
                    processed[j] = true;
                }
            }
            
            for( int i = childNum - 1; i >= 0; i--) {
                SVGNavigatorNode childNode = (SVGNavigatorNode) m_children.get(i);

                for (int j = 0; j < elemNum; j++) {
                    if (!processed[j]) {
                        DocumentElement childElem = childElems.get(j);
                        if ( childNode.m_de.equals(childElem)) {
                            byte visibility = m_nodeTree.checkVisibility(childElem, true);
                            if (visibility == SVGNavigatorTree.VISIBILITY_NO) {
                                m_children.remove(i);
                                removedChildren[i] = childNode;
                                removedNum++;
                            } else {
                                if (childNode.m_nodeVisibility != visibility) {                                    
                                    childNode.m_nodeVisibility = visibility;
                                }  
                                childNode.refresh();                            
                            }
                            processedElemNum++;
                            processed[j] = true;
                            break;
                        }                         
                    }
                }                
           }

            // check if some nodes become invisible
            if (removedNum > 0) {
                int [] childIndices = new int[removedNum];
                Object [] childrenArr = new Object[removedNum];
                for (int j = 0, k = 0; j < childNum; j++) {
                    if (removedChildren[j] != null) {
                        childrenArr[k]  = removedChildren[j];
                        childIndices[k++] = j;
                    }
                }
                m_nodeTree.getTreeModel().nodesWereRemoved(this, childIndices, childrenArr);
                childNum -= removedNum;
            }
            
            assert childNum == m_children.size();

            // check is some nodes become visible
            if ( processedElemNum < elemNum) {
                int    childIndex   = 0;
                int    elemIndex    = 0;
                int [] childIndices = new int[elemNum - processedElemNum];
                int    addedNum     = 0;

                main_loop : while( childIndex < childNum || elemIndex < elemNum) {
                    DocumentElement childNodeElem;
                    if (childIndex < childNum) {
                        //TODO Fix OutOfBoundsException here
                        childNodeElem = ((SVGNavigatorNode) m_children.get(childIndex)).m_de;
                    } else {
                        childNodeElem = null;
                    }

                    while(elemIndex < elemNum) {
                        DocumentElement childElem = (DocumentElement) m_de.getElement(elemIndex);
                        if (childElem.equals(childNodeElem)) {
                            childIndex++;
                            elemIndex++;
                            continue main_loop;
                        } else {
                            if ( !processed[elemIndex]) {
                                byte visibility = m_nodeTree.checkVisibility(childElem, true);
                                if (visibility != SVGNavigatorTree.VISIBILITY_NO) {
                                    SVGNavigatorNode newChild = new SVGNavigatorNode(childElem, m_nodeTree, this, visibility);
                                    m_children.add(childIndex, newChild);
                                    childIndices[addedNum++] = childIndex;
                                    childIndex++;
                                    childNum++;
                                }
                            }
                            elemIndex++;
                        }
                    }
                }
                if (addedNum > 0) {
                    if (addedNum < childIndices.length) {
                        int [] t = childIndices;
                        childIndices = new int[addedNum];
                        System.arraycopy(t, 0, childIndices, 0, addedNum);                        
                    }
                    m_nodeTree.getTreeModel().nodesWereInserted(this, childIndices);
                }
            }
        }
    }

    private synchronized void checkChildrenAdapters() {
        if(m_children == null) {
            //attach myself to the document element as a listener
            m_de.addDocumentElementListener(this);

            //lazyloading children for node
            m_children = new ArrayList<SVGNavigatorNode>();
            Iterator i = m_de.getChildren().iterator();
            //boolean textElementAdded = false;
            while(i.hasNext()) {
                DocumentElement chde = (DocumentElement)i.next();
                if (SVGNavigatorTree.isTreeElement(chde)) {
                    byte visibility = m_nodeTree.checkVisibility(chde, true);
                    
                    //add the adapter only when there isn't any
                    int index = getChildTreeNodeIndex(chde);
                    
                    if(index == -1) {
                        if (visibility != SVGNavigatorTree.VISIBILITY_NO) {
                            SVGNavigatorNode tna = new SVGNavigatorNode(chde, m_nodeTree, this, visibility);
                            m_children.add(tna);
                        }
                    } else {
                        if (visibility == SVGNavigatorTree.VISIBILITY_NO) {
                            m_children.remove(index);
                        }
                    }
                } else if (SVGFileModel.isError(chde)) {
                    markNodeAsError(this);
                } 
            }
            //update text text content of the node
            //if(textElementAdded) childTextElementChanged();
        }
    }
/*
    private void childTextElementChanged() {
        nodeTree.getTreeModel().nodeChanged(this);
    }
    
    private final class TextElementWrapper implements DocumentElementListener {        
        private final DocumentElement de;
        
        public TextElementWrapper(DocumentElement de) {
            this.de = de;
            de.addDocumentElementListener(TextElementWrapper.this);
        }
        
        public DocumentElement getDocumentElement() {
            return de;
        }
        
        public void contentChanged(DocumentElementEvent e) {
            SVGNavigatorNode.this.childTextElementChanged();
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
*/    
    private static final boolean debug = Boolean.getBoolean("org.netbeans.modules.xml.text.structure.debug");  //NOI18N
    
    private static final int ATTRIBS_MAX_LEN = 100;
    //private static final int TEXT_MAX_LEN = ATTRIBS_MAX_LEN;    
}
