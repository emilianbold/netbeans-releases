/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt. 
  * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */   
package org.netbeans.modules.mobility.svgcore.palette;

import org.netbeans.modules.mobility.svgcore.view.source.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;
import org.netbeans.modules.mobility.svgcore.view.svg.SVGViewTopComponent;
import org.netbeans.spi.palette.PaletteActions;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;
import org.openide.loaders.DataObject;
import org.openide.text.ActiveEditorDrop;
import org.openide.text.CloneableEditor;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.openide.windows.TopComponent;

/**
 *
 * @author Pavel Benes
 */
public final class SVGPaletteFactory {
    public static final String SVGXML_PALETTE_FOLDER        = "SVGXMLPalette";  //NOI18N
    public static final String SVG_PALETTE_THUMBNAIL_FOLDER = "SVGPalette/ThumbnailImages"; //NOI18N
    
    private static PaletteController palette = null;
    
    public static synchronized PaletteController getPalette() throws IOException {
        if (palette == null) {           
            palette = PaletteFactory.createPalette( SVGXML_PALETTE_FOLDER,
                      new SVGPaletteActions(), null, null);
        }
        /*
        palette.addPropertyChangeListener( new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if ( PaletteController.PROP_SELECTED_ITEM.equals(evt.getPropertyName())) {
                    Lookup selItem = palette.getSelectedItem();
                    if ( selItem != null) {
                        Node selNode = selItem.lookup(Node.class);
                        if ( selNode != null) {
                            System.out.println("Palette item selected: " + selNode.getDisplayName());
                        }
                    }
                }
            }
        });*/ 
        return palette;
    }
    
    /** Creates a new instance of SVGXMLPaletteFactory */
    public SVGPaletteFactory() { }
    
    private static class SVGPaletteActions extends PaletteActions {
        public Action getPreferredAction(final Lookup lookup) {
            return new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    for (TopComponent tc : TopComponent.getRegistry().getOpened()) {
                        if (tc.isVisible()) {
                            SVGDataObject targetDObj = tc.getLookup().lookup(SVGDataObject.class);
                            if ( targetDObj != null && !targetDObj.getSceneManager().isReadOnly()) {
                                try {
                                    MultiViewElement elem = targetDObj.getActiveElement();
                                    assert elem != null;
                                    JComponent comp = elem.getVisualRepresentation();
                                    if ( comp instanceof CloneableEditor) {
                                        ActiveEditorDrop aed    = lookup.lookup(ActiveEditorDrop.class);
                                        JTextComponent   editor = ((CloneableEditor) comp).getEditorPane();
                                        if (aed != null && editor != null) {
                                            aed.handleTransfer(editor);
                                        } else {
                                            SceneManager.log(Level.SEVERE, "Paletter drop failed - could not obtain context."); //NOI18N
                                        }
                                    } else if (comp instanceof SVGViewTopComponent) {
                                        DataObject sourceDObj = lookup.lookup(DataObject.class);
                                        assert sourceDObj != null;
                                        ((SVGViewTopComponent) comp).dropDataObject(sourceDObj);
                                    }
                                } catch( Exception ex) {
                                    SceneManager.error("Palette drop failed.", ex); //NOI18N
                                }
                                return;
                            }
                        }
                    }
                }                
            };
        }

        public Action[] getCustomItemActions(Lookup lookup) {
            return null;
        }

        public Action[] getCustomCategoryActions(Lookup lookup) {
            return null;
        }

        public Action[] getImportActions() {
            return null;
        }

        public Action[] getCustomPaletteActions() {
            return new Action[] {
                SystemAction.get( ImportPaletteFolderAction.class)
            };
        }        
    }    
}
