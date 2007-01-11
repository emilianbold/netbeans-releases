/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.upgrade.systemoptions;

import java.io.*;
import java.util.Iterator;
import java.util.Set;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Radek Matous
 */
public class SystemOptionsParser  {
    static final String EXPECTED_INSTANCE = "org.openide.options.SystemOption";//NOI18N
    
    private String systemOptionInstanceName;
    private boolean types;
    
    private SystemOptionsParser(final String systemOptionInstanceName, final boolean types) {
        this.systemOptionInstanceName = systemOptionInstanceName;
        this.types = types;
    }
    
    public static DefaultResult parse(FileObject settingsFo, boolean types) throws IOException, ClassNotFoundException {
        SettingsRecognizer instance = getRecognizer(settingsFo);
        
        SystemOptionsParser rImpl = null;
        InputStream is = instance.getSerializedInstance();
        try {
            SerParser sp = new SerParser(is);
            SerParser.Stream s = sp.parse();
            rImpl = new SystemOptionsParser(instance.instanceName(), types);
            DefaultResult ret = (DefaultResult)rImpl.processContent(s.contents.iterator(), false);
            ret.setModuleName(instance.getCodeNameBase().replace('.','/'));
            return ret;
        } finally {
            is.close();
        }
    }
    
    private Result processContent(final Iterator<Object> it, final boolean reachedWriteReplace) {
        for (; it.hasNext();) {
            Object elem = it.next();
            if (!reachedWriteReplace && elem instanceof SerParser.ObjectWrapper) {
                SerParser.ObjectWrapper ow = (SerParser.ObjectWrapper)elem;
                String name = ow.classdesc.name;
                if (name.endsWith("org.openide.util.SharedClassObject$WriteReplace;")) {//NOI18N
                    return processContent(ow.data.iterator(), true);
                }
            } else if (reachedWriteReplace && elem instanceof SerParser.NameValue ) {
                SerParser.NameValue nv = (SerParser.NameValue)elem;
                if (systemOptionInstanceName.equals(nv.value)) {
                        Result result = ContentProcessor.parseContent(systemOptionInstanceName, types, it);
                    return result;
                }
            }
        }
        return null;
    }            
            
    private static SettingsRecognizer getRecognizer(final FileObject settingsFo) throws IOException {
        SettingsRecognizer recognizer = new SettingsRecognizer(false, settingsFo);
        recognizer.parse();
        
        Set instances = recognizer.getInstanceOf();
        String iName = recognizer.instanceName();
        if (!instances.contains(EXPECTED_INSTANCE)) {
            throw new IOException(iName);
        }
        return recognizer;
    }
}




