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

package org.netbeans.modules.soa.mapper.basicmapper;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import org.netbeans.modules.soa.mapper.basicmapper.canvas.MapperCanvasView;
import org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo.AbstractCanvasView;
import org.netbeans.modules.soa.mapper.basicmapper.tree.DestTree;
import org.netbeans.modules.soa.mapper.basicmapper.tree.SourceTree;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicMapperModel;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicViewManager;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.IMapperCanvasView;
import org.netbeans.modules.soa.mapper.common.basicmapper.palette.IPaletteView;
import org.netbeans.modules.soa.mapper.common.basicmapper.tree.IMapperTreeView;
import org.netbeans.modules.soa.mapper.common.IMapperEvent;
import org.netbeans.modules.soa.mapper.common.IMapperViewModel;

/**
 * <p>
 *
 * Title: </p> BasicViewManager <p>
 *
 * Description: </p> BasicViewManager provides basic IBasicViewManager
 * implemetation, and instaniates all the views object. <p>
 *
 * @author    Un Seng Leong
 * @created   January 6, 2003
 */
public class BasicViewManager
     implements IBasicViewManager {

    /**
     * mapper model selected layer change listener
     */
    protected PropertyChangeListener changeLayerListener =
        new ChangeLayerListener();

    /**
     * the canvas view
     */
    private MapperCanvasView mCanvasView;

    /**
     * destinated tree view
     */
    private DestTree mDestView;

    /**
     * the mapper event queue
     */
    private List mEventQueue;

    /**
     * the mapper model
     */
    private IBasicMapperModel mModel;

    /**
     * the palette view
     */
    private IPaletteView mPaletteToolbar;

    /**
     * the source tree view
     */
    private SourceTree mSourceView;

    /*
     * flag to indicate if link needs to be highlighted.
     *  
     */
    private boolean highlightLink = true;
    
    /**
     * flag if highlighting is on or off.
     */
    private boolean toggleHighlighting = true;
    

    /**
     * Creates a new BasicViewManager object.
     */
    public BasicViewManager() {
        super();
        mEventQueue =
            new Vector() {
                public synchronized boolean add(Object obj) {
                    if (super.add(obj)) {
                        this.notify();
                        return true;
                    }
                    return false;
                }

                public synchronized void add(
                    int i,
                    Object obj) {
                    super.add(i, obj);
                    this.notify();
                }
            };
        mCanvasView = new MapperCanvasView();
        mCanvasView.setViewManager(this);
        mDestView = new DestTree();
        mDestView.setViewManager(this);
        mSourceView = new SourceTree();
        mSourceView.setViewManager(this);
        
//      add key listener to toggle highlighting
        ToggleHighlightAction hAction = new ToggleHighlightAction();
        
        ((AbstractCanvasView) mCanvasView.getCanvas()).getActionMap().put("toggleHighlight", hAction);
        ((AbstractCanvasView) mCanvasView.getCanvas()).getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_MASK, false), "toggleHighlight");
        mSourceView.getTree().getActionMap().put("toggleHighlight", hAction);
        mSourceView.getTree().getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_MASK, false), "toggleHighlight");
        
        mDestView.getTree().getActionMap().put("toggleHighlight", hAction);
        mDestView.getTree().getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_MASK, false), "toggleHighlight");
    }


    /**
     * Return the transform canvas view of this mapper.
     *
     * @return   the transform canvas view of this mapper
     */
    public IMapperCanvasView getCanvasView() {
        return mCanvasView;
    }


    /**
     * Return the destination view of this manager.
     *
     * @return   the destination view of this manager.
     */
    public IMapperTreeView getDestView() {
        return mDestView;
    }


    /**
     * Return the event queue of this mapper.
     *
     * @return   the event queue in a list object repersentation.
     */
    public List getEventQueue() {
        return mEventQueue;
    }


    /**
     * Return the mapper model of this mapper.
     *
     * @return   the mapper model manager of this mapper.
     */
    public IBasicMapperModel getMapperModel() {
        return mModel;
    }


    /**
     * Return the palette of this mapper.
     *
     * @return   the palette of this mapper.
     */
    public IPaletteView getPaletteView() {
        return mPaletteToolbar;
    }

    /**
     * Set the palette view instance.
     *
     * @param view  the palette view instance.
     */
    public void setPaletteView(IPaletteView view) {
        mPaletteToolbar = view;
    }

    /**
     * Return the source view of this manager.
     *
     * @return   the source view of this manager.
     */
    public IMapperTreeView getSourceView() {
        return mSourceView;
    }


    /**
     * Set the mapper model
     *
     * @param model  the mapper model
     */
    public void setMapperModel(IBasicMapperModel model) {
        if (mModel == model) {
            return;
        }

        if (mModel != null) {
            mModel.removePropertyChangeListener(changeLayerListener);
        }

        mModel = model;

        IMapperViewModel viewModel = null;

        if (mModel != null) {
            viewModel = mModel.getSelectedViewModel();
            mModel.addPropertyChangeListener(changeLayerListener);
        }
        setNewViewModel(viewModel);
    }

    /**
     * Post a mapper event to mapper event queue.
     *
     * @param e  the mapper event invoked.
     */
    public void postMapperEvent(IMapperEvent e) {
        if (e != null) {
            mEventQueue.add(e);
        }
    }

    /**
     * Set the view model for each of the mapper views as the currect view
     * model.
     *
     * @param viewModel  the view model.
     */
    private void setNewViewModel(IMapperViewModel viewModel) {
        mSourceView.setViewModel(viewModel);
        mDestView.setViewModel(viewModel);
        mCanvasView.setViewModel(viewModel);
    }


    /**
     * PropertyChangeListener listens on the model selected view model change
     * and set the view model to all mapper views accordingly.
     *
     * @author    Un Seng Leong
     * @created   January 6, 2003
     * @version   
     */
    private class ChangeLayerListener
         implements PropertyChangeListener {
        /**
         * Set the selected view model from the mapper model to all mapper views
         * accordingly.
         *
         * @param event  the PropertyChangeEvent object
         */
        public void propertyChange(PropertyChangeEvent event) {
            if (event.getPropertyName()
                .equals(IBasicMapperModel.SELECTED_VIEWMODEL_CHANGED)) {
                setNewViewModel((IMapperViewModel) event.getNewValue());
            }
        }
    }
    
    
    /**
     * set flag to indicate whether to highlight a link.
     * @param highlight flag
     */
    public void setHighlightLink(boolean highlight) {
        this.highlightLink = highlight;
    }

    /**
     * check if link needs to highlighted.
     * @return true if link needs to be highlighed.
     */
    public boolean isHighlightLink() {
        return this.highlightLink;
    }

    /**
     * flag to set if highlighting needs to be toggled.
     * @param toggle flag
     */
    public void setToggleHighlighting(boolean toggle) {
        this.toggleHighlighting = toggle;
    }

    /**
     * check if highlighting is toggled
     * @return true then highlighting needs to be enabled.
     */
    public boolean isToggleHighlighting() {
        return this.toggleHighlighting;
    }

    
    class ToggleHighlightAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            boolean oldState = isToggleHighlighting();
            setToggleHighlighting(!oldState);
            if (oldState) {
                ((AbstractCanvasView) mCanvasView.getCanvas()).repaint();
                mSourceView.getTree().repaint();
                mDestView.getTree().repaint();

            }
        }
    }
}
