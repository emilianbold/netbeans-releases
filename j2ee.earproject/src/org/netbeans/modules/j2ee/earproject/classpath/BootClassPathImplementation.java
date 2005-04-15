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
package org.netbeans.modules.j2ee.earproject.classpath;

import java.beans.PropertyChangeEvent;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.java.classpath.ClassPath;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.util.WeakListeners;

final class BootClassPathImplementation implements ClassPathImplementation, PropertyChangeListener {

    private static final String PLATFORM_ACTIVE = "platform.active";        //NOI18N
    private static final String ANT_NAME = "platform.ant.name";             //NOI18N
    private static final String J2SE = "j2se";                              //NOI18N

    private AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private List resourcesCache;
    private PropertyChangeSupport support = new PropertyChangeSupport(this);

    public BootClassPathImplementation(AntProjectHelper helper, PropertyEvaluator evaluator) {
        assert helper != null;
        this.helper = helper;
        this.evaluator = evaluator;
        evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
    }

    public synchronized List /*<PathResourceImplementation>*/ getResources() {
        if (this.resourcesCache == null) {
            JavaPlatform jp = findActivePlatform ();
            if (jp != null) {
                //TODO: May also listen on CP, but from Platform it should be fixed.
                ClassPath cp = jp.getBootstrapLibraries();
                List entries = cp.entries();
                ArrayList result = new ArrayList (entries.size());
                for (Iterator it = entries.iterator(); it.hasNext();) {
                    ClassPath.Entry entry = (ClassPath.Entry) it.next();
                    result.add (ClassPathSupport.createResource(entry.getURL()));
                }
                resourcesCache = Collections.unmodifiableList (result);
            }
        }
        return this.resourcesCache;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.support.addPropertyChangeListener (listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.support.removePropertyChangeListener (listener);
    }

    private JavaPlatform findActivePlatform () {
        JavaPlatformManager pm = JavaPlatformManager.getDefault();
        String platformName = evaluator.getProperty(PLATFORM_ACTIVE);
        if (platformName!=null) {
            JavaPlatform[] installedPlatforms = pm.getInstalledPlatforms();
            for (int i = 0; i< installedPlatforms.length; i++) {
                Specification spec = installedPlatforms[i].getSpecification();
                String antName = (String) installedPlatforms[i].getProperties().get (ANT_NAME);
                if (J2SE.equalsIgnoreCase(spec.getName())
                    && platformName.equals(antName)) {
                    return installedPlatforms[i];
                }
            }
        }
        //Invalid platform ID or default platform
        return pm.getDefaultPlatform();
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(PLATFORM_ACTIVE)) {
            synchronized (this) {
                resourcesCache = null;
            }
            support.firePropertyChange(PROP_RESOURCES, null, null);
        }
    }
    
}
