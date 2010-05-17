/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
/*
 * ISO4217CurrencyCode.java
 *
 * Created on September 21, 2005, 12:06 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.visualweb.faces.dt.converter;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.NbBundle;

/**
 * The class lists all the currency code defined by ISO 4217
 *
 * @author cao
 */
public class ISO4217CurrencyCode {

    // I got the list from http://www.xe.com/iso4217.htm
    // Note: some of them are commented out because the java.util.Currency throws IllegalArgumentException on them in JDK1.4
    private static String[] descriptions = new String[] {
                "AED " + NbBundle.getMessage( ISO4217CurrencyCode.class, "AED" ), // United Arab Emirates, Dirhams
                "AFA " + NbBundle.getMessage( ISO4217CurrencyCode.class, "AFA" ), // Afghanistan, Afghanis",
                "ALL " + NbBundle.getMessage( ISO4217CurrencyCode.class, "ALL" ), // Albania, Leke",
                "AMD " + NbBundle.getMessage( ISO4217CurrencyCode.class, "AMD" ), // Armenia, Drams",
                "ANG " + NbBundle.getMessage( ISO4217CurrencyCode.class, "ANG" ), // Netherlands Antilles, Guilders (also called Florins)",
                "AOA " + NbBundle.getMessage( ISO4217CurrencyCode.class, "AOA" ), // Angola, Kwanza",
                "ARS " + NbBundle.getMessage( ISO4217CurrencyCode.class, "ARS" ), // Argentina, Pesos",
                "AUD " + NbBundle.getMessage( ISO4217CurrencyCode.class, "AUD" ), // Australia, Dollars",
                "AWG " + NbBundle.getMessage( ISO4217CurrencyCode.class, "AWG" ), // Aruba, Guilders (also called Florins)",
                "AZM " + NbBundle.getMessage( ISO4217CurrencyCode.class, "AZM" ), // Azerbaijan, Manats",
                "BAM " + NbBundle.getMessage( ISO4217CurrencyCode.class, "BAM" ), // Bosnia and Herzegovina, Convertible Marka",
                "BBD " + NbBundle.getMessage( ISO4217CurrencyCode.class, "BBD" ), // Barbados, Dollars",
                "BDT " + NbBundle.getMessage( ISO4217CurrencyCode.class, "BDT" ), // Bangladesh, Taka",
                "BGN " + NbBundle.getMessage( ISO4217CurrencyCode.class, "BGN" ), // Bulgaria, Leva",
                "BHD " + NbBundle.getMessage( ISO4217CurrencyCode.class, "BHD" ), // Bahrain, Dinars",
                "BIF " + NbBundle.getMessage( ISO4217CurrencyCode.class, "BIF" ), // Burundi, Francs",
                "BMD " + NbBundle.getMessage( ISO4217CurrencyCode.class, "BMD" ), // Bermuda, Dollars",
                "BND " + NbBundle.getMessage( ISO4217CurrencyCode.class, "BND" ), // Brunei Darussalam, Dollars",
                "BOB " + NbBundle.getMessage( ISO4217CurrencyCode.class, "BOB" ), // Bolivia, Bolivianos",
                "BRL " + NbBundle.getMessage( ISO4217CurrencyCode.class, "BRL" ), // Brazil, Brazil Real",
                "BSD " + NbBundle.getMessage( ISO4217CurrencyCode.class, "BSD" ), // Bahamas, Dollars",
                "BTN " + NbBundle.getMessage( ISO4217CurrencyCode.class, "BTN" ), // Bhutan, Ngultrum",
                "BWP " + NbBundle.getMessage( ISO4217CurrencyCode.class, "BWP" ), // Botswana, Pulas",
                "BYR " + NbBundle.getMessage( ISO4217CurrencyCode.class, "BYR" ), // Belarus, Rubles",
                "BZD " + NbBundle.getMessage( ISO4217CurrencyCode.class, "BZD" ), // Belize, Dollars",
                "CAD " + NbBundle.getMessage( ISO4217CurrencyCode.class, "CAD" ), // Canada, Dollars",
                "CDF " + NbBundle.getMessage( ISO4217CurrencyCode.class, "CDF" ), // Congo/Kinshasa, Congolese Francs",
                "CHF " + NbBundle.getMessage( ISO4217CurrencyCode.class, "CHF" ), // Switzerland, Francs",
                "CLP " + NbBundle.getMessage( ISO4217CurrencyCode.class, "CLP" ), // Chile, Pesos",
                "CNY " + NbBundle.getMessage( ISO4217CurrencyCode.class, "CNY" ), // China, Yuan Renminbi",
                "COP " + NbBundle.getMessage( ISO4217CurrencyCode.class, "COP" ), // Colombia, Pesos",
                "CRC " + NbBundle.getMessage( ISO4217CurrencyCode.class, "CRC" ), // Costa Rica, Colones",
                //"CSD Serbia, Dinars",
                "CUP " + NbBundle.getMessage( ISO4217CurrencyCode.class, "CUP" ), // Cuba, Pesos",
                "CVE " + NbBundle.getMessage( ISO4217CurrencyCode.class, "CVE" ), // Cape Verde, Escudos",
                "CYP " + NbBundle.getMessage( ISO4217CurrencyCode.class, "CYP" ), // Cyprus, Pounds",
                "CZK " + NbBundle.getMessage( ISO4217CurrencyCode.class, "CZK" ), // Czech Republic, Koruny",
                "DJF " + NbBundle.getMessage( ISO4217CurrencyCode.class, "DJF" ), // Djibouti, Francs",
                "DKK " + NbBundle.getMessage( ISO4217CurrencyCode.class, "DKK" ), // Denmark, Kroner",
                "DOP " + NbBundle.getMessage( ISO4217CurrencyCode.class, "DOP" ), // Dominican Republic, Pesos",
                "DZD " + NbBundle.getMessage( ISO4217CurrencyCode.class, "DZD" ), // Algeria, Algeria Dinars",
                "EEK " + NbBundle.getMessage( ISO4217CurrencyCode.class, "EEK" ), // Estonia, Krooni",
                "EGP " + NbBundle.getMessage( ISO4217CurrencyCode.class, "EGP" ), // Egypt, Pounds",
                "ERN " + NbBundle.getMessage( ISO4217CurrencyCode.class, "ERN" ), // Eritrea, Nakfa",
                "ETB " + NbBundle.getMessage( ISO4217CurrencyCode.class, "ETB" ), // Ethiopia, Birr",
                "EUR " + NbBundle.getMessage( ISO4217CurrencyCode.class, "EUR" ), // Euro Member Countries, Euro",
                "FJD " + NbBundle.getMessage( ISO4217CurrencyCode.class, "FJD" ), // Fiji, Dollars",
                "FKP " + NbBundle.getMessage( ISO4217CurrencyCode.class, "FKP" ), // Falkland Islands (Malvinas), Pounds",
                "GBP " + NbBundle.getMessage( ISO4217CurrencyCode.class, "GBP" ), // United Kingdom, Pounds",
                "GEL " + NbBundle.getMessage( ISO4217CurrencyCode.class, "GEL" ), // Georgia, Lari",
                //"GGP Guernsey, Pounds",
                "GHC " + NbBundle.getMessage( ISO4217CurrencyCode.class, "GHC" ), // Ghana, Cedis",
                "GIP " + NbBundle.getMessage( ISO4217CurrencyCode.class, "GIP" ), // Gibraltar, Pounds",
                "GMD " + NbBundle.getMessage( ISO4217CurrencyCode.class, "GMD" ), // Gambia, Dalasi",
                "GNF " + NbBundle.getMessage( ISO4217CurrencyCode.class, "GNF" ), // Guinea, Francs",
                "GTQ " + NbBundle.getMessage( ISO4217CurrencyCode.class, "GTQ" ), // Guatemala, Quetzales",
                "GYD " + NbBundle.getMessage( ISO4217CurrencyCode.class, "GYD" ), // Guyana, Dollars",
                "HKD " + NbBundle.getMessage( ISO4217CurrencyCode.class, "HKD" ), // Hong Kong, Dollars",
                "HNL " + NbBundle.getMessage( ISO4217CurrencyCode.class, "HNL" ), // Honduras, Lempiras",
                "HRK " + NbBundle.getMessage( ISO4217CurrencyCode.class, "HRK" ), // Croatia, Kuna",
                "HTG " + NbBundle.getMessage( ISO4217CurrencyCode.class, "HTG" ), // Haiti, Gourdes",
                "HUF " + NbBundle.getMessage( ISO4217CurrencyCode.class, "HUF" ), // Hungary, Forint",
                "IDR " + NbBundle.getMessage( ISO4217CurrencyCode.class, "IDR" ), // Indonesia, Rupiahs",
                "ILS " + NbBundle.getMessage( ISO4217CurrencyCode.class, "ILS" ), // Israel, New Shekels",
                //"IMP Isle of Man, Pounds",
                "INR " + NbBundle.getMessage( ISO4217CurrencyCode.class, "INR" ), // India, Rupees",
                "IQD " + NbBundle.getMessage( ISO4217CurrencyCode.class, "IQD" ), // Iraq, Dinars",
                "IRR " + NbBundle.getMessage( ISO4217CurrencyCode.class, "IRR" ), // Iran, Rials",
                "ISK " + NbBundle.getMessage( ISO4217CurrencyCode.class, "ISK" ), // Iceland, Kronur",
                //"JEP Jersey, Pounds",
                "JMD " + NbBundle.getMessage( ISO4217CurrencyCode.class, "JMD" ), // Jamaica, Dollars",
                "JOD " + NbBundle.getMessage( ISO4217CurrencyCode.class, "JOD" ), // Jordan, Dinars",
                "JPY " + NbBundle.getMessage( ISO4217CurrencyCode.class, "JPY" ), // Japan, Yen",
                "KES " + NbBundle.getMessage( ISO4217CurrencyCode.class, "KES" ), // Kenya, Shillings",
                "KGS " + NbBundle.getMessage( ISO4217CurrencyCode.class, "KGS" ), // Kyrgyzstan, Soms",
                "KHR " + NbBundle.getMessage( ISO4217CurrencyCode.class, "KHR" ), // Cambodia, Riels",
                "KMF " + NbBundle.getMessage( ISO4217CurrencyCode.class, "KMF" ), // Comoros, Francs",
                "KPW " + NbBundle.getMessage( ISO4217CurrencyCode.class, "KPW" ), // Korea (North), Won",
                "KRW " + NbBundle.getMessage( ISO4217CurrencyCode.class, "KRW" ), // Korea (South), Won",
                "KWD " + NbBundle.getMessage( ISO4217CurrencyCode.class, "KWD" ), // Kuwait, Dinars",
                "KYD " + NbBundle.getMessage( ISO4217CurrencyCode.class, "KYD" ), // Cayman Islands, Dollars",
                "KZT " + NbBundle.getMessage( ISO4217CurrencyCode.class, "KZT" ), // Kazakhstan, Tenge",
                "LAK " + NbBundle.getMessage( ISO4217CurrencyCode.class, "LAK" ), // Laos, Kips",
                "LBP " + NbBundle.getMessage( ISO4217CurrencyCode.class, "LBP" ), // Lebanon, Pounds",
                "LKR " + NbBundle.getMessage( ISO4217CurrencyCode.class, "LKR" ), // Sri Lanka, Rupees",
                "LRD " + NbBundle.getMessage( ISO4217CurrencyCode.class, "LRD" ), // Liberia, Dollars",
                "LSL " + NbBundle.getMessage( ISO4217CurrencyCode.class, "LSL" ), // Lesotho, Maloti",
                "LTL " + NbBundle.getMessage( ISO4217CurrencyCode.class, "LTL" ), // Lithuania, Litai",
                "LVL " + NbBundle.getMessage( ISO4217CurrencyCode.class, "LVL" ), // Latvia, Lati",
                "LYD " + NbBundle.getMessage( ISO4217CurrencyCode.class, "LYD" ), // Libya, Dinars",
                "MAD " + NbBundle.getMessage( ISO4217CurrencyCode.class, "MAD" ), // Morocco, Dirhams",
                "MDL " + NbBundle.getMessage( ISO4217CurrencyCode.class, "MDL" ), // Moldova, Lei",
                //"MGA Madagascar, Ariary",
                "MKD " + NbBundle.getMessage( ISO4217CurrencyCode.class, "MKD" ), // Macedonia, Denars",
                "MMK " + NbBundle.getMessage( ISO4217CurrencyCode.class, "MMK" ), // Myanmar (Burma), Kyats",
                "MNT " + NbBundle.getMessage( ISO4217CurrencyCode.class, "MNT" ), // Mongolia, Tugriks",
                "MOP " + NbBundle.getMessage( ISO4217CurrencyCode.class, "MOP" ), // Macau, Patacas",
                "MRO " + NbBundle.getMessage( ISO4217CurrencyCode.class, "MRO" ), // Mauritania, Ouguiyas",
                "MTL " + NbBundle.getMessage( ISO4217CurrencyCode.class, "MTL" ), // Malta, Liri",
                "MUR " + NbBundle.getMessage( ISO4217CurrencyCode.class, "MUR" ), // Mauritius, Rupees",
                "MVR " + NbBundle.getMessage( ISO4217CurrencyCode.class, "MVR" ), // Maldives (Maldive Islands), Rufiyaa",
                "MWK " + NbBundle.getMessage( ISO4217CurrencyCode.class, "MWK" ), // Malawi, Kwachas",
                "MXN " + NbBundle.getMessage( ISO4217CurrencyCode.class, "MXN" ), // Mexico, Pesos",
                "MYR " + NbBundle.getMessage( ISO4217CurrencyCode.class, "MYR" ), // Malaysia, Ringgits",
                "MZM " + NbBundle.getMessage( ISO4217CurrencyCode.class, "MZM" ), // Mozambique, Meticais",
                "NAD " + NbBundle.getMessage( ISO4217CurrencyCode.class, "NAD" ), // Namibia, Dollars",
                "NGN " + NbBundle.getMessage( ISO4217CurrencyCode.class, "NGN" ), // Nigeria, Nairas",
                "NIO " + NbBundle.getMessage( ISO4217CurrencyCode.class, "NIO" ), // Nicaragua, Cordobas",
                "NOK " + NbBundle.getMessage( ISO4217CurrencyCode.class, "NOK" ), // Norway, Krone",
                "NPR " + NbBundle.getMessage( ISO4217CurrencyCode.class, "NPR" ), // Nepal, Nepal Rupees",
                "NZD " + NbBundle.getMessage( ISO4217CurrencyCode.class, "NZD" ), // New Zealand, Dollars",
                "OMR " + NbBundle.getMessage( ISO4217CurrencyCode.class, "OMR" ), // Oman, Rials",
                "PAB " + NbBundle.getMessage( ISO4217CurrencyCode.class, "PAB" ), // Panama, Balboa",
                "PEN " + NbBundle.getMessage( ISO4217CurrencyCode.class, "PEN" ), // Peru, Nuevos Soles",
                "PGK " + NbBundle.getMessage( ISO4217CurrencyCode.class, "PGK" ), // Papua New Guinea, Kina",
                "PHP " + NbBundle.getMessage( ISO4217CurrencyCode.class, "PHP" ), // Philippines, Pesos",
                "PKR " + NbBundle.getMessage( ISO4217CurrencyCode.class, "PKR" ), // Pakistan, Rupees",
                "PLN " + NbBundle.getMessage( ISO4217CurrencyCode.class, "PLN" ), // Poland, Zlotych",
                "PYG " + NbBundle.getMessage( ISO4217CurrencyCode.class, "PYG" ), // Paraguay, Guarani",
                "QAR " + NbBundle.getMessage( ISO4217CurrencyCode.class, "QAR" ), // Qatar, Rials",
                //"RON Romania, New Lei",
                "RUB " + NbBundle.getMessage( ISO4217CurrencyCode.class, "RUB" ), // Russia, Rubles",
                "RWF " + NbBundle.getMessage( ISO4217CurrencyCode.class, "RWF" ), // Rwanda, Rwanda Francs",
                "SAR " + NbBundle.getMessage( ISO4217CurrencyCode.class, "SAR" ), // Saudi Arabia, Riyals",
                "SBD " + NbBundle.getMessage( ISO4217CurrencyCode.class, "SBD" ), // Solomon Islands, Dollars",
                "SCR " + NbBundle.getMessage( ISO4217CurrencyCode.class, "SCR" ), // Seychelles, Rupees",
                "SDD " + NbBundle.getMessage( ISO4217CurrencyCode.class, "SDD" ), // Sudan, Dinars",
                "SEK " + NbBundle.getMessage( ISO4217CurrencyCode.class, "SEK" ), // Sweden, Kronor",
                "SGD " + NbBundle.getMessage( ISO4217CurrencyCode.class, "SGD" ), // Singapore, Dollars",
                "SHP " + NbBundle.getMessage( ISO4217CurrencyCode.class, "SHP" ), // Saint Helena, Pounds",
                "SIT " + NbBundle.getMessage( ISO4217CurrencyCode.class, "SIT" ), // Slovenia, Tolars",
                "SKK " + NbBundle.getMessage( ISO4217CurrencyCode.class, "SKK" ), // Slovakia, Koruny",
                "SLL " + NbBundle.getMessage( ISO4217CurrencyCode.class, "SLL" ), // Sierra Leone, Leones",
                "SOS " + NbBundle.getMessage( ISO4217CurrencyCode.class, "SOS" ), // Somalia, Shillings",
                //"SPL Seborga, Luigini",
                //"SRD Suriname, Dollars",
                "STD " + NbBundle.getMessage( ISO4217CurrencyCode.class, "STD" ), // Sao Tome and Principe, Dobras",
                "SVC " + NbBundle.getMessage( ISO4217CurrencyCode.class, "SVC" ), // El Salvador, Colones",
                "SYP " + NbBundle.getMessage( ISO4217CurrencyCode.class, "SYP" ), // Syria, Pounds",
                "SZL " + NbBundle.getMessage( ISO4217CurrencyCode.class, "SZL" ), // Swaziland, Emalangeni",
                "THB " + NbBundle.getMessage( ISO4217CurrencyCode.class, "THB" ), // Thailand, Baht",
                "TJS " + NbBundle.getMessage( ISO4217CurrencyCode.class, "TJS" ), // Tajikistan, Somoni",
                "TMM " + NbBundle.getMessage( ISO4217CurrencyCode.class, "TMM" ), // Turkmenistan, Manats",
                "TND " + NbBundle.getMessage( ISO4217CurrencyCode.class, "TND" ), // Tunisia, Dinars",
                "TOP " + NbBundle.getMessage( ISO4217CurrencyCode.class, "TOP" ), // Tonga, Pa\'anga",
                "TRL " + NbBundle.getMessage( ISO4217CurrencyCode.class, "TRL" ), // Turkey, Liras [being phased out]",
                //"TRY Turkey, New Lira",
                "TTD " + NbBundle.getMessage( ISO4217CurrencyCode.class, "TTD" ), // Trinidad and Tobago, Dollars",
                //"TVD Tuvalu, Tuvalu Dollars",
                "TWD " + NbBundle.getMessage( ISO4217CurrencyCode.class, "TWD" ), // Taiwan, New Dollars",
                "TZS " + NbBundle.getMessage( ISO4217CurrencyCode.class, "TZS" ), // Tanzania, Shillings",
                "UAH " + NbBundle.getMessage( ISO4217CurrencyCode.class, "UAH" ), // Ukraine, Hryvnia",
                "UGX " + NbBundle.getMessage( ISO4217CurrencyCode.class, "UGX" ), // Uganda, Shillings",
                "USD " + NbBundle.getMessage( ISO4217CurrencyCode.class, "USD" ), // United States of America, Dollars",
                "UYU " + NbBundle.getMessage( ISO4217CurrencyCode.class, "UYU" ), // Uruguay, Pesos",
                "UZS " + NbBundle.getMessage( ISO4217CurrencyCode.class, "UZS" ), // Uzbekistan, Sums",
                "VEB " + NbBundle.getMessage( ISO4217CurrencyCode.class, "VEB" ), // Venezuela, Bolivares",
                "VND " + NbBundle.getMessage( ISO4217CurrencyCode.class, "VND" ), // Viet Nam, Dong",
                "VUV " + NbBundle.getMessage( ISO4217CurrencyCode.class, "VUV" ), // Vanuatu, Vatu",
                "WST " + NbBundle.getMessage( ISO4217CurrencyCode.class, "WST" ), // Samoa, Tala",
                "XAF " + NbBundle.getMessage( ISO4217CurrencyCode.class, "XAF" ), // Communaute Financiere Africaine BEAC, Francs",
                "XAG " + NbBundle.getMessage( ISO4217CurrencyCode.class, "XAG" ), // Silver, Ounces",
                "XAU " + NbBundle.getMessage( ISO4217CurrencyCode.class, "XAU" ), // Gold, Ounces",
                "XCD " + NbBundle.getMessage( ISO4217CurrencyCode.class, "XCD" ), // East Caribbean Dollars",
                "XDR " + NbBundle.getMessage( ISO4217CurrencyCode.class, "XDR" ), // International Monetary Fund (IMF) Special Drawing Rights",
                "XOF " + NbBundle.getMessage( ISO4217CurrencyCode.class, "XOF" ), // Communaute Financiere Africaine BCEAO, Francs",
                "XPD " + NbBundle.getMessage( ISO4217CurrencyCode.class, "XPD" ), // Palladium Ounces",
                "XPF " + NbBundle.getMessage( ISO4217CurrencyCode.class, "XPF" ), // Comptoirs Fran?ais du Pacifique Francs",
                "XPT " + NbBundle.getMessage( ISO4217CurrencyCode.class, "XPT" ), // Platinum, Ounces",
                "YER " + NbBundle.getMessage( ISO4217CurrencyCode.class, "YER" ), // Yemen, Rials",
                "ZAR " + NbBundle.getMessage( ISO4217CurrencyCode.class, "ZAR" ), // South Africa, Rand",
                "ZMK " + NbBundle.getMessage( ISO4217CurrencyCode.class, "ZMK" ), // Zambia, Kwacha",
                "ZWD " + NbBundle.getMessage( ISO4217CurrencyCode.class, "ZWD" ) // Zimbabwe, Zimbabwe Dollars" 
    };
         
    // Note: the codes should be in the same order as the descriptions
    private static Map codesMap = new HashMap();
    static {
        for( int i = 0; i < descriptions.length; i ++ ) {
            codesMap.put( descriptions[i].substring(0, 3), descriptions[i] );
        }
    }

    public static String getCode( int index ) {
        return descriptions[index].substring(0, 3);
    }

    public static String[] getDisplayNames() {

        return descriptions;
    }
    
    public static String getDisplayName( String code ) {
        return (String)codesMap.get(code);
    }
}
