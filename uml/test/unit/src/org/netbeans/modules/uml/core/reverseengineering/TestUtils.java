
/*
 * File       : TestUtils.java
 * Created on : Feb 3, 2004
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
/**
 * @author Aztec
 */
public class TestUtils
{

    /**
     * @param xml
     * @param outFile
     */
    public static void dumpToFile(String xml, String outFile)
    {
        try
        {
            File f = new File(outFile);
            f.createNewFile();
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(f));
            dos.writeBytes("<TestSnip>\r\n" + xml + "\r\n</TestSnip>");
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

}
