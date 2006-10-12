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

package org.netbeans.modules.xml.xdm.diff;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.text.syntax.XMLKit;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xdm.XDMModel;
import org.netbeans.modules.xml.xdm.nodes.Attribute;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.netbeans.modules.xml.xdm.nodes.NodeImpl;
import org.netbeans.modules.xml.xdm.nodes.Text;
import org.netbeans.modules.xml.xdm.diff.Change.AttributeChange;
import org.netbeans.modules.xml.xdm.diff.Change.AttributeDiff;
import org.netbeans.modules.xml.xdm.visitor.PositionFinderVisitor;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import javax.swing.text.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 *
 * @author Ayub Khan
 */
public class XDMUtil {
    
    public enum ComparisonCriteria {
        EQUAL,
        IDENTICAL;
    }
    
    /**
     * Constructor for XDMUtil
     */
    public XDMUtil() {
    }

    /*
     * returns a pretty print version of xml doc, using given indentation
     */
    public String prettyPrintXML(String doc, String indentation) 
    throws UnsupportedEncodingException, IOException, BadLocationException {
        Document sd1 = new BaseDocument(XMLKit.class, false);
        XDMModel m1 = createXDMModel(sd1, doc);
        Node root1 = m1.getDocument();
        
        Document sd2 = new BaseDocument(XMLKit.class, false);
        XDMModel m2 = createXDMModel(sd2);
        m2.setPretty(true);
        m2.setIndentation(indentation);
        m2.sync();
        Node root2 = m2.getDocument();
        
        root2 = doPrettyPrint(m2, root2, root1);
        m2.flush();
        
        String prettyXMLStr = sd2.getText(0, sd2.getLength());
        
        int firstChildPos1 = -1;
        Node firstChild1 = (Node) root1.getChildNodes().item(0);
        if(firstChild1 != null)
            firstChildPos1 = new PositionFinderVisitor().findPosition(
                    m1.getDocument(), firstChild1);        
        
        int firstChildPos2 = -1;
        Node firstChild2 = (Node) root2.getChildNodes().item(0);
        if(firstChild2 != null)
            firstChildPos2 = new PositionFinderVisitor().findPosition(
                    m2.getDocument(), firstChild2);  
        
        return (firstChildPos1==-1?doc:(sd1.getText(0, firstChildPos1) + 
                    sd2.getText(firstChildPos2, sd2.getLength() - firstChildPos2)));      
    }
    
    /*
     * compares 2 xml document contents using a criteria
     * 
     * @param firstDoc
     * @param secondDoc
     * @param type
     *   ComparisonCriteria.EQUAL - means Two documents are considered to be
     *      equal if they contain the same elements and attributes regardless
     *      of order.
     *   ComparisonCriteria.IDENTICAL -  means Two documents are considered to
     *      be "identical" if they contain the same elements and attributes in
     *      the same order.
     * filters -
     *      Whitespace diffs
     *      Namespace attribute diffs
     *      Namespace attribute prefix diffs
     *      Attribute whitespace diffs
     *
     * Use the next API (4 argument compareXML api) if you do not want to filter
     * all of the above 4 types of diffs
     */  
    public List<Difference> compareXML(String xml1, String xml2,
            XDMUtil.ComparisonCriteria criteria)
            throws Exception {
        XDMUtil util = new XDMUtil();
        List<Difference> diffs = compareXML(xml1, xml2, criteria, true);
        filterNSAttrDiffs(diffs);
        filterNSPrefixDiffs(diffs);
        filterAttrWhitespaceDiffs(diffs);
        return diffs;
    }
    
    /*
     * compares 2 xml document contents using a criteria
     * 
     * @param firstDoc
     * @param secondDoc
     * @param type
     *   ComparisonCriteria.EQUAL - means Two documents are considered to be
     *      equal if they contain the same elements and attributes regardless
     *      of order.
     *   ComparisonCriteria.IDENTICAL -  means Two documents are considered to
     *      be "identical" if they contain the same elements and attributes in
     *      the same order.
     * @param ignoreWhiteSpace - filters whitespace diffs
     */    
    public List<Difference> compareXML(String firstDoc,  
            String secondDoc, ComparisonCriteria type, boolean filterWhiteSpace) 
    throws BadLocationException, IOException {
        Document sd1 = new BaseDocument(XMLKit.class, false);
        XDMModel m1 = createXDMModel(sd1);
        sd1.remove(0, XML_PROLOG.length());
        sd1.insertString(0, firstDoc, null);
        m1.sync();
        
        Document sd2 = new BaseDocument(XMLKit.class, false);
        sd2.getText(0, sd2.getLength());
        XDMModel m2 = createXDMModel(sd2);
        sd2.remove(0, XML_PROLOG.length());
        sd2.insertString(0, secondDoc, null);
        m2.setPretty(true);
        m2.sync();
        
        XDMTreeDiff dif = new XDMTreeDiff(m1.getElementIdentity());
        List<Difference> diffs = 
                dif.performDiff(m1.getDocument(), m2.getDocument());
        if(filterWhiteSpace)
            diffs = filterWhitespace(diffs);//filter whitespace diffs
        if(type == ComparisonCriteria.EQUAL) {//remove order change diffs
            List<Difference> filteredDiffs = new ArrayList<Difference>();
            for(Difference d:diffs) {
                if(d instanceof Change) {
                    Change c = (Change)d;
                    if(c.isPositionChanged())//node (element/text) pos change
                        if(!c.isTokenChanged() && !c.isAttributeChanged())
                            continue;
                    if(c.isAttributeChanged() && !c.isTokenChanged()) {//attr change only
                        List<Change.AttributeDiff> removeList = 
                                new ArrayList<Change.AttributeDiff>();
                        List<Change.AttributeDiff> attrChanges = c.getAttrChanges();
                        for(int i=0;i<attrChanges.size();i++) {
                            if(attrChanges.get(i) instanceof Change.AttributeChange) {
                                Change.AttributeChange ac = 
                                    (Change.AttributeChange) attrChanges.get(i);
                                if(ac.isPositionChanged() && !ac.isTokenChanged())//attr pos change only
                                    removeList.add(ac);
                            }
                        }
                        for(int i=0;i<removeList.size();i++)
                            c.removeAttrChanges(removeList.get(i));
                        if(c.getAttrChanges().size() == 0) //filter this diff
                            continue;
                    }
                    filteredDiffs.add(d);
                }
            }
            return filteredDiffs;
        }
        return diffs;
    }

    private XDMModel createXDMModel(Document sd)
    throws BadLocationException, IOException {
        return createXDMModel(sd, "");
    }
    
    private XDMModel createXDMModel(Document sd, String content) 
    throws BadLocationException, IOException {
        boolean foundXMLProlog = true;
        if(content.indexOf("<?xml") == -1) //insert xml prolog, otherwise XMLSyntaxParser will fail
            sd.insertString(0, XML_PROLOG+content, null);
        else
            sd.insertString(0, content, null);
        Lookup lookup = Lookups.singleton(sd);
        ModelSource ms = new ModelSource(lookup, true);
        XDMModel model = new XDMModel(ms);
        model.sync();
        return model;
    }
    
    
    private Node doPrettyPrint(XDMModel m2, Node n2, Node n1) {
        Node newNode = null;        
        NodeList childs1 = n1.getChildNodes();
        int count = 0;
        for(int i=0;i<childs1.getLength();i++) {
            n1 = (NodeImpl) childs1.item(i);
            newNode = ((NodeImpl)n1).copy();           
            List<Node> ancestors = m2.add(n2, newNode, count++);
            n2 = ancestors.get(0);
        }
        List<Node> ancestors = new ArrayList<Node>();
        fixPrettyText(m2, n2, ancestors);
        n2 = ancestors.get(0);
        return n2;
    }    
    
    private void fixPrettyText(XDMModel m, final Node n, List<Node> ancestors) {
        Node parent = n;
        int index = m.getIndentation().length();
        NodeList childs = parent.getChildNodes();
        List<Node> visitList = new ArrayList<Node>();
        for(int i=0;i<childs.getLength();i++) {
            Node child = (Node) childs.item(i);
            if(checkPrettyText(child)) {
                Text txt = (Text) ((NodeImpl)child).cloneNode(true);
                if(txt.getText().length() >= (index+1))
                    txt.setText("\n"+txt.getText().substring(index+1));
                List<Node> ancestors2 = m.modify(child, txt);
                parent = ancestors2.get(0);
            }
            else if(childs.item(i) instanceof Element)
                visitList.add((Node)childs.item(i));
        }
        ancestors.add(parent);
        for(int i=0;i<visitList.size();i++) {
            fixPrettyText(m, (Node)visitList.get((i)), ancestors);
        }
        visitList.clear(); //no need to keep it beyond here
    }    

    public static boolean checkPrettyText(Node txt) {
        if (txt instanceof Text) {
            if ((((NodeImpl)txt).getTokens().size() == 1) &&
                    isWhitespaceOnly(((NodeImpl)txt).getTokens().get(0).getValue())) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isPossibleWhiteSpace(String text) {
        return text.length() > 0 &&
                Character.isWhitespace(text.charAt(0)) &&
                Character.isWhitespace(text.charAt(text.length()-1));
    }
    
    public static boolean isWhitespaceOnly(String tn) {
        return isPossibleWhiteSpace(tn) &&
                tn.trim().length() == 0;
    }
    

    public static int findPosition(final Node n) {
        return new PositionFinderVisitor().findPosition(
                (Node)n.getOwnerDocument(), n);
    }
    
    /*
     * filters or removes diffs that are whitespace changes only
     */
    public static List<Difference> filterWhitespace(final List<Difference> diffs) {
        return DiffFinder.filterWhitespace(diffs);
    }
    
    /*
     * filters or removes diffs that are attr position changes
     */
    public static void filterAttributeOrderChange(final List<Difference> diffs) {
        List<Difference> removeDiffs = new ArrayList<Difference>();
        for(Difference dif:diffs) {
            if(dif instanceof Change) {
                Change c = (Change)dif;
                //filter attibute position changes only
                if(c.isAttributeChanged() && !c.isPositionChanged() && !c.isTokenChanged()) {
                    List<Change.AttributeDiff> attrdiffs = c.getAttrChanges();
                    int size = attrdiffs.size();
                    List<Change.AttributeDiff> removeAttrs = new ArrayList<Change.AttributeDiff>();
                    for(Change.AttributeDiff attrdif:attrdiffs) {
                        if(attrdif instanceof Change.AttributeChange) {
                            Change.AttributeChange attrChange =
                                    (AttributeChange) attrdif;
                            if(attrChange.isPositionChanged() && !attrChange.isTokenChanged())
                                removeAttrs.add(attrdif);
                        }
                    }
                    for(Change.AttributeDiff attrdif:removeAttrs) {
                        c.removeAttrChanges(attrdif);
                    }
                    if(size > 0 && c.getAttrChanges().size() == 0)
                        removeDiffs.add(dif);
                }
            }
        }
        for(Difference dif:removeDiffs) {
            diffs.remove(dif);
        }
    }
    
    /*
     * filters or removes diffs that are ns attr "xmlns:prefix='some url'"
     */
    public static void filterNSAttrDiffs(final List<Difference> diffs) {
        List<Difference> removeDiffs = new ArrayList<Difference>();
        for(Difference dif:diffs) {
            if(dif instanceof Change) {
                //Add, Delete, Change types of differences apply to Nodes [Element, Text, CData] in
                //xml.
                //Add, Delete diffs indicates add or delete of nodes (element, text, cdata)
                //Change has a bigger role in xml diff, it contains three types of changes
                //related to an element (and 1,2 types for text, cdata). They are
                //1) element/text/cdata token change, ie., <test x="x"/> is diff from <test  x="x"/>
                //there is a extra space before attribute x, and is counted as token diff for element 'test'
                // usage:  Change.isTokenChanged() returns true if there is a token change
                //2) element/text/cdata position change within a parent element.
                // usage:  Change.isPositionChanged() returns true if there is a position change
                //3) element attribute changes. They are further classified into 3
                //   Change.AttributeAdd, Change.AttributeDelete, Change.AttributeChange
                // usage:  Change.getAttrChanges() returns list of all (Change.AttributeDiff) attribute changes,
                //iterate through the list and do a instanceof to Change.AttributeAdd, Change.AttributeDelete,
                //Change.AttributeChange, to process each one. Change.AttributeChange futher
                //tells you if there is position and/or token change involved.
                //usage: Change.AttributeChange.isTokenChanged(), Change.AttributeChange.isPositionChanged()
                Change c = (Change)dif;
                //filter namespace attibute changes only
                if(c.isAttributeChanged() && !c.isPositionChanged() &&
                        !c.isTokenChanged() && (removeNSAttrDiffs(c) ||
                        removeSchemaLocationAttrDiffs(c))) {
                    removeDiffs.add(dif);
                }
            }
        }
        for(Difference dif:removeDiffs) {
            diffs.remove(dif);
        }
    }
    
    /*
     * filters or removes diffs that are token changes <h:somename> -> <a:somename>
     */
    public static void filterNSPrefixDiffs(final List<Difference> diffs) {
        List<Difference> removeDiffs = new ArrayList<Difference>();
        for(Difference dif:diffs) {
            if(dif instanceof Change) {
                Change c = (Change)dif;
                if(c.isTokenChanged() && !c.isPositionChanged()) {
                    String oName = c.getOldNodeInfo().getNode().getNodeName().trim();
                    oName = oName.substring(oName.indexOf(':')!=-1?oName.indexOf(':')+1:0);
                    String nName = c.getNewNodeInfo().getNode().getNodeName().trim();
                    nName = nName.substring(nName.indexOf(':')!=-1?nName.indexOf(':')+1:0);
                    if(oName.equals(nName) && (!c.isAttributeChanged() ||
                            c.isAttributeChanged() && (removeNSAttrDiffs(c) ||
                            removeSchemaLocationAttrDiffs(c))))
                        removeDiffs.add(dif);
                }
            }
        }
        for(Difference dif:removeDiffs) {
            diffs.remove(dif);
        }
    }
    
    /*
     * filters or removes diffs that are attr whitespace changes x="y" -> x ="y" or x= "y"
     */
    public static void filterAttrWhitespaceDiffs(final List<Difference> diffs) {
        List<Difference> removeDiffs = new ArrayList<Difference>();
        for(Difference dif:diffs) {
            if(dif instanceof Change) {
                Change c = (Change)dif;
                //filter whitespace between attibute changes only
                if(c.isAttributeChanged() && !c.isPositionChanged() && !c.isTokenChanged()) {
                    List<Change.AttributeDiff> attrdiffs = c.getAttrChanges();
                    int size = attrdiffs.size();
                    List<Change.AttributeDiff> removeAttrs = new ArrayList<Change.AttributeDiff>();
                    for(Change.AttributeDiff attrdif:attrdiffs) {
                        if(attrdif instanceof Change.AttributeChange) {
                            Change.AttributeChange attrChange =
                                    (AttributeChange) attrdif;
                            if(!attrChange.isPositionChanged()) {
                                Attribute oldAttr = attrdif.getOldAttribute();
                                Attribute newAttr = attrdif.getNewAttribute();
                                if(oldAttr != null && newAttr != null &&
                                        oldAttr.getNodeValue().trim().equals(
                                        newAttr.getNodeValue().trim()))
                                    removeAttrs.add(attrdif);
                            }
                        }
                    }
                    for(Change.AttributeDiff attrdif:removeAttrs) {
                        c.removeAttrChanges(attrdif);
                    }
                    if(size > 0 && attrdiffs.size() == 0)
                        removeDiffs.add(dif);
                }
            }
        }
        for(Difference dif:removeDiffs) {
            diffs.remove(dif);
        }
    }
    
    /*
     * removes attr diffs that are ns attr "xmlns:prefix='some url'"
     */
    public static boolean removeNSAttrDiffs(Change c) {
        List<Change.AttributeDiff> attrdiffs = c.getAttrChanges();
        int size = attrdiffs.size();
        List<Change.AttributeDiff> removeAttrs = new ArrayList<Change.AttributeDiff>();
        for(Change.AttributeDiff attrdif:attrdiffs) {
            Attribute oldAttr = attrdif.getOldAttribute();
            Attribute newAttr = attrdif.getNewAttribute();
            if(oldAttr != null && oldAttr.getName().startsWith(NS_PREFIX))
                removeAttrs.add(attrdif);
            else if(newAttr != null && newAttr.getName().startsWith(NS_PREFIX))
                removeAttrs.add(attrdif);
        }
        for(Change.AttributeDiff attrdif:removeAttrs) {
            c.removeAttrChanges(attrdif);
        }
        if(size > 0 && attrdiffs.size() == 0)
            return true;
        return false;
    }
    
    /*
     * removes attr diffs that are ns attr "prefix:schemaLocation='some url'"
     */
    public static boolean removeSchemaLocationAttrDiffs(Change c) {
        List<Change.AttributeDiff> attrdiffs = c.getAttrChanges();
        int size = attrdiffs.size();
        List<Change.AttributeDiff> removeAttrs = new ArrayList<Change.AttributeDiff>();
        for(Change.AttributeDiff attrdif:attrdiffs) {
            Attribute oldAttr = attrdif.getOldAttribute();
            Attribute newAttr = attrdif.getNewAttribute();
            if(oldAttr != null && oldAttr.getName().endsWith(SCHEMA_LOCATION))
                removeAttrs.add(attrdif);
            else if(newAttr != null && newAttr.getName().endsWith(SCHEMA_LOCATION))
                removeAttrs.add(attrdif);
        }
        for(Change.AttributeDiff attrdif:removeAttrs) {
            c.removeAttrChanges(attrdif);
        }
        if(size > 0 && attrdiffs.size() == 0)
            return true;
        return false;
    }
    
    public final static String NS_PREFIX = "xmlns";
    public final static String SCHEMA_LOCATION = "schemaLocation";    
    public final static String XML_PROLOG = "<?xml version=\"1.0\"?>\n";
}
