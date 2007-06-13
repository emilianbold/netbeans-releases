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

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.util.Collection;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.common.ActiveDocumentSupport;
import org.netbeans.modules.vmd.game.model.Editable;
import org.netbeans.modules.vmd.game.model.EditorManagerListener;
import org.netbeans.modules.vmd.game.model.GlobalRepository;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.util.Lookup;

/**
 *
 * @author Karel Herink
 */
public class GameBuilderNavigator extends JPanel implements NavigatorPanel, EditorManagerListener {
    
	private GlobalRepository gameDesign;
	private ADSL listener;

	public GameBuilderNavigator() {
		this.setNavigator(null);
		this.listener = new ADSL();
	}
	    
    public String getDisplayName() {
        return "Game Builder Navigator";
    }

    public String getDisplayHint() {
        return "Show logical structure of game design components.";
    }

    public JComponent getComponent() {
        return this;
    }

    public void panelActivated(Lookup context) {
		System.out.println("panelActivated()");
		ActiveDocumentSupport.getDefault().addActiveDocumentListener(this.listener);

		final JComponent[] navigator = {null};
		final DesignDocument doc = ActiveDocumentSupport.getDefault().getActiveDocument();
		System.out.println("\tActive document: " + doc );
		if (doc == null)
			return;
		doc.getTransactionManager().readAccess(new Runnable() {
			public void run() {
					GameAccessController controller = doc.getListenerManager().getAccessController(GameAccessController.class);
					if (controller == null)
						return;
					GameBuilderNavigator.this.gameDesign = controller.getGameDesign();
					System.out.println("\t\t gameDesign = " + controller.getGameDesign());
					GameBuilderNavigator.this.gameDesign.getMainView().addEditorManagerListener(GameBuilderNavigator.this);
					Editable editable = GameBuilderNavigator.this.gameDesign.getMainView().getCurrentEditable();
					if (editable == null)
						return;
					navigator[0] = editable.getNavigator();
			}			
		});
		this.setNavigator(navigator[0]);
    }

    public void panelDeactivated() {
		ActiveDocumentSupport.getDefault().removeActiveDocumentListener(this.listener);
    }

    public Lookup getLookup() {
        return null;
    }
    
	private void setNavigator(JComponent navigator) {
		System.out.println("setNavigator: " + navigator);
		this.removeAll();
		if (navigator != null) {
			this.setLayout(new BorderLayout());
			this.add(new JScrollPane(navigator), BorderLayout.CENTER);
		}
		else {
			this.setLayout(new GridBagLayout());
			this.add(new JLabel("<No structure available>"));
		}
		this.validate();
		this.repaint();
	}
    
    public void editing(Editable editable) {
		System.out.println("\tediting: " + editable);
		this.setNavigator(editable.getNavigator());
    }

	private class ADSL implements ActiveDocumentSupport.Listener {
		public void activeDocumentChanged(DesignDocument deactivatedDocument, final DesignDocument activatedDocument) {
			System.out.println("activeDocumentChanged: " + activatedDocument);
			if (deactivatedDocument == activatedDocument)
				return;
			if (activatedDocument == null) {
				setNavigator(null);
				return;
			}
			final JComponent[] navigator = {null};
			activatedDocument.getTransactionManager().readAccess(new Runnable() {
				public void run () {
					GameAccessController controller = activatedDocument.getListenerManager().getAccessController(GameAccessController.class);
					GameBuilderNavigator.this.gameDesign = controller.getGameDesign();
					GameBuilderNavigator.this.gameDesign.getMainView().addEditorManagerListener(GameBuilderNavigator.this);
					Editable editable = GameBuilderNavigator.this.gameDesign.getMainView().getCurrentEditable();
					if (editable == null)
						return;
					navigator[0] = editable.getNavigator();
				}
			});
			GameBuilderNavigator.this.setNavigator(navigator[0]);

		}
		public void activeComponentsChanged(Collection<DesignComponent> activeComponents) {
		}
	}

}
