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

package org.netbeans.modules.web.project;

import java.io.File;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.util.Mutex;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.queries.SharabilityQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;

/**
 * SharabilityQueryImplementation for webproject with multiple sources
 */
public class WebSharabilityQuery implements SharabilityQueryImplementation, PropertyChangeListener {

    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final SourceRoots srcRoots;
    private final SourceRoots testRoots;
    private SharabilityQueryImplementation delegate;

    /**
     * Creates new WebSharabilityQuery
     * @param helper AntProjectHelper
     * @param evaluator PropertyEvaluator
     * @param srcRoots sources
     * @param testRoots tests
     */
    WebSharabilityQuery (AntProjectHelper helper, PropertyEvaluator evaluator, SourceRoots srcRoots, SourceRoots testRoots) {
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
                synchronized (WebSharabilityQuery.this) {
                    if (delegate == null) {
                        delegate = createDelegate ();
                    }
                    return Integer.valueOf(delegate.getSharability (file));
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
        String[] props = new String [srcProps.length + testProps.length + 1];
        for (int i=0; i<srcProps.length; i++) {
            props[i] = "${"+srcProps[i]+"}";
        }
        for (int i=0; i<testProps.length; i++) {
            props[srcProps.length+i] = "${"+testProps[i]+"}";
        }
        props[props.length - 1] = "${" + WebProjectProperties.WEB_DOCBASE_DIR + "}";
        return helper.createSharabilityQuery(this.evaluator, props,
            new String[] {
                "${" + WebProjectProperties.DIST_DIR + "}", // NOI18N
                "${" + WebProjectProperties.BUILD_DIR + "}", // NOI18N
            });
    }


}
