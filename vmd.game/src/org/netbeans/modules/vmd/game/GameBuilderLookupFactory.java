/*
 * GameBuilderLookupFactory.java
 *
 * Created on January 31, 2007, 10:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
    
    public Collection<?> getLookupObjects(DataObjectContext context, String viewID, DataEditorView.Kind viewKind) {
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
