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

package org.netbeans.modules.vmd.game;

import org.netbeans.modules.vmd.api.model.*;
import javax.swing.*;
import java.util.Collection;
import org.netbeans.modules.vmd.game.model.GlobalRepository;

/**
 *
 * @author kherink
 */
public class GameAccessController implements AccessController {

    public static final boolean DEBUG = true;
    
    private DesignDocument document;
	
	private GlobalRepository gameDesign;
	
	/** Creates a new instance of GameAccessController */
	public GameAccessController(DesignDocument document) {
		if (DEBUG) System.out.println("new GameAccessController() with document: " + document);  //NOI18N
		this.document = document;
		this.gameDesign = new GlobalRepository(document);
	}
	
	public GlobalRepository getGameDesign() {
		return this.gameDesign;
	}
	
	
	public void writeAccess(Runnable runnable) {
		runnable.run();
	}
	
	public void notifyEventFiring(DesignEvent event) {
	}
	
	public void notifyEventFired(DesignEvent event) {
//		if (DEBUG) System.out.println("GameAccessController.notifyEventFired() : " + event);
	}
	
	public void notifyComponentsCreated(Collection<DesignComponent> createdComponents) {
	}
	
    public static class Factory implements AccessControllerFactory {
        public AccessController createAccessController(DesignDocument document) {
            return new GameAccessController (document);
        }
    }
}
