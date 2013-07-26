/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.debugger.jpda.projects;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;

import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.text.Annotation;
import org.openide.text.DataEditorSupport;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.text.Line.Part;
import org.openide.util.RequestProcessor;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.This;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.PopupManager;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ToolTipSupport;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.spi.debugger.jpda.EditorContext.Operation;

import org.netbeans.spi.debugger.ui.EditorContextDispatcher;


public class ToolTipAnnotation extends Annotation implements Runnable {

    private static final Set<String> JAVA_KEYWORDS = new HashSet<String>(Arrays.asList(new String[] {
        "abstract",     "continue",     "for",          "new",  	"switch",
        "assert", 	"default", 	"goto", 	"package", 	"synchronized",
        "boolean", 	"do",           "if",           "private", 	/*"this",*/
        "break",        "double", 	"implements", 	"protected", 	"throw",
        "byte",         "else", 	"import", 	"public", 	"throws",
        "case",         "enum", 	"instanceof", 	"return", 	"transient",
        "catch",        "extends", 	"int",          "short", 	"try",
        "char",         "final", 	"interface", 	"static", 	"void",
        /*"class",*/    "finally", 	"long", 	"strictfp", 	"volatile",
        "const",        "float", 	"native", 	"super", 	"while",
    }));
    private static final int MAX_TOOLTIP_TEXT = 100000;

    private Part lp;
    private EditorCookie ec;

    @Override
    public String getShortDescription () {
        // [TODO] hack for org.netbeans.modules.debugger.jpda.actions.MethodChooser that disables tooltips
        if ("true".equals(System.getProperty("org.netbeans.modules.debugger.jpda.doNotShowTooltips"))) { // NOI18N
            return null;
        }
        DebuggerEngine currentEngine = DebuggerManager.getDebuggerManager ().
            getCurrentEngine ();
        if (currentEngine == null) {
            return null;
        }
        JPDADebugger d = currentEngine.lookupFirst(null, JPDADebugger.class);
        if (d == null) {
            return null;
        }

        Part lp = (Part) getAttachedAnnotatable();
        if (lp == null) {
            return null;
        }
        Line line = lp.getLine ();
        DataObject dob = DataEditorSupport.findDataObject (line);
        if (dob == null) {
            return null;
        }
        EditorCookie ec = dob.getLookup().lookup(EditorCookie.class);
        if (ec == null) {
            return null;
            // Only for editable dataobjects
        }

        this.lp = lp;
        this.ec = ec;
        RequestProcessor rp = currentEngine.lookupFirst(null, RequestProcessor.class);
        if (rp == null) {
            // Debugger is likely finishing...
            rp = RequestProcessor.getDefault();
        }
        rp.post (this);
        return null;
    }

    @Override
    public void run () {
        ObjectVariable tooltipVariable = null;
        if (lp == null || ec == null) {
            return ;
        }
        StyledDocument doc;
        try {
            doc = ec.openDocument();
        } catch (IOException ex) {
            return ;
        }
        final JEditorPane ep = EditorContextDispatcher.getDefault().getMostRecentEditor ();
        if (ep == null || ep.getDocument() != doc) {
            return ;
        }
        DebuggerEngine currentEngine = DebuggerManager.getDebuggerManager ().
            getCurrentEngine ();
        if (currentEngine == null) {
            return;
        }
        final JPDADebugger d = currentEngine.lookupFirst(null, JPDADebugger.class);
        if (d == null) {
            return;
        }
        JPDAThread t = d.getCurrentThread();
        if (t == null || !t.isSuspended()) {
            return;
        }

        int offset;
        boolean[] isMethodPtr = new boolean[] { false };
        final String expression = getIdentifier (
            d,
            doc,
            ep,
            offset = NbDocument.findLineOffset (
                doc,
                lp.getLine ().getLineNumber ()
            ) + lp.getColumn (),
            isMethodPtr
        );
        if (expression == null) {
            return;
        }

        String toolTipText;
        try {
            Variable v = null;
            List<Operation> operations = t.getLastOperations();
            if (operations != null) {
                for (Operation operation: operations) {
                    if (!expression.endsWith(operation.getMethodName())) {
                        continue;
                    }
                    if (operation.getMethodStartPosition().getOffset() <= offset &&
                        offset <= operation.getMethodEndPosition().getOffset()) {
                        v = operation.getReturnValue();
                    }
                }
            }
            if (v == null) {
                if (isMethodPtr[0]) {
                    return ; // We do not evaluate methods
                }
                v = d.evaluate (expression);
            }
            if (v == null) {
                return ; // Something went wrong...
            }
            String type = v.getType ();
            if (v instanceof ObjectVariable) {
                tooltipVariable = (ObjectVariable) v;
                try {
                    String toString = tooltipVariable.getToStringValue();
                    toolTipText = expression + " = " +
                        (type.length () == 0 ?
                            "" :
                            "(" + type + ") ") +
                        toString;
                } catch (InvalidExpressionException ex) {
                    toolTipText = expression + " = " +
                        (type.length () == 0 ?
                            "" :
                            "(" + type + ") ") +
                        v.getValue ();
                }
            } else {
                toolTipText = expression + " = " +
                    (type.length () == 0 ?
                        "" :
                        "(" + type + ") ") +
                    v.getValue ();
            }
        } catch (InvalidExpressionException e) {
            String typeName = resolveTypeName(offset, doc);
            if (typeName != null) {
                toolTipText = typeName;
            } else {
                toolTipText = expression + " = >" + e.getMessage () + "<";
            }
        }

        toolTipText = truncateLongText(toolTipText);
        if (tooltipVariable != null) {
            final ObjectVariable var = tooltipVariable;
            final String toolTip = toolTipText;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    final ToolTipView.ExpandableTooltip et = ToolTipView.createExpandableTooltip(toolTip);
                    et.addExpansionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            et.setBorder(BorderFactory.createLineBorder(et.getForeground()));
                            et.removeAll();
                            et.setWidthCheck(false);
                            final ToolTipView ttView = ToolTipView.getToolTipView(d, expression, var);
                            et.add(ttView);
                            et.revalidate();
                            et.repaint();
                            SwingUtilities.invokeLater(new Runnable() {
                                public @Override void run() {
                                    EditorUI eui = Utilities.getEditorUI(ep);
                                    if (eui != null) {
                                        ttView.setToolTipSupport(eui.getToolTipSupport());
                                        eui.getToolTipSupport().setToolTip(et, PopupManager.ViewPortBounds, PopupManager.AbovePreferred, 0, 0, ToolTipSupport.FLAGS_HEAVYWEIGHT_TOOLTIP);
                                    } else {
                                        firePropertyChange (PROP_SHORT_DESCRIPTION, null, toolTip);
                                    }
                                }
                            });
                        }
                    });
                    EditorUI eui = Utilities.getEditorUI(ep);
                    if (eui != null) {
                        eui.getToolTipSupport().setToolTip(et);
                    } else {
                        firePropertyChange (PROP_SHORT_DESCRIPTION, null, toolTip);
                    }
                }
            });
        } else {
            firePropertyChange (PROP_SHORT_DESCRIPTION, null, toolTipText);
        }
    }
    
    private static String truncateLongText(String text) {
        if (text.length() > MAX_TOOLTIP_TEXT) {
            text = text.substring(0, MAX_TOOLTIP_TEXT) + "...";
        }
        return text;
    }

    @Override
    public String getAnnotationType () {
        return null; // Currently return null annotation type
    }

    private static String getIdentifier (
        JPDADebugger debugger,
        StyledDocument doc,
        JEditorPane ep,
        int offset,
        boolean[] isMethodPtr
    ) {
        // do always evaluation if the tooltip is invoked on a text selection
        String t = null;
        if ( (ep.getSelectionStart () <= offset) &&
             (offset <= ep.getSelectionEnd ())
        ) {
            t = ep.getSelectedText ();
        }
        if (t != null) {
            return t;
        }
        int line = NbDocument.findLineNumber (
            doc,
            offset
        );
        int col = NbDocument.findLineColumn (
            doc,
            offset
        );
        try {
            Element lineElem =
                NbDocument.findLineRootElement (doc).
                getElement (line);

            if (lineElem == null) {
                return null;
            }
            int lineStartOffset = lineElem.getStartOffset ();
            int lineLen = lineElem.getEndOffset() - lineStartOffset;
            t = doc.getText (lineStartOffset, lineLen);
            int identStart = col;
            while (identStart > 0 &&
                (Character.isJavaIdentifierPart (
                    t.charAt (identStart - 1)
                ) ||
                (t.charAt (identStart - 1) == '.'))) {
                identStart--;
            }
            int identEnd = col;
            while (identEnd < lineLen &&
                   Character.isJavaIdentifierPart(t.charAt(identEnd))
            ) {
                identEnd++;
            }

            if (identStart == identEnd) {
                return null;
            }

            int newOffset = NbDocument.findLineOffset(doc, line) + identStart + 1;
            if (!isValidTooltipLocation(debugger, doc, newOffset)) {
                return null;
            }

            String ident = t.substring (identStart, identEnd);
            if (JAVA_KEYWORDS.contains(ident)) {
                // Java keyword => Do not show anything
                return null;
            }
            while (identEnd < lineLen &&
                   Character.isWhitespace(t.charAt(identEnd))
            ) {
                identEnd++;
            }
            if (identEnd < lineLen && t.charAt(identEnd) == '(') {
                // We're at a method call
                isMethodPtr[0] = true;
            }
            return ident;
        } catch (BadLocationException e) {
            return null;
        }
    }

    private static boolean isValidTooltipLocation(JPDADebugger debugger, final StyledDocument doc, final int offset) {
        CallStackFrame currentFrame = debugger.getCurrentCallStackFrame();
        if (currentFrame == null) {
            return false;
        }

        final boolean[] isValid = new boolean[]{true};
        final String[] className = new String[]{""};
        Future<Void> parsingTask;
        try {
            parsingTask = ParserManager.parseWhenScanFinished(Collections.singleton(Source.create(doc)), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    Result res = resultIterator.getParserResult(offset);
                    if (res == null) {
                        return;
                    }
                    CompilationController controller = CompilationController.get(res);
                    if (controller == null || controller.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                        return;
                    }
                    TreeUtilities treeUtilities = controller.getTreeUtilities();
                    SourcePositions positions = controller.getTrees().getSourcePositions();
                    TreePath mainPath = treeUtilities.pathFor(offset);
                    CompilationUnitTree unitTree = controller.getCompilationUnit();
                    // is offset it package name section?
                    Tree packgTree = unitTree.getPackageName();
                    if (offset >= positions.getStartPosition(unitTree, packgTree) &&
                            offset <= positions.getEndPosition(unitTree, packgTree)) {
                        isValid[0] = false;
                        return;
                    }
                    Tree tree = mainPath.getLeaf();
                    Tree.Kind kind = tree.getKind();
                    // [TODO] do not show tooltip for a string literal (it does not work correctly)
                    if (kind == Tree.Kind.STRING_LITERAL) {
                        isValid[0] = false;
                        return;
                    }
                    // check for comments and other non-supported elements
                    int startPos = (int)positions.getStartPosition(unitTree, tree);
                    int endPos = (int)positions.getEndPosition(unitTree, tree);
                    int startLine = Utilities.getLineOffset((BaseDocument)doc, startPos);
                    int endLine = Utilities.getLineOffset((BaseDocument)doc, endPos);
                    int line = Utilities.getLineOffset((BaseDocument)doc, offset);
                    if (kind != Tree.Kind.VARIABLE && (startLine != line || endLine != line)) {
                        isValid[0] = false;
                        return;
                    }
                    // check whether offset is in a preceding comment
                    for (Comment comm : treeUtilities.getComments(tree, true)) {
                        if (comm.pos() < 0) {
                            continue;
                        }
                        if (comm.pos() <= offset && offset <= comm.endPos()) {
                            isValid[0] = false;
                            return;
                        }
                    }
                    // check whether offset is in a trailing comment
                    for (Comment comm : treeUtilities.getComments(tree, false)) {
                        if (comm.pos() < 0) {
                            continue;
                        }
                        if (comm.pos() <= offset && offset <= comm.endPos()) {
                            isValid[0] = false;
                            return;
                        }
                    }
                    // is offset in import section?
                    TreePath path = mainPath;
                    while (path != null) {
                        tree = path.getLeaf();
                        kind = tree.getKind();
                        if (kind == Tree.Kind.IMPORT) {
                            isValid[0] = false;
                            return;
                        }
                        if (TreeUtilities.CLASS_TREE_KINDS.contains(kind) && className[0].length() == 0) {
                            TypeElement typeElement = (TypeElement)controller.getTrees().getElement(path);
                            className[0] = ElementUtilities.getBinaryName(typeElement);
                        }
                        path = path.getParentPath();
                    }
                }
            });
        } catch (ParseException ex) {
            return false;
        }
        parsingTask.cancel(false); // for the case that scanning has not finished yet
        if (!isValid[0]) {
            return false;
        }
        if (className[0].length() > 0) {
            Set<String> superTypeNames = new HashSet<String>();
            This thisVar = currentFrame.getThisVariable();
            if (thisVar != null) {
                String fqn = thisVar.getType();
                addClassNames(fqn, superTypeNames);
                ObjectVariable superTypeVar = thisVar.getSuper();
                while (superTypeVar != null) {
                    fqn = superTypeVar.getType();
                    superTypeNames.add(fqn);
                    superTypeVar = superTypeVar.getSuper();
                }
            } else {
                addClassNames(currentFrame.getClassName(), superTypeNames);
            }
            if (!superTypeNames.contains(className[0])) {
                return false;
            }
        }
        return true;
    }

    // include the class name plus all enclosing classes
    private static void addClassNames(String fqn, Set<String> typeNames) {
        do {
            typeNames.add(fqn);
            int i = fqn.lastIndexOf('$');
            if (i > 0) {
                fqn = fqn.substring(0, i);
            } else {
                fqn = null;
            }
        } while(fqn != null);
    }

    private String resolveTypeName (final int offset, Document doc) {
        final String[] result = new String[1];
        result[0] = null;
        try {
            ParserManager.parse(Collections.singleton(Source.create(doc)), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    Result res = resultIterator.getParserResult(offset);
                    if (res == null) {
                        return;
                    }
                    CompilationController controller = CompilationController.get(res);
                    if (controller == null || controller.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                        return;
                    }
                    TreePath path = controller.getTreeUtilities().pathFor(offset);
                    javax.lang.model.element.Element elem = controller.getTrees().getElement(path);
                    ElementKind kind = elem.getKind();
                    if (kind == ElementKind.CLASS || kind == ElementKind.ENUM || kind == ElementKind.ANNOTATION_TYPE) {
                        result[0] = elem.asType().toString();
                    }
                }
            });
        } catch (ParseException ex) {
        }
        return result[0];
    }

}

