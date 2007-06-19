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

import org.netbeans.modules.j2ee.ejbcore._RetoucheUtil;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.common.method.MethodCustomizerFactory;
import org.netbeans.modules.j2ee.common.method.MethodCustomizer;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.ejbcore.action.SelectMethodGenerator;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EjbMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * @author Pavel Buzek
 */
public class AddSelectMethodStrategy extends AbstractAddMethodStrategy {
    
    public AddSelectMethodStrategy() {
        super(NbBundle.getMessage(AddSelectMethodStrategy.class, "LBL_AddSelectMethodAction"));
    }
    
    public AddSelectMethodStrategy(String name) {
        super(name);
    }
    
    @Override
    public MethodModel getPrototypeMethod() {
        Set<Modifier> modifiers = new HashSet<Modifier>();
        modifiers.add(Modifier.PUBLIC);
        modifiers.add(Modifier.ABSTRACT);
        return MethodModel.create(
                "ejbSelectBy",
                "int",
                "",
                Collections.<MethodModel.Variable>emptyList(),
                Collections.singletonList("javax.ejb.FinderException"),
                modifiers
                );
    }
    
    protected MethodCustomizer createDialog(FileObject fileObject, final MethodModel methodModel) throws IOException {
        String className = _RetoucheUtil.getMainClassName(fileObject);
        EjbMethodController ejbMethodController = EjbMethodController.createFromClass(fileObject, className);
        return MethodCustomizerFactory.selectMethod(
                getTitle(),
                methodModel,
                ejbMethodController.createDefaultQL(methodModel),
                Collections.<MethodModel>emptySet() //TODO: RETOUCHE collect all methods
                );
    }
    
//    @SuppressWarnings("deprecation") //NOI18N
//    protected void okButtonPressed(final MethodCustomizer methodCustomizer, final MethodType methodType, 
//            final FileObject fileObject, String classHandle) throws java.io.IOException {
//        ProgressHandle handle = ProgressHandleFactory.createHandle("Adding method");
//        try {
//            handle.start(100);
//            String className = _RetoucheUtil.getMainClassName(fileObject);
//            EjbMethodController ejbMethodController = EjbMethodController.createFromClass(fileObject, className);
//            MethodModel method = methodType.getMethodElement();
//            EntityMethodController entityMethodController = (EntityMethodController) ejbMethodController;
//            entityMethodController.addSelectMethod(method, methodCustomizer.getEjbQL(), getEjbModule(fileObject).getDeploymentDescriptor());
//            handle.progress(99);
//        } finally {
//            handle.finish();
//        }
//    }
    
    public MethodType.Kind getPrototypeMethodKind() {
        return MethodType.Kind.SELECT;
    }

    protected void generateMethod(MethodModel method, boolean isOneReturn,
                                  boolean publishToLocal,
                                  boolean publishToRemote, String ejbql,
                                  FileObject ejbClassFO, String ejbClass) throws IOException {
        SelectMethodGenerator generator = SelectMethodGenerator.create(ejbClass, ejbClassFO);
        generator.generate(method, publishToLocal, publishToRemote, isOneReturn, ejbql);
    }

    public boolean supportsEjb(FileObject fileObject,final String className) {

        boolean isCMP = false;
        
        EjbJar ejbModule = getEjbModule(fileObject);
        if (ejbModule != null) {
            MetadataModel<EjbJarMetadata> metadataModel = ejbModule.getMetadataModel();
            try {
                isCMP = metadataModel.runReadAction(new MetadataModelAction<EjbJarMetadata, Boolean>() {
                    public Boolean run(EjbJarMetadata metadata) {
                        Ejb ejb = metadata.findByEjbClass(className);
                        if (ejb instanceof Entity) {
                            Entity entity = (Entity) ejb;
                            return Entity.PERSISTENCE_TYPE_CONTAINER.equals(entity.getPersistenceType());
                        }
                        return false;
                    }
                });
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        
        return isCMP;

    }
}
