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

package org.netbeans.modules.bpel.mapper.predicates.editor;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import org.netbeans.modules.bpel.mapper.tree.BpelMapperContext;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.mappercore.model.MapperModel;


/**
 *
 * @author nk160297
 * @author AlexanderPermyakov
 */
public class PredicatesMapperFactory {

    public static Mapper createMapper(MapperModel model) {
        Mapper newMapper = new Mapper(model);
        newMapper.setContext(new BpelMapperContext());
        //
        // Specify the action processing
        InputMap inputMap1 = newMapper.getInputMap(JComponent.WHEN_FOCUSED);
        InputMap inputMap2 = newMapper.getInputMap(
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = newMapper.getActionMap();
        //
        //
        return newMapper;
    }
    
}
