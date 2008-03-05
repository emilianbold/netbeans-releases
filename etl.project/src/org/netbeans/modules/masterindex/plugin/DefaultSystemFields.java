/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.masterindex.plugin;

import java.util.HashMap;
import java.util.logging.Logger;

/**
 *
 * @author Manish
 */
public class DefaultSystemFields {

    // Enter System fields that need to be set as defaults (UPPERCASE)
    // These fields must be present in the source system from where data is being pulled out.
     private static final String[] defaults = {
            "GID",
            "SYSTEMCODE",
            "LID",
            "UPDATEDATE",
            "USR",};
    
    //logger
     private static Logger logger = Logger.getLogger(DefaultSystemFields.class.getName());
    HashMap fieldMap = new HashMap<String, Integer>();

    public DefaultSystemFields() {
        logger.info("Default System Fields ..." + printfields());
        createFieldMap();
    }

    private String printfields() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < defaults.length; i++) {
            sb.append("\n   " + defaults[i]);
        }
        return sb.toString();
    }

    // This is to speed up search of fields at runtime
    private void createFieldMap() {
        for (int i = 0; i < defaults.length; i++) {
            fieldMap.put(defaults[i], new Integer(i));
        }
    }

    // If Attribute is found, index is returned for the attribute
    public int isAttributeDefault(String attrib) {
        Integer index = (Integer) fieldMap.get(attrib);
        if (index != null) {
            return index.intValue();
        }
        return -1;
    }
    
    public static String[] getDefaultSystemFields(){
        return defaults;
    }
}
