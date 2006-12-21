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

/**
 *
 * @author Karel Herink
 */
public class GameController implements DesignDocumentAwareness {
	
	public static final String PROJECT_TYPE_GAME = "vmd-midp-game"; // NOI18N

	private DataObjectContext context;
	private JComponent loadingPanel;
	private JPanel visual;
	
	private DesignDocument designDocument;
	private JComponent view;
	
	
	/** Creates a new instance of GameController */
	public GameController(DataObjectContext context) {
		this.context = context;
		this.loadingPanel = IOUtils.createLoadingPanel();
		this.visual = new JPanel(new BorderLayout());
		this.context.addDesignDocumentAwareness(this);
	}
	
	public JComponent getVisualRepresentation() {
		return visual;
	}
	
	public void setDesignDocument(final DesignDocument designDocument) {
		IOUtils.runInAWTNoBlocking(new Runnable() {
			public void run() {
				GameController.this.designDocument = designDocument;

				GameAccessController accessController = designDocument != null ? designDocument.getListenerManager ().getAccessController (GameAccessController.class) : null;
				view = accessController != null ? accessController.createView() : null;

				visual.removeAll();

				if (view != null) {
					visual.add(view, BorderLayout.CENTER);
				} else
					visual.add(loadingPanel, BorderLayout.CENTER);

			}
		});
	}
	
}
