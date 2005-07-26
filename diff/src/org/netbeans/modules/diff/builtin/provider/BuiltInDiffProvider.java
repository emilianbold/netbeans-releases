/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.diff.builtin.provider;

import java.io.IOException;
import java.io.Reader;

import org.openide.util.NbBundle;

import org.netbeans.api.diff.Difference;
import org.netbeans.spi.diff.DiffProvider;

import org.netbeans.modules.diff.builtin.provider.io.LineIndexedAccess;

/**
 *
 * @author  Martin Entlicher
 */
public class BuiltInDiffProvider extends DiffProvider {
    
    /** Creates a new instance of BuiltInDiffProvider */
    public BuiltInDiffProvider() {
    }
    
    /**
     * Get the display name of this diff provider.
     */
    public String getDisplayName() {
        return NbBundle.getMessage(BuiltInDiffProvider.class, "BuiltInDiffProvider.displayName");
    }
    
    /**
     * Get a short description of this diff provider.
     */
    public String getShortDescription() {
        return NbBundle.getMessage(BuiltInDiffProvider.class, "BuiltInDiffProvider.shortDescription");
    }
    
    /**
     * Create the differences of the content two streams.
     * @param r1 the first source
     * @param r2 the second source to be compared with the first one.
     * @return the list of differences found, instances of {@link Difference};
     *        or <code>null</code> when some error occured.
     */
    public Difference[] computeDiff(Reader r1, Reader r2) throws IOException {
        /*
        CharArrayWriter w1 = new CharArrayWriter(BUFF_LENGTH);
        CharArrayWriter w2 = new CharArrayWriter(BUFF_LENGTH);
        char[] buffer = new char[BUFF_LENGTH];
        int length;
        while((length = r1.read(buffer)) > 0) w1.write(buffer, 0, length);
        while((length = r2.read(buffer)) > 0) w2.write(buffer, 0, length);
        r1.close();
        r2.close();
        w1.close();
        w2.close();
        return Diff.diff(w1.toString(), w2.toString());
         */
        LineIndexedAccess l1 = new LineIndexedAccess(r1);
        LineIndexedAccess l2 = new LineIndexedAccess(r2);
        return LineDiff.diff(l1, l2);
    }
    
}
