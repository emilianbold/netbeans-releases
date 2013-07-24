/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.j2seembedded.platform;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.platform.JavaPlatform;
import org.openide.cookies.InstanceCookie;
import org.openide.loaders.DataObject;
import org.openide.loaders.Environment;
import org.openide.loaders.XMLDataObject;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.xml.XMLUtil;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Tomas Zezula
 */
public final class RemotePlatformProvider implements InstanceCookie.Of, Environment.Provider {
    
    private final XMLDataObject store;
    //@GuardedBy("this")
    private Reference<RemotePlatform> platformRef;
    
    private RemotePlatformProvider(@NonNull final XMLDataObject store) {
        Parameters.notNull("store", store); //NOI18N
        this.store = store;
    }

    @Override
    public boolean instanceOf(@NonNull final Class<?> type) {
        return type.isAssignableFrom(JavaPlatform.class);
    }

    @Override
    public String instanceName() {
        return store.getName();
    }

    @NonNull
    @Override
    public Class<?> instanceClass() throws IOException, ClassNotFoundException {
        return JavaPlatform.class;
    }

    @Override
    @NonNull
    public synchronized Object instanceCreate() throws IOException, ClassNotFoundException {
        RemotePlatform remotePlatform = platformRef == null ? null : platformRef.get();
        if (remotePlatform == null) {
            final SAXHandler handler = new SAXHandler();
            try (InputStream in = store.getPrimaryFile().getInputStream()) {
                final XMLReader reader = XMLUtil.createXMLReader();
                InputSource is = new InputSource(in);
                is.setSystemId(store.getPrimaryFile().toURL().toExternalForm());
                reader.setContentHandler(handler);
                reader.setErrorHandler(handler);
                reader.setEntityResolver(handler);
                reader.parse(is);
            } catch (SAXException ex) {
                final Exception x = ex.getException();
                if (x instanceof java.io.IOException) {
                    throw (IOException)x;
                } else {
                    throw new java.io.IOException(ex);
                }
            }
            remotePlatform = new RemotePlatform(
                    handler.name,
                    handler.installFolders,
                    handler.properties,
                    handler.sysProperties);
            platformRef = new WeakReference<RemotePlatform>(remotePlatform);
        }
        return remotePlatform;
    }

    @Override
    public Lookup getEnvironment(DataObject obj) {
        return lkp;
    }


    private static final class SAXHandler extends DefaultHandler implements EntityResolver {

        private static final String REMOTE_PLATFORM_DTD_ID = "-//NetBeans//DTD Remote Java Platform Definition 1.0//EN"; // NOI18N
        private static final String ELM_PLATFORM = "platform";  //NOI18N
        private static final String ELM_LOCATION = "location";  //NOI18N
        private static final String ELM_PROPERTIES = "properties"; // NOI18N
        private static final String ELM_SYSPROPERTIES = "sysproperties"; // NOI18N
        private static final String ELM_PROPERTY = "property"; // NOI18N
        private static final String ELM_RESOURCE = "resource";  //NOI18N
        private static final String ATTR_NAME = "name"; // NOI18N
        private static final String ATTR_VALUE = "value"; // NOI18N

        Map<String,String> properties;
        Map<String,String> sysProperties;
        List<URL> installFolders;
        String  name;

        private Map<String,String> active;
        private StringBuilder buffer;
        private List<URL> path;


        @Override
        public void startDocument () throws org.xml.sax.SAXException {
        }
        
        @Override
        public void endDocument () throws org.xml.sax.SAXException {
        }
        
        @Override
        public void startElement (
            @NonNull final String uri,
            @NonNull final String localName,
            @NonNull final String qName,
            @NonNull final Attributes attrs) throws org.xml.sax.SAXException {
            if (ELM_PLATFORM.equals(qName)) {
                name = attrs.getValue(ATTR_NAME);
                if (name == null || name.isEmpty()) {
                    throw new SAXException("Missing mandatory platform name."); //NOI18N
                }
            } else if (ELM_PROPERTIES.equals(qName)) {
                if (properties == null)
                    properties = new HashMap<>();
                active = properties;
            } else if (ELM_SYSPROPERTIES.equals(qName)) {
                if (sysProperties == null)
                    sysProperties = new HashMap<>();
                active = sysProperties;
            } else if (ELM_PROPERTY.equals(qName)) {
                if (active == null) {
                    throw new SAXException("property outside properties or sysproperties"); //NOI18N
                }
                String name = attrs.getValue(ATTR_NAME);
                if (name == null || name.isEmpty()) {
                    throw new SAXException("Missing mandatory property name."); //NOI18N
                }
                String val = attrs.getValue(ATTR_VALUE);
                active.put(name, val);
            } else if (ELM_LOCATION.equals(qName)) {
                installFolders = new ArrayList<> ();
                path = this.installFolders;
            } else if (ELM_RESOURCE.equals(qName)) {
                buffer = new StringBuilder();
            }
        }
        
        public void endElement (String uri, String localName, String qName) throws org.xml.sax.SAXException {
            if (ELM_PROPERTIES.equals(qName) || ELM_SYSPROPERTIES.equals(qName)) {
                active = null;
            } else if (ELM_LOCATION.equals(qName)) {
                path = null;
            } else if (ELM_RESOURCE.equals(qName)) {
                if (path == null) {
                    throw new SAXException("resource outside location element"); //NOI18N
                }
                try {
                    this.path.add (new URL(this.buffer.toString()));                    
                } catch (MalformedURLException mue) {
                    throw new SAXException(mue);
                }
                this.buffer = null;
            }
        }

        @Override
        public void characters(char chars[], int start, int length) throws SAXException {
            if (this.buffer != null) {
                this.buffer.append(chars, start, length);
            }
        }
        
        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
            if (REMOTE_PLATFORM_DTD_ID.equals (publicId)) {
                return new org.xml.sax.InputSource (new ByteArrayInputStream (new byte[0]));
            } else {
                return null; // i.e. follow advice of systemID
            }
        }
        
    }

}
