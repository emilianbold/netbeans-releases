/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Logger;
import org.netbeans.modules.css.editor.CssPropertyValue.ResolveContext.ResolveType;
import org.netbeans.modules.css.editor.PropertyModel.Element;
import org.netbeans.modules.css.editor.PropertyModel.GroupElement;
import org.netbeans.modules.css.editor.PropertyModel.ValueElement;
import org.netbeans.modules.css.editor.properties.Acceptors;
import org.netbeans.modules.css.editor.properties.CssPropertyValueAcceptor;
import org.netbeans.modules.css.editor.properties.KeywordUtil;

/**
 *
 * @author marek.fukala@sun.com
 */
public class CssPropertyValue {

    final GroupElement groupElement;
    private final String text;
    private final List<ResolvedToken> resolved = new ArrayList<ResolvedToken>();
    private Set<Element> resolvedAlternatives;
    private Set<Element> visibleAlternatives = new HashSet<Element>();
    private final Stack<String> stack = new Stack<String>();
    private final String propertyDefinition;
    Stack<String> originalStack;
    final StringBuffer log = new StringBuffer();

    public CssPropertyValue(Property property, String textOfTheValue) {
        this.groupElement = property.values();
        this.text = textOfTheValue;
        this.propertyDefinition = null;
        consume();
    }

    /** for unit tests */
    CssPropertyValue(String propertyValueDefinition, String textOfTheValue) {
        this.groupElement = PropertyModel.instance().parse(propertyValueDefinition);
        this.text = textOfTheValue;
        this.propertyDefinition = propertyValueDefinition;
        consume();
    }

    private void log(String msg) {
        log.append(msg);
    }
    
    String log() {
        return log.toString();
    }

    String inputText() {
        return text;
    }

    String propertyDefinition() {
        return propertyDefinition;
    }

    /** returns a list of value items not parsed */
    public List<String> left() {
        return stack;
    }

    public List<ResolvedToken> resolved() {
        return resolved;
    }

    public boolean success() {
        return stack.isEmpty();
    }

    public Set<Element> alternatives() {
         if(resolvedAlternatives == null) {
            initAlternatives();
        }
         return resolvedAlternatives;
    }
    
    
    /**
     * returns list of alternatives narrowed to the elements which 
     * may be offered by the completion, e.g. not 'units'
     */
    public Set<Element> visibleAlternatives() {
        if(resolvedAlternatives == null) {
            initAlternatives();
        }
        return visibleAlternatives;
    }
    
    /**
     * returns list of alternatives narrowed to the elements which 
     * may be offered by the completion, e.g. not 'units'
     */
    private void computeVisibleAlts() {
        for (Element e : alternatives()) {
            if (e instanceof PropertyModel.ValueElement) {
                if (((PropertyModel.ValueElement) e).isUnit()) {
                    continue; //skip units
                }
            }
            visibleAlternatives.add(e);
        }
    }

    private void consume() {
        fillStack(stack, text);
        originalStack = (Stack<String>) stack.clone();
        resolve(groupElement, stack, resolved);
    }
    
    private void initAlternatives()  {
        resolvedAlternatives = resolveElement(groupElement, new ArrayList<ResolvedToken>(resolved)).alternatives();
        eliminateDuplicatedAlternatives();
        computeVisibleAlts();
    }
    
    private void eliminateDuplicatedAlternatives() {
        //Under some circumstances especially if 0-sg. multiplicity is used in a sequence
        //it might happen that there are more alternative elements with the same toString() 
        //value which in fact comes from various brances of the parse tree.
        //An example if voice-family property.
        //
        //To eliminate these duplicities it seems to be safe to arbitrary remove the elements
        //which toString() is equals and keep just one of them.
        
        log("\nEliminated duplicate alternatives:\n");//NOI18N
        HashMap<String, Element> dupes = new HashMap<String, Element>();
        for(Element e : alternatives()) {
            if(dupes.put(e.toString(), e) != null) {
                log(e.path() + "\n");//NOI18N
            }
        }
        log("-----------------\n");//NOI18N
        
        alternatives().retainAll(dupes.values());
        
    }
    
    //------------------------------------------------------------------------
    private ResolveContext resolveElement(Element element, List<ResolvedToken> resolved) {

        ResolveType resolveType = ResolveType.UNEVALUATED;
        Set<Element> alternatives = new HashSet<Element>();

        //repeat the resolvation of the element with respect to its multiplicity
        int repeat;
        
        //previous multiplicity loop resolution
        ResolveType previousPassResolveType;
        
        multiplicity: for (repeat = 0; repeat < element.getMaximumOccurances(); repeat++) {

            previousPassResolveType = resolveType;
            
            //break the multiplicity loop if no element was resolved in last cycle
            if(resolveType == ResolveType.UNRESOLVED) {
                break;
            }
            
            resolveType = ResolveType.UNRESOLVED;
            
            if (element instanceof ValueElement) {
                //test if the element is in resolved list and if remove one from there
                if (consume(element, resolved)) {
                    //we resolved the element and consumed one entry from the resolved tokens list
                    resolveType = ResolveType.PARTIALLY_RESOLVED;
                } else {
                    //no element resolved
                    break; //the main multiplicity loop
                }
            } else if (element instanceof GroupElement) {
                //group element is resolved, adjust the behavior according to 
                //the group type
                GroupElement ge = (GroupElement) element;
                if (!ge.isList() && !ge.isSequence()) {
                    //SET GROUP - just one of the elements can be resolved
                    for (Element e : ge.elements()) {
                        ResolveContext rt = resolveElement(e, resolved);
                        
                        //eof of scanning, if a partially resolved sequence is reached, the only
                        //possible alternative is the next sequence element, nothing more.
                        if(rt.resolveType() == ResolveType.PARTIALLY_RESOLVED_SEQUENCE) {
                            resolveType = ResolveType.PARTIALLY_RESOLVED_SEQUENCE;
                            alternatives.clear();
                            alternatives.addAll(rt.alternatives());
                            break multiplicity;
                        }
                        
                        if (rt.resolved()) {
                            //the group memeber is resolved, 
                            //just alternatives of the element itself are valid
                            resolveType = ResolveType.FULLY_RESOLVED;

                            //clean the alts of previously unresolved elements
                            alternatives.clear();
                            alternatives.addAll(rt.alternatives());
                            break;
                        } else {
                            //add alternatives of the unresolved element to my alts
                            alternatives.addAll(rt.alternatives());
                        }
                    }

                } else if (ge.isList()) {
                    //LIST GROUP - all alternatives may be resolved
                    for (Element e : ge.elements()) {
                        ResolveContext rt = resolveElement(e, resolved);
                        alternatives.addAll(rt.alternatives());
                        
                        //eof of scanning, if a partially resolved sequence is reached, the only
                        //possible alternative is the next sequence element, nothing more.
                        if(rt.resolveType() == ResolveType.PARTIALLY_RESOLVED_SEQUENCE) {
                            resolveType = ResolveType.PARTIALLY_RESOLVED_SEQUENCE;
                            alternatives.clear();
                            alternatives.addAll(rt.alternatives());
                            break multiplicity;
                        }
                        
                        
                        if(rt.resolved()) {
                            resolveType = ResolveType.FULLY_RESOLVED;
                        }
                    }
                    
                } else if (ge.isSequence()) {
                    log("<S> " + ge.path() + "\n");//NOI18N
                    //sequence - alternatives are all elements after last resolved element
                    Element firstUnresolved = null;
                    
                    HashSet<Element> localAlts = new HashSet<Element>();
                    
                    for (Element e : ge.elements()) {
                        ResolveContext rt = resolveElement(e, resolved);
                        log("trying " + e.path() + " (MIN=" + e.getMinimumOccurances() + "; MAX=" + e.getMaximumOccurances() + "; resolved=" + rt.resolveType() + "; alts=" + rt.alternatives().size() + ")\n"); //NOI18N
                        if(rt.resolveType() == ResolveType.UNRESOLVED) {
                            localAlts.addAll(rt.alternatives());
                            if(e.getMinimumOccurances() > 0) {
                                //element not resolved => sequence also unresolved
                                firstUnresolved = e;
                                break;
                            } else {
                                //ignore unresolved elements which may not be present (minMult==0)
                            }
                        } else if(rt.resolveType() == ResolveType.FULLY_RESOLVED) {
                            //fully resolved, take next element
                            resolveType = ResolveType.PARTIALLY_RESOLVED_SEQUENCE;
                            
                            localAlts.clear();
                        } else if (rt.resolveType() == ResolveType.PARTIALLY_RESOLVED_SEQUENCE) {
                            resolveType = ResolveType.PARTIALLY_RESOLVED_SEQUENCE;
                            
                            localAlts.clear();
                            localAlts.addAll(rt.alternatives());
                            
                            firstUnresolved = e;
                            
                            log("break on " + e.path() + "\n");//NOI18N
                            
                            //break the group elements loop
                            break;
                        }
                    }
                    
                    alternatives.addAll(localAlts);
                    
                    if(firstUnresolved == null) {
                        //the whole sequence has been resolved
                        resolveType = ResolveType.FULLY_RESOLVED;
                    } else if(previousPassResolveType == ResolveType.FULLY_RESOLVED) {
                        //the seqence was not fully resolved
                        
                        //if the previous multiplicity cycle resolved something
                        //we need to return PARTIALLY_RESOLVED type even if nothing
                        //was resolved in this last cycle
                        
                        resolveType = ResolveType.PARTIALLY_RESOLVED_SEQUENCE;
                    }
                    
                    log("sequence " + resolveType + "\n");//NOI18N
                    
                    //break the big multiplicity loop since if sequence is just 
                    //partially resolved next loop cannot be run
                    if(resolveType == ResolveType.PARTIALLY_RESOLVED_SEQUENCE) {
                        break multiplicity;
                    }
                    
                } else {
                    assert true : "Invalid type of GroupElement " + ge + ". Fix the code!";//NOI18N
                }
            }

        }

        //test if there are really some alternatives
        int remainingPossibleOccurances = element.getMaximumOccurances() - repeat;

        if (element instanceof ValueElement) {
            //VALUES
            if (resolveType == ResolveType.UNRESOLVED || (resolveType == ResolveType.PARTIALLY_RESOLVED && remainingPossibleOccurances > 0)) {
                alternatives.add(element);
            }
            
            //set fully resolved status if appropriate
            if(resolveType == ResolveType.PARTIALLY_RESOLVED && remainingPossibleOccurances == 0) {
                resolveType = ResolveType.FULLY_RESOLVED;
            }
        }
        
        return ResolveContext.resolveContext(resolveType, alternatives);

    }

    private boolean consume(Element e, List<ResolvedToken> resolved) {
        for (ResolvedToken rt : resolved) {
            if (rt.element().equals(e)) {
                resolved.remove(rt);
                return true;
            }

        }
        return false;
    }

    static class ResolveContext {

        public enum ResolveType {
            UNEVALUATED,
            UNRESOLVED,
            PARTIALLY_RESOLVED,
            FULLY_RESOLVED,
            PARTIALLY_RESOLVED_SEQUENCE;
        }
        
        private ResolveType type;
        private Set<Element> alternatives;

        public static ResolveContext resolveContext(ResolveType type, Set<Element> alternatives) {
            return new ResolveContext(type, alternatives);
        }

        private ResolveContext(ResolveType type, Set<Element> alternatives) {
            this.type = type;
            this.alternatives = alternatives;
        }

        public boolean resolved() {
            return type == ResolveType.PARTIALLY_RESOLVED || type == ResolveType.FULLY_RESOLVED;
        }

        public ResolveType resolveType() {
            return type;
        }
        
        public Set<Element> alternatives() {
            return alternatives;
        }
    }

    static void fillStack(Stack<String> stack, String input) {
        //this semi-lexer started as three lines code and evolved to this
        //ugly beast. Should be recoded to normal state lexing.
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i <
                input.length(); i++) {
            char c = input.charAt(i);

            if (c == '\'' || c == '"') {
                //quoted values needs to be one token
                sb.append(c); //add the quotation mark into the value
                for (i++; i <
                        input.length(); i++) {
                    c = input.charAt(i);

                    if (c == '\'' || c == '"') {
                        break;
                    } else {
                        sb.append(c);
                    }

                }
                sb.append(c); //add the quotation mark into the value
                stack.add(0, sb.toString());
                sb =
                        new StringBuffer();

            } else if (sb.toString().equalsIgnoreCase("url") && c == '(') { //NOI18N 
                //store separate tokens: URL + ( + ..... + )
                stack.add(0, sb.toString());
                stack.add(0, "" + c); //NOI18N

                sb = new StringBuffer();
                //make one token until ) found
                for (i++ ; i <
                        input.length(); i++) {
                    c = input.charAt(i);

                    if (c == ')') {
                        break;
                    } else {
                        sb.append(c);
                    }

                }
                
                stack.add(0, sb.toString());
                stack.add(0, "" + c); //add the quotation mark into the value  //NOI18N
                sb = new StringBuffer();        
                
            } else if (c == ' ' || c == '\t' || c == '\n') {
                if (sb.length() > 0) {
                    stack.add(0, sb.toString());
                    sb =
                            new StringBuffer();
                }
//skip other potential whitespaces
                for (; i <
                        input.length(); i++) {
                    c = input.charAt(i);
                    if (c != ' ' || c != '\t') {
                        break;
                    }

                }

            } else {
                //handling of chars which are both delimiters and values
                if (c == ',' || c == '/' || c == '(' || c == ')') {
                    if (sb.length() > 0) {
                        stack.add(0, sb.toString());
                    }

                    stack.add(0, "" + c); //NOI18N

                    sb =
                            new StringBuffer();
                } else {
                    sb.append(c);
                }

            }
        }

        //value before eof
        if (sb.length() > 0) {
            stack.add(0, sb.toString());
        }

    }

    private boolean resolve(Element e, Stack<String> input, List<ResolvedToken> consumed) {
        log.append(e.path() + "\n"); //NOI18N
        boolean itemResolved = false;

        if (input.isEmpty()) {
            //resolved
            return true;
        }

        if (e instanceof GroupElement) {
            GroupElement ge = (GroupElement) e;

            //resolve all group members
            boolean resolved = false;

            for (int i = 0; i <
                    e.getMaximumOccurances(); i++) {
                //do not enter the same path twice
                Collection<Element> elementsToProcess = new ArrayList<Element>(ge.elements());

                loop:
                do { //try to loop until something gets resolved
                    for (Element member : elementsToProcess) {
                        //consume just one token if set or arbitrary number if list
                        resolved = resolve(member, input, consumed);
                        if (resolved) {
                            itemResolved = true;
                            if (!ge.isSequence()) {
                                if (!ge.isList()) {
                                    //if we are set break the whole main loop
                                    break loop;
                                } else {
                                    //remember we resolved something under this element so we do not enter it again
                                    log.append("sg resolved in " + member.path() + "\n"); //NOI18N
                                    elementsToProcess.remove(member);
                                    //start resolving the group from the beginning
                                    break;

                                }


                            }
                        } else {
                            //sequence logic
                            if (ge.isSequence() && member.getMinimumOccurances() > 0) {
                                //we didn't resolve the next element in row so quit if the element was mandatory (min occurences >0)
                                break;
                            }

                        }
                    }
                } while (!input.isEmpty() && resolved && !elementsToProcess.isEmpty() && ge.isList()); //of course just for lists

                if (!resolved || input.isEmpty()) {
                    break;
                }

            }

        } else if (e instanceof ValueElement) {
            String token = input.peek();
            ValueElement ve = (ValueElement) e;

            if (ve.isUnit() && !KeywordUtil.isKeyword(token)) {
                String unitName = ve.value();
                CssPropertyValueAcceptor acceptor = Acceptors.instance().getAcceptor(unitName);
                if (acceptor != null) {
                    if (acceptor.accepts(token)) {
                        //consumed
                        input.pop();
                        consumed.add(new ResolvedToken(token, e));
                        log.append("eaten UNIT '" + token + "'\n"); //NOI18N
                        return true;
                    }

                } else {
                    Logger.global.warning("ERROR - no acceptor for unit property value " + ve.value()); //NOI18N
                }

            } else if (token.equalsIgnoreCase(ve.value())) {
                //consumed
                input.pop();
                consumed.add(new ResolvedToken(token, e));
                log.append("eaten '" + token + "'\n"); //NOI18N
                return true;
            }

        }

        return itemResolved;
    }

    public class ResolvedToken {

        private String token;
        private Element element;

        private ResolvedToken(String token, Element element) {
            this.token = token;
            this.element = element;
        }

        public String token() {
            return token;
        }

        public Element element() {
            return element;
        }
    }
}
