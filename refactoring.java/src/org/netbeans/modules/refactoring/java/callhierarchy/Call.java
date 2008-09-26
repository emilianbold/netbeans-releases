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

package org.netbeans.modules.refactoring.java.callhierarchy;

import com.sun.source.util.TreePath;
import java.awt.EventQueue;
import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.swing.Icon;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.ui.ElementHeaders;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Jan Pokorsky
 */
final class Call implements CallDescriptor {
    
    private static final String TYPE_COLOR = "#707070"; // NOI18N

    private List<Call> references;
    private List<CallOccurrence> occurrences;
    CallHierarchyModel model;

    private String displayName;
    private String htmlDisplayName;
    private Icon icon;
    TreePathHandle selection;
    TreePathHandle declaration;
    private TreePathHandle overridden;
    private ElementHandle identity;
    private Call parent;
    private boolean leaf;
    /** collection of references might not be complete */
    private boolean canceled = false;
    private enum State { CANCELED, BROKEN }
    private State state;

    private Call() {
    }
    
    public List<Call> getReferences() {
        return references != null ? references : Collections.<Call>emptyList();
    }

    void setReferences(List<Call> references) {
        this.references = references;
    }

    public List<CallOccurrence> getOccurrences() {
        return occurrences;
    }

    CallHierarchyModel getModel() {
        return model;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getHtmlDisplayName() {
        return htmlDisplayName;
    }

    public Icon getIcon() {
        return icon;
    }

    public boolean isLeaf() {
        return leaf;
    }

    public boolean isCanceled() {
        return this.state == State.CANCELED;
    }

    void setCanceled(boolean canceled) {
        if (canceled) {
            this.state = State.CANCELED;
        }
    }

    void setBroken() {
        this.state = State.BROKEN;
    }

    public boolean isBroken() {
        return this.state == State.BROKEN;
    }
    
    TreePathHandle getSourceToQuery() {
        return overridden != null
                ? overridden
                : declaration != null ? declaration : selection;
    }

    public void open() {
        if (occurrences != null && !occurrences.isEmpty()) {
            occurrences.get(0).open();
        }
    }
    
    public static Call createRoot(CompilationInfo javac, TreePath selection, Element selectionElm, boolean isCallerGraph) {
        return createReference(javac, selection, selectionElm, null, isCallerGraph, Collections.<TreePath>emptyList());
    }
    
    public static Call createUsage(CompilationInfo javac, TreePath selection, Element selectionElm, Call parent, List<TreePath> occurrences) {
        return createReference(javac, selection, selectionElm, parent, parent.model.getType() == CallHierarchyModel.HierarchyType.CALLER, occurrences);
    }
    
    private static Call createReference(CompilationInfo javac, TreePath selection, Element selectionElm, Call parent, boolean isCallerGraph, List<TreePath> occurrences) {
        Call c = new Call();
        if (selectionElm.getKind() == ElementKind.INSTANCE_INIT || selectionElm.getKind() == ElementKind.STATIC_INIT) {
            c.displayName = "<init>"; // NOI18N
            c.identity = null;
        } else {
            c.displayName = ElementHeaders.getHeader(selectionElm, javac, ElementHeaders.NAME);
            c.identity = ElementHandle.create(selectionElm);
        }
        c.htmlDisplayName = createHtmlHeader(selectionElm, occurrences.size(), javac);
        c.icon = ElementIcons.getElementIcon(selectionElm.getKind(), selectionElm.getModifiers());
        c.selection = TreePathHandle.create(selection, javac);
        c.parent = parent;
        if (parent != null) {
            c.model = parent.model;
        }
        Element wanted = javac.getTrees().getElement(selection);

        if (isCallerGraph && wanted != null && wanted.getKind() == ElementKind.METHOD) {
            Collection<ExecutableElement> overridenMethods = RetoucheUtils.getOverridenMethods((ExecutableElement) wanted, javac);
            if (!overridenMethods.isEmpty()) {
                ExecutableElement next = overridenMethods.iterator().next();
                c.overridden = TreePathHandle.create(next, javac);
                c.identity = ElementHandle.create(next);
            }
        }
        
        if (wanted != null) {
            TreePath declarationPath = javac.getTrees().getPath(wanted);
            if (declarationPath != null) {
                c.declaration = TreePathHandle.create(declarationPath, javac);
            }
        }
        
        if (c.identity != null) {
            boolean[] recursion = {false};
            c.leaf = isLeaf(selectionElm, c.identity, parent, isCallerGraph, recursion);
            if (recursion[0]) {
                Image badge = ImageUtilities.loadImage("org/netbeans/modules/refactoring/java/resources/recursion_badge.png");
                Image icon2Image = ImageUtilities.icon2Image(c.icon);
                Image badgedImage = ImageUtilities.mergeImages(icon2Image, badge, 8, 8);
                c.icon = ImageUtilities.image2Icon(badgedImage);
            }
        } else {
            c.leaf = true;
        }

        c.occurrences = new ArrayList<CallOccurrence>(occurrences.size());
        for (TreePath occurrence : occurrences) {
            c.occurrences.add(CallOccurrence.createOccurrence(javac, occurrence, parent));
        }
        return c;
    }

    @Override
    public String toString() {
        return String.format("name='%s', handle='%s', refs='%s'", displayName, selection, references); // NOI18N
    }

    private static boolean isLeaf(Element elm, ElementHandle handle, Call parent, boolean isCallerGraph, boolean[] recursion) {
        ElementKind kind = elm.getKind();
        recursion[0] = false;
        if (kind != ElementKind.METHOD && kind != ElementKind.CONSTRUCTOR) {
            return true;
        }
        if (!isCallerGraph && elm.getModifiers().contains(Modifier.ABSTRACT)) {
            return true;
        }
        while (parent != null) {                
            if (handle.equals(parent.identity)) {
                recursion[0] = true;
                return true;
            }
            parent = parent.parent;
        }
        return false;
    }
    
    private static String createHtmlHeader(Element e, int occurrences, CompilationInfo javac) {
        String member;
        switch (e.getKind()) {
            case METHOD:
            case CONSTRUCTOR:
                member = createHtmlHeader((ExecutableElement) e,javac);
                break;
            case INSTANCE_INIT:
            case STATIC_INIT:
                member = "&lt;init&gt;"; // NOI18N
                break;
            default:
                member = ElementHeaders.getHeader(e, javac, ElementHeaders.NAME);
        }
        
        String encloser = String.format("<font color=%s>%s</font>", TYPE_COLOR,
                javac.getElements().getBinaryName((TypeElement) e.getEnclosingElement()).toString());
        
        String occurrencesHeader = occurrences > 1
                ? ", " + occurrences + " occurrences"
                : ""; // NOI18N
        
        return String.format("<html>%s :: %s%s</html>", member, encloser, occurrencesHeader);
    }
    /**
     * Creates HTML display name of the Executable element
     * @see org.netbeans.modules.java.navigation.ElementScanningTask
     */
    private static String createHtmlHeader(ExecutableElement e, CompilationInfo javac) {
        
        boolean isDeprecated = javac.getElements().isDeprecated(e);

        StringBuilder sb = new StringBuilder();
        if (isDeprecated) {
            sb.append("<s>"); // NOI18N
        }
        if (e.getKind() == ElementKind.CONSTRUCTOR) {
            sb.append(e.getEnclosingElement().getSimpleName());
        } else {
            sb.append(e.getSimpleName());
        }
        if (isDeprecated) {
            sb.append("</s>"); // NOI18N
        }

        sb.append("("); // NOI18N

        for (VariableElement param : e.getParameters()) {
            sb.append("<font color=" + TYPE_COLOR + ">"); // NOI18N
            sb.append(print(param.asType()));
            sb.append("</font>"); // NOI18N
            sb.append(" "); // NOI18N
            sb.append(param.getSimpleName());
//            if (it.hasNext()) {
                sb.append(", "); // NOI18N
//            }
        }

        if (sb.charAt(sb.length() - 1) == '(') {
            sb.append(")"); // NOI18N
        } else {
            sb.replace(sb.length() - 2, sb.length(), ")"); // NOI18N
        }

//        if (e.getKind() != ElementKind.CONSTRUCTOR) {
//            TypeMirror rt = e.getReturnType();
//            if (rt.getKind() != TypeKind.VOID) {
//                sb.append(" : "); // NOI18N     
//                sb.append("<font color=" + TYPE_COLOR + ">"); // NOI18N
//                sb.append(print(e.getReturnType()));
//                sb.append("</font>"); // NOI18N                    
//            }
//        }

        return sb.toString();
    }

    private static String print(TypeMirror tm) {
        StringBuilder sb;

        switch (tm.getKind()) {
            case DECLARED:
                DeclaredType dt = (DeclaredType) tm;
                sb = new StringBuilder(dt.asElement().getSimpleName().toString());
                List<? extends TypeMirror> typeArgs = dt.getTypeArguments();
                if (!typeArgs.isEmpty()) {
                    sb.append("&lt;"); // NOI18N

                    for (Iterator<? extends TypeMirror> it = typeArgs.iterator(); it.hasNext();) {
                        TypeMirror ta = it.next();
                        sb.append(print(ta));
                        if (it.hasNext()) {
                            sb.append(", "); // NOI18N
                        }
                    }
                    sb.append("&gt;"); // NOI18N
                }

                return sb.toString();
            case TYPEVAR:
                TypeVariable tv = (TypeVariable) tm;
                sb = new StringBuilder(tv.asElement().getSimpleName().toString());
                return sb.toString();
            case ARRAY:
                ArrayType at = (ArrayType) tm;
                sb = new StringBuilder(print(at.getComponentType()));
                sb.append("[]"); // NOI18N
                return sb.toString();
            case WILDCARD:
                WildcardType wt = (WildcardType) tm;
                sb = new StringBuilder("?");
                if (wt.getExtendsBound() != null) {
                    sb.append(" extends "); // NOI18N
                    sb.append(print(wt.getExtendsBound()));
                }
                if (wt.getSuperBound() != null) {
                    sb.append(" super "); // NOI18N
                    sb.append(print(wt.getSuperBound()));
                }
                return sb.toString();
            default:
                return tm.toString();
        }
    }
    
    static boolean doOpen(FileObject fo, PositionBounds bounds) {
        try {
            final int begin = bounds.getBegin().getOffset();
            final int end = bounds.getEnd().getOffset();
            DataObject od = DataObject.find(fo);
            final EditorCookie ec = od.getCookie(org.openide.cookies.EditorCookie.class);
            LineCookie lc = od.getCookie(org.openide.cookies.LineCookie.class);

            if (ec != null && lc != null && begin != -1) {                
                StyledDocument doc = ec.openDocument();                
                if (doc != null) {
                    int line = NbDocument.findLineNumber(doc, begin);
                    int lineOffset = NbDocument.findLineOffset(doc, line);
                    int column = begin - lineOffset;

                    if (line != -1) {
                        Line l = lc.getLineSet().getCurrent(line);

                        if (l != null) {
                            l.show(Line.SHOW_GOTO, column);

                            EventQueue.invokeLater(new Runnable() {

                                public void run() {
                                    ec.getOpenedPanes()[0].setSelectionStart(begin);
                                    ec.getOpenedPanes()[0].setSelectionEnd(end);
                                }
                            });
                            return true;
                        }
                    }
                }
            }

            OpenCookie oc = od.getCookie(org.openide.cookies.OpenCookie.class);

            if (oc != null) {
                oc.open();                
                return true;
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }

        return false;
    }
    
}
