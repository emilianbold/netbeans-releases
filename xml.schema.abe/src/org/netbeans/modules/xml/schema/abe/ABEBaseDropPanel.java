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

package org.netbeans.modules.xml.schema.abe;

import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JPanel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.schema.abe.nodes.ABEAbstractNode;
import org.netbeans.modules.xml.xam.ui.XAMUtils;

/**
 *
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public abstract class ABEBaseDropPanel extends JPanel {
    protected static final long serialVersionUID = 7526472295622776147L;
    protected InstanceUIContext context;
    protected boolean firstTimeRename = false;
    /**
     *
     *
     */
    public ABEBaseDropPanel(InstanceUIContext context) {
        super();
        this.context = context;
        this.context.addPropertyChangeListener(new PropertyChangeListener(){
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getPropertyName().equals(InstanceDesignConstants.
                        PROP_SHUTDOWN)){
                    ABEBaseDropPanel.this.context.removePropertyChangeListener(this);
                }
            }
        });
        initialize();
        initKeyList();
    }
      
    private void initKeyList(){
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if( e.getKeyCode() ==  KeyEvent.VK_CONTEXT_MENU) {
                    context.getMultiComponentActionManager().showPopupMenu(e, ABEBaseDropPanel.this);
                    return;
                }
               
            }
            
        });
    }
    
    
    public boolean isWritable(){
        return XAMUtils.isWritable(context.getAXIModel());
    }
    
    /**
     *
     *
     */
    private void initialize() {
        // Set up the drop target to accept items from the palette
        //allow DnD only if the model is writable
        setDropTarget(
                new DropTarget(this,
                new DropTargetListener() {
            public void dragEnter(DropTargetDragEvent event) {
                if(!isWritable())
                    return;
                setActive(true);
                ABEBaseDropPanel.this.dragEnter(event);
            }
            
            public void dragExit(DropTargetEvent event) {
                if(!isWritable())
                    return;
                setActive(false);
                ABEBaseDropPanel.this.dragExit(event);
            }
            
            public void dragOver(DropTargetDragEvent event) {
                if(!isWritable())
                    return;
                ABEBaseDropPanel.this.dragOver(event);
            }
            
            public void drop(DropTargetDropEvent event) {
                if(!isWritable())
                    return;
                setActive(false);
                ABEBaseDropPanel.this.drop(event);
            }
            
            public void dropActionChanged(DropTargetDragEvent event) {
                if(!isWritable())
                    return;
                ABEBaseDropPanel.this.dropActionChanged(event);
            }
        })
        );
        
    }
    
    
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Drag methods
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     *
     *
     */
    public boolean isActive() {
        return active;
    }
    
    
    /**
     *
     *
     */
    public void setActive(boolean value) {
        if (value!=active) {
            boolean oldValue=active;
            active=value;
            
            handleActive(value);
            firePropertyChange(PROP_ACTIVE,oldValue,active);
        }
    }
    
    
    /**
     *
     *
     */
    protected void handleActive(boolean value) {
        // Do nothing
    }
    
    
    /**
     *
     *
     */
    public void dragEnter(DropTargetDragEvent event) {
        event.rejectDrag();
        // Do nothing
    }
    
    
    /**
     *
     *
     */
    public void dragExit(DropTargetEvent event) {
        // Do nothing
    }
    
    
    /**
     *
     *
     */
    public void dragOver(DropTargetDragEvent event) {
        event.rejectDrag();
        // Do nothing
    }
    
    
    /**
     *
     *
     */
    public void drop(DropTargetDropEvent event) {
        event.rejectDrop();
        // Do nothing
    }
    
    
    /**
     *
     *
     */
    public void dropActionChanged(DropTargetDragEvent event) {
        event.rejectDrag();
        // Do nothing
    }
    
    public ABEAbstractNode getNBNode(){
        return null;
    }
    
    public AXIComponent getAXIComponent(){
        return null;
    }
    
    
    public abstract void accept(UIVisitor visitor);
    
    
    boolean selected;
    public void setSelected(boolean selected){
        if(selected == this.selected)
            return;
        firePropertyChange(PROP_SELECTED, this.selected, selected);
        this.selected = selected;
    }

    public void removeNotify() {
        super.removeNotify();
        firePropertyChange(PROP_COMPONENT_REMOVED, " ", this);
    }
    
    public ABEBaseDropPanel getUIComponentFor(AXIComponent axiComponent){
        if(getAXIComponent() == axiComponent)
            return this;
        return null;
    }
    
    
    public ABEBaseDropPanel getChildUIComponentFor(AXIComponent axiComponent){
        return null;
    }
    
    
    public InstanceUIContext getContext(){
        return this.context;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Instance members
    ////////////////////////////////////////////////////////////////////////////
    
    public static final String PROP_ACTIVE="active";
    public static final String PROP_SELECTED="SELECTED";
    public static final String PROP_COMPONENT_REMOVED="COMPONENT_REMOVED";
    
    
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Instance members
    ////////////////////////////////////////////////////////////////////////////
    
    private boolean active;
}
