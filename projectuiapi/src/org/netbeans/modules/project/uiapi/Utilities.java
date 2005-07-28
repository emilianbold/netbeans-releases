/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.uiapi;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;

/**
 * Way of getting implementations of UI components defined in projects/projectui.
 * @author Petr Hrebejk, Jesse Glick
 */
public class Utilities {
    
    private static final Map/*<ProjectCustomizer.Category, CategoryChangeSupport>*/ CATEGORIES = new HashMap();
    
    private Utilities() {}
    
    /** Gets action factory from the global Lookup.
     */
    public static ActionsFactory getActionsFactory() {
        ActionsFactory instance = (ActionsFactory) Lookup.getDefault().lookup(ActionsFactory.class);
        assert instance != null;
        return instance;
    }
    
    /** Gets the projectChooser fatory from the global Lookup
     */
    public static ProjectChooserFactory getProjectChooserFactory() {
        ProjectChooserFactory instance = (ProjectChooserFactory) Lookup.getDefault().lookup(ProjectChooserFactory.class);
        assert instance != null;
        return instance;
    }
    
    /** Gets an object the OpenProjects can delegate to
     */
    public static OpenProjectsTrampoline getOpenProjectsTrampoline() {
        OpenProjectsTrampoline instance = (OpenProjectsTrampoline) Lookup.getDefault().lookup(OpenProjectsTrampoline.class);
        assert instance != null;
        return instance;
    }
    
    public static CategoryChangeSupport getCategoryChangeSupport(ProjectCustomizer.Category category) {
        CategoryChangeSupport cw = (CategoryChangeSupport) Utilities.CATEGORIES.get(category);
        return cw == null ? CategoryChangeSupport.NULL_INSTANCE : cw;
    }
    
    public static void putCategoryChangeSupport(
            ProjectCustomizer.Category category, CategoryChangeSupport wrapper) {
        Utilities.CATEGORIES.put(category, wrapper);
    }
    
}
