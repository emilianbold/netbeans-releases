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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
