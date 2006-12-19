/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.patterns;

import java.io.IOException;
import org.netbeans.modules.j2ee.dd.api.ejb.CmpField;
import org.netbeans.modules.j2ee.dd.api.ejb.CmrField;
import org.netbeans.modules.j2ee.ejbcore.api.codegeneration.EjbGenerationUtil;
import org.openide.filesystems.FileObject;

/**
 * Class that generates the DTO source from Entity Bean
 * @author blaha
 */
public class DTOGenerator {
    private EjbGenerationUtil genUtil = new EjbGenerationUtil();
    private static final String DTO_TEMPLATE = EjbGenerationUtil.TEMPLATE_BASE + "DTO.xml"; //NOI18N
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
            pkgName = EjbGenerationUtil.getSelectedPackageName(pkg, dtoHelp.getProject());
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
