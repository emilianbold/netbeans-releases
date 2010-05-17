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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.bpel.project;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.openide.util.NbPreferences;

/**
 * Settings for the PackageView presentation.
 * According to the value of {@link PackageViewSettings#getPackageViewType}
 * the package view is displayed.
 * Currently there are two modes, the package structure and tree structure.
 * @author Tomas Zezula
 */
public final class PackageViewSettings {

    private static PackageViewSettings INSTANCE = new PackageViewSettings();
    private static final PropertyChangeSupport propChangeSupport = 
            new PropertyChangeSupport(PackageViewSettings.class);
    
    private PackageViewSettings() {
    }
    
    /**
     * The package view should be diplayed as a list of packages
     */
    public static final int TYPE_PACKAGE_VIEW = 0;
    
    /**
     * The package view should be diplayed as a tree of folders
     */
    public static final int TYPE_TREE = 1;
    
    public static final String PROP_PACKAGE_VIEW_TYPE = "packageViewType"; //NOI18N
    
    /**
     * Returns how the package view should be displayed.
     * @return {@link PackageViewSettings#TYPE_PACKAGE_VIEW} or
     * {@link PackageViewSettings#TYPE_TREE}
     *
     */
    public int getPackageViewType () {
        return NbPreferences.forModule(PackageViewSettings.class)
                    .getInt(PROP_PACKAGE_VIEW_TYPE, TYPE_PACKAGE_VIEW);
    }
    
    /**
     * Sets how the package view should be displayed.
     * @param type either {@link PackageViewSettings#TYPE_PACKAGE_VIEW} or
     * {@link PackageViewSettings#TYPE_TREE}
     *
     */
    public void setPackageViewType (int type) {
        int currentType = getPackageViewType();
        
        if (currentType != type) {
            NbPreferences.forModule(PackageViewSettings.class)
                .putInt(PROP_PACKAGE_VIEW_TYPE, type);
            propChangeSupport.firePropertyChange(
                    PROP_PACKAGE_VIEW_TYPE, currentType, type);
        }
    }
    
    /**
     * Returns an instance of the PackageViewSettings
     * @return PackageViewSettings
     */
    public static PackageViewSettings getDefault () {
        return INSTANCE;
    }
    
    public static void addPropertyChangeListener(PropertyChangeListener l) {
        propChangeSupport.addPropertyChangeListener(l);
    }

    public static void removePropertyChangeListener(PropertyChangeListener l) {
        propChangeSupport.removePropertyChangeListener(l);
    }
}
