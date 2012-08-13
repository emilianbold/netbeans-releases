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
package org.netbeans.modules.j2ee.persistence.spi.jpql;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.lang.model.element.TypeElement;
import org.eclipse.persistence.jpa.jpql.TypeHelper;
import org.eclipse.persistence.jpa.jpql.spi.IType;
import org.eclipse.persistence.jpa.jpql.spi.ITypeRepository;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObject;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.util.MetadataModelReadHelper;
import org.openide.filesystems.FileObject;

/**
 *
 * @author sp153251
 */
public class TypeRepository implements ITypeRepository {
    private final Project project;
    private final Map<String, IType[]> types;
    private MetadataModelReadHelper<EntityMappingsMetadata, List<org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity>> readHelper;
    private final ManagedTypeProvider mtp;
    private AnnotationModelHelper amh;


    TypeRepository(Project project, ManagedTypeProvider mtp) {
        this.project = project;
        this.mtp = mtp;
        types = new HashMap<String, IType[]>();
    }
    
    @Override
    public IType getEnumType(String fqn) {
        IType[] ret = types.get(fqn);
        if(ret == null){
            //get main type
            int lastPoint = fqn.lastIndexOf('.');
            String mainPart = lastPoint > 0 ? fqn.substring(0, lastPoint) : null;
            if(mainPart != null){
                IType[] mainType = types.get(mainPart);
                if(mainType == null){
                    fillTypeElement(mainPart);
                }
                mainType = types.get(mainPart);
                if(mainType[0] != null){
                    fillTypeElement(fqn);
                } else {
                    types.put(fqn, new Type[]{null});
                }
            } else {
                //shouldn't happens
                fillTypeElement(fqn);
            }
            ret = types.get(fqn);
        }
        return ret[0];
    }

    @Override
    public IType getType(Class<?> type) {
        String fqn = type.getCanonicalName();
        IType[] ret = types.get(fqn);
        if(ret == null){
            fillTypeElement(type);
            ret = types.get(fqn);
        }
        return ret[0];
    }

    @Override
    public IType getType(String fqn) {
        IType[] ret = types.get(fqn);
        if(ret == null && isValid()){
            if(IType.UNRESOLVABLE_TYPE.equals(fqn)){
                types.put(fqn, new Type[] {new Type(this, fqn)});
            } else {
                //try to find in managed
                int lastPnt = fqn.lastIndexOf('.');
                ManagedType mt = (ManagedType) (lastPnt > -1 ? mtp.getManagedType(fqn.substring(lastPnt+1)) :  mtp.getManagedType(fqn));
                if(mt != null  && mt.getPersistentObject() != null && mt.getPersistentObject().getTypeElement()!=null && mt.getPersistentObject().getTypeElement().getQualifiedName().contentEquals(fqn)) {
                    types.put(fqn, new Type[]{new Type(TypeRepository.this, mt.getPersistentObject())});
                } else {
                    //
                    fillTypeElement(fqn);
                }
            }
            ret = types.get(fqn);
        }
        return ret[0];
    }

    @Override
    public TypeHelper getTypeHelper() {
        return new TypeHelper(this);
    }
    
    private void fillTypeElement(final String fqn){
        types.put(fqn, new Type[]{null});
        if(isValid()){ 
            getAnnotationModelHelper();
            if(amh != null && isValid()) {
                try {
                    amh.runJavaSourceTask(new Callable<Void>() {

                        @Override
                        public Void call() throws Exception {
                                if(isValid()) {//model will be filled with nulls  after provider invalidation and with values only if valid provider
                                    TypeElement te = amh.getCompilationController().getElements().getTypeElement(fqn);
                                    if(te!=null) {
                                        PersistentObject po = new PersistentObject(amh, te) {};
                                        types.put(fqn, new Type[]{new Type(TypeRepository.this, po)});
                                    }
                                }
                                return null;
                        }
                    });
                } catch (IOException ex) {
                    //TODO: any logging?
                }
            }
        }
    }
    private void fillTypeElement(Class<?> type){
        types.put(type.getName(), new Type[]{new Type(TypeRepository.this, type)});
    }
    
    AnnotationModelHelper getAnnotationModelHelper() {
        if(amh == null) {
                Sources sources=ProjectUtils.getSources(project);
                SourceGroup groups[]=sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                if(groups != null && groups.length>0){
                    SourceGroup firstGroup=groups[0];
                    FileObject fo=firstGroup.getRootFolder();
                    ClasspathInfo classpathInfo = ClasspathInfo.create(fo);
                    amh = AnnotationModelHelper.create(classpathInfo);
                }            
        }
        return amh;
    }
    
    boolean isValid(){
        return mtp.isValid();
    }
}
