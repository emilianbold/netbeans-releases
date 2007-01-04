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

package org.netbeans.modules.cnd.settings;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.ResourceBundle;
import org.openide.ErrorManager;
import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/** Settings for the C/C++/Fortran. The compile/build options stored
 * in this class are <B>default</B> options which will be applied to new files.
 * Once a file has been created its options may diverge from the defaults. A
 * files options do not change if these default options are changed.
 */

public class CppSettings extends SystemOption {

    /** The singleton instance */
    //static CppSettings cppSettings;

    /** serial uid */
    static final long serialVersionUID = -2942467713237077336L;

    public static final int DEFAULT_PARSING_DELAY = 2000;

    // Option labels
    public static final String
		PROP_PARSING_DELAY	= "parsingDelay",		//NOI18N
		PROP_REPLACEABLE_STRINGS_TABLE = "replaceableStringsTable",	//NOI18N
                PROP_FREE_FORMAT_FORTRAN = "freeFormatFortran"; // NOI18N
    
    /** The resource bundle for the form editor */
    public static ResourceBundle bundle;


    /**
     *  Initialize each property.
     */
    protected void initialize() {
	super.initialize();
    }


    /** Return the signleton cppSettings */
    public static CppSettings getDefault() {
	return (CppSettings) findObject(CppSettings.class, true);
    }

    /** Gets the delay time for the start of the parsing.
    * @return The time in milis
    */
    public int getParsingDelay() {
        Integer delay = (Integer)getProperty(PROP_PARSING_DELAY);
        if (delay == null)
            return DEFAULT_PARSING_DELAY;
        return delay.intValue();
    }

    /** Sets the delay time for the start of the parsing.
    * @param delay The time in milis
    */
    public void setParsingDelay(int delay) {
        if (delay != 0 && delay < 1000) {
            IllegalArgumentException e = new IllegalArgumentException();
	    ErrorManager.getDefault().annotate(e, getString("INVALID_AUTO_PARSING_DELAY"));
	    //ErrorManager.getDefault().notify(e);
	    throw e;
	}
        putProperty(PROP_PARSING_DELAY, new Integer(delay));
    }


    /**
     * Get the display name.
     *
     *  @return value of OPTION_CPP_SETTINGS_NAME
     */
    public String displayName () {
	return getString("OPTION_CPP_SETTINGS_NAME");		        //NOI18N
    }
    
    public HelpCtx getHelpCtx () {
	return new HelpCtx ("Welcome_opt_editing_sources");	        //NOI18N
    }


    /** Sets the replaceable strings table - used during instantiating
    * from template.
    */
    public void setReplaceableStringsTable(String table) {
        String t = getReplaceableStringsTable();
        if (t.equals(table))
            return;
        putProperty(PROP_REPLACEABLE_STRINGS_TABLE, table, true);
    }

    /** Gets the replacable strings table - used during instantiating
    * from template.
    */
    public String getReplaceableStringsTable() {
        String table = (String)getProperty(PROP_REPLACEABLE_STRINGS_TABLE);
        if (table == null) {
            return "USER="+System.getProperty("user.name"); // NOI18N
        } else {
            return table;
        }
    }


    /** Gets the replaceable table as the Properties class.
    * @return the properties
    */
    public Properties getReplaceableStringsProps() {
        Properties props = new Properties();
        // PENDING: don't use StringBufferInputStream, it does not encode characters
        // well.
        try {
            props.load(new ByteArrayInputStream(getReplaceableStringsTable().getBytes()));
        }
        catch (IOException e) {
        }
        return props;
    }

    
    /** @return localized string */
    static String getString(String s) {
	if (bundle == null) {
	    bundle = NbBundle.getBundle(CppSettings.class);
	}
	return bundle.getString(s);
    }
    

     public boolean isFreeFormatFortran(){
         Boolean b = (Boolean)getProperty(PROP_FREE_FORMAT_FORTRAN);
         if( b == null ){
             try{
                 // Need to go through SystemClassLoader :(
                 Class fSettingsDefaults = Class.forName(
                     "org.netbeans.modules.cnd.editor.fortran.FSettingsDefaults", // NOI18N
                     true, (ClassLoader) Lookup.getDefault().lookup(ClassLoader.class)
                 );
                 java.lang.reflect.Field defaultFreeFormat =
                     fSettingsDefaults.getField("defaultFreeFormat"); // NOI18N
                 b = (defaultFreeFormat.getBoolean(null))?Boolean.TRUE:Boolean.FALSE;
             }catch(Exception e){
                 // let's cheat, we know the default is TRUE (from FSettingsDefault)
                 b = Boolean.TRUE;
             }
             putProperty(PROP_FREE_FORMAT_FORTRAN, b);
         }
         return b.booleanValue();
     }

     public void setFreeFormatFortran(boolean state){
         putProperty(PROP_FREE_FORMAT_FORTRAN,state ? Boolean.TRUE : Boolean.FALSE);
      }    
}
