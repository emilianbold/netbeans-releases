/*
 * PageFlowDeleteAction.java
 *
 * Created on April 12, 2007, 12:22 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.jsf.navigation.graph.actions;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import javax.swing.AbstractAction;
import org.netbeans.modules.web.jsf.navigation.NavigationCaseNode;
import org.netbeans.modules.web.jsf.navigation.PinNode;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowScene;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author joelle
 */
public class PageFlowDeleteAction extends AbstractAction{
    PageFlowScene scene;
    
    /** Creates a new instance of PageFlowDeleteAction 
     * @param scene 
     */
    public PageFlowDeleteAction(PageFlowScene scene) {
        this.scene = scene;
        
    }
    
    
    @Override
    public boolean isEnabled() {
        //Workaround: Temporarily Wrapping Collection because of Issue: 100127
        Set<? extends Object> selectedObjs = scene.getSelectedObjects();
        if (selectedObjs.size() == 0 ){
            return false;
        }
        
        for( Object selectedObj : selectedObjs ){
            /* HACK until PinNode is made a Node */
            if(!( selectedObj instanceof Node )  && !(selectedObj instanceof PinNode ) ){
                return false;
            }
        }
        
        return super.isEnabled();
    }
    
    public void actionPerformed(ActionEvent event) {
        
        Queue<Node> deleteNodesList = new LinkedList<Node>();
        //Workaround: Temporarily Wrapping Collection because of Issue: 100127
        Set<Object> selectedObjects = new HashSet<Object>(scene.getSelectedObjects());
        
        /*When deleteing only one item. */
        if (selectedObjects.size() == 1){
            Object myObj = selectedObjects.toArray()[0];
            if( myObj instanceof Node ) {
                deleteNodesList.add((Node)myObj);
                deleteNodes(deleteNodesList);
                return;
            }
        }
        
        /* When deleting multiple objects, make sure delete all the links first. */
        for( Object selectedObj : selectedObjects ){
            if( scene.isEdge(selectedObj) ){
                deleteNodesList.add((Node)selectedObj);
                //                    delete((Node)selectedObj);
            }
        }
        /* The deleted links should not be selected anymore */
        selectedObjects = new HashSet<Object>(scene.getSelectedObjects());
        for( Object selectedObj : selectedObjects ){
            if( selectedObj instanceof Node ) {
                deleteNodesList.add((Node)selectedObj);
                //                    delete((Node)selectedObj);
            }
        }
        deleteNodes(deleteNodesList);
        
    }
    
    //        public Queue<Node> myDeleteNodes;
    private void deleteNodes( Queue<Node> deleteNodes ){
        final Queue<Node> myDeleteNodes = deleteNodes;
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    //This should walk through in order.
                    for( Node deleteNode : myDeleteNodes ){
                        if( deleteNode.canDestroy() ){
                            
                            if( deleteNode instanceof NavigationCaseNode ){
                                updateSourcePins((NavigationCaseNode)deleteNode);
                            }
                            
                            
                            deleteNode.destroy();
                        }
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
    }
    
    private void updateSourcePins(NavigationCaseNode navCaseNode) {
        PinNode source = scene.getEdgeSource(navCaseNode);
        if( source != null && !source.isDefault()) {
            source.setFromOutcome(null);
        } 
        return;
    }
    
    
}
