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
package org.netbeans.modules.coherence.xml.cache;

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
    "operationName",
    "preferredSize",
    "delayMillis",
    "threadThreshold",
    "autoAdjust"
})
@XmlRootElement(name = "bundle-config")
public class BundleConfig {

    @XmlElement(name = "operation-name", required = true)
    protected String operationName;
    @XmlElement(name = "preferred-size")
    protected String preferredSize;
    @XmlElement(name = "delay-millis")
    protected String delayMillis;
    @XmlElement(name = "thread-threshold")
    protected String threadThreshold;
    @XmlElement(name = "auto-adjust")
    protected String autoAdjust;

    /**
     * Gets the value of the operationName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOperationName() {
        return operationName;
    }

    /**
     * Sets the value of the operationName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOperationName(String value) {
        this.operationName = value;
    }

    /**
     * Gets the value of the preferredSize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPreferredSize() {
        return preferredSize;
    }

    /**
     * Sets the value of the preferredSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPreferredSize(String value) {
        this.preferredSize = value;
    }

    /**
     * Gets the value of the delayMillis property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDelayMillis() {
        return delayMillis;
    }

    /**
     * Sets the value of the delayMillis property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDelayMillis(String value) {
        this.delayMillis = value;
    }

    /**
     * Gets the value of the threadThreshold property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getThreadThreshold() {
        return threadThreshold;
    }

    /**
     * Sets the value of the threadThreshold property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setThreadThreshold(String value) {
        this.threadThreshold = value;
    }

    /**
     * Gets the value of the autoAdjust property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAutoAdjust() {
        return autoAdjust;
    }

    /**
     * Sets the value of the autoAdjust property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAutoAdjust(String value) {
        this.autoAdjust = value;
    }

}
