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
package org.netbeans.modules.java.j2seproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.filesystems.FileObject;
import org.netbeans.api.queries.FileBuiltQuery;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.modules.java.j2seproject.ui.customizer.J2SEProjectProperties;





public class J2SEFileBuiltQuery implements FileBuiltQueryImplementation, PropertyChangeListener {

    private FileBuiltQueryImplementation delegate;
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final SourceRoots sourceRoots;
    private final SourceRoots testRoots;

    J2SEFileBuiltQuery (AntProjectHelper helper, PropertyEvaluator evaluator,
                        SourceRoots sourceRoots, SourceRoots testRoots) {
        this.helper = helper;
        this.evaluator = evaluator;
        this.sourceRoots = sourceRoots;
        this.testRoots = testRoots;
    }

    public synchronized FileBuiltQuery.Status getStatus(FileObject file) {
        if (this.delegate == null) {
            this.delegate = createDelegate ();
        }
        return this.delegate.getStatus (file);
    }


    private FileBuiltQueryImplementation createDelegate () {
        String[] srcRoots = this.sourceRoots.getRootProperties();
        String[] tstRoots = this.testRoots.getRootProperties();
        String[] from = new String [srcRoots.length + tstRoots.length];
        String[] to = new String [srcRoots.length + tstRoots.length];
        for (int i=0; i< srcRoots.length; i++) {
            from[i] = "${" + srcRoots[i] + "}/*.java"; // NOI18N
            to[i] = "${" + J2SEProjectProperties.BUILD_CLASSES_DIR + "}/*.class"; // NOI18N
        }
        for (int i=0; i<tstRoots.length; i++) {
            from[srcRoots.length+i] = "${" + tstRoots[i] + "}/*.java"; // NOI18N
            to[srcRoots.length+i] = "${" + J2SEProjectProperties.BUILD_TEST_CLASSES_DIR + "}/*.class"; // NOI18N
        }
        return helper.createGlobFileBuiltQuery(evaluator, from, to);    //Safe to pass APH
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (SourceRoots.PROP_ROOT_PROPERTIES.equals (evt.getPropertyName())) {
            synchronized(this) {
                this.delegate = null;
                ///XXX: What to do with already returned Statuses
            }
        }
    }
}
