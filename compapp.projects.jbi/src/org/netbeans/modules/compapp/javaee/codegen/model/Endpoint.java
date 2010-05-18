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
 * Endpoint.java
 *
 * Created on October 4, 2006, 2:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.compapp.javaee.codegen.model;

import javax.xml.namespace.QName;
import org.openide.util.NbBundle;

/**
 *
 * @author gpatil
 */
public class Endpoint {
    public static enum EndPointType { Provider, Consumer}
    
    private EndPointType ept;
    private String endPointName;     // PortName
    private QName interfaceName;    // PortType
    private QName serviceName;      // ServiceName
        
    private String  wsdlLocation = "";  //NOI18N
    
    private int hash = -1;
    
    /**
     * Creates a new instance of Endpoint
     */
    public Endpoint() {
    }
    
    public Endpoint(EndPointType type, String portName, QName portType, QName nServiceName) {
        this.ept = type;
        this.endPointName = portName;
        this.interfaceName = portType;
        this.serviceName = nServiceName;
    }
    
    public Endpoint(Endpoint orig) {
        this.ept = orig.ept;
        this.endPointName = orig.endPointName;
        this.interfaceName = orig.interfaceName;
        this.serviceName = orig.serviceName;
    }
    
    public void setEndPointType(EndPointType nEpt){
        this.ept = nEpt;
        this.hash = -1;
    }
    
    public EndPointType getEndPointType(){
        return this.ept;
    }
    
    public void setEndPointName(String nName){
        this.endPointName = nName;
        this.hash = -1;
    }
    
    public String getEndPointName(){
        return this.endPointName;
    }
    
    public void setInterfaceName(QName nName){
        this.interfaceName = nName;
        this.hash = -1;
    }
    
    public QName getInterfaceName(){
        return this.interfaceName;
    }
    
    public void setServiceName(QName nName){
        this.serviceName = nName;
        this.hash = -1;
    }
       
    public QName getServiceName(){
        return this.serviceName;
    }
        
    public String getWSDLLocation(){
        return this.wsdlLocation;
    }
    
    public void setWSDLLocation(String nWsdlLoc){
        this.wsdlLocation = nWsdlLoc;
    }

    // Used only by getLocalizedString
    private static String LOCALIZED_STR_PROVIDES = null;
    private static String LOCALIZED_STR_CONSUMES = null;
    private static String LOCALIZED_STR_SERVICE = null;
    private static String LOCALIZED_STR_INTERFACE = null;
    private static String LOCALIZED_STR_ENDPOINT = null;

    static {        
        LOCALIZED_STR_PROVIDES = NbBundle.getMessage(Endpoint.class, "LBL_Provides"); //NOI18N
        LOCALIZED_STR_CONSUMES = NbBundle.getMessage(Endpoint.class, "LBL_Consumes"); //NOI18N
        LOCALIZED_STR_SERVICE = NbBundle.getMessage(Endpoint.class, "LBL_Service"); //NOI18N
        LOCALIZED_STR_INTERFACE = NbBundle.getMessage(Endpoint.class, "LBL_Ineterface"); //NOI18N
        LOCALIZED_STR_ENDPOINT = NbBundle.getMessage(Endpoint.class, "LBL_Endpoint"); //NOI18N
    }
    
    public String getEPToolTip(){
        StringBuffer sb = new StringBuffer();
        sb.append("<html>");//NOI18N

        if (EndPointType.Provider.equals(this.ept)){
            sb.append(LOCALIZED_STR_PROVIDES);
        } else {
            sb.append(LOCALIZED_STR_CONSUMES);
        }
        sb.append("<br>"); //NOI18N
        sb.append(LOCALIZED_STR_SERVICE);
        sb.append(":"); //NOI18N
        sb.append(this.serviceName);
        sb.append("<br>"); //NOI18N
        sb.append(LOCALIZED_STR_ENDPOINT);
        sb.append(":"); //NOI18N
        sb.append(this.endPointName);
        sb.append("<br>"); //NOI18N
        sb.append(LOCALIZED_STR_INTERFACE);
        sb.append(":"); //NOI18N
        sb.append(this.interfaceName);   
        sb.append("</html>");//NOI18N
        return sb.toString();
    }
    
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("EndPoint("); //NOI18N
        sb.append(this.ept);
        sb.append("):service:");//NOI18N
        sb.append(this.serviceName);
        sb.append(":port:");//NOI18N
        sb.append(this.endPointName);
        sb.append(":portType:");//NOI18N
        sb.append(this.interfaceName);
        return sb.toString();
    }
    
    public String toEndpointConfigXML(String indent){
        if (indent == null){
            indent = ""; 
        }
        
        String newlineIndent = "\n" + indent;
        
        StringBuffer sb = new StringBuffer();
        sb.append(indent);
        sb.append("<endpoint ");//NOI18N
        sb.append(newlineIndent);        
        sb.append("  endpointType=\"");//NOI18N
        sb.append(this.ept);
        sb.append("\"");  //NOI18N
        sb.append(newlineIndent);
        sb.append("  portName=\""); //NOI18N
        sb.append(this.endPointName);
        sb.append("\"");          //NOI18N
        sb.append(newlineIndent);
        sb.append("  portTypeLocalName=\"");//NOI18N
        sb.append(this.interfaceName.getLocalPart());
        sb.append("\"");          //NOI18N
        sb.append(newlineIndent);
        sb.append("  portTypeNamespace=\"");//NOI18N
        sb.append(this.interfaceName.getNamespaceURI());
        sb.append("\"");          //NOI18N
        sb.append(newlineIndent);        
        sb.append("  serviceLocalName=\"");//NOI18N
        sb.append(this.serviceName.getLocalPart());
        sb.append("\"");          //NOI18N
        sb.append(newlineIndent);        
        sb.append("  serviceNamespace=\"");//NOI18N
        sb.append(this.serviceName.getNamespaceURI());
        sb.append("\"/>\n");//NOI18N
        return sb.toString();
    }
    
    /**
     *
     */
    public boolean equals(Object obj) {
        boolean ret = true;
        Endpoint oObj = null;
        
        if (this == obj){
            return true;
        }
        
        if ((obj == null) || (!(obj instanceof Endpoint))){
            return false;
        }
        
        oObj = (Endpoint) obj;
        
        if (this.ept != oObj.ept){
            return false;
        }
        
        if ((this.interfaceName == null) && (oObj.getInterfaceName() != null)){
            return false;
        }
        
        if ((this.interfaceName != null) && (!this.interfaceName.equals(oObj.getInterfaceName()))){
            return false;
        }
        
        if ((this.serviceName == null) && (oObj.getServiceName() != null)){
            return false;
        }
        
        if ((this.serviceName != null) && (!this.serviceName.equals(oObj.getServiceName()))){
            return false;
        }
        
        if ((this.endPointName == null) && (oObj.getEndPointName() != null)){
            return false;
        }
        
        if ((this.endPointName != null) && (!this.endPointName.equals(oObj.getEndPointName()))){
            return false;
        }
        
        return ret;
    }
    
    public int hashCode() {
        if (this.hash == -1){
            // Do not include "UseBridge", "usingDefaultNames"
            StringBuffer sb = new StringBuffer();
            sb.append(this.endPointName);
            sb.append(this.ept);
            sb.append(this.interfaceName);
            sb.append(this.serviceName);
            this.hash = sb.toString().hashCode();
        }
        return this.hash;
    }
}

