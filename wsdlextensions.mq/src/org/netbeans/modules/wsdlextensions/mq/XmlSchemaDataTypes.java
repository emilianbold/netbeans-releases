package org.netbeans.modules.wsdlextensions.mq;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

/**
 * An enumeration of a subset of primitive and derivative XML Schema Datatypes.
 *
 * @link http://www.w3.org/TR/xmlschema-2/#built-in-datatypes
 * @author Noel.Ang@sun.com
 */
public enum XmlSchemaDataTypes {
    BASE64             (new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "base64Binary")),
    HEXBINARY          (new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "hexBinary")),
    DATETIME           (new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "dateTime")),
    INTEGER            (new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "integer")),
    NONNEGATIVEINTEGER (new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "nonNegativeInteger")),
    UNSIGNEDLONG       (new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "unsignedLong")),
    POSITIVEINTEGER    (new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "positiveInteger")),
    UNSIGNEDINT        (new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "unsignedInt")),
    UNSIGNEDSHORT      (new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "unsignedShort")),
    LONG               (new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "long")),
    INT                (new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "int")),
    SHORT              (new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "short")),
    BYTE               (new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "byte")),
    NONPOSITIVEINTEGER (new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "nonPositiveInteger")),
    NEGATIVEINTEGER    (new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "negativeInteger")),
    STRING             (new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "string")),
    NORMALIZEDSTRING   (new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "normalizedString")),
    TOKEN              (new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "token")),
    LANGUAGE           (new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "language")),
    NAME               (new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "name")),
    NMTOKEN            (new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "nmtoken")),
    NCNAME             (new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "ncname")),
    NMTOKENS           (new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "nmtokens")),
    ID                 (new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "id")),
    IDREF              (new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "idref")),
    ENTITY             (new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "entity")),
    IDREFS             (new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "idrefs")),
    ENTITIES           (new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "entities")),
    ;
    
    private XmlSchemaDataTypes(QName qname) {
        this.qname = qname;
    }
    
    public boolean isDerivableTo(QName name) {
        boolean isDerivableTo = false;
        Collection<XmlSchemaDataTypes> derivations = types.get(this);
        
        if (derivations != null) {
            // Immediate derivatives
            for (Iterator<XmlSchemaDataTypes> iter = derivations.iterator()
                    ; iter.hasNext() && !isDerivableTo
                    ;) {
                isDerivableTo = iter.next().qname.equals(name);
            }
        
            // Indirect derivatives
            if (!isDerivableTo) {
                for (Iterator<XmlSchemaDataTypes> iter = derivations.iterator()
                        ; iter.hasNext() && !isDerivableTo
                        ;) {
                    isDerivableTo = iter.next().isDerivableTo(name);
                }
            }
        }
        
        return isDerivableTo;
    }
    
    public Collection<QName> getLineage() {
        Collection<QName> lineage = new HashSet<QName>();
        lineage.add(qname);
        
        Collection<XmlSchemaDataTypes> derivations = types.get(this);
        if (derivations != null) {
            for (XmlSchemaDataTypes derivation : derivations) {
                lineage.add(derivation.qname);
            }
            for (XmlSchemaDataTypes derivation : derivations) {
                lineage.addAll(derivation.getLineage());
            }
        }
        return lineage;
    }

    private static final Map<XmlSchemaDataTypes, Collection<XmlSchemaDataTypes>> types;
    public final QName qname;
    
    static {
        // type hierarchy
        types = new HashMap<XmlSchemaDataTypes, Collection<XmlSchemaDataTypes>>();
        
        // Integer sub hierarchy
        {
            types.put(UNSIGNEDSHORT, new HashSet<XmlSchemaDataTypes>());
            types.put(UNSIGNEDINT, Arrays.asList(UNSIGNEDSHORT));
            types.put(UNSIGNEDLONG, Arrays.asList(UNSIGNEDINT));
            types.put(POSITIVEINTEGER, new HashSet<XmlSchemaDataTypes>());
            types.put(NONNEGATIVEINTEGER, Arrays.asList(UNSIGNEDLONG, POSITIVEINTEGER));
            types.put(BYTE, new HashSet<XmlSchemaDataTypes>());
            types.put(SHORT, Arrays.asList(BYTE));
            types.put(INT, Arrays.asList(SHORT));
            types.put(LONG, Arrays.asList(INT));
            types.put(NEGATIVEINTEGER, new HashSet<XmlSchemaDataTypes>());
            types.put(NONPOSITIVEINTEGER, Arrays.asList(NEGATIVEINTEGER));
            types.put(INTEGER, Arrays.asList(NONNEGATIVEINTEGER, LONG, NONPOSITIVEINTEGER));
        }
        
        // String sub hierarchy
        {
            types.put(IDREFS, new HashSet<XmlSchemaDataTypes>());
            types.put(ENTITIES, new HashSet<XmlSchemaDataTypes>());
            types.put(ID, new HashSet<XmlSchemaDataTypes>());
            types.put(NMTOKEN, new HashSet<XmlSchemaDataTypes>());
            types.put(LANGUAGE, new HashSet<XmlSchemaDataTypes>());
            types.put(IDREF, Arrays.asList(IDREFS));
            types.put(ENTITY, Arrays.asList(ENTITIES));
            types.put(NCNAME, Arrays.asList(ID, IDREF, ENTITY));
            types.put(NAME, Arrays.asList(NCNAME));
            types.put(NMTOKEN, Arrays.asList(NMTOKENS));
            types.put(TOKEN, Arrays.asList(LANGUAGE, NAME, NMTOKEN));
            types.put(NORMALIZEDSTRING, Arrays.asList(TOKEN));
            types.put(STRING, Arrays.asList(NORMALIZEDSTRING));
        }
    }
}
