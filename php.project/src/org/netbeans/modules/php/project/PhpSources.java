/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.openide.util.ChangeSupport;
import org.openide.util.Mutex;

/**
 * Php Sources class.
 * Is a wrapper for Sources created using 'new SourcesHelper(AntProjectHelper, PropertyEvaluator).createSources()'.
 * Is created to add possibility to reload Sources object stored into Project's lookup.<br>
 * Implements ChangeListener to react on wrapped Sourses.<br>
 * Implements AntProjectListener to react on modified properties file.<br>
 * @author avk
 */
public class PhpSources implements Sources, ChangeListener, PropertyChangeListener {

    public static final String SOURCES_TYPE_PHP = "PHPSOURCE"; // NOI18N

    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final SourceRoots sourceRoots;
    private final SourceRoots testRoots;

    private SourcesHelper sourcesHelper;
    private Sources delegate;
    /**
     * Flag to forbid multiple invocation of {@link SourcesHelper#registerExternalRoots}
     **/
    private volatile boolean externalRootsRegistered;
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    public PhpSources(AntProjectHelper helper, PropertyEvaluator evaluator, final SourceRoots sourceRoots, final SourceRoots testRoots) {
        assert helper != null;
        assert evaluator != null;
        assert sourceRoots != null;
        assert testRoots != null;

        this.helper = helper;
        this.evaluator = evaluator;
        this.sourceRoots = sourceRoots;
        this.testRoots = testRoots;

        this.evaluator.addPropertyChangeListener(this);
        this.sourceRoots.addPropertyChangeListener(this);
        this.testRoots.addPropertyChangeListener(this);
        initSources(); // have to register external build roots eagerly
    }

    public SourceGroup[] getSourceGroups(final String type) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<SourceGroup[]>() {
            public SourceGroup[] run() {
                Sources _delegate;
                synchronized (PhpSources.this) {
                    if (delegate == null) {
                        delegate = initSources();
                        delegate.addChangeListener(PhpSources.this);
                    }
                    _delegate = delegate;
                }
                return _delegate.getSourceGroups(type);
            }
        });
    }

    public void addChangeListener(ChangeListener changeListener) {
        changeSupport.addChangeListener(changeListener);
    }

    public void removeChangeListener(ChangeListener changeListener) {
        changeSupport.removeChangeListener(changeListener);
    }

    private Sources initSources() {
        sourcesHelper = new SourcesHelper(helper, evaluator);   //Safe to pass APH
        register(sourceRoots);
        register(testRoots);

        externalRootsRegistered = false;
        ProjectManager.mutex().postWriteRequest(new Runnable() {
            public void run() {
                if (!externalRootsRegistered) {
                    sourcesHelper.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
                    externalRootsRegistered = true;
                }
            }
        });
        return sourcesHelper.createSources();
    }

    private void register(SourceRoots roots) {
        String[] propNames = roots.getRootProperties();
        String[] rootNames = roots.getRootNames();
        for (int i = 0; i < propNames.length; i++) {
            String prop = propNames[i];
            String displayName = roots.getRootDisplayName(rootNames[i], prop);
            String loc = "${" + prop + "}"; // NOI18N
            sourcesHelper.addPrincipalSourceRoot(loc, displayName, null, null); // NOI18N
            sourcesHelper.addTypedSourceRoot(loc, SOURCES_TYPE_PHP, displayName, null, null);
         }
     }

    private void fireChange() {
        synchronized (this) {
            if (delegate != null) {
                delegate.removeChangeListener(this);
                delegate = null;
            }
        }
        changeSupport.fireChange();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if (SourceRoots.PROP_ROOTS.equals(propName)) {
            fireChange();
        }
    }

    public void stateChanged(ChangeEvent event) {
        fireChange();
    }
}
