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
package org.netbeans.modules.websvc.jaxrpc.project;

import java.util.HashMap;
import java.util.Map;
import org.openide.util.NbBundle;

/**
 *
 * @author rico
 * Table containing wscompile options and corresponding description
 */
public class WsCompileFeatures {
    
    static Map<String, String> featuresMap = null;
    /** Creates a new instance of WsCompileFeatures */
    private WsCompileFeatures() {
    }
    
    public static Map<String, String> getFeaturesMap(){
        if(featuresMap == null){
            featuresMap = new HashMap<String, String>();
            featuresMap.put("datahandleronly", NbBundle.getMessage(WsCompileFeatures.class, "DESC_DATAHANDLERONLY"));
            featuresMap.put("documentliteral", NbBundle.getMessage(WsCompileFeatures.class, "DESC_DOCUMENTLITERAL"));
            featuresMap.put("rpcliteral", NbBundle.getMessage(WsCompileFeatures.class, "DESC_RPCLITERAL"));
            featuresMap.put("explicitcontext", NbBundle.getMessage(WsCompileFeatures.class, "DESC_EXPLICITCONTEXT"));
            featuresMap.put("jaxbenumtype", NbBundle.getMessage(WsCompileFeatures.class, "DESC_JAXBENUMTYPE"));
            featuresMap.put("nodatabinding", NbBundle.getMessage(WsCompileFeatures.class, "DESC_NODATABINDING"));
            featuresMap.put("noencodedtypes", NbBundle.getMessage(WsCompileFeatures.class, "DESC_NOENCODEDTYPES"));
            featuresMap.put("nomultirefs" , NbBundle.getMessage(WsCompileFeatures.class, "DESC_NOMULTIREFS"));
            featuresMap.put("norpcstructures", NbBundle.getMessage(WsCompileFeatures.class, "DESC_NORPCSTRUCTURES"));
            featuresMap.put("novalidation", NbBundle.getMessage(WsCompileFeatures.class, "DESC_NOVALIDATION"));
            featuresMap.put("resolveidref", NbBundle.getMessage(WsCompileFeatures.class, "DESC_RESOLVEIDREF"));
            featuresMap.put("searchschema", NbBundle.getMessage(WsCompileFeatures.class, "DESC_SEARCHSCHEMA"));
            featuresMap.put("serializeinterfaces", NbBundle.getMessage(WsCompileFeatures.class, "DESC_SERIALIZEINTERFACES"));
            featuresMap.put("strict", NbBundle.getMessage(WsCompileFeatures.class, "DESC_STRICT"));
            featuresMap.put("useonewayoperations", NbBundle.getMessage(WsCompileFeatures.class, "DESC_USEONEWAYOPERATIONS"));
            featuresMap.put("wsi", NbBundle.getMessage(WsCompileFeatures.class, "DESC_WSI"));
            featuresMap.put("unwrap", NbBundle.getMessage(WsCompileFeatures.class, "DESC_UNWRAP"));
            featuresMap.put("donotoverride", NbBundle.getMessage(WsCompileFeatures.class, "DESC_DONOTOVERRIDE"));
            featuresMap.put("donotunwrap", NbBundle.getMessage(WsCompileFeatures.class, "DESC_DONOTUNWRAP"));
        }
        return featuresMap;
    }
}
