/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.compapp.casaeditor.graph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 * A widget that supports multi-line text.
 * The text is automatically centered horizontally.
 * To center the text vertically, ensure this widget stretches to the full
 * height of whatever widget contains this widget.
 * 
 * @author Josh Sandusky
 */
public class LineBreakingLabelWidget extends Widget {
    
    private static final int MINIMUM_WIDTH = 100;
    private static final String[] ELLIPSES = { "..." };
    
    private String mLabel;
    private Color mTextColor;
    private Font mFont;
    private LabelWidget[] mLabelWidgets;
    private Dimension mLastPreferredSize = new Dimension();
    private LineBreakMeasurer mLineMeasurer;
    private int mParagraphStart;
    private int mParagraphEnd;
    private DependenciesRegistry mDependenciesRegistry = new DependenciesRegistry(this);
    
    
    public LineBreakingLabelWidget(final Scene scene) {
        super(scene);
        
        // Set up a vertical layout of label widgets, justified to the same width.
        setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, 4));
    }
    
    
    protected void notifyAdded() {
        mDependenciesRegistry.registerDependency(new Widget.Dependency() {
            private boolean mIsIgnore;
            public void revalidateDependency() {
                if (mIsIgnore || getBounds() == null) {
                    return;
                }
                if (reBreak()) {
                    try {
                        mIsIgnore = true;
                        getScene().validate();
                    } finally {
                        mIsIgnore = false;
                    }
                }
            }
        });
    }
    
    protected void notifyRemoved() {
        mDependenciesRegistry.removeAllDependencies();
    }
    
    public void setText(String label, Color textColor, Font font) {
        mLabel = label;
        mTextColor = textColor;
        mFont = font;
        
        AttributedString attributedString = new AttributedString(label, mFont.getAttributes());
        AttributedCharacterIterator paragraph = attributedString.getIterator();
        mParagraphStart = paragraph.getBeginIndex();
        mParagraphEnd = paragraph.getEndIndex();
        
        // Create a new LineBreakMeasurer from the paragraph.
        mLineMeasurer = new LineBreakMeasurer(
                paragraph, 
                new FontRenderContext(null, false, false));

        reBreak();
    }
    
    private boolean reBreak() {
        if (getPreferredBounds() == null || getPreferredBounds().getSize().equals(mLastPreferredSize)) {
            // As long as our size remains the same, line-breaking will not change.
            return false;
        }
        mLastPreferredSize = getPreferredBounds().getSize();
        
        boolean isValidateRequired = false;
        String[] stringWithLineBreaks = calculateLineBreaks(mLabel);
        if (stringWithLineBreaks == null) {
            return false;
        }
        
        if (
                mLabelWidgets != null && 
                stringWithLineBreaks.length == mLabelWidgets.length) {
            // Just update the text, no need to remove/add widgets.
            for (int i=0; i < stringWithLineBreaks.length; i++) {
                mLabelWidgets[i].setLabel(stringWithLineBreaks[i]);
            }
        } else {
            // Each line is represented with its own single-line label widget.
            isValidateRequired = true;
            removeChildren();
            mLabelWidgets = new LabelWidget[stringWithLineBreaks.length];
            for (int i=0; i < stringWithLineBreaks.length; i++) {
                mLabelWidgets[i] = new LabelWidget(getScene(), stringWithLineBreaks[i]);
                mLabelWidgets[i].setAlignment(LabelWidget.Alignment.CENTER);
                mLabelWidgets[i].setForeground(mTextColor);
                mLabelWidgets[i].setFont(mFont);
                addChild(mLabelWidgets[i]);
            }
        }
        
        return isValidateRequired;
    }
    
    public void animateVisible(Color color) {
        mTextColor = color;
        if (mLabelWidgets != null) {
            for (LabelWidget labelWidget : mLabelWidgets) {
                labelWidget.setForeground(color);
                // forego the animation, this seems to not work reliably
//                getScene().getSceneAnimator().animateForegroundColor(labelWidget, color);
            }
        }
    }
    
    private String[] calculateLineBreaks(String str) {
        // Only use 3/4 of the width for our text.
        float formatWidth = (float) getPreferredBounds().width * 0.75f;
        if (formatWidth <= 0) {
            return null;
        } else if (formatWidth < MINIMUM_WIDTH) {
            return ELLIPSES;
        }
        
        mLineMeasurer.setPosition(mParagraphStart);
        int oldPos = mParagraphStart;
        List<String> strings = new ArrayList<String>();
        
        // Get lines from lineMeasurer until the entire
        // paragraph has been displayed.
        while (mLineMeasurer.getPosition() < mParagraphEnd) {
            int pos = mLineMeasurer.nextOffset(formatWidth);
            mLineMeasurer.setPosition(pos);
            strings.add(str.substring(oldPos, pos));
            oldPos = pos;
        }

        return (String[]) strings.toArray(new String[strings.size()]);
    }
}
