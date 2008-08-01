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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.jruby.ast.MethodDefNode;
import org.jruby.ast.Node;
import org.jruby.ast.NodeType;
import org.jruby.ast.types.INameNode;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.EditRegions;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.api.Hint;
import org.netbeans.modules.gsf.api.EditList;
import org.netbeans.modules.gsf.api.HintFix;
import org.netbeans.modules.gsf.api.HintSeverity;
import org.netbeans.modules.gsf.api.PreviewableFix;
import org.netbeans.modules.gsf.api.RuleContext;
import org.netbeans.modules.ruby.AstPath;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.NbUtilities;
import org.netbeans.modules.ruby.RubyParseResult;
import org.netbeans.modules.ruby.RubyStructureAnalyzer.AnalysisResult;
import org.netbeans.modules.ruby.elements.AstAttributeElement;
import org.netbeans.modules.ruby.elements.AstClassElement;
import org.netbeans.modules.ruby.hints.infrastructure.RubyAstRule;
import org.netbeans.modules.ruby.hints.infrastructure.RubyRuleContext;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Detect accidental local variable assignment intended to be an attribute call,
 * such as
 * <pre>
 *    class Foo
 *       attr_accessor :bar
 * 
 *       def foo
 *          bar = 50  # this does NOT change the bar property, shoudl be self.bar
 *       end
 *    end
 * </pre>
 * 
 * 
 * 
 * @author Tor Norbye
 */
public class AttributeIsLocal extends RubyAstRule {
    public AttributeIsLocal() {
    }
    
    private Map<AstClassElement,Set<AstAttributeElement>> attributes;
    private Set<String> attributeNames;

    public boolean appliesTo(RuleContext context) {
        CompilationInfo info = context.compilationInfo;
        RubyParseResult rpr = AstUtilities.getParseResult(info);
        AnalysisResult ar = rpr.getStructure();
        this.attributes = ar.getAttributes();

        if (attributes == null || attributes.size() == 0) {
            return false;
        }

        attributeNames = new HashSet<String>();
        for (AstClassElement clz : attributes.keySet()) {
            Set<AstAttributeElement> ats = attributes.get(clz);
            for (AstAttributeElement ae : ats) {
                if (!ae.isReadOnly()) {
                    attributeNames.add(ae.getName());
                }
            }
        }
        
        return true;
    }

    public Set<NodeType> getKinds() {
        return Collections.singleton(NodeType.LOCALASGNNODE);
    }

    public void run(RubyRuleContext context, List<Hint> result) {
        Node node = context.node;
        AstPath path = context.path;
        CompilationInfo info = context.compilationInfo;
        
        String name = ((INameNode)node).getName();
        AstAttributeElement element = null;
        if (attributeNames.contains(name)) {
            // Possible clash! See if the class is right (the attribute could have been in another
            // class than the one we're looking at)
            Set<AstClassElement> keySet = attributes.keySet();
            boolean match = false;
            AstClassElement clzElement = null;
            String fqn = AstUtilities.getFqnName(path);

            for (AstClassElement clz : keySet) {
                if (fqn.equals(clz.getFqn())) {
                    clzElement = clz;
                    break;
                }
            }
            
            if (clzElement == null) {
                return;
            }
            
            match = false;
            Set<AstAttributeElement> attribs = attributes.get(clzElement);
            if (attribs != null) {
                for (AstAttributeElement ae : attribs) {
                    if (ae.getName().equals(name)) {
                        element = ae;
                        match = true;
                        break;
                    }
                }
            }
            
            if (!match) {
                return;
            }
            
            // Make sure it's not a parameter; these are not intended to access the attribute
            // (e.g. example
            //      attr_accessor   :nodoc
            //
            // def initialize(varname, types, inivalue, arraysuffix, comment,nodoc=false)
            // 
            // In the above, nodoc=false is not a local assignment that should be this.nodoc=false
            Iterator<Node> it = path.leafToRoot();
            while (it.hasNext()) {
                Node n = it.next();
                if (n.nodeId == NodeType.ARGSNODE) {
                    return;
                }
            }
            
            assert element != null;
            OffsetRange range = AstUtilities.getNameRange(node);
            List<HintFix> fixList = new ArrayList<HintFix>(1);
            fixList.add(new ShowAttributeFix(info, element));
            fixList.add(new AttributeConflictFix(context, node, true));
            fixList.add(new AttributeConflictFix(context, node, false));
            range = LexUtilities.getLexerOffsets(info, range);
            if (range != OffsetRange.NONE) {
                Hint desc = new Hint(this, getDisplayName(), info.getFileObject(), range, fixList, 50);
                result.add(desc);
            }
        }
    }

    public String getId() {
        return "Attribute_Is_Local"; // NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(AttributeIsLocal.class, "AttributeIsLocal");
    }

    public String getDescription() {
        return NbBundle.getMessage(AttributeIsLocal.class, "AttributeIsLocalDesc");
    }

    public boolean getDefaultEnabled() {
        return true;
    }

    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }

    public boolean showInTasklist() {
        return true;
    }

    public JComponent getCustomizer(Preferences node) {
        return null;
    }
    
    private static class AttributeConflictFix implements PreviewableFix {

        private final RubyRuleContext context;
        private final boolean fixSelf;
        private final Node node;

        AttributeConflictFix(RubyRuleContext context, Node node, boolean fixSelf) {
            this.context = context;
            this.node = node;
            this.fixSelf = fixSelf;
        }

        public String getDescription() {
            return fixSelf ?
                NbBundle.getMessage(AttributeIsLocal.class, "FixSelf", ((INameNode)node).getName()) :
                NbBundle.getMessage(AttributeIsLocal.class, "FixRename");
        }


        public void implement() throws Exception {
            EditList edits = createEditList(true);
            if (edits != null) {
                edits.apply();
            }
        }

        public EditList getEditList() throws Exception {
            return createEditList(false);
        }

        private EditList createEditList(boolean doit) throws Exception {
            BaseDocument doc = context.doc;
            CompilationInfo info = context.compilationInfo;
            EditList edits = new EditList(doc);
            
            if (fixSelf) {
                OffsetRange range = AstUtilities.getRange(node);
                int start = range.getStart();
                start = LexUtilities.getLexerOffset(info, start);
                if (start != -1) {
                    edits.replace(start, 0, "self.", false, 0); // NOI18N
                }
            } else {
                // Initiate synchronous editing:
                String name = ((INameNode)node).getName();
                Node root = AstUtilities.getRoot(info);
                AstPath path = new AstPath(root, node);
                Node scope = AstUtilities.findLocalScope(path.leaf(), path);
                Set<OffsetRange> ranges = new HashSet<OffsetRange>();
                addLocalRegions(scope, name, ranges);
                // Pick the first range as the caret offset
                int caretOffset = Integer.MAX_VALUE;
                for (OffsetRange range : ranges) {
                    if (range.getStart() < caretOffset) {
                        caretOffset = range.getStart();
                    }
                }
                if (doit) {
                    EditRegions.getInstance().edit(info.getFileObject(), ranges, caretOffset);
                    return null;
                } else {
                    String oldName = ((INameNode)path.leaf()).getName();
                    int oldLen = oldName.length();
                    String newName = "new_name";
                    for (OffsetRange range : ranges) {
                        edits.replace(range.getStart(), oldLen, newName, false, 0);
                    }
                }
            }
            
            return edits;
        }

        private void addLocalRegions(Node node, String name, Set<OffsetRange> ranges) {
            if ((node.nodeId == NodeType.LOCALASGNNODE || node.nodeId == NodeType.LOCALVARNODE) && name.equals(((INameNode)node).getName())) {
                OffsetRange range = AstUtilities.getNameRange(node);
                range = LexUtilities.getLexerOffsets(context.compilationInfo, range);
                if (range != OffsetRange.NONE) {
                    ranges.add(range);
                }
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
                addLocalRegions(child, name, ranges);
            }
        }

        public boolean isSafe() {
            return false;
        }

        public boolean isInteractive() {
            return true;
        }

        public boolean canPreview() {
            return true;
        }
    }

    private static class ShowAttributeFix implements HintFix {

        private final CompilationInfo info;
        private final AstAttributeElement element;

        ShowAttributeFix(CompilationInfo info, AstAttributeElement element) {
            this.info = info;
            this.element = element;
        }

        public String getDescription() {
            Node creationNode = element.getCreationNode();
            String desc;
            if (creationNode instanceof INameNode) {
                desc = ((INameNode)creationNode).getName() + " " + element.getName(); // NOI18N
            } else {
                desc = element.getName();
            }
                    
            return NbBundle.getMessage(AttributeIsLocal.class, "ShowAttribute", desc);
        }

        public void implement() throws Exception {
            FileObject fo = info.getFileObject();
            int astOffset = element.getNode().getPosition().getStartOffset();
            int lexOffset = LexUtilities.getLexerOffset(info, astOffset);
            if (lexOffset != -1) {
                NbUtilities.open(fo, lexOffset, element.getName());
            }
        }

        public boolean isSafe() {
            return true;
        }

        public boolean isInteractive() {
            return true;
        }
    }

}
