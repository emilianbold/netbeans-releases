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

package org.netbeans.modules.iep.model;

import org.netbeans.modules.xml.xam.dom.ComponentFactory;

/**
 *
 * 
 * Factory for providing concrete implementations of WLMComponent
 */

public interface IEPComponentFactory extends ComponentFactory<IEPComponent> {

        /*
	TImport createImport(WLMModel model);
 
        TTasks createTasks (WLMModel model);
 	
        TTask createTask(WLMModel model);
        
	TAssignment createAssignment (WLMModel model);
        
	User createUser (WLMModel model) ;
	
	Group createGroup (WLMModel model);
	
	Role createRole (WLMModel model);
        
	TTimeout createTimeout (WLMModel model);
        
	TDeadlineExpr createDeadline (WLMModel model);
        
	TDurationExpr createDuration (WLMModel model);
	
	TEscalation createEscalation (WLMModel model);
	
	TNotification createNotification (WLMModel model);
	
	TAction createAction (WLMModel model);
        
	TRecipient createRecipient (WLMModel model);
        
	TAddress createAddress (WLMModel model);
        */
}
