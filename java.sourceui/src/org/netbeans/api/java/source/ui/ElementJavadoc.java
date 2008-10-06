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
package org.netbeans.api.java.source.ui;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.util.*;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.AbstractAction;
import javax.swing.Action;
import com.sun.javadoc.*;
import com.sun.javadoc.AnnotationDesc.ElementValuePair;
import java.net.MalformedURLException;
import java.util.ArrayList;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;

/** Utility class for viewing Javadoc comments as HTML.
 *
 * @author Dusan Balek, Petr Hrebejk
 */
public class ElementJavadoc {
    
    private static final String API = "/api";                                   //NOI18N
    private static final Set<String> LANGS;
    
    static {
        Locale[] availableLocales = Locale.getAvailableLocales();
        Set<String> locNames = new HashSet<String>((int) (availableLocales.length/.75f) + 1);
        for (Locale locale : availableLocales) {
            locNames.add(locale.toString());
        }
        LANGS = Collections.unmodifiableSet(locNames);
    }
    
    private ElementJavadoc() {
    }

    private ClasspathInfo cpInfo;
    //private Doc doc;
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
    private static final String LITERAL_TAG = "@literal"; //NOI18N
    private static final String DEPRECATED_TAG = "@deprecated"; //NOI18N
    private static final String VALUE_TAG = "@value"; //NOI18N
    
    /** Creates an object describing the Javadoc of given element. The object
     * is capable of getting the text formated into HTML, resolve the links,
     * jump to external javadoc.
     * 
     * @param compilationInfo CompilationInfo
     * @param element Element the javadoc is required for
     * @return ElementJavadoc describing the javadoc
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

    /** Resolves a link contained in the Javadoc comment to an object 
     * describing the linked javadoc
     * @param link Link which has to be resolved
     * @return ElementJavadoc describing the javadoc of liked element
     */
    public ElementJavadoc resolveLink(final String link) {
        final ElementJavadoc[] ret = new ElementJavadoc[1];
        try {
            final ElementHandle<? extends Element> linkDoc = links.get(link);
            FileObject fo = linkDoc != null ? SourceUtils.getFile(linkDoc, cpInfo) : null;
            if (fo != null && fo.isFolder() && linkDoc.getKind() == ElementKind.PACKAGE) {
                fo = fo.getFileObject("package-info", "java"); //NOI18N
            }
            if (cpInfo == null && fo == null) {
                //link cannot be resolved by this element
                try {
                    URL u = docURL != null ? new URL(docURL, link) : new URL(link);
                    ret[0] = new ElementJavadoc(u);
                } catch (MalformedURLException ex) {
                    // ignore
                }
                return ret[0];
            }
            JavaSource js = fo != null ? JavaSource.forFileObject(fo) : JavaSource.create(cpInfo);
            if (js != null) {
                js.runUserActionTask(new Task<CompilationController>() {
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
                                    if( uri.isAbsolute() ) {
                                        ret[0] = new ElementJavadoc( uri.toURL() );
                                    } else if (docURL != null) {
                                        try {
                                            ret[0] = new ElementJavadoc(new URL(docURL, link));
                                        } catch (MalformedURLException ex) {
                                            // ignore
                                        }
                                    }
                                } 
                            }
                        }
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
        Doc doc = eu.javaDocFor(element);
        boolean localized = false;
        if (element != null) {
            docURL = SourceUtils.getJavadoc(element, cpInfo);
            localized = isLocalized(docURL, element);
            if (!localized) {
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
                }
            }
        }
        this.content = prepareContent(eu, doc, localized);
    }
    
    private ElementJavadoc(URL url) {
        assert url != null;
        this.content = null;
        this.docURL = url;
    }

    // Private section ---------------------------------------------------------
    
    
    private boolean isLocalized (final URL docURL, final Element element) {
        if (docURL == null) {
            return false;
        }
        Element pkg = element;
        while (pkg.getKind() != ElementKind.PACKAGE) {
            pkg = pkg.getEnclosingElement();
            if (pkg == null) {
                return false;
            }
        }
        String pkgBinName = ((PackageElement)pkg).getQualifiedName().toString();
        String surl = docURL.toString();
        int index = surl.lastIndexOf('/');      //NOI18N
        if (index < 0) {
            return false;
        }
        index-=(pkgBinName.length()+1);
        if (index < 0) {
            return false;
        }
        index-=API.length();        
        if (index < 0 || !surl.regionMatches(index,API,0,API.length())) {
            return false;
        }
        int index2 = surl.lastIndexOf('/', index-1);  //NOI18N
        if (index2 < 0) {
            return false;
        }
        String lang = surl.substring(index2+1, index);        
        return LANGS.contains(lang);
    }
           
    /**
     * Creates javadoc content
     * @param eu element utilities to find out elements
     * @param doc javac javadoc model
     * @param useJavadoc preffer javadoc to sources
     * @return Javadoc content
     */
    private String prepareContent(ElementUtilities eu, Doc doc, final boolean useJavadoc) {
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
            } else if (doc instanceof PackageDoc) {
                sb.append(getPackageHeader(eu, (PackageDoc)doc));
            }
            sb.append("<p>"); //NOI18N
            if (!useJavadoc) {
                Tag[] inlineTags = doc.inlineTags();
                if (doc.isMethod()) {
                    MethodDoc mdoc = (MethodDoc)doc;
                    List<Tag> inheritedTags = null;
                    if (inlineTags.length == 0) {
                        inheritedTags = new ArrayList<Tag>();
                    } else {
                        for (Tag tag : inlineTags) {
                            if (INHERIT_DOC_TAG.equals(tag.kind())) {
                                if (inheritedTags == null)
                                    inheritedTags = new ArrayList<Tag>();
                            }
                        }
                    }
                    Tag[] returnTags = mdoc.tags(RETURN_TAG);
                    List<Tag> inheritedReturnTags = null;
                    if (!"void".equals(mdoc.returnType().typeName())) { //NOI18N
                        if (returnTags.length == 0) {
                            inheritedReturnTags = new ArrayList<Tag>();
                        } else {
                            List<Tag> tags = new ArrayList<Tag>();
                            for(Tag tag : returnTags) {
                                for(Tag t : tag.inlineTags()) {
                                    if (INHERIT_DOC_TAG.equals(t.kind())) {
                                        if (inheritedReturnTags == null)
                                            inheritedReturnTags = new ArrayList<Tag>();
                                    }
                                    tags.add(t);
                                }
                            }
                            returnTags = tags.toArray(new Tag[tags.size()]);
                        }
                    }
                    Set<Integer> paramPos = new HashSet<Integer>();
                    Map<Integer, List<Tag>> paramTags = null;
                    Map<Integer, ParamTag> inheritedParamTags = null;
                    Map<Integer, List<Tag>> inheritedParamInlineTags = null;
                    Parameter[] parameters = mdoc.parameters();
                    if (parameters.length > 0) {
                        paramTags = new LinkedHashMap<Integer, List<Tag>>();
                        for (int i = 0; i < parameters.length; i++)
                            paramPos.add(i);
                    }
                    for(ParamTag tag : mdoc.paramTags()) {
                        Integer pos = paramPos(mdoc, tag);
                        if (paramPos.remove(pos)) {
                            List<Tag> tags = new ArrayList<Tag>();
                            paramTags.put(pos, tags);
                            for(Tag t : tag.inlineTags()) {
                                if (INHERIT_DOC_TAG.equals(t.kind())) {
                                    if (inheritedParamTags == null)
                                        inheritedParamTags = new LinkedHashMap<Integer,ParamTag>();
                                    if (inheritedParamInlineTags == null)
                                        inheritedParamInlineTags = new LinkedHashMap<Integer, List<Tag>>();
                                    paramPos.add(pos);
                                } else {
                                    tags.add(t);
                                }
                            }
                        }
                    }
                    if (!paramPos.isEmpty()) {
                        if (inheritedParamTags == null)
                            inheritedParamTags = new LinkedHashMap<Integer,ParamTag>();
                        if (inheritedParamInlineTags == null)
                            inheritedParamInlineTags = new LinkedHashMap<Integer, List<Tag>>();
                    }
                    Set<String> throwsTypes = new HashSet<String>();
                    List<ThrowsTag> throwsTags = new ArrayList<ThrowsTag>();
                    Map<String, List<Tag>> throwsInlineTags = new HashMap<String, List<Tag>>();
                    Map<String, ThrowsTag> inheritedThrowsTags = null;
                    Map<String, List<Tag>> inheritedThrowsInlineTags = null;
                    for (Type exc : mdoc.thrownExceptionTypes())
                        throwsTypes.add(exc.typeName());
                    for(ThrowsTag tag : mdoc.throwsTags()) {
                        throwsTypes.remove(tag.exceptionName());
                        List<Tag> tags = new ArrayList<Tag>();
                        throwsTags.add(tag);
                        throwsInlineTags.put(tag.exceptionName(), tags);
                        for(Tag t : tag.inlineTags()) {
                            if (INHERIT_DOC_TAG.equals(t.kind())) {
                                if (inheritedThrowsTags == null)
                                    inheritedThrowsTags = new LinkedHashMap<String, ThrowsTag>();
                                if (inheritedThrowsInlineTags == null)
                                    inheritedThrowsInlineTags = new HashMap<String, List<Tag>>();
                                throwsTypes.add(tag.exceptionName());
                            } else {
                                tags.add(t);
                            }
                        }
                    }
                    if (!throwsTypes.isEmpty()) {
                        if (inheritedThrowsTags == null)
                            inheritedThrowsTags = new LinkedHashMap<String, ThrowsTag>();
                        if (inheritedThrowsInlineTags == null)
                            inheritedThrowsInlineTags = new HashMap<String, List<Tag>>();
                    }
                    if (inheritedTags != null && inheritedTags.isEmpty() ||
                            inheritedReturnTags != null && inheritedReturnTags.isEmpty() ||
                            paramPos != null && !paramPos.isEmpty() ||
                            throwsTypes != null && !throwsTypes.isEmpty())
                        inheritedDocFor(mdoc, mdoc.containingClass(), inheritedTags, inheritedReturnTags,
                                paramPos, inheritedParamTags, inheritedParamInlineTags,
                                throwsTypes, inheritedThrowsTags, inheritedThrowsInlineTags);
                    if (inheritedTags != null && !inheritedTags.isEmpty()) {
                        if (inlineTags.length == 0) {
                            inlineTags = inheritedTags.toArray(new Tag[inheritedTags.size()]);
                        } else {
                            List<Tag> tags = new ArrayList<Tag>();
                            for (Tag tag : inlineTags) {
                                if (INHERIT_DOC_TAG.equals(tag.kind()))
                                    tags.addAll(inheritedTags);
                                else
                                    tags.add(tag);
                            }
                            inlineTags = tags.toArray(new Tag[tags.size()]);
                        }
                    }
                    if (inheritedReturnTags != null && !inheritedReturnTags.isEmpty()) {
                        if (returnTags.length == 0) {
                            returnTags = inheritedReturnTags.toArray(new Tag[inheritedReturnTags.size()]);
                        } else {
                            List<Tag> tags = new ArrayList<Tag>();
                            for(Tag tag : returnTags) {
                                for(Tag t : tag.inlineTags()) {
                                    if (INHERIT_DOC_TAG.equals(t.kind()))
                                        tags.addAll(inheritedReturnTags);
                                    else
                                        tags.add(t);
                                }
                            }
                            returnTags = tags.toArray(new Tag[tags.size()]);
                        }
                    }
                    List<Integer> ppos = new ArrayList<Integer>();
                    if (inheritedParamTags != null && !inheritedParamTags.isEmpty()) {
                        for (Integer pos : paramTags.keySet()) {
                            ppos.add(pos);
                            ParamTag paramTag = inheritedParamTags.remove(pos);
                            List<Tag> tags = inheritedParamInlineTags.get(pos);
                            if (tags != null && !tags.isEmpty()) {
                                List<Tag> inTags = paramTags.get(pos);
                                inTags.clear();
                                for (Tag tag : paramTag.inlineTags()) {
                                    if (INHERIT_DOC_TAG.equals(tag.kind()))
                                        inTags.addAll(tags);
                                    else
                                        inTags.add(tag);
                                }
                            }
                        }
                        for (Integer pos : inheritedParamTags.keySet()) {
                            ppos.add(pos);
                            List<Tag> tags = inheritedParamInlineTags.get(pos);
                            if (tags != null && !tags.isEmpty())
                                paramTags.put(pos, tags);
                        }
                    }
                    if (inheritedThrowsTags != null && !inheritedThrowsTags.isEmpty()) {
                        for (ThrowsTag throwsTag : throwsTags) {
                            inheritedThrowsTags.remove(throwsTag.exceptionName());
                            List<Tag> tags = inheritedThrowsInlineTags.get(throwsTag.exceptionName());
                            if (tags != null && !tags.isEmpty()) {
                                List<Tag> inTags = throwsInlineTags.get(throwsTag.exceptionName());
                                inTags.clear();
                                for (Tag tag : throwsTag.inlineTags()) {
                                    if (INHERIT_DOC_TAG.equals(tag.kind()))
                                        inTags.addAll(tags);
                                    else
                                        inTags.add(tag);
                                }
                            }
                        }
                        for (Map.Entry<String, ThrowsTag> entry : inheritedThrowsTags.entrySet()) {
                            throwsTags.add(entry.getValue());
                            List<Tag> tags = inheritedThrowsInlineTags.get(entry.getKey());
                            if (tags != null && !tags.isEmpty())
                                throwsInlineTags.put(entry.getKey(), tags);
                        }
                    }
                    if (inlineTags.length > 0 || doc.tags().length > 0) {
                        sb.append(getDeprecatedTag(eu, doc));
                        sb.append(inlineTags(eu, doc, inlineTags));
                        sb.append("</p><p>"); //NOI18N
                        sb.append(getMethodTags(eu, mdoc, returnTags, paramTags,
                                throwsTags, throwsInlineTags));
                        sb.append("</p>"); //NOI18N
                        return sb.toString();
                    }
                } else {
                    if (inlineTags.length > 0 || doc.tags().length > 0) {
                        sb.append(getDeprecatedTag(eu, doc));
                        sb.append(inlineTags(eu, doc, inlineTags));
                        sb.append("</p><p>"); //NOI18N
                        sb.append(getTags(eu, doc));
                        sb.append("</p>"); //NOI18N
                        return sb.toString();
                    }
                }
            }
            String jdText = docURL != null ? HTMLJavadocParser.getJavadocText(docURL, false) : null;
            if (jdText != null)
                sb.append(jdText);
            else
                sb.append(NbBundle.getMessage(ElementJavadoc.class, "javadoc_content_not_found")); //NOI18N
            sb.append("</p>"); //NOI18N
            return sb.toString();
        }
        sb.append(NbBundle.getMessage(ElementJavadoc.class, "javadoc_content_not_found")); //NOI18N
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
        sb.append(getAnnotations(eu, mdoc.annotations()));
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
        sb.append(getAnnotations(eu, fdoc.annotations()));
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
        sb.append(getAnnotations(eu, cdoc.annotations()));
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
        sb.append("<b>").append(cdoc.simpleTypeName()); //NOI18N
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
    
    private CharSequence getPackageHeader(ElementUtilities eu, PackageDoc pdoc) {
        StringBuilder sb = new StringBuilder();
        sb.append("<p><tt>"); //NOI18N
        sb.append(getAnnotations(eu, pdoc.annotations()));
        sb.append("package <b>").append(pdoc.name()).append("</b>"); //NOI18N
        sb.append("</tt></p>"); //NOI18N
        return sb;
    }
    
    private CharSequence getAnnotations(ElementUtilities eu, AnnotationDesc[] annotations) {
        StringBuilder sb = new StringBuilder();
        for (AnnotationDesc annotationDesc : annotations) {
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
    
    private CharSequence getMethodTags(ElementUtilities eu, MethodDoc doc, Tag[] returnTags, Map<Integer, List<Tag>> paramInlineTags,
            List<ThrowsTag> throwsTags, Map<String, List<Tag>> throwsInlineTags) {
        StringBuilder ret = new StringBuilder();
        if (returnTags.length > 0) {
            ret.append(inlineTags(eu, doc, returnTags));
            ret.append("<br>"); //NOI18N
        }
        StringBuilder par = new StringBuilder();
        if (paramInlineTags != null) {
            Parameter[] parameters = doc.parameters();
            for (Integer pos : paramInlineTags.keySet()) {
                par.append("<code>").append(parameters[pos].name()).append("</code>"); //NOI18N
                List<Tag> tags = paramInlineTags.get(pos);
                Tag[] its = tags.toArray(new Tag[tags.size()]);                
                if (its.length > 0) {
                    CharSequence cs = inlineTags(eu, doc, its);
                    if (cs.length() > 0) {
                        par.append(" - "); //NOI18N
                        par.append(cs);
                    }
                }
                par.append("<br>"); //NOI18N            
            }
        }
        StringBuilder tpar = new StringBuilder();
        ParamTag[] tpTags = doc.typeParamTags();
        if (tpTags.length > 0) {
            for (ParamTag pTag : tpTags) {
                tpar.append("<code>").append(pTag.parameterName()).append("</code>"); //NOI18N
                Tag[] its = pTag.inlineTags();
                if (its.length > 0) {
                    CharSequence cs = inlineTags(eu, doc, its);
                    if (cs.length() > 0) {
                        tpar.append(" - "); //NOI18N
                        tpar.append(cs);
                    }
                }
                tpar.append("<br>"); //NOI18N            
            }
        }
        StringBuilder thr = new StringBuilder();
        if (throwsTags != null) {
            for (ThrowsTag throwsTag : throwsTags) {
                thr.append("<code>"); //NOI18N
                Type exType = throwsTag.exceptionType();
                if (exType != null) {
                    createLink(thr, eu.elementFor(exType.asClassDoc()), exType.simpleTypeName());
                } else {
                    thr.append(throwsTag.exceptionName());
                }
                thr.append("</code>"); //NOI18N
                List<Tag> tags = throwsInlineTags.get(throwsTag.exceptionName());
                Tag[] its = tags == null ? throwsTag.inlineTags() : tags.toArray(new Tag[tags.size()]);                
                if (its.length > 0) {
                    CharSequence cs = inlineTags(eu, doc, its);
                    if (cs.length() > 0) {
                        thr.append(" - "); //NOI18N
                        thr.append(cs);
                    }
                }
                thr.append("<br>"); //NOI18N
            }
        }
        StringBuilder see = new StringBuilder();
        String since = null;
        for (Tag tag : doc.tags()) {
            if (SEE_TAG.equals(tag.kind())) {
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
        if (tpar.length() > 0) {
            sb.append("<b>").append(NbBundle.getMessage(ElementJavadoc.class, "JCD-typeparams")).append("</b><blockquote>").append(tpar).append("</blockquote>"); //NOI18N
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
    
    private CharSequence getTags(ElementUtilities eu, Doc doc) {
        StringBuilder see = new StringBuilder();
        StringBuilder par = new StringBuilder();
        StringBuilder thr = new StringBuilder();
        StringBuilder ret = new StringBuilder();
        String since = null;
        for (Tag tag : doc.tags()) {
            if (PARAM_TAG.equals(tag.kind()) && !doc.isMethod()) {
                par.append("<code>").append(((ParamTag)tag).parameterName()).append("</code>"); //NOI18N
                Tag[] its = tag.inlineTags();
                if (its.length > 0) {
                    par.append(" - "); //NOI18N
                    par.append(inlineTags(eu, doc, its));
                }
                par.append("<br>"); //NOI18N
            } else if (THROWS_TAG.equals(tag.kind()) && !doc.isMethod()) {
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
            } else if (RETURN_TAG.equals(tag.kind()) && !doc.isMethod()) {
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
            if (DEPRECATED_TAG.equals(tag.kind())) {
                sb.append("<b>").append(NbBundle.getMessage(ElementJavadoc.class, "JCD-deprecated")).append("</b> <i>").append(inlineTags(eu, doc, tag.inlineTags())).append("</i></p><p>"); //NOI18N
                break;
            }
        }
        return sb;
    }
    
    private CharSequence inlineTags(ElementUtilities eu, Doc doc, Tag[] tags) {
        StringBuilder sb = new StringBuilder();
        for (Tag tag : tags) {
            if (SEE_TAG.equals(tag.kind())) {
                SeeTag stag = (SeeTag)tag;
                if (VALUE_TAG.equals(tag.name())) {
                    Doc mdoc = stag.referencedMember();
                    if (mdoc == null && tag.text().length() == 0)
                        mdoc = stag.holder();
                    if (mdoc != null && mdoc.isField()) {
                        try {
                            sb.append(XMLUtil.toElementContent(((FieldDoc)mdoc).constantValueExpression()));
                        } catch (IOException ioe) {
                        }
                    }                    
                } else {
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
                }
            } else if (INHERIT_DOC_TAG.equals(tag.kind())) {
                if (doc.isMethod()) {
                    MethodDoc mdoc = ((MethodDoc)doc).overriddenMethod();
                    if (mdoc != null)
                        sb.append(inlineTags(eu, mdoc, mdoc.inlineTags()));
                }
            } else if (LITERAL_TAG.equals(tag.kind())) {
                try {
                    sb.append(XMLUtil.toElementContent(tag.text()));
                } catch (IOException ioe){}
            } else if (CODE_TAG.equals(tag.kind())) {
                sb.append("<code>"); //NOI18N
                try {
                    sb.append(XMLUtil.toElementContent(tag.text()));
                } catch (IOException ioe){}
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
    
    private void inheritedDocFor(MethodDoc mdoc, ClassDoc cdoc, List<Tag> inlineTags, List<Tag> returnTags,
            Set<Integer> paramPos, Map<Integer, ParamTag> paramTags, Map<Integer, List<Tag>> paramInlineTags,
            Set<String> throwsTypes, Map<String, ThrowsTag> throwsTags, Map<String, List<Tag>> throwsInlineTags) {
        for (ClassDoc ifaceDoc : cdoc.interfaces()) {
            for (MethodDoc methodDoc : ifaceDoc.methods(false)) {
                if (mdoc.overrides(methodDoc)) {
                    List<Tag> inheritedInlineTags = null;
                    if (inlineTags != null && inlineTags.isEmpty()) {
                        for (Tag tag : methodDoc.inlineTags()) {
                            if (INHERIT_DOC_TAG.equals(tag.kind())) {
                                if (inheritedInlineTags == null)
                                    inheritedInlineTags = new ArrayList<Tag>();
                            } else {
                                inlineTags.add(tag);
                            }
                        }
                    }
                    List<Tag> inheritedReturnTags = null;
                    if (returnTags != null && returnTags.isEmpty()) {
                        for(Tag tag : methodDoc.tags(RETURN_TAG)) {
                            for(Tag t : tag.inlineTags()) {
                                if (INHERIT_DOC_TAG.equals(t.kind())) {
                                    if (inheritedReturnTags == null)
                                        inheritedReturnTags = new ArrayList<Tag>();
                                } else {
                                    returnTags.add(t);
                                }
                            }
                        }
                    }
                    Set<Integer> inheritedParamPos = null;
                    Map<Integer, ParamTag> inheritedParamTags = null;
                    Map<Integer, List<Tag>> inheritedParamInlineTags = null;
                    if (paramTags != null && paramPos != null && !paramPos.isEmpty()) {
                        for(ParamTag tag : methodDoc.paramTags()) {
                            Integer pos = paramPos(methodDoc, tag);
                            if (paramPos.remove(pos)) {
                                List<Tag> tags = new ArrayList<Tag>();
                                paramTags.put(pos, tag);
                                paramInlineTags.put(pos, tags);
                                for(Tag t : tag.inlineTags()) {
                                    if (INHERIT_DOC_TAG.equals(t.kind())) {
                                        if (inheritedParamPos == null)
                                            inheritedParamPos = new HashSet<Integer>();
                                        if (inheritedParamTags == null)
                                            inheritedParamTags = new LinkedHashMap<Integer,ParamTag>();
                                        if (inheritedParamInlineTags == null)
                                            inheritedParamInlineTags = new LinkedHashMap<Integer, List<Tag>>();
                                        inheritedParamPos.add(pos);
                                    } else {
                                        tags.add(t);
                                    }
                                }
                            }
                        }
                    }
                    Set<String> inheritedThrowsTypes = null;
                    Map<String, ThrowsTag> inheritedThrowsTags = null;
                    Map<String, List<Tag>> inheritedThrowsInlineTags = null;
                    if (throwsTags != null && throwsTypes != null && !throwsTypes.isEmpty()) {
                        for(ThrowsTag tag : methodDoc.throwsTags()) {
                            if (throwsTypes.remove(tag.exceptionName())) {
                                List<Tag> tags = new ArrayList<Tag>();
                                throwsTags.put(tag.exceptionName(), tag);
                                throwsInlineTags.put(tag.exceptionName(), tags);
                                for(Tag t : tag.inlineTags()) {
                                    if (INHERIT_DOC_TAG.equals(t.kind())) {
                                        if (inheritedThrowsTypes == null)
                                            inheritedThrowsTypes = new HashSet<String>();
                                        if (inheritedThrowsTags == null)
                                            inheritedThrowsTags = new LinkedHashMap<String, ThrowsTag>();
                                        if (inheritedThrowsInlineTags == null)
                                            inheritedThrowsInlineTags = new HashMap<String, List<Tag>>();
                                        inheritedThrowsTypes.add(tag.exceptionName());
                                    } else {
                                        tags.add(t);
                                    }
                                }
                            }
                        }
                    }
                    if (inheritedInlineTags != null || inheritedReturnTags != null ||
                            inheritedParamPos != null && inheritedParamTags != null)
                        inheritedDocFor(mdoc, ifaceDoc, inheritedInlineTags, inheritedReturnTags,
                                inheritedParamPos, inheritedParamTags, inheritedParamInlineTags,
                                inheritedThrowsTypes, inheritedThrowsTags, inheritedThrowsInlineTags);
                    if (inheritedInlineTags != null && !inheritedInlineTags.isEmpty()) {
                        inlineTags.clear();
                        for (Tag tag : methodDoc.inlineTags()) {
                            if (INHERIT_DOC_TAG.equals(tag.kind()))
                                inlineTags.addAll(inheritedInlineTags);
                            else
                                inlineTags.add(tag);
                        }
                    }
                    if (inheritedReturnTags != null && !inheritedReturnTags.isEmpty()) {
                        returnTags.clear();
                        for(Tag tag : methodDoc.tags(RETURN_TAG)) {
                            for(Tag t : tag.inlineTags()) {
                                if (INHERIT_DOC_TAG.equals(t.kind()))
                                    returnTags.addAll(inheritedReturnTags);
                                else
                                    returnTags.add(t);
                            }
                        }
                    }
                    if (inheritedParamTags != null && !inheritedParamTags.isEmpty()) {
                        for (Integer pos : inheritedParamTags.keySet()) {
                            List<Tag> tags = paramInlineTags.get(pos);
                            tags.clear();
                            for(Tag t : paramTags.get(pos).inlineTags()) {
                                if (INHERIT_DOC_TAG.equals(t.kind()))
                                    tags.addAll(inheritedParamInlineTags.get(pos));
                                else
                                    tags.add(t);
                            }
                        }
                    }
                    if (inheritedThrowsTags != null && !inheritedThrowsTags.isEmpty()) {
                        for (String param : inheritedThrowsTags.keySet()) {
                            List<Tag> tags = throwsInlineTags.get(param);
                            tags.clear();
                            for(Tag t : throwsTags.get(param).inlineTags()) {
                                if (INHERIT_DOC_TAG.equals(t.kind()))
                                    tags.addAll(inheritedThrowsInlineTags.get(param));
                                else
                                    tags.add(t);
                            }
                        }
                    }
                    break;
                }
            }
            if ((inlineTags == null || !inlineTags.isEmpty()) &&
                    (returnTags == null || !returnTags.isEmpty()) && 
                    (paramPos == null || paramPos.isEmpty()) &&
                    (throwsTypes == null || throwsTypes.isEmpty()))
                return;
        }
        for (ClassDoc ifaceDoc : cdoc.interfaces()) {
            inheritedDocFor(mdoc, ifaceDoc, inlineTags, returnTags,
                    paramPos, paramTags, paramInlineTags,
                    throwsTypes, throwsTags, throwsInlineTags);
            if ((inlineTags == null || !inlineTags.isEmpty()) &&
                    (returnTags == null || !returnTags.isEmpty()) && 
                    (paramPos == null || paramPos.isEmpty()) &&
                    (throwsTypes == null || throwsTypes.isEmpty()))
                return;
        }
        if (cdoc.superclass() != null) { //NOI18N
            for (MethodDoc methodDoc : cdoc.superclass().methods(false)) {
                if (mdoc.overrides(methodDoc)) {
                    List<Tag> inheritedInlineTags = null;
                    if (inlineTags != null && inlineTags.isEmpty()) {
                        for (Tag tag : methodDoc.inlineTags()) {
                            if (INHERIT_DOC_TAG.equals(tag.kind())) {
                                if (inheritedInlineTags == null)
                                    inheritedInlineTags = new ArrayList<Tag>();
                            } else {
                                inlineTags.add(tag);
                            }
                        }
                    }
                    List<Tag> inheritedReturnTags = null;
                    if (returnTags != null && returnTags.isEmpty()) {
                        for(Tag tag : methodDoc.tags(RETURN_TAG)) {
                            for(Tag t : tag.inlineTags()) {
                                if (INHERIT_DOC_TAG.equals(t.kind())) {
                                    if (inheritedReturnTags == null)
                                        inheritedReturnTags = new ArrayList<Tag>();
                                } else {
                                    returnTags.add(t);
                                }
                            }
                        }
                    }
                    Set<Integer> inheritedParamNames = null;
                    Map<Integer, ParamTag> inheritedParamTags = null;
                    Map<Integer, List<Tag>> inheritedParamInlineTags = null;
                    if (paramTags != null && paramPos != null && !paramPos.isEmpty()) {
                        for(ParamTag tag : methodDoc.paramTags()) {
                            Integer pos = paramPos(methodDoc, tag);
                            if (paramPos.remove(pos)) {
                                List<Tag> tags = new ArrayList<Tag>();
                                paramTags.put(pos, tag);
                                paramInlineTags.put(pos, tags);
                                for(Tag t : tag.inlineTags()) {
                                    if (INHERIT_DOC_TAG.equals(t.kind())) {
                                        if (inheritedParamNames == null)
                                            inheritedParamNames = new HashSet<Integer>();
                                        if (inheritedParamTags == null)
                                            inheritedParamTags = new LinkedHashMap<Integer, ParamTag>();
                                        if (inheritedParamInlineTags == null)
                                            inheritedParamInlineTags = new LinkedHashMap<Integer, List<Tag>>();
                                        inheritedParamNames.add(pos);
                                    } else {
                                        tags.add(t);
                                    }
                                }
                            }
                        }
                    }
                    Set<String> inheritedThrowsTypes = null;
                    Map<String, ThrowsTag> inheritedThrowsTags = null;
                    Map<String, List<Tag>> inheritedThrowsInlineTags = null;
                    if (throwsTags != null && throwsTypes != null && !throwsTypes.isEmpty()) {
                        for(ThrowsTag tag : methodDoc.throwsTags()) {
                            if (throwsTypes.remove(tag.exceptionName())) {
                                List<Tag> tags = new ArrayList<Tag>();
                                throwsTags.put(tag.exceptionName(), tag);
                                throwsInlineTags.put(tag.exceptionName(), tags);
                                for(Tag t : tag.inlineTags()) {
                                    if (INHERIT_DOC_TAG.equals(t.kind())) {
                                        if (inheritedThrowsTypes == null)
                                            inheritedThrowsTypes = new HashSet<String>();
                                        if (inheritedThrowsTags == null)
                                            inheritedThrowsTags = new LinkedHashMap<String, ThrowsTag>();
                                        if (inheritedThrowsInlineTags == null)
                                            inheritedThrowsInlineTags = new HashMap<String, List<Tag>>();
                                        inheritedThrowsTypes.add(tag.exceptionName());
                                    } else {
                                        tags.add(t);
                                    }
                                }
                            }
                        }
                    }
                    if (inheritedInlineTags != null || inheritedReturnTags != null ||
                            inheritedParamNames != null && inheritedParamTags != null)
                        inheritedDocFor(mdoc, cdoc.superclass(), inheritedInlineTags, inheritedReturnTags,
                                inheritedParamNames, inheritedParamTags, inheritedParamInlineTags,
                                inheritedThrowsTypes, inheritedThrowsTags, inheritedThrowsInlineTags);
                    if (inheritedInlineTags != null && !inheritedInlineTags.isEmpty()) {
                        inlineTags.clear();
                        for (Tag tag : methodDoc.inlineTags()) {
                            if (INHERIT_DOC_TAG.equals(tag.kind()))
                                inlineTags.addAll(inheritedInlineTags);
                            else
                                inlineTags.add(tag);
                        }
                    }
                    if (inheritedReturnTags != null && !inheritedReturnTags.isEmpty()) {
                        returnTags.clear();
                        for(Tag tag : methodDoc.tags(RETURN_TAG)) {
                            for(Tag t : tag.inlineTags()) {
                                if (INHERIT_DOC_TAG.equals(t.kind()))
                                    returnTags.addAll(inheritedReturnTags);
                                else
                                    returnTags.add(t);
                            }
                        }
                    }
                    if (inheritedParamTags != null && !inheritedParamTags.isEmpty()) {
                        for (Integer pos : inheritedParamTags.keySet()) {
                            List<Tag> tags = paramInlineTags.get(pos);
                            tags.clear();
                            for(Tag t : paramTags.get(pos).inlineTags()) {
                                if (INHERIT_DOC_TAG.equals(t.kind()))
                                    tags.addAll(inheritedParamInlineTags.get(pos));
                                else
                                    tags.add(t);
                            }
                        }
                    }
                    if (inheritedThrowsTags != null && !inheritedThrowsTags.isEmpty()) {
                        for (String param : inheritedThrowsTags.keySet()) {
                            List<Tag> tags = throwsInlineTags.get(param);
                            tags.clear();
                            for(Tag t : throwsTags.get(param).inlineTags()) {
                                if (INHERIT_DOC_TAG.equals(t.kind()))
                                    tags.addAll(inheritedParamInlineTags.get(param));
                                else
                                    tags.add(t);
                            }
                        }
                    }
                    break;
                }
            }
            if (inlineTags != null && inlineTags.isEmpty() ||
                    returnTags != null && returnTags.isEmpty() ||
                    paramPos != null && !paramPos.isEmpty() ||
                    throwsTypes != null && !throwsTypes.isEmpty())
                inheritedDocFor(mdoc, cdoc.superclass(), inlineTags, returnTags,
                        paramPos, paramTags, paramInlineTags,
                        throwsTypes, throwsTags, throwsInlineTags);
        }
    }
    
    private int paramPos(MethodDoc methodDoc, ParamTag paramTag) {
        Parameter[] parameters = methodDoc.parameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (parameter.name().equals(paramTag.parameterName()))
                return i;
        }
        return -1;

    }
}
