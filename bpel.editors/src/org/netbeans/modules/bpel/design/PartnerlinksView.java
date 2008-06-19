/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.bpel.design;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.geometry.FBounds;
import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.PartnerRole;
import org.netbeans.modules.bpel.design.model.connections.Connection;
import org.netbeans.modules.bpel.design.model.connections.MessageConnection;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.design.model.patterns.PartnerlinkPattern;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;
import org.netbeans.modules.bpel.design.selection.PlaceHolder;
import org.netbeans.modules.bpel.design.selection.placeholders.PartnerlinkPlaceholder;

import org.netbeans.modules.bpel.model.api.BpelEntity;

/**
 *
 * @author Alexey
 */
public class PartnerlinksView extends DiagramView {

    private PartnerRole mode;

    public PartnerlinksView(DesignView designView, PartnerRole mode) {
        super(designView);
        this.mode = mode;

        // vlv: print
        putClientProperty("print.printable", Boolean.TRUE); // NOI18N
        putClientProperty("print.weight", mode.getWeight()); // NOI18N
    }

    public PartnerRole getMode() {
        return this.mode;
    }

    @Override
    protected void paintPattern(Graphics2D g2, Graphics2D g2bw, Pattern pattern, FBounds clipBounds, boolean printMode) {
        List<Pattern> to_draw = getDesignView().getModel().getPartnerLinks(getMode());

        if (to_draw != null) {
            for (Pattern p : to_draw) {
                super.paintPattern(g2, g2bw, p, clipBounds, printMode);
            }
        }
    }

    @Override
    protected void paintPatternConnections(Graphics2D g2, Graphics2D g2bw, Pattern pattern, boolean printMode) {
        List<Pattern> to_draw = getDesignView().getModel().getPartnerLinks(getMode());
        if (to_draw != null) {
            for (Pattern p : to_draw) {
                for (VisualElement e : p.getElements()) {
                    for (Connection c : e.getAllConnections()) {
                        if (c instanceof MessageConnection) {
                            ((MessageConnection) c).paintPL(g2);
                        }
                    }
                }
            }
        }

    }

    @Override
    public FBounds getContentSize() {
        List<Pattern> my_patterns =
                getDesignView().getModel().getPartnerLinks(getMode());
        List<FBounds> bounds = new ArrayList<FBounds>(my_patterns.size());
        for (Pattern p : my_patterns) {
            bounds.add(p.getBounds());
        }
        return new FBounds(bounds);
    }

    @Override
    public Iterator<Pattern> getPatterns() {
        return getDesignView().getModel().getPartnerLinks(getMode()).iterator();
    }

    @Override
    public void getPlaceholders(Pattern draggedPattern, List<PlaceHolder> placeHolders) {
        
        if (!(draggedPattern instanceof PartnerlinkPattern)){
            return;
        }
        
        FBounds contentBounds = getContentSize();
        
        List<Pattern> patterns = getDesignView().getModel().getPartnerLinks(getMode());
        
        if (patterns.indexOf(draggedPattern) != 0) {
            placeHolders.add(new PartnerlinkPlaceholder(this, null, draggedPattern, contentBounds.getCenterX(),0));
        }
        
        for(Pattern p : patterns){
            if ( p == draggedPattern){
                continue;
            }
            BpelEntity insertAfter = p.getOMReference();
            FBounds bounds = p.getBounds();
            placeHolders.add(new PartnerlinkPlaceholder(this, insertAfter, draggedPattern, contentBounds.getCenterX(),bounds.getMaxY() + 10));
        }
    }
}
