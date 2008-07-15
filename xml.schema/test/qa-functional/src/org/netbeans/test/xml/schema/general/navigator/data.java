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
  " 0:root [unknown content] 0:version=\"1.0\" encoding=\"UTF-8\" 0:xs:"
+ "schema xmlns=\"http://xml.netbeans.org... 1:xs:element name=\"autoLoanApplication\""
+ " 2:xs:annotation 3:xs:documentation (A&nbsp;loan&nbsp;application&nbsp;xml&nbsp;"
+ "schema) 2:xs:complexType 3:xs:sequence 4:xs:element type=\"LoanType\", name=\"loan\""
+ " 4:xs:element type=\"xs:integer\", name=\"term\" 4:xs:element name=\"amount\" 5:xs:sim"
+ "pleType 6:xs:restriction base=\"xs:decimal\" 7:xs:minInclusive value=\"5000.00\" 7:x"
+ "s:maxInclusive value=\"10000.00\" 7:xs:fractionDigits value=\"2\" 4:xs:element name="
+ "\"application\" 5:xs:simpleType 6:xs:restriction base=\"xs:string\" 7:xs:enumeration"
+ " value=\"individual\" 7:xs:enumeration value=\"joint\" 4:xs:element type=\"StateType\""
+ ", name=\"state\" 4:xs:element name=\"applicant\", type=\"Applic... 4:xs:element minOc"
+ "curs=\"0\", name=\"tradeIn\" 5:xs:complexType 6:xs:sequence 7:xs:element type=\"CarTy"
+ "pe\", name=\"vehicle\" 7:xs:element name=\"monthlyPayment\" 8:xs:simpleType 9:xs:rest"
+ "riction base=\"xs:decimal\" 10:xs:fractionDigits value=\"2\" 10:xs:minInclusive valu"
+ "e=\"0.00\" 1:xs:simpleType name=\"LoanType\" 2:xs:restriction base=\"xs:string\" 3:xs:"
+ "enumeration value=\"new\" 3:xs:enumeration value=\"used dealer\" 3:xs:enumeration va"
+ "lue=\"used private\" 3:xs:enumeration value=\"refinance\" 3:xs:enumeration value=\"le"
+ "ase buyout\" 1:xs:simpleType name=\"StateType\" 2:xs:restriction base=\"xs:string\" 3"
+ ":xs:enumeration value=\"AZ\" 3:xs:enumeration value=\"CA\" 3:xs:enumeration value=\"N"
+ "Y\" 3:xs:enumeration value=\"TX\" 1:xs:complexType name=\"ApplicantType\" 2:xs:sequen"
+ "ce 3:xs:element name=\"name\" 4:xs:complexType 5:xs:sequence 6:xs:element type=\"xs"
+ ":string\", name=\"first\" 6:xs:element type=\"xs:string\", name=\"middle... 6:xs:eleme"
+ "nt type=\"xs:string\", name=\"last\" 6:xs:element name=\"suffix\" 7:xs:simpleType 8:xs"
+ ":restriction base=\"xs:string\" 9:xs:enumeration value=\"Jr\" 9:xs:enumeration value"
+ "=\"Sr\" 9:xs:enumeration value=\"II\" 9:xs:enumeration value=\"III\" 9:xs:enumeration "
+ "value=\"IV\" 9:xs:enumeration value=\"V\" 3:xs:element name=\"SSID\" 4:xs:simpleType 5"
+ ":xs:restriction base=\"xs:string\" 6:xs:pattern value=\"\\d{3}-\\d{2}-\\d{4}\" 3:xs:ele"
+ "ment type=\"xs:date\", name=\"birthDat... 3:xs:element type=\"PhoneNumberType\", name"
+ "=\"... 3:xs:element type=\"PhoneNumberType\", name=\"... 3:xs:element name=\"emailAdd"
+ "ress\" 4:xs:simpleType 5:xs:restriction base=\"xs:string\" 3:xs:element name=\"resid"
+ "ence\", type=\"Reside... 3:xs:element name=\"employment\" 4:xs:complexType 5:xs:sequ"
+ "ence 6:xs:element name=\"status\" 7:xs:simpleType 8:xs:restriction base=\"xs:string"
+ "\" 9:xs:enumeration value=\"unemployed\" 9:xs:enumeration value=\"employed\" 9:xs:enu"
+ "meration value=\"self employed\" 6:xs:element minOccurs=\"0\", name=\"detail\" 7:xs:co"
+ "mplexType 8:xs:sequence 9:xs:element type=\"xs:string\", name=\"occupa... 9:xs:elem"
+ "ent name=\"employer\" 10:xs:complexType 11:xs:sequence 12:xs:element name=\"name\" 1"
+ "2:xs:element type=\"AddressType\", name=\"addr... 12:xs:element type=\"DurationType\""
+ ", name=\"len... 9:xs:element type=\"PhoneNumberType\", name=\"... 9:xs:element name="
+ "\"grossMonthlyIncome\" 10:xs:simpleType 11:xs:restriction base=\"xs:decimal\" 12:xs:"
+ "minInclusive value=\"0.00\" 12:xs:fractionDigits value=\"2\" 3:xs:element name=\"supp"
+ "lementalIncome\" 4:xs:complexType 5:xs:sequence 6:xs:element name=\"source\" 7:xs:s"
+ "impleType 8:xs:restriction base=\"xs:string\" 9:xs:enumeration value=\"alimony\" 9:x"
+ "s:enumeration value=\"child support\" 9:xs:enumeration value=\"investments\" 9:xs:en"
+ "umeration value=\"part time job\" 9:xs:enumeration value=\"rental property\" 9:xs:en"
+ "umeration value=\"retirement income\" 9:xs:enumeration value=\"social security\" 6:x"
+ "s:element name=\"grossMonthlyIncome\" 7:xs:simpleType 8:xs:restriction base=\"xs:de"
+ "cimal\" 9:xs:minInclusive value=\"0.00\" 9:xs:fractionDigits value=\"2\" 1:xs:complex"
+ "Type name=\"AddressType\" 2:xs:sequence 3:xs:element name=\"address1\" 3:xs:element "
+ "name=\"address2\" 3:xs:element name=\"city\" 3:xs:element type=\"StateType\", name=\"st"
+ "ate\" 3:xs:element name=\"zip\" 4:xs:simpleType 5:xs:restriction base=\"xs:string\" 6"
+ ":xs:minLength value=\"5\" 6:xs:maxLength value=\"5\" 6:xs:pattern value=\"\\d{5}\" 1:xs"
+ ":complexType name=\"PhoneNumberType\" 2:xs:sequence 3:xs:element name=\"areaCode\" 3"
+ ":xs:element name=\"exchange\" 3:xs:element name=\"number\" 3:xs:element minOccurs=\"0"
+ "\", name=\"extension... 1:xs:complexType name=\"OccupancyType\" 2:xs:sequence 3:xs:e"
+ "lement name=\"type\" 4:xs:simpleType 5:xs:restriction base=\"xs:string\" 6:xs:enumer"
+ "ation value=\"rent\" 6:xs:enumeration value=\"own\" 3:xs:element type=\"xs:date\", nam"
+ "e=\"movedIn\" 3:xs:element type=\"DurationType\", name=\"len... 3:xs:element name=\"pa"
+ "yment\" 4:xs:simpleType 5:xs:restriction base=\"xs:decimal\" 6:xs:fractionDigits va"
+ "lue=\"2\" 6:xs:minInclusive value=\"0.00\" 1:xs:complexType name=\"ResidenceType\" 2:x"
+ "s:sequence 3:xs:element type=\"AddressType\", name=\"addr... 3:xs:element type=\"Occ"
+ "upancyType\", name=\"oc... 1:xs:complexType name=\"CarType\" 2:xs:sequence 3:xs:elem"
+ "ent type=\"xs:gYear\", name=\"year\" 3:xs:element type=\"xs:string\", name=\"make\" 3:xs"
+ ":element type=\"xs:string\", name=\"model\" 3:xs:element type=\"xs:string\", name=\"VIN"
+ "\" 1:xs:complexType name=\"DurationType\" 2:xs:sequence 3:xs:element type=\"xs:posit"
+ "iveInteger\", nam... 3:xs:element name=\"months\" 4:xs:simpleType 5:xs:restriction "
+ "base=\"xs:positiveInteger\" 6:xs:minInclusive value=\"1\" 6:xs:maxInclusive value=\"1"
+ "2\"";

  public static final String sAttributes =
" 0:root [unknown content] 0:version=\"1.0\" encoding=\"UTF-8\" 0:xs:"
+ "schema xmlns=\"http://xml.netbeans.org... 1:xs:element name=\"autoLoanApplication\""
+ " 2:xs:annotation 3:xs:documentation (A&nbsp;loan&nbsp;application&nbsp;xml&nbsp;"
+ "schema) 2:xs:complexType 3:xs:sequence 4:xs:element type=\"LoanType\", name=\"loan\""
+ " 4:xs:element type=\"xs:integer\", name=\"term\" 4:xs:element name=\"amount\" 5:xs:sim"
+ "pleType 6:xs:restriction base=\"xs:decimal\" 7:xs:minInclusive value=\"5000.00\" 7:x"
+ "s:maxInclusive value=\"10000.00\" 7:xs:fractionDigits value=\"2\" 4:xs:element name="
+ "\"application\" 5:xs:simpleType 6:xs:restriction base=\"xs:string\" 7:xs:enumeration"
+ " value=\"individual\" 7:xs:enumeration value=\"joint\" 4:xs:element type=\"StateType\""
+ ", name=\"state\" 4:xs:element name=\"applicant\", type=\"Applic... 4:xs:element minOc"
+ "curs=\"0\", name=\"tradeIn\" 5:xs:complexType 6:xs:sequence 7:xs:element type=\"CarTy"
+ "pe\", name=\"vehicle\" 7:xs:element name=\"monthlyPayment\" 8:xs:simpleType 9:xs:rest"
+ "riction base=\"xs:decimal\" 10:xs:fractionDigits value=\"2\" 10:xs:minInclusive valu"
+ "e=\"0.00\" 1:xs:simpleType name=\"LoanType\" 2:xs:restriction base=\"xs:string\" 3:xs:"
+ "enumeration value=\"new\" 3:xs:enumeration value=\"used dealer\" 3:xs:enumeration va"
+ "lue=\"used private\" 3:xs:enumeration value=\"refinance\" 3:xs:enumeration value=\"le"
+ "ase buyout\" 1:xs:simpleType name=\"StateType\" 2:xs:restriction base=\"xs:string\" 3"
+ ":xs:enumeration value=\"AZ\" 3:xs:enumeration value=\"CA\" 3:xs:enumeration value=\"N"
+ "Y\" 3:xs:enumeration value=\"TX\" 1:xs:complexType name=\"ApplicantType\" 2:xs:sequen"
+ "ce 3:xs:element name=\"name\" 4:xs:complexType 5:xs:sequence 6:xs:element type=\"xs"
+ ":string\", name=\"first\" 6:xs:element type=\"xs:string\", name=\"middle... 6:xs:eleme"
+ "nt type=\"xs:string\", name=\"last\" 6:xs:element name=\"suffix\" 7:xs:simpleType 8:xs"
+ ":restriction base=\"xs:string\" 9:xs:enumeration value=\"Jr\" 9:xs:enumeration value"
+ "=\"Sr\" 9:xs:enumeration value=\"II\" 9:xs:enumeration value=\"III\" 9:xs:enumeration "
+ "value=\"IV\" 9:xs:enumeration value=\"V\" 3:xs:element name=\"SSID\" 4:xs:simpleType 5"
+ ":xs:restriction base=\"xs:string\" 6:xs:pattern value=\"\\d{3}-\\d{2}-\\d{4}\" 3:xs:ele"
+ "ment type=\"xs:date\", name=\"birthDat... 3:xs:element type=\"PhoneNumberType\", name"
+ "=\"... 3:xs:element type=\"PhoneNumberType\", name=\"... 3:xs:element name=\"emailAdd"
+ "ress\" 4:xs:simpleType 5:xs:restriction base=\"xs:string\" 3:xs:element name=\"resid"
+ "ence\", type=\"Reside... 3:xs:element name=\"employment\" 4:xs:complexType 5:xs:sequ"
+ "ence 6:xs:element name=\"status\" 7:xs:simpleType 8:xs:restriction base=\"xs:string"
+ "\" 9:xs:enumeration value=\"unemployed\" 9:xs:enumeration value=\"employed\" 9:xs:enu"
+ "meration value=\"self employed\" 6:xs:element minOccurs=\"0\", name=\"detail\" 7:xs:co"
+ "mplexType 8:xs:sequence 9:xs:element type=\"xs:string\", name=\"occupa... 9:xs:elem"
+ "ent name=\"employer\" 10:xs:complexType 11:xs:sequence 12:xs:element name=\"name\" 1"
+ "2:xs:element type=\"AddressType\", name=\"addr... 12:xs:element type=\"DurationType\""
+ ", name=\"len... 9:xs:element type=\"PhoneNumberType\", name=\"... 9:xs:element name="
+ "\"grossMonthlyIncome\" 10:xs:simpleType 11:xs:restriction base=\"xs:decimal\" 12:xs:"
+ "minInclusive value=\"0.00\" 12:xs:fractionDigits value=\"2\" 3:xs:element name=\"supp"
+ "lementalIncome\" 4:xs:complexType 5:xs:sequence 6:xs:element name=\"source\" 7:xs:s"
+ "impleType 8:xs:restriction base=\"xs:string\" 9:xs:enumeration value=\"alimony\" 9:x"
+ "s:enumeration value=\"child support\" 9:xs:enumeration value=\"investments\" 9:xs:en"
+ "umeration value=\"part time job\" 9:xs:enumeration value=\"rental property\" 9:xs:en"
+ "umeration value=\"retirement income\" 9:xs:enumeration value=\"social security\" 6:x"
+ "s:element name=\"grossMonthlyIncome\" 7:xs:simpleType 8:xs:restriction base=\"xs:de"
+ "cimal\" 9:xs:minInclusive value=\"0.00\" 9:xs:fractionDigits value=\"2\" 1:xs:complex"
+ "Type name=\"AddressType\" 2:xs:sequence 3:xs:element name=\"address1\" 3:xs:element "
+ "name=\"address2\" 3:xs:element name=\"city\" 3:xs:element type=\"StateType\", name=\"st"
+ "ate\" 3:xs:element name=\"zip\" 4:xs:simpleType 5:xs:restriction base=\"xs:string\" 6"
+ ":xs:minLength value=\"5\" 6:xs:maxLength value=\"5\" 6:xs:pattern value=\"\\d{5}\" 1:xs"
+ ":complexType name=\"PhoneNumberType\" 2:xs:sequence 3:xs:element name=\"areaCode\" 3"
+ ":xs:element name=\"exchange\" 3:xs:element name=\"number\" 3:xs:element minOccurs=\"0"
+ "\", name=\"extension... 1:xs:complexType name=\"OccupancyType\" 2:xs:sequence 3:xs:e"
+ "lement name=\"type\" 4:xs:simpleType 5:xs:restriction base=\"xs:string\" 6:xs:enumer"
+ "ation value=\"rent\" 6:xs:enumeration value=\"own\" 3:xs:element type=\"xs:date\", nam"
+ "e=\"movedIn\" 3:xs:element type=\"DurationType\", name=\"len... 3:xs:element name=\"pa"
+ "yment\" 4:xs:simpleType 5:xs:restriction base=\"xs:decimal\" 6:xs:fractionDigits va"
+ "lue=\"2\" 6:xs:minInclusive value=\"0.00\" 1:xs:complexType name=\"ResidenceType\" 2:x"
+ "s:sequence 3:xs:element type=\"AddressType\", name=\"addr... 3:xs:element type=\"Occ"
+ "upancyType\", name=\"oc... 1:xs:complexType name=\"CarType\" 2:xs:sequence 3:xs:elem"
+ "ent type=\"xs:gYear\", name=\"year\" 3:xs:element type=\"xs:string\", name=\"make\" 3:xs"
+ ":element type=\"xs:string\", name=\"model\" 3:xs:element type=\"xs:string\", name=\"VIN"
+ "\" 1:xs:complexType name=\"DurationType\" 2:xs:sequence 3:xs:element type=\"xs:posit"
+ "iveInteger\", nam... 3:xs:element name=\"months\" 4:xs:simpleType 5:xs:restriction "
+ "base=\"xs:positiveInteger\" 6:xs:minInclusive value=\"1\" 6:xs:maxInclusive value=\"1"
+ "2\"";


  public static final String sOriginalContent =
" 0:root [unknown content] 0:version=\"1.0\" encoding=\"UTF-8\" 0:xsd"
+ ":schema xmlns:xsd=\"http://www.w3.org/2... (helloworld)";

  public static final String sTurnedContent =
" 0:root [unknown content] 0:version=\"1.0\" encoding=\"UTF-8\" 0:xsd"
+ ":schema xmlns:xsd=\"http://www.w3.org/2...";

}
