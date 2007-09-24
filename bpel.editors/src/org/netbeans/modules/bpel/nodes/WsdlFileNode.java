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
package org.netbeans.modules.bpel.nodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.properties.ResolverUtility;
import org.netbeans.modules.soa.ui.SoaUiUtil;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * @author ads
 *
 */
public class WsdlFileNode extends BpelNode<WSDLModel> {
    
    public WsdlFileNode(WSDLModel wsdlModel, Lookup lookup) {
        super(wsdlModel, new MessageTypeChildren(wsdlModel, lookup), lookup);
    }
    
    public WsdlFileNode(WSDLModel wsdlModel, Children children,  Lookup lookup) {
        super(wsdlModel, children, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.WSDL_FILE;
    }
    
    protected String getNameImpl() {
        WSDLModel ref = getReference();
        if (ref == null) {
            return null;
        }
        FileObject fo = ref.getModelSource().getLookup().lookup(FileObject.class);
        if (fo == null || !fo.isValid()) {
            return null;
        }
        BpelModel bpelModel = getLookup().lookup(BpelModel.class);
        Project modelProject = ResolverUtility.safeGetProject(bpelModel);
        String relativePath = ResolverUtility.safeGetRelativePath(fo, modelProject);
        
        return relativePath != null ? relativePath : fo.getPath();
    }
    
    protected String getImplHtmlDisplayName() {
        return SoaUiUtil.getGrayString(super.getImplHtmlDisplayName());
    }
    
    static class MessageTypeChildren extends Children.Keys {
        
        private Lookup myLookup;
        
        @SuppressWarnings("unchecked")
        public MessageTypeChildren(WSDLModel wsdlModel, Lookup lookup) {
            myLookup = lookup;
            setKeys(new Object[] {wsdlModel});
        }
        
        /* (non-Javadoc)
         * @see org.openide.nodes.Children.Keys#createNodes(java.lang.Object)
         */
        protected Node[] createNodes( Object key ) {
            if (key instanceof WSDLModel){
                List<Node> list = new ArrayList<Node>();
                Collection<Message> messages =
                        ((WSDLModel)key).getDefinitions().getMessages();
                for (Message message : messages) {
                    Node newNode = new MessageTypeNode(message, myLookup);
                    list.add(newNode);
                }
                //
                return list.toArray(new Node[list.size()]);
            }
            return null;
        }
    }
}
