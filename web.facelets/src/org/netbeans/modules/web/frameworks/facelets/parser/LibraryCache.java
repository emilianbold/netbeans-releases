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

package org.netbeans.modules.web.frameworks.facelets.parser;

import java.io.File;
import java.util.Hashtable;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import org.netbeans.modules.web.api.webmodule.WebModule;

/**
 *
 * @author Petr Pisl
 */
public class LibraryCache {
    
    
    public static class TldCacheItem{
        /** Time, when the tld file was parsed.
         */
        private long time;
        private TLDParser.Result parserResult;
        
        public TldCacheItem(long time, TLDParser.Result parserResult){
            this.parserResult = parserResult;
            this.time = time;
        }

        public long getTime() {
            return time;
        }

        public TLDParser.Result getParserResult() {
            return parserResult;
        }
    }
    
    public static class LibraryCacheItem{
        private long time;
        private TagLibraryInfo library;
        
        public LibraryCacheItem(long time, TagLibraryInfo library){
            this.time = time;
            this.library = library;
        }

        public long getTime() {
            return time;
        }

        public TagLibraryInfo getLibrary() {
            return library;
        }
        
    }
    
}
