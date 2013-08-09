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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.api.java.source;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;

import javax.annotation.processing.Completion;
import javax.annotation.processing.Processor;
import javax.lang.model.element.*;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.ElementFilter;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Scope.ImportScope;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.TypeVar;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.comp.Check;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;

import java.net.URISyntaxException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.util.ElementScanner6;
import javax.swing.SwingUtilities;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.matching.Matcher;
import org.netbeans.api.java.source.matching.Occurrence;
import org.netbeans.api.java.source.matching.Pattern;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.modules.java.preprocessorbridge.spi.ImportProcessor;
import org.netbeans.modules.java.source.ElementHandleAccessor;
import org.netbeans.modules.java.source.JavadocHelper;
import org.netbeans.modules.java.source.indexing.JavaCustomIndexer;
import org.netbeans.modules.java.source.parsing.ClasspathInfoProvider;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.java.source.save.DiffContext;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.modules.java.source.usages.ClasspathInfoAccessor;
import org.netbeans.modules.java.source.usages.ExecutableFilesIndex;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 *
 * @author Dusan Balek
 */
public class SourceUtils {    
     
    private static final Logger LOG = Logger.getLogger(SourceUtils.class.getName());

    private SourceUtils() {}
    
    /**
     * @since 0.21
     */
    public static TokenSequence<JavaTokenId> getJavaTokenSequence(final TokenHierarchy hierarchy, final int offset) {
        if (hierarchy != null) {
            TokenSequence<?> ts = hierarchy.tokenSequence();
            while(ts != null && (offset == 0 || ts.moveNext())) {
                ts.move(offset);
                if (ts.language() == JavaTokenId.language())
                    return (TokenSequence<JavaTokenId>)ts;
                if (!ts.moveNext() && !ts.movePrevious())
                    return null;
                ts = ts.embedded();
            }
        }
        return null;
    }
    
    /**
     * Find duplicates for provided expression
     * @param info CompilationInfo
     * @param searchingFor expression which is being searched
     * @param scope scope for search
     * @param cancel option to cancel find duplicates
     * @return set of TreePaths representing duplicates
     * @since 0.85
     */
    public static Set<TreePath> computeDuplicates(CompilationInfo info, TreePath searchingFor, TreePath scope, AtomicBoolean cancel) {
        Set<TreePath> result = new HashSet<>();
        
        for (Occurrence od : Matcher.create(info).setCancel(cancel).setSearchRoot(scope).match(Pattern.createSimplePattern(searchingFor))) {
            result.add(od.getOccurrenceRoot());
        }

        return result;
    }    
    
    public static boolean checkTypesAssignable(CompilationInfo info, TypeMirror from, TypeMirror to) {
        Context c = ((JavacTaskImpl) info.impl.getJavacTask()).getContext();
        if (from.getKind() == TypeKind.TYPEVAR) {
            Types types = Types.instance(c);
            TypeVar t = types.substBound((TypeVar)from, com.sun.tools.javac.util.List.of((Type)from), com.sun.tools.javac.util.List.of(types.boxedTypeOrType((Type)to)));
            return info.getTypes().isAssignable(t.getUpperBound(), to)
                    || info.getTypes().isAssignable(to, t.getUpperBound());
        }
        if (from.getKind() == TypeKind.WILDCARD) {
            from = Types.instance(c).upperBound((Type)from);
        }
        return Check.instance(c).checkType(null, (Type)from, (Type)to).getKind() != TypeKind.ERROR;
    }
    
    public static TypeMirror getBound(WildcardType wildcardType) {
        Type.TypeVar bound = ((Type.WildcardType)wildcardType).bound;
        return bound != null ? bound.bound : null;
    }

    /**
     * Returns a list of completions for an annotation attribute value suggested by
     * annotation processors.
     * 
     * @param info the CompilationInfo used to resolve annotation processors
     * @param element the element being annotated
     * @param annotation the (perhaps partial) annotation being applied to the element
     * @param member the annotation member to return possible completions for
     * @param userText source code text to be completed
     * @return suggested completions to the annotation member
     * 
     * @since 0.57
     */
    public static List<? extends Completion> getAttributeValueCompletions(CompilationInfo info, Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
        List<Completion> completions = new LinkedList<>();
        if (info.getPhase().compareTo(Phase.ELEMENTS_RESOLVED) >= 0) {
            String fqn = ((TypeElement) annotation.getAnnotationType().asElement()).getQualifiedName().toString();
            Iterable<? extends Processor> processors = info.impl.getJavacTask().getProcessors();
            if (processors != null) {
                for (Processor processor : processors) {
                    boolean match = false;
                    for (String sat : processor.getSupportedAnnotationTypes()) {
                        if ("*".equals(sat)) { //NOI18N
                            match = true;
                            break;
                        } else if (sat.endsWith(".*")) { //NOI18N
                            sat = sat.substring(0, sat.length() - 1);
                            if (fqn.startsWith(sat)) {
                                match = true;
                                break;
                            }
                        } else if (fqn.equals(sat)) {
                            match = true;
                            break;
                        }
                    }
                    if (match) {
                        try {
                            for (Completion c : processor.getCompletions(element, annotation, member, userText)) {
                                completions.add(c);
                            }
                        } catch (Exception e) {
                            Logger.getLogger(processor.getClass().getName()).log(Level.INFO, e.getMessage(), e);
                        }
                    }
                }
            }
        }
        return completions;
    }

    /**
     * Returns the type element within which this member or constructor
     * is declared. Does not accept packages
     * If this is the declaration of a top-level type (a non-nested class
     * or interface), returns null.
     *
     * @return the type declaration within which this member or constructor
     * is declared, or null if there is none
     * @throws IllegalArgumentException if the provided element is a package element
     * @deprecated use {@link ElementUtilities#enclosingTypeElement(javax.lang.model.element.Element)}
     */
    public static @Deprecated TypeElement getEnclosingTypeElement( Element element ) throws IllegalArgumentException {
        return ElementUtilities.enclosingTypeElementImpl(element);
    }
    
    public static TypeElement getOutermostEnclosingTypeElement( Element element ) {
	
	Element ec =  getEnclosingTypeElement( element );
	if (ec == null) {
	    ec = element;
	}
	
	while( ec.getEnclosingElement().getKind().isClass() || 
	       ec.getEnclosingElement().getKind().isInterface() ) {
	
	    ec = ec.getEnclosingElement();
	}
		
	return (TypeElement)ec;
    }

    /**
     * Returns an array containing the JVM signature of the {@link ElementHandle}.
     * @param handle to obtain the JVM signature for.
     * @return an array containing the JVM signature. The signature depends on
     * the {@link ElementHandle}'s {@link ElementKind}. For class or package
     * it returns a single element array containing the class (package) binary
     * name (JLS section 13.1). For field (method) it returns three element array
     * containing owner class binary name (JLS section 13.1) in the first element,
     * field (method) name in the second element and JVM type (JVM method formal
     * parameters (JVMS section 2.10.1)) in the third element.
     * @since 0.84
     */
    @NonNull
    public static String[] getJVMSignature(@NonNull final ElementHandle<?> handle) {
        Parameters.notNull("handle", handle);   //NOI18N
        return ElementHandleAccessor.getInstance().getJVMSignature(handle);
    }


    /**Resolve full qualified name in the given context. Adds import statement as necessary.
     * Returns name that resolved to a given FQN in given context (either simple name
     * or full qualified name). Handles import conflicts.
     * 
     * <br><b>Note:</b> if the <code>info</code> passed to this method is not an instance of {@link WorkingCopy},
     * missing import statement is added from a separate modification task executed asynchronously.
     * <br><b>Note:</b> after calling this method, it is not permitted to rewrite copy.getCompilationUnit().
     * 
     * @param info CompilationInfo over which the method should work
     * @param context in which the fully qualified should be resolved
     * @param fqn the fully qualified name to resolve
     * @return either a simple name or a FQN that will resolve to given fqn in given context
     */
    public static String resolveImport(final CompilationInfo info, final TreePath context, final String fqn) throws NullPointerException, IOException {
        if (info == null)
            throw new NullPointerException();
        if (context == null)
            throw new NullPointerException();
        if (fqn == null)
            throw new NullPointerException();
        
        CodeStyle cs = DiffContext.getCodeStyle(info);
        if (cs.useFQNs())
            return fqn;
        CompilationUnitTree cut = info.getCompilationUnit();
        final Trees trees = info.getTrees();
        final Scope scope = trees.getScope(context);
        String qName = fqn;
        StringBuilder sqName = new StringBuilder();
        boolean clashing = false;
        ElementUtilities eu = info.getElementUtilities();
        ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
            @Override
            public boolean accept(Element e, TypeMirror type) {
                return (e.getKind().isClass() || e.getKind().isInterface()) && trees.isAccessible(scope, (TypeElement)e);
            }
        };
        Element toImport = null;
        while(qName != null && qName.length() > 0) {
            int lastDot = qName.lastIndexOf('.');
            Element element;
            if ((element = info.getElements().getTypeElement(qName)) != null) {
                clashing = false;
                String simple = qName.substring(lastDot < 0 ? 0 : lastDot + 1);
                if (sqName.length() > 0)
                    sqName.insert(0, '.');
                sqName.insert(0, simple);
                if (cs.useSingleClassImport() && (toImport == null || !cs.importInnerClasses())) {
                    toImport = element;
                }
                boolean matchFound = false;
                for(Element e : eu.getLocalMembersAndVars(scope, acceptor)) {
                    if (simple.contentEquals(e.getSimpleName())) {
                        //either a clash or already imported:
                        if (qName.contentEquals(((TypeElement)e).getQualifiedName())) {
                            return sqName.toString();
                        } else {
                            clashing = true;
                        }
                        matchFound = true;
                        break;
                    }
                }
                if (!matchFound) {
                    for(TypeElement e : eu.getGlobalTypes(acceptor)) {
                        if (simple.contentEquals(e.getSimpleName())) {
                            //either a clash or already imported:
                            if (qName.contentEquals(e.getQualifiedName())) {
                                return sqName.toString();
                            } else {
                                clashing = true;
                            }
                            break;
                        }
                    }
                }
                if (cs.importInnerClasses())
                    break;
            } else if ((element = info.getElements().getPackageElement(qName)) != null) {
                if (toImport == null || GeneratorUtilities.checkPackagesForStarImport(qName, cs))
                    toImport = element;
                break;
            }
            qName = lastDot < 0 ? null : qName.substring(0, lastDot);
        }
        if (clashing || toImport == null)
            return fqn;
        
        //not imported/visible so far by any means:
        String topLevelLanguageMIMEType = info.getFileObject().getMIMEType();
        if ("text/x-java".equals(topLevelLanguageMIMEType)){ //NOI18N
            final Set<Element> elementsToImport = Collections.singleton(toImport);
            if (info instanceof WorkingCopy) {
                CompilationUnitTree nue = (CompilationUnitTree) ((WorkingCopy)info).resolveRewriteTarget(cut);
                ((WorkingCopy)info).rewrite(info.getCompilationUnit(), GeneratorUtilities.get((WorkingCopy)info).addImports(nue, elementsToImport));
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ModificationResult.runModificationTask(Collections.singletonList(info.getSnapshot().getSource()), new UserTask() {
                                @Override
                                public void run(ResultIterator resultIterator) throws Exception {
                                    WorkingCopy copy = WorkingCopy.get(resultIterator.getParserResult());
                                    copy.toPhase(Phase.ELEMENTS_RESOLVED);
                                    copy.rewrite(copy.getCompilationUnit(), GeneratorUtilities.get(copy).addImports(copy.getCompilationUnit(), elementsToImport));
                                }
                            }).commit();
                        } catch (Exception e) {
                            Exceptions.printStackTrace(e);
                        }
                    }
                });
            }
            JCCompilationUnit unit = (JCCompilationUnit) info.getCompilationUnit();
            ImportScope importScope = new ImportScope(unit.namedImportScope.owner);
            for (Symbol symbol : unit.namedImportScope.getElements()) {
                importScope.enter(symbol);
                unit.namedImportScope = importScope;
                unit.namedImportScope.enterIfAbsent((Symbol) toImport);
            }        
        } else { // embedded java, look up the handler for the top level language
            Lookup lookup = MimeLookup.getLookup(MimePath.get(topLevelLanguageMIMEType));
            Collection<? extends ImportProcessor> instances = lookup.lookupAll(ImportProcessor.class);

            for (ImportProcessor importsProcesor : instances) {
                importsProcesor.addImport(info.getDocument(), fqn);
            }
            
        }
        return sqName.toString();
    }
    
    /**
     * Returns a {@link FileObject} in which the Element is defined.
     * @param element for which the {@link FileObject} should be located
     * @param cpInfo the classpaths context
     * @return the defining {@link FileObject} or null if it cannot be
     * found
     * 
     * @deprecated use {@link getFile(ElementHandle, ClasspathInfo)}
     */
    public static FileObject getFile (Element element, final ClasspathInfo cpInfo) {
        Parameters.notNull("element", element); //NOI18N
        Parameters.notNull("cpInfo", cpInfo);   //NOI18N
        
        Element prev = element.getKind() == ElementKind.PACKAGE ? element : null;
        while (element.getKind() != ElementKind.PACKAGE) {
            prev = element;
            element = element.getEnclosingElement();
        }
        final ElementKind kind = prev.getKind();
        if (!(kind.isClass() || kind.isInterface() || kind == ElementKind.PACKAGE)) {
            return null;
        }        
        final ElementHandle<? extends Element> handle = ElementHandle.create(prev);
        return getFile (handle, cpInfo);
    }
    
    /**
     * Returns a {@link FileObject} of the source file in which the handle is declared.
     * @param handle to find the {@link FileObject} for
     * @param cpInfo classpaths for resolving handle
     * @return {@link FileObject} or null when the source file cannot be found
     */
    public static FileObject getFile (final ElementHandle<? extends Element> handle, final ClasspathInfo cpInfo) {
        Parameters.notNull("handle", handle);
        Parameters.notNull("cpInfo", cpInfo);        
        try {
            boolean pkg = handle.getKind() == ElementKind.PACKAGE;
            String[] signature = handle.getSignature();
            assert signature.length >= 1;
            final ClassPath[] cps = 
                new ClassPath[] {
                    cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE),
                    createClassPath(cpInfo,ClasspathInfo.PathKind.OUTPUT),
                    createClassPath(cpInfo,ClasspathInfo.PathKind.BOOT),                    
                    createClassPath(cpInfo,ClasspathInfo.PathKind.COMPILE),
                };
           String pkgName, className = null;
            if (pkg) {
                pkgName = FileObjects.convertPackage2Folder(signature[0]);
            }
            else {
                int index = signature[0].lastIndexOf('.');                          //NOI18N
                if (index<0) {
                    pkgName = "";                                             //NOI18N
                    className = signature[0];
                }
                else {
                    pkgName = FileObjects.convertPackage2Folder(signature[0].substring(0,index));
                    className = signature[0].substring(index+1);
                }
            }
            final List<Pair<FileObject,ClassPath>> fos = findAllResources(pkgName, cps);
            for (Pair<FileObject,ClassPath> pair : fos) {                
                FileObject root = pair.second().findOwnerRoot(pair.first());
                if (root == null)
                    continue;
                FileObject[] sourceRoots = SourceForBinaryQuery.findSourceRoots(root.toURL()).getRoots();                        
                ClassPath sourcePath = ClassPathSupport.createClassPath(sourceRoots);
                LinkedList<FileObject> folders = new LinkedList<>(sourcePath.findAllResources(pkgName));
                if (pkg) {
                    return folders.isEmpty() ? pair.first() : folders.get(0);
                } else {               
                    final boolean caseSensitive = isCaseSensitive ();
                    final Object fnames = getSourceFileNames (className);
                    folders.addFirst(pair.first());
                    if (fnames instanceof String) {
                        FileObject match = findMatchingChild((String)fnames, folders, caseSensitive);
                        if (match != null) {
                            return match;
                        }
                    } else {
                        for (String candidate : (List<String>)fnames) {
                            FileObject match = findMatchingChild(candidate, folders, caseSensitive);
                            if (match != null) {
                                return match;
                            }
                        }
                    }
                    FileObject foundFo;
                    if (sourceRoots.length == 0) {
                        foundFo = findSource (signature[0],root);
                    }
                    else {
                        foundFo = findSource (signature[0],sourceRoots);
                    }
                    if (foundFo != null) {
                        return foundFo;
                    }
                }
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        return null;        
    }
    
    private static FileObject findMatchingChild(String sourceFileName, Collection<FileObject> folders, boolean caseSensitive) {
        final Match matchSet = caseSensitive ? new CaseSensitiveMatch(sourceFileName) : new CaseInsensitiveMatch(sourceFileName);
        for (FileObject folder : folders) {
            for (FileObject child : folder.getChildren()) {
                if (matchSet.apply(child)) {
                    return child;
                }
            }
        }
        return null;
    }
    
    @NonNull
    private static List<Pair<FileObject, ClassPath>> findAllResources(
            @NonNull final String resourceName,
            @NonNull final ClassPath[] cps) {
        final List<Pair<FileObject,ClassPath>> result = new ArrayList<>();
        for (ClassPath cp : cps) {
            for (FileObject fo : cp.findAllResources(resourceName)) {
                result.add(Pair.<FileObject,ClassPath>of(fo, cp));
            }            
        }
        return result;
    }
    
    private static FileObject findSource (final String binaryName, final FileObject... fos) throws IOException {
        final ClassIndexManager cim = ClassIndexManager.getDefault();
        try {
            for (FileObject fo : fos) {
                ClassIndexImpl ci = cim.getUsagesQuery(fo.toURL(), true);
                if (ci != null) {
                    String sourceName = ci.getSourceName(binaryName);
                    if (sourceName != null) {
                        FileObject result = fo.getFileObject(sourceName);
                        if (result != null) {
                            return result;
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            //canceled, pass - returns null
        }
        return null;
    }

    private static abstract class Match {

        private final String name;

        Match(final String names) {
            this.name = names;
        }

        final boolean apply(final FileObject fo) {
            final String foName = fo.getName();
            return match(foName,name) && isJava(fo);
        }

        protected abstract boolean match(String name1, String name2);

        private boolean isJava(final FileObject fo) {
            return  JavaDataLoader.JAVA_EXTENSION.equalsIgnoreCase(fo.getExt()) && fo.isData();
        }
    }

    private static class CaseSensitiveMatch extends Match {

        CaseSensitiveMatch(final String name) {
            super(name);
        }

        @Override
        protected boolean match(String name1, String name2) {
            return name1.equals(name2);
        }
    }

    private static class CaseInsensitiveMatch extends Match {

        CaseInsensitiveMatch(final String name) {
            super(name);
        }

        @Override
        protected boolean match(String name1, String name2) {
            return name1.equalsIgnoreCase(name2);
        }
    }
    
    /**
     * Finds {@link URL} of a javadoc page for given element when available. This method 
     * uses {@link JavadocForBinaryQuery} to find the javadoc page for the give element.
     * For {@link PackageElement} it returns the package-summary.html for given package.
     * @param element to find the Javadoc for
     * @param cpInfo classpaths used to resolve (currently unused)
     * @return the URL of the javadoc page or null when the javadoc is not available.
     */
    public static URL getJavadoc (final Element element, final ClasspathInfo cpInfo) {      
        JavadocHelper.TextStream page = JavadocHelper.getJavadoc(element);
        if (page == null) {
            return null;
        } else {
            page.close();
            return page.getLocation();
        }
    }
    
    /**
     * Tests whether the initial scan is in progress.
     */
    public static boolean isScanInProgress () {
        return IndexingManager.getDefault().isIndexing();
    }

    /**
     * Waits for the end of the initial scan, this helper method 
     * is designed for tests which require to wait for end of initial scan.
     * @throws InterruptedException is thrown when the waiting thread is interrupted.
     * @deprecated use {@link JavaSource#runWhenScanFinished}
     */
    public static void waitScanFinished () throws InterruptedException {
        try {
            class T extends UserTask implements ClasspathInfoProvider {
                private final ClassPath EMPTY_PATH = ClassPathSupport.createClassPath(new URL[0]);
                private final ClasspathInfo cpinfo = ClasspathInfo.create(EMPTY_PATH, EMPTY_PATH, EMPTY_PATH);
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    // no-op
                }

                @Override
                public ClasspathInfo getClasspathInfo() {
                    return cpinfo;
                }
            }
            Future<Void> f = ParserManager.parseWhenScanFinished(JavacParser.MIME_TYPE, new T());
            if (!f.isDone()) {
                f.get();
            }
        } catch (Exception ex) {
        }
    }
    
    
    /**
     * Returns the dependent source path roots for given source root.
     * It returns all the open project source roots which have either
     * direct or transitive dependency on the given source root.
     * @param root to find the dependent roots for
     * @return {@link Set} of {@link URL}s containing at least the
     * incoming root, never returns null.
     * @since 0.10
     */
    @NonNull
    @org.netbeans.api.annotations.common.SuppressWarnings(value = {"DMI_COLLECTION_OF_URLS"}, justification="URLs have never host part")
    public static Set<URL> getDependentRoots (@NonNull final URL root) {
        return getDependentRoots(root, true);
    }
    
    /**
     * Returns the dependent source path roots for given source root. It returns
     * all the source roots which have either direct or transitive dependency on
     * the given source root.
     *
     * @param root to find the dependent roots for
     * @param filterNonOpenedProjects true if the results should only contain roots for
     * opened projects
     * @return {@link Set} of {@link URL}s containing at least the incoming
     * root, never returns null.
     * @since 0.110
     */
    @NonNull
    @org.netbeans.api.annotations.common.SuppressWarnings(value = {"DMI_COLLECTION_OF_URLS"}, justification="URLs have never host part")
    public static Set<URL> getDependentRoots(
        @NonNull final URL root,
        final boolean filterNonOpenedProjects) {
        final FileObject rootFO = URLMapper.findFileObject(root);
        if (rootFO != null) {
            return mapToURLs(QuerySupport.findDependentRoots(rootFO,filterNonOpenedProjects));
        } else {
            return Collections.<URL>singleton(root);
        }
    }
        
    //Helper methods
    
    /**
     * Returns classes declared in the given source file which have the main method.
     * @param fo source file
     * @return the classes containing main method
     * @throws IllegalArgumentException when file does not exist or is not a java source file.
     */
    public static Collection<ElementHandle<TypeElement>> getMainClasses (final @NonNull FileObject fo) {
        Parameters.notNull("fo", fo);   //NOI18N
        if (!fo.isValid()) {
            throw new IllegalArgumentException ("FileObject : " + FileUtil.getFileDisplayName(fo) + " is not valid.");  //NOI18N
        }
        if (fo.isVirtual()) {
            throw new IllegalArgumentException ("FileObject : " + FileUtil.getFileDisplayName(fo) + " is virtual.");  //NOI18N
        }
        final JavaSource js = JavaSource.forFileObject(fo);        
        if (js == null) {
            throw new IllegalArgumentException ();
        }
        try {
            final LinkedHashSet<ElementHandle<TypeElement>> result = new LinkedHashSet<> ();
            js.runUserActionTask(new Task<CompilationController>() {            
                @Override
                public void run(final CompilationController control) throws Exception {
                    if (control.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED).compareTo (JavaSource.Phase.ELEMENTS_RESOLVED)>=0) {
                        final List<TypeElement>  types = new ArrayList<>();
                        final ElementScanner6<Void,Void> visitor = new ElementScanner6<Void, Void>() {
                            @Override
                            public Void visitType(TypeElement e, Void p) {
                                if (e.getEnclosingElement().getKind() == ElementKind.PACKAGE
                                   || e.getModifiers().contains(Modifier.STATIC)) {
                                    types.add(e);
                                    return super.visitType(e, p);
                                } else {
                                    return null;
                                }
                            }
                        };
                        visitor.scan(control.getTopLevelElements(), null);
                        for (TypeElement type : types) {
                            for (ExecutableElement exec :  ElementFilter.methodsIn(control.getElements().getAllMembers(type))) {
                                if (SourceUtils.isMainMethod(exec)) {
                                    result.add (ElementHandle.create(type));
                                }
                            }
                        }
                    }
                }                

            }, true);
            return result;
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
            return Collections.<ElementHandle<TypeElement>>emptySet();
        }		
    }
    
    /**
     * Returns true when the class contains main method.
     * @param qualifiedName the fully qualified name of class
     * @param cpInfo the classpath used to resolve the class
     * @return true when the class contains a main method
     */
    public static boolean isMainClass (final String qualifiedName, ClasspathInfo cpInfo) {
        return isMainClass(qualifiedName, cpInfo, false);
    }
    
    /**
     * Returns true when the class contains main method.
     * @param qualifiedName the fully qualified name of class
     * @param cpInfo the classpath used to resolve the class
     * @param optimistic when true does only index check without parsing the file.
     * The optimistic check is faster but it works only for source file not for binaries
     * for which index does not exist. It also does not handle inheritance of the main method.
     * @return true when the class contains a main method
     * @since 0.71
     */
    public static boolean isMainClass (final String qualifiedName, ClasspathInfo cpInfo, boolean optimistic) {
        if (qualifiedName == null || cpInfo == null) {
            throw new IllegalArgumentException ();
        }
        //Fast path check by index - main in sources
        for (ClassPath.Entry entry : cpInfo.getClassPath(PathKind.SOURCE).entries()) {
            final Iterable<? extends URL> mainClasses = ExecutableFilesIndex.DEFAULT.getMainClasses(entry.getURL());
            try {
                final URI root = entry.getURL().toURI();
                for (URL mainClass : mainClasses) {
                    try {
                        URI relative = root.relativize(mainClass.toURI());
                        final String resourceNameNoExt = FileObjects.stripExtension(relative.getPath());
                        final String ffqn = FileObjects.convertFolder2Package(resourceNameNoExt,'/');  //NOI18N
                        if (qualifiedName.equals(ffqn)) {
                            final ClassPath bootCp = cpInfo.getClassPath(PathKind.BOOT);
                            if (bootCp.findResource(resourceNameNoExt + '.' + FileObjects.CLASS)!=null) {
                                //Resource in platform, fall back to slow path
                                break;
                            } else {
                                return true;
                            }
                        }
                    } catch (URISyntaxException e) {
                        LOG.log(Level.INFO, "Ignoring fast check for file: {0} due to: {1}", new Object[]{mainClass.toString(), e.getMessage()}); //NOI18N
                    }
                }
            } catch (URISyntaxException e) {
                LOG.log(Level.INFO, "Ignoring fast check for root: {0} due to: {1}", new Object[]{entry.getURL().toString(), e.getMessage()}); //NOI18N
            }
        }
        
        final boolean[] result = new boolean[]{false};
        if (!optimistic) {
            //Slow path fallback - for main in libraries
            JavaSource js = JavaSource.create(cpInfo);
            try {
                js.runUserActionTask(new Task<CompilationController>() {

                    @Override
                    public void run(CompilationController control) throws Exception {
                        final JavacElements elms = (JavacElements)control.getElements();
                        TypeElement type = elms.getTypeElementByBinaryName(qualifiedName);
                        if (type == null) {
                            return;
                        }
                        List<? extends ExecutableElement> methods = ElementFilter.methodsIn(elms.getAllMembers(type));
                        for (ExecutableElement method : methods) {
                            if (SourceUtils.isMainMethod(method)) {
                                result[0] = true;
                                break;
                            }
                        }
                    }

                }, true);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        return result[0];
    }
    
    /**
     * Returns true if the method is a main method
     * @param method to be checked
     * @return true when the method is a main method
     */
    public static boolean isMainMethod (final ExecutableElement method) {
        if (!"main".contentEquals(method.getSimpleName())) {                //NOI18N
            return false;
        }
        long flags = ((Symbol.MethodSymbol)method).flags();                 //faster
        if (((flags & Flags.PUBLIC) == 0) || ((flags & Flags.STATIC) == 0)) {
            return false;
        }
        if (method.getReturnType().getKind() != TypeKind.VOID) {
            return false;
        }
        List<? extends VariableElement> params = method.getParameters();
        if (params.size() != 1) {
            return false;
        }
        TypeMirror param = params.get(0).asType();
        if (param.getKind() != TypeKind.ARRAY) {
            return false;
        }
        ArrayType array = (ArrayType) param;
        TypeMirror compound = array.getComponentType();
        if (compound.getKind() != TypeKind.DECLARED) {
            return false;
        }
        if (!"java.lang.String".contentEquals(((TypeElement)((DeclaredType)compound).asElement()).getQualifiedName())) {   //NOI18N
            return false;
        }
        return true;
    }
    
    /**
     * Returns classes declared under the given source roots which have the main method.
     * @param sourceRoots the source roots
     * @return the classes containing the main methods
     * Currently this method is not optimized and may be slow
     */
    public static Collection<ElementHandle<TypeElement>> getMainClasses (final FileObject[] sourceRoots) {
        final List<ElementHandle<TypeElement>> result = new LinkedList<> ();
        for (final FileObject root : sourceRoots) {
            try {               
                final File rootFile = FileUtil.toFile(root);
                ClassPath bootPath = ClassPath.getClassPath(root, ClassPath.BOOT);
                ClassPath compilePath = ClassPath.getClassPath(root, ClassPath.COMPILE);
                ClassPath srcPath = ClassPathSupport.createClassPath(new FileObject[] {root});
                ClasspathInfo cpInfo = ClasspathInfo.create(bootPath, compilePath, srcPath);
                JavaSource js = JavaSource.create(cpInfo);
                js.runUserActionTask(new Task<CompilationController>() {
                    @Override
                    public void run(CompilationController control) throws Exception {
                        final URL rootURL = root.toURL();
                        Iterable<? extends URL> mainClasses = ExecutableFilesIndex.DEFAULT.getMainClasses(rootURL);                        
                        List<ElementHandle<TypeElement>> classes = new LinkedList<>();
                        for (URL mainClass : mainClasses) {
                            File mainFo = Utilities.toFile(URI.create(mainClass.toExternalForm()));
                            if (mainFo.exists()) {
                                classes.addAll(JavaCustomIndexer.getRelatedTypes(mainFo, rootFile));
                            }
                        }
                        for (ElementHandle<TypeElement> cls : classes) {
                            TypeElement te = cls.resolve(control);
                            if (te != null) {
                                Iterable<? extends ExecutableElement> methods = ElementFilter.methodsIn(te.getEnclosedElements());
                                for (ExecutableElement method : methods) {
                                    if (isMainMethod(method)) {
                                        if (isIncluded(cls, control.getClasspathInfo())) {
                                            result.add (cls);
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }, false);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
                return Collections.<ElementHandle<TypeElement>>emptySet();
            }
        }
        return result;
    }

    private static boolean isIncluded (final ElementHandle<TypeElement> element, final ClasspathInfo cpInfo) {
        FileObject fobj = getFile (element,cpInfo);
        if (fobj == null) {
            //Not source
            return true;
        }
        ClassPath sourcePath = cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE);
        for (ClassPath.Entry e : sourcePath.entries()) {
            FileObject root = e.getRoot ();
            if (root != null && FileUtil.isParentOf(root,fobj)) {
                return e.includes(fobj);
            }
        }
        return true;
    }
    
    private static boolean isCaseSensitive () {
        return ! new File ("a").equals (new File ("A"));    //NOI18N
    }
    
    /**
     * Returns candidate filenames given a classname. The return value is either 
     * a String (top-level class, no $) or List&lt;String> as the JLS permits $ in
     * class names. 
     */
    private static Object getSourceFileNames (String classFileName) {
        int max = classFileName.length() - 1;
        int index = classFileName.indexOf('$');
        if (index == -1) {
            return classFileName;
        }
        List<String> ll = new ArrayList<String>(3);
        do {
            ll.add(classFileName.substring(0, index));
            if (index >= max) {
                break;
            }
            index = classFileName.indexOf('$', index + 1);
        } while (index >= 0);
        ll.add(classFileName);
        return ll;
    }
        
    /**
     * @since 0.24
     */
    public static WildcardType resolveCapturedType(TypeMirror type) {
        if (type instanceof Type.CapturedType) {
            return ((Type.CapturedType) type).wildcard;
        } else {
            return null;
        }
    }
    
    // --------------- Helper methods of getFile () -----------------------------
    private static ClassPath createClassPath (ClasspathInfo cpInfo, PathKind kind) throws MalformedURLException {
	return ClasspathInfoAccessor.getINSTANCE().getCachedClassPath(cpInfo, kind);	
    }    
    
    // --------------- End of getFile () helper methods ------------------------------

    private static final int MAX_LEN = 6;
    /**
     * Utility method for generating method parameter names based on incoming
     * class name when source is unavailable.
     * <p/>
     * This method uses both subjective heuristics to follow common patterns
     * for common JDK classes, acronym creation for bicapitalized names, and
     * vowel and repeated character elision if that fails, to generate
     * readable, programmer-friendly method names.
     *
     * @param typeName The fqn of the parameter class
     * @param used A set of names that have already been used for parameters
     * and should not be reused, to avoid creating uncompilable code
     * @return A programmer-friendly parameter name (i.e. not arg0, arg1...)
     */
    static @NonNull String generateReadableParameterName (@NonNull String typeName, @NonNull Set<String> used) {
        boolean arr = typeName.indexOf ("[") > 0 || typeName.endsWith("..."); //NOI18N
        typeName = trimToSimpleName (typeName);
        String result = typeName.toLowerCase();
        //First, do some common, sane substitutions that are common java parlance
        if ( typeName.endsWith ( "Listener" ) ) { //NOI18N
            result = Character.toLowerCase(typeName.charAt(0)) + "l"; //NOI18N
        } else if ( "Object".equals (typeName)) { //NOI18N
            result = "o"; //NOI18N
        } else if ("Class".equals(typeName)) { //NOI18N
            result = "type"; //NOI18N
        } else if ( "InputStream".equals(typeName)) { //NOI18N
            result = "in"; //NOI18N
        } else if ( "OutputStream".equals(typeName)) {
            result = "out"; //NOI18N
        } else if ( "Runnable".equals(typeName)) {
            result = "r"; //NOI18N
        } else if ( "Lookup".equals(typeName)) {
            result = "lkp"; //NOI18N
        } else if ( typeName.endsWith ( "Stream" )) { //NOI18N
            result = "stream"; //NOI18N
        } else if ( typeName.endsWith ("Writer")) { //NOI18N
            result = "writer"; //NOI18N
        } else if ( typeName.endsWith ("Reader")) { //NOI18N
            result = "reader"; //NOI18N
        } else if ( typeName.endsWith ( "Panel" )) { //NOI18N
            result = "pnl"; //NOI18N
        } else if ( typeName.endsWith ( "Action" )) { //NOI18N
            result = "action"; //NOI18N
        }
        //Now see if we've made a large and unwieldy variable - people
        //usually prefer reasonably short but legible arguments
        if ( result.length () > MAX_LEN ) {
            //See if we can turn, say, NoClassDefFoundError into "ncdfe"
            result = tryToMakeAcronym ( typeName );
            //No luck?  We've probably got one long word like Component or Runnable
            if (result.length() > MAX_LEN) {
                //First, strip out vowels - people easily figure out words
                //missing vowels - common in abbreviations and spam mails
                result = elideVowelsAndRepetitions(result);
                if (result.length() > MAX_LEN) {
                    //Still too long?  Give up and give them a 1 character var name
                    result = new StringBuilder().append(
                            result.charAt(0)).toString().toLowerCase();
                }
            }
        }
        //Make sure we haven't killed everything - if so, use a generic version
        if ( result.trim ().length () == 0 ) {
            result = "value"; //NOI18N
        }
        //If it's an array, pluralize it (english language style - but better than nothing)
        if (arr) {
            result += "s"; //NOI18N
        }
        //Now make sure it's legal;  if not, make it a single letter
        if ( isPrimitiveTypeName ( result ) || !Utilities.isJavaIdentifier ( result ) ) {
            StringBuilder sb = new StringBuilder();
            sb.append (result.charAt(0));
            result = sb.toString();
        }
        //Now make sure we're not duplicating a variable name we already used
        String test = result;
        int revs = 0;
        while ( used.contains ( test ) ) {
            revs++;
            test = result + revs;
        }
        result = test;
        used.add ( result );
        return result;
    }

    /**
     * Trims to the simple class name and removes and generics
     *
     * @param typeName The class name
     * @return A simplified class name
     */
    private static String trimToSimpleName (String typeName) {
        String result = typeName;
        int ix = result.indexOf ("<"); //NOI18N
        if (ix > 0 && ix != typeName.length() - 1) {
            result = typeName.substring(0, ix);
        }
        if (result.endsWith ("...")) { //NOI18N
            result = result.substring (0, result.length() - 3);
        }
        ix = result.lastIndexOf ("$"); //NOI18N
        if (ix > 0 && ix != result.length() - 1) {
            result = result.substring(ix + 1);
        } else {
            ix = result.lastIndexOf("."); //NOI18N
            if (ix > 0 && ix != result.length() - 1) {
                result = result.substring(ix + 1);
            }
        }
        ix = result.indexOf ( "[" ); //NOI18N
        if ( ix > 0 ) {
            result = result.substring ( 0, ix );
        }
        return result;
    }

    /**
     * Removes vowels and repeated letters.  This is used to generate names
     * where the class name a single long word - e.g. abbreviate
     * Runnable to rnbl
     * @param name The name
     * @return A shortened version of it
     */
    private static String elideVowelsAndRepetitions (String name) {
        char[] chars = name.toCharArray();
        StringBuilder sb = new StringBuilder();
        char last = 0;
        char lastUsed = 0;
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (Character.isDigit(c)) {
                continue;
            }
            if (i == 0 || Character.isUpperCase(c)) {
                if (lastUsed != c) {
                    sb.append (c);
                    lastUsed = c;
                }
            } else if (c != last && !isVowel(c)) {
                if (lastUsed != c) {
                    sb.append (c);
                    lastUsed = c;
                }
            }
            last = c;
        }
        return sb.toString();
    }

    private static boolean isVowel(char c) {
        return Arrays.binarySearch(VOWELS, c) >= 0;
    }

    /**
     * Vowels in various indo-european-based languages
     */
    private static final char[] VOWELS = new char[] {
    //IMPORTANT:  This array is sorted.  If you add to it,
    //add in the correct place or Arrays.binarySearch will break on it
    '\u0061', '\u0065', '\u0069', '\u006f', '\u0075', '\u0079', '\u00e9', '\u00ea',  //NOI18N
    '\u00e8', '\u00e1', '\u00e2', '\u00e6', '\u00e0', '\u03b1', '\u00e3',  //NOI18N
    '\u00e5', '\u00e4', '\u00eb', '\u00f3', '\u00f4', '\u0153', '\u00f2',  //NOI18N
    '\u03bf', '\u00f5', '\u00f6', '\u00ed', '\u00ee', '\u00ec', '\u03b9',  //NOI18N
    '\u00ef', '\u00fa', '\u00fb', '\u00f9', '\u03d2', '\u03c5', '\u00fc',  //NOI18N
    '\u0430', '\u043e', '\u044f', '\u0438', '\u0439', '\u0435', '\u044b',  //NOI18N
    '\u044d', '\u0443', '\u044e', };

    //PENDING:  The below would be much prettier;  whether it survives
    //cross-platform encoding issues in hg is another question;  the hg diff generated
    //was incorrect
/*
    'a', 'e', 'i', 'o', 'u', 'y', 'à', 'á', //NOI18N
    'â', 'ã', 'ä', 'å', 'æ', 'è', 'é', //NOI18N
    'ê', 'ë', 'ì', 'í', 'î', 'ï', 'ò', //NOI18N
    'ó', 'ô', 'õ', 'ö', 'ù', 'ú', 'û', //NOI18N
    'ü', 'œ', 'α', 'ι', 'ο', 'υ', 'ϒ', //NOI18N
    'а', 'е', 'и', 'й', 'о', 'у', 'ы', //NOI18N
    'э', 'ю', 'я'}; //NOI18N
*/
    /**
     * Determine if a string matches a java primitive type.  Used in generating reasonable variable names.
     */
    private static boolean isPrimitiveTypeName (String typeName) {
        return (
                //Whoa, ascii art!
                "void".equals ( typeName ) || //NOI18N
                "int".equals ( typeName ) || //NOI18N
                "long".equals ( typeName ) || //NOI18N
                "float".equals ( typeName ) || //NOI18N
                "double".equals ( typeName ) || //NOI18N
                "short".equals ( typeName ) || //NOI18N
                "char".equals ( typeName ) || //NOI18N
                "boolean".equals ( typeName ) ); //NOI18N
    }

    /**
     * Try to create an acronym-style variable name from a string - i.e.,
     * "JavaDataObject" becomes "jdo".
     */
    private static String tryToMakeAcronym (String s) {
        char[] c = s.toCharArray ();
        StringBuilder sb = new StringBuilder ();
        for ( int i = 0; i < c.length; i++ ) {
            if ( Character.isUpperCase (c[i])) {
                sb.append ( c[ i ] );
            }
        }
        if ( sb.length () > 1 ) {
            return sb.toString ().toLowerCase ();
        } else {
            return s.toLowerCase();
        }
    }

    @NonNull
    private static Set<URL> mapToURLs(
        @NonNull final Collection<? extends FileObject> fos) {
        final Set<URL> res = new HashSet<>(fos.size());
        for (FileObject fo : fos) {
            res.add(fo.toURL());
        }
        return res;
    }
}
