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

package org.netbeans.modules.xml.wsdl.ui.view.property;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.ui.commands.ConstraintNamedPropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.commands.NamedPropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.model.StringAttribute;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.ErrorManager;

/**
 * 
 * @author radval
 *
 */
public class OperationInputOutputFaultPropertyAdapter extends ConstraintNamedPropertyAdapter implements NamedPropertyAdapter {
    
    private OperationParameter mOParameter;
    
    public OperationInputOutputFaultPropertyAdapter(WSDLComponent wsdlComponent) {
        super(wsdlComponent);
        this.mOParameter  = (OperationParameter) wsdlComponent;
    }
    
    @Override
    public String getName() {
        if (mOParameter.getAttribute(new StringAttribute("name")) == null) {
            return "";
        }
        return super.getName();
    }
     
     public String getMessage() {
         NamedComponentReference message = mOParameter.getMessage();
         if(message == null) {
             return "";
         }
         QName messageQName = message.getQName();
         return Utility.fromQNameToString(messageQName);
     }
     
     public void setMessage(String message) {
         if(message != null) {
             try {
                 org.netbeans.modules.xml.wsdl.ui.common.QName messageQName = org.netbeans.modules.xml.wsdl.ui.common.QName.getQNameFromString(message);
                 if(messageQName == null) {
                     mOParameter.getModel().startTransaction();
                     mOParameter.setMessage(null);
                     mOParameter.getModel().endTransaction();
                 } else {
                     
                     String ns = messageQName.getNamespaceURI();
                     String prefix = messageQName.getPrefix();
                     if(ns == null || ns.trim().equals("")) {
                         ns = Utility.getNamespaceURI(prefix, mOParameter.getModel());
                     }
                     
                     QName qname = null;
                     if (ns != null) {
                         qname = new QName(ns, messageQName.getLocalName());
                     }
                     
                     if(qname != null) {
                         Message msg = mOParameter.getModel().findComponentByName(qname, Message.class);
                         if (msg == null) {
                             ErrorManager.getDefault().notify(ErrorManager.ERROR, new Exception ("Not a valid type"));
                         } else {
                             mOParameter.getModel().startTransaction();
                             mOParameter.setMessage(mOParameter.createReferenceTo(msg, Message.class));
                             
                             mOParameter.getModel().endTransaction();
                         }
                     }
                 }
             } catch (Exception e) {
                 ErrorManager.getDefault().notify(e);
             }
         }
     }

    @Override
    public boolean isNameExists(String name) {
        boolean exists = false;
        Operation operation = (Operation) mOParameter.getParent();
        if (operation.getInput() != null) {
            exists = operation.getInput().getName().equals(name);
        }
        
        if (operation.getOutput() != null) {
            exists = operation.getOutput().getName().equals(name);
        }
        
        return exists;
    }

}
