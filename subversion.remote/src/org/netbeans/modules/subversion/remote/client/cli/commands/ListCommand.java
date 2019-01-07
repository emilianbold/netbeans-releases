/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.subversion.remote.client.cli.commands;

import org.netbeans.modules.subversion.remote.api.ISVNDirEntry;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.subversion.remote.api.ISVNNotifyListener;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.api.SVNNodeKind;
import org.netbeans.modules.subversion.remote.api.SVNRevision;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.client.cli.SvnCommand;
import org.openide.filesystems.FileSystem;
import org.openide.xml.XMLUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * 
 */
public class ListCommand extends SvnCommand {

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); //NOI18N

    private byte[] output;
    private final SVNUrl url;
    private final SVNRevision revision;
    private final boolean recursive;

    public ListCommand(FileSystem fileSystem, SVNUrl url, SVNRevision revision, boolean recursive) {
        super(fileSystem);
        this.url = url;
        this.revision = revision;
        this.recursive = recursive;
        dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT")); //NOI18N
    }
    
    @Override
    protected boolean hasBinaryOutput() {
        return true;
    }

    @Override
    protected boolean notifyOutput() {
        return false;
    }    
    
    @Override
    protected ISVNNotifyListener.Command getCommand() {
        return ISVNNotifyListener.Command.LS;
    }
    
    @Override
    public void prepareCommand(Arguments arguments) throws IOException {
        arguments.add("list"); //NOI18N
        if (recursive) {
            arguments.add("-R"); //NOI18N
        }        
        arguments.add("--xml"); //NOI18N
        arguments.add(revision);
        arguments.add(url); 
    }

    @Override
    public void output(byte[] bytes) {
        output = bytes;
    }
    
    public ISVNDirEntry[] getEntries() throws SVNClientException {
        if (output == null || output.length == 0) {
            return new ISVNDirEntry[0];
        }
        try {
            XMLReader saxReader = XMLUtil.createXMLReader();

            XmlEntriesHandler xmlEntriesHandler = new XmlEntriesHandler();
            saxReader.setContentHandler(xmlEntriesHandler);
            saxReader.setErrorHandler(xmlEntriesHandler);
            InputSource source = new InputSource(new ByteArrayInputStream(output));

            saxReader.parse(source);
            return xmlEntriesHandler.getEntryAttributes();
            
        } catch (SAXException ex) {
            throw new SVNClientException(ex);
        } catch (IOException ex) {
            throw new SVNClientException(ex);
        }
        
    }
    
    private static class XmlEntriesHandler extends DefaultHandler {
        
        private static final String LIST_ELEMENT_NAME   = "list";   // NOI18N
        private static final String ENTRY_ELEMENT_NAME  = "entry";  // NOI18N
        private static final String NAME_ELEMENT_NAME   = "name";   // NOI18N
        private static final String SIZE_ELEMENT_NAME   = "size";   // NOI18N
        private static final String COMMIT_ELEMENT_NAME = "commit"; // NOI18N
        private static final String AUTHOR_ELEMENT_NAME = "author"; // NOI18N        
        private static final String DATE_ELEMENT_NAME   = "date";   // NOI18N        
        
        private static final String KIND_ATTRIBUTE      = "kind";   // NOI18N        
        private static final String PATH_ATTRIBUTE      = "path";   // NOI18N        
        private static final String REVISION_ATTRIBUTE  = "revision";   // NOI18N

        private static final String REVISION_ATTR       = "revision_attr"; //NOI18N
        private static final String KIND_ATTR           = "kind_attr"; //NOI18N
        private static final String PATH_ATTR           = "path_attr"; //NOI18N
        
        private final List<ISVNDirEntry> entries = new ArrayList<>();        
//        <?xml version="1.0"?>
//        <lists>
//        <list
//            path="file:///foo">
//            <entry kind="file">
//                <name>Bar1.java</name>
//                <commit revision="2">
//                    <author>Hugo</author>
//                    <date>2008-02-31T16:48:08.105011Z</date>
//                </commit>
//            </entry>
//            <entry kind="file">
//                <name>Bar2.java</name>
//                <commit revision="2">
//                    <author>Hugo</author>
//                    <date>2008-02-31T16:48:08.105011Z</date>
//                </commit>
//            </entry>        
//        </list>
//        </lists>
            
        
        private Map<String, String> values;
        private String tag;               

        @Override
        public void startElement(String uri, String localName, String qName, Attributes elementAttributes) throws SAXException {            
            tag = qName.trim();                
            if (ENTRY_ELEMENT_NAME.equals(qName)) {                        
                values = new HashMap<>();
                values.put(KIND_ATTR, elementAttributes.getValue(KIND_ATTRIBUTE));
            } else if (COMMIT_ELEMENT_NAME.equals(qName)) {                                
                values.put(REVISION_ATTR, elementAttributes.getValue(REVISION_ATTRIBUTE));
            }
            if(values != null) {
                values.put(tag, "");
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if(values == null || tag == null) {
                return;
            }
            String s = toString(length, ch, start);
            values.put(tag, s);
        }                
        
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            tag = null;
            if (ENTRY_ELEMENT_NAME.equals(qName)) {                                
                if(values != null) {
                    String name = values.get(NAME_ELEMENT_NAME);
                    if (name == null) {
                        throw new SAXException("'name' tag expected under 'entry'"); //NOI18N
                    }
                                                                    
                    String commit = values.get(COMMIT_ELEMENT_NAME);
                    if (commit == null) {
                        throw new SAXException("'commit' tag expected under 'entry'"); //NOI18N
                    }
                    
                    String author = values.get(AUTHOR_ELEMENT_NAME);
                    
                    Date date = null;
                    String dateValue = values.get(DATE_ELEMENT_NAME);
                    if(dateValue != null) {
                        try {
                            date = dateFormat.parse(dateValue);
                        } catch (ParseException ex) {
                            // ignore
                        }
                    }
                    
                    SVNRevision.Number revision = null;
                    String revisionValue = values.get(REVISION_ATTR);
                    if(revisionValue != null && !revisionValue.trim().equals("")) { //NOI18N
                        try {
                            revision = new SVNRevision.Number(Long.parseLong(revisionValue));
                        } catch (NumberFormatException e) {
                            revision = new SVNRevision.Number(-1);
                        }
                    }
                
                    long size = 0;
                    String kindValue = values.get(KIND_ATTR);
                    SVNNodeKind kind = SVNNodeKind.UNKNOWN;
                    if ("file".equals(kindValue)) { //NOI18N
                        
                        kind = SVNNodeKind.FILE;					
                        
                        String sizeValue = values.get(SIZE_ELEMENT_NAME);
                        if (sizeValue == null) {
                            throw new SAXException("'size' tag expected under 'entry'"); //NOI18N
                        }                        
                        try {
                            size = Long.parseLong(sizeValue);
                        } catch (NumberFormatException ex) {
                            // ignore
                        }
                        
                    } else if ("dir".equals(kindValue)) { //NOI18N
                        kind = SVNNodeKind.DIR;
                    }
                                        
                    entries.add(new DirEntry(name, date, revision, false, author, kind, size));
                }
                values = null;
            } 
        }
                
        @Override
        public void error(SAXParseException e) throws SAXException {
            throw e;
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            throw e;
        }

        public ISVNDirEntry[] getEntryAttributes() {            
            return entries != null ? entries.toArray(new ISVNDirEntry[entries.size()]) : new ISVNDirEntry[] {} ;
        }

        private String toString(int length, char[] ch, int start) {
            char[] c = new char[length];
            System.arraycopy(ch, start, c, 0, length);
            return new String(c);
        }
    }       
    
    private static class DirEntry implements ISVNDirEntry {

        private final String path;
        private final Date lastChangedDate;
        private final SVNRevision.Number lastChangedRevision;
        private final boolean hasProps;
        private final String lastCommitAuthor;
        private final SVNNodeKind kind;
        private final long size;

        public DirEntry(String path, Date lastChangedDate, SVNRevision.Number lastChangedRevision, boolean hasProps, String lastCommitAuthor, SVNNodeKind kind, long size) {
            this.path = path;
            this.lastChangedDate = lastChangedDate;
            this.lastChangedRevision = lastChangedRevision;
            this.hasProps = hasProps;
            this.lastCommitAuthor = lastCommitAuthor;
            this.kind = kind;
            this.size = size;
        }
        
        @Override
        public String getPath() {
            return path;
        }

        @Override
        public Date getLastChangedDate() {
            return lastChangedDate;
        }

        @Override
        public SVNRevision.Number getLastChangedRevision() {
            return lastChangedRevision;
        }

        @Override
        public boolean getHasProps() {
            return hasProps;
        }

        @Override
        public String getLastCommitAuthor() {
            return lastCommitAuthor;
        }

        @Override
        public SVNNodeKind getNodeKind() {
            return kind;
        }

        @Override
        public long getSize() {
            return size;
        }
        
    }
}
