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

package org.netbeans.modules.maven;

import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.spi.project.LookupMerger;
import org.openide.util.Lookup;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * implementation of AuxiliaryProperties.
 * @author mkleint
 */
public class MavenProjectPropsImpl {

    public static final String NAMESPACE = "http://www.netbeans.org/ns/maven-properties-data/1"; //NOI18N
    static String ROOT = "properties"; //NOI18N

    private boolean transaction = false;
    private TreeMap<String, String> transPropsShared;
    private TreeMap<String, String> transPropsPrivate;
    private AuxiliaryConfiguration aux;
    private boolean sharedChanged;
    private final NbMavenProject nbprj;

    MavenProjectPropsImpl(AuxiliaryConfiguration aux, NbMavenProject pr) {
        this.aux = aux;
        nbprj = pr;
    }

    private AuxiliaryConfiguration getAuxConf() {
        return aux;
    }

    public synchronized String get(String key, boolean shared) {
        return get(key, shared, true);
    }

    public synchronized String get(String key, boolean shared, boolean usePom) {
        if (transaction) {
            if (shared && transPropsShared.containsKey(key)) {
                return transPropsShared.get(key);
            }
            if (!shared && transPropsPrivate.containsKey(key)) {
                return transPropsPrivate.get(key);
            }
        } else {
            TreeMap<String, String> props = readProperties(getAuxConf(), shared);
            //TODO optimize
            String ret =  props.get(key);
            if (ret != null) {
                return ret;
            }
        }
        if (shared && usePom) {
            String val = nbprj.getMavenProject().getProperties().getProperty(key);
            if (val != null) {
                return val;
            }
        }
        return null;
    }

    public synchronized void put(String key, String value, boolean shared) {
        if (shared) {
            //TODO put props to project.. shall we actually do it here?
        }
        if (transaction) {
            if (shared) {
                transPropsShared.put(key, value);
                sharedChanged = true;
            } else {
                transPropsPrivate.put(key, value);
            }
        } else {
            writeAuxiliaryData(getAuxConf(), key, value, shared);
        }
    }

    public synchronized Iterable<String> listKeys(boolean shared) {
        TreeMap<String, String> props = readProperties(getAuxConf(), shared);
        if (shared) {
            Properties mvnprops =  nbprj.getMavenProject().getProperties();
            for (Object prop: mvnprops.keySet()) {
                props.put((String)prop, "any"); //NOI18N
            }
        }
        return props.keySet();
    }

    private void writeAuxiliaryData(AuxiliaryConfiguration conf, String property, String value, boolean shared) {
        Element el = getOrCreateRootElement(conf, shared);
        Element enEl;
        NodeList list = el.getElementsByTagNameNS(NAMESPACE, property);
        if (list.getLength() > 0) {
            enEl = (Element)list.item(0);
        } else {
            enEl = el.getOwnerDocument().createElementNS(NAMESPACE, property);
            el.appendChild(enEl);
        }
        if (value != null) {
            enEl.setTextContent(value);
        } else {
            el.removeChild(enEl);
        }
        conf.putConfigurationFragment(el, shared);
    }

    private void writeAuxiliaryData(AuxiliaryConfiguration conf, TreeMap<String, String> props, boolean shared) {
        Element el = getOrCreateRootElement(conf, shared);
        Element enEl;
        for (String key : props.keySet()) {
            NodeList list = el.getElementsByTagNameNS(NAMESPACE, key);
            if (list.getLength() > 0) {
                enEl = (Element)list.item(0);
            } else {
                enEl = el.getOwnerDocument().createElementNS(NAMESPACE, key);
                el.appendChild(enEl);
            }
            String value = props.get(key);
            if (value != null) {
                enEl.setTextContent(value);
            } else {
                el.removeChild(enEl);
            }
        }
        conf.putConfigurationFragment(el, shared);
    }

    private Element getOrCreateRootElement(AuxiliaryConfiguration conf, boolean shared) {
        Element el = conf.getConfigurationFragment(ROOT, NAMESPACE, shared);
        if (el == null) {
            el = XMLUtil.createDocument(ROOT, NAMESPACE, null, null).getDocumentElement();
            if (shared) {
                Comment comment = el.getOwnerDocument().createComment("\nProperties that influence various parts of the IDE, especially code formatting and the like. \n" + //NOI18N
                        "You can copy and paste the single properties, into the pom.xml file and the IDE will pick them up.\n" + //NOI18N
                        "That way multiple projects can share the same settings (useful for formatting rules for example).\n" + //NOI18N
                        "Any value defined here will override the pom.xml file value but is only applicable to the current project.\n"); //NOI18N
                el.appendChild(comment);
            }
        }
        return el;
    }


    private TreeMap<String, String> readProperties(AuxiliaryConfiguration aux, boolean shared) {
        TreeMap<String, String> props = new TreeMap<String, String>();
        Element el = aux.getConfigurationFragment(ROOT, NAMESPACE, shared);
        if (el != null) {
            NodeList list = el.getChildNodes();
            if (list.getLength() > 0) {
                for (int i = 0; i < list.getLength(); i++) {
                    Node nd = list.item(i);
                    if (nd instanceof Element) {
                        Element enEl = (Element)nd;
                        props.put(enEl.getNodeName(), enEl.getTextContent());
                    }
                }
            }
        }
        return props;
    }

    public synchronized TreeMap<String, String> getRawProperties(boolean shared) {
        return readProperties(getAuxConf(), shared);
    }

    public synchronized void startTransaction() {
        transaction = true;
        transPropsShared = getRawProperties(true);
        transPropsPrivate = getRawProperties(false);
        sharedChanged = false;

    }

    public synchronized void commitTransaction() {
        transaction = false;
        if (transPropsShared == null) {
            Logger.getLogger(MavenProjectPropsImpl.class.getName()).info("Commiting a transaction that was cancelled.");
            return;
        }
        if (sharedChanged) {
            writeAuxiliaryData(getAuxConf(), transPropsShared, true);
        }
        writeAuxiliaryData(getAuxConf(), transPropsPrivate, false);
        transPropsPrivate = null;
        transPropsShared = null;
    }

    public synchronized void cancelTransaction() {
        transaction = false;
        transPropsPrivate = null;
        transPropsShared = null;
    }

    static class Merger implements LookupMerger<AuxiliaryProperties> {
        private MavenProjectPropsImpl primary;

        Merger(MavenProjectPropsImpl primary) {
            this.primary = primary;
        }

        public Class<AuxiliaryProperties> getMergeableClass() {
            return AuxiliaryProperties.class;
        }

        public AuxiliaryProperties merge(Lookup lookup) {
            return new MergedAuxProperties(lookup, primary);
        }

    }

    static class MergedAuxProperties implements AuxiliaryProperties {
        Lookup.Result<AuxiliaryProperties> props;
        private MavenProjectPropsImpl primary;
        private MergedAuxProperties(Lookup lookup, MavenProjectPropsImpl primary) {
             props = lookup.lookupResult(AuxiliaryProperties.class);
             this.primary = primary;
        }


        public String get(String key, boolean shared) {
            String toRet = primary.get(key, shared);
            if (toRet == null) {
                for (AuxiliaryProperties prop : props.allInstances()) {
                    toRet = prop.get(key, shared);
                    if (toRet != null) {
                        break;
                    }
                }
            }
            return toRet;
        }

        public void put(String key, String value, boolean shared) {
            primary.put(key, value, shared);
        }

        public Iterable<String> listKeys(boolean shared) {
            Set<String> toRet = new TreeSet<String>();
            Iterator<String> s = primary.listKeys(shared).iterator();
            while (s.hasNext()) {
                toRet.add(s.next());
            }
            for (AuxiliaryProperties aux : props.allInstances()) {
                s = aux.listKeys(shared).iterator();
                while (s.hasNext()) {
                    toRet.add(s.next());
                }
            }
            return toRet;
        }

    }

}
