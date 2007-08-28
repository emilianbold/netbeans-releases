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

package org.netbeans.modules.form.palette;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.*;
import java.text.MessageFormat;
import java.io.File;
import java.io.IOException;

import org.netbeans.spi.palette.*;
import org.openide.ErrorManager;
import org.openide.nodes.*;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.filesystems.*;
import org.openide.util.*;
import org.openide.util.lookup.*;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.FileOwnerQuery;

import org.netbeans.modules.form.project.ClassSource;

/**
 * Class providing various useful methods for palette classes.
 *
 * @author Tomas Pavek, Jan Stola
 */

public final class PaletteUtils {
    
    private static FileObject paletteFolder;
    private static DataFolder paletteDataFolder;

    private static FileObject context;
    private static Map<Project,ProjectPaletteInfo> palettes = new WeakHashMap<Project,ProjectPaletteInfo>();

    private static class ProjectPaletteInfo {
        PaletteLookup paletteLookup;
        ClassPathFilter paletteFilter;
        List<PropertyChangeListener> paletteListeners;

        PaletteController getPalette() {
            return paletteLookup.lookup(PaletteController.class);
        }
    }

    private PaletteUtils() {
    }
    
    static String getItemComponentDescription(PaletteItem item) {
        ClassSource classSource = item.getComponentClassSource();
        
        if (classSource == null || classSource.getCPRootCount() == 0) {
            String className = classSource.getClassName();
            if (className != null) {
                if (className.startsWith("javax.") // NOI18N
                        || className.startsWith("java.")) // NOI18N
                    return getBundleString("MSG_StandardJDKComponent"); // NOI18N
                if (className.startsWith("org.netbeans.")) // NOI18N
                    return getBundleString("MSG_NetBeansComponent"); // NOI18N
            }
        }
        else {
            String type = classSource.getCPRootType(0);
            String name = classSource.getCPRootName(0);
            
            if (ClassSource.JAR_SOURCE.equals(type)) {
                return MessageFormat.format(
                        getBundleString("FMT_ComponentFromJar"), // NOI18N
                        new Object[] { name });
            }
            else if (ClassSource.LIBRARY_SOURCE.equals(type)) {
                Library lib = LibraryManager.getDefault().getLibrary(name);
                return MessageFormat.format(
                        getBundleString("FMT_ComponentFromLibrary"), // NOI18N
                        new Object[] { lib != null ? lib.getDisplayName() : name });
            }
            else if (ClassSource.PROJECT_SOURCE.equals(type)) {
                try {
                    Project project = FileOwnerQuery.getOwner(new File(name).toURI());
                    return MessageFormat.format(
                            getBundleString("FMT_ComponentFromProject"), // NOI18N
                            new Object[] { project == null ? name :
                                FileUtil.getFileDisplayName(project.getProjectDirectory()) });
                } catch (Exception ex) {
                    // XXX must catch specific exceptions and notify them or explain why they are ignored!
                }
            }
        }
        
        return getBundleString("MSG_UnspecifiedComponent"); // NOI18N
    }
    
    public static FileObject getPaletteFolder() {
        if (paletteFolder != null)
            return paletteFolder;
        
        try {
            paletteFolder = Repository.getDefault().getDefaultFileSystem()
                    .findResource("FormDesignerPalette"); // NOI18N
            if (paletteFolder == null) // not found, create new folder
                paletteFolder = Repository.getDefault().getDefaultFileSystem()
                        .getRoot().createFolder("FormDesignerPalette"); // NOI18N
        }
        catch (java.io.IOException ex) {
            throw new IllegalStateException("Palette folder not found and cannot be created."); // NOI18N
        }
        return paletteFolder;
    }
    
    public static Node getPaletteNode() {
        return getPaletteDataFolder().getNodeDelegate();
    }

    public static void showPaletteManager() {
        try {
            PaletteFactory.createPalette("FormDesignerPalette", // NOI18N
                                         new FormPaletteActions(),
                                         new ClassPathFilter(null), // filters out only invisible Layouts category
                                         null)
                    .showCustomizer();
        }
        catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }

    public static void setContext(FileObject fileInProject) {
        context = fileInProject;
    }

    public static synchronized void addPaletteListener(PropertyChangeListener listener,
                                                       FileObject context)
    {
        ProjectPaletteInfo pInfo = preparePalette(context);
        if (pInfo != null) {
            if (pInfo.paletteListeners == null) {
                pInfo.paletteListeners = new LinkedList<PropertyChangeListener>();
            }
            pInfo.paletteListeners.add(listener);
            pInfo.getPalette().addPropertyChangeListener(listener);
        }
    }

    public static synchronized void removePaletteListener(PropertyChangeListener listener,
                                                          FileObject context)
    {
        Project project = FileOwnerQuery.getOwner(context);
        if (project != null) {
            ProjectPaletteInfo pInfo = palettes.get(project);
            if (pInfo != null && pInfo.paletteListeners != null) {
                pInfo.paletteListeners.remove(listener);
                pInfo.getPalette().removePropertyChangeListener(listener);
            }
        }
    }

    public static Lookup getPaletteLookup(FileObject context) {
        ProjectPaletteInfo pInfo = preparePalette(context);
        return pInfo != null ? pInfo.paletteLookup : Lookups.fixed(new Object[0]);
    }

    private static PaletteController getPalette() {
        ProjectPaletteInfo pInfo = preparePalette(context);
        return pInfo != null ? pInfo.getPalette() : null;
    }

    private static ClassPathFilter getPaletteFilter() {
        if (context != null) {
            Project project = FileOwnerQuery.getOwner(context);
            if (project != null) {
                ProjectPaletteInfo pInfo = palettes.get(project);
                if (pInfo != null)
                    return pInfo.paletteFilter;
            }
        }
        return null;
    }

    /**
     * Gets the registered palette and related data for given context (project
     * of given file). Creates new palette if does not exist yet.
     */
    private static ProjectPaletteInfo preparePalette(FileObject context) {
        if (context == null)
            return null;

        Project project = FileOwnerQuery.getOwner(context);
        if (project == null)
            return null;

        ProjectPaletteInfo pInfo = palettes.get(project);
        if (pInfo == null) {
            ClassPath classPath = ClassPath.getClassPath(context, ClassPath.BOOT);
            classPath.addPropertyChangeListener(new ClassPathListener(classPath, project));

            PaletteLookup lookup = new PaletteLookup();
            ClassPathFilter filter = new ClassPathFilter(classPath);
            lookup.setPalette(createPalette(filter));

            pInfo = new ProjectPaletteInfo();
            pInfo.paletteLookup = lookup;
            pInfo.paletteFilter = filter;
            palettes.put(project, pInfo);
        }
        return pInfo;
    }

    /**
     * Creates a new palette with filter for given ClassPath.
     */
    private static PaletteController createPalette(ClassPathFilter filter) {
        try {
            return PaletteFactory.createPalette("FormDesignerPalette", // NOI18N
                                                new FormPaletteActions(),
                                                filter,
                                                null);
        }
        catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return null;
        }
    }

    /**
     * Called when the project's boot classpath changes (typically means that
     * the project's platform has changed). This method creates a new palette
     * with a filter based on the new classpath, updating the lookup providing
     * the palette. Palette listeners are transferred automatically.
     */
    private static synchronized void bootClassPathChanged(Project p, ClassPath cp) {
        ProjectPaletteInfo pInfo = palettes.get(p);
        if (pInfo != null) {
            PaletteLookup lookup = pInfo.paletteLookup;
            PaletteController oldPalette = pInfo.getPalette();
            oldPalette.clearSelection();
            ClassPathFilter newFilter = new ClassPathFilter(cp);
            PaletteController newPalette = createPalette(newFilter);
            if (pInfo.paletteListeners != null) {
                for (PropertyChangeListener l : pInfo.paletteListeners) {
                    oldPalette.removePropertyChangeListener(l);
                    newPalette.addPropertyChangeListener(l);
                }
            }
            lookup.setPalette(newPalette);
            pInfo.paletteFilter = newFilter;
        }
    }

    static DataFolder getPaletteDataFolder() {
        if (paletteDataFolder == null)
            paletteDataFolder = DataFolder.findFolder(getPaletteFolder());
        return paletteDataFolder;
    }
    
    public static void clearPaletteSelection() {
        getPalette().clearSelection();
    }
    
    public static PaletteItem getSelectedItem() {
        PaletteController palette = getPalette();
        if (palette == null) {
            return null;
        }
        Lookup lkp = palette.getSelectedItem();
        
        return lkp.lookup(PaletteItem.class);
    }
    
    public static void selectItem( PaletteItem item ) {
        if( null == item ) {
            getPalette().clearSelection();
        } else {
            // This is not the node returned by getPaletteNode()!
            Node paletteNode = getPalette().getRoot().lookup(Node.class);
            Node[] categories = getCategoryNodes(paletteNode, true, true, true);
            for( int i=0; i<categories.length; i++ ) {
                Node[] items = getItemNodes( categories[i], true );
                for( int j=0; j<items.length; j++ ) {
                    PaletteItem formItem = items[j].getLookup().lookup( PaletteItem.class );
                    if( item.equals( formItem ) ) {
                        getPalette().setSelectedItem( categories[i].getLookup(), items[j].getLookup() );
                    }
                }
            }
        }
    }
    
    public static PaletteItem[] getAllItems() {
        HashSet<PaletteItem> uniqueItems = null;
        // collect valid items from all categories (including invisible)
        Node[] categories = getCategoryNodes(getPaletteNode(), false, true, false);
        for( int i=0; i<categories.length; i++ ) {
            Node[] items = getItemNodes( categories[i], true );
            for( int j=0; j<items.length; j++ ) {
                PaletteItem formItem = items[j].getLookup().lookup( PaletteItem.class );
                if( null != formItem ) {
                    if( null == uniqueItems ) {
                        uniqueItems = new HashSet<PaletteItem>();
                    }
                    uniqueItems.add( formItem );
                }
            }
        }
        PaletteItem[] res;
        if( null != uniqueItems ) {
            res = uniqueItems.toArray( new PaletteItem[uniqueItems.size()] );
        } else {
            res = new PaletteItem[0];
        }
        return res;
    }
    
    static String getBundleString(String key) {
        return NbBundle.getBundle(PaletteUtils.class).getString(key);
    }
    
    /**
     * Get an array of Node for the given category.
     *
     * @param categoryNode Category node.
     * @param mustBeValid True if all the nodes returned must be valid palette items.
     * @return An array of Nodes for the given category.
     */
    public static Node[] getItemNodes( Node categoryNode, boolean mustBeValid ) {
        Node[] nodes = categoryNode.getChildren().getNodes( true );
        if (!mustBeValid)
            return nodes;

        ClassPathFilter filter = getPaletteFilter();
        if (filter == null)
            return nodes;

        List<Node> validList = null;
        for (int i=0; i < nodes.length; i++) {
            PaletteItem item = nodes[i].getCookie(PaletteItem.class);
            if (filter.isValidItem(item)) {
                if (validList != null)
                    validList.add(nodes[i]);
            }
            else if (validList == null) {
                validList = new ArrayList<Node>(nodes.length);
                for (int j=0; j < i; j++) {
                    validList.add(nodes[j]);
                }
            }
        }
        if (validList != null)
            nodes = validList.toArray(new Node[validList.size()]);

        return nodes;
    }
    
    /**
     * Get an array of all categories in the given palette.
     *
     * @param paletteNode Palette's root node.
     * @param mustBeVisible True to return only visible categories, false to return also
     * categories with Hidden flag.
     * @return An array of categories in the given palette.
     */
    public static Node[] getCategoryNodes(Node paletteNode, boolean mustBeVisible) {
        return getCategoryNodes(paletteNode, mustBeVisible, mustBeVisible, true);
    }
    
    /**
     * Get an array of all categories in the given palette.
     *
     * @param paletteNode Palette's root node.
     * @param mustBeVisible True to return only visible categories, false to return also
     *        categories with Hidden flag (user can setup what's visibile in palette manager).
     * @param mustBeValid True to return only categories containing some
     *        classpath-valid items, false to don't care about platform classpath.
     * @param mustBePaletteCategory True to return only categories not tagged as
     *        'isNoPaletteCategory' (marks a never visible category like Layouts)
     * @return An array of category nodes in the given palette.
     */
    private static Node[] getCategoryNodes(Node paletteNode,
                                           boolean mustBeVisible,
                                           boolean mustBeValid,
                                           boolean mustBePaletteCategory)
    {
        if (mustBeVisible)
            mustBeValid = mustBePaletteCategory = true;

        Node[] nodes = paletteNode.getChildren().getNodes(true);

        ClassPathFilter filter = mustBeValid ? getPaletteFilter() : null;
        java.util.List<Node> list = null; // don't create until needed
        for( int i=0; i<nodes.length; i++ ) {
            if ((!mustBeVisible || isVisibleCategoryNode(nodes[i]))
                && (!mustBeValid || filter == null || filter.isValidCategory(nodes[i]))
                && (!mustBePaletteCategory || representsShowableCategory(nodes[i])))
            {   // this is a relevant category
                if( list != null ) {
                    list.add(nodes[i]);
                }
            } else if( list == null ) {
                list = new ArrayList<Node>(nodes.length);
                for( int j=0; j < i; j++ ) {
                    list.add(nodes[j]);
                }
            }
        }
        if( list != null ) {
            nodes = new Node[list.size()];
            list.toArray(nodes);
        }
        return nodes;
    }
    
    /**
     * @return True if the given node is a DataFolder and does not have Hidden flag set.
     */
    private static boolean isVisibleCategoryNode(Node node) {
        DataFolder df = node.getCookie(DataFolder.class);
        if (df != null) {
            Object value = node.getValue("psa_" + PaletteController.ATTR_IS_VISIBLE); // NOI18N
            if (null == value || "null".equals(value)) { // NOI18N
                value = df.getPrimaryFile().getAttribute(PaletteController.ATTR_IS_VISIBLE);
            }
            if (value == null) {
                value = Boolean.TRUE;
            }
            return Boolean.valueOf(value.toString()).booleanValue();
        }
        return false;
    }

    private static boolean representsShowableCategory(Node node) {
        DataFolder df = node.getCookie(DataFolder.class);
        return (df != null) && !Boolean.TRUE.equals(df.getPrimaryFile().getAttribute("isNoPaletteCategory")); // NOI18N
    }
    
    // -----

    /**
     * Filter for PaletteController. Filters items from platform (i.e. not user
     * beans) based on given classpath. If classpath is null, all items pass.
     * Also filters out categories containing only unavailable items.
     * Always filters out permanently invisible categories (e.g. Layouts).
     */
    private static class ClassPathFilter extends PaletteFilter {
        private ClassPath classPath;
        private Set<PaletteItem> validItems;
        private Set<PaletteItem> invalidItems;

        ClassPathFilter(ClassPath cp) {
            if (cp != null) {
                validItems = new WeakSet<PaletteItem>();
                invalidItems = new WeakSet<PaletteItem>();
            }
            classPath = cp;
        }

        public boolean isValidCategory(Lookup lkp) {
            Node categoryNode = lkp.lookup(Node.class);
            if (!representsShowableCategory(categoryNode))
                return false; // filter out categories that should never be visible (e.g. Layouts)

            return isValidCategory(categoryNode);
        }

        boolean isValidCategory(Node node) {
            if (classPath == null)
                return true;

            // check if there is some valid item in this category
            // [ideally we should listen on the category for adding/removing items,
            //  practically we just need to hide Swing categories on some mobile platforms]
            DataFolder folder = node.getCookie(DataFolder.class);
            if (folder == null)
                return false;

            DataObject[] dobjs = folder.getChildren();
            for (int i=0; i < dobjs.length; i++) {
                PaletteItem item = dobjs[i].getCookie(PaletteItem.class);
                if (item == null || isValidItem(item))
                    return true;
            }
            return dobjs.length == 0;
        }

        public boolean isValidItem(Lookup lkp) {
            return isValidItem(lkp.lookup(PaletteItem.class));
        }

        boolean isValidItem(PaletteItem item) {
            if (classPath == null)
                return true;
            
            if (item == null) // Issue 81506
                return false;

            if (item.getComponentClassSource().getCPRootCount() > 0
                || PaletteItem.TYPE_CHOOSE_BEAN.equals(item.getExplicitComponentType())
                || "org.netbeans.modules.form.layoutsupport.delegates.NullLayoutSupport".equals(item.getComponentClassName())) // NOI18N
                return true; // this is not a platform component

           if (validItems.contains(item)) {
                return true;
            }
            else if (invalidItems.contains(item)) {
                return false;
            }

            // check if the class is available on platform classpath
            String resName = item.getComponentClassName().replace('.', '/').concat(".class"); // NOI18N
            if (classPath.findResource(resName) != null) {
                validItems.add(item);
                return true;
            }
            else {
                invalidItems.add(item);
                return false;
            }
        }
    }

    /**
     * Reacts on classpath changes and updates the palette for given project
     * accordingly (in lookup).
     */
    private static class ClassPathListener implements PropertyChangeListener {
        private ClassPath classPath;
        private WeakReference<Project> projRef;

        ClassPathListener(ClassPath cp, Project p) {
            classPath = cp;
            projRef = new WeakReference<Project>(p);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (ClassPath.PROP_ROOTS.equals(evt.getPropertyName())) {
                Project p = projRef.get();
                if (p != null)
                    PaletteUtils.bootClassPathChanged(p, classPath);
                else
                    classPath.removePropertyChangeListener(this);
            }
        }
    }

    /**
     * Lookup providing a PaletteController. Can be updated with a new instance.
     */
    private static class PaletteLookup extends AbstractLookup {
        private InstanceContent content;

        PaletteLookup() {
            this(new InstanceContent());
        }

        private PaletteLookup(InstanceContent content) {
            super(content);
            this.content = content;
        }

        void setPalette(PaletteController palette) {
            content.set(Arrays.asList(new PaletteController[] { palette }), null);
        }
    }
}
