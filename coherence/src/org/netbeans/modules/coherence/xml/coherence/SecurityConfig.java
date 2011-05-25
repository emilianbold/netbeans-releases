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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "enabled",
    "loginModuleName",
    "accessController",
    "callbackHandler",
    "identityAsserter",
    "identityTransformer",
    "subjectScope"
})
@XmlRootElement(name = "security-config")
public class SecurityConfig {

    @XmlAttribute(name = "xml-override")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String xmlOverride;
    @XmlElement(required = true)
    protected Enabled enabled;
    @XmlElement(name = "login-module-name", required = true)
    protected LoginModuleName loginModuleName;
    @XmlElement(name = "access-controller", required = true)
    protected AccessController accessController;
    @XmlElement(name = "callback-handler")
    protected CallbackHandler callbackHandler;
    @XmlElement(name = "identity-asserter")
    protected IdentityAsserter identityAsserter;
    @XmlElement(name = "identity-transformer")
    protected IdentityTransformer identityTransformer;
    @XmlElement(name = "subject-scope")
    protected String subjectScope;

    /**
     * Gets the value of the xmlOverride property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getXmlOverride() {
        return xmlOverride;
    }

    /**
     * Sets the value of the xmlOverride property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setXmlOverride(String value) {
        this.xmlOverride = value;
    }

    /**
     * Gets the value of the enabled property.
     * 
     * @return
     *     possible object is
     *     {@link Enabled }
     *     
     */
    public Enabled getEnabled() {
        return enabled;
    }

    /**
     * Sets the value of the enabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link Enabled }
     *     
     */
    public void setEnabled(Enabled value) {
        this.enabled = value;
    }

    /**
     * Gets the value of the loginModuleName property.
     * 
     * @return
     *     possible object is
     *     {@link LoginModuleName }
     *     
     */
    public LoginModuleName getLoginModuleName() {
        return loginModuleName;
    }

    /**
     * Sets the value of the loginModuleName property.
     * 
     * @param value
     *     allowed object is
     *     {@link LoginModuleName }
     *     
     */
    public void setLoginModuleName(LoginModuleName value) {
        this.loginModuleName = value;
    }

    /**
     * Gets the value of the accessController property.
     * 
     * @return
     *     possible object is
     *     {@link AccessController }
     *     
     */
    public AccessController getAccessController() {
        return accessController;
    }

    /**
     * Sets the value of the accessController property.
     * 
     * @param value
     *     allowed object is
     *     {@link AccessController }
     *     
     */
    public void setAccessController(AccessController value) {
        this.accessController = value;
    }

    /**
     * Gets the value of the callbackHandler property.
     * 
     * @return
     *     possible object is
     *     {@link CallbackHandler }
     *     
     */
    public CallbackHandler getCallbackHandler() {
        return callbackHandler;
    }

    /**
     * Sets the value of the callbackHandler property.
     * 
     * @param value
     *     allowed object is
     *     {@link CallbackHandler }
     *     
     */
    public void setCallbackHandler(CallbackHandler value) {
        this.callbackHandler = value;
    }

    /**
     * Gets the value of the identityAsserter property.
     * 
     * @return
     *     possible object is
     *     {@link IdentityAsserter }
     *     
     */
    public IdentityAsserter getIdentityAsserter() {
        return identityAsserter;
    }

    /**
     * Sets the value of the identityAsserter property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentityAsserter }
     *     
     */
    public void setIdentityAsserter(IdentityAsserter value) {
        this.identityAsserter = value;
    }

    /**
     * Gets the value of the identityTransformer property.
     * 
     * @return
     *     possible object is
     *     {@link IdentityTransformer }
     *     
     */
    public IdentityTransformer getIdentityTransformer() {
        return identityTransformer;
    }

    /**
     * Sets the value of the identityTransformer property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentityTransformer }
     *     
     */
    public void setIdentityTransformer(IdentityTransformer value) {
        this.identityTransformer = value;
    }

    /**
     * Gets the value of the subjectScope property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSubjectScope() {
        return subjectScope;
    }

    /**
     * Sets the value of the subjectScope property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSubjectScope(String value) {
        this.subjectScope = value;
    }

}
