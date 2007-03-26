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

package org.netbeans.modules.editor.settings.storage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.WeakListeners;

/**
 *
 * @author Vita Stejskal
 */
public final class ProfilesTracker {
    
    /**
     * The property name for notifying changes in the tracked profiles.
     */
    public static final String PROP_PROFILES = "profiles"; //NOI18N
    
    /**
     * Creates a new instance of ProfilesTracker.
     * 
     * @param type 
     * @param mimeTypes 
     * @param strict 
     */
    public ProfilesTracker(SettingsType type, MimeTypesTracker mimeTypes) {
        assert type != null : "The parameter type must not be null"; //NOI18N
        assert type.isUsingProfiles() : "No need to track profiles for settings that do not use profiles."; //NOI18N
        
        this.locator = type == null ? null : type.getLocator();
        this.mimeTypes = mimeTypes;

        rebuild();

        // Start listening
        this.listener = new Listener();
        this.sfs = Repository.getDefault().getDefaultFileSystem();
        this.sfs.addFileChangeListener(WeakListeners.create(FileChangeListener.class, listener, this.sfs));
        this.mimeTypes.addPropertyChangeListener(listener);
    }
    
    /**
     * Gets the list of profiles for the tracked setting type.
     * 
     * @return Profiles as a map of profile name -&gt; profile display name.
     */
    public Set<String> getProfilesDisplayNames() {
        synchronized (LOCK) {
            return profilesByDisplayName.keySet();
        }
    }

    /**
     * Gets description for a profile by its name.
     * 
     * @param displayName The display name of the profile to get the description for.
     * @retutn The profile's description or <code>null</code> if there is no
     *   profile with the display name.
     */
    public ProfileDescription getProfileByDisplayName(String displayName) {
        synchronized (LOCK) {
            return profilesByDisplayName.get(displayName);
        }
    }
    
    /**
     * Adds a listener that will be receiving <code>PROP_PROFILES</code> notifcations.
     * 
     * @param l The listener to add.
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    /**
     * Removes a previously added listener.
     * 
     * @param l The listener to remove.
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    public static final class ProfileDescription {
        private final String id;
        private final String displayName;
        private final boolean isRollbackAllowed;
        
        private ProfileDescription(String id, String displayName, boolean isRollbackAllowed) {
            this.id = id;
            this.displayName = displayName;
            this.isRollbackAllowed = isRollbackAllowed;
        }
        
        public boolean isRollbackAllowed() {
            return isRollbackAllowed;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getId() {
            return id;
        }

    } // End of ProfileDescription class
    
    // ------------------------------------------------------------------
    // private implementation
    // ------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(ProfilesTracker.class.getName());

    private final SettingsType.Locator locator;
    private final MimeTypesTracker mimeTypes;
    
    private final FileSystem sfs;
    private final Listener listener;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    private final String LOCK = new String("ProfilesTracker.LOCK"); //NOI18N
    private Map<String, ProfileDescription> profiles = Collections.<String, ProfileDescription>emptyMap();
    private Map<String, ProfileDescription> profilesByDisplayName = Collections.<String, ProfileDescription>emptyMap();
    
    private void rebuild() {
        PropertyChangeEvent event = null;
        
        synchronized (LOCK) {
            FileSystem sfs = Repository.getDefault().getDefaultFileSystem();
            Map<String, List<Object []>> scan = new HashMap<String, List<Object []>>();

            FileObject baseFolder = sfs.findResource(mimeTypes.getBasePath());
            if (baseFolder != null && baseFolder.isFolder()) {
                // Scan base folder
                locator.scan(baseFolder, null, null, false, true, true, scan);

                // Scan mime type folders
                Collection<String> mimes = mimeTypes.getMimeTypes();
                for(String mime : mimes) {
                    locator.scan(baseFolder, mime, null, false, true, true, scan);
                }
            }

            HashMap<String, ProfileDescription> newProfiles = new HashMap<String, ProfileDescription>();
            HashMap<String, ProfileDescription> newProfilesByDisplayName = new HashMap<String, ProfileDescription>();
            for(String id : scan.keySet()) {
                List<Object []> profileInfos = scan.get(id);
                
                // Determine profile's display name and if it can roll back user changes
                String displayName  = null;
                boolean canRollback = false;
                for(Object [] info : profileInfos) {
                    FileObject profileHome = (FileObject) info[0];
                    FileObject settingFile = (FileObject) info[1];
                    boolean modulesFile = ((Boolean) info[2]);

                    if (displayName == null && profileHome != null) {
                        // First try the standard way for filesystem annotations
                        displayName = Utils.getLocalizedName(profileHome, null);

                        // Then try the crap way introduced with Tools-Options
                        if (displayName == null) {
                            displayName = Utils.getLocalizedName(profileHome, id, null);
                        }
                    }
                    
                    if (!canRollback) {
                        canRollback = modulesFile;
                    }
                    
                    if (displayName != null && canRollback) {
                        break;
                    }
                }
                displayName = displayName == null ? id : displayName;

                // Check for duplicate display names
                ProfileDescription maybeDupl = newProfilesByDisplayName.get(displayName);
                if (maybeDupl != null) {
                    LOG.warning("Ignoring profile '" + id + "', it's got the same display name as '" + maybeDupl.getId()); //NOI18N
                    continue;
                }
                
                ProfileDescription desc = reuseOrCreate(id, displayName, canRollback);
                newProfiles.put(id, desc);
                newProfilesByDisplayName.put(displayName, desc);
            }

            // Just a sanity check
            assert newProfilesByDisplayName.size() == newProfiles.size() : "Inconsistent profile maps"; //NOI18N
            
            if (!profiles.equals(newProfiles)) {
                event = new PropertyChangeEvent(this, PROP_PROFILES, profiles, newProfiles);
                profiles = newProfiles;
                profilesByDisplayName = newProfilesByDisplayName;
            }
        }
        
        if (event != null) {
            pcs.firePropertyChange(event);
        }
    }

    private ProfileDescription reuseOrCreate(String id, String displayName, boolean rollback) {
        ProfileDescription desc = profiles.get(id);
        if (desc != null) {
            if (desc.getDisplayName().equals(displayName) && desc.isRollbackAllowed() == rollback) {
                return desc;
            }
        }
        return new ProfileDescription(id, displayName, rollback);
    }
    
    private final class Listener extends FileChangeAdapter implements PropertyChangeListener {
        
        public Listener() {
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            rebuild();
        }
        
        @Override
        public void fileDataCreated(FileEvent fe) {
            notifyRebuild(fe.getFile());
        }
        
        @Override
        public void fileFolderCreated(FileEvent fe) {
            notifyRebuild(fe.getFile());
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            notifyRebuild(fe.getFile());
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            notifyRebuild(fe.getFile());
        }
        
        private void notifyRebuild(FileObject file) {
            String path = file.getPath();
            if (path.startsWith(mimeTypes.getBasePath())) {
                rebuild();
            }
        }
    } // End of Listener class
}
