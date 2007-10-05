
package com.strikeiron.search;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AUTHENTICATION_STYLE.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="AUTHENTICATION_STYLE">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="SimpleParam"/>
 *     &lt;enumeration value="SoapHeader"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "AUTHENTICATION_STYLE")
@XmlEnum
public enum AUTHENTICATIONSTYLE {

    @XmlEnumValue("SimpleParam")
    SIMPLE_PARAM("SimpleParam"),
    @XmlEnumValue("SoapHeader")
    SOAP_HEADER("SoapHeader");
    private final String value;

    AUTHENTICATIONSTYLE(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static AUTHENTICATIONSTYLE fromValue(String v) {
        for (AUTHENTICATIONSTYLE c: AUTHENTICATIONSTYLE.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
