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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.visualweb.complib;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.visualweb.api.complib.ComplibEvent;
import org.netbeans.modules.visualweb.api.complib.ComplibException;
import org.netbeans.modules.visualweb.api.complib.ComplibListener;
import org.netbeans.modules.visualweb.api.complib.ComplibService;
import org.netbeans.modules.visualweb.complib.Complib.Identifier;
import org.netbeans.modules.visualweb.complib.Complib.InitialPaletteFolder;
import org.netbeans.modules.visualweb.complib.Complib.InitialPaletteItem;
import org.netbeans.modules.visualweb.complib.ComplibManifest.EeSpecVersion;
import org.netbeans.modules.visualweb.complib.PaletteUtil.Category;
import org.netbeans.modules.visualweb.complib.PaletteUtil.Item;
import org.netbeans.modules.visualweb.complib.PaletteUtil.Palette;
import org.netbeans.modules.visualweb.complib.ui.ComplibsRootNode;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.netbeans.spi.palette.PaletteActions;
import org.netbeans.spi.palette.PaletteFilter;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

import com.sun.rave.designtime.BeanCreateInfo;
import com.sun.rave.designtime.BeanCreateInfoSet;
import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.DisplayItem;

/**
 * Singleton used to manage component libraries. The old name of this class used to be
 * ComponentLibraryManger.
 * 
 * @author Edwin Goei
 */
public class ComplibServiceProvider implements ComplibService {

    public static final String ADDABLE_COMPLIBS = "addableComplibs";

    /**
     * PaletteFilter used to implement complib versioning. If a project contains a complib with a
     * particular namespace URI and a version, this feature prevents components from any new complib
     * in the userdir from appearing on the palette if the complib has the same namespace URI but
     * different version number. Thus preventing the user from adding it to the project. In the
     * future, it may be possible to upgrade a complib.
     */
    private static class ComplibPaletteFilter extends PaletteFilter {
        private final Project project;

        public ComplibPaletteFilter(Project project) {
            this.project = project;
        }

        public boolean isValidCategory(Lookup lookup) {
            if (project == null) {
                return true;
            }
            Node itemNode = (Node) lookup.lookup(Node.class);
            if (itemNode == null) {
                return true;
            }
            DataObject dataObj = (DataObject) itemNode.getLookup().lookup(DataObject.class);
            if (!(dataObj instanceof DataFolder)) {
                return true;
            }
            DataFolder dataFolder = (DataFolder) dataObj;

            // Categories created by users should always be visible
            if (!PaletteUtil.isCreatedByComplib(dataFolder.getPrimaryFile())) {
                return true;
            }

            // If any child items are visible then show the category
            boolean show = false;
            for (DataObject child : dataFolder.getChildren()) {
                if (isVisible(child)) {
                    show = true;
                    break;
                }
            }
            return show;
        }

        public boolean isValidItem(Lookup lookup) {
            if (project == null) {
                return true;
            }
            Node itemNode = (Node) lookup.lookup(Node.class);
            if (itemNode == null) {
                return true;
            }
            DataObject dataObj = (DataObject) itemNode.getLookup().lookup(DataObject.class);
            if (dataObj == null) {
                return true;
            }

            return isVisible(dataObj);
        }

        private boolean isVisible(DataObject dataObj) {
            if (dataObj instanceof ComplibPaletteItemDataObject) {
                // This is a complib component on the palette
                ComplibPaletteItemDataObject cpido = (ComplibPaletteItemDataObject) dataObj;
                Complib itemComplib = cpido.getComplib();
                if (itemComplib == null) {
                    // Abnormal condition: complib cannot be found so hide it
                    return false;
                }

                /*
                 * Get the list of complibs for a project. This part only changes when a complib is
                 * added or removed from a project and does not need to be recalculated for each
                 * palette item. Optimize this if this is a bottleneck.
                 */

                // Check the embedded project complibs
                Scope scope;
                try {
                    scope = Scope.getScopeForProject(project);
                } catch (IOException e) {
                    IdeUtil.logWarning("Unable to find scope for project", e);
                    return true;
                }
                Set<ExtensionComplib> projectComplibs = scope.getComplibs();
                Identifier itemId = itemComplib.getIdentifier();
                for (ExtensionComplib complib : projectComplibs) {
                    if (itemId.equals(complib.getIdentifier())) {
                        return true;
                    }
                }

                // Check any shared complibs used by the project
                Set<SharedComplib> sharedComplibs = SharedComplibState.getInstance()
                        .getSharedComplibs(project);
                for (SharedComplib complib : sharedComplibs) {
                    if (itemId.equals(complib.getIdentifier())) {
                        return true;
                    }
                }

                return false;
            } else {
                // By default, show the object
                return true;
            }
        }
    }

    private static class ComplibPaletteActions extends PaletteActions {

        @Override
        public Action[] getCustomCategoryActions(Lookup category) {
            Project project = IdeUtil.getActiveProject();
            Node complibsRootNode = ComplibServiceProvider.getInstance().getComplibsRootNode(
                    project);
            return complibsRootNode.getActions(true);
        }

        @Override
        public Action[] getCustomItemActions(Lookup item) {
            return getCustomCategoryActions(item);
        }

        @Override
        public Action[] getCustomPaletteActions() {
            return null;
        }

        @Override
        public Action[] getImportActions() {
            return null;
        }

        @Override
        public Action getPreferredAction(Lookup item) {
            return null;
        }

    }

    /**
     * Part of interface that ComplibServiceProvider exports to the UI
     */
    public static class ComponentInfo implements Comparable {
        /** className is unique within the scope of a ComplibImpl */
        private String className;

        private HashSet<String> initialCategories = new HashSet<String>();

        /** ComplibImpl this component belongs to */
        private Complib complib;

        private String displayName;

        private String tooltip;

        private String helpId;

        private Icon icon;

        private static final Image defaultIconImage = Utilities
                .loadImage("org/netbeans/modules/visualweb/palette/resources/custom_component.png");

        private ComponentInfo(String className, Complib complib) throws ComplibException {
            this.className = className;
            this.complib = complib;

            // Get info from BeanInfo or BeanCreateInfo/Set

            ClassLoader complibLoader = complib.getClassLoader();

            /*
             * 6393979 Simulate an appropriate context ClassLoader. The context ClassLoader in
             * NetBeans, which is the NetBeans system class loader, is not what components expect so
             * temporarily set the context ClassLoader to simulate a Java EE container and restore
             * it later.
             */
            Thread currentThread = Thread.currentThread();
            ClassLoader origContextLoader = currentThread.getContextClassLoader();
            currentThread.setContextClassLoader(complibLoader);
            Class<?> beanClass;
            try {
                beanClass = Class.forName(className, true, complibLoader);
            } catch (ClassNotFoundException cnfe) {
                ComplibException e = new ComplibException("Class with name=" + className
                        + " was not found by classLoader=" + complibLoader, cnfe);
                IdeUtil.logWarning(e);
                throw e;
            } finally {
                currentThread.setContextClassLoader(origContextLoader);
            }

            try {
                Image image;
                if (BeanCreateInfo.class.isAssignableFrom(beanClass)
                        || BeanCreateInfoSet.class.isAssignableFrom(beanClass)) {
                    DisplayItem beanCreateInfoOrSet = (DisplayItem) beanClass.newInstance();
                    displayName = beanCreateInfoOrSet.getDisplayName();
                    tooltip = beanCreateInfoOrSet.getDescription();
                    helpId = beanCreateInfoOrSet.getHelpKey();
                    image = beanCreateInfoOrSet.getSmallIcon();
                } else {
                    BeanInfo bi = Introspector.getBeanInfo(beanClass);
                    BeanDescriptor bd = bi.getBeanDescriptor();
                    displayName = bd.getDisplayName();
                    tooltip = bd.getShortDescription();
                    helpId = (String) bd.getValue(Constants.BeanDescriptor.HELP_KEY);
                    image = bi.getIcon(BeanInfo.ICON_COLOR_16x16);
                }
                if (image == null) {
                    image = defaultIconImage;
                }
                icon = new ImageIcon(image);
            } catch (Exception e) {
                throw new ComplibException("Unable to access component BeanInfo", e);
            }
        }

        public int hashCode() {
            return getDisplayName().hashCode() + getClassName().hashCode();
        }

        public boolean equals(Object anObject) {
            if (this == anObject) {
                return true;
            }
            if (anObject instanceof ComponentInfo) {
                return compareTo(anObject) == 0;
            }
            return false;
        }

        public String getClassName() {
            return className;
        }

        public Icon getIcon() {
            return icon;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getTooltip() {
            return tooltip;
        }

        public String getHelpId() {
            return helpId;
        }

        public Set<String> getInitialCategories() {
            return initialCategories;
        }

        private void addInitialCategory(String category) {
            initialCategories.add(category);
        }

        public Complib getComplib() {
            return complib;
        }

        public int compareTo(Object o) {
            // Order by display name then className
            ComponentInfo anotherComponentInfo = (ComponentInfo) o;
            int dispNameCompare = getDisplayName().compareTo(anotherComponentInfo.getDisplayName());

            // If display name is equal, then sort based on className
            if (dispNameCompare == 0) {
                return getClassName().compareTo(anotherComponentInfo.getClassName());
            } else {
                return dispNameCompare;
            }
        }
    }

    /**
     * Represents a collection of related user scope complibs which have in common the same
     * namespace URI but different versions.
     * 
     * @author Edwin Goei
     */
    public static class RelatedComplibs {
        private List<ExtensionComplib> newerComplibs;

        private List<ExtensionComplib> olderComplibs;

        private ExtensionComplib sameVersionComplib;

        private RelatedComplibs(List<ExtensionComplib> newerComplibs,
                List<ExtensionComplib> olderComplibs, ExtensionComplib sameVersionComplib) {
            this.newerComplibs = newerComplibs;
            this.olderComplibs = olderComplibs;
            this.sameVersionComplib = sameVersionComplib;
        }

        public List<ExtensionComplib> getNewerComplibs() {
            return newerComplibs;
        }

        public List<ExtensionComplib> getOlderComplibs() {
            return olderComplibs;
        }

        public ExtensionComplib getSameVersionComplib() {
            return sameVersionComplib;
        }
    }

    private static final File complibsToInstallDir = new File(
            IdeUtil.getNetBeansInstallDirectory(), "complibs_to_install"); // NO18N

    private static final String COMPLIB_EXTENSION = "complib"; // NOI18N

    private static final ComponentInfo[] EMPTY_COMPONENT_INFO_ARRAY = new ComponentInfo[0];

    private EventListenerList listenerList = new EventListenerList();

    private PropertyChangeSupport pceListeners = new PropertyChangeSupport(this);

    /** Scope object where user imported complib files live */
    private Scope userScope;

    /** Stores preferences for the complib module */
    private Preferences preferences;

    public static ComplibServiceProvider getInstance() {
        return (ComplibServiceProvider) Lookup.getDefault().lookup(ComplibService.class);
    }

    /**
     * This should only be called once via ComplibService Lookup.
     * 
     * @throws IOException
     */
    public ComplibServiceProvider() throws IOException {
        /*
         * Note: no code executed as a result of calling this constructor should call back into this
         * constructor. For example, do not call ComplibServiceProvider.getInstance() or else this
         * will cause initialization problems.
         * 
         * TODO Reduce the amount of code here so it is easier to tell
         */

        initUserScope();

        installNewComplibPackages(complibsToInstallDir);

        // Get initial set of open projects
        Project[] projectsArray = OpenProjects.getDefault().getOpenProjects();
        final HashSet<Project> initialProjects = new HashSet<Project>(Arrays.asList(projectsArray));
        initProjectComplibs(initialProjects);
        initSharedComplibs(initialProjects);

        // Listen for project open events and ensure complib library defs and refs are initialized
        OpenProjects.getDefault().addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (OpenProjects.PROPERTY_OPEN_PROJECTS.equals(evt.getPropertyName())) {
                    List<Project> oldOpenProjectsList = Arrays
                            .asList((Project[]) evt.getOldValue());
                    List<Project> newOpenProjectsList = Arrays
                            .asList((Project[]) evt.getNewValue());

                    Set<Project> openedProjectsSet = new LinkedHashSet<Project>(newOpenProjectsList);
                    openedProjectsSet.removeAll(oldOpenProjectsList);
                    initProjectComplibs(openedProjectsSet);
                    initSharedComplibs(openedProjectsSet);

                    Set<Project> closedProjectsSet = new LinkedHashSet<Project>(oldOpenProjectsList);
                    closedProjectsSet.removeAll(newOpenProjectsList);
                    cleanUpProjects(closedProjectsSet);
                }
            }
        });
    }

    private void initUserScope() throws IOException {
        File userInstallDir = new File(IdeUtil.getComplibStateDir(), "installed-complibs");
        if (!userInstallDir.isDirectory()) {
            // Try to migrate installed complibs from old to new location
            String userDir = System.getProperty("netbeans.user"); // NOI18N
            if (userDir != null) {
                File oldInstallDir = new File(userDir, "complibs"); // NOI18N
                if (oldInstallDir.exists()) {
                    IdeUtil.copyFileRecursive(oldInstallDir, userInstallDir);

                    // Try to remove the old directory
                    if (!IdeUtil.deleteRecursive(oldInstallDir)) {
                        IdeUtil.logWarning("Unable to remove directory: " + oldInstallDir);
                    }
                }
            }
        }

        userScope = Scope.createScope(userInstallDir);
    }

    private void initSharedComplibs(Set<Project> projects) {
        for (Project project : projects) {
            // Load in shared complib info used by this project
            SharedComplibState.getInstance().getSharedComplibs(project);
        }
    }

    /**
     * Return just the rave VWP projects.
     * 
     * @return
     */
    private Set<Project> getOpenRaveProjects() {
        HashSet<Project> projects = new HashSet<Project>();
        Project[] projectsArray = OpenProjects.getDefault().getOpenProjects();
        for (Project project : projectsArray) {
            if (JsfProjectUtils.isJsfProject(project)) {
                projects.add(project);
            }
        }
        return projects;
    }

    private void initProjectComplibs(Set<Project> projects) {
        for (Project project : projects) {
            if (JsfProjectUtils.isJsfProject(project)) {
                try {
                    Scope scope = Scope.getScopeForProject(project);
                    Set<ExtensionComplib> projectComplibs = scope.getComplibs();
                    ensureProjectComplibsAreInstalled(projectComplibs);

                    // Init library ref for each complib
                    for (ExtensionComplib projectComplib : projectComplibs) {
                        ExtensionComplib userComplib = userScope.getExistingComplib(projectComplib);
                        if (userComplib != null) {
                            try {
                                removeLegacyLibraryRefsAndDefs(project, projectComplib);
                            } catch (IOException e) {
                                IdeUtil
                                        .logWarning(
                                                "Unable to remove legacy library reference and definition. Use Project Properties to remove manually",
                                                e);
                            }
                            addLibraryRefAndDef(project, userComplib);
                        }
                    }
                } catch (IOException e) {
                    IdeUtil.logError("Unable to initialize component libraries for project: "
                            + project.getProjectDirectory(), e);
                }
            }
        }
    }

    private void cleanUpProjects(Set<Project> projects) {
        for (Project project : projects) {
            if (JsfProjectUtils.isJsfProject(project)) {
                try {
                    Scope.destroyScopeForProject(project);
                } catch (IOException e) {
                    IdeUtil.logError(e);
                }
            }
        }
    }

    /**
     * Make sure the project complibs are installed on the palette.
     * 
     * @param projectComplibs
     */
    private void ensureProjectComplibsAreInstalled(Set<ExtensionComplib> projectComplibs) {
        ArrayList<ExtensionComplib> al = new ArrayList<ExtensionComplib>();
        for (ExtensionComplib projectComplib : projectComplibs) {
            if (!userScope.contains(projectComplib)) {
                al.add(projectComplib);
            }
        }

        if (al.isEmpty()) {
            // Project complibs are already on user palette
            return;
        }

        // Inform user
        // TODO Not sure this is the right UI
        String msg = NbBundle.getMessage(ComplibServiceProvider.class,
                "complib.initProjectComplibs"); // NOI18N
        NotifyDescriptor nd = new NotifyDescriptor.Message(msg,
                NotifyDescriptor.INFORMATION_MESSAGE);
        DialogDisplayer.getDefault().notify(nd);

        // Install complibs into user scope and the palette
        for (Iterator<ExtensionComplib> iter = al.iterator(); iter.hasNext();) {
            ExtensionComplib projectCompLib = iter.next();
            try {
                installProjectComplib(projectCompLib);
            } catch (Exception e) {
                // Output warning to IDE log
                IdeUtil.logWarning(e);
            }
        }
    }

    /**
     * Remove existing legacy Library Refs and Library Defs to an embedded complib for a particular
     * project.
     * 
     * Note: This is a hack because as of 2007-07 there isn't a NB API to enumerate all of the
     * project's library references. If and when NB provides such an API, we can change this code.
     * 
     * @param project
     * @param projectComplib
     *            complib embedded in project
     * @throws IOException
     */
    private void removeLegacyLibraryRefsAndDefs(Project project, ExtensionComplib projectComplib)
            throws IOException {
        if (removeLegacyShortfinLibRefsAndDefs(project, projectComplib)) {
            return;
        }

        removeLegacyCreatorLibRefsAndDefs(project, projectComplib);
    }

    private boolean removeLegacyShortfinLibRefsAndDefs(Project project, ExtensionComplib complib)
            throws IOException {
        /*
         * In NetBeans VWP 5.5.x or shortfin, there was only a single Lib Def with a "design-time"
         * volume but two separate Lib Refs that showed up in the Project Properites UI as a node
         * under "Libraries" w/o package checkbox and another under "Packaging".
         */
        String projectName = project.getProjectDirectory().getName();
        String libName = IdeUtil.removeWhiteSpace(projectName + "_"
                + complib.getDirectoryBaseName());
        Library libDef = getMaybeFakeLibDef(libName, complib);

        // Remove existing "compile" Lib Ref
        boolean foundCompile = JsfProjectUtils.removeLibraryReferences(project,
                new Library[] { libDef }, ClassPath.COMPILE);

        // Remove existing "package" Lib Ref
        boolean foundPackaging = JsfProjectUtils.removeLibraryReferences(project,
                new Library[] { libDef }, ClassPath.EXECUTE);

        // Remove the Lib Def
        JsfProjectUtils.removeLibrary(libName);

        // Cleanup bundle
        LibraryLocalizationBundle.remove(libName);

        // If we found at least one of them then the project was probably a shortfin project
        return foundCompile || foundPackaging;
    }

    private boolean removeLegacyCreatorLibRefsAndDefs(Project project, ExtensionComplib complib)
            throws IOException {
        /*
         * In Creator, there were two pairs of Lib Def and Lib Refs, one for design-time and one for
         * runtime. So we remove both pairs. First the runtime...
         */
        String projectName = project.getProjectDirectory().getName();
        String libName = IdeUtil.removeWhiteSpace(projectName + " Design-time "
                + complib.getTitle());
        Library libDef = getMaybeFakeLibDef(libName, complib);
        // Remove existing "compile" Lib Ref
        boolean foundCompile = JsfProjectUtils.removeLibraryReferences(project,
                new Library[] { libDef }, ClassPath.COMPILE);

        // Remove the Lib Def
        JsfProjectUtils.removeLibrary(libName);

        // Cleanup bundle
        LibraryLocalizationBundle.remove(libName);

        /*
         * Now, the design-time which appears in the Project Properties "Packaging" sub-node.
         */
        libName = IdeUtil.removeWhiteSpace(projectName + " Runtime " + complib.getTitle());
        libDef = getMaybeFakeLibDef(libName, complib);
        // Remove existing "package" Lib Ref
        boolean foundPackaging = JsfProjectUtils.removeLibraryReferences(project,
                new Library[] { libDef }, ClassPath.EXECUTE);

        // Remove the Lib Def
        JsfProjectUtils.removeLibrary(libName);

        // Cleanup bundle
        LibraryLocalizationBundle.remove(libName);

        // If we found at least one of them then the project was probably a Creator project
        return foundCompile || foundPackaging;
    }

    private Library getMaybeFakeLibDef(String libName, ExtensionComplib complib) throws IOException {
        Library libDef = LibraryManager.getDefault().getLibrary(libName);
        if (libDef == null) {
            /*
             * There is no NB API to remove just a Lib Ref so we create a dummy Lib Def to check if
             * there is a Lib Ref that points to it. As of 2007-04-22, we also need to have a
             * classpath specified or else JsfProjectUtils.hasLibraryReference(project, libDef) does
             * not work.
             */
            List<URL> rtPath = fileListToUrlList(complib.getRuntimePath());
            libDef = JsfProjectUtils.createComponentLibrary(libName, null, null, rtPath, null,
                    null, null);
        }
        return libDef;
    }

    /**
     * Ensures that a Library Ref to a user scoped complib for a project exists and it may also
     * create the associated Lib Def, if needed.
     * 
     * @param project
     * @param userComplib
     * @throws IOException
     */
    private void addLibraryRefAndDef(Project project, ExtensionComplib userComplib)
            throws IOException {
        String libName = deriveUniqueLibraryName(userComplib);
        Library libDef = LibraryManager.getDefault().getLibrary(libName);
        if (libDef == null) {
            /*
             * Library Defs are created when complib is installed into userdir (since NB 6).
             * However, it is still possible that the user removes the Lib Def afterward and so we
             * re-create it here in case that happens.
             */
            libDef = createUserScopeLibDef(userComplib);
        }
        JsfProjectUtils.addLibraryReferences(project, new Library[] { libDef });
    }

    /**
     * Derive unique name for global NB Library Defs.
     * 
     * @param complib
     *            User scope complib
     * @return
     */
    private String deriveUniqueLibraryName(ExtensionComplib complib) {
        // assert: complib is in user scope
        return complib.getDirectoryBaseName();
    }

    private List<URL> fileListToUrlList(List<File> path) throws MalformedURLException {
        ArrayList<URL> urlList = new ArrayList<URL>(path.size());
        for (File file : path) {
            urlList.add(file.toURI().toURL());
        }
        return urlList;
    }

    /**
     * Attempt to install any new complib package files in the specified directory. Any problems are
     * logged and the problem complib is then skipped.
     * 
     * @param dir
     *            Directory that contains complib package files
     */
    private void installNewComplibPackages(final File dir) {
        if (dir == null || !dir.exists()) {
            return;
        }

        /*
         * Typically, this code will run in the SwingUtilities.isEventDispatchThread() which causes
         * problems when modifying the palette so we run this in a new thread.
         */
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                // TODO Need to have modified timestamp per complib
                long scopeLastModified = userScope.getLastModified();

                FileObject complibFo = FileUtil.toFileObject(dir);
                FileObject[] children = complibFo.getChildren();
                for (int i = 0; i < children.length; i++) {
                    FileObject fo = children[i];
                    File absFile = FileUtil.toFile(fo);
                    // Install any new unexpanded complib files
                    if (COMPLIB_EXTENSION.equals(fo.getExt())) {
                        if (absFile.lastModified() > scopeLastModified) {
                            try {
                                ComplibPackage pkg = new ComplibPackage(absFile);
                                installComplibPackage(pkg);
                            } catch (Exception e) {
                                // Log the exception and continue
                                IdeUtil.logError(e);
                                continue;
                            }
                        }
                    }
                }
            }

        });
    }

    /**
     * Return all installed component libraries. Installed means built-in or in user scope.
     * 
     * @return
     */
    public ArrayList<Complib> getInstalledComplibs() {
        ArrayList<Complib> complibs = new ArrayList<Complib>();
        // TODO resurrect notion of built-in component libraries
        // complibs.add(BuiltInComplib.getInstance());
        complibs.addAll(userScope.getComplibs());
        return complibs;
    }

    /**
     * Installed complib means a user-scoped complib or the built-in complib
     * 
     * @param id
     * @return installed complib with specified identifier or null if not found
     */
    private Complib getInstalledComplib(Complib.Identifier id) {
        // FIXME Figure out what to do with built-in complibs
        // BuiltInComplib builtInCompLib = BuiltInComplib.getInstance();
        // if (builtInCompLib.getIdentifier().equals(id)) {
        // return builtInCompLib;
        // }

        // Check user scope
        Set<ExtensionComplib> complibs = userScope.getComplibs();
        for (ExtensionComplib complib : complibs) {
            if (complib.getIdentifier().equals(id)) {
                return complib;
            }
        }

        // Check any shared complibs
        /*
         * XXX Warning this may cause some interaction problems with older complib code.
         */
        HashSet<SharedComplib> shComplibs = SharedComplibState.getInstance().getAllSharedComplibs();
        for (SharedComplib complib : shComplibs) {
            if (complib.getIdentifier().equals(id)) {
                return complib;
            }
        }

        return null;
    }

    public void installComplibFile(File complibFile, boolean overwrite) throws ComplibException {
        try {
            ComplibPackage pkg = new ComplibPackage(complibFile);
            if (isInstalled(pkg) && !overwrite) {
                return;
            }
            installComplibPackage(pkg);
        } catch (IOException e) {
            throw new ComplibException("Unable to install complib file", e);
        }
    }

    /**
     * Install a complib package into user scope and into the palette overwriting any existing
     * complib and handling any failures by undoing changes.
     * 
     * @param pkg
     *            complib package
     * @throws ComplibException
     * @throws IOException
     */
    public ExtensionComplib installComplibPackage(ComplibPackage pkg) throws ComplibException,
            IOException {
        Identifier identifer = pkg.getIdentifer();
        removeExistingInstalledComplib(identifer);
        ExtensionComplib complib = userScope.installComplibPackage(pkg);
        finishInstallToUserScope(complib);
        return complib;
    }

    /**
     * Install a project scoped complib into user scope and into the palette overwriting any
     * existing complib and handling any failures by undoing changes.
     * 
     * @param projectComplib
     *            expanded project scoped complib
     * @throws ComplibException
     * @throws IOException
     */
    private void installProjectComplib(ExtensionComplib projectComplib) throws ComplibException,
            IOException {
        Identifier identifer = projectComplib.getIdentifier();
        removeExistingInstalledComplib(identifer);
        ExtensionComplib newComplib = userScope.installComplib(projectComplib);
        finishInstallToUserScope(newComplib);
    }

    private void finishInstallToUserScope(ExtensionComplib userComplib) throws ComplibException,
            IOException, MalformedURLException {
        /*
         * Temporarily install the complib into the system so that UI Item-s can be created and then
         * rollback if a problem occurs
         */
        try {
            addToPalette(userComplib);
        } catch (ComplibException e1) {
            // Rollback
            remove(userComplib);
            throw e1;
        }
        JavaHelpStorage.installComplibHelp(userComplib);

        // Create Lib Def
        createUserScopeLibDef(userComplib);

        fireAddableComplibsChanged(userScope);
    }

    /**
     * Creates a new Lib Def. Any existing Lib Def with the same name will be removed first.
     * 
     * @param complib
     * @return
     * @throws IOException
     * @throws MalformedURLException
     */
    private Library createUserScopeLibDef(ExtensionComplib complib) throws IOException,
            MalformedURLException {
        String libName = deriveUniqueLibraryName(complib);

        Library libDef = LibraryManager.getDefault().getLibrary(libName);
        if (libDef != null) {
            // If one exists, remove it first
            JsfProjectUtils.removeLibrary(libName);
        }

        // Use unique libName as a key for the description.
        String description = complib.getVersionedTitle();
        LibraryLocalizationBundle.add(libName, description);

        String localizingBundle = LibraryLocalizationBundle.class.getName();
        List<URL> rtPath = fileListToUrlList(complib.getRuntimePath());
        List<URL> dtPath = fileListToUrlList(complib.getDesignTimePath());
        List<URL> javadocPath = fileListToUrlList(complib.getJavadocPath());
        List<URL> sourcePath = fileListToUrlList(complib.getSourcePath());
        return JsfProjectUtils.createComponentLibrary(libName, libName, localizingBundle, rtPath,
                sourcePath, javadocPath, dtPath);
    }

    /**
     * Replace any existing installed complib with the same identifier
     * 
     * @param identifer
     * @throws ComplibException
     */
    private void removeExistingInstalledComplib(Identifier identifer) throws ComplibException {
        Complib existingCompLib = getInstalledComplib(identifer);
        if (existingCompLib != null) {
            if (existingCompLib instanceof ExtensionComplib) {
                ExtensionComplib extCompLib = (ExtensionComplib) existingCompLib;
                remove(extCompLib);
            } else {
                throw new ComplibException("Cannot install non-extension complib with identifier: "
                        + identifer);
            }
        }
    }

    /**
     * Add components in complib to user palette(s).
     * 
     * @param complib
     * @throws ComplibException
     */
    private void addToPalette(Complib complib) throws ComplibException {
        // Get the palette roots to add categories to
        List<Palette> palRoots = PaletteUtil.getPaletteRoots(complib);

        /*
         * Process the folders in reverse order so that if they are new, then they will appear in
         * the correct order on the palette.
         */
        List<InitialPaletteFolder> topFolders = complib.getComponentItemsInFolders();
        Collections.reverse(topFolders);

        for (InitialPaletteFolder folder : topFolders) {
            String catName = folder.getName();

            for (Palette pal : palRoots) {
                Category dstPalCat = pal.getOrCreateCategory(catName);
                for (InitialPaletteItem item : folder.getChildren()) {
                    mayBeCreatePaletteItemRecurse(dstPalCat, complib, item);
                }
            }
        }

        // Notify listeners that the palette has changed
        firePaletteChanged(new ComplibEvent(complib));
    }

    private void mayBeCreatePaletteItemRecurse(Category cat, Complib complib,
        InitialPaletteItem initItem) {
        // This method can support a hierarchical palette but NetBeans palette
        // does not support hierarchy so we place all child items in the same
        // category.
        // TODO remove this restriction in future.

        String itemClassName = initItem.getClassName();

        // 6200271 Don't add to the palette, this may be an abstract
        // base component
        if (complib.isHidden(itemClassName)) {
            return;
        }

        try {
            ComponentInfo compInfo = new ComponentInfo(itemClassName, complib);
            cat.createItem(compInfo);
        } catch (Exception e) {
            // Warn and skip this item
            IdeUtil.logWarning(e);
            return;
        }

        // Recurse by creating Item-s for each child
        List<InitialPaletteItem> children = initItem.getChildren();
        for (InitialPaletteItem item : children) {
            mayBeCreatePaletteItemRecurse(cat, complib, item);
        }

        return;
    }

    /**
     * Remove all components in a complib from user scope and the UI
     * 
     * @param complib
     */
    public void remove(ExtensionComplib complib) {
        // Remove from the UI
        removeFromPaletteCategories(complib);

        // Remove from IDE Help
        try {
            JavaHelpStorage.uninstallComplibHelp(complib);
        } catch (IOException e) {
            IdeUtil.logWarning("Unable to remove component library Help from system", e);
        }

        // Remove from user scope
        userScope.remove(complib);

        // Remove the corresponding Library Definition
        String libName = deriveUniqueLibraryName(complib);
        Library libDef = LibraryManager.getDefault().getLibrary(libName);
        if (libDef != null) {
            try {
                JsfProjectUtils.removeLibrary(libName);
            } catch (IOException e) {
                IdeUtil.logWarning("Unable to remove Library Definition: " + libName, e);
            }
        }

        fireAddableComplibsChanged(userScope);
    }

    /**
     * Iterate through all palette categories and delete PaletteItems associated with a complib
     * having the same Id. Idempotent. Also removes any empty palette categories that were
     * automatically created by the complib module.
     * 
     * @param compLibId
     */
    private void removeFromPaletteCategories(Complib complib) {
        Complib.Identifier compLibId = complib.getIdentifier();
        for (Category palCat : PaletteUtil.getAllCategories()) {
            for (Item palItem : palCat.getChildren()) {
                if (palItem.getComplibId().equals(compLibId)) {
                    palItem.remove();
                }
            }

            // Remove the palette category if it's empty
            if (palCat.isCreatedByComplib() && palCat.getChildren().isEmpty()) {
                palCat.remove();
            }
        }

        // Notify listeners that the palette has changed
        firePaletteChanged(new ComplibEvent(complib));
    }

    /**
     * @param pkg
     *            ComplibPackage
     * @return true iff pkg has the same complib ID as an installed or the built-in component
     *         library
     */
    public boolean isInstalled(ComplibPackage pkg) {
        return isInstalled(pkg.getIdentifer());
    }

    /**
     * @param complibId
     *            Complib identifier
     * @return true iff Id is same as an installed or the built-in component library
     */
    private boolean isInstalled(Complib.Identifier complibId) {
        return getInstalledComplib(complibId) != null;
    }

    /**
     * Returns information about each component contained in a complib in sorted order by display
     * name.
     * 
     * @param complib
     * @return
     * @throws ComplibException
     */
    public ComponentInfo[] getComponentInfos(Complib complib) throws ComplibException {
        // Maps String className to ComponentInfo
        HashMap<String, ComponentInfo> compInfoMap = new HashMap<String, ComponentInfo>();

        // Populate compInfoMap and also determine initial palette state
        for (InitialPaletteFolder folder : complib.getComponentItemsInFolders()) {
            String catName = folder.getName();
            getUniqueInitialItems(compInfoMap, complib, catName, folder.getChildren());
        }

        // Sort the returned components
        SortedSet<ComponentInfo> compInfos = new TreeSet<ComponentInfo>(compInfoMap.values());

        return (ComponentInfo[]) compInfos.toArray(EMPTY_COMPONENT_INFO_ARRAY);
    }

    private void getUniqueInitialItems(Map<String, ComponentInfo> compInfoMap, Complib complib,
        String folderName, List<InitialPaletteItem> items) {
        for (InitialPaletteItem item : items) {
            String className = item.getClassName();
            ComponentInfo compInfo = compInfoMap.get(className);
            if (compInfo == null) {
                // Not in Map so add an entry
                try {
                    compInfo = new ComponentInfo(className, complib);
                } catch (ComplibException e) {
                    // Skip if there are any problems
                    IdeUtil.logError(e);
                    continue;
                }
                compInfoMap.put(className, compInfo);
            }

            compInfo.addInitialCategory(folderName);

            // Supports hierarchical palette
            getUniqueInitialItems(compInfoMap, complib, folderName, item.getChildren());
        }
    }

    /**
     * Reset the palette state of a currently installed complib to a predefined initial state.
     * 
     * @param complib
     * @throws ComplibException
     */
    public void resetToInitialPalette(Complib complib) throws ComplibException {
        assert isInstalled(complib.getIdentifier());
        removeFromPaletteCategories(complib);
        addToPalette(complib);
    }

    /**
     * Remove any existing Library Ref and the complib itself from the project scope.
     * 
     * @param project
     * @param prjComplib
     * @throws IOException
     */
    private void removeComplibFromProject0(Project project, ExtensionComplib prjComplib)
            throws IOException {

        ExtensionComplib userComplib = userScope.getExistingComplib(prjComplib);
        if (userComplib == null) {
            IdeUtil.logWarning("Unable to find installed component library in userdir: "
                    + prjComplib.getVersionedTitle());
        } else {
            String libName = deriveUniqueLibraryName(userComplib);
            Library libDef = LibraryManager.getDefault().getLibrary(libName);
            if (libDef != null) {
                JsfProjectUtils.removeLibraryReferences(project, new Library[] { libDef });
            }
        }

        Scope scope = Scope.getScopeForProject(project);
        scope.remove(prjComplib);
        fireAddableComplibsChanged(scope);

        // TODO remove any project resources that were originally
        // installed
    }

    /**
     * Copy an installed userdir complib into a project and add it to the project classpath.
     * 
     * @param userDirExtComplib
     * @param project
     * @return
     * @throws IOException
     * @throws ComplibException
     */
    private ExtensionComplib copyComplibToProject(ExtensionComplib userDirExtComplib,
        Project project) throws IOException, ComplibException {
        Scope scope = Scope.getScopeForProject(project);
        ExtensionComplib projectComplib = scope.installComplib(userDirExtComplib);
        fireAddableComplibsChanged(scope);
        addLibraryRefAndDef(project, userDirExtComplib);
        installProjectResources(userDirExtComplib, project);
        return projectComplib;
    }

    private void installProjectResources(ExtensionComplib prjCompLib, Project project)
            throws IOException {
        FileObject docRootFo = JsfProjectUtils.getDocumentRoot(project);
        File docRoot = FileUtil.toFile(docRootFo);

        List<File> files = prjCompLib.getWebResourcePath();
        for (File file : files) {
            if (file.isDirectory()) {
                // Note: the named dir itself will be copied into the docRoot
                IdeUtil.copyFileRecursive(file, docRoot);
            } else {
                String baseName = file.getName();
                if (baseName.endsWith(".zip") || baseName.endsWith(".jar")) {
                    IdeUtil.unzip(file, docRoot);
                }
            }
        }
    }

    public Complib getInstalledComplib(String namespaceUri, String version) {
        Complib.Identifier id = new Complib.Identifier(namespaceUri, version);
        return getInstalledComplib(id);
    }

    public PaletteFilter createComplibPaletteFilter(Project project) {
        return new ComplibPaletteFilter(project);
    }

    public PaletteActions createComplibPaletteActions() {
        return new ComplibPaletteActions();
    }

    public void addComplibListener(ComplibListener listener) {
        listenerList.add(ComplibListener.class, listener);
    }

    public void removeComplibListener(ComplibListener listener) {
        listenerList.remove(ComplibListener.class, listener);
    }

    /**
     * Safely notifies listeners that the palette has changed by adding a new event to the dispatch
     * queue. This avoids a bug such as: 1) user drags an item from the palette, 2) complib gets
     * added to project which may change the items displayed on palette so, 3) listeners are
     * notified, 4) one listener calls refresh on palette model, 5) new Categories are created, 6)
     * drag method has a stale old Category without a parent. Fix is that step #3 is deferred until
     * later so that old Category continues to be used during drag processing. Future drag
     * processing uses new Category.
     * 
     * To support the project navigator, this event is also overloaded and may mean that a complib
     * was added or removed from a project.
     * 
     * @param evt
     */
    private void firePaletteChanged(final ComplibEvent evt) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Object[] listeners = listenerList.getListenerList();
                // Each listener occupies two elements - the first is the
                // listener class
                // and the second is the listener instance
                for (int i = 0; i < listeners.length; i += 2) {
                    if (listeners[i] == ComplibListener.class) {
                        ((ComplibListener) listeners[i + 1]).paletteChanged(evt);
                    }
                }
            };
        });
    }

    public Set<ExtensionComplib> getComplibsForProject(Project project) {
        Scope scope;
        try {
            scope = Scope.getScopeForProject(project);
        } catch (IOException e) {
            // Should not happen
            IdeUtil.logWarning(e);
            return Collections.emptySet();
        }
        return scope.getComplibs();
    }

    public Node getComplibsRootNode(Project project) {
        return new ComplibsRootNode(project);
    }

    /**
     * Remove an embedded project complib from a project and inform listeners.
     * 
     * @param project
     * @param complib
     * @throws IOException
     */
    public void removeComplibFromProject(Project project, ExtensionComplib complib)
            throws IOException {
        try {
            removeComplibFromProject0(project, complib);
        } catch (IOException e) {
            throw e;
        } finally {
            // Notify listeners that the set of complibs changed
            firePaletteChanged(new ComplibEvent(complib));
        }
    }

    /**
     * Returns a list of currently installed complibs that have the same namespace as the complib
     * parameter. The lists are sorted in highest version first order.
     * 
     * @param complib
     * @return
     */
    public RelatedComplibs getRelatedComplibs(ExtensionComplib complib) {
        Identifier thisId = complib.getIdentifier();
        URI thisNamespace = thisId.getNamespaceUri();
        Version thisVersion = thisId.getVersion();

        ArrayList<ExtensionComplib> newerComplibs = new ArrayList<ExtensionComplib>();
        ArrayList<ExtensionComplib> olderComplibs = new ArrayList<ExtensionComplib>();
        ExtensionComplib sameVersionComplib = null;
        Set<ExtensionComplib> userComplibs = userScope.getComplibs();
        for (ExtensionComplib iComplib : userComplibs) {
            Identifier iId = iComplib.getIdentifier();
            if (iId.getNamespaceUri().equals(thisNamespace)) {
                int cmp = iId.getVersion().compareTo(thisVersion);
                if (cmp > 0) {
                    newerComplibs.add(iComplib);
                } else if (cmp < 0) {
                    olderComplibs.add(iComplib);
                } else {
                    sameVersionComplib = iComplib;
                }
            }
        }

        // Return the newer complib version first
        Collections.sort(newerComplibs, Collections.reverseOrder());
        Collections.sort(olderComplibs, Collections.reverseOrder());
        return new RelatedComplibs(newerComplibs, olderComplibs, sameVersionComplib);
    }

    /**
     * Replace a project-scoped complib with a new installed user-scoped complib. They share the
     * same namespace. A null origComplib means to just add newComplib to the project.
     * 
     * @param project
     * @param origComplib
     *            original project-scoped complib, may be null
     * @param newComplib
     *            new installed user-scoped complib
     * @throws IOException
     * @throws ComplibException
     */
    public void replaceProjectComplib(Project project, ExtensionComplib origComplib,
        ExtensionComplib newComplib) throws IOException, ComplibException {
        ExtensionComplib projectComplib = null;
        try {
            if (origComplib != null) {
                removeComplibFromProject0(project, origComplib);
            }
            projectComplib = copyComplibToProject(newComplib, project);
        } catch (IOException e) {
            throw e;
        } catch (ComplibException e) {
            throw e;
        } finally {
            // Notify listeners that the set of complibs changed
            firePaletteChanged(new ComplibEvent(projectComplib));
        }
    }

    /**
     * Returns a list of installed user-imported complibs that may still be added to a project.
     * Complibs in a different namespace from any other complib in the project may still be added to
     * a project. Also, enforce Java EE spec version constraint.
     * 
     * @param project
     * @return
     */
    public List<ExtensionComplib> getAddableComplibs(Project project) {
        boolean isNonEe5Project = !JsfProjectUtils.JAVA_EE_5.equals(JsfProjectUtils
                .getJ2eePlatformVersion(project));

        HashSet<ExtensionComplib> result = new HashSet<ExtensionComplib>(userScope.getComplibs());

        Set<ExtensionComplib> prjComplibs = getComplibsForProject(project);
        for (Iterator iter = result.iterator(); iter.hasNext();) {
            ExtensionComplib usrComplib = (ExtensionComplib) iter.next();

            // TODO Generalize this code to work with future EE versions
            if (isNonEe5Project
                    && usrComplib.getCompLibManifest().getEeSpecVersion() == EeSpecVersion.JAVA_EE_5) {
                // Cannot use a Java EE 5 complib in a non-EE 5 project
                iter.remove();
                continue;
            }

            URI ns = usrComplib.getIdentifier().getNamespaceUri();
            for (ExtensionComplib prjComplib : prjComplibs) {
                if (ns.equals(prjComplib.getIdentifier().getNamespaceUri())) {
                    iter.remove();
                }
            }
        }
        return new ArrayList<ExtensionComplib>(result);
    }

    public void addProjectComplib(Project project, ExtensionComplib newComplib) throws IOException,
            ComplibException {
        replaceProjectComplib(project, null, newComplib);
    }

    /**
     * Use to keep track of AddableComplib changes
     * 
     * @param listener
     */
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        pceListeners.addPropertyChangeListener(listener);
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        pceListeners.removePropertyChangeListener(listener);
    }

    /**
     * Notify listeners that targetScope has changed
     * 
     * @param targetScope
     */
    private void fireAddableComplibsChanged(Scope targetScope) {
        pceListeners.firePropertyChange(ADDABLE_COMPLIBS, null, targetScope);
    }

    /**
     * Returns either null or a comma separated list of display names of currently opened rave
     * projects that use a particular complib. "Use" means that the complib has the same ComplibId
     * as a complib that is embedded in a project. A return value of null, means no projects use
     * that complib.
     * 
     * @param complib
     * @return
     */
    public String getInUseProjectNames(ExtensionComplib complib) {
        StringBuffer buf = new StringBuffer();
        int i = 0;
        Set<Project> openRaveProjects = getOpenRaveProjects();
        for (Project project : openRaveProjects) {
            String displayName = ProjectUtils.getInformation(project).getDisplayName();

            Scope scope;
            try {
                scope = Scope.getScopeForProject(project);
            } catch (IOException e) {
                IdeUtil.logWarning("Skipping project: " + displayName, e);
                continue;
            }

            if (scope.contains(complib)) {
                if (i > 0) {
                    buf.append(", ");
                }
                buf.append(displayName);
                i++;
            }
        }

        return i == 0 ? null : buf.toString();
    }

    public Preferences getPreferences() {
        if (preferences == null) {
            preferences = NbPreferences.forModule(ComplibServiceProvider.class);
        }
        return preferences;
    }

    private static class SharedComplibState {
        /*
         * Preferences key = value format is:
         * 
         * "sharedComplibs." + project-dir-full-path = CSV of dependent-project-base-names
         */
        private static final String SHARED_COMPLIBS = "sharedComplibs.";

        private static final SharedComplibState INSTANCE = new SharedComplibState();

        private Preferences prefs = NbPreferences.forModule(ComplibServiceProvider.class);

        /**
         * Maps Project-s to Set-s containing the SharedComplib-s for a project
         */
        private Map<Project, Set<SharedComplib>> map;

        private HashSet<SharedComplib> allSharedComplibs = new HashSet<SharedComplib>();

        public static SharedComplibState getInstance() {
            return INSTANCE;
        }

        private SharedComplibState() {
            map = new HashMap<Project, Set<SharedComplib>>();
        }

        public void addSharedComplib(Project project, SharedComplib complib) {
            allSharedComplibs.add(complib);

            Set<SharedComplib> complibs = map.get(project);
            if (complibs == null) {
                complibs = new HashSet<SharedComplib>();
            }
            complibs.add(complib);
            map.put(project, complibs);
            saveSharedComplibsForProject(project, complibs);
        }

        public Set<SharedComplib> getSharedComplibs(Project project) {
            Set<SharedComplib> complibs = map.get(project);
            if (complibs == null) {
                complibs = loadSharedComplibsForProject(project);
                allSharedComplibs.addAll(complibs);
            }
            return complibs;
        }

        public HashSet<SharedComplib> getAllSharedComplibs() {
            return allSharedComplibs;
        }

        public void removeSharedComplib(Project project, SharedComplib complibToRemove) {
            Set<SharedComplib> complibs = getSharedComplibs(project);
            if (complibs.remove(complibToRemove)) {
                // Remove from set of all shared complibs if no longer used
                boolean found = false;
                Collection<Set<SharedComplib>> allShared = map.values();
                for (Set<SharedComplib> set : allShared) {
                    if (set.contains(complibToRemove)) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    allSharedComplibs.remove(complibToRemove);
                }

                saveSharedComplibsForProject(project, complibs);
            }
        }

        /**
         * Factory method to create a SharedComplib from a NetBeans Project. Note that a particular
         * build structure is assumed.
         * 
         * @param project
         * @return SharedComplib or throws exception
         * @throws ComplibException
         * @throws IOException
         */
        public SharedComplib createSharedComplibFromProject(Project project)
                throws ComplibException, IOException {
            FileObject complibRoot = projectToSharedComplibDir(project);
            if (complibRoot == null) {
                throw new ComplibException(
                        "Shared component library project must have a built 'build/complib' directory.");
            }

            SharedComplib complib = new SharedComplib(FileUtil.toFile(complibRoot));
            return complib;
        }

        public FileObject projectToSharedComplibDir(Project project) {
            FileObject projectDirFo = project.getProjectDirectory();

            // XXX Warning assumes a particular build dir structure
            FileObject complibRoot = projectDirFo.getFileObject("build/complib");
            return complibRoot;
        }

        /**
         * Returns the Project directory corresponding to a SharedComplib. Assumes a particular
         * directory structure. Return null if there is a problem.
         * 
         * @param complib
         * @return
         */
        public File sharedComplibDirToProjectDir(SharedComplib complib) {
            // XXX Assumes structure is projectDir/build/complib/
            File complibDir = complib.getDirectory();
            File parentFile = complibDir.getParentFile();
            if (parentFile == null) {
                // Somethings wrong so skip it
                return null;
            }
            parentFile = parentFile.getParentFile();
            if (parentFile == null) {
                // Somethings wrong so skip it
                return null;
            }
            return parentFile;
        }

        private Set<SharedComplib> loadSharedComplibsForProject(Project project) {
            HashSet<SharedComplib> complibs = new HashSet<SharedComplib>();

            String key = SHARED_COMPLIBS + project.getProjectDirectory().getPath();

            // Init from the persisted projects list
            String projectCSV = prefs.get(key, "");
            String[] projectBasenames = projectCSV.split(",");
            for (String projectName : projectBasenames) {
                Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
                for (Project iProject : openProjects) {
                    if (iProject.getProjectDirectory().getNameExt().equals(projectName)) {
                        SharedComplib complib;
                        try {
                            complib = createSharedComplibFromProject(iProject);
                        } catch (Exception e) {
                            IdeUtil.logWarning("Skipping bad shared complib", e);
                            continue;
                        }
                        complibs.add(complib);
                        break;
                    }
                }
            }
            return complibs;
        }

        /**
         * @param project
         * @param complibs
         */
        private void saveSharedComplibsForProject(Project project, Set<SharedComplib> complibs) {
            String key = SHARED_COMPLIBS + project.getProjectDirectory().getPath();

            StringBuffer sb = new StringBuffer();
            for (SharedComplib complib : complibs) {
                File projectDir = sharedComplibDirToProjectDir(complib);
                if (projectDir == null) {
                    // Skip if there is a problem
                    continue;
                }

                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(projectDir.getName());
            }
            prefs.put(key, sb.toString());
        }
    }

    private Project getActiveProject() throws ComplibException {
        Project activeProject = IdeUtil.getActiveProject();
        if (activeProject == null) {
            throw new ComplibException("Error: No active project. Select one in designer.");
        }
        return activeProject;
    }

    public List<Project> getEligibleSharedComplibProjects() {
        ArrayList<Project> result = new ArrayList<Project>();
        Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
        SharedComplibState scs = SharedComplibState.getInstance();
        for (Project project : openProjects) {
            if (scs.projectToSharedComplibDir(project) != null) {
                result.add(project);
            }
        }
        return result;
    }

    /**
     * Attempt to add project as a shared complib project to the active project.
     * 
     * @param shComplibProject
     * @throws IOException
     * @throws ComplibException
     */
    public void addSharedComplibProject(Project shComplibProject) throws ComplibException,
            IOException {
        Project activeProject = getActiveProject();
        SharedComplibState scs = SharedComplibState.getInstance();
        SharedComplib complib = scs.createSharedComplibFromProject(shComplibProject);

        // Only add to palette if it is not already there
        if (!scs.getAllSharedComplibs().contains(complib)) {
            try {
                addToPalette(complib);
            } catch (ComplibException e1) {
                // Rollback
                removeFromPaletteCategories(complib);
                throw e1;
            }
        }

        scs.addSharedComplib(activeProject, complib);
        addSharedComplibLibraryDefAndRefs(activeProject, complib);
    }

    public Set<SharedComplib> getSharedComplibsForActiveProject() throws ComplibException {
        Project activeProject = getActiveProject();
        return SharedComplibState.getInstance().getSharedComplibs(activeProject);
    }

    public void removeSharedComplibsFromActiveProject(Set<SharedComplib> complibsToRemove)
            throws IOException, ComplibException {
        Project activeProject = getActiveProject();
        SharedComplibState scs = SharedComplibState.getInstance();
        for (SharedComplib complib : complibsToRemove) {
            removeSharedComplibLibraryDefAndRefs(activeProject, complib);

            scs.removeSharedComplib(activeProject, complib);

            // Remove from palette only when no longer in use
            Set<SharedComplib> allShared = scs.getAllSharedComplibs();
            if (!allShared.contains(complib)) {
                removeFromPaletteCategories(complib);
            }
        }
    }

    public void refreshSharedComplibsForActiveProject() throws ComplibException {
        Set<SharedComplib> complibs = getSharedComplibsForActiveProject();
        for (SharedComplib complib : complibs) {
            removeFromPaletteCategories(complib);
            addToPalette(complib);
        }
    }

    /**
     * Add any needed library definition and references for a shared complib to a project.
     * 
     * @param project
     * @param sharedComplib
     * @throws IOException
     */
    private void addSharedComplibLibraryDefAndRefs(Project project, SharedComplib sharedComplib)
            throws IOException {
        String localizingBundle = LibraryLocalizationBundle.class.getName();

        LibraryDescriptor libDescriptor = deriveSharedComplibLibraryName(sharedComplib);
        String libName = libDescriptor.getLibName();

        Library libDef = LibraryManager.getDefault().getLibrary(libName);
        if (libDef != null) {
            // Assume the definition is correct, if not the user can manually
            // remove it and it will be recreated when the project is re-opened
            return;
        }

        // Use the name of the library as a key for the description
        LibraryLocalizationBundle.add(libName, libDescriptor.getDescription());

        List<URL> rtPath = fileListToUrlList(sharedComplib.getRuntimePath());
        List<URL> dtPath = fileListToUrlList(sharedComplib.getDesignTimePath());
        List<URL> javadocPath = fileListToUrlList(sharedComplib.getJavadocPath());
        List<URL> sourcePath = fileListToUrlList(sharedComplib.getSourcePath());
        libDef = JsfProjectUtils.createComponentLibrary(libName, libName, localizingBundle, rtPath,
                sourcePath, javadocPath, dtPath);

        // Create new compile-time and deploy Library Ref
        if (!JsfProjectUtils.addLibraryReferences(project, new Library[] { libDef })) {
            IdeUtil.logError("Failed to add compile-time library reference to project: "
                    + libDef.getName());
        }
    }

    /**
     * Remove any existing NB Library Defs and Refs corresponding to a shared complib for a project
     * 
     * @param project
     * @param sharedComplib
     * @throws IOException
     */
    private void removeSharedComplibLibraryDefAndRefs(Project project, SharedComplib sharedComplib)
            throws IOException {
        LibraryDescriptor libDescriptor = deriveSharedComplibLibraryName(sharedComplib);
        String libName = libDescriptor.getLibName();

        Library libDef = LibraryManager.getDefault().getLibrary(libName);
        if (libDef != null) {
            // Existing definition so first remove any existing references
            JsfProjectUtils.removeLibraryReferences(project, new Library[] { libDef });

            JsfProjectUtils.removeLibrary(libName);

            // Cleanup bundle
            LibraryLocalizationBundle.remove(libName);
        }
    }

    private static class LibraryDescriptor {
        private String libName;

        private String description;

        public LibraryDescriptor(String libName, String description) {
            this.libName = libName;
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public String getLibName() {
            return libName;
        }

    }

    /**
     * Derive name and description for global NB Library Defs for a shared component library.
     * 
     * @param sharedComplib
     * @return
     */
    private LibraryDescriptor deriveSharedComplibLibraryName(SharedComplib sharedComplib) {
        String description = SharedComplibState.getInstance().sharedComplibDirToProjectDir(
                sharedComplib).getName()
                + " Shared Component Library";
        String libName = IdeUtil.removeWhiteSpace(description);
        return new LibraryDescriptor(libName, description);
    }

}
