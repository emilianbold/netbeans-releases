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

package org.netbeans.spi.project.ui.support;

import java.awt.Dialog;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.netbeans.modules.project.uiapi.CategoryModel;
import org.netbeans.modules.project.uiapi.CategoryView;
import org.netbeans.modules.project.uiapi.CategoryChangeSupport;
import org.netbeans.modules.project.uiapi.CustomizerDialog;
import org.netbeans.modules.project.uiapi.CustomizerPane;
import org.netbeans.modules.project.uiapi.Utilities;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/** Support for creating dialogs which can be used as project
 * customizers. The dialog may display multiple panels or categories.
 * @see org.netbeans.spi.project.ui.CustomizerProvider
 * @see ProjectCustomizer.Category
 *
 * @author Petr Hrebejk, Martin Krauskopf
 */
public final class ProjectCustomizer {
    
    /** Factory/Namespace class only. */
    private ProjectCustomizer() {
    }
    
    /** Creates standard customizer dialog which can be used for implementation
     * of {@link org.netbeans.spi.project.ui.CustomizerProvider}. You don't need
     * to call <code>pack()</code> method on the dialog. The resulting dialog will
     * be non-modal. <br>
     * Call <code>show()</code> on the dialog to make it visible. If you want the dialog to be
     * closed after user presses the "OK" button you have to call hide() and dispose() on it.
     * (Usually in the <code>actionPerformed(...)</code> method of the listener
     * you provided as a parameter. In case of the click on the "Cancel" button
     * the dialog will be closed automatically.
     * @param categories array of descriptions of categories to be shown in the
     *        dialog. Note that categories have the <code>valid</code>
     *        property. If any of the given categories is not valid cusomizer's
     *        OK button will be disabled until all categories become valid
     *        again.
     * @param componentProvider creator of GUI components for categories in the
     *        customizer dialog.
     * @param preselectedCategory name of one of the supplied categories or null.
     *        Category with given name will be selected. If  <code>null</code>
     *        or if the category of given name does not exist the first category will
     *        be selected.
     * @param okOptionListener listener which will be notified when the user presses
     *        the OK button.
     * @param helpCtx Help context for the dialog, which will be used when the
     *        panels in the customizer do not specify their own help context.
     * @return standard project customizer dialog.
     */
    public static Dialog createCustomizerDialog( Category[] categories,
                                                 CategoryComponentProvider componentProvider,
                                                 String preselectedCategory,
                                                 ActionListener okOptionListener,
                                                 HelpCtx helpCtx ) {
        return createCustomizerDialog(categories, componentProvider, preselectedCategory, okOptionListener, null, helpCtx);
    }
    
    /** Creates standard customizer dialog which can be used for implementation
     * of {@link org.netbeans.spi.project.ui.CustomizerProvider}. Use this version if you need 
     * to run processing of the customizer data partially off AWT Event Queue. You don't need
     * to call <code>pack()</code> method on the dialog. The resulting dialog will
     * be non-modal. <br>
     * Call <code>show()</code> on the dialog to make it visible. If you want the dialog to be
     * closed after user presses the "OK" button you have to call hide() and dispose() on it.
     * (Usually in the <code>actionPerformed(...)</code> method of the listener
     * you provided as a parameter. In case of the click on the "Cancel" button
     * the dialog will be closed automatically.
     * @since org.netbeans.modules.projectuiapi/1 1.25
     * @param categories array of descriptions of categories to be shown in the
     *        dialog. Note that categories have the <code>valid</code>
     *        property. If any of the given categories is not valid cusomizer's
     *        OK button will be disabled until all categories become valid
     *        again.
     * @param componentProvider creator of GUI components for categories in the
     *        customizer dialog.
     * @param preselectedCategory name of one of the supplied categories or null.
     *        Category with given name will be selected. If  <code>null</code>
     *        or if the category of given name does not exist the first category will
     *        be selected.
     * @param okOptionListener listener which will be notified when the user presses
     *        the OK button.
     * @param storeListener listener which will be notified when the user presses OK button.
     *        Listener will be executed after okOptionListener outside of AWT EventQueue.
     *        Usually to be used to save modified files on disk.
     * @param helpCtx Help context for the dialog, which will be used when the
     *        panels in the customizer do not specify their own help context.
     * @return standard project customizer dialog.
     */
    public static Dialog createCustomizerDialog( Category[] categories,
                                                 CategoryComponentProvider componentProvider,
                                                 String preselectedCategory,
                                                 ActionListener okOptionListener,
                                                 ActionListener storeListener,
                                                 HelpCtx helpCtx ) {
        CustomizerPane innerPane = createCustomizerPane(categories, componentProvider, preselectedCategory);
        Dialog dialog = CustomizerDialog.createDialog(okOptionListener, storeListener, innerPane, helpCtx, categories, componentProvider);
        return dialog;
    }
    
    /**
     * Creates standard customizer dialog that can be used for implementation of
     * {@link org.netbeans.spi.project.ui.CustomizerProvider} based on content of a folder in Layers.
     * Use this method when you want to allow composition and 3rd party additions to your customizer UI.
     * You don't need to call <code>pack()</code> method on the dialog. The resulting dialog will
     * be non-modal. <br> 
     * Call <code>show()</code> on the dialog to make it visible. If you want the dialog to be
     * closed after user presses the "OK" button you have to call hide() and dispose() on it.
     * (Usually in the <code>actionPerformed(...)</code> method of the listener
     * you provided as a parameter. In case of the click on the "Cancel" button
     * the dialog will be closed automatically.
     * @since org.netbeans.modules.projectuiapi/1 1.15
     * @param folderPath the path in the System Filesystem that is used as root for panel composition.
     *        The content of the folder is assummed to be {@link org.netbeans.spi.project.ui.support.ProjectCustomizer.CompositeCategoryProvider} instances
     * @param context the context for the panels, up to the project type what the context shall be, for example org.netbeans.api.project.Project instance
     * @param preselectedCategory name of one of the supplied categories or null.
     *        Category with given name will be selected. If  <code>null</code>
     *        or if the category of given name does not exist the first category will
     *        be selected.
     * @param okOptionListener listener which will be notified when the user presses
     *        the OK button.
     * @param helpCtx Help context for the dialog, which will be used when the
     *        panels in the customizer do not specify their own help context.
     * @return standard project customizer dialog.
     */
    public static Dialog createCustomizerDialog( String folderPath,
                                                 Lookup context,
                                                 String preselectedCategory,
                                                 ActionListener okOptionListener,
                                                 HelpCtx helpCtx) {
        return createCustomizerDialog(folderPath, context, preselectedCategory, 
                                      okOptionListener, null, helpCtx);
    }
    
    /**
     * Creates standard customizer dialog that can be used for implementation of
     * {@link org.netbeans.spi.project.ui.CustomizerProvider} based on content of a folder in Layers.
     * Use this method when you want to allow composition and 3rd party additions to your customizer UI.
     * This version runs processing of the customizer data partially off AWT Event Queue.
     * You don't need to call <code>pack()</code> method on the dialog. The resulting dialog will
     * be non-modal. <br> 
     * Call <code>show()</code> on the dialog to make it visible. If you want the dialog to be
     * closed after user presses the "OK" button you have to call hide() and dispose() on it.
     * (Usually in the <code>actionPerformed(...)</code> method of the listener
     * you provided as a parameter. In case of the click on the "Cancel" button
     * the dialog will be closed automatically.
     * 
     * @since org.netbeans.modules.projectuiapi/1 1.25
     * @param folderPath the path in the System Filesystem that is used as root for panel composition.
     *        The content of the folder is assummed to be {@link org.netbeans.spi.project.ui.support.ProjectCustomizer.CompositeCategoryProvider} instances
     * @param context the context for the panels, up to the project type what the context shall be, for example org.netbeans.api.project.Project instance
     * @param preselectedCategory name of one of the supplied categories or null.
     *        Category with given name will be selected. If  <code>null</code>
     *        or if the category of given name does not exist the first category will
     *        be selected.
     * @param okOptionListener listener which will be notified when the user presses
     *        the OK button.
     * @param storeListener listener which will be notified when the user presses OK button.
     *        Listener will be executed after okOptionListener outside of AWT EventQueue.
     *        Usually to be used to save modified files on disk
     * @param helpCtx Help context for the dialog, which will be used when the
     *        panels in the customizer do not specify their own help context.
     * @return standard project customizer dialog.
     */
    public static Dialog createCustomizerDialog( String folderPath,
                                                 Lookup context,
                                                 String preselectedCategory,
                                                 ActionListener okOptionListener,
                                                 ActionListener storeListener,
                                                 HelpCtx helpCtx) {
        FileObject root = Repository.getDefault().getDefaultFileSystem().findResource(folderPath);
        if (root == null) {
            throw new IllegalArgumentException("The designated path " + folderPath + " doesn't exist. Cannot create customizer.");
        }
        DataFolder def = DataFolder.findFolder(root);
        assert def != null : "Cannot find DataFolder for " + folderPath;
        DelegateCategoryProvider prov = new DelegateCategoryProvider(def, context);
        return createCustomizerDialog(prov.getSubCategories(), prov, preselectedCategory, 
                                      okOptionListener, storeListener, helpCtx);
    }
    
    /** Creates standard innerPane for customizer dialog.
     */
    private static CustomizerPane createCustomizerPane( Category[] categories,
                                                CategoryComponentProvider componentProvider,
                                                String preselectedCategory ) {
        
        CategoryChangeSupport changeSupport = new CategoryChangeSupport();
        registerCategoryChangeSupport(changeSupport, categories);
        
        CategoryModel categoryModel = new CategoryModel( categories );
        JPanel categoryView = new CategoryView( categoryModel );
        CustomizerPane customizerPane = new CustomizerPane( categoryView, categoryModel, componentProvider );
        
        if ( preselectedCategory == null ) {
            preselectedCategory = categories[0].getName();
        }
        
        Category c = categoryModel.getCategory( preselectedCategory );
        if ( c != null ) {
            categoryModel.setCurrentCategory( c );
        }
        
        return customizerPane;
    }

    private static void registerCategoryChangeSupport(final CategoryChangeSupport changeSupport, 
            final Category[] categories) {        
        for (int i = 0; i < categories.length; i++) {
            Utilities.putCategoryChangeSupport(categories[i], changeSupport);
            Category[] subCategories = categories[i].getSubcategories();
            if (subCategories != null) {
                registerCategoryChangeSupport(changeSupport, subCategories);
            }
        }
    }

    
    /** Provides components for categories.
     */
    public static interface CategoryComponentProvider {
        
        /** Creates component which has to be shown for given category.
         * @param category The Category
         * @return UI component for category customization
         */
        JComponent create( Category category );
        
    }

    /**
     * Interface for creation of Customizer categories and their respective UI panels.
     * Implementations are to be registered in System FileSystem via module layers. Used by the
     * {@link org.netbeans.spi.project.ui.support.ProjectCustomizer#createCustomizerDialog(String,Lookup,String,ActionListener,HelpCtx)}
     * The panel/category created by the provider can get notified that the customizer got
     * closed by setting an <code>ActionListener</code> to 
     * {@link org.netbeans.spi.project.ui.support.ProjectCustomizer.Category#setOkButtonListener} .
     * UI Component can be defined for category folder that is represented as node with subnodes in the category
     * tree of project customizer. Name of the file that defines the instance class in layer for such category 
     * must be named "Self". Such CompositeCategory won't have the createCategory() method called, but will have the category created by
     * the infrastructure based on the folder content.
     * For details and usage see issue #91276.
     * @since org.netbeans.modules.projectuiapi/1 1.22
     */
    public static interface CompositeCategoryProvider {

        /**
         * create the Category instance for the given project customizer context.
         * @param context Lookup instance passed from project The content is up to the project type, please consult documentation
         * for the project type you want to integrate your panel into.
         * @return A category instance, can be null, in which case no category and no panels are created for given context.
         *   The instance is expected to have no subcategories.
         */
        Category createCategory( Lookup context );

        /**
         * create the UI component for given category and context.
         * The panel/category created by the provider can get notified that the customizer got
         * closed by setting an <code>ActionListener</code> to 
         * {@link org.netbeans.spi.project.ui.support.ProjectCustomizer.Category#setOkButtonListener}.
         * @param category Category instance that was created in the createCategory method.
         * @param context Lookup instance passed from project The content is up to the project type, please consult documentation
         * for the project type you want to integrate your panel into.
         */
        JComponent createComponent (Category category, Lookup context );
    }
    
    /** Describes category of properties to be customized by given component
     */
    public static final class Category {
        
        private String name;
        private String displayName;
        private Image icon;
        private Category[] subcategories;
        private boolean valid;
        private String errorMessage;
        private ActionListener okListener;
        private ActionListener storeListener;
        
        /** Private constructor. See the factory method.
         */
        private Category( String name,
                         String displayName,
                         Image icon,
                         Category[] subcategories ) {
            
            this.name = name;
            this.displayName = displayName;
            this.icon = icon;
            this.subcategories = subcategories;
            this.valid = true; // default
        }
        
        /** Factory method which creates new category description.
         * @param name Programmatic name of the category
         * @param displayName Name to be shown to the user
         * @param icon Icon for given category. Will use default icon if null.
         * @param subcategories Subcategories to be shown under given category.
         *        Category won't be expandable if null or empty array.
         * @return a new category description
         */
        public static Category create( String name,
                                       String displayName,
                                       Image icon,
                                       Category... subcategories ) {
            return new Category( name, displayName, icon, subcategories );
        }
        
        // Public methods ------------------------------------------------------
        
        /** Gets programmatic name of given category.
         * @return Programmatic name of the category
         */
        public String getName() {
            return this.name;
        }
        
        /** Gets display name of given category.
         * @return Display name of the category
         */
        public String getDisplayName() {
            return this.displayName;
        }
        
        /** Gets icon of given category.
         * @return Icon name of the category or null
         */
        public Image getIcon() {
            return this.icon;
        }
        
        /** Gets subcategories of given category.
         * @return Subcategories of the category or null
         */
        public Category[] getSubcategories() {
            return this.subcategories;
        }
        
        /**
         * Returns an error message for this category.
         * @return the error message (could be null)
         */
        public String getErrorMessage() {
            return errorMessage;
        }
        
        /**
         * Returns whether this category is valid or not. See {@link
         * ProjectCustomizer#createCustomizerDialog} for more details.
         * @return whether this category is valid or not (true by default)
         */
        public boolean isValid() {
            return valid;
        }
        
        /**
         * Set a validity of this category. See {@link
         * ProjectCustomizer#createCustomizerDialog} for more details.
         * @param valid set whether this category is valid or not
         */
        public void setValid(boolean valid) {
            if (this.valid != valid) {
                this.valid = valid;
                Utilities.getCategoryChangeSupport(this).firePropertyChange(
                        CategoryChangeSupport.VALID_PROPERTY, !valid, valid);
            }
        }
        
        /**
         * Set an errror message for this category which than may be shown in a
         * project customizer.
         *
         * @param message message for this category. To <em>reset</em> a
         *        message usually <code>null</code> or an empty string is
         *        passed. (similar to behaviour of {@link
         *        javax.swing.text.JTextComponent#setText(String)})
         */
        public void setErrorMessage(String message) {
            if (message == null) {
                message = "";
            }
            if (!message.equals(this.errorMessage)) {
                String oldMessage = this.errorMessage;
                this.errorMessage = message;
                Utilities.getCategoryChangeSupport(this).firePropertyChange(
                        CategoryChangeSupport.ERROR_MESSAGE_PROPERTY, oldMessage, message);
            }
        }
        
        /**
         * Set the action listener that will get notified when the changes in the customizer 
         * are to be applied.
         * @param okButtonListener ActionListener to notify 
         * @since org.netbeans.modules.projectuiapi/1 1.20
         */ 
        public void setOkButtonListener(ActionListener okButtonListener) {
            okListener = okButtonListener;
        }
        
        /**
         * Returns the action listener associated with this category that gets notified
         * when OK button is pressed on the customizer.
         * @return instance of ActionListener or null if not set.
         * @since org.netbeans.modules.projectuiapi/1 1.20
         */ 
        public ActionListener getOkButtonListener() {
            return okListener;
        }
        
        /**
         * Set the action listener that will get notified when the changes in the customizer 
         * are to be applied. Listener is executed after OkButtonListener outside of AWT EventQueue. 
         * Usually to be used to save modified files on disk.
         * @param listener ActionListener to notify 
         * @since org.netbeans.modules.projectuiapi/1 1.25
         */
        public void setStoreListener(ActionListener listener) {
            storeListener = listener;
        }
        
        /**
         * Returns the action listener that is executed outside of AWT EQ and is associated 
         * with this category that gets notified when OK button is pressed on the customizer.
         * @return instance of ActionListener or null if not set.
         * @since org.netbeans.modules.projectuiapi/1 1.25
         */
        public ActionListener getStoreListener() {
            return storeListener;
        }
        
    }

    /*private*/ static class DelegateCategoryProvider implements CategoryComponentProvider, CompositeCategoryProvider, Lookup.Provider {

        private final Lookup context;
        private final Map<ProjectCustomizer.Category,CompositeCategoryProvider> category2provider;
        private final DataFolder folder;
        private final CompositeCategoryProvider selfProvider;

        public DelegateCategoryProvider(DataFolder folder, Lookup context) {
            this(folder, context, new HashMap<ProjectCustomizer.Category,CompositeCategoryProvider>());
        }

        private DelegateCategoryProvider(DataFolder folder, Lookup context, Map<ProjectCustomizer.Category,CompositeCategoryProvider> cat2Provider) {
            this(folder, context, cat2Provider, null);
        }
        
        private DelegateCategoryProvider(DataFolder folder, Lookup context, Map<ProjectCustomizer.Category,CompositeCategoryProvider> cat2Provider, CompositeCategoryProvider sProv) {
            this.context = context;
            this.folder = folder;
            category2provider = cat2Provider;
            selfProvider = sProv;
        }

        public JComponent create(ProjectCustomizer.Category category) {
            CompositeCategoryProvider prov = category2provider.get(category);
            assert prov != null : "Category doesn't have a provider associated.";
            return prov.createComponent(category, context);
        }

        public ProjectCustomizer.Category[] getSubCategories() {
            try {
               return readCategories(folder);
            } catch (IOException exc) {
                Logger.getAnonymousLogger().log(Level.WARNING, "Cannot construct Project UI panels", exc);
                return new ProjectCustomizer.Category[0];
            } catch (ClassNotFoundException ex) {
                Logger.getAnonymousLogger().log(Level.WARNING, "Cannot construct Project UI panels", ex);
                return new ProjectCustomizer.Category[0];
            }
        }


        /*private*/ ProjectCustomizer.Category[] readCategories(DataFolder folder) throws IOException, ClassNotFoundException {
            List<ProjectCustomizer.Category> toRet = new ArrayList<ProjectCustomizer.Category>();
            for (DataObject dob : folder.getChildren()) {
                if (dob instanceof DataFolder) {
                    CompositeCategoryProvider sProvider = null;
                    DataObject subDobs[] = ((DataFolder) dob).getChildren();
                    for (DataObject subDob : subDobs) {
                        if (subDob.getName().equals("Self")) { // NOI18N
                            InstanceCookie cookie = subDob.getCookie(InstanceCookie.class);
                            if (cookie != null && CompositeCategoryProvider.class.isAssignableFrom(cookie.instanceClass())) {
                                sProvider = (CompositeCategoryProvider) cookie.instanceCreate();
                            }
                        }
                    }
                    CompositeCategoryProvider prov = null;
                    if (sProvider != null) {
                        prov = new DelegateCategoryProvider((DataFolder) dob, context, category2provider, sProvider);
                    } else {
                        prov = new DelegateCategoryProvider((DataFolder) dob, context, category2provider);
                    }
                    ProjectCustomizer.Category cat = prov.createCategory(context);
                    toRet.add(cat);
                    category2provider.put(cat, prov);
                }
                if (!dob.getName().equals("Self")) { // NOI18N
                    InstanceCookie cook = dob.getCookie(InstanceCookie.class);
                    if (cook != null && CompositeCategoryProvider.class.isAssignableFrom(cook.instanceClass())) {
                        CompositeCategoryProvider provider = (CompositeCategoryProvider)cook.instanceCreate();
                        ProjectCustomizer.Category cat = provider.createCategory(context);
                        if (cat != null) {
                            toRet.add(cat);
                            category2provider.put(cat, provider);
                            includeSubcats(cat.getSubcategories(), provider);
                        }
                    }
                }
            }
            return toRet.toArray(new ProjectCustomizer.Category[toRet.size()]);
        }
        
        private void includeSubcats(ProjectCustomizer.Category[] cats, ProjectCustomizer.CompositeCategoryProvider provider) {
            if (cats != null) {
                for (ProjectCustomizer.Category cat : cats) {
                    category2provider.put(cat, provider);
                    includeSubcats(cat.getSubcategories(), provider);
                }
            }
        }

        /**
         * provides category for folder..
         */
        public ProjectCustomizer.Category createCategory(Lookup context) {
            FileObject fo = folder.getPrimaryFile();
            String dn = fo.getNameExt();
            try {
                dn = fo.getFileSystem().getStatus().annotateName(fo.getNameExt(), Collections.singleton(fo));
            } catch (FileStateInvalidException ex) {
                Logger.getAnonymousLogger().log(Level.WARNING, "Cannot retrieve display name for folder " + fo.getPath(), ex);
            }
            return ProjectCustomizer.Category.create(folder.getName(), dn, null, getSubCategories());
        }

        /**
         * provides component for folder category
         */
        public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
            if (selfProvider != null) {
                return selfProvider.createComponent(category, context);
            }
            return new JPanel();
        }
        //#97998 related
        public Lookup getLookup() {
            return context;
        }
    }
}
