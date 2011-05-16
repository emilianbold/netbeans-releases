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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.j2ee.weblogic9.config;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination.Type;
import org.netbeans.modules.j2ee.weblogic9.dd.model.MessageModel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Petr Hejl
 */
public class WLMessageDestinationSupport {

    private static final String JMS_FILE = "-jms.xml"; // NOI18N

    private static final String NAME_PATTERN = "message-"; // NOI18N

    private static final FileFilter JMS_FILE_FILTER = new FileFilter() {

        @Override
        public boolean accept(File pathname) {
            return !pathname.isDirectory() && pathname.getName().endsWith(JMS_FILE);
        }
    };

    private static final Logger LOGGER = Logger.getLogger(WLMessageDestinationSupport.class.getName());

    private File resourceDir;

    public WLMessageDestinationSupport(File resourceDir) {
        assert resourceDir != null : "Resource directory can't be null"; // NOI18N
        this.resourceDir = FileUtil.normalizeFile(resourceDir);
    }

    static Set<WLMessageDestination> getMessageDestinations(File domain,
            FileObject inputFile, boolean systemDefault) throws ConfigurationException {
        if (inputFile == null || !inputFile.isValid() || !inputFile.canRead()) {
            if (LOGGER.isLoggable(Level.INFO) && inputFile != null) {
                LOGGER.log(Level.INFO, NbBundle.getMessage(WLMessageDestinationSupport.class, "ERR_WRONG_CONFIG_DIR", inputFile));
            }
            return Collections.emptySet();
        }
        // domain config
        if (inputFile.isData() && inputFile.hasExt("xml")) {
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser = factory.newSAXParser();
                JmsHandler handler = new JmsHandler(domain);
                parser.parse(new BufferedInputStream(inputFile.getInputStream()), handler);

                Map<File, Boolean> confs = new HashMap<File, Boolean>();
                Set<String> nameOnly = new HashSet<String>();

                // load by path in config.xml
                for (JmsResource resource : handler.getResources()) {
                    // FIXME check target
                    if (resource.getFile() != null) {
                        File config = resource.resolveFile();
                        if (config != null) {
                            confs.put(config, resource.isSystem());
                        }
                    } else if (resource.getName() != null && resource.isSystem()) {
                        nameOnly.add(resource.getName());
                    }
                }

                Set<WLMessageDestination> result = new HashSet<WLMessageDestination>();
                result.addAll(getMessageDestinations(confs));

                // load those in config/jms by name
                if (!nameOnly.isEmpty()) {
                    Set<WLMessageDestination> configMessageDestinations =
                            getMessageDestinations(domain, inputFile.getParent().getFileObject("jms"), true); // NOI18N
                    for (WLMessageDestination ds : configMessageDestinations) {
                        if (nameOnly.contains(ds.getName())) {
                            result.add(ds);
                        }
                    }
                }

                return result;
            } catch (IOException ex) {
                return Collections.emptySet();
            } catch (ParserConfigurationException ex) {
                return Collections.emptySet();
            } catch (SAXException ex) {
                return Collections.emptySet();
            }
        // directory project
        } else if (inputFile.isFolder()) {
            File file = FileUtil.toFile(inputFile);
            Map<File, Boolean> confs = new HashMap<File, Boolean>();
            for (File jdbcFile : file.listFiles(JMS_FILE_FILTER)) {
                confs.put(jdbcFile, systemDefault);
            }

            if (confs.isEmpty()) { // nowhere to search
                return Collections.emptySet();
            }

            return getMessageDestinations(confs);
        }
        return Collections.emptySet();
    }

    private static Set<WLMessageDestination> getMessageDestinations(Map<File, Boolean> confs) throws ConfigurationException {
        Set<WLMessageDestination> messageDestinations = new HashSet<WLMessageDestination>();

        for (Map.Entry<File, Boolean> entry : confs.entrySet()) {
            File jmsFile = entry.getKey();
            try {
                MessageModel messageModel = null;
                try {
                    messageModel = MessageModel.forFile(jmsFile);
                } catch (RuntimeException re) {
                    String msg = NbBundle.getMessage(WLMessageDestinationSupport.class, "MSG_NotParseableMessages", jmsFile.getAbsolutePath());
                    LOGGER.log(Level.INFO, msg);
                    continue;
                }

                for (String name : messageModel.getQueues()) {
                    messageDestinations.add(new WLMessageDestination(
                            name, Type.QUEUE, jmsFile, entry.getValue()));
                }
                for (String name : messageModel.getTopics()) {
                    messageDestinations.add(new WLMessageDestination(
                            name, Type.TOPIC, jmsFile, entry.getValue()));
                }
            } catch (IOException ioe) {
                String msg = NbBundle.getMessage(WLMessageDestinationSupport.class, "MSG_CannotReadMessages", jmsFile.getAbsolutePath());
                LOGGER.log(Level.FINE, null, ioe);
                throw new ConfigurationException(msg, ioe);
            } catch (RuntimeException re) {
                String msg = NbBundle.getMessage(WLMessageDestinationSupport.class, "MSG_NotParseableMessages", jmsFile.getAbsolutePath());
                LOGGER.log(Level.FINE, null, re);
                throw new ConfigurationException(msg, re);
            }
        }

        return messageDestinations;
    }

    public Set<WLMessageDestination> getMessageDestinations() throws ConfigurationException {
        FileObject resource = FileUtil.toFileObject(resourceDir);

        return getMessageDestinations(null, resource, false);
    }

    private static class JmsSystemResourceHandler extends DefaultHandler {

        private final List<JmsResource> resources = new ArrayList<JmsResource>();

        private final File configDir;

        private JmsResource resource;

        private String value;

        public JmsSystemResourceHandler(File configDir) {
            this.configDir = configDir;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            value = null;
            if ("jms-system-resource".equals(qName)) { // NOI18N
                resource = new JmsResource(configDir, true);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (resource == null) {
                return;
            }

            if ("jms-system-resource".equals(qName)) { // NOI18N
                resources.add(resource);
                resource = null; 
            } else if("name".equals(qName)) { // NOI18N
                resource.setName(value);
            } else if ("taget".equals(qName)) { // NOI18N
                resource.setTarget(value);
            } else if ("descriptor-file-name".equals(qName)) { // NOI18N
                resource.setFile(value);
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            value = new String(ch, start, length);
        }

        public List<JmsResource> getResources() {
            return resources;
        }
    }

    private static class JmsApplicationHandler extends DefaultHandler {

        private final List<JmsResource> resources = new ArrayList<JmsResource>();

        private final File domainDir;

        private JmsResource resource;

        private String value;

        private boolean isJms;

        public JmsApplicationHandler(File domainDir) {
            this.domainDir = domainDir;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            value = null;
            if ("app-deployment".equals(qName)) { // NOI18N
                resource = new JmsResource(domainDir, false);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (resource == null) {
                return;
            }

            if ("app-deployment".equals(qName)) { // NOI18N
                if (isJms) {
                    resources.add(resource);
                }
                isJms = false;
                resource = null;
            } else if("name".equals(qName)) { // NOI18N
                resource.setName(value);
            } else if ("taget".equals(qName)) { // NOI18N
                resource.setTarget(value);
            } else if ("source-path".equals(qName)) { // NOI18N
                resource.setFile(value);
            } else if ("module-type".equals(qName)) { // NOI18N
                if ("jms".equals(value)) {
                    isJms = true;
                }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            value = new String(ch, start, length);
        }

        public List<JmsResource> getResources() {
            return resources;
        }
    }

    private static class JmsHandler extends DefaultHandler {

        private final JmsSystemResourceHandler system;

        private final JmsApplicationHandler application;

        public JmsHandler(File domainDir) {
            File configDir = domainDir != null ? new File(domainDir, "config") : null;
            system = new JmsSystemResourceHandler(configDir);
            application = new JmsApplicationHandler(domainDir);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            system.startElement(uri, localName, qName, attributes);
            application.startElement(uri, localName, qName, attributes);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            system.endElement(uri, localName, qName);
            application.endElement(uri, localName, qName);
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            system.characters(ch, start, length);
            application.characters(ch, start, length);
        }

        public List<JmsResource> getResources() {
            List<JmsResource> resources = new ArrayList<JmsResource>();
            resources.addAll(system.getResources());
            resources.addAll(application.getResources());
            return resources;
        }
    }

    private static class JmsResource {

        private final File baseFile;

        private final boolean system;
        
        private String name;

        private String target;

        private String file;

        public JmsResource(File baseFile, boolean system) {
            this.baseFile = baseFile;
            this.system = system;
        }

        @CheckForNull
        public File resolveFile() {
            if (file == null) {
                return null;
            }

            File config = new File(file);
            if (!config.isAbsolute()) {
                if (baseFile != null) {
                    config = new File(baseFile, file);
                } else {
                    return null;
                }
            }
            if (config.exists() && config.isFile() && config.canRead()) {
                return config;
            }
            return null;
        }

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public boolean isSystem() {
            return system;
        }

    }
}
