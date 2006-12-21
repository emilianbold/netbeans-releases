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
package org.netbeans.modules.vmd.api.model.common;

import java.util.Collection;

import org.netbeans.modules.vmd.api.model.AccessController;
import org.netbeans.modules.vmd.api.model.AccessControllerFactory;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.DesignEvent;

/**
 *
 * @author Karol Harezlak
 */
public class TestAccessController implements AccessController {

    public static final String CONTROLER_ID = "TEST_CONTROLLER_ID";
    private boolean notifyEventFiringFlag = false;
    private boolean notifyEventFiredFlag = false;
    private boolean notifyComponentsCreated = false;
    private Collection<DesignComponent> createComponents;
    
    /**
     * Creates a new instance of MyAccessController
     */
    public TestAccessController()    {
    
    }
    
    public String getControllerID() {
        return TestAccessController.CONTROLER_ID;
    }
    
    public void writeAccess(Runnable runnable) {
        runnable.run();
    }
    
    public void notifyEventFiring(DesignEvent event) {
        notifyEventFiringFlag = true;
        System.out.println("notifyEventFiring, MyAccessController"); // NOI18N
    }
    
    public void notifyEventFired(DesignEvent event) {
        notifyEventFiredFlag = true;
        System.out.println("notifyEventFired, MyAccessController"); // NOI18N
    }
    
    public void notifyComponentsCreated(Collection<DesignComponent> createdComponents) {
        notifyComponentsCreated = true;
        this.createComponents = createdComponents;
        for (DesignComponent component: createdComponents){
            System.out.println("Created component: "+component);
        }
        System.out.println("notifyComponentCreated, MyAccessController"); // NOI18N
    }
    
    public boolean isNotifyEventFiringFlag(){
        return notifyEventFiringFlag;
    }
    
    public boolean isNotifyEventFiredFlag(){
        return notifyEventFiredFlag;
    }
    
    public boolean isNotifyComponentsCreated(){
        return notifyComponentsCreated;
    }
    
    public void resetFlags(){
        notifyComponentsCreated = false;
        notifyEventFiredFlag = false;
        notifyEventFiringFlag = false;
    }

    public Collection getCreatedComponents(){
        return createComponents;
    }
    
    public static class Factory implements AccessControllerFactory{
        public AccessController createAccessController(DesignDocument document) {
            return new TestAccessController();
        }
    }
    
}
