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

package org.openide.actions;

import java.io.IOException;
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.loaders.SaveAsCapable;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;
import org.netbeans.core.windows.*;
import org.openide.util.ContextAwareAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Tests SaveAsAction.
 * 
 * @author S. Aubrecht
 */
public class SaveAsActionTest extends NbTestCase {
    
    private TopComponent editorWithSaveAs;
    private TopComponent editorWithoutSaveAs;

    private TopComponent viewWithSaveAs;
    private TopComponent viewWithoutSaveAs;
    
    private ModeImpl editorMode;
    
    public SaveAsActionTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        Lookup editorLkp = Lookups.fixed( new SaveAsCapable() {
            public void saveAs(FileObject folder, String name) throws IOException {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        Lookup viewLkp = Lookups.fixed( new SaveAsCapable() {
            public void saveAs(FileObject folder, String name) throws IOException {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        editorWithSaveAs = new TopComponent( editorLkp );
        editorWithoutSaveAs = new TopComponent();
        
        viewWithSaveAs = new TopComponent( viewLkp );
        viewWithoutSaveAs = new TopComponent();
        
        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        
        editorMode = wm.createMode("_my_editor_mode", Constants.MODE_KIND_EDITOR, Constants.MODE_STATE_JOINED, false, new SplitConstraint[0] );
        ModeImpl viewMode = wm.createMode("_my_view_mode", Constants.MODE_KIND_VIEW, Constants.MODE_STATE_JOINED, false, new SplitConstraint[0] );
        
        editorMode.dockInto( editorWithSaveAs );
        editorMode.dockInto( editorWithoutSaveAs );
        
        viewMode.dockInto( viewWithSaveAs );
        viewMode.dockInto( viewWithoutSaveAs );

        editorWithSaveAs.open();
        editorWithoutSaveAs.open();
        viewWithSaveAs.open();
        viewWithoutSaveAs.open();
        
        assertTrue( editorMode.getOpenedTopComponents().contains( editorWithSaveAs ) );
        assertTrue( editorMode.getOpenedTopComponents().contains( editorWithoutSaveAs ) );
        assertTrue( viewMode.getOpenedTopComponents().contains( viewWithSaveAs ) );
        assertTrue( viewMode.getOpenedTopComponents().contains( viewWithoutSaveAs ) );
    }
    
    protected boolean runInEQ() {
        return true;
    }
    
    public void testSaveAsActionDisabledForNonEditorWindows() throws Exception {
        viewWithoutSaveAs.requestActive();
        assertTrue( TopComponent.getRegistry().getActivated() == viewWithoutSaveAs );
        
        ContextAwareAction action = SaveAsAction.create();
        assertFalse( "action is disabled when SaveAsCapable is not present in active TC lookup", action.isEnabled() );
    }
    
    public void testSaveAsActionDisabledForViewsWithSaveAsCapable() throws Exception {
        viewWithSaveAs.requestActive();
        assertTrue( TopComponent.getRegistry().getActivated() == viewWithSaveAs );
        
        ContextAwareAction action = SaveAsAction.create();
        assertFalse( "action is disabled other window than editor is activated", action.isEnabled() );
    }
    
    public void testSaveAsActionEnabledForEditorsOnly() throws Exception {
        editorWithSaveAs.requestActive();
        assertTrue( TopComponent.getRegistry().getActivated() == editorWithSaveAs );
        
        ContextAwareAction action = SaveAsAction.create();
        assertTrue( "action is enabled for editor windows with SaveAsCapable in their Lookup", action.isEnabled() );
    }
    
    public void testSaveAsActionDisabledForEditorsWithoutSaveAsCapable() throws Exception {
        editorWithoutSaveAs.requestActive();
        assertTrue( TopComponent.getRegistry().getActivated() == editorWithoutSaveAs );
        
        ContextAwareAction action = SaveAsAction.create();
        assertFalse( "action is disabled for editor windows without SaveAsCapable in their Lookup", action.isEnabled() );
    }
    
    public void testActionStatusUpdatedOnLookupChange() throws Exception {
        final InstanceContent content = new InstanceContent();
        Lookup lkp = new AbstractLookup( content );
        final SaveAsCapable saveAsImpl = new SaveAsCapable() {
            public void saveAs(FileObject folder, String name) throws IOException {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        
        TopComponent tc = new TopComponent( lkp );
        editorMode.dockInto( tc );
        tc.open();
        assertTrue( editorMode.getOpenedTopComponents().contains( tc ) );
        
        ContextAwareAction action = SaveAsAction.create();
        assertFalse( "action is disabled for editor windows without SaveAsCapable in their Lookup", action.isEnabled() );
        
        Action a = action.createContextAwareInstance( tc.getLookup() );
        
        content.add( saveAsImpl );
        assertTrue( "action is enabled for editor windows with SaveAsCapable in their Lookup", a.isEnabled() );
        content.remove( saveAsImpl );
        assertFalse( "action is disabled for editor windows without SaveAsCapable in their Lookup", a.isEnabled() );
    }
}
