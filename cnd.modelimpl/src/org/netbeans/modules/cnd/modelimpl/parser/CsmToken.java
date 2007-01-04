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

package org.netbeans.modules.cnd.modelimpl.parser;

import java.io.*;
import org.netbeans.modules.cnd.apt.impl.support.APTBaseToken;

/**
 *
 * @author Dmitriy Ivanov
 */
public class CsmToken extends APTBaseToken implements Serializable {

    public static final CsmToken NIL = new CsmToken();

    /** Creates a new instance of CsmToken */
    public CsmToken() {
        super();
//        type = 0;
//        text = "";
//        line = 0;
//        col = 0;
//        offset = 0;
    }

//    public Object readResolve() throws ObjectStreamException {
//        return this;
//    }
//    
//    public Object writeReplace() throws ObjectStreamException {
//        return this;
//    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeInt(getType());
        out.writeObject(getText());
        out.writeInt(getOffset());
        out.writeInt(getLine());
        out.writeInt(getColumn());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {       
        in.defaultReadObject();
        setType(in.readInt());
        setText((String) in.readObject());
        setOffset(in.readInt());
        setLine(in.readInt());
        setColumn(in.readInt());
    }
  
    // FIXUP: added for testing only
//    protected String file = null;
    public String getFilename() {
//            return file;
        String file = super.getFilename();
        return file;
    }

    public void setFilename(String name) {
//        this.file = name;
        super.setFilename(name);
    }
        
    public String toString() {
        return "[\"" + getText() + "\",<" + getType() + ">,line=" + getLine() + ",col=" + getColumn() + "]" + ",offset="+getOffset()+",file="+getFilename();
    }
}
