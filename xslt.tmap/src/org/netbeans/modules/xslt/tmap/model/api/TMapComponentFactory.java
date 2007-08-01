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
package org.netbeans.modules.xslt.tmap.model.api;

import org.netbeans.modules.xml.xam.dom.ComponentFactory;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public interface TMapComponentFactory extends ComponentFactory<TMapComponent> {

/**
 * <p>
 * The following transformmap sample specifies the expected content in transfoprmmap.
 * 
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?&gt;
 * &lt;transformmap xmlns='http://xml.netbeans.org/schema/transformmap'&gt;
 *        
 *      &lt;service partnerLinkType="ns0:plt1" roleName="role1"&gt;
 *            &lt;operation opName="operation0"&gt;
 *                &lt;transform result="" source="" file=""&gt;
 *                    &lt;param name="param1" type="t1"&gt;&lt;/param&gt;
 *                    &lt;param name="param2" type="t2"&gt;&lt;/param&gt;
 *                &lt;/transform&gt;
 *            &lt;/operation&gt;
 *            &lt;operation opName="operation1"&gt;
 *                &lt;invoke  roleName="role2" partnerLinkType="ns0:plt2" inputVariable=""  outputVariable=""/&gt;
 *            &lt;/operation&gt;
 *        &lt;/service&gt;
 * 
 * &lt;/transformmap&gt;
 * </pre>
 */

    TransformMap createTransformMap();
    
    Service createService();
    
    Operation createOperation();
    
    Invoke createInvoke();

    Transform createTransform();

    Param createParam();
}
