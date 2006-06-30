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
            System.getProperties().put("jdk.home",System.getProperty("java.home"));     //NOI18N
            this.defaultPlatform = DefaultPlatformImpl.create (null,null,null);
        }
        return defaultPlatform;
    }
    
    public JavaPlatform getDefaultPlatform() {
        return createDefaultPlatform ();
    }    
    
    
}
