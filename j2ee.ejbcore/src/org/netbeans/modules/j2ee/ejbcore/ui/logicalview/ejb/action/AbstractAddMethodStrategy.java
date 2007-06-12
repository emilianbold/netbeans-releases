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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action;

import java.io.IOException;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.common.method.MethodCustomizer;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.MethodsNode;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 * Strategy for visual support for adding various methods into an EJB.
 * 
 * @author Pavel Buzek
 * @author Martin Adamek
 */
public abstract class AbstractAddMethodStrategy {
    
    private final String name;
    
    public AbstractAddMethodStrategy(String name) {
        this.name = name;
    }
    
    protected abstract MethodModel getPrototypeMethod();
    
    /** Describes method type handled by this action. */
    public abstract MethodType.Kind getPrototypeMethodKind();
    
    protected abstract MethodCustomizer createDialog(FileObject fileObject, MethodModel methodModel) throws IOException;

    protected abstract void generateMethod(MethodModel method, boolean isOneReturn, boolean publishToLocal, boolean publishToRemote,
            String ejbql, FileObject ejbClassFO, String ejbClass) throws IOException;
    
    public abstract boolean supportsEjb(FileObject fileObject, String className);

    public String getTitle() {
        return name;
    }
    
    public void addMethod(FileObject fileObject, String className) throws IOException {
        if (className == null) {
            return;
        }
        MethodModel methodModel = getPrototypeMethod();
        MethodCustomizer methodCustomizer = createDialog(fileObject, methodModel);
        if (methodCustomizer.customizeMethod()) {
            try {
                MethodModel method = methodCustomizer.getMethodModel();
                boolean isOneReturn = methodCustomizer.finderReturnIsSingle();
                boolean publishToLocal = methodCustomizer.publishToLocal();
                boolean publishToRemote = methodCustomizer.publishToRemote();
                String ejbql = methodCustomizer.getEjbQL();
                generateMethod(method, isOneReturn, publishToLocal, publishToRemote, ejbql, fileObject, className);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
    }
    
    protected EjbJar getEjbModule(FileObject fileObject) {
        return org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(fileObject);
    }
    
    protected static MethodsNode getMethodsNode() {
        Node[] nodes = Utilities.actionsGlobalContext().lookup(new Lookup.Template<Node>(Node.class)).allInstances().toArray(new Node[0]);
        if (nodes.length != 1) {
            return null;
        }
        return nodes[0].getLookup().lookup(MethodsNode.class);
    }
    
}
