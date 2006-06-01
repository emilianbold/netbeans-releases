/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.welcome.content;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Map;
import javax.swing.Action;
import javax.swing.UIManager;
import org.openide.ErrorManager;
import org.openide.awt.HtmlBrowser;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 *
 * @author S. Aubrecht
 */
public class Utils {
    
    /** Creates a new instance of Utils */
    private Utils() {
    }

    public static Graphics2D prepareGraphics(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Map rhints = (Map)(Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints")); //NOI18N
        if( rhints == null && Boolean.getBoolean("swing.aatext") ) { //NOI18N
             g2.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
        } else if( rhints != null ) {
            g2.addRenderingHints( rhints );
        }
        return g2;
    }

    public static void showURL(String href) {
        try {
            HtmlBrowser.URLDisplayer displayer = HtmlBrowser.URLDisplayer.getDefault();
            if (displayer != null) {
                displayer.showURL(new URL(href));
            }
        } catch (Exception e) {}
    }

    static int getDefaultFontSize() {
        Integer customFontSize = (Integer)UIManager.get("customFontSize"); // NOI18N
        if (customFontSize != null) {
            return customFontSize.intValue();
        } else {
            Font systemDefaultFont = UIManager.getFont("TextField.font"); // NOI18N
            return (systemDefaultFont != null)
                ? systemDefaultFont.getSize()
                : 12;
        }
    }

    static String getFontName() {
        //#75759 Verdana not supported on Japanese locale
        return null;//Utilities.isWindows() ? "Verdana" : null; // NOI18N
    }

    public static Action findAction( String key ) {
        FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource(key);
        
        if (fo != null && fo.isValid()) {
            try {
                DataObject dob = DataObject.find(fo);
                InstanceCookie ic = (InstanceCookie) dob.getCookie(InstanceCookie.class);
                
                if (ic != null) {
                    Object instance = ic.instanceCreate();
                    if (instance instanceof Action) {
                        Action a = (Action) instance;
                        return a;
                    }
                }
            } catch (Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
                return null;
            }
        }
        return null;
    }

    public static Action createSampleProjectAction() {
        ClassLoader loader = (ClassLoader)Lookup.getDefault().lookup( ClassLoader.class );
        if( null == loader )
            loader = ClassLoader.getSystemClassLoader();
        try {
            Class clazz = Class.forName( "org.netbeans.modules.project.ui.actions.NewProject", true, loader ); // NOI18N
            Method getDefault = clazz.getMethod( "newSample", null ); // NOI18N
            Object newSample = getDefault.invoke( null, null );
            if( newSample instanceof Action )
                return (Action)newSample;
        } catch( Exception e ) {
            ErrorManager.getDefault().notify( ErrorManager.INFORMATIONAL, e );
        }
        return null;
    }
}
