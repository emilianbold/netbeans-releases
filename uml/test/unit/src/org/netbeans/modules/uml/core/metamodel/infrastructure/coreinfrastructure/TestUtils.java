package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 */
public final class TestUtils
{
    public static void writeFile(String path, String contents) {
        if (path == null) path = "Xyz.java";
        if (contents == null) contents = "public class Xyz {\n}\n";

        File f = new File(path);
        File parent = f.getParentFile();
        if (parent != null && !parent.exists())
            parent.mkdirs();
        
        try {
            FileWriter fw = new FileWriter(f);
            PrintWriter pw = new PrintWriter(fw);
            pw.print(contents);
            pw.close();
            fw.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}