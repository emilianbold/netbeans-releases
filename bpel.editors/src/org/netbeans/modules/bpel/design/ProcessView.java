/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.bpel.design;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.decoration.Decoration;
import org.netbeans.modules.bpel.design.geometry.FBounds;
import org.netbeans.modules.bpel.design.geometry.FPoint;
import org.netbeans.modules.bpel.design.layout.LayoutManager;
import org.netbeans.modules.bpel.design.model.connections.Connection;
import org.netbeans.modules.bpel.design.model.connections.MessageConnection;
import org.netbeans.modules.bpel.design.model.elements.BorderElement;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.design.model.patterns.CompositePattern;
import org.netbeans.modules.bpel.design.model.patterns.PartnerLinksPattern;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;
import org.netbeans.modules.bpel.design.selection.PlaceHolder;

/**
 * @author Alexey
 */
public class ProcessView extends DiagramView implements 
        TriScrollPane.Thumbnailable 
{

    public ProcessView(DesignView designView) {
        super(designView);

        // vlv: print
        putClientProperty("print.printable", Boolean.TRUE); // NOI18N
        putClientProperty("print.name", getProcessName(designView.getBPELModel())); // NOI18N
        putClientProperty("print.order", new Integer(1)); // NOI18N
    }

    private String getProcessName(BpelModel model) {
      if (model == null) {
        return null;
      }
      Process process = model.getProcess();

      if (process == null) {
        return null;
      }
      return process.getName();
    }

    @Override
    /**
     * Ignores the content of PLs pattern;
     * 
     */
    protected void paintPattern(Graphics2D g2, Graphics2D g2bw, Pattern pattern, FBounds clipBounds, boolean printMode) {
        if (pattern instanceof PartnerLinksPattern) {
            return;
        }
        MessageConnection.resetRoutingInfo();
        super.paintPattern(g2, g2bw, pattern, clipBounds, printMode);
    }

    public FBounds getContentSize() {
        Pattern rp = getDesignView().getModel().getRootPattern();
        return (rp != null) ? rp.getBounds() : new FBounds(0,0);
    }

    @Override
    public Iterator<Pattern> getPatterns() {
        return new ListBuilder().getList();

    }
    
    public Insets getAutoscrollInsets() {
        Insets insets = super.getAutoscrollInsets();
        TriScrollPane scrollPane = getDesignView().getScrollPane();
        insets.left += scrollPane.getLeftPreferredWidth();
        insets.right += scrollPane.getRightPreferredWidth();
        return insets;
    }    

    class ListBuilder {

        private ArrayList<Pattern> list;

        public Iterator<Pattern> getList(){
            list = new ArrayList<Pattern>();
            buildList(getDesignView().getModel().getRootPattern());
            return list.iterator();
        }
        private void buildList(Pattern pattern) {
            if (pattern instanceof PartnerLinksPattern) {
                return;
            }

            list.add(pattern);

            if (pattern instanceof CompositePattern) {
                for (Pattern p : ((CompositePattern) pattern).getNestedPatterns()) {
                    buildList(p);
                }
            }

        }
    }

    @Override
    public void getPlaceholders(Pattern draggedPattern, List<PlaceHolder> placeHolders) {
        //EMPTY. This view does not provide any additional placeholders
    }

    @Override
    public VisualElement findElement(double x, double y) {
        Pattern root = getDesignView().getModel().getRootPattern();
        return findElement(root, x, y);
    }
    private VisualElement findElement(Pattern pattern, double x, double y){
        if (pattern instanceof PartnerLinksPattern){
            return null;
        }
        
        if (pattern instanceof CompositePattern){
            for(Pattern p : ((CompositePattern) pattern).getNestedPatterns()){
                VisualElement e = findElement(p, x, y);
                if (e != null){
                    return e;
                }
            }
        }
        return findElementInPattern(pattern, x, y);
    }

    public void paintThumbnail(Graphics g) {
        Pattern rootPattern = getDesignView().getModel().getRootPattern();
        
        if (rootPattern == null) return;
        
        Graphics2D g2 = GUtils.createGraphics(g);
        
        double zoom = getDesignView().getCorrectedZoom();
        
        Point p = convertDiagramToScreen(new FPoint(0, 0));
        g2.translate(p.x, p.y);
        g2.scale(zoom, zoom);
        
        Graphics2D g2bw = new BWGraphics2D(g2);
        
        Rectangle clipBounds = g2.getClipBounds();
        
        double exWidth = LayoutManager.HSPACING * zoom;
        double exHeight = LayoutManager.VSPACING * zoom;

        FBounds exClipBounds = new FBounds(
                clipBounds.x - exWidth,
                clipBounds.y - exHeight,
                clipBounds.width + 2 * exWidth,
                clipBounds.height + 2 * exHeight);
        
        paintPatternThumbnail(g2, g2bw, exClipBounds, rootPattern);
        paintPatternThumbnailConnections(g2, g2bw, rootPattern);
        g2.dispose();
        
        Graphics componentGraphics = g.create();
//        for (int i = getComponentCount() - 1; i >= 0; i--) {
//            Component c = getComponent(i);
//            if (c instanceof GlassPane) {
//                int tx = c.getX();
//                int ty = c.getY();
//                componentGraphics.translate(tx, ty);
//                ((GlassPane) c).paintThumbnail(componentGraphics);
//                componentGraphics.translate(-tx, -ty);
//            }
//        }
        componentGraphics.dispose();
    }
    
    private void paintPatternThumbnail(Graphics2D g2, Graphics2D g2bw, 
            FBounds clipBounds, Pattern pattern) {
        if (!pattern.getBounds().isIntersects(clipBounds)) {
            return;
        }
        
        if (pattern instanceof PartnerLinksPattern) {
            return;
        }
        
        Decoration decoration = getDesignView().getDecoration(pattern);
        
        if (pattern instanceof CompositePattern) {
            CompositePattern composite = (CompositePattern) pattern;
            
            BorderElement border = composite.getBorder();
            
            Graphics2D g = (decoration.hasDimmed()) ? g2bw : g2;
            
            if (border != null) {
                border.paintThumbnail(g);
            }
            
            for (VisualElement e : composite.getElements()) {
                e.paintThumbnail(g);
            }
            
            for (Pattern p : composite.getNestedPatterns()) {
                paintPatternThumbnail(g2, g2bw, clipBounds, p);
            }
            
            if (decoration.hasStroke()) {
                decoration.getStroke().paint(g2, composite.createSelection());
            }
        } else {
            Graphics2D g = (decoration.hasDimmed()) ? g2bw : g2;
            
            for (VisualElement e : pattern.getElements()) {
                e.paintThumbnail(g);
            }
            
            if (decoration.hasStroke()) {
                decoration.getStroke().paint(g2, pattern.createSelection());
            }
        }
    }
    

    private void paintPatternThumbnailConnections(Graphics2D g2, 
            Graphics2D g2bw, Pattern pattern) 
    {
        if (pattern == null) return;
        
        if (pattern instanceof CompositePattern) {
            CompositePattern composite = (CompositePattern) pattern;
            for (Pattern p : composite.getNestedPatterns()) {
                paintPatternThumbnailConnections(g2, g2bw, p);
            }
        }
        
        Graphics2D g = (getDesignView().getDecoration(pattern).hasDimmed()) ?
            g2bw 
            : g2;
        
        for (Connection c : pattern.getConnections()) {
            if (!(c instanceof MessageConnection)) {
                c.paintThumbnail(g);
            }
        }
    }
}
