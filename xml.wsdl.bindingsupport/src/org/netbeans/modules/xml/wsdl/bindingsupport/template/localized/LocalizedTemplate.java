/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * LocalizedTemplate.java
 *
 * Created on September 1, 2006, 12:21 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.bindingsupport.template.localized;

import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementTemplateProvider;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateType;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.WsdlElementType;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.WsdlTemplateType;

/**
 *
 * @author radval
 */
public class LocalizedTemplate {
    
    public enum Mode {
        INBOUND,OUTBOUND,BOTH, NOT_SPECIFIED
    }
    
    public static final String TEMPLATE = "TEMPLATE"; //NOI18N
    public static final String INBOUND = "inbound"; //NOI18N
    public static final String OUTBOUND = "outbound"; //NOI18N
    public static final String BOTH = "both"; //NOI18N
    public static final String MODE_NOT_INITIALIZED = "MODE_NOT_INITIALIZED"; //NOI18N
    
    
    private LocalizedTemplateGroup mParent;
    
    private TemplateType mTemplateType;
    
    private ExtensibilityElementTemplateProvider mProvider;
    
    /** Creates a new instance of LocalizedTemplate */
    public LocalizedTemplate(LocalizedTemplateGroup parent, TemplateType template, ExtensibilityElementTemplateProvider provider) {
        this.mParent = parent;
        this.mTemplateType = template;
        this.mProvider = provider;
    }
    
    public LocalizedTemplateGroup getTemplateGroup() {
        return this.mParent;
    }
    
    public String getName() {
        String lName = null;
        try {
         String name = TEMPLATE + "_name_" + this.mTemplateType.getName(); //NOI18N
         lName = this.mProvider.getLocalizedMessage(name, null);
        } catch (Exception ex) {
            lName = this.mTemplateType.getName();
        }
        
        return lName;
    }
    
    public TemplateType getDelegate() {
        return this.mTemplateType;
    }

    public ExtensibilityElementTemplateProvider getMProvider() {
        return mProvider;
    }
    
    /**
     * get WSDLElementType 
     * @param wsdlElementName name attribute of <wsdlElement> 
     */
    public WsdlElementType getWSDLElementType(String wsdlElementName) {
        WsdlElementType wsdlElement = null;
        
        WsdlElementType[] wsdlElements = this.mTemplateType.getWsdlElement();
        if(wsdlElements != null) {
            for(int i = 0; i < wsdlElements.length; i++) {
                WsdlElementType w = wsdlElements[i];
                if(wsdlElementName.equals(w.getName())) {
                    wsdlElement = w;
                    break;
                }
            }
        }
        
        return wsdlElement;
    }
    
    public String getWSDLTemplateFile() {
        WsdlTemplateType template = mTemplateType.getWsdlTemplate();
        if (template != null) {
            return template.getFile();
        }
        return null;
    }
    
    public boolean isSkeleton() {
        return mTemplateType.isSkeleton();
    }
    public Mode getMode() {
        String mode = mTemplateType.getMode();
        Mode returnMode = Mode.BOTH;
        if ( mode != null) {
            if ( mode.equals(INBOUND )){
                returnMode = Mode.INBOUND;
            } else if ( mode.equals(OUTBOUND)){
                returnMode = Mode.OUTBOUND;
            }
        }
        return returnMode;
    }
    public String getDescription() {
        String lDescription = null;
        try {
         String description = TEMPLATE + "_description_" + // NOI18N
                 this.mTemplateType.getName(); 
         lDescription = this.mProvider.getLocalizedMessage(description, null);
        } catch (Exception ex) {
            lDescription = this.mTemplateType.getName();
        }
        
        return lDescription;
    }    
}
