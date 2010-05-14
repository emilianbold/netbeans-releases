/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.encoder.ui.basic;

/**
 * The instance of this class represents the encoding mark held in an XSD if it
 * has encoding style applied.
 *
 * @author Jun Xu
 */
public class EncodingMark {
    
    /**
     * Encoding style name
     */
    private final String mName;
    
    /**
     * Encoding style namespace used in AppInfo
     */
    private final String mNamespace;
    
    /**
     * Encoding style ID
     */
    private final String mStyle;
    
    /** Creates a new instance of EncodingMark
     * @param name encoding style name, e.g. "Custom Encoding"
     * @param namespace namespace of encoding style e.g. "urn:com.sun:encoder-custom-1.0"
     * @param style encoding style ID, e.g. "customencoder-1.0"
     */
    public EncodingMark(String name, String namespace, String style) {
        mName = name;
        mNamespace = namespace;
        mStyle = style;
    }
    
    public String getName() {
        return mName;
    }
    
    public String getNamespace() {
        return mNamespace;
    }
    
    public String getStyle() {
        return mStyle;
    }
}
