/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.xslt.tmap.model.api;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xslt.tmap.TMapConstants;
import org.netbeans.modules.xslt.tmap.model.api.completion.TMapCompletionUtil;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author Alex Petrov (03.07.2008)
 */
public class WsdlDataHolder implements TMapConstants {
    private TMapModel tmapModel;
    private WSDLModel wsdlModel;
    private String namespace, location, qnamePrefix;

    protected WsdlDataHolder(TMapModel tmapModel) {
        this.tmapModel = tmapModel;
    }

    public String getQNamePrefix() {
        if (qnamePrefix == null) {
            try {
                TransformMap transformMap = tmapModel.getTransformMap();
                qnamePrefix = transformMap.getNamespaceContext().getPrefix(
                    getNamespace());
                if (qnamePrefix == null) { // maybe 2 equal prefixes are defined
                    NamedNodeMap nodeMap = transformMap.getPeer().getAttributes();
                    for (int i = 0; i < nodeMap.getLength(); ++i) {
                        Node attribute = nodeMap.item(i);
                        String attrValue = attribute.getNodeValue();
                        if (! attrValue.equals(getNamespace())) continue;
                            
                        String attrName = attribute.getNodeName();
                        if (! attrName.startsWith(TRANSFORMMAP_ATTRIBUTE_NAMESPACE_PREFIX))
                            continue;

                        int index = attrName.indexOf(COLON);
                        qnamePrefix  = ((index < 0) || (attrName.endsWith(COLON))) ? 
                            "" : attrName.substring(index + 1);
                        break;
                    }
                }
            } catch(Exception e) {
                TMapCompletionUtil.logExceptionInfo(e);
            }
        }
        return qnamePrefix;
    }
    public String getNamespace() {
        if (namespace == null) {
            try {
                namespace = wsdlModel.getDefinitions().getTargetNamespace();
            } catch(Exception e) {
                TMapCompletionUtil.logExceptionInfo(e);
            }
        }
        return namespace;
    }
    public TMapModel getTmapModel() {return tmapModel;}
    public WSDLModel getWsdlModel() {return wsdlModel;}
    public String getLocation() {return location;}
    
    public static List<WsdlDataHolder> getImportedWsdlList(TMapModel tmapModel) {
        if (tmapModel == null) return null;
        try {
            List<Import> tmapImports = tmapModel.getTransformMap().getImports();
            List<WsdlDataHolder> wsdlHolders = new ArrayList<WsdlDataHolder>(
                tmapImports.size());
            for (Import tmapImport : tmapImports) {
                try {
                    WsdlDataHolder wsdlHolder = new WsdlDataHolder(tmapModel);
                    wsdlHolder.namespace = tmapImport.getNamespace();
                    wsdlHolder.location = tmapImport.getLocation();
                    if (wsdlHolder.location == null) continue;

                    //wsdlHolder.wsdlModel = TMapCompletionUtil.getWsdlModel(tmapModel, 
                    //    wsdlHolder.location, false);
                    wsdlHolder.wsdlModel = tmapImport.getImportModel();
                    
                    if (wsdlHolder.wsdlModel == null) continue;
                    if (wsdlHolder.getQNamePrefix() == null) continue;
                    wsdlHolders.add(wsdlHolder);
                } catch(Exception e) {
                    TMapCompletionUtil.logExceptionInfo(e);
                }
            }
            return wsdlHolders;
        } catch(Exception e) {
            TMapCompletionUtil.logExceptionInfo(e);
            return null;
        }
    }
    
    public static WsdlDataHolder findWsdlByQNamePrefix(String requiredQNamePrefix, 
        List<WsdlDataHolder> wsdlHolders) {
        if ((requiredQNamePrefix == null) || (requiredQNamePrefix.length() < 1))
            return null;
        
        if ((wsdlHolders == null) || (wsdlHolders.isEmpty())) return null;
        for (WsdlDataHolder wsdlHolder : wsdlHolders) {
            String qnamePrefix = wsdlHolder.getQNamePrefix();
            if (requiredQNamePrefix.equals(qnamePrefix)) return wsdlHolder;
        }        
        return null;
    }
}