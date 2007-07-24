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
/*
 * CodeClipItemNode.java
 *
 * Created on July 27, 2006, 10:16 AM
 *
 * This node represents the dataobject in the palette
 *
 * @author Joelle Lam <joelle.lam@sun.com>
 * @date 08/20/2006
 */

package org.netbeans.modules.visualweb.palette.codeclips;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.beans.BeanInfo;
import java.io.IOException;
import java.util.MissingResourceException;
import javax.swing.text.JTextComponent;
import org.openide.ErrorManager;
import org.openide.cookies.EditCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataNode;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

public final class CodeClipItemNode extends FilterNode implements EditCookie {

    private static final Node.PropertySet[] NO_PROPERTIES = new Node.PropertySet[0];

    private String name;
    private String bundleName;
    private String displayNameKey;
    private String tooltipKey;
    private String icon16URL;
    private String icon32URL;
    private String displayName;
    private String description;
    private Image icon16;
    private Image icon32;
    private String body;
    private DataNode dataNode;
    private FileObject fileObj;


    /*
     * Class constructor
     *
     * @param
     */
    CodeClipItemNode(DataNode dataNode,
            String name,
            String bundleName,
            String displayNameKey,
            String tooltipKey,
            String icon16URL,
            String icon32URL,
            String body,
            InstanceContent content) {


        super(dataNode, Children.LEAF, new AbstractLookup(content));


//        DataObject dataObj = dataNode.getDataObject();
        fileObj  = (dataNode.getDataObject()).getPrimaryFile();

        this.dataNode = dataNode;
        content.add( this );
        this.name = name;
        this.bundleName = bundleName;
        this.displayNameKey = displayNameKey;
        this.tooltipKey = tooltipKey;
        this.icon16URL = icon16URL;
        this.icon32URL = icon32URL;
        this.body = body;

    }

    public String getName() {
        return getDisplayName();
    }

    public void setName(String s) {

        if ( !fileObj.canWrite() ) {
            System.out.println("Cannot write to file.");
            return;
        }
//        File file = FileUtil.toFile(fileObj);
        try {
            this.destroy();
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }

        displayNameKey = s;

        try {
            CodeClipUtilities.createCodeClipFile(fileObj.getParent(), body, displayNameKey, this.bundleName, tooltipKey);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }

    }

    public String getDisplayName() {
        if (displayName == null)
            displayName = _getDisplayName(bundleName, displayNameKey);

        return displayName;
    }

    public String getShortDescription() {
        if (description == null && bundleName !=  null)
            description = _getShortDescription(bundleName, tooltipKey, displayNameKey);

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
        } else if (type == BeanInfo.ICON_COLOR_32x32 || type == BeanInfo.ICON_MONO_32x32) {
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
        return true;
    }
    
    
    // TODO properties
//    public Node.PropertySet[] getPropertySets() {
//        return NO_PROPERTIES;
//    }
    
    public Transferable clipboardCopy() throws IOException {
        
        ExTransferable t = ExTransferable.create( super.clipboardCopy() );
        
        Lookup lookup = getLookup();
        
        // Can we create our own version of Active Editor Drop here?
        ActiveEditorDrop drop = (ActiveEditorDrop) lookup.lookup(ActiveEditorDrop.class);
        ActiveEditorDropTransferable s = new ActiveEditorDropTransferable(drop);
        t.put(s);
        
        return t;
    }
    
    public Transferable drag() throws IOException {
        Transferable t = clipboardCopy();
        return t;
    }
    
    private static class ActiveEditorDropTransferable extends ExTransferable.Single {
        
        private ActiveEditorDrop drop;
        
        ActiveEditorDropTransferable(ActiveEditorDrop drop) {
            super(ActiveEditorDrop.FLAVOR);
            
            this.drop = drop;
        }
        
        public Object getData() {
            return drop;
        }
        
    }
    
    public String _getDisplayName(
            String bundleName,
            String displayNameKey) {
        String displayName = null;
        if( bundleName != null ) {
            try {
                displayName = NbBundle.getBundle(bundleName).getString(displayNameKey);
            } catch (MissingResourceException mre) {
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                displayName = displayNameKey;
            }
        } else {
            displayName = displayNameKey;
        }
        
        return (displayName);
    }
    
    public String _getShortDescription(
            String bundleName,
            String tooltipKey,
            String displayNameKey) {
        
        String tooltip = null;
        if ( tooltipKey != null ) {
            try {
                tooltip = NbBundle.getBundle(bundleName).getString(tooltipKey);
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        } else if (body != null) {
            
            /* This will make sure the tooltips.  I chose to do this here even
             * thou it means that sometimes it may happen twice due to
             * performance.  I prefer that the palette not be dependent on loading
             * all of the body comments.  By doing this, it is only loaded when
             * the category is opened. I wish there was a better way to do this.
             * I would suggest that we never allow the user to use the body as
             * the tooltip, but I guess that is preference.
             */
            tooltip = CodeClipUtilities.fillFromBundle(body, bundleName);
            if (tooltip.length() > 200) {
                tooltip = tooltip.substring(0, 200);
            }
            
            tooltip = tooltip.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br>");
            
            /*
             * This is a temporary workaround.
             * For some reason if the tooltip bigs with / the first line
             * is removed altogether form the tooltip.  Not sure where this is
             * happening.
             */
            if( tooltip.startsWith("//")) {
                tooltip = " <br>" + tooltip;
            }
            
            tooltip = "<html>".concat(tooltip).concat("</html>");
            
        } else {
            // no tooltip derived from the item
            tooltip = _getDisplayName(bundleName, displayNameKey);
        }
        
        return tooltip;
        
    }
    
    public Image _getIcon(String iconURL) {
        
        Image icon = null;
        try {
            icon = Utilities.loadImage(iconURL);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        
        
        return icon;
    }
    
    public String getBody() {
        return body;
    }
    
    public void setBody( String body ) {
        this.body = body;
    }
    
    
    /* 
     * Edit this codeclip
     * @param none
     */    
    public void edit() {
        
        CodeSnippetViewer snippetViewer = new CodeSnippetViewer(this.body, this.getDisplayName());
        snippetViewer.setVisible(true);
        String text = snippetViewer.getDispText();
        try {
            CodeClipUtilities.createCodeClipFile(fileObj.getParent(), text, displayNameKey, this.bundleName, tooltipKey);
            this.destroy();
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }        
//        this.setBody(text);
        
    }
    
    /* 
     * Drop the codeclip on the given text component target 
     * @param target JTextComponent or the targeted editor component
     */
    public void drop(JTextComponent target){

//        if (target == null) {
//            String msg = NbBundle.getMessage(CodeClipItemNode.class, "MSG_ErrorNoFocusedDocument");
//            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE));
//            return;
//        }

        ActiveEditorDrop drop = (ActiveEditorDrop) getLookup().lookup(ActiveEditorDrop.class);
        drop.handleTransfer(target);

    }
    
    
    
}
