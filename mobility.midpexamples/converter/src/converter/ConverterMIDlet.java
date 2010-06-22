/*
 * Copyright (c) 2010, Oracle.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of Oracle nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * ConverterMIDlet.java
 */
package converter;

import java.io.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.rms.*;

/**
 *
 */
public class ConverterMIDlet extends javax.microedition.midlet.MIDlet {
    
    private static String storedDataStr = "ConverterData";
    
    public String[] currencies = new String[] { "US $", "Yen \u00a5", "Euro \u20ac" };
    
    public boolean[] selected = new boolean[] { true, true, true, true };
    
    public long[][] rates =  {{   1000000, 117580000,    911079 },
                              {      8504,   1000000,      7749 },
                              {   1097600, 129056000,   1000000 }};
    
    private RecordStore   storedData;
  
    public void startApp() {
        try {
            storedData = RecordStore.openRecordStore(storedDataStr, true);
            if (storedData.getNumRecords() > 0) {
                DataInputStream in = new DataInputStream(new ByteArrayInputStream(storedData.getRecord(1)));
                try {
                    int size = in.readInt();
                    currencies = new String[size];
                    selected = new boolean[size];
                    rates = new long[size][];
                    for (int i=0; i<size; i++) {
                        currencies[i] = in.readUTF();
                        selected[i] = in.readBoolean();
                        rates[i] = new long[size];
                        for (int j=0; j<size; j++) {
                            rates[i][j] = in.readLong();
                        }
                    }
                    in.close();
                } catch (IOException ioe) {
                }
            }
        } catch (RecordStoreException e) {
        }
        notifySettingsChanged();
    }
    
    public void pauseApp() {
    }
    
    public void destroyApp(boolean unconditional) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bytes);
            try {
                out.writeInt(currencies.length);
                for (int i=0; i<currencies.length; i++) {
                    out.writeUTF(currencies[i]);
                    out.writeBoolean(selected[i]);
                    for (int j=0; j<currencies.length; j++) {
                        out.writeLong(rates[i][j]);
                    }
                }
                out.close();
                if (storedData.getNumRecords() > 0)
                    storedData.setRecord(1, bytes.toByteArray(), 0, bytes.size());
                else
                    storedData.addRecord(bytes.toByteArray(), 0, bytes.size());
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        } catch (RecordStoreException e) {
            e.printStackTrace();
        }
        notifyDestroyed();
    }

    public void showSettings() {
         Display.getDisplay(this).setCurrent(new CurrenciesSelector(this));
    }
    
    public void notifySettingsChanged() {
        Display.getDisplay(this).setCurrent(new Converter(this));
    }

    public long convert(long frval, int fridx, int toidx) {
        return (frval * rates[fridx][toidx]) / 1000000;
    }
}
