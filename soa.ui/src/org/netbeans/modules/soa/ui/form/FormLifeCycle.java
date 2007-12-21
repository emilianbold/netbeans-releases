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
package org.netbeans.modules.soa.ui.form;

/**
 *
 * @author nk160297
 */
public interface FormLifeCycle {
    
    /**
     * This method is intended to create controls.
     * Sometimes an initialization can be performed here.
     */
    void createContent();
    
    /**
     * This method is intended to initialize controls with some data.
     * Be aware that the special method is intended to do event subscription. 
     *
     * @return the completion status flag. The false value indicates 
     * that it necessary to look for a child component with Life Cycle.
     */
    boolean initControls();
    
    /**
     * This method is intended to subscribe listeners. 
     * So after the method is called, chooser starts receiving events.
     *
     * @return the completion status flag. The false value indicates 
     * that it necessary to look for a child component with Life Cycle.
     */
    boolean subscribeListeners();
    
    /**
     * This method is intended to unsubscribe listeners. 
     * So after the method is called, chooser stop receiving events.
     *
     * @return the completion status flag. The false value indicates 
     * that it necessary to look for a child component with Life Cycle.
     */
    boolean unsubscribeListeners();
    
    /**
     * This method is intended to perform different finalization activities after closing.
     * For example, listeners cab be unsubscribed here.
     *
     * @return the completion status flag. The false value indicates 
     * that it necessary to look for a child component with Life Cycle.
     */
    boolean afterClose();
    
}
