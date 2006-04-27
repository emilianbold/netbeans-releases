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

package org.netbeans.bluej;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.support.ant.AntProjectHelper;

/**
 * Factory for simple bluej based projects.
 * @author Milos Kleint
 */
public final class BluejProjectType implements AntBasedProjectType {
    
    public static final String TYPE = "org.netbeans.bluej.bluejproject"; // NOI18N
    private static final String PROJECT_CONFIGURATION_NAME = "data"; // NOI18N
    public static final String PROJECT_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/bluej-project/1"; // NOI18N
    private static final String PRIVATE_CONFIGURATION_NAME = "data"; // NOI18N
    private static final String PRIVATE_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/bluej-project-private/1"; // NOI18N
    
    private List weakList = new ArrayList();
    /** Do nothing, just a service. */
    public BluejProjectType() {}
    
    public String getType() {
        return TYPE;
    }
    
    public Project createProject(AntProjectHelper helper) throws IOException {
        Iterator it = weakList.iterator();
        while (it.hasNext()) {
            WeakReference ref = (WeakReference) it.next();
            Project elem = (Project)ref.get();
            if (elem != null && elem.getProjectDirectory().equals(helper.getProjectDirectory())) {
                return elem;
            }
        }
        Project toReturn =  new BluejProject(helper);
        weakList.add(new WeakReference(toReturn));
        return toReturn;
    }

    public String getPrimaryConfigurationDataElementName(boolean shared) {
        return shared ? PROJECT_CONFIGURATION_NAME : PRIVATE_CONFIGURATION_NAME;
    }
    
    public String getPrimaryConfigurationDataElementNamespace(boolean shared) {
        return shared ? PROJECT_CONFIGURATION_NAMESPACE : PRIVATE_CONFIGURATION_NAMESPACE;
    }
    
}
