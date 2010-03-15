/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009-2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.jackpot.impl.pm;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.TreeScanner;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.jackpot.impl.Utilities;
import org.netbeans.modules.java.hints.jackpot.impl.pm.NFA.Key;
import org.netbeans.modules.java.hints.jackpot.impl.pm.NFA.State;
import org.openide.util.Exceptions;

/**
 *
 * @author lahvac
 */
public class NFABasedBulkSearch extends BulkSearch {

    public NFABasedBulkSearch() {
        super(false);
    }

    @Override
    public Map<String, Collection<TreePath>> match(CompilationInfo info, TreePath tree, BulkPattern patternIn, Map<String, Long> timeLog) {
        BulkPatternImpl pattern = (BulkPatternImpl) patternIn;
        
        final Map<String, Collection<TreePath>> occurringPatterns = new HashMap<String, Collection<TreePath>>();
        final NFA<Input, Res> nfa = pattern.toNFA();

        new TreePathScanner<Void, Void>() {
            private State active = nfa.getStartingState();
            @Override
            public Void scan(Tree node, Void p) {
                if (node == null) {
                    return null;
                }

                boolean[] goDeeper = new boolean[1];
                final State newActiveAfterVariable = nfa.transition(active, new Input(Kind.IDENTIFIER, "$", false));
                Input[] bypass = new Input[1];
                Input normalizedInput = normalizeInput(node, goDeeper, bypass);
                State bypassed = bypass[0] != null ? nfa.transition(active, bypass[0]) : null;
                active = nfa.transition(active, normalizedInput);

                if (goDeeper[0]) {
                    super.scan(node, p);
                }

                State s1 = nfa.transition(active, new Input(normalizedInput.kind, normalizedInput.name, true));
                State s2 = nfa.transition(newActiveAfterVariable, new Input(Kind.IDENTIFIER, "$", true));

                if (bypassed != null) {
                    //XXX: performance, might be better to have join(State, State, State):
                    State bypassed2 = nfa.transition(bypassed, new Input(bypass[0].kind, bypass[0].name, true));
                    
                    active = nfa.join(nfa.join(s1, s2), bypassed2);
                } else {
                    active = nfa.join(s1, s2);
                }

                for (Res r : nfa.getResults(active)) {
                    addOccurrence(r, node);
                }

                return null;
            }

            private void addOccurrence(Res r, Tree node) {
                Collection<TreePath> occurrences = occurringPatterns.get(r.pattern);
                if (occurrences == null) {
                    occurringPatterns.put(r.pattern, occurrences = new LinkedList<TreePath>());
                }
                occurrences.add(new TreePath(getCurrentPath(), node));
            }
        }.scan(tree, null);
        
        return occurringPatterns;
    }

    @Override
    public BulkPattern create(Collection<? extends String> code, Collection<? extends Tree> patterns) {
        int startState = 0;
        final int[] nextState = new int[] {1};
        final Map<Key<Input>, State> transitionTable = new LinkedHashMap<Key<Input>, State>();
        Map<Integer, Res> finalStates = new HashMap<Integer, Res>();
        List<Set<? extends String>> identifiers = new LinkedList<Set<? extends String>>();
        List<Set<? extends String>> kinds = new LinkedList<Set<? extends String>>();
        Iterator<? extends String> codeIt = code.iterator();

        for (final Tree pattern : patterns) {
            final int[] currentState = new int[] {startState};
            final Set<String> patternIdentifiers = new HashSet<String>();
            final Set<String> patternKinds = new HashSet<String>();

            identifiers.add(patternIdentifiers);
            kinds.add(patternKinds);

            class Scanner extends TreeScanner<Void, Void> {
                public Void scan(Tree t, Void v) {
                    if (t == null) {
                        return null;
                    }

                    if (Utilities.isMultistatementWildcardTree(t)) {
                        int target = nextState[0]++;

                        setBit(transitionTable, Key.create(currentState[0], new Input(Kind.IDENTIFIER, "$", false)), target);
                        setBit(transitionTable, Key.create(target, new Input(Kind.IDENTIFIER, "$", true)), currentState[0]);
                        
                        return null;
                    }

                    if (t.getKind() == Kind.BLOCK) {
                        StatementTree singletonStatement = null;
                        BlockTree bt = (BlockTree) t;

                        if (!bt.isStatic()) {
                            switch (bt.getStatements().size()) {
                                case 1:
                                    singletonStatement = bt.getStatements().get(0);
                                    break;
                                case 2:
                                    if (Utilities.isMultistatementWildcardTree(bt.getStatements().get(0))) {
                                        singletonStatement = bt.getStatements().get(1);
                                    } else {
                                        if (Utilities.isMultistatementWildcardTree(bt.getStatements().get(1))) {
                                            singletonStatement = bt.getStatements().get(0);
                                        }
                                    }
                                    break;
                                case 3:
                                    if (Utilities.isMultistatementWildcardTree(bt.getStatements().get(0)) && Utilities.isMultistatementWildcardTree(bt.getStatements().get(2))) {
                                        singletonStatement = bt.getStatements().get(1);
                                    }
                                    break;
                            }
                        }

                        if (singletonStatement != null) {
                            int backup = currentState[0];

                            scan(singletonStatement, null);

                            int target = currentState[0];

                            setBit(transitionTable, Key.create(backup, new Input(Kind.BLOCK, null, false)), currentState[0] = nextState[0]++);

                            for (StatementTree st : bt.getStatements()) {
                                scan(st, null);
                            }
                            
                            setBit(transitionTable, Key.create(currentState[0], new Input(Kind.BLOCK, null, true)), target);
                            currentState[0] = target;

                            return null;
                        }
                    }
                    
                    boolean[] goDeeper = new boolean[1];
                    Input[] bypass = new Input[1];
                    Input i = normalizeInput(t, goDeeper, bypass);

                    if (i.name == null) {
                        patternKinds.add(i.kind.name());
                    } else {
                        if (!"$".equals(i.name) && !Utilities.isPureMemberSelect(t, false))
                            patternIdentifiers.add(i.name);
                    }

                    int backup = currentState[0];

                    handleTree(i, goDeeper, t, bypass);

                    if (StatementTree.class.isAssignableFrom(t.getKind().asInterface()) && t != pattern) {
                        int target = currentState[0];

                        setBit(transitionTable, Key.create(backup, new Input(Kind.BLOCK, null, false)), currentState[0] = nextState[0]++);
                        handleTree(i, goDeeper, t, bypass);
                        setBit(transitionTable, Key.create(currentState[0], new Input(Kind.BLOCK, null, true)), target);
                        currentState[0] = target;
                    }

                    return null;
                }

                private void handleTree(Input i, boolean[] goDeeper, Tree t, Input[] bypass) {
                    int backup = currentState[0];

                    setBit(transitionTable, Key.create(backup, i), currentState[0] = nextState[0]++);

                    if (goDeeper[0]) {
                        super.scan(t, null);
                    }

                    if (bypass[0] != null) {
                        setBit(transitionTable, Key.create(backup, bypass[0]), currentState[0]);
                    }

                    int target = nextState[0]++;

                    setBit(transitionTable, Key.create(currentState[0], new Input(i.kind, i.name, true)), target);

                    if (bypass[0] != null) {
                        setBit(transitionTable, Key.create(currentState[0], new Input(bypass[0].kind, bypass[0].name, true)), target);
                    }
                    
                    currentState[0] = target;
                }
            };

            Scanner s = new Scanner();

            s.scan(pattern, null);

            finalStates.put(currentState[0], new Res(codeIt.next()));
        }

        NFA<Input, Res> nfa = NFA.<Input, Res>create(startState, nextState[0], null, transitionTable, finalStates);

        return BulkPatternImpl.create(identifiers, kinds, nfa);
    }

    private static void setBit(Map<Key<Input>, State> transitionTable, Key<Input> input, int state) {
        State target = transitionTable.get(input);

        if (target == null) {
            transitionTable.put(input, target = State.create());
        }

        target.mutableOr(state);
    }

    private static Input normalizeInput(Tree t, boolean[] goDeeper, Input[] bypass) {
        if (t.getKind() == Kind.IDENTIFIER && ((IdentifierTree) t).getName().toString().startsWith("$")) {
            goDeeper[0] = false;
            return new Input(Kind.IDENTIFIER, "$", false);
        }

        if (Utilities.getWildcardTreeName(t) != null) {
            goDeeper[0] = false;
            return new Input(Kind.IDENTIFIER, "$", false);
        }
        
        if (t.getKind() == Kind.IDENTIFIER) {
            goDeeper[0] = false;
            String name = ((IdentifierTree) t).getName().toString();
            return new Input(Kind.IDENTIFIER, name, false);
        }

        if (t.getKind() == Kind.MEMBER_SELECT) {
            String name = ((MemberSelectTree) t).getIdentifier().toString();
            if (name.startsWith("$")) {
                goDeeper[0] = false;
                return new Input(Kind.IDENTIFIER, "$", false);
            }
            if (bypass != null && Utilities.isPureMemberSelect(t, true)) {
                bypass[0] = new Input(Kind.IDENTIFIER, name, false);
            }
            goDeeper[0] = true;
            return new Input(Kind.MEMBER_SELECT, name, false);
        }

        goDeeper[0] = true;
        return new Input(t.getKind(), null, false);
    }

    @Override
    public boolean matches(CompilationInfo info, TreePath tree, BulkPattern pattern) {
        //XXX: performance
        return !match(info, tree, pattern).isEmpty();
    }

    @Override
    public void encode(Tree tree, final EncodingContext ctx) {
        final Set<String> identifiers = new HashSet<String>();
        final Set<String> treeKinds = new HashSet<String>();
        new TreeScanner<Void, Void>() {
            @Override
            public Void scan(Tree t, Void v) {
                if (t == null) return null;

                if (t instanceof StatementTree && Utilities.isMultistatementWildcardTree((StatementTree) t)) {
                    return null;
                }

                boolean[] goDeeper = new boolean[1];

                Input i = normalizeInput(t, goDeeper, null);
                try {
                    ctx.getOut().write('(');
                    ctx.getOut().write(kind2Encoded.get(i.kind));
                    if (i.name == null) {
                        treeKinds.add(i.kind.name());
                    } else {
                        identifiers.add(i.name);
                        ctx.getOut().write('$');
                        ctx.getOut().write(i.name.getBytes("UTF-8"));
                        ctx.getOut().write(';');
                    }

                    if (goDeeper[0]) {
                        super.scan(t, v);
                    }

                    ctx.getOut().write(')');
                } catch (IOException ex) {
                    //XXX
                    Exceptions.printStackTrace(ex);
                }

                return null;
            }
        }.scan(tree, null);

        ctx.setIdentifiers(identifiers);
        ctx.setKinds(treeKinds);
    }

    @Override
    public boolean matches(InputStream encoded, BulkPattern patternIn) {
        try {
            return matchesImpl(encoded, patternIn);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
    }

    private boolean matchesImpl(InputStream encoded, BulkPattern patternIn) throws IOException {
        BulkPatternImpl pattern = (BulkPatternImpl) patternIn;
        final NFA<Input, Res> nfa = pattern.toNFA();
        Stack<Input> unfinished = new Stack<Input>();
        Stack<State> skips = new Stack<State>();
        Stack<State> bypassed = new Stack<State>();
        State active = nfa.getStartingState();
        int read = encoded.read();

        while (read != (-1)) {
            if (read == '(') {
                read = encoded.read(); //kind

                Kind k = encoded2Kind.get((read << 8) + encoded.read());

                read = encoded.read();

                String name;

                if (read == '$') {
                    //XXX:
                    read = encoded.read();

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    //read name:
                    while (read != ';') {
                        baos.write(read);
                        read = encoded.read();
                    }

                    read = encoded.read();
                    name = new String(baos.toByteArray(), "UTF-8");
                } else {
                    name = null;
                }
                
                final State newActiveAfterVariable = nfa.transition(active, new Input(Kind.IDENTIFIER, "$", false));
                Input normalizedInput = new Input(k, name, false);
                State bypassedState = k == Kind.MEMBER_SELECT && name != null ? nfa.transition(active, new Input(Kind.IDENTIFIER, name, false)) : null;
                active = nfa.transition(active, normalizedInput);

                unfinished.push(normalizedInput);
                skips.push(newActiveAfterVariable);
                bypassed.push(bypassedState);
            } else {
                Input i = unfinished.pop();
                State newActiveAfterVariable = skips.pop();
                State s1 = nfa.transition(active, new Input(i.kind, i.name, true));
                State s2 = nfa.transition(newActiveAfterVariable, new Input(Kind.IDENTIFIER, "$", true));

                State bypassedState = bypassed.pop();

                if (bypassedState != null) {
                    State activeAfterBypassed = nfa.transition(bypassedState, new Input(Kind.IDENTIFIER, i.name, true));
                    
                    active = nfa.join(nfa.join(s1, s2), activeAfterBypassed);
                } else {
                    active = nfa.join(s1, s2);
                }
                
                if (!nfa.getResults(active).isEmpty()) {
                    return true;
                }

                read = encoded.read();
            }
        }

        return false;
    }

    private static final Map<Kind, byte[]> kind2Encoded;
    private static final Map<Integer, Kind> encoded2Kind;

    static {
        kind2Encoded = new EnumMap<Kind, byte[]>(Kind.class);
        encoded2Kind = new HashMap<Integer, Kind>();

        for (Kind k : Kind.values()) {
            String enc = Integer.toHexString(k.ordinal());

            if (enc.length() < 2) {
                enc = "0" + enc;
            }

            try {
                final byte[] bytes = enc.getBytes("UTF-8");

                assert bytes.length == 2;

                kind2Encoded.put(k, bytes);


                encoded2Kind.put((bytes[0] << 8) + bytes[1], k);
            } catch (UnsupportedEncodingException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    public static class BulkPatternImpl extends BulkPattern {

        private final NFA<Input, Res> nfa;

        private BulkPatternImpl(List<? extends Set<? extends String>> identifiers, List<? extends Set<? extends String>> kinds, NFA<Input, Res> nfa) {
            super(identifiers, kinds);
            this.nfa = nfa;
        }

        NFA<Input, Res> toNFA() {
            return nfa;
        }
        
        private static BulkPattern create(List<? extends Set<? extends String>> identifiers, List<? extends Set<? extends String>> kinds, NFA<Input, Res> nfa) {
            return new BulkPatternImpl(identifiers, kinds, nfa);
        }

    }

    private static final class Res {
        private final String pattern;

        public Res(String pattern) {
            this.pattern = pattern;
        }

    }

    private static final class Input {
        private final Kind kind;
        private final String name;
        private final boolean end;

        private Input(Kind kind, String name, boolean end) {
            this.kind = kind;
            this.name = name;
            this.end = end;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Input other = (Input) obj;
            if (this.kind != other.kind) {
                return false;
            }
            if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
                return false;
            }
            if (this.end != other.end) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 47 * hash + (this.kind != null ? this.kind.hashCode() : 17);
            hash = 47 * hash + (this.name != null ? this.name.hashCode() : 0);
            hash = 47 * hash + (this.end ? 1 : 0);
            return hash;
        }

        @Override
        public String toString() {
            return kind + ", " + name + ", " + end;
        }

    }
}
