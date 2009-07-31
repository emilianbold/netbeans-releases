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
import java.util.List;
import org.netbeans.modules.maven.M2AuxilaryConfigImpl;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.ProjectProfileHandler;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.util.lookup.InstanceContent;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * a class that is always present in projects lookup and can be queried 
 * if M2ConfigProvider is enabled or not and sets enable state value as well.
 * @author mkleint
 */
public class ConfigurationProviderEnabler {
    private final NbMavenProjectImpl project;
    static String NAMESPACE = "http://www.netbeans.org/ns/maven-config-data/1"; //NOI18N
    static String ROOT = "config-data"; //NOI18N
    static String ENABLED = "enabled"; //NOI18N
    static String ACTIVATED = "activated"; //NOI18N
    static String CONFIGURATIONS = "configurations"; //NOI18N
    static String CONFIG = "configuration"; //NOI18N
    static String CONFIG_PROFILES_ATTR = "profiles"; //NOI18N
    static String CONFIG_ID_ATTR = "id"; //NOI18N
    
    private Boolean cached;
    private InstanceContent instanceContent;
    private M2ConfigProvider provider;
    private M2AuxilaryConfigImpl aux;

    public ConfigurationProviderEnabler(NbMavenProjectImpl project, M2AuxilaryConfigImpl auxiliary, ProjectProfileHandler hand) {
        this.project = project;
        aux = auxiliary;
        provider = new M2ConfigProvider(project, aux, hand);
    }
    
    public M2ConfigProvider getConfigProvider() {
        return provider;
    }

    public synchronized boolean isConfigurationEnabled() {
        boolean enabled = false;
        if (cached == null) {
            Element el = aux.getConfigurationFragment(ROOT, NAMESPACE, false);
            if (el != null) {
                NodeList list = el.getElementsByTagNameNS(NAMESPACE, ENABLED);
                if (list.getLength() > 0) {
                    Element enEl = (Element)list.item(0);
                    enabled = Boolean.parseBoolean(enEl.getTextContent());
                }
            }
            cached = enabled;
        } else {
            enabled = cached;
        }
        return enabled;
    }
    
    public synchronized void enableConfigurations(boolean enable) {
        if (enable) {
            writeAuxiliaryData(aux, ENABLED, Boolean.toString(enable));
            if (instanceContent != null) {
                instanceContent.add(provider);
            }
        } else {
            aux.removeConfigurationFragment(ROOT, NAMESPACE, false);
            if (instanceContent != null) {
                instanceContent.remove(provider);
            }
        }
        cached = enable;
    }
    
    public synchronized void setInstanceContent(InstanceContent ic) {
        this.instanceContent = ic;
        if (isConfigurationEnabled()) {
            ic.add(provider);
        }
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
    
    static void writeAuxiliaryData(AuxiliaryConfiguration conf, boolean shared, List<M2Configuration> configs) {
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
