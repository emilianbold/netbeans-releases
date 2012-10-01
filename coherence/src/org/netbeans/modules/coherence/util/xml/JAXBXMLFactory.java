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

import org.netbeans.modules.coherence.util.file.FileUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.MarshalException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.helpers.DefaultValidationEventHandler;
import javax.xml.bind.util.ValidationEventCollector;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.xml.EntityCatalog;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * @author ahopkinson
 * @version $Revision: 1.3 $
 *
 * Create Date 16-Feb-2005
 *
 */
public class JAXBXMLFactory {
    // Define Class Variables

    private Logger logger = Logger.getLogger(JAXBXMLFactory.class.getCanonicalName());
    protected FileUtils fileUtils = FileUtils.getInstance();
    private static JAXBXMLFactory instance = null;

    public static synchronized JAXBXMLFactory getInstance() {
        if (instance == null) {
            instance = new JAXBXMLFactory();
        }
        return instance;
    }

    /**
     *
     */
    protected JAXBXMLFactory() {
        super();
    }

    public void marshalXMLToFile(Object xml, String xmlName) {
        marshalXMLToFile(xml, xmlName, xml.getClass().getPackage().getName());
    }

    public void marshalXMLToFile(Object xml, String xmlName, String packageName) {
        marshalXMLToFile(xml, xmlName, packageName, true);
    }

    public void marshalXMLToFile(Object xml, String xmlName, boolean formatted) {
        marshalXMLToFile(xml, xmlName, xml.getClass().getPackage().getName(), formatted);
    }

    public void marshalXMLToFile(Object xml, String xmlName, String packageName, boolean formatted) {
        String filename = fileUtils.convertFileSystemSeparators(xmlName);
        marshalXMLToFile(xml, new File(filename), packageName, formatted);
    }

    public void marshalXMLToFile(Object xml, File xmlFile) {
        marshalXMLToFile(xml, xmlFile, xml.getClass().getPackage().getName());
    }

    public void marshalXMLToFile(Object xml, File xmlFile, String packageName) {
        marshalXMLToFile(xml, xmlFile, packageName, true);
    }

    public void marshalXMLToFile(Object xml, File xmlFile, boolean formatted) {
        marshalXMLToFile(xml, xmlFile, xml.getClass().getPackage().getName(), formatted);
    }

    public void marshalXMLToFile(Object xml, File xmlFile, String packageName, boolean formatted) {
        JAXBContext jc = null;
        Marshaller m = null;
        OutputStream os = null;

        try {
            jc = JAXBContext.newInstance(packageName);
            m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formatted);
            os = new FileOutputStream(xmlFile);
            m.marshal(xml, os);
        } catch (MarshalException e) {
            logger.log(Level.WARNING, "Failed to Marshal " + xmlFile.getAbsolutePath() + " MarshalException");
            logger.log(Level.FINER, null, e);
        } catch (JAXBException e) {
            logger.log(Level.WARNING, "Failed to Marshal " + xmlFile.getAbsolutePath() + " JAXBException");
            logger.log(Level.FINER, null, e);
        } catch (FileNotFoundException e) {
            logger.log(Level.WARNING, "Failed to Marshal " + xmlFile.getAbsolutePath() + " FileNotFound");
            logger.log(Level.FINER, null, e);
        } finally {
            try {
                os.close();
            } catch (Exception e) {
            }
        }
    }

    public String marshalXMLToString(Object xml) {
        return marshalXMLToString(xml, null);
    }

    public String marshalXMLToString(Object xml, String xmlHeader) {
        return marshalXMLToString(xml, false, xmlHeader);
    }

    public String marshalXMLToString(Object xml, boolean formatted, String xmlHeader) {
        String xmlString = null;
        OutputStream os = null;

        os = new ByteArrayOutputStream();
        marshalXMLToStream(xml, os, formatted, xmlHeader);
        xmlString = os.toString();

        return xmlString;
    }

    public void marshalXMLToStream(Object xml, OutputStream os, String xmlHeader) {
        marshalXMLToStream(xml, os, xml.getClass().getPackage().getName(), xmlHeader);
    }

    public void marshalXMLToStream(Object xml, OutputStream os, String packageName, String xmlHeader) {
        marshalXMLToStream(xml, os, packageName, true, xmlHeader);
    }

    public void marshalXMLToStream(Object xml, OutputStream os, boolean formatted, String xmlHeader) {
        marshalXMLToStream(xml, os, xml.getClass().getPackage().getName(), formatted, xmlHeader);
    }

    public void marshalXMLToStream(Object xml, OutputStream os, String packageName, boolean formatted, String xmlHeader) {
        JAXBContext jc = null;
        Marshaller m = null;

        try {
            jc = JAXBContext.newInstance(packageName);
            m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formatted);
            if (xmlHeader != null) {
                m.setProperty("com.sun.xml.bind.xmlHeaders", xmlHeader);
            }
            m.marshal(xml, os);
        } catch (MarshalException e) {
            logger.log(Level.WARNING, "Failed to Marshal MarshalException");
            logger.log(Level.FINER, null, e);
        } catch (JAXBException e) {
            logger.log(Level.WARNING, "Failed to Marshal JAXBException");
            logger.log(Level.FINER, null, e);
        }
    }

    public Object unmarshalXMLFromFile(String xmlName, String packageName) throws FileNotFoundException {
        String filename = fileUtils.convertFileSystemSeparators(xmlName);
        return unmarshalXMLFromFile(new File(filename), packageName);
    }

    public Object unmarshalXMLFromFile(File xmlFile, String packageName) throws FileNotFoundException {
        FileInputStream fis = new FileInputStream(xmlFile);
        Object xml = unmarshalXMLFromStream(fis, packageName);

        try {
            fis.close();
        } catch (IOException ex) {
            logger.log(Level.FINE, null, ex);
        }
        return xml;
    }

    public Object unmarshalXMLFromString(String xmlString, String packageName) {
        return unmarshalXMLFromStream(new ByteArrayInputStream(xmlString.getBytes()), packageName);
    }
    private SAXParserFactory saxParserFactory = null;
    private SAXParser saxParser = null;
    private SAXSource saxSource = null;
    private XMLReader saxXMLReader = null;

    public Object unmarshalXMLFromStream(InputStream xmlInputStream, String packageName) {
        Object xml = null;
        JAXBContext jc = null;
        Unmarshaller u = null;

        logger.log(Level.FINE, "packageName ".concat(packageName));

        try {
            if (xmlInputStream != null) {
                // Setup SAX Parser
                if (saxParserFactory == null) {
                    saxParserFactory = SAXParserFactory.newInstance();
                    saxParserFactory.setNamespaceAware(false);
                    saxParserFactory.setValidating(false);
                }
                if (saxParser == null) {
                    saxParser = saxParserFactory.newSAXParser();
                }
                saxXMLReader = saxParser.getXMLReader();
                saxXMLReader.setEntityResolver(new CoherenceEntityResolver());
                saxSource = new SAXSource(saxXMLReader, new InputSource(xmlInputStream));
                // Configure JAXB
                jc = JAXBContext.newInstance(packageName);
                u = jc.createUnmarshaller();
                u.setEventHandler(validationEventHandler);
                u.setSchema(saxParser.getSchema());
                xml = u.unmarshal(saxSource);
                logger.log(Level.FINEST, "*** APH-I1 : Unmarshalled XML = " + xml);
            }
        } catch (UnmarshalException e) {
            logger.log(Level.WARNING, "Failed to Unmarshal Stream UnmarshalException \n" + e.getMessage());
            logger.log(Level.FINE, null, e);
            if (e.getLinkedException() != null && e.getLinkedException() instanceof FileNotFoundException && e.getLinkedException().getMessage() != null) {
                String msg = "Missing $DTD.dtd either comment out or remove the <!DOCTYPE $DTD SYSTEM \"$DTD.dtd\"> \nfrom the xml or add the $DTD.dtd to the source directory";
                if (e.getLinkedException().getMessage().indexOf("pof-config.dtd") > 0) {
//                    JOptionPane.showMessageDialog(null, msg.replace("$DTD", "pof-config"));
                    StatusDisplayer.getDefault().setStatusText(msg.replace("$DTD", "pof-config"));
                } else if (e.getLinkedException().getMessage().indexOf("cache-config.dtd") > 0) {
//                    JOptionPane.showMessageDialog(null, msg.replace("$DTD", "cache-config"));
                    StatusDisplayer.getDefault().setStatusText(msg.replace("$DTD", "cache-config"));
                } else if (e.getLinkedException().getMessage().indexOf("coherence.dtd") > 0) {
//                    JOptionPane.showMessageDialog(null, msg.replace("$DTD", "coherence"));
                    StatusDisplayer.getDefault().setStatusText(msg.replace("$DTD", "coherence"));
                } else {
                    logger.log(Level.INFO, null, e);
                }
            }
        } catch (JAXBException e) {
            logger.log(Level.WARNING, "Failed to Unmarshal Stream JAXBException \n" + e.getMessage());
            logger.log(Level.FINER, null, e);
        } catch (ParserConfigurationException e) {
            logger.log(Level.WARNING, "Failed to Unmarshal Stream ParserConfigurationException \n" + e.getMessage());
            logger.log(Level.FINER, null, e);
        } catch (SAXException e) {
            logger.log(Level.WARNING, "Failed to Unmarshal Stream SAXException \n" + e.getMessage());
            logger.log(Level.FINER, null, e);
        }

        return xml;
    }
    private IgnoreDocTypeValidationEventHandler validationEventHandler = new IgnoreDocTypeValidationEventHandler();

    public class IgnoreDocTypeValidationEventHandler extends DefaultValidationEventHandler {

        @Override
        public boolean handleEvent(ValidationEvent event) {
            System.out.println("*** APH-I1 IgnoreDocTypeValidationEventHandler : " + event);
            if (event != null) {
                System.out.println("*** APH-I2 IgnoreDocTypeValidationEventHandler : " + event.getMessage());
                if (event.getLinkedException() != null) {
                    System.out.println("*** APH-I3 IgnoreDocTypeValidationEventHandler : " + event.getLinkedException().getMessage());
                }
                if (event.getLinkedException() != null && event.getLinkedException() instanceof FileNotFoundException) {
                    System.out.println("*** APH-I3 IgnoreDocTypeValidationEventHandler : FileNotFound " + event.getLinkedException().getMessage());
                    return true;
                }
            }
            return true;
        }
    }
    private IgnoreValidationEventCollector validationEventCollector = new IgnoreValidationEventCollector();

    public class IgnoreValidationEventCollector extends ValidationEventCollector {

        @Override
        public boolean handleEvent(ValidationEvent event) {
            System.out.println("*** APH-I1 : IgnoreValidationEventCollector " + event);
            if (event != null) {
                System.out.println("*** APH-I2 IgnoreValidationEventCollector : " + event.getMessage());
                if (event.getLinkedException() != null) {
                    System.out.println("*** APH-I3 IgnoreValidationEventCollector : " + event.getLinkedException().getMessage());
                }
                if (event.getLinkedException() != null && event.getLinkedException() instanceof FileNotFoundException) {
                    System.out.println("*** APH-I3 IgnoreValidationEventCollector : FileNotFound " + event.getLinkedException().getMessage());
                    return true;
                }
            }
//            return super.handleEvent(event);
            return true;
        }

        @Override
        public ValidationEvent[] getEvents() {
            return new ValidationEvent[0];
        }

        @Override
        public boolean hasEvents() {
            return false;
        }
    }

    public class CoherenceEntityResolver implements EntityResolver {

        @Override
        public InputSource resolveEntity(String pubid, String sysid) throws SAXException, IOException {
//            logger.log(Level.INFO, "*** APH-I1 : pubid = " + pubid);
//            logger.log(Level.INFO, "*** APH-I1 : sysid = " + sysid);

            if (sysid.contains("pof-config.dtd") || sysid.contains("coherence.dtd") || sysid.contains("cache-config.dtd")) {
                return new InputSource(new ByteArrayInputStream(new byte[0]));
            } else {
                return EntityCatalog.getDefault().resolveEntity(pubid, sysid);
            }
        }
    }
}
