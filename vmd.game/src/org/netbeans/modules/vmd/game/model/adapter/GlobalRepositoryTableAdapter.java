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
 */package org.netbeans.modules.vmd.game.model.adapter;

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
