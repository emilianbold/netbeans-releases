/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
import org.openide.util.NbBundle;

/**
 *
 * @author Karel Herink
 */
public class GameBuilderNavigator extends JPanel implements NavigatorPanel, EditorManagerListener {
    
	private final static boolean DEBUG = false;
	
	private GlobalRepository gameDesign; 
	private ADSL listener;

	public GameBuilderNavigator() {
		this.setNavigator(null);
		this.listener = new ADSL();
	}
	    
    public String getDisplayName() {
        return NbBundle.getMessage(GameBuilderNavigator.class, "GameBuilderNavigator.DisplayName");
    }

    public String getDisplayHint() {
        return NbBundle.getMessage(GameBuilderNavigator.class, "GameBuilderNavigator.DisplayHint");
    }

    public JComponent getComponent() {
        return this;
    }

    public void panelActivated(Lookup context) {
		if (DEBUG) System.out.println("panelActivated()"); // NOI18N
		ActiveDocumentSupport.getDefault().addActiveDocumentListener(this.listener);

		final JComponent[] navigator = {null};
		final DesignDocument doc = ActiveDocumentSupport.getDefault().getActiveDocument();
		if (DEBUG) System.out.println("\tActive document: " + doc ); // NOI18N
		if (doc == null)
			return;
		doc.getTransactionManager().readAccess(new Runnable() {
			public void run() {
                GameAccessController controller = doc.getListenerManager().getAccessController(GameAccessController.class);
                if (controller == null)
                    return;
                GameBuilderNavigator.this.gameDesign = controller.getGameDesign();
                if (DEBUG) System.out.println("\t\t gameDesign = " + controller.getGameDesign()); // NOI18N
                GameBuilderNavigator.this.gameDesign.getMainView().addEditorManagerListener(GameBuilderNavigator.this);
                Editable editable = GameBuilderNavigator.this.gameDesign.getMainView().getCurrentEditable();
                if (editable == null) {
                    return;
                }
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
		if (DEBUG) System.out.println("setNavigator: " + navigator); // NOI18N
		this.removeAll();
		if (navigator != null) {
			this.setLayout(new BorderLayout());
			this.add(new JScrollPane(navigator), BorderLayout.CENTER);
		}
		else {
			this.setLayout(new GridBagLayout());
			this.add(new JLabel(NbBundle.getMessage(GameBuilderNavigator.class, "GameBuilderNavigator.no_structure_lbl")));
		}
		this.validate();
		this.repaint();
	}
    
    public void editing(Editable editable) {
		if (DEBUG) System.out.println("\tediting: " + editable); // NOI18N
		this.setNavigator(editable.getNavigator());
    }

	private class ADSL implements ActiveDocumentSupport.Listener {
		public void activeDocumentChanged(DesignDocument deactivatedDocument, final DesignDocument activatedDocument) {
			if (DEBUG) System.out.println("activeDocumentChanged: " + activatedDocument); // NOI18N
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
