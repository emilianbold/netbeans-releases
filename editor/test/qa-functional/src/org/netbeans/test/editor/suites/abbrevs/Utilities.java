/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.test.editor.suites.abbrevs;

import org.netbeans.modules.editor.options.BaseOptions;
import java.util.ResourceBundle;
import org.openide.util.NbBundle;
import org.netbeans.modules.editor.options.BaseOptionsBeanInfo;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
//import org.netbeans.modules.editor.options.HTMLOptions; //SPLIT-TEMP
//import org.netbeans.modules.editor.options.JavaOptions; //SPLIT-TEMP
//import org.netbeans.modules.editor.options.PlainOptions; //SPLIT-TEMP
import org.openide.options.SystemOption;

/**
 *
 * @author  Jan Lahoda
 */
public class Utilities {
    
    
    /** Creates a new instance of Utilities */
    public Utilities() {
    }
    
    public static ResourceBundle getEditorBundle() {
        return NbBundle.getBundle(BaseOptionsBeanInfo.class);
    }
    
    /**Saves abbreviations maps for all editor kits installed. You should not
     * rely on the particular type returned (currently java.util.Map), but just save the
     * returned Object and pass it unchanged to restoreAbbreviationsState();
     */
    public static Object saveAbbreviationsState() {
        //        SystemOption[] options = ((AllOptions) AllOptions.findObject(AllOptions.class, true)).getOptions();  -- //return null
        SystemOption[] options = new SystemOption[]{};//SPLIT-TEMP
        //{(JavaOptions)SystemOption.findObject(JavaOptions.class),
        //(PlainOptions)SystemOption.findObject(PlainOptions.class),(HTMLOptions)SystemOption.findObject(HTMLOptions.class)};
        
        Map            result  = new HashMap();
        
        for (int cntr = 0; cntr < options.length; cntr++) {
            if (options[cntr] instanceof BaseOptions) {
                BaseOptions baseOptions      = (BaseOptions) options[cntr];
                result.put(baseOptions.getClass(), baseOptions.getAbbrevMap());
            }
        }
        return result;
    }
    
    public static void restoreAbbreviationsState(Object state) throws ClassCastException {
        Map abbreviations = (Map) state; //ClassCastException
        
        /*Just check, no functionality:
         */
        {
            Iterator it = abbreviations.values().iterator();
            Map      dummy;
            
            while (it.hasNext()) {
                dummy = (Map) it.next();
            }
        }
        
        /*The main functionality:
         */
        SystemOption[] options = new SystemOption[] {};//SPLIT-TEMP
        //{(JavaOptions)SystemOption.findObject(JavaOptions.class),
        //(PlainOptions)SystemOption.findObject(PlainOptions.class),(HTMLOptions)SystemOption.findObject(HTMLOptions.class)};
        
        //        SystemOption[] options = ((AllOptions) AllOptions.findObject(AllOptions.class)).getOptions();   --- //return null
        
        for (int cntr = 0; cntr < options.length; cntr++) {
            if (options[cntr] instanceof BaseOptions) {
                BaseOptions baseOptions      = (BaseOptions) options[cntr];
                Map         kitAbbreviations = (Map) abbreviations.get(baseOptions.getClass()); //ClassCastException
                
                if (kitAbbreviations != null) {
                    baseOptions.setAbbrevMap(kitAbbreviations);
                }
            }
        }
        
    }
    
    public static void main(String[] argv) {
        Utilities.saveAbbreviationsState();
    }
    
}
