/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.compapp.casaeditor.nodes;

import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponentVisitor;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConnection;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConsumes;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpoint;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPort;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaProvides;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaServiceEngineServiceUnit;

/**
 *
 * @author jsandusky
 */
public class CasaNodeCreationVisitor extends CasaComponentVisitor.Default {
    
    private CasaNode mNode;
    private CasaNodeFactory mNodeFactory;
    
    
    public CasaNodeCreationVisitor(CasaNodeFactory factory) {
        mNodeFactory = factory;
    }
    
    
    public CasaNode getNode() {
        return mNode;
    }
    
    @Override
    public void visit(CasaServiceEngineServiceUnit data) {
        mNode = new ServiceUnitNode(data, mNodeFactory);
    }

    @Override
    public void visit(CasaConnection data) {
        mNode = new ConnectionNode(data, mNodeFactory);
    }

    @Override
    public void visit(CasaConsumes data) {
        mNode = new ConsumesNode(data, mNodeFactory);
    }

    @Override
    public void visit(CasaProvides data) {
        mNode = new ProvidesNode(data, mNodeFactory);
    }

    @Override
    public void visit(CasaPort data) {
        mNode = new WSDLEndpointNode(data, mNodeFactory);
    }
    
    @Override
    public void visit(CasaEndpoint data) {
        mNode = new ServiceUnitProcessNode(data, mNodeFactory);
    }
    
}
