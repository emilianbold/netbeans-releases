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

package org.netbeans.spi.palette;
import javax.swing.Action;
import org.netbeans.modules.palette.Category;
import org.netbeans.modules.palette.Item;
import org.netbeans.modules.palette.Model;
import org.netbeans.modules.palette.ModelListener;
import org.openide.util.Lookup;



/**
 *
 * @author Stanislav Aubrecht
 */
public class ProxyModel implements Model {
    
    boolean showCustomizerCalled = false;
    private Model original;
    
    /** Creates a new instance of DummyModel */
    public ProxyModel( Model original ) {
        this.original = original;
    }

    public void showCustomizer(org.netbeans.modules.palette.Settings settings) {
        showCustomizerCalled = true;
        //super.showCustomizer(settings);
    }

    public void addModelListener(ModelListener listener) {
        original.addModelListener( listener );
    }

    public void removeModelListener(ModelListener listener) {
        original.removeModelListener( listener );
    }

    public boolean moveCategory( Category source, Category target, boolean moveBefore ) {
        return original.moveCategory( source, target, moveBefore );
    }

    public void refresh() {
        original.refresh();
    }

    public void setSelectedItem(Category category, Item item) {
        original.setSelectedItem( category, item );
    }

    public Action[] getActions() {
        return original.getActions();
    }

    public Category[] getCategories() {
        return original.getCategories();
    }

    public String getName() {
        return original.getName();
    }

    public Lookup getRoot() {
        return original.getRoot();
    }

    public Category getSelectedCategory() {
        return original.getSelectedCategory();
    }

    public Item getSelectedItem() {
        return original.getSelectedItem();
    }

    public void reset() {
        original.reset();
    }

}
