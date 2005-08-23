/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.editor.codetemplates;

import java.awt.Color;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
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
    
    public static final String NAME = "code-template-draw-layer";
    
    public static final int VISIBILITY = 5000;
    
    private static final Coloring COLORING = new Coloring(null, null, new Color(138, 191, 236));
    
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
    
    public void init(DrawContext ctx) {
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
                coloring = colorBackground ? COLORING : null;
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

}
