/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.uml.project.ui.customizer.uiapi;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;


/** Maintains current category in the customizer.
 *
 * @author Petr Hrebejk
 */
public class CategoryModel {

    public static final String PROP_CURRENT_CATEGORY = "propCurrentCategory";

    private ProjectCustomizer.Category[] categories;

    private ProjectCustomizer.Category currentCategory;

    // Might be vetoable later
    private PropertyChangeSupport pcs;
    
    public CategoryModel( ProjectCustomizer.Category[] categories ) {
        
        if ( categories == null || categories.length == 0 ) {
            throw new IllegalArgumentException( "Must provide at least one category" ); // NOI18N
        }
        
        this.categories = categories;
        this.currentCategory = categories[0];
        this.pcs = new PropertyChangeSupport( this );
    }
    
    
    public ProjectCustomizer.Category getCurrentCategory() {
        return this.currentCategory;
    }
    
    public ProjectCustomizer.Category getCategory( String name ) {
        return findCategoryByName( name, categories );
    }
    
    public void setCurrentCategory( ProjectCustomizer.Category category ) {
        
        if ( currentCategory != category ) {
            ProjectCustomizer.Category oldValue = currentCategory;
            this.currentCategory = category;
            firePropertyChange( PROP_CURRENT_CATEGORY, oldValue, category );
        }
        
    }
    
    public ProjectCustomizer.Category[] getCategories() {
        return this.categories;
    }
    
    public void addPropertyChangeListener( String propertyName, PropertyChangeListener l ) {
        pcs.addPropertyChangeListener( propertyName, l );
    }
    
    public void addPropertyChangeListener( PropertyChangeListener l ) {
        pcs.addPropertyChangeListener( l );
    }
    
    public void removePropertyChangeListener( String propertyName, PropertyChangeListener l ) {
        pcs.removePropertyChangeListener( propertyName, l );
    }
    
    public void removePropertyChangeListener( PropertyChangeListener l ) {
        pcs.removePropertyChangeListener( l );
    }
    
    public void firePropertyChange( String propertyName, Object oldValue, Object newValue ) {
        pcs.firePropertyChange( propertyName, oldValue, newValue );
    }
    
    // Private methods ---------------------------------------------------------
    
    private static ProjectCustomizer.Category findCategoryByName( String name, ProjectCustomizer.Category[] categories ) {
        
        for( int i = 0; i < categories.length; i++ ) {
            if ( name.equals( categories[i].getName() ) ) {
                return categories[i];
            }
            
            ProjectCustomizer.Category[] subcategories = categories[i].getSubcategories();
            if ( subcategories != null ) {
                ProjectCustomizer.Category category = findCategoryByName( name, subcategories );
                if ( category != null ) {
                    return category;
                }
            }
            
        }
        
        return null;
    }
    
}
