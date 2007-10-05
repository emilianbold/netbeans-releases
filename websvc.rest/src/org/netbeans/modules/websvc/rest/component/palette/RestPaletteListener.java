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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.rest.component.palette;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.Method;
import javax.swing.text.BadLocationException;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.spi.palette.PaletteController;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;
import org.xml.sax.SAXException;

/**
 *
 * @author Ayub Khan
 */
public class RestPaletteListener implements PropertyChangeListener, FileChangeListener {
    
    public static final String REST_TEMPLATE = "@UriTemplate";      //NOI18N
    
    public static final String HTTP_METHOD = "@HttpMethod";         //NOI18N
    
    PaletteController pc = null;
    
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(TopComponent.Registry.PROP_ACTIVATED )) {
            if(pc == null)
                pc = RestPaletteFactory.createPalette();
            if(pc == null)
                return;
            TopComponent activeTc = TopComponent.getRegistry().getActivated();
            if( null != activeTc ) {
                DataObject d = activeTc.getLookup().lookup(DataObject.class);
                if(d != null)
                    associatePalette(d, pc);
            }
        }
    }
    
    public PaletteController getController() {
        return pc;
    }
    
    void associatePalette(DataObject d, PaletteController pc) {
        if( isRestJavaFile( d ) ) {
            CookieSet cookies = getCookieSet( d );
            cookies.assign( PaletteController.class, pc );
        }
    }
    
    private CookieSet getCookieSet(DataObject d) {
        try {
            Method[] methods = MultiDataObject.class.getDeclaredMethods();
            for( Method m : methods ) {
                if( "getCookieSet".equals( m.getName() ) ) {
                    m.setAccessible( true );
                    Object res = m.invoke( d );
                    if( res instanceof CookieSet )
                        return (CookieSet)res;
                }
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    private boolean isRestJavaFile(DataObject d) {
        try {
            if(d == null || ! "java".equals(d.getPrimaryFile().getExt())) //NOI18N
                return false;
            EditorCookie ec = d.getCookie(EditorCookie.class);
            if(ec == null)
                return false;
            javax.swing.text.Document doc = ec.getDocument();
            if (doc != null) {
                String docText = doc.getText(0, doc.getLength());
                
                return (docText.indexOf(REST_TEMPLATE) != -1) ||
                        (docText.indexOf(HTTP_METHOD) != -1);
            }
        } catch (BadLocationException ex) {
        }
        return false;
    }

    public void fileFolderCreated(FileEvent fe) {
        //ignore
    }

    public void fileDataCreated(FileEvent fe) {
        try {
            RestPaletteFactory.createPaletteItemFromComponent(fe.getFile());
        } catch (IOException ex) {
        } catch (ParserConfigurationException ex) {
        } catch (SAXException ex) {
        }
    }

    public void fileChanged(FileEvent fe) {
        try {
            RestPaletteFactory.createPaletteItemFromComponent(fe.getFile());
        } catch (IOException ex) {
        } catch (ParserConfigurationException ex) {
        } catch (SAXException ex) {
        }
    }

    public void fileDeleted(FileEvent fe) {
        RestPaletteFactory.updateAllPaletteItems();
    }

    public void fileRenamed(FileRenameEvent fe) {
        //ignore
    }

    public void fileAttributeChanged(FileAttributeEvent fe) {
        //ignore
    }
}
