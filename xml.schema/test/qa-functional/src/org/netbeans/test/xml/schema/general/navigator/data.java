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

package org.netbeans.test.xml.schema.general.navigator;

public class data
{
  public static final String sOriginal =

" 0:root"
+ " [unknown content] 0:version=\"1.0\" encoding=\"UTF-8\" 0:xs:schema targetNamespace="
+ "\"http://xml.ne... 1:xs:element name=\"autoLoanApplication\" 2:xs:annotation 3:xs:d"
+ "ocumentation (A&nbsp;loan&nbsp;application&nbsp;xml&nbsp;schema) 2:xs:complexTyp"
+ "e 3:xs:sequence 4:xs:element name=\"loan\", type=\"LoanType\" 4:xs:element name=\"ter"
+ "m\", type=\"xs:integer\" 4:xs:element name=\"amount\" 5:xs:simpleType 6:xs:restrictio"
+ "n base=\"xs:decimal\" 7:xs:minInclusive value=\"5000.00\" 7:xs:maxInclusive value=\"1"
+ "0000.00\" 7:xs:fractionDigits value=\"2\" 4:xs:element name=\"application\" 5:xs:simp"
+ "leType 6:xs:restriction base=\"xs:string\" 7:xs:enumeration value=\"individual\" 7:x"
+ "s:enumeration value=\"joint\" 4:xs:element name=\"state\", type=\"StateType\" 4:xs:ele"
+ "ment name=\"applicant\", type=\"Applic... 4:xs:element name=\"tradeIn\", minOccurs=\"0"
+ "\" 5:xs:complexType 6:xs:sequence 7:xs:element name=\"vehicle\", type=\"CarType\" 7:x"
+ "s:element name=\"monthlyPayment\" 8:xs:simpleType 9:xs:restriction base=\"xs:decima"
+ "l\" 10:xs:fractionDigits value=\"2\" 10:xs:minInclusive value=\"0.00\" 1:xs:simpleTyp"
+ "e name=\"LoanType\" 2:xs:restriction base=\"xs:string\" 3:xs:enumeration value=\"new\""
+ " 3:xs:enumeration value=\"used dealer\" 3:xs:enumeration value=\"used private\" 3:xs"
+ ":enumeration value=\"refinance\" 3:xs:enumeration value=\"lease buyout\" 1:xs:simple"
+ "Type name=\"StateType\" 2:xs:restriction base=\"xs:string\" 3:xs:enumeration value=\""
+ "AZ\" 3:xs:enumeration value=\"CA\" 3:xs:enumeration value=\"NY\" 3:xs:enumeration val"
+ "ue=\"TX\" 1:xs:complexType name=\"ApplicantType\" 2:xs:sequence 3:xs:element name=\"n"
+ "ame\" 4:xs:complexType 5:xs:sequence 6:xs:element name=\"first\", type=\"xs:string\" "
+ "6:xs:element name=\"middle\", type=\"xs:string... 6:xs:element name=\"last\", type=\"x"
+ "s:string\" 6:xs:element name=\"suffix\" 7:xs:simpleType 8:xs:restriction base=\"xs:s"
+ "tring\" 9:xs:enumeration value=\"Jr\" 9:xs:enumeration value=\"Sr\" 9:xs:enumeration "
+ "value=\"II\" 9:xs:enumeration value=\"III\" 9:xs:enumeration value=\"IV\" 9:xs:enumera"
+ "tion value=\"V\" 3:xs:element name=\"SSID\" 4:xs:simpleType 5:xs:restriction base=\"x"
+ "s:string\" 6:xs:pattern value=\"\\d{3}-\\d{2}-\\d{4}\" 3:xs:element name=\"birthDate\", "
+ "type=\"xs:dat... 3:xs:element name=\"homePhone\", type=\"PhoneN... 3:xs:element name"
+ "=\"cellPhone\", type=\"PhoneN... 3:xs:element name=\"emailAddress\" 4:xs:simpleType 5"
+ ":xs:restriction base=\"xs:string\" 3:xs:element name=\"residence\", type=\"Reside... "
+ "3:xs:element name=\"employment\" 4:xs:complexType 5:xs:sequence 6:xs:element name="
+ "\"status\" 7:xs:simpleType 8:xs:restriction base=\"xs:string\" 9:xs:enumeration valu"
+ "e=\"unemployed\" 9:xs:enumeration value=\"employed\" 9:xs:enumeration value=\"self em"
+ "ployed\" 6:xs:element name=\"detail\", minOccurs=\"0\" 7:xs:complexType 8:xs:sequence"
+ " 9:xs:element name=\"occupation\", type=\"xs:st... 9:xs:element name=\"employer\" 10:"
+ "xs:complexType 11:xs:sequence 12:xs:element name=\"name\" 12:xs:element name=\"addr"
+ "ess\", type=\"AddressT... 12:xs:element name=\"lengthOfEmployment\", typ... 9:xs:ele"
+ "ment name=\"workPhone\", type=\"PhoneN... 9:xs:element name=\"grossMonthlyIncome\" 10"
+ ":xs:simpleType 11:xs:restriction base=\"xs:decimal\" 12:xs:minInclusive value=\"0.0"
+ "0\" 12:xs:fractionDigits value=\"2\" 3:xs:element name=\"supplementalIncome\" 4:xs:co"
+ "mplexType 5:xs:sequence 6:xs:element name=\"source\" 7:xs:simpleType 8:xs:restrict"
+ "ion base=\"xs:string\" 9:xs:enumeration value=\"alimony\" 9:xs:enumeration value=\"ch"
+ "ild support\" 9:xs:enumeration value=\"investments\" 9:xs:enumeration value=\"part t"
+ "ime job\" 9:xs:enumeration value=\"rental property\" 9:xs:enumeration value=\"retire"
+ "ment income\" 9:xs:enumeration value=\"social security\" 6:xs:element name=\"grossMo"
+ "nthlyIncome\" 7:xs:simpleType 8:xs:restriction base=\"xs:decimal\" 9:xs:minInclusiv"
+ "e value=\"0.00\" 9:xs:fractionDigits value=\"2\" 1:xs:complexType name=\"AddressType\""
+ " 2:xs:sequence 3:xs:element name=\"address1\" 3:xs:element name=\"address2\" 3:xs:el"
+ "ement name=\"city\" 3:xs:element name=\"state\", type=\"StateType\" 3:xs:element name="
+ "\"zip\" 4:xs:simpleType 5:xs:restriction base=\"xs:string\" 6:xs:minLength value=\"5\""
+ " 6:xs:maxLength value=\"5\" 6:xs:pattern value=\"\\d{5}\" 1:xs:complexType name=\"Phon"
+ "eNumberType\" 2:xs:sequence 3:xs:element name=\"areaCode\" 3:xs:element name=\"excha"
+ "nge\" 3:xs:element name=\"number\" 3:xs:element name=\"extension\", minOccurs=\"0... 1"
+ ":xs:complexType name=\"OccupancyType\" 2:xs:sequence 3:xs:element name=\"type\" 4:xs"
+ ":simpleType 5:xs:restriction base=\"xs:string\" 6:xs:enumeration value=\"rent\" 6:xs"
+ ":enumeration value=\"own\" 3:xs:element name=\"movedIn\", type=\"xs:date\" 3:xs:elemen"
+ "t name=\"lengthOfOccupancy\", type... 3:xs:element name=\"payment\" 4:xs:simpleType "
+ "5:xs:restriction base=\"xs:decimal\" 6:xs:fractionDigits value=\"2\" 6:xs:minInclusi"
+ "ve value=\"0.00\" 1:xs:complexType name=\"ResidenceType\" 2:xs:sequence 3:xs:element"
+ " name=\"address\", type=\"AddressT... 3:xs:element name=\"occupancy\", type=\"Occupa.."
+ ". 1:xs:complexType name=\"CarType\" 2:xs:sequence 3:xs:element name=\"year\", type=\""
+ "xs:gYear\" 3:xs:element name=\"make\", type=\"xs:string\" 3:xs:element name=\"model\", "
+ "type=\"xs:string\" 3:xs:element name=\"VIN\", type=\"xs:string\" 1:xs:complexType name"
+ "=\"DurationType\" 2:xs:sequence 3:xs:element name=\"years\", type=\"xs:positiv... 3:x"
+ "s:element name=\"months\" 4:xs:simpleType 5:xs:restriction base=\"xs:positiveIntege"
+ "r\" 6:xs:minInclusive value=\"1\" 6:xs:maxInclusive value=\"12\"";

  public static final String sAttributes =

" 0:root [unknown content] 0:version=\"1.0\" encoding=\"UTF-8\" 0:xs"
+ ":schema targetNamespace=\"http://xml.ne... 1:xs:element name=\"autoLoanApplication"
+ "\" 2:xs:annotation 3:xs:documentation (A&nbsp;loan&nbsp;application&nbsp;xml&nbsp"
+ ";schema) 2:xs:complexType 3:xs:sequence 4:xs:element name=\"loan\", type=\"LoanType"
+ "\" 4:xs:element name=\"term\", type=\"xs:integer\" 4:xs:element name=\"amount\" 5:xs:si"
+ "mpleType 6:xs:restriction base=\"xs:decimal\" 7:xs:minInclusive value=\"5000.00\" 7:"
+ "xs:maxInclusive value=\"10000.00\" 7:xs:fractionDigits value=\"2\" 4:xs:element name"
+ "=\"application\" 5:xs:simpleType 6:xs:restriction base=\"xs:string\" 7:xs:enumeratio"
+ "n value=\"individual\" 7:xs:enumeration value=\"joint\" 4:xs:element name=\"state\", t"
+ "ype=\"StateType\" 4:xs:element name=\"applicant\", type=\"Applic... 4:xs:element name"
+ "=\"tradeIn\", minOccurs=\"0\" 5:xs:complexType 6:xs:sequence 7:xs:element name=\"vehi"
+ "cle\", type=\"CarType\" 7:xs:element name=\"monthlyPayment\" 8:xs:simpleType 9:xs:res"
+ "triction base=\"xs:decimal\" 10:xs:fractionDigits value=\"2\" 10:xs:minInclusive val"
+ "ue=\"0.00\" 1:xs:simpleType name=\"LoanType\" 2:xs:restriction base=\"xs:string\" 3:xs"
+ ":enumeration value=\"new\" 3:xs:enumeration value=\"used dealer\" 3:xs:enumeration v"
+ "alue=\"used private\" 3:xs:enumeration value=\"refinance\" 3:xs:enumeration value=\"l"
+ "ease buyout\" 1:xs:simpleType name=\"StateType\" 2:xs:restriction base=\"xs:string\" "
+ "3:xs:enumeration value=\"AZ\" 3:xs:enumeration value=\"CA\" 3:xs:enumeration value=\""
+ "NY\" 3:xs:enumeration value=\"TX\" 1:xs:complexType name=\"ApplicantType\" 2:xs:seque"
+ "nce 3:xs:element name=\"name\" 4:xs:complexType 5:xs:sequence 6:xs:element name=\"f"
+ "irst\", type=\"xs:string\" 6:xs:element name=\"middle\", type=\"xs:string... 6:xs:elem"
+ "ent name=\"last\", type=\"xs:string\" 6:xs:element name=\"suffix\" 7:xs:simpleType 8:x"
+ "s:restriction base=\"xs:string\" 9:xs:enumeration value=\"Jr\" 9:xs:enumeration valu"
+ "e=\"Sr\" 9:xs:enumeration value=\"II\" 9:xs:enumeration value=\"III\" 9:xs:enumeration"
+ " value=\"IV\" 9:xs:enumeration value=\"V\" 3:xs:element name=\"SSID\" 4:xs:simpleType "
+ "5:xs:restriction base=\"xs:string\" 6:xs:pattern value=\"\\d{3}-\\d{2}-\\d{4}\" 3:xs:el"
+ "ement name=\"birthDate\", type=\"xs:dat... 3:xs:element name=\"homePhone\", type=\"Pho"
+ "neN... 3:xs:element name=\"cellPhone\", type=\"PhoneN... 3:xs:element name=\"emailAd"
+ "dress\" 4:xs:simpleType 5:xs:restriction base=\"xs:string\" 3:xs:element name=\"resi"
+ "dence\", type=\"Reside... 3:xs:element name=\"employment\" 4:xs:complexType 5:xs:seq"
+ "uence 6:xs:element name=\"status\" 7:xs:simpleType 8:xs:restriction base=\"xs:strin"
+ "g\" 9:xs:enumeration value=\"unemployed\" 9:xs:enumeration value=\"employed\" 9:xs:en"
+ "umeration value=\"self employed\" 6:xs:element name=\"detail\", minOccurs=\"0\" 7:xs:c"
+ "omplexType 8:xs:sequence 9:xs:element name=\"occupation\", type=\"xs:st... 9:xs:ele"
+ "ment name=\"employer\" 10:xs:complexType 11:xs:sequence 12:xs:element name=\"name\" "
+ "12:xs:element name=\"address\", type=\"AddressT... 12:xs:element name=\"lengthOfEmpl"
+ "oyment\", typ... 9:xs:element name=\"workPhone\", type=\"PhoneN... 9:xs:element name"
+ "=\"grossMonthlyIncome\" 10:xs:simpleType 11:xs:restriction base=\"xs:decimal\" 12:xs"
+ ":minInclusive value=\"0.00\" 12:xs:fractionDigits value=\"2\" 3:xs:element name=\"sup"
+ "plementalIncome\" 4:xs:complexType 5:xs:sequence 6:xs:element name=\"source\" 7:xs:"
+ "simpleType 8:xs:restriction base=\"xs:string\" 9:xs:enumeration value=\"alimony\" 9:"
+ "xs:enumeration value=\"child support\" 9:xs:enumeration value=\"investments\" 9:xs:e"
+ "numeration value=\"part time job\" 9:xs:enumeration value=\"rental property\" 9:xs:e"
+ "numeration value=\"retirement income\" 9:xs:enumeration value=\"social security\" 6:"
+ "xs:element name=\"grossMonthlyIncome\" 7:xs:simpleType 8:xs:restriction base=\"xs:d"
+ "ecimal\" 9:xs:minInclusive value=\"0.00\" 9:xs:fractionDigits value=\"2\" 1:xs:comple"
+ "xType name=\"AddressType\" 2:xs:sequence 3:xs:element name=\"address1\" 3:xs:element"
+ " name=\"address2\" 3:xs:element name=\"city\" 3:xs:element name=\"state\", type=\"State"
+ "Type\" 3:xs:element name=\"zip\" 4:xs:simpleType 5:xs:restriction base=\"xs:string\" "
+ "6:xs:minLength value=\"5\" 6:xs:maxLength value=\"5\" 6:xs:pattern value=\"\\d{5}\" 1:x"
+ "s:complexType name=\"PhoneNumberType\" 2:xs:sequence 3:xs:element name=\"areaCode\" "
+ "3:xs:element name=\"exchange\" 3:xs:element name=\"number\" 3:xs:element name=\"exten"
+ "sion\", minOccurs=\"0... 1:xs:complexType name=\"OccupancyType\" 2:xs:sequence 3:xs:"
+ "element name=\"type\" 4:xs:simpleType 5:xs:restriction base=\"xs:string\" 6:xs:enume"
+ "ration value=\"rent\" 6:xs:enumeration value=\"own\" 3:xs:element name=\"movedIn\", ty"
+ "pe=\"xs:date\" 3:xs:element name=\"lengthOfOccupancy\", type... 3:xs:element name=\"p"
+ "ayment\" 4:xs:simpleType 5:xs:restriction base=\"xs:decimal\" 6:xs:fractionDigits v"
+ "alue=\"2\" 6:xs:minInclusive value=\"0.00\" 1:xs:complexType name=\"ResidenceType\" 2:"
+ "xs:sequence 3:xs:element name=\"address\", type=\"AddressT... 3:xs:element name=\"oc"
+ "cupancy\", type=\"Occupa... 1:xs:complexType name=\"CarType\" 2:xs:sequence 3:xs:ele"
+ "ment name=\"year\", type=\"xs:gYear\" 3:xs:element name=\"make\", type=\"xs:string\" 3:x"
+ "s:element name=\"model\", type=\"xs:string\" 3:xs:element name=\"VIN\", type=\"xs:strin"
+ "g\" 1:xs:complexType name=\"DurationType\" 2:xs:sequence 3:xs:element name=\"years\","
+ " type=\"xs:positiv... 3:xs:element name=\"months\" 4:xs:simpleType 5:xs:restriction"
+ " base=\"xs:positiveInteger\" 6:xs:minInclusive value=\"1\" 6:xs:maxInclusive value=\""
+ "12\"";

  public static final String sOriginalContent =
" 0:root [unknown content] 0:version=\"1.0\" encoding=\"UTF-8\" 0:xsd"
+ ":schema xmlns:xsd=\"http://www.w3.org/2... (helloworld)";

  public static final String sTurnedContent =
" 0:root [unknown content] 0:version=\"1.0\" encoding=\"UTF-8\" 0:xsd"
+ ":schema xmlns:xsd=\"http://www.w3.org/2...";

}
