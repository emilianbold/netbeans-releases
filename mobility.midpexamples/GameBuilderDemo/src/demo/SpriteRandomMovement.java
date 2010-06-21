/*
 * Copyright (c) 2010, Oracle.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of Oracle nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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

    public void setSequences(int[] downSeq, int downTrans, int[] upSeq, int upTrans, int[] leftSeq, int leftTrans, int[] rightSeq, int rightTrans) {
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
            } else if (this.direction == GameCanvas.UP) {
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
            } else if (this.direction == GameCanvas.LEFT) {
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
            } else if (this.direction == GameCanvas.RIGHT) {
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
