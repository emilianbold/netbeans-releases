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

package org.netbeans.modules.ant.freeform;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.openide.util.Mutex;
import org.w3c.dom.Element;

/**
 * Handles source dir list for a freeform project.
 * XXX will not correctly unregister released external source roots
 * @author Jesse Glick
 */
final class FreeformSources implements Sources, AntProjectListener {
    
    private final FreeformProject project;
    
    public FreeformSources(FreeformProject project) {
        this.project = project;
        project.helper().addAntProjectListener(this);
        initSources(); // have to register external build roots eagerly
    }
    
    private Sources delegate;
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    
    public SourceGroup[] getSourceGroups(final String type) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<SourceGroup[]>() {
            public SourceGroup[] run() {
                if (delegate == null) {
                    delegate = initSources();
                }
                return delegate.getSourceGroups(type);
            }
        });
    }
    
    private Sources initSources() {
        final SourcesHelper h = new SourcesHelper(project.helper(), project.evaluator());
        Element genldata = project.getPrimaryConfigurationData();
        Element foldersE = Util.findElement(genldata, "folders", FreeformProjectType.NS_GENERAL); // NOI18N
        if (foldersE != null) {
            for (Element folderE : Util.findSubElements(foldersE)) {
                Element locationE = Util.findElement(folderE, "location", FreeformProjectType.NS_GENERAL); // NOI18N
                String location = Util.findText(locationE);
                if (folderE.getLocalName().equals("build-folder")) { // NOI18N
                    h.addNonSourceRoot(location);
                } else {
                    assert folderE.getLocalName().equals("source-folder") : folderE;
                    Element nameE = Util.findElement(folderE, "label", FreeformProjectType.NS_GENERAL); // NOI18N
                    String name = Util.findText(nameE);
                    Element typeE = Util.findElement(folderE, "type", FreeformProjectType.NS_GENERAL); // NOI18N
                    String includes = null;
                    Element includesE = Util.findElement(folderE, "includes", FreeformProjectType.NS_GENERAL); // NOI18N
                    if (includesE != null) {
                        includes = Util.findText(includesE);
                    }
                    String excludes = null;
                    Element excludesE = Util.findElement(folderE, "excludes", FreeformProjectType.NS_GENERAL); // NOI18N
                    if (excludesE != null) {
                        excludes = Util.findText(excludesE);
                    }
                    if (typeE != null) {
                        String type = Util.findText(typeE);
                        h.addTypedSourceRoot(location, includes, excludes, type, name, null, null);
                    } else {
                        h.addPrincipalSourceRoot(location, includes, excludes, name, null, null);
                    }
                }
            }
        }
        ProjectManager.mutex().postWriteRequest(new Runnable() {
            public void run() {
                h.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
            }
        });
        return h.createSources();
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
    
    private void fireChange() {
        ChangeListener[] _listeners;
        synchronized (listeners) {
            delegate = null;
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
    
    public void configurationXmlChanged(AntProjectEvent ev) {
        fireChange();
    }
    
    public void propertiesChanged(AntProjectEvent ev) {
        // ignore
    }
    
}
