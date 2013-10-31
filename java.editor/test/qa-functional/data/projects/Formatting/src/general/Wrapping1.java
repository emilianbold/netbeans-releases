/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package general;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import javax.swing.DefaultCellEditor;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author jp159440
 */
public class Wrapping1 extends DefaultTableModel 
{

    class InnerClassWithQuiteALongName extends Thread
    {
    }

    class InnerClassAgainWithLongName implements
       Serializable
    {
    }

    class InnerClass implements Serializable
    {
    }

    public Wrapping1 method(int parameter1,
       int parameter2,
       String... var)
    {
        return this;
    }

    @interface MyAnot
    {

        int a();

        String b();

        String c();
    }

    @MyAnot(a = 11111, b = "       ",
    c = "        ")
    public void method2()
    {
        method(1, 2, "string vararg",
           "string vararg");
        method(1, 1, "").method(2, 2, "2").method(
           3, 3, "3");
    }

    public void m() throws IOException,
       MalformedURLException
    {
    }

    public void lngMethodNa(String a) throws
       IOException
    {
    }
    String[] s = new String[]
    {
        "aaaaa", "bbbbb"
    };
}
