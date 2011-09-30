/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.coherence.xml.pof.impl;

import org.netbeans.modules.coherence.xml.pof.AllowInterfaces;
import org.netbeans.modules.coherence.xml.pof.AllowSubclasses;
import org.netbeans.modules.coherence.xml.pof.ClassName;
import org.netbeans.modules.coherence.xml.pof.DefaultSerializer;
import org.netbeans.modules.coherence.xml.pof.Include;
import org.netbeans.modules.coherence.xml.pof.InitParam;
import org.netbeans.modules.coherence.xml.pof.InitParams;
import org.netbeans.modules.coherence.xml.pof.ParamType;
import org.netbeans.modules.coherence.xml.pof.ParamValue;
import org.netbeans.modules.coherence.xml.pof.PofConfig;
import org.netbeans.modules.coherence.xml.pof.PofConfigComponent;
import org.netbeans.modules.coherence.xml.pof.PofConfigVisitor;
import org.netbeans.modules.coherence.xml.pof.Serializer;
import org.netbeans.modules.coherence.xml.pof.SerializerType;
import org.netbeans.modules.coherence.xml.pof.TypeId;
import org.netbeans.modules.coherence.xml.pof.UserType;
import org.netbeans.modules.coherence.xml.pof.UserTypeList;
import org.netbeans.modules.coherence.xml.pof.UserTypeListElement;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.ComponentUpdater.Operation;

/**
 *
 * @author Andrew Hopkinson (Oracle A-Team)
 */
public class SyncUpdateVisitor implements ComponentUpdater<PofConfigComponent>, PofConfigVisitor {

    private PofConfigComponent myParent;
    private int myIndex;
    private Operation myOperation;

    public Operation getOperation() {
        return myOperation;
    }

    public int getIndex() {
        return myIndex;
    }

    public PofConfigComponent getParent() {
        return myParent;
    }

    private boolean isAdd() {
        return getOperation() == Operation.ADD;
    }

    private boolean isRemove() {
        return getOperation() == Operation.REMOVE;
    }

    @Override
    public void update(PofConfigComponent target, PofConfigComponent child, Operation operation) {
        update(target, child, -1, operation);
    }

    @Override
    public void update(PofConfigComponent target, PofConfigComponent child, int index, Operation operation) {
        if (target != null && child != null
                && (operation == null || operation == Operation.ADD || operation == Operation.REMOVE)) {
            myIndex = index;
            myOperation = operation;
            myParent = target;
            child.accept(this);
        }
    }

    @Override
    public void visit(PofConfig pofConfig) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(AllowInterfaces element) {
        if (getParent() instanceof PofConfig) {
            PofConfig parent = (PofConfig) getParent();
            if (isAdd()) {
                parent.setAllowInterfaces(element);
            } else if (isRemove()) {
                parent.setAllowInterfaces(null);
            }
        }
    }

    @Override
    public void visit(AllowSubclasses element) {
        if (getParent() instanceof PofConfig) {
            PofConfig parent = (PofConfig) getParent();
            if (isAdd()) {
                parent.setAllowSubclasses(element);
            } else if (isRemove()) {
                parent.setAllowSubclasses(null);
            }
        }
    }

    @Override
    public void visit(ClassName element) {
        if (getParent() instanceof SerializerType) {
            SerializerType parent = (SerializerType) getParent();
            if (isAdd()) {
                parent.setClassName(element);
            } else if (isRemove()) {
                parent.setClassName(null);
            }
        } else if (getParent() instanceof UserType) {
            UserType parent = (UserType) getParent();
            if (isAdd()) {
                parent.setClassName(element);
            } else if (isRemove()) {
                parent.setClassName(null);
            }
        }

    }

    @Override
    public void visit(DefaultSerializer element) {
        if (getParent() instanceof PofConfig) {
            PofConfig parent = (PofConfig) getParent();
            if (isAdd()) {
                parent.setDefaultSerializer(element);
            } else if (isRemove()) {
                parent.setDefaultSerializer(null);
            }
        }
    }

    @Override
    public void visit(Include include) {
        visitUserTypeListElement(include);
    }

    @Override
    public void visit(InitParam element) {
        if (getParent() instanceof InitParams) {
            InitParams parent = (InitParams) getParent();
            if (isAdd()) {
                parent.addInitParam(getIndex(), element);
            } else if (isRemove()) {
                parent.removeInitParam(element);
            }
        }
    }

    @Override
    public void visit(InitParams element) {
        if (getParent() instanceof SerializerType) {
            SerializerType parent = (SerializerType) getParent();
            if (isAdd()) {
                parent.setInitParams(element);
            } else if (isRemove()) {
                parent.setInitParams(null);
            }
        }
    }

    @Override
    public void visit(ParamType element) {
        if (getParent() instanceof InitParam) {
            InitParam parent = (InitParam) getParent();
            if (isAdd()) {
                parent.setParamType(element);
            } else if (isRemove()) {
                parent.setParamType(null);
            }
        }
    }

    @Override
    public void visit(ParamValue element) {
        if (getParent() instanceof InitParam) {
            InitParam parent = (InitParam) getParent();
            if (isAdd()) {
                parent.setParamValue(element);
            } else if (isRemove()) {
                parent.setParamValue(null);
            }
        }
    }

    @Override
    public void visit(Serializer element) {
        if (getParent() instanceof UserType) {
            UserType parent = (UserType) getParent();
            if (isAdd()) {
                parent.setSerializer(element);
            } else if (isRemove()) {
                parent.setSerializer(null);
            }
        }
    }

    @Override
    public void visit(SerializerType serializerType) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(TypeId element) {
        if (getParent() instanceof UserType) {
            UserType parent = (UserType) getParent();
            if (isAdd()) {
                parent.setTypeId(element);
            } else if (isRemove()) {
                parent.setTypeId(null);
            }
        }
    }

    @Override
    public void visit(UserType userType) {
        visitUserTypeListElement(userType);
    }

    @Override
    public void visit(UserTypeList element) {
        if (getParent() instanceof PofConfig) {
            PofConfig parent = (PofConfig) getParent();
            if (isAdd()) {
                parent.setUserTypeList(element);
            } else if (isRemove()) {
                parent.setUserTypeList(null);
            }
        }
    }

    private void visitUserTypeListElement(UserTypeListElement element) {
        try {
            if (getParent() instanceof UserTypeList) {
                UserTypeList parent = (UserTypeList) getParent();
                if (isAdd()) {
                    parent.addElement(getIndex(), element);
                } else if (isRemove()) {
                    parent.removeElement(element);
                }
            }
        } catch (Exception e) {
        }
    }
}
