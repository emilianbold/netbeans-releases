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

package org.netbeans.modules.html;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.openide.ErrorManager;
import org.openide.awt.HtmlBrowser;
import org.openide.cookies.ViewCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.SaveAsCapable;
import org.openide.loaders.UniFileLoader;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;

/** Object that represents one html file.
*
* @author Ian Formanek
*/
public class HtmlDataObject extends MultiDataObject implements CookieSet.Factory {

    public static final String PROP_ENCODING = "encoding"; // NOI18N
    public static final String DEFAULT_ENCODING = new InputStreamReader(System.in).getEncoding();
    static final long serialVersionUID =8354927561693097159L;
    
    /** New instance.
    * @param pf primary file object for this data object
    * @param loader the data loader creating it
    * @exception DataObjectExistsException if there was already a data object for it 
    */
    public HtmlDataObject(FileObject pf, UniFileLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        CookieSet set = getCookieSet();
        set.add(HtmlEditorSupport.class, this);
        set.add(ViewSupport.class, this);
        set.assign(SaveAsCapable.class, new SaveAsCapable() {
            public void saveAs( FileObject folder, String fileName ) throws IOException {
                HtmlEditorSupport es = getCookie( HtmlEditorSupport.class );
                es.saveAs( folder, fileName );
            }
        });
    }

    protected org.openide.nodes.Node createNodeDelegate () {
        DataNode n = new HtmlDataNode (this, Children.LEAF);
        n.setIconBaseWithExtension("org/netbeans/modules/html/htmlObject.png"); // NOI18N
        return n;
    }
    
    /** Creates new Cookie */
    public Node.Cookie createCookie(Class klass) {
        if (klass.isAssignableFrom (HtmlEditorSupport.class)) {
            HtmlEditorSupport es = new HtmlEditorSupport(this);
            return es;
        } else if (klass.isAssignableFrom (ViewSupport.class)) {
            return new ViewSupport(getPrimaryEntry());
        } else {
            return null;
        }
    }

    // Package accessibility for HtmlEditorSupport:
    CookieSet getCookieSet0() {
        return getCookieSet();
    }
    
    public String getFileEncoding() {
        //read the encoding property and if not empty return it
        String encoding = (String)getPrimaryFile().getAttribute(PROP_ENCODING);
        if (encoding != null) {
            return encoding;
        }
        //detect encoding from input stream
        InputStream is = null;
        try {
            is = getPrimaryFile().getInputStream();
            byte[] arr = new byte[4096];
            int len = is.read(arr);
            len = (len >= 0) ? len : 0;
            //check UTF-16 mark
            if (len > 1) {
                int mark = (arr[0]&0xff)*0x100+(arr[1]&0xff);
                if (mark == 0xfeff) {
                    encoding = "UTF-16"; // NOI18N
                }
            }
            if (encoding == null) {
                encoding = DEFAULT_ENCODING;
            }
            String txt = new String(arr, 0, len, encoding).toUpperCase();
            encoding = HtmlEditorSupport.findEncoding(txt);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, ex);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, ex);
            }
        }
        // if cannot detect return default
        if (encoding == null) {
            encoding = DEFAULT_ENCODING;
        }
        encoding.trim();
        return encoding;
    }
    
    public void setFileEncoding(String encoding) {
        encoding = encoding.trim();
        if(encoding.length() == 0) {
            encoding = null; //clear the property
        }
        try {
            getPrimaryFile().setAttribute(PROP_ENCODING, encoding);
        } catch(IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
        }
    }
    
    static final class ViewSupport implements ViewCookie {
        /** entry */
        private MultiDataObject.Entry primary;
        
        /** Constructs new ViewSupport */
        public ViewSupport(MultiDataObject.Entry primary) {
            this.primary = primary;
        }
        
         public void view () {
             try {
                 HtmlBrowser.URLDisplayer.getDefault ().showURL (primary.getFile ().getURL ());
             } catch (FileStateInvalidException e) {
             }
         }
    }
    
}
