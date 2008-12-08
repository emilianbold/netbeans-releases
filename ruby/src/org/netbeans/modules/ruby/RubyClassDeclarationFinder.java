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
package org.netbeans.modules.ruby;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jruby.nb.ast.AliasNode;
import org.jruby.nb.ast.ClassNode;
import org.jruby.nb.ast.Colon2Node;
import org.jruby.nb.ast.ConstDeclNode;
import org.jruby.nb.ast.Node;
import org.jruby.nb.ast.types.INameNode;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.ruby.elements.IndexedClass;
import org.netbeans.modules.ruby.elements.IndexedElement;

final class RubyClassDeclarationFinder extends RubyBaseDeclarationFinder {

    DeclarationLocation findClassDeclaration(
            final CompilationInfo info,
            final Node root,
            final AstPath path,
            final RubyIndex index,
            final Node closest) {
        String className = ((INameNode) closest).getName();
        // Disable local class searching for now - it should instead be a
        // criterion for increasing match priority
        Node localClass = null; // findClassDeclaration(root, className);

        if (localClass != null) {
            // Ensure that we have the right FQN if specific
            if (closest instanceof Colon2Node) {
                AstPath classPath = new AstPath(root, localClass);

                if (classPath.leaf() != null) {
                    String fqn1 = AstUtilities.getFqn((Colon2Node) closest);
                    String fqn2 = AstUtilities.getFqnName(classPath);

                    if (fqn1.equals(fqn2)) {
                        return fix(getLocation(info, localClass), info);
                    }
                } else {
                    assert false : localClass.toString();
                }
            } else {
                return fix(getLocation(info, localClass), info);
            }
        }

        if (closest instanceof Colon2Node) {
            className = AstUtilities.getFqn((Colon2Node) closest);
        }

        // E.g. for "include Assertions" within Test::Unit::TestCase, try looking
        // for Test::Unit::TestCase::Assertions, Test::Unit:Assertions, Test::Assertions.
        // And for "include Util::Backtracefilter" try Test::Unit::Util::Backtracefilter etc.
        String possibleFqn = AstUtilities.getFqnName(path);

        // Try searching by qualified name by context first, if it's not qualified
        Set<IndexedClass> classes = Collections.emptySet();
        String fqn = possibleFqn;

        // First try looking only at the local scope
        Set<String> uniqueClasses = new HashSet<String>();
        while ((classes.size() == 0) && (fqn.length() > 0)) {
            classes =
                    index.getClasses(fqn + "::" + className, NameKind.EXACT_NAME, true, false, // NOI18N
                    false, RubyIndex.ALL_SCOPE, uniqueClasses);

            int f = fqn.lastIndexOf("::"); // NOI18N

            if (f == -1) {
                break;
            } else {
                fqn = fqn.substring(0, f);
            }
        }

        if (classes.size() == 0) {
            classes = index.getClasses(className, NameKind.EXACT_NAME, true, false, false,
                    RubyIndex.ALL_SCOPE, uniqueClasses);
        }

        // If no success with looking only at the source scope, look in libraries as well
        if (classes.size() == 0) {
            fqn = possibleFqn;

            // Try looking at the libraries too
            while ((classes.size() == 0) && (fqn.length() > 0)) {
                classes =
                        index.getClasses(fqn + "::" + className, NameKind.EXACT_NAME, true, false, // NOI18N
                        false);

                int f = fqn.lastIndexOf("::");

                if (f == -1) {
                    break;
                } else {
                    fqn = fqn.substring(0, f);
                }
            }

            if (classes.size() == 0) {
                classes = index.getClasses(className, NameKind.EXACT_NAME, true, false, false);
            }
        }

        return getClassDeclaration(info, classes, path, closest, index);
    }

    DeclarationLocation getClassDeclaration(
            final CompilationInfo info,
            final Set<IndexedClass> classes,
            final AstPath path,
            final Node closest,
            final RubyIndex index) {
        
        final IndexedClass candidate =
                findBestClassMatch(classes, path, closest, index);

        if (candidate != null) {
            IndexedElement com = candidate;
            Node node = AstUtilities.getForeignNode(com, (Node[]) null);

            DeclarationLocation loc = new DeclarationLocation(com.getFile().getFileObject(),
                    node.getPosition().getStartOffset(), com);

            if (!CHOOSE_ONE_DECLARATION && classes.size() > 1) {
                // Could the :nodoc: alternatives: if there is only one nodoc'ed alternative
                // don't ask user!
                int not_nodoced = 0;
                for (final IndexedClass clz : classes) {
                    if (!clz.isNoDoc()) {
                        not_nodoced++;
                    }
                }
                if (not_nodoced >= 2) {
                    for (final IndexedClass clz : classes) {
                        loc.addAlternative(new RubyAltLocation(clz, clz == candidate));
                    }
                }
            }

            return loc;
        }

        return DeclarationLocation.NONE;
    }

    Node findClass(
            final Node node,
            final String name,
            final boolean ignoreAlias) {
        if (node instanceof ClassNode) {
            String n = AstUtilities.getClassOrModuleName((ClassNode) node);

            if (n.equals(name)) {
                return node;
            }
        } else if (node instanceof ConstDeclNode) {
            if (((INameNode) node).getName().equals(name)) {
                return node;
            }
        } else if (!ignoreAlias && node instanceof AliasNode) {
            if (((AliasNode) node).getNewName().equals(name)) {
                return node;
            }
        }

        List<Node> list = node.childNodes();

        for (Node child : list) {
            if (child.isInvisible()) {
                continue;
            }
            Node match = findClass(child, name, ignoreAlias);

            if (match != null) {
                return match;
            }
        }

        return null;
    }

    IndexedClass findBestClassMatch(
            final Set<? extends IndexedClass> classSet,
            final AstPath path,
            final Node reference,
            final RubyIndex index) {
        // Make sure that the best fit method actually has a corresponding valid
        // source location and parse tree
        Set<IndexedClass> classes = new HashSet<IndexedClass>(classSet);

        while (!classes.isEmpty()) {
            IndexedClass clz = findBestClassMatchHelper(classes, path, reference, index);
            Node node = AstUtilities.getForeignNode(clz, (Node[]) null);

            if (node != null) {
                return clz;
            }

            // TODO: Sort results, then pick candidate number modulo methodSelector
            if (!classes.contains(clz)) {
                // Avoid infinite loop when we somehow don't find the node for
                // the best class and we keep trying it
                classes.remove(classes.iterator().next());
            } else {
                classes.remove(clz);
            }
        }

        return null;
    }

    // Now that I have a common RubyObject superclass, can I combine this and
    // findBestMethodMatchHelper since there's a lot of code duplication that
    // could be shared by just operating on RubyObjects ?
    private IndexedClass findBestClassMatchHelper(
            final Set<? extends IndexedClass> origClasses,
            final AstPath path,
            final Node reference,
            final RubyIndex index) {
        Set<? extends IndexedClass> classes = new HashSet<IndexedClass>(origClasses);
        // 1. First see if the reference is fully qualified. If so the job should
        //   be easier: prune the result set down
        // If I have the fqn, I can also call RubyIndex.getRDocLocation to pick the
        // best candidate
        Set<IndexedClass> candidates = new HashSet<IndexedClass>();

        if (reference instanceof Colon2Node) {
            String fqn = AstUtilities.getFqn((Colon2Node) reference);

            while ((fqn != null) && (fqn.length() > 0)) {
                for (IndexedClass clz : classes) {
                    if (fqn.equals(clz.getSignature())) {
                        candidates.add(clz);
                    }
                }

                // TODO: Use the fqn to check if the class is documented: if so, prefer it

                // Check inherited methods; for example, if we've determined
                // that you're looking for Integer::foo, I should happily match
                // Numeric::foo.
                IndexedClass superClass = index.getSuperclass(fqn);

                if (superClass != null) {
                    fqn = superClass.getSignature();
                } else {
                    break;
                }
            }
        }

        if (candidates.size() == 1) {
            return candidates.iterator().next();
        } else if (!candidates.isEmpty()) {
            classes = candidates;
        }

        // 2. See if the reference is followed by a method call - if so, that may
        //   help disambiguate which reference we're after.
        // TODO

        // 3. See which of the class references are defined in files directly
        //   required by this file.
        Set<String> requires = null;

        if (path != null) {
            candidates = new HashSet<IndexedClass>();

            requires = AstUtilities.getRequires(path.root());

            for (IndexedClass clz : classes) {
                String require = clz.getRequire();

                if (requires.contains(require)) {
                    candidates.add(clz);
                }
            }

            if (candidates.size() == 1) {
                return candidates.iterator().next();
            } else if (!candidates.isEmpty()) {
                classes = candidates;
            }
        }

        // 4. See if any of the classes are "kernel" classes (builtins) and for these
        //   go to the known locations
        candidates = new HashSet<IndexedClass>();

        for (IndexedClass clz : classes) {
            String url = clz.getFileUrl();

            if (url != null && url.indexOf("rubystubs") != -1) { // NOI18N
                candidates.add(clz);
            }
        }

        if (candidates.size() == 1) {
            return candidates.iterator().next();
        } else if (!candidates.isEmpty()) {
            classes = candidates;
        }

        // 5. See which classes are documented, and prefer those over undocumented classes
        candidates = new HashSet<IndexedClass>();

        int longestDocLength = 0;

        for (IndexedClass clz : classes) {
            int length = clz.getDocumentationLength();

            if (length > longestDocLength) {
                candidates.clear();
                candidates.add(clz);
                longestDocLength = length;
            } else if ((length > 0) && (length == longestDocLength)) {
                candidates.add(clz);
            }
        }

        if (candidates.size() == 1) {
            return candidates.iterator().next();
        } else if (!candidates.isEmpty()) {
            classes = candidates;
        }

        // 6. Look at transitive closure of require statements and see which files
        //  are most likely candidates
        if ((index != null) && (requires != null)) {
            candidates = new HashSet<IndexedClass>();

            Set<String> allRequires = index.getRequiresTransitively(requires);

            for (IndexedClass clz : classes) {
                String require = clz.getRequire();

                if (allRequires.contains(require)) {
                    candidates.add(clz);
                }
            }

            if (candidates.size() == 1) {
                return candidates.iterator().next();
            } else if (!candidates.isEmpty()) {
                classes = candidates;
            }
        }

        // 7. Other heuristics: Look at the method definition with the
        //   most methods associated with it. Look at other uses of this
        //   class in this parse tree, look at the methods and see if we
        //   can rule out candidates based on that
        //
        // 7b. Give priority to class definitions that are local: obviously
        //   there are class definitions in the same file, and then in the same project
        //
        // 8. Look at superclasses and consider -their- requires to figure out
        //   which class we're supposed to use
        // TODO
        candidates = new HashSet<IndexedClass>();

        // Pick one arbitrarily
        if (classes.size() > 0) {
            return classes.iterator().next();
        } else {
            return null;
        }
    }
}
