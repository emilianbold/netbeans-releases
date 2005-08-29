/*
 * XMLDocumentModel.java
 *
 * Created on 22. �erven 2005, 15:21
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.xml.text.structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
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
            
            boolean textOnly = true;
            //if(dch.getChangeType() == DocumentChange.INSERT) {
            try {
                //scan the inserted text - if it contains only text set textOnly flag
                TokenItem ti = sup.getTokenChain(changeOffset, changeOffset + 1);
                while(ti != null && ti.getOffset() < (changeOffset + changeLength)) {
                    if(ti.getTokenID() != XMLTokenIDs.TEXT 
                            && ti.getTokenID() != XMLTokenIDs.DECLARATION
                            && ti.getTokenID() != XMLTokenIDs.PI_CONTENT
                            && ti.getTokenID() != XMLTokenIDs.CDATA_SECTION) {
                        textOnly = false;
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
                    || leaf.getType().equals(XML_CDATA))){
                //just a text written into a text element simply fire document element change event and do not regenerate anything
                //add the element update request into transaction
                if(debug) System.out.println("ONLY CONTENT UPDATE!!!");
                dtm.updateDocumentElement(leaf);
//                continue;
            }
            
            //if one or more elements are deleted get correct paret to regenerate
            if(leaf.getStartOffset() == leaf.getEndOffset())
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
        
        ArrayList addedElements = new ArrayList();
        ArrayList skipped = new ArrayList();
        try {
            Stack elementsStack = new Stack(); //we need this to determine tags nesting
            
            //the syntax element is created for token on offset - 1
            //so I need to add 1 to the startOffset
            SyntaxElement sel = sup.getElementChain(Math.min(doc.getLength(), startOffset+1));
            
            //scan the document for syntax elements - from startOffset to endOffset
            while(sel != null && getSyntaxElementEndOffset(sel) <= endOffset) {
//                if(debug) System.out.println("--- found syntax element ---\n"+sel.toString());
                
                if(sel instanceof SyntaxElement.Error) {
                    if(debug) System.out.println("Error found! => breaking the generation.");
                    throw new DocumentModelException("XML File is unparsable.");
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
                            sel = sup.getElementChain(Math.min(doc.getLength(), tagDE.getEndOffset() + 1));
                            skipped.add(tagDE);
                            continue;
                        }
                    }
                    
                    //add the tag syntax element into stack
                    elementsStack.add(sel);
                    
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
                    addedElements.add(dtm.addDocumentElement(((ProcessingInstructionImpl)sel).getNodeName(), XML_PI, Collections.EMPTY_MAP,
                            sel.getElementOffset(), getSyntaxElementEndOffset(sel)));
                } else if (sel instanceof DocumentTypeImpl) {
                    //document type <!DOCTYPE xxx [...]>
                    addedElements.add(dtm.addDocumentElement(((DocumentTypeImpl)sel).getName(), XML_DOCTYPE, Collections.EMPTY_MAP,
                            sel.getElementOffset(), getSyntaxElementEndOffset(sel)));
                } else if (sel instanceof CommentImpl) {
                    //comment element <!-- xxx -->
                    //DO NOT CREATE ELEMENT FOR COMMENTS
//                    addedElements.add(dtm.addDocumentElement("", XML_COMMENT, Collections.EMPTY_MAP,
//                            sel.getElementOffset(), getSyntaxElementEndOffset(sel)));
                } else {
                    //everything else is content
                    addedElements.add(dtm.addDocumentElement("...", XML_CONTENT, Collections.EMPTY_MAP, sel.getElementOffset(), getSyntaxElementEndOffset(sel)));
                }
                //find next syntax element
                sel = sel.getNext();
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
        HashMap map = new HashMap(tag.getAttributes().getLength());
        for(int i = 0; i < tag.getAttributes().getLength(); i++) {
            AttrImpl attr = (AttrImpl)tag.getAttributes().item(i);
            map.put(attr.getName(), attr.getValue());
        }
        return map;
    }
    
    public static final String XML_TAG = "tag";
    public static final String XML_EMPTY_TAG = "empty_tag";
    public static final String XML_CONTENT = "content";
    public static final String XML_PI = "pi";
    public static final String XML_CDATA = "cdata";
    public static final String XML_DOCTYPE = "doctype";
    public static final String XML_COMMENT = "comment";
    
    
    private static final boolean debug = Boolean.getBoolean("org.netbeans.modules.xml.text.structure.debug");
    
}
