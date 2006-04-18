/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.subversion.config;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import org.netbeans.modules.subversion.util.FileUtils;
import org.openide.ErrorManager;

/**
 *
 * @author Tomas Stupka
 *
 */
public class KVFile {
    
    private Map map;
    private final File file;            

    public KVFile(File file) {
        this.file = file;
        try {
            if(file.exists()) {
                parse();
            }
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }
        
    protected byte[] getValue(Key key) {
        return (byte[]) getMap().get(key);
    }
    
    protected void setValue(Key key, byte[] value) {
        getMap().put(key, value);
    }
 
    public Map getMap() {
        if(map==null) {
            map = new TreeMap();
        }
        return map;
    }
   
    void parse() throws IOException {        
        InputStream is = null;        
        // XXX encoding ?
        try {            
            is = FileUtils.createInputStream(file);                                    
            int keyIdx = 0;
            while(!checkEOF(is)) {                      
               int keyLength = readEntryLength(is);     // key length
               byte[] keyName = new byte[keyLength];
               is.read(keyName);
               is.read(); // skip '\n'
               int valueLength = readEntryLength(is);   // value length
               byte[] value = new byte[valueLength];
               is.read(value);               
               getMap().put(new Key(keyIdx, new String(keyName)), value);
               is.read(); // skip '\n'
               keyIdx++;
            }
        } catch (EOFException eofe) {
            if(getMap().size() > 0) {
                // there are already some key-value pairs ->
                // something in the file structure seems to be wrong
                throw new EOFException(file.getAbsolutePath());
            }
            // otherwise skip the exception, could be just an empty file
        } finally {
            try {                 
                if (is != null) {        
                    is.close();
                }
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e); // should not happen
            }                              
        }
    }  
    
    private boolean checkEOF(InputStream is) throws IOException {
        is.mark(3);
        byte[] end = new byte[3];
        is.read(end);
        is.reset();
        if(end[0] == -1 || end[1] == -1 || end[2] == -1) {
            throw new EOFException();
        }
        return end[0] == 'E' && end[1] == 'N' && end[2] == 'D';
    }
    
    private int readEntryLength(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte b = (byte) is.read();
        while ( b != '\n')  {
            if(b == -1) {
                throw new EOFException();
            }
            baos.write(b);
            b = (byte) is.read();
        }
        String line = baos.toString();
        return Integer.decode(line.substring(2)).intValue();
    }          

    public void store() throws IOException {        
        store(file);
    }
    
    public void store(File file) throws IOException {
        OutputStream os = null; 
        try {
            File parent = file.getParentFile();
            if(parent!=null && !parent.exists()) {
                parent.mkdirs();
            }
            os = FileUtils.createOutputStream(file);            
            for (Iterator it = getMap().keySet().iterator(); it.hasNext();) {
                Key key = (Key) it.next();
                byte[] value = (byte[]) map.get(key);                
                
                StringBuffer sb = new StringBuffer();
                sb.append("K ");
                sb.append(key.getName().length());
                sb.append("\n");
                sb.append(key.getName());
                sb.append("\n");
                sb.append("V ");
                sb.append(value.length);
                sb.append("\n");
                os.write(sb.toString().getBytes());    
                os.write(value);            
                os.write("\n".getBytes());
            }
            os.write("END\n".getBytes());     
            os.flush();
            
        } finally {
            if(os != null) {
                try {
                    os.close();    
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }                
            }            
        }        
    }    
    
    protected File getFile() {
        return file;
    }

    void setValue(Key key, String value) {
        setValue(key, value.getBytes());
    }

    protected static class Key implements Comparable {
        private final int idx;
        private final String name;
        protected Key(int idx, String name) {
            this.name = name;
            this.idx = idx;
        }
        public int getIndex() {
            return idx;
        }       
        public String getName() {
            return name;
        }
        public boolean  equals(Object obj) {
            if( !(obj instanceof Key) ) {
                return false;
            }
            Key key = (Key) obj;
            return key.getIndex() == getIndex() && key.getName().equals(getName());
        }      
        public int hashCode() {
            StringBuffer sb = new StringBuffer();
            sb.append(getName());            
            sb.append(getIndex());
            return sb.toString().hashCode();
        }
        public int compareTo(Object obj) {
            if( !(obj instanceof Key) ) {
                return 0;
            }
            Key key = (Key) obj;
            if (key.getIndex() < getIndex()) {
                return 1;
            } else if (key.getIndex() > getIndex()) {
                return -1;
            }    
            return 0;
        }
        public String toString() {
            return name;
        }
    }    
   
}
