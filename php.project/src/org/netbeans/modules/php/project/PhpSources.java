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
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 * Php Sources class.
 * Is a wrapper for Sources created using 'new SourcesHelper(AntProjectHelper, PropertyEvaluator).createSources()'.
 * Is created to add possibility to reload Sources object stored into Project's lookup.<br>
 * Implements ChangeListener to react on wrapped Sourses.<br>
 * Implements AntProjectListener to react on modified properties file.<br>
 * @author avk
 */
public class PhpSources implements Sources, ChangeListener, PropertyChangeListener {

    private final AntProjectHelper myHelper;
    private PropertyEvaluator myEvaluator;

    private SourcesHelper sourcesHelper;
    private Sources delegate;
    /**
     * Flag to forbid multiple invocation of {@link SourcesHelper#registerExternalRoots}
     **/
    private boolean externalRootsRegistered;
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();

    public PhpSources(AntProjectHelper helper, PropertyEvaluator evaluator) {
        this.myHelper = helper;
        this.myEvaluator = evaluator;

        myEvaluator.addPropertyChangeListener(this);
        
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
        synchronized (listeners) {
            listeners.add(changeListener);
        }
    }

    public void removeChangeListener(ChangeListener changeListener) {
        synchronized (listeners) {
            listeners.remove(changeListener);
        }
    }

    private Sources initSources() {
        this.sourcesHelper = new SourcesHelper(myHelper, myEvaluator);
        /*
         * Main source root config.
         */
        String label = NbBundle.getMessage(PhpProject.class, PhpProject.SOURCE_LBL);
        sourcesHelper.addPrincipalSourceRoot(PhpProject.SRC_DIR, label, null, null);

        List<String> labels = new ArrayList<String>();
        List<String> roots = new ArrayList<String>();
        readSources(labels, roots);
        for (int i = 0; i < labels.size(); i++) {
            sourcesHelper.addPrincipalSourceRoot(roots.get(i), labels.get(i), null, null);
            sourcesHelper.addTypedSourceRoot(roots.get(i), PhpProject.SOURCES_TYPE_PHP, labels.get(i), null, null);
        }



        externalRootsRegistered = false;
        ProjectManager.mutex().postWriteRequest(new Runnable() {

            public void run() {
                if (!externalRootsRegistered) {
                    sourcesHelper.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
                    externalRootsRegistered = true;
                }
            }
        });
        return this.sourcesHelper.createSources();
    }

    private void readSources(final List<String> labels, final List<String> roots) {
        ProjectManager.mutex().readAccess(new Mutex.Action<Object>() {

            public Object run() {
                EditableProperties props = myHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                for (Entry<String, String> entry : props.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if (key.equals(PhpProject.SRC_DIR)) {
                        continue;
                    }
                    if (key.startsWith(PhpProject.SRC_) && key.endsWith(PhpProject._DIR)) {
                        String lbl = key.substring(PhpProject.SRC_.length()).substring(0, PhpProject._DIR.length());
                        labels.add(lbl);
                        roots.add(value);
                    }
                    continue;
                }
                return null;
            }
        });
    }

    /*
     * implementation of AntProjectListener.
     * Is not used now because we do not store data in  project xml file
     * (e.g. customizer doesn't update this file)
     */
    public void configurationXmlChanged(AntProjectEvent ev) {
        // PhpSources is not interested in xml changes
    }

    /** impl of PropertyChangeListener.
     * Is used to listen updates in project properties file
     * (e.g. if customizer updates this file)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        String property = evt.getPropertyName();
        if (property.startsWith(PhpProject.SRC_) && property.endsWith(PhpProject._DIR)) {
           this.fireChange();
        }
    }

    /*
     * implementation of ChangetListener.
     * Is used to listen for changes in the real Sources object,
     * wrapped by this one.
     */
    public void stateChanged(ChangeEvent e) {
        this.fireChange();
    }

    private void fireChange() {
        ChangeListener[] _listeners;
        synchronized (this) {
            if (delegate != null) {
                delegate.removeChangeListener(this);
                delegate = null;
            }
        }
        synchronized (listeners) {
            if (listeners.isEmpty()) {
                return;
            }
            _listeners = listeners.toArray(new ChangeListener[listeners.size()]);
        }
        ChangeEvent ev = new ChangeEvent(this);
        for (ChangeListener l : _listeners) {
            l.stateChanged(ev);
        }
    }

}
