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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xslt.mapper.model.nodes.actions;

import java.awt.Image;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.xslt.model.CopyOf;
import org.netbeans.modules.xslt.model.ForEach;
import org.netbeans.modules.xslt.model.If;
import org.netbeans.modules.xslt.model.Instruction;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author nk160297
 */
public enum SupportedRuleTypes {
    IF(If.class),
    FOR_EACH(ForEach.class),
    COPY_OF(CopyOf.class);
    
    private static Map<Class<? extends Instruction>, SupportedRuleTypes> classToType;
    
    private static final String COMMON_RULE_IMAGE_NAME =
            "org/netbeans/modules/xslt/mapper/model/nodes/actions/Instruction.png"; // NOI18N
    private static final Image COMMON_IMAGE =
            Utilities.loadImage(COMMON_RULE_IMAGE_NAME);
    
    private Class<? extends Instruction> myInterface = null;
    private String myDisplayName;
    private Image myDefaultImage;
    
    SupportedRuleTypes() {
    }
    
    SupportedRuleTypes(Class<? extends Instruction> instrInterf) {
        assert instrInterf.isInterface() : "The interface should be specified"; // NOI18N
        myInterface = instrInterf;
        //
        registerType(instrInterf, this);
    }
    
    public Class<? extends Instruction> getInterface() {
        return myInterface;
    }
    
    private static synchronized void registerType(
            Class<? extends Instruction> instrInterf, SupportedRuleTypes ruleType) {
        if (instrInterf != null && ruleType != null) {
            if (classToType == null) {
                classToType = new HashMap
                        <Class<? extends Instruction>, SupportedRuleTypes>();
            }
            classToType.put(instrInterf, ruleType);
        }
    }
    
    public static SupportedRuleTypes getRuleType(Instruction instruction) {
        return classToType.get(instruction.getClass());
    }
    
    public static SupportedRuleTypes getRuleType(
            Class<? extends Instruction> instrClass) {
        return classToType.get(instrClass);
    }
    
    public static Image getCommonImage() {
        return COMMON_IMAGE;
    }
    
    public synchronized String getDisplayName() {
        if (myDisplayName == null) {
            try {
                myDisplayName = NbBundle.getMessage(
                        SupportedRuleTypes.class, this.toString());
            } catch(Exception ex) {
                myDisplayName = name();
            }
        }
        return myDisplayName;
    }
    
    public synchronized Image getImage() {
        return COMMON_IMAGE;
    }
    
}
