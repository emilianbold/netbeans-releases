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

package org.netbeans.modules.compapp.javaee.sunresources.tool.archive;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.compapp.javaee.sunresources.generated.ejb21.*;
import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.CMapNode;
import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.EJBDepend;
import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.EJBInterface;
import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.EJBNode;
import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.MDBNode;
import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.ResourceDepend;
import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.CMapNode.CMapNodeType;
import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.EJBInterface.EJBInterfaceType;
import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.ResourceNode.ResourceType;
import org.openide.util.NbBundle;

/**
 * @author echou
 *
 */
public class EjbDDJaxbHandler {

    // sun-ejb-jar.xml handle
    private SunEjbDDJaxbHandler sunEjbDD;
    // webservices.xml handle
    private WebservicesDDJaxbHandler webservicesDD;
    private EjbJarType root;
    
    private ArrayList<CMapNode> nodes = new ArrayList<CMapNode> ();
    
    public EjbDDJaxbHandler(Object root, SunEjbDDJaxbHandler sunEjbDD, 
            WebservicesDDJaxbHandler webservicesDD) throws Exception {
        this.sunEjbDD = sunEjbDD;
        this.webservicesDD = webservicesDD;
        if (root instanceof EjbJarType) {
            this.root = (EjbJarType) root;
        } else {
            throw new Exception(
                    NbBundle.getMessage(EjbDDJaxbHandler.class, "EXC_bad_jaxbroot", root.getClass()));
        }
        init();
    }
    
    private void init() {
        // iterator through all the session, mdb, entity beans
        for (Iterator<Object> i = root.getEnterpriseBeans().getSessionOrEntityOrMessageDriven().iterator();
            i.hasNext(); ) {
            Object obj = i.next();
            
            if (obj instanceof SessionBeanType) {  // Session Beans
                SessionBeanType sb = (SessionBeanType) obj;
                EJBNode node = new EJBNode();
                
                // check session type
                if ("Stateful".equals(sb.getSessionType().getValue())) { // NOI18N
                    node.setType(CMapNodeType.STATEFUL);
                } else if ("Stateless".equals(sb.getSessionType().getValue())) { // NOI18N
                    node.setType(CMapNodeType.STATELESS);
                }
                // check ejb name
                node.setLogicalName(sb.getEjbName().getValue());
                // check ejb class
                node.setNodeClass(sb.getEjbClass().getValue());
                // check local intf exists
                if (sb.getLocal() != null) {
                    node.addImplementedIntfs(new EJBInterface(
                        sb.getLocal().getValue(), EJBInterfaceType.LOCAL));
                }
                // check remote intf exists
                if (sb.getRemote() != null) {
                    node.addImplementedIntfs(new EJBInterface(
                            sb.getRemote().getValue(), EJBInterfaceType.REMOTE));
                }
                // check ejb local refs
                checkEjbLocalRefs(node, sb.getEjbLocalRef());                    
                // check ejb refs
                checkEjbRefs(node, sb.getEjbRef());
                // check resource refs
                checkResourceRefs(node, sb.getResourceRef());
                // check service refs
                checkServiceRefs(node, sb.getServiceRef());
                // check message destination refs
                checkMsgDestRefs(node, sb.getMessageDestinationRef());
                // check service endpoint exists
                if (sb.getServiceEndpoint() != null) {
                    node.setIsWebService(true);
                    java.lang.String serviceEndpoint = sb.getServiceEndpoint().getValue();
                    node.getProps().setProperty("endpointInterface", serviceEndpoint); // NOI18N
                    // resolve using sunEjbDD
                    if (sunEjbDD != null) {
                        sunEjbDD.resolveWebservice(node, node.getLogicalName(), serviceEndpoint, webservicesDD);
                    }
                }
                
                nodes.add(node);
                
            } else if (obj instanceof MessageDrivenBeanType) {  // MDBs
                MessageDrivenBeanType mdb = (MessageDrivenBeanType) obj;
                MDBNode node = new MDBNode();
                node.setType(CMapNodeType.MDB);
                // check ejb name
                node.setLogicalName(mdb.getEjbName().getValue());
                // check ejb class
                node.setNodeClass(mdb.getEjbClass().getValue());
                // check ejb local refs
                checkEjbLocalRefs(node, mdb.getEjbLocalRef()); 
                // check ejb refs
                checkEjbRefs(node, mdb.getEjbRef());
                // check resource refs
                checkResourceRefs(node, mdb.getResourceRef());
                // check service refs
                checkServiceRefs(node, mdb.getServiceRef());
                // check message destination refs
                checkMsgDestRefs(node, mdb.getMessageDestinationRef());
                // check activation config
                if (mdb.getActivationConfig() != null) {
                    for (Iterator<ActivationConfigPropertyType> acIter = 
                        mdb.getActivationConfig().getActivationConfigProperty().iterator();
                        acIter.hasNext(); ) {
                        ActivationConfigPropertyType acProp = acIter.next();
                        node.getActivationConfig().setProperty(
                                acProp.getActivationConfigPropertyName().getValue(), 
                                acProp.getActivationConfigPropertyValue().getValue());
                    }
                }
                
                // resolve using sunEjbDD
                if (sunEjbDD != null) {
                    node.setMappedName(sunEjbDD.findJndiByEjbName(node.getLogicalName()));
                }
                
                nodes.add(node);
                
            } else if (obj instanceof EntityBeanType) {
                // ignore entity beans for now
            }
        }
    }
    
    // check ejb local refs
    private void checkEjbLocalRefs(CMapNode node, List<EjbLocalRefType> localRefs) {
        for (Iterator<EjbLocalRefType> lrefIter = localRefs.iterator(); 
            lrefIter.hasNext(); ) {
            EjbLocalRefType localRef = lrefIter.next();
            EJBDepend ejbDepend = new EJBDepend(node);
            ejbDepend.setTargetIntfName(localRef.getLocal().getValue());
            node.getEjbDepends().add(ejbDepend);
        }
    }
    
    // check ejb refs
    private void checkEjbRefs(CMapNode node, List<EjbRefType> refs) {
        for (Iterator<EjbRefType> refIter = refs.iterator(); 
            refIter.hasNext(); ) {
            EjbRefType ref = refIter.next();
            EJBDepend ejbDepend = new EJBDepend(node);
            ejbDepend.setTargetIntfName(ref.getRemote().getValue());
            node.getEjbDepends().add(ejbDepend);
        }
    }
    
    // check resource refs
    private void checkResourceRefs(CMapNode node, List<ResourceRefType> resRefs) {
        for (Iterator<ResourceRefType> resRefIter = resRefs.iterator();
            resRefIter.hasNext(); ) {
            ResourceRefType resRef = resRefIter.next();
            java.lang.String resRefName = resRef.getResRefName().getValue();
            java.lang.String resType = resRef.getResType().getValue();
            if (ignoreResType(resType)) {
                // if this resource ref points to jms connection factory
                // we need to ignore them, since we want the destination
                // instead
                continue;
            }
            ResourceDepend resDepend = new ResourceDepend(node);
            resDepend.setType(ResourceType.OTHER);
            resDepend.setTargetResType(resType);
            // resolve using sunEjbDD
            if (sunEjbDD != null) {
                java.lang.String jndi = sunEjbDD.resolveResRef(node.getLogicalName(), resRefName);
                resDepend.setMappedName(jndi);
                resDepend.setTargetResJndiName(jndi);
            } else  {
                resDepend.setMappedName(resRefName);
            }
            node.getResDepends().add(resDepend);
        }
    }
    
    // check service refs
    private void checkServiceRefs(CMapNode node, List<ServiceRefType> serviceRefs) {
        // TODO
    }
    
    // check message destination refs
    private void checkMsgDestRefs(CMapNode node, List<MessageDestinationRefType> msgDestRefs) {
        for (Iterator<MessageDestinationRefType> msgDestRefIter = msgDestRefs.iterator();
            msgDestRefIter.hasNext(); ) {
            MessageDestinationRefType msgDestRef = msgDestRefIter.next();
            java.lang.String msgDestRefName = msgDestRef.getMessageDestinationRefName().getValue();
            java.lang.String msgDestRefType = msgDestRef.getMessageDestinationType().getValue();
            java.lang.String msgDestRefLink = msgDestRef.getMessageDestinationLink().getValue();
            ResourceDepend resDepend = new ResourceDepend(node);
            resDepend.setType(ResourceType.JMS);
            resDepend.setTargetResType(msgDestRefType);
            // resolve using sunEjbDD
            if (sunEjbDD != null) {
                java.lang.String jndi = sunEjbDD.resolveMsgDestRef(node.getLogicalName(), msgDestRefLink);
                resDepend.setMappedName(jndi);
                resDepend.setTargetResJndiName(jndi);
            } else {
                resDepend.setMappedName(msgDestRefName);
            }
            node.getResDepends().add(resDepend);
        }
    }
    
    private boolean ignoreResType(java.lang.String resType) {
        if (resType.equals("javax.jms.ConnectionFactory") || // NOI18N
            resType.equals("javax.jms.QueueConnectionFactory") || // NOI18N
            resType.equals("javax.jms.TopicConnectionFactory") // NOI18N
        ) {
            return true;
        } else {
            return false;
        }
    }

    public ArrayList<CMapNode> getNodes() {
        return this.nodes;
    }
}
