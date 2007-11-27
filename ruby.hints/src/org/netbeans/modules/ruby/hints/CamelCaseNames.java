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
import java.util.Collections;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import org.jruby.ast.MethodDefNode;
import org.jruby.ast.Node;
import org.jruby.ast.NodeTypes;
import org.jruby.ast.types.INameNode;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.netbeans.modules.ruby.AstPath;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.RubyUtils;
import org.netbeans.modules.ruby.hints.spi.AstRule;
import org.netbeans.modules.ruby.hints.spi.Description;
import org.netbeans.modules.ruby.hints.spi.Fix;
import org.netbeans.modules.ruby.hints.spi.HintSeverity;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.openide.cookies.EditorCookie;
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
public class CamelCaseNames implements AstRule {
    public CamelCaseNames() {
    }

    public boolean appliesTo(CompilationInfo info) {
        return true;
    }

    public Set<Integer> getKinds() {
        Set<Integer> integers = new HashSet<Integer>();
        integers.add(NodeTypes.LOCALASGNNODE);
        integers.add(NodeTypes.DEFNNODE);
        integers.add(NodeTypes.DEFSNODE);
        return integers;
    }
    
    public void run(CompilationInfo info, Node node, AstPath path, int caretOffset, List<Description> result) {
        String name = ((INameNode)node).getName();

        for (int i = 0; i < name.length(); i++) {
            if (Character.isUpperCase(name.charAt(i))) {
                String key =  node.nodeId == NodeTypes.LOCALASGNNODE ? "InvalidLocalName" : "InvalidMethodName"; // NOI18N
                String displayName = NbBundle.getMessage(CamelCaseNames.class, key);
                OffsetRange range = AstUtilities.getNameRange(node);
                range = LexUtilities.getLexerOffsets(info, range);
                if (range != OffsetRange.NONE) {
                    List<Fix> fixList = new ArrayList<Fix>(2);
                    Node root = AstUtilities.getRoot(info);
                    AstPath childPath = new AstPath(root, node); // TODO - make a simple clone method to clone AstPath path
                    if (node.nodeId == NodeTypes.LOCALASGNNODE) {
                        fixList.add(new RenameFix(info, childPath, RubyUtils.camelToUnderlinedName(name)));
                    }
                    fixList.add(new RenameFix(info, childPath, null));
                    Description desc = new Description(this, displayName, info.getFileObject(), range, fixList, 1500);
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
    
    private static class RenameFix implements Fix, Runnable {

        private CompilationInfo info;
        private AstPath path;
        private String newName;

        RenameFix(CompilationInfo info, AstPath path, String newName) {
            this.info = info;
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

        public void implement() throws Exception {
            if (newName != null) {
                Node node = path.leaf();
                assert node.nodeId == NodeTypes.LOCALASGNNODE;
                String oldName = ((INameNode)node).getName();
                int oldLength = oldName.length();
                
                Node scope = AstUtilities.findLocalScope(node, path);
                Set<OffsetRange> ranges = new HashSet<OffsetRange>();
                addLocalRegions(scope, oldName, ranges);
                
                BaseDocument doc = (BaseDocument)info.getDocument();
                try {
                    doc.atomicLock();
                    
                    List<Integer> starts = new ArrayList<Integer>();
                    for (OffsetRange range : ranges) {
                        int start = range.getStart();
                        starts.add(start);
                        assert range.getLength() == oldLength;
                    }
                    // Replace the words in document-decreasing order (to ensure offsets are right)
                    Collections.sort(starts);
                    Collections.reverse(starts);
                    for (int start : starts) {
                        doc.replace(start, oldLength, newName, null);
                    }
                } catch (BadLocationException ble) {
                    Exceptions.printStackTrace(ble);
                } finally {
                    doc.atomicUnlock();
                }
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
                DataObject od = DataObject.find(info.getFileObject());
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
        
        private void addLocalRegions(Node node, String name, Set<OffsetRange> ranges) {
            if ((node.nodeId == NodeTypes.LOCALASGNNODE || node.nodeId == NodeTypes.LOCALVARNODE) && name.equals(((INameNode)node).getName())) {
                OffsetRange range = AstUtilities.getNameRange(node);
                range = LexUtilities.getLexerOffsets(info, range);
                if (range != OffsetRange.NONE) {
                    ranges.add(range);
                }
            }

            @SuppressWarnings(value = "unchecked")
            List<Node> list = node.childNodes();

            for (Node child : list) {

                // Skip inline method defs
                if (child instanceof MethodDefNode) {
                    continue;
                }
                addLocalRegions(child, name, ranges);
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
