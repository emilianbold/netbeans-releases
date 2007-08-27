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
	private static final int MIN_BUFFER = 20;
	
	private int viewPortX = 0;
	private int viewPortY = 0;
	
	private byte lastDirection = -1;
	
	private TiledLayer tlBase;
	
	private boolean interrupted;
	private LayerManager lm;
	private GameDesign gameDesign;
	private Timer timer;
	
	private Sprite spriteKarel;
	private SpriteAnimationTask spriteKarelAnimator;
	
	private Sprite spriteThomas;
	private SpriteAnimationTask spriteThomasAnimator;
	private SpriteRandomMovement spriteThomasRandomMovement;
	
	private TiledLayer tlWater;
	private TiledLayer tlTrees;
	private TiledLayer tlThings;
	
	private TileAnimationTask waterAnimator;
	
	
	public DemoGameCanvas() {
		super(true);
		try {
			this.setFullScreenMode(true);
			this.init();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Initialize the Game Design, then load all layers and start animation threads.
	 */
	private void init() throws IOException {
		this.timer = new Timer();
		this.gameDesign = new GameDesign();
		
		
		this.spriteKarel = gameDesign.getKarel();
		//define the reference in the midle of sprites frame so that transformations work well
		this.spriteKarel.defineReferencePixel(8, 8);
		this.spriteKarelAnimator = new SpriteAnimationTask(this.spriteKarel, false);
		this.timer.scheduleAtFixedRate(this.spriteKarelAnimator, 0, gameDesign.KarelSeqWalkDownDelay);
		
		
		this.waterAnimator = new TileAnimationTask(gameDesign.getWater(), gameDesign.AnimWaterWater, gameDesign.AnimWaterSeq001, true);
		this.timer.scheduleAtFixedRate(this.waterAnimator, 0, gameDesign.AnimWaterSeq001Delay);
		
		
		this.tlThings = this.gameDesign.getThings();
		this.tlTrees = this.gameDesign.getTrees();
		this.tlWater = this.gameDesign.getWater();
		this.tlBase = this.gameDesign.getBase();
		
		this.lm = new LayerManager();
		gameDesign.updateLayerManagerForForest(lm);
		
		this.spriteThomas = gameDesign.getThomas();
		//define the reference in the midle of sprites frame so that transformations work well
		this.spriteThomas.defineReferencePixel(8, 8);
		this.spriteThomasAnimator = new SpriteAnimationTask(this.spriteThomas, true);
		this.spriteThomasAnimator.setMoving(true);
		this.timer.scheduleAtFixedRate(this.spriteThomasAnimator, 0, gameDesign.ThomasSeqWalkHorizDelay);
		this.spriteThomasRandomMovement = new SpriteRandomMovement(this, spriteThomas);
		this.spriteThomasRandomMovement.setSequences(
				gameDesign.ThomasSeqWalkVert, Sprite.TRANS_NONE,
				gameDesign.ThomasSeqWalkVert, Sprite.TRANS_ROT180,
				gameDesign.ThomasSeqWalkHoriz, Sprite.TRANS_ROT180,
				gameDesign.ThomasSeqWalkHoriz, Sprite.TRANS_NONE);
		(new Thread(spriteThomasRandomMovement)).start();
	}
	
	/**
	 * Check if sprite collides with either the other sprite or
	 * with a layer that holds obstacles or with the edge of the base layer.
	 *
	 * @param sprite the sprite checked for collision with other layers
	 * @return true is sprite does collide, false otherwise
	 */
	public boolean spriteCollides(Sprite sprite) {
		return (sprite.collidesWith(sprite == this.spriteKarel ? this.spriteThomas : this.spriteKarel, true)
				|| sprite.collidesWith(this.tlThings, true)
				|| sprite.collidesWith(this.tlTrees, true)
				|| sprite.collidesWith(this.tlWater, true)
				|| sprite.getX() < 0
				|| sprite.getY() < 0
				|| sprite.getX() > (this.tlBase.getWidth() - sprite.getWidth())
				|| sprite.getY() > (this.tlBase.getHeight() - sprite.getHeight())
				);
	}
	
	/**
	 * Adjust the viewport to keep the main animated sprite inside the screen.
	 * The coordinates are checked for game bounaries and adjusted only if it
	 * makes sense.
	 *
	 * @param x viewport X coordinate
	 * @param y viewport Y coordinate
	 */
	private void adjustViewport(int x, int y) {
		
		int sx = this.spriteKarel.getX();
		int sy = this.spriteKarel.getY();
		
		int xmin = this.viewPortX + MIN_BUFFER;
		int xmax = this.viewPortX + this.getWidth() - this.spriteKarel.getWidth() - MIN_BUFFER;
		int ymin = this.viewPortY + MIN_BUFFER;
		int ymax = this.viewPortY + this.getHeight() - this.spriteKarel.getHeight() - MIN_BUFFER;
		
		//if the sprite is not near the any screen edges don't adjust
		if (
				sx >= xmin
				&& sx <= xmax
				&& sy >= ymin
				&& sy <= ymax
				) {
			return;
		}
		
		//if the sprite is moving left but isn't near the left edge of the screen don't adjust
		if ((this.lastDirection == LEFT && sx >= xmin)) {
			return;
		}
		//if the sprite is moving right but isn't near the right edge of the screen don't adjust
		if (this.lastDirection == RIGHT && sx <= xmax) {
			return;
		}
		//if the sprite is moving up but isn't at near top edge of the screen don't adjust
		if (this.lastDirection == UP && sy >= ymin) {
			return;
		}
		//if the sprite is moving down but isn't at near bottom edge of the screen don't adjust
		if (this.lastDirection == DOWN && sy <= ymax) {
			return;
		}
		
		//only adjust x to values that ensure the base tiled layer remains visible
		//and no white space is shown
		if (x < this.tlBase.getX()) {
			this.viewPortX = this.tlBase.getX();
		} else if (x > this.tlBase.getX() + this.tlBase.getWidth() - this.getWidth()) {
			this.viewPortX = this.tlBase.getX() + this.tlBase.getWidth() - this.getWidth();
		} else {
			this.viewPortX = x;
		}
		
		//only adjust y to values that ensure the base tiled layer remains visible
		//and no white space is shown
		if (y < this.tlBase.getY()) {
			this.viewPortY = this.tlBase.getY();
		} else if (y > this.tlBase.getY() + this.tlBase.getHeight() - this.getHeight()) {
			this.viewPortY = this.tlBase.getY() + this.tlBase.getHeight() - this.getHeight();
		} else {
			this.viewPortY = y;
		}
		
		//adjust the viewport
		this.lm.setViewWindow(this.viewPortX, this.viewPortY, this.getWidth(), this.getHeight());
	}
	
	
	/**
	 * The main game loop that checks for user input and repaints canvas.
	 */
	public void run() {
		Graphics g = getGraphics();
		
		while (!this.interrupted) {
			//check for user input
			int keyState = getKeyStates();
			
			//if user is pressing the left button
			if ((keyState & LEFT_PRESSED) != 0)  {
				//if the previous direction was other than left set the sequence
				//correct sequence & transform needed for walking to the left
				if (this.lastDirection != LEFT) {
					this.lastDirection = LEFT;
					this.spriteKarel.setFrameSequence(gameDesign.KarelSeqWalkSide);
					this.spriteKarel.setTransform(Sprite.TRANS_MIRROR);
					continue;
				}
				//assign the sequence playback direction
				this.spriteKarelAnimator.forward();
				//move the sprite to the left
				this.spriteKarel.move(-SPEED, 0);
				//if moving the sprite generates a collision return sprite back
				//to its original position
				if (this.spriteCollides(this.spriteKarel)) {
					this.spriteKarel.move(SPEED, 0);
					continue;
				}
				//attempt to adjust the viewport to keep the sprite on the screen
				this.adjustViewport(this.viewPortX - SPEED, this.viewPortY);
			} else if ((keyState & RIGHT_PRESSED) != 0) {
				if (this.lastDirection != RIGHT) {
					this.lastDirection = RIGHT;
					this.spriteKarel.setFrameSequence(gameDesign.KarelSeqWalkSide);
					this.spriteKarel.setTransform(Sprite.TRANS_NONE);
					continue;
				}
				this.spriteKarelAnimator.forward();
				this.spriteKarel.move(SPEED, 0);
				if (this.spriteCollides(this.spriteKarel)) {
					this.spriteKarel.move(-SPEED, 0);
					continue;
				}
				this.adjustViewport(this.viewPortX + SPEED, this.viewPortY);
			} else if ((keyState & UP_PRESSED) != 0) {
				if (this.lastDirection != UP) {
					this.lastDirection = UP;
					this.spriteKarel.setFrameSequence(gameDesign.KarelSeqWalkUp);
					this.spriteKarel.setTransform(Sprite.TRANS_NONE);
					continue;
				}
				this.spriteKarelAnimator.forward();
				this.spriteKarel.move(0, -SPEED);
				if (this.spriteCollides(this.spriteKarel)) {
					this.spriteKarel.move(0, SPEED);
					continue;
				}
				this.adjustViewport(this.viewPortX, this.viewPortY - SPEED);
			} else if ((keyState & DOWN_PRESSED) != 0) {
				if (this.lastDirection != DOWN) {
					this.lastDirection = DOWN;
					this.spriteKarel.setFrameSequence(gameDesign.KarelSeqWalkDown);
					this.spriteKarel.setTransform(Sprite.TRANS_NONE);
					continue;
				}
				this.spriteKarelAnimator.forward();
				this.spriteKarel.move(0, SPEED);
				if (this.spriteCollides(this.spriteKarel)) {
					this.spriteKarel.move(0, -SPEED);
					continue;
				}
				this.adjustViewport(this.viewPortX, this.viewPortY + SPEED);
			} else {
				this.spriteKarelAnimator.setMoving(false);
			}
			
			this.lm.paint(g, 0, 0);
			flushGraphics(0, 0, this.getWidth(), this.getHeight());
			
			try {
				Thread.sleep(20);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * Stops the main game loop.
	 */
	public void stop() {
		this.interrupted = true;
		this.spriteThomasRandomMovement.stop();
	}
	
	/**
	 * Animates animated tiles in a tiled layer.
	 */
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
	
	/**
	 * Animates a sprite.
	 */
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
