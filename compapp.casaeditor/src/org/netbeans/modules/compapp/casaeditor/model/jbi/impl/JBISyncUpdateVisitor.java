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
package org.netbeans.modules.compapp.casaeditor.model.jbi.impl;

import org.netbeans.modules.compapp.casaeditor.model.jbi.Consumes;
import org.netbeans.modules.compapp.casaeditor.model.jbi.JBI;
import org.netbeans.modules.compapp.casaeditor.model.jbi.JBIComponent;
import org.netbeans.modules.compapp.casaeditor.model.visitor.JBIVisitor;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Connection;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Connections;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Consumer;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Identification;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Provider;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Provides;
import org.netbeans.modules.compapp.casaeditor.model.jbi.ServiceAssembly;
import org.netbeans.modules.compapp.casaeditor.model.jbi.ServiceUnit;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Services;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Target;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.ComponentUpdater.Operation;

/**
 * @author jqian
 */
public class JBISyncUpdateVisitor extends JBIVisitor.Default 
        implements ComponentUpdater<JBIComponent> {
    
    private JBIComponent target;
    private Operation operation;
    private int index;
    
    public JBISyncUpdateVisitor() {
    }

    public void update(JBIComponent target, JBIComponent child, Operation operation) {
        update(target, child, -1 , operation);
    }

    public void update(JBIComponent target, JBIComponent child, int index, Operation operation) {
        assert target != null;
        assert child != null;
        this.target = target;
        this.index = index;
        this.operation = operation;
        child.accept(this);
    }

    private void insert(String propertyName, JBIComponent component) {
        ((JBIComponentImpl)target).insertAtIndex(propertyName, component, index);
    }
    
    private void remove(String propertyName, JBIComponent component) {
        ((JBIComponentImpl)target).removeChild(propertyName, component);
    }
    
    public void visit(Services component) {
        if (target instanceof JBI) {
            if (operation == Operation.ADD) {
                insert(JBI.SERVICES_PROPERTY, component);
            } else {
                remove(JBI.SERVICES_PROPERTY, component);
            }
        }
    }

    public void visit(Consumes component) {
        if (target instanceof Services) {
            if (operation == Operation.ADD) {
                insert(Services.CONSUMES_PROPERTY, component);
            } else {
                remove(Services.CONSUMES_PROPERTY, component);
            }
        }
    }

     public void visit(Provides component) {
        if (target instanceof Services) {
            if (operation == Operation.ADD) {
                insert(Services.PROVIDES_PROPERTY, component);
            } else {
                remove(Services.PROVIDES_PROPERTY, component);
            }
        }
    }
     
    public void visit(ServiceAssembly component) {
        if (target instanceof JBI) {
            if (operation == Operation.ADD) {
                insert(JBI.SERVICE_ASSEMBLY_PROPERTY, component);
            } else {
                remove(JBI.SERVICE_ASSEMBLY_PROPERTY, component);
            }
        }
    }
    
    public void visit(ServiceUnit component) {
        if (target instanceof ServiceAssembly) {
            if (operation == Operation.ADD) {
                insert(ServiceAssembly.SERVICE_UNIT_PROPERTY, component);
            } else {
                remove(ServiceAssembly.SERVICE_UNIT_PROPERTY, component);
            }
        }
    }
    
    public void visit(Connections component) {
        if (target instanceof ServiceAssembly) {
            if (operation == Operation.ADD) {
                insert(ServiceAssembly.CONNECTIONS_PROPERTY, component);
            } else {
                remove(ServiceAssembly.CONNECTIONS_PROPERTY, component);
            }
        }
    }
    
    public void visit(Identification component) {
        if (target instanceof ServiceAssembly) {
            if (operation == Operation.ADD) {
                insert(ServiceAssembly.IDENTIFICATION_PROPERTY, component);
            } else {
                remove(ServiceAssembly.IDENTIFICATION_PROPERTY, component);
            }
        } else if (target instanceof ServiceUnit) {
            if (operation == Operation.ADD) {
                insert(ServiceUnit.IDENTIFICATION_PROPERTY, component);
            } else {
                remove(ServiceUnit.IDENTIFICATION_PROPERTY, component);
            }
        }
    }
    
    public void visit(Target component) {
        if (target instanceof ServiceUnit) {
            if (operation == Operation.ADD) {
                insert(ServiceUnit.TARGET_PROPERTY, component);
            } else {
                remove(ServiceUnit.TARGET_PROPERTY, component);
            }
        } 
    }
    
    public void visit(Connection component) {
        if (target instanceof Connections) {
            if (operation == Operation.ADD) {
                insert(Connections.CONNECTION_PROPERTY, component);
            } else {
                remove(Connections.CONNECTION_PROPERTY, component);
            }
        }
    }

    public void visit(Consumer component) {
        if (target instanceof Connection) {
            if (operation == Operation.ADD) {
                insert(Connection.CONSUMER_PROPERTY, component);
            } else {
                remove(Connection.CONSUMER_PROPERTY, component);
            }
        }
    }

     public void visit(Provider component) {
        if (target instanceof Connection) {
            if (operation == Operation.ADD) {
                insert(Connection.PROVIDER_PROPERTY, component);
            } else {
                remove(Connection.PROVIDER_PROPERTY, component);
            }
        }
    }
}
