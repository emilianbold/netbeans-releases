/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Batik" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation. For more  information on the
 Apache Software Foundation, please see <http://www.apache.org/>.

*/

package org.apache.batik.css.engine;

import org.w3c.css.sac.SACMediaList;
// <rave>
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.batik.css.engine.sac.CSSAndCondition;
import org.apache.batik.css.engine.sac.CSSAttributeCondition;
import org.apache.batik.css.engine.sac.CSSChildSelector;
import org.apache.batik.css.engine.sac.CSSClassCondition;
import org.apache.batik.css.engine.sac.CSSConditionalSelector;
import org.apache.batik.css.engine.sac.CSSDescendantSelector;
import org.apache.batik.css.engine.sac.CSSDirectAdjacentSelector;
import org.apache.batik.css.engine.sac.CSSElementSelector;
import org.apache.batik.css.engine.sac.CSSIdCondition;
import org.apache.batik.css.engine.sac.CSSOneOfAttributeCondition;
import org.apache.batik.css.engine.sac.CSSPseudoElementSelector;
import org.apache.batik.css.engine.sac.ExtendedSelector;
import org.w3c.css.sac.Condition;
import org.w3c.css.sac.NegativeSelector;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SelectorList;
import org.w3c.css.sac.SimpleSelector;
// </rave>

/**
 * This class represents a list of rules.
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @version $Id$
 */
public class StyleSheet {
    
    /**
     * The rules.
     */
// <rave>
    // TODO: Bump up the rule count so I don't have to grow it when dealing with the braveheart stylesheet
// </rave>
    protected Rule[] rules = new Rule[16];
    
    /**
     * The number of rules.
     */
    protected int size;

    /**
     * The parent sheet, if any.
     */
    protected StyleSheet parent;

    /**
     * Whether or not this stylesheet is alternate.
     */
    protected boolean alternate;

    /**
     * The media to use to cascade properties.
     */
    protected SACMediaList media;

    /**
     * The style sheet title.
     */
    protected String title;

    /**
     * Sets the media to use to compute the styles.
     */
    public void setMedia(SACMediaList m) {
        media = m;
    }

    /**
     * Returns the media to use to compute the styles.
     */
    public SACMediaList getMedia() {
        return media;
    }

    /**
     * Returns the parent sheet.
     */
    public StyleSheet getParent() {
        return parent;
    }

    /**
     * Sets the parent sheet.
     */
    public void setParent(StyleSheet ss) {
        parent = ss;
    }

    /**
     * Sets the 'alternate' attribute of this style-sheet.
     */
    public void setAlternate(boolean b) {
        alternate = b;
    }

    /**
     * Tells whether or not this stylesheet is alternate.
     */
    public boolean isAlternate() {
        return alternate;
    }

    /**
     * Sets the 'title' attribute of this style-sheet.
     */
    public void setTitle(String t) {
        title = t;
    }

    /**
     * Returns the title of this style-sheet.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the number of rules.
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns the rule at the given index.
     */
    public Rule getRule(int i) {
        return rules[i];
    }

    /**
     * Clears the content.
     */
    public void clear() {
        size = 0;
        rules = new Rule[10];
    }

    /**
     * Appends a rule to the stylesheet.
     */
    public void append(Rule r) {
        if (size == rules.length) {
            Rule[] t = new Rule[size * 2];
            for (int i = 0; i < size; i++) {
                t[i] = rules[i];
            }
            rules = t;
        }
        rules[size++] = r;
    }

    /**
     * Returns a printable representation of this style-sheet.
     */
    public String toString(CSSEngine eng) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < size; i++) {
            sb.append(rules[i].toString(eng));
        }
        return sb.toString();
    }
// <rave>
    // BEGIN RAVE MODIFICATIONS
    public String toString() {
        return super.toString() + "[" + getTitle() + "]";
    }
    // Support for rule filtering optimization. See CSSEngine.addMatchingRules for details.
    // For rule filtering
    private HashMap tagMap;
    private HashMap classMap;
    private HashMap idMap;
    private Rule[] remainingRules;
    
    /** Return a Map which maps from tag names to ExtendedSelector[] of potential rules
     * that could match an element of the given tag name (obviously the rules
     * may specify additional constraints such as descendant selectors etc. that
     * must be checked.)
     */
    public HashMap getTagMap() {
        return tagMap;
    }
    
    /** Return a Map which maps from class attribute values to ExtendedSelector[] of potential rules
     * that could match an element which has a class attribute including the given style
     * class name. (obviously the rules may specify additional constraints such as 
     * descendant selectors etc. that must be checked.)
     */
    public HashMap getClassMap() {
        return classMap;
    }
    
    /** Return a Map which maps from element ids to ExtendedSelector[] of potential rules
     * that could match an element with the given id attribute (obviously the rules
     * may specify additional constraints such as descendant selectors etc. that
     * must be checked.)
     */
    public HashMap getIdMap() {
        return idMap;
    }
    
    /** Return an array of rules that do not fall into the tag, class or id list of
     * rules. All of these rules must be checked against an element since they couldn't
     * be filtered out based on simple criteria.
     * @todo Remove ExtendedSelectors from the Rule's selector list that are covered
     *   by the other cases. But it turns out that for stylesheets I've looked at, even
     *   moderately complex ones like the braveheart ones, there aren't many of these
     *   so not much would be gained by going to this trouble
     */
    public Rule[] getRemainingRules() {
        return remainingRules;
    }

    private boolean addCondition(Condition condition, Map tagMap, Map classMap, Map idMap, List otherList, Rule rule, ExtendedSelector extSelector) {
        switch (condition.getConditionType()) {
            case Condition.SAC_AND_CONDITION: {
                Condition first = ((CSSAndCondition)condition).getFirstCondition();
                Condition second = ((CSSAndCondition)condition).getSecondCondition();
                // Note short-circuit evaluation
                return addCondition(first, tagMap, classMap, idMap, otherList, rule, extSelector) ||
                       addCondition(second, tagMap, classMap, idMap, otherList, rule, extSelector);
            }
            /*
            case Condition.SAC_OR_CONDITION: {
                Condition first = ((CSSCondition)condition).getFirstCondition();
                Condition second = ((CSSOrCondition)condition).getSecondCondition();
                // Note short-circuit evaluation
                //return addCondition(first, tagMap, classMap, idMap, otherList, rule, extSelector) ||
                //       addCondition(second, tagMap, classMap, idMap, otherList, rule, extSelector);
            }
             */
            case Condition.SAC_CLASS_CONDITION: {
               String styleClass = ((CSSClassCondition)condition).getValue();
               List list = (List)classMap.get(styleClass);
               if (list == null) {
                   list = new ArrayList(30);
                   classMap.put(styleClass, list);
               }
               list.add(extSelector);
               return true;
            }
            case Condition.SAC_ATTRIBUTE_CONDITION: {
               String localName = ((CSSAttributeCondition)condition).getLocalName();
               if (localName.equals("class")) {
                   String styleClass = ((CSSAttributeCondition)condition).getValue();
                   List list = (List)classMap.get(styleClass);
                   if (list == null) {
                       list = new ArrayList(30);
                       classMap.put(styleClass, list);
                   }
                   list.add(extSelector);
                   return true;
               } else {
                   otherList.add(rule);
                   return false;
               }
            }
            case Condition.SAC_ID_CONDITION: {
               String id = ((CSSIdCondition)condition).getValue();
               List list = (List)idMap.get(id);
               if (list == null) {
                   list = new ArrayList(30);
                   idMap.put(id, list);
               }
               list.add(extSelector);
               return true;
            }
            case Condition.SAC_ONE_OF_ATTRIBUTE_CONDITION: {
               String localName = ((CSSOneOfAttributeCondition)condition).getLocalName();
               if (localName.equals("class")) {
                   String styleClass = ((CSSOneOfAttributeCondition)condition).getValue();
                   List list = (List)classMap.get(styleClass);
                   if (list == null) {
                       list = new ArrayList(30);
                       classMap.put(styleClass, list);
                   }
                   list.add(extSelector);
                   return true;
               } else {
                   otherList.add(rule);
                   return false;
               }
            }
            default:
               otherList.add(rule);
               return false;
        }

    }
    
    /**
     * Add the selector to one of the tag map, class map, id map, or the list of other unknown rules.
     * @return True iff the rule was added to one of the maps (e.g. not the otherList).
     */
    private boolean addSelector(Selector selector, Map tagMap, Map classMap, Map idMap, List otherList, Rule rule, ExtendedSelector extSelector) {
        switch (selector.getSelectorType()) {                            
            /**
             * This is a conditional selector.
             * example:
             * <pre class="example">
             *   simple[role="private"]
             *   .part1
             *   H1#myId
             *   P:lang(fr).p1
             * </pre>
             *
             * @see ConditionalSelector
             */
            case Selector.SAC_CONDITIONAL_SELECTOR: {
                Condition condition = ((CSSConditionalSelector)selector).getCondition();
                boolean added = addCondition(condition, tagMap, classMap, idMap, otherList, rule, extSelector);
                if (added) {
                    return true;
                }
                SimpleSelector simple = ((CSSConditionalSelector)selector).getSimpleSelector();
                added = addSelector(simple, tagMap, classMap, idMap, otherList, rule, extSelector); // recurse
                if (added) {
                    return true;
                }
                otherList.add(rule);
                return false;
            }

            /**
             * This selector matches any node.
             * @see SimpleSelector
             */    
            case Selector.SAC_ANY_NODE_SELECTOR:
                // Rules which match all cannot be limited.... right?
                otherList.add(rule);
                return false;

            /**
             * This selector matches the root node.
             * @see SimpleSelector
             */    
            case Selector.SAC_ROOT_NODE_SELECTOR:
                // XXX Decide if I need to worry about this one
                otherList.add(rule);
                return false;

            /**
             * This selector matches only node that are different from a specified one.
             * @see NegativeSelector
             */    
            case Selector.SAC_NEGATIVE_SELECTOR: {
                SimpleSelector simple = ((NegativeSelector)selector).getSimpleSelector();
                addSelector(simple, tagMap, classMap, idMap, otherList, rule, extSelector); // recurse
                return false;
            }

            /**
             * This selector matches only element node.
             * example:
             * <pre class="example">
             *   H1
             *   animate
             * </pre>
             * @see ElementSelector
             */
            case Selector.SAC_ELEMENT_NODE_SELECTOR: {
                String tag = ((CSSElementSelector)selector).getLocalName();
                if (tag != null) {
                    tag = tag.toLowerCase();
                    List list = (List)tagMap.get(tag);
                    if (list == null) {
                        list = new ArrayList(30);
                        tagMap.put(tag, list);
                    }
                    list.add(extSelector);
                }
                return true;
            }

            /**
             * This selector matches only text node.
             * @see CharacterDataSelector
             */
            case Selector.SAC_TEXT_NODE_SELECTOR:
                // XXX Decide if I need to worry about this one
                otherList.add(rule);
                return false;

            /**
             * This selector matches only cdata node.
             * @see CharacterDataSelector
             */
            case Selector.SAC_CDATA_SECTION_NODE_SELECTOR:
                otherList.add(rule);
                return false;

            /**
             * This selector matches only processing instruction node.
             * @see ProcessingInstructionSelector
             */
            case Selector.SAC_PROCESSING_INSTRUCTION_NODE_SELECTOR:
                otherList.add(rule);
                return false;

            /**
             * This selector matches only comment node.
             * @see CharacterDataSelector
             */    
            case Selector.SAC_COMMENT_NODE_SELECTOR:
                otherList.add(rule);
                return false;

            /**
             * This selector matches the 'first line' pseudo element.
             * example:
             * <pre class="example">
             *   :first-line
             * </pre>
             * @see ElementSelector
             */
            case Selector.SAC_PSEUDO_ELEMENT_SELECTOR: {
                String tag = ((CSSPseudoElementSelector)selector).getLocalName();
                if (tag != null) {
                    tag = tag.toLowerCase();
                    List list = (List)tagMap.get(tag);
                    if (list == null) {
                        list = new ArrayList(30);
                        tagMap.put(tag, list);
                    }
                    list.add(extSelector);
                }
                return true;
            }

            /* combinator selectors */

            /**
             * This selector matches an arbitrary descendant of some ancestor element.
             * example:
             * <pre class="example">
             *   E F
             * </pre>
             * @see DescendantSelector
             */    
            case Selector.SAC_DESCENDANT_SELECTOR: {
                SimpleSelector simple = ((CSSDescendantSelector)selector).getSimpleSelector();
                return addSelector(simple, tagMap, classMap, idMap, otherList, rule, extSelector); // recurse
            }

            /**
             * This selector matches a childhood relationship between two elements.
             * example:
             * <pre class="example">
             *   E > F
             * </pre>
             * @see DescendantSelector
             */    
            case Selector.SAC_CHILD_SELECTOR: {
                SimpleSelector simple = ((CSSChildSelector)selector).getSimpleSelector();
                return addSelector(simple, tagMap, classMap, idMap, otherList, rule, extSelector); // recurse
            }

            /**
             * This selector matches two selectors who shared the same parent in the
             * document tree and the element represented by the first sequence
             * immediately precedes the element represented by the second one.
             * example:
             * <pre class="example">
             *   E + F
             * </pre>
             * @see SiblingSelector
             */
            case Selector.SAC_DIRECT_ADJACENT_SELECTOR: {
                //otherList.add(r);
                //break;
                // XXX Figure out if this works right...
                SimpleSelector simple = ((CSSDirectAdjacentSelector)selector).getSiblingSelector();
                return addSelector(simple, tagMap, classMap, idMap, otherList, rule, extSelector); // recurse
            }
        }
        
        return false;
    }
    
    
    /** Process the full set of rules in this stylesheet and try to set up the rule filter
     * data structures such that the tag map, class map, etc. maps can be used to quickly
     * match elements of given criteria.  This should only be done once for a stylesheet.
     */
    public void setupFilters() {
        if (!CSSEngine.RULE_FILTERING) {
            return;
        }
        
        //long start = System.currentTimeMillis();
        
        int len = getSize();
        tagMap = new HashMap(2*len);
        classMap = new HashMap(2*len);
        idMap = new HashMap(2*len);
        ArrayList otherList = new ArrayList(100);
        
        for (int i = 0; i < len; i++) {
            Rule r = getRule(i);
            switch (r.getType()) {
            case StyleRule.TYPE:
                StyleRule style = (StyleRule)r;                
                style.setPosition(i);                
                SelectorList sl = style.getSelectorList();
                int slen = sl.getLength();
                for (int j = 0; j < slen; j++) {
                    ExtendedSelector s = (ExtendedSelector)sl.item(j);
                    addSelector(s, tagMap, classMap, idMap, otherList, r, s);
                    s.setRule(r);
                }
                break;
                
            case MediaRule.TYPE:
            case ImportRule.TYPE:
                // XXX TODO FIXME    I should just recursively add these rules into the
                // maps too such that I later don't have to worry about it. E.g. the maps
                // should be fully transitive.

                otherList.add(r);
                break;
            }
        }
        
        // Replace the List objects in the map with actual ExtendedSelector[] such that the hot code which
        // -uses- these datastructures can do so quickly and with less casting etc. (Plus
        // this means we'll use less memory since we'll only hold what we need)
        Iterator it = tagMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            List list = (List)entry.getValue();
            ExtendedSelector[] array = (ExtendedSelector[])list.toArray(new ExtendedSelector[list.size()]);
            entry.setValue(array);
        }
        it = classMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            List list = (List)entry.getValue();
            ExtendedSelector[] array = (ExtendedSelector[])list.toArray(new ExtendedSelector[list.size()]);
            entry.setValue(array);
        }
        it = idMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            List list = (List)entry.getValue();
            ExtendedSelector[] array = (ExtendedSelector[])list.toArray(new ExtendedSelector[list.size()]);
            entry.setValue(array);
        }

        // Look for duplicates
        Object prev = null;
        int unique = 0;
        for (int i = 0, n = otherList.size(); i < n; i++) {
            Object next = otherList.get(i);
            if (next != prev) {
                unique++;
                prev = next;
            }
        }
        
        if (unique != otherList.size()) {
            remainingRules = new Rule[unique];
            prev = null;
            int index = 0;
            for (int i = 0, n = otherList.size(); i < n; i++) {
                Object next = otherList.get(i);
                if (next != prev) {
                    remainingRules[index] = (Rule)next;
                    assert index == 0 || !(remainingRules[index] instanceof StyleRule) ||
                            !(remainingRules[index-1] instanceof StyleRule) ||
                            ((StyleRule)remainingRules[index]).getPosition() > ((StyleRule)remainingRules[index-1]).getPosition();
                    prev = next;
                    index++;
                }
            }
            assert index == unique;
        } else {
            remainingRules = (Rule[])otherList.toArray(new Rule[otherList.size()]);
        }

        //if (CSSEngine.DEBUG_FILTERING) {
        //   long end = System.currentTimeMillis();
        //   long delay = end-start;
        //   float seconds = delay/1000.0f;
        //   System.err.println("INITIALIZING STYLESHEET took " + delay + " milliseconds = " + seconds + "s");        
        //    for (int i = 0; i < remainingRules.length; i++) {
        //        System.out.println("otherrule " + i + ": " + remainingRules[i]);
        //    }
        //}
    }    
    
    // END RAVE MODIFICATIONS
// </rave>
}
