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
package org.netbeans.modules.websvc.rest.nodes;

import java.awt.Image;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.websvc.rest.model.api.HttpMethod;
import org.openide.nodes.AbstractNode;
import org.netbeans.modules.websvc.rest.model.api.RestServicesMetadata;
import org.openide.nodes.Children;

public class HttpMethodNode extends AbstractNode{
    private String methodName;
    private String produceMime;
    private String consumeMime;
    private String returnType;
    private MetadataModel<RestServicesMetadata> model;
    
    public HttpMethodNode(MetadataModel<RestServicesMetadata> model, HttpMethod method) {
        super(Children.LEAF);
        this.methodName = method.getName();
        this.produceMime = method.getProduceMime();
        this.consumeMime = method.getConsumeMime();
        this.returnType = method.getReturnType();
    }
    
    public String getDisplayName() {
        if (consumeMime.length() > 0 || produceMime.length() > 0) {
            return methodName + "() : " + Utils.stripPackageName(returnType) +
                    " [\"" + consumeMime + "\" \"" + produceMime + "\"]";
        } else {
            return methodName + "() : " + Utils.stripPackageName(returnType);
        }
    }
    
    public String getShortDescription() {
        return "";
    }
    
    private static final java.awt.Image METHOD_BADGE =
            org.openide.util.Utilities.loadImage( "org/netbeans/modules/websvc/rest/nodes/resources/method.png" ); //NOI18N
    
    public java.awt.Image getIcon(int type) {
        return METHOD_BADGE;
    }
    
    void changeIcon() {
        fireIconChange();
    }
    
    public Image getOpenedIcon(int type){
        return getIcon( type);
    }
}
