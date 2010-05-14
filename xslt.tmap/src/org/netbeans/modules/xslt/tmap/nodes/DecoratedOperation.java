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

package org.netbeans.modules.xslt.tmap.nodes;

import org.netbeans.modules.xslt.tmap.model.api.Operation;
import org.netbeans.modules.xslt.tmap.model.api.Variable;
import org.netbeans.modules.xslt.tmap.util.Util;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class DecoratedOperation  extends DecoratedTMapComponentAbstract<Operation> {

    public DecoratedOperation(Operation orig) {
        super(orig);
    }
    

    @Override
    public String getHtmlDisplayName() {
        Operation ref = getReference();
        String opName = null;
        if (ref != null) {
            opName = Util.getReferenceLocalName(ref.getOperation());
        }
        String addon = null;
        if (opName != null) {
            addon = TMapComponentNode.WHITE_SPACE+opName; // NOI18N
        }
        
        return Util.getGrayString(super.getHtmlDisplayName(), addon);
    }
    
    @Override
    public String getTooltip() {
        Operation ref = getReference();
        StringBuffer attributesTooltip = new StringBuffer();
        if (ref != null) {
            attributesTooltip.append(
                    Util.getLocalizedAttribute(ref.getOperation()
                    , Operation.OPERATION_NAME));
            
            Variable inputVar = ref.getInputVariable();
            if (inputVar == null) {
                inputVar = ref.getDefaultInputVariable();
                if (inputVar != null) {
                    attributesTooltip.append(
                            Util.getLocalizedAttribute(inputVar.getName()
                            , Operation.DEFAULT_INPUT_VARIABLE));
                }
            } else {
                attributesTooltip.append(
                        Util.getLocalizedAttribute(inputVar.getName()
                        , Operation.INPUT_VARIABLE));
            }

            Variable outputVar = ref.getOutputVariable();
            if (outputVar == null) {
                outputVar = ref.getDefaultOutputVariable();
                if (outputVar != null) {
                    attributesTooltip.append(
                            Util.getLocalizedAttribute(outputVar.getName()
                            , Operation.DEFAULT_OUTPUT_VARIABLE));
                }
            } else {
                attributesTooltip.append(
                        Util.getLocalizedAttribute(outputVar.getName()
                        , Operation.OUTPUT_VARIABLE));
            }
        }


        return NbBundle.getMessage(TMapComponentNode.class, 
                "LBL_LONG_TOOLTIP_HTML_TEMPLATE", super.getName(), 
                attributesTooltip.toString());        
    }
   
}

