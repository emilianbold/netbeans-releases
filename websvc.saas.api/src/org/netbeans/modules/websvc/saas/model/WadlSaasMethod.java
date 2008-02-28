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

package org.netbeans.modules.websvc.saas.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.websvc.saas.model.jaxb.Method;
import org.netbeans.modules.websvc.saas.model.wadl.Application;
import org.netbeans.modules.websvc.saas.model.wadl.Resource;
import org.netbeans.modules.websvc.saas.model.wadl.Resources;
import org.netbeans.modules.websvc.saas.util.SaasUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author nam
 */
public class WadlSaasMethod extends SaasMethod {
    private Resource[] path;
    private WadlSaasResource parent;
    private org.netbeans.modules.websvc.saas.model.wadl.Method wadlMethod;

    public WadlSaasMethod(WadlSaas wadlSaas, Method method) {
        super(wadlSaas, method);
    }
    
    public WadlSaasMethod(WadlSaasResource parent, org.netbeans.modules.websvc.saas.model.wadl.Method wadlMethod) {
        this(parent.getSaas(), (Method) null);
        this.parent = parent;
        this.wadlMethod = wadlMethod;
    }

    public WadlSaas getSaas() {
        return (WadlSaas) super.getSaas();
    }
    
    public WadlSaasResource getParentResource() {
        return parent;
    }
    
    public Resource[] getResourcePath() {
        if (path == null || path.length == 0) {
            ArrayList<Resource> result = new ArrayList<Resource>();
            try {
                Application app = this.getSaas().getWadlModel();
                Resources rs = app.getResources();
                for (Resource r : rs.getResource()) {
                    findPathToMethod(r, result);
                }
            } catch (IOException ex) {
            }
//            WadlSaasResource current = getParentResource();
//            while (current != null) {
//                result.add(0, current.getResource());
//                current = current.getParent();
//            }
            path = result.toArray(new Resource[result.size()]);
        }
        return path;
    }

    private void findPathToMethod(Resource current, List<Resource> resultPath) {
        if (current.getMethodOrResource().contains(getWadlMethod())) {
            resultPath.add(current);
            return;
        }

        for (Object o : current.getMethodOrResource()) {
            if (o instanceof Resource) {
                findPathToMethod((Resource) o, resultPath);
                if (resultPath.size() > 0) {
                    break;
                }
            }
        }

        resultPath.add(0, current);
    }
    
    public org.netbeans.modules.websvc.saas.model.wadl.Method getWadlMethod() {
        if (wadlMethod == null) {
            if (getHref() != null && getHref().length() > 0) {
                try {
                    if (getHref().charAt(0) == '/') {
                        wadlMethod = SaasUtil.wadlMethodFromXPath(getSaas().getWadlModel(), getHref());
                    } else {
                        wadlMethod = SaasUtil.wadlMethodFromIdRef(getSaas().getWadlModel(), getHref());
                    }
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe); 
                }
            } else {
                throw new IllegalArgumentException("Element method " + getName() + " should define attribute 'href'");
            }
        }
        return wadlMethod;
    }
    
}
