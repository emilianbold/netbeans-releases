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
package org.netbeans.modules.vmd.game.model;

import java.util.EventListener;
import java.util.Set;

public interface TiledLayerListener extends EventListener {

	public void tileChanged(TiledLayer source, int row, int col);
	
	public void tilesChanged(TiledLayer source, Set positions);
	
	public void columnsInserted(TiledLayer source, int index, int count);
	
	public void columnsRemoved(TiledLayer source, int index, int count);
	
	public void rowsInserted(TiledLayer source, int index, int count);
	
	public void rowsRemoved(TiledLayer source, int index, int count);	
	
}
