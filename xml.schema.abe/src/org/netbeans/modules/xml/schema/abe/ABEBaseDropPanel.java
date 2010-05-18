/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
                        PROP_SHUTDOWN)) {
                    fireComponentRemoved();
                    ABEBaseDropPanel.this.context.removePropertyChangeListener(this);
                }
            }
        });
        initialize();
        initKeyList();
    }
    
    protected void fireComponentRemoved() {
        firePropertyChange(PROP_COMPONENT_REMOVED, " ", this);
    }

    private void initKeyList(){
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
               if(context.getFocusTraversalManager().isFocusChangeEvent(e)) {
                    context.getFocusTraversalManager().handleEvent(e, ABEBaseDropPanel.this);
                }
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
