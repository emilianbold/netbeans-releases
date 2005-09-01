/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.project.ui.support;

import java.awt.Dialog;
import java.awt.Image;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.netbeans.modules.project.uiapi.CategoryModel;
import org.netbeans.modules.project.uiapi.CategoryView;
import org.netbeans.modules.project.uiapi.CategoryChangeSupport;
import org.netbeans.modules.project.uiapi.CustomizerDialog;
import org.netbeans.modules.project.uiapi.CustomizerPane;
import org.netbeans.modules.project.uiapi.Utilities;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.util.HelpCtx;

/** Support for creating dialogs which can be used as project
 * customizers. The dialog may display multiple panels or categories.
 * @see org.netbeans.spi.project.ui.CustomizerProvider
 * @see org.netbeans.spi.project.ui.support.ProjectCustomizer#Category
 *
 * @author Petr Hrebejk, Martin Krauskopf
 */
public final class ProjectCustomizer {
    
    /** Factory/Namespace class only. */
    private ProjectCustomizer() {
    }
    
    /** Creates standard which can be used for implementation
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
        CustomizerPane innerPane = (CustomizerPane) createCustomizerPane( categories, componentProvider, preselectedCategory );
        Dialog dialog = CustomizerDialog.createDialog( okOptionListener, innerPane, helpCtx, categories );
        return dialog;
    }
    
    /** Creates standard innerPane for customizer dialog.
     */
    private static JPanel createCustomizerPane( Category[] categories,
                                                CategoryComponentProvider componentProvider,
                                                String preselectedCategory ) {
        
        CategoryChangeSupport changeSupport = new CategoryChangeSupport();
        for (int i = 0; i < categories.length; i++) {
            Utilities.putCategoryChangeSupport(categories[i], changeSupport);
            Category[] subCategories = categories[i].getSubcategories();
            if (subCategories != null) {
                for (int j = 0; j < subCategories.length; j++) {
                    Utilities.putCategoryChangeSupport(subCategories[j], changeSupport);
                }
            }
        }
        
        CategoryModel categoryModel = new CategoryModel( categories );
        JPanel categoryView = new CategoryView( categoryModel );
        JPanel customizerPane = new CustomizerPane( categoryView, categoryModel, componentProvider );
        
        if ( preselectedCategory == null ) {
            preselectedCategory = categories[0].getName();
        }
        
        Category c = categoryModel.getCategory( preselectedCategory );
        if ( c != null ) {
            categoryModel.setCurrentCategory( c );
        }
        
        return customizerPane;
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
    
    /** Describes category of properties to be customized by given component
     */
    public static final class Category {
        
        private String name;
        private String displayName;
        private Image icon;
        private Category[] subcategories;
        private boolean valid;
        private String errorMessage;
        
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
         */
        public static Category create( String name,
                                       String displayName,
                                       Image icon,
                                       Category[] subcategories ) {
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
                        CategoryChangeSupport.VALID_PROPERTY, Boolean.valueOf(!valid), Boolean.valueOf(valid));
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
        
    }
    
}
