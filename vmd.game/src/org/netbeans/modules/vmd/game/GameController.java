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

import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.DesignDocumentAwareness;
import org.netbeans.modules.vmd.api.io.IOUtils;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.game.model.GlobalRepository;
import org.netbeans.modules.vmd.game.model.SceneCD;
import org.netbeans.modules.vmd.game.view.main.MainView;

/**
 *
 * @author Karel Herink
 */
public class GameController implements DesignDocumentAwareness {
	
	public static final String PROJECT_TYPE_GAME = "vmd-midp-game"; // NOI18N

	private DataObjectContext context;
	private JComponent loadingPanel;
	private JComponent visual;
	
	private DesignDocument document;
	
	
	/** Creates a new instance of GameController */
	public GameController(DataObjectContext context) {
		this.context = context;
		this.loadingPanel = IOUtils.createLoadingPanel();
		this.visual = this.loadingPanel;
		this.context.addDesignDocumentAwareness(this);
	}
	
	public JComponent getVisualRepresentation() {
		return this.visual;
	}
	
	public void setDesignDocument(final DesignDocument designDocument) {
        this.document = designDocument;
		if (this.document == null) {
			this.visual = this.loadingPanel;
			return;
		}
		this.document.getTransactionManager().readAccess(new Runnable() {
			public void run() {
				//clean my internal model - remove all scenes, layers, and image resources
				//GlobalRepository.getInstance().removeAllComponents();
				
				GameController.this.visual = MainView.getInstance().getRootComponent();
				
				//add all components in the document
				DesignComponent root = designDocument.getRootComponent();
				Collection<DesignComponent> children = root.getComponents();
				
				//do this recursively - to start at the bottom of the hierarchy
				
				//for (DesignComponent designComponent : children) {
				//	GameController.this.modelComponent(designComponent);
				//}
			}
		});
		
	}
	
	private void modelComponent(DesignComponent designComponent) {
		TypeID typeId = designComponent.getType();
		if (typeId == SceneCD.TYPEID) {
			//load scene
		}
	}
	
}
