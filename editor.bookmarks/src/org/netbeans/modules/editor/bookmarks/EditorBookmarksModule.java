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

package org.netbeans.modules.editor.bookmarks;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.lib.editor.bookmarks.actions.BookmarksKitInstallAction;
import org.openide.modules.ModuleInstall;


/**
 * Module installation class for editor.
 *
 * @author Miloslav Metelka
 */
public class EditorBookmarksModule extends ModuleInstall {

    private PropertyChangeListener openProjectsListener;

    public void restored () {
        synchronized (Settings.class) {
            SettingsUtil.updateListSetting(BaseKit.class,
                    SettingsNames.CUSTOM_ACTION_LIST,
                    new Object[] { BookmarksKitInstallAction.INSTANCE }
            );
            SettingsUtil.updateListSetting(BaseKit.class,
                    SettingsNames.KIT_INSTALL_ACTION_NAME_LIST,
                    new Object[] { BookmarksKitInstallAction.INSTANCE.getValue(Action.NAME) }
            );
            Settings.addInitializer(new BookmarksSettingsInitializer());
        }
        
        // Start listening on project closing
        openProjectsListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                // null, null fired -> thus save all projects' bookmarks
                PersistentBookmarks.saveAllProjectBookmarks();
            }
        };
        OpenProjects.getDefault().addPropertyChangeListener(openProjectsListener);
    }
    
    /**
     * Called when all modules agreed with closing and the IDE will be closed.
     */
    public void close() {
        finish();
    }
    
    /**
     * Called when module is uninstalled.
     */
    public void uninstalled() {
        finish();
    }
    
    private void finish() {
        // Stop listening on projects closing
        OpenProjects.getDefault().removePropertyChangeListener(openProjectsListener);
        
        synchronized (Settings.class) {
            List l = SettingsUtil.getClonedList(BaseKit.class,
                    SettingsNames.CUSTOM_ACTION_LIST);
        }
        
        Settings.removeInitializer(BookmarksSettingsInitializer.NAME);
        Settings.reset();
        
        // Save bookmarks for all opened projects with touched bookmarks
        PersistentBookmarks.saveAllProjectBookmarks();
    }

    private static final class BookmarksSettingsInitializer extends Settings.AbstractInitializer {
        
        static final String NAME = "bookmarks-settings-initializer";
        
        BookmarksSettingsInitializer() {
            super(NAME);
        }

        public void updateSettingsMap(Class kitClass, java.util.Map settingsMap) {
            if (kitClass == BaseKit.class) {
                SettingsUtil.updateListSetting(settingsMap,
                        SettingsNames.CUSTOM_ACTION_LIST,
                        new Object[] { BookmarksKitInstallAction.INSTANCE }
                );
                SettingsUtil.updateListSetting(settingsMap,
                        SettingsNames.KIT_INSTALL_ACTION_NAME_LIST,
                        new Object[] { BookmarksKitInstallAction.INSTANCE.getValue(Action.NAME) }
                );
            }
        }
        
    }
}
