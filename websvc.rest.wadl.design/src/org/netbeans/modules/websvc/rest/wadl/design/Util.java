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

package org.netbeans.modules.websvc.rest.wadl.design;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.websvc.rest.wadl.model.*;

/**
 * Util
 *
 * @author Ayub Khan
 */
public class Util {

    public static void addResource(String serviceUrl, WadlModel model) {
        Resources resources = model.getApplication().getResources().iterator().next();
        String serviceBase = resources.getBase();
        if(serviceUrl.startsWith(serviceBase)) {
            String params = "";
            int ndx = serviceUrl.indexOf("?");
            if(ndx != -1) {
                params = serviceUrl.substring(ndx+1);
                serviceUrl = serviceUrl.substring(0, ndx);
            }
            String[] paths = serviceUrl.substring(serviceBase.length()).split("/");
            List<Resource> rList = new ArrayList<Resource>();
            getResourceRecursively(resources, paths, 0, rList);
            if(paths.length >= rList.size()) {
                model.startTransaction();
                WadlComponent parent = resources;
                for(int i=rList.size();i<paths.length;i++) {
                    Resource r = createNewResource(paths[i], MethodType.GET, params.split("&"), model);
                    if(parent instanceof Resources)
                        ((Resources)parent).addResource(r);
                    else
                        ((Resource)parent).addResource(r);
                    parent = r;
                }
                model.endTransaction();
            }
        }
    }
    
    public static Resource createNewResource(String path, MethodType methodType, String[] params, WadlModel model) {
        Resource r = model.getFactory().createResource();
        r.setPath(path);
        
        Method m = model.getFactory().createMethod();
        m.setName(methodType.value());
        r.addMethod(m);
        
        Request request = model.getFactory().createRequest();
        for(String p:params) {
            if(p.equals("")) continue;
            String[] ps = p.split("=");
            Param param = model.getFactory().createParam();
            param = model.getFactory().createParam();
            param.setName(ps[0]);
            param.setType(new QName(WadlModel.XML_SCHEMA_NS, "string", 
                    model.getApplication().getSchemaNamespacePrefix()));
            param.setStyle(ParamStyle.QUERY.value());
            if(ps.length>1)
                param.setDefault(ps[1]);
            request.addParam(param);
        }
        m.addRequest(request);
        
        Response response = model.getFactory().createResponse();
        Representation rep2 = model.getFactory().createRepresentation();
        rep2.setMediaType(MediaType.XML.value());
        response.addRepresentation(rep2);
        m.addResponse(response);
        
        return r;
    }

    public static void getResourceRecursively(Resources resources, String[] paths, int currIndex, List<Resource> rList) {
        for(Resource child:resources.getResource()) {
            if(!child.getPath().equals(paths[currIndex]))
                return;
            rList.add(child);
            getResourceRecursively(child, paths, currIndex++, rList);
        }
    }
    
    public static void getResourceRecursively(Resource resource, String[] paths, int currIndex, List<Resource> rList) {
        for(Resource child:resource.getResource()) {
            if(!child.getPath().equals(paths[currIndex]))
                return;
            rList.add(child);
            getResourceRecursively(child, paths, currIndex++, rList);
        }
    }
    
    public static String generateUniqueName(String name, List<String> existingNames) {
        List<Integer> numberSuffixes = new ArrayList<Integer>();
        for (String eName : existingNames) {
            if (!name.equals(eName) && eName.startsWith(name)) {
                String suffix = eName.substring(name.length());
                if (isNumber(suffix)) {
                    numberSuffixes.add(Integer.parseInt(suffix));
                }
            }
        }
        Collections.sort(numberSuffixes);
        String result = name;
        if (numberSuffixes.size() > 0) {
            int newSuffix = numberSuffixes.get(numberSuffixes.size() - 1) + 1;
            result = name + newSuffix;
        } else if (existingNames.size() > 0) {
            result = name + 1;
        }
        return result;
    }

    public static boolean isNumber(String value) {
        for (char character : value.toCharArray()) {
            if (!Character.isDigit(character)) {
                return false;
            }
        }
        return true;
    }
}
