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

package org.netbeans.modules.j2ee.ejbjarproject;

import java.io.File;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.util.Mutex;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.queries.SharabilityQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.EjbJarProjectProperties;

/**
 * SharabilityQueryImplementation for j2seproject with multiple sources
 */
public class EjbJarSharabilityQuery implements SharabilityQueryImplementation, PropertyChangeListener {

    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final SourceRoots srcRoots;
    private final SourceRoots testRoots;
    private SharabilityQueryImplementation delegate;

    /**
     * Creates new J2SESharabilityQuery
     * @param helper AntProjectHelper
     * @param evaluator PropertyEvaluator
     * @param srcRoots sources
     * @param testRoots tests
     */
    EjbJarSharabilityQuery (AntProjectHelper helper, PropertyEvaluator evaluator, SourceRoots srcRoots, SourceRoots testRoots) {
        this.helper = helper;
        this.evaluator = evaluator;
        this.srcRoots = srcRoots;
        this.testRoots = testRoots;
        this.srcRoots.addPropertyChangeListener(this);
        this.testRoots.addPropertyChangeListener(this);
    }

    /**
     * Check whether a file or directory should be shared.
     * If it is, it ought to be committed to a VCS if the user is using one.
     * If it is not, it is either a disposable build product, or a per-user
     * private file which is important but should not be shared.
     * @param file a file to check for sharability (may or may not yet exist)
     * @return one of {@link org.netbeans.api.queries.SharabilityQuery}'s constants
     */
    public int getSharability(final File file) {
        Integer ret = (Integer) ProjectManager.mutex().readAccess( new Mutex.Action() {
            public Object run() {
                synchronized (EjbJarSharabilityQuery.this) {
                    if (delegate == null) {
                        delegate = createDelegate ();
                    }
                    return new Integer(delegate.getSharability (file));
                }
            }
        });
        return ret.intValue();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (SourceRoots.PROP_ROOT_PROPERTIES.equals(evt.getPropertyName())) {
            synchronized (this) {
                this.delegate = null;
            }
        }
    }

    private SharabilityQueryImplementation createDelegate () {
        String[] srcProps = srcRoots.getRootProperties();
        String[] testProps = testRoots.getRootProperties();
        String[] props = new String [srcProps.length + testProps.length];
        for (int i=0; i<srcProps.length; i++) {
            props[i] = "${"+srcProps[i]+"}"; // NOI18N
        }
        for (int i=0; i<testProps.length; i++) {
            props[srcProps.length+i] = "${"+testProps[i]+"}"; // NOI18N
        }
        return helper.createSharabilityQuery(this.evaluator, props,
            new String[] {
                "${" + EjbJarProjectProperties.DIST_DIR + "}", // NOI18N
                "${" + EjbJarProjectProperties.BUILD_DIR + "}", // NOI18N
            });
    }
}
