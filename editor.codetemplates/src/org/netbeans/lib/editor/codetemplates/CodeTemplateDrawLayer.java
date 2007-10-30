/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.lib.editor.codetemplates;

import java.awt.Color;
import javax.swing.text.AttributeSet;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.DrawContext;
import org.netbeans.editor.DrawLayer;
import org.netbeans.editor.MarkFactory;
import org.netbeans.lib.editor.util.swing.PositionRegion;

/**
 * Code template drawing layer allows to render frames around the text
 * and highlight regions of the current parameter.
 *
 * @author Miloslav Metelka
 */
final class CodeTemplateDrawLayer extends DrawLayer.AbstractLayer {
    
    public static final String NAME = "code-template-draw-layer"; // NOI18N
    
    public static final int VISIBILITY = 5000;
    
    private static final Coloring defaultSyncedTextBlocksHighlight = new Coloring(null, null, new Color(138, 191, 236));
    
    private static int instanceCounter;
    
    private CodeTemplateParameterImpl paramImpl;
    
    private CodeTemplateInsertHandler handler;
    
    private int regionIndex;
    
    private Position regionStartPosition;
    
    private Position regionEndPosition;
    
    private boolean colorBackground;
    
    private Coloring coloring;
    
    private boolean textFramePropertyAssigned;
    
    CodeTemplateDrawLayer(CodeTemplateParameterImpl paramImpl) {
        super(NAME + instanceCounter++); // must have distinct names
        this.paramImpl = paramImpl;

        handler = paramImpl.getHandler();
    }
    
    public @Override void init(DrawContext ctx) {
        coloring = null;
        JTextComponent c = ctx.getEditorUI().getComponent();
        regionStartPosition = null;
        if (c != null) {
            if (handler.getActiveMasterImpl() == paramImpl) {
                colorBackground = true;
                int startOffset = ctx.getStartOffset();
                regionIndex = 0;
                SyncDocumentRegion syncRegion = paramImpl.getRegion();
                int regionCount = syncRegion.getRegionCount();
                while (regionIndex < regionCount) {
                    PositionRegion region = syncRegion.getSortedRegion(regionIndex);
                    Position startPos = region.getStartPosition();
                    if (startOffset <= startPos.getOffset()) {
                        regionStartPosition = startPos;
                        regionEndPosition = region.getEndPosition();
                        setNextActivityChangeOffset(regionStartPosition.getOffset());
                        break;
                    }
                    regionIndex++;
                }
            } else {
                colorBackground = false;
            }
        }
    }

    public boolean isActive(DrawContext ctx, MarkFactory.DrawMark mark) {
        if (regionStartPosition != null) {
            int regionStartOffset = regionStartPosition.getOffset();
            int regionEndOffset = regionEndPosition.getOffset();
            int fragmentOffset = ctx.getFragmentOffset();
            SyncDocumentRegion syncRegion = paramImpl.getRegion();
            if (fragmentOffset == regionStartOffset && regionStartOffset != regionEndOffset) {
                // Cannot set earlier as there could be other text frames
                // located before this one
                if (regionStartOffset == syncRegion.getFirstRegionStartOffset()) {
                    JTextComponent c = ctx.getEditorUI().getComponent();
                    c.putClientProperty(DrawLayer.TEXT_FRAME_START_POSITION_COMPONENT_PROPERTY, 
                            regionStartPosition);
                    c.putClientProperty(DrawLayer.TEXT_FRAME_END_POSITION_COMPONENT_PROPERTY,
                            regionEndPosition);
                    textFramePropertyAssigned = true;
                }
                coloring = colorBackground ? getSyncedTextBlocksHighlight() : null;
                setNextActivityChangeOffset(regionEndOffset);

            } else if (fragmentOffset == regionEndOffset) {
                coloring = null;
                regionIndex++;
                if (regionIndex < syncRegion.getRegionCount()) {
                    PositionRegion region = syncRegion.getSortedRegion(regionIndex);
                    regionStartPosition = region.getStartPosition();
                    regionEndPosition = region.getEndPosition();
                    setNextActivityChangeOffset(regionStartPosition.getOffset());
                } else {
                    regionStartPosition = null;
                    regionEndPosition = null;
                }
                JTextComponent c = ctx.getEditorUI().getComponent();
                resetTextFrameProperties(c);
            }
            return true;

        } else {
            return false;
        }
    }
    
    void resetTextFrameProperties(JTextComponent c) {
        if (textFramePropertyAssigned) {
            c.putClientProperty(DrawLayer.TEXT_FRAME_START_POSITION_COMPONENT_PROPERTY, null);
            c.putClientProperty(DrawLayer.TEXT_FRAME_END_POSITION_COMPONENT_PROPERTY, null);
            textFramePropertyAssigned = false;
        }
    }
    
    public void updateContext(DrawContext ctx) {
        if (coloring != null) {
            coloring.apply(ctx);
        }
    }

    private static Coloring getSyncedTextBlocksHighlight() {
        FontColorSettings fcs = MimeLookup.getLookup(MimePath.EMPTY).lookup(FontColorSettings.class);
        AttributeSet as = fcs.getFontColors("synchronized-text-blocks"); //NOI18N
        return as == null ? defaultSyncedTextBlocksHighlight : Coloring.fromAttributeSet(as);
    }
}
