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
 * LocalizedTemplateGroup.java
 *
 * Created on September 1, 2006, 12:15 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.bindingsupport.template.localized;

import java.util.ArrayList;
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
    
        
    private LocalizedTemplate skeletonTemplate = null;
    
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
            List<LocalizedTemplate> lttList = new ArrayList<LocalizedTemplate>();
            //mLtts = new LocalizedTemplate[tts.length];
            for(int i =0; i < tts.length; i++) {
                TemplateType tt = tts[i];
                if (!tt.isSkeleton()) {
                    LocalizedTemplate ltt = new LocalizedTemplate(this, tt, this.mProvider);
                    lttList.add(ltt);
                } else {
                    skeletonTemplate = new LocalizedTemplate(this, tt, mProvider);
                }
            }
            mLtts = lttList.toArray(new LocalizedTemplate[lttList.size()]);
        }
        
        return mLtts;
    }
    
    public LocalizedTemplate getSkeletonTemplate() {
        if (mLtts != null && skeletonTemplate !=  null) {
            return skeletonTemplate;
        }
        getTemplate();
        return skeletonTemplate;
    }
    
    @Override
    public String toString() {
        return getName();
    }

    public int compareTo(LocalizedTemplateGroup o) {
        return toString().compareTo(o.toString());
    }
}
