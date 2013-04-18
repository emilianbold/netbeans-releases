/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.j2seprofiles;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.swing.JComponent;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.support.ProfileSupport;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.analysis.spi.Analyzer;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.netbeans.modules.refactoring.api.Scope;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Zezula
 */
public class ProfilesAnalyzer implements Analyzer {

    private static final String ICON = "org/netbeans/modules/java/j2seprofiles/resources/profile.gif"; //NOI18N
    
    private final Context context;
    private final Result result;
    private final AtomicBoolean canceled = new AtomicBoolean();

    private ProfilesAnalyzer(
            @NonNull final Context context,
            @NonNull final Result result) {
        Parameters.notNull("context", context); //NOI18N
        Parameters.notNull("result", result);   //NOI18N
        this.context = context;
        this.result = result;
    }

    @Override
    @NonNull
    @NbBundle.Messages ({
        "MSG_AnalyzingRoot=Analyzing root {0}",
        "MSG_ProjectHigherProfile=Project requires profile: {0}",
        "DESC_ProjectHigherProfile=The project {0} located in {1} requires profile: {2}",
        "MSG_LibraryHigherProfile=Library requires profile: {0}",
        "DESC_LibraryHigherProfile=The Profile attribute in the manifest of the library {0} requires profile: {1}",
        "MSG_LibraryInvalidProfile=Library has invalid profile",
        "DESC_LibraryInvalidProfile=The library Manifest of the library {0} has invalid value of the Profile attribute",
        "MSG_ClassFileHigherProfile={0} requires profile: {1}",
        "DESC_ClassFileHigherProfile=The {0} used in class {1} of library {2} requires profile: {3}"
    })
    public Iterable<? extends ErrorDescription> analyze() {
        final Scope scope = context.getScope();
        final Set<FileObject> roots = scope.getSourceRoots();        
        final HashMap<URI,Set<Project>> submittedBinaries = new HashMap<>();
        final Set<URI> submittedSources = new HashSet<>();
        final CollectorFactory cf = new CollectorFactory();
        for (FileObject root : roots) {
            if (canceled.get()) {
                break;
            }
            final SourceLevelQuery.Profile profile = SourceLevelQuery.getSourceLevel2(root).getProfile();
            if (profile != SourceLevelQuery.Profile.DEFAULT) {
                final ClassPath boot = ClassPath.getClassPath(root, ClassPath.BOOT);
                final ClassPath compile = ClassPath.getClassPath(root, ClassPath.COMPILE);
                if (boot == null || compile == null) {
                    continue;
                }
                final Project owner = FileOwnerQuery.getOwner(root);
                if (owner == null) {
                    continue;
                }
                submittedSources.add(root.toURI());
                final Set<Project> projectRefs = new HashSet<>();
                ProfileSupport.findProfileViolations(
                    profile,
                    cpToRootUrls(boot, null, null, null),
                    cpToRootUrls(compile, owner, submittedBinaries, projectRefs),
                    Collections.singleton(root.toURL()),
                    EnumSet.of(
                        ProfileSupport.Validation.BINARIES_BY_MANIFEST,
                        ProfileSupport.Validation.BINARIES_BY_CLASS_FILES,
                        ProfileSupport.Validation.SOURCES),
                    cf);
                for (Project p : projectRefs) {
                    final FileObject pHome = p.getProjectDirectory();
                    final SourceLevelQuery.Profile pProfile = SourceLevelQuery.getSourceLevel2(pHome).getProfile();
                    if (pProfile.compareTo(profile) > 0) {
                        result.reportError(owner, ErrorDescriptionFactory.createErrorDescription(
                                null,
                                Severity.ERROR,
                                Bundle.MSG_ProjectHigherProfile(pProfile.getDisplayName()),
                                Bundle.DESC_ProjectHigherProfile(
                                    ProjectUtils.getInformation(p).getDisplayName(),
                                    FileUtil.getFileDisplayName(pHome),
                                    profile.getDisplayName()),
                                ErrorDescriptionFactory.lazyListForFixes(Collections.<Fix>emptyList()),
                                p.getProjectDirectory(),
                                null));
                    }
                }
            }            
        }
        if (!canceled.get()) {
            context.start(submittedBinaries.size() + submittedSources.size());
            int count = 0;
            while (!submittedBinaries.isEmpty() || !submittedSources.isEmpty()) {
                try {
                    final Pair<URL,Collection<? extends ProfileSupport.Violation>> violationsPair = cf.poll(2500);
                    if (violationsPair == null) {
                        continue;
                    }
                    if (canceled.get()) {
                        break;
                    }
                    final URI rootURI = violationsPair.first.toURI();
                    final FileObject root = URLMapper.findFileObject(rootURI.toURL());
                    context.progress(Bundle.MSG_AnalyzingRoot(FileUtil.getFileDisplayName(archiveFileOrFolder(root))), count);
                    final Collection<? extends ProfileSupport.Violation> violations = violationsPair.second;
                    final Set<Project> projects = submittedBinaries.remove(rootURI);
                    final boolean binary = projects != null;
                    if (!binary) {
                        submittedSources.remove(rootURI);
                    }
                    if (violations.isEmpty()) {
                        continue;
                    }
                    if (binary) {
                        //Binary roots
                        for (ProfileSupport.Violation violation : violations) {
                            final URL fileURL = violation.getFile();
                            FileObject target;
                            String message;
                            String description;
                            final SourceLevelQuery.Profile requiredProfile = violation.getRequiredProfile();                            
                            if (fileURL == null) {
                                target = root;
                                if (requiredProfile != null) {
                                    message = Bundle.MSG_LibraryHigherProfile(requiredProfile.getDisplayName());
                                    description = Bundle.DESC_LibraryHigherProfile(
                                            FileUtil.getFileDisplayName(archiveFileOrFolder(target)),
                                            requiredProfile.getDisplayName());
                                } else {
                                    message = Bundle.MSG_LibraryInvalidProfile();
                                    description = Bundle.DESC_LibraryInvalidProfile(FileUtil.getFileDisplayName(archiveFileOrFolder(target)));
                                }
                            } else {
                                final ElementHandle<TypeElement> usedType = violation.getUsedType();
                                assert usedType != null;
                                assert requiredProfile != null;
                                target = URLMapper.findFileObject(fileURL);
                                message = Bundle.MSG_ClassFileHigherProfile(
                                        simpleName(usedType),
                                        requiredProfile.getDisplayName());
                                description = Bundle.DESC_ClassFileHigherProfile(
                                        usedType.getQualifiedName(),
                                        stripExtension(FileUtil.getRelativePath(root, target)),
                                        FileUtil.getFileDisplayName(archiveFileOrFolder(root)),
                                        requiredProfile.getDisplayName());
                            }
                            for (Project p : projects) {
                                result.reportError(p, ErrorDescriptionFactory.createErrorDescription(
                                    null,
                                    Severity.ERROR,
                                    message,
                                    description,
                                    ErrorDescriptionFactory.lazyListForFixes(Collections.<Fix>emptyList()),
                                    target,
                                    null));
                            }
                        }
                    } else {
                        //Source roots
                        try {                            
                            if (root != null) {
                                final ClasspathInfo cpInfo = ClasspathInfo.create(root);
                                final Map<FileObject,Collection<ProfileSupport.Violation>> violationsByFiles =
                                    new HashMap<>();
                                final JavaSource js = JavaSource.create(
                                    cpInfo,
                                    violationsToFileObjects(violations, violationsByFiles));
                                if (js != null) {
                                    js.runUserActionTask(
                                        new Task<CompilationController>(){
                                            @Override
                                            public void run(@NonNull final CompilationController cc) throws Exception {
                                                cc.toPhase(JavaSource.Phase.RESOLVED);
                                                final FileObject currentFile = cc.getFileObject();
                                                final FindPosScanner fps = new FindPosScanner(
                                                        currentFile,
                                                        cc.getTrees(),
                                                        cc.getElements(),
                                                        cc.getTreeUtilities(),
                                                        violationsByFiles.get(currentFile),
                                                        result);
                                                fps.scan(cc.getCompilationUnit(), null);

                                            }
                                        },
                                        true);
                                }
                            }
                        } catch (IOException ioe) {
                            Exceptions.printStackTrace(ioe);
                        }
                    }
                    context.progress(++count);
                } catch (InterruptedException ex) {
                    break;
                } catch (URISyntaxException | MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            context.finish();
        }
        return Collections.<ErrorDescription>emptySet();
    }

    @Override
    public boolean cancel() {
        canceled.set(true);
        return true;
    }

    @NonNull
    private static FileObject archiveFileOrFolder(@NonNull final FileObject root) {
        final FileObject archiveFile = FileUtil.getArchiveFile(root);
        return archiveFile != null ? archiveFile : root;
    }

    @NonNull
    private static String stripExtension(@NonNull final String path) {
        final int index = path.lastIndexOf('.');    //NOI18N
        return index <= 0 ? path : path.substring(0, index);
    }

    @NonNull
    private static String simpleName(@NullAllowed final ElementHandle<TypeElement> eh) {
        if (eh == null) {
            return "";  //NOI18N
        }
        final String qn = eh.getQualifiedName();
        int index = qn.lastIndexOf('.');    //NOI18N
        return index < 0 ? qn : qn.substring(index+1);
    }

    @NonNull
    private static Iterable<URL> cpToRootUrls(
            @NonNull final ClassPath cp,
            @NullAllowed final Project owner,
            @NullAllowed final Map<URI,Set<Project>> alreadyProcessed,
            @NullAllowed final Set<? super Project> projectRefs) {
        assert (owner == null && alreadyProcessed == null && projectRefs == null) ||
               (owner != null && alreadyProcessed != null && projectRefs != null);
        final Queue<URL> res = new ArrayDeque<>();
nextCpE:for (ClassPath.Entry e : cp.entries()) {
            final URL url = e.getURL();
            try {
                if (projectRefs != null) {
                    final SourceForBinaryQuery.Result2 sfbqRes = SourceForBinaryQuery.findSourceRoots2(url);
                    if (sfbqRes.preferSources()) {
                        for (FileObject src : sfbqRes.getRoots()) {
                            final Project prj = FileOwnerQuery.getOwner(src);
                            if (prj != null) {
                                for (SourceGroup sg : ProjectUtils.getSources(prj).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
                                    if (src.equals(sg.getRootFolder())) {
                                        if (!prj.equals(owner)) {
                                            projectRefs.add(prj);
                                        }
                                        continue nextCpE;
                                    }
                                }
                            }
                        }
                    }
                }
                
                if (alreadyProcessed == null) {
                    res.offer(url);
                } else {
                    final URI uri = url.toURI();
                    Set<Project> projects = alreadyProcessed.get(uri);
                    if (projects == null) {
                        projects = new HashSet<>();
                        alreadyProcessed.put(uri, projects);
                        res.offer(url);
                    }                    
                    projects.add(owner);
                }               
            } catch (URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return res;
    }

    private static FileObject[] violationsToFileObjects(
            @NonNull final Collection<? extends ProfileSupport.Violation> violations,
            @NullAllowed final Map<FileObject,Collection<ProfileSupport.Violation>> violationsByFiles) {
        final Collection<FileObject> fos = new HashSet<>(violations.size());
        for (ProfileSupport.Violation v : violations) {
            final URL fileURL = v.getFile();
            if (fileURL != null) {
                final FileObject fo = URLMapper.findFileObject(fileURL);
                if (fo != null) {
                    fos.add(fo);
                    if (violationsByFiles != null) {
                        Collection<ProfileSupport.Violation> violationsInFile = violationsByFiles.get(fo);
                        if (violationsInFile == null) {
                            violationsInFile = new ArrayList<>();
                            violationsByFiles.put(fo, violationsInFile);
                        }
                        violationsInFile.add(v);
                    }
                }
            }
        }
        return fos.toArray(new FileObject[fos.size()]);
    }


    //@ThreadSafe
    private final class CollectorFactory implements ProfileSupport.ViolationCollectorFactory {

        
        private final BlockingQueue<Pair<URL,Collection<? extends ProfileSupport.Violation>>>
                allViolations = new LinkedBlockingQueue<>();

        @Override
        public ProfileSupport.ViolationCollector create(@NonNull final URL root) {
            return new Collector(root);
        }

        @Override
        public boolean isCancelled() {
            return canceled.get();
        }

        @CheckForNull
        Pair<URL, Collection<? extends ProfileSupport.Violation>> poll(long timeOut) throws InterruptedException {
            return allViolations.poll(timeOut, TimeUnit.MILLISECONDS);
        }

        private synchronized void addViolations(
                @NonNull final URL root,
                @NonNull final Collection<? extends ProfileSupport.Violation> violations) {
            allViolations.offer(Pair.<URL,Collection<? extends ProfileSupport.Violation>>of(root,violations));
        }

        private final class Collector implements ProfileSupport.ViolationCollector {

            private final URL root;

            Collector(@NonNull final URL root) {
                Parameters.notNull("root", root);   //NOI18N
                this.root = root;
            }

            private final Queue<ProfileSupport.Violation> violations = new ArrayDeque<>();

            @Override
            public void reportProfileViolation(@NonNull final ProfileSupport.Violation violation) {
                violations.offer(violation);
            }

            @Override
            public void finished() {
                addViolations(root, violations);
            }

        }

    }

    //@NonThreadSafe
    private final class FindPosScanner extends TreePathScanner<Void, Void> {

        private final FileObject target;
        private final Elements elements;
        private final TreeUtilities treeUtilities;
        private final Trees trees;
        private final Result errors;
        private final Map<String,ProfileSupport.Violation> violationsByBinNames =
                new HashMap<>();        

        FindPosScanner(
                @NonNull final FileObject target,
                @NonNull final Trees trees,
                @NonNull final Elements elements,
                @NonNull final TreeUtilities treeUtilities,
                @NonNull final Collection<? extends ProfileSupport.Violation> violations,
                @NonNull final Result errors) {
            assert target != null;
            assert trees != null;
            assert elements != null;
            assert treeUtilities != null;
            assert violations != null;
            assert errors != null;
            this.target = target;
            this.trees = trees;
            this.elements = elements;
            this.treeUtilities = treeUtilities;
            this.errors = errors;
            for (ProfileSupport.Violation v : violations) {
                final ElementHandle<TypeElement> eh = v.getUsedType();
                if (eh != null) {
                    violationsByBinNames.put(eh.getBinaryName(), v);
                }
            }
        }

        @Override
        public Void visitIdentifier(IdentifierTree node, Void p) {
            handleIdentSelect();
            return super.visitIdentifier(node, p);
        }

        @Override
        public Void visitMemberSelect(MemberSelectTree node, Void p) {
            handleIdentSelect();
            return super.visitMemberSelect(node, p);
        }

        @NbBundle.Messages({
            "MSG_SourceFileHigherProfile={0} requires profile: {1}",
            "DESC_SourceFileHigherProfile=The {0} requires profile: {1}"
        })
        private void handleIdentSelect() {
            final TreePath tp = getCurrentPath();
            Element e = trees.getElement(tp);
            if (e != null) {
                final ElementKind ek = e.getKind();
                if (ek == ElementKind.OTHER ||
                    ek.isField() ||
                    ek == ElementKind.CONSTRUCTOR ||
                    ek == ElementKind.METHOD) {
                        e = e.getEnclosingElement();
                }
                if ((e.getKind().isClass() || e.getKind().isInterface()) && !treeUtilities.isSynthetic(tp)) {
                    final Name binName = elements.getBinaryName((TypeElement)e);
                    final ProfileSupport.Violation v = violationsByBinNames.get(binName.toString());
                    if (v != null) {
                        final SourcePositions sp = trees.getSourcePositions();                        
                        final int start = (int) sp.getStartPosition(tp.getCompilationUnit(), tp.getLeaf());
                        final int end = (int) sp.getEndPosition(tp.getCompilationUnit(), tp.getLeaf());
                        final SourceLevelQuery.Profile requiredProfile = v.getRequiredProfile();
                        assert requiredProfile != null;
                        errors.reportError(ErrorDescriptionFactory.createErrorDescription(
                            null,
                            Severity.ERROR,
                            Bundle.MSG_SourceFileHigherProfile(e.getSimpleName(), requiredProfile.getDisplayName()),
                            Bundle.DESC_SourceFileHigherProfile(((TypeElement)e).getQualifiedName(), requiredProfile.getDisplayName()),
                            ErrorDescriptionFactory.lazyListForFixes(Collections.<Fix>emptyList()),
                            target,
                            start,
                            end));
                    }
                }
            }
        }
    }

    @NbBundle.Messages({
        "NAME_JdkProfiles=JRE 8 Profiles Conformance"
    })
    @ServiceProvider(service=AnalyzerFactory.class)
    public static final class Factory extends Analyzer.AnalyzerFactory {

        public Factory() {
            super("jdk-profiles", Bundle.NAME_JdkProfiles(), ICON);
        }
        
        @Override
        public Analyzer createAnalyzer(
                @NonNull final Context context,
                @NonNull final Result result) {
            return new ProfilesAnalyzer(context, result);
        }

        @Override
        public Analyzer createAnalyzer(@NonNull final Context context) {
            throw new IllegalStateException();
        }

        @Override
        public Iterable<? extends WarningDescription> getWarnings() {
            return Collections.emptySet();
        }

        @CheckForNull
        @Override
        public <D, C extends JComponent> CustomizerProvider<D, C> getCustomizerProvider() {
            return null;
        }
    }

}
