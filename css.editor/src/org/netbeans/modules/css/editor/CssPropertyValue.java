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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import org.netbeans.modules.css.editor.PropertyModel.Element;
import org.netbeans.modules.css.editor.PropertyModel.GroupElement;
import org.netbeans.modules.css.editor.PropertyModel.ValueElement;
import org.netbeans.modules.css.editor.properties.Acceptors;
import org.netbeans.modules.css.editor.properties.CssPropertyValueAcceptor;

/**
 *
 * @author marek.fukala@sun.com
 */
public class CssPropertyValue {

    final GroupElement groupElement;
    private final String text;
    private final List<ResolvedToken> resolved = new ArrayList<ResolvedToken>();
    private final Set<Element> alternatives = new HashSet<Element>(20);
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
        return alternatives;
    }

    private void consume() {
        fillStack(stack, text);
        
        originalStack = (Stack<String>)stack.clone();
        
        resolve(groupElement, stack, resolved);
        searchAlternatives(resolved);
    }

    private Set<Element> searchAlternatives(List<ResolvedToken> consumed) {
        if (consumed.isEmpty()) {
            //nothing resolved - may offer everything
            alternatives.addAll(groupElement.getAllPossibleValues());
        } else {
            Collection<GroupElement> lists = new HashSet<GroupElement>();
            Collection<Element> remove = new HashSet<Element>();
            Collection<Element> keep = new HashSet<Element>();

            //something was resolved - respect the parse tree
            for (ResolvedToken rt : consumed) {
                Element e = rt.element();
                GroupElement p;
                while ((p = e.parent()) != null) {
                    if (p.isList()) {
                        lists.add(p);
                        remove.add(e);
                    } else if(p.isSequence()) {
                        remove.add(e);
                        int eIndex = p.elements().indexOf(e);
                        if(eIndex < p.elements().size() - 1) {
                            //not last element
                            //keep its successor
                            keep.add(p.elements().get(eIndex + 1));
                        }
                    }
                    
                    e = p;
                }
            }

            //remove explicitly resolved elements
            keep.removeAll(remove);
            for (Element e : keep) {
                if (e instanceof GroupElement) {
                    alternatives.addAll(((GroupElement) e).getAllPossibleValues());
                } else {
                    alternatives.add(e);
                }
            }
            
            for (GroupElement e : lists) {
                Collection<Element> children = new HashSet<Element>(e.elements());
                children.removeAll(remove);

                for (Element child : children) {
                    if (!e.equals(child)) {
                        if (child instanceof GroupElement) {
                            alternatives.addAll(((GroupElement) child).getAllPossibleValues());
                        } else {
                            alternatives.add(child);
                        }
                    }
                }

            }

        }

        return alternatives;
    }

    static void fillStack(Stack<String> stack, String input) {
        //this semi-lexer started as three lines code and evolved to this
        //ugly beast. Should be recoded to normal state lexing.
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            
            if(c == '\'' || c == '"') {
                //quoted values needs to be one token
                sb.append(c); //add the quotation mark into the value
                for(i++; i < input.length(); i++) {
                    c = input.charAt(i);
                    
                    if(c == '\'' || c == '"') {
                        break;
                    } else {
                        sb.append(c);
                    }
                }
                sb.append(c); //add the quotation mark into the value
                stack.add(0, sb.toString());
                sb = new StringBuffer();
            
            } else if(c == '(') {
                //make one token until ) found
                for(; i < input.length(); i++) {
                    c = input.charAt(i);
                    
                    if(c == ')') {
                        break;
                    } else {
                        sb.append(c);
                    }
                }
                sb.append(c); //add the quotation mark into the value
            } else if(c == ' ' || c == '\t') {
                if(sb.length() > 0) {
                    stack.add(0, sb.toString());
                    sb = new StringBuffer();
                }
                //skip other potential whitespaces
                for(; i < input.length(); i++) {
                    c = input.charAt(i);
                    if(c != ' ' || c != '\t') {
                        break;
                    }
                }
                
            } else {
                //handling of chars which are both delimiters and values
                if(c == ',' || c == '/') {
                    if(sb.length() > 0) {
                        stack.add(0, sb.toString());
                    }
                    stack.add(0, ""+c);
                    
                    sb = new StringBuffer();
                } else {
                    sb.append(c);
                }
            }
        }
        
        //value before eof
        if(sb.length() > 0) {
            stack.add(0, sb.toString());
        }
        
    }

    private boolean resolve(Element e, Stack<String> input, List<ResolvedToken> consumed) {
        log.append(e.path() + "\n");
        boolean itemResolved = false;

        if (input.isEmpty()) {
            //resolved
            return true;
        }

        if (e instanceof GroupElement) {
            GroupElement ge = (GroupElement) e;

            //resolve all group members
            boolean resolved = false;

            for (int i = 0; i < e.getMaximumOccurances(); i++) {
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
                                    log.append("sg resolved in " + member.path() + "\n");
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

            if (ve.isUnit()) {
                String unitName = ve.value();
                CssPropertyValueAcceptor acceptor = Acceptors.instance().getAcceptor(unitName);
                if (acceptor != null) {
                    if (acceptor.accepts(token)) {
                        //consumed
                        input.pop();
                        consumed.add(new ResolvedToken(token, e));
                        log.append("eaten UNIT '" + token + "'\n");
                        return true;
                    }
                } else {
                    System.out.println("ERROR - no acceptor for unit property value " + ve.value());
                }

            } else if (token.equalsIgnoreCase(ve.value())) {
                //consumed
                input.pop();
                consumed.add(new ResolvedToken(token, e));
                log.append("eaten '" + token + "'\n");
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
