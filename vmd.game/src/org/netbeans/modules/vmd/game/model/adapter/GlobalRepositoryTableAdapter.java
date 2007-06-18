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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.vmd.game.model.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import org.netbeans.modules.vmd.game.model.Editable;
import org.netbeans.modules.vmd.game.model.GlobalRepository;

/**
 *
 * @author karel herink
 */
public class GlobalRepositoryTableAdapter extends DefaultTableModel {

	public static final int COL_INDEX_ICON = 0;
	public static final int COL_INDEX_NAME = 1;
	
	private GlobalRepository gameDesign;
	
    public GlobalRepositoryTableAdapter(GlobalRepository gameDesign) {
		this.gameDesign = gameDesign;
    }

    public int getColumnCount() {
        return 2;
    }

    public int getRowCount() {
        return this.gameDesign.getSprites().size()
				+ this.gameDesign.getTiledLayers().size()
				+ this.gameDesign.getScenes().size();
    }

    public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == COL_INDEX_ICON) {
			return Editable.class;
		}
		if (columnIndex == COL_INDEX_NAME) {
			return String.class;
		}
        return Object.class;
    }
	
	
	
    @Override
    public Object getValueAt(int row, int column) {
		//sprites, tiled layers, scenes
		List<Editable> l = this.getAll();
		if (row >= l.size() || column >= this.getColumnCount()) {
			return null;
		}
		if (column == COL_INDEX_NAME) {
			return l.get(row).getName();
		}
		else {
			return l.get(row);
		}
    }
	
	private List<Editable> getAll() {
		List<Editable> l = new ArrayList<Editable>();
		l.addAll(this.gameDesign.getSprites());
		l.addAll(this.gameDesign.getTiledLayers());
		l.addAll(this.gameDesign.getScenes());
		return Collections.unmodifiableList(l);
	}

}
