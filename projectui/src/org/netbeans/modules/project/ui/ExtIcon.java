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

package org.netbeans.modules.project.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.openide.util.Utilities;


/**
 * Class for persisting icons
 * @author Milan Kubec, mkleint
 */
public class ExtIcon {

    Icon icon;

    public ExtIcon() {
    }

    public ExtIcon(byte[] content) {
        ObjectInputStream objin = null;
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(content);
            objin = new ObjectInputStream(in);
            Object obj = objin.readObject();
            if (obj instanceof Icon) {
                setIcon((Icon)obj);
            }
        } catch (Exception ex) {
            setIcon(new ImageIcon(Utilities.loadImage("org/openide/resources/actions/empty.gif"))); //NOI18N
        } finally {
            try {
                objin.close();
            } catch (IOException ex) {
            }
        }
    }

    public void setIcon(Icon icn) {
        icon = icn;
    }

    public Icon getIcon() {
        return icon;
    }

    public byte[] getBytes() throws IOException {
        if (getIcon() == null) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(out);
        objOut.writeObject(getIcon());
        objOut.close();
        return out.toByteArray();
    }
}