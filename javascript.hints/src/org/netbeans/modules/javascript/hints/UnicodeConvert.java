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
package org.netbeans.modules.javascript.hints;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.Token;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.netbeans.modules.javascript.editing.AstPath;
import org.netbeans.modules.javascript.editing.AstUtilities;
import org.netbeans.modules.javascript.editing.JsUtils;
import org.netbeans.modules.javascript.hints.spi.AstRule;
import org.netbeans.modules.javascript.hints.spi.Description;
import org.netbeans.modules.javascript.hints.spi.EditList;
import org.netbeans.modules.javascript.hints.spi.Fix;
import org.netbeans.modules.javascript.hints.spi.HintSeverity;
import org.netbeans.modules.javascript.hints.spi.PreviewableFix;
import org.netbeans.modules.javascript.hints.spi.RuleContext;
import org.netbeans.modules.javascript.editing.lexer.LexUtilities;
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
 * JavaScript can contain unicode, but not all browsers support it.
 * Identify these, and offer to convert!
 * 
 * @author Tor Norbye
 */
public class UnicodeConvert implements AstRule {
    public UnicodeConvert() {
    }

    public boolean appliesTo(CompilationInfo info) {
        return true;
    }

    public Set<Integer> getKinds() {
        Set<Integer> integers = new HashSet<Integer>();
        // XXX root
//        integers.add(-1);
//        integers.add(NodeTypes.LOCALASGNNODE);
//        integers.add(NodeTypes.DEFNNODE);
//        integers.add(NodeTypes.DEFSNODE);
        integers.add(Token.STRING);
        return integers;
    }
    
    public void run(RuleContext context, List<Description> result) {
        CompilationInfo info = context.compilationInfo;
        Node node = context.node;
        
        String s = node.getString();
        for (int i = 0, n = s.length(); i < n; i++) {
            char c = s.charAt(i);
            if ((int)c >= 256) {
                OffsetRange astRange = AstUtilities.getRange(info, node);
                int lexOffset = LexUtilities.getLexerOffset(info, astRange.getStart());
                if (lexOffset == -1) {
                    return;
                }
                try {
                    Document doc = info.getDocument();
                    if (lexOffset < doc.getLength()-i-1) {
                        char d = doc.getText(lexOffset+i, 1).charAt(0);
                        if (d != c) {
                            // Didn't find the actual unicode char there;
                            // it's probably already in \\u form
                            return;
                        }
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
//                lexOffset++; // Skip " ?
                lexOffset += i;
                
                OffsetRange range = new OffsetRange(lexOffset, lexOffset+1);
                List<Fix> fixList = new ArrayList<Fix>();
                fixList.add(new ConvertFix(info, lexOffset, c));
                    String displayName = getDisplayName();
                    Description desc = new Description(this, displayName, info.getFileObject(), range, fixList, 1500);
                    result.add(desc);
            }
        }
        
//        Node node = context.node;
//
//        String name = ((INameNode)node).getName();
//
//        for (int i = 0; i < name.length(); i++) {
//            if (Character.isUpperCase(name.charAt(i))) {
//                String key =  node.nodeId == NodeTypes.LOCALASGNNODE ? "InvalidLocalName" : "InvalidMethodName"; // NOI18N
//                String displayName = NbBundle.getMessage(UnicodeConvert.class, key);
//                OffsetRange range = AstUtilities.getNameRange(node);
//                range = LexUtilities.getLexerOffsets(info, range);
//                if (range != OffsetRange.NONE) {
//                    List<Fix> fixList = new ArrayList<Fix>(2);
//                    Node root = AstUtilities.getRoot(info);
//                    AstPath childPath = new AstPath(root, node); // TODO - make a simple clone method to clone AstPath path
//                    if (node.nodeId == NodeTypes.LOCALASGNNODE) {
//                        fixList.add(new RenameFix(info, childPath, RubyUtils.camelToUnderlinedName(name)));
//                    }
//                    fixList.add(new RenameFix(info, childPath, null));
//                    Description desc = new Description(this, displayName, info.getFileObject(), range, fixList, 1500);
//                    result.add(desc);
//                }
//                return;
//            }
//        }
    }

    public String getId() {
        return "UnicodeConvert"; // NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(UnicodeConvert.class, "UnicodeConvert");
    }

    public String getDescription() {
        return NbBundle.getMessage(UnicodeConvert.class, "UnicodeConvertDesc");
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
    
    private static class ConvertFix implements PreviewableFix {

        private CompilationInfo info;
        private int lexOffset;
        private char c;

        ConvertFix(CompilationInfo info, int offset, char c) {
            this.info = info;
            this.lexOffset = offset;
            this.c = c;
        }
        
        private String getConverted() {
            StringBuilder sb = new StringBuilder();
            java.util.Formatter formatter = new java.util.Formatter(sb);
            formatter.format("\\u%04x", (int)c);
            return sb.toString();
        }

        public String getDescription() {
            String converted = getConverted();
            return NbBundle.getMessage(UnicodeConvert.class, "UnicodeConvertFix", c, converted);
        }
        
        public boolean canPreview() {
            return true;
        }

        public EditList getEditList() throws Exception {
            BaseDocument doc = (BaseDocument) info.getDocument();
            EditList edits = new EditList(doc);
            edits.replace(lexOffset, 1, getConverted(), false, 0);
            
            return edits;
        }
        
        public void implement() throws Exception {
            EditList edits = getEditList();
            edits.apply();
        }

        public boolean isSafe() {
            return false;
        }

        public boolean isInteractive() {
            return true;
        }
    }
}
