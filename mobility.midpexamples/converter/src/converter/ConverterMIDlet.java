/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
