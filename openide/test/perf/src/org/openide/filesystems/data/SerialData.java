/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.filesystems.data;

import java.util.ArrayList;
import java.io.*;

/**
 * Serves for generating
*/
public class SerialData extends Object implements Serializable {
    
    private static String serialData;
    
    private int data1;
    private String data2;
    private Object data3;
    private ArrayList data4;

    /** Creates new SerialData */
    public SerialData() {
        data1 = 0;
        data2 = "data2";
        data3 = new Integer(5);
        data4 = new ArrayList();
        data4.add(data3);
    }
    
    public static String getSerialDataString() throws Exception {
        if (serialData == null) {
            serialData = createSerialDataString();
        }
        
        return serialData;
    }
    
    private static String createSerialDataString() throws Exception {
        ByteArrayOutputStream barros = new ByteArrayOutputStream();
        ObjectOutputStream obos = new ObjectOutputStream(barros);
        
        obos.writeObject(new SerialData());
        obos.close();
        
        return bytes2String(barros.toByteArray());
    }

    /**
    * @param args the command line arguments
    */
    /*
    public static void main(String args[]) throws Exception {
        ByteArrayOutputStream barros = new ByteArrayOutputStream();
        ObjectOutputStream obos = new ObjectOutputStream(barros);
        
        obos.writeObject(new SerialData());
        obos.close();
        
        byte[] bytes = barros.toByteArray();
        System.out.println(bytes2String(bytes));
    }*/
    
    private static String bytes2String (byte[] bytes) {
        StringBuffer buffer = new StringBuffer(2 * bytes.length);
        
        for (int i = 0; i < bytes.length; i++) {
            addByte(bytes[i], buffer);
        }
        
        return buffer.toString();
    }
    
    private static void addByte(int b, StringBuffer buffer) {
        if (b < 0) {
            b += 256;
        }
        
        int rest = b % 16;
        b = b / 16;
        buffer.append(toChar(b));
        buffer.append(toChar(rest));
    }
    
    private static char toChar(int b) {
        if (b > 9) {
            return (char) ('a' + b - 10);
        } else {
            return (char) ('0' + b);
        }
    }
}
