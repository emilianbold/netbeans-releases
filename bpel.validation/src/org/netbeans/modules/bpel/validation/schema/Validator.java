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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.validation.schema;

import java.io.InputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import org.netbeans.modules.bpel.model.api.AnotherVersionBpelProcess;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.resources.ResourcePackageMarker;
import org.netbeans.modules.bpel.model.ext.logging.api.Trace;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.XsdBasedValidator;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import static org.netbeans.modules.soa.ui.util.UI.*;

public final class Validator extends XsdBasedValidator {

  @Override
  public ValidationResult validate(Model model, final Validation validation, final Validation.ValidationType validationType) {
    if ( !(model instanceof BpelModel)) {
        return null;
    }
    startTime();
    ValidationResult result = Validator.super.validate((BpelModel) model, validation, validationType);
    endTime("Validator " + getName() + "    "); // NOI18N

    return result;
  }

  public String getName() {
    return getClass().getName();
  }

  protected Schema getSchema(Model model) {
      if ( !(model instanceof BpelModel)) {
          return null;
      }
      // # 90585
      AnotherVersionBpelProcess process = ((BpelModel) model).getAnotherVersionProcess();

      if (process != null) {
          String ns = process.getNamespaceUri();
          // we have BPEL 1.1 process file, validate against his schema.
          if (BpelEntity.BUSINESS_PROCESS_1_1_NS_URI.equals(ns)) {
              return getBpel11Schema();
          }
      }
      return getBpel20Schema();
  }

  private Schema getBpel20Schema() {
      if (compiledBPELSchema == null) {
          compiledBPELSchema = getCompiledSchema(
                  new InputStream[] { Validator.class.getResourceAsStream(BPEL_XSD_URL) },
                  new BPELEntityResolver());
      }
      return compiledBPELSchema;
  }
  
  private Schema getBpel11Schema() {
      if (BPEL_1_1_SCHEMA == null) {
          BPEL_1_1_SCHEMA = getCompiledSchema(
                  new InputStream[] { Validator.class.getResourceAsStream(BPEL_1_1_XSD_URL) },
                  new BPELEntityResolver());
      }
      return BPEL_1_1_SCHEMA;
  }

  private class BPELEntityResolver implements LSResourceResolver {

      public BPELEntityResolver() {}

      public LSInput resolveResource( String type, String namespaceURI, String publicId, String systemId, String baseURI) {
          InputStream inputStream = null;

          if (systemId.equals(XML_XSD_SYSTEMID)) {
              inputStream = Validator.class.getResourceAsStream(XML_XSD_URL);
          }
          else if (systemId.equals(XML_WSDL_SYSTEMID)) {
              inputStream = Validator.class.getResourceAsStream(XML_WSDL_URL);
          }
          else if (systemId.equals(Trace.LOGGING_NAMESPACE_URI)) {
              inputStream = Validator.class.getResourceAsStream(TRACE_2_0_XSD_URL);
          }
          if ( inputStream!= null ) {
              DOMImplementation domImpl = null;
              try {
                  domImpl = DocumentBuilderFactory.newInstance().newDocumentBuilder().getDOMImplementation();
              }
              catch (ParserConfigurationException ex) {
                  return null;
              }
              DOMImplementationLS dols = (DOMImplementationLS) domImpl.getFeature("LS", "3.0"); // NOI18N
              LSInput lsi = dols.createLSInput();
              lsi.setByteStream(inputStream);
              return lsi;
          }
          return null;
      }
  }

  private static final String BPEL_XSD_URL = "/" + ResourcePackageMarker.getPackage() + "/" + ResourcePackageMarker.WS_BPEL_SCHEMA;
  private static final String BPEL_1_1_XSD_URL = "/" + ResourcePackageMarker.getPackage() + "/" + ResourcePackageMarker.WS_BPEL_1_1_SCHEMA; 
  private static final String XML_XSD_URL = "/" + ResourcePackageMarker.getPackage() + "/" + ResourcePackageMarker.XSD_SCHEMA;
  private static final String TRACE_2_0_XSD_URL = "/" + ResourcePackageMarker.getPackage() + "/" + ResourcePackageMarker.TRACE_SCHEMA; 
  private static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource"; // NOI18N
  private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage"; // NOI18N
  private static final String XML_WSDL_URL = "nbres:/org/netbeans/modules/xml/wsdl/validator/resources/wsdl-2004-08-24.xsd"; // NOI18N
  private static final String XML_XSD_SYSTEMID = "http://www.w3.org/2001/xml.xsd"; // NOI18N
  private static final String XML_WSDL_SYSTEMID = "http://schemas.xmlsoap.org/wsdl/"; // NOI18N
  
  private static Schema compiledBPELSchema;
  private static Schema BPEL_1_1_SCHEMA; 
}
