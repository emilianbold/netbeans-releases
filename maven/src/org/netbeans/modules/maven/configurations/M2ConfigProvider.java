/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.configurations;

import hidden.org.codehaus.plexus.util.StringUtils;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.ProjectProfileHandler;
import org.netbeans.modules.maven.api.customizer.ModelHandle;
import org.netbeans.modules.maven.customizer.CustomizerProviderImpl;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * WARNING: this class shall in no way use project.getLookup() as it's called
 * in the critical loop (getOriginalMavenproject
 * @author mkleint
 */
public class M2ConfigProvider implements ProjectConfigurationProvider<M2Configuration> {

    private PropertyChangeSupport support = new PropertyChangeSupport(this);
    private NbMavenProjectImpl project;
    private List<M2Configuration> profiles = null;
    private List<M2Configuration> shared = null;
    private List<M2Configuration> nonshared = null;
    private final M2Configuration DEFAULT;
    private M2Configuration active;
    private String initialActive;
    private AuxiliaryConfiguration aux;
    private ProjectProfileHandler profileHandler;
    private PropertyChangeListener propertyChange;

    static String NAMESPACE = "http://www.netbeans.org/ns/maven-config-data/1"; //NOI18N
    static String ROOT = "config-data"; //NOI18N
    static String ENABLED = "enabled"; //NOI18N
    static String ACTIVATED = "activated"; //NOI18N
    static String CONFIGURATIONS = "configurations"; //NOI18N
    static String CONFIG = "configuration"; //NOI18N
    static String CONFIG_PROFILES_ATTR = "profiles"; //NOI18N
    static String CONFIG_ID_ATTR = "id"; //NOI18N

    
    public M2ConfigProvider(NbMavenProjectImpl proj, AuxiliaryConfiguration aux, ProjectProfileHandler prof) {
        project = proj;
        this.aux = aux;
        profileHandler = prof;
        DEFAULT = M2Configuration.createDefault(project);
        //read the active one..
        Element el = aux.getConfigurationFragment(ROOT, NAMESPACE, false);
        if (el != null) {
            NodeList list = el.getElementsByTagNameNS(NAMESPACE, ACTIVATED);
            if (list.getLength() > 0) {
                Element enEl = (Element)list.item(0);
                initialActive = new String(enEl.getTextContent());
            }
        }
        
        active = DEFAULT;
        propertyChange = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (NbMavenProjectImpl.PROP_PROJECT.equals(evt.getPropertyName())) {
                    synchronized (M2ConfigProvider.this) {
                        profiles = null;
                    }
                    RequestProcessor.getDefault().post(new Runnable() {
                        public void run() {
                            checkActiveAgainstAll(getConfigurations(), false);
                            firePropertyChange();
                        }

                    });
                }
            }
        };
    }

    private void checkActiveAgainstAll(Collection<M2Configuration> confs, boolean async) {
        boolean found = false;
        for (M2Configuration conf : confs) {
            if (conf.getId().equals(active.getId())) {
                found = true;
                break;
            }
        }
        if (!found) {
            Runnable dothis = new Runnable() {
                    public void run() {
                        try {
                            doSetActiveConfiguration(DEFAULT, active);
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                };
            if (async) {
                RequestProcessor.getDefault().post(dothis);
            } else {
                dothis.run();
            }
        }
    }
    
    private synchronized Collection<M2Configuration> getConfigurations(boolean skipProfiles) {
        if (profiles == null && !skipProfiles) {
            profiles = createProfilesList();
        }
        if (shared == null) {
            //read from auxconf
            shared = readConfiguration(true);
        }
        if (nonshared == null) {
            //read from auxconf
            nonshared = readConfiguration(false);
        }
        ArrayList<M2Configuration> toRet = new ArrayList<M2Configuration>();
        toRet.add(DEFAULT);
        toRet.addAll(shared);
        toRet.addAll(nonshared);
        if (!skipProfiles) {
            toRet.addAll(profiles);
        }
        return toRet;
        
    }
    
    public synchronized Collection<M2Configuration> getConfigurations() {
        return getConfigurations(false);
    }

    public M2Configuration getDefaultConfig() {
        return DEFAULT;
    }
    
    public synchronized Collection<M2Configuration> getProfileConfigurations() {
        getConfigurations();
        return profiles;
    }
    
    public synchronized Collection<M2Configuration> getSharedConfigurations() {
        getConfigurations();
        return shared;
    }
    
    public synchronized Collection<M2Configuration> getNonSharedConfigurations() {
        getConfigurations();
        return nonshared;
    }
    
    public boolean hasCustomizer() {
        return true;
    }

    public void customize() {
        CustomizerProviderImpl prv = project.getLookup().lookup(CustomizerProviderImpl.class);
        prv.showCustomizer(ModelHandle.PANEL_CONFIGURATION);
    }

    public boolean configurationsAffectAction(String action) {
        if (ActionProvider.COMMAND_DELETE.equals(action) || ActionProvider.COMMAND_COPY.equals(action) || ActionProvider.COMMAND_MOVE.equals(action)) {
            return false;
        }
        return true;
    }


    public synchronized M2Configuration getActiveConfiguration() {
        Collection<M2Configuration> confs = getConfigurations(false);
        if (initialActive != null) {
            for (M2Configuration conf : confs) {
                if (initialActive.equals(conf.getId())) {
                    active = conf;
                    initialActive = null;
                    break;
                }
            }
            if (initialActive != null) {
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        try {
                            doSetActiveConfiguration(DEFAULT, null);
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
                initialActive = null;
            }
        }
        checkActiveAgainstAll(confs, true);
        return active;
    }



    
    public synchronized void setConfigurations(List<M2Configuration> shared, List<M2Configuration> nonshared, boolean includeProfiles) {
        writeAuxiliaryData(aux, true, shared);
        writeAuxiliaryData(aux, false, nonshared);
        this.shared = shared;
        this.nonshared = nonshared;
        this.profiles = null;
        firePropertyChange();
    }

    public synchronized void setActiveConfiguration(M2Configuration configuration) throws IllegalArgumentException, IOException {
        if (active == configuration || (active != null && active.equals(configuration))) {
            return;
        }
        doSetActiveConfiguration(configuration, active);
        NbMavenProject.fireMavenProjectReload(project);
    }

    private synchronized void doSetActiveConfiguration(M2Configuration newone, M2Configuration old) throws IllegalArgumentException, IOException {
        System.out.println("set new one..");
        active = newone;
        writeAuxiliaryData(
                aux,
                ACTIVATED, active.getId());
        support.firePropertyChange(PROP_CONFIGURATION_ACTIVE, old, active);
    }

    private List<M2Configuration> createProfilesList() {
        List<String> profs = profileHandler.getAllProfiles();
        List<M2Configuration> config = new ArrayList<M2Configuration>();
//        config.add(DEFAULT);
        for (String prof : profs) {
            M2Configuration c = new M2Configuration(prof, project);
            c.setActivatedProfiles(Collections.singletonList(prof));
            config.add(c);
        }
        return config;
    }

    public synchronized void addPropertyChangeListener(PropertyChangeListener lst) {
        if (support.getPropertyChangeListeners().length == 0) {
            project.getProjectWatcher().addPropertyChangeListener(propertyChange);
        }
        support.addPropertyChangeListener(lst);

    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener lst) {
        support.removePropertyChangeListener(lst);
        if (support.getPropertyChangeListeners().length == 0) {
            project.getProjectWatcher().removePropertyChangeListener(propertyChange);
        }
    }


    private void firePropertyChange() {
        support.firePropertyChange(ProjectConfigurationProvider.PROP_CONFIGURATIONS, null, null);
    }
    
    private List<M2Configuration> readConfiguration(boolean shared) {
        Element el = aux.getConfigurationFragment(ROOT, NAMESPACE, shared);
        if (el != null) {
            NodeList list = el.getElementsByTagNameNS(NAMESPACE, CONFIG);
            if (list.getLength() > 0) {
                List<M2Configuration> toRet = new ArrayList<M2Configuration>();
                int len = list.getLength();
                for (int i = 0; i < len; i++) {
                    Element enEl = (Element)list.item(i);
                    
                    M2Configuration c = new M2Configuration(enEl.getAttribute(CONFIG_ID_ATTR), project);
                    String profs = enEl.getAttribute(CONFIG_PROFILES_ATTR);
                    if (profs != null) {
                        String[] s = profs.split(" ");
                        List<String> prf = new ArrayList<String>();
                        for (String s2 : s) {
                            if (s2.trim().length() > 0) {
                                prf.add(s2.trim());
                            }
                        }
                        c.setActivatedProfiles(prf);
                    }
                    toRet.add(c);
                }
                return toRet;
            }
        }
        return new ArrayList<M2Configuration>();
    }

    public static void writeAuxiliaryData(AuxiliaryConfiguration conf, String property, String value) {
        Element el = conf.getConfigurationFragment(ROOT, NAMESPACE, false);
        if (el == null) {
            el = XMLUtil.createDocument(ROOT, NAMESPACE, null, null).getDocumentElement();
        }
        Element enEl;
        NodeList list = el.getElementsByTagNameNS(NAMESPACE, property);
        if (list.getLength() > 0) {
            enEl = (Element)list.item(0);
        } else {
            enEl = el.getOwnerDocument().createElementNS(NAMESPACE, property);
            el.appendChild(enEl);
        }
        enEl.setTextContent(value);
        conf.putConfigurationFragment(el, false);
    }

    private static void writeAuxiliaryData(AuxiliaryConfiguration conf, boolean shared, List<M2Configuration> configs) {
        Element el = conf.getConfigurationFragment(ROOT, NAMESPACE, shared);
        if (el == null) {
            el = XMLUtil.createDocument(ROOT, NAMESPACE, null, null).getDocumentElement();
        }
        Element enEl;
        NodeList list = el.getElementsByTagNameNS(NAMESPACE, CONFIGURATIONS);
        if (list.getLength() > 0) {
            enEl = (Element)list.item(0);
            NodeList nl = enEl.getChildNodes();
            int len = nl.getLength();
            for (int i = 0; i < len; i++) {
                enEl.removeChild(nl.item(0));
            }
        } else {
            enEl = el.getOwnerDocument().createElementNS(NAMESPACE, CONFIGURATIONS);
            el.appendChild(enEl);
        }
        for (M2Configuration config : configs) {
            Element child  = enEl.getOwnerDocument().createElementNS(NAMESPACE, CONFIG);
            child.setAttribute(CONFIG_ID_ATTR, config.getId());
            child.setAttribute(CONFIG_PROFILES_ATTR, StringUtils.join(config.getActivatedProfiles().iterator(), " "));
            enEl.appendChild(child);
        }
        conf.putConfigurationFragment(el, shared);
    }

}
