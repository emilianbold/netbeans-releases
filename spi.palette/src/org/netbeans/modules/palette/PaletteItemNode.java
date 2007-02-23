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

package org.netbeans.modules.palette;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.beans.BeanInfo;
import java.io.IOException;
import java.util.ArrayList;
import org.openide.ErrorManager;
import org.openide.loaders.DataNode;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;




/**
 *
 * @author Libor Kotouc
 */
public final class PaletteItemNode extends FilterNode {
    
    private static final Node.PropertySet[] NO_PROPERTIES = new Node.PropertySet[0];

    private String name;
    private String bundleName;
    private String displayNameKey;
    private String className;
    private String tooltipKey; 
    private String icon16URL;
    private String icon32URL; 
    
    private String displayName;
    private String description;
    private Image icon16;
    private Image icon32;
    
    PaletteItemNode(DataNode original, 
                    String name, 
                    String bundleName, 
                    String displayNameKey, 
                    String className, 
                    String tooltipKey, 
                    String icon16URL, 
                    String icon32URL, 
                    InstanceContent content) 
    {
        super(original, Children.LEAF, new AbstractLookup(content));
        
        content.add( this );
        this.name = name;
        this.bundleName = bundleName; 
        this.displayNameKey = displayNameKey;
        this.className = className;
        this.tooltipKey = tooltipKey;
        this.icon16URL = icon16URL;
        this.icon32URL = icon32URL;
    }
 
    PaletteItemNode(DataNode original, 
                    String name, 
                    String displayName, 
                    String tooltip, 
                    String icon16URL, 
                    String icon32URL, 
                    InstanceContent content) 
    {
        super(original, Children.LEAF, new AbstractLookup(content));
        
        content.add( this );
        this.name = name;
        this.bundleName = bundleName; 
        assert null != displayName;
        this.displayName = displayName;
        this.description = tooltip;
        if( null == this.description )
            description = displayName;
        this.icon16URL = icon16URL;
        this.icon32URL = icon32URL;
    }
    
    public String getName() {
        return name;
    }

    public String getDisplayName() {
        if (displayName == null)
            displayName = _getDisplayName(bundleName, displayNameKey, className);
        
        return displayName;
    }

    public String getShortDescription() {
        if (description == null)
            description = _getShortDescription(bundleName, tooltipKey, className, displayNameKey);
        
        return description;
    }

    public Image getIcon(int type) {

        Image icon = null;
        
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            if (icon16 == null) {
                icon16 = _getIcon(icon16URL);
                if (icon16 == null)
                    icon16 = Utilities.loadImage("org/netbeans/modules/palette/resources/unknown16.gif"); // NOI18N
            }
            icon = icon16;
        }
        else if (type == BeanInfo.ICON_COLOR_32x32 || type == BeanInfo.ICON_MONO_32x32) {
            if (icon32 == null) {
                icon32 = _getIcon(icon32URL);
                if (icon32 == null)
                    icon32 = Utilities.loadImage("org/netbeans/modules/palette/resources/unknown32.gif"); // NOI18N
            }
            icon = icon32;
        }
        
        return icon;
    }
    
    public boolean canRename() {
        return false;
    }

    // TODO properties
    public Node.PropertySet[] getPropertySets() {
        return NO_PROPERTIES;
    }

    public Transferable clipboardCopy() throws IOException {

        ExTransferable t = ExTransferable.create( super.clipboardCopy() );
        
        Lookup lookup = getLookup();
        ActiveEditorDrop drop = (ActiveEditorDrop) lookup.lookup(ActiveEditorDrop.class);
        ActiveEditorDropTransferable s = new ActiveEditorDropTransferable(drop);
        t.put(s);

        //do not allow external DnD flavors otherwise some items may get interpreted
        //as an external file dropped into the editor window
        return new NoExternalDndTransferable( t );
    }

    public Transferable drag() throws IOException {
        return clipboardCopy();
    }

    private static class ActiveEditorDropTransferable extends ExTransferable.Single {
        
        private ActiveEditorDrop drop;

        ActiveEditorDropTransferable(ActiveEditorDrop drop) {
            super(ActiveEditorDrop.FLAVOR);
            
            this.drop = drop;
        }
               
        public Object getData () {
            return drop;
        }
        
    }
    
    public String _getDisplayName(
            String bundleName, 
            String displayNameKey, 
            String instanceName) 
    {

        String displayName = null;
        try {
            displayName = NbBundle.getBundle(bundleName).getString(displayNameKey);

            if (displayName == null && displayNameKey != null)
                displayName = displayNameKey;

            if (displayName == null) {//derive name from the instance name
                if (instanceName != null && instanceName.trim().length() > 0) {
                    int dotIndex = instanceName.lastIndexOf('.'); // NOI18N
                    displayName = instanceName.substring(dotIndex);
                }
            }

            if (displayName == null) // no name derived from the item
                displayName = name;

        }
        catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }

        return (displayName == null ? "" : displayName);
    }

    public String _getShortDescription(
            String bundleName, 
            String tooltipKey, 
            String instanceName, 
            String displayNameKey) 
    {

        String tooltip = null;
        try {
            tooltip = NbBundle.getBundle(bundleName).getString(tooltipKey);

            if (tooltip == null && tooltipKey != null)
                tooltip = tooltipKey;

            if (tooltip == null) {//derive name from instance name
                if (instanceName != null && instanceName.trim().length() > 0) {
                    int dotIndex = instanceName.indexOf('.'); // NOI18N
                    tooltip = instanceName.substring(0, dotIndex).replace('-', '.'); // NOI18N
                }
            }

            if (tooltip == null) // no tooltip derived from the item
                tooltip = _getDisplayName(bundleName, displayNameKey, instanceName);

        }
        catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }

        return (tooltip == null ? "" :  tooltip);
    }

    public Image _getIcon(String iconURL) {

        Image icon = null;
        try {
            icon = Utilities.loadImage(iconURL);
        }
        catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }

        return icon;
    }
    
    public HelpCtx getHelpCtx() {
        DataNode dn = (DataNode) getOriginal();
        Object helpId = dn.getDataObject().getPrimaryFile().getAttribute("helpId"); //NOI18N
        return (helpId == null ? super.getHelpCtx() : new HelpCtx(helpId.toString()));
    }
    
    /**
     * Transferable wrapper that does not allow DataFlavors for external drag and drop
     * (FileListFlavor and URI list flavors)
     */
    private static class NoExternalDndTransferable implements Transferable {
        private Transferable t;
        private DataFlavor uriListFlavor;
        public NoExternalDndTransferable( Transferable t ) {
            this.t = t;
        }
    
        public DataFlavor[] getTransferDataFlavors() {
            DataFlavor[] flavors = t.getTransferDataFlavors();
            if( t.isDataFlavorSupported( DataFlavor.javaFileListFlavor ) 
                || t.isDataFlavorSupported( getUriListFlavor() ) ) {
                ArrayList<DataFlavor> tmp = new ArrayList<DataFlavor>( flavors.length );
                for( int i=0; i<flavors.length; i++ ) {
                    if( isDataFlavorSupported( flavors[i] ) )
                        tmp.add( flavors[i] );
                }
                flavors = tmp.toArray( new DataFlavor[tmp.size()] );
            }
            return flavors;
        }

        public boolean isDataFlavorSupported( DataFlavor flavor ) {
            if( DataFlavor.javaFileListFlavor.equals( flavor ) || getUriListFlavor().equals( flavor ) )
                return false;
            return t.isDataFlavorSupported(flavor);
        }

        public Object getTransferData( DataFlavor flavor ) throws UnsupportedFlavorException, IOException {
            if( !isDataFlavorSupported(flavor) )
                throw new UnsupportedFlavorException( flavor );
            return t.getTransferData( flavor );
        }
        
        private DataFlavor getUriListFlavor () {
            if( null == uriListFlavor ) {
                try {
                    uriListFlavor = new DataFlavor("text/uri-list;class=java.lang.String");
                } catch (ClassNotFoundException ex) {
                    //cannot happen
                    throw new AssertionError(ex);
                }
            }
            return uriListFlavor;
        }
    }
}
