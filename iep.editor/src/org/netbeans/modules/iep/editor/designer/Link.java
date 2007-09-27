/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.iep.editor.designer;

import com.nwoods.jgo.*;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponent;
import org.netbeans.modules.iep.editor.designer.GuiConstants;
import org.netbeans.modules.iep.editor.model.Plan;
import org.netbeans.modules.iep.editor.tcg.model.TcgModelManager;
import java.util.logging.Level;

// Flows are implemented as labeled links
//
// For this example app, the only property, Text, is
// actually just the label's Text.
public class Link extends JGoLink implements GuiConstants, ComponentHolder {
    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(Link.class.getName());

    private static final JGoPen mPen = JGoPen.black;

    // State
    private transient TcgComponent mComponent;
    
    // Used for carring content of mComponent during cut, copy, and paste
    private String mComponentXml;

    public Link() {
        super();
    }
    
    public Link(TcgComponent component, JGoPort from, JGoPort to) {
        super(from, to);
        try {
            mComponent = component;
            EntityNode fromNode = getFromNode();
            if (fromNode != null) {
                String fromId = (String)fromNode.getComponent().getProperty(ID_KEY).getValue();
                component.getProperty(FROM_KEY).setValue(fromId);
            }
            EntityNode toNode = getToNode();
            if (toNode != null) {
                String toId = (String)toNode.getComponent().getProperty(ID_KEY).getValue();
                component.getProperty(TO_KEY).setValue(toId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
   /**
    * Create a new instance of this object.
    * JGoDocument.copyFromCollection does not copy links until after both ports have been copied.
    *
    * Called when copy from PdCanvas to Clipboard
    *
    * @param env the environment for the copy operation - keeps track of copied objects.
    */
    public JGoObject copyObject(JGoCopyEnvironment env) {
        Link newObj = (Link)super.copyObject(env);
        newObj.mComponentXml = mComponent.toXml();
        return newObj;
    }

    
    public void initialize() {
        setJumpsOver(true);
        setAvoidsNodes(true);
        setAdjustingStyle(JGoLink.AdjustingStyleStretch);
        setPen(mPen);
    }
    
    public PdModel getDoc() { 
        return (PdModel)getDocument(); 
    }
    
    public EntityNode getFromNode() {
        if (getFromPort() != null) {
            return (EntityNode)(getFromPort().getParent());
        }
        return null;
    }
    
    public EntityNode getToNode() {
        if (getToPort() != null) {
            return (EntityNode)(getToPort().getParent());
        }
        return null;
    }
    
    // Normally you wouldn't need this--but this override of setOrthogonal
    // is here just because the user interface allows the link style
    // to be switched dynamically from Orthogonal to non-Orthogonal.
    // The overridden calculateStroke doesn't call removeAllPoints,
    // it just shifts the existing stroke points.  When the links
    // are orthogonal, extra stroke points are added, which causes
    // amusing behavior when the user switches back to non-orthogonal
    // style.
    public void setOrthogonal(boolean bOrtho) {
        if (bOrtho != isOrthogonal()) {
            removeAllPoints();
        }
        super.setOrthogonal(bOrtho);
    }

    //========================================
    public TcgComponent getComponent() {
        return mComponent;
    }

    public void releaseComponent() {
        mComponent = null;
    }
    
    //========================================
    /**
     * Used to paste from Clipboard to PdCanvas
     */
    public JGoObject copyObjectAndResetContextProperties(JGoCopyEnvironment env, Plan plan) {
        Link newObj = (Link)super.copyObject(env);
        TcgComponent component = null;
        try {
            component = TcgModelManager.getComponent("Clipboard", mComponentXml);
        } catch (Exception e) {
            mLog.log(Level.SEVERE, "copyObjectAndResetContextProperties failed", e);
            return newObj;
        }
        // new id and name for newObj.mComponent
        newObj.mComponent = plan.copyAndAddLink(component);
        return newObj;
    }
    
    
}