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

package org.netbeans.modules.java.j2seplatform.platformdefinition;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.ArrayList;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.java.platform.JavaPlatformProvider;

/**
 *
 * @author  tom
 */
public class JavaPlatformProviderImpl implements JavaPlatformProvider {
    
    
    private PropertyChangeSupport support;
    private List platforms;
    private JavaPlatform defaultPlatform;
    
    /** Creates a new instance of JavaPlatformProviderImpl */
    public JavaPlatformProviderImpl() {
        this.support = new PropertyChangeSupport (this);
        this.platforms = new ArrayList ();
        this.addPlatform (this.createDefaultPlatform());
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.support.addPropertyChangeListener(listener);
    }    
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.support.removePropertyChangeListener (listener);
    }    
    
    public void addPlatform (JavaPlatform platform) {
        this.platforms.add (platform);
    }
    
    public void removePlatform (JavaPlatform platform) {
        this.platforms.add (platform);        
    }
        
    public JavaPlatform[] getInstalledPlatforms() {
        return (JavaPlatform[]) this.platforms.toArray(new JavaPlatform[this.platforms.size()]);
    }    
    
    private synchronized JavaPlatform createDefaultPlatform () {
        if (this.defaultPlatform == null) {
            this.defaultPlatform = DefaultPlatformImpl.create (null,null);
        }
        return defaultPlatform;
    }
    
    public JavaPlatform getDefaultPlatform() {
        return createDefaultPlatform ();
    }    
    
    
}
