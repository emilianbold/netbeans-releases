
package com.strikeiron.search;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SORT_BY.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="SORT_BY">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Rank"/>
 *     &lt;enumeration value="Name"/>
 *     &lt;enumeration value="Provider"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "SORT_BY")
@XmlEnum
public enum SORTBY {

    @XmlEnumValue("Rank")
    RANK("Rank"),
    @XmlEnumValue("Name")
    NAME("Name"),
    @XmlEnumValue("Provider")
    PROVIDER("Provider");
    private final String value;

    SORTBY(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SORTBY fromValue(String v) {
        for (SORTBY c: SORTBY.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
