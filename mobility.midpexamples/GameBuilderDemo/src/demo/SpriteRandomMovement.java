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

import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.lcdui.game.Sprite;

/**
 * Animate a sprite on canvas using a simple algorithm
 * to recover from collisions.
 * 
 * @author Karel Herink
 */
public class SpriteRandomMovement implements Runnable {

	private static final int SPEED = 3;

	private DemoGameCanvas canvas;
	private Sprite sprite;
	private byte previousDirection = GameCanvas.DOWN;
	private byte direction = GameCanvas.DOWN;
	private boolean interrupted;
	
	private int[] downSeq;
	private int downTrans;
	
	private int[] upSeq;
	private int upTrans;
	
	private int[] leftSeq;
	private int leftTrans;
	
	private int[] rightSeq;
	private int rightTrans;
	
    public SpriteRandomMovement(DemoGameCanvas canvas, Sprite sprite) {
		this.canvas = canvas;
		this.sprite = sprite;
    }
	
	public void setSequences(
			int[] downSeq, int downTrans,
			int[] upSeq, int upTrans,
			int[] leftSeq, int leftTrans,
			int[] rightSeq, int rightTrans
			) {
		this.downSeq = downSeq;
		this.downTrans = downTrans;
		this.upSeq = upSeq;
		this.upTrans = upTrans;
		this.leftSeq = leftSeq;
		this.leftTrans = leftTrans;
		this.rightSeq = rightSeq;
		this.rightTrans = rightTrans;
	}
	
	public void stop() {
		this.interrupted = true;
	}

    public void run() {
		while (!this.interrupted) {
			if (this.direction == GameCanvas.DOWN) {
				if (this.previousDirection != this.direction) {
					this.sprite.setFrameSequence(this.downSeq);
					this.sprite.setTransform(this.downTrans);
					this.previousDirection = this.direction;
				}
				this.sprite.move(0, SPEED);
				if (this.canvas.spriteCollides(this.sprite)) {
					this.sprite.move(0, -SPEED);
					this.direction = GameCanvas.LEFT;
					continue;
				}
			}
			else if (this.direction == GameCanvas.UP) {
				if (this.previousDirection != this.direction) {
					this.sprite.setFrameSequence(this.upSeq);
					this.sprite.setTransform(this.upTrans);
					this.previousDirection = this.direction;
				}
				this.sprite.move(0, -SPEED);
				if (this.canvas.spriteCollides(this.sprite)) {
					this.sprite.move(0, SPEED);
					this.direction = GameCanvas.RIGHT;
					continue;
				}
			}
			else if (this.direction == GameCanvas.LEFT) {
				if (this.previousDirection != this.direction) {
					this.sprite.setFrameSequence(this.leftSeq);
					this.sprite.setTransform(this.leftTrans);
					this.previousDirection = this.direction;
				}
				this.sprite.move(-SPEED, 0);
				if (this.canvas.spriteCollides(this.sprite)) {
					this.sprite.move(SPEED, 0);
					this.direction = GameCanvas.UP;
					continue;
				}
			}
			else if (this.direction == GameCanvas.RIGHT) {
				if (this.previousDirection != this.direction) {
					this.sprite.setFrameSequence(this.rightSeq);
					this.sprite.setTransform(this.rightTrans);
					this.previousDirection = this.direction;
				}
				this.sprite.move(SPEED, 0);
				if (this.canvas.spriteCollides(this.sprite)) {
					this.sprite.move(-SPEED, 0);
					this.direction = GameCanvas.DOWN;
					continue;
				}
			}
			try {
				Thread.sleep(300);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
    }

}
