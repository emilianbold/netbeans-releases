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

package org.netbeans.modules.mobility.project;
import org.netbeans.spi.mobility.project.support.DefaultPropertyParsers;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.netbeans.spi.mobility.project.ui.customizer.support.VisualPropertySupport;
import org.netbeans.api.project.configurations.ProjectConfiguration;
import org.netbeans.api.project.configurations.ProjectConfigurationsProvider;
import org.netbeans.modules.mobility.project.ui.customizer.J2MEProjectProperties;

import java.util.*;
import java.io.IOException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import org.openide.util.RequestProcessor;

/**
 * @author David Kaspar
 */
public class MIDletsCacheHelper implements AntProjectListener {
    
    final private AntProjectHelper helper;
    final private ProjectConfigurationsHelper confs;    
    final protected ArrayList<MIDletsCacheListener> list = new ArrayList<MIDletsCacheListener>();
    private HashSet<String> set = new HashSet<String>();
    
    public MIDletsCacheHelper(AntProjectHelper helper, ProjectConfigurationsHelper confs) {
        this.helper = helper;
        this.confs = confs;
        refresh();
        helper.addAntProjectListener(this);
        confs.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(final PropertyChangeEvent evt) {
                if (ProjectConfigurationsProvider.PROP_CONFIGURATION_ACTIVE.equals(evt.getPropertyName()))
                    refresh();
            }
            
        });
    }
    
    public synchronized boolean contains(final String midlet) {
        return set.contains(midlet);
    }
    
    public void configurationXmlChanged(@SuppressWarnings("unused")
	final AntProjectEvent ev) {
    }
    
    public void propertiesChanged(@SuppressWarnings("unused")
	final AntProjectEvent ev) {
        refresh();
    }
    
    final public void refresh() {
        refreshCore();
        RequestProcessor.getDefault().post(new Runnable(){
            public void run() {
                MIDletsCacheListener mcl[];
                synchronized (list) {
                    mcl = list.toArray(new MIDletsCacheListener[list.size()]);
                }
                for (int i=0; i<mcl.length; i++) {
                    mcl[i].cacheChanged();
                }
            }
        });
    }
    
    private synchronized void refreshCore() {
        set = new HashSet<String>();
        
        final ProjectConfiguration activeProjectConfiguration = confs.getActiveConfiguration();
        final String activeConfiguration = (activeProjectConfiguration != null   &&  activeProjectConfiguration != confs.getDefaultConfiguration()) ? activeProjectConfiguration.getName() : null;
        String value = null;
        final PropertyEvaluator pe = helper.getStandardPropertyEvaluator();
        if (activeConfiguration != null) {
            final String property = VisualPropertySupport.prefixPropertyName(activeConfiguration, DefaultPropertiesDescriptor.MANIFEST_MIDLETS);
            value = pe.getProperty(property);
        }
        if (value == null)
            value = pe.getProperty(DefaultPropertiesDescriptor.MANIFEST_MIDLETS);
        if (value == null)
            return;
        
        HashMap<String,String> map = (HashMap<String,String>)DefaultPropertyParsers.MANIFEST_PROPERTY_PARSER.decode(value, null, null);
        if (map == null)
            return;
        
        int i = 1;
        Object item;
        while ((item = map.get("MIDlet-" + i)) != null) { // NOI18N
            i ++;
            if (!(item instanceof String))
                continue;
            final String[] strs = ((String) item).split(",", -1); // NOI18N
            if (strs.length < 2)
                continue;
            final String midlet = strs[2].trim();
            if ("".equals(midlet)) // NOI18N
                continue;
            set.add(midlet);
        }
    }
    
    public void addMIDletsCacheListener(final MIDletsCacheListener listener) {
        synchronized (list) {
            list.add(listener);
        }
    }
    
    public void removeMIDletsCacheListener(final MIDletsCacheListener listener) {
        synchronized (list) {
            list.remove(listener);
        }
    }
    
}
