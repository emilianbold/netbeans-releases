/*
 * Constants.java
 *
 * Created on March 22, 2007, 1:04 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.rest.codegen;

import javax.lang.model.element.Modifier;

/**
 *
 * @author PeterLiu
 */
class Constants {
    
    public static final String WEB_APPLICATION_EXCEPTION = "com.sun.ws.rest.api.WebApplicationException";
    
    public static final String[] CONTAINER_IMPORTS = {
        "com.sun.ws.rest.api.UriTemplate",                      //NOI18N
        "com.sun.ws.rest.api.HttpMethod",                       //NOI18N
        "com.sun.ws.rest.api.ProduceMime",                      //NOI18N
        "com.sun.ws.rest.api.SubResources",                     //NOI18N
        WEB_APPLICATION_EXCEPTION,
        "javax.annotation.Resource",                            //NOI18N
        "javax.persistence.Persistence"                         //NOI18N
                       
        //"com.sun.ws.rest.api.ConsumeMime",                      //NOI18N
        //"com.sun.ws.rest.api.UriParam"                          //NOI18N
    };
    
    public static final String[] ITEM_IMPORTS = {
        "com.sun.ws.rest.api.UriTemplate",                      //NOI18N
        "com.sun.ws.rest.api.HttpMethod",                       //NOI18N
        "com.sun.ws.rest.api.ProduceMime",                      //NOI18N
        WEB_APPLICATION_EXCEPTION,
        "javax.annotation.Resource",                            //NOI18N
        "javax.persistence.Persistence",                         //NOI18N
        "com.sun.ws.rest.api.ConsumeMime",                      //NOI18N
                //"com.sun.ws.rest.api.UriParam"                          //NOI18N
    };
    
    public static final String[] CONVERTER_ANNOTATION_IMPORTS = {
        "javax.xml.bind.annotation.XmlRootElement",             //NOI18N
        "javax.xml.bind.annotation.XmlElement",                 //NOI18N
        "javax.xml.bind.annotation.XmlTransient",                 //NOI18N
    };
    
    public static final String[] REF_CONVERTER_ANNOTATION_IMPORTS = {
        "javax.xml.bind.annotation.XmlRootElement",             //NOI18N
        "javax.xml.bind.annotation.XmlElement",                 //NOI18N
        "javax.xml.bind.annotation.XmlAttribute",                 //NOI18N
        "javax.xml.bind.annotation.XmlTransient",                 //NOI18N
        Constants.QUERY_TYPE,
        Constants.ENTITY_MANAGER_TYPE,
    };
    
    public static final String RESOURCE_ANNOTATION = "Resource";        //NOI18N
    
    public static final String SUB_RESOURCES_ANNOTATION = "SubResources";        //NOI18N
    
    public static final String URI_TEMPLATE_ANNOTATION = "UriTemplate"; //NOI18N
    
    public static final String HTTP_METHOD_ANNOTATION = "HttpMethod";   //NOI18N
    
    public static final String PRODUCE_MIME_ANNOTATION = "ProduceMime"; //NOI18N
    
    public static final String CONSUME_MIME_ANNOTATION = "ConsumeMime"; //NOI18N

    public static final String XML_TRANSIENT_ANNOTATION = "XmlTransient"; //NOI18N
    
    public static final String XML_MIME_TYPE = "application/xml";       //NOI18N
    
    public static final String HTTP_GET_METHOD = "GET";             //NOI18N
    
    public static final String HTTP_PUT_METHOD = "PUT";             //NOI18N
    
    public static final String HTTP_POST_METHOD = "POST";             //NOI18N
    
    public static final String HTTP_DELETE_METHOD = "DELETE";             //NOI18N
    
    public static final String XML_ROOT_ELEMENT_ANNOTATION = "XmlRootElement";  //NOI18N
    
    public static final String XML_ELEMENT_ANNOTATION = "XmlElement";  //NOI18N
    
    public static final String XML_ATTRIBUTE_ANNOTATION = "XmlAttribute";  //NOI18N
    
    public static final String URI_TYPE = "java.net.URI";       //NOI18N      
    
    public static final String QUERY_TYPE = "javax.persistence.Query";       //NOI18N      

    public static final String ENTITY_MANAGER_TYPE = "javax.persistence.EntityManager";       //NOI18N      

    public static final String ENTITY_MANAGER_FACTORY = "javax.persistence.EntityManagerFactory";       //NOI18N      

    public static final String ENTITY_TRANSACTION = "javax.persistence.EntityTransaction";

    public static final String PERSISTENCE = "javax.persistence.Persistence";
    
    public static final String VOID = "void";
    
    public static final String COLLECTION_TYPE = "java.util.Collection";

    public static final Modifier[] PUBLIC = new Modifier[] { Modifier.PUBLIC };
    
    public static final Modifier[] PRIVATE = new Modifier[] { Modifier.PRIVATE };
    
    public static final Modifier[] PRIVATE_STATIC = new Modifier[] { 
        Modifier.PRIVATE, Modifier.STATIC };
    
    public static final Modifier[] PUBLIC_STATIC = new Modifier[] {
        Modifier.PUBLIC, Modifier.STATIC
    };
}
