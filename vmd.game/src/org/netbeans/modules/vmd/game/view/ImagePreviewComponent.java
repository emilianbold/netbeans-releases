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
package org.netbeans.modules.vmd.game.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import javax.swing.JComponent;
import javax.swing.JFrame;
import org.netbeans.modules.vmd.game.model.GlobalRepository;
import org.netbeans.modules.vmd.game.model.ImageResource;
import org.netbeans.modules.vmd.game.model.Previewable;
import org.netbeans.modules.vmd.game.model.Scene;
import org.netbeans.modules.vmd.game.model.Sequence;
import org.netbeans.modules.vmd.game.model.Sprite;
import org.netbeans.modules.vmd.game.model.StaticTile;
import org.netbeans.modules.vmd.game.view.main.MainView;


public class ImagePreviewComponent extends JComponent {

	public static final boolean DEBUG = false;

	private boolean scaled = false;
	private boolean centeredH = true;
	private boolean centeredV = true;
	
	private Previewable previewable;
	
	public ImagePreviewComponent (boolean scaled, boolean centeredHor, boolean centeredVer) {
		this(scaled);
		this.centeredH = centeredHor;
		this.centeredV = centeredVer;
	}

	public ImagePreviewComponent (boolean scaled) {
		this.setDoubleBuffered(true);
		this.scaled = scaled;
		this.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                ImagePreviewComponent.this.scaled = !ImagePreviewComponent.this.scaled;
				ImagePreviewComponent.this.repaint();
            }
		});
		this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				if (DEBUG) System.out.println("resized - updating preview");
				ImagePreviewComponent.this.repaint();
			}
		});
	}
	
	public void setPreviewable(Previewable previewable) {
		this.previewable = previewable;
		Dimension d = new Dimension(previewable.getWidth(), previewable.getHeight());
		this.setMinimumSize(d);
		this.setPreferredSize(d);
		if (!this.scaled) {
			this.setMaximumSize(d);
		}
		this.repaint();
	}

	public void paintComponent(Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		int offX = 0;
		int offY = 0;
		if (this.previewable != null) {
			if (this.centeredH) {
				offX = (this.getWidth() - this.previewable.getWidth()) / 2;
			}
			if (this.centeredV) {
				offY = (this.getHeight() - this.previewable.getHeight()) / 2;
			}
			this.previewable.paint((Graphics2D) g, offX, offY);
		}
	}
	
	public static void main(String[] args) {
		ImagePreviewComponent imgPreview = new ImagePreviewComponent(true);
		JFrame frame = new JFrame("ResourceImageGridTable Test");
		frame.getContentPane().add(imgPreview);

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		Scene scene = GlobalRepository.getInstance().createScene("scene1");
		
		URL urlRock = MainView.class.getResource("res/color_tiles.png");
		Sprite rock = scene.createSprite("rock2", GlobalRepository.getImageResource(urlRock, 20, 20), 5);
		Sequence def = rock.getDefaultSequence();
		ImageResource imgRock = GlobalRepository.getImageResource(urlRock, 20, 20);
		def.setFrame((StaticTile) imgRock.getTile(1), 0);
		def.setFrame((StaticTile) imgRock.getTile(3), 1);
		def.setFrame((StaticTile) imgRock.getTile(5), 2);
		def.setFrame((StaticTile) imgRock.getTile(7), 3);
		def.setFrame((StaticTile) imgRock.getTile(9), 4);
		def.setName("Slow");
		
		imgPreview.setPreviewable(rock);

		frame.setSize(new Dimension(300, 300));
		frame.validate();
		frame.setVisible(true);
		
	}
	
}
