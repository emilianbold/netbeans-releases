/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.java;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import javax.lang.model.element.Element;
import javax.swing.AbstractAction;
import javax.swing.Action;
import com.sun.javadoc.*;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.UiUtils;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
public class JavaCompletionDoc implements CompletionDocumentation {

    private ClasspathInfo cpInfo;
    private Doc doc;
    private String content = null;
    private Hashtable<String, ElementHandle<? extends Element>> links = new Hashtable<String, ElementHandle<? extends Element>>();
    private int linkCounter = 0;
    private URL docURL = null;
    private AbstractAction goToSource = null;

    private static final String PARAM_TAG = "@param"; //NOI18N
    private static final String RETURN_TAG = "@return"; //NOI18N
    private static final String THROWS_TAG = "@throws"; //NOI18N
    private static final String SEE_TAG = "@see"; //NOI18N
    private static final String SINCE_TAG = "@since"; //NOI18N
    private static final String INHERIT_DOC_TAG = "@inheritDoc"; //NOI18N
    
    public static final JavaCompletionDoc create(CompilationController controller, Element element) {
        return new JavaCompletionDoc(controller, element);
    }
    
    private JavaCompletionDoc(CompilationController controller, Element element) {
        ElementUtilities eu = controller.getElementUtilities();
        this.cpInfo = controller.getClasspathInfo();
        this.doc = eu.javaDocFor(element);
        this.content = prepareContent(eu);
        if (element != null) {
            final FileObject fo = SourceUtils.getFile(element, controller.getClasspathInfo());
            if (fo != null) {
                final ElementHandle<? extends Element> handle = ElementHandle.create(element);
                goToSource = new AbstractAction() {
                    public void actionPerformed(ActionEvent evt) {
                        UiUtils.open(fo, handle);
                    }
                };
            }
        }
    }

    public String getText() {
        return content;
    }

    public URL getURL() {
        return docURL;
    }

    public CompletionDocumentation resolveLink(final String link) {
        final CompletionDocumentation[] ret = new CompletionDocumentation[1];
        try {
            final ElementHandle<? extends Element> linkDoc = links.get(link);
            if (linkDoc != null) {
                FileObject fo = SourceUtils.getFile(linkDoc, cpInfo);
                if (fo != null) {
                    JavaSource.forFileObject(fo).runUserActionTask(new CancellableTask<CompilationController>() {
                        public void run(CompilationController controller) throws IOException {
                            controller.toPhase(Phase.ELEMENTS_RESOLVED);
                            ret[0] = JavaCompletionDoc.create(controller, linkDoc.resolve(controller));
                        }
                        public void cancel() {
                        }
                    }, true);
                }
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        return ret[0];
    }

    public Action getGotoSourceAction() {
        return goToSource;
    }
    
    private String prepareContent(ElementUtilities eu) {
        StringBuilder sb = new StringBuilder();
        if (doc != null) {
            if (doc instanceof ProgramElementDoc) {
                sb.append(getContainingClassOrPacakgeHeader(eu, (ProgramElementDoc)doc));
            }
            if (doc.isMethod() || doc.isConstructor()) {
                sb.append(getMethodHeader(eu, (ExecutableMemberDoc)doc));
            } else if (doc.isField() || doc.isEnumConstant()) {
                sb.append(getFieldHeader(eu, (FieldDoc)doc));
            } else if (doc.isClass() || doc.isInterface()) {
                sb.append(getClassHeader(eu, (ClassDoc)doc));
            }
            sb.append("<p>"); //NOI18N
            sb.append(inlineTags(eu, doc, doc.inlineTags()));
            sb.append("</p><p>"); //NOI18N
            sb.append(getTags(eu, doc));
            sb.append("</p>"); //NOI18N
        } else {
            sb.append(NbBundle.getMessage(JavaCompletionDoc.class, "javadoc_content_not_found")); //NOI18N
        }
        return sb.toString();
    }
    
    private String getContainingClassOrPacakgeHeader(ElementUtilities eu, ProgramElementDoc peDoc) {
        StringBuilder sb = new StringBuilder();
        ClassDoc cls = peDoc.containingClass();
        if (cls != null) {
            sb.append("<font size='+0'><b>"); //NOI18N
            createLink(sb, eu.elementFor(cls), cls.qualifiedName());
            sb.append("</b></font>"); //NOI18N)
        } else {
            PackageDoc pkg = peDoc.containingPackage();
            if (pkg != null) {
                sb.append("<font size='+0'><b>"); //NOI18N
                createLink(sb, eu.elementFor(pkg), pkg.name());
                sb.append("</b></font>"); //NOI18N)
            }
        }
        return sb.toString();
    }
    
    private String getMethodHeader(ElementUtilities eu, ExecutableMemberDoc mdoc) {
        StringBuilder sb = new StringBuilder();
        sb.append("<pre>"); //NOI18N
        sb.append(mdoc.modifiers());
        int len = sb.length() - 5;
        if (mdoc.isMethod()) {
            if (sb.length() > 0) {
                sb.append(' '); //NOI18N
                len++;
            }
            len += appendType(eu, sb, ((MethodDoc)mdoc).returnType(), false);
        }
        String name = mdoc.name();
        len += name.length();
        sb.append(" <b>").append(name).append("</b>("); //NOI18N
        len++;
        Parameter[] params = mdoc.parameters();
        for(int i = 0; i < params.length; i++) {
            appendType(eu, sb, params[i].type(), i == params.length - 1 && mdoc.isVarArgs());
            sb.append(' ').append(params[i].name()); //NOI18N
            if (i < params.length - 1) {
                sb.append(",\n"); //NOI18N
                appendSpace(sb, len);
            }
        }
        sb.append(')'); //NOI18N
        Type[] exs = mdoc.thrownExceptionTypes();
        if (exs.length > 0) {
            sb.append("\nthrows "); //NOI18N
            for (int i = 0; i < exs.length; i++) {
                appendType(eu, sb, exs[i], false);
                if (i < exs.length - 1)
                    sb.append(", "); //NOI18N
            }
        }
        sb.append("</pre>"); //NOI18N
        return sb.toString();
    }
    
    private String getFieldHeader(ElementUtilities eu, FieldDoc fdoc) {
        StringBuilder sb = new StringBuilder();
        sb.append("<pre>"); //NOI18N
        sb.append(fdoc.modifiers());
        if (sb.length() > 0)
            sb.append(' '); //NOI18N
        appendType(eu, sb, fdoc.type(), false);
        sb.append(" <b>").append(fdoc.name()).append("</b>"); //NOI18N
        sb.append("</pre>"); //NOI18N
        return sb.toString();
    }
    
    private String getClassHeader(ElementUtilities eu, ClassDoc cdoc) {
        StringBuilder sb = new StringBuilder();
        sb.append("<pre>"); //NOI18N
        sb.append(cdoc.modifiers());
        if (sb.length() > 0)
            sb.append(' '); //NOI18N
        if (cdoc.isOrdinaryClass())
            sb.append("class "); //NOI18N
        else if (cdoc.isEnum())
            sb.append("enum "); //NOI18N
        sb.append("<b>").append(cdoc.name()).append("</b>"); //NOI18N
        Type supercls = cdoc.superclassType();
        if (supercls != null) {
            sb.append("\nextends "); //NOI18N
            appendType(eu, sb, supercls, false);
        }
        Type[] ifaces = cdoc.interfaceTypes();
        if (ifaces.length > 0) {
            sb.append("\nimplements "); //NOI18N
            for (int i = 0; i < ifaces.length; i++) {
                appendType(eu, sb, ifaces[i], false);
                if (i < ifaces.length - 1)
                    sb.append(", "); //NOI18N
            }
        }
        sb.append("</pre>"); //NOI18N
        return sb.toString();
    }
    
    private String getTags(ElementUtilities eu, Doc doc) {
        StringBuilder see = new StringBuilder();
        StringBuilder par = new StringBuilder();
        StringBuilder thr = new StringBuilder();
        StringBuilder ret = new StringBuilder();
        String since = null;
        for (Tag tag : doc.tags()) {
            if (PARAM_TAG.equals(tag.kind())) {
                par.append("<code>").append(((ParamTag)tag).parameterName()).append("</code>"); //NOI18N
                Tag[] its = tag.inlineTags();
                if (its.length > 0) {
                    par.append(" - "); //NOI18N
                    par.append(inlineTags(eu, doc, its));
                }
                par.append("<br>"); //NOI18N
            } else if (THROWS_TAG.equals(tag.kind())) {
                thr.append("<code>"); //NOI18N
                Type exType = ((ThrowsTag)tag).exceptionType();
                if (exType != null)
                    createLink(thr, eu.elementFor(exType.asClassDoc()), exType.qualifiedTypeName());
                else
                    thr.append(((ThrowsTag)tag).exceptionName());
                thr.append("</code>"); //NOI18N
                Tag[] its = tag.inlineTags();
                if (its.length > 0) {
                    thr.append(" - "); //NOI18N
                    thr.append(inlineTags(eu, doc, its));
                }
                thr.append("<br>"); //NOI18N
            } else if (RETURN_TAG.equals(tag.kind())) {
                ret.append(inlineTags(eu, doc, tag.inlineTags()));
                ret.append("<br>"); //NOI18N
            } else if (SEE_TAG.equals(tag.kind())) {
                SeeTag stag = (SeeTag)tag;
                String className = stag.referencedClassName();
                String memberName = stag.referencedMemberName();
                if (memberName != null) {
                    createLink(see, eu.elementFor(stag.referencedMember()), className + "." + memberName); //NOI18N
                    see.append("<br>"); //NOI18N
                } else if (className != null) {
                    createLink(see, eu.elementFor(stag.referencedClass()), className);
                    see.append("<br>"); //NOI18N
                }
            } else if (SINCE_TAG.equals(tag.kind())) {
                since = tag.text();
            }
        }
        StringBuilder sb = new StringBuilder();
        if (par.length() > 0) {
            sb.append("<b>").append(NbBundle.getMessage(JavaCompletionDoc.class, "JCD-params")).append("</b><blockquote>").append(par).append("</blockquote>"); //NOI18N
        }
        if (ret.length() > 0) {
            sb.append("<b>").append(NbBundle.getMessage(JavaCompletionDoc.class, "JCD-returns")).append("</b><blockquote>").append(ret).append("</blockquote>"); //NOI18N
        }
        if (thr.length() > 0) {
            sb.append("<b>").append(NbBundle.getMessage(JavaCompletionDoc.class, "JCD-throws")).append("</b><blockquote>").append(thr).append("</blockquote>"); //NOI18N
        }
        if (see.length() > 0) {
            sb.append("<b>").append(NbBundle.getMessage(JavaCompletionDoc.class, "JCD-see")).append("</b><blockquote>").append(see).append("</blockquote>"); //NOI18N
        }
        if (since != null) {
            sb.append("<b>").append(NbBundle.getMessage(JavaCompletionDoc.class, "JCD-since")).append("</b><blockquote>").append(since).append("</blockquote>"); //NOI18N
        }
        return sb.toString();
    }
    
    private String inlineTags(ElementUtilities eu, Doc doc, Tag[] tags) {
        StringBuilder sb = new StringBuilder();
        for (Tag tag : tags) {
            if (SEE_TAG.equals(tag.kind())) {
                SeeTag stag = (SeeTag)tag;
                String className = stag.referencedClassName();
                String memberName = stag.referencedMemberName();
                if (memberName != null) {
                    createLink(sb, eu.elementFor(stag.referencedMember()), className + "." + memberName); //NOI18N
                } else if (className != null) {
                    createLink(sb, eu.elementFor(stag.referencedClass()), className);
                }
            } else if (INHERIT_DOC_TAG.equals(tag.kind())) {
                if (doc.isMethod()) {
                    MethodDoc mdoc = ((MethodDoc)doc).overriddenMethod();
                    if (mdoc != null)
                        sb.append(inlineTags(eu, mdoc, mdoc.inlineTags()));
                } else if (doc.isClass() || doc.isInterface()) {
                    ClassDoc cdoc = ((ClassDoc)doc).superclass();
                    if (cdoc != null)
                        sb.append(inlineTags(eu, cdoc, cdoc.inlineTags()));
                }
            } else {
                sb.append(tag.text());
            }
        }
        return sb.toString();
    }
    
    private void appendSpace(StringBuilder sb, int length) {
        while (length-- >= 0)
            sb.append(' '); //NOI18N            
    }
    
    private int appendType(ElementUtilities eu, StringBuilder sb, Type type, boolean varArg) {
        int len = 0;
        WildcardType wt = type.asWildcardType();
        if (wt != null) {
            sb.append('?'); //NOI18N
            len++;
            Type[] bounds = wt.extendsBounds();
            if (bounds != null && bounds.length > 0) {
                sb.append(" extends "); //NOI18N
                len += 9;
                len += appendType(eu, sb, bounds[0], false);
            }
            bounds = wt.superBounds();
            if (bounds != null && bounds.length > 0) {
                sb.append(" super "); //NOI18N
                len += 7;
                len += appendType(eu, sb, bounds[0], false);
            }
        } else {
            len = createLink(sb, eu.elementFor(type.asClassDoc()), type.simpleTypeName());
            ParameterizedType pt = type.asParameterizedType();
            if (pt != null) {
                Type[] targs = pt.typeArguments();
                if (targs.length > 0) {
                    sb.append("&lt;"); //NOI18N
                    for (int j = 0; j < targs.length; j++) {
                        len += appendType(eu, sb, targs[j], false);
                        if (j < targs.length - 1) {
                            sb.append(", "); //NOI18N
                            len += 2;
                        }
                    }
                    sb.append("&gt;"); //NOI18N
                    len += 2;
                }
            }
        }
        String dim = type.dimension();
        if (dim.length() > 0) {
            if (varArg)
                dim = dim.substring(2) + "..."; //NOI18N
            sb.append(dim);
            len += dim.length();
        }
        return len;
    }
    
    private int createLink(StringBuilder sb, Element e, String text) {
        if (e != null) {
            String link = "*" + linkCounter++; //NOI18N
            links.put(link, ElementHandle.create(e));
            sb.append("<a href='").append(link).append("'>"); //NOI18N
        }
        sb.append(text);
        if (e != null)
            sb.append("</a>"); //NOI18N
        return text.length();
    }
}
