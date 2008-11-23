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

import java.awt.Dialog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import org.mozilla.nb.javascript.Node;
import org.mozilla.nb.javascript.Token;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.csl.api.CompilationInfo;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.NameKind;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.javascript.editing.AstUtilities;
import org.netbeans.modules.javascript.editing.BrowserVersion;
import org.netbeans.modules.javascript.editing.ElementUtilities;
import org.netbeans.modules.javascript.editing.IndexedElement;
import org.netbeans.modules.javascript.editing.JsIndex;
import org.netbeans.modules.javascript.editing.JsTypeAnalyzer;
import org.netbeans.modules.javascript.editing.SupportedBrowsers;
import org.netbeans.modules.javascript.editing.lexer.JsTokenId;
import org.netbeans.modules.javascript.editing.lexer.LexUtilities;
import org.netbeans.modules.javascript.hints.infrastructure.JsAstRule;
import org.netbeans.modules.javascript.hints.infrastructure.JsRuleContext;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * Check for unsupported browser calls.
 * 
 * @author Tor Norbye
 */
public class UnsupportedCalls extends JsAstRule {
    public UnsupportedCalls() {
    }
    
    //private Map<String,Integer> STATISTICS = new HashMap<String,Integer>();

    public boolean appliesTo(RuleContext context) {
        return true;
    }

    public Set<Integer> getKinds() {
        HashSet<Integer> kinds = new HashSet<Integer>();
        kinds.add(Token.STRING);
        kinds.add(Token.NEW);
        return kinds;
    }
    
    public void run(JsRuleContext context, List<Hint> result) {
        Node node = context.node;
        
        if (node.getType() == Token.NEW) {
            // TODO - handle FQN's for constructors?
            // Don't need it yet
            Node first = node.getFirstChild();
            if (first != null && first.getType() == Token.NAME) {
                String name = first.getString();
                if (NAME_SET.contains(name)) {
                    processCall(context, result, node, name, first);
                }
            }
        } else {
            assert node.getType() == Token.STRING;
            Node parent = node.getParentNode();
            if (parent.getType() == Token.GETPROP) {
                Node grandParent = parent.getParentNode();
                if (grandParent != null && 
                        (grandParent.getType() == Token.CALL || grandParent.getType() == Token.NEW) &&
                        grandParent.getFirstChild() == parent) {
                    String name = node.getString();
                    Node callNode = grandParent;
                    if (NAME_SET.contains(name)) {
                        processCall(context, result, callNode, name, node);
                    }
                }
            }
        }
    }
    
    private void processCall(JsRuleContext context, List<Hint> result, Node callNode, String name, Node node) {
        //if (STATISTICS != null) {
        //    Integer count = STATISTICS.get(name);
        //    if (count == null) {
        //        count = Integer.valueOf(1);
        //    } else {
        //        count = Integer.valueOf(count.intValue()+1);
        //    }
        //    STATISTICS.put(name, count);
        //}                    

        // For names that are unique (no other functions of the same name) I don't have
        // to go to the trouble and expense of computing the expression type - if 
        // we see the name, we know the forbidden method is being called. To determine
        // if the function is 
        CompilationInfo info = context.compilationInfo;
        Boolean skipFqnCheck = MUST_CHECK_FQN.get(name);
        if (skipFqnCheck == null) {
            // Check index to see if 
            JsIndex index = JsIndex.get(info.getIndex(JsTokenId.JAVASCRIPT_MIME_TYPE));
            Set<IndexedElement> elements = index.getAllNames(name, NameKind.EXACT_NAME, JsIndex.ALL_SCOPE, null);
            if (elements.size() <= 1) {
                // Exactly one match, or no such known element - don't bother looking
                // up the fqn of calls, just assume this is the one
                skipFqnCheck = Boolean.TRUE;
            } else {
                int functionCount = 0;
                for (IndexedElement element : elements) {
                    if (element.getKind() == ElementKind.METHOD) {
                        functionCount++;
                        if (functionCount == 2) {
                            break;
                        }
                    }
                }
                if (functionCount <= 1) {
                    // There are other symbols of this name but only one is a function
                    // so we figure it's unique
                    skipFqnCheck = Boolean.TRUE;
                } else {
                    skipFqnCheck = Boolean.FALSE;
                }
            }

            MUST_CHECK_FQN.put(name, skipFqnCheck);
        }

        String fqn;
        if (skipFqnCheck == Boolean.FALSE) {
            // Do fuller check to see if this method is actually the one
            //String fqn = JsTypeAnalyzer.getCallFqn(info, callNode, false);
            fqn = JsTypeAnalyzer.getCallFqn(info, callNode, true);
            if (fqn == null) {
                return;
            }
            if (!COMPAT_MAP.containsKey(fqn)) {
                return;
            }
        } else {
            fqn = NAME_TO_FQN.get(name);
            if (fqn == null) {
                return;
            }
        }
        // Make sure this isn't one we've deliberately skipped
        if (getSkipMap().contains(fqn)) {
            return;
        }
        // Yessirree
        // TODO - figure out the real type
        EnumSet<BrowserVersion> compat = COMPAT_MAP.get(fqn);
        if (!SupportedBrowsers.getInstance().isSupported(compat)) {
            // Quickfix!
            OffsetRange astRange = AstUtilities.getRange(info, node);
            OffsetRange lexRange = LexUtilities.getLexerOffsets(info, astRange);
            if (lexRange == OffsetRange.NONE) {
                return;
            }

            List<HintFix> fixList = new ArrayList<HintFix>(3);
            fixList.add(new ShowDetails(info, fqn, compat));
            fixList.add(new SkipFunction(context, fqn));
            fixList.add(new ChangeTargetFix());
            String displayName = NbBundle.getMessage(UnsupportedCalls.class, "UnsupportedCallFqn", fqn);
            Hint desc = new Hint(this, displayName, info.getFileObject(), lexRange, fixList, 1450);
            result.add(desc);
        }
    }
    
    private Collection<String> skip = null;

    private Preferences getPreferences() {
        return NbPreferences.forModule(UnsupportedCalls.class).node("unsupportedCalls"); //NOI18N
    }
    
    private Collection<String> getSkipMap() {
        if (skip == null) {
            Preferences pref = getPreferences();
            if (pref != null) {
                try {
                    skip = Arrays.asList(pref.keys());
                } catch (BackingStoreException ex) {
                    Exceptions.printStackTrace(ex);
                    skip = new HashSet<String>();
                }
            }
        }
        
        return skip;
    }
    
    private void skip(String fqn) {
        getSkipMap();
        if (skip.contains(fqn)) {
            return;
        }
        
        getPreferences().putBoolean(fqn, true);
        skip = null;
    }

    public String getId() {
        return "UnsupportedCalls"; // NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(UnsupportedCalls.class, "UnsupportedCalls");
    }

    public String getDescription() {
        return NbBundle.getMessage(UnsupportedCalls.class, "UnsupportedCallsDesc");
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
    
    private class SkipFunction implements HintFix {
        private String fqn;
        private JsRuleContext context;
        
        SkipFunction(JsRuleContext context, String fqn) {
            this.context = context;
            this.fqn = fqn;
        }
        
        public String getDescription() {
            return NbBundle.getMessage(UnsupportedCalls.class, "SkipFunction", fqn);
        }
        
        public void implement() throws Exception {
            skip(fqn);

            // Trigger rescan
            context.manager.refreshHints(context);
        }

        public boolean isSafe() {
            return false;
        }

        public boolean isInteractive() {
            return true;
        }
    }

    private static class ChangeTargetFix implements HintFix {
        ChangeTargetFix() {
        }
        
        public String getDescription() {
            return NbBundle.getMessage(UnsupportedCalls.class, "ChangeBrowserTargets");
        }
        
        public void implement() throws Exception {
            OptionsDisplayer.getDefault().open("Advanced/JsOptions"); // NOI18N
        }

        public boolean isSafe() {
            return false;
        }

        public boolean isInteractive() {
            return true;
        }
    }

    private static class ShowDetails implements HintFix {
        private EnumSet<BrowserVersion> compat;
        private CompilationInfo info;
        private String fqn;
        
        ShowDetails(CompilationInfo info, String fqn, EnumSet<BrowserVersion> compat) {
            this.info = info;
            this.fqn = fqn;
            this.compat = compat;
        }
        
        public String getDescription() {
            return NbBundle.getMessage(UnsupportedCalls.class, "ShowUnsupportedDetails");
        }
        
        public void implement() throws Exception {
            int dot = fqn.lastIndexOf('.');
            String prefix;
            String type;
            if (dot != -1) {
                prefix = fqn.substring(dot+1);
                type = fqn.substring(0, dot);
            } else {
                prefix = "";
                type = fqn;
            }
            Set<IndexedElement> elements = JsIndex.get(info.getIndex(JsTokenId.JAVASCRIPT_MIME_TYPE)).getElements(prefix, type, NameKind.EXACT_NAME, JsIndex.ALL_SCOPE, null);
            String html;
            if (elements.size() > 0) {
                IndexedElement element = elements.iterator().next();
                String signature = ElementUtilities.getSignature(element);

                html = "<html><body>" + signature + "</body></html>"; // NOI18N
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("<p style=\"background:#ffcccc\">"); // NOI18N
                sb.append(NbBundle.getMessage(UnsupportedCalls.class, "NotSupportedBr"));
                sb.append("\n"); // NOI18N
                sb.append("<ul>"); // NOI18N
                for (BrowserVersion v : BrowserVersion.ALL) {
                    if (SupportedBrowsers.getInstance().isSupported(v) && 
                            !compat.contains(v)) {
                        sb.append("<li>"); // NOI18N
                        sb.append(v.getDisplayName());
                    }
                }
                sb.append("</ul>\n"); // NOI18N
                sb.append("\n"); // NOI18N
                sb.append("</p>"); // NOI18N
                html = "<html><body>" + sb.toString() + "</body></html>"; // NOI18N
            }

            JButton close =
                    new JButton(NbBundle.getMessage(UnsupportedCalls.class, "CTL_Close"));
            JLabel htmlLabel = new JLabel(html);
            htmlLabel.setBorder(new EmptyBorder(12, 12, 11, 11));
            DialogDescriptor descriptor =
                new DialogDescriptor(htmlLabel, fqn,
                    true, new Object[] { close }, close, DialogDescriptor.DEFAULT_ALIGN,
                    new HelpCtx(UnsupportedCalls.class), null);
            Dialog dlg = null;

            try {
                dlg = DialogDisplayer.getDefault().createDialog(descriptor);
                dlg.setVisible(true);
            } finally {
                if (dlg != null) {
                    dlg.dispose();
                }
            }
        }

        public boolean isSafe() {
            return false;
        }

        public boolean isInteractive() {
            return true;
        }
    }
    
    // TODO - add a way to customize this by user-configurable files
    // This code is automatically generated by #emitBrowserMaps()
    // in the javascript.generatestubs project in the misc repository.
    private static final Set<String> NAME_SET = new HashSet<String>(258);
    private static final Map<String,Boolean> MUST_CHECK_FQN = new HashMap<String,Boolean>(258);
    private static final Map<String,String> NAME_TO_FQN = new HashMap<String,String>(258);
    private static final Map<String,EnumSet<BrowserVersion>> COMPAT_MAP = new HashMap<String,EnumSet<BrowserVersion>>(258);
    private static final EnumSet<BrowserVersion> FF2FF3OPERASAFARI2SAFARI3KONQ = BrowserVersion.fromFlags("FF2|FF3|OPERA|SAFARI2|SAFARI3|KONQ"); // NOI18N
    private static final EnumSet<BrowserVersion> NOT_IE = BrowserVersion.fromFlags("FF1|FF2|FF3|OPERA|SAFARI2|SAFARI3|KONQ"); // NOI18N
    private static final EnumSet<BrowserVersion> NOT_IE55 = BrowserVersion.fromFlags("IE6|IE7|FF1|FF2|FF3|OPERA|SAFARI2|SAFARI3|KONQ"); // NOI18N
    private static final EnumSet<BrowserVersion> NOT_IE55_OR_6 = BrowserVersion.fromFlags("IE7|FF1|FF2|FF3|OPERA|SAFARI2|SAFARI3|KONQ"); // NOI18N
    private static final EnumSet<BrowserVersion> FIREFOX = BrowserVersion.fromFlags("FF1|FF2|FF3"); // NOI18N
    private static final EnumSet<BrowserVersion> FF3 = BrowserVersion.fromFlags("FF3"); // NOI18N
    private static final EnumSet<BrowserVersion> IE = BrowserVersion.fromFlags("IE55|IE6|IE7"); // NOI18N
    static {
        COMPAT_MAP.put("CanvasGradient.addColorStop", FF2FF3OPERASAFARI2SAFARI3KONQ); // NOI18N
        COMPAT_MAP.put("CanvasRenderingContext2D.arc", FF2FF3OPERASAFARI2SAFARI3KONQ); // NOI18N
        COMPAT_MAP.put("CanvasRenderingContext2D.arcTo", FF2FF3OPERASAFARI2SAFARI3KONQ); // NOI18N
        COMPAT_MAP.put("CanvasRenderingContext2D.beginPath", FF2FF3OPERASAFARI2SAFARI3KONQ); // NOI18N
        COMPAT_MAP.put("CanvasRenderingContext2D.bezierCurveTo", FF2FF3OPERASAFARI2SAFARI3KONQ); // NOI18N
        COMPAT_MAP.put("CanvasRenderingContext2D.clearRect", FF2FF3OPERASAFARI2SAFARI3KONQ); // NOI18N
        COMPAT_MAP.put("CanvasRenderingContext2D.clip", FF2FF3OPERASAFARI2SAFARI3KONQ); // NOI18N
        COMPAT_MAP.put("CanvasRenderingContext2D.closePath", FF2FF3OPERASAFARI2SAFARI3KONQ); // NOI18N
        COMPAT_MAP.put("CanvasRenderingContext2D.createImageData", FF2FF3OPERASAFARI2SAFARI3KONQ); // NOI18N
        COMPAT_MAP.put("CanvasRenderingContext2D.createLinearGradient", FF2FF3OPERASAFARI2SAFARI3KONQ); // NOI18N
        COMPAT_MAP.put("CanvasRenderingContext2D.createPattern", FF2FF3OPERASAFARI2SAFARI3KONQ); // NOI18N
        COMPAT_MAP.put("CanvasRenderingContext2D.createRadialGradient", FF2FF3OPERASAFARI2SAFARI3KONQ); // NOI18N
        COMPAT_MAP.put("CanvasRenderingContext2D.drawImage", FF2FF3OPERASAFARI2SAFARI3KONQ); // NOI18N
        COMPAT_MAP.put("CanvasRenderingContext2D.fill", FF2FF3OPERASAFARI2SAFARI3KONQ); // NOI18N
        COMPAT_MAP.put("CanvasRenderingContext2D.fillRect", FF2FF3OPERASAFARI2SAFARI3KONQ); // NOI18N
        COMPAT_MAP.put("CanvasRenderingContext2D.getImageData", FF2FF3OPERASAFARI2SAFARI3KONQ); // NOI18N
        COMPAT_MAP.put("CanvasRenderingContext2D.isPointInPath", FF2FF3OPERASAFARI2SAFARI3KONQ); // NOI18N
        COMPAT_MAP.put("CanvasRenderingContext2D.lineTo", FF2FF3OPERASAFARI2SAFARI3KONQ); // NOI18N
        COMPAT_MAP.put("CanvasRenderingContext2D.load", FF2FF3OPERASAFARI2SAFARI3KONQ); // NOI18N
        COMPAT_MAP.put("CanvasRenderingContext2D.moveTo", FF2FF3OPERASAFARI2SAFARI3KONQ); // NOI18N
        COMPAT_MAP.put("CanvasRenderingContext2D.putImageData", FF2FF3OPERASAFARI2SAFARI3KONQ); // NOI18N
        COMPAT_MAP.put("CanvasRenderingContext2D.quadraticCurveTo", FF2FF3OPERASAFARI2SAFARI3KONQ); // NOI18N
        COMPAT_MAP.put("CanvasRenderingContext2D.rect", FF2FF3OPERASAFARI2SAFARI3KONQ); // NOI18N
        COMPAT_MAP.put("CanvasRenderingContext2D.rotate", FF2FF3OPERASAFARI2SAFARI3KONQ); // NOI18N
        COMPAT_MAP.put("CanvasRenderingContext2D.save", FF2FF3OPERASAFARI2SAFARI3KONQ); // NOI18N
        COMPAT_MAP.put("CanvasRenderingContext2D.scale", FF2FF3OPERASAFARI2SAFARI3KONQ); // NOI18N
        COMPAT_MAP.put("CanvasRenderingContext2D.setTransform", FF2FF3OPERASAFARI2SAFARI3KONQ); // NOI18N
        COMPAT_MAP.put("CanvasRenderingContext2D.stroke", FF2FF3OPERASAFARI2SAFARI3KONQ); // NOI18N
        COMPAT_MAP.put("CanvasRenderingContext2D.strokeRect", FF2FF3OPERASAFARI2SAFARI3KONQ); // NOI18N
        COMPAT_MAP.put("CanvasRenderingContext2D.transform", FF2FF3OPERASAFARI2SAFARI3KONQ); // NOI18N
        COMPAT_MAP.put("CanvasRenderingContext2D.translate", FF2FF3OPERASAFARI2SAFARI3KONQ); // NOI18N
        COMPAT_MAP.put("Document.adoptNode", NOT_IE55_OR_6); // NOI18N
        COMPAT_MAP.put("Document.createAttributeNS", NOT_IE55_OR_6); // NOI18N
        COMPAT_MAP.put("Document.createDocumentFragment", NOT_IE55); // NOI18N
        COMPAT_MAP.put("Document.createElementNS", NOT_IE55_OR_6); // NOI18N
        COMPAT_MAP.put("Document.getElementsByTagNameNS", NOT_IE55_OR_6); // NOI18N
        COMPAT_MAP.put("Document.implementation.createDocument", NOT_IE); // NOI18N
        COMPAT_MAP.put("Document.implementation.hasFeature", NOT_IE55); // NOI18N
        COMPAT_MAP.put("Document.importNode", NOT_IE55_OR_6); // NOI18N
        COMPAT_MAP.put("Document.normalizeDocument", NOT_IE55_OR_6); // NOI18N
        COMPAT_MAP.put("Document.renameNode", NOT_IE55_OR_6); // NOI18N
        COMPAT_MAP.put("DocumentRange.createRange", NOT_IE); // NOI18N
        COMPAT_MAP.put("DocumentTraversal.createNodeIterator", NOT_IE); // NOI18N
        COMPAT_MAP.put("DocumentTraversal.createTreeWalker", NOT_IE); // NOI18N
        COMPAT_MAP.put("Element.applyElement", IE); // NOI18N
        COMPAT_MAP.put("Element.contains", NOT_IE); // NOI18N
        COMPAT_MAP.put("Element.createAttribute", NOT_IE55); // NOI18N
        COMPAT_MAP.put("Element.getAttributeNS", NOT_IE55_OR_6); // NOI18N
        COMPAT_MAP.put("Element.getAttributeNode", NOT_IE55); // NOI18N
        COMPAT_MAP.put("Element.getAttributeNodeNS", NOT_IE55_OR_6); // NOI18N
        COMPAT_MAP.put("Element.getBoundingClientRect", FF3); // NOI18N
        COMPAT_MAP.put("Element.getClientRects", FF3); // NOI18N
        COMPAT_MAP.put("Element.getElementsByTagNameNS", NOT_IE55_OR_6); // NOI18N
// This one is overridden in prototype... Element.hasAttribute may be referring to it!
// For now, don't warn about this one.
//        COMPAT_MAP.put("Element.hasAttribute", NOT_IE); // NOI18N
        COMPAT_MAP.put("Element.hasAttributeNS", NOT_IE55_OR_6); // NOI18N
        COMPAT_MAP.put("Element.hasAttributes", NOT_IE); // NOI18N
        COMPAT_MAP.put("Element.removeAttributeNS", NOT_IE55_OR_6); // NOI18N
        COMPAT_MAP.put("Element.removeAttributeNode", NOT_IE55_OR_6); // NOI18N
        COMPAT_MAP.put("Element.setAttributeNS", NOT_IE55_OR_6); // NOI18N
        COMPAT_MAP.put("Element.setAttributeNode", NOT_IE55); // NOI18N
        COMPAT_MAP.put("Element.setAttributeNodeNS", NOT_IE55_OR_6); // NOI18N
        COMPAT_MAP.put("Element.setIdAttribute", NOT_IE55_OR_6); // NOI18N
        COMPAT_MAP.put("Element.setIdAttributeNS", NOT_IE55_OR_6); // NOI18N
        COMPAT_MAP.put("Element.setIdAttributeNode", NOT_IE55_OR_6); // NOI18N
        COMPAT_MAP.put("HTMLCanvasElement.getContext", FF2FF3OPERASAFARI2SAFARI3KONQ); // NOI18N
        COMPAT_MAP.put("HTMLCanvasElement.toDataURL", FF2FF3OPERASAFARI2SAFARI3KONQ); // NOI18N
        COMPAT_MAP.put("NameList.contains", NOT_IE55_OR_6); // NOI18N
        COMPAT_MAP.put("NameList.containsNS", NOT_IE55_OR_6); // NOI18N
        COMPAT_MAP.put("NameList.getName", NOT_IE55_OR_6); // NOI18N
        COMPAT_MAP.put("NameList.getNamespaceURI", NOT_IE55_OR_6); // NOI18N
        COMPAT_MAP.put("NamedNodeMap.getNamedItemNS", NOT_IE55_OR_6); // NOI18N
        COMPAT_MAP.put("NamedNodeMap.removeNamedItemNS", NOT_IE55_OR_6); // NOI18N
        COMPAT_MAP.put("NamedNodeMap.setNamedItemNS", NOT_IE55_OR_6); // NOI18N
        COMPAT_MAP.put("Node.appendData", NOT_IE55); // NOI18N
        COMPAT_MAP.put("Node.clearAttributes", IE); // NOI18N
        COMPAT_MAP.put("Node.compareDocumentPosition", NOT_IE55_OR_6); // NOI18N
        COMPAT_MAP.put("Node.getFeature", NOT_IE55_OR_6); // NOI18N
        COMPAT_MAP.put("Node.getUserData", NOT_IE55_OR_6); // NOI18N
        COMPAT_MAP.put("Node.hasAttributes", NOT_IE55_OR_6); // NOI18N
        COMPAT_MAP.put("Node.insertData", NOT_IE55); // NOI18N
        COMPAT_MAP.put("Node.isDefaultNamespace", NOT_IE55_OR_6); // NOI18N
        COMPAT_MAP.put("Node.isEqualNode", NOT_IE55_OR_6); // NOI18N
        COMPAT_MAP.put("Node.isSameNode", NOT_IE55_OR_6); // NOI18N
        COMPAT_MAP.put("Node.isSupported", NOT_IE55_OR_6); // NOI18N
        COMPAT_MAP.put("Node.lookupNamespaceURI", NOT_IE55_OR_6); // NOI18N
        COMPAT_MAP.put("Node.lookupPrefix", NOT_IE55_OR_6); // NOI18N
        COMPAT_MAP.put("Node.mergeAttributes", IE); // NOI18N
        COMPAT_MAP.put("Node.normalize", NOT_IE55_OR_6); // NOI18N
        COMPAT_MAP.put("Node.removeNode", IE); // NOI18N
        COMPAT_MAP.put("Node.replaceData", NOT_IE55); // NOI18N
        COMPAT_MAP.put("Node.replaceNode", IE); // NOI18N
        COMPAT_MAP.put("Node.setUserData", NOT_IE55_OR_6); // NOI18N
        COMPAT_MAP.put("Node.substringData", NOT_IE55); // NOI18N
        COMPAT_MAP.put("Node.swapNode", IE); // NOI18N
        COMPAT_MAP.put("NodeIterator.detach", NOT_IE); // NOI18N
        COMPAT_MAP.put("NodeIterator.nextNode", NOT_IE); // NOI18N
        COMPAT_MAP.put("NodeIterator.previousNode", NOT_IE); // NOI18N
        COMPAT_MAP.put("Range.cloneContents", NOT_IE); // NOI18N
        COMPAT_MAP.put("Range.cloneRange", NOT_IE); // NOI18N
        COMPAT_MAP.put("Range.collapse", NOT_IE); // NOI18N
        COMPAT_MAP.put("Range.compareBoundaryPoints", NOT_IE); // NOI18N
        COMPAT_MAP.put("Range.compareNode", FIREFOX); // NOI18N
        COMPAT_MAP.put("Range.comparePoint", FIREFOX); // NOI18N
        COMPAT_MAP.put("Range.createContextualFragment", FIREFOX); // NOI18N
        COMPAT_MAP.put("Range.deleteContents", NOT_IE); // NOI18N
        COMPAT_MAP.put("Range.detach", NOT_IE); // NOI18N
        COMPAT_MAP.put("Range.extractContents", NOT_IE); // NOI18N
        COMPAT_MAP.put("Range.insertNode", NOT_IE); // NOI18N
        COMPAT_MAP.put("Range.intersectsNode", FIREFOX); // NOI18N
        COMPAT_MAP.put("Range.isPointInRange", FIREFOX); // NOI18N
        COMPAT_MAP.put("Range.selectNode", NOT_IE); // NOI18N
        COMPAT_MAP.put("Range.selectNodeContents", NOT_IE); // NOI18N
        COMPAT_MAP.put("Range.setEnd", NOT_IE); // NOI18N
        COMPAT_MAP.put("Range.setEndAfter", NOT_IE); // NOI18N
        COMPAT_MAP.put("Range.setEndBefore", NOT_IE); // NOI18N
        COMPAT_MAP.put("Range.setStart", NOT_IE); // NOI18N
        COMPAT_MAP.put("Range.setStartAfter", NOT_IE); // NOI18N
        COMPAT_MAP.put("Range.setStartBefore", NOT_IE); // NOI18N
        COMPAT_MAP.put("Range.surroundContents", NOT_IE); // NOI18N
        COMPAT_MAP.put("Text.replaceWholeText", NOT_IE55_OR_6); // NOI18N
        COMPAT_MAP.put("TreeWalker.firstChild", NOT_IE); // NOI18N
        COMPAT_MAP.put("TreeWalker.lastChild", NOT_IE); // NOI18N
        COMPAT_MAP.put("TreeWalker.nextNode", NOT_IE); // NOI18N
        COMPAT_MAP.put("TreeWalker.nextSibling", NOT_IE); // NOI18N
        COMPAT_MAP.put("TreeWalker.parentNode", NOT_IE); // NOI18N
        COMPAT_MAP.put("TreeWalker.previousNode", NOT_IE); // NOI18N
        COMPAT_MAP.put("TreeWalker.previousSibling", NOT_IE); // NOI18N
        COMPAT_MAP.put("TypeInfo.isDerivedFrom", NOT_IE55_OR_6); // NOI18N
        COMPAT_MAP.put("XMLHttpRequest", NOT_IE55_OR_6); // NOI18N

        NAME_SET.add("XMLHttpRequest"); // NOI18N
        NAME_SET.add("addColorStop"); // NOI18N
        NAME_SET.add("adoptNode"); // NOI18N
        NAME_SET.add("appendData"); // NOI18N
        NAME_SET.add("applyElement"); // NOI18N
        NAME_SET.add("arc"); // NOI18N
        NAME_SET.add("arcTo"); // NOI18N
        NAME_SET.add("beginPath"); // NOI18N
        NAME_SET.add("bezierCurveTo"); // NOI18N
        NAME_SET.add("clearAttributes"); // NOI18N
        NAME_SET.add("clearRect"); // NOI18N
        NAME_SET.add("clip"); // NOI18N
        NAME_SET.add("cloneContents"); // NOI18N
        NAME_SET.add("cloneRange"); // NOI18N
        NAME_SET.add("closePath"); // NOI18N
        NAME_SET.add("collapse"); // NOI18N
        NAME_SET.add("compareBoundaryPoints"); // NOI18N
        NAME_SET.add("compareDocumentPosition"); // NOI18N
        NAME_SET.add("compareNode"); // NOI18N
        NAME_SET.add("comparePoint"); // NOI18N
        NAME_SET.add("contains"); // NOI18N
        NAME_SET.add("containsNS"); // NOI18N
        NAME_SET.add("createAttribute"); // NOI18N
        NAME_SET.add("createAttributeNS"); // NOI18N
        NAME_SET.add("createContextualFragment"); // NOI18N
        NAME_SET.add("createDocument"); // NOI18N
        NAME_SET.add("createDocumentFragment"); // NOI18N
        NAME_SET.add("createElementNS"); // NOI18N
        NAME_SET.add("createImageData"); // NOI18N
        NAME_SET.add("createLinearGradient"); // NOI18N
        NAME_SET.add("createNodeIterator"); // NOI18N
        NAME_SET.add("createPattern"); // NOI18N
        NAME_SET.add("createRadialGradient"); // NOI18N
        NAME_SET.add("createRange"); // NOI18N
        NAME_SET.add("createTreeWalker"); // NOI18N
        NAME_SET.add("deleteContents"); // NOI18N
        NAME_SET.add("detach"); // NOI18N
        NAME_SET.add("drawImage"); // NOI18N
        NAME_SET.add("extractContents"); // NOI18N
        NAME_SET.add("fill"); // NOI18N
        NAME_SET.add("fillRect"); // NOI18N
        NAME_SET.add("firstChild"); // NOI18N
        NAME_SET.add("getAttributeNS"); // NOI18N
        NAME_SET.add("getAttributeNode"); // NOI18N
        NAME_SET.add("getAttributeNodeNS"); // NOI18N
        NAME_SET.add("getBoundingClientRect"); // NOI18N
        NAME_SET.add("getClientRects"); // NOI18N
        NAME_SET.add("getContext"); // NOI18N
        NAME_SET.add("getElementsByTagNameNS"); // NOI18N
        NAME_SET.add("getFeature"); // NOI18N
        NAME_SET.add("getImageData"); // NOI18N
        NAME_SET.add("getName"); // NOI18N
        NAME_SET.add("getNamedItemNS"); // NOI18N
        NAME_SET.add("getNamespaceURI"); // NOI18N
        NAME_SET.add("getUserData"); // NOI18N
//        NAME_SET.add("hasAttribute"); // NOI18N
        NAME_SET.add("hasAttributeNS"); // NOI18N
        NAME_SET.add("hasAttributes"); // NOI18N
        NAME_SET.add("hasFeature"); // NOI18N
        NAME_SET.add("importNode"); // NOI18N
        NAME_SET.add("insertData"); // NOI18N
        NAME_SET.add("insertNode"); // NOI18N
        NAME_SET.add("intersectsNode"); // NOI18N
        NAME_SET.add("isDefaultNamespace"); // NOI18N
        NAME_SET.add("isDerivedFrom"); // NOI18N
        NAME_SET.add("isEqualNode"); // NOI18N
        NAME_SET.add("isPointInPath"); // NOI18N
        NAME_SET.add("isPointInRange"); // NOI18N
        NAME_SET.add("isSameNode"); // NOI18N
        NAME_SET.add("isSupported"); // NOI18N
        NAME_SET.add("lastChild"); // NOI18N
        NAME_SET.add("lineTo"); // NOI18N
        NAME_SET.add("load"); // NOI18N
        NAME_SET.add("lookupNamespaceURI"); // NOI18N
        NAME_SET.add("lookupPrefix"); // NOI18N
        NAME_SET.add("mergeAttributes"); // NOI18N
        NAME_SET.add("moveTo"); // NOI18N
        NAME_SET.add("nextNode"); // NOI18N
        NAME_SET.add("nextSibling"); // NOI18N
        NAME_SET.add("normalize"); // NOI18N
        NAME_SET.add("normalizeDocument"); // NOI18N
        NAME_SET.add("parentNode"); // NOI18N
        NAME_SET.add("previousNode"); // NOI18N
        NAME_SET.add("previousSibling"); // NOI18N
        NAME_SET.add("putImageData"); // NOI18N
        NAME_SET.add("quadraticCurveTo"); // NOI18N
        NAME_SET.add("rect"); // NOI18N
        NAME_SET.add("removeAttributeNS"); // NOI18N
        NAME_SET.add("removeAttributeNode"); // NOI18N
        NAME_SET.add("removeNamedItemNS"); // NOI18N
        NAME_SET.add("removeNode"); // NOI18N
        NAME_SET.add("renameNode"); // NOI18N
        NAME_SET.add("replaceData"); // NOI18N
        NAME_SET.add("replaceNode"); // NOI18N
        NAME_SET.add("replaceWholeText"); // NOI18N
        NAME_SET.add("rotate"); // NOI18N
        NAME_SET.add("save"); // NOI18N
        NAME_SET.add("scale"); // NOI18N
        NAME_SET.add("selectNode"); // NOI18N
        NAME_SET.add("selectNodeContents"); // NOI18N
        NAME_SET.add("setAttributeNS"); // NOI18N
        NAME_SET.add("setAttributeNode"); // NOI18N
        NAME_SET.add("setAttributeNodeNS"); // NOI18N
        NAME_SET.add("setEnd"); // NOI18N
        NAME_SET.add("setEndAfter"); // NOI18N
        NAME_SET.add("setEndBefore"); // NOI18N
        NAME_SET.add("setIdAttribute"); // NOI18N
        NAME_SET.add("setIdAttributeNS"); // NOI18N
        NAME_SET.add("setIdAttributeNode"); // NOI18N
        NAME_SET.add("setNamedItemNS"); // NOI18N
        NAME_SET.add("setStart"); // NOI18N
        NAME_SET.add("setStartAfter"); // NOI18N
        NAME_SET.add("setStartBefore"); // NOI18N
        NAME_SET.add("setTransform"); // NOI18N
        NAME_SET.add("setUserData"); // NOI18N
        NAME_SET.add("stroke"); // NOI18N
        NAME_SET.add("strokeRect"); // NOI18N
        NAME_SET.add("substringData"); // NOI18N
        NAME_SET.add("surroundContents"); // NOI18N
        NAME_SET.add("swapNode"); // NOI18N
        NAME_SET.add("toDataURL"); // NOI18N
        NAME_SET.add("transform"); // NOI18N
        NAME_SET.add("translate"); // NOI18N

        NAME_TO_FQN.put("XMLHttpRequest", "XMLHttpRequest"); // NOI18N
        NAME_TO_FQN.put("addColorStop", "CanvasGradient.addColorStop"); // NOI18N
        NAME_TO_FQN.put("adoptNode", "Document.adoptNode"); // NOI18N
        NAME_TO_FQN.put("appendData", "Node.appendData"); // NOI18N
        NAME_TO_FQN.put("applyElement", "Element.applyElement"); // NOI18N
        NAME_TO_FQN.put("arc", "CanvasRenderingContext2D.arc"); // NOI18N
        NAME_TO_FQN.put("arcTo", "CanvasRenderingContext2D.arcTo"); // NOI18N
        NAME_TO_FQN.put("beginPath", "CanvasRenderingContext2D.beginPath"); // NOI18N
        NAME_TO_FQN.put("bezierCurveTo", "CanvasRenderingContext2D.bezierCurveTo"); // NOI18N
        NAME_TO_FQN.put("clearAttributes", "Node.clearAttributes"); // NOI18N
        NAME_TO_FQN.put("clearRect", "CanvasRenderingContext2D.clearRect"); // NOI18N
        NAME_TO_FQN.put("clip", "CanvasRenderingContext2D.clip"); // NOI18N
        NAME_TO_FQN.put("cloneContents", "Range.cloneContents"); // NOI18N
        NAME_TO_FQN.put("cloneRange", "Range.cloneRange"); // NOI18N
        NAME_TO_FQN.put("closePath", "CanvasRenderingContext2D.closePath"); // NOI18N
        NAME_TO_FQN.put("collapse", "Range.collapse"); // NOI18N
        NAME_TO_FQN.put("compareBoundaryPoints", "Range.compareBoundaryPoints"); // NOI18N
        NAME_TO_FQN.put("compareDocumentPosition", "Node.compareDocumentPosition"); // NOI18N
        NAME_TO_FQN.put("compareNode", "Range.compareNode"); // NOI18N
        NAME_TO_FQN.put("comparePoint", "Range.comparePoint"); // NOI18N
        NAME_TO_FQN.put("containsNS", "NameList.containsNS"); // NOI18N
        NAME_TO_FQN.put("createAttribute", "Element.createAttribute"); // NOI18N
        NAME_TO_FQN.put("createAttributeNS", "Document.createAttributeNS"); // NOI18N
        NAME_TO_FQN.put("createContextualFragment", "Range.createContextualFragment"); // NOI18N
        NAME_TO_FQN.put("createDocument", "Document.implementation.createDocument"); // NOI18N
        NAME_TO_FQN.put("createDocumentFragment", "Document.createDocumentFragment"); // NOI18N
        NAME_TO_FQN.put("createElementNS", "Document.createElementNS"); // NOI18N
        NAME_TO_FQN.put("createImageData", "CanvasRenderingContext2D.createImageData"); // NOI18N
        NAME_TO_FQN.put("createLinearGradient", "CanvasRenderingContext2D.createLinearGradient"); // NOI18N
        NAME_TO_FQN.put("createNodeIterator", "DocumentTraversal.createNodeIterator"); // NOI18N
        NAME_TO_FQN.put("createPattern", "CanvasRenderingContext2D.createPattern"); // NOI18N
        NAME_TO_FQN.put("createRadialGradient", "CanvasRenderingContext2D.createRadialGradient"); // NOI18N
        NAME_TO_FQN.put("createRange", "DocumentRange.createRange"); // NOI18N
        NAME_TO_FQN.put("createTreeWalker", "DocumentTraversal.createTreeWalker"); // NOI18N
        NAME_TO_FQN.put("deleteContents", "Range.deleteContents"); // NOI18N
        NAME_TO_FQN.put("drawImage", "CanvasRenderingContext2D.drawImage"); // NOI18N
        NAME_TO_FQN.put("extractContents", "Range.extractContents"); // NOI18N
        NAME_TO_FQN.put("fill", "CanvasRenderingContext2D.fill"); // NOI18N
        NAME_TO_FQN.put("fillRect", "CanvasRenderingContext2D.fillRect"); // NOI18N
        NAME_TO_FQN.put("firstChild", "TreeWalker.firstChild"); // NOI18N
        NAME_TO_FQN.put("getAttributeNS", "Element.getAttributeNS"); // NOI18N
        NAME_TO_FQN.put("getAttributeNode", "Element.getAttributeNode"); // NOI18N
        NAME_TO_FQN.put("getAttributeNodeNS", "Element.getAttributeNodeNS"); // NOI18N
        NAME_TO_FQN.put("getBoundingClientRect", "Element.getBoundingClientRect"); // NOI18N
        NAME_TO_FQN.put("getClientRects", "Element.getClientRects"); // NOI18N
        NAME_TO_FQN.put("getContext", "HTMLCanvasElement.getContext"); // NOI18N
        NAME_TO_FQN.put("getFeature", "Node.getFeature"); // NOI18N
        NAME_TO_FQN.put("getImageData", "CanvasRenderingContext2D.getImageData"); // NOI18N
        NAME_TO_FQN.put("getName", "NameList.getName"); // NOI18N
        NAME_TO_FQN.put("getNamedItemNS", "NamedNodeMap.getNamedItemNS"); // NOI18N
        NAME_TO_FQN.put("getNamespaceURI", "NameList.getNamespaceURI"); // NOI18N
        NAME_TO_FQN.put("getUserData", "Node.getUserData"); // NOI18N
//        NAME_TO_FQN.put("hasAttribute", "Element.hasAttribute"); // NOI18N
        NAME_TO_FQN.put("hasAttributeNS", "Element.hasAttributeNS"); // NOI18N
        NAME_TO_FQN.put("hasFeature", "Document.implementation.hasFeature"); // NOI18N
        NAME_TO_FQN.put("importNode", "Document.importNode"); // NOI18N
        NAME_TO_FQN.put("insertData", "Node.insertData"); // NOI18N
        NAME_TO_FQN.put("insertNode", "Range.insertNode"); // NOI18N
        NAME_TO_FQN.put("intersectsNode", "Range.intersectsNode"); // NOI18N
        NAME_TO_FQN.put("isDefaultNamespace", "Node.isDefaultNamespace"); // NOI18N
        NAME_TO_FQN.put("isDerivedFrom", "TypeInfo.isDerivedFrom"); // NOI18N
        NAME_TO_FQN.put("isEqualNode", "Node.isEqualNode"); // NOI18N
        NAME_TO_FQN.put("isPointInPath", "CanvasRenderingContext2D.isPointInPath"); // NOI18N
        NAME_TO_FQN.put("isPointInRange", "Range.isPointInRange"); // NOI18N
        NAME_TO_FQN.put("isSameNode", "Node.isSameNode"); // NOI18N
        NAME_TO_FQN.put("isSupported", "Node.isSupported"); // NOI18N
        NAME_TO_FQN.put("lastChild", "TreeWalker.lastChild"); // NOI18N
        NAME_TO_FQN.put("lineTo", "CanvasRenderingContext2D.lineTo"); // NOI18N
        NAME_TO_FQN.put("load", "CanvasRenderingContext2D.load"); // NOI18N
        NAME_TO_FQN.put("lookupNamespaceURI", "Node.lookupNamespaceURI"); // NOI18N
        NAME_TO_FQN.put("lookupPrefix", "Node.lookupPrefix"); // NOI18N
        NAME_TO_FQN.put("mergeAttributes", "Node.mergeAttributes"); // NOI18N
        NAME_TO_FQN.put("moveTo", "CanvasRenderingContext2D.moveTo"); // NOI18N
        NAME_TO_FQN.put("nextSibling", "TreeWalker.nextSibling"); // NOI18N
        NAME_TO_FQN.put("normalize", "Node.normalize"); // NOI18N
        NAME_TO_FQN.put("normalizeDocument", "Document.normalizeDocument"); // NOI18N
        NAME_TO_FQN.put("parentNode", "TreeWalker.parentNode"); // NOI18N
        NAME_TO_FQN.put("previousSibling", "TreeWalker.previousSibling"); // NOI18N
        NAME_TO_FQN.put("putImageData", "CanvasRenderingContext2D.putImageData"); // NOI18N
        NAME_TO_FQN.put("quadraticCurveTo", "CanvasRenderingContext2D.quadraticCurveTo"); // NOI18N
        NAME_TO_FQN.put("rect", "CanvasRenderingContext2D.rect"); // NOI18N
        NAME_TO_FQN.put("removeAttributeNS", "Element.removeAttributeNS"); // NOI18N
        NAME_TO_FQN.put("removeAttributeNode", "Element.removeAttributeNode"); // NOI18N
        NAME_TO_FQN.put("removeNamedItemNS", "NamedNodeMap.removeNamedItemNS"); // NOI18N
        NAME_TO_FQN.put("removeNode", "Node.removeNode"); // NOI18N
        NAME_TO_FQN.put("renameNode", "Document.renameNode"); // NOI18N
        NAME_TO_FQN.put("replaceData", "Node.replaceData"); // NOI18N
        NAME_TO_FQN.put("replaceNode", "Node.replaceNode"); // NOI18N
        NAME_TO_FQN.put("replaceWholeText", "Text.replaceWholeText"); // NOI18N
        NAME_TO_FQN.put("rotate", "CanvasRenderingContext2D.rotate"); // NOI18N
        NAME_TO_FQN.put("save", "CanvasRenderingContext2D.save"); // NOI18N
        NAME_TO_FQN.put("scale", "CanvasRenderingContext2D.scale"); // NOI18N
        NAME_TO_FQN.put("selectNode", "Range.selectNode"); // NOI18N
        NAME_TO_FQN.put("selectNodeContents", "Range.selectNodeContents"); // NOI18N
        NAME_TO_FQN.put("setAttributeNS", "Element.setAttributeNS"); // NOI18N
        NAME_TO_FQN.put("setAttributeNode", "Element.setAttributeNode"); // NOI18N
        NAME_TO_FQN.put("setAttributeNodeNS", "Element.setAttributeNodeNS"); // NOI18N
        NAME_TO_FQN.put("setEnd", "Range.setEnd"); // NOI18N
        NAME_TO_FQN.put("setEndAfter", "Range.setEndAfter"); // NOI18N
        NAME_TO_FQN.put("setEndBefore", "Range.setEndBefore"); // NOI18N
        NAME_TO_FQN.put("setIdAttribute", "Element.setIdAttribute"); // NOI18N
        NAME_TO_FQN.put("setIdAttributeNS", "Element.setIdAttributeNS"); // NOI18N
        NAME_TO_FQN.put("setIdAttributeNode", "Element.setIdAttributeNode"); // NOI18N
        NAME_TO_FQN.put("setNamedItemNS", "NamedNodeMap.setNamedItemNS"); // NOI18N
        NAME_TO_FQN.put("setStart", "Range.setStart"); // NOI18N
        NAME_TO_FQN.put("setStartAfter", "Range.setStartAfter"); // NOI18N
        NAME_TO_FQN.put("setStartBefore", "Range.setStartBefore"); // NOI18N
        NAME_TO_FQN.put("setTransform", "CanvasRenderingContext2D.setTransform"); // NOI18N
        NAME_TO_FQN.put("setUserData", "Node.setUserData"); // NOI18N
        NAME_TO_FQN.put("stroke", "CanvasRenderingContext2D.stroke"); // NOI18N
        NAME_TO_FQN.put("strokeRect", "CanvasRenderingContext2D.strokeRect"); // NOI18N
        NAME_TO_FQN.put("substringData", "Node.substringData"); // NOI18N
        NAME_TO_FQN.put("surroundContents", "Range.surroundContents"); // NOI18N
        NAME_TO_FQN.put("swapNode", "Node.swapNode"); // NOI18N
        NAME_TO_FQN.put("toDataURL", "HTMLCanvasElement.toDataURL"); // NOI18N
        NAME_TO_FQN.put("transform", "CanvasRenderingContext2D.transform"); // NOI18N
        NAME_TO_FQN.put("translate", "CanvasRenderingContext2D.translate"); // NOI18N

        // Initialize items we know we can't search by duplicate
        MUST_CHECK_FQN.put("TreeWalker.previousNode", Boolean.FALSE); // NOI18N
        MUST_CHECK_FQN.put("TreeWalker.nextNode", Boolean.FALSE); // NOI18N
        MUST_CHECK_FQN.put("Range.detach", Boolean.FALSE); // NOI18N
        MUST_CHECK_FQN.put("Document.getElementsByTagNameNS", Boolean.FALSE); // NOI18N
        MUST_CHECK_FQN.put("Element.hasAttributes", Boolean.FALSE); // NOI18N
        MUST_CHECK_FQN.put("NameList.contains", Boolean.FALSE); // NOI18N
    }
}
