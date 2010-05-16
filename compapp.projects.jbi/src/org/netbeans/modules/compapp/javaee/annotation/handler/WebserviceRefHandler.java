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

/*
 * WebserviceRefHandler.java
 *
 * Created on October 2, 2006, 12:21 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.compapp.javaee.annotation.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.netbeans.modules.classfile.Annotation;
import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.classfile.ClassName;
import org.netbeans.modules.classfile.Variable;
import org.netbeans.modules.compapp.javaee.codegen.model.Endpoint;

/**
 *
 * @author gpatil
 */
public class WebserviceRefHandler implements AnnotationHandler {
    private List<Endpoint> endPoints = new ArrayList<Endpoint>();
    private static Logger logger = Logger.getLogger(WebserviceRefHandler.class.getName());
    
    public WebserviceRefHandler() {
    }
    
    public String getAnnotationClassConstant(){
        return ANNO_WEB_SERVICE_REF ;
    }
    
    public void handle(ClassFileLoader cl, ClassFile theClass) {
        Collection<Variable> fields = theClass.getVariables();
        ClassFile typeClass = null;
        Annotation anno = null;
        
        if ((fields != null) && (fields.size() > 0)){
            for(Variable field : fields){
                anno = field.getAnnotation(ClassName.getClassName(ANNO_WEB_SERVICE_REF));
                if (anno != null){
                    String type = field.getDescriptor();
                    typeClass = cl.getClassFileFromInternalName(type);
                    processWebserviceClient(cl, typeClass);
                }
            }
        }
    }
    
    public void processWebserviceClient(ClassFileLoader cl, ClassFile theClass){
        Annotation annoWsc = theClass.getAnnotation(ClassName.getClassName(ANNO_WEBSERVICE_CLIENT));
        Annotation annoWep =  null;
        if (annoWsc != null){
            NBAnnonationWrapper wsc = new NBAnnonationWrapper(annoWsc);
            //WebEndpoint
            Collection<org.netbeans.modules.classfile.Method> mthds = theClass.getMethods();
            if ((mthds != null) && (mthds.size() > 0)){
                for(org.netbeans.modules.classfile.Method method: mthds){
                    annoWep= method.getAnnotation(ClassName.getClassName(ANNO_WEB_ENDPOINT));
                    if (annoWep != null){
                        NBAnnonationWrapper wep = new NBAnnonationWrapper(annoWep);
                        QName serviceName = new QName(wsc.getStringValue(PROP_TNS), wsc.getStringValue(PROP_NAME));
                        String returnType = method.getReturnType();
                        ClassFile returnClass = cl.getClassFileFromInternalName(returnType);
                        QName portType = getPortType(returnClass);
                        Endpoint ep = new Endpoint(Endpoint.EndPointType.Consumer, wep.getStringValue(PROP_NAME),
                                (portType == null) ? null : portType, serviceName);
                        this.endPoints.add(ep);
                    }
                }
            }
        }
    }
    
    private QName getPortType(ClassFile cls){
        QName ret = null;
        Annotation annoWs = cls.getAnnotation(ClassName.getClassName(ANNO_WEB_SERVICE));
        if ( annoWs != null){
            NBAnnonationWrapper ws = new NBAnnonationWrapper(annoWs);
            ret = new QName(ws.getStringValue(PROP_TNS), ws.getStringValue(PROP_NAME)) ;
        }
        return ret;
    }
    
    public List<Endpoint> getEndPoints(){
        return this.endPoints;
    }
    
    public void resetEndPoints(){
        this.endPoints.clear();
    }
}
