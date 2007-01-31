/*
 * GameBuilderNavigator.java
 *
 * Created on January 31, 2007, 8:56 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.vmd.game;

import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.netbeans.modules.vmd.game.model.Editable;
import org.netbeans.modules.vmd.game.model.EditorManagerListener;
import org.netbeans.modules.vmd.game.view.main.MainView;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.util.Lookup;

/**
 *
 * @author kaja
 */
public class GameBuilderNavigator extends JPanel implements NavigatorPanel, EditorManagerListener {
    
    /** Creates a new instance of GameBuilderNavigator */
    public GameBuilderNavigator() {
		System.out.println("GameBuilderNavigator instance created!");
		MainView.getInstance().addEditorManagerListener(this);
		this.setLayout(new BorderLayout());
		Editable editable = MainView.getInstance().getCurrentEditable();
		if (editable == null)
			return;
		JComponent navigator = editable.getNavigator();
		this.setNavigator(navigator);
    }
    
    public String getDisplayName() {
        return "Game Builder Navigator";
    }

    public String getDisplayHint() {
        return "Show logical structure of game components.";
    }

    public JComponent getComponent() {
        return this;
    }

    public void panelActivated(Lookup context) {
    }

    public void panelDeactivated() {
    }

    public Lookup getLookup() {
        return null;
    }
    
	private void setNavigator(JComponent navigator) {
		System.out.println("setNavigator: " + navigator);
		this.removeAll();
		if (navigator != null) {
			this.add(new JScrollPane(navigator), BorderLayout.CENTER);
		}
		else {
			this.add(new JLabel("<No structure available>"), BorderLayout.CENTER);
		}
		this.validate();
		this.repaint();
	}
    
    public void editing(Editable e) {
		this.setNavigator(e.getNavigator());
    }


}
