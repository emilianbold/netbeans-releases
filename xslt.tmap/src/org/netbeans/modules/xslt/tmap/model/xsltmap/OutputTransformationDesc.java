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
package org.netbeans.modules.xslt.tmap.model.xsltmap;

import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xslt.tmap.util.Util;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class OutputTransformationDesc extends AbstractTransformationDesc {
    
    public OutputTransformationDesc(XsltMapModel model, TransformationUC parent) {
        super(model, parent);
    }
    
    public TransformationDescType getType() {
        return TransformationDescType.OUTPUT;
    }

    public AXIComponent getTargetAXIType(FileObject projectRoot) {
        TransformationUC tUC = getParent();
        if (tUC == null) {
            return null;
        }
        
        InputTransformationDesc itDesc = tUC.getInputTransformationDesc();
        return itDesc == null ? null : itDesc.getPltTargetAXIType(projectRoot);
    }

    public AXIComponent getSourceAXIType(FileObject projectRoot) {
        return getPltTargetAXIType(projectRoot);
    }

    public void setMessageType(String messageType) {
        if (isFilterRequestReply()) {
            super.setMessageType(messageType);
        }
    }

    public void setFile(String file) {
        if (isFilterRequestReply()) {
            super.setFile(file);
        }
    }

    public void setTransformJBI(boolean transformJBI) {
        if (isFilterRequestReply()) {
            super.setTransformJBI(transformJBI);
        }
    }

    public void setTransformJBI(String transformJBI) {
        if (isFilterRequestReply()) {
            super.setTransformJBI(transformJBI);
        }
    }

    public void setMessageType(Operation inputOperation, Operation outputOperation) {
        TransformationUC tUC = getParent();
        if (tUC == null || (inputOperation == null && outputOperation == null)) {
            return;
        }
        
        String messageType = Util.getMessageType(inputOperation, false);
        
        setMessageType(messageType);
    }
    
    private boolean isFilterRequestReply() {
        TransformationUC tUC = getParent();
        return tUC != null 
                && TransformationType.FILTER_REQUEST_REPLY.equals(tUC.getTransformationType()); 
    }
}
