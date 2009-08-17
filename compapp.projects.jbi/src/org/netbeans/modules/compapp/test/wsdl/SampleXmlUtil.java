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
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.compapp.test.wsdl;

/*
 * XmlBeans class for generating XML from XML Schemas
 *
 * TODO:
 *  Comment on enumerations?
 *  Comment on facets?
 *  Have a verbose option?
 *  Have a sample data option, would create valid instance with sample data?
 *  Add the pattern facet; this is tricky, considering the relationship with length
 */

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.GDate;
import org.apache.xmlbeans.GDateBuilder;
import org.apache.xmlbeans.GDuration;
import org.apache.xmlbeans.GDurationBuilder;
import org.apache.xmlbeans.SchemaLocalElement;
import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SimpleValue;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlDate;
import org.apache.xmlbeans.XmlDateTime;
import org.apache.xmlbeans.XmlDecimal;
import org.apache.xmlbeans.XmlDuration;
import org.apache.xmlbeans.XmlGDay;
import org.apache.xmlbeans.XmlGMonth;
import org.apache.xmlbeans.XmlGMonthDay;
import org.apache.xmlbeans.XmlGYear;
import org.apache.xmlbeans.XmlGYearMonth;
import org.apache.xmlbeans.XmlInteger;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlTime;
import org.apache.xmlbeans.impl.util.Base64;
import org.apache.xmlbeans.impl.util.HexBin;
import org.apache.xmlbeans.soap.SOAPArrayType;
import org.apache.xmlbeans.soap.SchemaWSDLArrayType;
import org.openide.util.NbBundle;

public class SampleXmlUtil {
    private boolean _soapEnc;
    
    public SampleXmlUtil(boolean soapEnc) {
        _soapEnc = soapEnc;
    }
    
    public boolean isSoapEnc() {
        return _soapEnc;
    }
    
    public static String createSampleForType(SchemaType sType) {
        XmlObject object = XmlObject.Factory.newInstance();
        XmlCursor cursor = object.newCursor();
        // Skip the document node
        cursor.toNextToken();
        // Using the type and the cursor, call the utility method to get a
        // sample XML payload for that Schema element
        new SampleXmlUtil(false).createSampleForType(sType, cursor);
        // Cursor now contains the sample payload
        // Pretty print the result.  Note that the cursor is positioned at the
        // end of the doc so we use the original xml object that the cursor was
        // created upon to do the xmlText() against.
        XmlOptions options = new XmlOptions();
        options.put(XmlOptions.SAVE_PRETTY_PRINT);
        options.put(XmlOptions.SAVE_PRETTY_PRINT_INDENT, 2);
        options.put(XmlOptions.SAVE_AGGRESSIVE_NAMESPACES);
        String result = object.xmlText(options);
        
        return result;
    }
    
    Random _picker = new Random(1);
    
    
    
    private boolean ignoreOptional;
    
    /**
     * Cursor position
     * Before:
     * <theElement>^</theElement>
     * After:
     * <theElement><lots of stuff/>^</theElement>
     */
    public void createSampleForType(SchemaType stype, XmlCursor xmlc) {
        if (_typeStack.contains( stype ))
            return;
        
        _typeStack.add( stype );
        
        try {
            if (stype.isSimpleType() || stype.isURType()) {
                processSimpleType(stype, xmlc);
                return;
            }
            
            // complex Type
            // <theElement>^</theElement>
            processAttributes(stype, xmlc);
            
            // <theElement attri1="string">^</theElement>
            switch (stype.getContentType()) {
                case SchemaType.NOT_COMPLEX_TYPE :
                case SchemaType.EMPTY_CONTENT :
                    // noop
                    break;
                case SchemaType.SIMPLE_CONTENT :
                {
                    processSimpleType(stype, xmlc);
                }
                break;
                case SchemaType.MIXED_CONTENT :
                    xmlc.insertChars(pick(WORDS) + " "); // NOI18N
                    if (stype.getContentModel() != null) {
                        processParticle(stype.getContentModel(), xmlc, true);
                    }
                    xmlc.insertChars(pick(WORDS));
                    break;
                case SchemaType.ELEMENT_CONTENT :
                    if (stype.getContentModel() != null) {
                        processParticle(stype.getContentModel(), xmlc, false);
                    }
                    break;
            }
        } finally {
            _typeStack.remove( _typeStack.size() - 1 );
        }
    }
    
    private void processSimpleType(SchemaType stype, XmlCursor xmlc) {
        if (_soapEnc) {
            QName typeName = stype.getName();
            if (typeName != null) {
                xmlc.insertAttributeWithValue(XSI_TYPE, formatQName(xmlc, typeName));
            }
        }
        
        String sample = sampleDataForSimpleType(stype);
        xmlc.insertChars(sample);
    }
    
    private String sampleDataForSimpleType(SchemaType sType) {
        return "?" + _sampleDataForSimpleType(sType) + "?"; // NOI18N
    }
    
    private String _sampleDataForSimpleType(SchemaType sType) {
//        if( sType != null )
//            return "";
        
        if (XmlObject.type.equals(sType))
            return "anyType"; // NOI18N
        
        if (XmlAnySimpleType.type.equals(sType))
            return "anySimpleType"; // NOI18N
        
        if (sType.getSimpleVariety() == SchemaType.LIST) {
            SchemaType itemType = sType.getListItemType();
            StringBuffer sb = new StringBuffer();
            int length = pickLength(sType);
            if (length > 0)
                sb.append(sampleDataForSimpleType(itemType));
            for (int i = 1; i < length; i += 1) {
                sb.append(' ');
                sb.append(sampleDataForSimpleType(itemType));
            }
            return sb.toString();
        }
        
        if (sType.getSimpleVariety() == SchemaType.UNION) {
            SchemaType[] possibleTypes = sType.getUnionConstituentTypes();
            if (possibleTypes.length == 0)
                return ""; // NOI18N
            return sampleDataForSimpleType(possibleTypes[pick(possibleTypes.length)]);
        }
        
        XmlAnySimpleType[] enumValues = sType.getEnumerationValues();
        if (enumValues != null && enumValues.length > 0) {
            return enumValues[pick(enumValues.length)].getStringValue();
        }
        
        switch (sType.getPrimitiveType().getBuiltinTypeCode()) {
            default:
            case SchemaType.BTC_NOT_BUILTIN:
                return ""; // NOI18N
                
            case SchemaType.BTC_ANY_TYPE:
            case SchemaType.BTC_ANY_SIMPLE:
                return "anything"; // NOI18N
                
            case SchemaType.BTC_BOOLEAN:
                return pick(2) == 0 ? "true" : "false"; // NOI18N
                
            case SchemaType.BTC_BASE_64_BINARY:
            {
                String result = null;
                try {
                    result = new String(Base64.encode(formatToLength(pick(WORDS), sType).getBytes("utf-8")));  // NOI18N
                } catch (java.io.UnsupportedEncodingException e) {
                    /* Can't possibly happen */
                }
                return result;
            }
            
            case SchemaType.BTC_HEX_BINARY:
                return HexBin.encode(formatToLength(pick(WORDS), sType));
                
            case SchemaType.BTC_ANY_URI:
                return formatToLength("http://www." + pick(DNS1) + "." + pick(DNS2) + "/" + pick(WORDS) + "/" + pick(WORDS), sType);  // NOI18N
                
            case SchemaType.BTC_QNAME:
                return formatToLength("qname", sType); // NOI18N
                
            case SchemaType.BTC_NOTATION:
                return formatToLength("notation", sType); // NOI18N
                
            case SchemaType.BTC_FLOAT:
                return "1.5E2"; // NOI18N
            case SchemaType.BTC_DOUBLE:
                return "1.051732E7"; // NOI18N
            case SchemaType.BTC_DECIMAL:
                switch (closestBuiltin(sType).getBuiltinTypeCode()) {
                    case SchemaType.BTC_SHORT:
                        return formatDecimal("1", sType); // NOI18N
                    case SchemaType.BTC_UNSIGNED_SHORT:
                        return formatDecimal("5", sType); // NOI18N
                    case SchemaType.BTC_BYTE:
                        return formatDecimal("2", sType); // NOI18N
                    case SchemaType.BTC_UNSIGNED_BYTE:
                        return formatDecimal("6", sType); // NOI18N
                    case SchemaType.BTC_INT:
                        return formatDecimal("3", sType); // NOI18N
                    case SchemaType.BTC_UNSIGNED_INT:
                        return formatDecimal("7", sType); // NOI18N
                    case SchemaType.BTC_LONG:
                        return formatDecimal("10", sType); // NOI18N
                    case SchemaType.BTC_UNSIGNED_LONG:
                        return formatDecimal("11", sType); // NOI18N
                    case SchemaType.BTC_INTEGER:
                        return formatDecimal("100", sType); // NOI18N
                    case SchemaType.BTC_NON_POSITIVE_INTEGER:
                        return formatDecimal("-200", sType); // NOI18N
                    case SchemaType.BTC_NEGATIVE_INTEGER:
                        return formatDecimal("-201", sType); // NOI18N
                    case SchemaType.BTC_NON_NEGATIVE_INTEGER:
                        return formatDecimal("200", sType); // NOI18N
                    case SchemaType.BTC_POSITIVE_INTEGER:
                        return formatDecimal("201", sType); // NOI18N
                    default:
                    case SchemaType.BTC_DECIMAL:
                        return formatDecimal("1000.00", sType); // NOI18N
                }
                
            case SchemaType.BTC_STRING:
            {
                String result;
                switch (closestBuiltin(sType).getBuiltinTypeCode()) {
                    case SchemaType.BTC_STRING:
                    case SchemaType.BTC_NORMALIZED_STRING:
                        result = "string"; // NOI18N
                        break;
                        
                    case SchemaType.BTC_TOKEN:
                        result = "token"; // NOI18N
                        break;
                        
                    default:
                        result = "string"; // NOI18N
                        break;
                }
                
                return formatToLength(result, sType);
            }
            
            case SchemaType.BTC_DURATION:
                return formatDuration(sType);
                
            case SchemaType.BTC_DATE_TIME:
            case SchemaType.BTC_TIME:
            case SchemaType.BTC_DATE:
            case SchemaType.BTC_G_YEAR_MONTH:
            case SchemaType.BTC_G_YEAR:
            case SchemaType.BTC_G_MONTH_DAY:
            case SchemaType.BTC_G_DAY:
            case SchemaType.BTC_G_MONTH:
                return formatDate(sType);
        }
    }
    
    // a bit from the Aenid
    public static final String[] WORDS = new String[]
    {
        "ipsa", "iovis", "rapidum", "iaculata", "e", "nubibus", "ignem", // NOI18N
        "disiecitque", "rates", "evertitque", "aequora", "ventis", // NOI18N
        "illum", "exspirantem", "transfixo", "pectore", "flammas", // NOI18N
        "turbine", "corripuit", "scopuloque", "infixit", "acuto", // NOI18N
        "ast", "ego", "quae", "divum", "incedo", "regina", "iovisque", // NOI18N
        "et", "soror", "et", "coniunx", "una", "cum", "gente", "tot", "annos", // NOI18N
        "bella", "gero", "et", "quisquam", "numen", "iunonis", "adorat", // NOI18N
        "praeterea", "aut", "supplex", "aris", "imponet", "honorem", // NOI18N
        "talia", "flammato", "secum", "dea", "corde", "volutans", // NOI18N
        "nimborum", "in", "patriam", "loca", "feta", "furentibus", "austris", // NOI18N
        "aeoliam", "venit", "hic", "vasto", "rex", "aeolus", "antro", // NOI18N
        "luctantis", "ventos", "tempestatesque", "sonoras", // NOI18N
        "imperio", "premit", "ac", "vinclis", "et", "carcere", "frenat", // NOI18N
        "illi", "indignantes", "magno", "cum", "murmure", "montis", // NOI18N
        "circum", "claustra", "fremunt", "celsa", "sedet", "aeolus", "arce", // NOI18N
        "sceptra", "tenens", "mollitque", "animos", "et", "temperat", "iras", // NOI18N
        "ni", "faciat", "maria", "ac", "terras", "caelumque", "profundum", // NOI18N
        "quippe", "ferant", "rapidi", "secum", "verrantque", "per", "auras", // NOI18N
        "sed", "pater", "omnipotens", "speluncis", "abdidit", "atris", // NOI18N
        "hoc", "metuens", "molemque", "et", "montis", "insuper", "altos", // NOI18N
        "imposuit", "regemque", "dedit", "qui", "foedere", "certo", // NOI18N
        "et", "premere", "et", "laxas", "sciret", "dare", "iussus", "habenas", // NOI18N
    };
    
    
    
    private static final String[] DNS1 = new String[] { "corp", "your", "my", "sample", "company", "test", "any" }; // NOI18N
    private static final String[] DNS2 = new String[] { "com", "org", "com", "gov", "org", "com", "org", "com", "edu" }; // NOI18N
    
    private int pick(int n) {
        return _picker.nextInt(n);
    }
    
    private String pick(String[] a) {
        return a[pick(a.length)];
    }
    
    private String pick(String[] a, int count) {
        if (count <= 0)
            return ""; // NOI18N
        
        int i = pick(a.length);
        StringBuffer sb = new StringBuffer(a[i]);
        while (count-- > 0) {
            i += 1;
            if (i >= a.length)
                i = 0;
            sb.append(' ');
            sb.append(a[i]);
        }
        return sb.toString();
    }
    
    private String pickDigits(int digits) {
        StringBuffer sb = new StringBuffer();
        while (digits-- > 0)
            sb.append(Integer.toString(pick(10)));
        return sb.toString();
    }
    
    private int pickLength(SchemaType sType) {
        XmlInteger length = (XmlInteger) sType.getFacet(SchemaType.FACET_LENGTH);
        if (length != null)
            return length.getBigIntegerValue().intValue();
        XmlInteger min    = (XmlInteger) sType.getFacet(SchemaType.FACET_MIN_LENGTH);
        XmlInteger max    = (XmlInteger) sType.getFacet(SchemaType.FACET_MAX_LENGTH);
        int minInt, maxInt;
        if (min == null)
            minInt = 0;
        else
            minInt = min.getBigIntegerValue().intValue();
        if (max == null)
            maxInt = Integer.MAX_VALUE;
        else
            maxInt = max.getBigIntegerValue().intValue();
        // We try to keep the length of the array within reasonable limits,
        // at least 1 item and at most 3 if possible
        if (minInt == 0 && maxInt >= 1)
            minInt = 1;
        if (maxInt > minInt + 2)
            maxInt = minInt + 2;
        if (maxInt < minInt)
            maxInt = minInt;
        return minInt + pick(maxInt-minInt);
    }
    
    /**
     * Formats a given string to the required length, using the following operations:
     * - append the source string to itself as necessary to pass the minLength;
     * - truncate the result of previous step, if necessary, to keep it within minLength.
     */
    private String formatToLength(String s, SchemaType sType) {
        String result = s;
        try {
            SimpleValue min = (SimpleValue)sType.getFacet(SchemaType.FACET_LENGTH);
            if (min == null)
                min = (SimpleValue)sType.getFacet(SchemaType.FACET_MIN_LENGTH);
            if (min != null) {
                int len = min.getIntValue();
                while (result.length() < len)
                    result = result + result;
            }
            SimpleValue max = (SimpleValue)sType.getFacet(SchemaType.FACET_LENGTH);
            if (max == null)
                max = (SimpleValue)sType.getFacet(SchemaType.FACET_MAX_LENGTH);
            if (max != null) {
                int len = max.getIntValue();
                if (result.length() > len)
                    result = result.substring(0, len);
            }
        } catch (Exception e) // intValue can be out of range
        {
        }
        return result;
    }
    
    private String formatDecimal(String start, SchemaType sType) {
        BigDecimal result = new BigDecimal(start);
        XmlDecimal xmlD;
        xmlD = (XmlDecimal) sType.getFacet(SchemaType.FACET_MIN_INCLUSIVE);
        BigDecimal min = xmlD != null ? xmlD.getBigDecimalValue() : null;
        xmlD = (XmlDecimal) sType.getFacet(SchemaType.FACET_MAX_INCLUSIVE);
        BigDecimal max = xmlD != null ? xmlD.getBigDecimalValue() : null;
        boolean minInclusive = true, maxInclusive = true;
        xmlD = (XmlDecimal) sType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE);
        if (xmlD != null) {
            BigDecimal minExcl = xmlD.getBigDecimalValue();
            if (min == null || min.compareTo(minExcl) < 0) {
                min = minExcl;
                minInclusive = false;
            }
        }
        xmlD = (XmlDecimal) sType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE);
        if (xmlD != null) {
            BigDecimal maxExcl = xmlD.getBigDecimalValue();
            if (max == null || max.compareTo(maxExcl) > 0) {
                max = maxExcl;
                maxInclusive = false;
            }
        }
        xmlD = (XmlDecimal) sType.getFacet(SchemaType.FACET_TOTAL_DIGITS);
        int totalDigits = -1;
        if (xmlD != null) {
            totalDigits = xmlD.getBigDecimalValue().intValue();
            
            StringBuffer sb = new StringBuffer(totalDigits);
            for (int i = 0; i < totalDigits; i++)
                sb.append('9');
            BigDecimal digitsLimit = new BigDecimal(sb.toString());
            if (max != null && max.compareTo(digitsLimit) > 0) {
                max = digitsLimit;
                maxInclusive = true;
            }
            digitsLimit = digitsLimit.negate();
            if (min != null && min.compareTo(digitsLimit) < 0) {
                min = digitsLimit;
                minInclusive = true;
            }
        }
        
        int sigMin = min == null ? 1 : result.compareTo(min);
        int sigMax = max == null ? -1 : result.compareTo(max);
        boolean minOk = sigMin > 0 || sigMin == 0 && minInclusive;
        boolean maxOk = sigMax < 0 || sigMax == 0 && maxInclusive;
        
        // Compute the minimum increment
        xmlD = (XmlDecimal) sType.getFacet(SchemaType.FACET_FRACTION_DIGITS);
        int fractionDigits = -1;
        BigDecimal increment;
        if (xmlD == null)
            increment = new BigDecimal(1);
        else {
            fractionDigits = xmlD.getBigDecimalValue().intValue();
            if (fractionDigits > 0) {
                StringBuffer sb = new StringBuffer("0."); // NOI18N
                for (int i = 1; i < fractionDigits; i++)
                    sb.append('0');
                sb.append('1');
                increment = new BigDecimal(sb.toString());
            } else
                increment = new BigDecimal(1);
        }
        
        if (minOk && maxOk) {
            // OK
        } else if (minOk && !maxOk) {
            // TOO BIG
            if (maxInclusive)
                result = max;
            else
                result = max.subtract(increment);
        } else if (!minOk && maxOk) {
            // TOO SMALL
            if (minInclusive)
                result = min;
            else
                result = min.add(increment);
        } else {
            // MIN > MAX!!
        }
        
        // We have the number
        // Adjust the scale according to the totalDigits and fractionDigits
        int digits = 0;
        BigDecimal ONE = new BigDecimal(BigInteger.ONE);
        for (BigDecimal n = result; n.abs().compareTo(ONE) >= 0; digits++)
            n = n.movePointLeft(1);
        
        if (fractionDigits > 0)
            if (totalDigits >= 0)
                result.setScale(Math.max(fractionDigits, totalDigits - digits));
            else
                result.setScale(fractionDigits);
        else if (fractionDigits == 0)
            result.setScale(0);
        
        return result.toString();
    }
    
    private String formatDuration(SchemaType sType) {
        XmlDuration d =
                (XmlDuration) sType.getFacet(SchemaType.FACET_MIN_INCLUSIVE);
        GDuration minInclusive = null;
        if (d != null)
            minInclusive = d.getGDurationValue();
        
        d = (XmlDuration) sType.getFacet(SchemaType.FACET_MAX_INCLUSIVE);
        GDuration maxInclusive = null;
        if (d != null)
            maxInclusive = d.getGDurationValue();
        
        d = (XmlDuration) sType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE);
        GDuration minExclusive = null;
        if (d != null)
            minExclusive = d.getGDurationValue();
        
        d = (XmlDuration) sType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE);
        GDuration maxExclusive = null;
        if (d != null)
            maxExclusive = d.getGDurationValue();
        
        GDurationBuilder gdurb = new GDurationBuilder();
        BigInteger min, max;
        
        gdurb.setSecond(pick(800000));
        gdurb.setMonth(pick(20));
        
        // Years
        // Months
        // Days
        // Hours
        // Minutes
        // Seconds
        // Fractions
        if (minInclusive != null) {
            if (gdurb.getYear() < minInclusive.getYear())
                gdurb.setYear(minInclusive.getYear());
            if (gdurb.getMonth() < minInclusive.getMonth())
                gdurb.setMonth(minInclusive.getMonth());
            if (gdurb.getDay() < minInclusive.getDay())
                gdurb.setDay(minInclusive.getDay());
            if (gdurb.getHour() < minInclusive.getHour())
                gdurb.setHour(minInclusive.getHour());
            if (gdurb.getMinute() < minInclusive.getMinute())
                gdurb.setMinute(minInclusive.getMinute());
            if (gdurb.getSecond() < minInclusive.getSecond())
                gdurb.setSecond(minInclusive.getSecond());
            if (gdurb.getFraction().compareTo(minInclusive.getFraction()) < 0)
                gdurb.setFraction(minInclusive.getFraction());
        }
        
        if (maxInclusive != null) {
            if (gdurb.getYear() > maxInclusive.getYear())
                gdurb.setYear(maxInclusive.getYear());
            if (gdurb.getMonth() > maxInclusive.getMonth())
                gdurb.setMonth(maxInclusive.getMonth());
            if (gdurb.getDay() > maxInclusive.getDay())
                gdurb.setDay(maxInclusive.getDay());
            if (gdurb.getHour() > maxInclusive.getHour())
                gdurb.setHour(maxInclusive.getHour());
            if (gdurb.getMinute() > maxInclusive.getMinute())
                gdurb.setMinute(maxInclusive.getMinute());
            if (gdurb.getSecond() > maxInclusive.getSecond())
                gdurb.setSecond(maxInclusive.getSecond());
            if (gdurb.getFraction().compareTo(maxInclusive.getFraction()) > 0)
                gdurb.setFraction(maxInclusive.getFraction());
        }
        
        if (minExclusive != null) {
            if (gdurb.getYear() <= minExclusive.getYear())
                gdurb.setYear(minExclusive.getYear()+1);
            if (gdurb.getMonth() <= minExclusive.getMonth())
                gdurb.setMonth(minExclusive.getMonth()+1);
            if (gdurb.getDay() <= minExclusive.getDay())
                gdurb.setDay(minExclusive.getDay()+1);
            if (gdurb.getHour() <= minExclusive.getHour())
                gdurb.setHour(minExclusive.getHour()+1);
            if (gdurb.getMinute() <= minExclusive.getMinute())
                gdurb.setMinute(minExclusive.getMinute()+1);
            if (gdurb.getSecond() <= minExclusive.getSecond())
                gdurb.setSecond(minExclusive.getSecond()+1);
            if (gdurb.getFraction().compareTo(minExclusive.getFraction()) <= 0)
                gdurb.setFraction(minExclusive.getFraction().add(new BigDecimal(0.001)));
        }
        
        if (maxExclusive != null) {
            if (gdurb.getYear() > maxExclusive.getYear())
                gdurb.setYear(maxExclusive.getYear());
            if (gdurb.getMonth() > maxExclusive.getMonth())
                gdurb.setMonth(maxExclusive.getMonth());
            if (gdurb.getDay() > maxExclusive.getDay())
                gdurb.setDay(maxExclusive.getDay());
            if (gdurb.getHour() > maxExclusive.getHour())
                gdurb.setHour(maxExclusive.getHour());
            if (gdurb.getMinute() > maxExclusive.getMinute())
                gdurb.setMinute(maxExclusive.getMinute());
            if (gdurb.getSecond() > maxExclusive.getSecond())
                gdurb.setSecond(maxExclusive.getSecond());
            if (gdurb.getFraction().compareTo(maxExclusive.getFraction()) > 0)
                gdurb.setFraction(maxExclusive.getFraction());
        }
        
        gdurb.normalize();
        return gdurb.toString();
    }
    
    private String formatDate(SchemaType sType) {
        GDateBuilder gdateb = new GDateBuilder(new Date(1000L * pick(365 * 24 * 60 * 60) + (30L + pick(20)) * 365 * 24 * 60 * 60 * 1000));
        GDate min = null, max = null;
        GDate temp;
        
        // Find the min and the max according to the type
        switch (sType.getPrimitiveType().getBuiltinTypeCode()) {
            case SchemaType.BTC_DATE_TIME:
            {
                XmlDateTime x = (XmlDateTime) sType.getFacet(SchemaType.FACET_MIN_INCLUSIVE);
                if (x != null)
                    min = x.getGDateValue();
                x = (XmlDateTime) sType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE);
                if (x != null)
                    if (min == null || min.compareToGDate(x.getGDateValue()) <= 0)
                        min = x.getGDateValue();
                
                x = (XmlDateTime) sType.getFacet(SchemaType.FACET_MAX_INCLUSIVE);
                if (x != null)
                    max = x.getGDateValue();
                x = (XmlDateTime) sType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE);
                if (x != null)
                    if (max == null || max.compareToGDate(x.getGDateValue()) >= 0)
                        max = x.getGDateValue();
                break;
            }
            case SchemaType.BTC_TIME:
            {
                XmlTime x = (XmlTime) sType.getFacet(SchemaType.FACET_MIN_INCLUSIVE);
                if (x != null)
                    min = x.getGDateValue();
                x = (XmlTime) sType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE);
                if (x != null)
                    if (min == null || min.compareToGDate(x.getGDateValue()) <= 0)
                        min = x.getGDateValue();
                
                x = (XmlTime) sType.getFacet(SchemaType.FACET_MAX_INCLUSIVE);
                if (x != null)
                    max = x.getGDateValue();
                x = (XmlTime) sType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE);
                if (x != null)
                    if (max == null || max.compareToGDate(x.getGDateValue()) >= 0)
                        max = x.getGDateValue();
                break;
            }
            case SchemaType.BTC_DATE:
            {
                XmlDate x = (XmlDate) sType.getFacet(SchemaType.FACET_MIN_INCLUSIVE);
                if (x != null)
                    min = x.getGDateValue();
                x = (XmlDate) sType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE);
                if (x != null)
                    if (min == null || min.compareToGDate(x.getGDateValue()) <= 0)
                        min = x.getGDateValue();
                
                x = (XmlDate) sType.getFacet(SchemaType.FACET_MAX_INCLUSIVE);
                if (x != null)
                    max = x.getGDateValue();
                x = (XmlDate) sType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE);
                if (x != null)
                    if (max == null || max.compareToGDate(x.getGDateValue()) >= 0)
                        max = x.getGDateValue();
                break;
            }
            case SchemaType.BTC_G_YEAR_MONTH:
            {
                XmlGYearMonth x = (XmlGYearMonth) sType.getFacet(SchemaType.FACET_MIN_INCLUSIVE);
                if (x != null)
                    min = x.getGDateValue();
                x = (XmlGYearMonth) sType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE);
                if (x != null)
                    if (min == null || min.compareToGDate(x.getGDateValue()) <= 0)
                        min = x.getGDateValue();
                
                x = (XmlGYearMonth) sType.getFacet(SchemaType.FACET_MAX_INCLUSIVE);
                if (x != null)
                    max = x.getGDateValue();
                x = (XmlGYearMonth) sType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE);
                if (x != null)
                    if (max == null || max.compareToGDate(x.getGDateValue()) >= 0)
                        max = x.getGDateValue();
                break;
            }
            case SchemaType.BTC_G_YEAR:
            {
                XmlGYear x = (XmlGYear) sType.getFacet(SchemaType.FACET_MIN_INCLUSIVE);
                if (x != null)
                    min = x.getGDateValue();
                x = (XmlGYear) sType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE);
                if (x != null)
                    if (min == null || min.compareToGDate(x.getGDateValue()) <= 0)
                        min = x.getGDateValue();
                
                x = (XmlGYear) sType.getFacet(SchemaType.FACET_MAX_INCLUSIVE);
                if (x != null)
                    max = x.getGDateValue();
                x = (XmlGYear) sType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE);
                if (x != null)
                    if (max == null || max.compareToGDate(x.getGDateValue()) >= 0)
                        max = x.getGDateValue();
                break;
            }
            case SchemaType.BTC_G_MONTH_DAY:
            {
                XmlGMonthDay x = (XmlGMonthDay) sType.getFacet(SchemaType.FACET_MIN_INCLUSIVE);
                if (x != null)
                    min = x.getGDateValue();
                x = (XmlGMonthDay) sType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE);
                if (x != null)
                    if (min == null || min.compareToGDate(x.getGDateValue()) <= 0)
                        min = x.getGDateValue();
                
                x = (XmlGMonthDay) sType.getFacet(SchemaType.FACET_MAX_INCLUSIVE);
                if (x != null)
                    max = x.getGDateValue();
                x = (XmlGMonthDay) sType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE);
                if (x != null)
                    if (max == null || max.compareToGDate(x.getGDateValue()) >= 0)
                        max = x.getGDateValue();
                break;
            }
            case SchemaType.BTC_G_DAY:
            {
                XmlGDay x = (XmlGDay) sType.getFacet(SchemaType.FACET_MIN_INCLUSIVE);
                if (x != null)
                    min = x.getGDateValue();
                x = (XmlGDay) sType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE);
                if (x != null)
                    if (min == null || min.compareToGDate(x.getGDateValue()) <= 0)
                        min = x.getGDateValue();
                
                x = (XmlGDay) sType.getFacet(SchemaType.FACET_MAX_INCLUSIVE);
                if (x != null)
                    max = x.getGDateValue();
                x = (XmlGDay) sType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE);
                if (x != null)
                    if (max == null || max.compareToGDate(x.getGDateValue()) >= 0)
                        max = x.getGDateValue();
                break;
            }
            case SchemaType.BTC_G_MONTH:
            {
                XmlGMonth x = (XmlGMonth) sType.getFacet(SchemaType.FACET_MIN_INCLUSIVE);
                if (x != null)
                    min = x.getGDateValue();
                x = (XmlGMonth) sType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE);
                if (x != null)
                    if (min == null || min.compareToGDate(x.getGDateValue()) <= 0)
                        min = x.getGDateValue();
                
                x = (XmlGMonth) sType.getFacet(SchemaType.FACET_MAX_INCLUSIVE);
                if (x != null)
                    max = x.getGDateValue();
                x = (XmlGMonth) sType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE);
                if (x != null)
                    if (max == null || max.compareToGDate(x.getGDateValue()) >= 0)
                        max = x.getGDateValue();
                break;
            }
        }
        
        if (min != null && max == null) {
            if (min.compareToGDate(gdateb) >= 0) {
                // Reset the date to min + (1-8) hours
                Calendar c = gdateb.getCalendar();
                c.add(Calendar.HOUR_OF_DAY, pick(8));
                gdateb = new GDateBuilder(c);
            }
        } else if (min == null && max != null) {
            if (max.compareToGDate(gdateb) <= 0) {
                // Reset the date to max - (1-8) hours
                Calendar c = gdateb.getCalendar();
                c.add(Calendar.HOUR_OF_DAY, 0-pick(8));
                gdateb = new GDateBuilder(c);
            }
        } else if (min != null && max != null) {
            if (min.compareToGDate(gdateb) >= 0 || max.compareToGDate(gdateb) <= 0) {
                // Find a date between the two
                Calendar c = min.getCalendar();
                Calendar cmax = max.getCalendar();
                c.add(Calendar.HOUR_OF_DAY, 1);
                if (c.after(cmax)) {
                    c.add(Calendar.HOUR_OF_DAY, -1);
                    c.add(Calendar.MINUTE, 1);
                    if (c.after(cmax)) {
                        c.add(Calendar.MINUTE, -1);
                        c.add(Calendar.SECOND, 1);
                        if (c.after(cmax)) {
                            c.add(Calendar.SECOND, -1);
                            c.add(Calendar.MILLISECOND, 1);
                            if (c.after(cmax))
                                c.add(Calendar.MILLISECOND, -1);
                        }
                    }
                }
                gdateb = new GDateBuilder(c);
            }
        }
        
        gdateb.setBuiltinTypeCode(sType.getPrimitiveType().getBuiltinTypeCode());
        if (pick(2) == 0)
            gdateb.clearTimeZone();
        return gdateb.toString();
    }
    
    private SchemaType closestBuiltin(SchemaType sType) {
        while (!sType.isBuiltinType())
            sType = sType.getBaseType();
        return sType;
    }
    
    /**
     * Cracks a combined QName of the form URL:localname
     */
    public static QName crackQName(String qName) {
        String ns;
        String name;
        
        int index = qName.lastIndexOf( ':' );
        if (index >= 0) {
            ns   = qName.substring( 0, index );
            name = qName.substring( index + 1);
        } else {
            ns   = ""; // NOI18N
            name = qName;
        }
        
        return new QName(ns, name);
    }
    
    
    /**
     * Cursor position:
     * Before this call:
     * <outer><foo/>^</outer>  (cursor at the ^)
     * After this call:
     * <<outer><foo/><bar/>som text<etc/>^</outer>
     */
    private void processParticle(SchemaParticle sp, XmlCursor xmlc, boolean mixed) {
        int loop = determineMinMaxForSample(sp, xmlc);
        
        while (loop-- > 0) {
            switch (sp.getParticleType()) {
                case (SchemaParticle.ELEMENT) :
                    processElement(sp, xmlc, mixed);
                    break;
                case (SchemaParticle.SEQUENCE) :
                    processSequence(sp, xmlc, mixed);
                    break;
                case (SchemaParticle.CHOICE) :
                    processChoice(sp, xmlc, mixed);
                    break;
                case (SchemaParticle.ALL) :
                    processAll(sp, xmlc, mixed);
                    break;
                case (SchemaParticle.WILDCARD) :
                    processWildCard(sp, xmlc, mixed);
                    break;
                default :
                    // throw new Exception("No Match on Schema Particle Type: " + String.valueOf(sp.getParticleType()));
            }
        }
    }
    
    private int determineMinMaxForSample(SchemaParticle sp, XmlCursor xmlc) {
        int minOccurs = sp.getIntMinOccurs();
        int maxOccurs = sp.getIntMaxOccurs();
        
        if (minOccurs == maxOccurs)
            return minOccurs;
        
        if( minOccurs == 0 && ignoreOptional )
            return 0;
        
        int result = minOccurs;
        if (result == 0)
            result = 1;
        
        if (sp.getParticleType() != SchemaParticle.ELEMENT)
            return result;
        
        // it probably only makes sense to put comments in front of individual elements that repeat
        
        if (sp.getMaxOccurs() == null) {
            // xmlc.insertComment("The next " + getItemNameOrType(sp, xmlc) + " may be repeated " + minOccurs + " or more times");
            if (minOccurs == 0)
                xmlc.insertComment(NbBundle.getMessage(SampleXmlUtil.class, "LBL_Zero_or_more_repetitions"));  // NOI18N
            else
                xmlc.insertComment(NbBundle.getMessage(SampleXmlUtil.class,
                        "LBL_N_or_more_repetitions", "" + minOccurs));  // NOI18N
        } else if (sp.getIntMaxOccurs() > 1) {
            xmlc.insertComment(NbBundle.getMessage(SampleXmlUtil.class,
                    "LBL_N_to_M_repetitions", "" + minOccurs, String.valueOf(sp.getMaxOccurs())));  // NOI18N
        } else {
            xmlc.insertComment(NbBundle.getMessage(SampleXmlUtil.class, "LBL_Optional"));  // NOI18N
        }
        return result;
    }
    
    /*
     Return a name for the element or the particle type to use in the comment for minoccurs, max occurs
     */
    private String getItemNameOrType(SchemaParticle sp, XmlCursor xmlc) {
        String elementOrTypeName = null;
        if (sp.getParticleType() == SchemaParticle.ELEMENT) {
            elementOrTypeName = "Element (" + sp.getName().getLocalPart() + ")"; // NOI18N
        } else {
            elementOrTypeName = printParticleType(sp.getParticleType());
        }
        return elementOrTypeName;
    }
    
    private void processElement(SchemaParticle sp, XmlCursor xmlc, boolean mixed) {
        // cast as schema local element
        SchemaLocalElement element = (SchemaLocalElement) sp;
        /// ^  -> <elemenname></elem>^
        if (_soapEnc)
            xmlc.insertElement(element.getName().getLocalPart()); // soap encoded? drop namespaces.
        else
            xmlc.insertElement(element.getName().getLocalPart(), element.getName().getNamespaceURI());
        /// -> <elem>^</elem>
        // processAttributes( sp.getType(), xmlc );
        
        xmlc.toPrevToken();
        // -> <elem>stuff^</elem>
        
        createSampleForType(element.getType(), xmlc);
        // -> <elem>stuff</elem>^
        xmlc.toNextToken();
        
    }
    
    private void moveToken(int numToMove, XmlCursor xmlc) {
        for (int i = 0; i < Math.abs(numToMove); i++) {
            if (numToMove < 0) {
                xmlc.toPrevToken();
            } else {
                xmlc.toNextToken();
            }
        }
    }
    
    private static final String formatQName(XmlCursor xmlc, QName qName) {
        XmlCursor parent = xmlc.newCursor();
        parent.toParent();
        String prefix = parent.prefixForNamespace(qName.getNamespaceURI());
        parent.dispose();
        String name;
        if (prefix == null || prefix.length() == 0)
            name = qName.getLocalPart();
        else
            name = prefix + ":" + qName.getLocalPart(); // NOI18N
        return name;
    }
    
    private static final QName HREF = new QName("href"); // NOI18N
    private static final QName ID = new QName("id"); // NOI18N
    public static final QName XSI_TYPE = new QName("http://www.w3.org/2001/XMLSchema-instance", "type"); // NOI18N
    private static final QName ENC_ARRAYTYPE = new QName("http://schemas.xmlsoap.org/soap/encoding/", "arrayType"); // NOI18N
    private static final QName ENC_OFFSET = new QName("http://schemas.xmlsoap.org/soap/encoding/", "offset"); // NOI18N
    
    private static final Set SKIPPED_SOAP_ATTRS = new HashSet(Arrays.asList(new QName[] { HREF, ID, ENC_OFFSET}));
    private void processAttributes(SchemaType stype, XmlCursor xmlc) {
        if (_soapEnc) {
            QName typeName = stype.getName();
            if (typeName != null) {
                xmlc.insertAttributeWithValue(XSI_TYPE, formatQName(xmlc, typeName));
            }
        }
        
        SchemaProperty[] attrProps = stype.getAttributeProperties();
        for (int i = 0; i < attrProps.length; i++) {
            SchemaProperty attr = attrProps[i];
            if (_soapEnc) {
                if (SKIPPED_SOAP_ATTRS.contains(attr.getName()))
                    continue;
                if (ENC_ARRAYTYPE.equals(attr.getName())) {
                    SOAPArrayType arrayType = ((SchemaWSDLArrayType)stype.getAttributeModel().getAttribute(attr.getName())).getWSDLArrayType();
                    if (arrayType != null)
                        xmlc.insertAttributeWithValue(attr.getName(), formatQName(xmlc, arrayType.getQName()) + arrayType.soap11DimensionString());
                    continue;
                }
            }
            String defaultValue = attr.getDefaultText();
            xmlc.insertAttributeWithValue(attr.getName(), defaultValue == null ?
                sampleDataForSimpleType(attr.getType()) : defaultValue);
        }
    }
    
    private void processSequence(SchemaParticle sp, XmlCursor xmlc, boolean mixed) {
        SchemaParticle[] spc = sp.getParticleChildren();
        for (int i=0; i < spc.length; i++) {
            /// <parent>maybestuff^</parent>
            processParticle(spc[i], xmlc, mixed);
            //<parent>maybestuff...morestuff^</parent>
            if (mixed && i < spc.length-1)
                xmlc.insertChars(pick(WORDS));
        }
    }
    
    private void processChoice(SchemaParticle sp, XmlCursor xmlc, boolean mixed) {
        SchemaParticle[] spc = sp.getParticleChildren();
        xmlc.insertComment("You have a CHOICE of the next " + String.valueOf(spc.length) + " items at this level"); // NOI18N
        for (int i=0; i < spc.length; i++) {
            processParticle(spc[i], xmlc, mixed);
        }
    }
    
    private void processAll(SchemaParticle sp, XmlCursor xmlc, boolean mixed) {
        SchemaParticle[] spc = sp.getParticleChildren();
        // xmlc.insertComment("You may enter the following " + String.valueOf(spc.length) + " items in any order");
        for (int i=0; i < spc.length; i++) {
            processParticle(spc[i], xmlc, mixed);
            if (mixed && i < spc.length-1)
                xmlc.insertChars(pick(WORDS));
        }
    }
    
    private void processWildCard(SchemaParticle sp, XmlCursor xmlc, boolean mixed) {
        xmlc.insertComment("You may enter ANY elements at this point"); // NOI18N
        xmlc.insertElement("AnyElement"); // NOI18N
    }
    
    /**
     * This method will get the base type for the schema type
     */
    
    private static QName getClosestName(SchemaType sType) {
        while (sType.getName() == null)
            sType = sType.getBaseType();
        
        return sType.getName();
    }
    
    private String printParticleType(int particleType) {
        StringBuffer returnParticleType = new StringBuffer();
        returnParticleType.append("Schema Particle Type: "); // NOI18N
        
        switch (particleType) {
            case SchemaParticle.ALL :
                returnParticleType.append("ALL\n"); // NOI18N
                break;
            case SchemaParticle.CHOICE :
                returnParticleType.append("CHOICE\n"); // NOI18N
                break;
            case SchemaParticle.ELEMENT :
                returnParticleType.append("ELEMENT\n"); // NOI18N
                break;
            case SchemaParticle.SEQUENCE :
                returnParticleType.append("SEQUENCE\n"); // NOI18N
                break;
            case SchemaParticle.WILDCARD :
                returnParticleType.append("WILDCARD\n"); // NOI18N
                break;
            default :
                returnParticleType.append("Schema Particle Type Unknown"); // NOI18N
                break;
        }
        
        return returnParticleType.toString();
    }
    
    private ArrayList _typeStack = new ArrayList();
    
    public boolean isIgnoreOptional() {
        return ignoreOptional;
    }
    
    public void setIgnoreOptional(boolean ignoreOptional) {
        this.ignoreOptional = ignoreOptional;
    }
}
