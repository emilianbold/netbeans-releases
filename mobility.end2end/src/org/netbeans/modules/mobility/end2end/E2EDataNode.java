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

/*
 * E2EDataNode.java
 *
 * Created on June 27, 2005, 2:54 PM
 *
 */
package org.netbeans.modules.mobility.end2end;

import java.awt.Image;
import javax.swing.Action;
import org.netbeans.modules.mobility.end2end.ui.editor.GenerateAction;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.FileSystemAction;
import org.openide.actions.OpenAction;
import org.openide.actions.PasteAction;
import org.openide.actions.PropertiesAction;
import org.openide.actions.SaveAction;
import org.openide.loaders.DataNode;
import org.openide.nodes.Children;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Michal Skvor
 */
public class E2EDataNode extends DataNode {
    
    /** Creates a new instance of E2EDataNode */
    public E2EDataNode( E2EDataObject obj ) {
        super( obj, Children.LEAF );
    }
    
    public Image getIcon(@SuppressWarnings("unused")
	final int type) {
        return Utilities.loadImage(
                "org/netbeans/modules/mobility/end2end/resources/e2eclienticon.png" ); // NOI18N
    }
    
    public Action[] getActions(@SuppressWarnings("unused")
	final boolean context) {
        final Action[] result = new Action[] {
            SystemAction.get( OpenAction.class ),
            SystemAction.get( SaveAction.class ),
            null,
            SystemAction.get( GenerateAction.class ),
            null,
            SystemAction.get( FileSystemAction.class ),
            null,
            SystemAction.get( CutAction.class ),
            SystemAction.get( CopyAction.class ),
            SystemAction.get( PasteAction.class ),
            null,
            SystemAction.get( DeleteAction.class ),
            null,
            SystemAction.get( PropertiesAction.class )
        };
        return result;
    }
    
    public Action getPreferredAction() {
        return SystemAction.get( OpenAction.class );
    }
    
    public boolean canCopy(){
        return false;
    }
    
    public boolean canCut(){
        return false;
    }
}
