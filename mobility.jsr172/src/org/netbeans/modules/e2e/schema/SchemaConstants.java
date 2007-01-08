/*
 * SchemaConstants.java
 *
 * Created on October 9, 2006, 5:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.schema;

import javax.xml.namespace.QName;

/**
 *
 * @author Michal Skvor
 */
public final class SchemaConstants {

    public static String SCHEMA_URI = "http://www.w3.org/2001/XMLSchema";

    /* qnames */
    public static QName SCHEMA              = new QName( SCHEMA_URI, "schema" );
    public static QName COMPLEX_TYPE        = new QName( SCHEMA_URI, "complexType" );
    public static QName ELEMENT             = new QName( SCHEMA_URI, "element" );
    public static QName SEQUENCE            = new QName( SCHEMA_URI, "sequence" );
    public static QName SIMPLE_TYPE         = new QName( SCHEMA_URI, "simpleType" );        

    /* unsupported */
    public static QName SIMPLE_CONTENT      = new QName( SCHEMA_URI, "simpleContent" );
    public static QName RESTRICTION         = new QName( SCHEMA_URI, "restriction" );
    public static QName EXTENSION           = new QName( SCHEMA_URI, "extension" );
    public static QName ATTRIBUTE           = new QName( SCHEMA_URI, "attribute" );
    public static QName ATTRIBUTE_GROUP     = new QName( SCHEMA_URI, "attributeGroup" );
    public static QName ANY_ATTRIBUTE       = new QName( SCHEMA_URI, "anyAttribute" );
    public static QName COMPLEX_CONTENT     = new QName( SCHEMA_URI, "complexContent" );
    public static QName ALL                 = new QName( SCHEMA_URI, "all" );
    public static QName CHOICE              = new QName( SCHEMA_URI, "choice" );
    public static QName GROUP               = new QName( SCHEMA_URI, "group" );
    public static QName ANY                 = new QName( SCHEMA_URI, "any" );
    public static QName ANNOTATION          = new QName( SCHEMA_URI, "annotation" );

    /* types */
    public static QName TYPE_STRING         = new QName( SCHEMA_URI, "string" );                
    public static QName TYPE_INT            = new QName( SCHEMA_URI, "int" );
    public static QName TYPE_LONG           = new QName( SCHEMA_URI, "long" );
    public static QName TYPE_SHORT          = new QName( SCHEMA_URI, "short" );
    public static QName TYPE_BOOLEAN        = new QName( SCHEMA_URI, "boolean" );
    public static QName TYPE_BYTE           = new QName( SCHEMA_URI, "byte" );
    public static QName TYPE_FLOAT          = new QName( SCHEMA_URI, "float" );
    public static QName TYPE_DOUBLE         = new QName( SCHEMA_URI, "double" );
    public static QName TYPE_BASE64_BINARY  = new QName( SCHEMA_URI, "base64Binary" );
    public static QName TYPE_HEX_BINARY     = new QName( SCHEMA_URI, "hexBinary" );
    public static QName TYPE_QNAME          = new QName( SCHEMA_URI, "QName" );

}
