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

    public void showCustomizer(PaletteController pc, org.netbeans.modules.palette.Settings settings) {
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

    public void setSelectedItem(Lookup category, Lookup item) {
        original.setSelectedItem( category, item );
    }

    public void clearSelection() {
        original.clearSelection();
    }

    public boolean canReorderCategories() {
        return original.canReorderCategories();
    }
}
