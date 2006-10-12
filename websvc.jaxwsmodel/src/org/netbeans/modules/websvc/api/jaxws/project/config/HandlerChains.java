
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
 * HandlerChains.java
 *
 * Created on March 19, 2006, 8:54 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.api.jaxws.project.config;

import java.beans.PropertyChangeListener;
import java.io.OutputStream;
import org.netbeans.modules.schema2beans.BaseBean;
/**
 *
 * @author Roderico Cruz
 */
public class HandlerChains {
     private org.netbeans.modules.websvc.jaxwsmodel.handler_config1_0.HandlerChains handlerChains;
    /** Creates a new instance of HandlerChains */
    public HandlerChains(org.netbeans.modules.websvc.jaxwsmodel.handler_config1_0.HandlerChains handlerChains) {
        this.handlerChains = handlerChains;
    }
    
    public HandlerChain[] getHandlerChains() {
        org.netbeans.modules.websvc.jaxwsmodel.handler_config1_0.HandlerChain[] chains = handlerChains.getHandlerChain();
        HandlerChain[] newChains = new HandlerChain[chains.length];
        for (int i=0;i<chains.length;i++) {
            newChains[i]=new HandlerChain(chains[i]);
        }
        return newChains;
    }
    
    public HandlerChain newChain() {
        org.netbeans.modules.websvc.jaxwsmodel.handler_config1_0.HandlerChain chain = handlerChains.newHandlerChain();
        return new HandlerChain(chain);
    }
    
    public void addHandlerChain(String handlerName, HandlerChain chain) {
        handlerChains.addHandlerChain((org.netbeans.modules.websvc.jaxwsmodel.handler_config1_0.HandlerChain)chain.getOriginal());
    }
    
    public void removeHandlerChain(HandlerChain chain) {
        handlerChains.removeHandlerChain((org.netbeans.modules.websvc.jaxwsmodel.handler_config1_0.HandlerChain)chain.getOriginal());
    }
    
    public HandlerChain findHandlerChainByName(String handlerChainName) {
        HandlerChain[] chains = getHandlerChains();
        for (int i=0;i<chains.length;i++) {
            if (handlerChainName.equals(chains[i].getHandlerChainName())) return chains[i];
        }
        return null;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        handlerChains.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        handlerChains.removePropertyChangeListener(l);
    }
    
    public void merge(HandlerChains newChains) {
        if (newChains.handlerChains!=null)
            handlerChains.merge(newChains.handlerChains,BaseBean.MERGE_UPDATE);
    }
    
    public void write(OutputStream os) throws java.io.IOException {
        handlerChains.write(os);
    }
    
}
