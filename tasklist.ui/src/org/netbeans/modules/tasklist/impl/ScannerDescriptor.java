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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tasklist.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.spi.tasklist.FileTaskScanner;
import org.netbeans.spi.tasklist.PushTaskScanner;

/**
 *
 * @author S. Aubrecht
 */
public abstract class ScannerDescriptor implements Comparable<ScannerDescriptor> {

    public abstract String getType();
    
    public abstract String getDisplayName();
    
    public abstract String getDescription();
    
    public abstract String getOptionsPath();
    
    
    public int compareTo( ScannerDescriptor sd ) {
        return getDisplayName().compareTo( sd.getDisplayName() );
    }
    
    public static String getType( FileTaskScanner scanner ) {
        return scanner.getClass().getName();
    }
    
    public static String getType( PushTaskScanner scanner ) {
        return scanner.getClass().getName();
    }
    
    public static List<? extends ScannerDescriptor> getDescriptors() {
        List<? extends FileTaskScanner> fileScanners = ScannerList.getFileScannerList().getScanners();
        List<? extends PushTaskScanner> simpleScanners = ScannerList.getPushScannerList().getScanners();
        
        ArrayList<ScannerDescriptor> res = new ArrayList<ScannerDescriptor>( fileScanners.size() + simpleScanners.size() );
        for( FileTaskScanner s : fileScanners ) {
            res.add( new FileDescriptor( s ) );
        }
        for( PushTaskScanner s : simpleScanners ) {
            res.add( new PushDescriptor( s ) );
        }
        Collections.sort( res );
        return res;
    }
    
    private static class FileDescriptor extends ScannerDescriptor {
        private FileTaskScanner scanner;
        public FileDescriptor( FileTaskScanner scanner ) {
            assert null != scanner;
            this.scanner = scanner;
        }
    
        public String getDisplayName() {
            return Accessor.getDisplayName( scanner );
        }

        public String getDescription() {
            return Accessor.getDescription( scanner );
        }

        public String getOptionsPath() {
            return Accessor.getOptionsPath( scanner );
        }
    
        public String getType() {
            return getType( scanner );
        }
    }
    
    private static class PushDescriptor extends ScannerDescriptor {
        private PushTaskScanner scanner;
        public PushDescriptor( PushTaskScanner scanner ) {
            assert null != scanner;
            this.scanner = scanner;
        }
    
        public String getDisplayName() {
            return Accessor.getDisplayName( scanner );
        }

        public String getDescription() {
            return Accessor.getDescription( scanner );
        }

        public String getOptionsPath() {
            return Accessor.getOptionsPath( scanner );
        }
    
        public String getType() {
            return getType( scanner );
        }
    }
}
