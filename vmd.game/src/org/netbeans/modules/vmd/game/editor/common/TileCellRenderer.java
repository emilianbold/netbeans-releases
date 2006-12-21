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
package org.netbeans.modules.vmd.game.editor.common;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.netbeans.modules.vmd.game.model.Tile;

public class TileCellRenderer extends JComponent implements TableCellRenderer, ListCellRenderer {

	private int padX;
	private int padY;

	private static final int EMPTY_LIST_INDICATOR_WIDTH = 2;
	
	//TODO : replace those colors with the correct color resources from L&F
	private final static Color MOST_COLOR = Color.RED;
	private final static Color NO_COLOR = Color.LIGHT_GRAY;
	private final static Color MEDIUM_COLOR = Color.ORANGE;
	
	private JTable table;
	private Tile tile;
	private boolean isSelected;
	private boolean hasFocus;
	
	public TileCellRenderer(int padX, int padY) {
		this.padX = padX;
		this.padY = padY;
	}
	
	public String getToolTipText(MouseEvent e) {
		return "Index: " + this.tile.getIndex();
	}
	

	
	public void paintComponent(Graphics g) {
		Color c = this.isSelected && this.hasFocus ? MOST_COLOR : (this.isSelected ? MEDIUM_COLOR : NO_COLOR);
		g.setColor(c);
		g.fillRect(0, 0, padX, this.getHeight()); //left vertical
		g.fillRect(this.getWidth() - padX, 0, padX, this.getHeight()); //right vertical
		g.fillRect(padX, 0, this.getWidth() - 2 * padX, padY); //top horizontal
		g.fillRect(padX, this.getHeight() - padY, this.getWidth() - 2 * padX, padY); //bottom horizontal
		if (this.tile != null) {
			//g.setClip(padX, padY, this.tile.getWidth(), this.tile.getHeight());
			this.tile.paint((Graphics2D) g, padX, padY);
		}
		this.paintBorder(g);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		if (value instanceof Tile) {
			this.table = table;
			this.tile = (Tile) value;
			this.isSelected = isSelected;
			this.hasFocus = hasFocus;
			Dimension dimension = new Dimension(this.tile.getWidth() + 2*padX, this.tile.getHeight() + 2*padY);
			this.setToolTipText("Tile index " + tile.getIndex());
			this.setPreferredSize(dimension);
			this.setMinimumSize(dimension);
			this.setMaximumSize(dimension);
			return this;
		}
		if (value instanceof Integer) {
			Integer integer = (Integer) value; //right now the only int value can be SequenceListModel.EMPTY_COL
			this.tile = null;
			this.isSelected = isSelected;
			this.hasFocus = hasFocus;
			Dimension prefSize = this.getPreferredSize();
			Dimension newSize = new Dimension(this.padX*2 + EMPTY_LIST_INDICATOR_WIDTH, prefSize.height);
			this.setPreferredSize(newSize);
			this.setMaximumSize(newSize);
			this.setMinimumSize(newSize);
			return this;
		}
		throw new IllegalArgumentException("Only org.netbeans.mobility.game.model.Tile can be rendered.");
	}
	
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean hasFocus) {
		if (value instanceof Tile) {
			Tile tile = (Tile) value;
			this.tile = tile;
			this.isSelected = isSelected;
			this.hasFocus = hasFocus;
			Dimension dimension = new Dimension(tile.getWidth() + 2*padX, tile.getHeight() + 2*padY);
			this.setPreferredSize(dimension);
			return this;
		}
		if (value instanceof Integer) {
			Integer integer = (Integer) value; //right now the only int value can be SequenceListModel.EMPTY_COL
			this.tile = null;
			this.isSelected = isSelected;
			this.hasFocus = hasFocus;
			Dimension prefSize = this.getPreferredSize();
			Dimension newSize = new Dimension(this.padX*2 + EMPTY_LIST_INDICATOR_WIDTH, prefSize.height);
			this.setPreferredSize(newSize);
			this.setMaximumSize(newSize);
			this.setMinimumSize(newSize);
			return this;
		}
		throw new IllegalArgumentException("Only org.netbeans.mobility.game.model.Tile or java.lang.Integer can be rendered.");
	}
}
