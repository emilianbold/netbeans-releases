/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * LocalizedTemplateGroup.java
 *
 * Created on September 1, 2006, 12:15 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.bindingsupport.template.localized;

import java.util.List;

import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementTemplateProvider;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateGroup;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateType;

/**
 *
 * @author radval
 */
public class LocalizedTemplateGroup implements Comparable<LocalizedTemplateGroup> {
    
    public static final String TEMPLATE_GROUP="TEMPLATEGROUP";//NOI18N
    
    private TemplateGroup mTGroup;
    
    private ExtensibilityElementTemplateProvider mProvider;
    
    private LocalizedTemplate[] mLtts = null;
    
    /** Creates a new instance of LocalizedTemplateGroup */
    public LocalizedTemplateGroup(TemplateGroup tGroup, ExtensibilityElementTemplateProvider provider) {
        this.mTGroup = tGroup;
        this.mProvider = provider;
    }
    
    public String getName() {
        String lName = null;
        String name = TEMPLATE_GROUP + "_name"; //NOI18N
        try {
            lName =  this.mProvider.getLocalizedMessage(name, null);
        } catch (Exception ex) {
            lName = "UNKNOWN_NAME";
        }
        
        return lName;
    }
    
    public String getPrefix() {
        String lPrefix = null;
        String name = TEMPLATE_GROUP + "_prefix_" + this.mTGroup.getPrefix(); //NOI18N
        try {
            lPrefix =  this.mProvider.getLocalizedMessage(name, null);
        } catch (Exception ex) {
            lPrefix = "UNKNOWN_PREFIX";
        }
        
        return lPrefix;
    }
  
    public String getNamespace() {
        return this.mTGroup.getNamespace();
    }
    
    public TemplateGroup getDelegate() {
        return this.mTGroup;
    }
    
    public LocalizedTemplate[] getTemplate() { 
        if(mLtts != null) {
            return mLtts;
        }
        
        TemplateType[] tts = this.mTGroup.getTemplate();
        if(tts != null) {
            mLtts = new LocalizedTemplate[tts.length];
            for(int i =0; i < tts.length; i++) {
                TemplateType tt = tts[i];
                LocalizedTemplate ltt = new LocalizedTemplate(this, tt, this.mProvider);
                mLtts[i] = ltt;
            }
        }
        
        return mLtts;
    }
    
    @Override
    public String toString() {
        return getName();
    }

    public int compareTo(LocalizedTemplateGroup o) {
        return toString().compareTo(o.toString());
    }
}
