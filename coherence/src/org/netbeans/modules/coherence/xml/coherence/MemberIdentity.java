/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.coherence.xml.coherence;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "clusterName",
    "siteName",
    "rackName",
    "machineName",
    "processName",
    "memberName",
    "roleName",
    "priority"
})
@XmlRootElement(name = "member-identity")
public class MemberIdentity {

    @XmlElement(name = "cluster-name")
    protected ClusterName clusterName;
    @XmlElement(name = "site-name")
    protected SiteName siteName;
    @XmlElement(name = "rack-name")
    protected RackName rackName;
    @XmlElement(name = "machine-name")
    protected MachineName machineName;
    @XmlElement(name = "process-name")
    protected ProcessName processName;
    @XmlElement(name = "member-name")
    protected MemberName memberName;
    @XmlElement(name = "role-name")
    protected RoleName roleName;
    protected Priority priority;

    /**
     * Gets the value of the clusterName property.
     * 
     * @return
     *     possible object is
     *     {@link ClusterName }
     *     
     */
    public ClusterName getClusterName() {
        return clusterName;
    }

    /**
     * Sets the value of the clusterName property.
     * 
     * @param value
     *     allowed object is
     *     {@link ClusterName }
     *     
     */
    public void setClusterName(ClusterName value) {
        this.clusterName = value;
    }

    /**
     * Gets the value of the siteName property.
     * 
     * @return
     *     possible object is
     *     {@link SiteName }
     *     
     */
    public SiteName getSiteName() {
        return siteName;
    }

    /**
     * Sets the value of the siteName property.
     * 
     * @param value
     *     allowed object is
     *     {@link SiteName }
     *     
     */
    public void setSiteName(SiteName value) {
        this.siteName = value;
    }

    /**
     * Gets the value of the rackName property.
     * 
     * @return
     *     possible object is
     *     {@link RackName }
     *     
     */
    public RackName getRackName() {
        return rackName;
    }

    /**
     * Sets the value of the rackName property.
     * 
     * @param value
     *     allowed object is
     *     {@link RackName }
     *     
     */
    public void setRackName(RackName value) {
        this.rackName = value;
    }

    /**
     * Gets the value of the machineName property.
     * 
     * @return
     *     possible object is
     *     {@link MachineName }
     *     
     */
    public MachineName getMachineName() {
        return machineName;
    }

    /**
     * Sets the value of the machineName property.
     * 
     * @param value
     *     allowed object is
     *     {@link MachineName }
     *     
     */
    public void setMachineName(MachineName value) {
        this.machineName = value;
    }

    /**
     * Gets the value of the processName property.
     * 
     * @return
     *     possible object is
     *     {@link ProcessName }
     *     
     */
    public ProcessName getProcessName() {
        return processName;
    }

    /**
     * Sets the value of the processName property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProcessName }
     *     
     */
    public void setProcessName(ProcessName value) {
        this.processName = value;
    }

    /**
     * Gets the value of the memberName property.
     * 
     * @return
     *     possible object is
     *     {@link MemberName }
     *     
     */
    public MemberName getMemberName() {
        return memberName;
    }

    /**
     * Sets the value of the memberName property.
     * 
     * @param value
     *     allowed object is
     *     {@link MemberName }
     *     
     */
    public void setMemberName(MemberName value) {
        this.memberName = value;
    }

    /**
     * Gets the value of the roleName property.
     * 
     * @return
     *     possible object is
     *     {@link RoleName }
     *     
     */
    public RoleName getRoleName() {
        return roleName;
    }

    /**
     * Sets the value of the roleName property.
     * 
     * @param value
     *     allowed object is
     *     {@link RoleName }
     *     
     */
    public void setRoleName(RoleName value) {
        this.roleName = value;
    }

    /**
     * Gets the value of the priority property.
     * 
     * @return
     *     possible object is
     *     {@link Priority }
     *     
     */
    public Priority getPriority() {
        return priority;
    }

    /**
     * Sets the value of the priority property.
     * 
     * @param value
     *     allowed object is
     *     {@link Priority }
     *     
     */
    public void setPriority(Priority value) {
        this.priority = value;
    }

}
