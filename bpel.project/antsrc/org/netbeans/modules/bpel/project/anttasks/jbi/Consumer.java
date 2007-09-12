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
package org.netbeans.modules.bpel.project.anttasks.jbi;

public class Consumer {
    //Member variable representing partner link name
    private String mPartnerLinkName = null;
    //Member variable representing port name
    private String mPortName = null;
    //Member variable representing partnerlink Namespace
    private String mPartnerLinkNS = null;
    //Member variable representing portname namespace
    private String mPortNameNS = null;
    //Member variable representing  role name
    private String mPartnerRoleName = null;   
    //Member variable representing Partnerlink Namespace Prefix
    private String mPartnerLinkNSPrefix = null;
    //Member variable representing Portname Namespace Prefix
    private String mPortNameNSPrefix = null;       
     
    /**
     * Constructor
     * @param partnerLinkName Partner link name
     * @param portName    Port name
     * @param partnerLinkNS Namespace URI of the Partner Link
     * @param portNameNS Namespace URI of the portname
     * @param partnerRoleName Partner role name
     */
    public Consumer(String partnerLinkName, String portName, String partnerLinkNS, String portNameNS, String partnerRoleName, String partnerLinkNSPrefix, String portNameNSPrefix) {
        mPartnerLinkName = partnerLinkName;
        mPortName = portName;
        mPartnerLinkNS = partnerLinkNS;
        mPortNameNS = portNameNS;
        mPartnerRoleName =partnerRoleName;
        mPartnerLinkNSPrefix = partnerLinkNSPrefix;
        mPortNameNSPrefix = portNameNSPrefix;        
    }
    
    
    /**
     * Get Name of the Partner Link
     * @return Name of the Partner Link
     */
    public String getPartnerLinkName() {
        return mPartnerLinkName;
    }
    /**
     * Get Name of the Port
     * @return Name of the Port
     */
    public String getPortName() {
        return mPortName;
    }
    /**
     * Get Namespace URI of the Partner Link
     * @return Namespace URI of the Partner Link
     */
    public String getPartnerLinkNamespace() {
        return mPartnerLinkNS;
    }
    /**
     * Get Namespace Prefix of the Partner Link
     * @return Namespace URI of the Partner Link
     */
    public String getPartnerLinkNamespacePrefix() {
        return mPartnerLinkNSPrefix;
    }   
    /**
     * Return Namespace URI of portName
     * @return Namespace URI of the portname
     */
    public String getPortNameNamespace() {
        return mPortNameNS;
    }    
    /**
     * Return Namespace Prefix of portName
     * @return Namespace URI of the portname
     */
    public String getPortNameNamespacePrefix() {
        return mPortNameNSPrefix;
    }     
    /**
     * Return Partner Role name
     * @return Partner Role name
     */   
    public String getPartnerRoleName(){
        return mPartnerRoleName;
    }
    
    public boolean equals(Object obj) {
        if (! (obj instanceof Provider)) {
            return false;
        }
        Consumer consumer = (Consumer)obj;
        if (this.mPartnerLinkName.equals(consumer.getPartnerLinkName()) && 
            this.mPortName.equals(consumer.getPortName()) && 
            this.mPartnerLinkNS.equals(consumer.getPartnerLinkNamespace())&& 
            this.mPortNameNSPrefix.equals(consumer.getPortNameNamespacePrefix())&& 
            this.mPartnerRoleName.equals(consumer.getPartnerRoleName())){
                return true;
        }
        return false;
    }
    
    public int hashCode() {
        return (this.mPartnerLinkName+this.mPortName+this.mPartnerRoleName).hashCode();
    }
    
}
