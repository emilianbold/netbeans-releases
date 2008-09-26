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

package org.netbeans.modules.uml.documentation.ui;

import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import java.awt.BorderLayout;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.SwingUtilities;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.requirementsframework.IRequirement;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 *  TopComponent for the Describe 6.0 documentation (JavaDoc) editor.
 *
 * @author  Darshan
 * @version 1.0
 */
public class DocumentationTopComponnet extends TopComponent implements PropertyChangeListener
{
    private PropertyChangeListener listener = null;;
    /**
     *  Serialization ID used by NetBeans. Note that this is faked, not
     * generated.
     */
    static final long serialVersionUID = 17754071377356384L;
    private static DocumentationTopComponnet mTopComponent = null;
    
    /**
     *  The Describe documentation editor ActiveX control wrapper class.
     */
    private static DocumentationPane pane;
    private static IElement current = null;
    private final String default_title =
            NbBundle.getMessage(DocumentationTopComponnet.class, "Pane.Documentation.Title");
    
    /**
     *  Creates a documentation editor top component; the document editor
     * control is instantiated on addNotify() and destroyed on removeNotify().
     */
    public DocumentationTopComponnet()
    {
        initializeTopComponent();
        setName(default_title);
        String desc = NbBundle.getMessage(DocumentationTopComponnet.class, "ACDS_DOCUMENTATION");
        getAccessibleContext().setAccessibleDescription(desc);
        
        listener = this;
        TopComponent.getRegistry().addPropertyChangeListener(
                WeakListeners.propertyChange(listener, TopComponent.getRegistry()));
        pane.addPropertyChangeListener(
                WeakListeners.propertyChange(listener, pane));
        processActivatedNodes();
    }
    
    private void initializeTopComponent()
    {
        if (pane == null)
        {
            pane = new DocumentationPane(false);
            setLayout(new BorderLayout());
            add(pane, BorderLayout.CENTER);
        }
    }
    
    
    public static void saveDocumentation()
    {
        if ( pane == null || !pane.isDirty())
            return;
        
        String body = pane.getTrimmedDocumentation();

        if (current != null)
        {
            current.setDocumentation(body);
            pane.setDocumentText(body);
        }
    }
    
    public boolean canClose()
    {
        saveDocumentation();
        return true;
    }
    
    
    public static synchronized DocumentationTopComponnet getDefault()
    {
        if (mTopComponent == null)
        {
            mTopComponent = new DocumentationTopComponnet();
        }
        return mTopComponent;
    }
    
    public static synchronized DocumentationTopComponnet getInstance()
    {
        if(mTopComponent == null)
        {
            TopComponent tc = WindowManager.getDefault().findTopComponent("documentation");
            if (tc != null)
                mTopComponent = (DocumentationTopComponnet)tc;
            else
                mTopComponent = new DocumentationTopComponnet();
        }
        
        return mTopComponent;
    }
    
    public int getPersistenceType()
    {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
    
    public String preferredID()
    {
        return getClass().getName();
    }
    
    public Image getIcon()
    {
        return ImageUtilities.loadImage(
                "org/netbeans/modules/uml/documentation/ui/resources/DocPane.gif"); // NOI18N
    }
    
    
    
    public HelpCtx getHelpCtx()
    {
        return new HelpCtx("DDEToolsDocumentation2_htm_wp1342319");
    }
    
    public void componentActivated()
    {   
        super.componentActivated();
        // Fixed iz=111959. request for the JTextPane to have the input focus 
        // when this component is activated.
        pane.getTextPane().requestFocusInWindow();
    }
    
    public void componentShowing()
    { 
        super.componentShowing();
        pane.addPropertyChangeListener(this);
    }
    
    public void componentHidden()
    {
        super.componentHidden();
        pane.removePropertyChangeListener(this);
    }
    
    
    private void clear()
    {
        pane.setDocumentText("");
        pane.setEnabled(false);
        setName(default_title);
        current = null;
    }
    
    
    private void processActivatedNodes()
    {
        
        org.openide.nodes.Node[] arr = TopComponent.getRegistry().getActivatedNodes();
        if(arr.length == 0 ||  arr.length > 1)
        {
            clear();
            return;
        }
        
        final IProjectTreeItem item = arr[0].getCookie(IProjectTreeItem.class);
        IPresentationElement pe = arr[0].getCookie(IPresentationElement.class);
        
        if(item == null && pe == null)
        {
            clear();
            return;
        }

        saveDocumentation();
        
        current = item != null ? item.getModelElement() : pe.getFirstSubject();
        
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                setName(current == null? default_title : current.toString() + " - " +  default_title);
            }
        });
        
        pane.setDocumentText(current == null? "" : current.getDocumentation());
        
        // todo: for IRequirement
    }
    
    
    
    /**
     * Listen for activated nodes property change events.
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        // save element doc before switching to another component, 79828
        
        if (evt.getPropertyName().equals( TopComponent.Registry.PROP_ACTIVATED_NODES ))
        {
            saveDocumentation();
            processActivatedNodes();
        }
        else if (evt.getPropertyName().equals(DocumentationPane.PROP_DIRTY))
        {
            if (current != null)
            {
                saveDocumentation();
                current.getProject().setDirty(true);
            }
        }
    }
}
