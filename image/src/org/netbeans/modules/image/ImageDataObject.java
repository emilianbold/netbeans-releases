/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.image;


import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.openide.actions.OpenAction;
import org.openide.cookies.CompilerCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.TopManager;
import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/** 
 * Object that represents one file containing an image.
 * @author Petr Hamernik, Jaroslav Tulach, Ian Formanek
 */
public class ImageDataObject extends MultiDataObject implements CookieSet.Factory {
    
    /** Generated serialized version UID. */
    static final long serialVersionUID = -6035788991669336965L;

    /** Base for image resource. */
    private static final String IMAGE_ICON_BASE = "org/netbeans/modules/image/imageObject"; // NOI18N
    
    /** Open support for this image data object. */
    private transient ImageOpenSupport openSupport;

    
    /** Constructor.
     * @param pf primary file object for this data object
     * @param loader the data loader creating it
     * @exception DataObjectExistsException if there was already a data object for it 
     */
    public ImageDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        
        getCookieSet().add(ImageOpenSupport.class, this);
    }


    /** Implements <code>CookieSet.Factory</code> interface. */
    public Node.Cookie createCookie(Class clazz) {
        if(clazz.isAssignableFrom(ImageOpenSupport.class))
            return getOpenSupport();
        else
            return null;
    }
    
    /** Gets image open support. */
    private ImageOpenSupport getOpenSupport() {
        if(openSupport == null) {
            synchronized(this) {
                if(openSupport == null)
                    openSupport = new ImageOpenSupport(getPrimaryEntry());
            }
        }
        
        return openSupport;
    }
    
    /** Help context for this object.
     * @return the help context
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ImageDataObject.class);
    }

    /** Get a URL for the image.
     * @return the image url
     */
    URL getImageURL() {
        try {
            return getPrimaryFile().getURL();
        } catch (FileStateInvalidException ex) {
            return null;
        }
    }

    /** Get image data for the image.
     * @return the image data
     */
    public byte[] getImageData() {
        try {
            FileObject fo = getPrimaryFile();
            byte[] imageData = new byte[(int)fo.getSize()];
            BufferedInputStream in = new BufferedInputStream(fo.getInputStream());
            in.read(imageData, 0, (int)fo.getSize());
            in.close();
            return imageData; 
        } catch(IOException ioe) {
            return new byte[0];
        }
    }


    /** Create a node to represent the image. Overrides superclass method.
     * @return node delegate */
    protected Node createNodeDelegate () {
        return new ImageNode(this);
    }
    
    
    /** Node representing <code>ImageDataObject</code>. */
    private static final class ImageNode extends DataNode {
        /** Constructs image node. */
        public ImageNode(ImageDataObject obj) {
            super(obj, Children.LEAF);
            setIconBase(IMAGE_ICON_BASE);
            setDefaultAction (SystemAction.get (OpenAction.class));
        }
        
        /** Creates property sheet. Ovrrides superclass method. */
        protected Sheet createSheet() {
            Sheet s = super.createSheet();
            Sheet.Set ss = s.get(Sheet.PROPERTIES);
            if (ss == null) {
                ss = Sheet.createPropertiesSet();
                s.put(ss);
            }
            ss.put(new ThumbnailProperty(getDataObject()));
            return s;
        }
        

        /** Property representing for thumbanil property in the sheet. */
        private static final class ThumbnailProperty extends PropertySupport.ReadOnly {
            /** (Image) data object associated with. */
            private final DataObject obj;
            
            /** Constructs property. */
            public ThumbnailProperty(DataObject obj) {
                super("thumbnail", Icon.class, // NOI18N
                    NbBundle.getMessage(ImageDataObject.class, "PROP_Thumbnail"),
                    NbBundle.getMessage(ImageDataObject.class, "HINT_Thumbnail"));
                this.obj = obj;
            }
            
            /** Gets value of property. Overrides superclass method. */
            public Object getValue() throws InvocationTargetException {
                try {
                    return new ImageIcon(obj.getPrimaryFile().getURL());
                } catch (FileStateInvalidException fsie) {
                    throw new InvocationTargetException(fsie);
                }
            }
            
            /** Gets property editor. */
            public PropertyEditor getPropertyEditor() {
                return new ThumbnailPropertyEditor();
            }
            
            
            /** Property editor for thumbnail property. */
            private final class ThumbnailPropertyEditor extends PropertyEditorSupport {
                /** Overrides superclass method.
                 * @return <code>true</code> */
                public boolean isPaintable() {
                    return true;
                }
                
                /** Patins thumbanil of the image. Overrides superclass method. */
                public void paintValue(Graphics g, Rectangle r) {
                    ImageIcon icon = null;
                    
                    try {
                        icon = (ImageIcon)ThumbnailProperty.this.getValue();
                    } catch(InvocationTargetException ioe) {
                        if(Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                            TopManager.getDefault().getErrorManager().notify(ioe);
                        }
                    }
                    
                    if(icon != null) {
                        int iconWidth = icon.getIconWidth();
                        int iconHeight = icon.getIconHeight();
                        

                        // Shrink image if necessary.
                        double scale = (double)iconWidth / iconHeight;
                        
                        if(iconWidth > r.width) {
                            iconWidth = r.width;
                            iconHeight = (int) (iconWidth / scale);
                        }

                        if(iconHeight > r.height) {
                            iconHeight = r.height;
                            iconWidth = (int) (iconHeight * scale);
                        }
                        
                        // Try to center it if it fits, else paint as much as possible.
                        int x;
                        if(iconWidth < r.x) {
                            x = (r.x - iconWidth) / 2;
                        } else {
                            x = 5; // XXX Indent.
                        }
                        
                        int y;
                        if(iconHeight < r.y) {
                            y = (r.y - iconHeight) / 2;
                        } else {
                            y = 0;
                        }
                        
                        Graphics g2 = g.create(r.x, r.y, r.width, r.height);
                        g.drawImage(icon.getImage(), x, y, iconWidth, iconHeight, null);
                    }
                }

                /** Overrides superclass method.
                 * @return <code>null</code> */
                public String getAsText() {
                    return null;
                }
            } // End of class ThumbnailPropertyEditor.
        } // End of class ThumbnailProperty.
    } // End of class ImageNode.

}
