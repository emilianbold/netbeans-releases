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

package demo;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.lcdui.game.LayerManager;
import javax.microedition.lcdui.game.Sprite;
import javax.microedition.lcdui.game.TiledLayer;

/**
 *
 * @author  Karel Herink
 * @version 1.0
 */
public class DemoGameCanvas extends GameCanvas implements Runnable {
	
	private static final int SPEED = 3;
	
	private int viewPortX = 0;
	private int viewPortY = 0;
	
	private byte lastDirection = -1;
	
	private boolean interrupted;
	private LayerManager lm;
	private GameDesign gameCanvas;
	private Timer timer;
	
	private Sprite spriteKarel;
	private SpriteAnimationTask spriteKarelAnimator;
	
	private Sprite spriteThomas;
	private SpriteAnimationTask spriteThomasAnimator;
	
	private TileAnimationTask waterAnimator;
	
	
	public DemoGameCanvas() {
		super(true);
		try {
			this.init();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private void init() throws IOException {
		this.timer = new Timer();
		this.gameCanvas = new GameDesign();
		
		this.spriteKarel = gameCanvas.get_Karel();
		this.spriteKarelAnimator = new SpriteAnimationTask(this.spriteKarel, false);
		this.timer.scheduleAtFixedRate(this.spriteKarelAnimator, 0, gameCanvas.KarelSeqWalkDown_delay);
		
		this.spriteThomas = gameCanvas.get_Thomas();
		this.spriteThomasAnimator = new SpriteAnimationTask(this.spriteThomas, true);
		this.spriteThomasAnimator.setMoving(true);
		this.timer.scheduleAtFixedRate(this.spriteThomasAnimator, 0, gameCanvas.ThomasSeqWalkHoriz_delay);
		
		
		this.waterAnimator = new TileAnimationTask(gameCanvas.get_Water(), gameCanvas.AnimWater_Water, gameCanvas.AnimWaterSeq001, true);
		this.timer.scheduleAtFixedRate(this.waterAnimator, 0,gameCanvas.AnimWaterSeq001_delay);
		
		this.lm = new LayerManager();
		gameCanvas.updateLayerManagerForForest(lm);
	}
	
	
	public void run() {
		// Get the Graphics object for the off-screen buffer
		Graphics g = getGraphics();
		
		while (!this.interrupted) {
			// Check user input and update positions if necessary
			int keyState = getKeyStates();
			if ((keyState & LEFT_PRESSED) != 0)  {
				if (this.lastDirection != LEFT) {
					this.lastDirection = LEFT;
					this.spriteKarel.setFrameSequence(gameCanvas.KarelSeqWalkSide);
					this.spriteKarel.setTransform(Sprite.TRANS_MIRROR);
				}
				this.lm.setViewWindow(this.viewPortX -= SPEED, this.viewPortY, Integer.MAX_VALUE, Integer.MAX_VALUE);
				this.spriteKarelAnimator.forward();
				this.spriteKarel.move(-SPEED, 0);
			} else if ((keyState & RIGHT_PRESSED) != 0) {
				if (this.lastDirection != RIGHT) {
					this.lastDirection = RIGHT;
					this.spriteKarel.setFrameSequence(gameCanvas.KarelSeqWalkSide);
					this.spriteKarel.setTransform(Sprite.TRANS_NONE);
				}
				this.lm.setViewWindow(this.viewPortX += SPEED, this.viewPortY, Integer.MAX_VALUE, Integer.MAX_VALUE);
				this.spriteKarelAnimator.backward();
				this.spriteKarel.move(SPEED, 0);
			} else if ((keyState & UP_PRESSED) != 0) {
				if (this.lastDirection != UP) {
					this.lastDirection = UP;
					this.spriteKarel.setFrameSequence(gameCanvas.KarelSeqWalkUp);
					this.spriteKarel.setTransform(Sprite.TRANS_NONE);
				}
				this.lm.setViewWindow(this.viewPortX, this.viewPortY -= SPEED, Integer.MAX_VALUE, Integer.MAX_VALUE);
				this.spriteKarelAnimator.backward();
				this.spriteKarel.move(0, -SPEED);
			} else if ((keyState & DOWN_PRESSED) != 0) {
				if (this.lastDirection != DOWN) {
					this.lastDirection = DOWN;
					this.spriteKarel.setFrameSequence(gameCanvas.KarelSeqWalkDown);
					this.spriteKarel.setTransform(Sprite.TRANS_NONE);
				}
				this.lm.setViewWindow(this.viewPortX, this.viewPortY += SPEED, Integer.MAX_VALUE, Integer.MAX_VALUE);
				this.spriteKarelAnimator.forward();
				this.spriteKarel.move(0, SPEED);
			} else {
				this.spriteKarelAnimator.setMoving(false);
			}
			g.setColor(0xFFFFFF);
			g.fillRect(0, 0, getWidth(), getHeight());
			this.lm.paint(g, 0, 0);
			flushGraphics();
			
			try {
				Thread.sleep(20);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public void stop() {
		this.interrupted = true;
	}
	
	private class TileAnimationTask extends TimerTask {
		private boolean moving = true;
		private boolean forward = true;
		
		private TiledLayer tiledLayer;
		private int animatedTileIndex;
		private int[] sequence;
		private int sequenceIndex;
		
		public TileAnimationTask(TiledLayer tiledLayer, int animatedTileIndex, int[] sequence, boolean forward) {
			this.tiledLayer = tiledLayer;
			this.animatedTileIndex = animatedTileIndex;
			this.sequence = sequence;
			this.forward = forward;
		}
		public void run() {
			if (!this.moving)
				return;
			if (forward) {
				if (++this.sequenceIndex >= this.sequence.length)
					sequenceIndex = 0;
			} else {
				if (--this.sequenceIndex < 0)
					sequenceIndex = this.sequence.length - 1;
			}
			this.tiledLayer.setAnimatedTile(this.animatedTileIndex, this.sequence[sequenceIndex]);
		}
		public void forward() {
			this.forward = true;
			this.moving = true;
		}
		public void backward() {
			this.forward = false;
			this.moving = true;
		}
		public void setMoving(boolean isMoving) {
			this.moving = isMoving;
		}
	}
	
	private class SpriteAnimationTask extends TimerTask {
		private boolean moving = false;
		private boolean forward = true;
		private Sprite sprite;
		public SpriteAnimationTask(Sprite sprite, boolean forward) {
			this.sprite = sprite;
			this.forward = forward;
		}
		public void run() {
			if (!this.moving)
				return;
			if (this.forward) {
				this.sprite.nextFrame();
			} else {
				this.sprite.prevFrame();
			}
		}
		public void forward() {
			this.forward = true;
			this.moving = true;
		}
		public void backward() {
			this.forward = false;
			this.moving = true;
		}
		public void setMoving(boolean isMoving) {
			this.moving = isMoving;
		}
	}
}
