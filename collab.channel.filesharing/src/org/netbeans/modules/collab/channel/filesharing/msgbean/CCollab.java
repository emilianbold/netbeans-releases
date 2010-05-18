/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
/**
 *        This generated bean class CCollab
 *        matches the schema element 'c:collab'.
 *
 *        Generated on Mon Sep 27 16:53:13 PDT 2004
 *
 *        This class matches the root element of the XML Schema,
 *        and is the root of the bean graph.
 *
 *         c:collab : CCollab
 *                 version : java.lang.String
 *                 | ch:send-file : SendFile?
 *                 |         file-groups : FileGroups
 *                 |                 file-group : FileGroup[1,n]
 *                 |                         file-group-name : java.lang.String
 *                 |                         user : User
 *                 |                                 id : java.lang.String
 *                 |                                 name : java.lang.String?
 *                 |                                 description : java.lang.String?
 *                 |                         file-name : java.lang.String[1,n]
 *                 |         send-file-data : SendFileData[1,n]
 *                 |                 file-data : FileData
 *                 |                         file-name : java.lang.String
 *                 |                         content-type : java.lang.String
 *                 |                         description : java.lang.String?
 *                 |                 choose-line-region-function : boolean
 *                 |                 | line-region-function : LineRegionFunction
 *                 |                 |         funtion-name : java.lang.String
 *                 |                 |         arguments : java.lang.String[1,n]
 *                 |                 | line-region : LineRegion[1,n]
 *                 |                 |         region-name : java.lang.String
 *                 |                 content : Content
 *                 |                         encoding : java.lang.String
 *                 |                         digest : java.lang.String
 *                 |                         data : java.lang.String
 *                 | ch:file-changed : FileChanged?
 *                 |         file-groups : FileGroups
 *                 |                 file-group : FileGroup[1,n]
 *                 |                         file-group-name : java.lang.String
 *                 |                         user : User
 *                 |                                 id : java.lang.String
 *                 |                                 name : java.lang.String?
 *                 |                                 description : java.lang.String?
 *                 |                         file-name : java.lang.String[1,n]
 *                 |         file-changed-data : FileChangedData[1,n]
 *                 |                 file-name : java.lang.String
 *                 |                 digest : java.lang.String
 *                 |                 region-changed : RegionChanged[1,n]
 *                 |                         | text-region-changed : TextRegionChanged
 *                 |                         |         text-region : TextRegion
 *                 |                         |                 region-name : java.lang.String
 *                 |                         |                 begin-offset : java.math.BigInteger
 *                 |                         |                 length : java.math.BigInteger
 *                 |                         |         text-change : TextChange
 *                 |                         |                 | change-texts : ChangeTexts
 *                 |                         |                 |         remove-texts : RemoveTexts
 *                 |                         |                 |                 remove-text : RemoveText[1,n]
 *                 |                         |                 |                         offset : java.math.BigInteger
 *                 |                         |                 |         insert-texts : InsertTexts
 *                 |                         |                 |                 insert-text : InsertText[1,n]
 *                 |                         |                 |                         offset : java.math.BigInteger
 *                 |                         |                 |                         content : Content
 *                 |                         |                 |                                 encoding : java.lang.String
 *                 |                         |                 |                                 digest : java.lang.String
 *                 |                         |                 |                                 data : java.lang.String
 *                 |                         |                 | content : Content
 *                 |                         |                 |         encoding : java.lang.String
 *                 |                         |                 |         digest : java.lang.String
 *                 |                         |                 |         data : java.lang.String
 *                 |                         | java-region-changed : JavaRegionChanged
 *                 |                         |         java-region : JavaRegion
 *                 |                         |                 region-name : java.lang.String
 *                 |                         |                 begin-offset : java.math.BigInteger
 *                 |                         |                 length : java.math.BigInteger
 *                 |                         |         java-change : JavaChange
 *                 |                         |                 | change-texts : ChangeTexts
 *                 |                         |                 |         remove-texts : RemoveTexts
 *                 |                         |                 |                 remove-text : RemoveText[1,n]
 *                 |                         |                 |                         offset : java.math.BigInteger
 *                 |                         |                 |         insert-texts : InsertTexts
 *                 |                         |                 |                 insert-text : InsertText[1,n]
 *                 |                         |                 |                         offset : java.math.BigInteger
 *                 |                         |                 |                         content : Content
 *                 |                         |                 |                                 encoding : java.lang.String
 *                 |                         |                 |                                 digest : java.lang.String
 *                 |                         |                 |                                 data : java.lang.String
 *                 |                         |                 | content : Content
 *                 |                         |                 |         encoding : java.lang.String
 *                 |                         |                 |         digest : java.lang.String
 *                 |                         |                 |         data : java.lang.String
 *                 |                         | line-region-changed : LineRegionChanged
 *                 |                         |         line-region : LineRegion
 *                 |                         |                 region-name : java.lang.String
 *                 |                         |         line-change : LineChange
 *                 |                         |                 content : Content
 *                 |                         |                         encoding : java.lang.String
 *                 |                         |                         digest : java.lang.String
 *                 |                         |                         data : java.lang.String
 *                 | ch:lock-region : LockRegion?
 *                 |         file-groups : FileGroups
 *                 |                 file-group : FileGroup[1,n]
 *                 |                         file-group-name : java.lang.String
 *                 |                         user : User
 *                 |                                 id : java.lang.String
 *                 |                                 name : java.lang.String?
 *                 |                                 description : java.lang.String?
 *                 |                         file-name : java.lang.String[1,n]
 *                 |         lock-region-data : LockRegionData[1,n]
 *                 |                 file-name : java.lang.String
 *                 |                 line-region : LineRegion[1,n]
 *                 |                         | region-name : java.lang.String
 *                 |                 | text-region : TextRegion
 *                 |                 |         region-name : java.lang.String
 *                 |                 |         begin-offset : java.math.BigInteger
 *                 |                 |         length : java.math.BigInteger
 *                 |                 | java-region : JavaRegion
 *                 |                 |         region-name : java.lang.String
 *                 |                 |         begin-offset : java.math.BigInteger
 *                 |                 |         length : java.math.BigInteger
 *                 |                 content : Content
 *                 |                         encoding : java.lang.String
 *                 |                         digest : java.lang.String
 *                 |                         data : java.lang.String
 *                 | ch:unlock-region : UnlockRegion?
 *                 |         file-groups : FileGroups
 *                 |                 file-group : FileGroup[1,n]
 *                 |                         file-group-name : java.lang.String
 *                 |                         user : User
 *                 |                                 id : java.lang.String
 *                 |                                 name : java.lang.String?
 *                 |                                 description : java.lang.String?
 *                 |                         file-name : java.lang.String[1,n]
 *                 |         unlock-region-data : UnlockRegionData[1,n]
 *                 |                 file-name : java.lang.String
 *                 |                 line-region : LineRegion[1,n]
 *                 |                         | region-name : java.lang.String
 *                 |                 | text-region : TextRegion
 *                 |                 |         region-name : java.lang.String
 *                 |                 |         begin-offset : java.math.BigInteger
 *                 |                 |         length : java.math.BigInteger
 *                 |                 | java-region : JavaRegion
 *                 |                 |         region-name : java.lang.String
 *                 |                 |         begin-offset : java.math.BigInteger
 *                 |                 |         length : java.math.BigInteger
 *                 |                 content : Content
 *                 |                         encoding : java.lang.String
 *                 |                         digest : java.lang.String
 *                 |                         data : java.lang.String
 *                 | ch:join-filesharing : JoinFilesharing?
 *                 |         | begin-join : boolean?
 *                 |         | end-join : boolean?
 *                 |         user : User
 *                 |                 id : java.lang.String
 *                 |                 name : java.lang.String?
 *                 |                 description : java.lang.String?
 *                 | ch:pause-filesharing : PauseFilesharing?
 *                 |         join-user : JoinUser
 *                 |                 user : User
 *                 |                         id : java.lang.String
 *                 |                         name : java.lang.String?
 *                 |                         description : java.lang.String?
 *                 |         moderator : Moderator
 *                 |                 users : Users
 *                 |                         user : User[0,n]
 *                 |                                 id : java.lang.String
 *                 |                                 name : java.lang.String?
 *                 |                                 description : java.lang.String?
 *                 |         file-owners : FileOwners
 *                 |                 users : Users
 *                 |                         user : User[0,n]
 *                 |                                 id : java.lang.String
 *                 |                                 name : java.lang.String?
 *                 |                                 description : java.lang.String?
 *                 |         users : Users
 *                 |                 user : User[0,n]
 *                 |                         id : java.lang.String
 *                 |                         name : java.lang.String?
 *                 |                         description : java.lang.String?
 *                 |         file-groups : FileGroups
 *                 |                 file-group : FileGroup[1,n]
 *                 |                         file-group-name : java.lang.String
 *                 |                         user : User
 *                 |                                 id : java.lang.String
 *                 |                                 name : java.lang.String?
 *                 |                                 description : java.lang.String?
 *                 |                         file-name : java.lang.String[1,n]
 *                 | ch:resume-filesharing : ResumeFilesharing?
 *                 |         moderator : Moderator
 *                 |                 users : Users
 *                 |                         user : User[0,n]
 *                 |                                 id : java.lang.String
 *                 |                                 name : java.lang.String?
 *                 |                                 description : java.lang.String?
 *                 | ch:leave-filesharing : LeaveFilesharing?
 *                 |         user : User
 *                 |                 id : java.lang.String
 *                 |                 name : java.lang.String?
 *                 |                 description : java.lang.String?
 *                 |         new-moderator : Moderator
 *                 |                 users : Users
 *                 |                         user : User[0,n]
 *                 |                                 id : java.lang.String
 *                 |                                 name : java.lang.String?
 *                 |                                 description : java.lang.String?
 *                 |         new-file-owner : NewFileOwner
 *                 |                 users : Users
 *                 |                         user : User[0,n]
 *                 |                                 id : java.lang.String
 *                 |                                 name : java.lang.String?
 *                 |                                 description : java.lang.String?
 *                 |         file-groups : FileGroups
 *                 |                 file-group : FileGroup[1,n]
 *                 |                         file-group-name : java.lang.String
 *                 |                         user : User
 *                 |                                 id : java.lang.String
 *                 |                                 name : java.lang.String?
 *                 |                                 description : java.lang.String?
 *                 |                         file-name : java.lang.String[1,n]
 *                 | ch:commands : Commands?
 *                 |         filesystem-command : FilesystemCommand
 *                 |                 delete-file : DeleteFile
 *                 |                         file-name : java.lang.String
 *
 */
package org.netbeans.modules.collab.channel.filesharing.msgbean;

public class CCollab {
    private java.lang.String _Version;
    private SendFile _ChSendFile;
    private FileChanged _ChFileChanged;
    private LockRegion _ChLockRegion;
    private UnlockRegion _ChUnlockRegion;
    private JoinFilesharing _ChJoinFilesharing;
    private PauseFilesharing _ChPauseFilesharing;
    private ResumeFilesharing _ChResumeFilesharing;
    private LeaveFilesharing _ChLeaveFilesharing;
    private Commands _ChCommands;

    public CCollab() {
        _Version = "";
    }

    // Deep copy
    public CCollab(org.netbeans.modules.collab.channel.filesharing.msgbean.CCollab source) {
        _Version = source._Version;
        _ChSendFile = new org.netbeans.modules.collab.channel.filesharing.msgbean.SendFile(source._ChSendFile);
        _ChFileChanged = new org.netbeans.modules.collab.channel.filesharing.msgbean.FileChanged(source._ChFileChanged);
        _ChLockRegion = new org.netbeans.modules.collab.channel.filesharing.msgbean.LockRegion(source._ChLockRegion);
        _ChUnlockRegion = new org.netbeans.modules.collab.channel.filesharing.msgbean.UnlockRegion(
                source._ChUnlockRegion
            );
        _ChJoinFilesharing = new org.netbeans.modules.collab.channel.filesharing.msgbean.JoinFilesharing(
                source._ChJoinFilesharing
            );
        _ChPauseFilesharing = new org.netbeans.modules.collab.channel.filesharing.msgbean.PauseFilesharing(
                source._ChPauseFilesharing
            );
        _ChResumeFilesharing = new org.netbeans.modules.collab.channel.filesharing.msgbean.ResumeFilesharing(
                source._ChResumeFilesharing
            );
        _ChLeaveFilesharing = new org.netbeans.modules.collab.channel.filesharing.msgbean.LeaveFilesharing(
                source._ChLeaveFilesharing
            );
        _ChCommands = new org.netbeans.modules.collab.channel.filesharing.msgbean.Commands(source._ChCommands);
    }

    // This attribute is mandatory
    public void setVersion(java.lang.String value) {
        _Version = value;
    }

    public java.lang.String getVersion() {
        return _Version;
    }

    // This attribute is optional
    public void setChSendFile(org.netbeans.modules.collab.channel.filesharing.msgbean.SendFile value) {
        _ChSendFile = value;
    }

    public org.netbeans.modules.collab.channel.filesharing.msgbean.SendFile getChSendFile() {
        return _ChSendFile;
    }

    // This attribute is optional
    public void setChFileChanged(org.netbeans.modules.collab.channel.filesharing.msgbean.FileChanged value) {
        _ChFileChanged = value;
    }

    public org.netbeans.modules.collab.channel.filesharing.msgbean.FileChanged getChFileChanged() {
        return _ChFileChanged;
    }

    // This attribute is optional
    public void setChLockRegion(org.netbeans.modules.collab.channel.filesharing.msgbean.LockRegion value) {
        _ChLockRegion = value;
    }

    public org.netbeans.modules.collab.channel.filesharing.msgbean.LockRegion getChLockRegion() {
        return _ChLockRegion;
    }

    // This attribute is optional
    public void setChUnlockRegion(org.netbeans.modules.collab.channel.filesharing.msgbean.UnlockRegion value) {
        _ChUnlockRegion = value;
    }

    public org.netbeans.modules.collab.channel.filesharing.msgbean.UnlockRegion getChUnlockRegion() {
        return _ChUnlockRegion;
    }

    // This attribute is optional
    public void setChJoinFilesharing(org.netbeans.modules.collab.channel.filesharing.msgbean.JoinFilesharing value) {
        _ChJoinFilesharing = value;
    }

    public org.netbeans.modules.collab.channel.filesharing.msgbean.JoinFilesharing getChJoinFilesharing() {
        return _ChJoinFilesharing;
    }

    // This attribute is optional
    public void setChPauseFilesharing(org.netbeans.modules.collab.channel.filesharing.msgbean.PauseFilesharing value) {
        _ChPauseFilesharing = value;
    }

    public org.netbeans.modules.collab.channel.filesharing.msgbean.PauseFilesharing getChPauseFilesharing() {
        return _ChPauseFilesharing;
    }

    // This attribute is optional
    public void setChResumeFilesharing(org.netbeans.modules.collab.channel.filesharing.msgbean.ResumeFilesharing value) {
        _ChResumeFilesharing = value;
    }

    public org.netbeans.modules.collab.channel.filesharing.msgbean.ResumeFilesharing getChResumeFilesharing() {
        return _ChResumeFilesharing;
    }

    // This attribute is optional
    public void setChLeaveFilesharing(org.netbeans.modules.collab.channel.filesharing.msgbean.LeaveFilesharing value) {
        _ChLeaveFilesharing = value;
    }

    public org.netbeans.modules.collab.channel.filesharing.msgbean.LeaveFilesharing getChLeaveFilesharing() {
        return _ChLeaveFilesharing;
    }

    // This attribute is optional
    public void setChCommands(org.netbeans.modules.collab.channel.filesharing.msgbean.Commands value) {
        _ChCommands = value;
    }

    public org.netbeans.modules.collab.channel.filesharing.msgbean.Commands getChCommands() {
        return _ChCommands;
    }

    public void write(java.io.OutputStream out) throws java.io.IOException {
        write(out, null);
    }

    public void write(java.io.OutputStream out, String encoding)
    throws java.io.IOException {
        java.io.Writer w;

        if (encoding == null) {
            encoding = "UTF-8"; // NOI18N
        }

        w = new java.io.BufferedWriter(new java.io.OutputStreamWriter(out, encoding));
        write(w, encoding);
        w.flush();
    }

    // Print this Java Bean to @param out including an XML header.
    // @param encoding is the encoding style that @param out was opened with.
    public void write(java.io.Writer out, String encoding)
    throws java.io.IOException {
        out.write("<?xml version='1.0'"); // NOI18N

        if (encoding != null) {
            out.write(" encoding='" + encoding + "'"); // NOI18N
        }

        out.write(" ?>\n"); // NOI18N
        writeNode(out, "c:collab", ""); // NOI18N
    }

    public void writeNode(java.io.Writer out, String nodeName, String indent)
    throws java.io.IOException {
        out.write(indent);
        out.write("<");
        out.write(nodeName);
        out.write(" xmlns='"); // NOI18N
        out.write("http://sun.com/ns/collab/dev/1_0/filesharing"); // NOI18N
        out.write("'"); // NOI18N
        out.write(">\n");

        String nextIndent = indent + "	";

        if (_Version != null) {
            out.write(nextIndent);
            out.write("<version"); // NOI18N
            out.write(">"); // NOI18N
            org.netbeans.modules.collab.channel.filesharing.msgbean.CCollab.writeXML(out, _Version, false);
            out.write("</version>\n"); // NOI18N
        }

        if (_ChSendFile != null) {
            _ChSendFile.writeNode(out, "ch:send-file", nextIndent);
        }

        if (_ChFileChanged != null) {
            _ChFileChanged.writeNode(out, "ch:file-changed", nextIndent);
        }

        if (_ChLockRegion != null) {
            _ChLockRegion.writeNode(out, "ch:lock-region", nextIndent);
        }

        if (_ChUnlockRegion != null) {
            _ChUnlockRegion.writeNode(out, "ch:unlock-region", nextIndent);
        }

        if (_ChJoinFilesharing != null) {
            _ChJoinFilesharing.writeNode(out, "ch:join-filesharing", nextIndent);
        }

        if (_ChPauseFilesharing != null) {
            _ChPauseFilesharing.writeNode(out, "ch:pause-filesharing", nextIndent);
        }

        if (_ChResumeFilesharing != null) {
            _ChResumeFilesharing.writeNode(out, "ch:resume-filesharing", nextIndent);
        }

        if (_ChLeaveFilesharing != null) {
            _ChLeaveFilesharing.writeNode(out, "ch:leave-filesharing", nextIndent);
        }

        if (_ChCommands != null) {
            _ChCommands.writeNode(out, "ch:commands", nextIndent);
        }

        out.write(indent);
        out.write("</" + nodeName + ">\n");
    }

    public static CCollab read(java.io.InputStream in)
    throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
        return read(new org.xml.sax.InputSource(in), false, null, null);
    }

    // Warning: in readNoEntityResolver character and entity references will
    // not be read from any DTD in the XML source.
    // However, this way is faster since no DTDs are looked up
    // (possibly skipping network access) or parsed.
    public static CCollab readNoEntityResolver(java.io.InputStream in)
    throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
        return read(
            new org.xml.sax.InputSource(in), false,
            new org.xml.sax.EntityResolver() {
                public org.xml.sax.InputSource resolveEntity(String publicId, String systemId) {
                    java.io.ByteArrayInputStream bin = new java.io.ByteArrayInputStream(new byte[0]);

                    return new org.xml.sax.InputSource(bin);
                }
            }, null
        );
    }

    public static CCollab read(
        org.xml.sax.InputSource in, boolean validate, org.xml.sax.EntityResolver er, org.xml.sax.ErrorHandler eh
    ) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf.setValidating(validate);

        javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();

        if (er != null) {
            db.setEntityResolver(er);
        }

        if (eh != null) {
            db.setErrorHandler(eh);
        }

        org.w3c.dom.Document doc = db.parse(in);

        return read(doc);
    }

    public static CCollab read(org.w3c.dom.Document document) {
        CCollab aCCollab = new CCollab();
        aCCollab.readNode(document.getDocumentElement());

        return aCCollab;
    }

    public void readNode(org.w3c.dom.Node node) {
        org.w3c.dom.NodeList children = node.getChildNodes();

        for (int i = 0, size = children.getLength(); i < size; ++i) {
            org.w3c.dom.Node childNode = children.item(i);
            String childNodeName = ((childNode.getLocalName() == null) ? childNode.getNodeName().intern()
                                                                       : childNode.getLocalName().intern());
            String childNodeValue = "";

            if (childNode.getFirstChild() != null) {
                childNodeValue = childNode.getFirstChild().getNodeValue();
            }

            if (childNodeName == "version") {
                _Version = childNodeValue;
            } else if (childNodeName == "ch:send-file") {
                _ChSendFile = new org.netbeans.modules.collab.channel.filesharing.msgbean.SendFile();
                _ChSendFile.readNode(childNode);
            } else if (childNodeName == "ch:file-changed") {
                _ChFileChanged = new org.netbeans.modules.collab.channel.filesharing.msgbean.FileChanged();
                _ChFileChanged.readNode(childNode);
            } else if (childNodeName == "ch:lock-region") {
                _ChLockRegion = new org.netbeans.modules.collab.channel.filesharing.msgbean.LockRegion();
                _ChLockRegion.readNode(childNode);
            } else if (childNodeName == "ch:unlock-region") {
                _ChUnlockRegion = new org.netbeans.modules.collab.channel.filesharing.msgbean.UnlockRegion();
                _ChUnlockRegion.readNode(childNode);
            } else if (childNodeName == "ch:join-filesharing") {
                _ChJoinFilesharing = new org.netbeans.modules.collab.channel.filesharing.msgbean.JoinFilesharing();
                _ChJoinFilesharing.readNode(childNode);
            } else if (childNodeName == "ch:pause-filesharing") {
                _ChPauseFilesharing = new org.netbeans.modules.collab.channel.filesharing.msgbean.PauseFilesharing();
                _ChPauseFilesharing.readNode(childNode);
            } else if (childNodeName == "ch:resume-filesharing") {
                _ChResumeFilesharing = new org.netbeans.modules.collab.channel.filesharing.msgbean.ResumeFilesharing();
                _ChResumeFilesharing.readNode(childNode);
            } else if (childNodeName == "ch:leave-filesharing") {
                _ChLeaveFilesharing = new org.netbeans.modules.collab.channel.filesharing.msgbean.LeaveFilesharing();
                _ChLeaveFilesharing.readNode(childNode);
            } else if (childNodeName == "ch:commands") {
                _ChCommands = new org.netbeans.modules.collab.channel.filesharing.msgbean.Commands();
                _ChCommands.readNode(childNode);
            } else {
                // Found extra unrecognized childNode
            }
        }
    }

    // Takes some text to be printed into an XML stream and escapes any
    // characters that might make it invalid XML (like '<').
    public static void writeXML(java.io.Writer out, String msg)
    throws java.io.IOException {
        writeXML(out, msg, true);
    }

    public static void writeXML(java.io.Writer out, String msg, boolean attribute)
    throws java.io.IOException {
        if (msg == null) {
            return;
        }

        int msgLength = msg.length();

        for (int i = 0; i < msgLength; ++i) {
            char c = msg.charAt(i);
            writeXML(out, c, attribute);
        }
    }

    public static void writeXML(java.io.Writer out, char msg, boolean attribute)
    throws java.io.IOException {
        if (msg == '&') {
            out.write("&amp;");
        } else if (msg == '<') {
            out.write("&lt;");
        } else if (msg == '>') {
            out.write("&gt;");
        } else if (attribute && (msg == '"')) {
            out.write("&quot;");
        } else if (attribute && (msg == '\'')) {
            out.write("&apos;");
        } else if (attribute && (msg == '\n')) {
            out.write("&#xA;");
        } else if (attribute && (msg == '\t')) {
            out.write("&#x9;");
        } else {
            out.write(msg);
        }
    }

    public void changePropertyByName(String name, Object value) {
        if (name == null) {
            return;
        }

        name = name.intern();

        if (name == "version") {
            setVersion((java.lang.String) value);
        } else if (name == "chSendFile") {
            setChSendFile((SendFile) value);
        } else if (name == "chFileChanged") {
            setChFileChanged((FileChanged) value);
        } else if (name == "chLockRegion") {
            setChLockRegion((LockRegion) value);
        } else if (name == "chUnlockRegion") {
            setChUnlockRegion((UnlockRegion) value);
        } else if (name == "chJoinFilesharing") {
            setChJoinFilesharing((JoinFilesharing) value);
        } else if (name == "chPauseFilesharing") {
            setChPauseFilesharing((PauseFilesharing) value);
        } else if (name == "chResumeFilesharing") {
            setChResumeFilesharing((ResumeFilesharing) value);
        } else if (name == "chLeaveFilesharing") {
            setChLeaveFilesharing((LeaveFilesharing) value);
        } else if (name == "chCommands") {
            setChCommands((Commands) value);
        } else {
            throw new IllegalArgumentException(name + " is not a valid property name for CCollab");
        }
    }

    public Object fetchPropertyByName(String name) {
        if (name == "version") {
            return getVersion();
        }

        if (name == "chSendFile") {
            return getChSendFile();
        }

        if (name == "chFileChanged") {
            return getChFileChanged();
        }

        if (name == "chLockRegion") {
            return getChLockRegion();
        }

        if (name == "chUnlockRegion") {
            return getChUnlockRegion();
        }

        if (name == "chJoinFilesharing") {
            return getChJoinFilesharing();
        }

        if (name == "chPauseFilesharing") {
            return getChPauseFilesharing();
        }

        if (name == "chResumeFilesharing") {
            return getChResumeFilesharing();
        }

        if (name == "chLeaveFilesharing") {
            return getChLeaveFilesharing();
        }

        if (name == "chCommands") {
            return getChCommands();
        }

        throw new IllegalArgumentException(name + " is not a valid property name for CCollab");
    }

    // Return an array of all of the properties that are beans and are set.
    public java.lang.Object[] childBeans(boolean recursive) {
        java.util.List children = new java.util.LinkedList();
        childBeans(recursive, children);

        java.lang.Object[] result = new java.lang.Object[children.size()];

        return (java.lang.Object[]) children.toArray(result);
    }

    // Put all child beans into the beans list.
    public void childBeans(boolean recursive, java.util.List beans) {
        if (_ChSendFile != null) {
            if (recursive) {
                _ChSendFile.childBeans(true, beans);
            }

            beans.add(_ChSendFile);
        }

        if (_ChFileChanged != null) {
            if (recursive) {
                _ChFileChanged.childBeans(true, beans);
            }

            beans.add(_ChFileChanged);
        }

        if (_ChLockRegion != null) {
            if (recursive) {
                _ChLockRegion.childBeans(true, beans);
            }

            beans.add(_ChLockRegion);
        }

        if (_ChUnlockRegion != null) {
            if (recursive) {
                _ChUnlockRegion.childBeans(true, beans);
            }

            beans.add(_ChUnlockRegion);
        }

        if (_ChJoinFilesharing != null) {
            if (recursive) {
                _ChJoinFilesharing.childBeans(true, beans);
            }

            beans.add(_ChJoinFilesharing);
        }

        if (_ChPauseFilesharing != null) {
            if (recursive) {
                _ChPauseFilesharing.childBeans(true, beans);
            }

            beans.add(_ChPauseFilesharing);
        }

        if (_ChResumeFilesharing != null) {
            if (recursive) {
                _ChResumeFilesharing.childBeans(true, beans);
            }

            beans.add(_ChResumeFilesharing);
        }

        if (_ChLeaveFilesharing != null) {
            if (recursive) {
                _ChLeaveFilesharing.childBeans(true, beans);
            }

            beans.add(_ChLeaveFilesharing);
        }

        if (_ChCommands != null) {
            if (recursive) {
                _ChCommands.childBeans(true, beans);
            }

            beans.add(_ChCommands);
        }
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof org.netbeans.modules.collab.channel.filesharing.msgbean.CCollab)) {
            return false;
        }

        org.netbeans.modules.collab.channel.filesharing.msgbean.CCollab inst = (org.netbeans.modules.collab.channel.filesharing.msgbean.CCollab) o;

        if (!((_Version == null) ? (inst._Version == null) : _Version.equals(inst._Version))) {
            return false;
        }

        if (!((_ChSendFile == null) ? (inst._ChSendFile == null) : _ChSendFile.equals(inst._ChSendFile))) {
            return false;
        }

        if (!((_ChFileChanged == null) ? (inst._ChFileChanged == null) : _ChFileChanged.equals(inst._ChFileChanged))) {
            return false;
        }

        if (!((_ChLockRegion == null) ? (inst._ChLockRegion == null) : _ChLockRegion.equals(inst._ChLockRegion))) {
            return false;
        }

        if (!((_ChUnlockRegion == null) ? (inst._ChUnlockRegion == null) : _ChUnlockRegion.equals(inst._ChUnlockRegion))) {
            return false;
        }

        if (
            !((_ChJoinFilesharing == null) ? (inst._ChJoinFilesharing == null)
                                               : _ChJoinFilesharing.equals(inst._ChJoinFilesharing))
        ) {
            return false;
        }

        if (
            !((_ChPauseFilesharing == null) ? (inst._ChPauseFilesharing == null)
                                                : _ChPauseFilesharing.equals(inst._ChPauseFilesharing))
        ) {
            return false;
        }

        if (
            !((_ChResumeFilesharing == null) ? (inst._ChResumeFilesharing == null)
                                                 : _ChResumeFilesharing.equals(inst._ChResumeFilesharing))
        ) {
            return false;
        }

        if (
            !((_ChLeaveFilesharing == null) ? (inst._ChLeaveFilesharing == null)
                                                : _ChLeaveFilesharing.equals(inst._ChLeaveFilesharing))
        ) {
            return false;
        }

        if (!((_ChCommands == null) ? (inst._ChCommands == null) : _ChCommands.equals(inst._ChCommands))) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result = 17;
        result = (37 * result) + ((_Version == null) ? 0 : _Version.hashCode());
        result = (37 * result) + ((_ChSendFile == null) ? 0 : _ChSendFile.hashCode());
        result = (37 * result) + ((_ChFileChanged == null) ? 0 : _ChFileChanged.hashCode());
        result = (37 * result) + ((_ChLockRegion == null) ? 0 : _ChLockRegion.hashCode());
        result = (37 * result) + ((_ChUnlockRegion == null) ? 0 : _ChUnlockRegion.hashCode());
        result = (37 * result) + ((_ChJoinFilesharing == null) ? 0 : _ChJoinFilesharing.hashCode());
        result = (37 * result) + ((_ChPauseFilesharing == null) ? 0 : _ChPauseFilesharing.hashCode());
        result = (37 * result) + ((_ChResumeFilesharing == null) ? 0 : _ChResumeFilesharing.hashCode());
        result = (37 * result) + ((_ChLeaveFilesharing == null) ? 0 : _ChLeaveFilesharing.hashCode());
        result = (37 * result) + ((_ChCommands == null) ? 0 : _ChCommands.hashCode());

        return result;
    }
}

/*
                The following schema file has been used for generation:

<?xml version="1.0" encoding="UTF-8"?>
<!--
    Document   : collab.xsd
    Created on : May 21, 2004, 7:45 PM
    Author     : Ayub Khan
    Description:
        Purpose of the document follows.
-->
<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"
            targetNamespace="http://sun.com/ns/collab/dev/1_0/filesharing"
            xmlns:c="http://sun.com/ns/collab/dev/1_0"
            xmlns:ch="http://sun.com/ns/collab/dev/1_0/filesharing"
            elementFormDefault="qaulified">

    <!-- collab element -->
    <xsd:element name="c:collab" type="_collab">
    </xsd:element>

    <xsd:complexType name="_collab">
        <xsd:sequence>
            <xsd:element name="version" type="xsd:string"
                        minOccurs="1" maxOccurs="1"/>
            <xsd:choice maxOccurs="1">
                <xsd:element name="ch:send-file" type="_send-file"
                        minOccurs="0" maxOccurs="1"/>
                <xsd:element name="ch:file-changed" type="_file-changed"
                        minOccurs="0" maxOccurs="1"/>
                <xsd:element name="ch:lock-region" type="_lock-region"
                        minOccurs="0" maxOccurs="1"/>
                <xsd:element name="ch:unlock-region" type="_unlock-region"
                        minOccurs="0" maxOccurs="1"/>
                <xsd:element name="ch:join-filesharing" type="_join-filesharing"
                        minOccurs="0" maxOccurs="1"/>
                <xsd:element name="ch:pause-filesharing" type="_pause-filesharing"
                        minOccurs="0" maxOccurs="1"/>
                <xsd:element name="ch:resume-filesharing" type="_resume-filesharing"
                        minOccurs="0" maxOccurs="1"/>
                <xsd:element name="ch:leave-filesharing" type="_leave-filesharing"
                        minOccurs="0" maxOccurs="1"/>
                <xsd:element name="ch:commands" type="_commands"
                        minOccurs="0" maxOccurs="1"/>
            </xsd:choice>
        </xsd:sequence>
    </xsd:complexType>

    <!-- Schema for send-file -->
    <xsd:complexType name="_send-file">
        <xsd:sequence>
            <xsd:element name="file-groups" type="_file-groups"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="send-file-data" type="_send-file-data"
                    minOccurs="1" maxOccurs="unbounded"/>
        </xsd:sequence>
    </xsd:complexType>

   <!-- Schema for file-changed -->
    <xsd:complexType name="_file-changed">
        <xsd:sequence>
            <xsd:element name="file-groups" type="_file-groups"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="file-changed-data" type="_file-changed-data"
                    minOccurs="1" maxOccurs="unbounded"/>
        </xsd:sequence>
    </xsd:complexType>

    <!-- Schema for lock-region -->
    <xsd:complexType name="_lock-region">
        <xsd:sequence>
            <xsd:element name="file-groups" type="_file-groups"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="lock-region-data" type="_lock-region-data"
                    minOccurs="1" maxOccurs="unbounded"/>
        </xsd:sequence>
    </xsd:complexType>

    <!-- Schema for unlock-region -->
    <xsd:complexType name="_unlock-region">
        <xsd:sequence>
            <xsd:element name="file-groups" type="_file-groups"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="unlock-region-data" type="_unlock-region-data"
                    minOccurs="1" maxOccurs="unbounded"/>
        </xsd:sequence>
    </xsd:complexType>

    <!-- Schema for join filesharing -->
    <xsd:complexType name="_join-filesharing">
        <xsd:sequence>
            <xsd:choice maxOccurs="1">
                <xsd:element name="begin-join"
                        minOccurs="0" maxOccurs="1"/>
                <xsd:element name="end-join"
                        minOccurs="0" maxOccurs="1"/>
            </xsd:choice>
            <xsd:element name="user" type="_user"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <!-- Schema for pause filesharing -->
    <xsd:complexType name="_pause-filesharing">
        <xsd:sequence>
            <xsd:element name="join-user" type="_join-user"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="moderator" type="_moderator"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="file-owners" type="_file-owners"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="users" type="_users"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="file-groups" type="_file-groups"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <!-- Schema for resume filesharing -->
    <xsd:complexType name="_resume-filesharing">
        <xsd:sequence>
            <xsd:element name="moderator" type="_moderator"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <!-- Schema for pause filesharing -->
    <xsd:complexType name="_leave-filesharing">
        <xsd:sequence>
            <xsd:element name="user" type="_user"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="new-moderator" type="_moderator"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="new-file-owner" type="_new-file-owner"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="file-groups" type="_file-groups"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <!-- Schema for commands -->
    <xsd:complexType name="_commands">
        <xsd:sequence>
            <xsd:choice maxOccurs="1">
                <xsd:element name="filesystem-command" type="_filesystem-command"
                        minOccurs="1" maxOccurs="1"/>
            </xsd:choice>
        </xsd:sequence>
    </xsd:complexType>

    <!-- ===================================================== -->

    <xsd:complexType name="_file-groups">
        <xsd:sequence>
            <xsd:element name="file-group" type="_file-group"
                    minOccurs="1" maxOccurs="unbounded"/>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_file-group">
        <xsd:sequence>
            <xsd:element name="file-group-name" type="xsd:string"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="user" type="_user"
                    minOccurs="1" maxOccurs="1"/>
            <!-- file-name contains full path -->
            <xsd:element name="file-name" type="xsd:string"
                    minOccurs="1" maxOccurs="unbounded"/>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_send-file-data">
        <xsd:sequence>
            <xsd:element name="file-data" type="_file-data"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="choose-line-region-function" type="xsd:boolean"
                        minOccurs="1" maxOccurs="1"/>
            <xsd:choice maxOccurs="1">
                <xsd:element name="line-region-function" type="_line-region-function"
                        minOccurs="1" maxOccurs="1"/>
                <xsd:element name="line-region" type="_line-region"
                        minOccurs="1" maxOccurs="unbounded"/>
            </xsd:choice>
            <xsd:element name="content" type="_content"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_line-region-function">
        <xsd:sequence>
            <xsd:element name="funtion-name" type="xsd:string"
                        minOccurs="1" maxOccurs="1"/>
            <xsd:element name="arguments"  type="xsd:string"
                    minOccurs="1" maxOccurs="unbounded"/>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_file-data">
        <xsd:sequence>
            <!-- file-name contains full path -->
            <xsd:element name="file-name" type="xsd:string"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="content-type" type="xsd:string"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="description" type="xsd:string"
                    minOccurs="0" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

   <!-- Schema for file-changed -->
    <xsd:complexType name="_file-changed-data">
        <xsd:sequence>
            <!-- file-name contains full path -->
            <xsd:element name="file-name" type="xsd:string"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="digest" type="xsd:string"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="region-changed" type="_region-changed"
                    minOccurs="1" maxOccurs="unbounded"/>
        </xsd:sequence>
    </xsd:complexType>

    <!-- Schema for lock-region -->
    <xsd:complexType name="_lock-region-data">
        <xsd:sequence>
            <!-- file-name contains full path -->
            <xsd:element name="file-name" type="xsd:string"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="line-region" type="_line-region"
                    minOccurs="1" maxOccurs="unbounded"/>
            <xsd:choice maxOccurs="1">
                <xsd:element name="text-region" type="_text-region"
                        minOccurs="1" maxOccurs="1"/>
                <xsd:element name="java-region" type="_java-region"
                        minOccurs="1" maxOccurs="1"/>
            </xsd:choice>
            <xsd:element name="content" type="_content"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <!-- Schema for unlock-region -->
    <xsd:complexType name="_unlock-region-data">
        <xsd:sequence>
            <!-- file-name contains full path -->
            <xsd:element name="file-name" type="xsd:string"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="line-region" type="_line-region"
                    minOccurs="1" maxOccurs="unbounded"/>
            <xsd:choice maxOccurs="1">
                <xsd:element name="text-region" type="_text-region"
                        minOccurs="1" maxOccurs="1"/>
                <xsd:element name="java-region" type="_java-region"
                        minOccurs="1" maxOccurs="1"/>
            </xsd:choice>
            <xsd:element name="content" type="_content"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <!-- user elements -->
    <xsd:complexType name="_moderator">
        <xsd:sequence>
            <xsd:element name="users" type="_users"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_join-user">
        <xsd:sequence>
            <xsd:element name="user" type="_user"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_file-owners">
        <xsd:sequence>
            <xsd:element name="users" type="_users"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_new-file-owner">
        <xsd:sequence>
            <xsd:element name="users" type="_users"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_users">
        <xsd:sequence>
            <xsd:element name="user" type="_user"
                    minOccurs="0" maxOccurs="unbounded"/>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_user">
        <xsd:sequence>
            <xsd:element name="id" type="xsd:string"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="name" type="xsd:string"
                    minOccurs="0" maxOccurs="1"/>
            <xsd:element name="description" type="xsd:string"
                    minOccurs="0" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <!-- filesystem command schema -->
    <xsd:complexType name="_filesystem-command">
        <xsd:sequence>
            <xsd:choice maxOccurs="1">
            <!-- file commands -->
                <xsd:element name="delete-file" type="_delete-file"
                        minOccurs="1" maxOccurs="1"/>
            </xsd:choice>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_delete-file">
        <xsd:sequence>
            <!-- file-name contains full path -->
            <xsd:element name="file-name" type="xsd:string"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <!-- ===================================================== -->

    <xsd:complexType name="_region-changed">
        <xsd:sequence>
            <xsd:choice maxOccurs="1">
                <xsd:element name="text-region-changed" type="_text-region-changed"
                        minOccurs="1" maxOccurs="1"/>
                <xsd:element name="java-region-changed" type="_java-region-changed"
                        minOccurs="1" maxOccurs="1"/>
                <xsd:element name="line-region-changed" type="_line-region-changed"
                        minOccurs="1" maxOccurs="1"/>
            </xsd:choice>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_text-region-changed">
        <xsd:sequence>
            <xsd:element name="text-region" type="_text-region"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="text-change" type="_text-change"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_java-region-changed">
        <xsd:sequence>
            <xsd:element name="java-region" type="_java-region"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="java-change" type="_java-change"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_line-region-changed">
        <xsd:sequence>
            <xsd:element name="line-region" type="_line-region"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="line-change" type="_line-change"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_region" abstract="true">
        <xsd:sequence>
            <xsd:element name="region-name" type="xsd:string"
                        minOccurs="1" maxOccurs="1">
            </xsd:element>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_change" abstract="true"/>

    <xsd:complexType name="_content">
        <xsd:sequence>
            <xsd:element name="encoding" type="xsd:string"
                    minOccurs="1" maxOccurs="1">
            </xsd:element>
            <xsd:element name="digest" type="xsd:string"
                    minOccurs="1" maxOccurs="1">
            </xsd:element>
            <xsd:element name="data" type="xsd:string"
                    minOccurs="1" maxOccurs="1">
            </xsd:element>
        </xsd:sequence>
    </xsd:complexType>



    <!-- ===================================================== -->
    <!-- ================   Text Region    =================== -->
    <!-- ===================================================== -->

    <xsd:complexType name="_text-region">
        <xsd:complexContent>
            <xsd:extension base="_region">
                <xsd:sequence>
                    <xsd:element name="begin-offset" type="xsd:integer"
                            minOccurs="1" maxOccurs="1">
                    </xsd:element>
                    <xsd:element name="length" type="xsd:integer"
                            minOccurs="1" maxOccurs="1">
                    </xsd:element>
                </xsd:sequence>
            </xsd:extension>
        </xsd:complexContent>
    </xsd:complexType>

    <xsd:complexType name="_line-range">
        <xsd:sequence>
            <xsd:element name="from-line" type="xsd:integer"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="to-line" type="xsd:integer"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_offset-range">
        <xsd:sequence>
            <xsd:element name="begin-offset" type="xsd:integer"
                    minOccurs="1" maxOccurs="1">
            </xsd:element>
            <xsd:element name="length" type="xsd:integer"
                    minOccurs="1" maxOccurs="1">
            </xsd:element>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_text-change">
        <xsd:complexContent>
            <xsd:extension base="_change">
                    <xsd:choice maxOccurs="1">
                        <xsd:element name="change-texts" type="_change-texts"
                                minOccurs="1" maxOccurs="1"/>
                        <xsd:element name="content" type="_content"
                                minOccurs="1" maxOccurs="1"/>
                    </xsd:choice>
            </xsd:extension>
        </xsd:complexContent>
    </xsd:complexType>



    <!-- ===================================================== -->
    <!-- ================   Java Region    =================== -->
    <!-- ===================================================== -->
    <xsd:complexType name="_java-region">
        <xsd:complexContent>
            <xsd:extension base="_region">
                <xsd:sequence>
                    <xsd:element name="begin-offset" type="xsd:integer"
                            minOccurs="1" maxOccurs="1">
                    </xsd:element>
                    <xsd:element name="length" type="xsd:integer"
                            minOccurs="1" maxOccurs="1">
                    </xsd:element>
                </xsd:sequence>
            </xsd:extension>
        </xsd:complexContent>
    </xsd:complexType>

    <xsd:complexType name="_java-change">
        <xsd:complexContent>
            <xsd:extension base="_change">
                <xsd:sequence>
                    <xsd:choice maxOccurs="1">
                        <xsd:element name="change-texts" type="_change-texts"
                                minOccurs="1" maxOccurs="1"/>
                        <xsd:element name="content" type="_content"
                                minOccurs="1" maxOccurs="1"/>
                    </xsd:choice>
                </xsd:sequence>
            </xsd:extension>
        </xsd:complexContent>
    </xsd:complexType>



    <!-- ===================================================== -->
    <!-- ================   Line Region    =================== -->
    <!-- ===================================================== -->
    <xsd:complexType name="_line-region">
        <xsd:complexContent>
            <xsd:extension base="_region">
            </xsd:extension>
        </xsd:complexContent>
    </xsd:complexType>

    <xsd:complexType name="_line-change">
        <xsd:complexContent>
            <xsd:extension base="_change">
                <xsd:sequence>
                    <xsd:choice maxOccurs="1">
                        <xsd:element name="content" type="_content"
                                minOccurs="1" maxOccurs="1"/>
                    </xsd:choice>
                </xsd:sequence>
            </xsd:extension>
        </xsd:complexContent>
    </xsd:complexType>



    <!-- ===================================================== -->
    <!-- ================Common Region Type=================== -->
    <!-- ===================================================== -->

    <xsd:complexType name="_change-texts">
        <xsd:sequence>
            <xsd:element name="remove-texts" type="_remove-texts"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="insert-texts" type="_insert-texts"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_remove-texts">
        <xsd:sequence>
            <xsd:element name="remove-text" type="_remove-text"
                    minOccurs="1" maxOccurs="unbounded"/>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_insert-texts">
        <xsd:sequence>
            <xsd:element name="insert-text" type="_insert-text"
                    minOccurs="1" maxOccurs="unbounded"/>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_remove-text">
        <xsd:sequence>
            <xsd:element name="offset" type="xsd:integer"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="_insert-text">
        <xsd:sequence>
            <xsd:element name="offset" type="xsd:integer"
                    minOccurs="1" maxOccurs="1"/>
            <xsd:element name="content" type="_content"
                    minOccurs="1" maxOccurs="1"/>
        </xsd:sequence>
    </xsd:complexType>

</xsd:schema>

*/
