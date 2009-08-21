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

package org.netbeans.modules.web.jsf.api.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean;
import org.netbeans.modules.web.jsf.api.metamodel.JsfModel;
import org.netbeans.modules.web.jsf.api.metamodel.JsfModelFactory;

/**
 *
 * @author Petr Pisl
 * @author ads
 */
public class JSFBeanCache {
    
    public static List<FacesManagedBean> getBeans(WebModule webModule) {
        final List<FacesManagedBean> beans = new ArrayList<FacesManagedBean>();
        /* Old implementation based on several models over faces-config.xml files.
         * 
         * FileObject[] files = null; 
        
        
        if (webModule != null) {
            files = ConfigurationUtils.getFacesConfigFiles(webModule);
        }
        
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                    JSFConfigModel model = ConfigurationUtils.getConfigModel(files[i], true);
                    if (model != null) {
                        FacesConfig facesConfig = model.getRootComponent();
                        if (facesConfig != null) {
                            Collection<ManagedBean> managedBeans = facesConfig.getManagedBeans();
                            for (Iterator<ManagedBean> it = managedBeans.iterator(); it.hasNext();) {
                                beans.add(it.next());   
                            }
                        }
                    }
            }
        }*/
        MetadataModel<JsfModel> model = JsfModelFactory.getModel( webModule );
        if ( model == null){
            return beans;
        }
        try {
            model.runReadAction( new MetadataModelAction<JsfModel, Void>() {

                public Void run( JsfModel model ) throws Exception {
                    List<FacesManagedBean> managedBeans = model.getElements( 
                            FacesManagedBean.class);
                    beans.addAll( managedBeans );
                    return null;
                }
            });
        }
        catch (MetadataModelException e) {
            LOG.log( Level.WARNING , e.getMessage(), e );
        }
        catch (IOException e) {
            LOG.log( Level.WARNING , e.getMessage(), e );
        }
        return beans;
    }
    
    private static final Logger LOG = Logger.getLogger( 
            JSFBeanCache.class.getCanonicalName() );
}
