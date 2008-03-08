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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.netbeans.modules.websvc.saas.model.jaxb.Method;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasServices;
import org.netbeans.modules.websvc.saas.model.wadl.Application;
import org.netbeans.modules.websvc.saas.model.wadl.Resource;
import org.netbeans.modules.websvc.saas.util.SaasUtil;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author nam
 */
public class WadlSaas extends Saas {

    private Application wadlModel;
    private List<WadlSaasResource> resources;
    private FileObject wadlFile;
    
    public WadlSaas(SaasGroup parentGroup, SaasServices services) {
        super(parentGroup, services);
    }
    
    public WadlSaas(SaasGroup parent, String url, String displayName, String packageName) {
        super(parent, url, displayName, packageName);
    }
    
    public Application getWadlModel() throws IOException {
        if (wadlModel == null) {
            InputStream in = null;
            if (isUserDefined() ) {
                if (wadlFile == null) {
                    wadlFile = SaasUtil.retrieveWadlFile(this);
                }
                if (wadlFile != null) {
                    in = wadlFile.getInputStream();
                }
            } else {
                in = Thread.currentThread().getContextClassLoader().getResourceAsStream(getUrl());
            }
            
            try {
                if (in != null) {
                    wadlModel = SaasUtil.loadWadl(in);
                }
            } catch (JAXBException ex) {
                String msg = NbBundle.getMessage(WadlSaas.class, "MSG_ErrorLoadingWadl", getUrl());
                IOException ioe = new IOException(msg);
                ioe.initCause(ex);
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        }
        return wadlModel;
    }
    
    public List<WadlSaasResource> getResources() {
        if (resources == null) {
            resources = new ArrayList<WadlSaasResource>();
            try {
                for (Resource r : getWadlModel().getResources().getResource()) {
                    resources.add(new WadlSaasResource(this, null, r));
                }
            } catch(Exception ex) {
                Exceptions.printStackTrace(ex);
                return Collections.EMPTY_LIST;
            }
        }
        return Collections.unmodifiableList(resources);
    } 
    
    public FileObject getLocalWadlFile() {
        if (wadlFile == null) {
            try {
                wadlFile = SaasUtil.extractWadlFile(this);
            } catch(IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        return wadlFile;
    }
    
    @Override
    protected WadlSaasMethod createSaasMethod(Method m) {
        return new WadlSaasMethod(this, m);
    }
    
    @Override
    public void toStateReady(boolean synchronous) {
        if (wadlModel == null) {
            if (synchronous) {
                toStateReady();
            } else {
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        toStateReady();
                    }
                });
            }
        }
    }
    
    private void toStateReady() {
        try {
            getWadlModel();
            setState(State.READY);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
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
        return getResources();
    }

    public String getBaseURL() {
        try {
            return getWadlModel().getResources().getBase();
        } catch(IOException ioe) {
            // should not happen at this point
            return NbBundle.getMessage(WadlSaas.class, "LBL_BAD_WADL");
        }
    }
}
