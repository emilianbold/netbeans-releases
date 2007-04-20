package org.netbeans.installer.infra.utils.newlinecorrecter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        final File file = new File("D:/temp/nbi-build/build.sh");
        final String newline = "\n";
        
        final List<String> lines = new LinkedList<String>();
        
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line2read = null;
        while ((line2read = reader.readLine()) != null) {
            lines.add(line2read);
        }
        reader.close();
        
        PrintWriter writer = new PrintWriter(new FileWriter(file));
        for (String line2write: lines) {
            writer.write(line2write + newline);
        }
        writer.close();
    }
}
