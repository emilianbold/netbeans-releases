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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.vmd.game.view;

import java.awt.Color;
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
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import org.netbeans.modules.vmd.game.dialog.NewSceneDialog;
import org.netbeans.modules.vmd.game.model.GlobalRepository;
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
 * @author kaja
 */
public class GameDesignOverViewPanel extends ScrollableFlowPanel implements ComponentListener {

	private GlobalRepository gameDesign;
	
	private JLabel labelTiledLayers;
	private JLabel labelSprites;
	private JLabel labelScenes;
	
    public GameDesignOverViewPanel(GlobalRepository gameDesign) {
		this.gameDesign = gameDesign;
		this.manualInit();
		this.addComponentListener(this);
	}
	
	private void manualInit() {
		this.setBackground(Color.WHITE);
		((FlowLayout) getLayout()).setAlignment(FlowLayout.LEFT);
		
		this.populateScenePreviewList();
		this.populateTiledLayerPreviewList();
		this.populateSpritePreviewList();
	}
	
	private void populateTiledLayerPreviewList() {
		labelTiledLayers = new JLabel(NbBundle.getMessage(GameDesignOverViewPanel.class, "GameDesignOverViewPanel.labelTiledLayers.txt"));
        labelTiledLayers.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        labelTiledLayers.setForeground(new java.awt.Color(163, 184, 215));
		labelTiledLayers.setHorizontalAlignment(SwingConstants.LEFT);
		this.add(labelTiledLayers);
		
		List<TiledLayer> layers = this.gameDesign.getTiledLayers();
		for (TiledLayer tiledLayer : layers) {
			//System.out.println("preview tiledLayer: " + tiledLayer);
			add(new GameDesignPreviewComponent(gameDesign, tiledLayer.getPreview(), tiledLayer.getName(), tiledLayer));
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
		this.add(lblCreate);
	}

	private void populateSpritePreviewList() {
		labelSprites = new JLabel(NbBundle.getMessage(GameDesignOverViewPanel.class, "GameDesignOverViewPanel.labelSprites.txt"));
        labelSprites.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        labelSprites.setForeground(new java.awt.Color(163, 184, 215));
		labelSprites.setHorizontalAlignment(SwingConstants.LEFT);
		this.add(labelSprites);
				
		List<Sprite> sprites = this.gameDesign.getSprites();
		for (Sprite sprite : sprites) {
			//System.out.println("preview sprite: " + sprite);
			ImagePreviewComponent imagePreviewComponent = new ImagePreviewComponent(true);
			imagePreviewComponent.setPreviewable(sprite.getDefaultSequence().getFrame(0));
			add(new GameDesignPreviewComponent(gameDesign, imagePreviewComponent, sprite.getName(), sprite));
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
		this.add(lblCreate);
	}
	
	private void populateScenePreviewList() {
		labelScenes = new JLabel(NbBundle.getMessage(GameDesignOverViewPanel.class, "GameDesignOverViewPanel.labelScenes.txt"));
        labelScenes.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        labelScenes.setForeground(new java.awt.Color(163, 184, 215));
		labelScenes.setHorizontalAlignment(SwingConstants.LEFT);
		this.add(labelScenes);
				
		List<Scene> scenes = this.gameDesign.getScenes();
		for (Scene scene : scenes) {
			//System.out.println("preview scene: " + scene);
			add(new GameDesignPreviewComponent(gameDesign, scene.getPreview(), scene.getName(), scene));
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
		this.add(lblCreate);
	}

	private void resizeLabels() {
		Dimension d = labelTiledLayers.getPreferredSize();
		this.labelTiledLayers.setPreferredSize(new Dimension(this.getWidth(), d.getSize().height));
		
		d = labelSprites.getPreferredSize();
		this.labelSprites.setPreferredSize(new Dimension(this.getWidth(), d.getSize().height));
		
		d = labelScenes.getPreferredSize();
		this.labelScenes.setPreferredSize(new Dimension(this.getWidth(), d.getSize().height));
		
//		d = labelGameDesign.getPreferredSize();
//		this.labelGameDesign.setPreferredSize(new Dimension(this.getWidth(), d.getSize().height));
	}
	
    public void componentResized(ComponentEvent e) {
        this.resizeLabels();
    }

    public void componentMoved(ComponentEvent e) {
        this.resizeLabels();
    }

    public void componentShown(ComponentEvent e) {
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
