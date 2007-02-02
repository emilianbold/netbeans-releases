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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.lexer.test;

public class RandomModifyDescriptor {

    private final int opCount;

    private final RandomTextProvider randomTextProvider;

    private final double insertCharRatio;

    private final double insertTextRatio;

    private int insertTextMaxLength = 10;

    private final double insertFixedTextRatio;

    private final double removeCharRatio;

    private final double removeTextRatio;

    private int removeTextMaxLength = 10;

    private final double createSnapshotRatio;
    
    private final double destroySnapshotRatio;

    private double ratioSum;
    
    private boolean ratioSumInited;

    public RandomModifyDescriptor(int opCount, RandomTextProvider randomTextProvider,
    double insertCharRatio, double insertTextRatio, double insertFixedTextRatio,
    double removeCharRatio, double removeTextRatio,
    double createSnapshotRatio, double destroySnapshotRatio) {
        this.opCount = opCount;
        this.randomTextProvider = randomTextProvider;
        this.insertCharRatio = insertCharRatio;
        this.insertTextRatio = insertTextRatio;
        this.insertFixedTextRatio = insertFixedTextRatio;
        this.removeCharRatio = removeCharRatio;
        this.removeTextRatio = removeTextRatio;
        this.createSnapshotRatio = createSnapshotRatio;
        this.destroySnapshotRatio = destroySnapshotRatio;
    }
    
    protected double computeRatioSum() {
        return insertCharRatio + insertTextRatio + insertFixedTextRatio
                + removeCharRatio + removeTextRatio
                + createSnapshotRatio + destroySnapshotRatio;
    }
    
    public int opCount() {
        return opCount;
    }
    
    public RandomTextProvider randomTextProvider() {
        return randomTextProvider;
    }

    public double insertCharRatio() {
        return insertCharRatio;
    }
    
    public double insertTextRatio() {
        return insertTextRatio;
    }
    
    public int insertTextMaxLength() {
        return insertTextMaxLength;
    }
    
    public double insertFixedTextRatio() {
        return insertFixedTextRatio;
    }

    public void setInsertTextMaxLength(int insertTextMaxLength) {
        this.insertTextMaxLength = insertTextMaxLength;
    }
    
    public double removeCharRatio() {
        return removeCharRatio;
    }
    
    public double removeTextRatio() {
        return removeTextRatio;
    }
    
    public int removeTextMaxLength() {
        return removeTextMaxLength;
    }

    public void setRemoveTextMaxLength(int removeTextMaxLength) {
        this.removeTextMaxLength = removeTextMaxLength;
    }
    
    public double ratioSum() {
        if (!ratioSumInited) {
            ratioSumInited = true;
            ratioSum = computeRatioSum();
        }
        return ratioSum;
    }

    public double createSnapshotRatio() {
        return createSnapshotRatio;
    }
    
    public double destroySnapshotRatio() {
        return destroySnapshotRatio;
    }
    
}