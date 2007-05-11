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
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author joelle
 */
public class FacesModelPropertyChangeListener implements PropertyChangeListener {
    public PageFlowController pfc;
    public PageFlowView view;
//    public boolean refactoringIsLikely = false;
    //    PageFlowUtilities pfUtil = PageFlowUtilities.getInstance();
    
    public FacesModelPropertyChangeListener( PageFlowController pfc ){
        this.pfc = pfc;
        view = pfc.getView();
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
        //        String oldManagedBeanInfo = null;
        //        String newManagedBeanInfo = null;
        //        boolean managedBeanClassModified = false;
        if( ev.getOldValue() == State.NOT_WELL_FORMED ){
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    view.removeUserMalFormedFacesConfig();  // Does clear graph take care of this?
                    pfc.setupGraph();
                }
            });
        } else if( ev.getPropertyName().equals("managed-bean-class") ) {
            /* Can I guarentee that Insync will call this and then managed-bean-name? */
            //            managedBeanClassModified = true;
            
        } else if ( ev.getPropertyName().equals("managed-bean-name") ) {
            
            //Use this to notice that refactoring may have happened.
            //            oldManagedBeanInfo = (String) ev.getOldValue();
            //            newManagedBeanInfo = (String) ev.getNewValue();
            
        } else if ( ev.getPropertyName() == "navigation-case") {
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
            
            final String oldName = FacesModelUtility.getViewIdFiltiered( (String) ev.getOldValue() );
            final String newName = FacesModelUtility.getViewIdFiltiered( (String) ev.getNewValue() );
           
            final Object source = ev.getSource();
            final NavigationCase navCase = (source instanceof NavigationCase ? (NavigationCase)source : null);
            
            /* This code is only need if refactor calls rename of file before renaming the faces-config.
            if ( managedBeanClassModified && oldManagedBeanInfo != null && newManagedBeanInfo != null ) {
                if( oldManagedBeanInfo.equals(oldName) && newManagedBeanInfo.equals(newName)){
                    refactoringIsLikely = true;
                }
            }
            managedBeanClassModified = false;
            oldManagedBeanInfo = null;
            newManagedBeanInfo = null;
             */
            
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    replaceFromViewIdToViewIdEventHandler(navCase, oldName, newName);
                    //                    replaceFromViewIdToViewIdEventHandler(oldName, newName, refactoringIsLikely);
                }
            });
        } else if ( ev.getPropertyName() == "from-outcome" ){
            final String oldName = (String) ev.getOldValue();
            final String newName = (String) ev.getNewValue();
            final NavigationCase navCase = (NavigationCase)ev.getSource();
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    replaveFromOutcomeEventHandler(navCase, oldName, newName);
                    //                    replaceFromViewIdToViewIdEventHandler(oldName, newName, refactoringIsLikely);
                }
            });
        } else {
            //                System.out.println("Did not catch this event.: " + ev.getPropertyName());
            setupGraphInAWTThread();
        }
    }
    private final void replaveFromOutcomeEventHandler( NavigationCase navCase, String oldName, String newName ){
        NavigationCaseEdge edge = pfc.getCase2Node(navCase);
        view.renameEdgeWidget(edge, newName, oldName);
        view.validateGraph();
    }
    
    //    private final void replaceFromViewIdToViewIdEventHandler(String oldName, String newName, boolean possibleRefactor) {
    private final void replaceFromViewIdToViewIdEventHandler(NavigationCase navCase, String oldName, String newName) {
        /* Going to have to do this another day. */
        Page oldPageNode = pfc.getPageName2Node(oldName);
        Page newPageNode = pfc.getPageName2Node(newName);
        boolean isNewPageLinked = false;
        if( newPageNode != null && view.getNodeEdges(newPageNode).size() > 0 ){
            /* This tells me that the new page already exists.*/
            isNewPageLinked = true;
        }
        /* The below code is only necessary if Refactor calls rename on page before it modifies the file.  This is not the case right now so this never really gets executed
        if( possibleRefactor && !isNewPageLinked && oldPageNode != null && newPageNode != null && newPageNode.isDataNode()){
            // This means that we should replace the new node back to the old because refactoring has likely occured
            Node node = newPageNode.getWrappedNode();
            if ( node != null ) {
                oldPageNode.replaceWrappedNode(node);
                view.removeNodeWithEdges(newPageNode);
                pfc.removePageName2Node(newPageNode, true); // Use this instead of replace because I want it to destroy the old node
                pfc.putPageName2Node(newName, oldPageNode);
                view.resetNodeWidget(oldPageNode, true);
                return;
            }
        } */
        //DO I really need this isNewPageLinked?
        if ( oldPageNode != null && !pfc.isPageInFacesConfig(oldName) && !isNewPageLinked ) {
            FileObject fileObj = pfc.getWebFolder().getFileObject(newName);
            if ( fileObj != null && pfc.containsWebFile(fileObj) ){
                try                 {
                    Node delegate = DataObject.find(fileObj).getNodeDelegate();
                    oldPageNode.replaceWrappedNode(delegate);
                    view.resetNodeWidget(oldPageNode, true); /*** JUST PUT TRUE HERE AS A HOLDER */
                    view.validateGraph();
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                pfc.changeToAbstractNode(oldPageNode, newName);
            }
        }else if ( oldPageNode == null && !pfc.isPageInFacesConfig(oldName)) {
            //This means that oldPage has already been removed.  Do nothing.
            
        }else if ( navCase != null && newPageNode == null && pfc.isPageInFacesConfig(oldName)) {
            NavigationCaseEdge newCaseEdge = null;
            NavigationCaseEdge oldCaseEdge = null;
            oldCaseEdge = pfc.getCase2Node(navCase);
            oldCaseEdge = pfc.removeCase2Node(navCase);
            newCaseEdge = new NavigationCaseEdge(view.getPageFlowController(), navCase);
            pfc.putCase2Node(navCase, newCaseEdge);
            navigationCaseEdgeEventHandler( newCaseEdge, oldCaseEdge);
//            view.createNode(pfc.getPageName2Node(oldName), newName, glyphs)
            //I am not sure if I want to do this yet?  Can I just re-use some of the navigationCaseEventHandler.
        } else {
            pfc.setupGraph();
        }
    }
    
    private final void navigationCaseEventHandler(NavigationCase myNewCase, NavigationCase myOldCase) {
        NavigationCaseEdge newCaseEdge = null;
        NavigationCaseEdge oldCaseEdge = null;
        if( myNewCase != null ){
            newCaseEdge = new NavigationCaseEdge(view.getPageFlowController(), myNewCase);
            pfc.putCase2Node(myNewCase, newCaseEdge);
//            pfc.createEdge(newCaseEdge);
        }
        if ( myOldCase != null ){
            oldCaseEdge = pfc.removeCase2Node(myOldCase);
//            if( oldCaseEdge != null ) {
//                view.removeEdge(oldCaseEdge);
//                
//                String toPage = oldCaseEdge.getToViewId();
//                if( toPage != null ) {
//                    Page pageNode = pfc.getPageName2Node(toPage);
//                    if( pageNode != null && !pfc.isPageInFacesConfig(toPage)){
//                        if( !pageNode.isDataNode() || pfc.isFacesConfigCurrentScope()){
//                            view.removeNodeWithEdges(pageNode);
//                            pfc.removePageName2Node(pageNode,true);
//                            view.validateGraph();
//                        }
//                    }
//                }
//            }
        }
        navigationCaseEdgeEventHandler( newCaseEdge, oldCaseEdge);
//        view.validateGraph();
    }

    private final void navigationCaseEdgeEventHandler( NavigationCaseEdge newCaseEdge, NavigationCaseEdge oldCaseEdge ){

        if( newCaseEdge != null) {
//            NavigationCaseEdge newCaseEdge = new NavigationCaseEdge(view.getPageFlowController(), newCase);
//            pfc.putCase2Node(newCase, newCaseEdge);//     case2Node.put(myNewCase, node);
            Page fromPage = pfc.getPageName2Node(newCaseEdge.getFromViewId());
            Page toPage = pfc.getPageName2Node(newCaseEdge.getToViewId());
            if( fromPage == null ){
                fromPage = pfc.createPageFlowNode(newCaseEdge.getFromViewId());                
                view.createNode(fromPage, null, null);
            }
            if( toPage == null ){
                toPage = pfc.createPageFlowNode(newCaseEdge.getToViewId());            
                view.createNode(toPage, null, null);
            }
            pfc.createEdge(newCaseEdge);
        }
        if ( oldCaseEdge != null ){
//            NavigationCaseEdge oldCaseEdge = pfc.removeCase2Node(myOldCase);
            if( oldCaseEdge != null ) {
                view.removeEdge(oldCaseEdge);
                
                String toPage = oldCaseEdge.getToViewId();
                if( toPage != null ) {
                    Page pageNode = pfc.getPageName2Node(toPage);
                    if( pageNode != null && !pfc.isPageInFacesConfig(toPage)){
                        if( !pageNode.isDataNode() || pfc.isFacesConfigCurrentScope()){
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
                Page pageNode = pfc.getPageName2Node(fromPage);
                if( pageNode != null && !pfc.isPageInFacesConfig(fromPage)){
                    if( !pageNode.isDataNode() || pfc.isFacesConfigCurrentScope()){
                        view.removeNodeWithEdges(pageNode);
                        pfc.removePageName2Node(pageNode, true);
                        view.validateGraph();
                    }
                }
            }
        }
        if( myNewRule != null ){
            pfc.putNavRule2String(myNewRule, FacesModelUtility.getFromViewIdFiltered(myNewRule));
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
