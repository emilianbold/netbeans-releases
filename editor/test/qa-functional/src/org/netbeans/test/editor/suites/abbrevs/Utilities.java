/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
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
import org.netbeans.modules.editor.options.AllOptions;
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
    
    /**Saves abbreviations maps for all editor kits installed. You should no
     * rely on the particular type returned (java.util.Map), but just save the
     * returned Object and pass it unchanged to restoreAbbreviationsState();
     */
    public static Object saveAbbreviationsState() {
        SystemOption[] options = ((AllOptions) AllOptions.findObject(AllOptions.class)).getOptions();
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
        
        SystemOption[] options = ((AllOptions) AllOptions.findObject(AllOptions.class)).getOptions();

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
        
}
