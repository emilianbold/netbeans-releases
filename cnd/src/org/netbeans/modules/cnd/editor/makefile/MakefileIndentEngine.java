/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.editor.makefile;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.modules.cnd.MIMENames;
import org.netbeans.modules.editor.FormatterIndentEngine;

import org.openide.util.HelpCtx;

/**
 * Makefile indentation engine
 */

public class MakefileIndentEngine extends FormatterIndentEngine {
    
    public static final String MAKEFILE_TYPE = "MakefileType"; // NOI18N
    
    // Makefile type isn't implemented yet
//    public static final String SOLARIS_MAKEFILE_TYPE = "SolarisMakefileType";
//    public static final String GNU_MAKEFILE_TYPE = "GNUMakefileType";
//    
//    private String type = GNU_MAKEFILE_TYPE;
    
    private final static long serialVersionUID = -5085934337015783530L;

    public MakefileIndentEngine() {
        setAcceptedMimeTypes(new String[] { MIMENames.MAKEFILE_MIME_TYPE });
	setExpandTabs(false); // This should be the default for Makefilesd
	setSpacesPerTab(8);
    }
    
    protected ExtFormatter createFormatter() {
        return new MakefileFormatter(MakefileKit.class);
    }
    
    // Makefile type isn't implemented yet
//    public String getMakefileType() {
//        return type;
//    }
//    
//    public void setMakefileType(String type) {
//        this.type = type;
//    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("Welcome_opt_indent_makefile"); // NOI18N // FIXUP
    }
    
    // Serialization
    
    private static final ObjectStreamField[] serialPersistenFields = {
        new ObjectStreamField(MAKEFILE_TYPE, String.class)
    };
    
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
//        ObjectInputStream.GetField fields = ois.readFields();
//        setMakefileType((String) fields.get(MAKEFILE_TYPE, (Object) getMakefileType()));
    }
    
    private void writeObject(ObjectOutputStream oos) throws IOException, ClassNotFoundException {
//        ObjectOutputStream.PutField fields = oos.putFields();
//        fields.put(MAKEFILE_TYPE, getMakefileType());
//        oos.writeFields();
    }
}

