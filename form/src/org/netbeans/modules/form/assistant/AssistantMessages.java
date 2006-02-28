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
package org.netbeans.modules.form.assistant;

import java.util.*;

import org.openide.util.NbBundle;

/**
 * Repository of assistant messages.
 *
 * @author Jan Stola
 */
public class AssistantMessages {
    private static AssistantMessages defaultInstance = new AssistantMessages();
    private boolean initialized = false;
    private Map/*<String, String[]>*/ contextToMessages;
    
    private AssistantMessages() {
    }

    public static AssistantMessages getDefault() {
        return defaultInstance;
    }

    public String[] getMessages(String context) {
        if (!initialized) {
            initialize();
        }
        String[] messages = (String[])contextToMessages.get(context);
        return messages;
    }

    private void initialize() {
        Map contextToSet = new HashMap();
        ResourceBundle bundle = NbBundle.getBundle(AssistantMessages.class);
        Enumeration enumeration = bundle.getKeys();
        while (enumeration.hasMoreElements()) {
            String bundleKey = (String)enumeration.nextElement();
            String context = getContext(bundleKey);
            Set messages = (Set)contextToSet.get(context);
            if (messages == null) {
                messages = new HashSet();
                contextToSet.put(context, messages);
            }
            messages.add(bundle.getString(bundleKey));
        }

        // Transform sets into arrays
        contextToMessages = new HashMap();
        Iterator iter = contextToSet.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            String key = (String)entry.getKey();
            Set value = (Set)entry.getValue();
            String[] messages = (String[])value.toArray(new String[value.size()]);
            contextToMessages.put(key, messages);
        }
    }

    private String getContext(String bundleKey) {
        int index = bundleKey.indexOf('_');
        if (index == -1) {
            return bundleKey;
        } else {
            return bundleKey.substring(0, index);
        }
    }

}
