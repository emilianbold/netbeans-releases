
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
 * HandlerChain.java
 *
 * Created on March 19, 2006, 9:02 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.api.jaxws.project.config;

/**
 *
 * @author rico
 */
public class HandlerChain {
    private org.netbeans.modules.websvc.jaxwsmodel.handler_config1_0.HandlerChain chain;
    /** Creates a new instance of HandlerChain */
    public HandlerChain(org.netbeans.modules.websvc.jaxwsmodel.handler_config1_0.HandlerChain chain) {
        this.chain=chain;
    }
    
    Object getOriginal() {
        return chain;
    }
    
    public String getHandlerChainName() {
        return chain.getHandlerChainName();
    }
    
    public Handler[] getHandlers() {
        org.netbeans.modules.websvc.jaxwsmodel.handler_config1_0.Handler[] handlers = chain.getHandler();
        Handler[] newHandlers = new Handler[handlers.length];
        for (int i=0;i<handlers.length;i++) {
            newHandlers[i]=new Handler(handlers[i]);
        }
        return newHandlers;
    }
    
    public void setHandlerChainName(String value) {
        chain.setHandlerChainName(value);
    }
    
    public Handler newHandler() {
        org.netbeans.modules.websvc.jaxwsmodel.handler_config1_0.Handler handler = chain.newHandler();
        return new Handler(handler);
    }
    
    public void addHandler(String handlerName, String handlerClass) {
        org.netbeans.modules.websvc.jaxwsmodel.handler_config1_0.Handler handler = chain.newHandler();
        handler.setHandlerName(handlerName);
        handler.setHandlerClass(handlerClass);
        chain.addHandler(handler);
    }
    
    public boolean removeHandler(String handlerNameOrClass) {
        org.netbeans.modules.websvc.jaxwsmodel.handler_config1_0.Handler[] handlers = chain.getHandler();
        for (int i=0;i<handlers.length;i++) {
            if (handlerNameOrClass.equals(handlers[i].getHandlerName()) || 
                    handlerNameOrClass.equals(handlers[i].getHandlerClass())) {
                chain.removeHandler(handlers[i]);
                return true;
            }
        }
        return false;
    }
    
    public Handler findHandlerByName(String handlerName) {
        Handler[] handlers = getHandlers();
        for (int i=0;i<handlers.length;i++) {
            if (handlerName.equals(handlers[i].getHandlerName())) return handlers[i];
        }
        return null;
    }
}
