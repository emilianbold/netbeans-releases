/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.compapp.casaeditor.graph;

import java.awt.Point;
import java.awt.Rectangle;
import org.netbeans.api.visual.widget.*;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;


/**
 * This class represents a widget that has an optional error badge.
 *
 * @author jqian
 */
public abstract class ErrableWidget extends Widget {
    
    private DependenciesRegistry mDependenciesRegistry = new DependenciesRegistry(this);
    
    private Widget mErrorBadgeWidget;
    
    public ErrableWidget(Scene scene) {
        super(scene);                
    }
            
    /**
     * Sets the error/warning message.
     * 
     * @param message   error/warning message, 
     *                  <code>null</code> if there is no error/warning at all.
     * @param isError   <code>true</code> if it is an error; 
     *                  <code>false</code> if it is an warning.
     */
    public void setError(String message, boolean isError) {
        if (mErrorBadgeWidget == null) {
            if (message == null) {
                return;
            }
            
            mErrorBadgeWidget = new ImageWidget(getScene(), 
                    isError ? RegionUtilities.IMAGE_ERROR_BADGE_ICON :
                        RegionUtilities.IMAGE_WARNING_BADGE_ICON);
        }
        
        mErrorBadgeWidget.setToolTipText(message);        
        
        LayerWidget glassLayer = ((CasaModelGraphScene)getScene()).getGlassLayer();
        if (message == null) {
            mErrorBadgeWidget.removeFromParent();
            glassLayer.repaint();
        } else {
            if (!glassLayer.getChildren().contains(mErrorBadgeWidget)) {
                glassLayer.addChild(mErrorBadgeWidget);
                revalidate();
            }
        }
    }
    
    @Override
    protected void notifyAdded() {
        super.notifyAdded();              
        
        // Update the error badge location if the widget moves.
        Widget.Dependency errorDependency = new Widget.Dependency() {
            public void revalidateDependency() {
                if (hasPreferredLocation() && getPreferredLocation() == null ||
                        getBounds() == null ||
                        getParentWidget() == null) {
                    return;
                }
                               
                if (mErrorBadgeWidget != null && 
                        mErrorBadgeWidget.getToolTipText() != null) {

                    Point nodeSceneLocation = getParentWidget().convertLocalToScene(
                            hasPreferredLocation() ? getPreferredLocation() :
                                new Point(0, 0));
                               
                    int x = nodeSceneLocation.x + getErrorBadgeDeltaX();
                    int y = nodeSceneLocation.y + getErrorBadgeDeltaY();
                    mErrorBadgeWidget.setPreferredLocation(new Point(x, y));
                }
            }
        };
        getDependenciesRegistry().registerDependency(errorDependency);
    }
    
    @Override
    protected void notifyRemoved() {
        super.notifyRemoved();
        
        getDependenciesRegistry().removeAllDependencies();   
        
        if (mErrorBadgeWidget != null) {
            mErrorBadgeWidget.removeFromParent();
        }
    }
    
    /**
     * Initialization for the glass layer above the widget.
     * @param layer the glass layer
     */
    public void initializeGlassLayer(LayerWidget layer) {
    }
       
    protected DependenciesRegistry getDependenciesRegistry() {
        return mDependenciesRegistry;
    }   
    
    /**
     * Checks whether this widget has a preferred location.
     */
    protected abstract boolean hasPreferredLocation();
    
    /**
     * Gets the delta x for the error badge relative to the widget's 
     * left top corner.
     */
    protected abstract int getErrorBadgeDeltaX();
    
    /**
     * Gets the delta y for the error badge relative to the widget's 
     * left top corner.
     */
    protected abstract int getErrorBadgeDeltaY();
    
}
