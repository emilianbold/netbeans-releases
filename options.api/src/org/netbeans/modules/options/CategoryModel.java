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

package org.netbeans.modules.options;

import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsCategory;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.FolderLookup;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ProxyLookup;

/**
 * @author Radek Matous
 */
public final class CategoryModel  {
    private static WeakReference instance = new WeakReference(null);
    private final RequestProcessor RP = new RequestProcessor();
    
    private String currentCategoryName = null;
    private String highlitedCategoryName = null;    
    private final Map/**<String, CategoryItem>*/ names2CategoryItems = Collections.synchronizedMap(new LinkedHashMap());
    private MasterLookup masterLookup;
    private final RequestProcessor.Task masterLookupTask = RP.create(new Runnable() {
        public void run() {
            //SwingUtilities.invokeLater(new Runnable() {
                //public void run() {
                    String[] names = getCategoryNames();
                    List all = new ArrayList();
                    for (int i = 0; i < names.length; i++) {
                        Category item = getCategory(names[i]);
                        Lookup lkp = item.getLookup();
                        assert lkp != null;
                        if (lkp != Lookup.EMPTY) {
                            all.add(lkp);
                        }
                    }
                    getMasterLookup().setLookups(all);                    
                //}
            //});            
        }
    },true);
    private final RequestProcessor.Task categoryTask = RP.create(new Runnable() {
        public void run() {
            List all = loadOptionsCategories();       
            Map temp = new LinkedHashMap();
            for (Iterator it = all.iterator(); it.hasNext();) {
                OptionsCategory oc = (OptionsCategory) it.next();
                Category cat = new Category(oc);
                temp.put(cat.getCategoryName(), cat);
            }
            names2CategoryItems.clear();
            names2CategoryItems.putAll(temp);
            masterLookupTask.schedule(0);
        }
    },true);
    
    private CategoryModel() {
        categoryTask.schedule(0);
    }
    
    static CategoryModel getInstance() {
        CategoryModel retval = (CategoryModel)instance.get();
        if (retval == null) {
            retval = new CategoryModel();
            instance = new WeakReference(retval);
        }
        return retval;
    }
    
    boolean isInitialized() {
        return categoryTask.isFinished();
    }
    
    boolean isLookupInitialized() {
        return masterLookupTask.isFinished();
    }
    
    
    void waitForInitialization() {
        categoryTask.waitFinished();
    }
        
    String getCurrentCategoryName() {
        return verifyCategoryName(currentCategoryName);
    }

    String getHighlitedCategoryName() {
        return verifyCategoryName(highlitedCategoryName);
    }
        
    private String verifyCategoryName(String categName) {
        String retval = findCurrentCategoryName(categName) != -1 ? categName : null;
        if (retval == null) {
            String[] names = getCategoryNames();
            if (names.length > 0) {
                retval = categName = names[0];
            }
        }
        return retval;
    }
    
    private int findCurrentCategoryName(String name) {
        return name == null ? -1 : Arrays.asList(getCategoryNames()).indexOf(name);
    }
    
    String[] getCategoryNames() {
        categoryTask.waitFinished();
        Set keys = names2CategoryItems.keySet();
        return (String[])keys.toArray(new String[keys.size()]);
    }
    
    Category getCurrent() {
        String name =  getCurrentCategoryName();
        return (name == null) ? null : getCategory(name);
    }
    
    void setCurrent(Category item) {
        item.setCurrent();
    }

    void setHighlited(Category item) {
        item.setHighlited();
    }
            
    HelpCtx getHelpCtx() {
        final CategoryModel.Category category = getCurrent();
        return (category == null) ? null : category.getHelpCtx();
    }
    
    void update(PropertyChangeListener l, boolean force) {
        String[] names = getCategoryNames();
        for (int i = 0; i < names.length; i++) {
            CategoryModel.Category item = getCategory(names[i]);
            item.update(l, force);
        }
    }
    
    void save() {
        String[] names = getCategoryNames();
        for (int i = 0; i < names.length; i++) {
            CategoryModel.Category item = getCategory(names[i]);
            item.applyChanges();
        }
    }
    
    void cancel() {
        String[] names = getCategoryNames();
        for (int i = 0; i < names.length; i++) {
            CategoryModel.Category item = getCategory(names[i]);
            item.cancel();
        }
    }
    
    boolean dataValid() {
        boolean retval = true;
        String[] names = getCategoryNames();
        for (int i = 0; retval && i < names.length; i++) {
            CategoryModel.Category item = getCategory(names[i]);
            retval = item.isValid();
        }
        return retval;
    }
    
    boolean isChanged() {
        boolean retval = false;
        String[] names = getCategoryNames();
        for (int i = 0; !retval && i < names.length; i++) {
            CategoryModel.Category item = getCategory(names[i]);
            retval = item.isChanged();
        }
        return retval;
    }
    
    
    void setNextCategoryAsCurrent() {
        int idx =  findCurrentCategoryName(getCurrentCategoryName());
        String[] names = getCategoryNames();
        if (idx >= 0 && idx+1 < names.length) {
            currentCategoryName = names[idx+1];
        }  else {
            currentCategoryName = null;
        }
    }
    
    void setPreviousCategoryAsCurrent() {
        int idx =  findCurrentCategoryName(getCurrentCategoryName());
        String[] names = getCategoryNames();
        if (idx >= 0 && idx < names.length && names.length > 0) {
            if (idx-1 >= 0) {
                currentCategoryName = names[idx-1];
            }  else {
                currentCategoryName = names[names.length-1];
            }
        } else {
            currentCategoryName = null;
        }
    }
    
    
    Category getCategory(String categoryName) {
        categoryTask.waitFinished();
        return (Category)names2CategoryItems.get(categoryName);
    }
        
    private MasterLookup getMasterLookup() {
        if (masterLookup == null) {
            masterLookup = new MasterLookup();
        }
        return masterLookup;
    }
    
    private static List loadOptionsCategories() {
        FileObject fo = Repository.getDefault().getDefaultFileSystem().
                findResource("OptionsDialog");                            // NOI18N
        if (fo != null) {
            Lookup lookup = new FolderLookup(DataFolder.findFolder(fo)).
                    getLookup();
            return Collections.unmodifiableList(new ArrayList(lookup.lookup(
                    new Lookup.Template(OptionsCategory.class)
                    ).allInstances()));
        }
        return Collections.EMPTY_LIST;
    }
    
    final class Category  {
        private OptionsCategory category;
        private OptionsPanelController controller;
        private boolean isUpdated;
        private HelpCtx helpCtx;
        private JComponent component;
        private Lookup lookup;
        
        private Category(OptionsCategory category) {
            this.category = category;
        }
        
        boolean isCurrent() {
            return getCategoryName().equals(getCurrentCategoryName());
        }
        
        boolean isHighlited() {
            return getCategoryName().equals(getHighlitedCategoryName());
        }
                
        private void setCurrent() {
            currentCategoryName = getCategoryName();
        }

        private void setHighlited() {
            highlitedCategoryName = getCategoryName();
        }
                        
        public Icon getIcon() {
            return category.getIcon();
        }
        
        public String getCategoryName() {
            return category.getCategoryName();
        }
        
        public String getTitle() {
            return category.getTitle();
        }
        
        private synchronized OptionsPanelController create() {
            if (controller == null) {
                controller = category.create();
            }
            return controller;
        }
        
        final void update(PropertyChangeListener l, boolean forceUpdate) {
            if ((!isUpdated && !forceUpdate) || (isUpdated && forceUpdate)) {
                isUpdated = true;
                getComponent();
                create().update();
                if (l != null) {
                    create().addPropertyChangeListener(l);
                }
            }
        }
        
        private void applyChanges() {
            if (isUpdated) {
                create().applyChanges();
            }
        }
        
        private void cancel() {
            if (isUpdated) {
                create().cancel();
            }
        }
        
        private boolean isValid() {
            boolean retval = true;
            if (isUpdated) {
                retval = create().isValid();
            }
            return retval;
        }
        
        private boolean isChanged() {
            boolean retval = false;
            if (isUpdated) {
                retval = create().isChanged();
            }
            return retval;
        }
        
        public JComponent getComponent() {
            if (component == null) {
                component = create().getComponent(getMasterLookup());
            }
            return component;
        }
        
        private HelpCtx getHelpCtx() {
            if (helpCtx == null && isUpdated) {
                helpCtx = create().getHelpCtx();
            }
            return helpCtx;
        }
        
        
        private Lookup getLookup() {
            if (lookup == null) {
                lookup = create().getLookup();
            }
            return lookup;
        }
    }
    
    private class MasterLookup extends ProxyLookup {
        private void setLookups(List lookups) {
            setLookups((Lookup[])lookups.toArray(new Lookup[lookups.size()]));
        }
        protected void beforeLookup(Lookup.Template template) {
            super.beforeLookup(template);
            masterLookupTask.waitFinished();
        }
    }
}
