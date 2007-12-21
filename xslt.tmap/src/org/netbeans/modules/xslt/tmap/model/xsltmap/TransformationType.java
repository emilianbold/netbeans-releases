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
package org.netbeans.modules.xslt.tmap.model.xsltmap;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public enum TransformationType {
    REQUEST_REPLY_SERVICE("requestReplyService"), // NOI18N
    FILTER_ONE_WAY("filterOneWay", // NOI18N
    new TransformationDescType[] {TransformationDescType.INPUT,
                                    TransformationDescType.OUTPUT}), 
    FILTER_REQUEST_REPLY("filterRequestReply", // NOI18N
        new TransformationDescType[] {TransformationDescType.INPUT,
                                    TransformationDescType.OUTPUT}); 
    
    private String tagName;
    private TransformationDescType[] transformationDescs;
    private static Map<String, TransformationType> MAP_TAG_NAME_TO_TYPE = new HashMap<String, TransformationType>();
    static {
        for (TransformationType transformType : TransformationType.values()) {
            MAP_TAG_NAME_TO_TYPE.put(transformType.getTagName(), transformType);
        }
    }
    
    private TransformationType(String tagName) {
        this(tagName, new TransformationDescType[] {TransformationDescType.INPUT});
    }
    
    private TransformationType(String tagName, TransformationDescType[] tDescs) {
        this.tagName = tagName;
        this.transformationDescs = tDescs;
    }

    public static TransformationType getTypeByTagName(String tagName) {
        return MAP_TAG_NAME_TO_TYPE.get(tagName);
    }
    
    public String getTagName() {
        assert tagName != null && !"".equals(tagName); // NOI18N
        return tagName;
    }
    
    public TransformationDescType[] getTransformationDescs() {
        return transformationDescs;
    }
    
}
