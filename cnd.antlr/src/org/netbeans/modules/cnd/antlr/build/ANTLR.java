package org.netbeans.modules.cnd.antlr.build;

import java.io.*;

/** Simple class that uses build.Tool to compile ANTLR's Java stuff */
public class ANTLR {
    public static String compiler = "javac";
    public static String jarName = "org.netbeans.modules.cnd.antlr.jar";
    public static String root = ".";

    public static String[] srcdir = {
        "org/netbeans/modules/cnd/antlr",
        "org/netbeans/modules/cnd.antlr/actions/cpp",
        "org/netbeans/modules/cnd.antlr/actions/java",
        "org/netbeans/modules/cnd.antlr/actions/csharp",
        "org/netbeans/modules/cnd.antlr/collections",
        "org/netbeans/modules/cnd.antlr/collections/impl",
        "org/netbeans/modules/cnd.antlr/debug",
        "org/netbeans/modules/cnd.antlr/ASdebug",
        "org/netbeans/modules/cnd.antlr/debug/misc",
        "org/netbeans/modules/cnd.antlr/preprocessor"
    };

    public ANTLR() {
        compiler = System.getProperty("org.netbeans.modules.cnd.antlr.build.compiler", compiler);
        root = System.getProperty("org.netbeans.modules.cnd.antlr.build.root", root);
    }

    public String getName() { return "ANTLR"; }

    /** Build ANTLR.  action on cmd-line matches method name */
    public void build(Tool tool) {
        if ( !rootIsValidANTLRDir(tool) ) {
            return;
        }
        // run ANTLR on its own .g files
        tool.antlr(root+"/org/netbeans/modules/cnd/antlr/antlr.g");
        tool.antlr(root+"/org/netbeans/modules/cnd/antlr/tokdef.g");
        tool.antlr(root+"/org/netbeans/modules/cnd/antlr/preprocessor/preproc.g");
        tool.antlr(root+"/org/netbeans/modules/cnd/antlr/actions/java/action.g");
        tool.antlr(root+"/org/netbeans/modules/cnd/antlr/actions/cpp/action.g");
        tool.antlr(root+"/org/netbeans/modules/cnd/antlr/actions/csharp/action.g");
        for (int i=0; i<srcdir.length; i++) {
            String cmd = compiler+" -d "+root+" "+root+"/"+srcdir[i]+"/*.java";
            tool.system(cmd);
        }
    }

    /** Jar up all the .class files */
    public void jar(Tool tool) {
        if ( !rootIsValidANTLRDir(tool) ) {
            return;
        }
        StringBuffer cmd = new StringBuffer(2000);
        cmd.append("jar cvf "+root+"/"+jarName);
        for (int i=0; i<srcdir.length; i++) {
            cmd.append(" "+root+"/"+srcdir[i]+"/*.class");
        }
        tool.system(cmd.toString());
    }

    /** ANTLR root dir must contain an "org.netbeans.modules.cnd.antlr" dir and must have java
     *  files underneath etc...
     */
    protected boolean rootIsValidANTLRDir(Tool tool) {
        if ( root==null ) {
            return false;
        }
        File antlrRootDir = new File(root);
        if ( !antlrRootDir.exists() ) {
            tool.error("Property org.netbeans.modules.cnd.antlr.build.root=="+root+" does not exist");
            return false;
        }
        if ( !antlrRootDir.isDirectory() ) {
            tool.error("Property org.netbeans.modules.cnd.antlr.build.root=="+root+" is not a directory");
            return false;
        }
        String[] antlrDir = antlrRootDir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return dir.isDirectory() && name.equals("org.netbeans.modules.cnd.antlr");
            }
        });
        if ( antlrDir==null || antlrDir.length==0 ) {
            tool.error("Property org.netbeans.modules.cnd.antlr.build.root=="+root+" does not appear to be a valid ANTLR project root (no org.netbeans.modules.cnd.antlr subdir)");
            return false;
        }
        File antlrPackageDir = new File(root+"/org.netbeans.modules.cnd.antlr");
        String[] antlrPackageJavaFiles = antlrPackageDir.list();
        if ( antlrPackageJavaFiles==null || antlrPackageJavaFiles.length==0 ) {
            tool.error("Property org.netbeans.modules.cnd.antlr.build.root=="+root+" does not appear to be a valid ANTLR project root (no .java files in org.netbeans.modules.cnd.antlr subdir");
            return false;
        }
        return true;
    }
}
