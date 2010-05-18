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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.j2ee.ejbcore.patterns;

import java.io.IOException;
import org.netbeans.modules.j2ee.dd.api.ejb.CmpField;
import org.netbeans.modules.j2ee.dd.api.ejb.CmrField;
import org.netbeans.modules.j2ee.ejbcore.EjbGenerationUtil;
import org.openide.filesystems.FileObject;

/**
 * Class that generates the DTO source from Entity Bean
 * @author blaha
 */
public class DTOGenerator {
    private static final String DTO_SUFIX = "DTO"; //NOI18N
    private int indexField;
    private DTOHelper dtoHelp;
    
    /* Return the string with first capital character
     * @param str field name
     * @return method name with capital first character
     */
    //TODO good candidate to Util class
    private static String toFirstUpper(String str){
        StringBuffer buffer = new StringBuffer(str);
        buffer.setCharAt(0, Character.toUpperCase(buffer.charAt(0)));
        return buffer.toString();
    }
    
    /* Return DTO name from bean name
     * @param beanName bean name
     * @return bean name + sufix DTO
     */
    //TODO good candidate to Util class
    private static String getDTOName(String beanName){
//        return EjbGenerationUtil.getEjbNameBase(beanName) + DTO_SUFIX;
        //TODO: RETOUCHE
        return null;
    }
    
    /* Return DTO full name from bean name
     * @param beanName bean name
     * @return bean full name + sufix DTO
     */
    //TODO good candidate to Util class
    private static String getDTOFullName(String pkgName, String beanName){
        return pkgName + "." + getDTOName(beanName);
    }
    
    /* Generate DTO class
     * @param dtoHelper <code>DTOHelper</code> class that encapsulates bean
     * @param pkg package file object
     * @param generateCmrFields wheteher generate CMR fields, setter and getter methods for CMR
     */
    public void generateDTO(DTOHelper dtoHelp,
            FileObject pkg,
            boolean generateCmrFields)
            throws IOException {
        String pkgName;
        this.dtoHelp = dtoHelp;
        if(pkg != null) {
            pkgName = EjbGenerationUtil.getSelectedPackageName(pkg);
        } else {
            pkgName = dtoHelp.getPackage();
            pkg = EjbGenerationUtil.getPackageFileObject(dtoHelp.getSourceGroup(),pkgName, dtoHelp.getProject());
        }
        String dtoName = dtoHelp.getEntityName();
        //TODO: RETOUCHE
//        b = genUtil.getDefaultBean();
//        b.setClassname(true);
//        b.setClassnameName(getDTOName(dtoName));
//        b.setCommentDataEjbName(dtoHelp.getLocalName());
//        if(pkgName != null){
//            b.setClassnamePackage(pkgName);
//        }
//        b.setKey(true);  // this is needed for equals() and to hashCode() meth. generation
//        b.setKeyFullname(getDTOFullName(pkgName, dtoName));
//        // add CMP Fields
//        CmpField[] cmps = dtoHelp.getCmpFields();
//        addCmpFields(cmps);
//        // add CMR Fields
//        if(generateCmrFields) {
//            CmrField[] cmrs = dtoHelp.getCmrFields();
//            if(cmrs != null) addCmrFields(cmrs);
//        }
//        // generate DTO
//        genUtil.generateBeanClass(DTO_TEMPLATE, b, pkgName, pkg, true);
    }
    
    /* Add Cm field (CMR or CMP) to bean
     * @param index index of CM field in beans
     * @param fieldName field name
     * @param cmFieldClassname field class name, e.g. java.util.Collection, String, ...
     */
    private void addField(int index, String fieldName, String cmFieldClassName){
        //TODO: RETOUCHE
//        b.addCmField(true);
//        b.setCmFieldMethodName(index, toFirstUpper(fieldName));
//        b.setCmFieldClassname(index, cmFieldClassName);
//        b.addCmFieldName(fieldName);
    }
    
    /* Method adds CMP fields to the bean
     * @param CmpField[] array of CMP fields
     */
    private void addCmpFields(CmpField[] cmps){
        String fieldName;
        for(int i = 0; i < cmps.length; i++){
            if(cmps[i] != null ) {
                fieldName = cmps[i].getFieldName();
                addField(indexField, fieldName, dtoHelp.getFieldType(fieldName));
                //TODO: RETOUCHE
//                b.setCmFieldInKey(indexField, fieldName);
                indexField++;
            }
        }
    }
    
    /* Method adds CMR fields to the bean
     * @param CmrField[] array of CMR fields
     */
    private void addCmrFields(CmrField[] cmrs){
        String fieldName;
        String fieldClassName;
        String cmFieldCmrField;
        boolean isMultiplicity;
        int fieldIndex;
        
        for(int i = 0; i < cmrs.length; i++){
            if(cmrs[i] != null){
                fieldName = cmrs[i].getCmrFieldName();
                fieldIndex = indexField++;
                isMultiplicity = dtoHelp.isMultiple(cmrs[i]);
                
                if(isMultiplicity){
                    fieldClassName = dtoHelp.getFieldType(fieldName);
                    cmFieldCmrField = dtoHelp.getOppositeFieldType(cmrs[i]);
                } else {
                    fieldClassName = getDTOName(
                            dtoHelp.findEntityNameByLocalInt(dtoHelp.getFieldType(fieldName)));
                    cmFieldCmrField = fieldName;
                }
                
                addField(fieldIndex, fieldName, fieldClassName);
                //TODO: RETOUCHE
//                b.setCmFieldCmrField(fieldIndex, cmFieldCmrField);
//                // TODO change this hack. We should change the bean.dtd or something else
//                if(isMultiplicity) {
//                    b.setCmFieldCascadeDelete(fieldIndex,
//                            getDTOName(dtoHelp.findEntityNameByLocalInt(cmFieldCmrField)));
//                }
            }
        }
    } 
}
