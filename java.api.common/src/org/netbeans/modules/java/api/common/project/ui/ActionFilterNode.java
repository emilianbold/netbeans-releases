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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.java.api.common.project.ui;


import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Action;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.libraries.Library;

import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.libraries.LibrariesCustomizer;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;

import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.impl.ClassPathPackageAccessor;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.spi.java.project.support.ui.EditJarSupport;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.actions.EditAction;
import org.openide.actions.FindAction;
import org.openide.actions.OpenAction;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 * This class decorates package nodes and file nodes under the Libraries Nodes.
 * It removes all actions from these nodes except of file node's {@link OpenAction}
 * and package node's {@link FindAction} It also adds the {@link ShowJavadocAction}
 * to both file and package nodes. It also adds {@link RemoveClassPathRootAction} to
 * class path roots.
 */
final class ActionFilterNode extends FilterNode {

    private static enum Mode {
        ROOT {
            @Override
            public boolean isFolder() {
                return true;
            }
            @Override
            public boolean isRoot() {
                return true;
            }
        },
        EDITABLE_ROOT {
            @Override
            public boolean isFolder() {
                return true;
            }
            @Override
            public boolean isRoot() {
                return true;
            }
        },
        PACKAGE {
            @Override
            public boolean isFolder() {
                return true;
            }
            @Override
            public boolean isRoot() {
                return false;
            }
        },
        FILE {
            @Override
            public boolean isFolder() {
                return false;
            }
            @Override
            public boolean isRoot() {
                return false;
            }
        },
        FILE_CONTENT {
            @Override
            public boolean isFolder() {
                return false;
            }
            @Override
            public boolean isRoot() {
                return false;
            }
        };

        public abstract boolean isFolder();

        public abstract boolean isRoot();
    }

    private static final RequestProcessor RP = new RequestProcessor(ActionFilterNode.class);

    private final Mode mode;
    private Action[] actionCache;

    /**
     * Creates new ActionFilterNode for class path root
     * @param original the original node
     * @param helper used for implementing {@link RemoveClassPathRootAction.Removable}
     * @param classPathId ant property name of classpath to which these classpath root belongs 
     * @param entryId ant property name of this classpath root
     * @return ActionFilterNode
     */
    static FilterNode forRoot (
            final @NonNull Node original,
            final @NonNull UpdateHelper helper,
            final @NonNull String classPathId,
            final @NonNull String entryId,
            final @NullAllowed String webModuleElementName,     //xxx: remove
            final @NonNull ClassPathSupport cs,
            final @NonNull ReferenceHelper rh) {
        Parameters.notNull("original", original);   //NOI18N
        Parameters.notNull("helper", helper);       //NOI18N
        Parameters.notNull("classPathId", classPathId); //NOI18N
        Parameters.notNull("entryId", entryId);     //NOI18N
        Parameters.notNull("cs", cs);       //NOI18N
        Parameters.notNull("rh", rh);       //NOI18N

        final FileObject root =  getFolder(original);
        return new ActionFilterNode (original, Mode.ROOT, root, createLookup(original,
                new Removable (helper, classPathId, entryId, webModuleElementName, cs, rh),
                new JavadocProvider(root,root)));
    }

    static FilterNode forLibrary(
            final @NonNull Node original,
            final @NonNull UpdateHelper helper,
            final @NonNull String classPathId,
            final @NonNull String entryId,
            final @NullAllowed String webModuleElementName,     //xxx: remove
            final @NonNull ClassPathSupport cs,
            final @NonNull ReferenceHelper rh) {
        Parameters.notNull("original", original);   //NOI18N
        Parameters.notNull("helper", helper);       //NOI18N
        Parameters.notNull("classPathId", classPathId); //NOI18N
        Parameters.notNull("entryId", entryId);     //NOI18N
        Parameters.notNull("cs", cs);       //NOI18N
        Parameters.notNull("rh", rh);       //NOI18N

        final FileObject root =  getFolder(original);
        return new ActionFilterNode (original, Mode.EDITABLE_ROOT, root, createLookup(original,
                new Removable (helper, classPathId, entryId, webModuleElementName, cs, rh),
                new LibraryEditable(entryId, rh),
                new JavadocProvider(root,root)));
    }

    static FilterNode forArchive(
            final @NonNull Node original,
            final @NonNull UpdateHelper helper,
            final @NonNull PropertyEvaluator eval,
            final @NonNull String classPathId,
            final @NonNull String entryId,
            final @NullAllowed String webModuleElementName,     //xxx: remove
            final @NonNull ClassPathSupport cs,
            final @NonNull ReferenceHelper rh) {
        Parameters.notNull("original", original);   //NOI18N
        Parameters.notNull("helper", helper);       //NOI18N
        Parameters.notNull("eval", eval);           //NOI18N
        Parameters.notNull("classPathId", classPathId); //NOI18N
        Parameters.notNull("entryId", entryId);     //NOI18N
        Parameters.notNull("cs", cs);       //NOI18N
        Parameters.notNull("rh", rh);       //NOI18N

        final FileObject root =  getFolder(original);
        return new ActionFilterNode (original, Mode.EDITABLE_ROOT, root, createLookup(original,
                new Removable (helper, classPathId, entryId, webModuleElementName, cs, rh),
                new ArchiveEditable(entryId, helper, eval, rh),
                new JavadocProvider(root,root)));
    }

    static FilterNode forPackage(final @NonNull Node original) {
        Parameters.notNull("original", original);   //NOI18N

        final FileObject root = getFolder(original);
        return new ActionFilterNode (original, Mode.PACKAGE, root, createLookup(original,
                new JavadocProvider(root,root)));
    }

    private static FileObject getFolder(final Node original) {
        final DataObject dobj = original.getLookup().lookup(DataObject.class);
        assert dobj != null;
        return dobj.getPrimaryFile();
    }

    private static Lookup createLookup(final Node original, Object... toAdd) {
        final Lookup lkp = new ProxyLookup(
                original.getLookup(),
                Lookups.fixed (toAdd));
        return lkp;
    }



    private ActionFilterNode (Node original, Mode mode, FileObject cpRoot, FileObject resource) {
        this (original, mode, cpRoot,
            new ProxyLookup(new Lookup[] {original.getLookup(),Lookups.singleton(new JavadocProvider(cpRoot,resource))}));
    }

    private ActionFilterNode (Node original, Mode mode) {
        super (original, original.isLeaf() ? Children.LEAF : new ActionFilterChildren (original, mode, null));
        this.mode = mode;
    }

    private ActionFilterNode (Node original, Mode mode, FileObject root, Lookup lkp) {
        super (original, original.isLeaf() ? Children.LEAF : new ActionFilterChildren (original, mode,root),lkp);
        this.mode = mode;
    }

    @Override
    public Action[] getActions(boolean context) {
        Action[] result = initActions();        
        return result;
    }

    @Override
    public String getShortDescription() {
        final DataObject dobj = getLookup().lookup(DataObject.class);
        FileObject pf;
        if (dobj != null && (pf = dobj.getPrimaryFile()) != null) {
            return FileUtil.getFileDisplayName(pf);

        } else {
            return super.getShortDescription();
        }
    }

    @Override
    public Action getPreferredAction() {
        if (mode == Mode.FILE) {
            Action[] actions = initActions();
            if (actions.length > 0 && isOpenAction(actions[0])) {
                return actions[0];
            }
        }
        return null;
    }

    private Action[] initActions () {
        if (actionCache == null) {
            List<Action> result = new ArrayList<Action>(2);
            if (mode == Mode.FILE) {
                for (Action superAction : super.getActions(false)) {
                    if (isOpenAction(superAction)) {
                        result.add(superAction);
                    }
                }
                result.add (SystemAction.get(ShowJavadocAction.class));
            }
            else if (mode.isFolder()) {
                result.add (SystemAction.get(ShowJavadocAction.class));
                Action[] superActions = super.getActions(false);
                for (int i=0; i<superActions.length; i++) {
                    if (superActions[i] instanceof FindAction) {
                        result.add (superActions[i]);
                    }
                }                
                if (mode.isRoot()) {
                    result.add (SystemAction.get(RemoveClassPathRootAction.class));
                }
                if (mode == Mode.EDITABLE_ROOT) {
                    result.add (SystemAction.get(EditRootAction.class));
                }
            }            
            actionCache = result.toArray(new Action[result.size()]);
        }
        return actionCache;
    }

    private static boolean isOpenAction(final Action action) {
        if (action == null) {
            return false;
        }
        if (action instanceof OpenAction || action instanceof EditAction) {
            return true;
        }
        if ("org.netbeans.api.actions.Openable".equals(action.getValue("type"))) { //NOI18N
            return true;
        }
        return false;
    }

    private static class ActionFilterChildren extends FilterNode.Children {

        private final Mode mode;
        private final FileObject cpRoot;

        ActionFilterChildren (@NonNull Node original, @NonNull Mode mode, @NonNull FileObject cpRooot) {
            super (original);
            this.mode = mode;
            this.cpRoot = cpRooot;
        }

        @Override
        protected Node[] createNodes(Node n) {
            if (mode.isFolder()) {
                final FileObject fobj = n.getLookup().lookup(FileObject.class);
                if (fobj == null) {
                    if (n.isLeaf() && n.getActions(false).length == 0) {
                        //"Please Wait..." node
                        return super.createNodes(n);
                    } else {
                        assert false : String.format(
                            "DataNode without FileObject in Lookup %s : %s",   //NOI18N
                            n,
                            n.getClass());
                        return new Node[0];
                    }
                }
                else if (fobj.isFolder()) {
                    return new Node[] {new ActionFilterNode (n, Mode.PACKAGE, cpRoot, fobj)};
                }
                else {
                    return new Node[] {new ActionFilterNode (n, Mode.FILE, cpRoot, fobj)};
                }
            } else {
                return new Node[] {new ActionFilterNode (n, Mode.FILE_CONTENT)};
            }
        }
    }

    private static class JavadocProvider implements ShowJavadocAction.JavadocProvider {

        private static final AtomicBoolean initialized = new AtomicBoolean();

        private final FileObject cpRoot;
        private final FileObject resource;

        JavadocProvider (final @NonNull FileObject cpRoot, final @NullAllowed FileObject resource) {
            this.cpRoot = cpRoot;
            this.resource = resource;
            if (!initialized.getAndSet(true)) {
                RP.execute(new Runnable() {
                    @Override
                    public void run() {
                        JavadocForBinaryQuery.findJavadoc(cpRoot.toURL());
                    }
                });
            }
        }

        @Override
        public boolean hasJavadoc() {
            return resource != null && JavadocForBinaryQuery.findJavadoc(cpRoot.toURL()).getRoots().length>0;
        }

        @Override
        public void showJavadoc() {
                String relativeName = FileUtil.getRelativePath(cpRoot,resource);
                URL[] urls = JavadocForBinaryQuery.findJavadoc(cpRoot.toURL()).getRoots();
                URL pageURL;
                if (relativeName.length()==0) {
                    pageURL = ShowJavadocAction.findJavadoc ("overview-summary.html",urls); //NOI18N
                    if (pageURL == null) {
                        pageURL = ShowJavadocAction.findJavadoc ("index.html",urls); //NOI18N
                    }                    
                }
                else if (resource.isFolder()) {
                    //XXX Are the names the same also in the localized javadoc?                    
                    pageURL = ShowJavadocAction.findJavadoc (relativeName+"/package-summary.html",urls); //NOI18N
                }
                else {
                    String javadocFileName = relativeName.substring(0,relativeName.lastIndexOf('.'))+".html"; //NOI18Ns
                    pageURL = ShowJavadocAction.findJavadoc (javadocFileName,urls);
                }
                ShowJavadocAction.showJavaDoc(pageURL,relativeName.replace('/','.'));  //NOI18N
        }
    }

    static class Removable implements RemoveClassPathRootAction.Removable {

       private final UpdateHelper helper;
       private final String classPathId;
       private final String entryId;
       private final String webModuleElementName;
       private final ClassPathSupport cs;
       private ReferenceHelper rh;

       Removable (UpdateHelper helper, String classPathId, String entryId,
               String webModuleElementName, ClassPathSupport cs, ReferenceHelper rh) {
           this.helper = helper;
           this.classPathId = classPathId;
           this.entryId = entryId;
           this.webModuleElementName = webModuleElementName;
           this.cs = cs;
           this.rh = rh;
       }


        @Override
       public boolean canRemove () {
            //Allow to remove only entries from PROJECT_PROPERTIES, same behaviour as the project customizer
            EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            return props.getProperty (classPathId) != null;
        }

        @Override
       public Project remove() {
           // The caller has write access to ProjectManager
           // and ensures the project will be saved.
            boolean removed = false;
            EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
            String raw = props.getProperty (classPathId);
            List<ClassPathSupport.Item> resources = cs.itemsList( raw, webModuleElementName );
            for (Iterator i = resources.iterator(); i.hasNext();) {
                ClassPathSupport.Item item = (ClassPathSupport.Item)i.next();
                if (entryId.equals(CommonProjectUtils.getAntPropertyName(item.getReference()))) {
                    i.remove();
                    ClassPathPackageAccessor.getInstance().removeUnusedReference(item, classPathId, helper, rh);
                    removed = true;
                }
            }
            if (removed) {
                String[] itemRefs = cs.encodeToStrings(resources, webModuleElementName);
                props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);    //Reread the properties, PathParser changes them
                props.setProperty (classPathId, itemRefs);
                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
               return FileOwnerQuery.getOwner(helper.getAntProjectHelper().getProjectDirectory());
           } else {
               return null;
           }
       }

    }

    private static class LibraryEditable implements EditRootAction.Editable {

        private final ReferenceHelper refHelper;
        private final String entryId;

        private LibraryEditable(
               @NonNull final String entryId,
               @NonNull final ReferenceHelper refHelper) {
           Parameters.notNull("entryId", entryId);  //NOI18N
           Parameters.notNull("refHelper", refHelper);  //NOI18N
           if (!entryId.startsWith("libs.") || entryId.lastIndexOf('.')<=4) {   //NOI18N
               throw new IllegalArgumentException(entryId);
           }
           this.entryId = entryId;
           this.refHelper = refHelper;
        }

        @Override
        public boolean canEdit() {
            return getLibrary() != null;
        }

        @Override
        public void edit() {
            final Library lib = getLibrary();
            assert lib != null;
            LibrariesCustomizer.showSingleLibraryCustomizer(lib);
        }

        private Library getLibrary() {
            //Todo: Caching if needed
            final String libName = entryId.substring(5, entryId.lastIndexOf('.'));
            return refHelper.findLibrary(libName);
        }
    }

    private static class ArchiveEditable implements EditRootAction.Editable {

        private static final String FILE_REF = "file.reference.";   //NOI18N
        private static final String SRC_REF = "source.reference.";   //NOI18N
        private static final String JDOC_REF = "javadoc.reference.";  //NOI18N

        private final UpdateHelper updateHelper;
        private final PropertyEvaluator eval;
        private final ReferenceHelper refHelper;
        private final String entryId;

        private ArchiveEditable(
                final @NonNull String entryId,
                final @NonNull UpdateHelper updateHelper,
                final @NonNull PropertyEvaluator eval,
                final @NonNull ReferenceHelper refHelper) {
            Parameters.notNull("entryId", entryId); //NOI18N
            Parameters.notNull("updateHelper", updateHelper);   //NOI18N
            Parameters.notNull("eval", eval);   //NOI18N
            Parameters.notNull("refHelper", refHelper);   //NOI18N
            if (!entryId.startsWith(FILE_REF)) {
                throw new IllegalArgumentException(entryId);
            }
            this.entryId = entryId;
            this.updateHelper = updateHelper;
            this.eval = eval;
            this.refHelper = refHelper;
        }

        @Override
        public boolean canEdit() {
            final String propValue = eval.getProperty(entryId);
            return propValue != null;
        }

        @Override
        public void edit() {
            final String[] propValue = new String[1];
            final String[] oldSource = new String[1];
            final String[] oldJavadoc = new String[1];
            ProjectManager.mutex().readAccess(new Runnable(){
                @Override
                public void run () {
                    propValue[0] = eval.getProperty(entryId);
                    assert propValue[0] != null;
                    oldSource[0] = getSource();
                    oldJavadoc[0] = getJavadoc();
                }
            });
            final EditJarSupport.Item oldItem = new EditJarSupport.Item();
            oldItem.setJarFile(propValue[0]);
            oldItem.setSourceFile(oldSource[0]);
            oldItem.setJavadocFile(oldJavadoc[0]);
            final EditJarSupport.Item newItem = EditJarSupport.showEditDialog(updateHelper.getAntProjectHelper(), oldItem);
            if (newItem != null) {
                RP.execute(new Runnable() {
                    @Override
                    public void run() {
                        ProjectManager.mutex().writeAccess(new Runnable() {
                            @Override
                            public void run() {
                                store(getSourceProperty(), oldSource[0], newItem.getSourceFile());
                                store(getJavadocProperty(), oldJavadoc[0], newItem.getJavadocFile());
                            }
                        });
                    }
                });
            }
        }

        private String getSource() {
            return eval.getProperty(getSourceProperty());
        }

        private String getJavadoc() {
            return eval.getProperty(getJavadocProperty());
        }

        private String getSourceProperty() {
            return SRC_REF + entryId.substring(FILE_REF.length());
        }

        private String getJavadocProperty() {
            return JDOC_REF + entryId.substring(FILE_REF.length());
        }

        private void store (
                final @NonNull String property,
                final @NullAllowed String oldValue,
                final @NullAllowed String newValue) {
            Parameters.notNull("property", property);       //NOI18N
            if (oldValue == null ? newValue != null : !oldValue.equals(newValue)) {                
                if (newValue != null) {
                    refHelper.createExtraForeignFileReferenceAsIs(newValue, property);
                } else {
                    final EditableProperties ep = updateHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    ep.remove(property);
                    updateHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                }
                try {
                    final Project prj = FileOwnerQuery.getOwner(updateHelper.getAntProjectHelper().getProjectDirectory());
                    ProjectManager.getDefault().saveProject(prj);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
}
