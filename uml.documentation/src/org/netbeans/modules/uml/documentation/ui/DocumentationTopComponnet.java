/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.uml.documentation.ui;

import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import java.awt.BorderLayout;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.SwingUtilities;
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
    private static IProjectTreeItem current = null;
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
        if ("".equals(body))
            return;
        
        if (current != null)
        {
            if (current.getModelElement() != null)
            {
                current.getModelElement().setDocumentation(body);
            }
            else if (current.getDiagram() != null)
            {
                current.getDiagram().setDocumentation(body);
            }
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
        return Utilities.loadImage(
                "org/netbeans/modules/uml/documentation/ui/resources/DocPane.gif"); // NOI18N
    }
    
    
    
    public HelpCtx getHelpCtx()
    {
        return new HelpCtx("DDEToolsDocumentation2_htm_wp1342319");
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
        
        final IProjectTreeItem item = (IProjectTreeItem)arr[0].getCookie(IProjectTreeItem.class);
        if(item == null)
        {
            clear();
            return;
        }

        saveDocumentation();
        
        current = item;
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                setName(item.getItemText() + " - " +  default_title);
            }
        });
        
        if (current.getDiagram() != null)
        {
            pane.setDocumentText(item.getDiagram().getDocumentation());
        }
        else if (current.getModelElement() != null)
        {
            pane.setDocumentText(item.getModelElement().getDocumentation());
        }
        else
        {
            clear();
        }
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
