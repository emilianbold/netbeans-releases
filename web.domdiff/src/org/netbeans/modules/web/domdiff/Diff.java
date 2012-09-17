/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.domdiff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.html.editor.lib.api.HtmlSource;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.CloseTag;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.ElementFilter;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.web.domdiff.DiffElement.ChangeType;

/**
 * Calculate differences between two DOM trees.
 */
public class Diff {

    private OpenTag version1;
    private OpenTag version2;
    private DiffElement diff;
    private HtmlSource s1;
    private HtmlSource s2;

    /**
     * 
     * @param s1 - old version 
     * @param s2 - new version
     * @param v1 - root old version tag
     * @param v2 - root new version tag
     */
    public Diff(HtmlSource s1, HtmlSource s2, OpenTag v1, OpenTag v2) {
        this.s1 = s1;
        this.s2 = s2;
        this.version1 = v1;
        this.version2 = v2;
        diff = new DiffElement(v1, v2, ChangeType.NONE);
    }
    
    /**
     * Calculate differences.
     * 
     * @param previousDiff can be null otherwise used to identify previous document changes
     * @param previousDiffIndex can be -1 if not applicable
     * @return list of changes to persist and display over document
     */
    public List<Change> compare(Diff previousDiff, int previousDiffIndex) {
        compare(diff, version1, version2);
        
        detectMoves();
        detectMovedTextElement();

        if (previousDiff != null) {
            recordPreviouChanges(previousDiff.diff, this.diff, previousDiffIndex);
        }
        
        List<Change> result = new ArrayList<Change>();
        generateListOfChanges(diff, result, -1);        
        Collections.sort(result, CHANGE_COMPARATOR);
        return result;
    }

    List<Change> compare() {
        return compare(null, -1);
    }
    
    private void detectMoves() {
        
        List<DiffElement> removed = new ArrayList<DiffElement>();
        getElementsForChange(diff, removed, DiffElement.ChangeType.REMOVED);
        List<DiffElement> added = new ArrayList<DiffElement>();
        getElementsForChange(diff, added, DiffElement.ChangeType.ADDED);
        
        for (DiffElement r : removed) {
            DiffElement moved = findMovedElement(r, added);
            if (moved != null) {
                moved.markAsMovedFrom(r);
                // node which was before marked as ADDED does not have any attributes.
                // now when the node is ADDED_BY_MOVE i need to add also all its attributes:
                List<Attribute> l = new ArrayList<Attribute>(moved.getCurrentElement().attributes());
                List<DiffAttribute> attrs = new ArrayList<DiffAttribute>();
                for (Attribute a : l) {
                    attrs.add(new DiffAttribute(null, a, DiffAttribute.ChangeType.NONE));
                }
                moved.setAttribute(attrs);
            }
        }
    }
    
    private void detectMovedTextElement() {
        List<DiffText> removed = new ArrayList<DiffText>();
        getTextElementsForChange(diff, removed, DiffText.ChangeType.REMOVED);
        List<DiffText> added = new ArrayList<DiffText>();
        getTextElementsForChange(diff, added, DiffText.ChangeType.ADDED);
        
        for (DiffText r : removed) {
            CharSequence rValue = getTextNodeValue(s1, r.getPreviousText());
            for (DiffText a : added) {
                CharSequence aValue = getTextNodeValue(s2, a.getCurrentText());
                if (aValue.equals(rValue) && a.getChange() == DiffText.ChangeType.ADDED
                        && r.getChange() == DiffText.ChangeType.REMOVED) {
                    a.markAsMovedFrom(r);
                }
            }
        }
    }
    
    void compare(DiffElement parent, OpenTag e1, OpenTag e2) {
        
        // create lists of open tags
        List<OpenTag> v1 = getChildren(e1);
        List<OpenTag> v2 = getChildren(e2);
        
        // compare the tags in the lists
        List<DiffElement> result = compareSiblings(v1, v2);
        parent.setChildren(result);
        
        // check attributes on this node
        List<DiffAttribute> attrResult = compareAttributes(e1, e2);
        parent.setAttribute(attrResult);
        
        // check text nodes on this node
        List<DiffText> textResult = compareText(e1, e2);
        parent.setText(textResult);
        
        // for all tags which are ChangeType.NONE iterate over their children
        // and for others mark them either as added or removed
        for (DiffElement de : result) {
            if (de.getChange() == DiffElement.ChangeType.NONE) {
                compare(de, de.getPreviousElement(), de.getCurrentElement());
            } else if (de.getChange() == DiffElement.ChangeType.REMOVED) {
                addChildrenAsChanged(de, DiffElement.ChangeType.REMOVED);
                addAttributeAsChanged(de, DiffAttribute.ChangeType.REMOVED);
                addChildrenTextAsChanged(de, DiffText.ChangeType.REMOVED);
            } else if (de.getChange() == DiffElement.ChangeType.ADDED) {
                addChildrenAsChanged(de, DiffElement.ChangeType.ADDED);
                addAttributeAsChanged(de, DiffAttribute.ChangeType.ADDED);
                addChildrenTextAsChanged(de, DiffText.ChangeType.ADDED);
            }
        }
    }
    
    List<DiffElement> compareSiblings(List<OpenTag> v1, List<OpenTag> v2) {
        List<DiffElement> result = new ArrayList<DiffElement>();
        // check if lists are the same:
        if (v1.size() == v2.size() && sameLists(v1,v2)) {
            for (int i = 0; i < v1.size(); i++) {
                OpenTag e = v1.get(i);
                OpenTag e2 = v2.get(i);
                result.add(new DiffElement(e, e2, ChangeType.NONE));
            }
            return result;
        }
        // check if one of the lists was empty before:
        if (v1.isEmpty()) {
            for (OpenTag e2 : v2) {
                result.add(new DiffElement(null, e2, DiffElement.ChangeType.ADDED));
            }
            return result;
        }
        if (v2.isEmpty()) {
            for (OpenTag e1 : v1) {
                result.add(new DiffElement(e1, null, DiffElement.ChangeType.REMOVED));
            }
            return result;
        }
        // lists have some differencies; let's figure out what they are:
        List<Match> matches = findMatches(v1, v2);
        
        // I have matches now and will try to use them to break lists into sublists
        // and these sublists then will be ordered is some stupid manner:
        List<ListPair> sublists = createSublists(matches, v1, v2);
        
        for (ListPair l : sublists) {
            if (l.processed) {
                for (int i = 0; i < l.v1.size(); i++) {
                    OpenTag e = l.v1.get(i);
                    OpenTag e2 = l.v2.get(i);
                    result.add(new DiffElement(e, e2, ChangeType.NONE));
                }
            } else {
                dumbCompare(result, l);
            }
        }
        return result;
    }
    
    
    private List<ListPair> createSublists(List<Match> matches, List<OpenTag> v1, List<OpenTag> v2) {
        if (matches.isEmpty()) {
            return Collections.singletonList(new ListPair(v1, v2, false));
        }
        List<ListPair> res = new ArrayList<ListPair>();
        int start1 = 0;
        int start2 = 0;
        for (Match m : matches) {
            List<OpenTag> sub1 = v1.subList(start1, m.i1);
            List<OpenTag> sub2 = v2.subList(start2, m.i2);
            ListPair pair = new ListPair(sub1, sub2, false);
            if (sub1.isEmpty() && sub2.isEmpty()) {
                // ignore empty one
            } else {
                res.add(pair);
            }
            // record match itself:
            pair = new ListPair(v1.subList(m.i1, m.i1+1), v2.subList(m.i2, m.i2+1), true);
            res.add(pair);
            start1 = m.i1+1;
            start2 = m.i2+1;
        }
        // add reminding items:
        List<OpenTag> sub1 = v1.subList(start1, v1.size());
        List<OpenTag> sub2 = v2.subList(start2, v2.size());
        ListPair pair = new ListPair(sub1, sub2, false);
        if (sub1.isEmpty() && sub2.isEmpty()) {
            // ignore empty one
        } else {
            res.add(pair);
        }
        
        return res;
    }

    private void dumbCompare(List<DiffElement> result, ListPair l) {
        for (int i = 0; i < Math.max(l.v1.size(), l.v2.size()); i++) {
            OpenTag e1 = null;
            OpenTag e2 = null;
            if (i < l.v1.size()) {
                e1 = l.v1.get(i);
            }
            if (i < l.v2.size()) {
                e2 = l.v2.get(i);
            }
            if (e1 == null) {
                result.add(new DiffElement(null, e2, DiffElement.ChangeType.ADDED));
            } else if (e2 == null) {
                result.add(new DiffElement(e1, null, DiffElement.ChangeType.REMOVED));
            } else if (e1.name().equals(e2.name())) {
                result.add(new DiffElement(e1, e2, ChangeType.NONE));
            } else {
                result.add(new DiffElement(e1, null, DiffElement.ChangeType.REMOVED));
                result.add(new DiffElement(null, e2, DiffElement.ChangeType.ADDED));
            }
        }
    }

    private List<OpenTag> getChildren(OpenTag element) {
        if (element instanceof Node) {
            Node n = (Node)element;
            return new ArrayList<OpenTag>(n.children(OpenTag.class));
        }
        return Collections.emptyList();
    }

    List<DiffAttribute> compareAttributes(OpenTag e1, OpenTag e2) {
        List<DiffAttribute> result = new ArrayList<DiffAttribute>();
        List<Attribute> v1 = new ArrayList<Attribute>(e1.attributes());
        List<Attribute> v2 = new ArrayList<Attribute>(e2.attributes());
        for (Attribute a1 : v1) {
            Attribute a2 = removeAttribute(a1.name(), v2);
            if (a2 == null) {
                result.add(new DiffAttribute(a1, null, DiffAttribute.ChangeType.REMOVED));
            } else if (a1.value().equals(a2.value())) {
                result.add(new DiffAttribute(a1, a2, DiffAttribute.ChangeType.NONE));
            } else {
                result.add(new DiffAttribute(a1, a2, DiffAttribute.ChangeType.MODIFIED));
            }
        }
        for (Attribute a2 : v2) {
            result.add(new DiffAttribute(null, a2, DiffAttribute.ChangeType.ADDED));
        }
        return result;
    }

    private Attribute removeAttribute(CharSequence name1, List<Attribute> v2) {
        for (Iterator<Attribute> it = v2.iterator(); it.hasNext();) {
            Attribute a2 = it.next();
            if (a2.name().equals(name1)) {
                it.remove();
                return a2;
            }
        }
        return null;
    }

    private static ElementFilter FILTER_OUT_EMPTY_TEXT_NODES = new WhiteSpaceFilter();

    private void recordPreviouChanges(DiffElement previousDiff, DiffElement currentDiff, int previousDiffIndex) {
        // create lists of open tags
        List<DiffElement> v1 = getChildrenLeftSide(previousDiff);
        List<DiffElement> v2 = getChildrenRightSide(currentDiff);
        
        if (!sameLists2(v1, v2)) {
            throw new RuntimeException("lists should be the same:\n "+currentDiff+"\n "+v1+"\n "+v2+"\n\n"+
                    dumpSomeDiagnostics(previousDiff, currentDiff));
        }

        List<DiffAttribute> a1 = getAttributeLeftSide(previousDiff);
        List<DiffAttribute> a2 = getAttributeRightSide(currentDiff);

        if (!sameLists3(a1, a2)) {
            throw new RuntimeException("lists should be the same:\n "+currentDiff+"\n "+a1+"\n "+a2);
        }
        
        for (int i = 0; i < a1.size(); i++) {
            DiffAttribute da1 = a1.get(i);
            DiffAttribute da2 = a2.get(i);
            if (da2.getChange() == DiffAttribute.ChangeType.NONE) {
                if (da1.getChange() == DiffAttribute.ChangeType.ADDED ||
                        da1.getChange() == DiffAttribute.ChangeType.MODIFIED) {
                    da2.setOrigin(previousDiffIndex);
                } else if (da1.getOrigin() != null) {
                    da2.setOrigin(da1.getOrigin());
                }
            }
        }
        
        for (int i = 0; i < v1.size(); i++) {
            DiffElement d1 = v1.get(i);
            DiffElement d2 = v2.get(i);
            if (d2.getChange() == DiffElement.ChangeType.NONE) {
                if (d1.getChange() == DiffElement.ChangeType.ADDED) {
                    d2.setOrigin(previousDiffIndex);
                } else if (d1.getOrigin() != null) {
                    d2.setOrigin(d1.getOrigin());
                }
                recordPreviouChanges(d1, d2, previousDiffIndex);
            }
        }
        
    }
    
    private boolean sameLists2(List<DiffElement> v1, List<DiffElement> v2) {
        if (v1.size() != v2.size()) {
            return false;
        }
        for (int i = 0; i < v1.size(); i++) {
            if (!v1.get(i).getCurrentElement().name().equals(v2.get(i).getPreviousElement().name())) {
                return false;
            }
        }
        return true;
    }
    
    private boolean sameLists3(List<DiffAttribute> v1, List<DiffAttribute> v2) {
        if (v1.size() != v2.size()) {
            return false;
        }
        for (int i = 0; i < v1.size(); i++) {
            if (!v1.get(i).getCurrentAttribute().name().equals(v2.get(i).getPreviousAttribute().name())) {
                return false;
            }
        }
        return true;
    }
    

    private List<DiffElement> getChildrenLeftSide(DiffElement parent) {
        List<DiffElement> l = new ArrayList<DiffElement>();
        for (DiffElement de : parent.getChildren()) {
            if (de.getChange() == DiffElement.ChangeType.REMOVED || 
                    de.getChange() == DiffElement.ChangeType.REMOVED_BY_MOVE) {
                continue;
            }
            l.add(de);
        }
        return l;
    }
    
    private List<DiffElement> getChildrenRightSide(DiffElement parent) {
        List<DiffElement> l = new ArrayList<DiffElement>();
        for (DiffElement de : parent.getChildren()) {
            if (de.getChange() == DiffElement.ChangeType.ADDED || 
                    de.getChange() == DiffElement.ChangeType.ADDED_BY_MOVE) {
                continue;
            }
            l.add(de);
        }
        return l;
    }
    
    private List<DiffAttribute> getAttributeLeftSide(DiffElement parent) {
        List<DiffAttribute> l = new ArrayList<DiffAttribute>();
        for (DiffAttribute de : parent.getAttributes()) {
            if (de.getChange() == DiffAttribute.ChangeType.REMOVED) {
                continue;
            }
            l.add(de);
        }
        Collections.sort(l, ATTRIBUTES_COMPARATOR);
        return l;
    }
    
    private List<DiffAttribute> getAttributeRightSide(DiffElement parent) {
        List<DiffAttribute> l = new ArrayList<DiffAttribute>();
        for (DiffAttribute de : parent.getAttributes()) {
            if (de.getChange() == DiffAttribute.ChangeType.ADDED) {
                continue;
            }
            l.add(de);
        }
        Collections.sort(l, ATTRIBUTES_COMPARATOR);
        return l;
    }

    private void addAttributeAsChanged(DiffElement parentDiff, DiffAttribute.ChangeType changeType) {
        List<DiffAttribute> attrs = new ArrayList<DiffAttribute>();
        OpenTag parentElement;
        if (changeType == DiffAttribute.ChangeType.ADDED) {
            parentElement = parentDiff.getCurrentElement();
        } else {
            assert changeType == DiffAttribute.ChangeType.REMOVED;
            parentElement = parentDiff.getPreviousElement();
        }
        for (Attribute attr : parentElement.attributes()) {
            DiffAttribute childDiff;
            if (changeType == DiffAttribute.ChangeType.ADDED) {
                childDiff = new DiffAttribute(null, attr, changeType);
            } else {
                assert changeType == DiffAttribute.ChangeType.REMOVED;
                childDiff = new DiffAttribute(attr, null, changeType);
            }
            attrs.add(childDiff);
        }
        parentDiff.setAttribute(attrs);
    }

    private String dumpSomeDiagnostics(DiffElement previousDiff, DiffElement currentDiff) {
        StringBuilder sb = new StringBuilder();
        if (previousDiff.getCurrentElement() != null) {
            dumpElement("previous parent: ", previousDiff.getCurrentElement(), sb);
        }
        if (currentDiff.getPreviousElement() != null) {
            dumpElement("current parent: ", currentDiff.getPreviousElement(), sb);
        }
        for (Element e : previousDiff.getCurrentElement().children()) {
            dumpElement("  left node: ", e, sb);
        }
        for (DiffElement de : previousDiff.getChildren()) {
            dumpElement("  left diff: ", de, de.getCurrentElement(), sb);
        }
        for (Element e : currentDiff.getCurrentElement().children()) {
            dumpElement(" right node: ", e, sb);
        }
        for (DiffElement de : currentDiff.getChildren()) {
            dumpElement(" right diff: ", de, de.getPreviousElement(), sb);
        }
        return sb.toString();
    }

    private void dumpElement(CharSequence prefix, Element e, StringBuilder sb) {
        sb.append(prefix).append(""+e.from()+" ").append(e.image()).append("\n");
    }
    
    private void dumpElement(CharSequence prefix, DiffElement de, Element e, StringBuilder sb) {
        if (de == null) {
            sb.append(""+de+" - "+e);
        } else {
            sb.append(prefix).append(""+e.from()+" ").append(e.image()).append(" "+de.getChange()).append("\n");
        }
    }

    private static class WhiteSpaceFilter implements ElementFilter {

        @Override
        public boolean accepts(Element node) {
            if (node.type() == ElementType.TEXT && !isEmpty(node.image())) {
                return true;
            }
            return false;
        }

    }
    
    private static boolean isEmpty(CharSequence image) {
        for (int i = 0; i < image.length(); i++) {
            char ch = image.charAt(i);
            if (ch != ' ' && ch != '\n' && ch != '\t' && ch != '\r') {
                return false;
            }
        }
        return true;
    }
    
    private List<DiffText> compareText(OpenTag e1, OpenTag e2) {
        List<DiffText> result = new ArrayList<DiffText>();
        List<Element> v1 = new ArrayList<Element>(e1.children(FILTER_OUT_EMPTY_TEXT_NODES));
        List<Element> v2 = new ArrayList<Element>(e2.children(FILTER_OUT_EMPTY_TEXT_NODES));
        for (int i = 0; i < Math.max(v1.size(), v2.size()); i++) {
            Element t1 = null;
            CharSequence value1 = null;
            Element t2 = null;
            CharSequence value2 = null;
            if (i < v1.size()) {
                t1 = v1.get(i);
                value1 = getTextNodeValue1(t1);
            }
            if (i < v2.size()) {
                t2 = v2.get(i);
                value2 = getTextNodeValue2(t2);
            }
            if (t1 == null) {
                result.add(new DiffText(null, t2, DiffText.ChangeType.ADDED));
            } else if (t2 == null) {
                result.add(new DiffText(t1, null, DiffText.ChangeType.REMOVED));
            } else if (value1.equals(value2)) {
                result.add(new DiffText(t1, t2, DiffText.ChangeType.NONE));
            } else {
                result.add(new DiffText(t1, t2, DiffText.ChangeType.MODIFIED));
            }
        }
        return result;
    }
    
    private CharSequence getTextNodeValue1(Element e) {
        return getTextNodeValue(s1, e);
    }

    private CharSequence getTextNodeValue2(Element e) {
        return getTextNodeValue(s2, e);
    }

    public static CharSequence getTextNodeValue(HtmlSource source, Element e) {
        assert e.type() == ElementType.TEXT : e;
        return source.getSourceCode().subSequence(e.from(), e.to());
    }

    private void getElementsForChange(DiffElement element, List<DiffElement> all, DiffElement.ChangeType change) {
        if (element.getChange() == change) {
            all.add(element);
        }
        for (DiffElement e : element.getChildren()) {
            getElementsForChange(e, all, change);
        }
    }

    private void getTextElementsForChange(DiffElement element, List<DiffText> all, DiffText.ChangeType change) {
        for (DiffText text : element.getText()) {
            if (text.getChange() == change) {
                all.add(text);
            }
        }
        for (DiffElement e : element.getChildren()) {
            getTextElementsForChange(e, all, change);
        }
    }

    private void addChildrenAsChanged(DiffElement parentDiff, ChangeType changeType) {
        List<DiffElement> children = new ArrayList<DiffElement>();
        OpenTag parentElement;
        if (changeType == DiffElement.ChangeType.ADDED) {
            parentElement = parentDiff.getCurrentElement();
        } else {
            assert changeType == DiffElement.ChangeType.REMOVED;
            parentElement = parentDiff.getPreviousElement();
        }
        for (OpenTag openTag : getChildren(parentElement)) {
            DiffElement childDiff;
            if (changeType == DiffElement.ChangeType.ADDED) {
                childDiff = new DiffElement(null, openTag, changeType);
            } else {
                assert changeType == DiffElement.ChangeType.REMOVED;
                childDiff = new DiffElement(openTag, null, changeType);
            }
            children.add(childDiff);
            addAttributeAsChanged(childDiff, changeType == ChangeType.ADDED ? 
                    DiffAttribute.ChangeType.ADDED : DiffAttribute.ChangeType.REMOVED);
            addChildrenAsChanged(childDiff, changeType);
            int end = openTag.to()+1;
            if (openTag.matchingCloseTag() != null) {
                end = openTag.matchingCloseTag().to()+1;
            }
        }
        parentDiff.setChildren(children);
    }

    private void addChildrenTextAsChanged(DiffElement parentDiff, DiffText.ChangeType changeType) {
        List<DiffText> children = new ArrayList<DiffText>();
        OpenTag parentElement;
        if (changeType == DiffText.ChangeType.ADDED) {
            parentElement = parentDiff.getCurrentElement();
        } else {
            assert changeType == DiffText.ChangeType.REMOVED;
            parentElement = parentDiff.getPreviousElement();
        }
        for (Element text : parentElement.children(FILTER_OUT_EMPTY_TEXT_NODES)) {
            DiffText childDiff;
            if (changeType == DiffText.ChangeType.ADDED) {
                childDiff = new DiffText(null, text, changeType);
            } else {
                assert changeType == DiffText.ChangeType.REMOVED;
                childDiff = new DiffText(text, null, changeType);
            }
            children.add(childDiff);
        }
        parentDiff.setText(children);
        for (DiffElement de : parentDiff.getChildren()) {
            addChildrenTextAsChanged(de, changeType);
        }
    }

    private DiffElement findMovedElement(DiffElement removed, List<DiffElement> added) {
        for (DiffElement a : added) {
            if (a.getChange() != DiffElement.ChangeType.ADDED) {
                continue;
            }
            if (compareTwoOpenTagsById(removed.getPreviousElement(), a.getCurrentElement())) {
                return a;
            }
            if (compareTwoOpenTagsByImage(removed.getPreviousElement(), a.getCurrentElement(), 2)) {
                return a;
            }
        }
        return null;
    }

    private int[] findSameBeginningAndEnd(String oldValue, String newValue) {
        assert !oldValue.equals(newValue) : oldValue;
        int start = 0;
        boolean keepLookingStart = true;
        int end = 0;
        boolean keepLookingEnd = true;
        for (int i = 0; i < Math.min(oldValue.length(), newValue.length()) && keepLookingStart; i++) {
            if (oldValue.charAt(i) == newValue.charAt(i)) {
                start++;
            } else {
                keepLookingStart = false;
            }
        }
        for (int i = 0; i < (Math.min(oldValue.length(), newValue.length())-start) && keepLookingEnd; i++) {
            if (oldValue.charAt(oldValue.length()-1-i) == newValue.charAt(newValue.length()-1-i)) {
                end++;
            } else {
                keepLookingEnd = false;
            }
        }
        return new int[]{start, end};
    }

    private static class ListPair {
        private List<OpenTag> v1;
        private List<OpenTag> v2;
        private boolean processed;

        public ListPair(List<OpenTag> v1, List<OpenTag> v2, boolean processed) {
            this.v1 = v1;
            this.v2 = v2;
            this.processed = processed;
        }
        
    }
    
    private List<Match> findMatches(List<OpenTag> v1, List<OpenTag> v2) {
        List<Match> matches = new ArrayList<Match>();
        for (int i1 = 0; i1 < v1.size(); i1++) {
            OpenTag element = v1.get(i1);
            Match m = new Match(element, i1);
            if (findBestMatch(m, v2)) {
                if (m.type == Match.Type.WEAK) {
                    // only accept weak match if "element" is singleton in v1 as well:
                    if (!singleOccuranceOnly(element, v1)) {
                        continue;
                    }
                } 
                matches.add(m);
            }
        }
        Collections.sort(matches, MATCH_COMPARATOR2);
        filterOutConflictingMatches(matches);
        return matches;
    }
    
    private void filterOutConflictingMatches(List<Match> matches) {
        int bottom1 = -1;
        int bottom2 = -1;
        for (Iterator<Match> it = matches.iterator(); it.hasNext();) {
            Match match = it.next();
            if (match.i1 <= bottom1 || match.i2 <= bottom2) {
                it.remove();
                continue;
            }
            bottom1 = match.i1;
            bottom2 = match.i2;
        }
    }
    
    private boolean findBestMatch(Match m, List<OpenTag> v2) {
        int singleOccurance = -1;
        for (int i2 = 0; i2 < v2.size(); i2++) {
            OpenTag openTag = v2.get(i2);
            if (!openTag.name().equals(m.getT1().name())) {
                continue;
            }
            if (singleOccurance == -1) {
                singleOccurance = i2;
            } else {
                singleOccurance = -2;
            }
            if (compareTwoOpenTagsByImage(m.getT1(), openTag, 1)) {
                // whole tag was matched:
                m.matched(openTag, i2, Match.Type.HARD);
                return true;
            }
            if (compareTwoOpenTagsById(m.getT1(), openTag)) {
                // tag name and id attribute value were matched:
                m.matched(openTag, i2, Match.Type.GOOD);
                return true;
            }
        }
        if (singleOccurance >= 0) {
            // there is only single tag of this name so there is some match:
            m.matched(v2.get(singleOccurance), singleOccurance, Match.Type.WEAK);
            return true;
        }
        return false;
    }
    
    private boolean compareTwoOpenTagsByImage(OpenTag openTag1, OpenTag openTag2, int minimumNumberOfAttrs) {
        try {
            CharSequence image1 = s1.getSourceCode().subSequence(openTag1.from(), openTag1.to());
            CharSequence image2 = s2.getSourceCode().subSequence(openTag2.from(), openTag2.to());
            if (image1.equals(image2) && openTag1.attributes().size() >= minimumNumberOfAttrs) {
                // whole tag was matched:
                return true;
            }
            return false;
        } catch (StringIndexOutOfBoundsException se) {
            // ignore:
            return false;
        }
    }
    
    private boolean compareTwoOpenTagsById(OpenTag v1, OpenTag v2) {
        Attribute id1 = v1.getAttribute("id");
        Attribute id2 = v2.getAttribute("id");
        return id1 != null && id2 != null && id1.value().equals(id2.value());
    }

    private boolean sameLists(List<OpenTag> v1, List<OpenTag> v2) {
        assert v1.size() == v2.size();
        for (int i = 0; i < v1.size(); i++) {
            if (!v1.get(i).name().equals(v2.get(i).name())) {
                return false;
            }
        }
        return true;
    }

    private boolean singleOccuranceOnly(OpenTag element, List<OpenTag> v1) {
        Boolean once = null;
        for (OpenTag e : v1) {
            if (e.name().equals(element.name())) {
                if (once == null) {
                    once = Boolean.TRUE;
                } else {
                    return false;
                }
            }
        }
        return true;
    }
    
    private void generateListOfChanges(DiffElement diff, List<Change> changes, int offsetForRemovedElements) {
        Change lastChange = null;
        if (!changes.isEmpty()) {
            lastChange = changes.get(changes.size()-1);
        }
        List<Change> temp = new ArrayList<Change>();
        if (diff.getChange() == DiffElement.ChangeType.NONE) {
            for (DiffAttribute a : diff.getAttributes()) {
                if (a.getChange() == DiffAttribute.ChangeType.ADDED) {
                    String addedValue = s2.getSourceCode().subSequence(a.getCurrentAttribute().from(), a.getCurrentAttribute().to()).toString();
                    changes.add(Change.added(a.getCurrentAttribute().from(), a.getCurrentAttribute().to(), addedValue));
                }
                if (a.getChange() == DiffAttribute.ChangeType.MODIFIED) {
                    String oldValue = s1.getSourceCode().subSequence(a.getPreviousAttribute().valueOffset(), a.getPreviousAttribute().to()).toString();
                    String newValue = s2.getSourceCode().subSequence(a.getCurrentAttribute().valueOffset(), a.getCurrentAttribute().to()).toString();
                    int[] remove = findSameBeginningAndEnd(oldValue, newValue);
                    changes.add(Change.removed(a.getCurrentAttribute().valueOffset()+remove[0], a.getPreviousAttribute().valueOffset()+remove[0], oldValue.substring(remove[0], oldValue.length()-remove[1])));
                    changes.add(Change.added(a.getCurrentAttribute().valueOffset()+remove[0], a.getCurrentAttribute().to()-remove[1], newValue.substring(remove[0], newValue.length()-remove[1])));
                }
                if (a.getChange() == DiffAttribute.ChangeType.REMOVED) {
                    // "+1" at the end of next line is attempt to remove also space following attribute definition
                    String removedValue = s1.getSourceCode().subSequence(a.getPreviousAttribute().from(), a.getPreviousAttribute().to()+1).toString();
                    if (!removedValue.endsWith(" ")) {
                        removedValue = removedValue.subSequence(0, removedValue.length()-1).toString();
                    }
                    temp.add(Change.removed(-1, a.getPreviousAttribute().from(), removedValue));
                }
                if (a.getChange() == DiffAttribute.ChangeType.NONE && a.getOrigin() != null) {
                    changes.add(Change.origin(a.getCurrentAttribute().from(), a.getCurrentAttribute().to(), a.getOrigin().getIndex()));
                }
            }
        }
        if (!temp.isEmpty()) {
            assert diff.getChange() == DiffElement.ChangeType.NONE;
            int index = diff.getCurrentElement().from()+diff.getCurrentElement().name().length()+2;
            for (Change ch : temp) {
                changes.add(Change.removed(index, ch.getOriginalOffset(), ch.getRemovedText()));
            }
        }
        if ((diff.getChange() == DiffElement.ChangeType.NONE || 
                diff.getChange() == DiffElement.ChangeType.ADDED ||
                diff.getChange() == DiffElement.ChangeType.ADDED_BY_MOVE) && 
                diff.getCurrentElement().from() != -1 && diff.getCurrentElement().to() != -1) {
            // record position after any real node - that's were deleted nodes should
            // be inserted
            offsetForRemovedElements = diff.getCurrentElement().to();
        }
        
        if (diff.getChange() == DiffElement.ChangeType.ADDED && diff.getCurrentElement().from() != -1 && diff.getCurrentElement().to() != -1) {
            int end = diff.getCurrentElement().to();
            if (diff.getCurrentElement().matchingCloseTag() != null) {
                end = diff.getCurrentElement().matchingCloseTag().to();
            }
            String addedValue = s2.getSourceCode().subSequence(diff.getCurrentElement().from(), end).toString();
            Change ch = Change.added(diff.getCurrentElement().from(), end, addedValue);
            if (lastChange != null && lastChange.isAdd() && ch.getOffset() > lastChange.getOffset() &&
                    end < lastChange.getEndOffsetOfNewText()) {
                // region alrady covered: ignore it
            } else {
                changes.add(ch);
            }
        } else if (diff.getChange() == DiffElement.ChangeType.REMOVED && diff.getPreviousElement().from() != -1 && diff.getPreviousElement().to() != -1) {
            assert offsetForRemovedElements != -1;
            int end = diff.getPreviousElement().to();
//            if (diff.getPreviousElement().matchingCloseTag() != null) {
//                end = diff.getPreviousElement().matchingCloseTag().to();
//            }
            String removedValue = s1.getSourceCode().subSequence(diff.getPreviousElement().from(), end).toString();
            int originalOffset = diff.getPreviousElement().from();
            if (lastChange != null) {
                if (lastChange.isAdd() || lastChange.getOffset() != offsetForRemovedElements) {
                    lastChange = null;
                } else {
                    changes.remove(changes.size()-1);
                    removedValue = lastChange.getRemovedText() + removedValue;
                    originalOffset = lastChange.getOriginalOffset();
                }
            }
            changes.add(Change.removed(offsetForRemovedElements, originalOffset, removedValue));
        } else if (diff.getChange() == DiffElement.ChangeType.ADDED_BY_MOVE) {
            if (lastChange != null && lastChange.isAdd() && diff.getCurrentElement().from() > lastChange.getOffset() &&
                    diff.getCurrentElement().to() < lastChange.getEndOffsetOfNewText()) {
                int end = diff.getCurrentElement().to();
                if (diff.getCurrentElement().matchingCloseTag() != null) {
                    end = diff.getCurrentElement().matchingCloseTag().to();
                }
                changes.remove(lastChange);
                String addedValue = s2.getSourceCode().subSequence(lastChange.getOffset(), diff.getCurrentElement().from()).toString();
                changes.add(Change.added(lastChange.getOffset(), diff.getCurrentElement().from(), addedValue));
                addedValue = s2.getSourceCode().subSequence(end, lastChange.getEndOffsetOfNewText()).toString();
                changes.add(Change.added(end, lastChange.getEndOffsetOfNewText(), addedValue));
            }
        } else if (diff.getChange() == DiffElement.ChangeType.NONE && diff.getOrigin() != null) {
            changes.add(Change.origin(diff.getCurrentElement().from(), diff.getCurrentElement().to(), diff.getOrigin().getIndex()));
        }
        
        for (DiffElement e : diff.getChildren()) {
            generateListOfChanges(e, changes, offsetForRemovedElements);
            if (e.getChange() == DiffElement.ChangeType.NONE || 
                    e.getChange() == DiffElement.ChangeType.ADDED ||
                    e.getChange() == DiffElement.ChangeType.ADDED_BY_MOVE) {
                // record position after any real node - that's were deleted nodes should
                // be inserted
                offsetForRemovedElements = e.getCurrentElement().to();
                CloseTag ct = e.getCurrentElement().matchingCloseTag();
                if (ct != null) {
                    offsetForRemovedElements = ct.to();
                }
            }
        }
    }
    
    private static class Match {
        
        public enum Type {
            HARD,
            GOOD,
            WEAK
        }
        OpenTag t1;
        int i1;
        OpenTag t2;
        int i2;
        Type type;

        public Match(OpenTag t1, int i1) {
            this.t1 = t1;
            this.i1 = i1;
        }
        
        public void matched(OpenTag t2, int i2, Type type) {
            this.t2 = t2;
            this.i2 = i2;
            this.type = type;
        }

        public OpenTag getT1() {
            return t1;
        }

        public OpenTag getT2() {
            return t2;
        }

        @Override
        public String toString() {
            return "Match{" + " [" + t1 + ", " + i1 + "], [" + t2 + ", " + i2 + "], type=" + type + '}';
        }
        
    }

    // sort Matches from the most important one to least and from top to bottom
    private static final MatchComparator MATCH_COMPARATOR = new MatchComparator();
    
    // sort Matches from top to bottom
    private static final MatchComparator2 MATCH_COMPARATOR2 = new MatchComparator2();
    
    private static class MatchComparator implements Comparator<Match> {

        @Override
        public int compare(Match o1, Match o2) {
            if (o1.type == o2.type) {
                return o1.i1 - o2.i1;
            } else {
                return o1.type.compareTo(o2.type);
            }
        }
    }
    
    private static class MatchComparator2 implements Comparator<Match> {

        @Override
        public int compare(Match o1, Match o2) {
            return o1.i1 - o2.i1;
        }
    }

    public static final ChangeComparator CHANGE_COMPARATOR = new ChangeComparator();
    
    public static class ChangeComparator implements Comparator<Change> {

        @Override
        public int compare(Change o1, Change o2) {
            if (o1.getOffset() == o2.getOffset()) {
                if (o1.isRemove() && !o2.isRemove())  {
                    return -1;
                } else if (!o1.isRemove() && o2.isRemove())  {
                    return 1;
                } else {
                    return 0;
                }
            }
            return o1.getOffset() - o2.getOffset();
        }
    }


    public static final AttributesComparator ATTRIBUTES_COMPARATOR = new AttributesComparator();
    
    public static class AttributesComparator implements Comparator<DiffAttribute> {

        @Override
        public int compare(DiffAttribute o1, DiffAttribute o2) {
            return (o1.getPreviousAttribute() != null ? o1.getPreviousAttribute().name() : o1.getCurrentAttribute().name()).toString().compareTo(
                    (o2.getPreviousAttribute() != null ? o2.getPreviousAttribute().name() : o2.getCurrentAttribute().name()).toString());
        }
    }

    public static class SingleDiff {
        private int start;
        private int end;
    
    }
    
}
