/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import java.util.logging.Level;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeFileItem.Language;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectItemsListener;
import org.netbeans.modules.cnd.debug.DebugUtils;
import org.netbeans.modules.cnd.apt.support.APTFileCacheEntry;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler.State;
import org.netbeans.modules.cnd.apt.support.StartEntry;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.apt.support.APTSystemStorage;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.support.APTIncludeHandler;
import org.netbeans.modules.cnd.apt.support.APTIncludePathStorage;
import org.netbeans.modules.cnd.apt.support.APTMacroMap;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.support.APTWalker;
import org.netbeans.modules.cnd.apt.support.IncludeDirEntry;
import org.netbeans.modules.cnd.modelimpl.debug.Terminator;
import org.netbeans.modules.cnd.modelimpl.debug.Diagnostic;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;

import org.netbeans.modules.cnd.modelimpl.csm.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileContainer.FileEntry;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTParseFileWalker;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTRestorePreprocStateWalker;
import org.netbeans.modules.cnd.modelimpl.platform.ModelSupport;
import org.netbeans.modules.cnd.modelimpl.repository.KeyUtilities;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.ProjectNameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.DefaultCache;
import org.netbeans.modules.cnd.modelimpl.trace.TraceUtils;
import org.netbeans.modules.cnd.modelimpl.uid.LazyCsmCollection;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.CharSequences;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Cancellable;
import org.openide.util.RequestProcessor;

/**
 * Base class for CsmProject implementation
 * @author Dmitry Ivanov
 * @author Vladimir Kvashin
 */
public abstract class ProjectBase implements CsmProject, Persistent, SelfPersistent, CsmIdentifiable {

    /** Creates a new instance of CsmProjectImpl */
    protected ProjectBase(ModelImpl model, Object platformProject, String name) {
        namespaces = new ConcurrentHashMap<CharSequence, CsmUID<CsmNamespace>>();
        RepositoryUtils.openUnit(createProjectKey(platformProject));
        setStatus(Status.Initial);
        this.name = ProjectNameCache.getManager().getString(name);
        init(model, platformProject);
        initFields();
    }

    /*package*/final void initFields() {
        NamespaceImpl ns = new NamespaceImpl(this, false);
        assert ns != null;
        this.globalNamespaceUID = UIDCsmConverter.namespaceToUID(ns);
        declarationsSorageKey = new DeclarationContainer(this).getKey();
        classifierStorageKey = new ClassifierContainer(this).getKey();
        fileContainerKey = new FileContainer(this).getKey();
        graphStorageKey = new GraphContainer(this).getKey();
        FAKE_GLOBAL_NAMESPACE = new NamespaceImpl(this, true);
    }


    private void init(ModelImpl model, Object platformProject) {
        this.model = model;
        this.platformProject = platformProject;
        // remember in repository
        RepositoryUtils.hang(this);
        // create global namespace

        if (TraceFlags.CLOSE_AFTER_PARSE) {
            Terminator.create(this);
        }
    }

    private boolean checkConsistency() {
        long time = TraceFlags.TIMING ? System.currentTimeMillis() : 0;
        if (getFileContainer() == FileContainer.empty()) {
            return false;
        }
        if (getDeclarationsSorage() == DeclarationContainer.empty()) {
            return false;
        }
        if (getGraph() == GraphContainer.empty()) {
            return false;
        }
        if (getGlobalNamespace() == FAKE_GLOBAL_NAMESPACE) {
            return false;
        }
        if (TraceFlags.TIMING) {
            System.err.printf("Consistency check took %d ms\n", System.currentTimeMillis() - time);
        }
        return true;
    }

    private void setStatus(Status newStatus) {
        //System.err.printf("CHANGING STATUS %s -> %s for %s (%s)\n", status, newStatus, name, getClass().getName());
        status = newStatus;
    }

    protected static void cleanRepository(Object platformProject, boolean articicial) {
        Key key = createProjectKey(platformProject);
        RepositoryUtils.closeUnit(key, null, true);
    }

    private static Key createProjectKey(Object platfProj) {
        return KeyUtilities.createProjectKey(getUniqueName(platfProj).toString());
    }

    protected static ProjectBase readInstance(ModelImpl model, Object platformProject, String name) {

        long time = 0;
        if (TraceFlags.TIMING) {
            System.err.printf("Project %s: instantiating...\n", name);
            time = System.currentTimeMillis();
        }

        assert TraceFlags.PERSISTENT_REPOSITORY;
        Key key = createProjectKey(platformProject);
        RepositoryUtils.openUnit(key);
        Persistent o = RepositoryUtils.get(key);
        if (o != null) {
            assert o instanceof ProjectBase;
            ProjectBase impl = (ProjectBase) o;
            CharSequence aName = ProjectNameCache.getManager().getString(name);
            if (!impl.name.equals(aName)) {
                impl.setName(aName);
            }
            impl.init(model, platformProject);
            if (TraceFlags.TIMING) {
                time = System.currentTimeMillis() - time;
                System.err.printf("Project %s: loaded. %d ms\n", name, time);
            }
            if (impl.checkConsistency()) {
                return impl;
            }
        }
        return null;
    }

    @Override
    public final CsmNamespace getGlobalNamespace() {
        return _getGlobalNamespace();
    }

    @Override
    public final CharSequence getName() {
        return name;
    }

    protected final void setName(CharSequence name) {
        this.name = name;
    }

    /**
     * Returns a string that uniquely identifies this project.
     * One should never rely on this name structure,
     * just use it as in unique identifier
     */
    public final CharSequence getUniqueName() {
        if (this.uniqueName == null) {
            this.uniqueName = getUniqueName(getPlatformProject());
        }
        return this.uniqueName;
    }

    public static CharSequence getUniqueName(Object platformProject) {
        String result;
        if (platformProject instanceof NativeProject) {
            result = ((NativeProject) platformProject).getProjectRoot() + 'N';
        } else if (platformProject instanceof CharSequence) {
            result = ((CharSequence)platformProject).toString() + 'L';
        } else if (platformProject == null) {
            throw new IllegalArgumentException("Incorrect platform project: null"); // NOI18N
        } else {
            throw new IllegalArgumentException("Incorrect platform project class: " + platformProject.getClass()); // NOI18N
        }
        return ProjectNameCache.getManager().getString(result);
    }

    /** Gets an object, which represents correspondent IDE project */
    @Override
    public final Object getPlatformProject() {
        return platformProject;
    }

    /** Gets an object, which represents correspondent IDE project */
    protected final void setPlatformProject(Object platformProject) {
        this.platformProject = platformProject;
        // recreate unique name
        this.uniqueName = null;
        this.uniqueName = getUniqueName();
    }

    /** Finds namespace by its qualified name */
    public final CsmNamespace findNamespace(CharSequence qualifiedName, boolean findInLibraries) {
        CsmNamespace result = findNamespace(qualifiedName);
        if (result == null && findInLibraries) {
            for (Iterator<CsmProject> it = getLibraries().iterator(); it.hasNext();) {
                CsmProject lib = it.next();
                result = lib.findNamespace(qualifiedName);
                if (result != null) {
                    break;
                }
            }
        }
        return result;
    }

    /** Finds namespace by its qualified name */
    @Override
    public final CsmNamespace findNamespace(CharSequence qualifiedName) {
        CsmNamespace nsp = _getNamespace(qualifiedName);
        return nsp;
    }

    private static String getNestedNamespaceQualifiedName(CharSequence name, NamespaceImpl parent, boolean createForEmptyNames) {
        StringBuilder sb = new StringBuilder(name);
        if (parent != null) {
            if (name.length() == 0 && createForEmptyNames) {
                sb.append(parent.getNameForUnnamedElement());
            }
            if (!parent.isGlobal()) {
                sb.insert(0, "::"); // NOI18N
                sb.insert(0, parent.getQualifiedName());
            }
        }
        return sb.toString();
    }

    public final NamespaceImpl findNamespaceCreateIfNeeded(NamespaceImpl parent, CharSequence name) {
        synchronized (namespaceLock) {
            String qualifiedName = ProjectBase.getNestedNamespaceQualifiedName(name, parent, true);
            NamespaceImpl nsp = _getNamespace(qualifiedName);
            if (nsp == null) {
                nsp = new NamespaceImpl(this, parent, name.toString(), qualifiedName);
            }
            return nsp;
        }
    }

    public final void registerNamespace(NamespaceImpl namespace) {
        _registerNamespace(namespace);
    }

    public final void unregisterNamesace(NamespaceImpl namespace) {
        _unregisterNamespace(namespace);
    }

    public final CsmClassifier findClassifier(CharSequence qualifiedName, boolean findInLibraries) {
        CsmClassifier result = findClassifier(qualifiedName);
        if (result == null && findInLibraries) {
            for (Iterator<CsmProject> it = getLibraries().iterator(); it.hasNext();) {
                CsmProject lib = it.next();
                result = lib.findClassifier(qualifiedName);
                if (result != null) {
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public final CsmClassifier findClassifier(CharSequence qualifiedName) {
        CsmClassifier result = getClassifierSorage().getClassifier(qualifiedName);
        return result;
    }

    @Override
    public final Collection<CsmClassifier> findClassifiers(CharSequence qualifiedName) {
        CsmClassifier result = getClassifierSorage().getClassifier(qualifiedName);
        Collection<CsmClassifier> out = new ArrayList<CsmClassifier>();
        //Collection<CsmClassifier> out = new LazyCsmCollection<CsmClassifier, CsmClassifier>(new ArrayList<CsmUID<CsmClassifier>>(), TraceFlags.SAFE_UID_ACCESS);
        if (result != null) {
            if (CsmKindUtilities.isBuiltIn(result)) {
                return Collections.<CsmClassifier>singletonList(result);
            }
            CharSequence[] allClassifiersUniqueNames = Utils.getAllClassifiersUniqueNames(result.getUniqueName());
            Collection<CsmClassifier> fwds = new ArrayList<CsmClassifier>(1);
            for (CharSequence curUniqueName : allClassifiersUniqueNames) {
                Collection<? extends CsmDeclaration> decls = this.findDeclarations(curUniqueName);
                @SuppressWarnings("unchecked")
                Collection<CsmClassifier> classifiers = (Collection<CsmClassifier>) decls;
                for (CsmClassifier csmClassifier : classifiers) {
                    if (ForwardClass.isForwardClass(csmClassifier)) {
                        fwds.add(csmClassifier);
                    } else {
                        out.add(csmClassifier);
                    }
                }
            }
            // All forwards move at the end
            out.addAll(fwds);
        }
        return out;
    }

    @Override
    public final CsmDeclaration findDeclaration(CharSequence uniqueName) {
        return getDeclarationsSorage().getDeclaration(uniqueName);
    }

    @Override
    public final Collection<CsmOffsetableDeclaration> findDeclarations(CharSequence uniqueName) {
        return getDeclarationsSorage().findDeclarations(uniqueName);
    }

    public final Collection<CsmOffsetableDeclaration> findDeclarationsByPrefix(String prefix) {
        // To improve performance use char(255) instead real Character.MAX_VALUE
        char maxChar = 255; //Character.MAX_VALUE;
        return getDeclarationsSorage().getDeclarationsRange(prefix, prefix + maxChar); // NOI18N
    }

    public final Collection<CsmFriend> findFriendDeclarations(CsmOffsetableDeclaration decl) {
        return getDeclarationsSorage().findFriends(decl);
    }

    public static boolean isCppFile(CsmFile file) {
        return (file instanceof FileImpl) && ((FileImpl) file).isCppFile();
    }

//    public void registerClassifier(ClassEnumBase ce) {
//        classifiers.put(ce.getNestedNamespaceQualifiedName(), ce);
//        registerDeclaration(ce);
//    }
    public static boolean canRegisterDeclaration(CsmDeclaration decl) {
        // WAS: don't put unnamed declarations
        assert decl != null;
        assert decl.getName() != null;
        if (decl.getName().length() == 0) {
            return false;
        }
        CsmScope scope = decl.getScope();
        if (scope instanceof CsmCompoundClassifier) {
            return canRegisterDeclaration((CsmCompoundClassifier) scope);
        }
        return true;
    }

    public final boolean registerDeclaration(CsmOffsetableDeclaration decl) {

        if (!ProjectBase.canRegisterDeclaration(decl)) {
            if (TraceFlags.TRACE_REGISTRATION) {
                traceRegistration("not registered decl " + decl + " UID " + UIDs.get(decl)); //NOI18N
            }
            return false;
        }

        if (CsmKindUtilities.isClass(decl) || CsmKindUtilities.isEnum(decl)) {

            ClassEnumBase<?> cls = (ClassEnumBase<?>) decl;
            CharSequence qname = cls.getQualifiedName();

            synchronized (classifierReplaceLock) {
                CsmClassifier old = getClassifierSorage().getClassifier(qname);
                if (old != null) {
                    // don't register if the new one is weaker
                    if (cls.shouldBeReplaced(old)) {
                        if (TraceFlags.TRACE_REGISTRATION) {
                            traceRegistration("not registered decl " + decl + " UID " + UIDs.get(decl)); //NOI18N
                        }
                        return false;
                    }
                    // remove the old one if the new one is stronger
                    if ((old instanceof ClassEnumBase<?>) && ((ClassEnumBase<?>) old).shouldBeReplaced(cls)) {
                        if (TraceFlags.TRACE_REGISTRATION) {
                            System.err.println("disposing old decl " + old + " UID " + UIDs.get(decl)); //NOI18N
                        }
                        ((ClassEnumBase<?>) old).dispose();
                    }
                }
                getDeclarationsSorage().putDeclaration(decl);
                getClassifierSorage().putClassifier((CsmClassifier) decl);
            }

        } else if (CsmKindUtilities.isTypedef(decl)) { // isClassifier(decl) or isTypedef(decl) ??
            getDeclarationsSorage().putDeclaration(decl);
            getClassifierSorage().putClassifier((CsmClassifier) decl);
        } else {
            // only classes, enums and typedefs are registered as classifiers;
            // even if you implement CsmClassifier, this doesn't mean you atomatically get there ;)
            getDeclarationsSorage().putDeclaration(decl);
        }

        if (TraceFlags.TRACE_REGISTRATION) {
            System.err.println("registered " + decl + " UID " + UIDs.get(decl)); //NOI18N
        }
        return true;
    }

    public final void unregisterDeclaration(CsmOffsetableDeclaration decl) {
        if (TraceFlags.TRACE_REGISTRATION) {
            traceRegistration("unregistered " + decl + " UID " + UIDs.get(decl)); //NOI18N
        }
        if (decl instanceof CsmClassifier) {
            getClassifierSorage().removeClassifier(decl);
        }
        getDeclarationsSorage().removeDeclaration(decl);
    }

    private static void traceRegistration(String text) {
        assert TraceFlags.TRACE_REGISTRATION : "TraceFlags.TRACE_REGISTRATION should be checked *before* call !"; //NOI18N
        System.err.printf("registration: %s\n", text);
    }

    @Override
    public final void waitParse() {
        boolean insideParser = ParserThreadManager.instance().isParserThread();
        if (insideParser) {
            new Throwable("project.waitParse should NEVER be called in parser thread !!!").printStackTrace(System.err); // NOI18N
        }
        if (insideParser) {
            return;
        }
        ensureFilesCreated();
        ensureChangedFilesEnqueued();
        waitParseImpl();
    }

    private void waitParseImpl() {
        synchronized (waitParseLock) {
            while (ParserQueue.instance().hasPendingProjectRelatedWork(this, null)) {
                try {
                    //FIXUP - timeout is a workaround for #146436 hang on running unit tests
                    waitParseLock.wait(10000);
                } catch (InterruptedException ex) {
                    // do nothing
                }
            }
        }
    }

    protected void ensureChangedFilesEnqueued() {
    }

    /**
     * @param skipFile if null => check all files, otherwise skip checking
     * this file
     *
     */
    protected boolean hasChangedFiles(CsmFile skipFile) {
        return false;
    }

    public final boolean acceptNativeItem(NativeFileItem item) {
        if (item.getFile() == null) {
            return false;
        }
        NativeFileItem.Language language = item.getLanguage();
        return (language == NativeFileItem.Language.C ||
                language == NativeFileItem.Language.CPP ||
                language == NativeFileItem.Language.FORTRAN ||
                language == NativeFileItem.Language.C_HEADER) &&
                !item.isExcluded();
    }

    protected final synchronized void registerProjectListeners() {
        if (platformProject instanceof NativeProject) {
            if (projectListener == null) {
                projectListener = new NativeProjectListenerImpl(getModel(), (NativeProject) platformProject);
            }
            ((NativeProject) platformProject).addProjectItemsListener(projectListener);
        }
    }

    public final synchronized void enableProjectListeners(boolean enable) {
        if (projectListener != null) {
            projectListener.enableListening(enable);
        }
    }

    protected final synchronized void unregisterProjectListeners() {
        if (projectListener != null) {
            if (platformProject instanceof NativeProject) {
                ((NativeProject) platformProject).removeProjectItemsListener(projectListener);
            }
        }
    }

    /*package*/ final void scheduleReparse() {
        ensureFilesCreated();
        DeepReparsingUtils.reparseOnEdit(this.getAllFileImpls(), this, true);
    }

    protected void ensureFilesCreated() {
        if (status == Status.Ready) {
            return;
        }
        boolean notify = false;
        synchronized (this) {
            if (status == Status.Initial || status == Status.Restored) {
                try {
                    setStatus((status == Status.Initial) ? Status.AddingFiles : Status.Validating);
                    long time = 0;
                    if (TraceFlags.SUSPEND_PARSE_TIME != 0) {
                        System.err.println("suspend queue");
                        ParserQueue.instance().suspend();
                        if (TraceFlags.TIMING) {
                            time = System.currentTimeMillis();
                        }
                    }
                    ParserQueue.instance().onStartAddingProjectFiles(this);
                    registerProjectListeners();
                    NativeProject nativeProject = ModelSupport.getNativeProject(platformProject);
                    if (nativeProject != null) {
                        try {
                            ParserQueue.instance().suspend();
                            createProjectFilesIfNeed(nativeProject);
                        } finally {
                            ParserQueue.instance().resume();
                        }
                    }
                    if (TraceFlags.SUSPEND_PARSE_TIME != 0) {
                        if (TraceFlags.TIMING) {
                            time = System.currentTimeMillis() - time;
                            System.err.println("getting files from project system + put in queue took " + time + "ms");
                        }
                        try {
                            System.err.println("sleep for " + TraceFlags.SUSPEND_PARSE_TIME + "sec before resuming queue");
                            Thread.sleep(TraceFlags.SUSPEND_PARSE_TIME * 1000);
                            System.err.println("woke up after sleep");
                        } catch (InterruptedException ex) {
                            // do nothing
                        }
                        ParserQueue.instance().resume();
                    }
                    notify = true;
                } finally {
                    setStatus(Status.Ready);
                }
            }
        }
        if (notify) {
            ParserQueue.instance().onEndAddingProjectFiles(this);
        }
    }

    private void createProjectFilesIfNeed(NativeProject nativeProject) {

        if (TraceFlags.TIMING) {
            System.err.printf("\n\nGetting files from project system for %s...\n", getName());
        }
        if (TraceFlags.SUSPEND_PARSE_TIME != 0) {
            try {
                System.err.println("sleep for " + TraceFlags.SUSPEND_PARSE_TIME + "sec before getting files from project");
                Thread.sleep(TraceFlags.SUSPEND_PARSE_TIME * 1000);
                System.err.println("woke up after sleep");
            } catch (InterruptedException ex) {
                // do nothing
            }
        }
        long time = System.currentTimeMillis();
        final Set<NativeFileItem> removedFiles = Collections.synchronizedSet(new HashSet<NativeFileItem>());
        NativeProjectItemsListener projectItemListener = new NativeProjectItemsListener() {

            @Override
            public void fileAdded(NativeFileItem fileItem) {
            }

            @Override
            public void filesAdded(List<NativeFileItem> fileItems) {
            }

            @Override
            public void fileRemoved(NativeFileItem fileItem) {
                removedFiles.add(fileItem);
            }

            @Override
            public void filesRemoved(List<NativeFileItem> fileItems) {
                removedFiles.addAll(fileItems);
            }

            @Override
            public void fileRenamed(String oldPath, NativeFileItem newFileIetm) {
            }

            @Override
            public void filePropertiesChanged(NativeFileItem fileItem) {
            }

            @Override
            public void filesPropertiesChanged(List<NativeFileItem> fileItems) {
            }

            @Override
            public void filesPropertiesChanged() {
            }

            @Override
            public void projectDeleted(NativeProject nativeProject) {
            }
        };
        nativeProject.addProjectItemsListener(projectItemListener);
        List<NativeFileItem> sources = new ArrayList<NativeFileItem>();
        List<NativeFileItem> headers = new ArrayList<NativeFileItem>();
        List<NativeFileItem> excluded = new ArrayList<NativeFileItem>();
        for (NativeFileItem item : nativeProject.getAllFiles()) {
            if (!item.isExcluded()) {
                switch (item.getLanguage()) {
                    case C:
                    case CPP:
                    case FORTRAN:
                        sources.add(item);
                        break;
                    case C_HEADER:
                        headers.add(item);
                        break;
                    default:
                        break;
                }
            } else {
                switch (item.getLanguage()) {
                    case C:
                    case CPP:
                    case C_HEADER:
                    case FORTRAN:
                        excluded.add(item);
                        break;
                    default:
                        break;
                }
            }
        }

        if (TraceFlags.TIMING) {
            time = System.currentTimeMillis() - time;
            System.err.printf("Getting files from project system took  %d ms for %s\n", time, getName());
            System.err.printf("FILES COUNT for %s:\nSource files:\t%d\nHeader files:\t%d\nTotal files:\t%d\n",
                    getName(), sources.size(), headers.size(), sources.size() + headers.size());
            time = System.currentTimeMillis();
        }
        if (TraceFlags.SUSPEND_PARSE_TIME != 0) {
            try {
                System.err.println("sleep for " + TraceFlags.SUSPEND_PARSE_TIME + "sec after getting files from project");
                Thread.sleep(TraceFlags.SUSPEND_PARSE_TIME * 1000);
                System.err.println("woke up after sleep");
            } catch (InterruptedException ex) {
                // do nothing
            }
        }
        if (TraceFlags.DUMP_PROJECT_ON_OPEN) {
            ModelSupport.dumpNativeProject(nativeProject);
        }

        try {
            disposeLock.readLock().lock();

            if (TraceFlags.TIMING) {
                time = System.currentTimeMillis() - time;
                System.err.printf("Waited on disposeLock: %d ms for %s\n", time, getName());
                time = System.currentTimeMillis();
            }

            if (isDisposing()) {
                if (TraceFlags.TRACE_MODEL_STATE) {
                    System.err.printf("filling parser queue interrupted for %s\n", getName());
                }
                return;
            }

            ProjectSettingsValidator validator = null;
            if (status == Status.Validating) {
                validator = new ProjectSettingsValidator(this);
                validator.restoreSettings();
            }
            if (status == Status.Validating && RepositoryUtils.getRepositoryErrorCount(this) > 0){
                System.err.println("Clean index for project \""+getUniqueName()+"\" because index was corrupted (was "+RepositoryUtils.getRepositoryErrorCount(this)+" errors)."); // NOI18N
                validator = null;
                reopenUnit();
            }

            projectRoots.fixFolder(nativeProject.getProjectRoot());
            for (String root : nativeProject.getSourceRoots()) {
                projectRoots.fixFolder(root);
            }
            projectRoots.addSources(sources);
            projectRoots.addSources(headers);
            projectRoots.addSources(excluded);
            createProjectFilesIfNeed(sources, true, removedFiles, validator);
            if (status != Status.Validating  || RepositoryUtils.getRepositoryErrorCount(this) == 0){
                createProjectFilesIfNeed(headers, false, removedFiles, validator);
            }
            if (status == Status.Validating && RepositoryUtils.getRepositoryErrorCount(this) > 0){
                System.err.println("Clean index for project \""+getUniqueName()+"\" because index was corrupted (was "+RepositoryUtils.getRepositoryErrorCount(this)+" errors)."); // NOI18N
                validator = null;
                reopenUnit();
                createProjectFilesIfNeed(sources, true, removedFiles, validator);
                createProjectFilesIfNeed(headers, false, removedFiles, validator);
            }

        } finally {
            disposeLock.readLock().unlock();
            if (TraceFlags.TIMING) {
                time = System.currentTimeMillis() - time;
                System.err.printf("FILLING PARSER QUEUE took %d ms for %s\n", time, getName());
            }
        }
        nativeProject.removeProjectItemsListener(projectItemListener);
    // in fact if visitor used for parsing => visitor will parse all included files
    // recursively starting from current source file
    // so, when we visit headers, they should not be reparsed if already were parsed
    }

    private void reopenUnit() {
        setStatus(Status.Initial);
        ParserQueue.instance().clean(this);
        RepositoryUtils.closeUnit(this.getUniqueName().toString(), null, true);
        RepositoryUtils.openUnit(this);
        RepositoryUtils.hang(this);
        initFields();
    }

    private final RequestProcessor PROJECT_FILES_WORKER = new RequestProcessor("Project Files", CndUtils.getNumberCndWorkerThreads()); // NOI18N

    private void createProjectFilesIfNeed(List<NativeFileItem> items, boolean sources,
            Set<NativeFileItem> removedFiles, ProjectSettingsValidator validator) {

        List<FileImpl> reparseOnEdit = new ArrayList<FileImpl>();
        List<NativeFileItem> reparseOnPropertyChanged = new ArrayList<NativeFileItem>();
        AtomicBoolean enougth = new AtomicBoolean(false);
        int size = items.size();
        int threads = CndUtils.getNumberCndWorkerThreads()*3;
        CountDownLatch countDownLatch = new CountDownLatch(threads);
        int chunk = (size/threads) + 1;
        Iterator<NativeFileItem> it = items.iterator();
        for (int i = 0; i < threads; i++) {
            ArrayList<NativeFileItem> list = new ArrayList<NativeFileItem>(chunk);
            for(int j = 0; j < chunk; j++){
                if(it.hasNext()){
                    list.add(it.next());
                } else {
                    break;
                }
            }
            CreateFileRunnable r = new CreateFileRunnable(countDownLatch, list, sources, removedFiles,
                    validator, reparseOnEdit, reparseOnPropertyChanged, enougth);
            PROJECT_FILES_WORKER.post(r);
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException ex) {
        }
        //for (NativeFileItem nativeFileItem : items) {
        //    if (!createProjectFilesIfNeedRun(nativeFileItem, sources, removedFiles, validator,
        //            reparseOnEdit, reparseOnPropertyChanged, enougth)) {
        //        return;
        //    }
        //}
        if (!reparseOnEdit.isEmpty()) {
            DeepReparsingUtils.reparseOnEdit(reparseOnEdit, this, true);
        }
        if (!reparseOnPropertyChanged.isEmpty()) {
            DeepReparsingUtils.reparseOnPropertyChanged(reparseOnPropertyChanged, this);
        }
    }

    private boolean createProjectFilesIfNeedRun(NativeFileItem nativeFileItem, boolean sources,
            Set<NativeFileItem> removedFiles, ProjectSettingsValidator validator,
            List<FileImpl> reparseOnEdit, List<NativeFileItem> reparseOnPropertyChanged, AtomicBoolean enougth){
        if (enougth.get()) {
            return false;
        }
        if (isDisposing()) {
            if (TraceFlags.TRACE_MODEL_STATE) {
                System.err.printf("filling parser queue interrupted for %s\n", getName());
            }
            return false;
        }
        if (removedFiles.contains(nativeFileItem)) {
            return true;
        }
        assert (nativeFileItem.getFile() != null) : "native file item must have valid File object";
        if (TraceFlags.DEBUG) {
            ModelSupport.trace(nativeFileItem);
        }
        try {
            createIfNeed(nativeFileItem, sources, validator, reparseOnEdit, reparseOnPropertyChanged);
            if (status == Status.Validating && RepositoryUtils.getRepositoryErrorCount(this) > 0) {
                enougth.set(true);
                return false;
            }
        } catch (Exception ex) {
            DiagnosticExceptoins.register(ex);
        }
        return true;
    }


    /**
     * Creates FileImpl instance for the given file item if it hasn/t yet been created.
     * Is called when initializing the project or new file is added to project.
     * Isn't intended to be used in #included file processing.
     */
    final protected void createIfNeed(NativeFileItem nativeFile, boolean isSourceFile) {
        createIfNeed(nativeFile, isSourceFile, null, null, null);
    }

    private void createIfNeed(NativeFileItem nativeFile, boolean isSourceFile,
            ProjectSettingsValidator validator, List<FileImpl> reparseOnEdit, List<NativeFileItem> reparseOnPropertyChanged) {

        assert (nativeFile != null && nativeFile.getFile() != null);
        if (!acceptNativeItem(nativeFile)) {
            return;
        }
        File file = nativeFile.getFile();
        FileImpl.FileType fileType = isSourceFile ? getFileType(nativeFile) : FileImpl.FileType.HEADER_FILE;

        FileAndHandler fileAndHandler = createOrFindFileImpl(ModelSupport.getFileBuffer(file), nativeFile, fileType);

        if (fileAndHandler.preprocHandler == null) {
            fileAndHandler.preprocHandler = createPreprocHandler(nativeFile);
        }
        if (validator != null) {
            // fill up needed collections based on validation
            if (fileAndHandler.fileImpl.validate()) {
                if (validator.arePropertiesChanged(nativeFile)) {
                    if (TraceFlags.TRACE_VALIDATION) {
                        System.err.printf("Validation: %s properties are changed \n", nativeFile.getFile().getAbsolutePath());
                    }
                    reparseOnPropertyChanged.add(nativeFile);
                }
            } else {
                if (TraceFlags.TRACE_VALIDATION) {
                    System.err.printf("Validation: file %s is changed\n", nativeFile.getFile().getAbsolutePath());
                }
                reparseOnEdit.add(fileAndHandler.fileImpl);
            }
        } else {
            // put directly into parser queue if needed
            ParserQueue.instance().add(fileAndHandler.fileImpl, fileAndHandler.preprocHandler.getState(), ParserQueue.Position.TAIL);
        }
    }

    /**
     * Is called after project is added to model
     * and all listeners are notified
     */
    public final void onAddedToModel() {
        final boolean isRestored = status == Status.Restored;
        //System.err.printf("onAddedToModel isRestored=%b status=%s for %s (%s) \n", isRestored, status, name, getClass().getName());
        if (status == Status.Initial || status == Status.Restored) {
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    onAddedToModelImpl(isRestored);
                    synchronized (initializationTaskLock) {
                        initializationTask = null;
                    }
                }
            };
            String text = (status == Status.Initial) ? "Filling parser queue for " : "Validating files for ";	// NOI18N
            synchronized (initializationTaskLock) {
                initializationTask = ModelImpl.instance().enqueueModelTask(r, text + getName());
            }
        }
    }

    protected final Status getStatus() {
        return status;
    }

    private void onAddedToModelImpl(boolean isRestored) {

        if (isDisposing()) {
            return;
        }

        try {
            disposeLock.readLock().lock();
            if (isDisposing()) {
                return;
            }

            ensureFilesCreated();
            if (isDisposing()) {
                return;
            }

            ensureChangedFilesEnqueued();
            if (isDisposing()) {
                return;
            }
            Notificator.instance().flush();
        } finally {
            disposeLock.readLock().unlock();
        }

        if (isRestored) {
            ProgressSupport.instance().fireProjectLoaded(ProjectBase.this);
        }

        try {
            disposeLock.readLock().lock();
            if (isRestored && !isDisposing()) {
                // FIXUP for #109105 fix the reason instead!
                try {
                    // TODO: refactor this - remove waiting here!
                    // It was introduced in version 1.2.2.27.2.94.4.41
                    // when validation was introduced
                    waitParseImpl();
                    checkForRemoved();
                } catch (Exception e) {
                    DiagnosticExceptoins.register(e);
                }
            }
            if (isDisposing()) {
                return;
            }
            Notificator.instance().flush();
        } finally {
            disposeLock.readLock().unlock();
        }
    }

    /**
     * For the project that is restored from persistence,
     * is called when 1-st time parsed.
     * Cheks whether there are files in code model, that are removed from the project system
     */
    private void checkForRemoved() {

        NativeProject nativeProject = (platformProject instanceof NativeProject) ? (NativeProject) platformProject : null;

        // we might just ask NativeProject to find file,
        // but it's too ineffective; so we have to create a set of project files paths
        Set<String> projectFiles = null;
        if (nativeProject != null) {
            projectFiles = new HashSet<String>();
            for (NativeFileItem item : nativeProject.getAllFiles()) {
                if (!item.isExcluded()) {
                    switch (item.getLanguage()) {
                        case C:
                        case CPP:
                        case FORTRAN:
                        case C_HEADER:
                            projectFiles.add(item.getFile().getAbsolutePath());
                            //this would be a workaround for #116706 Code assistance do not recognize changes in file
                            //projectFiles.add(item.getFile().getCanonicalPath());
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        Set<FileImpl> candidates = new HashSet<FileImpl>();
        Set<FileImpl> removedPhysically = new HashSet<FileImpl>();
        for (FileImpl file : getAllFileImpls()) {
            if (!file.getFile().exists()) {
                removedPhysically.add(file);
            } else if (projectFiles != null) { // they might be null for library
                if (!projectFiles.contains(file.getAbsolutePath().toString())) {
                    candidates.add(file);
                }
            }
        }
        if (!removedPhysically.isEmpty()) {
            if (TraceFlags.TRACE_VALIDATION) {
                for (FileImpl file : removedPhysically) {
                    System.err.printf("Validation: removing (physically deleted) %s\n", file.getAbsolutePath()); //NOI18N
                }
            }
            onFileImplRemoved(new ArrayList<FileImpl>(removedPhysically));
        }
        for (FileImpl file : candidates) {
            boolean remove = true;
            Set<CsmFile> parents = getGraphStorage().getParentFiles(file);
            for (CsmFile parent : parents) {
                if (!candidates.contains((FileImpl)parent)) {
                    remove = false;
                    break;
                }
            }
            if (remove) {
                if (TraceFlags.TRACE_VALIDATION) {
                    System.err.printf("Validation: removing (removed from project) %s\n", file.getAbsolutePath());
                } //NOI18N
                onFileRemoved(file);
            }
        }
    }

    protected final APTPreprocHandler createEmptyPreprocHandler(File file) {
        StartEntry startEntry = new StartEntry(FileContainer.getFileKey(file, true).toString(),
                RepositoryUtils.UIDtoKey(getUID()));
        return APTHandlersSupport.createEmptyPreprocHandler(startEntry);
    }

    protected final APTPreprocHandler createPreprocHandler(NativeFileItem nativeFile) {
        assert (nativeFile != null);
        APTMacroMap macroMap = getMacroMap(nativeFile);
        APTIncludeHandler inclHandler = getIncludeHandler(nativeFile);
        APTPreprocHandler preprocHandler = APTHandlersSupport.createPreprocHandler(macroMap, inclHandler, isSourceFile(nativeFile));
        return preprocHandler;
    }

    private APTIncludeHandler getIncludeHandler(NativeFileItem nativeFile) {
        if (!isSourceFile(nativeFile)) {
            nativeFile = DefaultFileItem.toDefault(nativeFile);
        }
        List<String> origUserIncludePaths = nativeFile.getUserIncludePaths();
        List<String> origSysIncludePaths = nativeFile.getSystemIncludePaths();
        List<IncludeDirEntry> userIncludePaths = userPathStorage.get(origUserIncludePaths.toString(), origUserIncludePaths);
        List<IncludeDirEntry> sysIncludePaths = sysAPTData.getIncludes(origSysIncludePaths.toString(), origSysIncludePaths);
        StartEntry startEntry = new StartEntry(FileContainer.getFileKey(nativeFile.getFile(), true).toString(),
                RepositoryUtils.UIDtoKey(getUID()));
        return APTHandlersSupport.createIncludeHandler(startEntry, sysIncludePaths, userIncludePaths);
    }

    private APTMacroMap getMacroMap(NativeFileItem nativeFile) {
        if (!isSourceFile(nativeFile)) {
            nativeFile = DefaultFileItem.toDefault(nativeFile);
        }
        List<String> userMacros = nativeFile.getUserMacroDefinitions();
        List<String> sysMacros = nativeFile.getSystemMacroDefinitions();
        APTMacroMap map = APTHandlersSupport.createMacroMap(getSysMacroMap(sysMacros), userMacros);
        return map;
    }

    protected final boolean isSourceFile(NativeFileItem nativeFile) {
        FileImpl.FileType type = getFileType(nativeFile);
        return FileImpl.isSourceFileType(type);
    }

    protected static FileImpl.FileType getFileType(NativeFileItem nativeFile) {
        switch (nativeFile.getLanguage()) {
            case C:
                return FileImpl.FileType.SOURCE_C_FILE;
            case CPP:
                return FileImpl.FileType.SOURCE_CPP_FILE;
            case FORTRAN:
                return FileImpl.FileType.SOURCE_FORTRAN_FILE;
            case C_HEADER:
                return FileImpl.FileType.HEADER_FILE;
            default:
                return FileImpl.FileType.UNDEFINED_FILE;
        }
    }

    private APTMacroMap getSysMacroMap(List<String> sysMacros) {
        //TODO: it's faster to use sysAPTData.getMacroMap(configID, sysMacros);
        // but we need this ID to get somehow... how?
        APTMacroMap map = sysAPTData.getMacroMap(sysMacros.toString(), sysMacros);
        return map;
    }

    //@Deprecated
    public final APTPreprocHandler getPreprocHandler(File file) {
        return createPreprocHandler(file, getFileContainer().getPreprocState(file));
    }

    /*package*/ final APTPreprocHandler getPreprocHandler(File file, PreprocessorStatePair statePair) {
        return createPreprocHandler(file, statePair == null ? getFileContainer().getPreprocState(file) : statePair.state);
    }

    /* package */ final APTPreprocHandler createPreprocHandler(File file, APTPreprocHandler.State state) {
        APTPreprocHandler preprocHandler = createEmptyPreprocHandler(file);
        if (state != null) {
            if (state.isCleaned()) {
                return restorePreprocHandler(file, preprocHandler, state);
            } else {
                if (TRACE_PP_STATE_OUT) {
                    System.err.println("copying state for " + file);
                }
                preprocHandler.setState(state);
                return preprocHandler;
            }
        }
        if (TRACE_PP_STATE_OUT) {
            System.err.printf("null state for %s, returning default one", file);
        }
        return preprocHandler;
    }

    /*package-local*/ final Collection<PreprocessorStatePair> getPreprocessorStatePairs(File file) {
        return getFileContainer().getStatePairs(file);
    }

    public final Collection<APTPreprocHandler> getPreprocHandlers(File file) {
        Collection<APTPreprocHandler.State> states = getFileContainer().getPreprocStates(file);
        Collection<APTPreprocHandler> result = new ArrayList<APTPreprocHandler>(states.size());
        for (APTPreprocHandler.State state : states) {
            APTPreprocHandler preprocHandler = createEmptyPreprocHandler(file);
            if (state != null) {
                if (state.isCleaned()) {
                    preprocHandler = restorePreprocHandler(file, preprocHandler, state);
                } else {
                    if (TRACE_PP_STATE_OUT) {
                        System.err.println("copying state for " + file);
                    }
                    preprocHandler.setState(state);
                }
            }
            if (TRACE_PP_STATE_OUT) {
                System.err.printf("null state for %s, returning default one", file);
            }
            result.add(preprocHandler);
        }
        return result;
    }

    //@Deprecated
    public final APTPreprocHandler.State getPreprocState(FileImpl fileImpl) {
        APTPreprocHandler.State state = null;
        FileContainer fc = getFileContainer();
        File file = fileImpl.getBuffer().getFile();
        state = fc.getPreprocState(file);
        return state;
    }

    public final Collection<APTPreprocHandler.State> getPreprocStates(FileImpl fileImpl) {
        FileContainer fc = getFileContainer();
        return fc.getPreprocStates(fileImpl.getFile());
    }

    /**
     * This method for testing purpose only. Used from TraceModel
     */
    public final CsmFile testAPTParseFile(NativeFileItem item) {
        APTPreprocHandler preprocHandler = this.createPreprocHandler(item);
        return findFile(item.getFile(), false, getFileType(item), preprocHandler, true, preprocHandler.getState(), item);
    }

    /**
     * This method must be called only under stateLock,
     * to get state lock use
     * Object stateLock = getFileContainer().getLock(file);
     */
    private void putPreprocState(File file, APTPreprocHandler.State state) {
        assert state != null: "can not be null state for " + file;
        if (state != null && !state.isCleaned()) {
            state = APTHandlersSupport.createCleanPreprocState(state);
        }
        getFileContainer().putPreprocState(file, state);
    }

    protected final APTPreprocHandler.State setChangedFileState(NativeFileItem nativeFile) {
        APTPreprocHandler.State state;
        state = createPreprocHandler(nativeFile).getState();
        File file = nativeFile.getFile();
        FileContainer fileContainer = getFileContainer();
        FileContainer.FileEntry entry = fileContainer.getEntry(file);
        synchronized (entry.getLock()) {
            entry.invalidateStates();
            entry.setState(state, FilePreprocessorConditionState.PARSING);
        }
        fileContainer.put();
        return state;
    }

    protected final void invalidatePreprocState(File file) {
        FileContainer fileContainer = getFileContainer();
        Object stateLock = fileContainer.getLock(file);
        synchronized (stateLock) {
            fileContainer.invalidatePreprocState(file);
        }
        fileContainer.put();
    }

    /**
     * The method is for tracing/testing/debugging purposes only
     */
    public final void debugInvalidateFiles() {
        getFileContainer().debugClearState();
        for (Iterator<CsmProject> it = getLibraries().iterator(); it.hasNext();) {
            ProjectBase lib = (ProjectBase) it.next();
            lib.debugInvalidateFiles();
        }
    }

    private static final boolean TRACE_FILE = (TraceFlags.TRACE_FILE_NAME != null);
    /**
     * called to inform that file was #included from another file with specific preprocHandler
     *
     * @param file included file path
     * @param preprocHandler preprocHandler with which the file is including
     * @param mode of walker forced onFileIncluded for #include directive
     * @return true if it's first time of file including
     *          false if file was included before
     */
    public final FileImpl onFileIncluded(ProjectBase base, CharSequence file, APTPreprocHandler preprocHandler, APTMacroMap.State postIncludeState, int mode, boolean triggerParsingActivity) throws IOException {
        FileImpl csmFile = null;
        if (isDisposing()) {
            return null;
        }
        csmFile = findFile(new File(file.toString()), true, FileImpl.FileType.HEADER_FILE, preprocHandler, false, null, null);

        if (postIncludeState != null) {
            // we have post include state => no need to spend time in include walkers
            preprocHandler.getMacroMap().setState(postIncludeState);
            return csmFile;
        }
        if (isDisposing()) {
            return csmFile;
        }
        APTPreprocHandler.State newState = preprocHandler.getState();
        PreprocessorStatePair cachedOut = null;
        APTFileCacheEntry aptCacheEntry = null;
        FilePreprocessorConditionState pcState;
        if (mode == ProjectBase.GATHERING_TOKENS && !APTHandlersSupport.extractIncludeStack(newState).isEmpty()) {
            cachedOut = csmFile.getCachedVisitedState(newState);
        }
        if (cachedOut == null) {
            APTFile aptLight = getAPTLight(csmFile);
            if (aptLight == null) {
                // in the case file was just removed
                Utils.LOG.log(Level.INFO, "Can not find or build APT for file {0}", file); //NOI18N
                return csmFile;
            }

            // gather macro map from all includes and fill preprocessor conditions state
            FilePreprocessorConditionState.Builder pcBuilder = new FilePreprocessorConditionState.Builder(csmFile.getAbsolutePath());
            // ask for exclusive entry if absent
            aptCacheEntry = csmFile.getAPTCacheEntry(preprocHandler, Boolean.TRUE);
            APTParseFileWalker walker = new APTParseFileWalker(base, aptLight, csmFile, preprocHandler, triggerParsingActivity, pcBuilder,aptCacheEntry);
            walker.visit();
            pcState = pcBuilder.build();
            if (mode == ProjectBase.GATHERING_TOKENS && !APTHandlersSupport.extractIncludeStack(newState).isEmpty()) {
                csmFile.cacheVisitedState(newState, preprocHandler, pcState);
            }
        } else {
            preprocHandler.getMacroMap().setState(APTHandlersSupport.extractMacroMapState(cachedOut.state));
            pcState = cachedOut.pcState;
        }
        boolean updateFileContainer = false;
        try {
            if (isDisposing()) {
                return csmFile;
            }
            if (triggerParsingActivity) {
                FileContainer.FileEntry
                entry = getFileContainer().getEntry(csmFile.getBuffer().getFile());
                if (entry == null) {
                    entryNotFoundMessage(file);
                    return csmFile;
                }
                synchronized (entry.getLock()) {
                    List<PreprocessorStatePair> statesToKeep = new ArrayList<PreprocessorStatePair>(4);
                    AtomicBoolean newStateFound = new AtomicBoolean();
                    Collection<PreprocessorStatePair> entryStatePairs = entry.getStatePairs();
                    // Phase 1: check preproc states of entry comparing to current state
                    ComparisonResult comparisonResult = fillStatesToKeepBasedOnPPState(newState, entryStatePairs, statesToKeep, newStateFound);
                    if (TRACE_FILE && FileImpl.traceFile(file)) {
                        traceIncludeStates("comparison 2 " + comparisonResult, csmFile, newState, pcState, newStateFound.get(), null, statesToKeep); // NOI18N
                    }
                    if (comparisonResult == ComparisonResult.WORSE) {
                        if (TRACE_FILE && FileImpl.traceFile(file)) {
                            traceIncludeStates("worse 2", csmFile, newState, pcState, false, null, statesToKeep); // NOI18N
                        }
                        return csmFile;
                    } else if (comparisonResult == ComparisonResult.SAME) {
                        if (newStateFound.get()) {
                            // we are already in the list and not better than all, can stop
                            if (TRACE_FILE && FileImpl.traceFile(file)) {
                                traceIncludeStates("state is already here ", csmFile, newState, pcState, false, null, statesToKeep); // NOI18N
                            }
                            return csmFile;
                        }
                    }
                    // from that point we are NOT interested in what is in the entry:
                    // it's locked; "good" states are are in statesToKeep, "bad" states don't matter

                    assert comparisonResult != ComparisonResult.WORSE;

                    boolean clean;

                    List<APTPreprocHandler.State> statesToParse = new ArrayList<APTPreprocHandler.State>(4);
                    statesToParse.add(newState);

                    if (comparisonResult == ComparisonResult.BETTER) {
                        clean = true;
                        CndUtils.assertTrueInConsole(statesToKeep.isEmpty(), "states to keep must be empty 2"); // NOI18N
                        if (TRACE_FILE && FileImpl.traceFile(file)) {
                            traceIncludeStates("best state", csmFile, newState, pcState, clean, statesToParse, statesToKeep); // NOI18N
                        }
                    } else {  // comparisonResult == SAME
                        clean = false;
                        // Phase 2: check preproc conditional states of entry comparing to current conditional state
                        comparisonResult = fillStatesToKeepBasedOnPCState(pcState, new ArrayList<PreprocessorStatePair>(statesToKeep), statesToKeep);
                        if (TRACE_FILE && FileImpl.traceFile(file)) {
                            traceIncludeStates("pc state comparison " + comparisonResult, csmFile, newState, pcState, clean, statesToParse, statesToKeep); // NOI18N
                        }
                        switch (comparisonResult) {
                            case BETTER:
                                CndUtils.assertTrueInConsole(statesToKeep.isEmpty(), "states to keep must be empty 3"); // NOI18N
                                clean = true;
                                break;
                            case SAME:
                                break;
                            case WORSE:
                                return csmFile;
                            default:
                                assert false : "unexpected comparison result: " + comparisonResult; //NOI18N
                                return csmFile;
                        }
                    }
                    // TODO: think over, what if we aready changed entry,
                    // but now deny parsing, because base, but not this project, is disposing?!
                    if (!isDisposing() && !base.isDisposing()) {
                        if (clean) {
                            for (PreprocessorStatePair pair : statesToKeep) {
                                // if pair has parsing in pair.pcState => it was not valid source file
                                // skip it
                                if (pair.pcState != FilePreprocessorConditionState.PARSING) {
                                    statesToParse.add(pair.state);
                                }
                            }
                        }
                        csmFile.setAPTCacheEntry(preprocHandler, aptCacheEntry, clean);
                        entry.setStates(statesToKeep, new PreprocessorStatePair(newState, pcState));
                        ParserQueue.instance().add(csmFile, statesToParse, ParserQueue.Position.HEAD, clean,
                                clean ? ParserQueue.FileAction.MARK_REPARSE : ParserQueue.FileAction.MARK_MORE_PARSE);
                        csmFile.setAPTCacheEntry(preprocHandler, aptCacheEntry, clean);
                        if (TRACE_FILE && FileImpl.traceFile(file) &&
                                (TraceFlags.TRACE_PC_STATE || TraceFlags.TRACE_PC_STATE_COMPARISION)) {
                            traceIncludeStates("scheduling", csmFile, newState, pcState, clean, // NOI18N
                                    statesToParse, statesToKeep);
                        }
                        updateFileContainer = true;
                    }
                }
            }
            return csmFile;
        } finally {
            if (updateFileContainer) {
                getFileContainer().put();
            }
        }
    }

    private void entryNotFoundMessage(CharSequence file) {
        if (Utils.LOG.isLoggable(Level.INFO)) {
            // since file container can return empty container the entry can be null.
            StringBuilder buf = new StringBuilder("File container does not have file "); //NOI18N
            buf.append("[").append(file).append("]"); //NOI18N
            if (getFileContainer() == FileContainer.empty()) {
                buf.append(" because file container is EMPTY."); //NOI18N
            } else {
                buf.append("."); //NOI18N
            }
            if (isDisposing()) {
                buf.append("\n\tIt is very strange but project is disposing."); //NOI18N
            }
            if (!isValid()) {
                buf.append("\n\tIt is very strange but project is invalid."); //NOI18N
            }
            Status st = getStatus();
            if (st != null) {
                buf.append("\n\tProject ").append(toString()).append(" has status ").append(st).append("."); //NOI18N
            }
            Utils.LOG.info(buf.toString());
        }
    }

    private static void traceIncludeStates(CharSequence title,
            FileImpl file, APTPreprocHandler.State newState, FilePreprocessorConditionState pcState,
            boolean clean, Collection<APTPreprocHandler.State> statesToParse, Collection<PreprocessorStatePair> statesToKeep) {

        StringBuilder sb = new StringBuilder();
        for (PreprocessorStatePair pair : statesToKeep) {
            if (sb.length() > 0) {
                sb.append(", "); //NOI18N
            }
            sb.append(pair.pcState);
        }


        APTPreprocHandler preprocHandler = file.getProjectImpl(true).createEmptyPreprocHandler(file.getBuffer().getFile());
        preprocHandler.setState(newState);

        System.err.printf("%s %s (1) %s\n\tfrom %s \n\t%s %s \n\t%s keeping [%s]\n", title, //NOI18N
                (clean ? "reparse" : "  parse"), file.getAbsolutePath(), //NOI18N
                APTHandlersSupport.extractStartEntry(newState).getStartFile(),
                TraceUtils.getPreprocStateString(preprocHandler.getState()),
                TraceUtils.getMacroString(preprocHandler, TraceFlags.logMacros),
                pcState, sb);

        if (statesToParse != null) {
            for (APTPreprocHandler.State state : statesToParse) {
                if (!newState.equals(state)) {
                    FilePreprocessorConditionState currPcState = null;
                    for (PreprocessorStatePair pair : statesToKeep) {
                        if (newState.equals(pair.state)) {
                            currPcState = pair.pcState;
                            break;
                        }
                    }
                    System.err.printf("%s %s (2) %s \n\tfrom %s\n\t valid %b context %b %s\n", title,//NOI18N
                            "  parse", file.getAbsolutePath(), //NOI18N
                            APTHandlersSupport.extractStartEntry(state).getStartFile(),
                            state.isValid(), state.isCompileContext(), currPcState);
                }
            }
        }
    }

    boolean setParsedPCState(FileImpl csmFile, State ppState, FilePreprocessorConditionState pcState) {
        File file = csmFile.getBuffer().getFile();
        FileContainer.FileEntry entry = getFileContainer().getEntry(file);
        if (entry == null) {
            entryNotFoundMessage(file.getAbsolutePath());
            return false;
        }
        boolean entryFound;
        // IZ#179861: unstable test RepositoryValidation
        synchronized (entry.getLock()) {
            List<PreprocessorStatePair> statesToKeep = new ArrayList<PreprocessorStatePair>(4);
            Collection<PreprocessorStatePair> entryStatePairs = entry.getStatePairs();
            List<PreprocessorStatePair> copy = new ArrayList<PreprocessorStatePair>();
            entryFound = false;
            // put into copy array all except ourself
            for (PreprocessorStatePair pair : entryStatePairs) {
                assert pair != null : "can not be null element in " + entryStatePairs;
                assert pair.state != null: "state can not be null in pair " + pair + " for file " + csmFile;
                if ((pair.pcState == FilePreprocessorConditionState.PARSING) && pair.state.equals(ppState)) {
                    assert !entryFound;
                    entryFound = true;
                } else {
                    copy.add(pair);
                }
            }
            if (entryFound) {
                // Phase 2: check preproc conditional states of entry comparing to current conditional state
                ComparisonResult comparisonResult = fillStatesToKeepBasedOnPCState(pcState, copy, statesToKeep);
                switch (comparisonResult) {
                    case BETTER:
                        CndUtils.assertTrueInConsole(statesToKeep.isEmpty(), "states to keep must be empty 3"); // NOI18N
                        entry.setStates(statesToKeep, new PreprocessorStatePair(ppState, pcState));
                        break;
                    case SAME:
                        assert !statesToKeep.isEmpty();
                        entry.setStates(statesToKeep, new PreprocessorStatePair(ppState, pcState));
                        break;
                    case WORSE:
                        assert !copy.isEmpty();
                        entry.setStates(copy, null);
                        break;
                    default:
                        assert false : "unexpected comparison result: " + comparisonResult; //NOI18N
                        break;
                }
            } else {
                // we already were removed, because our ppState was worse
                // or
                // header was parsed with correct context =>
                // no reason to check pcState and replace FilePreprocessorConditionState.PARSING
                // which is not present
            }
        }
        if (entryFound) {
            FileContainer fileContainer = getFileContainer();
            fileContainer.put();
        }
        return entryFound;
    }

    void notifyOnWaitParseLock() {
        // notify client waiting for end of fake registration
        synchronized (waitParseLock) {
            waitParseLock.notifyAll();
        }
    }

    enum ComparisonResult {

        BETTER,
        SAME,
        WORSE
    }

    /**
     * Checks old states and new one, decides
     * 1. which states to keep
     *    (returns collection of these states)
     * 2. is new state better, worse, or ~same than old ones
     *
     * NB: all OUT parameters are set in this function, so their initial values don't matter
     *
     * @param newState  IN:  new proprocessor state
     *
     * @param oldStates IN:  a collection of old states;
     *                       it might contain newState as well
     *
     * @param statesToKeep  OUT: aray to fill with of old states
     *                      (except for new state! - it isnt copied here)
     *                      Unpredictable in the case function returns WORSE
     *
     * @param  newStateFound  OUT: set to true if new state is found among old ones
     *
     * @return  BETTER - new state is better than old ones
     *          SAME - new state is more or less  the same :) as old ones
     *          WORSE - new state is worse than old ones
     */
    private ComparisonResult fillStatesToKeepBasedOnPPState(
            APTPreprocHandler.State newState,
            Collection<PreprocessorStatePair> oldStates,
            Collection<PreprocessorStatePair> statesToKeep,
            AtomicBoolean newStateFound) {

        if (newState == null || !newState.isValid()) {
            return ComparisonResult.WORSE;
        }

        statesToKeep.clear();
        newStateFound.set(false);
        ComparisonResult result = ComparisonResult.SAME;

        for (PreprocessorStatePair pair : oldStates) {
            // newState might already be contained in oldStates
            // it should NOT be added to result
            if (newState.equals(pair.state)) {
                assert !newStateFound.get();
                newStateFound.set(true);
            } else {
                boolean keep = false;
                if (pair.state != null && pair.state.isValid()) {
                    if (pair.state.isCompileContext()) {
                        keep = true;
                        if (!newState.isCompileContext()) {
                            return ComparisonResult.WORSE;
                        }
                    } else {
                        keep = !newState.isCompileContext();
                    }
                }
                if (keep) {
                    if (!pair.state.isCleaned()) {
                        pair = new PreprocessorStatePair(APTHandlersSupport.createCleanPreprocState(pair.state), pair.pcState);
                    }
                    statesToKeep.add(pair);
                } else {
                    result = ComparisonResult.BETTER;
                }
            }
        }
        if (result == ComparisonResult.BETTER) {
            CndUtils.assertTrueInConsole(statesToKeep.isEmpty(), "states to keep must be empty "); // NOI18N
        }
        return result;
    }

    /**
     * If it returns WORSE, statesToKeep content is unpredictable!
     *
     * @param newState
     * @param pcState
     * @param oldStates
     * @param statesToKeep
     * @return
     */
    private ComparisonResult fillStatesToKeepBasedOnPCState(
            FilePreprocessorConditionState pcState,
            List<PreprocessorStatePair> oldStates,
            List<PreprocessorStatePair> statesToKeep) {

        boolean isSuperset = true; // true if this state is a superset of each old state

        // we assume that
        // 1. all oldStates are valid
        // 2. either them all are compileContext
        //    or this one and them all are NOT compileContext
        // so we do *not* check isValid & isCompileContext

        statesToKeep.clear();
        // in this place use direct for loop over list with known size
        // instead of "for (PreprocessorStatePair old : oldStates)"
        // due to performance problem of iterator.hasNext
        int size = oldStates.size();
        for (int i = 0; i < size; i++) {
            PreprocessorStatePair old = oldStates.get(i);
            if (old.pcState == FilePreprocessorConditionState.PARSING) {
                isSuperset = false;
                // not yet filled - file parsing is filling it right now => we don't know what it will be => keep it
                if (!old.state.isCleaned()) {
                    old = new PreprocessorStatePair(APTHandlersSupport.createCleanPreprocState(old.state), old.pcState);
                }
                statesToKeep.add(old);
            } else {
                if (old.pcState.isBetterOrEqual(pcState)) {
                    return ComparisonResult.WORSE;
                } else if (pcState.isBetterOrEqual(old.pcState)) {
                    // still superset or current can replace old
                } else {
                    // states are not comparable => not superset
                    isSuperset = false;
                    if (!old.state.isCleaned()) {
                        old = new PreprocessorStatePair(APTHandlersSupport.createCleanPreprocState(old.state), old.pcState);
                    }
                    statesToKeep.add(old);
                }
            }
        }
        if (isSuperset) {
            assert statesToKeep.isEmpty() : "should be empty, but it is: " + Arrays.toString(statesToKeep.toArray());
            return ComparisonResult.BETTER;
        } else {
            return ComparisonResult.SAME;
        }
    }

//    private static final boolean isValid(APTPreprocHandler.State state) {
//        return state != null && state.isValid();
//    }
    public ProjectBase findFileProject(CharSequence absPath) {
        // check own files
        // Wait while files are created. Otherwise project file will be recognized as library file.
        ensureFilesCreated();
        File file = new File(absPath.toString());
        if (getFile(file, false) != null) {
            return this;
        } else {
            // else check in libs
            for (CsmProject prj : getLibraries()) {
                // Wait while files are created. Otherwise project file will be recognized as library file.
                ((ProjectBase) prj).ensureFilesCreated();
                if (((ProjectBase) prj).getFile(file, false) != null) {
                    return (ProjectBase) prj;
                }
            }
        }
        return null;
    }

    public final boolean isMySource(String includePath) {
        return projectRoots.isMySource(includePath);
    }

    public abstract void onFileAdded(NativeFileItem nativeFile);

    public abstract void onFileAdded(List<NativeFileItem> items);
    //public abstract void onFileRemoved(NativeFileItem nativeFile);

    public abstract void onFileRemoved(FileImpl fileImpl);

    public abstract void onFileImplRemoved(List<FileImpl> files);

    public abstract void onFileRemoved(List<NativeFileItem> items);

    public abstract void onFilePropertyChanged(NativeFileItem nativeFile);

    public abstract void onFilePropertyChanged(List<NativeFileItem> items);

    protected abstract ParserQueue.Position getIncludedFileParserQueuePosition();

    public abstract NativeFileItem getNativeFileItem(CsmUID<CsmFile> file);

    protected abstract void putNativeFileItem(CsmUID<CsmFile> file, NativeFileItem nativeFileItem);

    protected abstract void removeNativeFileItem(CsmUID<CsmFile> file);

    protected abstract void clearNativeFileContainer();

    public final void onFileRemoved(File nativeFile) {
        onFileRemoved(getFile(nativeFile, false));
    }

    public final void onFileExternalCreate(FileObject file) {
        CndFileUtils.clearFileExistenceCache();
        DeepReparsingUtils.reparseOnAdded(file, this);
    }

    public final void onFileExternalChange(FileImpl file) {
        DeepReparsingUtils.reparseOnEdit(file, this);
    }

    @Override
    public final CsmFile findFile(Object absolutePathOrNativeFileItem, boolean snapShot) {
        CsmFile res = null;
        if (absolutePathOrNativeFileItem instanceof CharSequence) {
            res = findFileByPath((CharSequence) absolutePathOrNativeFileItem);
        } else if (absolutePathOrNativeFileItem instanceof NativeFileItem) {
            res = findFileByItem((NativeFileItem) absolutePathOrNativeFileItem);
        }
        if (snapShot && (res instanceof FileImpl)) {
            res = ((FileImpl)res).getSnapshot();
        }
        return res;
    }

    private CsmFile findFileByPath(CharSequence absolutePath) {
        File file = new File(absolutePath.toString());
        APTPreprocHandler preprocHandler = null;
        if (getFileContainer().getPreprocState(file) == null) {
            NativeFileItem nativeFile = null;
            // Try to find native file
            if (getPlatformProject() instanceof NativeProject) {
                NativeProject prj = (NativeProject) getPlatformProject();
                if (prj != null) {
                    nativeFile = prj.findFileItem(file);
                    if (nativeFile == null) {
                        // if not belong to NB project => not our file
                        return null;
                    // nativeFile = new DefaultFileItem(prj, absolutePath);
                    }
                    if (!acceptNativeItem(nativeFile)) {
                        return null;
                    }
                    preprocHandler = createPreprocHandler(nativeFile);
                }
            }
            if (preprocHandler != null) {
                return findFile(file, false, FileImpl.FileType.UNDEFINED_FILE, preprocHandler, true, preprocHandler.getState(), nativeFile);
            }
        }
        // if getPreprocState(file) isn't null, the file alreasy exists, so we may not pass nativeFile
        return findFile(file, false, FileImpl.FileType.UNDEFINED_FILE, preprocHandler, true, null, null);
    }

    private CsmFile findFileByItem(NativeFileItem nativeFile) {
        File file = nativeFile.getFile().getAbsoluteFile();
        APTPreprocHandler preprocHandler = null;
        if (getFileContainer().getPreprocState(file) == null) {
            if (!acceptNativeItem(nativeFile)) {
                return null;
            }
            // Try to find native file
            if (getPlatformProject() instanceof NativeProject) {
                NativeProject prj = nativeFile.getNativeProject();
                if (prj != null && nativeFile.getFile() != null) {
                    preprocHandler = createPreprocHandler(nativeFile);
                }
            }
            if (preprocHandler != null) {
                return findFile(file, false, FileImpl.FileType.UNDEFINED_FILE, preprocHandler, true, preprocHandler.getState(), nativeFile);
            }
        }
        // if getPreprocState(file) isn't null, the file alreasy exists, so we may not pass nativeFile
        return findFile(file, false, FileImpl.FileType.UNDEFINED_FILE, preprocHandler, true, null, null);
    }

    protected final FileImpl findFile(File file, boolean treatSymlinkAsSeparateFile, FileImpl.FileType fileType, APTPreprocHandler preprocHandler,
            boolean scheduleParseIfNeed, APTPreprocHandler.State initial, NativeFileItem nativeFileItem) {
        FileImpl impl = getFile(file, treatSymlinkAsSeparateFile);
        if (impl == null){
            impl = findFileImpl(file, treatSymlinkAsSeparateFile, fileType, preprocHandler, scheduleParseIfNeed, initial, nativeFileItem);
        }
        return impl;
    }

    private FileImpl findFileImpl(File file, boolean treatSymlinkAsSeparateFile, FileImpl.FileType fileType, APTPreprocHandler preprocHandler,
            boolean scheduleParseIfNeed, APTPreprocHandler.State initial, NativeFileItem nativeFileItem) {
        FileImpl impl = null;
        synchronized (fileContainerLock) {
            impl = getFile(file, treatSymlinkAsSeparateFile);
            if (impl == null) {
                preprocHandler = (preprocHandler == null) ? getPreprocHandler(file) : preprocHandler;
                impl = new FileImpl(ModelSupport.getFileBuffer(file), this, fileType, nativeFileItem);
                if (nativeFileItem != null) {
                    putNativeFileItem(impl.getUID(), nativeFileItem);
                }
                putFile(file, impl, initial);
                // NB: parse only after putting into a map
                if (scheduleParseIfNeed) {
                    APTPreprocHandler.State ppState = preprocHandler == null ? null : preprocHandler.getState();
                    ParserQueue.instance().add(impl, ppState, ParserQueue.Position.TAIL);
                }
            }
        }
        if (fileType == FileImpl.FileType.SOURCE_FILE && !impl.isSourceFile()) {
            impl.setSourceFile();
        } else if (fileType == FileImpl.FileType.HEADER_FILE && !impl.isHeaderFile()) {
            impl.setHeaderFile();
        }
        return impl;
    }

//    protected FileImpl createOrFindFileImpl(final NativeFileItem nativeFile) {
//	File file = nativeFile.getFile();
//	assert file != null;
//	return createOrFindFileImpl(ModelSupport.instance().getFileBuffer(file), nativeFile);
//    }
    protected final FileImpl createOrFindFileImpl(final FileBuffer buf, final NativeFileItem nativeFile) {
        return createOrFindFileImpl(buf, nativeFile, getFileType(nativeFile)).fileImpl;
    }

    private static class FileAndHandler {

        public FileAndHandler(FileImpl fileImpl, APTPreprocHandler preprocHandler) {
            this.fileImpl = fileImpl;
            this.preprocHandler = preprocHandler;
        }
        public FileImpl fileImpl;
        public APTPreprocHandler preprocHandler;
    }

    private FileAndHandler createOrFindFileImpl(final FileBuffer buf, final NativeFileItem nativeFile, FileImpl.FileType fileType) {
        APTPreprocHandler preprocHandler = null;
        File file = buf.getFile();
        FileImpl impl = getFile(file, true);
        CsmUID<CsmFile> aUid = null;
        if (impl == null) {
            preprocHandler = createPreprocHandler(nativeFile);
            synchronized (fileContainerLock) {
                impl = getFile(file, true);
                if (impl == null) {
                    assert preprocHandler != null;
                    impl = new FileImpl(buf, this, fileType, nativeFile);
                    putFile(file, impl, preprocHandler.getState());
                } else {
                    aUid = impl.getUID();
                }
            }
        } else {
            aUid = impl.getUID();
        }
        if (aUid != null) {
            putNativeFileItem(aUid, nativeFile);
        }
        return new FileAndHandler(impl, preprocHandler);
    }

    public final FileImpl getFile(File file, boolean treatSymlinkAsSeparateFile) {
        return getFileContainer().getFile(file, treatSymlinkAsSeparateFile);
    }

    protected final void removeFile(CharSequence file) {
        getFileContainer().removeFile(file);
    }

    protected final void putFile(File file, FileImpl impl, APTPreprocHandler.State state) {
        if (state != null && !state.isCleaned()) {
            state = APTHandlersSupport.createCleanPreprocState(state);
        }
        getFileContainer().putFile(file, impl, state);
    }

    protected Collection<Key> getLibrariesKeys() {
        List<Key> res = new ArrayList<Key>();
        if (platformProject instanceof NativeProject) {
            for (NativeProject nativeLib : ((NativeProject) platformProject).getDependences()) {
                final Key key = createProjectKey(nativeLib);
                if (key != null) {
                    res.add(key);
                }
            }
        }
        // Last dependent project is common library.
        //final Key lib = KeyUtilities.createProjectKey("/usr/include"); // NOI18N
        //if (lib != null) {
        //    res.add(lib);
        //}
        if (!isArtificial()) {
            for (CsmUID<CsmProject> library : LibraryManager.getInstance().getLirariesKeys(getUID())) {
                res.add(RepositoryUtils.UIDtoKey(library));
            }
        }
        return res;
    }

    @Override
    public List<CsmProject> getLibraries() {
        List<CsmProject> res = new ArrayList<CsmProject>();
        if (platformProject instanceof NativeProject) {
            List<NativeProject> dependences = ((NativeProject) platformProject).getDependences();
            int size = dependences.size();
            for (int i = 0; i < size; i++) {
                NativeProject nativeLib = dependences.get(i);
                CsmProject prj = model.findProject(nativeLib);
                if (prj != null) {
                    res.add(prj);
                }
            }
        }
        // Last dependent project is common library.
        //ProjectBase lib = getModel().getLibrary("/usr/include"); // NOI18N
        //if (lib != null) {
        //    res.add(lib);
        //}
        if (!isArtificial()) {
            List<LibProjectImpl> libraries = LibraryManager.getInstance().getLibraries((ProjectImpl) this);
            int size = libraries.size();
            for (int i = 0; i < size; i++) {
                res.add(libraries.get(i));
            }
        }
        return res;
    }

    public final List<ProjectBase> getDependentProjects() {
        List<ProjectBase> res = new ArrayList<ProjectBase>();
        for (CsmProject prj : model.projects()) {
            if (prj instanceof ProjectBase) {
                if (prj.getLibraries().contains(this)) {
                    res.add((ProjectBase) prj);
                }
            }
        }
        return res;
    }

    /**
     * Creates a dummy ClassImpl for uresolved name, stores in map
     * @param nameTokens name
     * @param file file that contains unresolved name (used for the purpose of statictics)
     * @param name offset that contains unresolved name (used for the purpose of statictics)
     */
    public final CsmClass getDummyForUnresolved(CharSequence[] nameTokens, CsmFile file, int offset) {
        if (Diagnostic.needStatistics()) {
            Diagnostic.onUnresolvedError(nameTokens, file, offset);
        }
        return getUnresolved().getDummyForUnresolved(nameTokens);
    }

    /**
     * Creates a dummy ClassImpl for uresolved name, stores in map.
     * Should be used only when restoring from persistence:
     * in contrary to getDummyForUnresolved(String[] nameTokens, CsmFile file, int offset),
     * it does not gather statistics!
     * @param nameTokens name
     */
    public final CsmClass getDummyForUnresolved(CharSequence name) {
        return getUnresolved().getDummyForUnresolved(name);
    }

    public final CsmNamespace getUnresolvedNamespace() {
        return getUnresolved().getUnresolvedNamespace();
    }

    public final CsmFile getUnresolvedFile() {
        return getUnresolved().getUnresolvedFile();
    }

    private synchronized Unresolved getUnresolved() {
        // we don't sinc here since this isn't important enough:
        // at worst a map with one or two dummies will be thrown away
        if (unresolved == null) {
            unresolved = new Unresolved(this);
        }
        return unresolved;
    }

    @Override
    public final boolean isValid() {
        return platformProject != null && !isDisposing();
    }

    public void setDisposed() {
        disposing.set(true);
        synchronized (initializationTaskLock) {
            if (initializationTask != null) {
                initializationTask.cancel();
                initializationTask = null;
            }
        }
        unregisterProjectListeners();
        ParserQueue.instance().removeAll(this);
    }

    public final boolean isDisposing() {
        return disposing.get();
    }

    public final void dispose(final boolean cleanPersistent) {

        long time = 0;
        if (TraceFlags.TIMING) {
            System.err.printf("\n\nProject %s: disposing...\n", name);
            time = System.currentTimeMillis();
        }

        // just in case it wasn't called before (it's inexpensive)
        setDisposed();

        try {

            disposeLock.writeLock().lock();

            ProjectSettingsValidator validator = new ProjectSettingsValidator(this);
            validator.storeSettings();
            getUnresolved().dispose();
            RepositoryUtils.closeUnit(getUID(), getRequiredUnits(), cleanPersistent);

            weakClassifierContainer = null;
            weakDeclarationContainer = null;
            weakFileContainer = null;
            weakGraphContainer = null;

            platformProject = null;
            unresolved = null;
            uid = null;
        } finally {
            disposeLock.writeLock().unlock();
        }

        if (TraceFlags.TIMING) {
            time = System.currentTimeMillis() - time;
            System.err.printf("Project %s: disposing took %d ms\n", name, time);
        }
    }

    protected final Set<String> getRequiredUnits() {
        Set<String> requiredUnits = new HashSet<String>();
        for (Key dependent : this.getLibrariesKeys()) {
            requiredUnits.add(dependent.getUnit().toString());
        }
        return requiredUnits;
    }

//    private void disposeFiles() {
//        Collection<FileImpl> list = getFileContainer().getFileImpls();
//        getFileContainer().clear();
//        for (FileImpl file : list){
//            file.onProjectClose();
//            APTDriver.getInstance().invalidateAPT(file.getBuffer());
//        }
//        //clearNativeFileContainer();
//    }
    private int preventMultiplyDiagnosticExceptionsGlobalNamespace = 0;
    private NamespaceImpl _getGlobalNamespace() {
        NamespaceImpl ns = (NamespaceImpl) UIDCsmConverter.UIDtoNamespace(globalNamespaceUID);
        if (ns == null && preventMultiplyDiagnosticExceptionsGlobalNamespace < 5) {
            DiagnosticExceptoins.register(new IllegalStateException("Failed to get global namespace by key " + globalNamespaceUID)); // NOI18N
            preventMultiplyDiagnosticExceptionsGlobalNamespace++;
        }
        return ns != null ? ns : FAKE_GLOBAL_NAMESPACE;
    }

    private NamespaceImpl _getNamespace(CharSequence key) {
        key = CharSequences.create(key);
        CsmUID<CsmNamespace> nsUID = namespaces.get(key);
        NamespaceImpl ns = (NamespaceImpl) UIDCsmConverter.UIDtoNamespace(nsUID);
        return ns;
    }

    private void _registerNamespace(NamespaceImpl ns) {
        assert (ns != null);
        CharSequence key = ns.getQualifiedName();
        assert CharSequences.isCompact(key);
        CsmUID<CsmNamespace> nsUID = RepositoryUtils.<CsmNamespace>put(ns);
        assert nsUID != null;
        namespaces.put(key, nsUID);
    }

    private void _unregisterNamespace(NamespaceImpl ns) {
        assert (ns != null);
        assert !ns.isGlobal();
        CharSequence key = ns.getQualifiedName();
        assert CharSequences.isCompact(key);
        CsmUID<CsmNamespace> nsUID = namespaces.remove(key);
        assert nsUID != null;
        RepositoryUtils.remove(nsUID, ns);
    }

    protected final ModelImpl getModel() {
        return model;
    }

    public void onFileEditStart(FileBuffer buf, NativeFileItem nativeFile) {
    }

    public void onFileEditEnd(FileBuffer buf, NativeFileItem nativeFile) {
    }
    private CsmUID<CsmProject> uid = null;

    @Override
    public final CsmUID<CsmProject> getUID() { // final because called from constructor
        CsmUID<CsmProject> out = uid;
        if (out == null) {
            synchronized (this) {
                if (uid == null) {
                    uid = out = UIDUtilities.createProjectUID(this);
                    if (TraceFlags.TRACE_CPU_CPP) {System.err.println("getUID for project UID@"+System.identityHashCode(uid) + uid + "on prj@"+System.identityHashCode(this));}
                }
            }
        }
        return uid;
    }

    @Override
    public boolean isStable(CsmFile skipFile) {
        if (status == Status.Ready && !isDisposing()) {
            return !ParserQueue.instance().hasPendingProjectRelatedWork(this, (FileImpl) skipFile);
        }
        return false;
    }

    public final void onParseFinish() {
        onParseFinishImpl(false);
    }

    private void onParseFinishImpl(boolean libsAlreadyParsed) {
        synchronized (waitParseLock) {
            waitParseLock.notifyAll();
        }
        // it's ok to move the entire sycle into synchronized block,
        // because from inter-session persistence point of view,
        // if we don't fix fakes, we'll later consider that files are ok,
        // which is incorrect if there are some fakes
        try {
            disposeLock.readLock().lock();

            if (!isDisposing()) {
                fixFakeRegistration(libsAlreadyParsed);
            }
        } catch (Exception e) {
            DiagnosticExceptoins.register(e);
        } finally {
            disposeLock.readLock().unlock();
            ProjectComponent.setStable(declarationsSorageKey);
            ProjectComponent.setStable(fileContainerKey);
            ProjectComponent.setStable(graphStorageKey);
            ProjectComponent.setStable(classifierStorageKey);
            checkStates(this, libsAlreadyParsed);

            if (!libsAlreadyParsed) {
                ParseFinishNotificator.onParseFinish(this);
            }
        }
        if (TraceFlags.PARSE_STATISTICS) {
            ParseStatistics.getInstance().printResults(this);
            ParseStatistics.getInstance().clear(this);
        }
    }

    private static void checkStates(ProjectBase prj, boolean libsAlreadyParsed){
        if (false) {
            System.err.println("Checking states for project "+prj.getName());
            for(Map.Entry<CharSequence, FileEntry> entry : prj.getFileContainer().getFileStorage().entrySet()){
                for(PreprocessorStatePair pair : entry.getValue().getStatePairs()){
                    if (!pair.state.isValid()){
                        System.err.println("Invalid state for file "+entry.getKey());
                    }
                }
            }
            if (libsAlreadyParsed) {
                for(CsmProject p : prj.getLibraries()){
                    if (p instanceof ProjectBase) {
                        checkStates((ProjectBase) p, false);
                    }
                }
            }
        }
    }

    private void fixFakeRegistration(boolean libsAlreadyParsed){
        Collection<CsmUID<CsmFile>> files = getAllFilesUID();
        int size = files.size();
        int threads = CndUtils.getNumberCndWorkerThreads()*3;
        CountDownLatch countDownLatch = new CountDownLatch(threads);
        int chunk = (size/threads) + 1;
        Iterator<CsmUID<CsmFile>> it = files.iterator();
        for (int i = 0; i < threads; i++) {
            ArrayList<CsmUID<CsmFile>> list = new ArrayList<CsmUID<CsmFile>>(chunk);
            for(int j = 0; j < chunk; j++) {
                if (it.hasNext()) {
                    list.add(it.next());
                } else {
                    break;
                }
            }
            FixRegistrationRunnable r = new FixRegistrationRunnable(countDownLatch, list, libsAlreadyParsed, disposing);
            PROJECT_FILES_WORKER.post(r);
        }
        try {
            countDownLatch.await();
            if (libsAlreadyParsed) {
                cleanAllFakeFunctionAST();
            }
        } catch (InterruptedException ex) {
        }
    }

    /* collection to keep fake ASTs during parse phase */
    private final Map<CsmUID<CsmFile>, Map<CsmUID<FunctionImplEx>, AST>> fakeASTs = new WeakHashMap<CsmUID<CsmFile>, Map<CsmUID<FunctionImplEx>, AST>>();
    /*package*/final void trackFakeFunctionAST(CsmUID<CsmFile> fileUID, CsmUID<FunctionImplEx> funUID, AST funAST) {
        synchronized (fakeASTs) {
            Map<CsmUID<FunctionImplEx>, AST> fileASTs = fakeASTs.get(fileUID);
            if (fileASTs == null) {
                // create always
                fileASTs = new HashMap<CsmUID<FunctionImplEx>, AST>();
                if (funAST != null) {
                    // remember new only if not null AST
                    fakeASTs.put(fileUID, fileASTs);
                }
            }
            if (funAST == null) {
                fileASTs.remove(funUID);
            } else {
                fileASTs.put(funUID, funAST);
            }
        }
    }

    /*package*/final void cleanAllFakeFunctionAST(CsmUID<CsmFile> fileUID) {
        synchronized (fakeASTs) {
            fakeASTs.remove(fileUID);
        }
    }

    private void cleanAllFakeFunctionAST() {
        synchronized (fakeASTs) {
            fakeASTs.clear();
        }
    }

    /*package*/AST getFakeFunctionAST(CsmUID<CsmFile> fileUID, CsmUID<FunctionImplEx> fakeUid) {
        synchronized (fakeASTs) {
            Map<CsmUID<FunctionImplEx>, AST> fileASTs = fakeASTs.get(fileUID);
            return fileASTs == null ? null : fileASTs.get(fakeUid);
        }
    }

    /*package*/final void onLibParseFinish() {
        onParseFinishImpl(true);
    }

    /**
     * CsmProject implementation
     */
    @Override
    public final Collection<CsmFile> getAllFiles() {
        return getFileContainer().getFiles();
    }

    /**
     * CsmProject implementation
     */
    public final Collection<CsmUID<CsmFile>> getAllFilesUID() {
        return getFileContainer().getFilesUID();
    }

    /**
     * We'd better name this getFiles();
     * but unfortunately there already is such method,
     * and it is used intensively
     */
    public final Collection<FileImpl> getAllFileImpls() {
        return getFileContainer().getFileImpls();
    }

    @Override
    public final Collection<CsmFile> getSourceFiles() {
        List<CsmUID<CsmFile>> uids = new ArrayList<CsmUID<CsmFile>>();
        for (FileImpl file : getAllFileImpls()) {
            if (file.isSourceFile()) {
                uids.add(file.getUID());
            }
        }
        return new LazyCsmCollection<CsmFile, CsmFile>(uids, TraceFlags.SAFE_UID_ACCESS);
    }

    @Override
    public final Collection<CsmFile> getHeaderFiles() {
        List<CsmUID<CsmFile>> uids = new ArrayList<CsmUID<CsmFile>>();
        for (FileImpl file : getAllFileImpls()) {
            if (!file.isSourceFile()) {
                uids.add(file.getUID());
            }
        }
        return new LazyCsmCollection<CsmFile, CsmFile>(uids, TraceFlags.SAFE_UID_ACCESS);
    }

    public final long getMemoryUsageEstimation() {
        //TODO: replace with some smart algorythm
        return getFileContainer().getSize();
    }

    @Override
    public final String toString() {
        return getName().toString() + ' ' + getClass().getName() + " @" + hashCode() + ":" + System.identityHashCode(this); // NOI18N
    }

    private volatile int hash = 0;

    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = super.hashCode();
        }
        return hash;
    }

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object obj) {
        return obj == this;
    }

    /**
     * Just a struct for the getStartEntryInfo return valie:
     * if java allowed passing pointers by reference, we won't create this...
     */
    private static class /*struct*/ StartEntryInfo {

        public final APTPreprocHandler preprocHandler;
        public final ProjectBase startProject;
        public final FileImpl csmFile;

        public StartEntryInfo(APTPreprocHandler preprocHandler, ProjectBase startProject, FileImpl csmFile) {
            this.preprocHandler = preprocHandler;
            this.startProject = startProject;
            this.csmFile = csmFile;
        }
    }

    private StartEntryInfo getStartEntryInfo(APTPreprocHandler preprocHandler, APTPreprocHandler.State state) {
        StartEntry startEntry = APTHandlersSupport.extractStartEntry(state);
        ProjectBase startProject = getStartProject(startEntry);
        FileImpl csmFile = startProject == null ? null : startProject.getFile(new File(startEntry.getStartFile().toString()), false);
        if (csmFile != null) {
            NativeFileItem nativeFile = csmFile.getNativeFileItem();
            if (nativeFile != null && nativeFile.getFile() != null) {
                preprocHandler = startProject.createPreprocHandler(nativeFile);
            }
        }
        return new StartEntryInfo(preprocHandler, startProject, csmFile);
    }

    private APTPreprocHandler restorePreprocHandler(File interestedFile, APTPreprocHandler preprocHandler, APTPreprocHandler.State state) {
        assert state != null;
        assert state.isCleaned();
        // walk through include stack to restore preproc information
        LinkedList<APTIncludeHandler.IncludeInfo> reverseInclStack = APTHandlersSupport.extractIncludeStack(state);
        assert (reverseInclStack != null);
        if (reverseInclStack.isEmpty()) {
            if (TRACE_PP_STATE_OUT) {
                System.err.println("stack is empty; return default for " + interestedFile);
            }
            return getStartEntryInfo(preprocHandler, state).preprocHandler;
        } else {
            if (TRACE_PP_STATE_OUT) {
                System.err.println("restoring for " + interestedFile);
            }
            // we need to reverse includes stack
            assert (!reverseInclStack.isEmpty()) : "state of stack is " + reverseInclStack;
            LinkedList<APTIncludeHandler.IncludeInfo> inclStack = reverse(reverseInclStack);
            StartEntryInfo sei = getStartEntryInfo(preprocHandler, state);
            FileImpl csmFile = sei.csmFile;
            ProjectBase startProject = sei.startProject;
            preprocHandler = sei.preprocHandler;

            APTFile aptLight = null;
            try {
                aptLight = csmFile == null ? null : getAPTLight(csmFile);
            } catch (IOException ex) {
                System.err.println("can't restore preprocessor state for " + interestedFile + //NOI18N
                        "\nreason: " + ex.getMessage());//NOI18N
                DiagnosticExceptoins.register(ex);
            }
            boolean ppStateRestored = false;
            if (aptLight != null) {
                // for testing remember restored file
                long time = REMEMBER_RESTORED ? System.currentTimeMillis() : 0;
                int stackSize = inclStack.size();
                // create concurrent entry if absent
                APTFileCacheEntry cacheEntry = csmFile.getAPTCacheEntry(preprocHandler, Boolean.FALSE);
                APTWalker walker = new APTRestorePreprocStateWalker(startProject, aptLight, csmFile, preprocHandler, inclStack, FileContainer.getFileKey(interestedFile, false).toString(), cacheEntry);
                walker.visit();
                // we do not remember cache entry because it is stopped before end of file
                // fileImpl.setAPTCacheEntry(handler, cacheEntry, false);

                if (preprocHandler.isValid()) {
                    if (REMEMBER_RESTORED) {
                        if (testRestoredFiles == null) {
                            testRestoredFiles = new ArrayList<String>();
                        }
                        FileImpl interestedFileImpl = getFile(interestedFile, false);
                        assert interestedFileImpl != null;
                        String msg = interestedFile.getAbsolutePath() + " [" + (interestedFileImpl.isHeaderFile() ? "H" : interestedFileImpl.isSourceFile() ? "S" : "U") + "]"; // NOI18N
                        time = System.currentTimeMillis() - time;
                        msg = msg + " within " + time + "ms" + " stack " + stackSize + " elems"; // NOI18N
                        System.err.println("#" + testRestoredFiles.size() + " restored: " + msg); // NOI18N
                        testRestoredFiles.add(msg);
                    }
                    if (TRACE_PP_STATE_OUT) {
                        System.err.println("after restoring " + preprocHandler); // NOI18N
                    }
                    ppStateRestored = true;
                }
            }
            if (!ppStateRestored) {
                // need to recover from the problem, when start file is invalid or absent
                // try to find project who can create default handler with correct
                // compiler settings
                // preferences is start project
                if (startProject == null) {
                    // otherwise use the project owner
                    startProject = this;
                }
                preprocHandler = startProject.createDefaultPreprocHandler(interestedFile);
            // remember
            // TODO: file container should accept all without checks
            // otherwise state will not be replaced
//                synchronized (getFileContainer().getLock(interestedFile)) {
//                    if (state.equals(getPreprocState(interestedFile))) {
//                        APTPreprocHandler.State recoveredState = preprocHandler.getState();
//                        assert !recoveredState.isCompileContext();
//                        putPreprocState(interestedFile, recoveredState);
//                    }
//                }
            }
            return preprocHandler;
        }
    }

    /*
    private APTPreprocHandler restorePreprocHandler(File interestedFile, APTPreprocHandler preprocHandler, APTPreprocHandler.State state) {
    assert state != null;
    assert state.isCleaned();
    // walk through include stack to restore preproc information
    List<APTIncludeHandler.IncludeInfo> reverseInclStack = APTHandlersSupport.extractIncludeStack(state);
    assert (reverseInclStack != null);
    if (reverseInclStack.isEmpty()) {
    if (TRACE_PP_STATE_OUT) System.err.println("stack is empty; return default for " + interestedFile);
    return getStartEntryInfo(preprocHandler, state).preprocHandler;
    } else {
    if (TRACE_PP_STATE_OUT) System.err.println("restoring for " + interestedFile);
    // we need to reverse includes stack
    assert (!reverseInclStack.isEmpty()) : "state of stack is " + reverseInclStack;
    StartEntryInfo sei = getStartEntryInfo(preprocHandler, state);
    FileImpl csmFile = sei.csmFile;
    ProjectBase startProject = sei.startProject;
    preprocHandler = sei.preprocHandler;

    ProjectBase prevProject = startProject;
    // for testing remember restored file
    long time = REMEMBER_RESTORED ? System.currentTimeMillis() : 0;
    boolean ppStateRestored = false;
    for (int i = 0; i < reverseInclStack.size() && !ppStateRestored; i++) {
    Stack<APTIncludeHandler.IncludeInfo> inclStack = reverse(reverseInclStack);
    if (i > 0) {
    System.err.println("need to try smaller stack");
    sei = getStartEntryInfo(preprocHandler, state);
    preprocHandler = sei.preprocHandler;
    for (int j = i; j > 0; j--) {
    inclStack.pop();
    }
    String includedPath = inclStack.peek().getIncludedPath();
    File startFile = new File(includedPath);
    csmFile = null;
    if (prevProject != null) {
    startProject = LibraryManager.getInstance().searchInProjectFiles(prevProject, startFile);
    csmFile = startProject == null ? null : startProject.getFile(startFile);
    }
    if (csmFile == null) {
    CsmFile foundCsmFile = CsmModelAccessor.getModel().findFile(includedPath);
    if (foundCsmFile instanceof FileImpl) {
    csmFile = (FileImpl)foundCsmFile;
    }
    }
    if (csmFile != null) {
    prevProject = csmFile.getProjectImpl();
    }
    }
    APTFile aptLight = null;
    try {
    aptLight = csmFile == null ? null : getAPTLight(csmFile);
    } catch (IOException ex) {
    System.err.println("can't restore preprocessor state for " + interestedFile + //NOI18N
    "\nreason: " + ex.getMessage());//NOI18N
    }
    if (aptLight != null) {
    int stackSize = inclStack.size();
    APTWalker walker = new APTRestorePreprocStateWalker(startProject, aptLight, csmFile, preprocHandler, inclStack, FileContainer.getFileKey(interestedFile, false));
    walker.visit();
    if (preprocHandler.isValid()) {
    if (REMEMBER_RESTORED) {
    if (testRestoredFiles == null) {
    testRestoredFiles = new ArrayList<String>();
    }
    FileImpl interestedFileImpl = getFile(interestedFile);
    assert interestedFileImpl != null;
    String msg = interestedFile.getAbsolutePath() + " [" + (interestedFileImpl.isHeaderFile() ? "H" : interestedFileImpl.isSourceFile() ? "S" : "U") + "]"; // NOI18N
    time = System.currentTimeMillis() - time;
    msg = msg + " within " + time + "ms" + " stack " + stackSize + " elems" + " orininal size was " + reverseInclStack.size(); // NOI18N
    System.err.println("#" + testRestoredFiles.size() + " restored: " + msg); // NOI18N
    testRestoredFiles.add(msg);
    }
    if (TRACE_PP_STATE_OUT) {
    System.err.println("after restoring " + preprocHandler); // NOI18N
    }
    ppStateRestored = true;
    }
    }
    }
    if (!ppStateRestored) {
    // need to recover from the problem, when start file is invalid or absent
    // try to find project who can create default handler with correct
    // compiler settings
    // preferences is start project
    if (startProject == null) {
    // otherwise use the project owner
    startProject = this;
    }
    preprocHandler = startProject.createDefaultPreprocHandler(interestedFile);
    // remember
    // TODO: file container should accept all without checks
    // otherwise state will not be replaced
    //                synchronized (getFileContainer().getLock(interestedFile)) {
    //                    if (state.equals(getPreprocState(interestedFile))) {
    //                        APTPreprocHandler.State recoveredState = preprocHandler.getState();
    //                        assert !recoveredState.isCompileContext();
    //                        putPreprocState(interestedFile, recoveredState);
    //                    }
    //                }
    }
    return preprocHandler;
    }
    }
     */
    private NativeProject findNativeProjectHolder(Set<ProjectBase> visited) {
        visited.add(this);
        NativeProject nativeProject = ModelSupport.getNativeProject(getPlatformProject());
        if (nativeProject == null) {
            // try to find dependent projects and ask them
            List<ProjectBase> deps = this.getDependentProjects();
            for (ProjectBase dependentPrj : deps) {
                if (!visited.contains(dependentPrj)) {
                    nativeProject = dependentPrj.findNativeProjectHolder(visited);
                    if (nativeProject != null) {
                        // found
                        break;
                    }
                }
            }
        }
        return nativeProject;
    }

    private APTPreprocHandler createDefaultPreprocHandler(File interestedFile) {
        NativeProject nativeProject = findNativeProjectHolder(new HashSet<ProjectBase>(10));
        APTPreprocHandler out = null;
        if (nativeProject != null) {
            // we have own native project to get settings from
            NativeFileItem item = new DefaultFileItem(nativeProject, interestedFile.getAbsolutePath());
            out = createPreprocHandler(item);
        } else {
            out = createEmptyPreprocHandler(interestedFile);
        }
        assert out != null : "failed creating default ppState for " + interestedFile;
        return out;
    }

    private static <T> LinkedList<T> reverse(LinkedList<T> original) {
        LinkedList<T> reverse = new LinkedList<T>();
        ListIterator<T> it = original.listIterator(original.size());
        while(it.hasPrevious()){
           reverse.addLast(it.previous());
        }
        return reverse;
    }

    public static NativeFileItem getCompiledFileItem(FileImpl fileImpl) {
        NativeFileItem out = null;
        ProjectBase filePrj = fileImpl.getProjectImpl(true);
        if (filePrj != null) {
            APTPreprocHandler.State state = filePrj.getPreprocState(fileImpl);
            FileImpl startFile = getStartFile(state);
            out = startFile != null ? startFile.getNativeFileItem() : null;
        }
        return out;
    }

    public static FileImpl getStartFile(final APTPreprocHandler.State state) {
        StartEntry startEntry = APTHandlersSupport.extractStartEntry(state);
        ProjectBase startProject = getStartProject(startEntry);
        FileImpl csmFile = startProject == null ? null : startProject.getFile(new File(startEntry.getStartFile().toString()), false);
        return csmFile;
    }

    public static ProjectBase getStartProject(final APTPreprocHandler.State state) {
        return getStartProject(APTHandlersSupport.extractStartEntry(state));
    }

    public static ProjectBase getStartProject(StartEntry startEntry) {
        if (startEntry == null) {
            return null;
        }
        Key key = startEntry.getStartFileProject();
        ProjectBase prj = (ProjectBase) RepositoryUtils.get(key);
        return prj;
    }

    public final APTFile getAPTLight(CsmFile csmFile) throws IOException {
        APTFile aptLight = null;
        aptLight = APTDriver.getInstance().findAPTLight(((FileImpl) csmFile).getBuffer());
        return aptLight;
    }

    public final GraphContainer getGraph() {
        return getGraphStorage();
    }

    protected final static class DefaultFileItem implements NativeFileItem {

        private NativeProject project;
        private String absolutePath;

        public DefaultFileItem(NativeProject project, String absolutePath) {
            this.project = project;
            this.absolutePath = absolutePath;
        }

        public DefaultFileItem(NativeFileItem nativeFile) {
            this.project = nativeFile.getNativeProject();
            this.absolutePath = nativeFile.getFile().getAbsolutePath();
        }

        public static NativeFileItem toDefault(NativeFileItem nativeFile) {
            // if not already fake
            if (!(nativeFile instanceof DefaultFileItem)) {
                nativeFile = new DefaultFileItem(nativeFile);
            }
            return nativeFile;
        }

        @Override
        public List<String> getUserMacroDefinitions() {
            if (project != null) {
                return project.getUserMacroDefinitions();
            }
            return Collections.<String>emptyList();
        }

        @Override
        public List<String> getUserIncludePaths() {
            if (project != null) {
                return project.getUserIncludePaths();
            }
            return Collections.<String>emptyList();
        }

        @Override
        public List<String> getSystemMacroDefinitions() {
            if (project != null) {
                return project.getSystemMacroDefinitions();
            }
            return Collections.<String>emptyList();
        }

        @Override
        public List<String> getSystemIncludePaths() {
            if (project != null) {
                return project.getSystemIncludePaths();
            }
            return Collections.<String>emptyList();
        }

        @Override
        public NativeProject getNativeProject() {
            return project;
        }

        @Override
        public File getFile() {
            return new File(absolutePath);
        }

        @Override
        public Language getLanguage() {
            return NativeFileItem.Language.C_HEADER;
        }

        @Override
        public LanguageFlavor getLanguageFlavor() {
            return NativeFileItem.LanguageFlavor.GENERIC;
        }

        @Override
        public boolean isExcluded() {
            return false;
        }

        @Override
        public String toString() {
            return absolutePath;
        }
    }

    /**
     * Represent the project status.
     *
     * Concerns only initial stage of project lifecycle:
     * allows to distingwish just newly-created project,
     * the phase when files are being added to project (and to parser queue)
     * and the phase when all files are already added.
     *
     * It isn't worth tracking further stages (stable/unstable)
     * since it's error prone (it's better to ask, say, parser queue
     * whether it contains files that belong to this projec or not)
     */
    protected static enum Status {

        Initial,
        Restored,
        AddingFiles,
        Validating,
        Ready;
    }
    private volatile Status status;
    /** The task that is run in a request processor during project initialization */
    private Cancellable initializationTask;
    /** The lock under which the initializationTask is set */
    private static final class InitializationTaskLock {}
    private final Object initializationTaskLock = new InitializationTaskLock();
    private static final class WaitParseLock {}
    private final Object waitParseLock = new WaitParseLock();
    // to profile monitor usages
    private static final class ClassifierReplaceLock {}
    private final Object classifierReplaceLock = new ClassifierReplaceLock();
    private ModelImpl model;
    private Unresolved unresolved;
    private CharSequence name;
    private CsmUID<CsmNamespace> globalNamespaceUID;
    private NamespaceImpl FAKE_GLOBAL_NAMESPACE;
    private Object platformProject;
    /**
     * Some notes concerning disposing and disposeLock fields.
     *
     * The purpose is not to perform some actions
     * (such as adding new files, continuing initialization, etc)
     * when the project is going to be disposed.
     *
     * The disposing field is changed only once,
     * from false to true (in setDispose() method)
     *
     * When it is changed to true, no lock is aquired, BUT:
     * it is guaranteed that events take place in the following order:
     * 1) disposing is set to true
     * 2) the disposeLock.writeLock() is locked after that
     * and remains locked during the entire project closure.
     *
     * Clients who need to check this, are obliged to
     * act in the following sequence:
     * 1) require disposeLock.readLock()
     * 2) check that the disposing field is still false
     * 3) keep disposeLock.readLock() locked
     * while performing critical actions
     * (the actions that should not be done
     * when the project is being disposed)
     *
     */
    private final AtomicBoolean disposing = new AtomicBoolean(false);
    private final ReadWriteLock disposeLock = new ReentrantReadWriteLock();
    private CharSequence uniqueName = null; // lazy initialized
    private final Map<CharSequence, CsmUID<CsmNamespace>> namespaces;
    //private ClassifierContainer classifierContainer = new ClassifierContainer();
    private Key classifierStorageKey;

    // collection of sharable system macros and system includes
    private final APTSystemStorage sysAPTData = APTSystemStorage.getDefault();
    private final APTIncludePathStorage userPathStorage = new APTIncludePathStorage();
    private static final class NamespaceLock {}
    private final Object namespaceLock = new NamespaceLock();
    private Key declarationsSorageKey;
    private Key fileContainerKey;
    private static final class FileContainerLock {}
    private final Object fileContainerLock = new FileContainerLock();
    private Key graphStorageKey;
    protected final SourceRootContainer projectRoots = new SourceRootContainer();
    private NativeProjectListenerImpl projectListener;

    //private NamespaceImpl fakeNamespace;

    // test variables.
    private static final boolean TRACE_PP_STATE_OUT = DebugUtils.getBoolean("cnd.dump.preproc.state", false); // NOI18N
    private static final boolean REMEMBER_RESTORED = TraceFlags.CLEAN_MACROS_AFTER_PARSE && (DebugUtils.getBoolean("cnd.remember.restored", false) || TRACE_PP_STATE_OUT);// NOI18N
    public static final int GATHERING_MACROS = 0;
    public static final int GATHERING_TOKENS = 1;

    ////////////////////////////////////////////////////////////////////////////
    /**
     * for tests only
     */
    public static List<String> testGetRestoredFiles() {
        return testRestoredFiles;
    }
    private static volatile List<String> testRestoredFiles = null;
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // impl of persistent
    @Override
    public void write(DataOutput aStream) throws IOException {
        assert aStream != null;
        UIDObjectFactory aFactory = UIDObjectFactory.getDefaultFactory();
        assert aFactory != null;
        assert this.name != null;
        PersistentUtils.writeUTF(name, aStream);
        //PersistentUtils.writeUTF(RepositoryUtils.getUnitName(getUID()), aStream);
        aFactory.writeUID(this.globalNamespaceUID, aStream);
        aFactory.writeStringToUIDMap(this.namespaces, aStream, false);

        ProjectComponent.writeKey(fileContainerKey, aStream);
        ProjectComponent.writeKey(declarationsSorageKey, aStream);
        ProjectComponent.writeKey(graphStorageKey, aStream);
        ProjectComponent.writeKey(classifierStorageKey, aStream);

        PersistentUtils.writeUTF(this.uniqueName, aStream);
    }

    protected ProjectBase(DataInput aStream) throws IOException {

        setStatus(Status.Restored);

        assert aStream != null;
        UIDObjectFactory aFactory = UIDObjectFactory.getDefaultFactory();
        assert aFactory != null : "default UID factory can not be bull";

        this.name = PersistentUtils.readUTF(aStream, ProjectNameCache.getManager());
        assert this.name != null : "project name can not be null";

        //CharSequence unitName = PersistentUtils.readUTF(aStream, DefaultCache.getManager());

        this.globalNamespaceUID = aFactory.readUID(aStream);
        assert globalNamespaceUID != null : "globalNamespaceUID can not be null";

        int collSize = aStream.readInt();
        if (collSize <= 0) {
            namespaces = new ConcurrentHashMap<CharSequence, CsmUID<CsmNamespace>>(0);
        } else {
            namespaces = new ConcurrentHashMap<CharSequence, CsmUID<CsmNamespace>>(collSize);
        }
        aFactory.readStringToUIDMap(this.namespaces, aStream, QualifiedNameCache.getManager(), collSize);

        fileContainerKey = ProjectComponent.readKey(aStream);
        assert fileContainerKey != null : "fileContainerKey can not be null";

        declarationsSorageKey = ProjectComponent.readKey(aStream);
        assert declarationsSorageKey != null : "declarationsSorageKey can not be null";

        graphStorageKey = ProjectComponent.readKey(aStream);
        assert graphStorageKey != null : "graphStorageKey can not be null";

        classifierStorageKey = ProjectComponent.readKey(aStream);
        assert classifierStorageKey != null : "classifierStorageKey can not be null";

        this.uniqueName = PersistentUtils.readUTF(aStream, DefaultCache.getManager());
        assert uniqueName != null : "uniqueName can not be null";
        this.uniqueName = ProjectNameCache.getManager().getString(this.uniqueName);

        this.model = (ModelImpl) CsmModelAccessor.getModel();

        this.FAKE_GLOBAL_NAMESPACE = new NamespaceImpl(this, true);
    }

    private WeakReference<DeclarationContainer> weakDeclarationContainer = TraceFlags.USE_WEAK_MEMORY_CACHE ? new WeakReference<DeclarationContainer>(null) : null;
    private int preventMultiplyDiagnosticExceptionsDeclarationsSorage = 0;
    DeclarationContainer getDeclarationsSorage() {
        DeclarationContainer dc = null;
        WeakReference<DeclarationContainer> weak = null;
        if (TraceFlags.USE_WEAK_MEMORY_CACHE && isValid()) {
            weak = weakDeclarationContainer;
            if (weak != null) {
                dc = weak.get();
                if (dc != null) {
                    return dc;
                }
            }
        }
        dc = (DeclarationContainer) RepositoryUtils.get(declarationsSorageKey);
        if (dc == null && isValid() && preventMultiplyDiagnosticExceptionsDeclarationsSorage < DiagnosticExceptoins.LimitMultiplyDiagnosticExceptions) {
            DiagnosticExceptoins.register(new IllegalStateException("Failed to get DeclarationsSorage by key " + declarationsSorageKey)); // NOI18N
            preventMultiplyDiagnosticExceptionsDeclarationsSorage++;
        }
        if (TraceFlags.USE_WEAK_MEMORY_CACHE && dc != null && weakDeclarationContainer != null) {
            weakDeclarationContainer = new WeakReference<DeclarationContainer>(dc);
        }
        return dc != null ? dc : DeclarationContainer.empty();
    }

    private WeakReference<FileContainer> weakFileContainer = TraceFlags.USE_WEAK_MEMORY_CACHE ? new WeakReference<FileContainer>(null) : null;
    private int preventMultiplyDiagnosticExceptionsFileContainer = 0;
    FileContainer getFileContainer() {
        FileContainer fc = null;
        WeakReference<FileContainer> weak = null;
        if (TraceFlags.USE_WEAK_MEMORY_CACHE && isValid()) {
            weak = weakFileContainer;
            if (weak != null) {
                fc = weak.get();
                if (fc != null) {
                    return fc;
                }
            }
        }
        fc = (FileContainer) RepositoryUtils.get(fileContainerKey);
        if (fc == null && isValid() && preventMultiplyDiagnosticExceptionsFileContainer < DiagnosticExceptoins.LimitMultiplyDiagnosticExceptions) {
            DiagnosticExceptoins.register(new IllegalStateException("Failed to get FileContainer by key " + fileContainerKey)); // NOI18N
            preventMultiplyDiagnosticExceptionsFileContainer++;
        }
        if (TraceFlags.USE_WEAK_MEMORY_CACHE && fc != null && weakFileContainer != null) {
            weakFileContainer = new WeakReference<FileContainer>(fc);
        }
        return fc != null ? fc : FileContainer.empty();
    }

    private WeakReference<GraphContainer> weakGraphContainer = TraceFlags.USE_WEAK_MEMORY_CACHE ? new WeakReference<GraphContainer>(null) : null;
    private int preventMultiplyDiagnosticExceptionsGraphStorage = 0;
    public final GraphContainer getGraphStorage() {
        GraphContainer gc = null;
        WeakReference<GraphContainer> weak = null;
        if (TraceFlags.USE_WEAK_MEMORY_CACHE && isValid()) {
            weak = weakGraphContainer;
            if (weak != null) {
                gc = weak.get();
                if (gc != null) {
                    return gc;
                }
            }
        }
        gc = (GraphContainer) RepositoryUtils.get(graphStorageKey);
        if (gc == null && isValid() && preventMultiplyDiagnosticExceptionsGraphStorage < DiagnosticExceptoins.LimitMultiplyDiagnosticExceptions) {
            DiagnosticExceptoins.register(new IllegalStateException("Failed to get GraphContainer by key " + graphStorageKey)); // NOI18N
            preventMultiplyDiagnosticExceptionsGraphStorage++;
        }
        if (TraceFlags.USE_WEAK_MEMORY_CACHE && gc != null && weakGraphContainer != null) {
            weakGraphContainer = new WeakReference<GraphContainer>(gc);
        }
        return gc != null ? gc : GraphContainer.empty();
    }

    private WeakReference<ClassifierContainer> weakClassifierContainer = TraceFlags.USE_WEAK_MEMORY_CACHE ? new WeakReference<ClassifierContainer>(null) : null;
    private int preventMultiplyDiagnosticExceptionsClassifierSorage = 0;
    final ClassifierContainer getClassifierSorage() {
        ClassifierContainer cc = null;
        WeakReference<ClassifierContainer> weak = null;
        if (TraceFlags.USE_WEAK_MEMORY_CACHE && isValid()) {
            weak = weakClassifierContainer;
            if (weak != null) {
                cc = weak.get();
                if (cc != null) {
                    return cc;
                }
            }
        }
        cc = (ClassifierContainer) RepositoryUtils.get(classifierStorageKey);
        if (cc == null && isValid() && preventMultiplyDiagnosticExceptionsClassifierSorage < DiagnosticExceptoins.LimitMultiplyDiagnosticExceptions) {
            DiagnosticExceptoins.register(new IllegalStateException("Failed to get ClassifierSorage by key " + classifierStorageKey)); // NOI18N
            preventMultiplyDiagnosticExceptionsClassifierSorage++;
        }
        if (TraceFlags.USE_WEAK_MEMORY_CACHE && cc != null && weakClassifierContainer != null) {
            weakClassifierContainer = new WeakReference<ClassifierContainer>(cc);
        }
        return cc != null ? cc : ClassifierContainer.empty();
    }

    private static class FixRegistrationRunnable implements Runnable {
        private final CountDownLatch countDownLatch;
        private final List<CsmUID<CsmFile>> files;
        private final boolean libsAlreadyParsed;
        private final AtomicBoolean cancelled;
        private FixRegistrationRunnable(CountDownLatch countDownLatch, List<CsmUID<CsmFile>> files, boolean libsAlreadyParsed, AtomicBoolean cancelled){
            this.countDownLatch = countDownLatch;
            this.files = files;
            this.libsAlreadyParsed = libsAlreadyParsed;
            this.cancelled = cancelled;
        }
        @Override
        public void run() {
            try {
                for(CsmUID<CsmFile> file : files) {
                    if (cancelled.get()) {
                        return;
                    }
                    if (file == null){
                        return;
                    }
                    FileImpl impl = (FileImpl) UIDCsmConverter.UIDtoFile(file);
                    CndUtils.assertTrueInConsole(impl != null, "no deref file for " + file); // NOI18N
                    // situation is possible for standalone files which were already replaced
                    // by real files
                    if (impl == null) {
                        return;
                    }
                    Thread.currentThread().setName("Fix registration "+file); // NOI18N
                    impl.onProjectParseFinished(libsAlreadyParsed);
                }
            } finally {
                countDownLatch.countDown();
            }
        }
    }

    private class CreateFileRunnable implements Runnable {
        private final CountDownLatch countDownLatch;
        private List<NativeFileItem> nativeFileItems;
        private boolean sources;
        private Set<NativeFileItem> removedFiles;
        private ProjectSettingsValidator validator;
        private List<FileImpl> reparseOnEdit;
        private List<NativeFileItem> reparseOnPropertyChanged;
        private AtomicBoolean enougth;

        private CreateFileRunnable(CountDownLatch countDownLatch, List<NativeFileItem> nativeFileItems, boolean sources,
            Set<NativeFileItem> removedFiles, ProjectSettingsValidator validator,
            List<FileImpl> reparseOnEdit, List<NativeFileItem> reparseOnPropertyChanged, AtomicBoolean enougth){
            this.countDownLatch = countDownLatch;
            this.nativeFileItems = nativeFileItems;
            this.sources = sources;
            this.removedFiles = removedFiles;
            this.validator = validator;
            this.reparseOnEdit = reparseOnEdit;
            this.reparseOnPropertyChanged = reparseOnPropertyChanged;
            this.enougth = enougth;
        }

        @Override
        public void run() {
            try {
                for(NativeFileItem nativeFileItem : nativeFileItems) {
                    if (!createProjectFilesIfNeedRun(nativeFileItem, sources, removedFiles, validator,
                                            reparseOnEdit, reparseOnPropertyChanged, enougth)){
                        return;
                    }
                }
            } finally {
                countDownLatch.countDown();
            }
        }
    }
}
