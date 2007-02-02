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

import java.util.Random;

public class RandomTextProvider {

    private static final RandomCharDescriptor[] EMPTY_DESCRIPTORS = {};

    private static final FixedTextDescriptor[] EMPTY_FIXED_TEXTS = {};

    private RandomCharDescriptor[] randomCharDescriptors;

    private FixedTextDescriptor[] fixedTexts;

    private double ratioSum;

    private double fixedTextsRatioSum;

    public RandomTextProvider(RandomCharDescriptor[] randomCharDescriptors) {
        this(randomCharDescriptors, null);
    }

    public RandomTextProvider(RandomCharDescriptor[] randomCharDescriptors,
    FixedTextDescriptor[] fixedTexts) {
        if (randomCharDescriptors == null) {
            randomCharDescriptors = EMPTY_DESCRIPTORS;
        }
        if (fixedTexts == null) {
            fixedTexts = EMPTY_FIXED_TEXTS;
        }
        this.randomCharDescriptors = randomCharDescriptors;
        this.fixedTexts = fixedTexts;
        
        // Compute sum of ratios of all random char descriptors
        for (int i = 0; i < randomCharDescriptors.length; i++) {
            ratioSum += randomCharDescriptors[i].ratio();
        }
        for (int i = 0; i < fixedTexts.length; i++) {
            fixedTextsRatioSum += fixedTexts[i].ratio();
        }
    }
    
    public char randomChar(Random random) {
        double r = random.nextDouble() * ratioSum;
        for (int i = 0; i < randomCharDescriptors.length; i++) {
            RandomCharDescriptor descriptor = randomCharDescriptors[i];
            if ((r -= descriptor.ratio()) < 0) {
                return descriptor.randomChar(random);
            }
        }
        // Internal error - randomCharAvailable() needs to be checked
        throw new IllegalStateException("No random char descriptions available");
    }
    
    public boolean randomCharAvailable() {
        return (randomCharDescriptors.length > 0);
    }
    
    /**
     *
     * @return non-empty random string with length less or equal to maxTextLength.
     */
    public String randomText(Random random, int maxTextLength) {
        if (randomCharAvailable()) {
            int len = random.nextInt(maxTextLength);
            StringBuilder sb = new StringBuilder();
            while (--len >= 0) {
                sb.append(randomChar(random));
            }
            return sb.toString();
        } else {
            return "";
        }
    }
    
    public String randomFixedText(Random random) {
        double r = random.nextDouble() * fixedTextsRatioSum;
        for (int i = 0; i < fixedTexts.length; i++) {
            FixedTextDescriptor fixedText = fixedTexts[i];
            if ((r -= fixedText.ratio()) < 0) {
                return fixedText.text();
            }
        }
        return ""; // no fixed texts available
    }

}