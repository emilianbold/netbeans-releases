/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.coherence.util.xml;

import org.netbeans.modules.coherence.xml.cache.*;
import org.netbeans.modules.coherence.xml.coherence.*;
import org.netbeans.modules.coherence.xml.pof.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBElement;

/**
 *
 * @author Andrew Hopkinson (Oracle A-Team)
 */
public final class XMLObjectFactory extends JAXBXMLFactory {
    /*
     * Constructors
     */

    protected XMLObjectFactory() {
    }

    public static synchronized XMLObjectFactory getInstance() {
        if (instance == null) {
            instance = new XMLObjectFactory();
        }
        return instance;
    }

    public static synchronized XMLObjectFactory getXMLObjectFactory() {
        return getInstance();
    }

    public static synchronized XMLObjectFactory newXMLObjectFactory() {
        return getInstance();
    }

    /*
     * Constants
     */

    /*
     * Properties
     */
    private static XMLObjectFactory instance = null;
    private Logger logger = Logger.getLogger(XMLObjectFactory.class.getCanonicalName());

    /*
     * Accessor Methods
     */

    /*
     * Methods
     */
    // CacheConfig File
    /**
     *
     * @param xmlFile
     * @return
     */
    public CacheConfig unmarshalCacheConfigFile(File xmlFile) throws Exception {
        FileInputStream fis = new FileInputStream(xmlFile);
        try {
            return unmarshalCacheConfigFile(fis);
        } finally {
            fis.close();
        }
    }

    /**
     *
     * @param xmlString
     * @return
     */
    public CacheConfig unmarshalCacheConfigFile(String xmlString) throws Exception {
        FileInputStream fis = new FileInputStream(xmlString);
        try {
            return unmarshalCacheConfigFile(fis);
        } finally {
            fis.close();
        }
    }

    /**
     *
     * @param xmlInputStream
     * @return
     */
    public CacheConfig unmarshalCacheConfigFile(InputStream xmlInputStream) throws Exception {
        CacheConfig xml = null;
        Object o = unmarshalXMLFromStream(xmlInputStream, CacheConfig.class.getPackage().getName());
        if (o instanceof CacheConfig) {
            xml = (CacheConfig) o;
        } else if (o instanceof JAXBElement) {
            JAXBElement element = (JAXBElement) o;
            xml = (CacheConfig) element.getValue();
        } else if (o != null) {
            String msg = "Unknown Return Type ".concat(o.getClass().getCanonicalName());
            logger.log(Level.SEVERE, msg);
            throw new Exception(msg);
        }
        return xml;
    }
    // PofConfig File

    /**
     *
     * @param xmlFile
     * @return
     */
    public PofConfig unmarshalPofConfigFile(File xmlFile) throws Exception {
        FileInputStream fis = new FileInputStream(xmlFile);
        try {
            return unmarshalPofConfigFile(fis);
        } finally {
            fis.close();
        }
    }

    /**
     *
     * @param xmlString
     * @return
     */
    public PofConfig unmarshalPofConfigFile(String xmlString) throws Exception {
        FileInputStream fis = new FileInputStream(xmlString);
        try {
            return unmarshalPofConfigFile(fis);
        } finally {
            fis.close();
        }
    }

    /**
     *
     * @param xmlInputStream
     * @return
     */
    public PofConfig unmarshalPofConfigFile(InputStream xmlInputStream) throws Exception {
        PofConfig xml = null;
        Object o = unmarshalXMLFromStream(xmlInputStream, PofConfig.class.getPackage().getName());
        if (o instanceof PofConfig) {
            xml = (PofConfig) o;
        } else if (o instanceof JAXBElement) {
            JAXBElement element = (JAXBElement) o;
            xml = (PofConfig) element.getValue();
        } else if (o != null) {
            String msg = "Unknown Return Type ".concat(o.getClass().getCanonicalName());
            logger.log(Level.SEVERE, msg);
            throw new Exception(msg);
        }
        return xml;
    }
    // Tangasol (Coherence)Config File

    /**
     *
     * @param xmlFile
     * @return
     */
    public Coherence unmarshalCoherenceConfigFile(File xmlFile) throws Exception {
        FileInputStream fis = new FileInputStream(xmlFile);
        try {
            return unmarshalCoherenceConfigFile(fis);
        } finally {
            fis.close();
        }
    }

    /**
     *
     * @param xmlString
     * @return
     */
    public Coherence unmarshalCoherenceConfigFile(String xmlString) throws Exception {
        FileInputStream fis = new FileInputStream(xmlString);
        try {
            return unmarshalCoherenceConfigFile(fis);
        } finally {
            fis.close();
        }
    }

    /**
     *
     * @param xmlInputStream
     * @return
     */
    public Coherence unmarshalCoherenceConfigFile(InputStream xmlInputStream) throws Exception {
        Coherence xml = null;
        Object o = unmarshalXMLFromStream(xmlInputStream, Coherence.class.getPackage().getName());
        if (o instanceof Coherence) {
            xml = (Coherence) o;
        } else if (o instanceof JAXBElement) {
            JAXBElement element = (JAXBElement) o;
            xml = (Coherence) element.getValue();
        } else if (o != null) {
            String msg = "Unknown Return Type ".concat(o.getClass().getCanonicalName());
            logger.log(Level.SEVERE, msg);
            throw new Exception(msg);
        }
        return xml;
    }
}
