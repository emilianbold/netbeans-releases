/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.core.multiview;

import java.awt.event.ActionEvent;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.Presenter;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Action to show in main menu to split document.
 *
 * @author Th. Oikonomou
 */
@Messages({"CTL_SplitDocumentAction=&Split Document", "CTL_SplitAction=&Split", "MultiViewElement.Spliting.Enabled=true"})
public class SplitAction extends AbstractAction implements Presenter.Menu, Presenter.Popup {

    boolean useSplitName = false;

    public SplitAction() {
	super(Bundle.CTL_SplitDocumentAction());
    }

    public SplitAction(boolean useSplitName) {
	super(Bundle.CTL_SplitDocumentAction());
	this.useSplitName = useSplitName;
    }

    static Action createSplitAction(Map map) {
	if(!isSplitingEnabled()) {
	    return null;
	}
	Object nameObj = map.get("displayName"); //NOI18N
	if (nameObj == null) {
	    return null;
	}
	return new SplitAction(nameObj.toString().equals(Bundle.CTL_SplitAction()));
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
	assert false;
    }

    @Override
    public JMenuItem getMenuPresenter() {
	return getSplitMenuItem();
    }

    @Override
    public JMenuItem getPopupPresenter() {
	return getSplitMenuItem();
    }
    
    private JMenuItem getSplitMenuItem() {
	if(!isSplitingEnabled()) {
	    return null;
	}
	JMenu menu = new SplitAction.UpdatingMenu();
	String label = useSplitName ? Bundle.CTL_SplitAction() : Bundle.CTL_SplitDocumentAction();
	Mnemonics.setLocalizedText(menu, label);
	return menu;
    }

    private static boolean isSplitingEnabled() {
	boolean splitingEnabled = "true".equals(Bundle.MultiViewElement_Spliting_Enabled()); // NOI18N
	return splitingEnabled;
    }

    private static final class UpdatingMenu extends JMenu implements DynamicMenuContent {

	@Override
	public JComponent[] synchMenuPresenters(JComponent[] items) {
	    return getMenuPresenters();
	}

	@Override
	public JComponent[] getMenuPresenters() {
	    assert SwingUtilities.isEventDispatchThread() : "Must be called from AWT";
	    removeAll();
	    final TopComponent tc = WindowManager.getDefault().getRegistry().getActivated();
	    if (tc != null) {
		setEnabled(true);
		if (tc instanceof MultiViewTopComponent || tc instanceof MultiViewCloneableTopComponent) {
		    JMenuItem item = new JMenuItem(new SplitDocumentAction(tc, JSplitPane.VERTICAL_SPLIT));
		    Mnemonics.setLocalizedText(item, item.getText());
		    add(item);
		    item = new JMenuItem(new SplitDocumentAction(tc, JSplitPane.HORIZONTAL_SPLIT));
		    Mnemonics.setLocalizedText(item, item.getText());
		    add(item);
		    item = new JMenuItem(new ClearSplitAction(tc));
		    Mnemonics.setLocalizedText(item, item.getText());
		    add(item);
		} else { // tc is not splitable
		    //No reason to enable action on any TC because now it was enabled even for Welcome page
		    setEnabled(false);
		    /*JRadioButtonMenuItem but = new JRadioButtonMenuItem();
		     Mnemonics.setLocalizedText(but, NbBundle.getMessage(EditorsAction.class, "EditorsAction.source"));
		     but.setSelected(true);
		     add(but);*/
		}
	    } else { // tc == null
		setEnabled(false);
	    }
	    return new JComponent[]{this};
	}
    }

    @NbBundle.Messages({"LBL_SplitDocumentActionVertical=&Vertically",
	"LBL_SplitDocumentActionHorizontal=&Horizontally",
	"LBL_ValueSplitVertical=splitVertically",
	"LBL_ValueSplitHorizontal=splitHorizontally"})
    private static class SplitDocumentAction extends AbstractAction {

	private final TopComponent tc;
	private final int orientation;

	public SplitDocumentAction(TopComponent tc, int orientation) {
	    this.tc = tc;
	    this.orientation = orientation;
	    putValue(Action.NAME, orientation == JSplitPane.VERTICAL_SPLIT ? Bundle.LBL_SplitDocumentActionVertical() : Bundle.LBL_SplitDocumentActionHorizontal());
	    //hack to insert extra actions into JDev's popup menu
	    putValue("_nb_action_id_", orientation == JSplitPane.VERTICAL_SPLIT ? Bundle.LBL_ValueSplitVertical() : Bundle.LBL_ValueSplitHorizontal()); //NOI18N
	    if (tc instanceof MultiViewTopComponent) {
		setEnabled(((MultiViewTopComponent) tc).getSplitOrientation() == -1
			|| ((MultiViewTopComponent) tc).getSplitOrientation() != orientation);
	    } else if (tc instanceof MultiViewCloneableTopComponent) {
		setEnabled(((MultiViewCloneableTopComponent) tc).getSplitOrientation() == -1
			|| ((MultiViewCloneableTopComponent) tc).getSplitOrientation() != orientation);
	    } else {
		setEnabled(false);
	    }
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
	    splitWindow(tc, orientation);
	}
    }

    @NbBundle.Messages({"LBL_ClearSplitAction=&Clear",
	"LBL_ValueClearSplit=clearSplit"})
    private static class ClearSplitAction extends AbstractAction {

	private final TopComponent tc;

	public ClearSplitAction(TopComponent tc) {
	    this.tc = tc;
	    putValue(Action.NAME, Bundle.LBL_ClearSplitAction());
	    //hack to insert extra actions into JDev's popup menu
	    putValue("_nb_action_id_", Bundle.LBL_ValueClearSplit()); //NOI18N
	    if (tc instanceof MultiViewTopComponent) {
		setEnabled(((MultiViewTopComponent) tc).getSplitOrientation() != -1);
	    } else if (tc instanceof MultiViewCloneableTopComponent) {
		setEnabled(((MultiViewCloneableTopComponent) tc).getSplitOrientation() != -1);
	    } else {
		setEnabled(false);
	    }
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
	    clearSplit(tc);
	}
    }

    static void splitWindow(TopComponent tc, int orientation) {
	if (tc instanceof MultiViewTopComponent || tc instanceof MultiViewCloneableTopComponent) {
	    TopComponent split;
	    if (tc instanceof MultiViewTopComponent) {
		split = ((MultiViewTopComponent) tc).splitComponent(orientation);
	    } else {
		split = ((MultiViewCloneableTopComponent) tc).splitComponent(orientation);
	    }
	    split.open();
	    split.requestActive();
	}
    }

    static void clearSplit(TopComponent tc) {
	if (tc instanceof MultiViewTopComponent || tc instanceof MultiViewCloneableTopComponent) {
	    TopComponent original;
	    if (tc instanceof MultiViewTopComponent) {
		original = ((MultiViewTopComponent) tc).clearSplit();
	    } else {
		original = ((MultiViewCloneableTopComponent) tc).clearSplit();
	    }
	    original.open();
	    original.requestActive();
	}
    }
}
