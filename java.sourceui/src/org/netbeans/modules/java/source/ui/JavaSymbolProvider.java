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

package org.netbeans.modules.java.source.ui;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.SimpleTypeVisitor6;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.modules.java.source.usages.ClasspathInfoAccessor;
import org.netbeans.modules.java.source.usages.ResultConvertor;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.spi.jumpto.symbol.SymbolProvider;
import org.netbeans.spi.jumpto.type.SearchType;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Zezula
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.jumpto.symbol.SymbolProvider.class)
public class JavaSymbolProvider implements SymbolProvider {

    private static final Logger LOGGER = Logger.getLogger(JavaSymbolProvider.class.getName());
    
    private static final String CAPTURED_WILDCARD = "<captured wildcard>"; //NOI18N
    private static final String UNKNOWN = "<unknown>"; //NOI18N
    
    private volatile boolean canceled;

    public String name() {
        return "java symbols";  //NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(JavaTypeProvider.class, "MSG_JavaSymbols");
    }
    
    public void computeSymbolNames(final Context context, final Result result) {
        try {
            final SearchType st = context.getSearchType();
            String _ident = context.getText();
            ClassIndex.NameKind _kind;
            boolean _caseSensitive;
            switch (st) {
                case PREFIX:
                    _kind = ClassIndex.NameKind.PREFIX;
                    _caseSensitive = true;
                    break;
                case REGEXP:
                    _kind = ClassIndex.NameKind.REGEXP;
                    _ident = removeNonJavaChars(_ident);            
                    _ident = _ident.replace( "*", ".*" ).replace( '?', '.' ) + ".*";       //NOI18N
                    _caseSensitive = true;
                    break;
                case CAMEL_CASE:
                    _kind = ClassIndex.NameKind.CAMEL_CASE;
                    _caseSensitive = true;
                    break;
                case EXACT_NAME:
                    _kind = ClassIndex.NameKind.SIMPLE_NAME;
                    _caseSensitive = true;
                    break;
                case CASE_INSENSITIVE_PREFIX:
                    _kind = ClassIndex.NameKind.CASE_INSENSITIVE_PREFIX;
                    _caseSensitive = false;
                    break;
                case CASE_INSENSITIVE_EXACT_NAME:
                    _kind = ClassIndex.NameKind.CASE_INSENSITIVE_REGEXP;
                    _caseSensitive = false;
                    break;
                case CASE_INSENSITIVE_REGEXP:
                    _kind = ClassIndex.NameKind.CASE_INSENSITIVE_REGEXP;
                    _ident = removeNonJavaChars(_ident);            
                    _ident = _ident.replace( "*", ".*" ).replace( '?', '.' ) + ".*";       //NOI18N
                    _caseSensitive = false;
                    break;
                default:
                    throw new IllegalArgumentException();
            }            
            final String ident = _ident;
            final ClassIndex.NameKind kind = _kind;
            final boolean caseSensitive = _caseSensitive;
            try {
                final ClassIndexManager manager = ClassIndexManager.getDefault();

                Collection<FileObject> roots = QuerySupport.findRoots(
                        (Project)null,
                        Collections.singleton(ClassPath.SOURCE),
                        Collections.<String>emptySet(),
                        Collections.<String>emptySet());

                final Set<URL> rootUrls = new HashSet<URL>();
                for(FileObject root : roots) {
                    if (canceled) {
                        return;
                    }
                    URL rootUrl;
                    try {
                        rootUrl = root.getURL();
                        rootUrls.add(rootUrl);
                    } catch (FileStateInvalidException fsie) {
                    }
                }

                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "Querying following roots:"); //NOI18N
                    for (URL url : rootUrls) {
                        LOGGER.log(Level.FINE, "  {0}", url); //NOI18N
                    }
                    LOGGER.log(Level.FINE, "-------------------------"); //NOI18N
                }
                
                for (URL url : rootUrls) {
                    if (canceled) {
                        return;
                    }
                    final FileObject root = URLMapper.findFileObject(url);
                    if (root == null) {
                        continue;
                    }

                    final Project project = FileOwnerQuery.getOwner(root);
                    final ClassIndexImpl impl = manager.getUsagesQuery(root.getURL());
                    if (impl != null) {
                        final Map<ElementHandle<TypeElement>,Set<String>> r = new HashMap<ElementHandle<TypeElement>,Set<String>>();
                        impl.getDeclaredElements(ident, kind, ResultConvertor.elementHandleConvertor(),r);
                        if (!r.isEmpty()) {
                            //final ClasspathInfo cpInfo = ClasspathInfo.create(root);
                            final ClasspathInfo cpInfo = ClasspathInfoAccessor.getINSTANCE().create(root,null,true,true,false);
                            final JavaSource js = JavaSource.create(cpInfo);
                            js.runUserActionTask(new Task<CompilationController>() {
                                public void run (final CompilationController controller) {
                                    for (final Map.Entry<ElementHandle<TypeElement>,Set<String>> p : r.entrySet()) {
                                        final ElementHandle<TypeElement> owner = p.getKey();
                                        final TypeElement te = owner.resolve(controller);
                                        final Set<String> idents = p.getValue();
                                        if (te != null) {
                                            if (idents.contains(getSimpleName(te))) {
                                                result.addResult(new JavaSymbolDescriptor(te.getSimpleName().toString(), te.getKind(), te.getModifiers(), owner, ElementHandle.create(te), project, root));
                                            }
                                            for (Element ne : te.getEnclosedElements()) {
                                                if (idents.contains(getSimpleName(ne))) {
                                                    result.addResult(new JavaSymbolDescriptor(getDisplayName(ne), ne.getKind(), ne.getModifiers(), owner, ElementHandle.create(ne), project, root));
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                private String getSimpleName (final Element element) {
                                    String result = element.getSimpleName().toString();
                                    if (!caseSensitive) {
                                        result = result.toLowerCase();
                                    }
                                    return result;
                                }
                            },true);
                        }                    

                    }
                }

            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
            catch (InterruptedException ie) {
                return;
            }
        } finally {
            cleanup();
        }
    }
    
    
    private static String getDisplayName (final Element e) {
        assert e != null;
        if (e.getKind() == ElementKind.METHOD || e.getKind() == ElementKind.CONSTRUCTOR) {
            StringBuilder sb = new StringBuilder();
            sb.append(e.getSimpleName());
            sb.append('('); //NOI18N
            ExecutableElement ee = (ExecutableElement) e;
            final List<? extends VariableElement> vl = ee.getParameters();
            for (Iterator<? extends VariableElement> it = vl.iterator(); it.hasNext();) {
                final VariableElement v = it.next();
                final TypeMirror tm = v.asType();                
                sb.append(getTypeName(tm, false, true));
                if (it.hasNext()) {
                    sb.append(", ");    //NOI18N
                }
            }
            sb.append(')');
            return sb.toString();
        }
        return e.getSimpleName().toString();
    }
    
    private static CharSequence getTypeName(TypeMirror type, boolean fqn, boolean varArg) {
	if (type == null)
            return ""; //NOI18N
        return new TypeNameVisitor(varArg).visit(type, fqn);
    }
    
    private static class TypeNameVisitor extends SimpleTypeVisitor6<StringBuilder,Boolean> {
        
        private boolean varArg;
        private boolean insideCapturedWildcard = false;
        
        private TypeNameVisitor(boolean varArg) {
            super(new StringBuilder());
            this.varArg = varArg;
        }
        
        @Override
        public StringBuilder defaultAction(TypeMirror t, Boolean p) {
            return DEFAULT_VALUE.append(t);
        }
        
        @Override
        public StringBuilder visitDeclared(DeclaredType t, Boolean p) {
            Element e = t.asElement();
            if (e instanceof TypeElement) {
                TypeElement te = (TypeElement)e;
                DEFAULT_VALUE.append((p ? te.getQualifiedName() : te.getSimpleName()).toString());
                Iterator<? extends TypeMirror> it = t.getTypeArguments().iterator();
                if (it.hasNext()) {
                    DEFAULT_VALUE.append("<"); //NOI18N
                    while(it.hasNext()) {
                        visit(it.next(), p);
                        if (it.hasNext())
                            DEFAULT_VALUE.append(", "); //NOI18N
                    }
                    DEFAULT_VALUE.append(">"); //NOI18N
                }
                return DEFAULT_VALUE;                
            } else {
                return DEFAULT_VALUE.append(UNKNOWN); //NOI18N
            }
        }
                        
        @Override
        public StringBuilder visitArray(ArrayType t, Boolean p) {
            boolean isVarArg = varArg;
            varArg = false;
            visit(t.getComponentType(), p);
            return DEFAULT_VALUE.append(isVarArg ? "..." : "[]"); //NOI18N
        }

        @Override
        public StringBuilder visitTypeVariable(TypeVariable t, Boolean p) {
            Element e = t.asElement();
            if (e != null) {
                String name = e.getSimpleName().toString();
                if (!CAPTURED_WILDCARD.equals(name))
                    return DEFAULT_VALUE.append(name);
            }
            DEFAULT_VALUE.append("?"); //NOI18N
            if (!insideCapturedWildcard) {
                insideCapturedWildcard = true;
                TypeMirror bound = t.getLowerBound();
                if (bound != null && bound.getKind() != TypeKind.NULL) {
                    DEFAULT_VALUE.append(" super "); //NOI18N
                    visit(bound, p);
                } else {
                    bound = t.getUpperBound();
                    if (bound != null && bound.getKind() != TypeKind.NULL) {
                        DEFAULT_VALUE.append(" extends "); //NOI18N
                        if (bound.getKind() == TypeKind.TYPEVAR)
                            bound = ((TypeVariable)bound).getLowerBound();
                        visit(bound, p);
                    }
                }
                insideCapturedWildcard = false;
            }
            return DEFAULT_VALUE;
        }

        @Override
        public StringBuilder visitWildcard(WildcardType t, Boolean p) {
            int len = DEFAULT_VALUE.length();
            DEFAULT_VALUE.append("?"); //NOI18N
            TypeMirror bound = t.getSuperBound();
            if (bound == null) {
                bound = t.getExtendsBound();
                if (bound != null) {
                    DEFAULT_VALUE.append(" extends "); //NOI18N
                    if (bound.getKind() == TypeKind.WILDCARD)
                        bound = ((WildcardType)bound).getSuperBound();
                    visit(bound, p);
                } else if (len == 0) {
                    bound = SourceUtils.getBound(t);
                    if (bound != null && (bound.getKind() != TypeKind.DECLARED || !((TypeElement)((DeclaredType)bound).asElement()).getQualifiedName().contentEquals("java.lang.Object"))) { //NOI18N
                        DEFAULT_VALUE.append(" extends "); //NOI18N
                        visit(bound, p);
                    }
                }
            } else {
                DEFAULT_VALUE.append(" super "); //NOI18N
                visit(bound, p);
            }
            return DEFAULT_VALUE;
        }

        @Override
        public StringBuilder visitError(ErrorType t, Boolean p) {
            Element e = t.asElement();
            if (e instanceof TypeElement) {
                TypeElement te = (TypeElement)e;
                return DEFAULT_VALUE.append((p ? te.getQualifiedName() : te.getSimpleName()).toString());
            }
            return DEFAULT_VALUE;
        }
    }
    
    private static String removeNonJavaChars(String text) {
       StringBuilder sb = new StringBuilder();

       for( int i = 0; i < text.length(); i++) {
           char c = text.charAt(i);
           if( Character.isJavaIdentifierPart(c) || c == '*' || c == '?') {
               sb.append(c);
           }
       }
       return sb.toString();
    }

    public void cancel() {
        canceled = true;
    }

    public void cleanup() {        
        canceled = false;
    }

}
