package org.netbeans.modules.cnd.classview;

import org.netbeans.modules.cnd.apt.utils.APTStringManager;


public class NameCache {
    private static final APTStringManager instance = APTStringManager.instance("PERSISTENT_NAME_CACHE"); // NOI18N    
    private NameCache() {
    }
    
    public static String getString(String text) {
        if (text == null){
            return text;
        }
        return instance.getString(text);
    }
    
    public static void dispose() {
        instance.dispose();
    }
}