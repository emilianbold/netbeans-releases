/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.editor.properties.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.css.editor.properties.Acceptors;
import org.netbeans.modules.css.editor.properties.CssPropertyValueAcceptor;
import org.netbeans.modules.css.editor.properties.KeywordUtil;
import org.netbeans.modules.web.common.api.Pair;
import static org.netbeans.modules.css.editor.properties.parser.GrammarResolver.Log.*;

/**
 *
 * @author marekfukala
 */
public class GrammarResolver {

    //logs types
    public static enum Log {

        DEFAULT,
        VALUES,
        ALTERNATIVES
    }
    static final Map<Log, AtomicBoolean> LOGGERS = new EnumMap<Log, AtomicBoolean>(Log.class);

    static {
        for (Log log : Log.values()) {
            LOGGERS.put(log, new AtomicBoolean(false));
        }
    }

    public static void setLogging(Log log, boolean enable) {
        LOGGERS.get(log).set(enable);
    }

    private static boolean isLoggingEnabled(Log log) {
        return LOGGERS.get(log).get();
    }
    private static final Logger LOGGER = Logger.getLogger(GrammarResolver.class.getName());

    public static GrammarResolver resolve(GroupGrammarElement grammar, String input) {
        return new GrammarResolver(grammar, input).initialize();
    }
    private List<ResolvedToken> resolvedTokens = new ArrayList<ResolvedToken>();
    private Stack<String> tokensLeft = new Stack<String>();
    private Stack<String> tokens = new Stack<String>();
    private GroupGrammarElement grammar;
    private String input;
    private boolean inputResolved;

    public GrammarResolver(GroupGrammarElement grammar, String input) {
        this.grammar = grammar;
        this.input = input;
    }

    public List<String> tokens() {
        return tokens;
    }

    /** returns a list of value items not parsed */
    public List<String> left() {
        return tokensLeft;
    }

    public List<ResolvedToken> resolved() {
        return resolvedTokens;
    }

    public boolean success() {
        return inputResolved;
    }

    private GrammarResolver initialize() {
        tokens = Tokenizer.tokenize(input);
        tokensLeft = (Stack<String>) tokens.clone();

        groupMemberResolved(grammar, grammar, createInputState(), true);

        inputResolved = resolve(grammar);

        if (!tokensLeft.isEmpty()) {
            //the main element resolved, but something left in the input -- fail
            inputResolved = false;
        }

        resolvingFinished();

        return this;
    }

    private void resolvingFinished() {
        if (isLoggingEnabled(DEFAULT)) {
            log("\nResolved tokens:");
            for (ResolvedToken rt : resolvedTokens) {
                log(rt.toString());
            }
        }

        if (isLoggingEnabled(ALTERNATIVES)) {
            log(ALTERNATIVES, "\nAlternatives:");
            for (ValueGrammarElement e : getAlternatives()) {
                log(ALTERNATIVES, e.path());
            }
        }
    }

    private InputState createInputState() {
        return new InputState(tokensLeft, resolvedTokens);
    }

    private boolean equalsToCurrentState(InputState state) {
        return tokensLeft.equals(state.input) && resolvedTokens.equals(state.consumed);
    }

    private void backupInputState(InputState state) {
        if (equalsToCurrentState(state)) {
            //no need to backup the same state
            return;
        }

        tokensLeft = (Stack<String>) state.input.clone();
        resolvedTokens = new ArrayList<ResolvedToken>(state.consumed);

        log(String.format("  state backup to: %s", state));
    }

    private boolean resolve(GrammarElement e) {
        log(String.format("+ entering %s, %s", e.path(), createInputState()));
        boolean resolves;
        switch (e.getKind()) {
            case GROUP:
                resolves = processGroup((GroupGrammarElement) e);
                break;
            case VALUE:
                resolves = processValue((ValueGrammarElement) e);
                break;
            default:
                throw new IllegalStateException();
        }
        log(String.format("- leaving %s, resolved: %s, %s", e.path(), resolves, createInputState()));
        return resolves;

    }
    
    //alternatives computation >>>
    //
    //keys are elements which matched the input, values are pairs of InputState 
    //in the time of the match and collection of possible values which may follow
    //the matched element
    private Map<GrammarElement, Pair<InputState, Collection<ValueGrammarElement>>> resolvedSomething = new LinkedHashMap<GrammarElement, Pair<InputState, Collection<ValueGrammarElement>>>();
    private GrammarElement lastResolved;

    private void valueNotAccepted(ValueGrammarElement valueGrammarElement) {
        if (resolvedTokens.size() < tokens.size()) {
            //ignore such alternatives, we need to find alts after all input tokens are resolved
            return;
        }

        log(ALTERNATIVES, String.format("value not accepted %s, %s", valueGrammarElement.path(), createInputState()));

        Pair<InputState, Collection<ValueGrammarElement>> pair = resolvedSomething.get(lastResolved);
        pair.getB().add(valueGrammarElement);
    }

    private void groupMemberResolved(GrammarElement member, GroupGrammarElement group, InputState state, boolean root) {
        if (!root && (state.consumed.size() < tokens.size())) {
            //ignore such alternatives, we need to find alts after all input tokens are resolved
            return;
        }

        log(ALTERNATIVES, String.format("input matched %s, %s", member.path(), state));
        resolvedSomething.put(group, new Pair<InputState, Collection<ValueGrammarElement>>(state, new LinkedList<ValueGrammarElement>()));
        lastResolved = group;
    }

    public Set<ValueGrammarElement> getAlternatives() {
        HashSet<ValueGrammarElement> alternatives = new HashSet<ValueGrammarElement>();
        for (Pair<InputState, Collection<ValueGrammarElement>> tri : resolvedSomething.values()) {
            for (ValueGrammarElement value : tri.getB()) {
                alternatives.add(value);
            }
        }
        return alternatives;
    }

    private boolean processGroup(GroupGrammarElement group) {
        //resolve all group members
        InputState successState = null;
        InputState enteringGroupState = createInputState();
        int inputCount = tokensLeft.size();
        multiplicity:
        for (int i = 0; i < group.getMaximumOccurances(); i++) {
            Collection<GrammarElement> grammarElementsToProcess = new ArrayList<GrammarElement>(group.elements());

            Map<GrammarElement, InputState> branchesResults =
                    new HashMap<GrammarElement, InputState>();

            collection_loop:
            for (;;) { //try to loop until the LIST group is resolved fully (or not at all)
                Collection<GrammarElement> failedListMembers = new HashSet<GrammarElement>();
                members:
                for (Iterator<GrammarElement> membersIterator = grammarElementsToProcess.iterator(); membersIterator.hasNext();) {
                    GrammarElement member = membersIterator.next();
                    boolean resolved = resolve(member);

                    if (!resolved) {
                        if (member instanceof ValueGrammarElement) {
                            valueNotAccepted((ValueGrammarElement) member);
                        }
                    }

                    if (resolved) {
                        InputState state = createInputState();

                        groupMemberResolved(member, group, state, false);

                        //member resolved some input
                        switch (group.getType()) {
                            case SET:
                                log(String.format("  added SET branch result: %s, %s", member.path(), state));
                                branchesResults.put(member, state);
                                successState = state;

                                backupInputState(enteringGroupState);
                                break;
                            case COLLECTION: //any of the member in any order
                                successState = state;
                                //remember we resolved something under this GrammarElement so we do not enter it again
                                grammarElementsToProcess.remove(member);
                                //start resolving the group from the beginning
                                continue collection_loop;
                            case LIST:
                                if (!membersIterator.hasNext()) {
                                    //the resolved element was the last one from the LIST so the group si resolved
                                    successState = state;
                                    break collection_loop;
                                }
                                break;
                        }

                    } else if (member.isOptional()) {
                        //the member hasn't resolved any input but it is optional
                        InputState state = createInputState();

                        log(String.format("  arbitrary member %s skipped", member.path()));

                        switch (group.getType()) {
                            case SET:
                                log(String.format(" added SET branch result: %s, %s", member, state));
                                branchesResults.put(member, state);
                                successState = state;
                                backupInputState(enteringGroupState);
                                break;
                            case LIST:
                                if (!membersIterator.hasNext()) {
                                    //the resolved element was the last one from the LIST so the group si resolved
                                    successState = state;
                                    break collection_loop;
                                }
                                break;
                        }

                    } else {
                        //member doesn't resolve
                        switch (group.getType()) {
                            case LIST:
                                //failure, cannot resolve the member and it is mandatory
                                //so we are sure the group cannot be resolved
                                break multiplicity;

                            case SET:
                            case COLLECTION:
                                //the failure of resolving this member doesn't
                                //necessarily mean the group element cannot be resolved ... continue resolving
                                failedListMembers.add(member);
                                break;
                        }
                    }
                } //members

                if (tokensLeft.size() == inputCount) {
                    //nothing from the input was resolved, stop resolving
                    break;
                } else {
                    inputCount = tokensLeft.size();
                }

                //loop if the grammar element is a collection otherwise leave
                switch (group.getType()) {
                    case COLLECTION:
                        if (grammarElementsToProcess.isEmpty()) {
                            break collection_loop;
                        }
                        if (failedListMembers.size() == grammarElementsToProcess.size()) {
                            break collection_loop;
                        }
                        break;
                    case SET:
                    case LIST:
                        break collection_loop;
                }

            } //:collection_loop

            switch (group.getType()) {
                case SET:
                    //process branches results - find longest match
                    //take first alternative if equals
                    GrammarElement bestMatchElement = null;
                    int bestMatchConsumed = 0;
                    for (GrammarElement member : group.elements()) {
                        InputState state = branchesResults.get(member);
                        if (state == null) {
                            //this branch matched nothing
                            continue;
                        }

                        if (bestMatchElement == null) {
                            bestMatchElement = member;
                            bestMatchConsumed = state.consumed.size();
                        } else {
                            if (bestMatchConsumed < state.consumed.size()) {
                                bestMatchElement = member;
                                bestMatchConsumed = state.consumed.size();
                            }
                        }
                    }
                    //set the success state to the best branch (consumed most input)
                    if (bestMatchElement != null) {
                        successState = branchesResults.get(bestMatchElement);
                        //put the state of the best match back
                        backupInputState(successState);
                        log(String.format("Decided to use best match %s, %s", bestMatchElement, successState));
                    }
                    break;
            }

            //we went through the first iteration of members (multiplicity 0 and more)
            //but nothing resolved so making another multiplicity loop makes no sense
            if (successState == null) {
                break multiplicity;
            }

            //nothing from the input was resolved during the multiplicity loop,
            //so stop resolving
            if (tokensLeft.size() == inputCount) {
                break;
            } else {
                inputCount = tokensLeft.size();
            }

        } //multiplicity loop


        if (successState == null) {
            //nothing from the group resolved, backup the input before leaving
            backupInputState(enteringGroupState);
            return false;
        } else {
            //the group is resolved, backup the last successful state
            //the backup must be here since the multiplicity loop may fail, but
            //still the group is resolved
            backupInputState(successState);
            return true;
        }

    }

    private boolean processValue(ValueGrammarElement ve) {
        if (tokensLeft.isEmpty()) {
            return false;
        }

        String token = tokensLeft.peek();

        if (ve.isUnit() && !KeywordUtil.isKeyword(token)) {
            String unitName = ve.value();
            CssPropertyValueAcceptor acceptor = Acceptors.instance().getAcceptor(unitName);
            if (acceptor != null) {
                if (acceptor.accepts(token)) {
                    //consumed
                    consumeValueGrammarElement(token, ve);
                    log(VALUES, String.format("eaten unit %s", token));
                    return true;
                }

            } else {
                LOGGER.log(Level.WARNING, String.format("Cannot find unit acceptor '%s'!", ve.value())); //NOI18N
            }

        } else if (token.equalsIgnoreCase(ve.value())) {
            //consumed
            consumeValueGrammarElement(token, ve);
            log(VALUES, String.format("eaten value %s", token));
            return true;
        }

        return false;

    }

    private void consumeValueGrammarElement(String tokenImage, ValueGrammarElement element) {
        tokensLeft.pop();
        resolvedTokens.add(new ResolvedToken(tokenImage, element));
    }

    private void log(String text) {
        log(DEFAULT, text);
    }

    private void log(Log log, String text) {
        if (isLoggingEnabled(log)) {
            System.out.println(text);
        }
    }

    private static class InputState {

        private final Stack<String> input;
        private List<ResolvedToken> consumed;

        public InputState(Stack<String> input, List<ResolvedToken> consumed) {
            this.input = (Stack<String>) input.clone();
            this.consumed = new ArrayList<ResolvedToken>(consumed);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            //resolved part
            sb.append('[');
            for (Iterator<ResolvedToken> i = consumed.iterator(); i.hasNext();) {
                ResolvedToken rt = i.next();
                sb.append(rt.token());
                if (i.hasNext()) {
                    sb.append(' ');
                }
            }
            sb.append(']');
            sb.append(' ');

            //unresolved part
            for (int i = input.size() - 1; i >= 0; i--) {
                sb.append(input.get(i));
                if (i > 0) {
                    sb.append(' ');
                }
            }
            return sb.toString();
        }
    }
}
