/*
 * GameBuilderLookupFactory.java
 *
 * Created on January 31, 2007, 10:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.vmd.game;

import java.util.Arrays;
import java.util.Collection;
import org.netbeans.modules.vmd.api.io.DataEditorView;
import org.netbeans.modules.vmd.api.io.DataEditorViewLookupFactory;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.spi.navigator.NavigatorLookupHint;

/**
 *
 * @author kaja
 */
public class GameBuilderLookupFactory implements DataEditorViewLookupFactory {
    
    public Collection<?> getLookupObjects(DataObjectContext context, String viewID, DataEditorView.Kind viewKind) {
		return Arrays.asList(
			new NavigatorLookupHint() {
				public String getContentType() {
					return "midpgame"; // NOI18N
				}
			}
		);
    }
}
