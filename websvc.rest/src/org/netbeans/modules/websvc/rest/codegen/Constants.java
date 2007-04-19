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

package org.netbeans.modules.websvc.rest.codegen;

import javax.lang.model.element.Modifier;

/**
 *
 * @author PeterLiu
 */
class Constants {
    
    public static final String REST_API_PACKAGE = "com.sun.ws.rest.api.";       //NOI18N
    
    public static final String WEB_APPLICATION_EXCEPTION = REST_API_PACKAGE + "WebApplicationException";
    
    public static final String NOT_FOUND_EXCEPTION = REST_API_PACKAGE + "NotFoundException";    //NOI18N
    
    public static final String URI_TEMPLATE = REST_API_PACKAGE + "UriTemplate";        //NOI18N
    
    public static final String HTTP_METHOD = REST_API_PACKAGE + "HttpMethod";         //NOI18N
    
    public static final String PRODUCE_MIME = REST_API_PACKAGE + "ProduceMime";        //NOI18N
    
    public static final String SUB_RESOURCES = REST_API_PACKAGE + "SubResources";      //NOI18N
 
    public static final String CONSUME_MIME = REST_API_PACKAGE + "ConsumeMime";    //NOI18N
    
    public static final String URI_PARAM = REST_API_PACKAGE + "UriParam";  //NOI18N
    
    public static final String HTTP_RESPONSE = REST_API_PACKAGE + "response.HttpResponse"; //NOI18N
    
    public static final String CREATED = REST_API_PACKAGE + "response.Created";     //NOI18N
    
    public static final String ENTITY_TYPE = REST_API_PACKAGE + "Entity";
    
    public static final String RESOURCE = "javax.annotation.Resource";      //NOI18N
    
    public static final String URI_TYPE = "java.net.URI";       //NOI18N
    
    public static final String QUERY_TYPE = "javax.persistence.Query";       //NOI18N
    
    public static final String ENTITY_MANAGER_TYPE = "javax.persistence.EntityManager";       //NOI18N
    
    public static final String ENTITY_MANAGER_FACTORY = "javax.persistence.EntityManagerFactory";       //NOI18N
    
    public static final String ENTITY_TRANSACTION = "javax.persistence.EntityTransaction";
    
    public static final String PERSISTENCE = "javax.persistence.Persistence";
    
    public static final String NO_RESULT_EXCEPTION = "javax.persistence.NoResultException";        //NOI18N
    
    public static final String XML_ROOT_ELEMENT = "javax.xml.bind.annotation.XmlRootElement";             //NOI18N
    
    public static final String XML_ELEMENT = "javax.xml.bind.annotation.XmlElement";                 //NOI18N
        
    public static final String XML_ATTRIBUTE = "javax.xml.bind.annotation.XmlAttribute";                 //NOI18N
    
    public static final String XML_TRANSIENT = "javax.xml.bind.annotation.XmlTransient";                 //NOI18N
    
    public static final String VOID = "void";           //NOI18N
    
    public static final String COLLECTION_TYPE = "java.util.Collection"; //NOI18N
    
    public static final String COLLECTIONS_TYPE = "java.util.Collections";  //NOI18N
    
    public static final String ARRAY_LIST_TYPE = "java.util.ArrayList"; //NOI18N
    
    public static final String RESOURCE_ANNOTATION = "Resource";        //NOI18N
    
    public static final String SUB_RESOURCES_ANNOTATION = "SubResources";        //NOI18N
    
    public static final String URI_TEMPLATE_ANNOTATION = "UriTemplate"; //NOI18N
    
    public static final String URI_PARAM_ANNOTATION = "UriParam";       //NOI18N
    
    public static final String HTTP_METHOD_ANNOTATION = "HttpMethod";   //NOI18N
    
    public static final String PRODUCE_MIME_ANNOTATION = "ProduceMime"; //NOI18N
    
    public static final String CONSUME_MIME_ANNOTATION = "ConsumeMime"; //NOI18N
    
    public static final String XML_TRANSIENT_ANNOTATION = "XmlTransient"; //NOI18N
    
    public static final String XML_MIME_TYPE = "application/xml";       //NOI18N
    
    public static final String HTML_MIME_TYPE = "text/html";       //NOI18N
    
    public static final String HTTP_GET_METHOD = "GET";             //NOI18N
    
    public static final String HTTP_PUT_METHOD = "PUT";             //NOI18N
    
    public static final String HTTP_POST_METHOD = "POST";             //NOI18N
    
    public static final String HTTP_DELETE_METHOD = "DELETE";             //NOI18N
    
    public static final String XML_ROOT_ELEMENT_ANNOTATION = "XmlRootElement";  //NOI18N
    
    public static final String XML_ELEMENT_ANNOTATION = "XmlElement";  //NOI18N
    
    public static final String XML_ATTRIBUTE_ANNOTATION = "XmlAttribute";  //NOI18N
       
    public static final Modifier[] PUBLIC = new Modifier[] { Modifier.PUBLIC };
    
    public static final Modifier[] PRIVATE = new Modifier[] { Modifier.PRIVATE };
    
    public static final Modifier[] PROTECTED = new Modifier[] { Modifier.PROTECTED };
    
    public static final Modifier[] PRIVATE_STATIC = new Modifier[] {
        Modifier.PRIVATE, Modifier.STATIC };
    
    public static final Modifier[] PUBLIC_STATIC = new Modifier[] {
        Modifier.PUBLIC, Modifier.STATIC
    };
    
    public static final String[] CONTAINER_IMPORTS = {
        URI_TEMPLATE,
        HTTP_METHOD,
        PRODUCE_MIME,
        SUB_RESOURCES,
        CREATED,
        RESOURCE
    };
    
    public static final String[] ITEM_IMPORTS = {
        URI_TEMPLATE,
        URI_PARAM,
        HTTP_METHOD,
        PRODUCE_MIME,
        CONSUME_MIME,
        RESOURCE,
        NOT_FOUND_EXCEPTION,
        NO_RESULT_EXCEPTION
    };
    
    public static final String[] CONTAINER_CONVERTER_IMPORTS = {
        XML_ROOT_ELEMENT,
        XML_ELEMENT,
        XML_TRANSIENT,
        XML_ATTRIBUTE,
        ARRAY_LIST_TYPE
    };
    
    public static final String[] ITEM_CONVERTER_IMPORTS = {
        XML_ROOT_ELEMENT,
        XML_ELEMENT,
        XML_TRANSIENT,
        XML_ATTRIBUTE
    };
    
    public static final String[] REF_CONVERTER_IMPORTS = {
        XML_ROOT_ELEMENT,
        XML_ELEMENT,
        XML_TRANSIENT,
        XML_ATTRIBUTE
    };
    
}
