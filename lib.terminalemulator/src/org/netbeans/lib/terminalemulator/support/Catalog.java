/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.lib.terminalemulator.support;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 *
 * @author ivan
 */
class Catalog {
    private static final Package pkg = Catalog.class.getPackage();
    private static final String baseName =
	pkg.getName().replace(".", "/") + "/Bundle";	// NOI18N
    private static final ResourceBundle bundle =
        ResourceBundle.getBundle(baseName);

    public static String get(String key) {
        return bundle.getString(key);
    }

    public static int mnemonic(String key) {
        return bundle.getString(key).charAt(0);
    }

    public static String format(String formatKey, Object... args) {
        return MessageFormat.format(get(formatKey), args);
    }
}
