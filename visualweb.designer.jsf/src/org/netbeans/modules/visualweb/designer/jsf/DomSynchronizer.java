/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.designer.jsf;

import org.netbeans.modules.visualweb.api.designer.markup.MarkupService;
import org.netbeans.modules.visualweb.insync.CustomizerDisplayer;
import org.netbeans.modules.visualweb.insync.InSyncServiceProvider;
import org.netbeans.modules.visualweb.insync.faces.FacesPageUnit;
import org.netbeans.modules.visualweb.insync.markup.MarkupUnit;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import javax.faces.component.UIComponent;
import javax.swing.SwingUtilities;
import org.apache.xerces.dom.events.MutationEventImpl;
import org.openide.filesystems.FileObject;

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventTarget;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignEvent;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.Position;
import com.sun.rave.designtime.event.DesignContextListener;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;
import org.openide.ErrorManager;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;

/**
 * XXX Moved from designer.
 * <p>
 * This class is responsible for updating the HTML DOM when changes in the master JSP DOM, or
 * associated properties in Java files or JavaScript event occur.
 * </p>
 *
 * @author Tor Norbye
 */
class DomSynchronizer implements /*DesignContextListener,*/ Runnable, org.w3c.dom.events.EventListener,
FacesDndSupport.UpdateSuspender {

    static {
        // XXX Very suspicious, why it is not more robust?
        CustomizerDisplayer.setBatchListener(new BeanModifyListener());
    }


    private static final boolean DEBUG = false;
    private static final int TYPE_NONE = 0;
    private static final int TYPE_INSERT = 1;
    private static final int TYPE_CHANGE = 2;
    private static final int TYPE_DELETE = 3;
    private static final int TYPE_REFRESH = 4;

    /** The currently planned bean to be changed, inserted or deleted (see pendingEventType) */
    private MarkupDesignBean pendingBean;
//    private WebForm webform;
    private JsfForm jsfForm;
    private org.w3c.dom.Document currentDOM;
    private Node deleteParent;
//    private DesignContext context;

    /** If true we're suspending all updates until setUpdatesSuspended is called to turn it off */
    private boolean suspendUpdates;

    /**
     * If true there's a timeout pending - we've enqueued a Runnable on the Swing event dispatch
     * thread
     */
    private boolean timeOutPending;

    /** If not TYPE_NONE, the type of pending update we're waiting for */
    private int pendingEventType = TYPE_NONE;

    /**
     * Create a new DomSynchronizer for the given webform
     */
    public DomSynchronizer(JsfForm jsfForm) {
        this.jsfForm = jsfForm;

        currentDOM = jsfForm.getJspDom();

        if (currentDOM == null) {
//            jsfForm.getFacesModel().sync();
            jsfForm.syncModel();
            currentDOM = jsfForm.getJspDom();

                // XXX What was this good for?
//                if (currentDOM == null) {
//                    currentDOM = MarkupUnit.createEmptyDocument(true);
//                }
        }

        if (currentDOM != null) {
            registerDomListeners();
        }
    }

//    /**
//     * Destroy this synchronizer - detach from listeners etc. when the object
//     * is no longer to be used
//     */
//    public void destroy() {
//        if (currentDOM != null) {
//            unregisterDomListeners();
//        }
//
////        detachContext();
//        jsfForm.detachContext();
//    }

//    /**
//     * Detach this synchronizer from the current DesignContext, if any
//     */
//    public void detachContext() {
//        if (context != null) {
//            context.removeDesignContextListener(this);
//        }
//
//        context = null;
//    }
//
//    /**
//     * Attach the synchronizer to the given DesignContext.
//     * The reason this is done "lazily", not part of the constructor,
//     * is that if we try to access a DesignContext after startup
//     * and the context has an error, the FacesModel will provide a null
//     * DesignContext. We don't obtain it until there has been a successful
//     * sync.
//     */
//    public void attachContext(DesignContext context) {
//        if (this.context == context) {
//            return;
//        }
//
//        detachContext();
//        this.context = context;
//
//        if (context != null) {
//            context.addDesignContextListener(this);
//        }
//    }

    // ----------- Implements DesignContextListener ---------------------------
    public void instanceNameChanged(DesignBean designBean, String oldInstanceName) {
    }

    /**
     * {@inheritDoc}
     */
    public void beanChanged(DesignBean designBean) {
        // TODO We need to find furthest ancestor that renders its own children.
        // It may be replicating this changed bean in multiple places, so needs
        // to be consulted.
        if (!(designBean instanceof MarkupDesignBean)) {
            // Could be something like a rowset or select items list.
            // We can't know visual beans are affected by this (at least we can't yet;
            // perhaps when we do a databinding view we'll know more about component
            // connections via value binding etc.) so we need to do a global re-render
            // Special case: when batch updates are suspended we know we can ignore
            // non markup beans are modified. Otherwise, dropping a listbox would
            // also have a select items object added and modified which would result
            // in a refresh if we didn't do this. (Similarly for DataTable; the non-markup
            // TableDataModel would also force a refresh). But in both cases we know
            // that the model itself is associated with the suspended/batched markup bean.
            if (suspendUpdates) {
                requestRefresh();
            }

            return;
        }

        // Insert new version of component
        MarkupDesignBean bean = (MarkupDesignBean)designBean;
        bean = getRenderTarget(bean);
        requestChange(bean);
    }

    public void beanCreated(DesignBean designBean) {
        // TODO We need to find furthest ancestor that renders its own children.
        // It may be replicating this changed bean in multiple places, so needs
        // to be consulted.
        if (!(designBean instanceof MarkupDesignBean)) {
            // We only care about markup beans when beans
            // are created (the same is not true for beanChanged
            // since an existing non-markup DesignBean can be
            // bound to a visual bean in some way and can affect
            // rendering - for example, the items property of
            // a SelectItems non-markup bean
            return;
        }

        MarkupDesignBean bean = (MarkupDesignBean)designBean;
        bean = getRenderTarget(bean);

        // Instead of inserting a new component directly we're re-rendering
        // a parent - this is a change, not an insert so we must delete the
        // old markup
        if (bean != designBean) {
            requestChange(bean);

            return;
        } else {
            requestInsert(bean);
        }
    }

    public void propertyChanged(DesignProperty prop, Object oldValue) {
        DesignBean designBean = prop.getDesignBean();

        if (!(designBean instanceof MarkupDesignBean)) {
            // Could be something like a rowset or select items list.
            // We can't know visual beans are affected by this (at least we can't yet;
            // perhaps when we do a databinding view we'll know more about component
            // connections via value binding etc.) so we need to do a global re-render
            requestRefresh();

            return;
        }

        MarkupDesignBean bean = (MarkupDesignBean)designBean;

        if (bean.getElement() == null) {
            // No element: the DesignBean does not correspond to a visual
            // component, or it might be one we're inserting; insync seems
            // to set some properties on these before actually inserting it
            // into the view hiearchy; we can safely ignore these requests
            // since the eventual DOM modification will cause a refresh.
            return;
        }

        bean = getRenderTarget(bean);
        requestChange(bean);
    }

    public void beanDeleted(DesignBean designBean) {
        if (!(designBean instanceof MarkupDesignBean)) {
            // Bean deletion of non-markup beans should have no effect on the visible
            // beans since if they are bound to this non-markup bean in some way,
            // insync should go and remove that relationship, and that will show up as
            // a property change on the markup bean which we will handle
            return;
        }

        MarkupDesignBean bean = (MarkupDesignBean)designBean;
        // XXX FIXME This can't work, the bean is supposed to be already removed from the tree.
        // How come it was working before and based on what hack? (see getRenderTarget).
        bean = getRenderTarget(bean);

        if (bean != designBean) {
            // If the bean to be deleted is inside a parent that must be
            // re-rendered, we should -change- the ancestor, not delete it.
            requestChange(bean);
        } else {
            requestDelete(bean);
        }
    }

    public void contextChanged(DesignContext context) {
        // The entire HTML needs to be replaced
        requestRefresh();
    }

    public void beanMoved(DesignBean designBean, DesignBean oldParent, Position pos) {
        if (!(designBean instanceof MarkupDesignBean)) {
            // Position on non-markup beans never matter to the HTML rendered
            return;
        } else if (!(oldParent instanceof MarkupDesignBean)) {
            // This should never be possible - and we should make sure this is
            // prevented in the UI to move beans around in e.g. the app outline and
            // the design surface
            return;
        }

        MarkupDesignBean bean = (MarkupDesignBean)designBean;
        bean = getRenderTarget(bean);

        if (bean == null) {
            requestRefresh();
        } else if (isBelow(oldParent, bean)) {
            // We've moving the bean from one position to another inside
            // the same encodes-children bean that we need to render
            // Nothing else to do
        } else if (isBelow(bean, oldParent)) {
            bean = (MarkupDesignBean)oldParent;
        } else {
            bean = (MarkupDesignBean)getCommonAncestor(bean, oldParent);
        }

        requestChange(bean);
    }

    public void contextActivated(DesignContext context) {
        // No-op: No impact on the rendered HTML DOM
    }

    public void contextDeactivated(DesignContext context) {
        // No-op: No impact on the rendered HTML DOM
    }

    public void eventChanged(DesignEvent event) {
        // No-op: No impact on the rendered HTML DOM
    }

    public void beanContextActivated(DesignBean designBean) {
        // No-op: No impact on the rendered HTML DOM
    }

    public void beanContextDeactivated(DesignBean designBean) {
        // No-op: No impact on the rendered HTML DOM
    }

    // ------- Coalesce Events --------------------------------
    // Queue up multiple simultaneous requests by tracking the "top level" bean that must
    // be inserted or replaced.
    // This is necessary because we often get "bursts" of activity; for example, when a component
    // is dragged from a gridpanel to a form, we have a deletion under the grid panel,
    // and addition under the form, and possibly multiple "set style property" calls.
    // We keep a list of changed beans and new requests are checked
    // for siblingness. Clients can also send hints to this code.

    /**
     * <p>
     * Add the ability to turn off updates for a while. When the suspend parameter is true, suspend
     * all updates to the DOM until the method is called again turning off the suspend.
     * </p>
     *
     * <p>
     * <b>NOTE:</b>: While updates are suspended, modifying any non-markup beans will NOT cause a
     * global refresh (which is usually the case since we don't know how a non-markup bean will
     * markup beans since there's no association map). When updates are suspended we're assuming
     * that any non-markup beans we are notified of are associated with the markup bean whose
     * updates are suspended.
     * </p>
     *
     * <p>
     * <b>NOTE:</b>: Calls to the batch facility CANNOT be nested!
     * </p>
     */
    public void setUpdatesSuspended(MarkupDesignBean bean, boolean suspend) {
        if (suspend) {
            assert !suspendUpdates;
            suspendUpdates = true;
        } else {
            suspendUpdates = false;
            bean = getRenderTarget(bean);
            requestChange(bean);
        }
    }

    /**
     * Dispatch the right type of event to the update manager only on the dispatch thread.
     * This is necessary because I don't do locking on the request variables like pendingEventType
     */
    private void requestUpdate(final int type, final MarkupDesignBean bean) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        requestUpdate(type, bean);
                    }
                });

            return;
        }

        switch (type) {
        case TYPE_INSERT:
            requestInsert(bean);

            break;

        case TYPE_DELETE:
            requestDelete(bean);

            break;

        case TYPE_CHANGE:
            requestChange(bean);

            break;

        case TYPE_REFRESH:
            requestRefresh();

            break;

        default:
            assert false;
        }
    }

//    /**
//     * Return true iff a refresh is pending
//     */
//    public boolean isRefreshPending() {
//        return pendingEventType == TYPE_REFRESH;
//    }

    /**
     * Return true iff any type of change is pending
     */
    public boolean isUpdatePending() {
        return pendingEventType != TYPE_NONE;
    }

    /**
     * Schedule a request to refresh the whole HTML DOM - this needs to be done after a synch() for
     * example. This will coalesce existing events if necessary.
     */
    public void requestRefresh() {
        if (!SwingUtilities.isEventDispatchThread()) {
            requestUpdate(TYPE_REFRESH, null);

            return;
        }

        if (!timeOutPending) { // XXX what about pending insert?
            pendingBean = null;
            pendingEventType = TYPE_REFRESH;
            timeOutPending = true;
            SwingUtilities.invokeLater(this);
        } else {
            // A refresh supercedes all other types of events since if we're going
            // to do a global refresh we don't need to worry about individual inserts
            // or changes - their new content will be rendered by the global recompute
            pendingBean = null;
            pendingEventType = TYPE_REFRESH;
        }
    }

    /**
     * <p>
     * Plan to change the given bean. This schedules a request to change this bean, unless one is
     * already pending; if it is, and the pending request includes the given bean position do
     * nothing, if it does not but this request would cover the pending one, change the request,
     * and if they are independent, leave both requests in place.
     * </p>
     *
     * <p>
     * <b>NOTE:</b> This method must be run on the event dispatch thread!
     * </p>
     */
    public void requestChange(MarkupDesignBean bean) {
        if (bean == null) {
            requestRefresh();

            return;
        }

        if (!SwingUtilities.isEventDispatchThread()) {
            requestUpdate(TYPE_CHANGE, bean);

            return;
        }

        if (!timeOutPending) {
            pendingBean = bean;
            pendingEventType = TYPE_CHANGE;
            timeOutPending = true;
            adjustRequestIfRoot();
            SwingUtilities.invokeLater(this);
        } else {
            if ((pendingEventType == TYPE_REFRESH) || (bean == pendingBean) ||
                    isBelow(bean, pendingBean)) {
                // The pending request will already take care of this bean, either because
                // it's going to render this or an ancestor node, or do a global refresh,
                // so we have nothing to worry about
                // In particular, don't change the type to TYPE_CHANGE even if bean==pendingBean;
                // if we have a pending insert then we still haven't inserted it so it's not
                // a change it's an insert; we'll just be inserting data that is now more
                // up to date!
                return;
            } else if (isBelow(pendingBean, bean)) {
                // We've now changed an anscestor of the already scheduled bean -
                // so just change the ancestor and the bean will be fine
                pendingBean = bean;
                pendingEventType = TYPE_CHANGE;
                adjustRequestIfRoot();
            } else {
                // They're in different parts of the tree. For now, just
                // re-render (change) their common ancestor. Later I can try to be smart and
                // look at their distances and perhaps just re-render each child -
                // this will be an advantage if you're just tweaking a couple of
                // leaves in a large tree. But it means I can't have a single
                // pendingBean field anymore, I need to maintain a list of pending nodes.
                pendingEventType = TYPE_CHANGE;
                pendingBean = (MarkupDesignBean)getCommonAncestor(bean, pendingBean);
                assert pendingBean != null;
                adjustRequestIfRoot();
            }
        }
    }

    /**
     * Check if the computed bean is at root level and if so change the
     * current request into a refresh
     */
    private void adjustRequestIfRoot() {
        // If the common ancestor has moved up to a fragment or document
        // root we need to refresh globally
        // refresh
//        RaveElement e = (RaveElement)pendingBean.getElement();
//        if (e.getRendered() != null) {
//            e = (RaveElement)e.getRendered();
//        }
        Element e = pendingBean.getElement();
        Element rendered = MarkupService.getRenderedElementForElement(e);
        if (rendered != null) {
            e = rendered;
        }

        if ((e == null) || (e.getParentNode() == null) ||
                (e.getParentNode().getNodeType() != Node.ELEMENT_NODE) ||
                e.getTagName().equals(HtmlTag.BODY.name)) {
            // We need to refresh the whole document
            pendingBean = null;
            pendingEventType = TYPE_REFRESH;
        }
    }

    /**
     * <p>
     * Plan to insert the given bean. This schedules a request to insert this bean, unless this
     * needs to be coalesced with an existing pending event.
     * </p>
     *
     * <p>
     * <b>NOTE:</b> This method must be run on the event dispatch thread!
     * </p>
     */
    public void requestInsert(MarkupDesignBean bean) {
        if (!SwingUtilities.isEventDispatchThread()) {
            requestUpdate(TYPE_INSERT, bean);

            return;
        }

        if (!timeOutPending) {
            pendingBean = bean;
            pendingEventType = TYPE_INSERT;
            timeOutPending = true;
            adjustRequestIfRoot();
            SwingUtilities.invokeLater(this);
        } else {
            if ((pendingEventType == TYPE_REFRESH) || (bean == pendingBean) || // is bean==pendingBean even possible?
                    isBelow(bean, pendingBean)) {
                // The pending request will already take care of this bean, either because
                // it's going to render this or an ancestor node, or do a global refresh,
                // so we have nothing to worry about
                return;

                /* This should not be possible -- you can't insert a bean
                   where we already know its descendants!
                   } else if (isBelow(pendingBean, bean)) {
                 */
            } else {
                // They're in different parts of the tree. For now, just
                // re-render (change) their common ancestor. Later I can try to be smart and
                // look at their distances and perhaps just re-render each child -
                // this will be an advantage if you're just tweaking a couple of
                // leaves in a large tree. But it means I can't have a single
                // pendingBean field anymore, I need to maintain a list of pending nodes.
                pendingBean = (MarkupDesignBean)getCommonAncestor(bean, pendingBean);
                pendingEventType = TYPE_CHANGE;
                assert pendingBean != null;
                adjustRequestIfRoot();
            }
        }
    }

    /**
     * <p>
     * Plan to remove the given bean. This schedules a request to remove this bean.
     * </p>
     *
     * <p>
     * <b>NOTE:</b> This method must be run on the event dispatch thread!
     * </p>
     */
    public void requestDelete(MarkupDesignBean bean) {
        if (!SwingUtilities.isEventDispatchThread()) {
            requestUpdate(TYPE_DELETE, bean);

            return;
        }

        if (!timeOutPending) {
            pendingBean = bean;
            pendingEventType = TYPE_DELETE;
            timeOutPending = true;
            adjustRequestIfRoot();
            SwingUtilities.invokeLater(this);
        } else {
            if (pendingEventType == TYPE_REFRESH) {
                return;
            } else if (bean == pendingBean) {
                pendingEventType = TYPE_DELETE; // no need to change something we're about to remove!

                return;
            } else if (isBelow(bean, pendingBean)) {
                // Will be handled by higher-up bean!
                return;
            } else if (isBelow(pendingBean, bean)) {
                pendingEventType = TYPE_DELETE;
                pendingBean = bean;
                adjustRequestIfRoot();
            } else {
                // They're in different parts of the tree. For now, just
                // re-render (change) their common ancestor. Later I can try to be smart and
                // look at their distances and perhaps just re-render each child -
                // this will be an advantage if you're just tweaking a couple of
                // leaves in a large tree. But it means I can't have a single
                // pendingBean field anymore, I need to maintain a list of pending nodes.
                pendingBean = (MarkupDesignBean)getCommonAncestor(bean, pendingBean);
                pendingEventType = TYPE_CHANGE;
                assert pendingBean != null;
                adjustRequestIfRoot();
            }
        }
    }

    /**
     * <p>
     * The given bean has either had text inserted as a child, or has had its existing text
     * changed.  Either way the text needs to be reflown. This should be called when we listen for
     * the JSP DOM and see a change in nodes of the types Node.TEXT_NODE,  Node.CDATA_SECTION_NODE
     * or Node.ENTITY_REFERENCE_NODE.
     * </p>
     *
     * @param bean The bean whose text child has been modified
     */
    public void requestTextUpdate(MarkupDesignBean bean) {
        beanChanged(bean);
    }

    /**
     * Process pending changes
     */
    public void run() {
        timeOutPending = false;
        processUpdates();
    }

    /**
     * Process all the changes that have been queued up
     */
    private void processUpdates() { // XXX Why is this called when we're editing the JSP doc?

        if ((pendingEventType == TYPE_NONE) || suspendUpdates) {
            return;
        }

        MarkupDesignBean bean = pendingBean;
        pendingBean = null;

        int type = pendingEventType;
        pendingEventType = TYPE_NONE;

        if (DEBUG) {
//            DocumentFragment html = jsfForm.getDomProvider().getHtmlDocumentFragment();
            DocumentFragment html = jsfForm.getHtmlDomFragment();

            if (html != null) {
                System.out.println("\nBefore delayed updates to bean " + bean + " the html is\n" +
                    InSyncServiceProvider.get().getHtmlStream(html));
            }
        }

        if (type == TYPE_CHANGE) {
            // Delete old version of component
//            Node previouslyRendered = ((RaveElement)bean.getElement()).getRendered();
            Node previouslyRendered = MarkupService.getRenderedElementForElement(bean.getElement());

//            if (!processDelete(bean)) {
//                processRefresh();
////                webform.getPane().getPaneUI().modelChanged();
//                jsfForm.modelChanged();
//
//                return;
//            }
//
//            // Insert new version of component
//            if (!processInsert(bean)) {
//                processRefresh();
////                webform.getPane().getPaneUI().modelChanged();
//                jsfForm.modelChanged();
//
//                return;
//            }
            List<Element> changedElements = new ArrayList<Element>();
            if (!processUpdate(bean, changedElements)) {
                processRefresh();
                jsfForm.modelChanged();
                return;
            }

            deleteParent = null;

//            Node rendered = ((RaveElement)bean.getElement()).getRendered();
            Node rendered = MarkupService.getRenderedElementForElement(bean.getElement());

            if (rendered != null) {
//                if (rendered != previouslyRendered) {
//                    Node parent = rendered.getParentNode();
////                    PageBox pageBox = webform.getPane().getPaneUI().getPageBox();
////                    pageBox.changed(rendered, parent, false);
//                    jsfForm.nodeChanged(rendered, parent, false);
//                } else {
//                    // We've re-rendered but the bean-reference doesn't point
//                    // to a new node.  This means that the component must have
//                    // supplied bogus component references when rendering.
//                    // Note: With wrong element referenced we're not well off anyway -
//                    // the component won't be selectable!
////                    PageBox pageBox = webform.getPane().getPaneUI().getPageBox();
////                    webform.getPane().getPaneUI().modelChanged();
//                    // XXX Now it means, that the node was updated, see the tryUpdateOriginalNode.
//                    jsfForm.modelChanged();
//                }
                // Now the original node could be reused (see the tryUpdateOriginalNode).
                Node parent = rendered.getParentNode();
                jsfForm.nodeChanged(rendered, parent, changedElements.toArray(new Element[changedElements.size()]));
            } else if (previouslyRendered != null) {
                // It was just deleted - for example when you change a component by
                // switching off its "rendered" property
                //webform.getPane().getPaneUI().modelChanged();
                Node parent = previouslyRendered.getParentNode();
//                PageBox pageBox = webform.getPane().getPaneUI().getPageBox();
//                pageBox.removed(previouslyRendered, parent);
                jsfForm.nodeRemoved(previouslyRendered, parent);
            }
        } else if (type == TYPE_INSERT) {
            // Insert new version of component
            if (!processInsert(bean)) {
                processRefresh();
//                webform.getPane().getPaneUI().modelChanged();
                jsfForm.modelChanged();

                return;
            }

//            Node rendered = ((RaveElement)bean.getElement()).getRendered();
            Node rendered = MarkupService.getRenderedElementForElement(bean.getElement());

            if (rendered != null) {
                Node parent = rendered.getParentNode();
//                webform.getPane().getPaneUI().getPageBox().inserted(rendered, parent);
                jsfForm.nodeInserted(rendered, parent);
            }
        } else if (type == TYPE_REFRESH) {
            // The entire HTML needs to be replaced
            processRefresh();

//            if ((webform.getPane() != null) && (webform.getPane().getPaneUI() != null)) {
//                webform.getPane().getPaneUI().modelChanged();
//            }
            jsfForm.modelChanged();
        } else if (type == TYPE_DELETE) {
            // Delete old version of component
            if (!processDelete(bean)) {
                processRefresh();
//                webform.getPane().getPaneUI().modelChanged();
                jsfForm.modelChanged();

                return;
            }

            deleteParent = null;

//            Node rendered = ((RaveElement)bean.getElement()).getRendered();
            Node rendered = MarkupService.getRenderedElementForElement(bean.getElement());

            if (rendered != null) {
                Node parent = rendered.getParentNode();
//                webform.getPane().getPaneUI().getPageBox().removed(rendered, parent);
                jsfForm.nodeRemoved(rendered, parent);
            }
        }

        if (DEBUG) {
//            DocumentFragment html = jsfForm.getDomProvider().getHtmlDocumentFragment();
            DocumentFragment html = jsfForm.getHtmlDomFragment();

            if (html != null) {
                System.out.println("\nAfter delayed updates to bean " + bean + " the html is\n" +
                    InSyncServiceProvider.get().getHtmlStream(html));
            }
        }
    }
    
    private boolean processUpdate(MarkupDesignBean bean, List<Element> changedElements) {
        // Deleting
//        if (!processDelete(bean)) {
//            return false;
//        }
        
        Element element = bean.getElement();
        Node rendered = MarkupService.getRenderedElementForElement(element);
        if (rendered == null) {
            return false;
        }

        Node parent = rendered.getParentNode();
        // Find leftmost node
        Node curr = rendered.getPreviousSibling();
        while (curr != null) {
            if (curr.getNodeType() == Node.ELEMENT_NODE) {
                Element xel = (Element)curr;
                if (MarkupUnit.getMarkupDesignBeanForElement(xel) == bean) {
                    rendered = curr;
                } else {
                    break;
                }
            }
            curr = curr.getPreviousSibling();
        }

        deleteParent = parent;

        List<Node> originalNodes = new ArrayList<Node>();
        Node before = null;
        // TODO - I ought to assert here that the parent is in the
        // HTML DOM I'm attached to
        // Remove all the rendered nodes
        while (rendered != null) {
//            Node before = rendered.getNextSibling();
            before = rendered.getNextSibling();
//            parent.removeChild(rendered);
            originalNodes.add(rendered);
            if (before == null) {
                break;
            }
            // See if I need to delete additional siblings. This is slightly
            // tricky because I can't just look at the next sibling. Components
            // may separate tags with text nodes that are associated with the
            // parent rather than the component being rendered; for example,
            // a component renderer may do this:
            //    writer.startElement(foo, component);
            //    writer.endElement(foo);
            //    writer.write("\n");
            //    writer.startElement(bar, component);
            //    ....
            // Note that we want to delete both foo and bar even though there's
            // a text node in between which is not associated with the component
            // since it's not within a startElement for "component", it's within
            // the element for the parent these children (foo and bar) are being
            // added to.
            // So, my strategy is to peek ahead and see if the next element
            // found is associated with the same DesignBean, and if so, delete
            // everything up to it.
            if (!moreElements(before, bean)) {
                break;
            }
            rendered = before;
        }
        
        // Inserting
//        if (!processInsert(bean)) {
//            return false;
//        }

        // XXX TODO There is not needed webform here.
        // Render the new element
        // TODO - this should not necessarily have to involve FacesBeans!
        // XXX Do not mark the new nodes as rendered for this case yet.
        DocumentFragment df = jsfForm.renderMarkupDesignBean(bean, false);

        // This seems to be not the correct place.
//        // XXX FIXME Is this correct here?
//        jsfForm.updateErrorsInComponent();

        if (DEBUG) {
            System.out.println("Got fragment from insert render html: " +
                InSyncServiceProvider.get().getHtmlStream(df));
        }

        List<Node> newNodes = new ArrayList<Node>();
        
        if (df != null) {
            // Insert nodes into the rendered dom
            NodeList nl = df.getChildNodes();
            int num = nl.getLength();

            if (num == 0) {
                // Rendered to nothing. This happens if you switch the Rendered attribute
                // to off for example - this comes in as a change request rather than
                // as a delete request - but we need to go and update the source element's
                // rendered reference since it would now be obsolete in the future
                // and we shouldn't attempt to use it
//                ((RaveElement)bean.getElement()).setRendered(null);
                // XXX FIXME Modifying the data structure!
                MarkupService.setRenderedElementForElement(bean.getElement(), null);
                return true;
            }

            // Can't remove from a NodeList while iterating over it - causes
            // surprises like null siblings. So we need to copy first.
            int n = nl.getLength();
            Node[] nodes = new Node[n];

            for (int i = 0; i < n; i++) {
                nodes[i] = nl.item(i);
            }

            for (int i = 0; i < n; i++) {
                Node nn = nodes[i];
                if (DEBUG) {
                    if (nn != null) {
                        System.out.println("next node is: " + InSyncServiceProvider.get().getHtmlStream(nn));
                        System.out.println("Fragment is now: " + InSyncServiceProvider.get().getHtmlStream(df));
                    }
                }

                if (nn != null) {
                    assert !isJspNode(nn);
//                    parent.insertBefore(nn, before);
                    newNodes.add(nn);
                }
            }
        } else {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new NullPointerException("Null DocumentFragment for JsfForm, jsfForm=" + jsfForm)); // NOI18N
            return false;
        }

        if (originalNodes.size() == 1 && newNodes.size() == 1) {
            Node originalNode = originalNodes.get(0);
            Node newNode = newNodes.get(0);
            // XXX #110662 Doesn't work for inline editing (the newly created elements need to be used).
            if (!jsfForm.isInlineEditing() && tryUpdateOriginalNode(originalNode, newNode, changedElements)) {
                // XXX The original nodes are updated, do not mark the new as rendered, they are not used.
                return true;
            } else {
                // XXX Clear possibly added elements.
                changedElements.clear();
                
                // XXX Mark the newly used nodes as rendered.
                MarkupService.markRenderedNodes(df);
                parent.replaceChild(newNode, originalNode);
            }
        } else {
            // XXX Mark the newly used nodes as rendered.
            MarkupService.markRenderedNodes(df);
            for (Node toRemove : originalNodes) {
                parent.removeChild(toRemove);
            }
            for (Node toAdd : newNodes) {
                parent.insertBefore(toAdd, before);
            }
        }
        
        return true;
    }
    
    
    private static boolean tryUpdateOriginalNode(Node originalNode, Node newNode, List<Element> changedElements) {
        if (originalNode.getNodeType() != newNode.getNodeType()) {
            return false;
        }
        
        if (originalNode.isEqualNode(newNode)) {
            return true;
        }
        
        // Update children
        NodeList originalNodeChildren = originalNode.getChildNodes();
        NodeList newNodeChildren = newNode.getChildNodes();
        if (originalNodeChildren == null) {
            if (newNodeChildren != null) {
                return false;
            }
        } else {
            if (newNodeChildren == null) {
                return false;
            }
            int originalNodeChildrenSize = originalNodeChildren.getLength();
            int newNodeChildrenSize = newNodeChildren.getLength();
            if (originalNodeChildrenSize != newNodeChildrenSize) {
                return false;
            }
            
            for (int i = 0; i < originalNodeChildrenSize; i++) {
                boolean childOK = tryUpdateOriginalNode(originalNodeChildren.item(i), newNodeChildren.item(i), changedElements);
                if (!childOK) {
                    return false;
                }
            }
        }
        
        // Update value.
        String originalValue = originalNode.getNodeValue();
        String newValue = newNode.getNodeValue();
        if ((originalValue == null && newValue != null)
        || (originalValue != null && !originalValue.equals(newValue))) {
            originalNode.setNodeValue(newValue);
        }
        
        // Update attributes.
        if (originalNode instanceof Element && newNode instanceof Element) {
            Element originalElement = (Element)originalNode;
            Element newElement = (Element)newNode;
            
            Map<String, Attr> originalMap = getAttributesMap(originalElement);
            Map<String, Attr> newMap = getAttributesMap(newElement);
            
            // Remove redundant attributes.
            for (String name : originalMap.keySet()) {
                if (newMap.containsKey(name)) {
                    continue;
                }
                originalElement.removeAttribute(name);
                if (!changedElements.contains(originalElement)) {
                    changedElements.add(originalElement);
                }
            }
            // Add/update the remaining attributes.
            for (String name : newMap.keySet()) {
                Attr newAttribute = newMap.get(name);
                Attr originalAttribute = originalMap.get(name);
                
                if (originalAttribute == null) {
                    originalElement.setAttributeNode((Attr)newAttribute.cloneNode(false));
                    if (!changedElements.contains(originalElement)) {
                        changedElements.add(originalElement);
                    }
                } else {
                    String oldAttributeValue = originalAttribute.getValue();
                    String newAttributeValue = newAttribute.getValue();
                    if (newAttributeValue == null) {
                        if (oldAttributeValue != null) {
                            originalElement.removeAttribute(name);
                            if (!changedElements.contains(originalElement)) {
                                changedElements.add(originalElement);
                            }
                        }
                    } else {
                        if (!newAttributeValue.equals(oldAttributeValue)) {
                            originalElement.setAttributeNode((Attr)newAttribute.cloneNode(false));
                            if (!changedElements.contains(originalElement)) {
                                changedElements.add(originalElement);
                            }
                        }
                    }
                }
            }
        }
        
        return true;
    }
    
    private static Map<String, Attr> getAttributesMap(Element element) {
        NamedNodeMap attributes = element.getAttributes();
        Map<String, Attr> attributesMap = new HashMap<String, Attr>();
        int size = attributes == null ? 0 : attributes.getLength();
        for (int i = 0; i < size; i++) {
            Node attr = attributes.item(i);
            if (attr instanceof Attr) {
                Attr attribute = (Attr)attr;
                attributesMap.put(attribute.getName(), attribute);
            }
        }
        return attributesMap;
    }

    /**
     * Check whether the given child is below the given parent.
     *
     * @param child The assumed child
     * @param parent The assumed parent
     *
     * @return true iff child is the same as parent or a descendant of the parent
     */
    public boolean isBelow(DesignBean child, DesignBean parent) {
        while (child != null) {
            if (child == parent) {
                return true;
            }

            child = child.getBeanParent();
        }

        return false;
    }

    /**
     * Given two DesignBeans known to not be different and known to not be ancestors or descendants
     * of each other, return a common ancestor DesignBean parent to both beans.
     *
     * @param a A DesignBean to locate a common ancestor with  <code>b</code> for
     * @param b A DesignBean to locate a common ancestor with  <code>a</code> for
     *
     * @return A common ancestor of <code>a</code> and <code>b</code>
     */
    public DesignBean getCommonAncestor(DesignBean a, DesignBean b) {
        a = a.getBeanParent();

        while (a != null) {
            if (isBelow(b, a)) {
                return a;
            }

            a = a.getBeanParent();
        }

        return null;
    }

    // ------------ Methods to actually modify the HTML DOM ---------------------------

    /**
     * Render the given source JSP element and insert its HTML content in the right place in the
     * HTML rendered DOM
     *
     * @param bean The bean to be rendered and inserted into the DOM
     *
     * @return true if everything works, otherwise false
     */
    private boolean processInsert(MarkupDesignBean bean) {
//        RaveElement element = (RaveElement)bean.getElement();
        Element element = bean.getElement();

        // Compute JSP DOM positions in the rendered markup
        Node parent = element.getParentNode();
        Node before = element.getNextSibling();

        // Compute HTML DOM corresponding positions
        if (before != null) {
//            if (before instanceof RaveRenderNode) {
//                before = ((RaveRenderNode)before).getRenderedNode();
//            } else {
//                before = null;
//
//                // TODO - how do we handle this? There could be whitespace text nodes
//                // in the JSP source dom which do not get rendered in the HTML...
//                // XXX Should I modify XhtmlText to do source<->model mapping too?
//                // ... fall through ...
//            }
            before = MarkupService.getRenderedNodeForNode(before);
        }

        if (before != null) {
            parent = before.getParentNode();
        } else if (deleteParent != null) {
            parent = deleteParent;
        } else {
            // Finding the parent could be tricky - but this will work because we walk
            // up past encodes-children components. For example, let's say you added a
            // <h:commandButton> component to a <h:gridPanel>.
            // You probably expect this to be inserted inside a <td> in the grid panel -
            // but the HTML element for the <h:gridPanel> is going to be the topmost
            // <table> tag!  But this is precisely why we skip up and re-render all
            // encodes-children components like gridpanel and data table. So you only
            // end up with cases where a parent is for example "<h:form>" and the
            // rendered dom parent insert position will be "<form>".
//            while (parent instanceof RaveRenderNode) {
//                Node renderedParent = ((RaveRenderNode)parent).getRenderedNode();
            while (parent != null) {
                Node renderedParent = MarkupService.getRenderedNodeForNode(parent);

                if (renderedParent != null) {
                    parent = renderedParent;

                    break;
                }

                parent = parent.getParentNode();
            }

            if (parent == null) {
                // You've inserted at the root of the document -- we should not allow that.
//                assert false : "Can't insert at document root";
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                        new NullPointerException("There is no rendered parent for the bean, insert not possible, bean=" + bean)); // NOI18N
                return false;
            }
        }

        // This should not be the case, but we REALLY have to make sure this does not happen
        // I can remove this check once I've fully debugged my new code
        if ((parent != null) && isJspNode(parent)) {
            // XXX What this check supposed to mean?
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalStateException("Parent may not be a jsp node, parent=" + parent)); // NOI18N
            return false;
        }

        // XXX TODO There is not needed webform here.
//        FileObject markupFile = jsfForm.getFacesModel().getMarkupFile();
        // Render the new element
        // TODO - this should not necessarily have to involve FacesBeans!
//        DocumentFragment df = FacesSupport.renderHtml(markupFile, bean, !CssBox.noBoxPersistence);
//        DocumentFragment df = InSyncServiceProvider.get().renderHtml(markupFile, bean);
//        FacesModel facesModel = jsfForm.getFacesModel();
//        DocumentFragment df = FacesPageUnit.renderHtml(facesModel, bean);
        DocumentFragment df = jsfForm.renderMarkupDesignBean(bean);
        
        // XXX FIXME Is this correct here?
//        webform.updateErrorsInComponent();
        jsfForm.updateErrorsInComponent();

        if (DEBUG) {
            System.out.println("Got fragment from insert render html: " +
                InSyncServiceProvider.get().getHtmlStream(df));
        }

        if (df != null) {
            // Insert nodes into the rendered dom
            NodeList nl = df.getChildNodes();
            int num = nl.getLength();

            if (num == 0) {
                // Rendered to nothing. This happens if you switch the Rendered attribute
                // to off for example - this comes in as a change request rather than
                // as a delete request - but we need to go and update the source element's
                // rendered reference since it would now be obsolete in the future
                // and we shouldn't attempt to use it
//                ((RaveElement)bean.getElement()).setRendered(null);
                // XXX FIXME Modifying the data structure!
                MarkupService.setRenderedElementForElement(bean.getElement(), null);

                return true;
            }

            // Can't remove from a NodeList while iterating over it - causes
            // surprises like null siblings. So we need to copy first.
            int n = nl.getLength();
            Node[] nodes = new Node[n];

            for (int i = 0; i < n; i++) {
                nodes[i] = nl.item(i);
            }

            for (int i = 0; i < n; i++) {
                Node nn = nodes[i];

                if (DEBUG) {
                    if (nn != null) {
                        System.out.println("next node is: " + InSyncServiceProvider.get().getHtmlStream(nn));
                        System.out.println("Fragment is now: " + InSyncServiceProvider.get().getHtmlStream(df));
                    }
                }

                if (nn != null) {
                    assert !isJspNode(nn);

                    parent.insertBefore(nn, before);
                }
            }
        } else {
//            assert false; // can this happen?
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
//                    new NullPointerException("Null DocumentFragment for FacesModel, facesModel=" + facesModel)); // NOI18N
                    new NullPointerException("Null DocumentFragment for JsfForm, jsfForm=" + jsfForm)); // NOI18N
            return false;
        }

        return true;
    }

    /**
     * Return true iff the given node is in the source JSP node
     */
    private boolean isJspNode(Node n) {
        // Determine if this node is in a DocumentFragment which means
        // it's read only
        while (n.getParentNode() != null) {
            n = n.getParentNode();
        }

        // We already have the isRendered property on XhtmlElements -- this should
        // agree with this actual parent-walking check. Assuming they are, I can
        // leave the faster isRendered check in the product once I feel confident
        // about this.
//        assert (!(n instanceof RaveElement)) ||
//        (((RaveElement)n).isRendered() == (n != webform.getDom()));
        // XXX One can't get out of this mess, what is actually supposed to be checked?
//        if (n instanceof Element 
//        && (!MarkupService.isRenderedNode(n) != (n == webform.getDom()))) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
//                    new IllegalStateException(); // XXX What to log?
//        }

        return n == jsfForm.getJspDom();
    }

    /**
     * Remove the rendered HTML for the given bean from the HTML rendered DOM
     *
     * @param bean The bean whose markup should be deleted from the HTML DOM
     *
     * @return true if everything works, otherwise false
     */
    private boolean processDelete(MarkupDesignBean bean) {
//        RaveElement element = (RaveElement)bean.getElement();
//        Node rendered = element.getRendered();
        Element element = bean.getElement();
        Node rendered = MarkupService.getRenderedElementForElement(element);

        if (rendered == null) {
            return false;
        }

        Node parent = rendered.getParentNode();

        // Find leftmost node
        Node curr = rendered.getPreviousSibling();

        while (curr != null) {
            if (curr.getNodeType() == Node.ELEMENT_NODE) {
//                RaveElement xel = (RaveElement)curr;
                Element xel = (Element)curr;

//                if (xel.getDesignBean() == bean) {
//                if (InSyncServiceProvider.get().getMarkupDesignBeanForElement(xel) == bean) {
                if (MarkupUnit.getMarkupDesignBeanForElement(xel) == bean) {
                    rendered = curr;
                } else {
                    break;
                }
            }

            curr = curr.getPreviousSibling();
        }

        deleteParent = parent;

        // TODO - I ought to assert here that the parent is in the
        // HTML DOM I'm attached to
        // Remove all the rendered nodes
        while (rendered != null) {
            Node before = rendered.getNextSibling();
            parent.removeChild(rendered);

            //assert deleted != null;
            if (before == null) {
                break;
            }

            // See if I need to delete additional siblings. This is slightly
            // tricky because I can't just look at the next sibling. Components
            // may separate tags with text nodes that are associated with the
            // parent rather than the component being rendered; for example,
            // a component renderer may do this:
            //    writer.startElement(foo, component);
            //    writer.endElement(foo);
            //    writer.write("\n");
            //    writer.startElement(bar, component);
            //    ....
            // Note that we want to delete both foo and bar even though there's
            // a text node in between which is not associated with the component
            // since it's not within a startElement for "component", it's within
            // the element for the parent these children (foo and bar) are being
            // added to.
            // So, my strategy is to peek ahead and see if the next element
            // found is associated with the same DesignBean, and if so, delete
            // everything up to it.
            if (!moreElements(before, bean)) {
                break;
            }

            rendered = before;
        }

        return true;
    }

    private boolean moreElements(Node node, MarkupDesignBean bean) {
        while (node != null) {
//            if (node instanceof RaveElement && (((RaveElement)node).getDesignBean() == bean)) {
            if ((node instanceof Element)
//            && (InSyncServiceProvider.get().getMarkupDesignBeanForElement((Element)node) == bean)) {
            && (MarkupUnit.getMarkupDesignBeanForElement((Element)node) == bean)) {
                return true;
            }

            node = node.getNextSibling();
        }

        return false;
    }

    private void processRefresh() {
        // XXX Revise this method, it might be meaningless here now.
//        webform.clearHtml();
//        jsfForm.getDomProvider().clearHtml();
        jsfForm.clearHtml();

        // Intentional side-effect -- cause re-generation of html dom
//        DocumentFragment html = jsfForm.getDomProvider().getHtmlDocumentFragment();
        DocumentFragment html = jsfForm.getHtmlDomFragment();

        if (DEBUG) {
            System.out.println("Refresh: Got new HTML fragment: " +
                InSyncServiceProvider.get().getHtmlStream(html));
        }

        //        webform.getPane().hideCaret();
        //        webform.getSelection().syncCaret();
//        webform.setGridMode(webform.getDocument().isGridMode()); // XXX
        jsfForm.updateGridMode();
    }

    /**
     * Given a bean in the DOM, duplicate its associated node tree and return this in a new
     * DocumentFragment. Additionally, the returned fragment will be marked as the source nodes
     * for the render fragment.
     */
    public DocumentFragment createSourceFragment(MarkupDesignBean bean) {
        //return FacesSupport.renderHtml(webform, bean);
//        RaveElement element = (RaveElement)bean.getElement();
//        Node rendered = element.getRendered();
        Element element = bean.getElement();
        Node rendered = MarkupService.getRenderedElementForElement(element);

        if (rendered == null) {
            return null;
        }

        org.w3c.dom.Document doc = jsfForm.getJspDom();
        DocumentFragment fragment = doc.createDocumentFragment();

        // Duplicate and move all the nodes in the HTML DOM into the new fragment
        while (rendered != null) {
            Node before = rendered.getNextSibling();
            Node n = doc.importNode(rendered, true);
            fragment.appendChild(n);
//            ((RaveRenderNode)n).setJspx(false); // XXX Gotta do this recursively. How?
            MarkupService.setJspxNode(n, false);
            
            MarkupService.markRendered(n, rendered);

            if (before == null) {
                break;
            }

//            if (before instanceof RaveRenderNode) {
//                RaveRenderNode rn = (RaveRenderNode)before;
//
//                if (rn.getSourceNode() != element) {
//                    break;
//                }
//            } else {
//                break;
//            }
            Node sourceNode = MarkupService.getSourceNodeForNode(before);
            if (sourceNode != null && sourceNode != element) {
                break;
            }

            rendered = before;
        }

        return fragment;
    }

    /**
     * Locate the most distant renders-children ancestor of this bean (if any) and return it -
     * otherwise return the bean itself. This is used to compute the leaf-most node in the tree we
     * need to re-render the HTML for. For example, think of a datagrid containin a single command
     * button. When this button is updated, the parent will re-generate this button in multiple
     * table cells. Thus we need to find any containing datagrid (or more generally, any component
     * which has rendersChildren set, since these can potentially replicate the children.)
     */
    private MarkupDesignBean getRenderTarget(MarkupDesignBean bean) {
        DesignBean curr = bean;

        for (; curr != null; curr = curr.getBeanParent()) {
            if (!(curr instanceof MarkupDesignBean)) {
                break;
            }

            MarkupDesignBean b = (MarkupDesignBean)curr;
            Object instance = curr.getInstance();

            if (instance instanceof UIComponent) {
                ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
            	try {
                    Thread.currentThread().setContextClassLoader(InSyncServiceProvider.get().getContextClassLoader(b));
                    // See #6475512. Not needed anymore.
//                    try {
                        if (((UIComponent)instance).getRendersChildren()) {
                            bean = b;

                            // Can't break here - there could be an outer
                            // renders-children parent
                        }
//                    } catch (NullPointerException ex) { // XXX Catching runtime exception is a wrong solution.
//                        // XXX #6465131 Temporary workaround to find out what is going on, see the bug.
//                        IllegalStateException ise = new IllegalStateException(
//                                "NPE occured when called UIComponent.getRendersChildren() on instance=" + instance, ex); // NOI18N
//                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ise);
//                         // XXX #6475481 Force refresh in that case.
//                        return null;
//                    }
                } finally {    		
                    Thread.currentThread().setContextClassLoader(oldContextClassLoader);
            	}

            }
                // Events in the <head> section typically implies a re-render
                // on the whole document - e.g. background color changes, or
                // style sheet changes, or character encoding changes...
//                RaveElement e = (RaveElement)b.getElement();
//                if (e.getRendered() != null) {
//                    e = (RaveElement)e.getRendered();
//                }
			Element e = b.getElement();
			Element rendered = MarkupService.getRenderedElementForElement(e);
			if (rendered != null) {
				e = rendered;
			}
			
			String tag = e.getTagName();
			
			if (tag.equals(HtmlTag.HEAD.name)) {
				return null; // Force refresh
			} else if ((curr == bean) && tag.equals(HtmlTag.BODY.name)) {
				// Refresh document if we're trying to change the body itself, not some
				// child of it
				return null;
			} else if ((curr == bean) &&
					   ((e.getParentNode() == null) ||
                        (e.getParentNode().getNodeType() != Node.ELEMENT_NODE))) {
				return null;
			}
        }

        return bean;
    }

    // -------- Source DOM listening ---------------------------------------------------
    // We need to listen to the DOM for changes because updates like text modifications
    // aren't communicated to us!
    public void handleEvent(final org.w3c.dom.events.Event e) {
        // I seem to get lots of useless mutation events - old value = new value
        //            if (e instanceof org.w3c.dom.events.MutationEvent) {
        //                org.w3c.dom.events.MutationEvent me =
        //                    (org.w3c.dom.events.MutationEvent)e;
        //                String old = me.getPrevValue();
        //                String nw = me.getNewValue();
        //                if (((old != null) && (nw != null) && (old.equals(nw)))) {
        //                    if (debugevents) {
        //                        Log.err.log("Event " + e + " on " + e.getTarget() + ": ignoring since prev-value==new-value");
        //                    }
        //                    return;
        //                }
        //            }
        //            if (debugevents) {
        //                Log.err.log("View.handleEvent(" + e + ")");
        //                Log.err.log("type = " + e.getType());
        //                Log.err.log("phase = " + e.getEventPhase());
        //                Log.err.log("bubbles = " + e.getBubbles());
        //                Log.err.log("target = " + e.getTarget());
        //                if (e.getTarget() != null) {
        //                    Log.err.log("target.parent = " + ((org.w3c.dom.Node)e.getTarget()).getParentNode());
        //                    if (((org.w3c.dom.Node)e.getTarget()).getParentNode() != null) {
        //                        Log.err.log("target.parent.parent = " + ((org.w3c.dom.Node)e.getTarget()).getParentNode().getParentNode());
        //                    }
        //                }
        //                Log.err.log("target = " + e.getTarget().getClass().getName());
        //                Log.err.log("currtarget = " + e.getCurrentTarget());
        //                Log.err.log("currtarget = " + e.getCurrentTarget().getClass().getName());
        //                if (e instanceof org.w3c.dom.events.MutationEvent) {
        //                    org.w3c.dom.events.MutationEvent me =
        //                        (org.w3c.dom.events.MutationEvent)e;
        //                    Log.err.log("getAttrName = " + me.getAttrName());
        //                    Log.err.log("attrchange = " + me.getAttrChange());
        //                    Log.err.log("newvalue = " + me.getNewValue());
        //                    Log.err.log("prevvalue = " + me.getPrevValue());
        //                    Log.err.log("relatednode = " + me.getRelatedNode());
        //                    if (me.getRelatedNode() != null) {
        //                        Log.err.log("relatednode.parent = " + me.getRelatedNode().getParentNode());
        //                    }
        //                }
        //            }
        if (e.getType().equals(MarkupUnit.DOM_DOCUMENT_REPLACED)) {
            updateDomListeners();

            // Ensure that the caret is in the new DOM
//            DesignerPane pane = webform.getPane();
//
//            if (pane != null) {
//                if (pane.getCaret() != null) {
//                    pane.getCaret().detachDom();
//
//                    //pane.setCaret(null);
//                }
//
//                //                pane.showCaretAtBeginning();
//            }
            jsfForm.documentReplaced();

            return;
        }

        Node node = (org.w3c.dom.Node)e.getTarget();

        // Text node or entity node changes should get translated
        // into a change event on their surrounding element...
        // XXX I could possibly handle to rebreak only
        // the LineBreakGroup.... That would save work -ESPECIALLY-
        // for text right within the <body> tag... but optimize that
        // later
        if (!(node instanceof Element) || ((Element)node).getTagName().equals(HtmlTag.BR.name)) { // text, cdata, entity, ...
            node = node.getParentNode();

            if (node instanceof Element) {
//                MarkupDesignBean bean = ((RaveElement)node).getDesignBean();
                MarkupDesignBean bean = InSyncServiceProvider.get().getMarkupDesignBeanForElement((Element)node);

                if (bean != null) {
                    requestTextUpdate((MarkupDesignBean)bean);

                    return;
                }
            }
        }
    }

    // -------- Listener Registration ---------------------------------------------------
    private void registerDomListeners() {
        if (currentDOM instanceof EventTarget) {
            EventTarget target = (org.w3c.dom.events.EventTarget)currentDOM;
            target.addEventListener(MutationEventImpl.DOM_ATTR_MODIFIED, this, false);

            /* This event seems to be redundant.
               target.addEventListener(MutationEventImpl.DOM_SUBTREE_MODIFIED, this, false);
             */
            target.addEventListener(MutationEventImpl.DOM_NODE_INSERTED, this, false);
            target.addEventListener(MutationEventImpl.DOM_NODE_INSERTED_INTO_DOCUMENT, this, false);
            target.addEventListener(MutationEventImpl.DOM_NODE_REMOVED, this, false);
            target.addEventListener(MutationEventImpl.DOM_NODE_REMOVED_FROM_DOCUMENT, this, false);
            target.addEventListener(MutationEventImpl.DOM_CHARACTER_DATA_MODIFIED, this, false);

            target.addEventListener(MarkupUnit.DOM_DOCUMENT_REPLACED, this, false);
        }
    }

    public void unregisterDomListeners() {
        if (currentDOM instanceof EventTarget) {
            EventTarget target = (org.w3c.dom.events.EventTarget)currentDOM;
            target.removeEventListener(MutationEventImpl.DOM_ATTR_MODIFIED, this, false);

            /* This event seems to be redundant.
               target.removeEventListener(MutationEventImpl.DOM_SUBTREE_MODIFIED, this, false);
             */
            target.removeEventListener(MutationEventImpl.DOM_NODE_INSERTED, this, false);
            target.removeEventListener(MutationEventImpl.DOM_NODE_INSERTED_INTO_DOCUMENT, this, false);
            target.removeEventListener(MutationEventImpl.DOM_NODE_REMOVED, this, false);
            target.removeEventListener(MutationEventImpl.DOM_NODE_REMOVED_FROM_DOCUMENT, this, false);
            target.removeEventListener(MutationEventImpl.DOM_CHARACTER_DATA_MODIFIED, this, false);

            target.removeEventListener(MarkupUnit.DOM_DOCUMENT_REPLACED, this, false);
        }
    }

    /**
     * The underlying DOM which manages the document content has changed. This might for example
     * happen if the document source is edited and reparsed.
     */
    public void updateDomListeners() {
        boolean wasDifferent = false;

        if (currentDOM != jsfForm.getJspDom()) {
            wasDifferent = true;

            if (currentDOM != null) {
                unregisterDomListeners();
            }

            currentDOM = jsfForm.getJspDom();

            if (currentDOM == null) {
                return;
            }
        }

        //        fireDomChangedUpdate();
        if (wasDifferent) {
            registerDomListeners();
        }

        //        webform.setGridMode(isGridMode());
    }

    /** Impl <code>UpdateSuspender</code>. */
    public void setSuspended(MarkupDesignBean markupDesignBean, boolean suspend) {
        setUpdatesSuspended(markupDesignBean, suspend);
    }
}
