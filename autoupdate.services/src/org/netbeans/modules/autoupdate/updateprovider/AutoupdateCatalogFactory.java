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

package org.netbeans.modules.autoupdate.updateprovider;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.autoupdate.UpdateUnitProvider.CATEGORY;
import org.netbeans.spi.autoupdate.UpdateProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jiri Rechtacek
 */
public class AutoupdateCatalogFactory {
    private static Logger err = Logger.getLogger ("org.netbeans.modules.autoupdate.updateproviders.AutoupdateCatalogFactory");
    
    /** Creates a new instance of CreateNewUpdatesProvider */
    private AutoupdateCatalogFactory () {
    }
    
    private static final String UPDATE_VERSION_PROP = "netbeans.autoupdate.version"; // NOI18N
    private static final String UPDATE_VERSION = "1.23"; // NOI18N
    private static final String IDE_HASH_CODE = "netbeans.hash.code"; // NOI18N
    private static final String SYSPROP_COUNTRY = "netbeans.autoupdate.country"; // NOI18N
    private static final String SYSPROP_LANGUAGE = "netbeans.autoupdate.language"; // NOI18N
    private static final String SYSPROP_VARIANT = "netbeans.autoupdate.variant"; // NOI18N
    
    public static final String ORIGINAL_URL = "originalUrl"; // NOI18N
    public static final String ORIGINAL_DISPLAY_NAME = "originalDisplayName"; // NOI18N
    public static final String ORIGINAL_ENABLED = "originalEnabled"; // NOI18N
    public static final String ORIGINAL_CATEGORY_NAME = "originalCategoryName"; // NOI18N
    
    public static UpdateProvider createUpdateProvider (FileObject fo) {
        String sKey = (String) fo.getAttribute ("url_key"); // NOI18N
        String remoteBundleName = (String) fo.getAttribute ("SystemFileSystem.localizingBundle"); // NOI18N
        assert remoteBundleName != null : "remoteBundleName should found in fo: " + fo;
        
        ResourceBundle bundle = NbBundle.getBundle (remoteBundleName);
        URL url = null;
        if (sKey != null) {
            String localizedValue = null;
            try {
                localizedValue = bundle.getString (sKey);
                url = new URL (localizedValue);
            } catch (MissingResourceException mre) {
                assert false : bundle + " should contain key " + sKey;
            } catch (MalformedURLException urlex) {
                assert false : "MalformedURLException when parsing name " + localizedValue;
            }
        } else {
            assert false : "url attrib ute is not supported.";
            Object o = fo.getAttribute("url"); // NOI18N
            try {
                if (o instanceof String) {
                    url = new URL ((String) o);
                } else {
                    url = (URL) o;
                }
            } catch (MalformedURLException urlex) {
                err.log (Level.INFO, urlex.getMessage (), urlex);
            }
        }
        url = modifyURL (url);
        String categoryName = (String) fo.getAttribute ("category"); // NOI18N    
        CATEGORY category = (categoryName != null) ? CATEGORY.valueOf(categoryName) : CATEGORY.COMMUNITY;
        AutoupdateCatalogProvider au_catalog = new AutoupdateCatalogProvider (sKey, displayName (fo), url, category);
        
        Preferences providerPreferences = getPreferences ().node (sKey);
        providerPreferences.put (ORIGINAL_URL, url.toExternalForm ());
        providerPreferences.put (ORIGINAL_DISPLAY_NAME, au_catalog.getDisplayName ());
        providerPreferences.put (ORIGINAL_CATEGORY_NAME, au_catalog.getCategory().name());        
        Boolean en = (Boolean) fo.getAttribute("enabled"); // NOI18N        
        if (en != null) {
            providerPreferences.putBoolean (ORIGINAL_ENABLED, en);
        }

        return au_catalog;
    }
    
    @Deprecated
    public static Object createXMLAutoupdateType (FileObject fo) throws IOException {
        return createUpdateProvider (fo);
    }
    
    // helper methods
    private static String displayName (FileObject fo) {
        String displayName = null;
        
        if (fo != null) {
            try {
                FileSystem fs = fo.getFileSystem ();
                FileSystem.Status s = fs.getStatus ();
                String x = s.annotateName ("", Collections.singleton (fo)); // NOI18N
                if (!x.equals ("")) { // NOI18N
                    displayName = x;
                }
            } catch (FileStateInvalidException e) {
                // OK, never mind.
            }
        }
        if (displayName == null) {
            displayName = NbBundle.getBundle (AutoupdateCatalogFactory.class).getString ("CTL_CatalogUpdatesProviderFactory_DefaultName");
        }
        
        return displayName;
    }
    
    private  static Preferences getPreferences() {
        return NbPreferences.root ().node ("/org/netbeans/modules/autoupdate"); // NOI18N
    }    
    
    private static URL modifyURL (URL original) {
        URL updateURL = null;
        
        if ( System.getProperty (UPDATE_VERSION_PROP) == null ) {
            System.setProperty (UPDATE_VERSION_PROP, UPDATE_VERSION);
        }
        
        if (System.getProperty (IDE_HASH_CODE) == null) {
            String id = getPreferences ().get ("ideIdentity", null);
            if (id == null) {
                // can ignore it, property used only for logging purposes
                Logger.getLogger(AutoupdateCatalogFactory.class.getName()).warning("Property PROP_IDE_IDENTITY hasn't been initialized yet."); // NOI18N
                id = "";
            }
            String prefix = NbBundle.getBundle (AutoupdateCatalogFactory.class).getString ("URL_Prefix_Hash_Code"); // NOI18N
            System.setProperty (IDE_HASH_CODE, "".equals (id) ? prefix + "0" : prefix + id); // NOI18N
        }
        
        try {
            updateURL = new URL (encode (replace (original.toString ())));
        } catch (MalformedURLException urlex) {
            err.log (Level.INFO, urlex.getMessage(), urlex);
        }

        return updateURL;
        
    }
    
    private static String encode (String stringURL) {
	String rval = stringURL;
            int q = stringURL.indexOf ('?');
            if(q > 0) {
		StringBuffer buf = new StringBuffer (stringURL.substring (0, q+1));
		StringTokenizer st = new StringTokenizer (stringURL.substring (q + 1), "&");
		while(st.hasMoreTokens ()) {
                    String a = st.nextToken ();
                    try {
                        int ei = a.indexOf ('=');
                        if(ei < 0) {
                            buf.append(URLEncoder.encode (a, "UTF-8"));
                        } else {
                            buf.append(URLEncoder.encode (a.substring(0, ei), "UTF-8"));
                            buf.append ('=');
                            String tna = a.substring( ei+1);
                            int tni = tna.indexOf ("%");
                            if( tni < 0) {
                                buf.append (URLEncoder.encode (tna, "UTF-8"));
                            } else {
                                buf.append (URLEncoder.encode (tna.substring (0, tni), "UTF-8"));
                                buf.append ('%');
                                buf.append (URLEncoder.encode (tna.substring (tni+1), "UTF-8"));
                            }
                        }
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger(AutoupdateCatalogFactory.class.getName()).log(Level.INFO, ex.getMessage(), ex);
                    }
                    if (st.hasMoreTokens ())
                        buf.append ('&');
                }
                rval = buf.toString ();
            }

	return rval;
    }
    
    private static String replace (String string) {

        // First of all set our system properties
        setSystemProperties ();

        if ( string == null )
            return null;

        StringBuffer sb = new StringBuffer ();

        int index, prevIndex;
        index = prevIndex = 0;
        while( ( index = string.indexOf( "{", index )) != -1 && index < string.length() - 1) { // NOI18N

            if ( string.charAt( index + 1 ) == '{' || string.charAt( index + 1 ) != '$'  ) {
                ++index;
                continue;
            }

            sb.append( string.substring (prevIndex, index) );
            int endBracketIndex = string.indexOf ("}", index); // NOI18N
            if (endBracketIndex != -1) {
                String whatToReplace = string.substring (index + 2, endBracketIndex);
                sb.append (getReplacement (whatToReplace));
            }
            prevIndex = endBracketIndex == -1 ? index + 2 : endBracketIndex + 1;
            ++index;
        }

        if ( prevIndex < string.length () - 1 )
            sb.append( string.substring (prevIndex));

        return sb.toString ();
    }

    private static void setSystemProperties() {
            
        if ( System.getProperty( SYSPROP_COUNTRY, null ) == null ) {
            System.setProperty( SYSPROP_COUNTRY, java.util.Locale.getDefault().getCountry() );
        }
        if ( System.getProperty( SYSPROP_LANGUAGE, null ) == null ) {
            System.setProperty( SYSPROP_LANGUAGE, java.util.Locale.getDefault().getLanguage() );
        }
        if ( System.getProperty( SYSPROP_VARIANT, null ) == null ) {
            System.setProperty( SYSPROP_VARIANT, java.util.Locale.getDefault().getVariant() );
        }
    }
    
    private static String getReplacement (String whatToReplace) {        
        return System.getProperty (whatToReplace, "");
    }
    
}
