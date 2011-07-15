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

package org.netbeans.modules.debugger.jpda.visual.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import org.netbeans.spi.viewmodel.Models;

import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;


public class View extends TopComponent implements org.openide.util.HelpCtx.Provider {
    
    public static final String EVENTS_VIEW_NAME = "EventsView";
    
    private transient JComponent contentComponent;
    private transient ViewModelListener viewModelListener;
    private String name; // Store just the name persistently, we'll create the component from that
    private transient String helpID;
    private transient String propertiesHelpID;
    private transient String displayNameResource;
    private transient String toolTipResource;
    
    private View (String icon, String name, String helpID, String propertiesHelpID,
                  String displayNameResource, String toolTipResource) {
        setIcon (ImageUtilities.loadImage (icon));
        // Remember the location of the component when closed.
        putClientProperty("KeepNonPersistentTCInModelWhenClosed", Boolean.TRUE); // NOI18N
        this.name = name;
        this.helpID = helpID;
        this.propertiesHelpID = propertiesHelpID;
        this.displayNameResource = displayNameResource;
        this.toolTipResource = toolTipResource;
    }

    @Override
    protected String preferredID() {
        return this.getClass().getPackage().getName() + "." + name;
    }

    @Override
    protected void componentShowing () {
        super.componentShowing ();
        if (viewModelListener != null) {
            viewModelListener.setUp();
            return ;
        }
        JComponent buttonsPane;
        if (contentComponent == null) {
            setLayout (new BorderLayout ());
            contentComponent = new javax.swing.JPanel(new BorderLayout ());
            
            //tree = Models.createView (Models.EMPTY_MODEL);
            contentComponent.setName (NbBundle.getMessage (View.class, toolTipResource));
            add (contentComponent, BorderLayout.CENTER);  //NOI18N
            JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
            toolBar.setFloatable(false);
            toolBar.setRollover(true);
            toolBar.setBorderPainted(true);
            if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
                toolBar.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
            }
            toolBar.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 1,
                    javax.swing.UIManager.getDefaults().getColor("Separator.background")),
                    javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 1,
                    javax.swing.UIManager.getDefaults().getColor("Separator.foreground"))));
            add(toolBar, BorderLayout.WEST);
            buttonsPane = toolBar;
        } else {
            buttonsPane = (JComponent) ((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.WEST);
        }
        // <RAVE> CR 6207738 - fix debugger help IDs
        // Use the modified constructor that stores the propertiesHelpID
        // for nodes in this view
        // viewModelListener = new ViewModelListener (
        //     "ThreadsView",
        //     tree
        // );
        // ====
        viewModelListener = new ViewModelListener (
            name,
            contentComponent,
            buttonsPane,
            propertiesHelpID,
            getIcon()
        );
        // </RAVE>
    }
    
    @Override
    protected void componentHidden () {
        super.componentHidden ();
        if (viewModelListener != null) {
            viewModelListener.destroy ();
        }
    }
    
    // <RAVE>
    // Implement getHelpCtx() with the correct help ID
    @Override
    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx(helpID);
    }
    // </RAVE>
    
    @Override
    public int getPersistenceType () {
        return PERSISTENCE_ALWAYS;
    }
        
    @Override
    public boolean requestFocusInWindow () {
        super.requestFocusInWindow ();
        if (contentComponent == null) return false;
        if (contentComponent.getComponentCount() > 0) {
            return contentComponent.getComponent(0).requestFocusInWindow ();
        } else {
            return contentComponent.requestFocusInWindow ();
        }
    }

    @Override
    public void requestActive() {
        super.requestActive();
        if (contentComponent != null) {
            if (contentComponent.getComponentCount() > 0) {
                contentComponent.getComponent(0).requestFocusInWindow ();
            } else {
                contentComponent.requestFocusInWindow ();
            }
        }
    }
    
    @Override
    public String getName () {
        return NbBundle.getMessage (View.class, displayNameResource);
    }
    
    @Override
    public String getToolTipText () {
        return NbBundle.getMessage (View.class, toolTipResource);// NOI18N
    }
    
    @Override
    public Object writeReplace() {
        return new ResolvableHelper(name);
    }
     
    
    
    /**
     * The serializing class.
     */
    private static final class ResolvableHelper implements Externalizable {
        
        private String name;
        
        private static final long serialVersionUID = 1L;
        
        public ResolvableHelper(String name) {
            this.name = name;
        }
        
        public ResolvableHelper() {
            // Just for the purpose of deserialization
        }
        
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(name);
        }
        
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            name = (String) in.readObject();
        }
        
        public Object readResolve() {
            return View.getView(name);
        }
    }
    
    
    /** Creates the view. Call from the module layer only!
     * @deprecated Do not call.
     */
    public static synchronized TopComponent getEventsView() {
        return new View(
            "org/netbeans/modules/debugger/resources/breakpointsView/Breakpoint.gif",
            EVENTS_VIEW_NAME,
            "NetbeansDebuggerEventNode",
            null,
            "CTL_Events_view",
            "CTL_Events_view_tooltip"
        );
    }
    
    public static TopComponent getView(String viewName) {
        if (viewName.equals(EVENTS_VIEW_NAME)) {
            return getEventsView();
        }
        throw new IllegalArgumentException(viewName);
    }
    
}
