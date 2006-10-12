
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
 *//*
 * Handler.java
 *
 * Created on March 19, 2006, 9:04 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.api.jaxws.project.config;

/**
 *
 * @author rico
 */
public class Handler {
    org.netbeans.modules.websvc.jaxwsmodel.handler_config1_0.Handler handler;
    /** Creates a new instance of Handler */
    public Handler(org.netbeans.modules.websvc.jaxwsmodel.handler_config1_0.Handler handler) {
        this.handler=handler;
    }
    
    public String getHandlerName() {
        return handler.getHandlerName();
    }
    public String getHandlerClass() {
        return handler.getHandlerClass();
    }
    public void setHandlerName(String name) {
        handler.setHandlerName(name);
    }
    public void setHandlerClass(String value) {
        handler.setHandlerClass(value);
    }
    
}
