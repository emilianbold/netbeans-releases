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

package org.netbeans.test.xml.schema.general.schemaview;

public class data
{
  public static String m_sData =
      " 0:http://xml.netbeans.org/examples/LoanApplication [Schema] 0:A"
    + "ttributes 0:Attribute Groups 0:Complex Types 1:AddressType [Global Complex Type]"
    + " 2:sequence [Sequence] 3:address1 [Local Element] 3:address2 [Local Element] 3:c"
    + "ity [Local Element] 3:state [Local Element] 4:StateType [Global Simple Type] 5:\""
    + "AZ\" [Enumeration] 5:\"CA\" [Enumeration] 5:\"NY\" [Enumeration] 5:\"TX\" [Enumeration]"
    + " 3:zip [Local Element] 4:simpleType [Local Simple Type] 1:ApplicantType [Global "
    + "Complex Type] 2:sequence [Sequence] 3:name [Local Element] 4:complexType [Local "
    + "Complex Type] 5:sequence [Sequence] 6:first [Local Element] 7:string [Global Sim"
    + "ple Type] 6:middle [Local Element] 7:string [Global Simple Type] 6:last [Local E"
    + "lement] 7:string [Global Simple Type] 6:suffix [Local Element] 7:simpleType [Loc"
    + "al Simple Type] 8:\"Jr\" [Enumeration] 8:\"Sr\" [Enumeration] 8:\"II\" [Enumeration] 8"
    + ":\"III\" [Enumeration] 8:\"IV\" [Enumeration] 8:\"V\" [Enumeration] 3:SSID [Local Elem"
    + "ent] 4:simpleType [Local Simple Type] 3:birthDate [Local Element] 4:date [Global"
    + " Simple Type] 3:homePhone [Local Element] 4:PhoneNumberType [Global Complex Type"
    + "] 5:sequence [Sequence] 6:areaCode [Local Element] 6:exchange [Local Element] 6:"
    + "number [Local Element] 6:extension [Local Element] 3:cellPhone [Local Element] 4"
    + ":PhoneNumberType [Global Complex Type] 5:sequence [Sequence] 6:areaCode [Local E"
    + "lement] 6:exchange [Local Element] 6:number [Local Element] 6:extension [Local E"
    + "lement] 3:emailAddress [Local Element] 4:simpleType [Local Simple Type] 3:reside"
    + "nce [Local Element] 4:ResidenceType [Global Complex Type] 5:sequence [Sequence] "
    + "6:address [Local Element] 7:AddressType [Global Complex Type] 8:sequence [Sequen"
    + "ce] 9:address1 [Local Element] 9:address2 [Local Element] 9:city [Local Element]"
    + " 9:state [Local Element] 10:StateType [Global Simple Type] 11:\"AZ\" [Enumeration]"
    + " 11:\"CA\" [Enumeration] 11:\"NY\" [Enumeration] 11:\"TX\" [Enumeration] 9:zip [Local "
    + "Element] 10:simpleType [Local Simple Type] 6:occupancy [Local Element] 7:Occupan"
    + "cyType [Global Complex Type] 8:sequence [Sequence] 9:type [Local Element] 10:sim"
    + "pleType [Local Simple Type] 11:\"rent\" [Enumeration] 11:\"own\" [Enumeration] 9:mov"
    + "edIn [Local Element] 10:date [Global Simple Type] 9:lengthOfOccupancy [Local Ele"
    + "ment] 10:DurationType [Global Complex Type] 11:sequence [Sequence] 12:years [Loc"
    + "al Element] 13:positiveInteger [Global Simple Type] 12:months [Local Element] 13"
    + ":simpleType [Local Simple Type] 9:payment [Local Element] 10:simpleType [Local S"
    + "imple Type] 3:employment [Local Element] 4:complexType [Local Complex Type] 5:se"
    + "quence [Sequence] 6:status [Local Element] 7:simpleType [Local Simple Type] 8:\"u"
    + "nemployed\" [Enumeration] 8:\"employed\" [Enumeration] 8:\"self employed\" [Enumerati"
    + "on] 6:detail [Local Element] 7:complexType [Local Complex Type] 8:sequence [Sequ"
    + "ence] 9:occupation [Local Element] 10:string [Global Simple Type] 9:employer [Lo"
    + "cal Element] 10:complexType [Local Complex Type] 11:sequence [Sequence] 12:name "
    + "[Local Element] 12:address [Local Element] 13:AddressType [Global Complex Type] "
    + "14:sequence [Sequence] 15:address1 [Local Element] 15:address2 [Local Element] 1"
    + "5:city [Local Element] 15:state [Local Element] 16:StateType [Global Simple Type"
    + "] 17:\"AZ\" [Enumeration] 17:\"CA\" [Enumeration] 17:\"NY\" [Enumeration] 17:\"TX\" [Enu"
    + "meration] 15:zip [Local Element] 16:simpleType [Local Simple Type] 12:lengthOfEm"
    + "ployment [Local Element] 13:DurationType [Global Complex Type] 14:sequence [Sequ"
    + "ence] 15:years [Local Element] 16:positiveInteger [Global Simple Type] 15:months"
    + " [Local Element] 16:simpleType [Local Simple Type] 9:workPhone [Local Element] 1"
    + "0:PhoneNumberType [Global Complex Type] 11:sequence [Sequence] 12:areaCode [Loca"
    + "l Element] 12:exchange [Local Element] 12:number [Local Element] 12:extension [L"
    + "ocal Element] 9:grossMonthlyIncome [Local Element] 10:simpleType [Local Simple T"
    + "ype] 3:supplementalIncome [Local Element] 4:complexType [Local Complex Type] 5:s"
    + "equence [Sequence] 6:source [Local Element] 7:simpleType [Local Simple Type] 8:\""
    + "alimony\" [Enumeration] 8:\"child support\" [Enumeration] 8:\"investments\" [Enumerat"
    + "ion] 8:\"part time job\" [Enumeration] 8:\"rental property\" [Enumeration] 8:\"retire"
    + "ment income\" [Enumeration] 8:\"social security\" [Enumeration] 6:grossMonthlyIncom"
    + "e [Local Element] 7:simpleType [Local Simple Type] 1:CarType [Global Complex Typ"
    + "e] 2:sequence [Sequence] 3:year [Local Element] 4:gYear [Global Simple Type] 3:m"
    + "ake [Local Element] 4:string [Global Simple Type] 3:model [Local Element] 4:stri"
    + "ng [Global Simple Type] 3:VIN [Local Element] 4:string [Global Simple Type] 1:Du"
    + "rationType [Global Complex Type] 2:sequence [Sequence] 3:years [Local Element] 4"
    + ":positiveInteger [Global Simple Type] 3:months [Local Element] 4:simpleType [Loc"
    + "al Simple Type] 1:OccupancyType [Global Complex Type] 2:sequence [Sequence] 3:ty"
    + "pe [Local Element] 4:simpleType [Local Simple Type] 5:\"rent\" [Enumeration] 5:\"ow"
    + "n\" [Enumeration] 3:movedIn [Local Element] 4:date [Global Simple Type] 3:lengthO"
    + "fOccupancy [Local Element] 4:DurationType [Global Complex Type] 5:sequence [Sequ"
    + "ence] 6:years [Local Element] 7:positiveInteger [Global Simple Type] 6:months [L"
    + "ocal Element] 7:simpleType [Local Simple Type] 3:payment [Local Element] 4:simpl"
    + "eType [Local Simple Type] 1:PhoneNumberType [Global Complex Type] 2:sequence [Se"
    + "quence] 3:areaCode [Local Element] 3:exchange [Local Element] 3:number [Local El"
    + "ement] 3:extension [Local Element] 1:ResidenceType [Global Complex Type] 2:seque"
    + "nce [Sequence] 3:address [Local Element] 4:AddressType [Global Complex Type] 5:s"
    + "equence [Sequence] 6:address1 [Local Element] 6:address2 [Local Element] 6:city "
    + "[Local Element] 6:state [Local Element] 7:StateType [Global Simple Type] 8:\"AZ\" "
    + "[Enumeration] 8:\"CA\" [Enumeration] 8:\"NY\" [Enumeration] 8:\"TX\" [Enumeration] 6:z"
    + "ip [Local Element] 7:simpleType [Local Simple Type] 3:occupancy [Local Element] "
    + "4:OccupancyType [Global Complex Type] 5:sequence [Sequence] 6:type [Local Elemen"
    + "t] 7:simpleType [Local Simple Type] 8:\"rent\" [Enumeration] 8:\"own\" [Enumeration]"
    + " 6:movedIn [Local Element] 7:date [Global Simple Type] 6:lengthOfOccupancy [Loca"
    + "l Element] 7:DurationType [Global Complex Type] 8:sequence [Sequence] 9:years [L"
    + "ocal Element] 10:positiveInteger [Global Simple Type] 9:months [Local Element] 1"
    + "0:simpleType [Local Simple Type] 6:payment [Local Element] 7:simpleType [Local S"
    + "imple Type] 0:Elements 1:autoLoanApplication [Global Element] 2:annotation [Anno"
    + "tation] 3:documentation [Documentation] 2:complexType [Local Complex Type] 3:seq"
    + "uence [Sequence] 4:loan [Local Element] 5:LoanType [Global Simple Type] 6:\"new\" "
    + "[Enumeration] 6:\"used dealer\" [Enumeration] 6:\"used private\" [Enumeration] 6:\"re"
    + "finance\" [Enumeration] 6:\"lease buyout\" [Enumeration] 4:term [Local Element] 5:i"
    + "nteger [Global Simple Type] 4:amount [Local Element] 5:simpleType [Local Simple "
    + "Type] 4:application [Local Element] 5:simpleType [Local Simple Type] 6:\"individu"
    + "al\" [Enumeration] 6:\"joint\" [Enumeration] 4:state [Local Element] 5:StateType [G"
    + "lobal Simple Type] 6:\"AZ\" [Enumeration] 6:\"CA\" [Enumeration] 6:\"NY\" [Enumeration"
    + "] 6:\"TX\" [Enumeration] 4:applicant [Local Element] 5:ApplicantType [Global Compl"
    + "ex Type] 6:sequence [Sequence] 7:name [Local Element] 8:complexType [Local Compl"
    + "ex Type] 9:sequence [Sequence] 10:first [Local Element] 11:string [Global Simple"
    + " Type] 10:middle [Local Element] 11:string [Global Simple Type] 10:last [Local E"
    + "lement] 11:string [Global Simple Type] 10:suffix [Local Element] 11:simpleType ["
    + "Local Simple Type] 12:\"Jr\" [Enumeration] 12:\"Sr\" [Enumeration] 12:\"II\" [Enumerat"
    + "ion] 12:\"III\" [Enumeration] 12:\"IV\" [Enumeration] 12:\"V\" [Enumeration] 7:SSID [L"
    + "ocal Element] 8:simpleType [Local Simple Type] 7:birthDate [Local Element] 8:dat"
    + "e [Global Simple Type] 7:homePhone [Local Element] 8:PhoneNumberType [Global Com"
    + "plex Type] 9:sequence [Sequence] 10:areaCode [Local Element] 10:exchange [Local "
    + "Element] 10:number [Local Element] 10:extension [Local Element] 7:cellPhone [Loc"
    + "al Element] 8:PhoneNumberType [Global Complex Type] 9:sequence [Sequence] 10:are"
    + "aCode [Local Element] 10:exchange [Local Element] 10:number [Local Element] 10:e"
    + "xtension [Local Element] 7:emailAddress [Local Element] 8:simpleType [Local Simp"
    + "le Type] 7:residence [Local Element] 8:ResidenceType [Global Complex Type] 9:seq"
    + "uence [Sequence] 10:address [Local Element] 11:AddressType [Global Complex Type]"
    + " 12:sequence [Sequence] 13:address1 [Local Element] 13:address2 [Local Element] "
    + "13:city [Local Element] 13:state [Local Element] 14:StateType [Global Simple Typ"
    + "e] 15:\"AZ\" [Enumeration] 15:\"CA\" [Enumeration] 15:\"NY\" [Enumeration] 15:\"TX\" [En"
    + "umeration] 13:zip [Local Element] 14:simpleType [Local Simple Type] 10:occupancy"
    + " [Local Element] 11:OccupancyType [Global Complex Type] 12:sequence [Sequence] 1"
    + "3:type [Local Element] 14:simpleType [Local Simple Type] 15:\"rent\" [Enumeration]"
    + " 15:\"own\" [Enumeration] 13:movedIn [Local Element] 14:date [Global Simple Type] "
    + "13:lengthOfOccupancy [Local Element] 14:DurationType [Global Complex Type] 15:se"
    + "quence [Sequence] 16:years [Local Element] 17:positiveInteger [Global Simple Typ"
    + "e] 16:months [Local Element] 17:simpleType [Local Simple Type] 13:payment [Local"
    + " Element] 14:simpleType [Local Simple Type] 7:employment [Local Element] 8:compl"
    + "exType [Local Complex Type] 9:sequence [Sequence] 10:status [Local Element] 11:s"
    + "impleType [Local Simple Type] 12:\"unemployed\" [Enumeration] 12:\"employed\" [Enume"
    + "ration] 12:\"self employed\" [Enumeration] 10:detail [Local Element] 11:complexTyp"
    + "e [Local Complex Type] 12:sequence [Sequence] 13:occupation [Local Element] 14:s"
    + "tring [Global Simple Type] 13:employer [Local Element] 14:complexType [Local Com"
    + "plex Type] 15:sequence [Sequence] 16:name [Local Element] 16:address [Local Elem"
    + "ent] 17:AddressType [Global Complex Type] 18:sequence [Sequence] 19:address1 [Lo"
    + "cal Element] 19:address2 [Local Element] 19:city [Local Element] 19:state [Local"
    + " Element] 20:StateType [Global Simple Type] 21:\"AZ\" [Enumeration] 21:\"CA\" [Enume"
    + "ration] 21:\"NY\" [Enumeration] 21:\"TX\" [Enumeration] 19:zip [Local Element] 20:si"
    + "mpleType [Local Simple Type] 16:lengthOfEmployment [Local Element] 17:DurationTy"
    + "pe [Global Complex Type] 18:sequence [Sequence] 19:years [Local Element] 20:posi"
    + "tiveInteger [Global Simple Type] 19:months [Local Element] 20:simpleType [Local "
    + "Simple Type] 13:workPhone [Local Element] 14:PhoneNumberType [Global Complex Typ"
    + "e] 15:sequence [Sequence] 16:areaCode [Local Element] 16:exchange [Local Element"
    + "] 16:number [Local Element] 16:extension [Local Element] 13:grossMonthlyIncome ["
    + "Local Element] 14:simpleType [Local Simple Type] 7:supplementalIncome [Local Ele"
    + "ment] 8:complexType [Local Complex Type] 9:sequence [Sequence] 10:source [Local "
    + "Element] 11:simpleType [Local Simple Type] 12:\"alimony\" [Enumeration] 12:\"child "
    + "support\" [Enumeration] 12:\"investments\" [Enumeration] 12:\"part time job\" [Enumer"
    + "ation] 12:\"rental property\" [Enumeration] 12:\"retirement income\" [Enumeration] 1"
    + "2:\"social security\" [Enumeration] 10:grossMonthlyIncome [Local Element] 11:simpl"
    + "eType [Local Simple Type] 4:tradeIn [Local Element] 5:complexType [Local Complex"
    + " Type] 6:sequence [Sequence] 7:vehicle [Local Element] 8:CarType [Global Complex"
    + " Type] 9:sequence [Sequence] 10:year [Local Element] 11:gYear [Global Simple Typ"
    + "e] 10:make [Local Element] 11:string [Global Simple Type] 10:model [Local Elemen"
    + "t] 11:string [Global Simple Type] 10:VIN [Local Element] 11:string [Global Simpl"
    + "e Type] 7:monthlyPayment [Local Element] 8:simpleType [Local Simple Type] 0:Grou"
    + "ps 0:Referenced Schemas 0:Simple Types 1:LoanType [Global Simple Type] 2:\"new\" ["
    + "Enumeration] 2:\"used dealer\" [Enumeration] 2:\"used private\" [Enumeration] 2:\"ref"
    + "inance\" [Enumeration] 2:\"lease buyout\" [Enumeration] 1:StateType [Global Simple "
    + "Type] 2:\"AZ\" [Enumeration] 2:\"CA\" [Enumeration] 2:\"NY\" [Enumeration] 2:\"TX\" [Enu"
    + "meration]";

}