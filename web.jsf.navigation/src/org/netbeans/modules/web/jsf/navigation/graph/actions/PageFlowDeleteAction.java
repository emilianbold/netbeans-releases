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
        Set<Object> selectedObjs = (Set<Object>) scene.getSelectedObjects();
        if (selectedObjs.size() == 0 ){
            return false;
        }
        
        for( Object selectedObj : selectedObjs ){
            if(!( selectedObj instanceof Node )){
                return false;
            }
        }
        
        return super.isEnabled();
    }
    
    public void actionPerformed(ActionEvent event) {
        
        //Workaround: Temporarily Wrapping Collection because of Issue: 100127
        Set<Object> selectedObjects = new HashSet<Object>(scene.getSelectedObjects());
        
        /*When deleteing only one item. */
        if (selectedObjects.size() == 1){
            Object myObj = selectedObjects.toArray()[0];
            if( myObj instanceof Node ) {
                delete((Node)myObj);
                return;
            }
        }
        
        //            Queue<Node> deleteNodesList  new LinkedList<Node>();
        Queue<Node> deleteNodesList = new LinkedList<Node>();
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
        delete(deleteNodesList);
        
    }
    
    //        public Queue<Node> myDeleteNodes;
    private void delete( Queue<Node> deleteNodes ){
        final Queue<Node> myDeleteNodes = deleteNodes;
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    //This should walk through in order.
                    for( Node deleteNode : myDeleteNodes ){
                        if( deleteNode.canDestroy() ){
                            deleteNode.destroy();
                        }
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
    }
    
    
    //        public Node myNode;
    private void delete( Node node ){
        final Node myNode = node;
        if ( node.canDestroy() ){
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    try {
                        myNode.destroy();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
            
        }
    }
    
    
}
