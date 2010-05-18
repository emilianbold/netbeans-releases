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

package org.netbeans.modules.uml.core.configstringframework;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.ResourceBundle;

import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;

/**
 * @author sumitabhk
 *
 */
public class ConfigStringTranslator implements IConfigStringTranslator {
    private static HashMap<String, String> m_Map = null;

    private static ResourceBundle messages = ResourceBundle.getBundle("org.netbeans.modules.uml.core.Bundle");
    private static ResourceBundle configStrings = ResourceBundle.getBundle("org.netbeans.modules.uml.core.configstringframework.Bundle");
    /**
     *
     */
    public ConfigStringTranslator() {
        super();
        this.buildStringMap();
    }
    
    /**
     * Translate the passed in PSK_String into a value found in our map, or if there is another
     * translator in the property definition, use it.
     *
     * For example, this routine will be passed "PSK_TRUE" and it will return
     * "True".
     *
     * If the string is not found, or is not prefixed with PSK, then the value
     * that is received will be returned
     *
     * @param pDef[in]			A property definition that may house another translator to call instead of our default
     * @param inStr[in]			The string to be translated
     * @param *outStr[out]		The translated string
     *
     * @return HRESULT
     */
    public String translate(IPropertyDefinition pDef, String sPSK) {
        // default the out string to what was passed in just in case we cannot properly translate it
        String outStr = sPSK;
        if (sPSK != null && sPSK.length() > 0) {
            boolean continueFlag = true;
            // check the passed in definition for a translator attribute
            // if it has one we will want to use it for the translation of this value
            if (pDef != null) {
                if("list".equals(pDef.getControlType()) == true) {
                    
                } else {
                    String translator = pDef.getFromAttrMap("translator");
                    if (translator != null && translator.length() > 0) {
                        try {
                            Class clazz = Class.forName(translator);
                            Object obj = clazz.newInstance();
                            if (obj instanceof ICustomTranslator) {
                                ICustomTranslator pTranslator = (ICustomTranslator)obj;
                                outStr = pTranslator.translate(pDef, sPSK);
                                
                                // this class handled it, so do not want to do it again
                                continueFlag = false;
                            }
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (InstantiationException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            
            if(continueFlag) {
                try {
                    //there is a possibility that this string does not
                    //exist in the resource bundle, so return the input string.
                    outStr = messages.getString(sPSK);
                } catch (Exception e) {
                    //do nothing at the moment
                }
            }
        }
        
        return outStr;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.configstringframework.IConfigStringTranslator#translateIntoPSK(org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition, java.lang.String)
         */
    public String translateIntoPSK(IPropertyDefinition pDef, String sNonPSK) {
        String str = "";
        // This operation is really costly. Find a better way
        if (sNonPSK != null && sNonPSK.length() > 0) {
            Enumeration keys = messages.getKeys();
            String key = null;
            while(keys.hasMoreElements()) {
                if(messages.getString((key = (String)keys.nextElement())).equals(sNonPSK)) {
                    // if there is a property definition passed in to this
                    // routine check that the value found in the map matches
                    // a value in the property definition
                    // we ran into setting the wrong psk_value because "Default"
                    // was found in the map more than once
                    if (pDef != null) {
                        String values = pDef.getValidValues2();
                        if (values != null && values.length() > 0) {
                            int pos = values.indexOf(key);
                            if( pos >= 0 ) {
                                str = key;
                                break;
                            }
                        }
                    } else {
                        str = key;
                        break;
                    }
                }
            }
        }
        return str;
        
    }
    public String translateWord(String sInStr) {
        String sOutStr = "";
        
        if (sInStr != null && sInStr.length() > 0) {
            sOutStr = lookUpInMap(sInStr, false);
        }
        
        return sOutStr;
    }
    
    public void addToMap(String sEntry, String nValue) {
        if (sEntry != null && sEntry.length() > 0) {
            if (!m_Map.containsKey(sEntry)) {
                m_Map.put(sEntry, nValue);
            }
        }
    }
    
    public void addToMap(String sEngEntry, String sPSKEntry, String nValue) {
        if (sEngEntry != null && sEngEntry.length() > 0) {
            addToMap(sEngEntry, nValue);
        }
        if (sPSKEntry != null && sPSKEntry.length() > 0) {
            addToMap(sPSKEntry, nValue);
        }
    }
    
    public void clearMap() {
        m_Map.clear();
    }
    
    public void buildStringMap() {
        // Clear the map, this shouldn't do anything since this class is referenced counted.
        //clearMap();
        
        // From C:\Development\UML\Core\ConfigStringFramework\ConfigStringFramework.rc
        if (m_Map == null) {
            m_Map = new HashMap<String, String>();
            
            addToMap("PSK_DEFAULT", "IDS_STRING101");
            addToMap("PSK_DEFAULT_HLP", "IDS_STRING102");
            addToMap("PSK_USEQNAME", "IDS_STRING103");
            addToMap("PSK_USEQNAME_HLP", "IDS_STRING104");
            addToMap("PSK_TRUE", "IDS_STRING105");
            addToMap("PSK_FALSE", "IDS_STRING106");
            addToMap("PSK_NEWPROJECT", "IDS_STRING107");
            addToMap("PSK_NEWPROJECT_HLP", "IDS_STRING108");
            addToMap("PSK_DEFAULTNAME", "IDS_STRING109");
            addToMap("PSK_DEFAULTNAME_HLP", "IDS_STRING110");
            addToMap("PSK_IDTYPE", "IDS_STRING111");
            addToMap("PSK_UUID", "IDS_STRING112");
            addToMap("PSK_NORMAL", "IDS_STRING113");
            addToMap("PSK_IDTYPE_HLP", "IDS_STRING114");
            addToMap("PSK_DEFAULTELEMENTNAME", "IDS_STRING115");
            addToMap("PSK_DEFAULTELEMENTNAME_HLP", "IDS_STRING116");
            addToMap("PSK_DEFAULTMODE", "IDS_STRING117");
            addToMap("PSK_ANALYSIS", "IDS_STRING118");
            addToMap("PSK_DESIGN", "IDS_STRING119");
            addToMap("Implementation","PSK_IMPLEMENTATION", "IDS_STRING120");
            addToMap("PSK_DEFAULTMODE_HLP", "IDS_STRING121");
            addToMap("PSK_UNKNOWNCLASSIFIER", "IDS_STRING122");
            addToMap("PSK_UNKNOWNCLASSIFIER_HLP", "IDS_STRING123");
            addToMap("PSK_UNKNOWNCLASSIFIERTYPE", "IDS_STRING126");
            addToMap("PSK_UNKNOWNCLASSIFIERSQDTYPE", "IDS_STRING1562");
            addToMap("PSK_UNKNOWNCLASSIFIERSQDTYPE_HLP", "IDS_STRING1563");
            addToMap("PSK_DATATYPE", "IDS_STRING127");
            addToMap("PSK_ALIASEDTYPE", "IDS_STRING1417");
            addToMap("PSK_UNKNOWNCLASSIFIERTYPE_HLP", "IDS_STRING128");
            addToMap("PSK_MODES", "IDS_STRING129");
            addToMap("PSK_MODES_HLP", "IDS_STRING130");
            addToMap("PSK_LIBRARIES", "IDS_STRING131");
            addToMap("PSK_ANALYSIS_HLP", "IDS_STRING132");
            addToMap("PSK_LIBRARIES_HLP", "IDS_STRING133");
            addToMap("PSK_UMLSTANDARDTYPES", "IDS_STRING134");
            addToMap("PSK_UMLSTANDARDTYPES_HLP", "IDS_STRING135");
            addToMap("PSK_CPPTYPES", "IDS_STRING136");
            addToMap("PSK_CPPTYPES_HLP", "IDS_STRING137");
            addToMap("PSK_JAVATYPES", "IDS_STRING138");
            addToMap("PSK_JAVATYPES_HLP", "IDS_STRING139");
            addToMap("PSK_UMLTYPES", "IDS_STRING140");
            addToMap("PSK_UMLTYPES_HLP", "IDS_STRING141");
            addToMap("PSK_CSHARPTYPES", "IDS_STRING1443");
            addToMap("PSK_CSHARPTYPES_HLP", "IDS_STRING1444");
            addToMap("PSK_VB6TYPES", "IDS_STRING1441");
            addToMap("PSK_VB6TYPES_HLP", "IDS_STRING1442");
            addToMap("PSK_JAVA13TYPES", "IDS_STRING1455");
            addToMap("PSK_JAVA13TYPES_HLP", "IDS_STRING1456");
            addToMap("PSK_JAVASTANDARDTYPES", "IDS_STRING1457");
            addToMap("PSK_JAVASTANDARDTYPES_HLP", "IDS_STRING1458");
            addToMap("PSK_JUNITTYPES", "IDS_STRING1657");
            addToMap("PSK_JUNITTYPES_HLP", "IDS_STRING1658");
            addToMap("PSK_DESIGN_HLP", "IDS_STRING142");
            addToMap("PSK_LANGUAGE", "IDS_STRING143");
            addToMap("PSK_LANGUAGE_HLP_D", "IDS_STRING144");
            addToMap("PSK_IMPLEMENTATION_HLP", "IDS_STRING145");
            addToMap("PSK_ROUNDTRIPDEFAULTS", "IDS_STRING146");
            addToMap("PSK_ROUNDTRIPDEFAULTS_HLP", "IDS_STRING147");
            addToMap("PSK_JAVA", "IDS_STRING148");
            addToMap("PSK_JAVA_HLP", "IDS_STRING149");
            addToMap("PSK_CREATECONSTRUCTOR", "IDS_STRING150");
            addToMap("PSK_CREATECONSTRUCTOR_HLP", "IDS_STRING151");
            addToMap("PSK_CREATEFINALIZE", "IDS_STRING152");
            addToMap("PSK_CREATEFINALIZE_HLP", "IDS_STRING153");
            addToMap("PSK_CREATEACCESSORS", "IDS_STRING154");
            addToMap("PSK_CREATEACCESSORS_HLP", "IDS_STRING155");
            addToMap("PSK_IMPLREDEFINABLEOPS", "IDS_STRING156");
            addToMap("PSK_IMPLREDEFINABLEOPS_HLP", "IDS_STRING157");
            addToMap("PSK_ALWAYS", "IDS_STRING158");
            addToMap("PSK_NEVER", "IDS_STRING159");
            addToMap("PSK_ASK", "IDS_STRING160");
            addToMap("PSK_READPREFIX", "IDS_STRING161");
            addToMap("PSK_READPREFIX_HLP", "IDS_STRING162");
            addToMap("PSK_WRITEPREFIX", "IDS_STRING163");
            addToMap("PSK_WRITEPREFIX_HLP", "IDS_STRING164");
            addToMap("PSK_ATTRPREFIX", "IDS_STRING165");
            addToMap("PSK_ATTRPREFIX_HLP", "IDS_STRING166");
            addToMap("PSK_NAMENAVENDS", "IDS_STRING167");
            addToMap("PSK_NAMENAVENDS_HLP", "IDS_STRING168");
            addToMap("PSK_SOURCEUPDATE", "IDS_STRING169");
            addToMap("PSK_SOURCEUPDATE_HLP", "IDS_STRING170");
            addToMap("PSK_IMMEDIATELY", "IDS_STRING171");
            addToMap("PSK_ONSAVE", "IDS_STRING172");
            addToMap("PSK_MODIFYREDEFINABLEMETHOD", "IDS_STRING173");
            addToMap("PSK_MODIFYREDEFINABLEMETHOD_HLP", "IDS_STRING174");
            addToMap("PSK_MODIFYALL", "IDS_STRING175");
            addToMap("PSK_CREATENEWOP", "IDS_STRING176");
            addToMap("PSK_EXPANSIONVARS", "IDS_STRING177");
            addToMap("PSK_EXPANSIONVARS_HLP", "IDS_STRING178");
            addToMap("PSK_CONFIGLOCATION", "IDS_STRING179");
            addToMap("PSK_CONFIGLOCATION_HLP", "IDS_STRING180");
            addToMap("PSK_PROPERTYEDITOR", "IDS_STRING181");
            addToMap("PSK_PROPERTYEDITOR_HLP", "IDS_STRING182");
            addToMap("PSK_PECUSTOMIZEFILE", "IDS_STRING183");
            addToMap("PSK_PECUSTOMIZEFILE_HLP", "IDS_STRING184");
            addToMap("PSK_PEFILTER", "IDS_STRING185");
            addToMap("PSK_PEFILTER_HLP", "IDS_STRING186");
            addToMap("PSK_DATA", "IDS_STRING187");
            addToMap("PSK_PRESENTATION", "IDS_STRING188");
            addToMap("PSK_ALL", "IDS_STRING189");
            addToMap("PSK_PEMAXSELECT", "IDS_STRING190");
            addToMap("PSK_PEMAXSELECT_HLP", "IDS_STRING191");
            addToMap("PSK_CODEGENERATION", "IDS_STRING194");
            addToMap("PSK_CODEGENERATION_HLP", "IDS_STRING195");
            addToMap("PSK_ROUNDTRIPBACKUPDIR", "IDS_STRING196");
            addToMap("PSK_ROUNDTRIPBACKUPDIR_HLP", "IDS_STRING197");
            addToMap("PSK_CONFIGURATIONMANAGEMENT", "IDS_STRING198");
            addToMap("PSK_CONFIGURATIONMANAGEMENT_HLP", "IDS_STRING199");
            addToMap("PSK_CMENABLED", "IDS_STRING200");
            addToMap("PSK_CMENABLED_HLP", "IDS_STRING201");
            addToMap("PSK_CMQUERYNEWPROJ", "IDS_STRING202");
            addToMap("PSK_CMQUERYNEWPROJ_HLP", "IDS_STRING203");
            addToMap("PSK_YES", "IDS_STRING204");
            addToMap("PSK_NO", "IDS_STRING205");
            addToMap("PSK_CMQUERYMOD", "IDS_STRING206");
            addToMap("PSK_CMQUERYMOD_HLP", "IDS_STRING207");
            addToMap("PSK_YESALWAYS", "IDS_STRING208");
            addToMap("PSK_NOALWAYS", "IDS_STRING209");
            addToMap("PSK_UNNAMED", "IDS_STRING211");
            addToMap("PSK_CLASSDIAGRAM", "IDS_STRING212");
            addToMap("PSK_CLASSDIAGRAM_HLP", "IDS_STRING213");
            addToMap("PSK_CREATECLASS", "IDS_STRING214");
            addToMap("PSK_CREATECLASS_HLP", "IDS_STRING215");
            addToMap("PSK_DISPLAYCOMPARTMENTTITLE", "IDS_STRING216");
            addToMap("PSK_DISPLAYCOMPARTMENTTITLE_HLP", "IDS_STRING217");
            addToMap("PSK_SELECTED", "IDS_STRING218");
            addToMap("PSK_SHOWEMPTYLISTS", "IDS_STRING219");
            addToMap("PSK_SHOWEMPTYLISTS_HLP", "IDS_STRING220");
            addToMap("PSK_SEQUENCEDIAGRAM", "IDS_STRING223");
            addToMap("PSK_SEQUENCEDIAGRAM_HLP", "IDS_STRING224");
            addToMap("PSK_CREATECLASS2", "IDS_STRING225");
            addToMap("PSK_CREATECLASS2_HLP", "IDS_STRING226");
            addToMap("PSK_NEWMESSAGE", "IDS_STRING227");
            addToMap("PSK_NEWMESSAGE_HLP", "IDS_STRING228");
            addToMap("PSK_NONE", "IDS_STRING229");
            addToMap("PSK_CREATEOPERATION", "IDS_STRING230");
            addToMap("PSK_NAMEMESSAGE", "IDS_STRING231");
            addToMap("PSK_SHIFT", "IDS_STRING236");
            addToMap("PSK_NEWDIAGRAMDEFAULTS", "IDS_STRING237");
            addToMap("PSK_NEWDIAGRAMDEFAULTS_HLP", "IDS_STRING238");
            addToMap("PSK_OPENLASTUSEDWORKSPACE", "IDS_STRING239");
            addToMap("PSK_OPENLASTUSEDWORKSPACE_HLP", "IDS_STRING240");
            addToMap("PSK_LASTUSEDWORKSPACE", "IDS_STRING267");
            addToMap("PSK_LASTUSEDWORKSPACE_HLP", "IDS_STRING268");
            addToMap("PSK_VERSIONABLE_ELEMENT_HLP", "IDS_STRING299");
            addToMap("PSK_VERSION_CONTROL_PROJECTS_HLP", "IDS_STRING300");
            addToMap("PSK_VERSION_CONTROL_CLASSES_HLP", "IDS_STRING618");
            addToMap("PSK_VERSION_CONTROL_INTERFACES_HLP", "IDS_STRING619");
            addToMap("PSK_VERSION_CONTROL_PACKAGES_HLP", "IDS_STRING620");
            addToMap("PSK_VERSION_CONTROL_ASSOCIATION_CLASSES_HLP", "IDS_STRING621");
            addToMap("PSK_VERSION_CONTROL_USE_CASES_HLP", "IDS_STRING622");
            addToMap("PSK_VERSION_CONTROL_ACTORS_HLP", "IDS_STRING623");
            addToMap("PSK_VERSIONABLE_ELEMENTS", "IDS_STRING301");
            addToMap("PSK_VERSION_CLASS_ELEMENTS", "IDS_STRING302");
            addToMap("PSK_VERSION_INTERFACE_ELEMENTS", "IDS_STRING303");
            addToMap("PSK_VERSION_PACKAGE_ELEMENTS", "IDS_STRING304");
            addToMap("PSK_VERSION_USECASE_ELEMENTS", "IDS_STRING305");
            addToMap("PSK_VERSION_ACTOR_ELEMENTS", "IDS_STRING306");
            addToMap("PSK_VERSION_PROJECT_ELEMENTS", "IDS_STRING311");
            addToMap("PSK_VERSIONEDFILENAME", "IDS_STRING312");
            addToMap("PSK_ELEMENT", "IDS_STRING313");
            addToMap("PSK_TAGGEDVALUES", "IDS_STRING314");
            addToMap("PSK_TOPLEVELID", "IDS_STRING315");
            addToMap("PSK_DOCUMENTATION", "IDS_STRING316");
            addToMap("PSK_TAGGEDVALUE", "IDS_STRING317");
            addToMap("PSK_TVNAME", "IDS_STRING318");
            addToMap("PSK_TVVALUE", "IDS_STRING319");
            addToMap("PSK_NAME", "IDS_STRING320");
            addToMap("PSK_ALIAS", "IDS_STRING321");
            addToMap("PSK_VISIBILITY", "IDS_STRING322");
            addToMap("PSK_NAMESPACE", "IDS_STRING323");
            addToMap("PSK_CLASSIFIER", "IDS_STRING324");
            addToMap("PSK_ATTRIBUTES", "IDS_STRING325");
            addToMap("PSK_OPERATIONS", "IDS_STRING326");
            addToMap("PSK_ABSTRACT", "IDS_STRING327");
            addToMap("PSK_LEAF", "IDS_STRING328");
            addToMap("Generalizations","PSK_GENERALIZATIONS", "IDS_STRING329");
            addToMap("PSK_SPECIALIZATIONS", "IDS_STRING330");
            addToMap("Associations","PSK_ASSOCIATIONS", "IDS_STRING331");
            addToMap("PSK_CLASS", "IDS_STRING332");
            addToMap("PSK_PACKAGE", "IDS_STRING333");
            addToMap("PSK_OWNEDELEMENTS", "IDS_STRING334");
            addToMap("PSK_INTERFACE", "IDS_STRING335");
            addToMap("PSK_COMPONENT", "IDS_STRING336");
            addToMap("PSK_NODE", "IDS_STRING337");
            addToMap("PSK_ARTIFACT", "IDS_STRING339");
            addToMap("PSK_TITLEBLOCK", "IDS_STRING1529");
            addToMap("PSK_ERTITLEBLOCK", "IDS_STRING1529");
            addToMap("PSK_ACTOR", "IDS_STRING340");
            addToMap("PSK_USECASE", "IDS_STRING341");
            addToMap("Association", "PSK_ASSOCIATION", "IDS_STRING342");
            addToMap("PSK_ENDS", "IDS_STRING343");
            addToMap("PSK_ISDERIVED", "IDS_STRING344");
            addToMap("PSK_ISREFLEXIVE", "IDS_STRING345");
            addToMap("PSK_ENUMERATION", "IDS_STRING346");
            addToMap("Generalization","PSK_GENERALIZATION", "IDS_STRING347");
            addToMap("PSK_SUBCLASS", "IDS_STRING348");
            addToMap("PSK_SUPERCLASS", "IDS_STRING349");
            addToMap("Realization","PSK_REALIZATION", "IDS_STRING350");
            addToMap("Dependency","PSK_DEPENDENCY", "IDS_STRING351");
            addToMap("Operation","PSK_OPERATION", "IDS_STRING352");
            addToMap("PSK_RETURNTYPE", "IDS_STRING353");
            addToMap("PSK_PARAMETERS", "IDS_STRING354");
            addToMap("PSK_FINAL", "IDS_STRING355");
            addToMap("PSK_ISSTATIC", "IDS_STRING356");
            addToMap("PSK_NATIVE", "IDS_STRING357");
            addToMap("PSK_CONCURRENCY", "IDS_STRING358");
            addToMap("PSK_QUERY", "IDS_STRING359");
            addToMap("PSK_PARAMETER", "IDS_STRING360");
            addToMap("PSK_TYPE", "IDS_STRING361");
            addToMap("PSK_DIRECTION", "IDS_STRING362");
            addToMap("PSK_PARAMDEFAULT", "IDS_STRING363");
            addToMap("Attribute","PSK_ATTRIBUTE", "IDS_STRING364");
            addToMap("Multiplicity","PSK_MULTIPLICITY", "IDS_STRING365");
            addToMap("PSK_TRANSIENT", "IDS_STRING366");
            addToMap("PSK_VOLATILE", "IDS_STRING367");
            addToMap("PSK_CLIENTCHANGEABILITY", "IDS_STRING368");
            addToMap("PSK_ORDERING", "IDS_STRING369");
            addToMap("PSK_DERIVED", "IDS_STRING370");
            addToMap("PSK_DIAGRAM", "IDS_STRING371");
            addToMap("PSK_FILENAME", "IDS_STRING372");
            addToMap("PSK_LAYOUTSTYLE", "IDS_STRING373");
            addToMap("PSK_DIAGRAMKIND", "IDS_STRING374");
            addToMap("PSK_PROJECT", "IDS_STRING375");
            addToMap("PSK_MODE", "IDS_STRING376");
            addToMap("PSK_DEFAULTLANGUAGE", "IDS_STRING377");
            addToMap("PSK_WORKSPACE", "IDS_STRING378");
            addToMap("PSK_WORKSPACE_HLP", "IDS_STRING935");
            addToMap("PSK_LOCATION", "IDS_STRING379");
            addToMap("PSK_RELATIONSHIP", "IDS_STRING380");
            addToMap("PSK_FLOW", "IDS_STRING381");
            addToMap("PSK_PRESENTATIONELEMENT", "IDS_STRING382");
            addToMap("PSK_TEMPLATEPARAMETER", "IDS_STRING383");
            addToMap("PSK_PEIMPLEMENTATION", "IDS_STRING384"); // PE for P)roperty Editor
            addToMap("PSK_IMPLEMENTINGCLASSIFIER", "IDS_STRING385");
            addToMap("PSK_CONTRACT", "IDS_STRING386");
            addToMap("PSK_COLLABORATIONOCCURENCE", "IDS_STRING387");
            addToMap("PSK_BEHAVIOR", "IDS_STRING388");
            addToMap("PSK_INCREMENT", "IDS_STRING389");
            addToMap("PSK_ASSOCIATIONEND", "IDS_STRING390");
            addToMap("PSK_PARTICIPANT", "IDS_STRING391");
            addToMap("PSK_NAVIGABLE", "IDS_STRING392");
            addToMap("PSK_PART", "IDS_STRING393");
            addToMap("PSK_CONNECTOR", "IDS_STRING394");
            addToMap("PSK_CONNECTOREND", "IDS_STRING395");
            addToMap("PSK_PORT", "IDS_STRING396");
            addToMap("PSK_RECEPTION", "IDS_STRING397");
            addToMap("PSK_ELEMENTIMPORT", "IDS_STRING398");
            addToMap("PSK_PACKAGEIMPORT", "IDS_STRING399");
            addToMap("PSK_STATEMACHINE", "IDS_STRING400");
            addToMap("PSK_DEPLOYMENT", "IDS_STRING401");
            addToMap("PSK_DEPLOYMENTSPECIFICATION", "IDS_STRING402");
            addToMap("Abstraction","PSK_ABSTRACTION", "IDS_STRING403");
            addToMap("PSK_TYPEDELEMENT", "IDS_STRING404");
            addToMap("PSK_BEHAVIORALFEATURE", "IDS_STRING405");
            addToMap("PSK_STRUCTURALFEATURE", "IDS_STRING406");
            addToMap("PSK_LIFELINE", "IDS_STRING407");
            addToMap("PSK_REPRESENTS", "IDS_STRING408");
            addToMap("PSK_DISCRIMINATOR", "IDS_STRING409");
            addToMap("PSK_REPRESENTINGCLASSIFIER", "IDS_STRING410");
            addToMap("PSK_MESSAGE", "IDS_STRING411");
            addToMap("PSK_MESSAGEKIND", "IDS_STRING412");
            addToMap("PSK_OPERATIONINVOKED", "IDS_STRING413");
            addToMap("PSK_RECEIVINGLIFELINE", "IDS_STRING414");
            addToMap("PSK_SENDINGLIFELINE", "IDS_STRING415");
            addToMap("PSK_RECEIVINGCLASSIFIER", "IDS_STRING416");
            addToMap("PSK_SENDINGCLASSIFIER", "IDS_STRING417");
            addToMap("PSK_COMBINEDFRAGMENT", "IDS_STRING418");
            addToMap("PSK_OPERANDS", "IDS_STRING419");
            addToMap("PSK_OPERATOR", "IDS_STRING420");
            addToMap("PSK_ENUMERATIONLITERAL", "IDS_STRING421");
            addToMap("PSK_ROLEBINDING", "IDS_STRING422");
            addToMap("PSK_RANGES", "IDS_STRING423");
            addToMap("PSK_ORDERED", "IDS_STRING424");
            addToMap("PSK_PACKAGEABLEELEMENT", "IDS_STRING425");
            addToMap("PSK_STATE", "IDS_STRING426");
            addToMap("PSK_TRANSITION", "IDS_STRING427");
            addToMap("PSK_PROTOCOLCONFORMANCE", "IDS_STRING428");
            addToMap("PSK_CONTAINER", "IDS_STRING429");
            addToMap("PSK_EXPRESSION", "IDS_STRING430");
            addToMap("PSK_BODY", "IDS_STRING431");
            addToMap("PSK_FEATURE", "IDS_STRING433");
            addToMap("PSK_SIGNAL", "IDS_STRING434");
            addToMap("PSK_PARTDECOMPOSITION", "IDS_STRING435");
            addToMap("PSK_INTERLIFELINECONNECTOR", "IDS_STRING436");
            addToMap("PSK_EVENT", "IDS_STRING437");
            addToMap("PSK_INTERACTION", "IDS_STRING438");
            addToMap("PSK_INTERACTIONFRAGMENT", "IDS_STRING439");
            addToMap("PSK_ACTION", "IDS_STRING440");
            addToMap("PSK_INTERACTIONOPERAND", "IDS_STRING441");
            addToMap("PSK_GATE", "IDS_STRING442");
            addToMap("PSK_COLLABORATIONOCCURRENCE", "IDS_STRING443");
            addToMap("PSK_RANGE", "IDS_STRING444");
            addToMap("PSK_LOWER", "IDS_STRING445");
            addToMap("PSK_UPPER", "IDS_STRING446");
            addToMap("PSK_STATEVERTEX", "IDS_STRING447");
            addToMap("PSK_REDEFINABLEELEMENT", "IDS_STRING448");
            addToMap("PSK_INTERACTIONOCCURRENCE", "IDS_STRING449");
            addToMap("PSK_ARGUMENT", "IDS_STRING450");
            addToMap("PSK_INTERGATECONNECTOR", "IDS_STRING451");
            addToMap("PSK_INTERACTIONCONSTRAINT", "IDS_STRING452");
            addToMap("PSK_CONSTRAINT", "IDS_STRING453");
            addToMap("Aggregation","PSK_AGGREGATION", "IDS_STRING454");
            addToMap("PSK_AGGREGATEEND", "IDS_STRING455");
            addToMap("PSK_PARTEND", "IDS_STRING456");
            addToMap("PSK_COMPOSITE", "IDS_STRING457");
            addToMap("Usage","PSK_USAGE", "IDS_STRING458");
            addToMap("PSK_COMMENT", "IDS_STRING459");
            addToMap("PSK_ANNOTATEDELEMENTS", "IDS_STRING460");
            addToMap("PSK_ASSOCIATIONCLASS", "IDS_STRING461");
            addToMap("PSK_NAVIGABLEEND", "IDS_STRING462");
            addToMap("PSK_SIGNALEVENT", "IDS_STRING463");
            addToMap("PSK_CALLEVENT", "IDS_STRING464");
            addToMap("PSK_TIMEEVENT", "IDS_STRING465");
            addToMap("PSK_CHANGEEVENT", "IDS_STRING466");
            addToMap("PSK_ACTIONSEQUENCE", "IDS_STRING467");
            addToMap("PSK_UNINTERPRETEDACTION", "IDS_STRING468");
            addToMap("PSK_CREATEACTION", "IDS_STRING469");
            addToMap("PSK_TERMINATEACTION", "IDS_STRING470");
            addToMap("PSK_CALLACTION", "IDS_STRING471");
            addToMap("PSK_RETURNACTION", "IDS_STRING472");
            addToMap("PSK_SENDACTION", "IDS_STRING473");
            addToMap("PSK_DESTROYACTION", "IDS_STRING474");
            addToMap("PSK_ASSIGNMENTACTION", "IDS_STRING475");
            addToMap("PSK_SOURCEFILEARTIFACT", "IDS_STRING476");
            addToMap("PSK_SOURCEFILE", "IDS_STRING477");
            addToMap("PSK_NAMEDELEMENT", "IDS_STRING478");
            addToMap("PSK_PUBLIC", "IDS_STRING479");
            addToMap("PSK_PROTECTED", "IDS_STRING480");
            addToMap("PSK_PRIVATE", "IDS_STRING481");
            addToMap("PSK_PEPACKAGE", "IDS_STRING482");  // PE for Property Editor
            addToMap("PSK_SEQUENTIAL", "IDS_STRING483");
            addToMap("PSK_GUARDED", "IDS_STRING484");
            addToMap("PSK_CONCURRENT", "IDS_STRING485");
            addToMap("PSK_IN", "IDS_STRING486");
            addToMap("PSK_INOUT", "IDS_STRING487");
            addToMap("PSK_OUT", "IDS_STRING488");
            addToMap("PSK_RESULT", "IDS_STRING489");
            addToMap("PSK_UNORDERED", "IDS_STRING490");
            addToMap("PSK_ORDERED2", "IDS_STRING491");
            addToMap("PSK_CPP", "IDS_STRING492");
            addToMap("PSK_UNRESTRICTED", "IDS_STRING493");
            addToMap("PSK_RESTRICTED", "IDS_STRING494");
            addToMap("PSK_ADDONLY", "IDS_STRING495");
            addToMap("PSK_REMOVEONLY", "IDS_STRING496");
            addToMap("PSK_NONE2", "IDS_STRING497");
            addToMap("PSK_HIERARCHICAL", "IDS_STRING498");
            addToMap("PSK_CIRCULAR", "IDS_STRING499");
            addToMap("PSK_SYMMETRIC", "IDS_STRING500");
            addToMap("PSK_TREE", "IDS_STRING501");
            addToMap("PSK_ORTHOGONAL", "IDS_STRING502");
            addToMap("PSK_SEQUENCE", "IDS_STRING503");
            addToMap("PSK_GLOBAL", "IDS_STRING504");
            addToMap("PSK_INCREMENTAL", "IDS_STRING505");
            addToMap("PSK_UNKNOWN", "IDS_STRING506");
            addToMap("PSK_DIAGRAM2", "IDS_STRING507");
            addToMap("PSK_ACTIVITYDIAGRAM", "IDS_STRING508");
            addToMap("PSK_PECLASSDIAGRAM", "IDS_STRING509"); // PE for Property Editor
            addToMap("PSK_COLLABDIAGRAM", "IDS_STRING510");
            addToMap("PSK_COMPONENTDIAGRAM", "IDS_STRING511");
            addToMap("PSK_DEPLOYMENTDIAGRAM", "IDS_STRING512");
            addToMap("PSK_PESEQUENCEDIAGRAM", "IDS_STRING513"); // PE for Property Editor
            addToMap("PSK_STATEDIAGRAM", "IDS_STRING514");
            addToMap("PSK_USECASEDIAGRAM", "IDS_STRING515");
            addToMap("PSK_CREATE2", "IDS_STRING516");
            addToMap("PSK_SYNCHRONOUS", "IDS_STRING517");
            addToMap("PSK_ASYNCHRONOUS", "IDS_STRING518");
            addToMap("PSK_VERSION_ASSOCIATIONCLASS_ELEMENTS", "IDS_STRING519");
            addToMap("PSK_SD_DELETECF", "IDS_SD_DELETECF");
            addToMap("PSK_SD_DELETECF_HLP", "IDS_SD_DELETECF_HLP");
            addToMap("PSK_SD_CFWIZARD", "IDS_SD_CFWIZARD");
            addToMap("PSK_SD_CFWIZARD_HLP", "IDS_SD_CFWIZARD_HLP");
            addToMap("PSK_SD_GROUP_OPERATIONS", "IDS_SD_GROUP_OPERATIONS");
            addToMap("PSK_SD_GROUP_OPERATIONS_HLP", "IDS_SD_GROUP_OPERATIONS_HLP");
            addToMap("PSK_SD_RESTRICT_OPERATIONS_BY_VISIBILITY", "IDS_SD_RESTRICT_OPERATIONS_BY_VISIBILITY");
            addToMap("PSK_SD_RESTRICT_OPERATIONS_BY_VISIBILITY_HLP", "IDS_SD_RESTRICT_OPERATIONS_BY_VISIBILITY_HLP");
            addToMap("PSK_SD_RESTRICT_OPERATIONS_SHOWN", "IDS_SD_RESTRICT_OPERATIONS_SHOWN");
            addToMap("PSK_SD_RESTRICT_OPERATIONS_SHOWN_HLP", "IDS_SD_RESTRICT_OPERATIONS_SHOWN_HLP");
            addToMap("PSK_SD_SHOW_MESSAGE_NUMBERS", "IDS_SD_SHOW_MESSAGE_NUMBERS");
            addToMap("PSK_SD_SHOW_MESSAGE_NUMBERS_HLP", "IDS_SD_SHOW_MESSAGE_NUMBERS_HLP");
            addToMap("PSK_SD_PROCESSINVOKEDOPERATION", "IDS_SD_PROCESSINVOKEDOPERATION");
            addToMap("PSK_SD_PROCESSINVOKEDOPERATION_HLP", "IDS_SD_PROCESSINVOKEDOPERATION_HLP");
            addToMap("PSK_SD_DELETEASSOCIATEDMESSAGES", "IDS_SD_DELETEASSOCIATEDMESSAGES");
            addToMap("PSK_SD_DELETEASSOCIATEDMESSAGES_HLP", "IDS_SD_DELETEASSOCIATEDMESSAGES_HLP");
            addToMap("PSK_SD_SHOW_BOUNDARY", "IDS_SD_SHOW_BOUNDARY");
            addToMap("PSK_SD_SHOW_BOUNDARY_HLP", "IDS_SD_SHOW_BOUNDARY_HLP");
            addToMap("PSK_COD_SHOW_MESSAGE_NUMBERS_HLP", "IDS_COD_SHOW_MESSAGE_NUMBERS_HLP");
            addToMap("PSK_DIAGRAMS", "IDS_STRING566");
            addToMap("PSK_DIAGRAMS_HLP", "IDS_STRING567");
            addToMap("PSK_FONTS", "IDS_STRING568");
            addToMap("PSK_FONTS_HLP", "IDS_STRING569");
            addToMap("PSK_PRESENTATION_HLP", "IDS_STRING571");
            addToMap("PSK_COLORS", "IDS_STRING1356");
            addToMap("PSK_NEVERCREATE", "IDS_STRING582");
            addToMap("PSK_DONOTHING", "IDS_STRING583");
            addToMap("PSK_LANGUAGE_HLP_I", "IDS_STRING590");
            addToMap("PSK_SUPPLIER", "IDS_STRING591");
            addToMap("PSK_CLIENT", "IDS_STRING592");
            addToMap("PSK_ASSOCIATEDARTIFACTS", "IDS_STRING593");
            addToMap("Permission","PSK_PERMISSION", "IDS_STRING610");
            addToMap("PSK_STEREOTYPES", "IDS_STRING611");
            addToMap("Implementations","PSK_IMPLEMENTATIONS", "IDS_STRING612");
            addToMap("PSK_STEREOTYPE", "IDS_STRING613");
            addToMap("PSK_TT_STEREOTYPE", "IDS_STRING613");
            addToMap("PSK_SUPPLIERDEPENDENCIES", "IDS_STRING614");
            addToMap("PSK_CLIENTDEPENDENCIES", "IDS_STRING615");
            addToMap("PSK_AUTORESIZE", "IDS_STRING624");
            addToMap("PSK_AUTORESIZE_HLP", "IDS_STRING625");
            addToMap("PSK_RESIZE_ASNEEDED", "IDS_STRING626");
            addToMap("PSK_RESIZE_EXPANDONLY", "IDS_STRING627");
            addToMap("PSK_RESIZE_UNLESSMANUAL", "IDS_STRING646");
            addToMap("PSK_RESIZE_NEVER", "IDS_STRING719");
            addToMap("PSK_RESIZEONALIASTOGGLE", "IDS_STRING1473");
            addToMap("PSK_RESIZEONALIASTOGGLE_HLP", "IDS_STRING1474");
            addToMap("PSK_CONSTRAINTS", "IDS_STRING643");
            addToMap("PSK_PEDISPLAYTYPEFSN", "IDS_STRING644");
            addToMap("PSK_PEDISPLAYTYPEFSN_HLP", "IDS_STRING645");
            addToMap("PSK_ISSTRICTFP", "IDS_STRING649");
            addToMap("PSK_RAISEDEXCEPTIONS", "IDS_STRING661");
            addToMap("PSK_EXCEPTION", "IDS_STRING662");
            addToMap("PSK_REFERENCING", "IDS_STRING706");
            addToMap("PSK_REFERRED", "IDS_STRING707");
            addToMap("PSK_REFERENCINGELE", "IDS_STRING708");
            addToMap("PSK_REFERREDELE", "IDS_STRING709");
            addToMap("PSK_REFERENCE", "IDS_STRING710");
            
            addToMap("PSK_PE_TEMPLATEPARAMETERS", "IDS_STRING833 ");
            addToMap("PSK_PE_DEFAULTELEMENT", "IDS_STRING834 ");
            addToMap("PSK_PE_ISPROPERTY", "IDS_STRING865 ");
            addToMap("PSK_PE_ISFRIEND", "IDS_STRING866 ");
            addToMap("PSK_PE_ISWITHEVENTS", "IDS_STRING867 ");
            addToMap("PSK_PE_HEAPBASED", "IDS_STRING868 ");
            addToMap("PSK_PE_PARAMKIND", "IDS_STRING869 ");
            addToMap("PSK_PE_PATTERNPARTICIPANT", "IDS_STRING192 ");
            addToMap("PSK_PE_TYPECONSTRAINT", "IDS_STRING193 ");
            addToMap("PSK_OPTIONAL", "IDS_STRING870 ");
            addToMap("PSK_BYVALUE", "IDS_STRING871 ");
            addToMap("PSK_BYREF", "IDS_STRING872 ");
            addToMap("PSK_ADDRESSOF", "IDS_STRING873 ");
            addToMap("PSK_DESIGNCENTER", "IDS_STRING1419 ");
            addToMap("PSK_DESIGNCENTER_HLP", "IDS_STRING1420 ");
            addToMap("PSK_DESIGNPATTERNCATALOG", "IDS_STRING1421 ");
            addToMap("PSK_DESIGNPATTERNCATALOG_HLP", "IDS_STRING1422 ");
            addToMap("PSK_CODEGENAFTERAPPLY", "IDS_STRING1423 ");
            addToMap("PSK_CODEGENAFTERAPPLY_HLP", "IDS_STRING1424 ");
            addToMap("PSK_PELANGUAGEFILTER", "IDS_STRING1425 ");
            addToMap("PSK_PELANGUAGEFILTER_HLP", "IDS_STRING1426 ");
            addToMap("PSK_REFERENCEDLIBRARIES", "IDS_STRING1427 ");
            addToMap("PSK_REFERENCEDLIBRARY", "IDS_STRING1428 ");
            addToMap("PSK_DISPLAYSETTINGS", "IDS_STRING1487 ");
            addToMap("PSK_DISPLAYSETTINGS_HLP", "IDS_STRING1488 ");
            addToMap("PSK_PARTICIPANT2", "IDS_STRING1493 ");
            addToMap("PSK_CHILDPROPERTIES", "IDS_STRING1564");
            addToMap("PSK_GRADIENTCOLOR_HLP","IDS_STRING2052");
            
            // Added for RoundTrip preferences
            
            addToMap("PSK_RTELEMENTS", "IDS_STRING520");
            addToMap("PSK_RTELEMENTS_HLP", "IDS_STRING521");
            addToMap("PSK_RTELEMENT_HLP", "IDS_STRING522");
            addToMap("PSK_RTMULTIPLICITYRANGE", "IDS_STRING523");
            addToMap("PSK_RTELEMENT_CLASS_HLP", "IDS_STRING596");
            addToMap("PSK_RTELEMENT_INTERFACE_HLP", "IDS_STRING597");
            addToMap("PSK_RTELEMENT_ATTRIBUTE_HLP", "IDS_STRING598");
            addToMap("PSK_RTELEMENT_OPERATION_HLP", "IDS_STRING599");
            addToMap("PSK_RTELEMENT_PACKAGE_HLP", "IDS_STRING600");
            addToMap("PSK_RTELEMENT_PARAMETER_HLP", "IDS_STRING601");
            addToMap("PSK_RTELEMENT_NAVIGABLEEND_HLP", "IDS_STRING602");
            addToMap("PSK_RTELEMENT_ASSOCIATION_HLP", "IDS_STRING603");
            addToMap("PSK_RTELEMENT_AGGREGATION_HLP", "IDS_STRING604");
            addToMap("PSK_RTELEMENT_GENERALIZATION_HLP", "IDS_STRING605");
            addToMap("PSK_RTELEMENT_IMPLEMENTATION_HLP", "IDS_STRING606");
            addToMap("PSK_RTELEMENT_ASSOCIATIONEND_HLP", "IDS_STRING607");
            addToMap("PSK_RTELEMENT_MULTIPLICITY_HLP", "IDS_STRING608");
            addToMap("PSK_RTELEMENT_PROJECT_HLP","IDS_STRING1416");
            addToMap("PSK_RTELEMENT_RANGE_HLP", "IDS_STRING609");
            addToMap("PSK_RTELEMENT_PARAMETERABLEELEMENT_HLP","IDS_STRING1930");
            addToMap("PSK_RTELEMENT_ENUMERATION_HLP","IDS_STRING1931");
            addToMap("PSK_RTELEMENT_ENUMERATIONLITERAL_HLP","IDS_STRING1932");
            addToMap("PSK_NOATTRPREFIX", "IDS_STRING594");
            addToMap("PSK_NOATTRPREFIX_HLP", "IDS_STRING595");
            addToMap("PSK_CAPONACCESSORS", "IDS_STRING650");
            addToMap("PSK_CAPONACCESSORS_HLP", "IDS_STRING651");
            
            // Added for RoundTrip Controls
            
            addToMap("PSK_RTCONTROLS", "IDS_STRING572");
            addToMap("PSK_RTCONTROLS_HLP", "IDS_STRING573");
            addToMap("PSK_RTTRANSFORMWARNING", "IDS_STRING584");
            addToMap("PSK_RTTRANSFORMWARNING_HLP", "IDS_STRING585");
            addToMap("PSK_SHOWDUPEOPDIALOG", "IDS_STRING682");
            addToMap("PSK_SHOWDUPEOPDIALOG_HLP", "IDS_STRING683");
            
            // Added for Stereotype creation support
            
            addToMap("PSK_UNKNOWNSTEREOTYPE", "IDS_STRING526 ");
            addToMap("PSK_UNKNOWNSTEREOTYPE_HLP", "IDS_STRING527 ");
            addToMap("PSK_UNKNOWN_STEREOTYPE_CREATE", "IDS_STRING528 ");
            addToMap("PSK_IN_PROJECT_PROFILE", "IDS_STRING529 ");
            addToMap("PSK_IN_CENTRAL_PROFILE", "IDS_STRING530 ");
            addToMap("PSK_UNKNOWN_STEREOTYPE_CREATE_HLP", "IDS_STRING531 ");
            
            // Custom Software Configuration Management (SCM) settings
            /*
//            addToMap("PSK_CUSTOM_PROVIDER",                        "IDS_STRING540 ");
//            addToMap("PSK_CUSTOM_PROVIDER_HLP",                    "IDS_STRING541 ");
            addToMap("PSK_COMMAND_LINE_PROVIDER",                  "IDS_STRING542 ");
            addToMap("PSK_COMMAND_LINE_PROVIDER_HLP",              "IDS_STRING543 ");
            addToMap("PSK_ADD_ARTIFACT_COMMAND_LINE",              "IDS_STRING544 ");
            addToMap("PSK_ADD_ARTIFACT_COMMAND_LINE_HLP",          "IDS_STRING545 ");
            addToMap("PSK_CHECK_OUT_ARTIFACT_COMMAND_LINE",        "IDS_STRING546 ");
            addToMap("PSK_CHECK_OUT_ARTIFACT_COMMAND_LINE_HLP",    "IDS_STRING547 ");
            addToMap("PSK_CHECK_IN_ARTIFACT_COMMAND_LINE",         "IDS_STRING548 ");
            addToMap("PSK_CHECK_IN_ARTIFACT_COMMAND_LINE_HLP",     "IDS_STRING549 ");
            addToMap("PSK_REMOVE_ARTIFACT_COMMAND_LINE",           "IDS_STRING550 ");
            addToMap("PSK_REMOVE_ARTIFACT_COMMAND_LINE_HLP",       "IDS_STRING551 ");
            addToMap("PSK_GET_LATEST_ARTIFACT_COMMAND_LINE",       "IDS_STRING552 ");
            addToMap("PSK_GET_LATEST_ARTIFACT_COMMAND_LINE_HLP",   "IDS_STRING553 ");
            addToMap("PSK_UNDO_CHECK_OUT_COMMAND_LINE",            "IDS_STRING554 ");
            addToMap("PSK_UNDO_CHECK_OUT_COMMAND_LINE_HLP",        "IDS_STRING555 ");
            addToMap("PSK_SHOW_ARTIFACT_HISTORY_OUT_COMMAND_LINE", "IDS_STRING556 ");
            addToMap("PSK_SHOW_ARTIFACT_HISTORY_COMMAND_LINE_HLP", "IDS_STRING557 ");
            addToMap("PSK_SHOW_ARTIFACT_DIFF_COMMAND_LINE",        "IDS_STRING558 ");
            addToMap("PSK_SHOW_ARTIFACT_DIFF_COMMAND_LINE_HLP",    "IDS_STRING559 ");
//            addToMap("PSK_DEFAULT_SCM_PROVIDER",                   "IDS_STRING560 ");
            addToMap("PSK_SCC_PROVIDER",                           "IDS_STRING561 ");
//            addToMap("PSK_DEFAULT_SCM_PROVIDER_HLP",               "IDS_STRING562 ");
            addToMap("PSK_SCM_CUSTOM",                             "IDS_STRING563 ");
            addToMap("PSK_SCM_COMMAND",                            "IDS_STRING564 ");
            addToMap("PSK_SCM_EXCLUSIVE_COMMAND",                  "IDS_STRING1545 ");
            addToMap("PSK_SCM_COMMAND_HLP",                        "IDS_STRING565 ");
            addToMap("PSK_LAUNCH_PROVIDER_COMMAND_LINE",           "IDS_STRING630 ");
            addToMap("PSK_LAUNCH_PROVIDER_COMMAND_LINE_HLP",       "IDS_STRING631 ");
            addToMap("PSK_OPERATION_AVAILABILITY_COMMAND_LINE",    "IDS_STRING632 ");
            addToMap("PSK_OPERATION_AVAILABILITY_COMMAND_LINE_HLP","IDS_STRING633 ");
            addToMap("PSK_SCM_VAR_BINARY_FLAG",                    "IDS_STRING636 ");
            addToMap("PSK_SCM_VAR_DESCRIPTION",                    "IDS_STRING637 ");
            addToMap("PSK_SCM_VAR_FILE_NAME",                      "IDS_STRING638 ");
            addToMap("PSK_SCM_EXPANSION_VARIABLES",                "IDS_STRING639 ");
            addToMap("PSK_SCM_EXPANSION_VARIABLES_HLP",            "IDS_STRING640 ");
            addToMap("PSK_SCM_VAR_BINARY_FLAG_HLP",                "IDS_STRING642 ");
            addToMap("PSK_SCM_ADD_FILE_COMMAND_HLP",               "IDS_STRING652 ");
            addToMap("PSK_SCM_CHECK_OUT_COMMAND_HLP",              "IDS_STRING653 ");
            addToMap("PSK_SCM_CHECK_IN_COMMAND_HLP",               "IDS_STRING654 ");
            addToMap("PSK_SCM_REMOVE_COMMAND_HLP",                 "IDS_STRING655 ");
            addToMap("PSK_SCM_GET_LATEST_COMMAND_HLP",             "IDS_STRING656 ");
            addToMap("PSK_SCM_LAUNCH_PROVIDER_COMMAND_HLP",        "IDS_STRING657 ");
            addToMap("PSK_SCM_UNDO_CHECK_OUT_COMMAND_HLP",         "IDS_STRING658 ");
            addToMap("PSK_SCM_SHOW_HISTORY_COMMAND_HLP",           "IDS_STRING659 ");
            addToMap("PSK_SCM_SHOW_DIFFERENCES_COMMAND_HLP",       "IDS_STRING660 ");
            addToMap("PSK_ADD_FOLDER_COMMAND_LINE",                "IDS_STRING676 ");
            addToMap("PSK_ADD_FOLDER_COMMAND_LINE_HLP",            "IDS_STRING677 ");
            addToMap("PSK_SCM_ADD_FOLDER_COMMAND_HLP",             "IDS_STRING678 ");
            addToMap("PSK_IS_FOLDER_CONTROLLED_COMMAND_LINE",      "IDS_STRING679 ");
            addToMap("PSK_IS_FOLDER_CONTROLLED_COMMAND_LINE_HLP",  "IDS_STRING680 ");
            addToMap("PSK_SCM_IS_FOLDER_CONTROLLED_COMMAND_HLP",   "IDS_STRING681 ");
            addToMap("PSK_SCM_SHOW_RESULTS_DLG_ONLY_ON_ERROR",     "IDS_STRING700 ");
            addToMap("PSK_SCM_SHOW_RESULTS_DLG_ONLY_ON_ERROR_HLP", "IDS_STRING701 ");
            addToMap("PSK_CMCHECKOUT_DLG",									"IDS_STRING711 ");
            addToMap("PSK_CMCHECKOUT_DLG_HLP",							"IDS_STRING712 ");
            addToMap("PSK_RT_UPDATE_MODEL_FROM_SOURCE",            "IDS_STRING616 ");
            addToMap("PSK_RT_UPDATE_MODEL_FROM_SOURCE_HLP",        "IDS_STRING617 ");
            addToMap("PSK_CVS_BIN",						        "IDS_STRING1581");
            addToMap("PSK_CVS_BIN_HLP",							"IDS_STRING1582");
            */
            
            // MessagePopupAddin
            addToMap("PSK_DISPLAYMESSAGEPOPUP",                    "IDS_STRING628 ");
            addToMap("PSK_DISPLAYMESSAGEPOPUP_HLP",                "IDS_STRING629 ");
            
            // Creating a new diagram after a new project
            addToMap("PSK_QUERYFORNEWDIAGRAM",                     "IDS_STRING634 ");
            addToMap("PSK_QUERYFORNEWDIAGRAM_HLP",                 "IDS_STRING635 ");
            
            // Reverse Engineering Preferences.
            //addToMap("PSK_REVERSEENGINEERING",                     "IDS_STRING646 ");
            addToMap("PSK_REVENG_PROMPT_TOSAVE",                   "IDS_STRING647 ");
            addToMap("PSK_REVENG_PROMPT_TOSAVE_HLP",               "IDS_STRING648 ");
            addToMap("PSK_REVERSEENGINEERING",                     "IDS_REVERSEENGINEERING ");
            addToMap("PSK_REVERSEENGINEERING_HLP",                 "IDS_REVERSEENGINEERING_HLP ");
            addToMap("PSK_RE_CREATEOPERATION",                     "IDS_RE_CREATEOPERATION ");
            addToMap("PSK_RE_CREATEOPERATION_HLP",                 "IDS_RE_CREATEOPERATION_HLP ");
            addToMap("PSK_OPRE_SHOWBASEDIRDIALOG",                 "IDS_OPRE_SHOWBASEDIRDIALOG ");
            addToMap("PSK_OPRE_SHOWBASEDIRDIALOG_HLP",             "IDS_OPRE_SHOWBASEDIRDIALOG_HLP ");
            addToMap("PSK_OPRE_SEARCHDIRS",                        "IDS_OPRE_SEARCHDIRS ");
            addToMap("PSK_OPRE_SEARCHDIRS_HLP",                    "IDS_OPRE_SEARCHDIRS_HLP ");
            
            // Project Tree Folders
            addToMap("PSK_RELATIONSHIPS",                          "IDS_STRING665 ");
            addToMap("PSK_PARTICIPANTS",                           "IDS_STRING673 ");
            addToMap("PSK_MESSAGES",                               "IDS_STRING674 ");
            addToMap("PSK_CREATE",                                 "IDS_STRING675 ");
            
            // Describe Logging Preferences
            addToMap("PSK_OUTPUT_MESSAGES",                        "IDS_STRING684");
            addToMap("PSK_OUTPUT_MESSAGES_HLP",                    "IDS_STRING685");
            addToMap("PSK_FUNC_NAMES",                             "IDS_STRING686");
            addToMap("PSK_FUNC_NAMES_HLP",                         "IDS_STRING687");
            addToMap("PSK_LOG_ERROR",                              "IDS_STRING688");
            addToMap("PSK_LOG_ERROR_HLP",                          "IDS_STRING689");
            addToMap("PSK_LOG_INFORMATION",                        "IDS_STRING690");
            addToMap("PSK_LOG_INFORMATION_HLP",                    "IDS_STRING691");
            addToMap("PSK_LOG_EXCEPTIONS",                         "IDS_STRING692");
            addToMap("PSK_LOG_EXCEPTIONS_HLP",                     "IDS_STRING693");
            addToMap("PSK_LOG_ENTRY",                              "IDS_STRING694");
            addToMap("PSK_LOG_ENTRY_HLP",                          "IDS_STRING695");
            addToMap("PSK_LOG_EXIT",                               "IDS_STRING696");
            addToMap("PSK_LOG_EXIT_HLP",                           "IDS_STRING697");
            addToMap("PSK_LOG_FILE",                               "IDS_STRING698");
            addToMap("PSK_LOG_FILE_HLP",                           "IDS_STRING699");
            
            addToMap("PSK_CMQUERY_FOR_PROJECT_RELOAD" ,            "IDS_STRING702 ");
            addToMap("PSK_CMQUERY_FOR_PROJECT_RELOAD_HLP" ,        "IDS_STRING703 ");
//            addToMap("PSK_CMQUERY_FOR_WORKSPACE_RELOAD" ,          "IDS_STRING704 ");
//            addToMap("PSK_CMQUERY_FOR_WORKSPACE_RELOAD_HLP" ,      "IDS_STRING705 ");
            addToMap("PSK_CMQUERY_FOR_DIAGRAM_RELOAD" ,            "IDS_STRING713 ");
            addToMap("PSK_CMQUERY_FOR_DIAGRAM_RELOAD_HLP" ,        "IDS_STRING714 ");
            
            // Email Support
            addToMap("PSK_EMAILSUPPORT" ,                          "IDS_STRING1659 ");
            addToMap("PSK_EMAILSUPPORT_HLP" ,                      "IDS_STRING1660 ");
            addToMap("PSK_EMAIL_INCLUDECACHED" ,                   "IDS_STRING1661 ");
            addToMap("PSK_EMAIL_INCLUDECACHED_HLP" ,               "IDS_STRING1662 ");
            addToMap("PSK_EMAIL_INCLUDEEXTARTS" ,                  "IDS_STRING1663 ");
            addToMap("PSK_EMAIL_INCLUDEEXTARTS_HLP" ,              "IDS_STRING1664 ");
            addToMap("PSK_EMAIL_INCLUDESOURCEARTS" ,               "IDS_STRING1665 ");
            addToMap("PSK_EMAIL_INCLUDESOURCEARTS_HLP" ,           "IDS_STRING1666 ");
            addToMap("PSK_EMAIL_INCLUDEREFLIBS" ,                  "IDS_STRING1667 ");
            addToMap("PSK_EMAIL_INCLUDEREFLIBS_HLP" ,              "IDS_STRING1668 ");
            addToMap("PSK_EMAIL_LONGSEARCH", "IDS_STRING1671");
            addToMap("PSK_EMAIL_LONGSEARCH_HLP", "IDS_STRING1672");
            
            // New workspace defaults
            addToMap("PSK_CREATEPRJAFTERNEWWS",                    "IDS_STRING717");
            addToMap("PSK_CREATEPRJAFTERNEWWS_HLP",                "IDS_STRING718");
            addToMap("PSK_AUTODISABLEALIASING",                    "IDS_STRING720");
            addToMap("PSK_AUTODISABLEALIASING_HLP",                "IDS_STRING721");
            
            addToMap("PSK_CMAUTO_ADD" ,                            "IDS_STRING722 ");
            addToMap("PSK_CMAUTO_ADD_HLP" ,                        "IDS_STRING723 ");
            addToMap("PSK_CMAUTO_REMOVE" ,                         "IDS_STRING724");
            addToMap("PSK_CMAUTO_REMOVE_HLP" ,                     "IDS_STRING725 ");
            addToMap("PSK_CMSOURCE_SYNC" ,                         "IDS_STRING726 ");
            addToMap("PSK_CMSOURCE_SYNC_HLP" ,                     "IDS_STRING727 ");
            
            // Types added to the property editor from the activity diagram
            addToMap("PSK_INVOCATIONNODE", "IDS_STRING773");
            addToMap("PSK_CONTROLFLOW", "IDS_STRING774");
            addToMap("PSK_OBJECTFLOW", "IDS_STRING775");
            
            addToMap("PSK_ACTIVITY", "IDS_STRING778");
            addToMap("ActivityEdge","PSK_ACTIVITYEDGE", "IDS_STRING779");
            addToMap("PSK_ACTIVITYNODE", "IDS_STRING780");
            addToMap("PSK_OBJECTNODE", "IDS_STRING781");
            addToMap("PSK_PARAMETERUSAGENODE", "IDS_STRING782");
            addToMap("PSK_SIGNALNODE", "IDS_STRING783");
            addToMap("PSK_CENTRALBUFFERNODE", "IDS_STRING784");
            addToMap("PSK_DATASTORENODE", "IDS_STRING785");
            addToMap("PSK_ACTIVITYGROUP", "IDS_STRING787");
            addToMap("PSK_INTERRUPTIBLEACTIVITYREGION", "IDS_STRING788");
            addToMap("PSK_STRUCTUREDACTIVITYGROUP", "IDS_STRING789");
            addToMap("PSK_ITERATIONACTIVITYGROUP", "IDS_STRING790");
            addToMap("PSK_CONTROLNODE", "IDS_STRING792");
            addToMap("PSK_INITIALNODE", "IDS_STRING793");
            addToMap("PSK_FINALNODE", "IDS_STRING794");
            addToMap("PSK_FLOWFINALNODE", "IDS_STRING795");
            addToMap("PSK_JOINNODE", "IDS_STRING796");
            addToMap("PSK_MERGENODE", "IDS_STRING797");
            addToMap("PSK_DECISIONNODE", "IDS_STRING799");
            addToMap("PSK_COLLABORATION", "IDS_STRING800");
            addToMap("PSK_ACTIVITYFINALNODE", "IDS_STRING801");
            addToMap("PSK_JOINFORKNODE", "IDS_STRING803");
            addToMap("PSK_DECISIONMERGENODE", "IDS_STRING804");
            addToMap("PSK_ACTIVITYPARTITION", "IDS_STRING835");
            
            addToMap("PSK_ITERATIONACTIVITYKIND", "IDS_STRING820");
            addToMap("PSK_TEST_AT_BEGIN", "IDS_STRING821");
            addToMap("PSK_TEST_AT_END", "IDS_STRING822");
            addToMap("PSK_COMPLEXACTIVITYGROUPKIND", "IDS_STRING823");
            addToMap("PSK_ITERATION", "IDS_STRING824");
            addToMap("PSK_STRUCTURED", "IDS_STRING825");
            addToMap("PSK_INTERRUPTIBLE", "IDS_STRING826");
            
            // edit control tooltip constants
            addToMap("PSK_TT_VISIBILITY", "IDS_STRING848");
            addToMap("PSK_TT_NAME", "IDS_STRING849");
            addToMap("PSK_TT_TYPE", "IDS_STRING850");
            addToMap("PSK_TT_MULTIPLICITY", "IDS_STRING851");
            addToMap("PSK_TT_ORDERING", "IDS_STRING852");
            addToMap("PSK_TT_INITIALVALUE", "IDS_STRING853");
            addToMap("PSK_TT_PROPERTY", "IDS_STRING854");
            addToMap("PSK_TT_EXPRESSION", "IDS_STRING855");
            addToMap("PSK_TT_INTERACTION_OPERATOR", "IDS_STRING856");
            addToMap("PSK_TT_INTERACTION_CONSTRAINT", "IDS_STRING857");
            addToMap("PSK_TT_DEFAULTVALUE", "IDS_STRING858");
            addToMap("PSK_TT_PARAMETERS", "IDS_STRING859");
            addToMap("PSK_TT_RANGES", "IDS_STRING860");
            addToMap("PSK_TT_ORDERED", "IDS_STRING861");
            addToMap("PSK_TT_TAGGEDVALUES", "IDS_STRING862");
            addToMap("PSK_TT_DIRECTION", "IDS_STRING863");
            addToMap("PSK_TT_RETURNTYPE", "IDS_STRING864");
            addToMap("PSK_TT_LOWER", "IDS_STRING893");
            addToMap("PSK_TT_UPPER", "IDS_STRING894");
            addToMap("PSK_TT_PARAMDEFAULT", "IDS_STRING895");
            addToMap("PSK_TT_DERIVED", "IDS_STRING896");
            addToMap("PSK_TT_PARAMETER", "IDS_STRING897");
            addToMap("PSK_TT_TAGGEDVALUE", "IDS_STRING898");
            addToMap("PSK_TT_TVNAME", "IDS_STRING899");
            addToMap("PSK_TT_TVVALUE", "IDS_STRING900");
            addToMap("PSK_TT_WITHEVENTS", "IDS_STRING1435");
            addToMap("PSK_TT_SUBROUTINE", "IDS_STRING1454");
            addToMap("PSK_TT_PARAMETERKIND", "IDS_STRING1516");
            
            addToMap("PSK_VISUALBASIC6", "IDS_STRING876");
            addToMap("PSK_VISUALBASIC6_HLP", "IDS_STRING877");
            
            addToMap("PSK_ENUMERATIONLITERALS", "IDS_STRING878");
            addToMap("PSK_SUBPARTITIONS", "IDS_STRING879");
            
            addToMap("PSK_REGION", "IDS_STRING888");
            addToMap("PSK_PROCEDURE", "IDS_STRING889");
            addToMap("PSK_REGIONS", "IDS_STRING890");
            addToMap("PSK_EXTERNALINTERFACES", "IDS_STRING907");
            
            
            addToMap("PSK_PACKAGE_IMPORT", "IDS_STRING920 ");
            addToMap("PSK_PACKAGE_IMPORTS", "IDS_STRING921 ");
            addToMap("PSK_PSEUDOSTATE", "IDS_STRING962 ");
            addToMap("PSK_FINALSTATE", "IDS_STRING967 ");
            addToMap("PSK_STOPSTATE", "IDS_STRING1418 ");
            
            addToMap("PSK_DEFAULTDIAGRAMNAME", "IDS_STRING974 ");
            addToMap("PSK_DEFAULTDIAGRAMNAME_HLP", "IDS_STRING975 ");
            addToMap("PSK_INFORMATIONLOGGING", "IDS_STRING976 ");
            addToMap("PSK_INFORMATIONLOGGING_HLP", "IDS_STRING977 ");
            addToMap("Include","PSK_INCLUDE", "IDS_STRING978 ");
            addToMap("Extend","PSK_EXTEND", "IDS_STRING979 ");
            addToMap("PSK_GRAPHIC", "IDS_STRING980 ");
            addToMap("PSK_DERIVATION", "IDS_STRING981 ");
            addToMap("PSK_TEMPLATE", "IDS_STRING982 ");
            addToMap("PSK_DERIVEDCLASSIFIER", "IDS_STRING983 ");
            addToMap("PSK_EXTENDS", "IDS_STRING984 ");
            addToMap("PSK_INCLUDES", "IDS_STRING985 ");
            addToMap("PSK_BASE", "IDS_STRING986 ");
            addToMap("PSK_EXTENSION", "IDS_STRING987 ");
            addToMap("PSK_CONDITION", "IDS_STRING988 ");
            addToMap("PSK_ADDITION", "IDS_STRING989 ");
            addToMap("PSK_EXTENSIONPOINT", "IDS_STRING990 ");
            addToMap("PSK_ACTIVITYDIAGRAM_HLP", "IDS_STRING992 ");
            addToMap("PSK_INDICATE_INTERRUP_EDGES", "IDS_STRING993 ");
            addToMap("PSK_INDICATE_INTERRUP_EDGES_HLP", "IDS_STRING994 ");
            addToMap("PSK_EXTENSIONPOINTS", "IDS_STRING995 ");
            addToMap("PSK_SOURCE", "IDS_STRING996 ");
            addToMap("PSK_TARGET", "IDS_STRING997 ");
            addToMap("PSK_DELEGATE", "IDS_STRING998 ");
            addToMap("PSK_WEBREPORT", "IDS_STRING999 ");
            addToMap("PSK_WR_FLATDIR", "IDS_STRING1000 ");
            addToMap("PSK_WEBREPORT_HLP", "IDS_STRING1001 ");
            addToMap("PSK_WR_FLATDIR_HLP", "IDS_STRING1002 ");
            addToMap("PSK_DERIVATIONCLASSIFIER", "IDS_STRING1003 ");
            addToMap("PSK_BINDING", "IDS_STRING1004 ");
            addToMap("PSK_BINDINGS", "IDS_STRING1005 ");
            addToMap("PSK_FORMAL", "IDS_STRING1006 ");
            addToMap("PSK_ACTUAL", "IDS_STRING1007 ");
            addToMap("PSK_SINGLECOPY", "IDS_STRING1008 ");
            addToMap("PSK_FLOWCHART", "IDS_STRING1009 ");
            addToMap("PSK_ACTIVITYKIND", "IDS_STRING1010 ");
            addToMap("PSK_INCOMING", "IDS_STRING271 ");
            addToMap("PSK_OUTGOING", "IDS_STRING272 ");
            addToMap("PSK_INCOMINGEDGES", "IDS_STRING271 ");
            addToMap("PSK_OUTGOINGEDGES", "IDS_STRING272 ");
            addToMap("PSK_PRECONDITIONS", "IDS_STRING279 ");
            addToMap("PSK_POSTCONDITIONS", "IDS_STRING280 ");
            addToMap("PSK_MULTIPLEINVOCATION", "IDS_STRING281 ");
            addToMap("PSK_INVOCATION_SYNCHRONOUS", "IDS_STRING282 ");
            addToMap("PSK_SHOWSTEREOTYPEICONS", "IDS_STRING1011 ");
            addToMap("PSK_SHOWSTEREOTYPEICONS_HLP", "IDS_STRING1012 ");
            addToMap("PSK_LIFO", "IDS_STRING283 ");
            addToMap("PSK_FIFO", "IDS_STRING284");
            addToMap("PSK_EXTERNAL", "IDS_STRING285");
            addToMap("PSK_DIMENSION", "IDS_STRING286");
            addToMap("PSK_INITIALSTATE", "IDS_STRING291");
            addToMap("PSK_JOINSTATE", "IDS_STRING292");
            addToMap("PSK_DECISION", "IDS_STRING744");
            addToMap("PSK_SHALLOWHISTORY", "IDS_STRING786");
            addToMap("PSK_DEEPHISTORY", "IDS_STRING791");
            addToMap("PSK_ENTRYPOINT", "IDS_STRING798");
            addToMap("PSK_JUNCTION", "IDS_STRING802");
            addToMap("PSK_ABORTEDFINALSTATE", "IDS_STRING819");
            addToMap("PSK_COMPOSITESTATE", "IDS_STRING1014");
            addToMap("PSK_SUBMACHINESTATE", "IDS_STRING1015");
            addToMap("PSK_INCOMINGTRANSITIONS", "IDS_STRING1016");
            addToMap("PSK_OUTGOINGTRANSITIONS", "IDS_STRING1017");
            addToMap("PSK_DEFERRABLEEVENTS", "IDS_STRING1019");
            addToMap("PSK_SUBMACHINE", "IDS_STRING1020");
            addToMap("PSK_DOACTIVITY", "IDS_STRING1021");
            addToMap("PSK_EXIT", "IDS_STRING1022");
            addToMap("PSK_ENTRY", "IDS_STRING1023");
            addToMap("PSK_PE_ORTHOGONAL", "IDS_STRING1024");
            addToMap("PSK_REFERREDOPERATIONS", "IDS_STRING1025");
            addToMap("PSK_TRIGGER", "IDS_STRING1026");
            addToMap("PSK_EFFECT", "IDS_STRING1027");
            addToMap("PSK_GUARD", "IDS_STRING1028");
            addToMap("PSK_INTERNAL", "IDS_STRING1029");
            addToMap("PSK_PROTOCOL", "IDS_STRING1030");
            addToMap("PSK_ISSERVICE", "IDS_STRING1031");
            addToMap("PSK_ISSIGNAL", "IDS_STRING1032");
            addToMap("PSK_REQUIREDINTERFACES", "IDS_STRING1033");
            addToMap("PSK_PROVIDEDINTERFACES", "IDS_STRING1034");
            addToMap("PSK_DEPLOYMENTS", "IDS_STRING1035");
            addToMap("PSK_DEPLOYEDELEMENTS", "IDS_STRING1036");
            addToMap("PSK_ASKBEFORELAYOUT", "IDS_STRING1037");
            addToMap("PSK_ASKBEFORELAYOUT_HLP", "IDS_STRING1038");
            addToMap("PSK_USECASEDETAILS", "IDS_STRING1039");
            addToMap("PSK_USECASEDETAIL", "IDS_STRING1040");
            addToMap("PSK_SUBDETAILS", "IDS_STRING1041");
            addToMap("PSK_ISCONSTRUCTOR", "IDS_STRING1050");
            addToMap("PSK_ISDESTRUCTOR", "IDS_STRING1051");
            addToMap("PSK_WR_SHOWHIDDENTVS", "IDS_STRING1052");
            addToMap("PSK_WR_SHOWHIDDENTVS_HLP", "IDS_STRING1053");
            addToMap("PSK_QUALIFIERS", "IDS_STRING1080");
            addToMap("PSK_INGREFERENCES", "IDS_STRING1081");
            addToMap("PSK_EDREFERENCES", "IDS_STRING1082");
            addToMap("PSK_REDEFININGOPERATIONS", "IDS_STRING1083");
            addToMap("PSK_REDEFININGATTRIBUTES", "IDS_STRING1084");
            addToMap("PSK_REDEFINED", "IDS_STRING1087");
            addToMap("PSK_PRIMARYKEY", "IDS_STRING1088");
            addToMap("PSK_PE_ACTORPARTICIPANT", "IDS_STRING1093");
            addToMap("PSK_PE_CLASSPARTICIPANT", "IDS_STRING1094");
            addToMap("PSK_PE_INTERFACEPARTICIPANT", "IDS_STRING1095");
            addToMap("PSK_PE_USECASEPARTICIPANT", "IDS_STRING1096");
            addToMap("PSK_DESIGNPATTERN", "IDS_STRING1265");
            addToMap("PSK_CHOICEPSUEDOSTATE", "IDS_STRING1266");
            addToMap("PSK_MULTIRECEIVE", "IDS_STRING1267");
            addToMap("PSK_MULTICAST", "IDS_STRING1268");
            addToMap("PSK_READ", "IDS_STRING1269");
            addToMap("PSK_UPDATE", "IDS_STRING1270");
            addToMap("PSK_DELETE", "IDS_STRING1271");
            addToMap("PSK_REPRESENTEDFEATURE", "IDS_STRING1272");
            addToMap("PSK_SPECIFICATION", "IDS_STRING1273");
            addToMap("PSK_CONTEXT", "IDS_STRING1274");
            addToMap("PSK_ISREENTRANT", "IDS_STRING1275");
            addToMap("PSK_CONTENTS", "IDS_STRING1276");
            addToMap("PSK_ISSUBMACHINESTATE", "IDS_STRING1277");
            addToMap("PSK_ISSIMPLE", "IDS_STRING1278");
            addToMap("PSK_ISCOMPOSITE", "IDS_STRING1279");
            addToMap("PSK_INSTANTIATION", "IDS_STRING1280");
            addToMap("PSK_ELEMENTIMPORTS", "IDS_STRING1281");
            addToMap("PSK_DIRECT", "IDS_STRING1282");
            addToMap("PSK_INDIRECT", "IDS_STRING1283");
            addToMap("PSK_INITIALCARDINALITY", "IDS_STRING1284");
            addToMap("PSK_DEFININGEND", "IDS_STRING1285");
            addToMap("PSK_DEPLOYMENTDESCRIPTORS", "IDS_STRING1286");
            addToMap("PSK_DEPLOYMENTLOCATION", "IDS_STRING1287");
            addToMap("PSK_EXECUTIONLOCATION", "IDS_STRING1288");
            addToMap("PSK_PRECONDITION", "IDS_STRING1365");
            addToMap("PSK_POSTCONDITION", "IDS_STRING1366");
            addToMap("PSK_IMPORTEDPACKAGES", "IDS_STRING1367");
            addToMap("PSK_IMPORTEDELEMENTS", "IDS_STRING1368");
            addToMap("PSK_GROUPS", "IDS_STRING1369");
            addToMap("PSK_NODES", "IDS_STRING1370");
            addToMap("PSK_ASSOCIATEDDIAGRAMS", "IDS_STRING1438");
            addToMap("PSK_ASSOCIATEDELEMENTS", "IDS_STRING1439");
            //#if _MSC_VER < 1300 // 6.1
            addToMap("PSK_WR_INCLUDEIMPORTED", "IDS_STRING1445 ");
            addToMap("PSK_WR_INCLUDEIMPORTED_HLP", "IDS_STRING1446 ");
            //#endif
            addToMap("PSK_OVERWRITEPARTICIPANTS", "IDS_STRING1447 ");
            addToMap("PSK_OVERWRITEPARTICIPANTS_HLP", "IDS_STRING1448 ");
            addToMap("PSK_PE_ISSUBROUTINE", "IDS_STRING1449 ");
            addToMap("PSK_PE_ISVIRTUAL", "IDS_STRING1450 ");
            addToMap("PSK_PE_ISOVERRIDE", "IDS_STRING1451 ");
            addToMap("PSK_PE_ISDELEGATE", "IDS_STRING1452 ");
            addToMap("PSK_PE_ISINDEXER", "IDS_STRING1453 ");
            
            //#if _MSC_VER >= 1300 // 6.5
            addToMap("PSK_WR_INCLUDEIMPORTEDELEMENTS", "IDS_STRING1445 ");
            addToMap("PSK_WR_INCLUDEIMPORTEDELEMENTS_HLP", "IDS_STRING1446 ");
            addToMap("PSK_WR_INCLUDEIMPORTEDPACKAGES", "IDS_STRING1647 ");
            addToMap("PSK_WR_INCLUDEIMPORTEDPACKAGES_HLP", "IDS_STRING1648 ");
            //#endif
            addToMap("PSK_WR_COPYARTS", "IDS_STRING1641 ");
            addToMap("PSK_WR_COPYARTS_HLP", "IDS_STRING1642 ");
            addToMap("PSK_WR_INCLUDEEXTARTS", "IDS_STRING1643 ");
            addToMap("PSK_WR_INCLUDEEXTARTS_HLP", "IDS_STRING1644 ");
            addToMap("PSK_WR_INCLUDESOURCEARTS", "IDS_STRING1645 ");
            addToMap("PSK_WR_INCLUDESOURCEARTS_HLP", "IDS_STRING1646 ");
            
            addToMap("PSK_SHORT_TABFONT", "IDS_STRING1085");
            addToMap("PSK_PROFILE", "IDS_STRING1086");
            addToMap("PSK_ANNOTATEDELEMENT", "IDS_STRING1628");
            addToMap("PSK_RAISEDEXCEPTION", "IDS_STRING1629");
            addToMap("PSK_QUICKFIND", "IDS_STRING1632");
            addToMap("PSK_QUICKFIND_HLP", "IDS_STRING1633");
            addToMap("PSK_QUICKFINDELEMENT_HLP", "IDS_STRING1634");
            addToMap("PSK_COMPONENTDIAGRAM_HLP", "IDS_STRING1635");
            addToMap("PSK_DEPLOYMENTDIAGRAM_HLP", "IDS_STRING1636");
            addToMap("PSK_ENTITYDIAGRAM_HLP", "IDS_STRING1637");
            addToMap("PSK_STATEDIAGRAM_HLP", "IDS_STRING1638");
            addToMap("PSK_USECASEDIAGRAM_HLP", "IDS_STRING1639");
            addToMap("PSK_ENTITY", "IDS_STRING1640");
            
            // The elements to be deleted when their enclosing namespace is deleted
            
            addToMap("PSK_NOTIFIED_ELEMENTS", "IDS_STRING1371");
            addToMap("PSK_NOTIFIED_ELEMENTS_HLP" , "IDS_STRING1372 ");
            addToMap("PSK_NOTIFIED_CLASS_ELEMENTS" , "IDS_STRING1373 ");
            addToMap("PSK_NOTIFIED_CLASSES_HLP" , "IDS_STRING1374 ");
            addToMap("PSK_NOTIFIED_INTERFACE_ELEMENTS" , "IDS_STRING1375 ");
            addToMap("PSK_NOTIFIED_INTERFACES_HLP" , "IDS_STRING1376 ");
            addToMap("PSK_NOTIFIED_PACKAGE_ELEMENTS" , "IDS_STRING1377 ");
            addToMap("PSK_NOTIFIED_PACKAGES_HLP" , "IDS_STRING1378 ");
            addToMap("PSK_NOTIFIED_ASSOCIATIONCLASS_ELEMENTS" , "IDS_STRING1379 ");
            addToMap("PSK_NOTIFIED_ASSOCIATION_CLASSES_HLP" , "IDS_STRING1380 ");
            addToMap("PSK_NOTIFIED_USECASE_ELEMENTS" , "IDS_STRING1381 ");
            addToMap("PSK_NOTIFIED_USE_CASES_HLP" , "IDS_STRING1382 ");
            addToMap("PSK_NOTIFIED_ACTOR_ELEMENTS" , "IDS_STRING1383 ");
            addToMap("PSK_NOTIFIED_ACTORS_HLP" , "IDS_STRING1384 ");
            addToMap("PSK_NOTIFIED_PARTFACADE_ELEMENTS" , "IDS_STRING1385 ");
            addToMap("PSK_NOTIFIED_PARTFACADE_HLP" , "IDS_STRING1386 ");
            addToMap("PSK_PROJECTSAVE" , "IDS_STRING1403 ");
            addToMap("PSK_PROJECTSAVE_HLP" , "IDS_STRING1404 ");
            
            addToMap("PSK_INDENTATIONSPACES" , "IDS_STRING1387 ");
            addToMap("PSK_INDENTATIONSPACES_HLP" , "IDS_STRING1388 ");
            
            addToMap("PSK_COLLECTION_OVERRIDE_DEFAULT" , "IDS_STRING1475");
            addToMap("PSK_COLLECTION_OVERRIDE_DEFAULT_HLP" , "IDS_STRING1476");
            addToMap("PSK_USE_GENERICS_DEFAULT" , "IDS_STRING2001");
            addToMap("PSK_USE_GENERICS_DEFAULT_HLP" , "IDS_STRING2002");

            addToMap("PSK_COLLECTION_OVERRIDE_DATA_TYPE" , "IDS_STRING2003");
            addToMap("PSK_COLLECTION_OVERRIDE_DATA_TYPE_HLP" , "IDS_STRING2004");
            addToMap("PSK_USE_GENERICS" , "IDS_STRING2005");
            addToMap("PSK_USE_GENERICS_HLP" , "IDS_STRING2006");

            addToMap("PSK_PROJECT_LOC_QUERY" , "IDS_STRING1485 ");
            addToMap("PSK_PROJECT_LOC_QUERY_HLP" , "IDS_STRING1486 ");
            
            addToMap("PSK_TT_LIFENAME" , "IDS_STRING1389 ");
            addToMap("PSK_TT_REPRESENTINGCLASSIFIER" , "IDS_STRING1390 ");
            
            addToMap("PSK_CMARTIFACT_SYNC" , "IDS_STRING1405 ");
            addToMap("PSK_CMARTIFACT_SYNC_HLP" , "IDS_STRING1406");
            addToMap("PSK_PE_ISACTIVE" , "IDS_STRING1407");
            addToMap("PSK_ALIASEDNAME" , "IDS_STRING1408");
            addToMap("PSK_ACTUALTYPE" , "IDS_STRING1409");
            addToMap("PSK_TYPEDECORATION" , "IDS_STRING1410");
            addToMap("PSK_INTERRUPTINGEDGES" , "IDS_STRING1415");
            
            addToMap("PSK_VERSION_ACTIVITY_ELEMENTS" , "IDS_STRING1429 ");
            addToMap("PSK_VERSION_CONTROL_ACTIVITY_HLP" , "IDS_STRING1432");
            addToMap("PSK_VERSION_INTERACTION_ELEMENTS" , "IDS_STRING1430");
            addToMap("PSK_VERSION_CONTROL_INTERACTION_HLP" , "IDS_STRING1433");
            addToMap("PSK_VERSION_STATEMACHINE_ELEMENTS" , "IDS_STRING1431");
            addToMap("PSK_VERSION_CONTROL_STATEMACHINE_HLP" , "IDS_STRING1434");
            
            // Showing / Hiding aliased names
            addToMap("PSK_SHOWALIASEDNAMES" , "IDS_STRING1471");
            addToMap("PSK_SHOWALIASEDNAMES_HLP" , "IDS_STRING1472");
            addToMap("PSK_SHOW_SHORT_SOURCE_FILE_NAMES" , "IDS_STRING1496");
            addToMap("PSK_SHOW_SHORT_SOURCE_FILE_NAMES_HLP" , "IDS_STRING1497");
            
            // Find Dialog Preferences
            addToMap("PSK_FINDDIALOG", "IDS_STRING586");
            addToMap("PSK_FINDDIALOG_HLP", "IDS_STRING587");
            addToMap("PSK_FINDDLG_OPENCLOSEDPRJ_HLP", "IDS_STRING588");
            addToMap("PSK_FINDDLG_OPENCLOSEDPRJ", "IDS_STRING589");
            addToMap("PSK_FINDDLG_LONGSEARCH", "IDS_STRING1436");
            addToMap("PSK_FINDDLG_LONGSEARCH_HLP", "IDS_STRING1437");
            addToMap("PSK_FINDDLG_DISPCOLS", "IDS_STRING1498");
            addToMap("PSK_FINDDLG_DISPCOLS_HLP", "IDS_STRING1499");
            addToMap("PSK_FINDDLG_ICON", "IDS_STRING1500");
            addToMap("PSK_FINDDLG_ICON_HLP", "IDS_STRING1501");
            addToMap("PSK_FINDDLG_NAME", "IDS_STRING1502");
            addToMap("PSK_FINDDLG_NAME_HLP", "IDS_STRING1503");
            addToMap("PSK_FINDDLG_ALIAS", "IDS_STRING1504");
            addToMap("PSK_FINDDLG_ALIAS_HLP", "IDS_STRING1505");
            addToMap("PSK_FINDDLG_TYPE", "IDS_STRING1506");
            addToMap("PSK_FINDDLG_TYPE_HLP", "IDS_STRING1507");
            addToMap("PSK_FINDDLG_FULL", "IDS_STRING1508");
            addToMap("PSK_FINDDLG_FULL_HLP", "IDS_STRING1509");
            addToMap("PSK_FINDDLG_PROJECT", "IDS_STRING1510");
            addToMap("PSK_FINDDLG_PROJECT_HLP", "IDS_STRING1511");
            addToMap("PSK_FINDDLG_ID", "IDS_STRING1512");
            addToMap("PSK_FINDDLG_ID_HLP", "IDS_STRING1513");
            
            addToMap("PSK_SCM_USER_NAME", "IDS_STRING1516");
            addToMap("PSK_SCM_USER_NAME_HLP", "IDS_STRING1517");
            addToMap("PSK_SCM_SERVER_NAME", "IDS_STRING1518");
            addToMap("PSK_SCM_SERVER_NAME_HLP", "IDS_STRING1519");
            addToMap("PSK_SCM_SERVER_MODE", "IDS_STRING1520");
            addToMap("PSK_SCM_SERVER_MODE_HLP", "IDS_STRING1521");
            addToMap("PSK_SCM_REPOSITORY_LOCATION", "IDS_STRING1522");
            addToMap("PSK_SCM_REPOSITORY_LOCATION_HLP", "IDS_STRING1523");
            addToMap("PSK_SCM_PASSWORD_REQUIRED", "IDS_STRING1524");
            addToMap("PSK_SCM_PASSWORD_REQUIRED_HLP", "IDS_STRING1525");
            addToMap("PSK_GET_LOGIN_STATUS_COMMAND_LINE", "IDS_STRING1526");
            addToMap("PSK_GET_LOGIN_STATUS_COMMAND_LINE_HLP", "IDS_STRING1527");
            addToMap("PSK_SCM_GET_LOGIN_STATUS_COMMAND_HLP", "IDS_STRING1528");
            
            addToMap("PSK_RECONNECTTONODEBOUNDARY", "IDS_STRING1537");
            addToMap("PSK_RECONNECTTONODEBOUNDARY_HLP", "IDS_STRING1538");
            
            // Help Text for Exclusive SCM Commands
            addToMap("PSK_SCM_ADD_FOLDER_EXCLUSIVE_COMMAND_HLP", "IDS_STRING1546");
            addToMap("PSK_SCM_ADD_FILE_EXCLUSIVE_COMMAND_HLP", "IDS_STRING1547");
            addToMap("PSK_SCM_CHECK_IN_EXCLUSIVE_COMMAND_HLP", "IDS_STRING1548");
            addToMap("PSK_SCM_CHECK_OUT_EXCLUSIVE_COMMAND_HLP", "IDS_STRING1549");
            addToMap("PSK_SCM_GET_LATEST_EXCLUSIVE_COMMAND_HLP", "IDS_STRING1550");
            addToMap("PSK_SCM_REMOVE_EXCLUSIVE_COMMAND_HLP", "IDS_STRING1551");
            addToMap("PSK_SCM_UNDO_CHECKOUT_EXCLUSIVE_COMMAND_HLP", "IDS_STRING1552");
            addToMap("PSK_SCM_EXCLUSIVE_MODE", "IDS_STRING1553");
            addToMap("PSK_SCM_EXCLUSIVE_MODE_HLP", "IDS_STRING1554");
            
            addToMap("PSK_LIST_FILES_COMMAND_LINE", "IDS_STRING1555 ");
            addToMap("PSK_LIST_FILES_COMMAND_LINE_HLP", "IDS_STRING1556 ");
            addToMap("PSK_SCM_LIST_FILES_COMMAND_HLP","IDS_STRING1557 ");
            addToMap("PSK_CVS_SERVER","IDS_STRING1558 ");
            addToMap("PSK_CVS_SERVER_HLP","IDS_STRING1559 ");
            
            // Requirements Property Editor
            addToMap("PSK_DESCRIPTION","IDS_STRING1560 ");
            addToMap("PSK_REQUIREMENT","IDS_STRING1561 ");
            
            // Names and Values for properties
            addToMap("Names","PSK_NAMES","IDS_STRING1570 ");
            addToMap("Values","PSK_VALUES", "IDS_STRING1571");
            
           
            // Another property editor preference as well as compare and merge preferences
            addToMap("PSK_PEPROPERTYORDER", "IDS_STRING1580 ");
            addToMap("PSK_PEPROPERTYORDER_HLP", "IDS_STRING1565 ");
            addToMap("PSK_ALPHABETICALLY", "IDS_STRING1575 ");
            addToMap("PSK_METATYPE", "IDS_STRING1574 ");
            
            addToMap("PSK_COMPAREANDMERGE", "IDS_STRING1576 ");
            addToMap("PSK_COMPAREANDMERGE_HLP", "IDS_STRING1577 ");
            addToMap("PSK_COMPAREADVANCEDATTRS", "IDS_STRING1579 ");
            addToMap("PSK_COMPAREADVANCEDATTRS_HLP", "IDS_STRING1578 ");
            
            // ERTitleBlock
            addToMap("PSK_AUTHOR", "IDS_STRING1613 ");
            addToMap("PSK_COMPANY", "IDS_STRING1614 ");
            addToMap("PSK_VERSION", "IDS_STRING1615 ");
            addToMap("PSK_MODIFIED", "IDS_STRING1616 ");
            addToMap("PSK_COPYRIGHT", "IDS_STRING1617 ");
            addToMap("PSK_COPYRIGHTDATE", "IDS_STRING1620 ");
            addToMap("PSK_COPYRIGHTCOMPANY", "IDS_STRING1621 ");
            
            // EREntity
            addToMap("PSK_ERENTITY", "IDS_STRING1622 ");
            
            // ERView
            addToMap("PSK_VIEW", "IDS_STRING1623 ");
            
            // EREntityAssociation
            addToMap("EREntityAssociation","PSK_ERENTITYASSOCIATION", "IDS_STRING1624 ");
            
            addToMap("PSK_UPDATE_MODEL_UPON_FOCUS_CHANGE","IDS_STRING1618 ");
            addToMap("PSK_UPDATE_MODEL_UPON_FOCUS_CHANGE_HLP","IDS_STRING1619 ");
            addToMap("PSK_ERATTRIBUTE" , "IDS_STRING1625 ");
            addToMap("PSK_ERATTRIBUTES" , "IDS_STRING1627 ");
            addToMap("EREntityAssociations","PSK_ERENTITYASSOCIATIONS", "IDS_STRING1626");
            
            addToMap("PSK_QUERY_DURING_MOVEMENT" , "IDS_STRING1651 ");
            addToMap("PSK_QUERY_DURING_MOVEMENT_HLP" , "IDS_STRING1652 ");
            
            addToMap("PSK_FILTER_COLLAPSE_NODES_WARNING", "IDS_STRING1940");
            addToMap("PSK_FILTER_COLLAPSE_NODES_WARNING_HLP", "IDS_STRING1941");
            addToMap("PSK_PROMPT_TO_SAVE_DIAGRAM", "IDS_STRING1942");
            addToMap("PSK_PROMPT_TO_SAVE_DIAGRAM_HLP", "IDS_STRING1943");
            
            addToMap("PSK_OPERATION_ELEMENTS", "IDS_STRING2010");
            addToMap("PSK_OPERATION_ELEMENTS_HLP", "IDS_STRING2011");
            addToMap("PSK_RE_SHOW_OUTPUT", "IDS_STRING2012");
            addToMap("PSK_RE_SHOW_OUTPUT_HLP", "IDS_STRING2013");

            addToMap("PSK_SOURCE_CODE", "IDS_STRING2020");
            addToMap("PSK_SOURCE_CODE_HLP", "IDS_STRING2021");
            
            addToMap("PSK_MODEL_ELEMENT_AUTOMATION", "IDS_STRING2030");
            addToMap("PSK_MODEL_ELEMENT_AUTOMATION_HLP", "IDS_STRING2031");
            
            addToMap("PSK_CODE_GENERATION", "IDS_STRING2040");
            addToMap("PSK_CODE_GENERATION_HLP", "IDS_STRING2041");
            addToMap("PSK_GC_SHOW_OUTPUT", "IDS_STRING2042");
            addToMap("PSK_GC_SHOW_OUTPUT_HLP", "IDS_STRING2043");
            
            // source file artifact property
            addToMap("PSK_SOURCE_FILE_ARTIFACTS", "IDS_STRING2050");
            addToMap("PSK_SOURCE_FILE_ARTIFACTS_HLP", "IDS_STRING2051");
            
            // open previous diagrams on project loading
            addToMap("PSK_OPENPROJECTDIAGRAMS_HLP", "IDS_STRING2053");
            
            // Add the colors and fonts
            addColorsAndFontsToMap();
        }
    }
    
    public void addColorsAndFontsToMap() {
        // Fonts dealing with other dialogs (ie Property Editor or Source Pane)
        addToMap("PSK_DEFAULTFONT", "IDS_STRING243");
        addToMap("PSK_DEFAULTFONT_HLP", "IDS_STRING244");
        addToMap("PSK_GRIDFONT", "IDS_STRING247");
        addToMap("PSK_GRIDFONT_HLP", "IDS_STRING248");
        addToMap("PSK_DOCFONT", "IDS_STRING663");
        addToMap("PSK_DOCFONT_HLP", "IDS_STRING664");
        
        // Colors in DefaultColors.etc
        addToMap("PSK_BORDERCOLOR", "IDS_STRING1675");
        addToMap("PSK_BORDERCOLOR_DESC", "IDS_STRING1676");
        addToMap("PSK_CLASSDRAWENGINE", "IDS_STRING1677");
        addToMap("PSK_CLASSDRAWENGINE_DESC", "IDS_STRING1678");
        addToMap("PSK_FILLCOLOR", "IDS_STRING1679");
        addToMap("PSK_FILLCOLOR_DESC", "IDS_STRING1680");
        addToMap("PSK_LISTTITLEFONT", "IDS_STRING1681");
        addToMap("PSK_LISTTITLEFONT_DESC", "IDS_STRING1682");
        addToMap("PSK_NAMEFONT", "IDS_STRING1683");
        addToMap("PSK_NAMEFONT_DESC", "IDS_STRING1684");
        addToMap("PSK_PKGATTRFONT", "IDS_STRING1685");
        addToMap("PSK_PKGATTRFONT_DESC", "IDS_STRING1686");
        addToMap("PSK_PKGIMPORTFONT", "IDS_STRING1687");
        addToMap("PSK_PKGIMPORTFONT_DESC", "IDS_STRING1688");
        addToMap("PSK_PKGOPERFONT", "IDS_STRING1689");
        addToMap("PSK_PKGOPERFONT_DESC", "IDS_STRING1690");
        addToMap("PSK_PRESENTATIONFONTSANDCOLORS", "IDS_STRING1691");
        addToMap("PSK_PRESENTATIONFONTSANDCOLORS_HLP", "IDS_STRING1692");
        addToMap("PSK_PRIATTRFONT", "IDS_STRING1693");
        addToMap("PSK_PRIATTRFONT_DESC", "IDS_STRING1694");
        addToMap("PSK_PRIOPERFONT", "IDS_STRING1695");
        addToMap("PSK_PRIOPERFONT_DESC", "IDS_STRING1696");
        addToMap("PSK_PROATTRFONT", "IDS_STRING1697");
        addToMap("PSK_PROATTRFONT_DESC", "IDS_STRING1698");
        addToMap("PSK_PROOPERFONT", "IDS_STRING1699");
        addToMap("PSK_PROOPERFONT_DESC", "IDS_STRING1700");
        addToMap("PSK_PUBATTRFONT", "IDS_STRING1701");
        addToMap("PSK_PUBATTRFONT_DESC", "IDS_STRING1702");
        addToMap("PSK_PUBOPERFONT", "IDS_STRING1703");
        addToMap("PSK_PUBOPERFONT_DESC", "IDS_STRING1704");
        addToMap("PSK_STATICTEXTFONT", "IDS_STRING1705");
        addToMap("PSK_STATICTEXTFONT_DESC", "IDS_STRING1706");
        addToMap("PSK_STEREOFONT", "IDS_STRING1707");
        addToMap("PSK_STEREOFONT_DESC", "IDS_STRING1708");
        addToMap("PSK_TAGGEDVALUESFONT", "IDS_STRING1709");
        addToMap("PSK_TAGGEDVALUESFONT_DESC", "IDS_STRING1710");
        addToMap("PSK_TEMPLATEFONT", "IDS_STRING1711");
        addToMap("PSK_TEMPLATEFONT_DESC", "IDS_STRING1712");
        addToMap("PSK_TEMPPARAMFONT", "IDS_STRING1713");
        addToMap("PSK_TEMPPARAMFONT_DESC", "IDS_STRING1714");
        addToMap("PSK_ACTIVITYEDGEDRAWENGINE", "IDS_STRING1715");
        addToMap("PSK_ACTIVITYEDGEDRAWENGINE_DESC", "IDS_STRING1716");
        addToMap("PSK_ACTIVITYFINALBORDERCOLOR", "IDS_STRING1717");
        addToMap("PSK_ACTIVITYFINALBORDERCOLOR_DESC", "IDS_STRING1718");
        addToMap("PSK_ACTIVITYFINALFILLCOLOR", "IDS_STRING1719");
        addToMap("PSK_ACTIVITYFINALFILLCOLOR_DESC", "IDS_STRING1720");
        addToMap("PSK_ACTIVITYGROUPDRAWENGINE", "IDS_STRING1721");
        addToMap("PSK_ACTIVITYGROUPDRAWENGINE_DESC", "IDS_STRING1722");
        addToMap("PSK_ACTIVITYNODEDRAWENGINE", "IDS_STRING1723");
        addToMap("PSK_ACTIVITYNODEDRAWENGINE_DESC", "IDS_STRING1724");
        addToMap("PSK_ACTORDRAWENGINE", "IDS_STRING1725");
        addToMap("PSK_ACTORDRAWENGINE_DESC", "IDS_STRING1726");
        addToMap("PSK_ADLABELDRAWENGINE", "IDS_STRING1727");
        addToMap("PSK_ADLABELDRAWENGINE_DESC", "IDS_STRING1728");
        addToMap("PSK_ARTIFACTDRAWENGINE", "IDS_STRING1729");
        addToMap("PSK_ARTIFACTDRAWENGINE_DESC", "IDS_STRING1730");
        addToMap("PSK_ASSOCIATIONCLASSCONNECTORDRAWENGINE", "IDS_STRING1731");
        addToMap("PSK_ASSOCIATIONCLASSCONNECTORDRAWENGINE_DESC", "IDS_STRING1732");
        addToMap("PSK_ASSOCIATIONEDGEDRAWENGINE", "IDS_STRING1733");
        addToMap("PSK_ASSOCIATIONEDGEDRAWENGINE_DESC", "IDS_STRING1734");
        addToMap("PSK_BOXBORDERCOLOR", "IDS_STRING1737");
        addToMap("PSK_BOXBORDERCOLOR_DESC", "IDS_STRING1738");
        addToMap("PSK_BOXFILLCOLOR", "IDS_STRING1739");
        addToMap("PSK_BOXFILLCOLOR_DESC", "IDS_STRING1740");
        addToMap("PSK_CLASSNODEDRAWENGINE", "IDS_STRING1741");
        addToMap("PSK_CLASSNODEDRAWENGINE_DESC", "IDS_STRING1742");
        addToMap("PSK_CLASSROBUSTNESSDRAWENGINE", "IDS_STRING1743");
        addToMap("PSK_CLASSROBUSTNESSDRAWENGINE_DESC", "IDS_STRING1744");
        addToMap("PSK_COLLABORATIONDRAWENGINE", "IDS_STRING1745");
        addToMap("PSK_COLLABORATIONDRAWENGINE_DESC", "IDS_STRING1746");
        addToMap("PSK_COLLABORATIONLIFELINEDRAWENGINE", "IDS_STRING1747");
        addToMap("PSK_COLLABORATIONLIFELINEDRAWENGINE_DESC", "IDS_STRING1748");
        addToMap("PSK_COMBINEDFRAGMENTDRAWENGINE", "IDS_STRING1749");
        addToMap("PSK_COMBINEDFRAGMENTDRAWENGINE_DESC", "IDS_STRING1750");
        addToMap("PSK_COMMENTDRAWENGINE", "IDS_STRING1751");
        addToMap("PSK_COMMENTDRAWENGINE_DESC", "IDS_STRING1752");
        addToMap("PSK_COMMENTEDGEDRAWENGINE", "IDS_STRING1753");
        addToMap("PSK_COMMENTEDGEDRAWENGINE_DESC", "IDS_STRING1754");
        addToMap("PSK_COMMENTFONT", "IDS_STRING1755");
        addToMap("PSK_COMMENTFONT_DESC", "IDS_STRING1756");
        addToMap("PSK_COMPONENTDRAWENGINE", "IDS_STRING1757");
        addToMap("PSK_COMPONENTDRAWENGINE_DESC", "IDS_STRING1758");
        addToMap("PSK_CONNECTOREDGEDRAWENGINE", "IDS_STRING1759");
        addToMap("PSK_CONNECTOREDGEDRAWENGINE_DESC", "IDS_STRING1760");
        addToMap("PSK_CONTROLNODEDRAWENGINE", "IDS_STRING1761");
        addToMap("PSK_CONTROLNODEDRAWENGINE_DESC", "IDS_STRING1762");
        addToMap("PSK_CORNERLABELCOLOR", "IDS_STRING1763");
        addToMap("PSK_CORNERLABELCOLOR_DESC", "IDS_STRING1764");
        addToMap("PSK_DATATYPEDRAWENGINE", "IDS_STRING1765");
        addToMap("PSK_DATATYPEDRAWENGINE_DESC", "IDS_STRING1766");
        addToMap("PSK_DEPENDENCYEDGEDRAWENGINE", "IDS_STRING1767");
        addToMap("PSK_DEPENDENCYEDGEDRAWENGINE_DESC", "IDS_STRING1768");
        addToMap("PSK_DEPLOYMENTSPECDRAWENGINE", "IDS_STRING1769");
        addToMap("PSK_DEPLOYMENTSPECDRAWENGINE_DESC", "IDS_STRING1770");
        addToMap("PSK_DERIVATIONCLASSIFIERDRAWENGINE", "IDS_STRING1771");
        addToMap("PSK_DERIVATIONCLASSIFIERDRAWENGINE_DESC", "IDS_STRING1772");
        addToMap("PSK_DERIVATIONEDGEDRAWENGINE", "IDS_STRING1773");
        addToMap("PSK_DERIVATIONEDGEDRAWENGINE_DESC", "IDS_STRING1774");
        addToMap("PSK_EDGECOLOR", "IDS_STRING1775");
        addToMap("PSK_EDGECOLOR_DESC", "IDS_STRING1776");
        addToMap("PSK_ENUMERATIONDRAWENGINE", "IDS_STRING1777");
        addToMap("PSK_ENUMERATIONDRAWENGINE_DESC", "IDS_STRING1778");
        addToMap("PSK_ERENTITYASSOCIATIONEDGEDRAWENGINE", "IDS_STRING1780");
        addToMap("PSK_ERENTITYASSOCIATIONEDGEDRAWENGINE_DESC", "IDS_STRING1781");
        addToMap("PSK_ERENTITYASSOCIATIONFONT", "IDS_STRING1782");
        addToMap("PSK_ERENTITYASSOCIATIONFONT_DESC", "IDS_STRING1893");
        addToMap("PSK_ERENTITYDRAWENGINE", "IDS_STRING1783");
        addToMap("PSK_ERENTITYDRAWENGINE_DESC", "IDS_STRING1784");
        addToMap("PSK_ERVIEWDRAWENGINE", "IDS_STRING1785");
        addToMap("PSK_ERVIEWDRAWENGINE_DESC", "IDS_STRING1786");
        addToMap("PSK_EXPRESSIONFONT", "IDS_STRING1787");
        addToMap("PSK_EXPRESSIONFONT_DESC", "IDS_STRING1788");
        addToMap("PSK_EXTENDEDGEDRAWENGINE", "IDS_STRING1789");
        addToMap("PSK_EXTENDEDGEDRAWENGINE_DESC", "IDS_STRING1790");
        addToMap("PSK_EXTENSIONPOINTFONT", "IDS_STRING1791");
        addToMap("PSK_EXTENSIONPOINTFONT_DESC", "IDS_STRING1792");
        addToMap("PSK_FINALSTATEDRAWENGINE", "IDS_STRING1795");
        addToMap("PSK_FINALSTATEDRAWENGINE_DESC", "IDS_STRING1796");
        addToMap("PSK_FLOWFINALBORDERCOLOR", "IDS_STRING1797");
        addToMap("PSK_FLOWFINALBORDERCOLOR_DESC", "IDS_STRING1798");
        addToMap("PSK_FLOWFINALFILLCOLOR", "IDS_STRING1799");
        addToMap("PSK_FLOWFINALFILLCOLOR_DESC", "IDS_STRING1800");
        addToMap("PSK_FORKBORDERCOLOR", "IDS_STRING1801");
        addToMap("PSK_FORKBORDERCOLOR_DESC", "IDS_STRING1802");
        addToMap("PSK_FORKFILLCOLOR", "IDS_STRING1803");
        addToMap("PSK_FORKFILLCOLOR_DESC", "IDS_STRING1804");
        addToMap("PSK_GENERALIZATIONEDGEDRAWENGINE", "IDS_STRING1805");
        addToMap("PSK_GENERALIZATIONEDGEDRAWENGINE_DESC", "IDS_STRING1806");
        addToMap("PSK_GRAPHICDRAWENGINE", "IDS_STRING1807");
        addToMap("PSK_GRAPHICDRAWENGINE_DESC", "IDS_STRING1808");
        addToMap("PSK_HEADCOLOR", "IDS_STRING1809");
        addToMap("PSK_HEADCOLOR_DESC", "IDS_STRING1810");
        addToMap("PSK_IMPLEMENTATIONEDGEDRAWENGINE", "IDS_STRING1811");
        addToMap("PSK_IMPLEMENTATIONEDGEDRAWENGINE_DESC", "IDS_STRING1812");
        addToMap("PSK_INCLUDEEDGEDRAWENGINE", "IDS_STRING1813");
        addToMap("PSK_INCLUDEEDGEDRAWENGINE_DESC", "IDS_STRING1814");
        addToMap("PSK_INITIALNODEBORDERCOLOR", "IDS_STRING1815");
        addToMap("PSK_INITIALNODEBORDERCOLOR_DESC", "IDS_STRING1816");
        addToMap("PSK_INITIALNODEFILLCOLOR", "IDS_STRING1817");
        addToMap("PSK_INITIALNODEFILLCOLOR_DESC", "IDS_STRING1818");
        addToMap("PSK_INTERACTIONFRAGMENTDRAWENGINE", "IDS_STRING1819");
        addToMap("PSK_INTERACTIONFRAGMENTDRAWENGINE_DESC", "IDS_STRING1820");
        addToMap("PSK_INTERACTIONFRAGMENTFONT", "IDS_STRING1821");
        addToMap("PSK_INTERACTIONFRAGMENTFONT_DESC", "IDS_STRING1822");
        addToMap("PSK_INTERFACEDRAWENGINE", "IDS_STRING1823");
        addToMap("PSK_INTERFACEDRAWENGINE_DESC", "IDS_STRING1824");
        addToMap("PSK_INVOCATIONNODEDRAWENGINE", "IDS_STRING1825");
        addToMap("PSK_INVOCATIONNODEDRAWENGINE_DESC", "IDS_STRING1826");
        addToMap("PSK_LABELFONT", "IDS_STRING1827");
        addToMap("PSK_LABELFONT_DESC", "IDS_STRING1828");
        addToMap("PSK_LIFELINEDRAWENGINE", "IDS_STRING1829");
        addToMap("PSK_LIFELINEDRAWENGINE_DESC", "IDS_STRING1830");
        addToMap("PSK_MERGEBORDERCOLOR", "IDS_STRING1831");
        addToMap("PSK_MERGEBORDERCOLOR_DESC", "IDS_STRING1832");
        addToMap("PSK_MERGEFILLCOLOR", "IDS_STRING1833");
        addToMap("PSK_MERGEFILLCOLOR_DESC", "IDS_STRING1834");
        addToMap("PSK_MESSAGEEDGEDRAWENGINE", "IDS_STRING1835");
        addToMap("PSK_MESSAGEEDGEDRAWENGINE_DESC", "IDS_STRING1836");
        addToMap("PSK_MSGCONNECTORARROWCOLOR", "IDS_STRING1837");
        addToMap("PSK_MSGCONNECTORARROWCOLOR_DESC", "IDS_STRING1838");
        addToMap("PSK_NAMETAGFONT", "IDS_STRING1841");
        addToMap("PSK_NAMETAGFONT_DESC", "IDS_STRING1842");
        addToMap("PSK_NAMETAGCOLOR", "IDS_STRING2841");
        addToMap("PSK_NAMETAGCOLOR_DESC", "IDS_STRING2842");
        addToMap("PSK_NESTEDLINKDRAWENGINE", "IDS_STRING1843");
        addToMap("PSK_NESTEDLINKDRAWENGINE_DESC", "IDS_STRING1844");
        addToMap("PSK_OBJECTNODEDRAWENGINE", "IDS_STRING1845");
        addToMap("PSK_OBJECTNODEDRAWENGINE_DESC", "IDS_STRING1846");
        addToMap("PSK_PACKAGEDRAWENGINE", "IDS_STRING1847");
        addToMap("PSK_PACKAGEDRAWENGINE_DESC", "IDS_STRING1848");
        addToMap("PSK_PARTFACADEEDGEDRAWENGINE", "IDS_STRING1849");
        addToMap("PSK_PARTFACADEEDGEDRAWENGINE_DESC", "IDS_STRING1850");
        addToMap("PSK_PARTITIONDRAWENGINE", "IDS_STRING1851");
        addToMap("PSK_PARTITIONDRAWENGINE_DESC", "IDS_STRING1852");
        addToMap("PSK_PKGBODYFONT", "IDS_STRING1853");
        addToMap("PSK_PKGBODYFONT_DESC", "IDS_STRING1854");
        addToMap("PSK_PKGTABFONT", "IDS_STRING1857");
        addToMap("PSK_PKGTABFONT_DESC", "IDS_STRING1858");
        addToMap("PSK_PORTDRAWENGINE", "IDS_STRING1859");
        addToMap("PSK_PORTDRAWENGINE_DESC", "IDS_STRING1860");
        addToMap("PSK_PORTPROVIDEDINTERFACEEDGEDRAWENGINE", "IDS_STRING1861");
        addToMap("PSK_PORTPROVIDEDINTERFACEEDGEDRAWENGINE_DESC", "IDS_STRING1862");
        addToMap("PSK_PSEUDOSTATEDRAWENGINE", "IDS_STRING1863");
        addToMap("PSK_PSEUDOSTATEDRAWENGINE_DESC", "IDS_STRING1864");
        addToMap("PSK_QUALIFIERDRAWENGINE", "IDS_STRING1865");
        addToMap("PSK_QUALIFIERDRAWENGINE_DESC", "IDS_STRING1866");
        addToMap("PSK_STATEDRAWENGINE", "IDS_STRING1867");
        addToMap("PSK_STATEDRAWENGINE_DESC", "IDS_STRING1868");
        addToMap("PSK_STATEMACHINEDRAWENGINE", "IDS_STRING1869");
        addToMap("PSK_STATEMACHINEDRAWENGINE_DESC", "IDS_STRING1870");
        addToMap("PSK_STICKFIGUREBORDERCOLOR", "IDS_STRING1875");
        addToMap("PSK_STICKFIGUREBORDERCOLOR_DESC", "IDS_STRING1876");
        addToMap("PSK_STICKFIGURECOLOR", "IDS_STRING1877");
        addToMap("PSK_STICKFIGURECOLOR_DESC", "IDS_STRING1878");
        addToMap("PSK_STICKFIGUREFILLCOLOR", "IDS_STRING1879");
        addToMap("PSK_STICKFIGUREFILLCOLOR_DESC", "IDS_STRING1880");
        addToMap("PSK_TITLEBLOCKDRAWENGINE", "IDS_STRING1887");
        addToMap("PSK_TITLEBLOCKDRAWENGINE_DESC", "IDS_STRING1888");
        addToMap("PSK_TRANSITIONEDGEDRAWENGINE", "IDS_STRING1889");
        addToMap("PSK_TRANSITIONEDGEDRAWENGINE_DESC", "IDS_STRING1890");
        addToMap("PSK_USECASEDRAWENGINE", "IDS_STRING1891");
        addToMap("PSK_USECASEDRAWENGINE_DESC", "IDS_STRING1892");
        addToMap("PSK_COMBINEDFRAGDIVIDERCOLOR", "IDS_STRING251");
        addToMap("PSK_COMBINEDFRAGDIVIDERCOLOR_DESC", "IDS_STRING257");
        addToMap("PSK_CORNERLABELFONT", "IDS_STRING263");
        addToMap("PSK_CORNERLABELFONT_DESC", "IDS_STRING536");
        addToMap("PSK_LABELICONBORDERCOLOR", "IDS_STRING740");
        addToMap("PSK_LABELICONBORDERCOLOR_DESC", "IDS_STRING740");
        
        // The Pseudostates
        addToMap("PSK_PSEUDOSTATECHOICEFILLCOLOR", "IDS_STRING1898");
        addToMap("PSK_PSEUDOSTATECHOICEFILLCOLOR_DESC", "IDS_STRING1899");
        addToMap("PSK_PSEUDOSTATECHOICEBORDERCOLOR", "IDS_STRING1900");
        addToMap("PSK_PSEUDOSTATECHOICEBORDERCOLOR_DESC", "IDS_STRING1901");
        addToMap("PSK_PSEUDOSTATEDEEPHISTORYFONT", "IDS_STRING738");
        addToMap("PSK_PSEUDOSTATEDEEPHISTORYFONT_DESC", "IDS_STRING738");
        addToMap("PSK_PSEUDOSTATEDEEPHISTORYFILLCOLOR", "IDS_STRING1902");
        addToMap("PSK_PSEUDOSTATEDEEPHISTORYFILLCOLOR_DESC", "IDS_STRING1903");
        addToMap("PSK_PSEUDOSTATEDEEPHISTORYBORDERCOLOR", "IDS_STRING1904");
        addToMap("PSK_PSEUDOSTATEDEEPHISTORYBORDERCOLOR_DESC", "IDS_STRING1905");
        addToMap("PSK_PSEUDOSTATESHALLOWHISTORYFONT", "IDS_STRING739");
        addToMap("PSK_PSEUDOSTATESHALLOWHISTORYFONT_DESC", "IDS_STRING739");
        addToMap("PSK_PSEUDOSTATESHALLOWHISTORYFILLCOLOR", "IDS_STRING1906");
        addToMap("PSK_PSEUDOSTATESHALLOWHISTORYFILLCOLOR_DESC", "IDS_STRING1907");
        addToMap("PSK_PSEUDOSTATESHALLOWHISTORYBORDERCOLOR", "IDS_STRING1908");
        addToMap("PSK_PSEUDOSTATESHALLOWHISTORYBORDERCOLOR_DESC", "IDS_STRING1909");
        addToMap("PSK_PSEUDOSTATEINITIALSTATEFILLCOLOR", "IDS_STRING1910");
        addToMap("PSK_PSEUDOSTATEINITIALSTATEFILLCOLOR_DESC", "IDS_STRING1911");
        addToMap("PSK_PSEUDOSTATEINITIALSTATEBORDERCOLOR", "IDS_STRING1912");
        addToMap("PSK_PSEUDOSTATEINITIALSTATEBORDERCOLOR_DESC", "IDS_STRING1913");
        addToMap("PSK_PSEUDOSTATEJOINSTATEFILLCOLOR", "IDS_STRING1914");
        addToMap("PSK_PSEUDOSTATEJOINSTATEFILLCOLOR_DESC", "IDS_STRING1915");
        addToMap("PSK_PSEUDOSTATEJOINSTATEBORDERCOLOR", "IDS_STRING1916");
        addToMap("PSK_PSEUDOSTATEJOINSTATEBORDERCOLOR_DESC", "IDS_STRING1917");
        addToMap("PSK_PSEUDOSTATEJUNCTIONSTATEFILLCOLOR", "IDS_STRING1918");
        addToMap("PSK_PSEUDOSTATEJUNCTIONSTATEFILLCOLOR_DESC", "IDS_STRING1919");
        addToMap("PSK_PSEUDOSTATEJUNCTIONSTATEBORDERCOLOR", "IDS_STRING1920");
        addToMap("PSK_PSEUDOSTATEJUNCTIONSTATEBORDERCOLOR_DESC", "IDS_STRING1921");
        addToMap("PSK_PSEUDOSTATEENTRYPOINTFILLCOLOR", "IDS_STRING1922");
        addToMap("PSK_PSEUDOSTATEENTRYPOINTFILLCOLOR_DESC", "IDS_STRING1923");
        addToMap("PSK_PSEUDOSTATEENTRYPOINTBORDERCOLOR", "IDS_STRING1924");
        addToMap("PSK_PSEUDOSTATEENTRYPOINTBORDERCOLOR_DESC", "IDS_STRING1925");
        addToMap("PSK_PSEUDOSTATESTOPSTATEFILLCOLOR", "IDS_STRING1926");
        addToMap("PSK_PSEUDOSTATESTOPSTATEFILLCOLOR_DESC", "IDS_STRING1927");
        addToMap("PSK_PSEUDOSTATESTOPSTATEBORDERCOLOR", "IDS_STRING1928");
        addToMap("PSK_PSEUDOSTATESTOPSTATEBORDERCOLOR_DESC", "IDS_STRING1929");
        
        // Package
        addToMap("PSK_PACKAGECLR", "IDS_STRING1101");
        addToMap("PSK_PACKAGECLR_HLP", "IDS_STRING1102");
        addToMap("PSK_PKGFILLCOLOR", "IDS_STRING287");
        addToMap("PSK_PKGFILLCOLOR_HLP", "IDS_STRING288");
        addToMap("PSK_PKGFONT", "IDS_STRING261");
        addToMap("PSK_PKGFONT_HLP", "IDS_STRING262");
        addToMap("PSK_PKGINTABFONT", "IDS_STRING532");
        addToMap("PSK_PKGINTABFONT_HLP", "IDS_STRING533");
        addToMap("PSK_PKGBORDERCOLOR", "IDS_STRING1326");
        addToMap("PSK_PKGBORDERCOLOR_HLP", "IDS_STRING1327");
        addToMap("PSK_PKGTEXTCOLOR", "IDS_STRING1328");
        addToMap("PSK_PKGTEXTCOLOR_HLP", "IDS_STRING1329");
        
        // ComponentAssembly
        addToMap("PSK_COMPONENTASSEMBLYCLR", "IDS_STRING1391");
        addToMap("PSK_COMPONENTASSEMBLYCLR_HLP", "IDS_STRING1392");
        addToMap("PSK_COMPONENTASSEMBLYFILLCOLOR", "IDS_STRING1393");
        addToMap("PSK_COMPONENTASSEMBLYFILLCOLOR_HLP", "IDS_STRING1394");
        addToMap("PSK_COMPONENTASSEMBLYFONT", "IDS_STRING1395");
        addToMap("PSK_COMPONENTASSEMBLYFONT_HLP", "IDS_STRING1396");
        addToMap("PSK_COMPONENTASSEMBLYINTABFONT", "IDS_STRING1397");
        addToMap("PSK_COMPONENTASSEMBLYINTABFONT_HLP", "IDS_STRING1398");
        addToMap("PSK_COMPONENTASSEMBLYBORDERCOLOR", "IDS_STRING1399");
        addToMap("PSK_COMPONENTASSEMBLYBORDERCOLOR_HLP", "IDS_STRING1400");
        addToMap("PSK_COMPONENTASSEMBLYTEXTCOLOR", "IDS_STRING1401");
        addToMap("PSK_COMPONENTASSEMBLYTEXTCOLOR_HLP", "IDS_STRING1402");
        
        // Graphic
        addToMap("PSK_GRAPHICCLR", "IDS_STRING1165");
        addToMap("PSK_GRAPHICCLR_HLP", "IDS_STRING1166");
        addToMap("PSK_GRAPHICTEXTCOLOR", "IDS_STRING1042");
        addToMap("PSK_GRAPHICTEXTCOLOR_HLP", "IDS_STRING1043");
        addToMap("PSK_GRAPHICFONT", "IDS_STRING842");
        addToMap("PSK_GRAPHICFONT_HLP", "IDS_STRING843");
        addToMap("PSK_GRAPHICFILLCOLOR", "IDS_STRING844");
        addToMap("PSK_GRAPHICFILLCOLOR_HLP", "IDS_STRING845");
        addToMap("PSK_GRAPHICBORDERCOLOR", "IDS_STRING846");
        addToMap("PSK_GRAPHICBORDERCOLOR_HLP", "IDS_STRING847");
        
        // SubPartition
        addToMap("PSK_SUBPARTITIONFONT", "IDS_STRING874");
        addToMap("PSK_SUBPARTITIONFONT_HLP", "IDS_STRING875");
        
        // Qualifier
        
        addToMap("PSK_QUALIFIERCLR", "IDS_STRING1080");
        addToMap("PSK_QUALIFIERCLR_HLP", "IDS_STRING1323");
        addToMap("PSK_QUALIFIERFONT", "IDS_STRING1044");
        addToMap("PSK_QUALIFIERFONT_HLP", "IDS_STRING1045");
        addToMap("PSK_QUALIFIERFILLCOLOR", "IDS_STRING1046");
        addToMap("PSK_QUALIFIERFILLCOLOR_HLP", "IDS_STRING1047");
        addToMap("PSK_QUALIFIERBORDERCOLOR", "IDS_STRING1048");
        addToMap("PSK_QUALIFIERBORDERCOLOR_HLP", "IDS_STRING1049");
        addToMap("PSK_QUALIFIERTEXTCOLOR", "IDS_STRING1324");
        addToMap("PSK_QUALIFIERTEXTCOLOR_HLP", "IDS_STRING1325");
        
        // Class
        addToMap("PSK_CLASSCLR", "IDS_STRING1097");
        addToMap("PSK_CLASSCLR_HLP", "IDS_STRING1098");
        addToMap("PSK_CLASSFILLCOLOR", "IDS_STRING273");
        addToMap("PSK_CLASSFILLCOLOR_HLP", "IDS_STRING274");
        addToMap("PSK_CLASSBORDERCOLOR", "IDS_STRING275");
        addToMap("PSK_CLASSBORDERCOLOR_HLP", "IDS_STRING276");
        addToMap("PSK_DEFAULTELENAMEFONT", "IDS_STRING245");
        addToMap("PSK_CLASSFONT", "IDS_STRING245");
        addToMap("PSK_CLASSFONT_HLP", "IDS_STRING246");
        
        // Comment
        addToMap("PSK_COMMENTCLR", "IDS_STRING1099");
        addToMap("PSK_COMMENTCLR_HLP", "IDS_STRING1100");
        addToMap("PSK_COMMENTBORDERCOLOR", "IDS_STRING277");
        addToMap("PSK_COMMENTBORDERCOLOR_HLP", "IDS_STRING278");
        addToMap("PSK_COMMENTCOLOR", "IDS_STRING295");
        addToMap("PSK_COMMENTCOLOR_HLP", "IDS_STRING296");
        addToMap("PSK_COMMENTFONT_HLP", "IDS_STRING258");
        addToMap("PSK_COMMENTTEXTCOLOR", "IDS_STRING1494");
        addToMap("PSK_COMMENTTEXTCOLOR_HLP", "IDS_STRING1495");
        
        // Actor
        addToMap("PSK_ACTORCLR", "IDS_STRING1103");
        addToMap("PSK_ACTORCLR_HLP", "IDS_STRING1104");
        addToMap("PSK_STICKCOLOR", "IDS_STRING297");
        addToMap("PSK_STICKCOLOR_HLP", "IDS_STRING298");
        addToMap("PSK_STICKBORDERCOLOR", "IDS_STRING1293");
        addToMap("PSK_STICKBORDERCOLOR_HLP", "IDS_STRING1294");
        addToMap("PSK_STICKTEXTCOLOR", "IDS_STRING1295");
        addToMap("PSK_STICKTEXTCOLOR_HLP", "IDS_STRING1296");
        addToMap("PSK_STICKFONT", "IDS_STRING1297");
        addToMap("PSK_STICKFONT_HLP", "IDS_STRING1298");
        
        // Compartments - list class name, comment name
        addToMap("PSK_LISTNAMEFONT", "IDS_STRING249");
        addToMap("PSK_LISTNAMEFONT_HLP", "IDS_STRING250");
        addToMap("PSK_LISTTITLEFONT_HLP", "IDS_STRING252");
        addToMap("PSK_ELENAMEFONT", "IDS_STRING253");
        addToMap("PSK_ELENAMEFONT_HLP", "IDS_STRING254");
        addToMap("PSK_STEREOTYPEFONT", "IDS_STRING255");
        addToMap("PSK_STEREOTYPEFONT_HLP", "IDS_STRING256");
        
        // Robustness
        addToMap("PSK_ROBUSTNESSCLR", "IDS_STRING1211 ");
        addToMap("PSK_ROBUSTNESSCLR_HLP", "IDS_STRING1212 ");
        addToMap("PSK_ROBUSTNESSFONT", "IDS_STRING968 ");
        addToMap("PSK_ROBUSTNESSFONT_HLP", "IDS_STRING969 ");
        addToMap("PSK_ROBUSTNESSFILLCOLOR", "IDS_STRING970 ");
        addToMap("PSK_ROBUSTNESSFILLCOLOR_HLP", "IDS_STRING971 ");
        addToMap("PSK_ROBUSTNESSBORDERCOLOR", "IDS_STRING972 ");
        addToMap("PSK_ROBUSTNESSBORDERCOLOR_HLP", "IDS_STRING973 ");
        addToMap("PSK_ROBUSTNESSTEXTCOLOR", "IDS_STRING1213 ");
        addToMap("PSK_ROBUSTNESSTEXTCOLOR_HLP", "IDS_STRING1214 ");
        
        // State
        addToMap("PSK_STATECLR", "IDS_STRING1173 ");
        addToMap("PSK_STATECLR_HLP", "IDS_STRING1174 ");
        addToMap("PSK_STATETEXTCOLOR", "IDS_STRING922 ");
        addToMap("PSK_STATETEXTCOLOR_HLP", "IDS_STRING923 ");
        addToMap("PSK_STATEFILLCOLOR", "IDS_STRING924 ");
        addToMap("PSK_STATEFILLCOLOR_HLP", "IDS_STRING925 ");
        addToMap("PSK_STATEBORDERCOLOR", "IDS_STRING926 ");
        addToMap("PSK_STATEBORDERCOLOR_HLP", "IDS_STRING927 ");
        addToMap("PSK_STATEFONT", "IDS_STRING776");
        addToMap("PSK_STATEFONT_HLP", "IDS_STRING777");
        
        // State Machine
        addToMap("PSK_STATEMACHINECLR", "IDS_STRING1159 ");
        addToMap("PSK_STATEMACHINECLR_HLP", "IDS_STRING1160 ");
        addToMap("PSK_STATEMACHINETEXTCOLOR", "IDS_STRING928 ");
        addToMap("PSK_STATEMACHINETEXTCOLOR_HLP", "IDS_STRING929 ");
        addToMap("PSK_STATEMACHINEBORDERCOLOR", "IDS_STRING1157 ");
        addToMap("PSK_STATEMACHINEBORDERCOLOR_HLP", "IDS_STRING1158 ");
        addToMap("PSK_STATEMACHINEFONT", "IDS_STRING815");
        addToMap("PSK_STATEMACHINEFONT_HLP", "IDS_STRING816");
        addToMap("PSK_STATEMACHINEFILLCOLOR", "IDS_STRING1155");
        addToMap("PSK_STATEMACHINEFILLCOLOR_HLP", "IDS_STRING1156");
        
        // Decisions
        addToMap("PSK_DECISIONMERGENODECLR", "IDS_STRING1133");
        addToMap("PSK_DECISIONMERGENODECLR_HLP", "IDS_STRING1134");
        addToMap("PSK_DECISIONMERGENODEBORDERCOLOR", "IDS_STRING1135 ");
        addToMap("PSK_DECISIONMERGENODEBORDERCOLOR_HLP", "IDS_STRING1136 ");
        addToMap("PSK_DECISIONMERGENODEFILLCOLOR", "IDS_STRING1137 ");
        addToMap("PSK_DECISIONMERGENODEFILLCOLOR_HLP", "IDS_STRING1138 ");
        addToMap("PSK_DECISIONMERGENODETEXTCOLOR", "IDS_STRING1139 ");
        addToMap("PSK_DECISIONMERGENODETEXTCOLOR_HLP", "IDS_STRING1140 ");
        
        // Initial Node
        addToMap("PSK_INITIALNODECLR", "IDS_STRING1113");
        addToMap("PSK_INITIALNODECLR_HLP", "IDS_STRING1114");
        addToMap("PSK_INITIALNODEBORDERCOLOR_HLP", "IDS_STRING939 ");
        addToMap("PSK_INITIALNODEFILLCOLOR_HLP", "IDS_STRING941 ");
        addToMap("PSK_INITIALNODETEXTCOLOR", "IDS_STRING1115 ");
        addToMap("PSK_INITIALNODETEXTCOLOR_HLP", "IDS_STRING1116 ");
        
        // Final Node
        addToMap("PSK_FINALNODECLR", "IDS_STRING1117");
        addToMap("PSK_FINALNODECLR_HLP", "IDS_STRING1118");
        addToMap("PSK_ACTIVITYFINALNODEBORDERCOLOR", "IDS_STRING1119 ");
        addToMap("PSK_ACTIVITYFINALNODEBORDERCOLOR_HLP", "IDS_STRING1120 ");
        addToMap("PSK_ACTIVITYFINALNODEFILLCOLOR", "IDS_STRING1121 ");
        addToMap("PSK_ACTIVITYFINALNODEFILLCOLOR_HLP", "IDS_STRING1122 ");
        addToMap("PSK_ACTIVITYFINALNODETEXTCOLOR", "IDS_STRING1123 ");
        addToMap("PSK_ACTIVITYFINALNODETEXTCOLOR_HLP", "IDS_STRING1124 ");
        
        // Flow Final Node
        addToMap("PSK_FLOWFINALNODECLR", "IDS_STRING1125");
        addToMap("PSK_FLOWFINALNODECLR_HLP", "IDS_STRING1126");
        addToMap("PSK_FLOWFINALNODEBORDERCOLOR", "IDS_STRING1127 ");
        addToMap("PSK_FLOWFINALNODEBORDERCOLOR_HLP", "IDS_STRING1128 ");
        addToMap("PSK_FLOWFINALNODEFILLCOLOR", "IDS_STRING1129 ");
        addToMap("PSK_FLOWFINALNODEFILLCOLOR_HLP", "IDS_STRING1130 ");
        addToMap("PSK_FLOWFINALNODETEXTCOLOR", "IDS_STRING1131 ");
        addToMap("PSK_FLOWFINALNODETEXTCOLOR_HLP", "IDS_STRING1132 ");
        
        // Final state
        addToMap("PSK_FINALSTATECLR", "IDS_STRING1207 ");
        addToMap("PSK_FINALSTATECLR_HLP", "IDS_STRING1208 ");
        addToMap("PSK_FINALSTATEFILLCOLOR", "IDS_STRING963 ");
        addToMap("PSK_FINALSTATEFILLCOLOR_HLP", "IDS_STRING964 ");
        addToMap("PSK_FINALSTATEBORDERCOLOR", "IDS_STRING965 ");
        addToMap("PSK_FINALSTATEBORDERCOLOR_HLP", "IDS_STRING966 ");
        addToMap("PSK_FINALSTATETEXTCOLOR", "IDS_STRING1209 ");
        addToMap("PSK_FINALSTATETEXTCOLOR_HLP", "IDS_STRING1210 ");
        addToMap("PSK_FINALSTATEFONT", "IDS_STRING1313 ");
        addToMap("PSK_FINALSTATEFONT_HLP", "IDS_STRING1314 ");
        
        // Lifeline
        addToMap("PSK_LIFELINECLR", "IDS_STRING1171");
        addToMap("PSK_LIFELINECLR_HLP", "IDS_STRING1172");
        addToMap("PSK_LIFELINEFONT", "IDS_STRING908");
        addToMap("PSK_LIFELINEFONT_HLP", "IDS_STRING909");
        addToMap("PSK_LIFELINETEXTCOLOR", "IDS_STRING914");
        addToMap("PSK_LIFELINETEXTCOLOR_HLP", "IDS_STRING915");
        addToMap("PSK_LIFELINEFILLCOLOR", "IDS_STRING910");
        addToMap("PSK_LIFELINEFILLCOLOR_HLP", "IDS_STRING911");
        addToMap("PSK_LIFELINEBORDERCOLOR", "IDS_STRING912");
        addToMap("PSK_LIFELINEBORDERCOLOR_HLP", "IDS_STRING913");
        
        // Use Case
        addToMap("PSK_USECASECLR", "IDS_STRING1151");
        addToMap("PSK_USECASECLR_HLP", "IDS_STRING1152");
        addToMap("PSK_USECASEFONT", "IDS_STRING891");
        addToMap("PSK_USECASEFONT_HLP", "IDS_STRING892");
        addToMap("PSK_USECASEFILLCOLOR", "IDS_STRING763");
        addToMap("PSK_USECASEFILLCOLOR_HLP", "IDS_STRING764");
        addToMap("PSK_USECASEBORDERCOLOR", "IDS_STRING1149");
        addToMap("PSK_USECASEBORDERCOLOR_HLP", "IDS_STRING1150");
        addToMap("PSK_USECASETEXTCOLOR", "IDS_STRING1332");
        addToMap("PSK_USECASETEXTCOLOR_HLP", "IDS_STRING1333");
        
        // Port
        addToMap("PSK_PORTCLR", "IDS_STRING1169");
        addToMap("PSK_PORTCLR_HLP", "IDS_STRING1170");
        addToMap("PSK_PORTFONT", "IDS_STRING901");
        addToMap("PSK_PORTFONT_HLP", "IDS_STRING902");
        addToMap("PSK_PORTFILLCOLOR", "IDS_STRING903");
        addToMap("PSK_PORTFILLCOLOR_HLP", "IDS_STRING904");
        addToMap("PSK_PORTBORDERCOLOR", "IDS_STRING905");
        addToMap("PSK_PORTBORDERCOLOR_HLP", "IDS_STRING906");
        
        // State events compartment
        addToMap("PSK_STATEEVENTSFONT", "IDS_STRING886");
        addToMap("PSK_STATEEVENTSFONT_HLP", "IDS_STRING887");
        
        // Derivation Classifier
        addToMap("PSK_DERIVATIONCLASSIFIERCLR", "IDS_STRING1167");
        addToMap("PSK_DERIVATIONCLASSIFIERCLR_HLP", "IDS_STRING1168");
        addToMap("PSK_DERIVATIONCLASSIFIERFONT", "IDS_STRING880");
        addToMap("PSK_DERIVATIONCLASSIFIERFONT_HLP", "IDS_STRING881");
        addToMap("PSK_DERIVATIONCLASSIFIERFILLCOLOR", "IDS_STRING882");
        addToMap("PSK_DERIVATIONCLASSIFIERFILLCOLOR_HLP", "IDS_STRING883");
        addToMap("PSK_DERIVATIONCLASSIFIERBORDERCOLOR", "IDS_STRING884");
        addToMap("PSK_DERIVATIONCLASSIFIERBORDERCOLOR_HLP", "IDS_STRING885");
        
        // expression compartment
        addToMap("PSK_EXPRESSIONFONT_HLP", "IDS_STRING828");
        
        // Collaboration Lifeline
        addToMap("PSK_COLLABORATIONLIFELINECLR", "IDS_STRING1161");
        addToMap("PSK_COLLABORATIONLIFELINECLR_HLP", "IDS_STRING1162");
        addToMap("PSK_COLLABORATIONLIFELINEFILLCOLOR", "IDS_STRING829");
        addToMap("PSK_COLLABORATIONLIFELINEFILLCOLOR_HLP", "IDS_STRING830");
        addToMap("PSK_COLLABORATIONLIFELINEBORDERCOLOR", "IDS_STRING831");
        addToMap("PSK_COLLABORATIONLIFELINEBORDERCOLOR_HLP", "IDS_STRING832");
        addToMap("PSK_COLLABORATIONLIFELINETEXTCOLOR", "IDS_STRING1241");
        addToMap("PSK_COLLABORATIONLIFELINETEXTCOLOR_HLP", "IDS_STRING1242");
        addToMap("PSK_COLLABORATIONLIFELINEFONT", "IDS_STRING1301");
        addToMap("PSK_COLLABORATIONLIFELINEFONT_HLP", "IDS_STRING1302");
        
        // Datatype
        addToMap("PSK_DATATYPECLR", "IDS_STRING1163");
        addToMap("PSK_DATATYPECLR_HLP", "IDS_STRING1164");
        addToMap("PSK_DATATYPEFONT", "IDS_STRING836");
        addToMap("PSK_DATATYPEFONT_HLP", "IDS_STRING837");
        addToMap("PSK_DATATYPEFILLCOLOR", "IDS_STRING838");
        addToMap("PSK_DATATYPEFILLCOLOR_HLP", "IDS_STRING839");
        addToMap("PSK_DATATYPEBORDERCOLOR", "IDS_STRING840");
        addToMap("PSK_DATATYPEBORDERCOLOR_HLP", "IDS_STRING841");
        addToMap("PSK_DATATYPETEXTCOLOR", "IDS_STRING1539");
        addToMap("PSK_DATATYPETEXTCOLOR_HLP", "IDS_STRING1540");
        
        // Object Node
        addToMap("PSK_OBJECTNODECLR", "IDS_STRING1105");
        addToMap("PSK_OBJECTNODECLR_HLP", "IDS_STRING1106");
        addToMap("PSK_OBJECTNODEFILLCOLOR", "IDS_STRING730");
        addToMap("PSK_OBJECTNODEFILLCOLOR_HLP", "IDS_STRING731");
        addToMap("PSK_OBJECTNODEBORDERCOLOR", "IDS_STRING732");
        addToMap("PSK_OBJECTNODEBORDERCOLOR_HLP", "IDS_STRING733");
        addToMap("PSK_OBJECTNODETEXTCOLOR", "IDS_STRING1354");
        addToMap("PSK_OBJECTNODETEXTCOLOR_HLP", "IDS_STRING1355");
        addToMap("PSK_OBJECTNODEFONT", "IDS_STRING1352");
        addToMap("PSK_OBJECTNODEFONT_HLP", "IDS_STRING1353");
        
        // Invocation Node
        addToMap("PSK_INVOCATIONNODECLR", "IDS_STRING1107");
        addToMap("PSK_INVOCATIONNODECLR_HLP", "IDS_STRING1108");
        addToMap("PSK_INVOCATIONNODEFILLCOLOR", "IDS_STRING734");
        addToMap("PSK_INVOCATIONNODEFILLCOLOR_HLP", "IDS_STRING735");
        addToMap("PSK_INVOCATIONNODEBORDERCOLOR", "IDS_STRING736");
        addToMap("PSK_INVOCATIONNODEBORDERCOLOR_HLP", "IDS_STRING737");
        addToMap("PSK_INVOCATIONNODETEXTCOLOR", "IDS_STRING1317");
        addToMap("PSK_INVOCATIONNODETEXTCOLOR_HLP", "IDS_STRING1318");
        addToMap("PSK_INVOCATIONNODEFONT", "IDS_STRING1319");
        addToMap("PSK_INVOCATIONNODEFONT_HLP", "IDS_STRING1320");
        
        // Control Node
        addToMap("PSK_CONTROLNODECLR", "IDS_STRING1109");
        addToMap("PSK_CONTROLNODECLR_HLP", "IDS_STRING1110");
        addToMap("PSK_CONTROLNODEFILLCOLOR", "IDS_STRING1357");
        addToMap("PSK_CONTROLNODEFILLCOLOR_HLP", "IDS_STRING1358");
        addToMap("PSK_CONTROLNODEBORDERCOLOR", "IDS_STRING1359");
        addToMap("PSK_CONTROLNODEBORDERCOLOR_HLP", "IDS_STRING1360");
        addToMap("PSK_CONTROLNODETEXTCOLOR", "IDS_STRING1363");
        addToMap("PSK_CONTROLNODETEXTCOLOR_HLP", "IDS_STRING1364");
        addToMap("PSK_CONTROLNODEFONT", "IDS_STRING1361");
        addToMap("PSK_CONTROLNODEFONT_HLP", "IDS_STRING1362");
        
        // Component
        addToMap("PSK_COMPONENTCLR", "IDS_STRING1153");
        addToMap("PSK_COMPONENTCLR_HLP", "IDS_STRING1154");
        addToMap("PSK_COMPONENTFILLCOLOR", "IDS_STRING769");
        addToMap("PSK_COMPONENTFILLCOLOR_HLP", "IDS_STRING770");
        addToMap("PSK_COMPONENTBORDERCOLOR", "IDS_STRING771");
        addToMap("PSK_COMPONENTBORDERCOLOR_HLP", "IDS_STRING772");
        addToMap("PSK_COMPONENTTEXTCOLOR", "IDS_STRING1305");
        addToMap("PSK_COMPONENTTEXTCOLOR_HLP", "IDS_STRING1306");
        addToMap("PSK_COMPONENTFONT", "IDS_STRING1307");
        addToMap("PSK_COMPONENTFONT_HLP", "IDS_STRING1308");
        
        // Package import compartment
        addToMap("PSK_PACKAGEIMPORTFONT", "IDS_STRING534 ");
        addToMap("PSK_PACKAGEIMPORTFONT_HLP", "IDS_STRING535 ");
        
        /// Tagged values compartment
        addToMap("PSK_TAGGEDVALUESFONT_HLP", "IDS_STRING537 ");
        
        // Artifact
        addToMap("PSK_ARTIFACTCLR", "IDS_STRING1143");
        addToMap("PSK_ARTIFACTCLR_HLP", "IDS_STRING1144");
        addToMap("PSK_ARTIFACTFONT", "IDS_STRING742");
        addToMap("PSK_ARTIFACTFONT_HLP", "IDS_STRING743");
        addToMap("PSK_ARTIFACTFILLCOLOR", "IDS_STRING751");
        addToMap("PSK_ARTIFACTFILLCOLOR_HLP", "IDS_STRING752");
        addToMap("PSK_ARTIFACTBORDERCOLOR", "IDS_STRING753");
        addToMap("PSK_ARTIFACTBORDERCOLOR_HLP", "IDS_STRING754");
        
        // Enumeration
        addToMap("PSK_ENUMCLR", "IDS_STRING1145");
        addToMap("PSK_ENUMCLR_HLP", "IDS_STRING1146");
        addToMap("PSK_ENUMFONT", "IDS_STRING765");
        addToMap("PSK_ENUMFONT_HLP", "IDS_STRING766");
        addToMap("PSK_ENUMFILLCOLOR", "IDS_STRING755");
        addToMap("PSK_ENUMFILLCOLOR_HLP", "IDS_STRING756");
        addToMap("PSK_ENUMBORDERCOLOR", "IDS_STRING757");
        addToMap("PSK_ENUMBORDERCOLOR_HLP", "IDS_STRING758");
        
        // Interface
        addToMap("PSK_INTERFACECLR", "IDS_STRING1147");
        addToMap("PSK_INTERFACECLR_HLP", "IDS_STRING1148");
        addToMap("PSK_INTERFACEFONT", "IDS_STRING745");
        addToMap("PSK_INTERFACEFONT_HLP", "IDS_STRING746");
        addToMap("PSK_INTERFACEFILLCOLOR", "IDS_STRING759");
        addToMap("PSK_INTERFACEFILLCOLOR_HLP", "IDS_STRING760");
        addToMap("PSK_INTERFACEBORDERCOLOR", "IDS_STRING761");
        addToMap("PSK_INTERFACEBORDERCOLOR_HLP", "IDS_STRING762");
        
        // Label
        addToMap("PSK_LABELCLR", "IDS_STRING1141");
        addToMap("PSK_LABELCLR_HLP", "IDS_STRING1142");
        addToMap("PSK_LABELFONT_HLP", "IDS_STRING264");
        addToMap("PSK_DEFAULTLABELFONT", "IDS_STRING263");
        addToMap("PSK_DEFAULTLABELFONT_HLP", "IDS_STRING264");
        addToMap("PSK_LABELFILLCOLOR", "IDS_STRING289");
        addToMap("PSK_LABELFILLCOLOR_HLP", "IDS_STRING290");
        addToMap("PSK_DEFAULTLABELFILLCOLOR", "IDS_STRING289");
        addToMap("PSK_DEFAULTLABELFILLCOLOR_HLP", "IDS_STRING290");
        addToMap("PSK_LABELBORDERCOLOR", "IDS_STRING293");
        addToMap("PSK_LABELBORDERCOLOR_HLP", "IDS_STRING294");
        addToMap("PSK_DEFAULTLABELBORDERCOLOR", "IDS_STRING293");
        addToMap("PSK_DEFAULTLABELBORDERCOLOR_HLP", "IDS_STRING294");
        addToMap("PSK_LABELTEXTCOLOR", "IDS_STRING1469");
        addToMap("PSK_LABELTEXTCOLOR_HLP", "IDS_STRING1470");
        addToMap("PSK_DEFAULTLABELTEXTCOLOR", "IDS_STRING1469");
        addToMap("PSK_DEFAULTLABELTEXTCOLOR_HLP", "IDS_STRING1470");
        
        // Generalization
        addToMap("PSK_GENERALIZATIONEDGECLR", "IDS_STRING1215");
        addToMap("PSK_GENERALIZATIONEDGECLR_HLP", "IDS_STRING1216");
        addToMap("PSK_GENERALIZATIONEDGEBORDERCOLOR", "IDS_STRING1054");
        addToMap("PSK_GENERALIZATIONEDGEBORDERCOLOR_HLP", "IDS_STRING1055");
        
        // Associations
        addToMap("PSK_ASSOCIATIONEDGECLR", "IDS_STRING1219");
        addToMap("PSK_ASSOCIATIONEDGECLR_HLP", "IDS_STRING1220");
        addToMap("PSK_ASSOCIATIONEDGEBORDERCOLOR", "IDS_STRING1056");
        addToMap("PSK_ASSOCIATIONEDGEBORDERCOLOR_HLP", "IDS_STRING1057");
        
        // Comment Links
        addToMap("PSK_COMMENTEDGECLR", "IDS_STRING1217");
        addToMap("PSK_COMMENTEDGECLR_HLP", "IDS_STRING1218");
        addToMap("PSK_COMMENTEDGEBORDERCOLOR", "IDS_STRING1058");
        addToMap("PSK_COMMENTEDGEBORDERCOLOR_HLP", "IDS_STRING1059");
        
        // Connectors
        addToMap("PSK_CONNECTOREDGECLR", "IDS_STRING1221");
        addToMap("PSK_CONNECTOREDGECLR_HLP", "IDS_STRING1222");
        addToMap("PSK_CONNECTOREDGEBORDERCOLOR", "IDS_STRING1060");
        addToMap("PSK_CONNECTOREDGEBORDERCOLOR_HLP", "IDS_STRING1061");
        
        // Dependencies
        addToMap("PSK_DEPENDENCYEDGECLR", "IDS_STRING1223");
        addToMap("PSK_DEPENDENCYEDGECLR_HLP", "IDS_STRING1224");
        addToMap("PSK_DEPENDENCYEDGEBORDERCOLOR", "IDS_STRING1062");
        addToMap("PSK_DEPENDENCYEDGEBORDERCOLOR_HLP", "IDS_STRING1063");
        
        // Derivations
        addToMap("PSK_DERIVATIONEDGECLR", "IDS_STRING1225");
        addToMap("PSK_DERIVATIONEDGECLR_HLP", "IDS_STRING1226");
        addToMap("PSK_DERIVATIONEDGEBORDERCOLOR", "IDS_STRING1064");
        addToMap("PSK_DERIVATIONEDGEBORDERCOLOR_HLP", "IDS_STRING1065");
        
        // Implementations
        addToMap("PSK_IMPLEMENTATIONEDGECLR", "IDS_STRING1227");
        addToMap("PSK_IMPLEMENTATIONEDGECLR_HLP", "IDS_STRING1228");
        addToMap("PSK_IMPLEMENTATIONEDGEBORDERCOLOR", "IDS_STRING1066");
        addToMap("PSK_IMPLEMENTATIONEDGEBORDERCOLOR_HLP", "IDS_STRING1067");
        
        // Nested Links
        addToMap("PSK_NESTEDLINKCLR", "IDS_STRING1229");
        addToMap("PSK_NESTEDLINKCLR_HLP", "IDS_STRING1230");
        addToMap("PSK_NESTEDLINKBORDERCOLOR", "IDS_STRING1068");
        addToMap("PSK_NESTEDLINKBORDERCOLOR_HLP", "IDS_STRING1069");
        
        // Activity Edges
        addToMap("PSK_ACTIVITYEDGECLR", "IDS_STRING1231");
        addToMap("PSK_ACTIVITYEDGECLR_HLP", "IDS_STRING1232");
        addToMap("PSK_ACTIVITYEDGEBORDERCOLOR", "IDS_STRING1070");
        addToMap("PSK_ACTIVITYEDGEBORDERCOLOR_HLP", "IDS_STRING1071");
        
        // Role Bindings
        addToMap("PSK_PARTFACADEEDGECLR", "IDS_STRING1233");
        addToMap("PSK_PARTFACADEEDGECLR_HLP", "IDS_STRING1234");
        addToMap("PSK_PARTFACADEEDGEBORDERCOLOR", "IDS_STRING1072");
        addToMap("PSK_PARTFACADEEDGEBORDERCOLOR_HLP", "IDS_STRING1073");
        
        // Port provided interface edge
        addToMap("PSK_PORTPROVIDEDINTERFACEEDGECLR", "IDS_STRING1235");
        addToMap("PSK_PORTPROVIDEDINTERFACEEDGECLR_HLP", "IDS_STRING1236");
        addToMap("PSK_PORTPROVIDEDINTERFACEEDGEBORDERCOLOR", "IDS_STRING1074");
        addToMap("PSK_PORTPROVIDEDINTERFACEEDGEBORDERCOLOR_HLP", "IDS_STRING1075");
        
        // Messages
        addToMap("PSK_MESSAGEEDGECLR", "IDS_STRING1237");
        addToMap("PSK_MESSAGEEDGECLR_HLP", "IDS_STRING1238");
        addToMap("PSK_MESSAGEEDGEBORDERCOLOR", "IDS_STRING1076");
        addToMap("PSK_MESSAGEEDGEBORDERCOLOR_HLP", "IDS_STRING1077");
        
        // Transitions
        addToMap("PSK_TRANSITIONEDGECLR", "IDS_STRING1239");
        addToMap("PSK_TRANSITIONEDGECLR_HLP", "IDS_STRING1238");
        addToMap("PSK_TRANSITIONEDGEBORDERCOLOR", "IDS_STRING1078");
        addToMap("PSK_TRANSITIONEDGEBORDERCOLOR_HLP", "IDS_STRING1079");
        
        // Collaborations
        addToMap("PSK_COLLABORATIONCLR", "IDS_STRING1243");
        addToMap("PSK_COLLABORATIONCLR_HLP", "IDS_STRING1244");
        addToMap("PSK_COLLABORATIONFILLCOLOR", "IDS_STRING1246");
        addToMap("PSK_COLLABORATIONFILLCOLOR_HLP", "IDS_STRING1245");
        addToMap("PSK_COLLABORATIONBORDERCOLOR", "IDS_STRING1247");
        addToMap("PSK_COLLABORATIONBORDERCOLOR_HLP", "IDS_STRING1248");
        addToMap("PSK_COLLABORATIONTEXTCOLOR", "IDS_STRING1249");
        addToMap("PSK_COLLABORATIONTEXTCOLOR_HLP", "IDS_STRING1250");
        addToMap("PSK_COLLABORATIONFONT", "IDS_STRING1303");
        addToMap("PSK_COLLABORATIONFONT_HLP", "IDS_STRING1304");
        
        // Activity Groups
        addToMap("PSK_ACTIVITYGROUPCLR", "IDS_STRING1251");
        addToMap("PSK_ACTIVITYGROUPCLR_HLP", "IDS_STRING1252");
        addToMap("PSK_ACTIVITYGROUPFILLCOLOR", "IDS_STRING1253");
        addToMap("PSK_ACTIVITYGROUPFILLCOLOR_HLP", "IDS_STRING1254");
        addToMap("PSK_ACTIVITYGROUPBORDERCOLOR", "IDS_STRING1255");
        addToMap("PSK_ACTIVITYGROUPBORDERCOLOR_HLP", "IDS_STRING1256");
        addToMap("PSK_ACTIVITYGROUPTEXTCOLOR", "IDS_STRING1257");
        addToMap("PSK_ACTIVITYGROUPTEXTCOLOR_HLP", "IDS_STRING1258");
        addToMap("PSK_ACTIVITYGROUPFONT", "IDS_STRING1291");
        addToMap("PSK_ACTIVITYGROUPFONT_HLP", "IDS_STRING1292");
        
        // Interaction Fragments
        addToMap("PSK_INTERACTIONFRAGMENTCLR", "IDS_STRING1535");
        addToMap("PSK_INTERACTIONFRAGMENTCLR_HLP", "IDS_STRING1536");
        addToMap("PSK_DEFAULTINTOPFONT", "IDS_STRING1541");
        addToMap("PSK_DEFAULTINTOPFONT_HLP", "IDS_STRING1542");
        
        // Combined Fragments
        addToMap("PSK_COMBINEDFRAGMENTCLR", "IDS_STRING1259");
        addToMap("PSK_COMBINEDFRAGMENTCLR_HLP", "IDS_STRING1260");
        addToMap("PSK_INTOPFONT", "IDS_STRING259");
        addToMap("PSK_INTOPFONT_HLP", "IDS_STRING260");
        addToMap("PSK_COMBINEDFRAGMENTFILLCOLOR", "IDS_STRING1477");
        addToMap("PSK_COMBINEDFRAGMENTFILLCOLOR_HLP", "IDS_STRING1478");
        addToMap("PSK_COMBINEDFRAGMENTBORDERCOLOR", "IDS_STRING1479");
        addToMap("PSK_COMBINEDFRAGMENTBORDERCOLOR_HLP", "IDS_STRING1480");
        addToMap("PSK_COMBINEDFRAGMENTTEXTCOLOR", "IDS_STRING1481");
        addToMap("PSK_COMBINEDFRAGMENTTEXTCOLOR_HLP", "IDS_STRING1482");
        addToMap("PSK_COMBINEDFRAGMENTEXTFONT", "IDS_STRING1483");
        addToMap("PSK_COMBINEDFRAGMENTEXTFONT_HLP", "IDS_STRING1484");
        
        // Template Parameteres
        addToMap("PSK_TEMPLATECLASSCLR", "IDS_STRING1261");
        addToMap("PSK_TEMPLATECLASSCLR_HLP", "IDS_STRING1262");
        addToMap("PSK_TEMPLATEFONT_HLP", "IDS_STRING818");
        
        // Sub partitions
        addToMap("PSK_SUBPARTITIONCLR", "IDS_STRING1263");
        addToMap("PSK_SUBPARTITIONCLR_HLP", "IDS_STRING1264");
        
        // DeploymentSpecifications
        addToMap("PSK_DEPLOYMENTSPECCLR", "IDS_STRING1459");
        addToMap("PSK_DEPLOYMENTSPECCLR_HLP", "IDS_STRING1460");
        addToMap("PSK_DEPLOYMENTSPECFILLCOLOR", "IDS_STRING1461");
        addToMap("PSK_DEPLOYMENTSPECFILLCOLOR_HLP", "IDS_STRING1462");
        addToMap("PSK_DEPLOYMENTSPECBORDERCOLOR", "IDS_STRING1463");
        addToMap("PSK_DEPLOYMENTSPECBORDERCOLOR_HLP", "IDS_STRING1464");
        addToMap("PSK_DEPLOYMENTSPECTEXTCOLOR", "IDS_STRING1465");
        addToMap("PSK_DEPLOYMENTSPECTEXTCOLOR_HLP", "IDS_STRING1466");
        addToMap("PSK_DEPLOYMENTSPECFONT", "IDS_STRING1467");
        addToMap("PSK_DEPLOYMENTSPECFONT_HLP", "IDS_STRING1468");
        
        // Nodes
        addToMap("PSK_CLASSNODECLR", "IDS_STRING1340");
        addToMap("PSK_CLASSNODECLR_HLP", "IDS_STRING1341");
        addToMap("PSK_CLASSNODEFILLCOLOR", "IDS_STRING1342");
        addToMap("PSK_CLASSNODEFILLCOLOR_HLP", "IDS_STRING1343");
        addToMap("PSK_CLASSNODEBORDERCOLOR", "IDS_STRING1344");
        addToMap("PSK_CLASSNODEBORDERCOLOR_HLP", "IDS_STRING1345");
        addToMap("PSK_CLASSNODETEXTCOLOR", "IDS_STRING1346");
        addToMap("PSK_CLASSNODETEXTCOLOR_HLP", "IDS_STRING1347");
        addToMap("PSK_CLASSNODEFONT", "IDS_STRING1348");
        addToMap("PSK_CLASSNODEFONT_HLP", "IDS_STRING1349");
        
        // Default Colors
        addToMap("PSK_DEFAULTTEXTCOLOR", "IDS_STRING805 ");
        addToMap("PSK_DEFAULTTEXTCOLOR_HLP", "IDS_STRING806 ");
        addToMap("PSK_DEFAULTBORDERCOLOR", "IDS_STRING807 ");
        addToMap("PSK_DEFAULTBORDERCOLOR_HLP", "IDS_STRING808 ");
        addToMap("PSK_DEFAULTTEXTFONT", "IDS_STRING809 ");
        addToMap("PSK_DEFAULTTEXTFONT_HLP", "IDS_STRING810 ");
        addToMap("PSK_DEFAULTFILLCOLOR", "IDS_STRING811 ");
        addToMap("PSK_DEFAULTFILLCOLOR_HLP", "IDS_STRING812 ");
        
        // Collaboration diagram preferences
        addToMap("PSK_COLLABORATIONDIAGRAM", "IDS_STRING1089");
        addToMap("PSK_COLLABORATIONDIAGRAM_HLP", "IDS_STRING1090");
        addToMap("PSK_DELETECONNECTORMESSAGES", "IDS_STRING1091");
        addToMap("PSK_DELETECONNECTORMESSAGES_HLP", "IDS_STRING1092");
        
        addToMap("PSK_SOURCEDIR", "IDS_STRING1440 ");
        
        addToMap("PSK_INTEGRATIONS",  "IDS_STRING242");
        addToMap("PSK_INTEGRATIONS_HLP" , "IDS_STRING241 ");
        addToMap("PSK_NEW_IDE_PROJECT", "IDS_STRING308 ");
        addToMap("PSK_NEW_IDE_PROJECT_HLP" , "IDS_STRING1532");
        addToMap("PSK_DEFAULT_WS_LOCATION" , "IDS_STRING1533 ");
        addToMap("PSK_DEFAULT_WS_LOCATION_HLP" , "IDS_STRING1534");
        addToMap("PSK_PROMPT_TARGET_WS" , "IDS_STRING1530 ");
        addToMap("PSK_PROMPT_TARGET_WS_HLP" , "IDS_STRING1531");
        
        // ERTitleBlock
        addToMap("PSK_ERTITLEBLOCKCLR", "IDS_STRING1583");
        addToMap("PSK_ERTITLEBLOCKCLR_HLP", "IDS_STRING1584");
        addToMap("PSK_ERTITLEBLOCKFONT", "IDS_STRING1585");
        addToMap("PSK_ERTITLEBLOCKFONT_HLP", "IDS_STRING1586");
        addToMap("PSK_ERTITLEBLOCKFILLCOLOR", "IDS_STRING1587");
        addToMap("PSK_ERTITLEBLOCKFILLCOLOR_HLP", "IDS_STRING1588");
        addToMap("PSK_ERTITLEBLOCKBORDERCOLOR", "IDS_STRING1589");
        addToMap("PSK_ERTITLEBLOCKBORDERCOLOR_HLP", "IDS_STRING1590");
        
        // EREntity
        addToMap("PSK_ERENTITYCLR", "IDS_STRING1591");
        addToMap("PSK_ERENTITYCLR_HLP", "IDS_STRING1592");
        addToMap("PSK_ERENTITYFILLCOLOR", "IDS_STRING1593");
        addToMap("PSK_ERENTITYFILLCOLOR_HLP", "IDS_STRING1594");
        addToMap("PSK_ERENTITYBORDERCOLOR", "IDS_STRING1595");
        addToMap("PSK_ERENTITYBORDERCOLOR_HLP", "IDS_STRING1596");
        addToMap("PSK_ERENTITYFONT", "IDS_STRING1598");
        addToMap("PSK_ERENTITYFONT_HLP", "IDS_STRING1599");
        
        // ERView
        addToMap("PSK_ERVIEWCLR", "IDS_STRING1600");
        addToMap("PSK_ERVIEWCLR_HLP", "IDS_STRING1601");
        addToMap("PSK_ERVIEWFILLCOLOR", "IDS_STRING1602");
        addToMap("PSK_ERVIEWFILLCOLOR_HLP", "IDS_STRING1603");
        addToMap("PSK_ERVIEWBORDERCOLOR", "IDS_STRING1604");
        addToMap("PSK_ERVIEWBORDERCOLOR_HLP", "IDS_STRING1605");
        addToMap("PSK_ERVIEWFONT", "IDS_STRING1607");
        addToMap("PSK_ERVIEWFONT_HLP", "IDS_STRING1608");
        
        // EREntityAssociation
        addToMap("PSK_ERENTITYASSOCIATIONEDGECLR", "IDS_STRING1609");
        addToMap("PSK_ERENTITYASSOCIATIONEDGECLR_HLP", "IDS_STRING1610");
        addToMap("PSK_ERENTITYASSOCIATIONEDGEBORDERCOLOR", "IDS_STRING1611");
        addToMap("PSK_ERENTITYASSOCIATIONEDGEBORDERCOLOR_HLP", "IDS_STRING1612");
        
        // TestHarness
        addToMap("PSK_TESTHARNESS", "IDS_STRING1894");
        addToMap("PSK_TESTHARNESS_HLP", "IDS_STRING1895");
        addToMap("PSK_TH_AUTOUPDATE", "IDS_STRING1896");
        addToMap("PSK_TH_AUTOUPDATE_HLP", "IDS_STRING1897");
    }
    
    public String lookUpInMap(String sEntry, boolean bOnlyForPSKs) 
    {
        String sTranslatedValue = sEntry;
        if (sEntry != null && sEntry.length() > 0) {
            String pskStr = "PSK_";
            int pos = sEntry.indexOf(pskStr);
            if(bOnlyForPSKs == false || pos != -1) {
                try {
                    String nValuestd = m_Map.get(sEntry);
                    if (nValuestd != null) {
                        // look up in the properties
                        sTranslatedValue = configStrings.getString(nValuestd);
                    }
                    else
                    {
                        // Check if the entry is the same name as the key.
                        sTranslatedValue = configStrings.getString(sEntry);
                    }
                } catch(Exception e) {
                    sTranslatedValue = "";
                }
            }
        }
        
        return sTranslatedValue;
    }
}



