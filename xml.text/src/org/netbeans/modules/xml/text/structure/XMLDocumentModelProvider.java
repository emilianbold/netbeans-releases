/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.xml.text.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenItem;
import org.netbeans.modules.editor.structure.api.DocumentElement;
import org.netbeans.modules.editor.structure.api.DocumentModel;
import org.netbeans.modules.editor.structure.api.DocumentModel.DocumentChange;
import org.netbeans.modules.editor.structure.api.DocumentModel.DocumentModelTransactionCancelledException;
import org.netbeans.modules.editor.structure.api.DocumentModelException;
import org.netbeans.modules.editor.structure.api.DocumentModelUtils;
import org.netbeans.modules.editor.structure.spi.DocumentModelProvider;
import org.netbeans.modules.xml.text.syntax.SyntaxElement;
import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;
import org.netbeans.modules.xml.text.syntax.XMLTokenIDs;
import org.netbeans.modules.xml.text.syntax.dom.AttrImpl;
import org.netbeans.modules.xml.text.syntax.dom.CDATASectionImpl;
import org.netbeans.modules.xml.text.syntax.dom.CommentImpl;
import org.netbeans.modules.xml.text.syntax.dom.DocumentTypeImpl;
import org.netbeans.modules.xml.text.syntax.dom.EmptyTag;
import org.netbeans.modules.xml.text.syntax.dom.EndTag;
import org.netbeans.modules.xml.text.syntax.dom.ProcessingInstructionImpl;
import org.netbeans.modules.xml.text.syntax.dom.StartTag;
import org.netbeans.modules.xml.text.syntax.dom.Tag;
import org.openide.ErrorManager;


/**
 *
 * @author mf100882
 */
public class XMLDocumentModelProvider implements DocumentModelProvider {
    
    
    public void updateModel(DocumentModel.DocumentModelModificationTransaction dtm,
            DocumentModel model, DocumentChange[] changes)
            throws DocumentModelException, DocumentModelTransactionCancelledException {
        
        long a = System.currentTimeMillis();
        
        if(debug) System.out.println("\n\n\n\n\n");
        if(debug) DocumentModelUtils.dumpElementStructure(model.getRootElement());
        
        ArrayList regenerate = new ArrayList(); //used to store elements to be regenerated
        
        for(int i = 0; i < changes.length; i++) {
            DocumentChange dch = changes[i];
            
            int changeOffset = dch.getChangeStart().getOffset();
            int changeLength = dch.getChangeLength();
            
            //find an element in which the change happened
            DocumentElement leaf = model.getLeafElementForOffset(changeOffset);
            DocumentElement toRegenerate = leaf;
            
            if(debug) System.out.println("");
            if(debug) System.out.println(dch);
            try {
                if(debug) System.out.println("inserted text:'" + model.getDocument().getText(changeOffset, changeLength) + "'");
            }catch(BadLocationException e) {
                ;
            }
            if(debug) System.out.println("leaf = " + leaf);
            
            //parse the document context
            XMLSyntaxSupport sup = (XMLSyntaxSupport)((BaseDocument)model.getDocument()).getSyntaxSupport();
            
            boolean textOnly = false;
            boolean attribsOnly = false;
            try {
                //scan the inserted text - if it contains only text set textOnly flag
                TokenItem ti = sup.getTokenChain(changeOffset, changeOffset + 1);
                while(ti != null && ti.getOffset() < (changeOffset + changeLength)) {
                    if(ti.getTokenID() == XMLTokenIDs.TEXT
                            || ti.getTokenID() == XMLTokenIDs.DECLARATION
                            || ti.getTokenID() == XMLTokenIDs.BLOCK_COMMENT
                            || ti.getTokenID() == XMLTokenIDs.PI_CONTENT
                            || ti.getTokenID() == XMLTokenIDs.CDATA_SECTION) {
                        textOnly = true;
                        break;
                    }
                    if(ti.getTokenID() == XMLTokenIDs.ARGUMENT
                            || ti.getTokenID() == XMLTokenIDs.OPERATOR
                            || ti.getTokenID() == XMLTokenIDs.VALUE) {
                        attribsOnly = true;
                        break;
                    }
                    ti = ti.getNext();
                }
            }catch(BadLocationException e) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
            }
            
            if(textOnly &&
                    ( leaf.getType().equals(XML_CONTENT)
                    || leaf.getType().equals(XML_DOCTYPE)
                    || leaf.getType().equals(XML_PI)
                    || leaf.getType().equals(XML_COMMENT)
                    || leaf.getType().equals(XML_CDATA))){
                //just a text written into a text element simply fire document element change event and do not regenerate anything
                //add the element update request into transaction
                if(debug) System.out.println("ONLY CONTENT UPDATE!!!");
                dtm.updateDocumentElementText(leaf);
                
                //do not scan the context tag if the change is only insert or remove of one character into a text (typing text perf. optimalization)
                if(dch.getChangeLength() == 1) {
                    continue;
                }
            }
            
            if((attribsOnly || dch.getChangeType() == DocumentChange.REMOVE)
                    && (leaf.getType().equals(XML_TAG)
                    || leaf.getType().equals(XML_EMPTY_TAG))) {
                if(debug) System.out.println("POSSIBLE ATTRIBS UPDATE!!!");
                //we need to parse the tag element attributes and set them according to the new values
                try {
                    SyntaxElement sel = sup.getElementChain(leaf.getStartOffset() + 1);
                    if(sel instanceof Tag || sel instanceof EmptyTag) {
                        //test whether the attributes changed
                        Map newAttrs = createAttributesMap((Tag)sel);
                        AttributeSet existing = leaf.getAttributes();
                        boolean update = false;
                        if(existing.getAttributeCount() == newAttrs.size()) {
                            Iterator itr = newAttrs.keySet().iterator();
                            while (itr.hasNext()) {
                                String attrName = (String) itr.next();
                                String attrValue = (String)newAttrs.get(attrName);
                                if(attrName != null && attrValue != null
                                        && !existing.containsAttribute(attrName, attrValue)) {
                                    update = true;
                                    break;
                                }
                                
                            }
                        } else update = true;
                        
                        if(update) {
                            dtm.updateDocumentElementAttribs(leaf, newAttrs);
                        }
                    }
                }catch(BadLocationException ble) {
                    ErrorManager.getDefault().notify(ErrorManager.WARNING, ble);
                }
            }
            
            //if one or more elements are deleted get correct paret to regenerate
            if((leaf.getStartOffset() == leaf.getEndOffset())
                    || (changeOffset == leaf.getStartOffset())
                    || (changeOffset == leaf.getEndOffset()))
                toRegenerate = leaf.getParentElement();
            else {
                //user written a tag or something what is not a text
                //we need to get the element's parent. Simple leaf.getParent() is not enought
                //since when an element is deleted then a wrong parent can be choosen
                if(leaf.getType().equals(XML_CONTENT)) {
                    do {
                        toRegenerate = toRegenerate.getParentElement();
                    } while(toRegenerate != null && toRegenerate.getType().equals(XML_CONTENT));
                    
                    if(toRegenerate == null) {
                        //no suitable parent found - the element is either a root or doesn't have any xml_tag ancestor => use root
                        toRegenerate = model.getRootElement();
                    }
                }
            }
            
            if(toRegenerate == null) toRegenerate = model.getRootElement(); //root element is empty
            
            //now regenerate all sub-elements inside parent of the affected element
            
            //check if the element is not a descendant a one of the elements
            //which are going to be regenerated
            Iterator itr = regenerate.iterator();
            boolean hasAncestor = false;
            while(itr.hasNext()) {
                DocumentElement de = (DocumentElement)itr.next();
                if(de.equals(toRegenerate) || model.isDescendantOf(de, toRegenerate)) {
                    hasAncestor = true;
                    break;
                }
            }
            
            if(!hasAncestor) {
                //check whether the element is not an ancestor of one or more element
                //which are going to be regenerated
                ArrayList toRemove = new ArrayList();
                Iterator i2 = regenerate.iterator();
                while(i2.hasNext()) {
                    DocumentElement de = (DocumentElement)i2.next();
                    if(model.isDescendantOf(toRegenerate, de)) toRemove.add(de);
                }
                
                //now really remove the elements
                regenerate.removeAll(toRemove);
                
                //add the element - it will be likely regenerated in next model update
                regenerate.add(toRegenerate);
                
                //debug>>>
                if(debug) System.out.println("===================================================================");
                if(debug) System.out.println("change happened in " + leaf);
                if(debug) System.out.println("we will regenerate its parent " + toRegenerate);
                //<<<debug
            }
        } //end of the changes loop
        
        //update the model
        Iterator elementsToUpdate = regenerate.iterator();
        while(elementsToUpdate.hasNext()) {
            DocumentElement de = (DocumentElement)elementsToUpdate.next();
            generateDocumentElements(dtm, model, de);
        }
        
        if(measure) System.out.println("[xmlmodel] generated in " + (System.currentTimeMillis() - a));
        
    }
    
    /** generates document elements within an area defined by startoffset and
     *endoffset. */
    private void generateDocumentElements(DocumentModel.DocumentModelModificationTransaction dtm,
            DocumentModel model, DocumentElement de) throws DocumentModelException, DocumentModelTransactionCancelledException {
        
        int startOffset = de.getStartOffset();
        int endOffset = de.getEndOffset();
        
        BaseDocument doc = (BaseDocument)model.getDocument();
        XMLSyntaxSupport sup = new XMLSyntaxSupport(doc);
        
        if(debug) System.out.println("[XMLDocumentModelProvider] regenerating " + de);
        
        Set addedElements = new TreeSet(DocumentModel.ELEMENTS_COMPARATOR);
        ArrayList skipped = new ArrayList();
        try {
            Stack elementsStack = new Stack(); //we need this to determine tags nesting
            
            //the syntax element is created for token on offset - 1
            //so I need to add 1 to the startOffset
            SyntaxElement sel = sup.getElementChain(Math.min(doc.getLength(), startOffset+1));
            
            //scan the document for syntax elements - from startOffset to endOffset
            while(sel != null && getSyntaxElementEndOffset(sel) <= endOffset) {
                if(sel instanceof SyntaxElement.Error) {
                    //add error element into the structure
                    if(debug) System.out.println("Error found! => adding error element.");
                    String errorText = doc.getText(sel.getElementOffset(), sel.getElementLength());
                    addedElements.add(dtm.addDocumentElement(errorText, XML_ERROR, Collections.EMPTY_MAP,
                            sel.getElementOffset(), getSyntaxElementEndOffset(sel)));
                }
                
                if(sel instanceof StartTag) {
                    //test if there is already an existing documet element in the model
                    StartTag stag = (StartTag)sel;
                    DocumentElement tagDE = DocumentModelUtils.findElement(model, sel.getElementOffset(), stag.getTagName(), XML_TAG);
                    
                    //do not skip the 'de' element which is to be regenerated
                    if(tagDE != null && !tagDE.equals(de)) {
                        //test if the element has also correct end tag
                        SyntaxElement endTagCheck = sup.getElementChain(Math.min(doc.getLength(), tagDE.getEndOffset() + 1));
                        if(endTagCheck instanceof EndTag && ((EndTag)endTagCheck).getTagName().equals(stag.getTagName())) {
                            //there is an element - skip it - analyze an element after the end of the
                            //existing element
                            if(debug) System.out.println("found existing element " + tagDE + " => skipping");
                            //sel = sup.getElementChain(Math.min(doc.getLength(), tagDE.getEndOffset() + 2));
                            sel = endTagCheck.getNext();
                            skipped.add(tagDE);
                            continue;
                        }
                    }
                    
                    //add the tag syntax element into stack
                    elementsStack.push(sel);
                    
                } else if(sel instanceof EndTag) {
                    if(!elementsStack.isEmpty()) {
                        StartTag latest = (StartTag)elementsStack.peek();
                        if(((EndTag)sel).getTagName().equals(latest.getTagName())) {
                            //we have encountered a pair end tag to open tag on the peek of the stack
                            Map attribs = createAttributesMap(latest);
                            addedElements.add(dtm.addDocumentElement(latest.getTagName(), XML_TAG, attribs,
                                    latest.getElementOffset(), getSyntaxElementEndOffset(sel)));
                            //remove the open tag syntax element from the stack
                            elementsStack.pop();
                        } else {
                            //found an end tag which doesn't have a start tag
                            //=> take elements from the stack until I found a matching tag
                            
                            //I need to save the pop-ed elements for the case that there isn't
                            //any matching start tag found
                            ArrayList savedElements = new ArrayList();
                            //this semaphore is used behind the loop to detect whether a
                            //matching start has been found
                            boolean foundStartTag = false;
                            
                            while(!elementsStack.isEmpty()) {
                                SyntaxElement s = (SyntaxElement)elementsStack.pop();
                                savedElements.add(s);
                                
                                Tag start = (Tag)s;
                                Tag end = (Tag)sel;
                                
                                if(s instanceof StartTag && start.getTagName().equals(end.getTagName())) {
                                    //found a matching start tag
                                    //XXX I am not sure whether this algorith is correct
                                    Map attribs = createAttributesMap((StartTag)s);
                                    addedElements.add(dtm.addDocumentElement(start.getTagName(), XML_TAG, attribs,
                                            start.getElementOffset(), getSyntaxElementEndOffset(end)));
                                    
                                    foundStartTag = true;
                                    break; //break the while loop
                                }
                            }
                            
                            if(!foundStartTag) {
                                //we didn't find any matching start tag =>
                                //return all elements back to the stack
                                for(int i = savedElements.size() - 1; i >= 0; i--) {
                                    elementsStack.push(savedElements.get(i));
                                }
                            }
                        }
                    }
                } else if(sel instanceof EmptyTag) {
                    Map attribs = createAttributesMap((Tag)sel);
                    addedElements.add(dtm.addDocumentElement(((EmptyTag)sel).getTagName(), XML_EMPTY_TAG, attribs,
                            sel.getElementOffset(), getSyntaxElementEndOffset(sel)));
                } else if (sel instanceof CDATASectionImpl) {
                    //CDATA section
                    addedElements.add(dtm.addDocumentElement("cdata", XML_CDATA, Collections.EMPTY_MAP,
                            sel.getElementOffset(), getSyntaxElementEndOffset(sel)));
                } else if (sel instanceof ProcessingInstructionImpl) {
                    //PI section
                    String nodeName = ((ProcessingInstructionImpl)sel).getNodeName();
                    //if the nodename is not parsed, then the element is somehow broken => do not show it.
                    if(nodeName != null) {
                        addedElements.add(dtm.addDocumentElement(nodeName, XML_PI, Collections.EMPTY_MAP,
                                sel.getElementOffset(), getSyntaxElementEndOffset(sel)));
                    }
                } else if (sel instanceof DocumentTypeImpl) {
                    //document type <!DOCTYPE xxx [...]>
                    String nodeName = ((DocumentTypeImpl)sel).getName();
                    //if the nodename is not parsed, then the element is somehow broken => do not show it.
                    if(nodeName != null) {
                        addedElements.add(dtm.addDocumentElement(nodeName, XML_DOCTYPE, Collections.EMPTY_MAP,
                                sel.getElementOffset(), getSyntaxElementEndOffset(sel)));
                    }
                } else if (sel instanceof CommentImpl) {
                    //comment element <!-- xxx -->
                    //DO NOT CREATE ELEMENT FOR COMMENTS
                    addedElements.add(dtm.addDocumentElement("comment", XML_COMMENT, Collections.EMPTY_MAP,
                            sel.getElementOffset(), getSyntaxElementEndOffset(sel)));
                } else {
                    //everything else is content
                    addedElements.add(dtm.addDocumentElement("...", XML_CONTENT, Collections.EMPTY_MAP, sel.getElementOffset(), getSyntaxElementEndOffset(sel)));
                }
                //find next syntax element
                //                sel = sel.getNext();     //this cannot be used since it chains the results and they are hard to GC then.
                try {
                    //prevent cycles
                    SyntaxElement prev = null;
                    int add = 0;
                    do {
                        add++;
                        prev = sup.getElementChain(sel.getElementOffset() + sel.getElementLength() + add);
                    } while(prev != null && sel.getElementOffset() >= prev.getElementOffset());
                    sel = prev;
                }catch(BadLocationException ble) {
                    sel = null;
                }
            }
            
            //*** elements removal ***
            // we need to remove those elements which existed before and not exist now
            //we need to get all descendants from non-skipped elements
            List existingElements = getDescendantsOfNotSkippedElements(de, skipped);
            existingElements.add(de);
            
            Iterator existingItr = existingElements.iterator();
            //iterate all existing elements and check if they are still valid
            while(existingItr.hasNext()) {
                DocumentElement d = (DocumentElement)existingItr.next();
                if(!addedElements.contains(d)) {
                    //remove the element - it doesn't longer exist
                    dtm.removeDocumentElement(d, false);
                    if(debug) System.out.println("[xml model] removed element " + d);
                }
            }
            
        } catch( BadLocationException e ) {
            throw new DocumentModelException("Error occurred during generation of Document elements", e);
        }
    }
    
    
    private List/*<DocumentElement>*/ getDescendantsOfNotSkippedElements(DocumentElement de, List/*<DocumentElement>*/ skippedElements) {
        ArrayList desc = new ArrayList();
        Iterator children = de.getChildren().iterator();
        while(children.hasNext()) {
            DocumentElement child = (DocumentElement)children.next();
            if(!skippedElements.contains(child)) {
                desc.add(child);
                desc.addAll(getDescendantsOfNotSkippedElements(child, skippedElements));
            }
        }
        return desc;
    }
    
    private int getSyntaxElementEndOffset(SyntaxElement sel) {
        //zmenil jsem velikost vsech elementu tak, ze jejich
        //delka je kratsi o jeden => to resi problem kdyz se zacne psat na end position ->
        //text se v tomto pripade pridava jeste do elementu pred end positionou
        //napr:
        // <a>xxx</a>X
        //predtim to X padalo do tagu <a>, coz je blbe. Ted to padne za nej.
        //TODO musi se ale nejak vyresit problem zkracene delky - u text elementu se pri cteni
        //hodnoty musi pouzit endoffset + 1
        return sel.getElementOffset() + sel.getElementLength() -1;
    }
    
    private Map createAttributesMap(Tag tag) {
        if(tag.getAttributes().getLength() == 0) {
            return Collections.EMPTY_MAP;
        } else {
            HashMap map = new LinkedHashMap(tag.getAttributes().getLength());
            for(int i = 0; i < tag.getAttributes().getLength(); i++) {
                AttrImpl attr = (AttrImpl)tag.getAttributes().item(i);
                map.put(attr.getName(), attr.getValue());
            }
            return map;
        }
    }
    
    public static final String XML_TAG = "tag";
    public static final String XML_EMPTY_TAG = "empty_tag";
    public static final String XML_CONTENT = "content";
    public static final String XML_PI = "pi";
    public static final String XML_CDATA = "cdata";
    public static final String XML_DOCTYPE = "doctype";
    public static final String XML_COMMENT = "comment";
    
    public static final String XML_ERROR = "error";
    
    
    private static final boolean debug = Boolean.getBoolean("org.netbeans.modules.xml.text.structure.debug");
    private static final boolean measure = Boolean.getBoolean("org.netbeans.modules.xml.text.structure.measure");
    
}
