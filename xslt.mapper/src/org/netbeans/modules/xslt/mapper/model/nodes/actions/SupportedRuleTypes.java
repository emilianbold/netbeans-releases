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

package org.netbeans.modules.xslt.mapper.model.nodes.actions;

import java.awt.Image;
import java.util.HashMap;
import java.util.Map;
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
    FOR_EACH(ForEach.class);
    
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
        assert instrInterf.isInterface() : "The interface should be specified"; // NOI8N
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
