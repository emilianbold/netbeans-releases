/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.vmd.midp.codegen;

import org.netbeans.modules.vmd.api.io.ActiveViewSupport;
import org.netbeans.modules.vmd.api.io.providers.IOSupport;
import org.openide.loaders.DataObject;

/**
 *
 * @author Karol Harezlak
 */
public final class MIDPDatabindingCodeSupport {
    
    /**
     * Types of BinderProviders provideres a
     */
    public static enum ProviderType {

        DataField,
        ImageItem,
        Item,
        //   List,
        StringItem,
        TextField
    };
    /**
     * Types of BinderFeatures provideres a
     */
    public static enum FeatureType {

        DataField_FeatureInputDateTime,
        DataField_FEATURE_DATETIME,
        DataField_FEATURE_INPUTMODE,
        ImageItem_FEATURE_IMAGE,
        Item_FEATURE_LABEL,
        //    List,
        StringItem_FEATURE_TEXT,
        TextField_FeatureText
    };

    private MIDPDatabindingCodeSupport() {
    }

    public static String getCodeProviderNama(ProviderType type) {
        switch (type) {
            case DataField:
                return "new DateFieldBindingProvider()"; //NOI18N
            case ImageItem:
                return "new ImageItemBindingProvider()"; //NIOI18N
            case Item:
                return "new ItemBindingProvider()"; //NOI18N
            //  case List:
            //      return "ListBindingProvider()"; //NOI18N
            case StringItem:
                return "new StringItemBindingProvider()"; //NOI18N
            case TextField:
                return "new TextFieldBindingProvider()"; //NOI18N    
        }
        throw new IllegalArgumentException();
    }

    public static String getCodeFeatureName(FeatureType type) {
        
        final DataObject dao = ActiveViewSupport.getDefault().getActiveView().getContext().getDataObject();
        IOSupport.getDataObjectInteface(dao).getEditorDocument();
                
        switch (type) {
            case DataField_FEATURE_DATETIME:
                return "DateFieldBindingProvider.FEATURE_DATETIME"; //NOI18N
            case DataField_FEATURE_INPUTMODE:
                return "DateFieldBindingProvider.FEATURE_INPUTMODE"; //NOI18N
            case DataField_FeatureInputDateTime:
                return "FeatureText(false)"; //NIOI18N
            case ImageItem_FEATURE_IMAGE:
                return "ImageItemBindingProvider.FEATURE_IMAGE"; //NOI18N
            //   case List:
            //       return "ListBindingProvider()"; //NOI18N
            case Item_FEATURE_LABEL:
                return "ItemBindingProvider.FEATURE_LABEL"; //NOI18N
            case StringItem_FEATURE_TEXT:
                return "StringItemBindingProvider.FEATURE_TEXT"; //NOI18N   
            case TextField_FeatureText:
                return "new TextFieldBindingProvider.FeatureText(false)"; //NOI18N
        }
        throw new IllegalArgumentException();
    }
    
    
}
