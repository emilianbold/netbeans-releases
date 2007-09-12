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
