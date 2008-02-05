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
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasServices;
import org.netbeans.modules.websvc.saas.model.wadl.Application;
import org.netbeans.modules.websvc.saas.model.wadl.Resource;
import org.netbeans.modules.websvc.saas.util.SaasUtil;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author nam
 */
public class WadlSaas extends Saas {

    private Application wadlModel;
    
    public WadlSaas(SaasGroup parentGroup, SaasServices services) {
        super(parentGroup, services);
    }
    
    public Application getWadlModel() throws IOException {
        if (wadlModel == null) {
            wadlModel = SaasUtil.loadWadl(getLocalWadlFile());
        }
        return wadlModel;
    }
    
    public List<Resource> getResources() {
        try {
            return getWadlModel().getResources().getResource();
        } catch(Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.EMPTY_LIST;
    } 
    
    public FileObject getLocalWadlFile() {
        //TODO
        return null;
    }
    
    /**
     * Returns either a list of resources defined by associated WADL model or
     * a list of filtered resource methods.
     * @return
     */
    public List getResourcesOrMethods() {
        if (getMethods() != null && getMethods().size() > 0) {
            return getMethods();
        }
        try {
            return getWadlModel().getResources().getResource();
        } catch(Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.EMPTY_LIST;
    }
}
