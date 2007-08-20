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

    InputStream in;
    OutputStream out;
    
    /** Creates a new instance of StreamHandler */
    public StreamHandler(InputStream in, OutputStream out) {
        this.in = new BufferedInputStream(in);
        this.out = new BufferedOutputStream(out);
    }
    
    public void run () {
        try {
            try {
                try {
                    int i;
                    while((i = in.read()) != -1) {
                        out.write(i);
                    }
                } finally {
                    in.close();
                }
                out.flush();
            } finally {
                out.close();
            }
        } catch (IOException ex) {
          ex.printStackTrace();
        }
    }    
}
