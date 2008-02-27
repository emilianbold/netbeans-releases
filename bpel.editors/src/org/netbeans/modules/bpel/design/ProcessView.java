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
import org.netbeans.modules.bpel.design.model.connections.MessageConnection;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.design.model.patterns.CompositePattern;
import org.netbeans.modules.bpel.design.model.patterns.PartnerLinksPattern;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;
import org.netbeans.modules.bpel.design.selection.PlaceHolder;
import org.netbeans.modules.bpel.core.helper.api.CoreUtil;

/**
 *
 * @author Alexey
 */
public class ProcessView extends DiagramView {

    public ProcessView(DesignView designView) {
        super(designView);

        // vlv: print
        putClientProperty(java.awt.print.Printable.class, CoreUtil.getProcessName(designView.getBPELModel()));
        putClientProperty(java.lang.Integer.class, new Integer(1));
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
}
