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

package org.netbeans.modules.websvc.core;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.common.source.GenerationUtils;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlParameter;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mkuchtiak
 */
public class MethodGenerator {
    FileObject implClassFo;
    WsdlModel wsdlModel;
    /** Creates a new instance of MethodGenerator */
    public MethodGenerator(WsdlModel wsdlModel, FileObject implClassFo) {
        this.implClassFo=implClassFo;
        this.wsdlModel=wsdlModel;
    }
    
    public void generateMethod(final String operationName) throws IOException {   
        
        // Use Progress API to display generator messages.
        //ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(MethodGenerator.class, "TXT_AddingMethod")); //NOI18N
        //handle.start(100);

        JavaSource targetSource = JavaSource.forFileObject(implClassFo);
        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
                if (genUtils!=null) {
                    
                    // get proper wsdlOparation;
                    WsdlOperation wsdlOperation = getWsdlOperation(wsdlModel, operationName);
                    
                    TreeMaker make = workingCopy.getTreeMaker();
                    ClassTree javaClass = genUtils.getClassTree();
                    
                    // return type
                    String returnType = wsdlOperation.getReturnTypeName();

                    // create parameters
                    List<WsdlParameter> parameters = wsdlOperation.getParameters();
                    List<VariableTree> params = new ArrayList<VariableTree>();
                    for (WsdlParameter parameter:parameters) {
                        // create parameter:
                        params.add(make.Variable(
                                make.Modifiers(
                                    Collections.<Modifier>emptySet(),
                                    Collections.<AnnotationTree>emptyList()
                                ),
                                parameter.getName(), // name
                                make.Identifier(parameter.getTypeName()), // parameter type
                                null // initializer - does not make sense in parameters.
                        ));
                    }

                    // create exceptions
                    Iterator<String> exceptions = wsdlOperation.getExceptions();
                    List<ExpressionTree> exc = new ArrayList<ExpressionTree>();
                    while (exceptions.hasNext()) {
                        String exception = exceptions.next();
                        TypeElement excEl = workingCopy.getElements().getTypeElement(exception);
                        exc.add(make.QualIdent(excEl));
                    }

                    // create method
                    ModifiersTree methodModifiers = make.Modifiers(
                        Collections.<Modifier>singleton(Modifier.PUBLIC),
                        Collections.<AnnotationTree>emptyList()
                    );
                    MethodTree method = make.Method(
                            methodModifiers, // public
                            wsdlOperation.getJavaName(), // operation name
                            make.Identifier(returnType), // return type 
                            Collections.<TypeParameterTree>emptyList(), // type parameters - none
                            params,
                            exc, // throws 
                            "{ //TODO implement this method\nthrow new UnsupportedOperationException(\"Not implemented yet.\") }", // body text
                            null // default value - not applicable here, used by annotations
                    );

                    ClassTree modifiedClass =  make.addClassMember(javaClass, method); 

                    workingCopy.rewrite(javaClass, modifiedClass);
                }
            }
            
            public void cancel() {
            }
        };
        targetSource.runModificationTask(task).commit();
    }
    
    private WsdlOperation getWsdlOperation (WsdlModel model, String operationName) {
        // TODO: exclude non DOCUMENT/LITERAL ports
        List<WsdlService> services = model.getServices();
        for (WsdlService service:services) {
            List<WsdlPort> ports = service.getPorts();
            for (WsdlPort port:ports) {
                List<WsdlOperation> operations = port.getOperations();
                for (WsdlOperation operation:operations) {
                    if (operationName.equals(operation.getName())) return operation;
                }
            }
        }
        return null;
    }
    
}
