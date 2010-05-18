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

/*
 * WebServiceNodeManager.java
 *
 * Created on August 3, 2005, 1:36 PM
 *
 */
package org.netbeans.modules.mobility.end2end.util;

import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.api.client.WebServicesClientView;
//import org.netbeans.modules.websvc.core.ServiceInformation;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientView;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

/**
 *
 * @author suchys
 */
public final class WebServiceNodeManager {
    
    
    public static Node getAvailableWSRootNode(final Project p, final String wsdlFile){
        final WebServicesClientSupport wscs = WebServicesClientSupport.getWebServicesClientSupport(p.getProjectDirectory());
        final FileObject rootFolder = wscs.getWsdlFolder();
        final JAXWSClientSupport clientSupport = JAXWSClientSupport.getJaxWsClientSupport( p.getProjectDirectory());
//        final WebServicesClientSupport clientSupport  = WebServicesClientSupport.getWebServicesClientSupport(rootFolder);
        final JAXWSClientView clientView = JAXWSClientView.getJAXWSClientView();
//        final WebServicesClientView clientView = WebServicesClientView.getWebServicesClientView(rootFolder);
        
        final Node clientRoot = clientView.createJAXWSClientView( p );
        return new FilterNode(clientRoot, new WsFilteredChildren( clientRoot, wsdlFile ));
    }
    
    private static class WsFilteredChildren extends FilterNode.Children{
        final private String name;
        
        public WsFilteredChildren(Node original, String name){
            super(original);
            this.name = name;
        }
        
        /** Create nodes representing copies of the original node's children.
         * The default implementation returns exactly one representative for each original node,
         * as returned by {@link #copyNode}.
         * Subclasses may override this to avoid displaying a copy of an original child at all,
         * or even to display multiple nodes representing the original.
         * @param key the original child node
         * @return zero or more nodes representing the original child node
         */
        protected Node[] createNodes(final Node n) {
//            final ServiceInformation si = (ServiceInformation)n.getCookie(ServiceInformation.class);
//            if (si != null){
//                if (name.equals(((DataObject)n.getCookie(DataObject.class)).getPrimaryFile().getNameExt())){
//                    // is run under read access lock so nobody can change children
//                    return new Node[] { copyNode(n) };
//                }
//            }
            return new Node[0];
        }
        
    }
}
