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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.web.client.rest.wizard;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;
import org.openide.filesystems.FileObject;


/**
 * @author ads
 *
 */
class JSClientGenerator {
    
    private static final Logger LOG = Logger.getLogger( JSClientGenerator.class.getName()); 
    
    private JSClientGenerator(RestServiceDescription description){
        myDescription = description;
    }

    static JSClientGenerator create( RestServiceDescription description )
    {
        return new JSClientGenerator(description);
    }

    public void generate( FileObject jsFile) {
        FileObject restSource = myDescription.getFile();
        if ( restSource == null ){
            return;
        }
        StringBuilder builder = new StringBuilder("$(function(){\n");
        JavaSource javaSource = JavaSource.forFileObject( restSource);
        Task<CompilationController> task = new Task<CompilationController>(){

            @Override
            public void run( CompilationController controller ) throws Exception {
                TypeElement restResource = controller.getElements().getTypeElement( 
                        myDescription.getClassName());
                List<ExecutableElement> methods = ElementFilter.methodsIn(
                        restResource.getEnclosedElements());
                for (ExecutableElement method : methods) {
                    List<? extends AnnotationMirror> annotations = 
                        method.getAnnotationMirrors();
                }
            }
            
        };
        try {
            Future<Void> future = javaSource.runWhenScanFinished( task, true);
            future.get();
        }
        catch (IOException e) {
            LOG.log(Level.INFO , null ,e );
        }
        catch (InterruptedException e) {
            LOG.log(Level.INFO , null ,e );
        }
        catch (ExecutionException e) {
            LOG.log(Level.INFO , null ,e );
        }
        builder.append("});");
    }
    
    private AnnotationMirror getAnnotion( List<? extends AnnotationMirror> annotations, 
            String annotation )
    {
        for (AnnotationMirror annotationMirror : annotations) {
            Element annotationElement = annotationMirror.getAnnotationType().asElement();
            if ( annotationElement instanceof TypeElement){
                TypeElement annotationDecl = (TypeElement) annotationElement;
                if ( annotationDecl.getQualifiedName().contentEquals( annotation)){
                    return annotationMirror;
                }
            }
        }
        return null;
    }
    
    private  AnnotationMirror getAnnotion( Element element, String annotation )
    {
        List<? extends AnnotationMirror> annotations = element.getAnnotationMirrors();
        return getAnnotion(annotations, annotation);
    }
    
    private RestServiceDescription myDescription;

}
