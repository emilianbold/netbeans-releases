/*
 * StreamHandler.java
 *
 * Created on 08 May 2006, 10:16
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.mercurial.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author peter
 */
public class StreamHandler extends Thread {

    InputStream in = null;
    OutputStream out = null;
    
    /** Creates a new instance of StreamHandler */
    public StreamHandler(InputStream in, OutputStream out) {
        this.in = new BufferedInputStream(in);
        if (out != null)
            this.out = new BufferedOutputStream(out);
    }
    
    public void run () {
        try {
            try {
                try {
                    int i;
                    while((i = in.read()) != -1) {
                        if (out != null)
                           out.write(i);
                    }
                } finally {
                    in.close();
                }
                if (out != null)
                    out.flush();
            } finally {
                if (out != null)
                    out.close();
            }
        } catch (IOException ex) {
          ex.printStackTrace();
        }
    }    
}
