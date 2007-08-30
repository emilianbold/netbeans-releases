/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.makeproject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.SourcesHelper;

/**
 * Handles source dir list for a freeform project.
 * XXX will not correctly unregister released external source roots
 */
public class MakeSources implements Sources, AntProjectListener {
    
    private MakeProject project;
    private AntProjectHelper helper;
    
    public MakeSources(MakeProject project, AntProjectHelper helper) {
        this.project = project;
        this.helper = helper;
        helper.addAntProjectListener(this);
    }
    
    private Sources delegate;
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    
    public synchronized SourceGroup[] getSourceGroups(String str) {
        if (!str.equals("generic")) { // NOI18N
            return new SourceGroup[0];
        }
        if (delegate == null) {
            delegate = initSources();
        }
	SourceGroup[] sg = delegate.getSourceGroups(str);
        return sg;
    }
    
    private Sources initSources() {
        final SourcesHelper h = new SourcesHelper(helper, project.evaluator());
	ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
	ConfigurationDescriptor pd = pdp.getConfigurationDescriptor();
	if (pd != null) {
	    MakeConfigurationDescriptor epd = (MakeConfigurationDescriptor)pd;
            Item[] projectItems = epd.getProjectItems();
	    if (projectItems != null) {
                Set<String> set = new HashSet<String>();
		for (int i = 0; i < projectItems.length; i++) {
		    Item item = projectItems[i];
		    String name = item.getPath();
		    if (!IpeUtils.isPathAbsolute(name))
			continue;
		    File file = new File(name);
		    if (!file.exists())
			continue;
		    if (!file.isDirectory())
			file = file.getParentFile();
		    name = file.getPath();
                    set.add(name);
                }
                for(String name : set) {
		    String displayName = name;
		    int index1 = displayName.lastIndexOf(File.separatorChar);
		    if (index1 > 0) {
			int index2 = displayName.substring(0, index1).lastIndexOf(File.separatorChar);
			if (index2 > 0)
			    displayName = "..." + displayName.substring(index2); // NOI18N
		    }
		    h.addPrincipalSourceRoot(name, displayName, null, null);
		    h.addTypedSourceRoot(name, "generic", displayName, null, null); // NOI18N
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
    
    public synchronized void addChangeListener(ChangeListener changeListener) {
        listeners.add(changeListener);
    }
    
    public synchronized void removeChangeListener(ChangeListener changeListener) {
        listeners.remove(changeListener);
    }
    
    private void fireChange() {
        ChangeListener[] _listeners;
        synchronized (this) {
            delegate = null;
            if (listeners.isEmpty()) {
                return;
            }
            _listeners = listeners.toArray(new ChangeListener[listeners.size()]);
        }
        ChangeEvent ev = new ChangeEvent(this);
        for (int i = 0; i < _listeners.length; i++) {
            _listeners[i].stateChanged(ev);
        }
    }
    
    public void configurationXmlChanged(AntProjectEvent ev) {
        fireChange();
    }
    
    public void descriptorChanged() {
        fireChange();
    }
    
    public void propertiesChanged(AntProjectEvent ev) {
        // ignore
    }
    
}
