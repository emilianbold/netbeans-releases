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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.spi.multiview;

import java.util.TooManyListenersException;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import org.netbeans.core.multiview.MultiViewElementCallbackDelegate;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;


    /** Requester type of class, allowing implementors of MultiViewElement
     * to send requests back to enclosing component and window system. Enclosing
     * component or other part of window system will set the instance of this class to elements upon
     * instantiation or deserialization of the element to receive requests properly. 
     * Implementors of MultiViewElement shall not attempt to serialize
     * the passed callback instance.
     * 
     */
    public final class MultiViewElementCallback {
        
        static {
            AccessorImpl.createAccesor();
        }
        
        private MultiViewElementCallbackDelegate delegate;
        
        MultiViewElementCallback(MultiViewElementCallbackDelegate del) {
            delegate = del;
        }
        
        /** Activates this multi view element in enclosing multi view component
         * context, if enclosing multi view top component is opened.
         */
        public void requestActive() {
            delegate.requestActive();
        }

        /** Selects this multi view element in enclosing component context,
         * if component is opened, but does not activate it 
         * unless enclosing component is in active mode already.
         */
        public void requestVisible () {
            delegate.requestVisible();
        }
        
        /**
         * Creates the default TopComponent actions as defined by the Window System.
         * Should be used by the element when constructing it's own getActions() return value.
         */
        public Action[] createDefaultActions() {
            return delegate.createDefaultActions();
        }
        
        /**
         * Update the multiview's topcomponent title.
         */
        public void updateTitle(String title) {
            delegate.updateTitle(title);
        }
        
        /**
         * Element can check if it's currently the selected element.
         */
        public boolean isSelectedElement() {
            return delegate.isSelectedElement();
        }
        
        /**
         * Returns the enclosing Multiview's topcomponent.
         */
        public TopComponent getTopComponent() {
            return delegate.getTopComponent();
        }
        
    } // end of ActionRequestListener
    
