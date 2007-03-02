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

import org.netbeans.modules.vmd.api.io.DataEditorView;
import org.netbeans.modules.vmd.api.io.DataEditorViewLookupFactory;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.spi.navigator.NavigatorLookupHint;

import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author kaja
 */
public class GameBuilderLookupFactory implements DataEditorViewLookupFactory {
    
    public Collection<?> getLookupObjects(DataObjectContext context, DataEditorView view) {
        if (! GameController.PROJECT_TYPE_GAME.equals (context.getProjectType ()))
            return null;
        return Arrays.asList(
			new NavigatorLookupHint() {
				public String getContentType() {
					return "midpgame"; // NOI18N
				}
			}
		);
    }
}
