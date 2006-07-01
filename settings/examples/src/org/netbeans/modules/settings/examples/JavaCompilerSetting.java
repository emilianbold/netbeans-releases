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

package org.netbeans.modules.settings.examples;

import java.util.Properties;

/**
 *
 * @author  Jan Pokorsky
 */
public final class JavaCompilerSetting {
    private final static String PROP_DEBUG = "debug"; //NOI18N
    private final static String PROP_DEPRECATION = "deprecation"; //NOI18N
    private final static String PROP_CLASS_PATH = "classPath"; //NOI18N
    private final static String PROP_EXEC_PATH = "path"; //NOI18N


    private java.beans.PropertyChangeSupport propertyChangeSupport =  new java.beans.PropertyChangeSupport(this);
    
    private boolean debug;
    private boolean deprecation;
    private String classpath = ""; //NOI18N
    private String path = ""; //NOI18N
    
    public JavaCompilerSetting() {
    }
    
    /** Adds a PropertyChangeListener to the listener list.
     * @param l The listener to add.
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }
    
    /** Removes a PropertyChangeListener from the listener list.
     * @param l The listener to remove.
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }
    
    private void readProperties(Properties p) {
        this.classpath = p.getProperty(PROP_CLASS_PATH);
        this.path = p.getProperty(PROP_EXEC_PATH);
        String bool = p.getProperty(PROP_DEBUG);
        if (bool != null)
            this.debug = Boolean.valueOf(bool).booleanValue();
        else
            this.debug = false;
                
        bool = p.getProperty(PROP_DEPRECATION);
        if (bool != null)
            this.deprecation = Boolean.valueOf(bool).booleanValue();
        else
            this.deprecation = false;
    }
    
    private void writeProperties(Properties p) {
        p.setProperty(PROP_CLASS_PATH, getClasspath());
        p.setProperty(PROP_EXEC_PATH, getPath());
        p.setProperty(PROP_DEPRECATION, String.valueOf(isDeprecation()));
        p.setProperty(PROP_DEBUG, String.valueOf(isDebug()));
    }
    
    public boolean isDebug() {
        return this.debug;
    }
    public void setDebug(boolean debug) {
        boolean oldDebug = this.debug;
        this.debug = debug;
        propertyChangeSupport.firePropertyChange(PROP_DEBUG, oldDebug, debug);
    }
    public boolean isDeprecation() {
        return this.deprecation;
    }
    public void setDeprecation(boolean deprecation) {
        boolean oldDeprecation = this.deprecation;
        this.deprecation = deprecation;
        propertyChangeSupport.firePropertyChange(PROP_DEPRECATION, oldDeprecation, deprecation);
    }
    public String getClasspath() {
        return this.classpath;
    }
    public void setClasspath(String classpath) {
        String oldClasspath = this.classpath;
        this.classpath = classpath;
        propertyChangeSupport.firePropertyChange(PROP_CLASS_PATH, oldClasspath, classpath);
    }
    public String getPath() {
        return this.path;
    }
    public void setPath(String path) {
        String oldPath = this.path;
        this.path = path;
        propertyChangeSupport.firePropertyChange(PROP_EXEC_PATH, oldPath, path);
    }
    
}
