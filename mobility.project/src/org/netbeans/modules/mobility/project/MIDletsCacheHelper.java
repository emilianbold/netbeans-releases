/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.mobility.project;
import org.netbeans.spi.mobility.project.support.DefaultPropertyParsers;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.netbeans.spi.mobility.project.ui.customizer.support.VisualPropertySupport;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.modules.mobility.project.ui.customizer.J2MEProjectProperties;

import java.util.*;
import java.io.IOException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import org.netbeans.api.project.ProjectManager;
import org.openide.util.Mutex;
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
                if (ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE.equals(evt.getPropertyName()))
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
    
    private void refreshCore() {        
        ProjectManager.mutex().readAccess(new Mutex.Action() {
            public Object run() {
                final ProjectConfiguration activeProjectConfiguration = confs.getActiveConfiguration();
                final String activeConfiguration = (activeProjectConfiguration != null   &&  activeProjectConfiguration != confs.getDefaultConfiguration()) ? activeProjectConfiguration.getDisplayName() : null;        
                set = new HashSet<String>();
                String value = null;
                final PropertyEvaluator pe = helper.getStandardPropertyEvaluator();
                if (activeConfiguration != null) {
                    final String property = VisualPropertySupport.prefixPropertyName(activeConfiguration, DefaultPropertiesDescriptor.MANIFEST_MIDLETS);
                    value = pe.getProperty(property);
                }
                if (value == null) {
                    value = pe.getProperty(DefaultPropertiesDescriptor.MANIFEST_MIDLETS);
                }
                if (value == null) {
                    return null;
                }
                HashMap<String, String> map = (HashMap<String, String>) DefaultPropertyParsers.MANIFEST_PROPERTY_PARSER.decode(value, null, null);
                if (map == null) {
                    return null;
                }
                int i = 1;
                Object item;
                while ((item = map.get("MIDlet-" + i)) != null) { // NOI18N

                    i++;
                    if (!(item instanceof String)) {
                        continue;
                    }
                    final String[] strs = ((String) item).split(",", -1); // NOI18N

                    if (strs.length < 2) {
                        continue;
                    }
                    final String midlet = strs[2].trim();
                    if ("".equals(midlet)) // NOI18N
                    {
                        continue;
                    }
                    set.add(midlet);
                }
                return null;
            }
        });
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
