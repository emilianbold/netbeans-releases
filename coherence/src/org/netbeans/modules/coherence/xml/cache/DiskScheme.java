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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "schemeName",
    "schemeRef",
    "className",
    "initParams",
    "fileManager",
    "rootDirectoryOrDirectory",
    "fileName",
    "highUnits",
    "initialSize",
    "maximumSize",
    "pageLimit",
    "pageDuration",
    "listener",
    "async",
    "asyncLimit"
})
@XmlRootElement(name = "disk-scheme")
public class DiskScheme {

    @XmlElement(name = "scheme-name")
    protected SchemeName schemeName;
    @XmlElement(name = "scheme-ref")
    protected String schemeRef;
    @XmlElement(name = "class-name")
    protected ClassName className;
    @XmlElement(name = "init-params")
    protected InitParams initParams;
    @XmlElement(name = "file-manager")
    protected String fileManager;
    @XmlElements({
        @XmlElement(name = "root-directory", type = RootDirectory.class),
        @XmlElement(name = "directory", type = Directory.class)
    })
    protected List<Object> rootDirectoryOrDirectory;
    @XmlElement(name = "file-name")
    protected String fileName;
    @XmlElement(name = "high-units")
    protected String highUnits;
    @XmlElement(name = "initial-size")
    protected InitialSize initialSize;
    @XmlElement(name = "maximum-size")
    protected MaximumSize maximumSize;
    @XmlElement(name = "page-limit")
    protected String pageLimit;
    @XmlElement(name = "page-duration")
    protected String pageDuration;
    protected Listener listener;
    protected String async;
    @XmlElement(name = "async-limit")
    protected String asyncLimit;

    /**
     * Gets the value of the schemeName property.
     * 
     * @return
     *     possible object is
     *     {@link SchemeName }
     *     
     */
    public SchemeName getSchemeName() {
        return schemeName;
    }

    /**
     * Sets the value of the schemeName property.
     * 
     * @param value
     *     allowed object is
     *     {@link SchemeName }
     *     
     */
    public void setSchemeName(SchemeName value) {
        this.schemeName = value;
    }

    /**
     * Gets the value of the schemeRef property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSchemeRef() {
        return schemeRef;
    }

    /**
     * Sets the value of the schemeRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSchemeRef(String value) {
        this.schemeRef = value;
    }

    /**
     * Gets the value of the className property.
     * 
     * @return
     *     possible object is
     *     {@link ClassName }
     *     
     */
    public ClassName getClassName() {
        return className;
    }

    /**
     * Sets the value of the className property.
     * 
     * @param value
     *     allowed object is
     *     {@link ClassName }
     *     
     */
    public void setClassName(ClassName value) {
        this.className = value;
    }

    /**
     * Gets the value of the initParams property.
     * 
     * @return
     *     possible object is
     *     {@link InitParams }
     *     
     */
    public InitParams getInitParams() {
        return initParams;
    }

    /**
     * Sets the value of the initParams property.
     * 
     * @param value
     *     allowed object is
     *     {@link InitParams }
     *     
     */
    public void setInitParams(InitParams value) {
        this.initParams = value;
    }

    /**
     * Gets the value of the fileManager property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFileManager() {
        return fileManager;
    }

    /**
     * Sets the value of the fileManager property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFileManager(String value) {
        this.fileManager = value;
    }

    /**
     * Gets the value of the rootDirectoryOrDirectory property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rootDirectoryOrDirectory property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRootDirectoryOrDirectory().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RootDirectory }
     * {@link Directory }
     * 
     * 
     */
    public List<Object> getRootDirectoryOrDirectory() {
        if (rootDirectoryOrDirectory == null) {
            rootDirectoryOrDirectory = new ArrayList<Object>();
        }
        return this.rootDirectoryOrDirectory;
    }

    /**
     * Gets the value of the fileName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the value of the fileName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFileName(String value) {
        this.fileName = value;
    }

    /**
     * Gets the value of the highUnits property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHighUnits() {
        return highUnits;
    }

    /**
     * Sets the value of the highUnits property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHighUnits(String value) {
        this.highUnits = value;
    }

    /**
     * Gets the value of the initialSize property.
     * 
     * @return
     *     possible object is
     *     {@link InitialSize }
     *     
     */
    public InitialSize getInitialSize() {
        return initialSize;
    }

    /**
     * Sets the value of the initialSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link InitialSize }
     *     
     */
    public void setInitialSize(InitialSize value) {
        this.initialSize = value;
    }

    /**
     * Gets the value of the maximumSize property.
     * 
     * @return
     *     possible object is
     *     {@link MaximumSize }
     *     
     */
    public MaximumSize getMaximumSize() {
        return maximumSize;
    }

    /**
     * Sets the value of the maximumSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link MaximumSize }
     *     
     */
    public void setMaximumSize(MaximumSize value) {
        this.maximumSize = value;
    }

    /**
     * Gets the value of the pageLimit property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPageLimit() {
        return pageLimit;
    }

    /**
     * Sets the value of the pageLimit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPageLimit(String value) {
        this.pageLimit = value;
    }

    /**
     * Gets the value of the pageDuration property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPageDuration() {
        return pageDuration;
    }

    /**
     * Sets the value of the pageDuration property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPageDuration(String value) {
        this.pageDuration = value;
    }

    /**
     * Gets the value of the listener property.
     * 
     * @return
     *     possible object is
     *     {@link Listener }
     *     
     */
    public Listener getListener() {
        return listener;
    }

    /**
     * Sets the value of the listener property.
     * 
     * @param value
     *     allowed object is
     *     {@link Listener }
     *     
     */
    public void setListener(Listener value) {
        this.listener = value;
    }

    /**
     * Gets the value of the async property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAsync() {
        return async;
    }

    /**
     * Sets the value of the async property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAsync(String value) {
        this.async = value;
    }

    /**
     * Gets the value of the asyncLimit property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAsyncLimit() {
        return asyncLimit;
    }

    /**
     * Sets the value of the asyncLimit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAsyncLimit(String value) {
        this.asyncLimit = value;
    }

}
