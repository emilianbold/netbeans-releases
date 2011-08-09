/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.editor.properties.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.css.editor.properties.parser.PropertyValue.ResolveContext.ResolveType;
import org.netbeans.modules.css.editor.properties.Acceptors;
import org.netbeans.modules.css.editor.properties.CssPropertyValueAcceptor;
import org.netbeans.modules.css.editor.properties.KeywordUtil;

/**
 *
 * @author mfukala@netbeans.org
 */
public class PropertyValue {

    final GroupGrammarElement groupGrammarElement;
    private final String text;
    private final List<ResolvedToken> resolved = new ArrayList<ResolvedToken>();
    private Set<GrammarElement> resolvedAlternatives;
    private Set<GrammarElement> visibleAlternatives = new HashSet<GrammarElement>();
    private final Stack<String> stack = new Stack<String>();
    private final String propertyDefinition;
    Stack<String> originalStack;
    final StringBuffer log = new StringBuffer();
    private static final Pattern FILTER_COMMENTS_PATTERN = Pattern.compile("/\\*.*?\\*/");//NOI18N

    private static String filterComments(String text) {
        Matcher m = FILTER_COMMENTS_PATTERN.matcher(text);
        StringBuilder b = new StringBuilder(text);
        while (m.find()) {
            int from = m.start();
            int to = m.end();
            if (from != to) {
                char[] spaces = new char[to-from];
                Arrays.fill(spaces, ' ');
                String replacement = new String(spaces);
                b.replace(from, to, replacement);
            }
        }
        return b.toString();
    }

    public PropertyValue(PropertyModel property, String textOfTheValue) {
        this.groupGrammarElement = property.values();
        this.text = filterComments(textOfTheValue);
        this.propertyDefinition = null;
        consume();
    }

    /** for unit tests */
    public PropertyValue(String propertyValueDefinition, String textOfTheValue) {
        this.groupGrammarElement = GrammarParser.parse(propertyValueDefinition);
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

    public Set<GrammarElement> alternatives() {
         if(resolvedAlternatives == null) {
            initAlternatives();
        }
         return resolvedAlternatives;
    }
    
    
    /**
     * returns list of alternatives narrowed to the GrammarElements which 
     * may be offered by the completion, e.g. not 'units'
     */
    public Set<GrammarElement> visibleAlternatives() {
        if(resolvedAlternatives == null) {
            initAlternatives();
        }
        return visibleAlternatives;
    }
    
    /**
     * returns list of alternatives narrowed to the GrammarElements which 
     * may be offered by the completion, e.g. not 'units'
     */
    private void computeVisibleAlts() {
        for (GrammarElement e : alternatives()) {
            if (e instanceof ValueGrammarElement) {
                if (((ValueGrammarElement) e).isUnit()) {
                    continue; //skip units
                }
            }
            visibleAlternatives.add(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void consume() {
        fillStack(stack, text);
        originalStack = (Stack<String>) stack.clone();
        resolve(groupGrammarElement, stack, resolved);
    }
    
    private void initAlternatives()  {
        resolvedAlternatives = resolveGrammarElement(groupGrammarElement, new ArrayList<ResolvedToken>(resolved)).alternatives();
        eliminateDuplicatedAlternatives();
        computeVisibleAlts();
    }
    
    private void eliminateDuplicatedAlternatives() {
        //Under some circumstances especially if 0-sg. multiplicity is used in a sequence
        //it might happen that there are more alternative GrammarElements with the same toString() 
        //value which in fact comes from various brances of the parse tree.
        //An example if voice-family property.
        //
        //To eliminate these duplicities it seems to be safe to arbitrary remove the GrammarElements
        //which toString() is equals and keep just one of them.
        
        log("\nEliminated duplicate alternatives:\n");//NOI18N
        HashMap<String, GrammarElement> dupes = new HashMap<String, GrammarElement>();
        for(GrammarElement e : alternatives()) {
            if(dupes.put(e.toString(), e) != null) {
                log(e.path() + "\n");//NOI18N
            }
        }
        log("-----------------\n");//NOI18N
        
        alternatives().retainAll(dupes.values());
        
    }
    
    //------------------------------------------------------------------------
    private ResolveContext resolveGrammarElement(GrammarElement GrammarElement, List<ResolvedToken> resolved) {

        ResolveType resolveType = ResolveType.UNEVALUATED;
        Set<GrammarElement> alternatives = new HashSet<GrammarElement>();

        //repeat the resolvation of the GrammarElement with respect to its multiplicity
        int repeat;
        
        //previous multiplicity loop resolution
        ResolveType previousPassResolveType;
        
        multiplicity: for (repeat = 0; repeat < GrammarElement.getMaximumOccurances(); repeat++) {

            previousPassResolveType = resolveType;
            
            //break the multiplicity loop if no GrammarElement was resolved in last cycle
            if(resolveType == ResolveType.UNRESOLVED) {
                break;
            }
            
            resolveType = ResolveType.UNRESOLVED;
            
            if (GrammarElement instanceof ValueGrammarElement) {
                //test if the GrammarElement is in resolved list and if remove one from there
                if (consume(GrammarElement, resolved)) {
                    //we resolved the GrammarElement and consumed one entry from the resolved tokens list
                    resolveType = ResolveType.PARTIALLY_RESOLVED;
                } else {
                    //no GrammarElement resolved
                    break; //the main multiplicity loop
                }
            } else if (GrammarElement instanceof GroupGrammarElement) {
                //group GrammarElement is resolved, adjust the behavior according to 
                //the group type
                GroupGrammarElement ge = (GroupGrammarElement) GrammarElement;
                if (!ge.isList() && !ge.isSequence()) {
                    //SET GROUP - just one of the GrammarElements can be resolved
                    for (GrammarElement e : ge.elements()) {
                        ResolveContext rt = resolveGrammarElement(e, resolved);
                        
                        //eof of scanning, if a partially resolved sequence is reached, the only
                        //possible alternative is the next sequence GrammarElement, nothing more.
                        if(rt.resolveType() == ResolveType.PARTIALLY_RESOLVED_SEQUENCE) {
                            resolveType = ResolveType.PARTIALLY_RESOLVED_SEQUENCE;
                            alternatives.clear();
                            alternatives.addAll(rt.alternatives());
                            break multiplicity;
                        }
                        
                        if (rt.resolved()) {
                            //the group memeber is resolved, 
                            //just alternatives of the GrammarElement itself are valid
                            resolveType = ResolveType.FULLY_RESOLVED;

                            //clean the alts of previously unresolved GrammarElements
                            alternatives.clear();
                            alternatives.addAll(rt.alternatives());
                            break;
                        } else {
                            //add alternatives of the unresolved GrammarElement to my alts
                            alternatives.addAll(rt.alternatives());
                        }
                    }

                } else if (ge.isList()) {
                    //LIST GROUP - all alternatives may be resolved
                    for (GrammarElement e : ge.elements()) {
                        ResolveContext rt = resolveGrammarElement(e, resolved);
                        alternatives.addAll(rt.alternatives());
                        
                        //eof of scanning, if a partially resolved sequence is reached, the only
                        //possible alternative is the next sequence GrammarElement, nothing more.
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
                    //sequence - alternatives are all GrammarElements after last resolved GrammarElement
                    GrammarElement firstUnresolved = null;
                    
                    HashSet<GrammarElement> localAlts = new HashSet<GrammarElement>();
                    
                    for (GrammarElement e : ge.elements()) {
                        ResolveContext rt = resolveGrammarElement(e, resolved);
                        log("trying " + e.path() + " (MIN=" + e.getMinimumOccurances() + "; MAX=" + e.getMaximumOccurances() + "; resolved=" + rt.resolveType() + "; alts=" + rt.alternatives().size() + ")\n"); //NOI18N
                        if(rt.resolveType() == ResolveType.UNRESOLVED) {
                            localAlts.addAll(rt.alternatives());
                            if(e.getMinimumOccurances() > 0) {
                                //GrammarElement not resolved => sequence also unresolved
                                firstUnresolved = e;
                                break;
                            } else {
                                //ignore unresolved GrammarElements which may not be present (minMult==0)
                            }
                        } else if(rt.resolveType() == ResolveType.FULLY_RESOLVED) {
                            //fully resolved, take next GrammarElement
                            resolveType = ResolveType.PARTIALLY_RESOLVED_SEQUENCE;
                            
                            localAlts.clear();
                        } else if (rt.resolveType() == ResolveType.PARTIALLY_RESOLVED_SEQUENCE) {
                            resolveType = ResolveType.PARTIALLY_RESOLVED_SEQUENCE;
                            
                            localAlts.clear();
                            localAlts.addAll(rt.alternatives());
                            
                            firstUnresolved = e;
                            
                            log("break on " + e.path() + "\n");//NOI18N
                            
                            //break the group GrammarElements loop
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
                    assert true : "Invalid type of GroupGrammarElement " + ge + ". Fix the code!";//NOI18N
                }
            }

        }

        //test if there are really some alternatives
        int remainingPossibleOccurances = GrammarElement.getMaximumOccurances() - repeat;

        if (GrammarElement instanceof ValueGrammarElement) {
            //VALUES
            if (resolveType == ResolveType.UNRESOLVED || (resolveType == ResolveType.PARTIALLY_RESOLVED && remainingPossibleOccurances > 0)) {
                alternatives.add(GrammarElement);
            }
            
            //set fully resolved status if appropriate
            if(resolveType == ResolveType.PARTIALLY_RESOLVED && remainingPossibleOccurances == 0) {
                resolveType = ResolveType.FULLY_RESOLVED;
            }
        }
        
        return ResolveContext.resolveContext(resolveType, alternatives);

    }

    private boolean consume(GrammarElement e, List<ResolvedToken> resolved) {
        for (ResolvedToken rt : resolved) {
            if (rt.getGrammarElement().equals(e)) {
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
        private Set<GrammarElement> alternatives;

        public static ResolveContext resolveContext(ResolveType type, Set<GrammarElement> alternatives) {
            return new ResolveContext(type, alternatives);
        }

        private ResolveContext(ResolveType type, Set<GrammarElement> alternatives) {
            this.type = type;
            this.alternatives = alternatives;
        }

        public boolean resolved() {
            return type == ResolveType.PARTIALLY_RESOLVED || type == ResolveType.FULLY_RESOLVED;
        }

        public ResolveType resolveType() {
            return type;
        }
        
        public Set<GrammarElement> alternatives() {
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

    private boolean resolve(GrammarElement e, Stack<String> input, List<ResolvedToken> consumed) {
        log.append(e.path()).append("\n"); //NOI18N
        boolean itemResolved = false;

        if (input.isEmpty()) {
            //resolved
            return true;
        }

        if (e instanceof GroupGrammarElement) {
            GroupGrammarElement ge = (GroupGrammarElement) e;

            //resolve all group members
            boolean isResolved = false;

            for (int i = 0; i <
                    e.getMaximumOccurances(); i++) {
                //do not enter the same path twice
                Collection<GrammarElement> GrammarElementsToProcess = new ArrayList<GrammarElement>(ge.elements());

                loop:
                do { //try to loop until something gets resolved
                    for (GrammarElement member : GrammarElementsToProcess) {
                        //consume just one token if set or arbitrary number if list
                        isResolved = resolve(member, input, consumed);
                        if (isResolved) {
                            itemResolved = true;
                            if (!ge.isSequence()) {
                                if (!ge.isList()) {
                                    //if we are set break the whole main loop
                                    break loop;
                                } else {
                                    //remember we resolved something under this GrammarElement so we do not enter it again
                                    log.append("sg resolved in ").append(member.path()).append("\n"); //NOI18N
                                    GrammarElementsToProcess.remove(member);
                                    //start resolving the group from the beginning
                                    break;

                                }


                            }
                        } else {
                            //sequence logic
                            if (ge.isSequence() && member.getMinimumOccurances() > 0) {
                                //we didn't resolve the next GrammarElement in row so quit if the GrammarElement was mandatory (min occurences >0)
                                break;
                            }

                        }
                    }
                } while (!input.isEmpty() && isResolved && !GrammarElementsToProcess.isEmpty() && ge.isList()); //of course just for lists

                if (!isResolved || input.isEmpty()) {
                    break;
                }

            }

        } else if (e instanceof ValueGrammarElement) {
            String token = input.peek();
            ValueGrammarElement ve = (ValueGrammarElement) e;

            if (ve.isUnit() && !KeywordUtil.isKeyword(token)) {
                String unitName = ve.value();
                CssPropertyValueAcceptor acceptor = Acceptors.instance().getAcceptor(unitName);
                if (acceptor != null) {
                    if (acceptor.accepts(token)) {
                        //consumed
                        input.pop();
                        consumed.add(new ResolvedToken(token, e));
                        log.append("eaten UNIT '").append(token).append("'\n"); //NOI18N
                        return true;
                    }

                } else {
                    Logger.getAnonymousLogger().log(Level.WARNING, "ERROR - no acceptor for unit property value {0}", ve.value()); //NOI18N
                }

            } else if (token.equalsIgnoreCase(ve.value())) {
                //consumed
                input.pop();
                consumed.add(new ResolvedToken(token, e));
                log.append("eaten '").append(token).append("'\n"); //NOI18N
                return true;
            }

        }

        return itemResolved;
    }

    public class ResolvedToken {

        private String token;
        private GrammarElement GrammarElement;

        private ResolvedToken(String token, GrammarElement GrammarElement) {
            this.token = token;
            this.GrammarElement = GrammarElement;
        }

        public String token() {
            return token;
        }

        public GrammarElement getGrammarElement() {
            return GrammarElement;
        }
    }
}
