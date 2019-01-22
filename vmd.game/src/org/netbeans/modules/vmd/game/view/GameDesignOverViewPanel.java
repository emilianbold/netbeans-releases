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
 */
package org.netbeans.modules.vmd.game.view;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import org.netbeans.modules.vmd.game.dialog.NewSceneDialog;
import org.netbeans.modules.vmd.game.model.GlobalRepository;
import org.netbeans.modules.vmd.game.model.GlobalRepositoryListener;
import org.netbeans.modules.vmd.game.model.ImageResource;
import org.netbeans.modules.vmd.game.model.Scene;
import org.netbeans.modules.vmd.game.model.Sprite;
import org.netbeans.modules.vmd.game.model.TiledLayer;
import org.netbeans.modules.vmd.game.nbdialog.SpriteDialog;
import org.netbeans.modules.vmd.game.nbdialog.TiledLayerDialog;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * 
 */
public class GameDesignOverViewPanel extends ScrollableFlowPanel implements ComponentListener {

    private GlobalRepository gameDesign;
    
    private JLabel labelTiledLayers;
    private JLabel labelSprites;
    private JLabel labelScenes;
    
    private ScrollableFlowPanel panelTiledLayers = new ScrollableFlowPanel();
    private ScrollableFlowPanel panelSprites = new ScrollableFlowPanel();
    private ScrollableFlowPanel panelScenes = new ScrollableFlowPanel();
    
    private Map<TiledLayer, GameDesignPreviewComponent> tiledLayerPreviews = new HashMap<TiledLayer, GameDesignPreviewComponent>();
    private Map<Sprite, GameDesignPreviewComponent> spritePreviews = new HashMap<Sprite, GameDesignPreviewComponent>();
    private Map<Scene, GameDesignPreviewComponent> scenePreviews = new HashMap<Scene, GameDesignPreviewComponent>();
    
    public GameDesignOverViewPanel(final GlobalRepository gameDesign) {
        this.gameDesign = gameDesign;
        this.gameDesign.addGlobalRepositoryListener(new GlobalRepositoryListener() {
            public void sceneAdded(Scene scene, int index) {
                GameDesignPreviewComponent preview = new GameDesignPreviewComponent(gameDesign, scene.getPreview(), scene.getName(), scene);
                panelScenes.add(preview, index);
                panelScenes.revalidate();
                panelScenes.repaint();
                scenePreviews.put(scene, preview);
            }
            public void sceneRemoved(Scene scene, int index) {
                JComponent preview = scenePreviews.remove(scene);
                if (preview != null) {
                    panelScenes.remove(preview);
                    panelScenes.revalidate();
                    panelScenes.repaint();
                }
            }
            
            public void tiledLayerAdded(TiledLayer tiledLayer, int index) {
                GameDesignPreviewComponent preview = new GameDesignPreviewComponent(gameDesign, tiledLayer.getPreview(), tiledLayer.getName(), tiledLayer);
                panelTiledLayers.add(preview, index);
                panelTiledLayers.revalidate();
                panelTiledLayers.repaint();
                tiledLayerPreviews.put(tiledLayer, preview);
            }
            public void tiledLayerRemoved(TiledLayer tiledLayer, int index) {
                JComponent preview = tiledLayerPreviews.remove(tiledLayer);
                if (preview != null) {
                    panelTiledLayers.remove(preview);
                    panelTiledLayers.revalidate();
                    panelTiledLayers.repaint();
                }
            }
            
            public void spriteAdded(Sprite sprite, int index) {
                ImagePreviewComponent imagePreviewComponent = new ImagePreviewComponent(true);
                imagePreviewComponent.setPreviewable(sprite.getDefaultSequence().getFrame(0));
                final GameDesignPreviewComponent preview = new GameDesignPreviewComponent(gameDesign, imagePreviewComponent, sprite.getName(), sprite);
                panelSprites.add(preview, index);
                panelSprites.revalidate();
                panelSprites.repaint();
                spritePreviews.put(sprite, preview);
            }
            public void spriteRemoved(Sprite sprite, int index) {
                JComponent preview = spritePreviews.remove(sprite);
                if (preview != null) {
                    panelSprites.remove(preview);
                    panelSprites.revalidate();
                    panelSprites.repaint();
                }
            }
            public void imageResourceAdded(ImageResource imageResource) {
            }
        });
        this.manualInit();
        this.addComponentListener(this);

        // vlv: print
        putClientProperty("print.printable", Boolean.TRUE); // NOI18N
    }
    
    private void manualInit() {
        this.setBackground(ColorConstants.COLOR_EDITOR_PANEL);
        this.panelTiledLayers.setBackground(ColorConstants.COLOR_EDITOR_PANEL);
        this.panelSprites.setBackground(ColorConstants.COLOR_EDITOR_PANEL);
        this.panelScenes.setBackground(ColorConstants.COLOR_EDITOR_PANEL);
        
        ((FlowLayout) this.getLayout()).setAlignment(FlowLayout.LEFT);
        ((FlowLayout) this.panelTiledLayers.getLayout()).setAlignment(FlowLayout.LEFT);
        ((FlowLayout) this.panelSprites.getLayout()).setAlignment(FlowLayout.LEFT);
        ((FlowLayout) this.panelScenes.getLayout()).setAlignment(FlowLayout.LEFT);
        
        //add scenes label
        labelScenes = new JLabel(NbBundle.getMessage(GameDesignOverViewPanel.class, "GameDesignOverViewPanel.labelScenes.txt"));
        labelScenes.setFont(new java.awt.Font("Dialog", 1, labelScenes.getFont().getSize()+7)); // NOI18N
        labelScenes.setForeground(ColorConstants.COLOR_TEXT_PLAIN);
        labelScenes.setHorizontalAlignment(SwingConstants.LEFT);
        this.add(labelScenes);      
        //add scenes list
        this.add(this.panelScenes);
        
        //add tiled layers label
        labelTiledLayers = new JLabel(NbBundle.getMessage(GameDesignOverViewPanel.class, "GameDesignOverViewPanel.labelTiledLayers.txt"));
        labelTiledLayers.setFont(new java.awt.Font("Dialog", 1, labelTiledLayers.getFont().getSize()+7)); // NOI18N
        labelTiledLayers.setForeground(new java.awt.Color(163, 184, 215));
        labelTiledLayers.setHorizontalAlignment(SwingConstants.LEFT);
        this.add(labelTiledLayers);
        //add tiled layers list
        this.add(this.panelTiledLayers);
        
        //add sprites label
        labelSprites = new JLabel(NbBundle.getMessage(GameDesignOverViewPanel.class, "GameDesignOverViewPanel.labelSprites.txt"));
        labelSprites.setFont(new java.awt.Font("Dialog", 1, labelSprites.getFont().getSize()+7)); // NOI18N
        labelSprites.setForeground(ColorConstants.COLOR_TEXT_PLAIN);
        labelSprites.setHorizontalAlignment(SwingConstants.LEFT);
        this.add(labelSprites);
        //add sprites list
        this.add(this.panelSprites);
        
        
        this.populateScenePreviewList(panelScenes);
        this.populateTiledLayerPreviewList(panelTiledLayers);
        this.populateSpritePreviewList(panelSprites);
        
        this.getAccessibleContext().setAccessibleName((NbBundle.getMessage(GameDesignOverViewPanel.class, "GameDesignOverViewPanel.accessible.name")));
        this.getAccessibleContext().setAccessibleDescription((NbBundle.getMessage(GameDesignOverViewPanel.class, "GameDesignOverViewPanel.accessible.description")));
        
    }
    
    private void populateTiledLayerPreviewList(JComponent container) {
        
        List<TiledLayer> layers = this.gameDesign.getTiledLayers();
        for (TiledLayer tiledLayer : layers) {
            GameDesignPreviewComponent preview = new GameDesignPreviewComponent(gameDesign, tiledLayer.getPreview(), tiledLayer.getName(), tiledLayer);
            container.add(preview);
            tiledLayerPreviews.put(tiledLayer, preview);
        }

        final JLabel lblCreate = new JLabel(NbBundle.getMessage(GameDesignOverViewPanel.class, "GameDesignOverViewPanel.labelNewTiledLayer.txt"));
        lblCreate.setPreferredSize(new Dimension(lblCreate.getPreferredSize().width + 15, 40));
        lblCreate.setForeground(new java.awt.Color(100, 123, 156));
        lblCreate.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                lblCreate.setFont(lblCreate.getFont().deriveFont(Font.BOLD));
            }
            public void mouseExited(MouseEvent e) {
                lblCreate.setFont(lblCreate.getFont().deriveFont(Font.PLAIN));
            }
            public void mouseClicked(MouseEvent e) {
                TiledLayerDialog nld = new TiledLayerDialog(gameDesign);
                DialogDescriptor dd = new DialogDescriptor(nld, NbBundle.getMessage(GameDesignOverViewPanel.class, "GameDesignOverViewPanel.dialogNewTiledLayer.txt"));
                dd.setButtonListener(nld);
                dd.setValid(false);
                nld.setDialogDescriptor(dd);
                Dialog d = DialogDisplayer.getDefault().createDialog(dd);
                d.setVisible(true);
           }
        });
        container.add(lblCreate);
    }

    private void populateSpritePreviewList(JComponent container) {
        List<Sprite> sprites = this.gameDesign.getSprites();
        for (Sprite sprite : sprites) {
            ImagePreviewComponent imagePreviewComponent = new ImagePreviewComponent(true);
            imagePreviewComponent.setPreviewable(sprite.getDefaultSequence().getFrame(0));
            final GameDesignPreviewComponent preview = new GameDesignPreviewComponent(gameDesign, imagePreviewComponent, sprite.getName(), sprite);
            container.add(preview);
            spritePreviews.put(sprite, preview);
        }
        
        final JLabel lblCreate = new JLabel(NbBundle.getMessage(GameDesignOverViewPanel.class, "GameDesignOverViewPanel.labelNewSprite.txt"));
        lblCreate.setPreferredSize(new Dimension(lblCreate.getPreferredSize().width + 15, 40));
        lblCreate.setForeground(new java.awt.Color(100, 123, 156));
        lblCreate.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                lblCreate.setFont(lblCreate.getFont().deriveFont(Font.BOLD));
            }
            public void mouseExited(MouseEvent e) {
                lblCreate.setFont(lblCreate.getFont().deriveFont(Font.PLAIN));
            }
            public void mouseClicked(MouseEvent e) {
                SpriteDialog nld = new SpriteDialog(gameDesign);
                DialogDescriptor dd = new DialogDescriptor(nld, NbBundle.getMessage(GameDesignOverViewPanel.class, "GameDesignOverViewPanel.dialogNewSprite.txt"));
                dd.setButtonListener(nld);
                dd.setValid(false);
                nld.setDialogDescriptor(dd);
                Dialog d = DialogDisplayer.getDefault().createDialog(dd);
                d.setVisible(true);
            }           
        });
        container.add(lblCreate);
    }
    
    private void populateScenePreviewList(JComponent container) {
        List<Scene> scenes = this.gameDesign.getScenes();
        for (Scene scene : scenes) {
            GameDesignPreviewComponent preview = new GameDesignPreviewComponent(gameDesign, scene.getPreview(), scene.getName(), scene);
            container.add(preview);
            scenePreviews.put(scene, preview);
        }
        
        final JLabel lblCreate = new JLabel(NbBundle.getMessage(GameDesignOverViewPanel.class, "GameDesignOverViewPanel.labelNewScene.txt"));
        lblCreate.setPreferredSize(new Dimension(lblCreate.getPreferredSize().width + 15, 40));
        lblCreate.setForeground(new java.awt.Color(100, 123, 156));
        lblCreate.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                lblCreate.setFont(lblCreate.getFont().deriveFont(Font.BOLD));
            }
            public void mouseExited(MouseEvent e) {
                lblCreate.setFont(lblCreate.getFont().deriveFont(Font.PLAIN));
            }
            public void mouseClicked(MouseEvent e) {
                NewSceneDialog dialog = new NewSceneDialog(gameDesign);
                DialogDescriptor dd = new DialogDescriptor(dialog, NbBundle.getMessage(GameDesignOverViewPanel.class, "GameDesignOverViewPanel.dialogNewScene.txt"));
                dd.setButtonListener(dialog);
                dd.setValid(false);
                dialog.setDialogDescriptor(dd);
                Dialog d = DialogDisplayer.getDefault().createDialog(dd);
                d.setVisible(true);
            }           
        });
        container.add(lblCreate);
    }

    private void resizeLabels() {
        Dimension d = labelTiledLayers.getPreferredSize();
        this.labelTiledLayers.setPreferredSize(new Dimension(this.getWidth(), d.getSize().height));
        
        d = labelSprites.getPreferredSize();
        this.labelSprites.setPreferredSize(new Dimension(this.getWidth(), d.getSize().height));
        
        d = labelScenes.getPreferredSize();
        this.labelScenes.setPreferredSize(new Dimension(this.getWidth(), d.getSize().height));
    }
    
    public void componentResized(ComponentEvent e) {
        this.resizeLabels();
    }

    public void componentMoved(ComponentEvent e) {
        this.resizeLabels();
    }

    public void componentShown(ComponentEvent e) {
        this.resizeLabels();
    }

    public void componentHidden(ComponentEvent e) {
    }
}


class ScrollableFlowPanel extends JPanel implements Scrollable {

    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, getParent().getWidth(), height);
    }

    public Dimension getPreferredSize() {
        return new Dimension(getWidth(), getPreferredHeight());
    }

    public Dimension getPreferredScrollableViewportSize() {
        return super.getPreferredSize();
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        int hundredth = (orientation == SwingConstants.VERTICAL ? getParent().getHeight() : getParent().getWidth()) / 100;
        return hundredth == 0 ? 1 : hundredth;
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return orientation == SwingConstants.VERTICAL ? getParent().getHeight() : getParent().getWidth();
    }

    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    private int getPreferredHeight() {
        int rv = 0;
        for (int k = 0, count = getComponentCount(); k < count; k++) {
            Component comp = getComponent(k);
            Rectangle r = comp.getBounds();
            int height = r.y + r.height;
            if (height > rv) {
                rv = height;
            }
        }
        rv += ((FlowLayout) getLayout()).getVgap();
        return rv;
    }
}
