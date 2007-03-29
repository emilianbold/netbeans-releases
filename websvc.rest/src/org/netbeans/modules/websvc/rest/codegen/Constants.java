/*
 * Constants.java
 *
 * Created on March 22, 2007, 1:04 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.rest.codegen;

/**
 *
 * @author PeterLiu
 */
class Constants {
    
    public static final String[] CONTAINER_IMPORTS = {
        "com.sun.ws.rest.api.UriTemplate",                      //NOI18N
        "com.sun.ws.rest.api.HttpMethod",                       //NOI18N
        "javax.annotation.Resource",                            //NOI18N
        "com.sun.ws.rest.api.representation.JAXBRepresentation", //NOI18N
        "javax.xml.bind.JAXBContext",                           //NOI18N
        "com.sun.ws.rest.api.WebApplicationException"           //NOI18N
                //"com.sun.ws.rest.api.ProduceMime",                      //NOI18N
                //"com.sun.ws.rest.api.ConsumeMime",                      //NOI18N
                //"com.sun.ws.rest.api.UriParam"                          //NOI18N
    };
    
    public static final String[] ITEM_IMPORTS = {
        "com.sun.ws.rest.api.UriTemplate",                      //NOI18N
        "com.sun.ws.rest.api.HttpMethod",                       //NOI18N
        "com.sun.ws.rest.api.representation.JAXBRepresentation", //NOI18N
        "javax.xml.bind.JAXBContext",                           //NOI18N
        "com.sun.ws.rest.api.WebApplicationException"           //NOI18N
        //"com.sun.ws.rest.api.ProduceMime",                      //NOI18N
        //"com.sun.ws.rest.api.ConsumeMime",                      //NOI18N
        //"com.sun.ws.rest.api.UriParam"                          //NOI18N
    };
    
    public static final String[] CONVERTER_ANNOTATION_IMPORTS = {
        "javax.xml.bind.annotation.XmlRootElement",             //NOI18N
        "javax.xml.bind.annotation.XmlElement"                 //NOI18N
    };
    
    public static final String[] REF_CONVERTER_ANNOTATION_IMPORTS = {
        "javax.xml.bind.annotation.XmlRootElement",             //NOI18N
        "javax.xml.bind.annotation.XmlElement",                 //NOI18N
        "javax.xml.bind.annotation.XmlAttribute",                 //NOI18N
    };
    
    public static final String RESOURCE_ANNOTATION = "Resource";        //NOI18N
    
    public static final String URI_TEMPLATE_ANNOTATION = "UriTemplate"; //NOI18N
    
    public static final String HTTP_METHOD_ANNOTATION = "HttpMethod";   //NOI18N
    
    public static final String XML_ROOT_ELEMENT_ANNOTATION = "XmlRootElement";  //NOI18N
    
    public static final String XML_ELEMENT_ANNOTATION = "XmlElement";  //NOI18N
    
    public static final String XML_ATTRIBUTE_ANNOTATION = "XmlAttribute";  //NOI18N
      
}
