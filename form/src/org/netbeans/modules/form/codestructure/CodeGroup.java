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

package org.netbeans.modules.form.codestructure;

import java.util.Iterator;

/**
 * @author Tomas Pavek
 */

public interface CodeGroup {

    public void addStatement(CodeStatement statement);
    public void addStatement(int index, CodeStatement statement);

    public void addGroup(CodeGroup group);
    public void addGroup(int index, CodeGroup group);

    public CodeStatement getStatement(int index);

    public int indexOf(Object object);

    public void remove(Object object);
    public void remove(int index);
    public void removeAll();

    public Iterator getStatementsIterator();
}
