/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.bpel.validation.schema;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.XsdBasedValidator;
import org.netbeans.modules.bpel.model.api.AnotherVersionBpelProcess;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.ConditionHolder;
import org.netbeans.modules.bpel.model.api.Empty;
import org.netbeans.modules.bpel.model.api.ForEach;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.model.api.resources.ResourcePackageMarker;
import org.netbeans.modules.bpel.model.ext.logging.api.Trace;
import static org.netbeans.modules.xml.ui.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.05.05
 */
public final class Validator extends XsdBasedValidator {

  @Override
  public ValidationResult validate(Model model, Validation validation, Validation.ValidationType validationType) {
    if ( !(model instanceof BpelModel)) {
      return null;
    }
    startTime();
    ValidationResult result = Validator.super.validate((BpelModel) model, validation, validationType);
    endTime("Validator " + getName() + "    "); // NOI18N

    // # 135148
    Iterator<ResultItem> items = result.getValidationResult().iterator();
    List<ResultItem> toBeAdded = new LinkedList<ResultItem>();
    List<ResultItem> toBeRemoved = new LinkedList<ResultItem>();

    while (items.hasNext()) {
      fixDescription(items.next(), toBeAdded, toBeRemoved);
    }
    return updateResult(result, toBeAdded, toBeRemoved);
  }

  // # 137885
  private void patch(ResultItem item, List<ResultItem> toBeAdded, List<ResultItem> toBeRemoved) {
    if (patchCondition(item, toBeAdded, toBeRemoved)) {
      return;
    }
    if (patchForEach(item, toBeAdded, toBeRemoved)) {
      return;
    }
  }

  private boolean patchCondition(ResultItem item, List<ResultItem> toBeAdded, List<ResultItem> toBeRemoved) {
    Component component = item.getComponents();
    Component parent = getParent(component);

    if ( !(parent instanceof ConditionHolder)) {
      return false;
    }
    ConditionHolder holder = (ConditionHolder) parent;

    if (holder.getCondition() != null) {
      return false;
    }
    toBeRemoved.add(item);
    toBeAdded.add(createItem(item, (Component) holder));

    return true;
  }

  private boolean patchForEach(ResultItem item, List<ResultItem> toBeAdded, List<ResultItem> toBeRemoved) {
    Component component = item.getComponents();
    Component parent = getParent(component);

    if ( !(parent instanceof ForEach)) {
      return false;
    }
    ForEach forEach = (ForEach) parent;

    if (forEach.getStartCounterValue() != null && forEach.getFinalCounterValue() != null && forEach.getCompletionCondition() != null) {
      return false;
    }
    toBeRemoved.add(item);
    toBeAdded.add(createItem(item, forEach));

    return true;
  }

  private ResultItem createItem(ResultItem item, Component component) {
    return new ResultItem(this, item.getType(), component, item.getDescription());
  }

  private Component getParent(Component component) {
    while(true) {
      if (component == null) {
        return null;
      }
      Component parent = component.getParent();

      if (parent instanceof Scope) {
        component = parent.getParent();
        continue;
      }
      return parent;
    }
  }

  private ValidationResult updateResult(ValidationResult result, List<ResultItem> toBeAdded, List<ResultItem> toBeRemoved) {
    Iterator<ResultItem> oldItems = result.getValidationResult().iterator();
    List<ResultItem> newItems = new LinkedList<ResultItem>();

    while (oldItems.hasNext()) {
      ResultItem item = oldItems.next();

      if (toBeRemoved.contains(item)) {
        continue;
      }
      newItems.add(item);
    }
    for (ResultItem item : toBeAdded) {
      newItems.add(item);
    }
    return new ValidationResult(newItems, result.getValidatedModels());
  }

  private void fixDescription(ResultItem item, List<ResultItem> toBeAdded, List<ResultItem> toBeRemoved) {
    String description = item.getDescription();

    if ( !description.startsWith("cvc-complex-type")) { // NOI18N
      return;
    }
    int k = description.indexOf(": "); // NOI18N

    if (k != -1) {
      description = description.substring(k + 2);
    }
    description = replace(description, "'{", ""); // NOI18N
    description = replace(description, "}'", ""); // NOI18N
    description = replace(description, "\"http://docs.oasis-open.org/wsbpel/2.0/process/executable\":", ""); // NOI18N
    description = replace(description, "WC[##other:\"http://docs.oasis-open.org/wsbpel/2.0/process/executable\"], ", ""); // NOI18N

    item.setDescription(adjustDescription(description));
    patch(item, toBeAdded, toBeRemoved);
  }

  // # 135858
  private String adjustDescription(String value) {
    int count = getCommaCount(value);

    if (count < 2+2+2) {
      return value;
    }
    int k = value.indexOf("One of "); // NOI18N

    if (k == -1) {
      return value;
    }
    return value.substring(0, k) + "The element must contain at least one child activity."; // NOI18N
  }

  private int getCommaCount(String value) {
    int count = 0;

    for (int i=0; i < value.length(); i++) {
      if (value.charAt(i) == ',') {
        count++;
      }
    }
    return count;
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
      // we have BPEL 1.1 process file, validate against his schema
      if (BpelEntity.BUSINESS_PROCESS_1_1_NS_URI.equals(ns)) {
        return getBpel11Schema();
      }
    }
    return getBpel20Schema();
  }

  private Schema getBpel20Schema() {
    if (ourBPEL20Schema == null) {
      ourBPEL20Schema = getCompiledSchema(new InputStream[] { Validator.class.getResourceAsStream(BPEL_XSD_URL)}, new BPELEntityResolver());
    }
    return ourBPEL20Schema;
  }
  
  private Schema getBpel11Schema() {
    if (ourBPEL11Schema == null) {
      ourBPEL11Schema = getCompiledSchema(new InputStream[] { Validator.class.getResourceAsStream(BPEL_1_1_XSD_URL)}, new BPELEntityResolver());
    }
    return ourBPEL11Schema;
  }

  private class BPELEntityResolver implements LSResourceResolver {

    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
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
      if (inputStream != null) {
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

  private static Schema ourBPEL11Schema; 
  private static Schema ourBPEL20Schema; 

  private static final String BPEL_XSD_URL = "/" + ResourcePackageMarker.getPackage() + "/" + ResourcePackageMarker.WS_BPEL_SCHEMA;
  private static final String BPEL_1_1_XSD_URL = "/" + ResourcePackageMarker.getPackage() + "/" + ResourcePackageMarker.WS_BPEL_1_1_SCHEMA;
  private static final String XML_XSD_URL = "/" + ResourcePackageMarker.getPackage() + "/" + ResourcePackageMarker.XSD_SCHEMA;
  private static final String TRACE_2_0_XSD_URL = "/" + ResourcePackageMarker.getPackage() + "/" + ResourcePackageMarker.TRACE_SCHEMA;
  private static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource"; // NOI18N
  private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage"; // NOI18N
  private static final String XML_WSDL_URL = "nbres:/org/netbeans/modules/xml/wsdl/validator/resources/wsdl-2004-08-24.xsd"; // NOI18N
  private static final String XML_XSD_SYSTEMID = "http://www.w3.org/2001/xml.xsd"; // NOI18N
  private static final String XML_WSDL_SYSTEMID = "http://schemas.xmlsoap.org/wsdl/"; // NOI18N
}
