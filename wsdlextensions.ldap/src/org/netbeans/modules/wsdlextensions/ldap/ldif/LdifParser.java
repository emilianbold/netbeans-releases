/*
 * LdifParser.java
 * 
 * Created on Apr 30, 2007, 2:42:38 PM
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.netbeans.modules.wsdlextensions.ldap.ldif;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Gary
 */
public class LdifParser {

    File mLdifFile;

    public LdifParser(File f) {
        mLdifFile = f;
    }

    private String findDefinition(String str) {
        String ret = "";
        int level = -1;

        for (int i = 0; i < str.length(); i++) {
            ret += str.charAt(i);
            if (str.charAt(i) == '(') {
                if (level < 0) {
                    level = 1;
                } else {
                    level++;
                }
            }

            if (str.charAt(i) == ')') {
                level--;
                if (level == 0) {
                    break;
                }
            }
        }

        return ret;
    }

    @SuppressWarnings("unchecked")
    public List parse() throws IOException {
        List ret = new ArrayList();

        FileInputStream fis = new FileInputStream(mLdifFile);
        byte[] buf = new byte[fis.available()];
        fis.read(buf);
        String ldifStr = new String(buf);
        ldifStr = ldifStr.replaceAll("[\\n]", "");
        //ldifStr = ldifStr.replaceAll("[\\s]", "");
        String objClassStr1 = "objectclass (";
        String objClassStr2 = "objectClasses: (";
        String objClassTag = "";
        if (ldifStr.contains(objClassStr1)) {
            objClassTag = objClassStr1;
        } else if (ldifStr.contains(objClassStr2)) {
            objClassTag = objClassStr2;
        }

        int pos = ldifStr.indexOf(objClassTag);

        while (pos >= 0) {
            ldifStr = ldifStr.substring(pos + objClassTag.length() - 1);
            String ldif = findDefinition(ldifStr);
            ret.add(getObjectClass(ldif));
            ldifStr = ldifStr.substring(ldif.length());

            if (ldifStr.contains(objClassStr1)) {
                objClassTag = objClassStr1;
            } else if (ldifStr.contains(objClassStr2)) {
                objClassTag = objClassStr2;
            }
            pos = ldifStr.indexOf(objClassTag);
        }

        fis.close();
        return ret;
    }

    private LdifObjectClass getObjectClass(String str) {
        LdifObjectClass ret = new LdifObjectClass();

        ret.setName(getName(str));
        //ret.setDescription(getDescription(str));
        String[] mays = getMayIds(str);
        if (mays != null) {
            for (int i = 0; i < mays.length; i++) {
                ret.addMay(mays[i].replaceAll("[\\s]", ""));
            }
        }
        String[] musts = getMustIds(str);
        if (musts != null) {
            for (int i = 0; i < musts.length; i++) {
                ret.addMust(musts[i].replaceAll("[\\s]", ""));
            }
        }

        return ret;
    }

    private String[] getMayIds(String str) {
        if (str.indexOf("MAY") == -1) {
            return null;
        }

        if (str.charAt(str.indexOf("MAY") + 4) != '(') {
            int end = str.indexOf(" ", str.indexOf("MAY") + 4);
            return new String[]{str.substring(str.indexOf("MAY") + 4, end)};
        }

        String mayPattern = "[M][A][Y][ ][(][\\s\\w\\$-]*[)]";
        String[] splitted = str.split(mayPattern);
        int before = splitted[0].length();
        int after = 0;
        if (splitted.length > 1) {
            after = str.indexOf(splitted[1]);
        }

        String ret = null;
        if (after == 0) {
            ret = str.substring(before);
        } else {
            ret = str.substring(before, after);
        }

        if (ret == null || ret.length() == 0) {
            return null;
        }

        return ret.substring(5, ret.length() - 1).split("\\$");
    }

    private String[] getMustIds(String str) {
        if (str.indexOf("MUST") == -1) {
            return null;
        }

        if (str.charAt(str.indexOf("MUST") + 5) != '(') {
            int end = str.indexOf(" ", str.indexOf("MUST") + 5);
            return new String[]{str.substring(str.indexOf("MUST") + 5, end)};
        }

        String mustPattern = "[M][U][S][T][ ][(][\\s\\w\\$-]*[)]";
        String[] splitted = str.split(mustPattern);
        int before = splitted[0].length();
        int after = 0;
        if (splitted.length > 1) {
            after = str.indexOf(splitted[1]);
        }

        String ret = null;
        if (after == 0) {
            ret = str.substring(before);
        } else {
            ret = str.substring(before, after);
        }

        if (ret == null || ret.length() == 0) {
            return null;
        }

        return ret.substring(6, ret.length() - 1).split("\\$");
    }

    private String getDescription(String str) {
        String descPattern = "[D][E][S][C][ ]['][^']*[']";
        String[] splitted = str.split(descPattern);
        int before = splitted[0].length();
        int after = 0;
        if (splitted.length > 1) {
            after = str.indexOf(splitted[1]);
        }

        String ret = null;
        if (after == 0) {
            ret = str.substring(before);
        } else {
            ret = str.substring(before, after);
        }

        ret = ret.substring(6, ret.length() - 1).replaceAll("[\\s]", "");
        return ret;
    }

    private String getName(String str) {
        String namePattern = "[N][A][M][E][ ]['][^']*[']";
        String[] splitted = str.split(namePattern);
        int before = splitted[0].length();
        int after = 0;
        if (splitted.length > 1) {
            after = str.indexOf(splitted[1]);
        }

        String ret = null;
        if (after == 0) {
            ret = str.substring(before);
        } else {
            ret = str.substring(before, after);
        }

        ret = ret.substring(6, ret.length() - 1).replaceAll("[\\s]", "");
        return ret;
    }

    public static void main(String[] args) throws IOException {
        //File testFile = new File("/home/liyunhai/core.ldif");
        File testFile = new File("/home/liyunhai/temp.ldif");
        //File testFile = new File("/home/liyunhai/99cvd.ldif");
        LdifParser parser = new LdifParser(testFile);
        List list = parser.parse();

        java.util.Iterator it = list.iterator();
        while (it.hasNext()) {
            LdifObjectClass obj = (LdifObjectClass) it.next();
            System.out.println(obj.getName());
            System.out.println(obj.getMust());
            System.out.println(obj.getMay());
            System.out.println();
        }

        System.out.println("Completed");
    }
}