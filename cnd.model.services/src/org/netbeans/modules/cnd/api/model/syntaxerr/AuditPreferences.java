/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.api.model.syntaxerr;

import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author Alexander Simon
 */
public final class AuditPreferences {
    public static final Preferences AUDIT_PREFERENCES_ROOT = NbPreferences.root().node("org/netbeans/modules/cnd/analysis"); // NOI18N
    private final Preferences preferences;


    public AuditPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    public Preferences getPreferences() {
        return preferences;
    }
    
    public String get(String audit, String key, String defValue) {
        String old = preferences.get(audit, ""); //NOI18N
        StringTokenizer st = new StringTokenizer(old,";"); //NOI18N
        while(st.hasMoreTokens()) {
            String token = st.nextToken();
            int i = token.indexOf('='); //NOI18N
            if (i > 0) {
                String rv = token.substring(0,i);
                if (key.equals(rv)) {
                    return token.substring(i+1);
                }
            }
        }
        return defValue;
    }

    public void put(String audit, String key, String value, String defValue) {
        String old = preferences.get(audit, ""); //NOI18N
        StringBuilder buf = new StringBuilder();
        StringTokenizer st = new StringTokenizer(old,";"); //NOI18N
        boolean found = false;
        while(st.hasMoreTokens()) {
            String token = st.nextToken();
            int i = token.indexOf('='); //NOI18N
            if (i > 0) {
                String rv = token.substring(0,i);
                if (key.equals(rv)) {
                    if (!value.equals(defValue)) {
                        if (buf.length() > 0) {
                            buf.append(';'); //NOI18N
                        }
                        buf.append(key);
                        buf.append('='); //NOI18N
                        buf.append(value);
                    }
                    found = true;
                } else {
                    if (buf.length() > 0) {
                        buf.append(';'); //NOI18N
                    }
                    buf.append(token);
                }
            }
        }
        if (!found && !value.equals(defValue)) {
            if (buf.length() > 0) {
                buf.append(';'); //NOI18N
            }
            buf.append(key);
            buf.append('='); //NOI18N
            buf.append(value);
        }
        if (buf.length() == 0) {
            preferences.remove(audit);
        } else {
            preferences.put(audit, buf.toString());
        }
    }

    @Override
    public String toString() {
        return preferences.toString();
    }
}
