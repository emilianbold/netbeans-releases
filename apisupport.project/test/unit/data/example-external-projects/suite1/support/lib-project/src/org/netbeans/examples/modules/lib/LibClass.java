package org.netbeans.examples.modules.lib;
import org.netbeans.examples.modules.misc.Misc;
public class LibClass {
    public static String getMagicToken() {
        return "Otev\u0159i " + Misc.getMagicTokenette() + "!";
    }
}
