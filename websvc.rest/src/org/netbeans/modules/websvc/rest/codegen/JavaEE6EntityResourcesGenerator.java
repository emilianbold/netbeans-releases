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

package org.netbeans.modules.websvc.rest.codegen;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.websvc.rest.RestUtils;
import org.netbeans.modules.websvc.rest.codegen.model.EntityResourceBean;
import org.netbeans.modules.websvc.rest.support.PersistenceHelper;
import org.netbeans.modules.websvc.rest.support.SourceGroupSupport;
import org.netbeans.modules.websvc.rest.wizard.Util;
import org.openide.filesystems.FileObject;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ModifiersTree;

/**
 *
 * @author ads
 */
public class JavaEE6EntityResourcesGenerator extends EntityResourcesGenerator {
    
    private static final String XMLROOT_ANNOTATION = 
        "javax.xml.bind.annotation.XmlRootElement";         // NOI18N
    
    @Override
    public Set<FileObject> generate(ProgressHandle pHandle) throws IOException {
        if (pHandle != null) {
            initProgressReporting(pHandle);
        }

        createFolders( false);

        //Make necessary changes to the persistence.xml
        new PersistenceHelper(project).configure(model.getBuilder().getAllEntityNames(),
                !RestUtils.hasJTASupport(project));
        
        Collection<EntityResourceBean> resourceBeans = model.getResourceBeans();
        Set<Entity> entities = new HashSet<Entity>();
        for (EntityResourceBean entityResourceBean : resourceBeans) {
            Entity entity = entityResourceBean.getEntityClassInfo().getEntity();
            entities.add( entity );
            modifyEntity( entity );
        }
        
        FileObject targetResourceFolder = null;
        SourceGroup[] sourceGroups = SourceGroupSupport.getJavaSourceGroups(project);
        SourceGroup targetSourceGroup = SourceGroupSupport.findSourceGroupForFile(
                sourceGroups, targetFolder);
        if (targetSourceGroup != null) {
            targetResourceFolder = SourceGroupSupport.getFolderForPackage(
                    targetSourceGroup, resourcePackageName, true);
        }
        if (targetResourceFolder == null) {
            targetResourceFolder = targetFolder;
        }
        
        Util.generateRESTFacades(project, entities, model, targetResourceFolder, 
                resourcePackageName);
        
        finishProgressReporting();

        return new HashSet<FileObject>();
    }

    private void modifyEntity( final Entity entity ) {
        try {
            FileObject entityFileObject = SourceGroupSupport.
                getFileObjectFromClassName(entity.getClass2(), project);

            if (entityFileObject == null) {
                return;
            }
            JavaSource javaSource = JavaSource.forFileObject(entityFileObject);
            if (javaSource == null) {
                return;
            }
            ModificationResult result = javaSource
                    .runModificationTask(new Task<WorkingCopy>() {

                        public void run( final WorkingCopy working )
                                throws IOException
                        {
                            working.toPhase(Phase.RESOLVED);

                            TreeMaker make = working.getTreeMaker();
                            
                            if (working.getElements().getTypeElement(
                                    XMLROOT_ANNOTATION) == null )
                            {
                                return;
                            }
                            
                            TypeElement entityElement = 
                                working.getTopLevelElements().get(0);
                            List<? extends AnnotationMirror> annotationMirrors = 
                                working.getElements().getAllAnnotationMirrors(
                                        entityElement);
                            boolean hasXmlRootAnnotation = false;
                            for (AnnotationMirror annotationMirror : annotationMirrors)
                            {
                                DeclaredType type = annotationMirror.getAnnotationType();
                                Element annotationElement = type.asElement();
                                if ( annotationElement instanceof TypeElement ){
                                    Name annotationName = ((TypeElement)annotationElement).
                                        getQualifiedName();
                                    if ( annotationName.contentEquals(XMLROOT_ANNOTATION))
                                    {
                                        hasXmlRootAnnotation = true;
                                    }
                                }
                            }
                            if ( !hasXmlRootAnnotation ){
                                ClassTree classTree = working.getTrees().getTree(
                                        entityElement);
                                GenerationUtils genUtils = GenerationUtils.
                                    newInstance(working);
                                ModifiersTree modifiersTree = make.addModifiersAnnotation(
                                        classTree.getModifiers(),
                                        genUtils.createAnnotation(XMLROOT_ANNOTATION));

                                working.rewrite( classTree.getModifiers(), 
                                        modifiersTree);
                            }
                        }
                    });
            result.commit();
        }
        catch (IOException e) {
            Logger.getLogger(JavaEE6EntityResourcesGenerator.class.getName()).
                log( Level.SEVERE, null, e);
        }
    }

}
