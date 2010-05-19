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

/**
 *
 */
package org.netbeans.modules.bpel.model.api;

import org.netbeans.modules.bpel.model.api.events.VetoException;

/**
 * @author ads
 *         <p>
 *         The &lt;import&gt; element is used within a WS-BPEL process to
 *         explicitly indicate a dependency on external XML Schema or WSDL
 *         definitions. Any number of &lt;import&gt; elements may appear as
 *         initial children of the &lt;process&gt; element, before any other
 *         child element. Each &lt;import&gt; element contains a mandatory and
 *         two optional attributes.
 *         </p>
 *         <p>
 *         <li>"namespace". The namespace attribute specifies an absolute URI
 *         that identifies the imported definitions. This attribute is optional.
 *         An import element without<span style="">&nbsp; </span>a namespace
 *         attribute indicates that external definitions are in use which are
 *         not namespace qualified.</li>
 *         </p>
 *         <p>
 *         <li>"location". The location attribute contains a URI indicating the
 *         location of a document that contains relevant definitions in the
 *         namespace specified. The location URI may be a relative URI,
 *         following the usual rules for resolution of the URI base (XML Base
 *         and RFC 2396). The location attribute is optional. An import element
 *         without a location attribute indicates that external definitions are
 *         used by the process but makes no statement about where those
 *         definitions may be found. The document located at the location URI
 *         MUST identify the definitions it contains with a URI matching the URI
 *         indicated by the namespace attribute. The location attribute is a
 *         hint and that the BPEL Processor is not required to retrieve the
 *         document being imported from the specified location.</li>
 *         </p>
 *         <p>
 *         <li>"importType". The importType attribute identifies the type of
 *         document being imported by providing an absolute URI that identifies
 *         the encoding language used in the document. The value of the
 *         importType attribute MUST be set to
 *         "http://www.w3.org/2001/XMLSchema"; when importing XML Schema 1.0
 *         documents, and to "http://schemas.xmlsoap.org/wsdl/"; when importing
 *         WSDL 1.1 documents. Note: other importType URI values MAY be used
 *         here. </li>
 *         </p>
 *         <p>
 *         Observe that according to these rules, it is permissible to have an
 *         import element without namespace and location attributes, and only
 *         containing an importType attribute. Such an import element indicates
 *         that external definitions of the indicated type are in use which are
 *         not namespace qualified, and makes no statement about where those
 *         definitions may be found.
 *         </p>
 *         <p>
 *         A BPEL process definition MUST import all XML Schema and WSDL
 *         definitions it uses. This includes all XML Schema type and element
 *         definitions, all WSLD port types and message types as well as
 *         property and property alias definitions used by the process. In order
 *         to support the use of definitions from namespaces spanning multiple
 *         documents, a BPEL process MAY include more than one import
 *         declarations for the same namespace and importType, provided that
 *         those declarations include different location values. Import elements
 *         are conceptually unordered. It is an error if the imported documents
 *         contain conflicting definitions of a component used by the importing
 *         process definition (as could be caused, for example, when the XSD
 *         redefinition mechanism is used).
 *         <p>
 *         Java class for tImport complex type.
 *         <p>
 *         The following schema fragment specifies the expected content
 *         contained within this class.
 * 
 * <pre>
 *   &lt;complexType name=&quot;tImport&quot;&gt;
 *     &lt;complexContent&gt;
 *       &lt;extension base=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}tExtensibleElements&quot;&gt;
 *         &lt;attribute name=&quot;importType&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}anyURI&quot; /&gt;
 *         &lt;attribute name=&quot;location&quot; use=&quot;optional&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}anyURI&quot; /&gt;
 *         &lt;attribute name=&quot;namespace&quot; use=&quot;optional&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}anyURI&quot; /&gt;
 *       &lt;/extension&gt;
 *     &lt;/complexContent&gt;
 *   &lt;/complexType&gt;
 * </pre>
 */
public interface Import extends ExtensibleElements, NamespaceSpec {

    String LOCATION = "location";                                   // NOI18N

    String IMPORT_TYPE = "importType";                              // NOI18N
    
    /**
     * This type should be used for xsd document.
     */
    String SCHEMA_IMPORT_TYPE ="http://www.w3.org/2001/XMLSchema";  // NOI18N
    
    /**
     * This type should be used for wsdl document.
     */
    String WSDL_IMPORT_TYPE = "http://schemas.xmlsoap.org/wsdl/";   // NOI18N

    /**
     * Getter for "location" attribute.
     * 
     * @return "location" attribute value.
     */
    String getLocation();

    /**
     * Setter for "location" attribute.
     * 
     * @param value
     *            New "location" attribute value.
     * @throws VetoException
     *             Will be thrown if value is not acceptable as value here.
     */
    void setLocation( String value ) throws VetoException;
    
    /**
     * Removes "location" attribute.
     */
    void removeLocation();

    /**
     * Getter for ""importType" attribute.
     * 
     * @return "importType" attribute value.
     */
    String getImportType();

    /**
     * Setter for ""importType" attribute.
     * 
     * @param value
     *            New "importType" attribute value.
     * @throws VetoException
     *             Will be thrown if value is not acceptable as value here.
     */
    void setImportType( String value ) throws VetoException;
    
    /**
     * Removes "namespace" attribute.
     */
    void removeNamespace();
}
