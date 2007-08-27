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

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;

/**
 * @author  Karel Herink
 * @version 1.0
 */
public class GameDesign {
    
    //<editor-fold defaultstate="collapsed" desc=" Generated Fields ">//GEN-BEGIN:|fields|0|
    private Sprite Thomas;
    public int ThomasSeqWalkHorizDelay = 140;
    public int[] ThomasSeqWalkHoriz = {80, 81, 82, 83};
    public int ThomasSeqWalkVertDelay = 140;
    public int[] ThomasSeqWalkVert = {84, 85, 86, 87};
    private TiledLayer Things;
    private TiledLayer Water;
    public int AnimWaterWater;
    private Image platform_tiles;
    private Image topview_tiles;
    public int AnimWaterSeq001Delay = 200;
    public int[] AnimWaterSeq001 = {71, 72, 73, 74, 75};
    private Sprite Karel;
    public int KarelSeqWalkUpDelay = 150;
    public int[] KarelSeqWalkUp = {94, 95, 96, 97};
    public int KarelSeqWalkSideFastDelay = 100;
    public int[] KarelSeqWalkSideFast = {100, 101, 102, 103};
    public int KarelSeqWalkDownDelay = 150;
    public int[] KarelSeqWalkDown = {88, 89, 90, 91};
    public int KarelSeqWalkSideDelay = 150;
    public int[] KarelSeqWalkSide = {100, 101, 102, 103};
    private TiledLayer Base;
    private TiledLayer Trees;
    //</editor-fold>//GEN-END:|fields|0|
    
    //<editor-fold defaultstate="collapsed" desc=" Generated Methods ">//GEN-BEGIN:|methods|0|
    //</editor-fold>//GEN-END:|methods|0|

    public void updateLayerManagerForForest(LayerManager lm) throws java.io.IOException {//GEN-LINE:|1-updateLayerManager|0|1-preUpdate
        // write pre-update user code here
        getKarel().setPosition(55, 43);//GEN-BEGIN:|1-updateLayerManager|1|1-postUpdate
        getKarel().setVisible(true);
        lm.append(getKarel());
        getThomas().setPosition(67, 80);
        getThomas().setVisible(true);
        lm.append(getThomas());
        getTrees().setPosition(191, 3);
        getTrees().setVisible(true);
        lm.append(getTrees());
        getThings().setPosition(16, 111);
        getThings().setVisible(true);
        lm.append(getThings());
        getWater().setPosition(109, 98);
        getWater().setVisible(true);
        lm.append(getWater());
        getBase().setPosition(0, 0);
        getBase().setVisible(true);
        lm.append(getBase());//GEN-END:|1-updateLayerManager|1|1-postUpdate
        // write post-update user code here
    }//GEN-BEGIN:|1-updateLayerManager|2|
//GEN-END:|1-updateLayerManager|2|

    public Image getTopview_tiles() throws java.io.IOException {//GEN-BEGIN:|2-getter|0|2-preInit
        if (topview_tiles == null) {//GEN-END:|2-getter|0|2-preInit
            // write pre-init user code here
            topview_tiles = Image.createImage("/topview_tiles.png");//GEN-BEGIN:|2-getter|1|2-postInit
        }//GEN-END:|2-getter|1|2-postInit
        // write post-init user code here
        return this.topview_tiles;//GEN-BEGIN:|2-getter|2|
    }
//GEN-END:|2-getter|2|

    public TiledLayer getWater() throws java.io.IOException {//GEN-BEGIN:|3-getter|0|3-preInit
        if (Water == null) {//GEN-END:|3-getter|0|3-preInit
            // write pre-init user code here
            Water = new TiledLayer(8, 6, getTopview_tiles(), 16, 16);//GEN-BEGIN:|3-getter|1|3-midInit
            AnimWaterWater = Water.createAnimatedTile(AnimWaterSeq001[0]);
            int[][] tiles = {
                { 0, 0, 44, 31, 31, 31, 31, 32 },
                { 0, 0, 47, 59, AnimWaterWater, 59, AnimWaterWater, 48 },
                { 0, 0, 47, AnimWaterWater, 59, 61, 64, 45 },
                { 0, 0, 38, AnimWaterWater, AnimWaterWater, 40, 0, 0 },
                { 44, 31, 62, AnimWaterWater, AnimWaterWater, 48, 0, 0 },
                { 46, 64, 64, 64, 64, 45, 0, 0 }
            };//GEN-END:|3-getter|1|3-midInit
            // write mid-init user code here
            for (int row = 0; row < 6; row++) {//GEN-BEGIN:|3-getter|2|3-postInit
                for (int col = 0; col < 8; col++) {
                    Water.setCell(col, row, tiles[row][col]);
                }
            }
        }//GEN-END:|3-getter|2|3-postInit
        // write post-init user code here
        return Water;//GEN-BEGIN:|3-getter|3|
    }
//GEN-END:|3-getter|3|

    public TiledLayer getBase() throws java.io.IOException {//GEN-BEGIN:|6-getter|0|6-preInit
        if (Base == null) {//GEN-END:|6-getter|0|6-preInit
            // write pre-init user code here
            Base = new TiledLayer(24, 23, getTopview_tiles(), 16, 16);//GEN-BEGIN:|6-getter|1|6-midInit
            int[][] tiles = {
                { 7, 3, 17, 6, 6, 6, 6, 35, 23, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16 },
                { 7, 3, 5, 16, 16, 16, 16, 26, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16 },
                { 24, 6, 23, 16, 16, 16, 16, 26, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16 },
                { 16, 16, 16, 16, 16, 16, 16, 26, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16 },
                { 16, 16, 16, 16, 16, 16, 16, 26, 16, 16, 16, 11, 11, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16 },
                { 16, 16, 16, 16, 16, 16, 16, 26, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16 },
                { 16, 15, 16, 16, 16, 16, 16, 30, 28, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16 },
                { 16, 16, 15, 16, 16, 8, 16, 16, 26, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16 },
                { 16, 16, 16, 16, 16, 16, 16, 16, 26, 16, 16, 16, 16, 16, 16, 16, 16, 16, 14, 15, 16, 16, 16, 16 },
                { 16, 16, 16, 16, 16, 16, 16, 16, 30, 25, 25, 25, 25, 28, 16, 16, 16, 16, 14, 16, 16, 16, 16, 16 },
                { 16, 16, 16, 16, 8, 16, 16, 11, 16, 16, 16, 16, 16, 26, 16, 16, 16, 14, 16, 16, 15, 16, 16, 16 },
                { 16, 16, 16, 16, 16, 16, 11, 11, 11, 11, 16, 16, 16, 26, 16, 16, 16, 16, 15, 16, 16, 16, 16, 16 },
                { 16, 16, 16, 16, 16, 11, 11, 11, 11, 11, 11, 16, 16, 26, 16, 16, 16, 16, 16, 15, 15, 16, 16, 16 },
                { 16, 16, 16, 16, 16, 11, 11, 11, 11, 11, 11, 16, 22, 36, 21, 16, 16, 16, 16, 16, 16, 16, 15, 16 },
                { 16, 16, 16, 16, 16, 11, 11, 11, 11, 11, 11, 16, 7, 3, 5, 16, 16, 16, 16, 16, 16, 15, 16, 16 },
                { 16, 16, 16, 16, 16, 11, 11, 11, 11, 11, 16, 27, 34, 1, 5, 16, 16, 16, 16, 16, 14, 16, 16, 16 },
                { 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 26, 24, 6, 23, 16, 16, 16, 16, 16, 14, 16, 16, 16 },
                { 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 26, 16, 16, 16, 16, 16, 15, 14, 14, 16, 16, 16, 16 },
                { 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 29, 16, 16, 16, 16, 16, 15, 15, 14, 16, 16, 16, 16 },
                { 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 15, 15, 16, 16, 16, 16, 16, 16, 16 },
                { 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 15, 16, 16, 15, 16, 16, 16, 16, 16, 16 },
                { 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 15, 16, 16, 16, 15, 16, 16, 16, 16 },
                { 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 15, 16, 16, 16, 16 }
            };//GEN-END:|6-getter|1|6-midInit
            // write mid-init user code here
            for (int row = 0; row < 23; row++) {//GEN-BEGIN:|6-getter|2|6-postInit
                for (int col = 0; col < 24; col++) {
                    Base.setCell(col, row, tiles[row][col]);
                }
            }
        }//GEN-END:|6-getter|2|6-postInit
        // write post-init user code here
        return Base;//GEN-BEGIN:|6-getter|3|
    }
//GEN-END:|6-getter|3|

    public TiledLayer getThings() throws java.io.IOException {//GEN-BEGIN:|31-getter|0|31-preInit
        if (Things == null) {//GEN-END:|31-getter|0|31-preInit
            // write pre-init user code here
            Things = new TiledLayer(15, 15, getTopview_tiles(), 16, 16);//GEN-BEGIN:|31-getter|1|31-midInit
            int[][] tiles = {
                { 0, 0, 0, 0, 0, 0, 0, 68, 0, 0, 78, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 67, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 67, 67, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 67, 67, 0, 0, 0, 0, 0, 0, 0, 0, 0, 70, 78, 0 },
                { 0, 0, 67, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0 },
                { 0, 0, 0, 67, 0, 78, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 69, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 79, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 78, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 69, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }
            };//GEN-END:|31-getter|1|31-midInit
            // write mid-init user code here
            for (int row = 0; row < 15; row++) {//GEN-BEGIN:|31-getter|2|31-postInit
                for (int col = 0; col < 15; col++) {
                    Things.setCell(col, row, tiles[row][col]);
                }
            }
        }//GEN-END:|31-getter|2|31-postInit
        // write post-init user code here
        return Things;//GEN-BEGIN:|31-getter|3|
    }
//GEN-END:|31-getter|3|

        public Sprite getThomas() throws java.io.IOException {//GEN-BEGIN:|51-getter|0|51-preInit
            if (Thomas == null) {//GEN-END:|51-getter|0|51-preInit
                // write pre-init user code here
                Thomas = new Sprite(getTopview_tiles(), 16, 16);//GEN-BEGIN:|51-getter|1|51-postInit
                Thomas.setFrameSequence(ThomasSeqWalkVert);//GEN-END:|51-getter|1|51-postInit
                // write post-init user code here
            }//GEN-BEGIN:|51-getter|2|
            return Thomas;
        }
//GEN-END:|51-getter|2|

        public Sprite getKarel() throws java.io.IOException {//GEN-BEGIN:|70-getter|0|70-preInit
            if (Karel == null) {//GEN-END:|70-getter|0|70-preInit
                // write pre-init user code here
                Karel = new Sprite(getTopview_tiles(), 16, 16);//GEN-BEGIN:|70-getter|1|70-postInit
                Karel.setFrameSequence(KarelSeqWalkDown);//GEN-END:|70-getter|1|70-postInit
                // write post-init user code here
            }//GEN-BEGIN:|70-getter|2|
            return Karel;
        }
//GEN-END:|70-getter|2|



        public Image getPlatform_tiles() throws java.io.IOException {//GEN-BEGIN:|161-getter|0|161-preInit
            if (platform_tiles == null) {//GEN-END:|161-getter|0|161-preInit
                // write pre-init user code here
                platform_tiles = Image.createImage("/platform_tiles.png");//GEN-BEGIN:|161-getter|1|161-postInit
            }//GEN-END:|161-getter|1|161-postInit
            // write post-init user code here
            return this.platform_tiles;//GEN-BEGIN:|161-getter|2|
        }
//GEN-END:|161-getter|2|

        public TiledLayer getTrees() throws java.io.IOException {//GEN-BEGIN:|276-getter|0|276-preInit
            if (Trees == null) {//GEN-END:|276-getter|0|276-preInit
                // write pre-init user code here
                Trees = new TiledLayer(11, 22, getTopview_tiles(), 16, 16);//GEN-BEGIN:|276-getter|1|276-midInit
                int[][] tiles = {
                    { 10, 10, 10, 10, 10, 10, 10, 0, 0, 0, 0 },
                    { 0, 12, 10, 10, 10, 10, 10, 10, 0, 0, 0 },
                    { 0, 12, 10, 12, 10, 10, 10, 10, 0, 10, 0 },
                    { 9, 0, 12, 10, 10, 10, 10, 10, 10, 10, 0 },
                    { 0, 0, 12, 12, 10, 10, 10, 10, 10, 10, 0 },
                    { 0, 0, 0, 12, 10, 10, 10, 10, 0, 0, 0 },
                    { 0, 0, 0, 12, 12, 10, 10, 10, 0, 0, 0 },
                    { 0, 0, 0, 12, 10, 10, 12, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 10, 10, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 12, 10, 0, 0, 13, 0, 0 },
                    { 0, 0, 0, 12, 0, 0, 0, 0, 13, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 0, 13, 13, 13 },
                    { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13 },
                    { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13 },
                    { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 13, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 13, 13, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 13, 0, 13, 13, 13, 0, 0, 0 }
                };//GEN-END:|276-getter|1|276-midInit
                // write mid-init user code here
                for (int row = 0; row < 22; row++) {//GEN-BEGIN:|276-getter|2|276-postInit
                    for (int col = 0; col < 11; col++) {
                        Trees.setCell(col, row, tiles[row][col]);
                    }
                }
            }//GEN-END:|276-getter|2|276-postInit
            // write post-init user code here
            return Trees;//GEN-BEGIN:|276-getter|3|
        }
//GEN-END:|276-getter|3|


    
}
