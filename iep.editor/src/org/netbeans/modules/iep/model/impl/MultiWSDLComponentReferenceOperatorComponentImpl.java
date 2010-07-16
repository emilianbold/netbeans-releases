/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.iep.model.impl;

import java.util.Collection;
import java.util.Iterator;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.MultiWSDLComponentReference;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.w3c.dom.Element;

/**
 *
 * @author radval
 */
public class MultiWSDLComponentReferenceOperatorComponentImpl extends OperatorComponentImpl implements MultiWSDLComponentReference {

    public MultiWSDLComponentReferenceOperatorComponentImpl(IEPModel model,  Element e) {
        super(model, e);
    }
        
    public MultiWSDLComponentReferenceOperatorComponentImpl(IEPModel model) {
        super(model);
    }
    
    public NamedComponentReference<PortType> getPortType() {
        return resolveGlobalReference(PortType.class, ATTR_PORTTYPE);
    }

    public void setPortType(NamedComponentReference<PortType> value) {
        setAttribute(PORT_TYPE, ATTR_PORTTYPE, value);
    }

    public NamedComponentReference<Operation> getOperation() {
        String v = getAttribute(ATTR_OPERATION);
        if(v == null) {
            return null;
        }
        
        NamedComponentReference<PortType> ptRef = getPortType();
        if(ptRef != null) {
            
            PortType pt = ptRef.get();
            if(pt != null) {
                Collection<Operation> ops = pt.getOperations();
                Iterator<Operation> it = ops.iterator();
                Operation matchingOp = null;
                
                while(it.hasNext()) {
                    Operation op = it.next();
                    if(v.equals(op.getName())) {
                        matchingOp = op;
                        break;
                    }
                }
                
                if(matchingOp != null) {
                    return matchingOp.createReferenceTo(matchingOp, Operation.class);
                }
            }
        }
        
        return null;
        //return resolveGlobalReference(Operation.class, ATTR_OPERATION);
    }

    public void setOperation(NamedComponentReference<Operation> value) {
       if(value != null) {
            Operation op = value.get();
            if(op != null) {
                setAttribute(OPERATION, ATTR_OPERATION, op.getName());
            }
       } else {
        setAttribute(OPERATION, ATTR_OPERATION, value);
       }
    }

    public NamedComponentReference<Message> getMessage() {
        String v = getAttribute(ATTR_MESSAGE);
        if(v == null) {
            return null;
        }
        
        NamedComponentReference<Operation> opRef = getOperation();
        if(opRef != null) {
            Operation op = opRef.get();
            if(op != null) {
                Input input = op.getInput();
                if(input != null){
                    NamedComponentReference<Message> msgRef = input.getMessage();
                    if(msgRef != null) {
                        Message msg = msgRef.get();
                        if(msg!=null && v.equals(msg.getName())) {
                            return msgRef;
                        }
                    }
                }
            }
        }
        
        return null;
//        return resolveGlobalReference(Message.class, ATTR_MESSAGE);
    }

    public void setMessage(NamedComponentReference<Message> value) {
        if(value != null) {
            Message msg = value.get();
            if(msg != null) {
                setAttribute(MESSAGE, ATTR_MESSAGE, msg.getName());
            }
       } else {
        setAttribute(MESSAGE, ATTR_MESSAGE, value);
       }
        
    }

    

    
}
