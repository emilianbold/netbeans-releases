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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.uml.ui.support.wizard;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;

import javax.swing.JPanel;

/**
 * A simpler alternative to a JPanel with a CardLayout.  The AWT CardLayout
 * layout manager can be inconvenient to use because the special "stack of
 * cards" operations it supports require a cast to use.  For example to show
 * the card named "myCard" given a JPanel with a CardLayout one would write:
 * <pre>
 * ((CardLayout)(myJPanel.getLayout())).show(myJPanel, "myCard");
 * </pre>
 * This doesn't work well with Swing - all of the CardLayout display operations, 
 * like <code>show</code> call validate directly.  Swing supports automatic
 * validation (see JComponent.revalidate()); this direct call to validate is
 * inefficient.
 * <p>
 * The CardPane JPanel subclass is intended to support a layout with a modest number
 * of cards, on the order of 100 or less.  A cards name is it's component 
 * name, as in java.awt.Component.getName(), which is set when the component
 * is added to the CardPanel:
 * <pre>
 * myCardPanel.add(myChild, "MyChildName");
 * myChild.getName() <i>=> "MyChildName"</i>
 * </pre>
 * As with CardLayout, the first child added to a CardPanel is made visible 
 * and there's only one child visible at a time.  The <code>showCard</code>
 * method accepts either a childs name or the child itself:
 * <pre>
 * myCardPanel.show("MyChildName");
 * myCardPanel.show(myChild);
 * </pre>
 * <p>
 * The CardPanel class doesn't support the vgap/hgap CardLayout properties since
 * one can add a Border, see JComponent.setBorder().
 */

public class CardPanel extends JPanel {

	private IWizardSheet m_ParentSheet = null;

	private static class Layout implements LayoutManager {
		/** 
		 * Set the childs name (if non-null) and and make it visible 
		 * iff it's the only CardPanel child.
		 * @see java.awt.Component#setName
		 */
		public void addLayoutComponent(String name, Component child) {
			if (name != null) {
				child.setName(name);
			}
			child.setVisible(child.getParent().getComponentCount() == 1);
		}

		/** 
		 * If this child was visible, then make the first remaining
		 * child visible.
		 */
		public void removeLayoutComponent(Component child) {
			if (child.isVisible()) {
				Container parent = child.getParent();
				if (parent.getComponentCount() > 0) {
					parent.getComponent(0).setVisible(true);
				}
			}
		}

		/**
		 * @return the maximum preferred width/height + the parents insets
		 */
		public Dimension preferredLayoutSize(Container parent) {
			int nChildren = parent.getComponentCount();
			Insets insets = parent.getInsets();
			int width = insets.left + insets.right;
			int height = insets.top + insets.bottom;

			for (int i = 0; i < nChildren; i++) {
				Dimension d = parent.getComponent(i).getPreferredSize();
				if (d.width > width) {
					width = d.width;
				}
				if (d.height > height) {
					height = d.height;
				}
			}
			return new Dimension(width, height);
		}

		/**
		 * @return the maximum minimum width/height + the parents insets
		 */
		public Dimension minimumLayoutSize(Container parent) {
			int nChildren = parent.getComponentCount();
			Insets insets = parent.getInsets();
			int width = insets.left + insets.right;
			int height = insets.top + insets.bottom;

			for (int i = 0; i < nChildren; i++) {
				Dimension d = parent.getComponent(i).getMinimumSize();
				if (d.width > width) {
					width = d.width;
				}
				if (d.height > height) {
					height = d.height;
				}
			}
			return new Dimension(width, height);
		}

		public void layoutContainer(Container parent) {
			int nChildren = parent.getComponentCount();
			Insets insets = parent.getInsets();
			for (int i = 0; i < nChildren; i++) {
				Component child = parent.getComponent(i);
				if (child.isVisible()) {
					Rectangle r = parent.getBounds();
					int width = r.width - insets.left + insets.right;
					int height = r.height - insets.top + insets.bottom;
					child.setBounds(insets.left, insets.top, width, height);
					break;
				}
			}
		}
	}


	/**
	 * Creates a CardPanel.  Children, called "cards" in this API, should be added 
	 * with add().  The first child we be made visible, subsequent children will 
	 * be hidden.  To show a card, use one of the show*Card methods.
	 */
	public CardPanel() {
		super(new Layout());
	}

	public CardPanel(IWizardSheet pParentSheet) {
		this();
		this.m_ParentSheet = pParentSheet;
	}

	/** 
	 * Return the index of the first (and one would hope - only)
	 * visible child.  If a visible child can't be found, 
	 * perhaps the caller has inexlicably hidden all of the 
	 * children, then return -1.
	 */
	private int getVisibleChildIndex() {
		int nChildren = getComponentCount();
		for (int i = 0; i < nChildren; i++) {
			Component child = getComponent(i);
			if (child.isVisible()) {
				return i;
			}
		}
		return -1;
	}

	public Component getCurrentCard() {
		Component retValue = null;
		int nChildren = getComponentCount();
		for (int i = 0; i < nChildren; i++) {
			Component child = getComponent(i);
			if (child.isVisible()) {
				retValue = child;
			}
		}
		return retValue;
	}

	public String getCurrentCardName() {
		String retValue = "";
		int nChildren = getComponentCount();
		for (int i = 0; i < nChildren; i++) {
			Component child = getComponent(i);
			if (child.isVisible()) {
				retValue = child.getName();
			}
		}
		return retValue;
	}

	public int getCurrentCardIndex() {
		int nChildren = getComponentCount();
		for (int i = 0; i < nChildren; i++) {
			Component child = getComponent(i);
			if (child.isVisible()) {
				return i;
			}
		}
		return -1;
	}

	public int getCardCount() {
		return getComponentCount();
	}

	/** 
	 * Hide the currently visible child  "card" and show the
	 * specified card.  If the specified card isn't a child
	 * of the CardPanel then we add it here.
	 */
	public void showCard(Component card) {

		if (card.getParent() != this) {
			add(card);
		}

		int index = getVisibleChildIndex();

		if (index != -1) {
			getComponent(index).setVisible(false);
		}

		card.setVisible(true);
		revalidate();
		repaint();
		
		if (m_ParentSheet != null){
			m_ParentSheet.onPageChange();
		}
	}

	/**
	 * Show the card with the specified name.
	 * @see java.awt.Component#getName
	 */
	public void showCard(String name) {
		int nChildren = getComponentCount();
		for (int i = 0; i < nChildren; i++) {
			Component child = getComponent(i);
			if (child.getName().equals(name)) {
				showCard(child);
				break;
			}
		}
	}

	public void showCard(int pIndex) {
		if (pIndex >= 0 && pIndex < getComponentCount()) {
			showCard(getComponent(pIndex));
		}
	}

	/**
	 * Show the card that was added to this CardPanel after the currently
	 * visible card.  If the currently visible card was added last, then
	 * show the first card.
	 */
	public void showNextCard() {
		if (getComponentCount() <= 0) {
			return;
		}
		int index = getVisibleChildIndex();
		if (index == -1) {
			showCard(getComponent(0));
		} else if (index == (getComponentCount() - 1)) {
			showCard(getComponent(0));
		} else {
			showCard(getComponent(index + 1));
		}
	}

	/**
	 * Show the card that was added to this CardPanel before the currently
	 * visible card.  If the currently visible card was added first, then
	 * show the last card.
	 */
	public void showPreviousCard() {
		if (getComponentCount() <= 0) {
			return;
		}
		int index = getVisibleChildIndex();
		if (index == -1) {
			showCard(getComponent(0));
		} else if (index == 0) {
			showCard(getComponent(getComponentCount() - 1));
		} else {
			showCard(getComponent(index - 1));
		}
	}

	/**
	 * Show the first card that was added to this CardPanel.
	 */
	public void showFirstCard() {
		if (getComponentCount() <= 0) {
			return;
		}
		showCard(getComponent(0));
	}

	/**
	 * Show the last card that was added to this CardPanel.
	 */
	public void showLastCard() {
		if (getComponentCount() <= 0) {
			return;
		}
		showCard(getComponent(getComponentCount() - 1));
	}
}
