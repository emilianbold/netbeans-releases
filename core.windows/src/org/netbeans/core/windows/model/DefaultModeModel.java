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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.core.windows.model;



import java.awt.*;
import java.util.List;

import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.SplitConstraint;
import org.openide.windows.TopComponent;


/**
 *
 * @author  Peter Zavadsky
 */
final class DefaultModeModel implements ModeModel {


    /** Programatic name of mode. */
    private final String name;

    private final Rectangle bounds = new Rectangle();

    private final Rectangle boundsSeparetedHelp = new Rectangle();

    /** State of mode: split or separate. */
    private /*final*/ int state;
    /** Kind of mode: editor or view. */
    private final int kind;
    
    /** Frame state. */
    private int frameState;

    /** Permanent property. */
    private final boolean permanent;

    /** Sub model which manages TopComponents stuff. */
    private final TopComponentSubModel topComponentSubModel;
    
    /** Context of tcx. Lazy initialization, because this will be used only by
     * sliding kind of modes */
    private TopComponentContextSubModel topComponentContextSubModel = null;

    // Locks>>
    /** */
    private final Object LOCK_STATE = new Object();
    /** */
    private final Object LOCK_BOUNDS = new Object();
    /** */
    private final Object LOCK_BOUNDS_SEPARATED_HELP = new Object();
    /** Locks frameState. */
    private final Object LOCK_FRAMESTATE = new Object();
    /** Locks top components. */
    private final Object LOCK_TOPCOMPONENTS = new Object();
    /** Locks tc contexts */
    private final Object LOCK_TC_CONTEXTS = new Object();
    
    
    public DefaultModeModel(String name, int state, int kind, boolean permanent) {
        this.name = name;
        this.state = state;
        this.kind = kind;
        this.permanent = permanent;
        this.topComponentSubModel = new TopComponentSubModel(kind);
    }

    /////////////////////////////////////
    // Mutator methods >>
    /////////////////////////////////////
    public void setState(int state) {
        synchronized(LOCK_STATE) {
            this.state = state;
        }
    }
    
    public void removeTopComponent(TopComponent tc, TopComponent recentTc) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.removeTopComponent(tc, recentTc);
        }
    }
    
    // XXX
    public void removeClosedTopComponentID(String tcID) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.removeClosedTopComponentID(tcID);
        }
    }
    
    /** Adds opened TopComponent. */
    public void addOpenedTopComponent(TopComponent tc) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.addOpenedTopComponent(tc);
        }
    }
    
    public void insertOpenedTopComponent(TopComponent tc, int index) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.insertOpenedTopComponent(tc, index);
        }
    }
    
    public void addClosedTopComponent(TopComponent tc) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.addClosedTopComponent(tc);
        }
    }
    
    public void addUnloadedTopComponent(String tcID) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.addUnloadedTopComponent(tcID);
        }
    }
    
    public void setUnloadedSelectedTopComponent(String tcID) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.setUnloadedSelectedTopComponent(tcID);
        }
    }
    
    public void setUnloadedPreviousSelectedTopComponent(String tcID) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.setUnloadedPreviousSelectedTopComponent(tcID);
        }
    }
    
    /** Sets seleted TopComponent. */
    public void setSelectedTopComponent(TopComponent selected) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.setSelectedTopComponent(selected);
        }
    }
    
    public void setPreviousSelectedTopComponentID(String prevSelectedId) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.setPreviousSelectedTopComponentID(prevSelectedId);
        }
    }

    /** Sets frame state */
    public void setFrameState(int frameState) {
        synchronized(LOCK_FRAMESTATE) {
            this.frameState = frameState;
        }
    }

    public void setBounds(Rectangle bounds) {
        if(bounds == null) {
            return;
        }
        
        synchronized(LOCK_BOUNDS) {
            this.bounds.setBounds(bounds);
        }
    }
    
    public void setBoundsSeparatedHelp(Rectangle boundsSeparatedHelp) {
        if(bounds == null) {
            return;
        }
        
        synchronized(LOCK_BOUNDS_SEPARATED_HELP) {
            this.boundsSeparetedHelp.setBounds(boundsSeparatedHelp);
        }
    }
    /////////////////////////////////////
    // Mutator methods <<
    /////////////////////////////////////


    /////////////////////////////////////
    // Accessor methods >>
    /////////////////////////////////////
    public String getName() {
        return name;
    }
    
    public Rectangle getBounds() {
        synchronized(LOCK_BOUNDS) {
            return (Rectangle)this.bounds.clone();
        }
    }
    
    public Rectangle getBoundsSeparatedHelp() {
        synchronized(LOCK_BOUNDS_SEPARATED_HELP) {
            return (Rectangle)this.boundsSeparetedHelp.clone();
        }
    }
    
    public int getState() {
        synchronized(LOCK_STATE) {
            return this.state;
        }
    }
    
    public int getKind() {
        return this.kind;
    }
    
    /** Gets frame state. */
    public int getFrameState() {
        synchronized(LOCK_FRAMESTATE) {
            return this.frameState;
        }
    }
    
    public boolean isPermanent() {
        return this.permanent;
    }
    
    public boolean isEmpty() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponentSubModel.isEmpty();
        }
    }
    
    public boolean containsTopComponent(TopComponent tc) {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponentSubModel.containsTopComponent(tc);
        }
    }

    /** Gets list of top components in this workspace. */
    public List<TopComponent> getTopComponents() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponentSubModel.getTopComponents();
        }
    }


    /** Gets selected TopComponent. */
    public TopComponent getSelectedTopComponent() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponentSubModel.getSelectedTopComponent();
        }
    }
    /** Gets the ID of top component that was selected before switching to/from maximized mode */
    public String getPreviousSelectedTopComponentID() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponentSubModel.getPreviousSelectedTopComponentID();
        }
    }

    /** Gets list of top components. */
    public List<TopComponent> getOpenedTopComponents() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponentSubModel.getOpenedTopComponents();
        }
    }
    
    // XXX
    public List<String> getOpenedTopComponentsIDs() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponentSubModel.getOpenedTopComponentsIDs();
        }
    }
    
    public List<String> getClosedTopComponentsIDs() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponentSubModel.getClosedTopComponentsIDs();
        }
    }
    
    public List<String> getTopComponentsIDs() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponentSubModel.getTopComponentsIDs();
        }
    }
    
    public int getOpenedTopComponentTabPosition (TopComponent tc) {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponentSubModel.getOpenedTopComponentTabPosition(tc);
        }
    }
    
    public SplitConstraint[] getTopComponentPreviousConstraints(String tcID) {
        synchronized(LOCK_TC_CONTEXTS) {
            return getContextSubModel().getTopComponentPreviousConstraints(tcID);
        }
    }
    
    public ModeImpl getTopComponentPreviousMode(String tcID) {
        synchronized(LOCK_TC_CONTEXTS) {
            return getContextSubModel().getTopComponentPreviousMode(tcID);
        }
    }
    /** Gets the tab index of the top component in its previous mode */
    public int getTopComponentPreviousIndex(String tcID) {
        synchronized(LOCK_TC_CONTEXTS) {
            return getContextSubModel().getTopComponentPreviousIndex(tcID);
        }
    }
    
    public void setTopComponentPreviousConstraints(String tcID, SplitConstraint[] constraints) {
        synchronized(LOCK_TC_CONTEXTS) {
            getContextSubModel().setTopComponentPreviousConstraints(tcID, constraints);
        }
    }
    
    public void setTopComponentPreviousMode(String tcID, ModeImpl mode, int prevIndex) {
        synchronized(LOCK_TC_CONTEXTS) {
            getContextSubModel().setTopComponentPreviousMode(tcID, mode, prevIndex);
        }
    }
    
    /////////////////////////////////////
    // Accessor methods <<
    /////////////////////////////////////
    
    private TopComponentContextSubModel getContextSubModel() {
        if (topComponentContextSubModel == null) {
            topComponentContextSubModel = new TopComponentContextSubModel();
        }
        return topComponentContextSubModel;
    }
    
}

