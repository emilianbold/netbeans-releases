/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
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
 * "Portions Copyrighted 2006 Ricoh Corporation"
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

package ricoh.util.dom;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 *
 * @author esanchez
 */
public class DalpDOMHandler extends DOMHandler
{
    public static final String DEFAULT_DALP_VERSION_STR = "0.10";
    public static final String DEFAULT_BASEPATH         = "current";
    public static final String DEFAULT_LOCATION         = "jar";
    public static final String DSDK_VERSION_STR         = "1.0";
    public static final String APP_VISIBLE_STR          = "true";
    public static final String INSTALL_MODE             = "manual";
    public static final String INSTALL_DEST             = "hdd";
    
    public static final int DIGITS_IN_UID = 8;
    
    private String appName           = null,
                   appVersion        = null,
                   email             = null, 
                   description       = null, 
                   detailDescription = null,
                   fax               = null, 
                   iconPath          = null, 
                   mainClass         = null,
                   phone             = null, 
                   targetJar         = null,       
                   uid               = null, 
                   vendor            = null,
                   dalpSpecVer       = null;
    
    private String seperator;
    
    /** Creates a new instance of DalpDOMHandler */
    public DalpDOMHandler() 
    {
        super();
        setCompression(false);
    }
    
    public void setDalpSpecVersion(String ver)
    {
        dalpSpecVer = ver;
    }
    
    public String getDalpSpecVersion()
    {
        return dalpSpecVer;
    }
    
    public void setAppID(String uid)
    {
        this.uid = uid;
    }
    
    public String getAppID()
    {
        return this.uid;
    }
    
    public void setXletClass(String mainClass)
    {
        this.mainClass = mainClass;
    }
    
    public String getXletClass()
    {
        return mainClass;
    }
    
    public void setEmail(String email)
    {
        this.email = email;
    }
    
    public String getEmail()
    {
        return email;
    }
    
    public void setFax(String fax)
    {
        this.fax = fax;
    }
    
    public String getFax()
    {
        return fax;
    }
    
    public void setTelephone(String number)
    {
        this.phone = number;
    }
    
    public String getTelephone()
    {
        return phone;
    }
    
    public String getAppName()
    {
        return this.appName;
    }
    
    public void setAppName(String newName)
    {
        this.appName = newName;
    }

    public void setAppVersion(String newVer)
    {
        this.appVersion = newVer;
    }
    
    public String getAppVersion()
    {
        return this.appVersion;
    }
    
    public void setIconPath(String iconPath)
    {
        this.iconPath = iconPath;
        if (iconPath.indexOf("/") == -1)
            seperator = "\\";
        else
            seperator = "/";
    }
    
    public void setTargetJar(String target)
    {
        targetJar = target;
    }
    
    public String getTargetJar()
    {
        return this.targetJar;
    }
    
    public boolean failRequiredInfo()
    {
        return !(isValidUid(this.uid));
    }
    
    public static boolean isValidUid(String num)
    {
        if (num == null)
            return false;
        try
        {
            if (Integer.parseInt(num) < 0)
                return false;
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }

        return true;
   }
    
    public Document loadDocument(File xmlFile)
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try
        {
            builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);
            
            return document;
        }
        catch(Exception pce)
        {
            pce.printStackTrace();
            return null;
        }
    }
    
    public Document makeDocument()
    {
        if (failRequiredInfo())
            return null;
        else
        {
            //root (dalp) tag
            Element root = document.createElement("dalp");
            root.setAttribute("dsdk", "");
            root.setAttribute("version", DEFAULT_DALP_VERSION_STR);
            document.appendChild(root);
            
            //information root element
            Element informationTag = document.createElement("information");
            root.appendChild(informationTag);

            //information->product-id
            Element productId = document.createElement("product-id");
            productId.setTextContent(uid);
            informationTag.appendChild(productId);
            
            //information->title
            Element title = document.createElement("title");
            informationTag.appendChild(title);
            title.setTextContent(appName);
            
            //information->vendor
            Element vendorTag = document.createElement("vendor");
            informationTag.appendChild(vendorTag);
            vendorTag.setTextContent(vendor);
            
            //information->icon
            if ((iconPath != null) && (iconPath.trim().equals("") == false))
            {
                Element iconTag = document.createElement("icon");
                informationTag.appendChild(iconTag);
                iconTag.setAttribute("href", "./" + this.targetJar + ".jar");
                iconTag.setAttribute("basepath", DEFAULT_BASEPATH);
                iconTag.setAttribute("location", DEFAULT_LOCATION);
                iconTag.setTextContent(iconPath.substring(iconPath.lastIndexOf(this.seperator) + 1));
            }

            //information->description
            Element descriptionTagA = document.createElement("description");
            Element descriptionTagB = document.createElement("description");
            informationTag.appendChild(descriptionTagA);
            informationTag.appendChild(descriptionTagB);
            descriptionTagA.setTextContent(this.description);
            descriptionTagB.setAttribute("type", "detail");
            descriptionTagB.setTextContent(this.detailDescription);
            
            //information->telephone, fax, e-mail
            Element telephoneTag    = document.createElement("telephone");
            Element faxTag          = document.createElement("fax");
            Element emailTag        = document.createElement("e-mail");
            telephoneTag.setTextContent(this.phone);
            faxTag.setTextContent(this.fax);
            emailTag.setTextContent(this.email);
            informationTag.appendChild(telephoneTag);
            informationTag.appendChild(faxTag);
            informationTag.appendChild(emailTag);
            
            //these are set by default to fixed values, no change supported as of yet
            //information->application-version, information->offline-allowed, dalp->security element (root child), 
            Element appVersionTag   = document.createElement("application-ver");
            appVersionTag.setTextContent(appVersion);
            informationTag.appendChild(appVersionTag);

            Element allowOfflineTag = document.createElement("offline-allowed");
            informationTag.appendChild(allowOfflineTag);
            
            Element securityTag = document.createElement("security");
            root.appendChild(securityTag);
            Element allPermissions = document.createElement("all-permissions");
            securityTag.appendChild(allPermissions);
            
            //dalp->resources
            Element resourcesTag = document.createElement("resources");
            root.appendChild(resourcesTag);
            
            //resources->dsdk
            Element dsdkTag = document.createElement("dsdk");
            dsdkTag.setAttribute("version", DSDK_VERSION_STR);
            resourcesTag.appendChild(dsdkTag);
            
            //resources->jar
            Element jarTag = document.createElement("jar");
            jarTag.setAttribute("href", "./" + targetJar + ".jar");
            jarTag.setAttribute("version", this.appVersion);
            jarTag.setAttribute("basepath", DEFAULT_BASEPATH);
            resourcesTag.appendChild(jarTag);
            
            //resources->encode-file
            Element encodeFileTag = document.createElement("encode-file");
            if (targetJar.contains(".jar"))
                encodeFileTag.setTextContent(targetJar.substring(0, targetJar.indexOf(".jar")).toLowerCase());
            else
                encodeFileTag.setTextContent(targetJar.toLowerCase());
                
            resourcesTag.appendChild(encodeFileTag);
            
            //dalp->application-desc
            Element appDescTag = document.createElement("application-desc");
            appDescTag.setAttribute("main-class", this.mainClass);
            appDescTag.setAttribute("visible", APP_VISIBLE_STR);
            root.appendChild(appDescTag);
            
            //dalp->install
            Element installTag = document.createElement("install");
            installTag.setAttribute("mode", INSTALL_MODE);
            installTag.setAttribute("destination", INSTALL_DEST);
            root.appendChild(installTag);
            
            return document;
        }
    }

    /**
     * Getter for property vendor.
     * @return Value of property vendor.
     */
    public String getVendor() {
        return this.vendor;
    }

    /**
     * Setter for property vendor.
     * @param vendor New value of property vendor.
     */
    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    /**
     * Getter for property iconPath.
     * @return Value of property iconPath.
     */
    public java.lang.String getIconPath() 
    {
        return this.iconPath;
    }

    /**
     * Getter for property description.
     * @return Value of property description.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Setter for property description.
     * @param description New value of property description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Getter for property detailDescription.
     * @return Value of property detailDescription.
     */
    public String getDetailDescription() {
        return this.detailDescription;
    }

    /**
     * Setter for property detailDescription.
     * @param detailDescription New value of property detailDescription.
     */
    public void setDetailDescription(String detailDescription) {
        this.detailDescription = detailDescription;
    }
    
    
}
