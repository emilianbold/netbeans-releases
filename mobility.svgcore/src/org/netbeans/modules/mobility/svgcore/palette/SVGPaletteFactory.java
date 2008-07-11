/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 */   
package org.netbeans.modules.mobility.svgcore.palette;

import java.awt.Point;
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
                                        // TODO calculate real mouse release point.
                                        float[] point = new float[]{0,0};
                                        ((SVGViewTopComponent) comp).dropDataObject(sourceDObj, point);
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
