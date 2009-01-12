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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.hints;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.jruby.nb.ast.MethodDefNode;
import org.jruby.nb.ast.Node;
import org.jruby.nb.ast.NodeType;
import org.jruby.nb.ast.types.INameNode;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.PreviewableFix;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.api.annotations.CheckForNull;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.netbeans.modules.ruby.AstPath;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.RubyUtils;
import org.netbeans.modules.ruby.hints.infrastructure.RubyAstRule;
import org.netbeans.modules.ruby.hints.infrastructure.RubyRuleContext;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Check names to see if they conform to standard Ruby conventions.
 * Checks both method definitions and local symbols to see if they are
 * using camel case rather than lowercase_names.
 * 
 * @todo Add fix to rename!
 * 
 * @author Tor Norbye
 */
public class CamelCaseNames extends RubyAstRule {
    
    public CamelCaseNames() {
    }

    public boolean appliesTo(RuleContext context) {
        return true;
    }

    public Set<NodeType> getKinds() {
        Set<NodeType> integers = new HashSet<NodeType>();
        integers.add(NodeType.LOCALASGNNODE);
        integers.add(NodeType.DEFNNODE);
        integers.add(NodeType.DEFSNODE);
        return integers;
    }
    
    public void run(RubyRuleContext context, List<Hint> result) {
        Node node = context.node;
        ParserResult info = context.parserResult;

        String name = ((INameNode)node).getName();

        for (int i = 0; i < name.length(); i++) {
            if (Character.isUpperCase(name.charAt(i))) {
                String key =  node.nodeId == NodeType.LOCALASGNNODE ? "InvalidLocalName" : "InvalidMethodName"; // NOI18N
                String displayName = NbBundle.getMessage(CamelCaseNames.class, key);
                OffsetRange range = AstUtilities.getNameRange(node);
                range = LexUtilities.getLexerOffsets(info, range);
                if (range != OffsetRange.NONE) {
                    List<HintFix> fixList = new ArrayList<HintFix>(2);
                    Node root = AstUtilities.getRoot(info);
                    AstPath childPath = new AstPath(root, node); // TODO - make a simple clone method to clone AstPath path
                    if (node.nodeId == NodeType.LOCALASGNNODE) {
                        fixList.add(new RenameFix(context, childPath, RubyUtils.camelToUnderlinedName(name)));
                    }
                    fixList.add(new RenameFix(context, childPath, null));
                    Hint desc = new Hint(this, displayName, RubyUtils.getFileObject(info), range, fixList, 1500);
                    result.add(desc);
                }
                return;
            }
        }
    }

    public String getId() {
        return "Camelcase_Names"; // NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(CamelCaseNames.class, "CamelCaseNames");
    }

    public String getDescription() {
        return NbBundle.getMessage(CamelCaseNames.class, "CamelCaseNamesDesc");
    }

    public boolean getDefaultEnabled() {
        return true;
    }

    public boolean showInTasklist() {
        return true;
    }

    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }

    public JComponent getCustomizer(Preferences node) {
        return null;
    }
    
    private static class RenameFix implements PreviewableFix, Runnable {

        private final RubyRuleContext context;
        private final AstPath path;
        private final String newName;

        RenameFix(RubyRuleContext context, AstPath path, String newName) {
            this.context = context;
            this.path = path;
            this.newName = newName;
        }

        public String getDescription() {
            if (newName != null) {
                return NbBundle.getMessage(CamelCaseNames.class, "RenameTo", newName);
            } else {
                return NbBundle.getMessage(CamelCaseNames.class, "RenameVar");
            }
        }
        
        private Set<OffsetRange> getRanges() {
            Node node = path.leaf();
            assert node.nodeId == NodeType.LOCALASGNNODE;
            String oldName = ((INameNode)node).getName();

            Node scope = AstUtilities.findLocalScope(node, path);
            Set<OffsetRange> ranges = new HashSet<OffsetRange>();
            addLocalRegions(scope, oldName, ranges, false);
            
            return ranges;
        }
        
        private String getOldName() {
            Node node = path.leaf();
            assert node.nodeId == NodeType.LOCALASGNNODE;
            String oldName = ((INameNode)node).getName();
            return oldName;
        }
        
        @CheckForNull
        private EditList getEditList(String name) {
            int oldLength = getOldName().length();
            Set<OffsetRange> ranges = getRanges();

            EditList edits = new EditList(context.doc);

            for (OffsetRange range : ranges) {
                edits.replace(range.getStart(), oldLength, name, false, 0);
                assert range.getLength() == oldLength;
            }
            
            return edits;
        }
        
        public boolean canPreview() {
            return newName != null;
        }

        public EditList getEditList() throws Exception {
            return getEditList(newName != null ? newName : "new_name");
        }
        
        public void implement() throws Exception {
            if (newName != null) {
                EditList edits = getEditList(newName);
                edits.apply();
            } else {
                // Full rename
                if (SwingUtilities.isEventDispatchThread()) {
                    run();
                } else {
                    SwingUtilities.invokeLater(this);
                }
            }
        }

        public void run() {
            // Full rename - can only be done from the event dispatch thread
            // (because the RefactoringActionsProvider calls getOpenedPanes on CloneableEditorSupport)
            try {
                DataObject od = DataObject.find(RubyUtils.getFileObject(context.parserResult));
                EditorCookie ec = od.getCookie(EditorCookie.class);
                org.openide.nodes.Node n = od.getNodeDelegate();
                InstanceContent ic = new InstanceContent();
                ic.add(ec);
                ic.add(n);

                Lookup actionContext = new AbstractLookup(ic);
                Action a =
                    RefactoringActionsFactory.renameAction().createContextAwareInstance(actionContext);
                a.actionPerformed(RefactoringActionsFactory.DEFAULT_EVENT);
            } catch (DataObjectNotFoundException dnf) {
                Exceptions.printStackTrace(dnf);
            }
        }
        
        private void addLocalRegions(Node node, String name, Set<OffsetRange> ranges, boolean isParameter) {
            if ((node.nodeId == NodeType.LOCALASGNNODE || node.nodeId == NodeType.LOCALVARNODE) && name.equals(((INameNode)node).getName())) {
                OffsetRange range = AstUtilities.getNameRange(node);
                range = LexUtilities.getLexerOffsets(context.parserResult, range);
                if (range != OffsetRange.NONE) {
                    ranges.add(range);
                }
            } else if (isParameter && (node.nodeId == NodeType.ARGUMENTNODE && name.equals(((INameNode)node).getName()))) {
                OffsetRange range = AstUtilities.getNameRange(node);
                range = LexUtilities.getLexerOffsets(context.parserResult, range);
                if (range != OffsetRange.NONE) {
                    ranges.add(range);
                }
            } else if (node.nodeId == NodeType.ARGSNODE) {
                isParameter = true;
            }


            List<Node> list = node.childNodes();

            for (Node child : list) {
                if (child.isInvisible()) {
                    continue;
                }

                // Skip inline method defs
                if (child instanceof MethodDefNode) {
                    continue;
                }

                addLocalRegions(child, name, ranges, isParameter);
            }
        }

        public boolean isSafe() {
            return false;
        }

        public boolean isInteractive() {
            return true;
        }
    }
}
