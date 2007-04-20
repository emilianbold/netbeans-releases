/*
 * FacesModelPropertyChangeListener.java
 *
 * Created on April 17, 2007, 6:47 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.jsf.navigation;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;
import org.netbeans.modules.xml.xam.Model.State;

/**
 *
 * @author joelle
 */
public class FacesModelPropertyChangeListener implements PropertyChangeListener {
    public PageFlowController pfc;
    public PageFlowView view;
    public FacesModelPropertyChangeListener( PageFlowController pfc ){
        this.pfc = pfc;
        view = pfc.getView();
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
        
        if( ev.getOldValue() == State.NOT_WELL_FORMED ){
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    view.removeUserMalFormedFacesConfig();  // Does clear graph take care of this?
                    pfc.setupGraph();
                }
            });
        }
        
        if ( ev.getPropertyName() == "navigation-case"){
            final NavigationCase myNewCase = (NavigationCase)ev.getNewValue();  //Should also check if the old one is null.
            final NavigationCase myOldCase = (NavigationCase)ev.getOldValue();
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    navigationCaseEventHandler(myNewCase, myOldCase);
                }
            });
            
        } else if (ev.getPropertyName() == "navigation-rule" ) {
            //You can actually do nothing.
            final NavigationRule myNewRule = (NavigationRule) ev.getNewValue();
            final NavigationRule myOldRule = (NavigationRule) ev.getOldValue();
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    navigationRuleEventHandler(myNewRule, myOldRule);
                }
            });
            
        } else if ( ev.getNewValue() == State.NOT_SYNCED ) {
            // Do nothing.
        } else if (ev.getNewValue() == State.NOT_WELL_FORMED ){
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    view.clearGraph();
                    view.warnUserMalFormedFacesConfig();
                }
            });
        } else if (ev.getPropertyName() == "textContent" ){
            setupGraphInAWTThread();
        } else if ( ev.getPropertyName() == "from-view-id"  || ev.getPropertyName() == "to-view-id"){
            /* Going to have to do this another day. */
            //                String oldName = (String) ev.getOldValue();
            //                String newName = (String) ev.getNewValue();
            //                PageFlowNode oldPageNode = pageName2Node.get(oldName);
            //                PageFlowNode newPageNode = pageName2Node.get(oldName);
            //                boolean isNewPageLinked = false;
            //                if( newPageNode != null && view.getNodeEdges(newPageNode).size() > 0 ){
            //                    isNewPageLinked = true;
            //                }
            //
            //                if ( oldPageNode != null && !isPageInFacesConfig(oldName) && !isNewPageLinked ) {
            //                    FileObject fileObj = getWebFolder().getFileObject(newName);
            //                    if ( fileObj != null && webFiles.contains(fileObj) ){
            //                        try                 {
            //                            Node delegate = DataObject.find(fileObj).getNodeDelegate();
            //                            oldPageNode.replaceWrappedNode(createPageFlowNode(delegate));
            //                            view.resetNodeWidget(oldPageNode);
            //                            view.validateGraph();
            //                        } catch (DataObjectNotFoundException ex) {
            //                            Exceptions.printStackTrace(ex);
            //                        }
            //                    } else {
            //                        changeToAbstractNode(oldPageNode, newName);
            //                    }
            //                } else {
            setupGraphInAWTThread();
            //                }
        } else {
            //                System.out.println("Did not catch this event.: " + ev.getPropertyName());
            setupGraphInAWTThread();
        }
    }
    
    private final void navigationCaseEventHandler(NavigationCase myNewCase, NavigationCase myOldCase) {
        
        if( myNewCase != null ){
            NavigationCaseNode node = new NavigationCaseNode(view.getPageFlowController(), myNewCase);
            pfc.putCase2Node(myNewCase, node);//     case2Node.put(myNewCase, node);
            pfc.createEdge(node);
        }
        if ( myOldCase != null ){
            NavigationCaseNode caseNode = pfc.removeCase2Node(myOldCase);
            if( caseNode != null ) {
                view.removeEdge(caseNode);
                
                String toPage = caseNode.getToViewId();
                if( toPage != null ) {
                    PageFlowNode pageNode = pfc.getPageName2Node(toPage);
                    if( pageNode != null && !pfc.isPageInFacesConfig(toPage)){
                        if( !pageNode.isDataNode() || PageFlowUtilities.getInstance().getCurrentScope() == PageFlowUtilities.LBL_SCOPE_FACESCONFIG){
                            view.removeNodeWithEdges(pageNode);
                            pfc.removePageName2Node(pageNode,true);
                            view.validateGraph();
                        }
                    }
                }
            }
        }
        view.validateGraph();
    }
    
    private final void navigationRuleEventHandler(NavigationRule myNewRule, NavigationRule myOldRule) {
        //This has side effects in PageFlowNode destroy.
        //Because it does not consistantly work, I can't account for reactions.
        if( myOldRule != null ){
            String fromPage = pfc.removeNavRule2String(myOldRule);
            
            if( fromPage != null ){
                PageFlowNode pageNode = pfc.getPageName2Node(fromPage);
                if( pageNode != null && !pfc.isPageInFacesConfig(fromPage)){
                    if( !pageNode.isDataNode() || PageFlowUtilities.getInstance().getCurrentScope() == PageFlowUtilities.LBL_SCOPE_FACESCONFIG){
                        view.removeNodeWithEdges(pageNode);
                        pfc.removePageName2Node(pageNode, true);
                        view.validateGraph();
                        //                                node.destroy(); //only okay because it is an abstract node.
                    }
                }
            }
        }
        if( myNewRule != null ){
            pfc.putNavRule2String(myNewRule, myNewRule.getFromViewId());
        }
    }
    
    private final void setupGraphInAWTThread() {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                pfc.setupGraph();
            }
        });
    }
}
