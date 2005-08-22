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

import javax.swing.Action;
import org.netbeans.modules.project.uiapi.Utilities;

/**
 * Factory for commonly needed generic project actions.
 * @author Jesse Glick, Petr Hrebejk
 */
public class CommonProjectActions {
    
    /**
     * {@link org.openide.filesystems.FileObject} value honored by {@link #newProjectAction}
     * that defines initial value for existing sources directory choosers.
     *
     * @since org.netbeans.modules.projectuiapi/1 1.3
     */
    public static final String EXISTING_SOURCES_FOLDER = "existingSourcesFolder";
    
    private CommonProjectActions() {}
        
    /**
     * Create an action "Set As Main Project".
     * It should be invoked with an action context containing
     * one {@link org.netbeans.api.project.Project}.
     * <p class="nonnormative">
     * You might include this in the context menu of a logical view.
     * </p>
     * @return an action
     */
    public static Action setAsMainProjectAction() {
        return Utilities.getActionsFactory().setAsMainProjectAction();
    }
    
    /**
     * Create an action "Customize Project".
     * It should be invoked with an action context containing
     * one {@link org.netbeans.api.project.Project}.
     * <p class="nonnormative">
     * You might include this in the context menu of a logical view.
     * </p>
     * @return an action
     */
    public static Action customizeProjectAction() {
        return Utilities.getActionsFactory().customizeProjectAction();
    }
    
    /**
     * Create an action "Open Subprojects".
     * It should be invoked with an action context containing
     * one or more {@link org.netbeans.api.project.Project}s.
     * <p class="nonnormative">
     * You might include this in the context menu of a logical view.
     * </p>
     * @return an action
     * @see org.netbeans.spi.project.SubprojectProvider
     */
    public static Action openSubprojectsAction() {
        return Utilities.getActionsFactory().openSubprojectsAction();
    }
    
    /**
     * Create an action "Close Project".
     * It should be invoked with an action context containing
     * one or more {@link org.netbeans.api.project.Project}s.
     * <p class="nonnormative">
     * You might include this in the context menu of a logical view.
     * </p>
     * @return an action
     */
    public static Action closeProjectAction() {
        return Utilities.getActionsFactory().closeProjectAction();
    }
    
    /**
     * Create an action project dependent "New File" action.
     * <p class="nonnormative">
     * You might include this in the context menu of a logical view.
     * </p>
     * @return an action
     * @see org.netbeans.spi.project.ui.PrivilegedTemplates
     * @see org.netbeans.spi.project.ui.RecommendedTemplates
     */
    public static Action newFileAction() {
        return Utilities.getActionsFactory().newFileAction();
    }
    
    /**
     * Create an action "Delete Project".
     * It should be invoked with an action context containing
     * one or more {@link org.netbeans.api.project.Project}s.
     * <p class="nonnormative">
     * You might include this in the context menu of a logical view.
     * </p>
     * @since 1.8
     * @return an action
     */
    public static Action deleteProjectAction() {
        return Utilities.getActionsFactory().deleteProjectAction();
    }

    /**
     * Create an action "Copy Project".
     * It should be invoked with an action context containing
     * one or more {@link org.netbeans.api.project.Project}s.
     * <p class="nonnormative">
     * You might include this in the context menu of a logical view.
     * </p>
     * @since 1.10
     * @return an action
     */
    public static Action copyProjectAction() {
        return Utilities.getActionsFactory().copyProjectAction();
    }
    
    /**
     * Create an action "Move Project".
     * It should be invoked with an action context containing
     * one or more {@link org.netbeans.api.project.Project}s.
     * <p class="nonnormative">
     * You might include this in the context menu of a logical view.
     * </p>
     * @since 1.10
     * @return an action
     */
    public static Action moveProjectAction() {
        return Utilities.getActionsFactory().moveProjectAction();
    }
    
    /**
     * Create an action "Rename Project".
     * It should be invoked with an action context containing
     * one or more {@link org.netbeans.api.project.Project}s.
     * <p class="nonnormative">
     * You might include this in the context menu of a logical view.
     * </p>
     * @since 1.10
     * @return an action
     */
    public static Action renameProjectAction() {
        return Utilities.getActionsFactory().renameProjectAction();
    }
    
    /**
     * Creates action that invokes <b>New Project</b> wizard.
     * 
     * <p>{@link #EXISTING_SOURCES_FOLDER} keyed action
     * value can carry {@link org.openide.filesystems.FileObject} that points
     * to existing sources folder. {@link Action#putValue Set this value}
     * if you open the wizard and you know user
     * expectations about initial value for wizard
     * choosers that refers to existing sources location.
     * 
     * @return an action
     *
     * @since org.netbeans.modules.projectuiapi/1 1.3
     */
    public static Action newProjectAction() {
        return Utilities.getActionsFactory().newProjectAction();
    }    

}
