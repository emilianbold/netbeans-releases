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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
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
    
    private static final Logger LOGGER = Logger.getLogger("org.netbeans.modules.web.jsf.navigation");
    //    public boolean refactoringIsLikely = false;
    //    PageFlowUtilities pfUtil = PageFlowUtilities.getInstance();
    
    public FacesModelPropertyChangeListener( PageFlowController pfc ){
        this.pfc = pfc;
        view = pfc.getView();
        LOGGER.setLevel(Level.ALL);
    }
    
    public void propertyChange(final PropertyChangeEvent ev) {
        //        String oldManagedBeanInfo = null;
        //        String newManagedBeanInfo = null;
        //        boolean managedBeanClassModified = false;
        if( ev.getOldValue() == State.NOT_WELL_FORMED ){
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    view.removeUserMalFormedFacesConfig();  // Does clear graph take care of this?
                    setupGraph(ev);
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
            setupGraphInAWTThread(ev);
        } else if ( ev.getPropertyName() == "from-view-id"  || ev.getPropertyName() == "to-view-id"){
            
            final String oldName = FacesModelUtility.getViewIdFiltiered( (String) ev.getOldValue() );
            final String newName = FacesModelUtility.getViewIdFiltiered( (String) ev.getNewValue() );
            
            final Object source = ev.getSource();
            
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
                    replaceFromViewIdToViewIdEventHandler(ev, source, oldName, newName);
                    //                    replaceFromViewIdToViewIdEventHandler(oldName, newName, refactoringIsLikely);
                }
            });
        } else if ( ev.getPropertyName() == "from-outcome" ){
            final String oldName = (String) ev.getOldValue();
            final String newName = (String) ev.getNewValue();
            final NavigationCase navCase = (NavigationCase)ev.getSource();
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    replaceFromOutcomeEventHandler(navCase, oldName, newName);
                    //                    replaceFromViewIdToViewIdEventHandler(oldName, newName, refactoringIsLikely);
                }
            });
        } else {
            //                System.out.println("Did not catch this event.: " + ev.getPropertyName());
            setupGraphInAWTThread(ev);
        }
    }
    private final void replaceFromOutcomeEventHandler( NavigationCase navCase, String oldName, String newName ){
        NavigationCaseEdge edge = pfc.getCase2Node(navCase);
        view.renameEdgeWidget(edge, newName, oldName);
        view.validateGraph();
    }
    
    //    private final void replaceFromViewIdToViewIdEventHandler(String oldName, String newName, boolean possibleRefactor) {
    private final void replaceFromViewIdToViewIdEventHandler(PropertyChangeEvent ev, Object source, String oldName, String newName) {
        
        LOGGER.entering("\n\nFacesModelPropertyChangeListener", "replaceFromViewIdToViewIdEventHandler");
        final NavigationCase navCase = (source instanceof NavigationCase ? (NavigationCase)source : null);
        final NavigationRule navRule = (source instanceof NavigationRule ? (NavigationRule)source : null);
        
        /* Going to have to do this another day. */
        Page oldPageNode = pfc.getPageName2Node(oldName);
        Page newPageNode = pfc.getPageName2Node(newName);
        LOGGER.finest("OldPageNode: " + oldPageNode + "\n" +
                "NewPageNode: " + newPageNode + "\n");
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
        if ( oldPageNode != null && !pfc.isPageInFacesConfig(oldName) && !isNewPageLinked ) {
            LOGGER.finest("CASE 1: OldPage is not null and does not exist in the facesconfig anymore.  This is the firsttime the new page is linked.");
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
            LOGGER.finest("CASE 2: OldPage was removed before.");
        }else if ( navCase != null && pfc.isPageInFacesConfig(oldName)) {
            LOGGER.finest("CASE 3: NavCase is not null");
            NavigationCaseEdge newCaseEdge = null;
            NavigationCaseEdge oldCaseEdge = null;
            oldCaseEdge = pfc.removeCase2Node(navCase);
            newCaseEdge = new NavigationCaseEdge(view.getPageFlowController(), navCase);
            pfc.putCase2Node(navCase, newCaseEdge);
            navigationCaseEdgeEventHandler( newCaseEdge, oldCaseEdge );
            //            if ( !pfc.isPageInFacesConfig(oldName) ){
            //                LOGGER.finest("CASE 3b: OldPage no longer exists in faces config.");
            //                view.removeNodeWithEdges(oldPageNode);
            //                pfc.removePageName2Node(oldPageNode, true);
            //                view.validateGraph();
            //            }
        } else if ( navRule != null && pfc.isPageInFacesConfig(oldName) ) {
            LOGGER.finest("CASE 4: NavRule is not null.");
            List<NavigationCase> navCases = navRule.getNavigationCases();
            pfc.putNavRule2String(navRule, FacesModelUtility.getViewIdFiltiered(newName));
            for( NavigationCase thisNavCase : navCases ){
                LOGGER.finest("CASE 4: Redrawing NavRules Case.");
                NavigationCaseEdge newCaseEdge = null;
                NavigationCaseEdge oldCaseEdge = null;
                //                oldCaseEdge = pfc.getCase2Node(thisNavCase);
                oldCaseEdge = pfc.removeCase2Node(thisNavCase);
                newCaseEdge = new NavigationCaseEdge(view.getPageFlowController(), thisNavCase);
                pfc.putCase2Node(navCase, newCaseEdge);
                navigationCaseEdgeEventHandler( newCaseEdge, oldCaseEdge);
            }
            //            if ( !pfc.isPageInFacesConfig(oldName) ){
            //                LOGGER.finest("CASE 4b: OldPage no longer exists in faces config.");
            //                view.removeNodeWithEdges(oldPageNode);
            //                pfc.removePageName2Node(oldPageNode, true);
            //                view.validateGraph();
            //            }
        }else {
            LOGGER.finest("CASE 5: Setup Graph");
            setupGraph(ev);
        }
        LOGGER.exiting("FacesModelPropertyChangeListener", "replaceFromViewIdToViewIdEventHandler");
    }
    
    private void setupGraph(PropertyChangeEvent ev) {
        LOGGER.fine("\n\nRe-setting Page Flow Editor because of change in faces config xml file.\n" +
                "Source Class:  org.netbeans.modules.web.jsf.navigation.FacesModelPropertyChangeListener\n" +
                "Method Name: setupGraph(PropertyChangeEvent ev)\n" +
                "Event: " + ev + "\n "+
                "PropertyName:" + ev.getPropertyName() + "\n "+
                "New Value: " + ev.getNewValue() + "\n "+
                "Old Value: " + ev.getOldValue() + "\n "+
                "Source: " + ev.getSource());
        
        LogRecord record = new LogRecord(Level.FINE, "Faces Config Change Re-Setting Graph");
        record.setSourceClassName("org.netbeans.modules.web.jsf.navigation.FacesModelPropertyChangeListener");
        record.setSourceMethodName("setupGraph(PropertyChangeEvent)");
        record.setParameters(new Object[] {ev});
        LOGGER.log(record);
        pfc.setupGraph();
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
    
    private final void setupGraphInAWTThread(final PropertyChangeEvent ev) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                setupGraph(ev);
            }
        });
    }
}
