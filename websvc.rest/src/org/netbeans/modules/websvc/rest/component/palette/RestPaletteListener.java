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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.rest.component.palette;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.Method;
import javax.swing.text.BadLocationException;
import org.netbeans.spi.palette.PaletteController;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;

/**
 *
 * @author Ayub Khan
 */
public class RestPaletteListener implements PropertyChangeListener {
    
    public static final String REST_TEMPLATE = "@UriTemplate";
    
    PaletteController pc = null;
    
    public void propertyChange(PropertyChangeEvent evt) {
        System.out.println("evt: "+evt.getPropertyName()+" "+
                evt.getOldValue()+" "+evt.getNewValue());
        if(evt.getPropertyName().equals(TopComponent.Registry.PROP_ACTIVATED )) {
            if(pc == null)
                pc = RestPaletteFactory.createPalette();
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
        return true; /*
        try {
            if(d == null)
                return false;
            EditorCookie ec = d.getCookie(EditorCookie.class);
            if(ec == null)
                return false;
            javax.swing.text.Document doc = ec.openDocument();
            if (doc != null) {
                return doc.getText(0, doc.getLength()).
                        indexOf(REST_TEMPLATE) != -1;
            }
        } catch (IOException ex) {
        } catch (BadLocationException ex) {
        }
        return false; */
    }
}
