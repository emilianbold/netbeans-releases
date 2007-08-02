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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.api.java.source.ui;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.swing.AbstractAction;
import javax.swing.Action;
import com.sun.javadoc.*;
import com.sun.javadoc.AnnotationDesc.ElementValuePair;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/** Utility class for viewing Javdoc comments as HTML.
 *
 * @author Dusan Balek, Petr Hrebejk
 */
public class ElementJavadoc {

    private ElementJavadoc() {
    }

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
    private static final String LINKPLAIN_TAG = "@linkplain"; //NOI18N
    private static final String CODE_TAG = "@code"; //NOI18N
    private static final String DEPRECATED_TAG = "@deprecated"; //NOI18N
    
    /** Creates an object describing the Javadoc of given element. The object
     * is capable of getting the text formated into HTML, resolve the links,
     * jump to external javadoc.
     * 
     * @param compilationInfo CompilationInfo
     * @param element Element the javadoc is required for
     * @return ElementJavadoc describing the jaadoc
     */
    public static final ElementJavadoc create(CompilationInfo compilationInfo, Element element) {
        return new ElementJavadoc(compilationInfo, element, null);
    }
    
    /** Gets the javadoc comment formated as HTML.      
     * @return HTML text of the javadoc
     */
    public String getText() {
        return content;
    }

    /** Gets URL of the external javadoc.
     * @return Text of the Javadoc comment formated as HTML
     */ 
    public URL getURL() {
        return docURL;
    }

    /** Resolves a link contained in the Javadoc comment to a n object 
     * describing the linked javadoc
     * @param link Link which has to be resolved
     * @return ElementJavadoc describing the javadoc of liked element
     */
    public ElementJavadoc resolveLink(final String link) {
        final ElementJavadoc[] ret = new ElementJavadoc[1];
        try {
            final ElementHandle<? extends Element> linkDoc = links.get(link);
            FileObject fo = linkDoc != null ? SourceUtils.getFile(linkDoc, cpInfo) : null;
            JavaSource js = fo != null ? JavaSource.forFileObject(fo) : JavaSource.create(cpInfo);
            if (js != null) {
                js.runUserActionTask(new CancellableTask<CompilationController>() {
                    public void run(CompilationController controller) throws IOException {
                        controller.toPhase(Phase.ELEMENTS_RESOLVED);
                        if (linkDoc != null) {
                            ret[0] = new ElementJavadoc(controller, linkDoc.resolve(controller), null);
                        } else {
                            int idx = link.indexOf('#'); //NOI18N
                            URI uri = URI.create(idx < 0 ? link : link.substring(0, idx));
                            if (uri != null) {
                                if (!uri.isAbsolute())
                                    uri = uri.normalize();
                                String path = uri.toString();
                                int startIdx = path.lastIndexOf(".."); //NOI18N
                                startIdx = startIdx < 0 ? 0 : startIdx + 3;
                                int endIdx = path.lastIndexOf('.'); //NOI18N
                                if (endIdx >= 0)
                                    path = path.substring(startIdx, endIdx);
                                String clsName = path.replace('/', '.'); //NOI18N
                                Element e = controller.getElements().getTypeElement(clsName);
                                if (e != null) {
                                    if (idx >= 0) {
                                        String fragment = link.substring(idx + 1);
                                        idx = fragment.indexOf('('); //NOI18N
                                        String name = idx < 0 ? fragment : fragment.substring(0, idx);
                                        for (Element member : e.getEnclosedElements()) {
                                            if (member.getSimpleName().contentEquals(name) && fragment.contentEquals(getFragment(member))) {
                                                e = member;
                                                break;
                                            }
                                        }
                                    }
                                    ret[0] = new ElementJavadoc(controller, e, new URL(docURL, link));
                                } else {
                                    //external URL
                                    if( uri.isAbsolute() )
                                        ret[0] = new ElementJavadoc( uri.toURL() );
                                } 
                            }
                        }
                    }
                    public void cancel() {
                    }
                }, true);
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        return ret[0];
    }

    
    /** Gets action capable of juming to source of the Element this Javadoc
     * belongs to.
     * @return Action going to the source of the Element described by this javadoc.
     */
    public Action getGotoSourceAction() {
        return goToSource;
    }
    
    private ElementJavadoc(CompilationInfo compilationInfo, Element element, URL url) {
        ElementUtilities eu = compilationInfo.getElementUtilities();
        this.cpInfo = compilationInfo.getClasspathInfo();
        this.doc = eu.javaDocFor(element);
        if (element != null) {
            final FileObject fo = SourceUtils.getFile(element, compilationInfo.getClasspathInfo());
            if (fo != null) {
                final ElementHandle<? extends Element> handle = ElementHandle.create(element);
                goToSource = new AbstractAction() {
                    public void actionPerformed(ActionEvent evt) {
                        ElementOpen.open(fo, handle);
                    }
                };
            }
            if (url != null) {
                docURL = url;
            } else {
                docURL = SourceUtils.getJavadoc(element, cpInfo);
            }
        }
        this.content = prepareContent(eu);
    }
    
    private ElementJavadoc(URL url) {
        assert url != null;
        this.content = null;
        this.docURL = url;
    }

    // Private section ---------------------------------------------------------
    
    private String prepareContent(ElementUtilities eu) {
        StringBuilder sb = new StringBuilder();
        if (doc != null) {
            if (doc instanceof ProgramElementDoc) {
                sb.append(getContainingClassOrPackageHeader(eu, (ProgramElementDoc)doc));
            }
            if (doc.isMethod() || doc.isConstructor() || doc.isAnnotationTypeElement()) {
                sb.append(getMethodHeader(eu, (ExecutableMemberDoc)doc));
            } else if (doc.isField() || doc.isEnumConstant()) {
                sb.append(getFieldHeader(eu, (FieldDoc)doc));
            } else if (doc.isClass() || doc.isInterface() || doc.isAnnotationType()) {
                sb.append(getClassHeader(eu, (ClassDoc)doc));
            }
            sb.append("<p>"); //NOI18N
            if (doc.commentText().length() > 0 || doc.tags().length > 0) {
                sb.append(getDeprecatedTag(eu, doc));
                sb.append(inlineTags(eu, doc, doc.inlineTags()));
                sb.append("</p><p>"); //NOI18N
                sb.append(getTags(eu, doc));
            } else {
                String jdText = docURL != null ? HTMLJavadocParser.getJavadocText(docURL, false) : null;
                if (jdText != null)
                    sb.append(jdText);
                else
                    sb.append(NbBundle.getMessage(ElementJavadoc.class, "javadoc_content_not_found")); //NOI18N
            }
            sb.append("</p>"); //NOI18N
        } else {
            sb.append(NbBundle.getMessage(ElementJavadoc.class, "javadoc_content_not_found")); //NOI18N
        }
        return sb.toString();
    }
    
    private CharSequence getContainingClassOrPackageHeader(ElementUtilities eu, ProgramElementDoc peDoc) {
        StringBuilder sb = new StringBuilder();
        ClassDoc cls = peDoc.containingClass();
        if (cls != null) {
            Element e = eu.elementFor(cls);
            if (e != null) {
                switch(e.getEnclosingElement().getKind()) {
                    case ANNOTATION_TYPE:
                    case CLASS:
                    case ENUM:
                    case INTERFACE:
                    case PACKAGE:
                        if (cls.containingClass() != null || cls.containingPackage() != null) {
                            sb.append("<font size='+0'><b>"); //NOI18N
                            createLink(sb, e, makeNameLineBreakable(cls.qualifiedName()));
                            sb.append("</b></font>"); //NOI18N)
                        }
                }
            }
        } else {
            PackageDoc pkg = peDoc.containingPackage();
            if (pkg != null) {
                sb.append("<font size='+0'><b>"); //NOI18N
                createLink(sb, eu.elementFor(pkg), makeNameLineBreakable(pkg.name()));
                sb.append("</b></font>"); //NOI18N)
            }
        }
        return sb;
    }
    private String makeNameLineBreakable(String name) {
        return name.replace(".", /* ZERO WIDTH SPACE */".&#x200B;");
    }
    
    private CharSequence getMethodHeader(ElementUtilities eu, ExecutableMemberDoc mdoc) {
        StringBuilder sb = new StringBuilder();
        sb.append("<p><tt>"); //NOI18N
        sb.append(getAnnotations(eu, mdoc));
        int len = sb.length();
        sb.append(Modifier.toString(mdoc.modifierSpecifier() &~ Modifier.NATIVE));
        len = sb.length() - len;
        TypeVariable[] tvars = mdoc.typeParameters();
        if (tvars.length > 0) {
            if (len > 0) {
                sb.append(' '); //NOI18N
                len++;
            }
            sb.append("&lt;"); //NOI18N
            for (int i = 0; i < tvars.length; i++) {
                len += appendType(eu, sb, tvars[i], false, true);
                if (i < tvars.length - 1) {
                    sb.append(","); //NOI18N
                    len++;
                }
            }
            sb.append("&gt;"); //NOI18N
            len += 2;
        }
        if (!mdoc.isConstructor()) {
            if (len > 0) {
                sb.append(' '); //NOI18N
                len++;
            }
            len += appendType(eu, sb, ((MethodDoc)mdoc).returnType(), false, false);
        }
        String name = mdoc.name();
        len += name.length();
        sb.append(" <b>").append(name).append("</b>"); //NOI18N
        if (!mdoc.isAnnotationTypeElement()) {
            sb.append('('); //NOI18N
            len++;
            Parameter[] params = mdoc.parameters();
            for(int i = 0; i < params.length; i++) {
                boolean varArg = i == params.length - 1 && mdoc.isVarArgs();
                appendType(eu, sb, params[i].type(), varArg, false);
                sb.append(' ').append(params[i].name()); //NOI18N
                String dim = params[i].type().dimension();
                if (dim.length() > 0) {
                    if (varArg)
                        dim = dim.substring(2) + "..."; //NOI18N
                }
                if (i < params.length - 1) {
                    sb.append(",\n"); //NOI18N
                    appendSpace(sb, len);
                }
            }
            sb.append(')'); //NOI18N            
        }
        Type[] exs = mdoc.thrownExceptionTypes();
        if (exs.length > 0) {
            sb.append("\nthrows "); //NOI18N
            for (int i = 0; i < exs.length; i++) {
                appendType(eu, sb, exs[i], false, false);
                if (i < exs.length - 1)
                    sb.append(", "); //NOI18N
            }
        }
        sb.append("</tt></p>"); //NOI18N
        return sb;
    }
    
    private CharSequence getFieldHeader(ElementUtilities eu, FieldDoc fdoc) {
        StringBuilder sb = new StringBuilder();
        sb.append("<p><tt>"); //NOI18N
        sb.append(getAnnotations(eu, fdoc));
        int len = sb.length();
        sb.append(fdoc.modifiers());
        len = sb.length() - len;
        if (len > 0)
            sb.append(' '); //NOI18N
        appendType(eu, sb, fdoc.type(), false, false);
        sb.append(" <b>").append(fdoc.name()).append("</b>"); //NOI18N
        sb.append("</tt></p>"); //NOI18N
        return sb;
    }
    
    private CharSequence getClassHeader(ElementUtilities eu, ClassDoc cdoc) {
        StringBuilder sb = new StringBuilder();
        sb.append("<p><tt>"); //NOI18N
        sb.append(getAnnotations(eu, cdoc));
        int mods = cdoc.modifierSpecifier() & ~Modifier.INTERFACE;
        if (cdoc.isEnum())
            mods &= ~Modifier.FINAL;
        sb.append(Modifier.toString(mods));
        if (sb.length() > 0)
            sb.append(' '); //NOI18N
        if (cdoc.isAnnotationType())
            sb.append("@interface "); //NOI18N
        else if (cdoc.isEnum())
            sb.append("enum "); //NOI18N
        else if (cdoc.isInterface())
            sb.append("interface "); //NOI18N
        else
            sb.append("class "); //NOI18N            
        sb.append("<b>").append(cdoc.name()); //NOI18N
        TypeVariable[] tvars = cdoc.typeParameters();
        if (tvars.length > 0) {
            sb.append("&lt;"); //NOI18N
            for (int i = 0; i < tvars.length; i++) {
                appendType(eu, sb, tvars[i], false, true);
                if (i < tvars.length - 1)
                    sb.append(","); //NOI18N
            }
            sb.append("&gt;"); //NOI18N
        }
        sb.append("</b>"); //NOi18N
        if (!cdoc.isAnnotationType()) {
            if (cdoc.isClass()) {
                Type supercls = cdoc.superclassType();
                if (supercls != null) {
                    sb.append("\nextends "); //NOI18N
                    appendType(eu, sb, supercls, false, false);
                }
                
            }
            Type[] ifaces = cdoc.interfaceTypes();
            if (ifaces.length > 0) {
                sb.append(cdoc.isInterface() ? "\nextends " : "\nimplements "); //NOI18N
                for (int i = 0; i < ifaces.length; i++) {
                    appendType(eu, sb, ifaces[i], false, false);
                    if (i < ifaces.length - 1)
                        sb.append(", "); //NOI18N
                }
            }
        }
        sb.append("</tt></p>"); //NOI18N
        return sb;
    }
    
    private CharSequence getAnnotations(ElementUtilities eu, ProgramElementDoc peDoc) {
        StringBuilder sb = new StringBuilder();
        for (AnnotationDesc annotationDesc : peDoc.annotations()) {
            AnnotationTypeDoc annotationType = annotationDesc.annotationType();
            if (annotationType != null) {
                appendType(eu, sb, annotationType, false, false);
                ElementValuePair[] pairs = annotationDesc.elementValues();
                if (pairs.length > 0) {
                    sb.append('('); //NOI18N
                    for (int i = 0; i < pairs.length; i++) {
                        AnnotationTypeElementDoc ated = pairs[i].element();
                        createLink(sb, eu.elementFor(ated), ated.name());
                        sb.append('='); //NOI18N
                        appendAnnotationValue(eu, sb, pairs[i].value());
                        if (i < pairs.length - 1)
                            sb.append(","); //NOI18N
                    }
                    sb.append(')'); //NOI18N
                }
                sb.append('\n'); //NOI18N
            }
        }
        return sb;
    }
    
    private void appendAnnotationValue(ElementUtilities eu, StringBuilder sb, AnnotationValue av) {
        Object value = av.value();
        if (value instanceof AnnotationValue[]) {
            int length = ((AnnotationValue[])value).length;
            if (length > 1)
                sb.append('{'); //NOI18N
            for(int i = 0; i < ((AnnotationValue[])value).length; i++) {
                appendAnnotationValue(eu, sb, ((AnnotationValue[])value)[i]);
                if (i < ((AnnotationValue[])value).length - 1)
                    sb.append(","); //NOI18N
            }
            if (length > 1)
                sb.append('}'); //NOI18N
        } else if (value instanceof Doc) {
            createLink(sb, eu.elementFor((Doc)value), ((Doc)value).name());
        } else {
            sb.append(value.toString());
        }
    } 
    
    private CharSequence getTags(ElementUtilities eu, Doc doc) {
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
                    createLink(thr, eu.elementFor(exType.asClassDoc()), exType.simpleTypeName());
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
                ClassDoc refClass = stag.referencedClass();
                String className = stag.referencedClassName();
                String memberName = stag.referencedMemberName();
                String label = stag.label();
                if (memberName != null) {
                    if (refClass != null) {
                        createLink(see, eu.elementFor(stag.referencedMember()), "<code>" + (label != null && label.length() > 0 ? label : (refClass.simpleTypeName() + "." + memberName)) + "</code>"); //NOI18N
                    } else {
                        see.append(className);
                        see.append('.'); //NOI18N
                        see.append(memberName);
                    }
                    see.append(", "); //NOI18N
                } else if (className != null) {
                    if (refClass != null) {
                        createLink(see, eu.elementFor(refClass), "<code>" + (label != null && label.length() > 0 ? label : refClass.simpleTypeName()) + "</code>"); //NOI18N
                    } else {
                        see.append(className);
                    }
                    see.append(", "); //NOI18N
                } else {
                    see.append(stag.text()).append(", "); //NOI18N
                }
            } else if (SINCE_TAG.equals(tag.kind())) {
                since = tag.text();
            }
        }
        StringBuilder sb = new StringBuilder();
        if (par.length() > 0) {
            sb.append("<b>").append(NbBundle.getMessage(ElementJavadoc.class, "JCD-params")).append("</b><blockquote>").append(par).append("</blockquote>"); //NOI18N
        }
        if (ret.length() > 0) {
            sb.append("<b>").append(NbBundle.getMessage(ElementJavadoc.class, "JCD-returns")).append("</b><blockquote>").append(ret).append("</blockquote>"); //NOI18N
        }
        if (thr.length() > 0) {
            sb.append("<b>").append(NbBundle.getMessage(ElementJavadoc.class, "JCD-throws")).append("</b><blockquote>").append(thr).append("</blockquote>"); //NOI18N
        }
        if (since != null) {
            sb.append("<b>").append(NbBundle.getMessage(ElementJavadoc.class, "JCD-since")).append("</b><blockquote>").append(since).append("</blockquote>"); //NOI18N
        }
        int length = see.length();
        if (length > 0) {
            sb.append("<b>").append(NbBundle.getMessage(ElementJavadoc.class, "JCD-see")).append("</b><blockquote>").append(see.delete(length - 2, length)).append("</blockquote>"); //NOI18N
        }
        return sb;
    }
    
    private CharSequence getDeprecatedTag(ElementUtilities eu, Doc doc) {
        StringBuilder sb = new StringBuilder();
        for (Tag tag : doc.tags()) {
            if (DEPRECATED_TAG.equals(tag.kind()))
                sb.append("<b>").append(NbBundle.getMessage(ElementJavadoc.class, "JCD-deprecated")).append("</b> <i>").append(inlineTags(eu, doc, tag.inlineTags())).append("</i></p><p>"); //NOI18N
        }
        return sb;
    }
    
    private CharSequence inlineTags(ElementUtilities eu, Doc doc, Tag[] tags) {
        StringBuilder sb = new StringBuilder();
        for (Tag tag : tags) {
            if (SEE_TAG.equals(tag.kind())) {
                SeeTag stag = (SeeTag)tag;
                ClassDoc refClass = stag.referencedClass();
                String memberName = stag.referencedMemberName();
                String label = stag.label();
                boolean plain = LINKPLAIN_TAG.equals(stag.name());
                if (memberName != null) {
                    if (refClass != null) {
                        createLink(sb, eu.elementFor(stag.referencedMember()), (plain ? "" : "<code>") + (label != null && label.length() > 0 ? label : (refClass.simpleTypeName() + "." + memberName)) + (plain ? "" : "</code>")); //NOI18N
                    } else {
                        sb.append(stag.referencedClassName());
                        sb.append('.'); //NOI18N
                        sb.append(memberName);
                    }
                } else {
                    if (refClass != null) {
                        createLink(sb, eu.elementFor(refClass), (plain ? "" : "<code>") + (label != null && label.length() > 0 ? label : refClass.simpleTypeName()) + (plain ? "" : "</code>")); //NOI18N
                    } else {
                        sb.append(stag.referencedClassName());
                    }
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
            } else if (CODE_TAG.equals(tag.kind())) {
                sb.append("<code>"); //NOI18N
                sb.append(tag.text());
                sb.append("</code>"); //NOI18N
            } else {
                sb.append(tag.text());
            }
        }
        return sb;
    }
    
    private CharSequence getFragment(Element e) {
        StringBuilder sb = new StringBuilder();
        if (!e.getKind().isClass() && !e.getKind().isInterface()) {
            if (e.getKind() == ElementKind.CONSTRUCTOR) {
                sb.append(e.getEnclosingElement().getSimpleName());
            } else {
                sb.append(e.getSimpleName());
            }
            if (e.getKind() == ElementKind.METHOD || e.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement ee = (ExecutableElement)e;
                sb.append('('); //NOI18N
                for (Iterator<? extends VariableElement> it = ee.getParameters().iterator(); it.hasNext();) {
                    VariableElement param = it.next();
                    appendType(sb, param.asType(), ee.isVarArgs() && !it.hasNext());
                    if (it.hasNext())
                        sb.append(", ");
                }
                sb.append(')'); //NOI18N
            }
        }
        return sb;
    }
    
    private void appendType(StringBuilder sb, TypeMirror type, boolean varArg) {
        switch (type.getKind()) {
            case ARRAY:
                appendType(sb, ((ArrayType)type).getComponentType(), false);
                sb.append(varArg ? "..." : "[]"); //NOI18N
                break;
            case DECLARED:
                sb.append(((TypeElement)((DeclaredType)type).asElement()).getQualifiedName());
                break;
            default:
                sb.append(type);
        }
    }

    private void appendSpace(StringBuilder sb, int length) {
        while (length-- >= 0)
            sb.append(' '); //NOI18N            
    }
    
    private int appendType(ElementUtilities eu, StringBuilder sb, Type type, boolean varArg, boolean typeVar) {
        int len = 0;
        WildcardType wt = type.asWildcardType();
        if (wt != null) {
            sb.append('?'); //NOI18N
            len++;
            Type[] bounds = wt.extendsBounds();
            if (bounds != null && bounds.length > 0) {
                sb.append(" extends "); //NOI18N
                len += 9;
                len += appendType(eu, sb, bounds[0], false, false);
            }
            bounds = wt.superBounds();
            if (bounds != null && bounds.length > 0) {
                sb.append(" super "); //NOI18N
                len += 7;
                len += appendType(eu, sb, bounds[0], false, false);
            }
        } else {
            TypeVariable tv = type.asTypeVariable();
            if (tv != null) {
                len += createLink(sb, null, tv.simpleTypeName());
                Type[] bounds = tv.bounds();
                if (typeVar && bounds != null && bounds.length > 0) {
                    sb.append(" extends "); //NOI18N
                    len += 9;
                    for (int i = 0; i < bounds.length; i++) {
                        len += appendType(eu, sb, bounds[i], false, false);
                        if (i < bounds.length - 1) {
                            sb.append(" & "); //NOI18N
                            len += 3;
                        }
                    }
                }
            } else {
                String tName = type.simpleTypeName();
                ClassDoc cd = type.asClassDoc();
                if (cd != null && cd.isAnnotationType())
                    tName = "@" + tName; //NOI18N
                len += createLink(sb, eu.elementFor(type.asClassDoc()), tName);
                ParameterizedType pt = type.asParameterizedType();
                if (pt != null) {
                    Type[] targs = pt.typeArguments();
                    if (targs.length > 0) {
                        sb.append("&lt;"); //NOI18N
                        for (int j = 0; j < targs.length; j++) {
                            len += appendType(eu, sb, targs[j], false, false);
                            if (j < targs.length - 1) {
                                sb.append(","); //NOI18N
                                len++;
                            }
                        }
                        sb.append("&gt;"); //NOI18N
                        len += 2;
                    }
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
        if (e != null && e.asType().getKind() != TypeKind.ERROR) {
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
