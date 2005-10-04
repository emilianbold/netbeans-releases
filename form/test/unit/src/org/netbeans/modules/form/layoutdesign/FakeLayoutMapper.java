/*
 * FakeLayoutMapper.java
 *
 * Created on 19 September 2005, 11:30
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.form.layoutdesign;
import java.awt.Dimension;
import java.awt.MenuComponent;
import java.awt.Rectangle;
import java.awt.Window;
import java.util.HashMap;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.VisualReplicator;

/**
 *
 * @author mg116726
 */
public class FakeLayoutMapper implements VisualMapper, LayoutConstants {

    private FormModel fm = null;
    private HashMap contInterior = null;
    private HashMap baselinePosition = null;
    private HashMap prefPaddingInParent = null;
    private HashMap prefPadding = null;
    private HashMap compBounds = null;
    private HashMap compMinSize = null;
    private HashMap compPrefSize = null;
    private HashMap hasExplicitPrefSize = null;
    
    private VisualReplicator replicator = null;
            
    public FakeLayoutMapper(FormModel fm, 
                            HashMap contInterior, 
                            HashMap baselinePosition, 
                            HashMap prefPaddingInParent,
                            HashMap compBounds,
                            HashMap compMinSize,
                            HashMap compPrefSize,
                            HashMap hasExplicitPrefSize,
                            HashMap prefPadding) {
        this.fm = fm;
        this.contInterior = contInterior;
        this.baselinePosition = baselinePosition;
        this.prefPaddingInParent = prefPaddingInParent;
        this.compBounds = compBounds;
        this.compMinSize = compMinSize;
        this.compPrefSize = compPrefSize;
        this.hasExplicitPrefSize = hasExplicitPrefSize;
        this.prefPadding = prefPadding;
        
        replicator = new VisualReplicator(
            null,
            new Class[] { Window.class,
                          java.applet.Applet.class,
                          MenuComponent.class },
            1 | 2 /* ==> VisualReplicator.ATTACH_FAKE_PEERS | VisualReplicator.DISABLE_FOCUSING*/);
        
    }
    
    // -------

    public Rectangle getComponentBounds(String componentId) {
        return (Rectangle) compBounds.get(componentId);
    }

    public Rectangle getContainerInterior(String componentId) {
        return (Rectangle) contInterior.get(componentId);
    }

    public Dimension getComponentMinimumSize(String componentId) {
        return (Dimension) compMinSize.get(componentId);
    }

    public Dimension getComponentPreferredSize(String componentId) {
        return (Dimension) compPrefSize.get(componentId);
    }

    public boolean hasExplicitPreferredSize(String componentId) {
        return ((Boolean) hasExplicitPrefSize.get(componentId)).booleanValue();
    }

    public int getBaselinePosition(String componentId, int width, int height) {
        String id = componentId + "-" + width + "-" + height; //NOI18N
        return ((Integer) baselinePosition.get(id)).intValue();
    }

    public int getPreferredPadding(String comp1Id,
                                   String comp2Id,
                                   int dimension,
                                   int comp2Alignment,
                                   int paddingType)
    {
        String id = comp1Id + "-" + comp2Id  + "-" + dimension + "-" + comp2Alignment + "-" + paddingType; //NOI18N
        return ((Integer) prefPadding.get(id)).intValue();
    }

    public int getPreferredPaddingInParent(String parentId,
                                           String compId,
                                           int dimension,
                                           int compAlignment)
    {
        String id = parentId + "-" + compId + "-" + dimension + "-" + compAlignment; //NOI18N
        return ((Integer) prefPaddingInParent.get(id)).intValue();
    }

    public boolean[] getComponentResizability(String compId, boolean[] resizability) {
        resizability[0] = resizability[1] = true;
        return resizability;
    }

    public void rebuildLayout(String contId) {
        System.out.println("FAKE!!!!!! rebuildlayout "); //NOI18N
//        replicator.updateContainerLayout((RADVisualContainer)getMetaComponent(contId));
//        replicator.getLayoutBuilder(contId).doLayout();
    }
    
}
