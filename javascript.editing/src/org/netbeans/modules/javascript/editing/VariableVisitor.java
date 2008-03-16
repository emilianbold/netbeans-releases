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

package org.netbeans.modules.javascript.editing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.Token;

/**
 * Visitor which tracks variables through scopes and answers questions about them.
 * Used for semantic highlighting as well as instant rename etc.
 * @todo Stash the results of this guy on the parse result so semantic analysis, occurrence
 *  marking etc. can all share the results.
 */
public class VariableVisitor implements ParseTreeVisitor {

    private AstPath path = new AstPath();
    private ScopeChain scopes = new ScopeChain();
    private boolean inWith;
    
    //private List<Scope> allScopes = new ArrayList<Scope>();

    private static class Scope implements Iterable<Scope> {
        private final Node node;
        private List<Scope> nested;
        private Scope parent;
        // This needs to be per scope
        private Set<String> locals = new HashSet<String>();
        private Set<String> readVars = new HashSet<String>();
        private Set<String> readCalls = new HashSet<String>();
        private Set<String> writtenVars = new HashSet<String>();

        Scope(Node node) {
            this.node = node;
        }

        void addScope(Scope scope) {
            if (nested == null) {
                nested = new ArrayList<Scope>();
            }
            nested.add(scope);
            scope.parent = this;
        }

        public Iterator<Scope> iterator() {
            if (nested != null) {
                return nested.iterator();
            } else {
                return Collections.<Scope>emptySet().iterator();
            }
        }

        private List<Node> findVarNodes(String name) {
            List<Node> nodes = new ArrayList<Node>();
            addNodes(node, name, nodes);
            
            return nodes;
        }
        
        // Iterate over a scope and mark the given unused locals and globals in the highlights map
        private void addNodes(Node node, String name, List<Node> result) {
            switch (node.getType()) {
                case Token.NAME:
                case Token.PARAMETER:
                case Token.BINDNAME: {
                    String s = node.getString();
                    if (s.equals(name)) {
                        result.add(node);
                    }
                    break;
                }
            }

            if (node.hasChildren()) {
                Node child = node.getFirstChild();

                for (; child != null; child = child.getNext()) {
                    int type = child.getType();
                    if (type == Token.FUNCTION || type == Token.SCRIPT) {
                        // It's another scope - skip
                        continue;
                    }
                    addNodes(child, name, result);
                }
            }
        }

        private List<Node> findVarNodes(Set<String> names) {
            List<Node> nodes = new ArrayList<Node>();
            addNodes(node, names, nodes);
            
            return nodes;
        }
        
        // Iterate over a scope and mark the given unused locals and globals in the highlights map
        private void addNodes(Node node, Set<String> names, List<Node> result) {
            switch (node.getType()) {
                case Token.NAME:
                case Token.PARAMETER:
                case Token.BINDNAME: {
                    String s = node.getString();
                    if (names.contains(s)) {
                        result.add(node);
                    }
                    break;
                }
            }

            if (node.hasChildren()) {
                Node child = node.getFirstChild();

                for (; child != null; child = child.getNext()) {
                    int type = child.getType();
                    if (type == Token.FUNCTION || type == Token.SCRIPT) {
                        // It's another scope - skip
                        continue;
                    }
                    addNodes(child, names, result);
                }
            }
        }

        @Override
        public String toString() {
            return "Scope(node=" + node + ",locals=" + locals + ",read=" + readVars + ",calls=" + readCalls + ", written=" + writtenVars + ")";
        }
    }

    private static class ScopeChain {
        private List<Scope> scopes = new ArrayList<Scope>();
        private List<Scope> roots = new ArrayList<Scope>();
        private Scope current;

        Scope getCurrent() {
            return current;
        }

        Scope push(Node parent) {
            Scope scope = new Scope(parent);
            if (roots.isEmpty()) {
                roots.add(scope);
            } else {
                current.addScope(scope);
            }
            scopes.add(scope);
            current = scope;

            return scope;
        }

        Scope pop() {
            current = current.parent;

            return current;
        }

        private Scope findScope(Node node) {
            // Locate surrounding function/script
            while (node != null) {
                int type = node.getType();
                if (type == Token.FUNCTION || type == Token.SCRIPT) {
                    for (Scope root : roots) {
                        Scope s = findScope(root, node);
                        if (s != null) {
                            return s;
                        }
                    }
                }
                
                node = node.getParentNode();
            }
            
            return null;
        }
        
        private Scope findScope(Scope scope, Node node) {
            if (scope.node == node) {
                return scope;
            }
            
            if (scope.nested != null) {
                for (Scope child : scope.nested) {
                    Scope s = findScope(child, node);
                    if (s != null) {
                        return s;
                    }
                }
            }
            
            return null;
        }

        @Override
        public String toString() {
            return "ScopeChain:" + roots;
        }
    }

    public VariableVisitor() {
        //scopes.add(new Scope()); // Default scope - not necessary, root should be a SCRIPTNODE
    }

    private Node getParent() {
        return path.leafParent();
    }

    public boolean visit(Node node) {
        path.descend(node);
        switch (node.getType()) {
            case Token.FUNCTION:
            case Token.SCRIPT: {
                scopes.push(node);
                break;
            }
            
            case Token.PARAMETER: {
                Scope scope = scopes.getCurrent();
                scope.locals.add(node.getString());
                break;
            }

            case Token.BINDNAME: {
                if (inWith) {
                    // Assignments in a with block really apply to the with object
                    // so these aren't globals or even locals, they are properties
                    break;
                }
                // TODO - only track when used in a SETNAME?
//                    Node parent = getParent();
//                    int type = -1;
//                    if (parent != null) {
//                        type = parent.getType();
//                    }
//                    assert type == Token.SETNAME : "unexpected BINDNAME - parent type = " + Token.fullName(type);

                Scope scope = scopes.getCurrent();
                String var = node.getString();
                scope.writtenVars.add(var);
                break;
            }
            
            case Token.WITH: {
                inWith = true;
                break;
            }

            case Token.NAME: {
                Node parent = getParent();

                int type = -1;
                if (parent != null) {
                    type = parent.getType();
                    if (type == Token.CALL && parent.getFirstChild() != node) {
                        // It's only the method being called if it's the first child
                        type = -1;
                    }
                }
                if (type == Token.VAR) {
                    // Variable definition
                    Scope scope = scopes.getCurrent();
                    if (scope.node.getType() == Token.SCRIPT) {
                        // Global
                        // TODO - track global assignments separately from
                        // just written globals, such that I can do a deeper check
                        // of global writes I haven't seen definitions for? These
                        // I can check against the global variable cache and warn
                        // if it looks truly unique (e.g. probably an error).
                        scope.writtenVars.add(node.getString());
                    } else {
                        // Local
                        scope.locals.add(node.getString());
                    }
                } else if (type == Token.CATCH_SCOPE) {
                    // It's a local variable defintion, or a variable in the catch clause
                    Scope scope = scopes.getCurrent();
                    scope.locals.add(node.getString());
                } else if (type == Token.CALL) {
                    Scope scope = scopes.getCurrent();
                    scope.readCalls.add(node.getString());
                } else {
                    if (inWith) {
                        // Assignments in a with block really apply to the with object
                        // so these aren't globals or even locals, they are properties
                        break;
                    }
                    // A variable read
                    Scope scope = scopes.getCurrent();
                    String str = node.getString();
                    if (!str.equals("undefined")) {
                        scope.readVars.add(str);
                    }
                }
                break;
            }
        }


        // The first node of a CALL is the name of the function we're calling -- not a variable
        // The first node of a 
        // Look for SETNAME nodes (with first child BINDNAME(name) providing the name of the bound thingy)

        return false;
    }

    public boolean unvisit(Node node) {
        switch (node.getType()) {
            case Token.SCRIPT:
            case Token.FUNCTION: {
                scopes.pop();

                break;
            }
            case Token.WITH: {
                // XXX Not accurate - should search outwards and see if 
                // I'm indeed in another nested with!
                inWith = false;
                break;
            }

        }

        path.ascend();

        return false;
    }

    Collection<Node> getUnusedVars() {
        List<Node> vars = new ArrayList<Node>();
        for (Scope root : scopes.roots) {
            // Recursively walk through scopes and compute all names
            // Annotate variable usages - unused, global, etc
            addUnused(root, vars);
        }
        
        return vars;
    }

    public Map<String,List<Node>> getLocalVars(Node node) {
        Scope scope = scopes.findScope(node);
        Map<String,List<Node>> result = new HashMap<String,List<Node>>();
        for (Scope s = scope; s != null; s = s.parent) {
            for (String name : s.locals) {
                List<Node> list = s.findVarNodes(name);
                assert list != null;
                List<Node> l = result.get(name);
                if (l != null) {
                    l.addAll(list);
                } else {
                    result.put(name, list);
                }
            }
        }
        return result;
    }

    private void addUnused(Scope scope, List<Node> vars) {
        Set<String> unused = new HashSet<String>(scope.locals);
        removeRead(scope, unused);

        if (unused.size() > 0) {
            // Locate nodes
            List<Node> nodes = scope.findVarNodes(unused);
            vars.addAll(nodes);
        }

        for (Scope nested : scope) {
            addUnused(nested, vars);
        }
    }

    private void removeRead(Scope scope, Set<String> unused) {
        unused.removeAll(scope.readVars);
        unused.removeAll(scope.readCalls);

        for (Scope nested : scope) {
            removeRead(nested, unused);
        }
    }

    private void removeLocals(Scope scope, Set<String> vars) {
        vars.removeAll(scope.locals);

        for (Scope nested : scope) {
            removeLocals(nested, vars);
        }
    }

    public List<Node> getGlobalVars(boolean writesOnly) {
        List<Node> vars = new ArrayList<Node>();
        for (Scope root : scopes.roots) {
            // Recursively walk through scopes and compute all names
            // Annotate variable usages - unused, global, etc
            addGlobals(root, vars, writesOnly);
        }
        
        return vars;
    }

    private void addGlobals(Scope scope, List<Node> vars, boolean writesOnly) {
        Set<String> globals;
        if (writesOnly) {
            globals = new HashSet<String>(scope.writtenVars);
        } else {
            globals = new HashSet<String>(scope.readVars);
            globals.addAll(scope.writtenVars);
        }
        removeLocals(scope, globals);
        for (Scope s = scope.parent; s != null; s = s.parent) {
            globals.removeAll(s.locals);
        }

        if (globals.size() > 0) {
            // Locate nodes
            List<Node> nodes = scope.findVarNodes(globals);
            vars.addAll(nodes);
        }

        for (Scope nested : scope) {
            addGlobals(nested, vars, writesOnly);
        }
    }
    
    public Node getDefiningScope(Node var) {
        int type = var.getType();
        if (type == Token.NAME || type == Token.BINDNAME || type == Token.PARAMETER) {
            Scope scope = scopes.findScope(var);
            if (scope != null) {
                return scope.node;
            }
        }
        
        return null;
    }
}
