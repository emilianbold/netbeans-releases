/*
 *   Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.tax;

import java.util.Map;
import java.util.TreeMap;
import java.util.Collection;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.netbeans.tax.spec.AttlistDecl;
import org.netbeans.tax.spec.Attribute;
import org.netbeans.tax.spec.CDATASection;
import org.netbeans.tax.spec.CharacterReference;
import org.netbeans.tax.spec.Comment;
import org.netbeans.tax.spec.ConditionalSection;
import org.netbeans.tax.spec.DocumentFragment;
import org.netbeans.tax.spec.Document;
import org.netbeans.tax.spec.DocumentType;
import org.netbeans.tax.spec.DTD;
import org.netbeans.tax.spec.ElementDecl;
import org.netbeans.tax.spec.Element;
import org.netbeans.tax.spec.EntityDecl;
import org.netbeans.tax.spec.GeneralEntityReference;
import org.netbeans.tax.spec.NotationDecl;
import org.netbeans.tax.spec.ParameterEntityReference;
import org.netbeans.tax.spec.ProcessingInstruction;
import org.netbeans.tax.spec.Text;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public final class TreeUtilities {
    /** */
    private static final boolean DEBUG = false;

    /** */
    private static Constraints constraints = new Constraints();


    //
    // Node.Constraints
    //

    /**
     */
    public static final void checkAttributeName (TreeName treeName) throws InvalidArgumentException {
        constraints.checkAttributeName (treeName);
    }

    /**
     */
    public static final boolean isValidAttributeName (TreeName treeName) {
        return constraints.isValidAttributeName (treeName);
    }

    /**
     */
    public static final void checkElementTagName (TreeName elementTreeName) throws InvalidArgumentException {
        constraints.checkElementTagName (elementTreeName);
    }
    
    /**
     */
    public static final boolean isValidElementTagName (TreeName elementTreeName) {
        return constraints.isValidElementTagName (elementTreeName);
    }
    
    /**
     */
    public static final void checkNotationDeclSystemId (String systemId) throws InvalidArgumentException {
        constraints.checkNotationDeclSystemId (systemId);
    }
 
    /**
     */
    public static final boolean isValidNotationDeclSystemId (String systemId) {
        return constraints.isValidNotationDeclSystemId (systemId);
    }
 
    /**
     */
    public static final void checkDocumentEncoding (String encoding) throws InvalidArgumentException {
        constraints.checkDocumentEncoding (encoding);
    }
 
    /**
     */
    public static final boolean isValidDocumentEncoding (String encoding) {
        return constraints.isValidDocumentEncoding (encoding);
    }
 
    /**
     */
    public static final void checkDTDEncoding (String encoding) throws InvalidArgumentException {
        constraints.checkDTDEncoding (encoding);
    }
 
    /**
     */
    public static final boolean isValidDTDEncoding (String encoding) {
        return constraints.isValidDTDEncoding (encoding);
    }
 
    /**
     */
    public static final void checkCharacterReferenceName (String name) throws InvalidArgumentException {
        constraints.checkCharacterReferenceName (name);
    }
 
    /**
     */
    public static final boolean isValidCharacterReferenceName (String name) {
        return constraints.isValidCharacterReferenceName (name);
    }
 
    /**
     */
    public static final void checkEntityDeclInternalText (String internalText) throws InvalidArgumentException {
        constraints.checkEntityDeclInternalText (internalText);
    }
 
    /**
     */
    public static final boolean isValidEntityDeclInternalText (String internalText) {
        return constraints.isValidEntityDeclInternalText (internalText);
    }

    /**
     */
    public static final void checkAttlistDeclElementName (String elementName) throws InvalidArgumentException {
        constraints.checkAttlistDeclElementName (elementName);
    }
 
    /**
     */
    public static final boolean isValidAttlistDeclElementName (String elementName) {
        return constraints.isValidAttlistDeclElementName (elementName);
    }
 
    /**
     */
    public static final void checkDTDVersion (String version) throws InvalidArgumentException {
        constraints.checkDTDVersion (version);
    }
 
    /**
     */
    public static final boolean isValidDTDVersion (String version) {
        return constraints.isValidDTDVersion (version);
    }
 
    /**
     */
    public static final void checkDocumentTypeSystemId (String systemId) throws InvalidArgumentException {
        constraints.checkDocumentTypeSystemId (systemId);
    }
 
    /**
     */
    public static final boolean isValidDocumentTypeSystemId (String systemId) {
        return constraints.isValidDocumentTypeSystemId (systemId);
    }
 
    /**
     */
    public static final void checkDocumentTypeElementName (String elementName) throws InvalidArgumentException {
        constraints.checkDocumentTypeElementName (elementName);
    }
 
    /**
     */
    public static final boolean isValidDocumentTypeElementName (String elementName) {
        return constraints.isValidDocumentTypeElementName (elementName);
    }
 
    /**
     */
    public static final void checkDocumentStandalone (String standalone) throws InvalidArgumentException {
        constraints.checkDocumentStandalone (standalone);
    }
 
    /**
     */
    public static final boolean isValidDocumentStandalone (String standalone) {
        return constraints.isValidDocumentStandalone (standalone);
    }
 
    /**
     */
    public static final void checkEntityDeclName (String name) throws InvalidArgumentException {
        constraints.checkEntityDeclName (name);
    }
 
    /**
     */
    public static final boolean isValidEntityDeclName (String name) {
        return constraints.isValidEntityDeclName (name);
    }
 
    /**
     */
    public static final void checkAttlistDeclAttributeEnumeratedType (String[] enumeratedType) throws InvalidArgumentException {
        constraints.checkAttlistDeclAttributeEnumeratedType (enumeratedType);
    }
 
    /**
     */
    public static final boolean isValidAttlistDeclAttributeEnumeratedType (String[] enumeratedType) {
        return constraints.isValidAttlistDeclAttributeEnumeratedType (enumeratedType);
    }
 
    /**
     */
    public static final void checkProcessingInstructionData (String data) throws InvalidArgumentException {
        constraints.checkProcessingInstructionData (data);
    }
 
    /**
     */
    public static final boolean isValidProcessingInstructionData (String data) {
        return constraints.isValidProcessingInstructionData (data);
    }
 
    /**
     */
    public static final void checkEntityDeclNotationName (String notationName) throws InvalidArgumentException {
        constraints.checkEntityDeclNotationName (notationName);
    }
 
    /**
     */
    public static final boolean isValidEntityDeclNotationName (String notationName) {
        return constraints.isValidEntityDeclNotationName (notationName);
    }
 
    /**
     */
    public static final void checkElementDeclName (String name) throws InvalidArgumentException {
        constraints.checkElementDeclName (name);
    }
 
    /**
     */
    public static final boolean isValidElementDeclName (String name) {
        return constraints.isValidElementDeclName (name);
    }
 
    /**
     */
    public static final void checkGeneralEntityReferenceName (String name) throws InvalidArgumentException {
        constraints.checkGeneralEntityReferenceName (name);
    }
 
    /**
     */
    public static final boolean isValidGeneralEntityReferenceName (String name) {
        return constraints.isValidGeneralEntityReferenceName (name);
    }
 
    /**
     */
    public static final void checkEntityDeclSystemId (String systemId) throws InvalidArgumentException {
        constraints.checkEntityDeclSystemId (systemId);
    }
 
    /**
     */
    public static final boolean isValidEntityDeclSystemId (String systemId) {
        return constraints.isValidEntityDeclSystemId (systemId);
    }
 
    /**
     */
    public static final void checkProcessingInstructionTarget (String target) throws InvalidArgumentException {
        constraints.checkProcessingInstructionTarget (target);
    }
 
    /**
     */
    public static final boolean isValidProcessingInstructionTarget (String target) {
        return constraints.isValidProcessingInstructionTarget (target);
    }
 
    /**
     */
    public static final void checkEntityDeclPublicId (String publicId) throws InvalidArgumentException {
        constraints.checkEntityDeclPublicId (publicId);
    }
 
    /**
     */
    public static final boolean isValidEntityDeclPublicId (String publicId) {
        return constraints.isValidEntityDeclPublicId (publicId);
    }
 
    /**
     */
    public static final void checkAttlistDeclAttributeDefaultValue (String defaultValue) throws InvalidArgumentException {
        constraints.checkAttlistDeclAttributeDefaultValue (defaultValue);
    }
 
    /**
     */
    public static final boolean isValidAttlistDeclAttributeDefaultValue (String defaultValue) {
        return constraints.isValidAttlistDeclAttributeDefaultValue (defaultValue);
    }
 
    /**
     */
    public static final void checkDocumentFragmentVersion (String version) throws InvalidArgumentException {
        constraints.checkDocumentFragmentVersion (version);
    }
 
    /**
     */
    public static final boolean isValidDocumentFragmentVersion (String version) {
        return constraints.isValidDocumentFragmentVersion (version);
    }
 
    /**
     */
    public static final void checkNotationDeclName (String name) throws InvalidArgumentException {
        constraints.checkNotationDeclName (name);
    }
 
    /**
     */
    public static final boolean isValidNotationDeclName (String name) {
        return constraints.isValidNotationDeclName (name);
    }
 
    /**
     */
    public static final void checkAttributeValue (String value) throws InvalidArgumentException {
        constraints.checkAttributeValue (value);
    }
 
    /**
     */
    public static final boolean isValidAttributeValue (String value) {
        return constraints.isValidAttributeValue (value);
    }
 
    /**
     */
    public static final void checkParameterEntityReferenceName (String name) throws InvalidArgumentException {
        constraints.checkParameterEntityReferenceName (name);
    }
 
    /**
     */
    public static final boolean isValidParameterEntityReferenceName (String name) {
        return constraints.isValidParameterEntityReferenceName (name);
    }
 
    /**
     */
    public static final void checkDocumentFragmentEncoding (String encoding) throws InvalidArgumentException {
        constraints.checkDocumentFragmentEncoding (encoding);
    }
 
    /**
     */
    public static final boolean isValidDocumentFragmentEncoding (String encoding) {
        return constraints.isValidDocumentFragmentEncoding (encoding);
    }
 
    /**
     */
    public static final void checkTextData (String data) throws InvalidArgumentException {
        constraints.checkTextData (data);
    }
 
    /**
     */
    public static final boolean isValidTextData (String data) {
        return constraints.isValidTextData (data);
    }
 
    /**
     */
    public static final void checkDocumentTypePublicId (String publicId) throws InvalidArgumentException {
        constraints.checkDocumentTypePublicId (publicId);
    }
 
    /**
     */
    public static final boolean isValidDocumentTypePublicId (String publicId) {
        return constraints.isValidDocumentTypePublicId (publicId);
    }
 
    /**
     */
    public static final void checkElementDeclContentType (TreeElementDecl.ContentType contentType) throws InvalidArgumentException {
        constraints.checkElementDeclContentType (contentType);
    }
 
    /**
     */
    public static final boolean isValidElementDeclContentType (TreeElementDecl.ContentType contentType) {
        return constraints.isValidElementDeclContentType (contentType);
    }
 
    /**
     */
    public static final void checkDocumentVersion (String version) throws InvalidArgumentException {
        constraints.checkDocumentVersion (version);
    }
 
    /**
     */
    public static final boolean isValidDocumentVersion (String version) {
        return constraints.isValidDocumentVersion (version);
    }
 
    /**
     */
    public static final void checkCDATASectionData (String data) throws InvalidArgumentException {
        constraints.checkCDATASectionData (data);
    }
 
    /**
     */
    public static final boolean isValidCDATASectionData (String data) {
        return constraints.isValidCDATASectionData (data);
    }
 
    /**
     */
    public static final void checkNotationDeclPublicId (String publicId) throws InvalidArgumentException {
        constraints.checkNotationDeclPublicId (publicId);
    }
 
    /**
     */
    public static final boolean isValidNotationDeclPublicId (String publicId) {
        return constraints.isValidNotationDeclPublicId (publicId);
    }
 
    /**
     */
    public static final void checkAttlistDeclAttributeName (String attributeName) throws InvalidArgumentException {
        constraints.checkAttlistDeclAttributeName (attributeName);
    }
 
    /**
     */
    public static final boolean isValidAttlistDeclAttributeName (String attributeName) {
        return constraints.isValidAttlistDeclAttributeName (attributeName);
    }
 
    /**
     */
    public static final void checkCommentData (String data) throws InvalidArgumentException {
        constraints.checkCommentData (data);
    }

    /**
     */
    public static final boolean isValidCommentData (String data) {
        return constraints.isValidCommentData (data);
    }

    /**
     */
    public static final void checkAttlistDeclAttributeType (short type) throws InvalidArgumentException {
        constraints.checkAttlistDeclAttributeType (type);
    }
    
    /**
     */
    public static final boolean isValidAttlistDeclAttributeType (short type) {
        return constraints.isValidAttlistDeclAttributeType (type);
    }
    
    /**
     */
    public static final void checkAttlistDeclAttributeDefaultType (short defaultType) throws InvalidArgumentException {
        constraints.checkAttlistDeclAttributeDefaultType (defaultType);
    }
    
    /**
     */
    public static final boolean isValidAttlistDeclAttributeDefaultType (short defaultType) {
        return constraints.isValidAttlistDeclAttributeDefaultType (defaultType);
    }
    


    //
    // Constraints
    //

    /**
     *
     */
    private static final class Constraints
        implements AttlistDecl.Constraints,
                   Attribute.Constraints,
                   CDATASection.Constraints,
                   CharacterReference.Constraints,
                   Comment.Constraints,
                   ConditionalSection.Constraints,
                   DocumentFragment.Constraints,
                   Document.Constraints,
                   DocumentType.Constraints,
                   DTD.Constraints,
                   ElementDecl.Constraints,
                   Element.Constraints,
                   EntityDecl.Constraints,
                   GeneralEntityReference.Constraints,
                   NotationDecl.Constraints,
                   ParameterEntityReference.Constraints,
                   ProcessingInstruction.Constraints,
                   Text.Constraints {


        //
        // itself
        //

        /**
         */
        private static void checkNullArgument (String argName, Object argValue) throws InvalidArgumentException {
            if ( argValue == null ) {
                throw new InvalidArgumentException (argName, Util.getString ("PROP_invalid_null_value"));
            }
        }

        /**
         */
        private static void checkEmptyString (String argName, String string, boolean trim) throws InvalidArgumentException {
            if ( (string.length() == 0) || (trim && (string.trim().equals (""))) ) { // NOI18N
                throw new InvalidArgumentException (argName, Util.getString ("PROP_invalid_empty_value"));
            }
        }


        //
        // generated from XML recomendation
        //

        /**
         * @see http://www.w3.org/TR/REC-xml#NT-Char
         */
        private static boolean isXMLChar (char c) {
            // #x0009
            if ( c == 0x0009 ) return true;

            // #x000a
            if ( c == 0x000a ) return true;

            // #x000d
            if ( c == 0x000d ) return true;
 
            // [ #x0020 - #xd7ff ]
            if ( c <  0x0020 ) return false; 
            if ( c <= 0xd7ff ) return true;

            // [ #xe000 - #xfffd ]
            if ( c <  0xe000 ) return false;
            if ( c <= 0xfffd ) return true;

            // [ #x10000 - #x10ffff ]
            if ( c <  0x10000  ) return false;
            if ( c <= 0x10ffff ) return true;
 
            return false;
        }

        /**
         * @see http://www.w3.org/TR/REC-xml#NT-NameChar
         */
        private static boolean isXMLNameChar (char c) {
            return ( ( isXMLLetter (c) )
                     ||  ( isXMLDigit (c) )
                     ||  ( c == '.' )
                     ||  ( c == '-' )
                     ||  ( c == '_' )
                     ||  ( c == ':' )
                     ||  ( isXMLCombiningChar (c) )
                     ||  ( isXMLExtender (c) ) );
        }

        /**
         * @see http://www.w3.org/TR/REC-xml#NT-Name
         */
        private static boolean isXMLNameStartChar (char c) {
            return ( ( isXMLLetter (c) )
                     || ( c == '_' )
                     || ( c ==':' ) );
        }

        /**
         * @see http://www.w3.org/TR/REC-xml#NT-Letter
         */
        private static boolean isXMLLetter (char c) {
            return ( isXMLBaseChar (c) || isXMLIdeographic (c) );
        }

        /**
         * @see http://www.w3.org/TR/REC-xml#NT-BaseChar
         */
        private static boolean isXMLBaseChar (char c) {
            // [ #x0041 - #x005a ]
            if ( c <  0x0041 ) return false;
            if ( c <= 0x005a ) return true;
 
            // [ #x0061 - #x007a ]
            if ( c <  0x0061 ) return false;
            if ( c <= 0x007a ) return true;
 
            // [ #x00c0 - #x00d6 ]
            if ( c <  0x00c0 ) return false;
            if ( c <= 0x00d6 ) return true;
 
            // [ #x00d8 - #x00f6 ]
            if ( c <  0x00d8 ) return false;
            if ( c <= 0x00f6 ) return true;
 
            // [ #x00f8 - #x00ff ]
            if ( c <  0x00f8 ) return false;
            if ( c <= 0x00ff ) return true;
 
            // [ #x0100 - #x0131 ]
            if ( c <  0x0100 ) return false;
            if ( c <= 0x0131 ) return true;
 
            // [ #x0134 - #x013e ]
            if ( c <  0x0134 ) return false;
            if ( c <= 0x013e ) return true;
 
            // [ #x0141 - #x0148 ]
            if ( c <  0x0141 ) return false;
            if ( c <= 0x0148 ) return true;
 
            // [ #x014a - #x017e ]
            if ( c <  0x014a ) return false;
            if ( c <= 0x017e ) return true;
 
            // [ #x0180 - #x01c3 ]
            if ( c <  0x0180 ) return false;
            if ( c <= 0x01c3 ) return true;
 
            // [ #x01cd - #x01f0 ]
            if ( c <  0x01cd ) return false;
            if ( c <= 0x01f0 ) return true;
 
            // [ #x01f4 - #x01f5 ]
            if ( c <  0x01f4 ) return false;
            if ( c <= 0x01f5 ) return true;
 
            // [ #x01fa - #x0217 ]
            if ( c <  0x01fa ) return false;
            if ( c <= 0x0217 ) return true;
 
            // [ #x0250 - #x02a8 ]
            if ( c <  0x0250 ) return false;
            if ( c <= 0x02a8 ) return true;
 
            // [ #x02bb - #x02c1 ]
            if ( c <  0x02bb ) return false;
            if ( c <= 0x02c1 ) return true;
 
            // #x0386
            if ( c == 0x0386 ) return true;
 
            // [ #x0388 - #x038a ]
            if ( c <  0x0388 ) return false;
            if ( c <= 0x038a ) return true;
 
            // #x038c
            if ( c == 0x038c ) return true;
 
            // [ #x038e - #x03a1 ]
            if ( c <  0x038e ) return false;
            if ( c <= 0x03a1 ) return true;
 
            // [ #x03a3 - #x03ce ]
            if ( c <  0x03a3 ) return false;
            if ( c <= 0x03ce ) return true;
 
            // [ #x03d0 - #x03d6 ]
            if ( c <  0x03d0 ) return false;
            if ( c <= 0x03d6 ) return true;
 
            // #x03da
            if ( c == 0x03da ) return true;
 
            // #x03dc
            if ( c == 0x03dc ) return true;
 
            // #x03de
            if ( c == 0x03de ) return true;
 
            // #x03e0
            if ( c == 0x03e0 ) return true;
 
            // [ #x03e2 - #x03f3 ]
            if ( c <  0x03e2 ) return false;
            if ( c <= 0x03f3 ) return true;
 
            // [ #x0401 - #x040c ]
            if ( c <  0x0401 ) return false;
            if ( c <= 0x040c ) return true;
 
            // [ #x040e - #x044f ]
            if ( c <  0x040e ) return false;
            if ( c <= 0x044f ) return true;
 
            // [ #x0451 - #x045c ]
            if ( c <  0x0451 ) return false;
            if ( c <= 0x045c ) return true;
 
            // [ #x045e - #x0481 ]
            if ( c <  0x045e ) return false;
            if ( c <= 0x0481 ) return true;
 
            // [ #x0490 - #x04c4 ]
            if ( c <  0x0490 ) return false;
            if ( c <= 0x04c4 ) return true;
 
            // [ #x04c7 - #x04c8 ]
            if ( c <  0x04c7 ) return false;
            if ( c <= 0x04c8 ) return true;
 
            // [ #x04cb - #x04cc ]
            if ( c <  0x04cb ) return false;
            if ( c <= 0x04cc ) return true;
 
            // [ #x04d0 - #x04eb ]
            if ( c <  0x04d0 ) return false;
            if ( c <= 0x04eb ) return true;
 
            // [ #x04ee - #x04f5 ]
            if ( c <  0x04ee ) return false;
            if ( c <= 0x04f5 ) return true;
 
            // [ #x04f8 - #x04f9 ]
            if ( c <  0x04f8 ) return false;
            if ( c <= 0x04f9 ) return true;
 
            // [ #x0531 - #x0556 ]
            if ( c <  0x0531 ) return false;
            if ( c <= 0x0556 ) return true;
 
            // #x0559
            if ( c == 0x0559 ) return true;
 
            // [ #x0561 - #x0586 ]
            if ( c <  0x0561 ) return false;
            if ( c <= 0x0586 ) return true;
 
            // [ #x05d0 - #x05ea ]
            if ( c <  0x05d0 ) return false;
            if ( c <= 0x05ea ) return true;
 
            // [ #x05f0 - #x05f2 ]
            if ( c <  0x05f0 ) return false;
            if ( c <= 0x05f2 ) return true;
 
            // [ #x0621 - #x063a ]
            if ( c <  0x0621 ) return false;
            if ( c <= 0x063a ) return true;
 
            // [ #x0641 - #x064a ]
            if ( c <  0x0641 ) return false;
            if ( c <= 0x064a ) return true;
 
            // [ #x0671 - #x06b7 ]
            if ( c <  0x0671 ) return false;
            if ( c <= 0x06b7 ) return true;
 
            // [ #x06ba - #x06be ]
            if ( c <  0x06ba ) return false;
            if ( c <= 0x06be ) return true;
 
            // [ #x06c0 - #x06ce ]
            if ( c <  0x06c0 ) return false;
            if ( c <= 0x06ce ) return true;
 
            // [ #x06d0 - #x06d3 ]
            if ( c <  0x06d0 ) return false;
            if ( c <= 0x06d3 ) return true;
 
            // #x06d5
            if ( c == 0x06d5 ) return true;
 
            // [ #x06e5 - #x06e6 ]
            if ( c <  0x06e5 ) return false;
            if ( c <= 0x06e6 ) return true;
 
            // [ #x0905 - #x0939 ]
            if ( c <  0x0905 ) return false;
            if ( c <= 0x0939 ) return true;
 
            // #x093d
            if ( c == 0x093d ) return true;
 
            // [ #x0958 - #x0961 ]
            if ( c <  0x0958 ) return false;
            if ( c <= 0x0961 ) return true;
 
            // [ #x0985 - #x098c ]
            if ( c <  0x0985 ) return false;
            if ( c <= 0x098c ) return true;
 
            // [ #x098f - #x0990 ]
            if ( c <  0x098f ) return false;
            if ( c <= 0x0990 ) return true;
 
            // [ #x0993 - #x09a8 ]
            if ( c <  0x0993 ) return false;
            if ( c <= 0x09a8 ) return true;
 
            // [ #x09aa - #x09b0 ]
            if ( c <  0x09aa ) return false;
            if ( c <= 0x09b0 ) return true;
 
            // #x09b2
            if ( c == 0x09b2 ) return true;
 
            // [ #x09b6 - #x09b9 ]
            if ( c <  0x09b6 ) return false;
            if ( c <= 0x09b9 ) return true;
 
            // [ #x09dc - #x09dd ]
            if ( c <  0x09dc ) return false;
            if ( c <= 0x09dd ) return true;
 
            // [ #x09df - #x09e1 ]
            if ( c <  0x09df ) return false;
            if ( c <= 0x09e1 ) return true;
 
            // [ #x09f0 - #x09f1 ]
            if ( c <  0x09f0 ) return false;
            if ( c <= 0x09f1 ) return true;
 
            // [ #x0a05 - #x0a0a ]
            if ( c <  0x0a05 ) return false;
            if ( c <= 0x0a0a ) return true;
 
            // [ #x0a0f - #x0a10 ]
            if ( c <  0x0a0f ) return false;
            if ( c <= 0x0a10 ) return true;
 
            // [ #x0a13 - #x0a28 ]
            if ( c <  0x0a13 ) return false;
            if ( c <= 0x0a28 ) return true;
 
            // [ #x0a2a - #x0a30 ]
            if ( c <  0x0a2a ) return false;
            if ( c <= 0x0a30 ) return true;
 
            // [ #x0a32 - #x0a33 ]
            if ( c <  0x0a32 ) return false;
            if ( c <= 0x0a33 ) return true;
 
            // [ #x0a35 - #x0a36 ]
            if ( c <  0x0a35 ) return false;
            if ( c <= 0x0a36 ) return true;
 
            // [ #x0a38 - #x0a39 ]
            if ( c <  0x0a38 ) return false;
            if ( c <= 0x0a39 ) return true;
 
            // [ #x0a59 - #x0a5c ]
            if ( c <  0x0a59 ) return false;
            if ( c <= 0x0a5c ) return true;
 
            // #x0a5e
            if ( c == 0x0a5e ) return true;
 
            // [ #x0a72 - #x0a74 ]
            if ( c <  0x0a72 ) return false;
            if ( c <= 0x0a74 ) return true;
 
            // [ #x0a85 - #x0a8b ]
            if ( c <  0x0a85 ) return false;
            if ( c <= 0x0a8b ) return true;
 
            // #x0a8d
            if ( c == 0x0a8d ) return true;
 
            // [ #x0a8f - #x0a91 ]
            if ( c <  0x0a8f ) return false;
            if ( c <= 0x0a91 ) return true;
 
            // [ #x0a93 - #x0aa8 ]
            if ( c <  0x0a93 ) return false;
            if ( c <= 0x0aa8 ) return true;
 
            // [ #x0aaa - #x0ab0 ]
            if ( c <  0x0aaa ) return false;
            if ( c <= 0x0ab0 ) return true;
 
            // [ #x0ab2 - #x0ab3 ]
            if ( c <  0x0ab2 ) return false;
            if ( c <= 0x0ab3 ) return true;
 
            // [ #x0ab5 - #x0ab9 ]
            if ( c <  0x0ab5 ) return false;
            if ( c <= 0x0ab9 ) return true;
 
            // #x0abd
            if ( c == 0x0abd ) return true;
 
            // #x0ae0
            if ( c == 0x0ae0 ) return true;
 
            // [ #x0b05 - #x0b0c ]
            if ( c <  0x0b05 ) return false;
            if ( c <= 0x0b0c ) return true;
 
            // [ #x0b0f - #x0b10 ]
            if ( c <  0x0b0f ) return false;
            if ( c <= 0x0b10 ) return true;
 
            // [ #x0b13 - #x0b28 ]
            if ( c <  0x0b13 ) return false;
            if ( c <= 0x0b28 ) return true;
 
            // [ #x0b2a - #x0b30 ]
            if ( c <  0x0b2a ) return false;
            if ( c <= 0x0b30 ) return true;
 
            // [ #x0b32 - #x0b33 ]
            if ( c <  0x0b32 ) return false;
            if ( c <= 0x0b33 ) return true;
 
            // [ #x0b36 - #x0b39 ]
            if ( c <  0x0b36 ) return false;
            if ( c <= 0x0b39 ) return true;
 
            // #x0b3d
            if ( c == 0x0b3d ) return true;
 
            // [ #x0b5c - #x0b5d ]
            if ( c <  0x0b5c ) return false;
            if ( c <= 0x0b5d ) return true;
 
            // [ #x0b5f - #x0b61 ]
            if ( c <  0x0b5f ) return false;
            if ( c <= 0x0b61 ) return true;
 
            // [ #x0b85 - #x0b8a ]
            if ( c <  0x0b85 ) return false;
            if ( c <= 0x0b8a ) return true;
 
            // [ #x0b8e - #x0b90 ]
            if ( c <  0x0b8e ) return false;
            if ( c <= 0x0b90 ) return true;
 
            // [ #x0b92 - #x0b95 ]
            if ( c <  0x0b92 ) return false;
            if ( c <= 0x0b95 ) return true;
 
            // [ #x0b99 - #x0b9a ]
            if ( c <  0x0b99 ) return false;
            if ( c <= 0x0b9a ) return true;
 
            // #x0b9c
            if ( c == 0x0b9c ) return true;
 
            // [ #x0b9e - #x0b9f ]
            if ( c <  0x0b9e ) return false;
            if ( c <= 0x0b9f ) return true;
 
            // [ #x0ba3 - #x0ba4 ]
            if ( c <  0x0ba3 ) return false;
            if ( c <= 0x0ba4 ) return true;
 
            // [ #x0ba8 - #x0baa ]
            if ( c <  0x0ba8 ) return false;
            if ( c <= 0x0baa ) return true;
 
            // [ #x0bae - #x0bb5 ]
            if ( c <  0x0bae ) return false;
            if ( c <= 0x0bb5 ) return true;
 
            // [ #x0bb7 - #x0bb9 ]
            if ( c <  0x0bb7 ) return false;
            if ( c <= 0x0bb9 ) return true;
 
            // [ #x0c05 - #x0c0c ]
            if ( c <  0x0c05 ) return false;
            if ( c <= 0x0c0c ) return true;
 
            // [ #x0c0e - #x0c10 ]
            if ( c <  0x0c0e ) return false;
            if ( c <= 0x0c10 ) return true;
 
            // [ #x0c12 - #x0c28 ]
            if ( c <  0x0c12 ) return false;
            if ( c <= 0x0c28 ) return true;
 
            // [ #x0c2a - #x0c33 ]
            if ( c <  0x0c2a ) return false;
            if ( c <= 0x0c33 ) return true;
 
            // [ #x0c35 - #x0c39 ]
            if ( c <  0x0c35 ) return false;
            if ( c <= 0x0c39 ) return true;
 
            // [ #x0c60 - #x0c61 ]
            if ( c <  0x0c60 ) return false;
            if ( c <= 0x0c61 ) return true;
 
            // [ #x0c85 - #x0c8c ]
            if ( c <  0x0c85 ) return false;
            if ( c <= 0x0c8c ) return true;
 
            // [ #x0c8e - #x0c90 ]
            if ( c <  0x0c8e ) return false;
            if ( c <= 0x0c90 ) return true;
 
            // [ #x0c92 - #x0ca8 ]
            if ( c <  0x0c92 ) return false;
            if ( c <= 0x0ca8 ) return true;
 
            // [ #x0caa - #x0cb3 ]
            if ( c <  0x0caa ) return false;
            if ( c <= 0x0cb3 ) return true;
 
            // [ #x0cb5 - #x0cb9 ]
            if ( c <  0x0cb5 ) return false;
            if ( c <= 0x0cb9 ) return true;
 
            // #x0cde
            if ( c == 0x0cde ) return true;
 
            // [ #x0ce0 - #x0ce1 ]
            if ( c <  0x0ce0 ) return false;
            if ( c <= 0x0ce1 ) return true;
 
            // [ #x0d05 - #x0d0c ]
            if ( c <  0x0d05 ) return false;
            if ( c <= 0x0d0c ) return true;
 
            // [ #x0d0e - #x0d10 ]
            if ( c <  0x0d0e ) return false;
            if ( c <= 0x0d10 ) return true;
 
            // [ #x0d12 - #x0d28 ]
            if ( c <  0x0d12 ) return false;
            if ( c <= 0x0d28 ) return true;
 
            // [ #x0d2a - #x0d39 ]
            if ( c <  0x0d2a ) return false;
            if ( c <= 0x0d39 ) return true;
 
            // [ #x0d60 - #x0d61 ]
            if ( c <  0x0d60 ) return false;
            if ( c <= 0x0d61 ) return true;
 
            // [ #x0e01 - #x0e2e ]
            if ( c <  0x0e01 ) return false;
            if ( c <= 0x0e2e ) return true;
 
            // #x0e30
            if ( c == 0x0e30 ) return true;
 
            // [ #x0e32 - #x0e33 ]
            if ( c <  0x0e32 ) return false;
            if ( c <= 0x0e33 ) return true;
 
            // [ #x0e40 - #x0e45 ]
            if ( c <  0x0e40 ) return false;
            if ( c <= 0x0e45 ) return true;
 
            // [ #x0e81 - #x0e82 ]
            if ( c <  0x0e81 ) return false;
            if ( c <= 0x0e82 ) return true;
 
            // #x0e84
            if ( c == 0x0e84 ) return true;
 
            // [ #x0e87 - #x0e88 ]
            if ( c <  0x0e87 ) return false;
            if ( c <= 0x0e88 ) return true;
 
            // #x0e8a
            if ( c == 0x0e8a ) return true;
 
            // #x0e8d
            if ( c == 0x0e8d ) return true;
 
            // [ #x0e94 - #x0e97 ]
            if ( c <  0x0e94 ) return false;
            if ( c <= 0x0e97 ) return true;
 
            // [ #x0e99 - #x0e9f ]
            if ( c <  0x0e99 ) return false;
            if ( c <= 0x0e9f ) return true;
 
            // [ #x0ea1 - #x0ea3 ]
            if ( c <  0x0ea1 ) return false;
            if ( c <= 0x0ea3 ) return true;
 
            // #x0ea5
            if ( c == 0x0ea5 ) return true;
 
            // #x0ea7
            if ( c == 0x0ea7 ) return true;
 
            // [ #x0eaa - #x0eab ]
            if ( c <  0x0eaa ) return false;
            if ( c <= 0x0eab ) return true;
 
            // [ #x0ead - #x0eae ]
            if ( c <  0x0ead ) return false;
            if ( c <= 0x0eae ) return true;
 
            // #x0eb0
            if ( c == 0x0eb0 ) return true;
 
            // [ #x0eb2 - #x0eb3 ]
            if ( c <  0x0eb2 ) return false;
            if ( c <= 0x0eb3 ) return true;
 
            // #x0ebd
            if ( c == 0x0ebd ) return true;
 
            // [ #x0ec0 - #x0ec4 ]
            if ( c <  0x0ec0 ) return false;
            if ( c <= 0x0ec4 ) return true;
 
            // [ #x0f40 - #x0f47 ]
            if ( c <  0x0f40 ) return false;
            if ( c <= 0x0f47 ) return true;
 
            // [ #x0f49 - #x0f69 ]
            if ( c <  0x0f49 ) return false;
            if ( c <= 0x0f69 ) return true;
 
            // [ #x10a0 - #x10c5 ]
            if ( c <  0x10a0 ) return false;
            if ( c <= 0x10c5 ) return true;
 
            // [ #x10d0 - #x10f6 ]
            if ( c <  0x10d0 ) return false;
            if ( c <= 0x10f6 ) return true;
 
            // #x1100
            if ( c == 0x1100 ) return true;
 
            // [ #x1102 - #x1103 ]
            if ( c <  0x1102 ) return false;
            if ( c <= 0x1103 ) return true;
 
            // [ #x1105 - #x1107 ]
            if ( c <  0x1105 ) return false;
            if ( c <= 0x1107 ) return true;
 
            // #x1109
            if ( c == 0x1109 ) return true;
 
            // [ #x110b - #x110c ]
            if ( c <  0x110b ) return false;
            if ( c <= 0x110c ) return true;
 
            // [ #x110e - #x1112 ]
            if ( c <  0x110e ) return false;
            if ( c <= 0x1112 ) return true;
 
            // #x113c
            if ( c == 0x113c ) return true;
 
            // #x113e
            if ( c == 0x113e ) return true;
 
            // #x1140
            if ( c == 0x1140 ) return true;
 
            // #x114c
            if ( c == 0x114c ) return true;
 
            // #x114e
            if ( c == 0x114e ) return true;
 
            // #x1150
            if ( c == 0x1150 ) return true;
 
            // [ #x1154 - #x1155 ]
            if ( c <  0x1154 ) return false;
            if ( c <= 0x1155 ) return true;
 
            // #x1159
            if ( c == 0x1159 ) return true;
 
            // [ #x115f - #x1161 ]
            if ( c <  0x115f ) return false;
            if ( c <= 0x1161 ) return true;
 
            // #x1163
            if ( c == 0x1163 ) return true;
 
            // #x1165
            if ( c == 0x1165 ) return true;
 
            // #x1167
            if ( c == 0x1167 ) return true;
 
            // #x1169
            if ( c == 0x1169 ) return true;
 
            // [ #x116d - #x116e ]
            if ( c <  0x116d ) return false;
            if ( c <= 0x116e ) return true;
 
            // [ #x1172 - #x1173 ]
            if ( c <  0x1172 ) return false;
            if ( c <= 0x1173 ) return true;
 
            // #x1175
            if ( c == 0x1175 ) return true;
 
            // #x119e
            if ( c == 0x119e ) return true;
 
            // #x11a8
            if ( c == 0x11a8 ) return true;
 
            // #x11ab
            if ( c == 0x11ab ) return true;
 
            // [ #x11ae - #x11af ]
            if ( c <  0x11ae ) return false;
            if ( c <= 0x11af ) return true;
 
            // [ #x11b7 - #x11b8 ]
            if ( c <  0x11b7 ) return false;
            if ( c <= 0x11b8 ) return true;
 
            // #x11ba
            if ( c == 0x11ba ) return true;
 
            // [ #x11bc - #x11c2 ]
            if ( c <  0x11bc ) return false;
            if ( c <= 0x11c2 ) return true;
 
            // #x11eb
            if ( c == 0x11eb ) return true;
 
            // #x11f0
            if ( c == 0x11f0 ) return true;
 
            // #x11f9
            if ( c == 0x11f9 ) return true;
 
            // [ #x1e00 - #x1e9b ]
            if ( c <  0x1e00 ) return false;
            if ( c <= 0x1e9b ) return true;
 
            // [ #x1ea0 - #x1ef9 ]
            if ( c <  0x1ea0 ) return false;
            if ( c <= 0x1ef9 ) return true;
 
            // [ #x1f00 - #x1f15 ]
            if ( c <  0x1f00 ) return false;
            if ( c <= 0x1f15 ) return true;
 
            // [ #x1f18 - #x1f1d ]
            if ( c <  0x1f18 ) return false;
            if ( c <= 0x1f1d ) return true;
 
            // [ #x1f20 - #x1f45 ]
            if ( c <  0x1f20 ) return false;
            if ( c <= 0x1f45 ) return true;
 
            // [ #x1f48 - #x1f4d ]
            if ( c <  0x1f48 ) return false;
            if ( c <= 0x1f4d ) return true;
 
            // [ #x1f50 - #x1f57 ]
            if ( c <  0x1f50 ) return false;
            if ( c <= 0x1f57 ) return true;
 
            // #x1f59
            if ( c == 0x1f59 ) return true;
 
            // #x1f5b
            if ( c == 0x1f5b ) return true;
 
            // #x1f5d
            if ( c == 0x1f5d ) return true;
 
            // [ #x1f5f - #x1f7d ]
            if ( c <  0x1f5f ) return false;
            if ( c <= 0x1f7d ) return true;
 
            // [ #x1f80 - #x1fb4 ]
            if ( c <  0x1f80 ) return false;
            if ( c <= 0x1fb4 ) return true;
 
            // [ #x1fb6 - #x1fbc ]
            if ( c <  0x1fb6 ) return false;
            if ( c <= 0x1fbc ) return true;
 
            // #x1fbe
            if ( c == 0x1fbe ) return true;
 
            // [ #x1fc2 - #x1fc4 ]
            if ( c <  0x1fc2 ) return false;
            if ( c <= 0x1fc4 ) return true;
 
            // [ #x1fc6 - #x1fcc ]
            if ( c <  0x1fc6 ) return false;
            if ( c <= 0x1fcc ) return true;
 
            // [ #x1fd0 - #x1fd3 ]
            if ( c <  0x1fd0 ) return false;
            if ( c <= 0x1fd3 ) return true;
 
            // [ #x1fd6 - #x1fdb ]
            if ( c <  0x1fd6 ) return false;
            if ( c <= 0x1fdb ) return true;
 
            // [ #x1fe0 - #x1fec ]
            if ( c <  0x1fe0 ) return false;
            if ( c <= 0x1fec ) return true;
 
            // [ #x1ff2 - #x1ff4 ]
            if ( c <  0x1ff2 ) return false;
            if ( c <= 0x1ff4 ) return true;
 
            // [ #x1ff6 - #x1ffc ]
            if ( c <  0x1ff6 ) return false;
            if ( c <= 0x1ffc ) return true;
 
            // #x2126
            if ( c == 0x2126 ) return true;
 
            // [ #x212a - #x212b ]
            if ( c <  0x212a ) return false;
            if ( c <= 0x212b ) return true;
 
            // #x212e
            if ( c == 0x212e ) return true;
 
            // [ #x2180 - #x2182 ]
            if ( c <  0x2180 ) return false;
            if ( c <= 0x2182 ) return true;
 
            // [ #x3041 - #x3094 ]
            if ( c <  0x3041 ) return false;
            if ( c <= 0x3094 ) return true;
 
            // [ #x30a1 - #x30fa ]
            if ( c <  0x30a1 ) return false;
            if ( c <= 0x30fa ) return true;
 
            // [ #x3105 - #x312c ]
            if ( c <  0x3105 ) return false;
            if ( c <= 0x312c ) return true;
 
            // [ #xac00 - #xd7a3 ]
            if ( c <  0xac00 ) return false;
            if ( c <= 0xd7a3 ) return true;
 
            return false;
        }
    
    
        /**
         * @see http://www.w3.org/TR/REC-xml#NT-Ideographic
         */
        private static boolean isXMLIdeographic (char c) {
            // #x3007
            if ( c == 0x3007 ) return true;
 
            // [ #x3021 - #x3029 ]
            if ( c <  0x3021 ) return false;
            if ( c <= 0x3029 ) return true;
 
            // [ #x4e00 - #x9fa5 ]
            if ( c <  0x4e00 ) return false;
            if ( c <= 0x9fa5 ) return true;
 
            return false;
        }
    
    
        /**
         * @see http://www.w3.org/TR/REC-xml#NT-CombiningChar
         */
        private static boolean isXMLCombiningChar (char c) {
            // [ #x0300 - #x0345 ]
            if ( c <  0x0300 ) return false;
            if ( c <= 0x0345 ) return true;
 
            // [ #x0360 - #x0361 ]
            if ( c <  0x0360 ) return false;
            if ( c <= 0x0361 ) return true;
 
            // [ #x0483 - #x0486 ]
            if ( c <  0x0483 ) return false;
            if ( c <= 0x0486 ) return true;
 
            // [ #x0591 - #x05a1 ]
            if ( c <  0x0591 ) return false;
            if ( c <= 0x05a1 ) return true;
 
            // [ #x05a3 - #x05b9 ]
            if ( c <  0x05a3 ) return false;
            if ( c <= 0x05b9 ) return true;
 
            // [ #x05bb - #x05bd ]
            if ( c <  0x05bb ) return false;
            if ( c <= 0x05bd ) return true;
 
            // #x05bf
            if ( c == 0x05bf ) return true;
 
            // [ #x05c1 - #x05c2 ]
            if ( c <  0x05c1 ) return false;
            if ( c <= 0x05c2 ) return true;
 
            // #x05c4
            if ( c == 0x05c4 ) return true;
 
            // [ #x064b - #x0652 ]
            if ( c <  0x064b ) return false;
            if ( c <= 0x0652 ) return true;
 
            // #x0670
            if ( c == 0x0670 ) return true;
 
            // [ #x06d6 - #x06dc ]
            if ( c <  0x06d6 ) return false;
            if ( c <= 0x06dc ) return true;
 
            // [ #x06dd - #x06df ]
            if ( c <  0x06dd ) return false;
            if ( c <= 0x06df ) return true;
 
            // [ #x06e0 - #x06e4 ]
            if ( c <  0x06e0 ) return false;
            if ( c <= 0x06e4 ) return true;
 
            // [ #x06e7 - #x06e8 ]
            if ( c <  0x06e7 ) return false;
            if ( c <= 0x06e8 ) return true;
 
            // [ #x06ea - #x06ed ]
            if ( c <  0x06ea ) return false;
            if ( c <= 0x06ed ) return true;
 
            // [ #x0901 - #x0903 ]
            if ( c <  0x0901 ) return false;
            if ( c <= 0x0903 ) return true;
 
            // #x093c
            if ( c == 0x093c ) return true;
 
            // [ #x093e - #x094c ]
            if ( c <  0x093e ) return false;
            if ( c <= 0x094c ) return true;
 
            // #x094d
            if ( c == 0x094d ) return true;
 
            // [ #x0951 - #x0954 ]
            if ( c <  0x0951 ) return false;
            if ( c <= 0x0954 ) return true;
 
            // [ #x0962 - #x0963 ]
            if ( c <  0x0962 ) return false;
            if ( c <= 0x0963 ) return true;
 
            // [ #x0981 - #x0983 ]
            if ( c <  0x0981 ) return false;
            if ( c <= 0x0983 ) return true;
 
            // #x09bc
            if ( c == 0x09bc ) return true;
 
            // #x09be
            if ( c == 0x09be ) return true;
 
            // #x09bf
            if ( c == 0x09bf ) return true;
 
            // [ #x09c0 - #x09c4 ]
            if ( c <  0x09c0 ) return false;
            if ( c <= 0x09c4 ) return true;
 
            // [ #x09c7 - #x09c8 ]
            if ( c <  0x09c7 ) return false;
            if ( c <= 0x09c8 ) return true;
 
            // [ #x09cb - #x09cd ]
            if ( c <  0x09cb ) return false;
            if ( c <= 0x09cd ) return true;
 
            // #x09d7
            if ( c == 0x09d7 ) return true;
 
            // [ #x09e2 - #x09e3 ]
            if ( c <  0x09e2 ) return false;
            if ( c <= 0x09e3 ) return true;
 
            // #x0a02
            if ( c == 0x0a02 ) return true;
 
            // #x0a3c
            if ( c == 0x0a3c ) return true;
 
            // #x0a3e
            if ( c == 0x0a3e ) return true;
 
            // #x0a3f
            if ( c == 0x0a3f ) return true;
 
            // [ #x0a40 - #x0a42 ]
            if ( c <  0x0a40 ) return false;
            if ( c <= 0x0a42 ) return true;
 
            // [ #x0a47 - #x0a48 ]
            if ( c <  0x0a47 ) return false;
            if ( c <= 0x0a48 ) return true;
 
            // [ #x0a4b - #x0a4d ]
            if ( c <  0x0a4b ) return false;
            if ( c <= 0x0a4d ) return true;
 
            // [ #x0a70 - #x0a71 ]
            if ( c <  0x0a70 ) return false;
            if ( c <= 0x0a71 ) return true;
 
            // [ #x0a81 - #x0a83 ]
            if ( c <  0x0a81 ) return false;
            if ( c <= 0x0a83 ) return true;
 
            // #x0abc
            if ( c == 0x0abc ) return true;
 
            // [ #x0abe - #x0ac5 ]
            if ( c <  0x0abe ) return false;
            if ( c <= 0x0ac5 ) return true;
 
            // [ #x0ac7 - #x0ac9 ]
            if ( c <  0x0ac7 ) return false;
            if ( c <= 0x0ac9 ) return true;
 
            // [ #x0acb - #x0acd ]
            if ( c <  0x0acb ) return false;
            if ( c <= 0x0acd ) return true;
 
            // [ #x0b01 - #x0b03 ]
            if ( c <  0x0b01 ) return false;
            if ( c <= 0x0b03 ) return true;
 
            // #x0b3c
            if ( c == 0x0b3c ) return true;
 
            // [ #x0b3e - #x0b43 ]
            if ( c <  0x0b3e ) return false;
            if ( c <= 0x0b43 ) return true;
 
            // [ #x0b47 - #x0b48 ]
            if ( c <  0x0b47 ) return false;
            if ( c <= 0x0b48 ) return true;
 
            // [ #x0b4b - #x0b4d ]
            if ( c <  0x0b4b ) return false;
            if ( c <= 0x0b4d ) return true;
 
            // [ #x0b56 - #x0b57 ]
            if ( c <  0x0b56 ) return false;
            if ( c <= 0x0b57 ) return true;
 
            // [ #x0b82 - #x0b83 ]
            if ( c <  0x0b82 ) return false;
            if ( c <= 0x0b83 ) return true;
 
            // [ #x0bbe - #x0bc2 ]
            if ( c <  0x0bbe ) return false;
            if ( c <= 0x0bc2 ) return true;
 
            // [ #x0bc6 - #x0bc8 ]
            if ( c <  0x0bc6 ) return false;
            if ( c <= 0x0bc8 ) return true;
 
            // [ #x0bca - #x0bcd ]
            if ( c <  0x0bca ) return false;
            if ( c <= 0x0bcd ) return true;
 
            // #x0bd7
            if ( c == 0x0bd7 ) return true;
 
            // [ #x0c01 - #x0c03 ]
            if ( c <  0x0c01 ) return false;
            if ( c <= 0x0c03 ) return true;
 
            // [ #x0c3e - #x0c44 ]
            if ( c <  0x0c3e ) return false;
            if ( c <= 0x0c44 ) return true;
 
            // [ #x0c46 - #x0c48 ]
            if ( c <  0x0c46 ) return false;
            if ( c <= 0x0c48 ) return true;
 
            // [ #x0c4a - #x0c4d ]
            if ( c <  0x0c4a ) return false;
            if ( c <= 0x0c4d ) return true;
 
            // [ #x0c55 - #x0c56 ]
            if ( c <  0x0c55 ) return false;
            if ( c <= 0x0c56 ) return true;
 
            // [ #x0c82 - #x0c83 ]
            if ( c <  0x0c82 ) return false;
            if ( c <= 0x0c83 ) return true;
 
            // [ #x0cbe - #x0cc4 ]
            if ( c <  0x0cbe ) return false;
            if ( c <= 0x0cc4 ) return true;
 
            // [ #x0cc6 - #x0cc8 ]
            if ( c <  0x0cc6 ) return false;
            if ( c <= 0x0cc8 ) return true;
 
            // [ #x0cca - #x0ccd ]
            if ( c <  0x0cca ) return false;
            if ( c <= 0x0ccd ) return true;
 
            // [ #x0cd5 - #x0cd6 ]
            if ( c <  0x0cd5 ) return false;
            if ( c <= 0x0cd6 ) return true;
 
            // [ #x0d02 - #x0d03 ]
            if ( c <  0x0d02 ) return false;
            if ( c <= 0x0d03 ) return true;
 
            // [ #x0d3e - #x0d43 ]
            if ( c <  0x0d3e ) return false;
            if ( c <= 0x0d43 ) return true;
 
            // [ #x0d46 - #x0d48 ]
            if ( c <  0x0d46 ) return false;
            if ( c <= 0x0d48 ) return true;
 
            // [ #x0d4a - #x0d4d ]
            if ( c <  0x0d4a ) return false;
            if ( c <= 0x0d4d ) return true;
 
            // #x0d57
            if ( c == 0x0d57 ) return true;
 
            // #x0e31
            if ( c == 0x0e31 ) return true;
 
            // [ #x0e34 - #x0e3a ]
            if ( c <  0x0e34 ) return false;
            if ( c <= 0x0e3a ) return true;
 
            // [ #x0e47 - #x0e4e ]
            if ( c <  0x0e47 ) return false;
            if ( c <= 0x0e4e ) return true;
 
            // #x0eb1
            if ( c == 0x0eb1 ) return true;
 
            // [ #x0eb4 - #x0eb9 ]
            if ( c <  0x0eb4 ) return false;
            if ( c <= 0x0eb9 ) return true;
 
            // [ #x0ebb - #x0ebc ]
            if ( c <  0x0ebb ) return false;
            if ( c <= 0x0ebc ) return true;
 
            // [ #x0ec8 - #x0ecd ]
            if ( c <  0x0ec8 ) return false;
            if ( c <= 0x0ecd ) return true;
 
            // [ #x0f18 - #x0f19 ]
            if ( c <  0x0f18 ) return false;
            if ( c <= 0x0f19 ) return true;
 
            // #x0f35
            if ( c == 0x0f35 ) return true;
 
            // #x0f37
            if ( c == 0x0f37 ) return true;
 
            // #x0f39
            if ( c == 0x0f39 ) return true;
 
            // #x0f3e
            if ( c == 0x0f3e ) return true;
 
            // #x0f3f
            if ( c == 0x0f3f ) return true;
 
            // [ #x0f71 - #x0f84 ]
            if ( c <  0x0f71 ) return false;
            if ( c <= 0x0f84 ) return true;
 
            // [ #x0f86 - #x0f8b ]
            if ( c <  0x0f86 ) return false;
            if ( c <= 0x0f8b ) return true;
 
            // [ #x0f90 - #x0f95 ]
            if ( c <  0x0f90 ) return false;
            if ( c <= 0x0f95 ) return true;
 
            // #x0f97
            if ( c == 0x0f97 ) return true;
 
            // [ #x0f99 - #x0fad ]
            if ( c <  0x0f99 ) return false;
            if ( c <= 0x0fad ) return true;
 
            // [ #x0fb1 - #x0fb7 ]
            if ( c <  0x0fb1 ) return false;
            if ( c <= 0x0fb7 ) return true;
 
            // #x0fb9
            if ( c == 0x0fb9 ) return true;
 
            // [ #x20d0 - #x20dc ]
            if ( c <  0x20d0 ) return false;
            if ( c <= 0x20dc ) return true;
 
            // #x20e1
            if ( c == 0x20e1 ) return true;
 
            // [ #x302a - #x302f ]
            if ( c <  0x302a ) return false;
            if ( c <= 0x302f ) return true;
 
            // #x3099
            if ( c == 0x3099 ) return true;
 
            // #x309a
            if ( c == 0x309a ) return true;
 
            return false;
        }
    
    
        /**
         * @see http://www.w3.org/TR/REC-xml#NT-Digit
         */
        private static boolean isXMLDigit (char c) {
            // [ #x0030 - #x0039 ]
            if ( c <  0x0030 ) return false;
            if ( c <= 0x0039 ) return true;
 
            // [ #x0660 - #x0669 ]
            if ( c <  0x0660 ) return false;
            if ( c <= 0x0669 ) return true;
 
            // [ #x06f0 - #x06f9 ]
            if ( c <  0x06f0 ) return false;
            if ( c <= 0x06f9 ) return true;
 
            // [ #x0966 - #x096f ]
            if ( c <  0x0966 ) return false;
            if ( c <= 0x096f ) return true;
 
            // [ #x09e6 - #x09ef ]
            if ( c <  0x09e6 ) return false;
            if ( c <= 0x09ef ) return true;
 
            // [ #x0a66 - #x0a6f ]
            if ( c <  0x0a66 ) return false;
            if ( c <= 0x0a6f ) return true;
 
            // [ #x0ae6 - #x0aef ]
            if ( c <  0x0ae6 ) return false;
            if ( c <= 0x0aef ) return true;
 
            // [ #x0b66 - #x0b6f ]
            if ( c <  0x0b66 ) return false;
            if ( c <= 0x0b6f ) return true;
 
            // [ #x0be7 - #x0bef ]
            if ( c <  0x0be7 ) return false;
            if ( c <= 0x0bef ) return true;
 
            // [ #x0c66 - #x0c6f ]
            if ( c <  0x0c66 ) return false;
            if ( c <= 0x0c6f ) return true;
 
            // [ #x0ce6 - #x0cef ]
            if ( c <  0x0ce6 ) return false;
            if ( c <= 0x0cef ) return true;
 
            // [ #x0d66 - #x0d6f ]
            if ( c <  0x0d66 ) return false;
            if ( c <= 0x0d6f ) return true;
 
            // [ #x0e50 - #x0e59 ]
            if ( c <  0x0e50 ) return false;
            if ( c <= 0x0e59 ) return true;
 
            // [ #x0ed0 - #x0ed9 ]
            if ( c <  0x0ed0 ) return false;
            if ( c <= 0x0ed9 ) return true;
 
            // [ #x0f20 - #x0f29 ]
            if ( c <  0x0f20 ) return false;
            if ( c <= 0x0f29 ) return true;
 
            return false;
        }
    
    
        /**
         * @see http://www.w3.org/TR/REC-xml#NT-Extender
         */
        private static boolean isXMLExtender (char c) {
            // #x00b7
            if ( c == 0x00b7 ) return true;
 
            // #x02d0
            if ( c == 0x02d0 ) return true;
 
            // #x02d1
            if ( c == 0x02d1 ) return true;
 
            // #x0387
            if ( c == 0x0387 ) return true;
 
            // #x0640
            if ( c == 0x0640 ) return true;
 
            // #x0e46
            if ( c == 0x0e46 ) return true;
 
            // #x0ec6
            if ( c == 0x0ec6 ) return true;
 
            // #x3005
            if ( c == 0x3005 ) return true;
 
            // [ #x3031 - #x3035 ]
            if ( c <  0x3031 ) return false;
            if ( c <= 0x3035 ) return true;
 
            // [ #x309d - #x309e ]
            if ( c <  0x309d ) return false;
            if ( c <= 0x309e ) return true;
 
            // [ #x30fc - #x30fe ]
            if ( c <  0x30fc ) return false;
            if ( c <= 0x30fe ) return true;
 
            return false;
        }
    


        /**
         * @see http://www.w3.org/TR/REC-xml-names/#NT-NCNameChar
         */
        private static boolean isXMLNCNameChar (char c) {
            return ( ( isXMLLetter (c) )
                     ||  ( isXMLDigit (c) )
                     ||  ( c == '.' )
                     ||  ( c == '-' )
                     ||  ( c == '_' )
                     ||  ( isXMLCombiningChar (c) )
                     ||  ( isXMLExtender (c) ) );
        }

        /**
         * @see http://www.w3.org/TR/REC-xml-names/#NT-NCName
         */
        private static boolean isXMLNCNameStartChar (char c) {
            return ( ( isXMLLetter (c) )
                     || ( c == '_' ) );
        }



        //
        // global constraints
        //

        /**
         * @see http://www.w3.org/TR/REC-xml#NT-Name
         */
        private static void checkXMLName (String argName, String name) throws InvalidArgumentException {
            checkNullArgument (argName, name);
            checkEmptyString (argName, name, true);
      
            char first = name.charAt (0);
            if (!!! isXMLNameStartChar (first)) {
                throw new InvalidArgumentException (argName, Util.getString ("PROP_invalid_first_char", String.valueOf (first)));
            }

            for (int i = 0, len = name.length(); i < len; i++) {
                char c = name.charAt (i);
                if (!!! isXMLNameChar (c)) {
                    throw new InvalidArgumentException (argName, Util.getString ("PROP_invalid_content_char", String.valueOf (c)));
                }
            }
        }

        /**
         * @see http://www.w3.org/TR/REC-xml#NT-Nmtoken
         */
        private static void checkNmToken (String argName, String token) throws InvalidArgumentException {

            checkNullArgument (argName, token);
            checkEmptyString (argName, token, true);
     
            for (int i = 0, len = token.length(); i < len; i++) {
                char c = token.charAt (i);
                if (!!! isXMLNameChar (c)) {
                    throw new InvalidArgumentException (argName, Util.getString ("PROP_invalid_content_char", String.valueOf (c)));
                }
            }
     
        }
 
        /**
         * @see http://www.w3.org/TR/REC-xml-names/#NT-NCName
         */
        private static void checkXMLNCName (String argName, String name) throws InvalidArgumentException {
            checkNullArgument (argName, name);
            checkEmptyString (argName, name, true);
      
            char first = name.charAt (0);
            if (!!! isXMLNCNameStartChar (first)) {
                throw new InvalidArgumentException (argName, Util.getString ("PROP_invalid_first_char", String.valueOf (first)));
            }

            for (int i = 0, len = name.length(); i < len; i++) {
                char c = name.charAt (i);
                if (!!! isXMLNCNameChar (c)) {
                    throw new InvalidArgumentException (argName, Util.getString ("PROP_invalid_content_char", String.valueOf (c)));
                }
            }
        }

        /**
         */
        private static void checkNamespacePrefix (String prefix) throws InvalidArgumentException {
            String argName = Util.getString ("PROP_NamespacePrefix");
            checkXMLNCName (argName, prefix);
        }

        /**
         */
        private static void checkNamespaceURI (String uri) throws InvalidArgumentException {
            String argName = Util.getString ("PROP_NamespaceURI");
            checkAttributeValue (argName, uri);
        }
    


        /**
         */
        private static void checkElementName (String argName, String name) throws InvalidArgumentException {
            checkNullArgument (argName, name);
            checkXMLName (argName, name);
        }

        /**
         */
        private static void checkAttributeName (String argName, String name) throws InvalidArgumentException {
            checkNullArgument (argName, name);
            checkXMLName (argName, name);
        }

        /**
         */
        private static void checkAttributeValue (String argName, String value) throws InvalidArgumentException {
            checkNullArgument (argName, value);
            checkCharacterData (argName, value);

            int index = value.indexOf ('<');
            if ( index != -1 ) {
                throw new InvalidArgumentException (argName, Util.getString ("EXC_invalid_attribute_value", value));
            }
            index = value.indexOf ('&');
            if ( index != -1 ) {
                throw new InvalidArgumentException (argName, Util.getString ("EXC_invalid_attribute_value", value));
            }

            boolean apostrofFound = false;
            boolean quoteFound = false;
            for (int i = 0, len = value.length(); i < len; i++) {
                char c = value.charAt (i);
                if (c == '\'')
                    if (quoteFound)
                        throw new InvalidArgumentException (argName, Util.getString ("EXC_invalid_attribute_value", value));
                    else
                        apostrofFound = true;
                if (c == '"')
                    if (apostrofFound)
                        throw new InvalidArgumentException (argName, Util.getString ("EXC_invalid_attribute_value", value));
                    else
                        quoteFound = true;
            }
        }
 
        /**
         */
        private static void checkCharacterData (String argName, String text) throws InvalidArgumentException {
            checkNullArgument (argName, text);

            // do check
            for (int i = 0, len = text.length(); i < len; i++) {
                char c = text.charAt (i);
                if (!!! isXMLChar (c)) {
                    throw new InvalidArgumentException (argName, Util.getString ("PROP_invalid_content_char", "0x" + Integer.toHexString (c))); // NOI18N
                }       
            }
        }

        /**
         */
        private static void checkSystemId (String argName, String systemId) throws InvalidArgumentException {
            boolean apostrofFound = false;
            boolean quoteFound = false;
            for (int i = 0, len = systemId.length(); i < len; i++) {
                char c = systemId.charAt (i);
                if (c == '\'')
                    if (quoteFound)
                        throw new InvalidArgumentException (argName, Util.getString ("EXC_Invalid_system_id", systemId));
                    else
                        apostrofFound = true;
                if (c == '"')
                    if (apostrofFound)
                        throw new InvalidArgumentException (argName, Util.getString ("EXC_Invalid_system_id", systemId));
                    else
                        quoteFound = true;
            }
        }

        /**
         */
        private static void checkPublicId (String argName, String publicId) throws InvalidArgumentException {
            boolean apostrofFound = false;
            boolean quoteFound = false;
            for (int i = 0, len = publicId.length(); i < len; i++) {
                char c = publicId.charAt (i);
                if (c == '\'')
                    if (quoteFound)
                        throw new InvalidArgumentException (argName, Util.getString ("EXC_Invalid_public_id",  publicId));
                    else
                        apostrofFound = true;
                if (c == '"')
                    if (apostrofFound)
                        throw new InvalidArgumentException (argName, Util.getString ("EXC_Invalid_public_id",  publicId));
                    else
                        quoteFound = true;
            }
        }

        /**
         */
        private static void checkNotationName (String argName, String name) throws InvalidArgumentException {
            checkNullArgument (argName, name);
            checkXMLName (argName, name);
        }


        /**
         */
        private static void checkEncoding (String argName, String encoding) throws InvalidArgumentException {
            if ( DEBUG ) {
                Util.debug ("TreeUtilities::checkEncoding: encoding = " + encoding); // NOI18N
            }

            ByteArrayInputStream stream = new ByteArrayInputStream (new byte[0]);

            if ( DEBUG ) {
                Util.debug ("      ::checkEncoding: stream = " + stream); // NOI18N
            }

            try {
                InputStreamReader reader = new InputStreamReader (stream, iana2java (encoding));

                if ( DEBUG ) {
                    Util.debug ("      ::checkEncoding: reader = " + reader); // NOI18N
                }
            } catch (IOException exc) {
                if ( DEBUG ) {
                    Util.debug ("      ::checkEncoding: IOException !!!", exc); // NOI18N
                }
                throw new InvalidArgumentException (argName, Util.getString ("EXC_Invalid_encoding", encoding));
            }
        }


        /**
         */
        public void checkAttributeName (TreeName treeName) throws InvalidArgumentException {
            String argName = Util.getString ("PROP_AttributeName");
            checkAttributeName (argName, treeName.getQualifiedName());
        }

        /**
         */
        public boolean isValidAttributeName (TreeName treeName) {
            try {
                checkAttributeName (treeName);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }


        /**
         */
        public void checkElementTagName (TreeName elementTreeName) throws InvalidArgumentException {
            checkElementName (Util.getString ("PROP_ElementTagName"), elementTreeName.getQualifiedName());
        }

        /**
         */
        public boolean isValidElementTagName (TreeName elementTreeName) {
            try {
                checkElementTagName (elementTreeName);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }


        /**
         */
        public void checkNotationDeclSystemId (String systemId) throws InvalidArgumentException {
            if ( systemId == null ) {
                return;
            }
            checkSystemId (Util.getString ("PROP_NotationDeclSystemId"), systemId);
        }
 
        /**
         */
        public boolean isValidNotationDeclSystemId (String systemId) {
            try {
                checkNotationDeclSystemId (systemId);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
 

        /**
         */
        public void checkDocumentEncoding (String encoding) throws InvalidArgumentException {
            if ( encoding == null )
                return;
            checkEncoding (Util.getString ("PROP_DocumentEncoding"), encoding);
        }
 
        /**
         */
        public boolean isValidDocumentEncoding (String encoding) {
            try {
                checkDocumentEncoding (encoding);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
 

        /**
         */
        public void checkDTDEncoding (String encoding) throws InvalidArgumentException {
            if ( encoding == null )
                return;
            checkEncoding (Util.getString ("PROP_DTDEncoding"), encoding);
        }
 
        /**
         */
        public boolean isValidDTDEncoding (String encoding) {
            try {
                checkDTDEncoding (encoding);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
 

        /**
         */
        public void checkCharacterReferenceName (String name) throws InvalidArgumentException {
            String argName = Util.getString ("PROP_CharacterReferenceName");
     
            checkNullArgument (argName, name);
            checkEmptyString (argName, name, true);

            int i = 0;
            char first = name.charAt (i);
            if ( first != '#' ) {
                throw new InvalidArgumentException (argName, Util.getString ("PROP_invalid_first_char", String.valueOf (first)));
            }

            i++;
            if ( name.length() <= i ) {
                throw new InvalidArgumentException (argName, Util.getString ("PROP_invalid_empty_value"));
            }

            char second = name.charAt (i);
            int radix = 10;
            if ( second == 'x' ) {
                radix = 16;

                i++;
                if ( name.length() <= i ) {
                    throw new InvalidArgumentException (argName, Util.getString ("PROP_invalid_empty_value"));
                }
            }
            for (int len = name.length(); i < len; i++) {
                char c = name.charAt (i);
                if ( Character.digit (c, radix) == -1 ) {
                    throw new InvalidArgumentException (argName, Util.getString ("PROP_invalid_content_char", String.valueOf (c)));
                }
            }
        }
 
        /**
         */
        public boolean isValidCharacterReferenceName (String name) {
            try {
                checkCharacterReferenceName (name);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
 

        /**
         */
        public void checkEntityDeclInternalText (String internalText) throws InvalidArgumentException {
            checkNullArgument (Util.getString ("PROP_EntityDeclInternalText"), internalText);
            boolean apostrofFound = false;
            boolean quoteFound = false;
            for (int i = 0, len = internalText.length(); i < len; i++) {
                char c = internalText.charAt (i);
                if (c == '\'')
                    if (quoteFound)
                        throw new InvalidArgumentException (Util.getString ("PROP_EntityDeclInternalText"), Util.getString ("EXC_Invalid_Entity_Decl_Internal_text", internalText));
                    else
                        apostrofFound = true;
                if (c == '"')
                    if (apostrofFound)
                        throw new InvalidArgumentException (Util.getString ("PROP_EntityDeclInternalText"), Util.getString ("EXC_Invalid_Entity_Decl_Internal_text", internalText));
                    else
                        quoteFound = true;
                // todo
                //    if (c == '%' || c == '&')
                //    throw new InvalidArgumentException ("EntityDeclInternalText", Util.getString ("EXC_Invalid_Entity_Decl_Internal_text", internalText));
            }
        }
 

        /**
         */
        public boolean isValidEntityDeclInternalText (String internalText) {
            try {
                checkEntityDeclInternalText (internalText);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
 

        /**
         */
        public void checkAttlistDeclElementName (String elementName) throws InvalidArgumentException {
            checkElementName (Util.getString ("PROP_AttlistDeclElementName"), elementName);
        }
 
        /**
         */
        public boolean isValidAttlistDeclElementName (String elementName) {
            try {
                checkAttlistDeclElementName (elementName);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
 

        /**
         */
        public void checkDTDVersion (String version) throws InvalidArgumentException {
            if (version == null)
                return;
            if (!!! version.equals ("1.0")) { // NOI18N
                String arg = Util.getString ("PROP_DTDVersion");
                String msg = Util.getString ("PROP_invalid_version_number", version);
                throw new InvalidArgumentException (arg, msg);
            }
        }
 
        /**
         */
        public boolean isValidDTDVersion (String version) {
            try {
                checkDTDVersion (version);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
 

        /**
         */
        public void checkDocumentTypeSystemId (String systemId) throws InvalidArgumentException {
            if ( systemId == null ) {
                return;
            }
            checkSystemId (Util.getString ("PROP_DocumentTypeSystemId"), systemId);
        }
 
        /**
         */
        public boolean isValidDocumentTypeSystemId (String systemId) {
            try {
                checkDocumentTypeSystemId (systemId);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
 

        /**
         */
        public void checkDocumentTypeElementName (String elementName) throws InvalidArgumentException {
            checkElementName (Util.getString ("PROP_DocumentTypeElementName"), elementName);
        }
 
        /**
         */
        public boolean isValidDocumentTypeElementName (String elementName) {
            try {
                checkDocumentTypeElementName (elementName);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
 

        /**
         */
        public void checkDocumentStandalone (String standalone) throws InvalidArgumentException {
            if (standalone == null)
                return;
            if (standalone.equals ("yes")) // NOI18N
                return;
            if (standalone.equals ("no")) // NOI18N
                return;
            throw new InvalidArgumentException (standalone, standalone + Util.getString ("PROP_is_not_valid_standalone_value"));
        }
 
        /**
         */
        public boolean isValidDocumentStandalone (String standalone) {
            try {
                checkDocumentStandalone (standalone);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
 

        /**
         */
        public void checkEntityDeclName (String name) throws InvalidArgumentException {
            checkXMLName (Util.getString ("PROP_EntityDeclName"), name);
        }
 
        /**
         */
        public boolean isValidEntityDeclName (String name) {
            try {
                checkEntityDeclName (name);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
 

        /**
         */
        public void checkAttlistDeclAttributeEnumeratedType (String[] enumeratedType) throws InvalidArgumentException {
            if ( enumeratedType == null ) {
                return;
            }
            for (int i = 0, len = enumeratedType.length; i < len; i++)
                checkNmToken(Util.getString ("PROP_AttlistDeclAttributeEnumeratedType"), enumeratedType[i]);
        }
 
        /**
         */
        public boolean isValidAttlistDeclAttributeEnumeratedType (String[] enumeratedType) {
            try {
                checkAttlistDeclAttributeEnumeratedType (enumeratedType);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
 

        /**
         */
        public void checkProcessingInstructionData (String data) throws InvalidArgumentException {
            checkCharacterData (Util.getString ("PROP_ProcessingInstructionData"), data);

            int index = data.indexOf ("?>"); // NOI18N
            if (index != -1) {
                throw new InvalidArgumentException (data, Util.getString ("PROP_invalid_processing_instruction_data"));
            }
        }
 
        /**
         */
        public boolean isValidProcessingInstructionData (String data) {
            try {
                checkProcessingInstructionData (data);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
 
        /**
         */
        public void checkEntityDeclNotationName (String notationName) throws InvalidArgumentException {
            if ( notationName == null ) {
                return;
            }
            checkNotationName (Util.getString ("PROP_EntityDeclNotationName"), notationName);
        }
 
        /**
         */
        public boolean isValidEntityDeclNotationName (String notationName) {
            try {
                checkEntityDeclNotationName (notationName);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
 

        /**
         */
        public void checkElementDeclName (String name) throws InvalidArgumentException {
            checkElementName (Util.getString ("PROP_ElementDeclName"), name);
        }
 
        /**
         */
        public boolean isValidElementDeclName (String name) {
            try {
                checkElementDeclName (name);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
 

        /**
         */
        public void checkGeneralEntityReferenceName (String name) throws InvalidArgumentException {
            checkXMLName (Util.getString ("PROP_GeneralEntityReferenceName"), name);
        }
 
        /**
         */
        public boolean isValidGeneralEntityReferenceName (String name) {
            try {
                checkGeneralEntityReferenceName (name);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
 

        /**
         */
        public void checkEntityDeclSystemId (String systemId) throws InvalidArgumentException {
            if ( systemId == null ) {
                return;
            }
            checkSystemId (Util.getString ("PROP_EntityDeclSystemId"), systemId);
        }
 
        /**
         */
        public boolean isValidEntityDeclSystemId (String systemId) {
            try {
                checkEntityDeclSystemId (systemId);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
 

        /**
         */
        public void checkProcessingInstructionTarget (String target) throws InvalidArgumentException {
            String argName = Util.getString ("PROP_ProcessingInstructionTarget");
            checkXMLName (argName, target);

            if (target.equalsIgnoreCase ("xml")) { // NOI18N
                throw new InvalidArgumentException (argName, Util.getString ("PROP_invalid_content_char", target));
            }
        }
 
        /**
         */
        public boolean isValidProcessingInstructionTarget (String target) {
            try {
                checkProcessingInstructionTarget (target);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
 

        /**
         */
        public void checkEntityDeclPublicId (String publicId) throws InvalidArgumentException {
            if ( publicId == null ) {
                return;
            }
            checkPublicId (Util.getString ("PROP_EntityDeclPublicId"), publicId);
        }

 
        /**
         */
        public boolean isValidEntityDeclPublicId (String publicId) {
            try {
                checkEntityDeclPublicId (publicId);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
 

        /**
         */
        public void checkAttlistDeclAttributeDefaultValue (String defaultValue) throws InvalidArgumentException {
            if ( defaultValue == null ) {
                return;
            }
            boolean apostrofFound = false;
            boolean quoteFound = false;
            for (int i = 0, len = defaultValue.length(); i < len; i++) {
                char c = defaultValue.charAt (i);
                if (c == '\'')
                    if (quoteFound)
                        throw new InvalidArgumentException (Util.getString ("PROP_AttlistDeclAttributeDefaultValue"), Util.getString ("EXC_invalid_attribute_default_value", defaultValue));
                    else
                        apostrofFound = true;
                if (c == '"')
                    if (apostrofFound)
                        throw new InvalidArgumentException (Util.getString ("PROP_AttlistDeclAttributeDefaultValue"), Util.getString ("EXC_invalid_attribute_default_value", defaultValue));
                    else
                        quoteFound = true;
                // todo
                //    if (c == '%' || c == '&')
                //    throw new InvalidArgumentException ("AttlistDeclAttributeDefaultValue", Util.getString ("EXC_invalid_attribute_default_value", defaultValue));
            }
        }
 
        /**
         */
        public boolean isValidAttlistDeclAttributeDefaultValue (String defaultValue) {
            try {
                checkAttlistDeclAttributeDefaultValue (defaultValue);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
 

        /**
         */
        public void checkDocumentFragmentVersion (String version) throws InvalidArgumentException {
            if ( version == null )
                return;
            if (!!! version.equals ("1.0")) { // NOI18N
                String arg = Util.getString ("PROP_DocumentFragmentVersion");
                String msg = Util.getString ("PROP_invalid_version_number", version);
                throw new InvalidArgumentException (arg, msg);
            }
        }
 
        /**
         */
        public boolean isValidDocumentFragmentVersion (String version) {
            try {
                checkDocumentFragmentVersion (version);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
 

        /**
         */
        public void checkNotationDeclName (String name) throws InvalidArgumentException {
            checkXMLName (Util.getString ("PROP_NotationDeclName"), name);
        }
 
        /**
         */
        public boolean isValidNotationDeclName (String name) {
            try {
                checkNotationDeclName (name);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
 

        /**
         */
        public void checkAttributeValue (String value) throws InvalidArgumentException {
            String argName = Util.getString ("PROP_AttributeValue");
            checkAttributeValue (argName, value);
        }
 
        /**
         */
        public boolean isValidAttributeValue (String value) {
            try {
                checkAttributeValue (value);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
 

        /**
         */
        public void checkParameterEntityReferenceName (String name) throws InvalidArgumentException {
            checkXMLName (Util.getString ("PROP_ParameterEntityReferenceName"), name);
        }
 
        /**
         */
        public boolean isValidParameterEntityReferenceName (String name) {
            try {
                checkParameterEntityReferenceName (name);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
 

        /**
         */
        public void checkDocumentFragmentEncoding (String encoding) throws InvalidArgumentException {
            if ( encoding == null )
                return;
            checkEncoding (Util.getString ("PROP_DocumentFragmentEncoding"), encoding);
        }
 
        /**
         */
        public boolean isValidDocumentFragmentEncoding (String encoding) {
            try {
                checkDocumentFragmentEncoding (encoding);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
 

        /**
         */
        public void checkTextData (String data) throws InvalidArgumentException {
            String argName = Util.getString ("PROP_TextData");
            checkCharacterData (argName, data);
            checkEmptyString (argName, data, false);

            int index = data.indexOf ('<');
            if ( index != -1 ) {
                throw new InvalidArgumentException (data, Util.getString ("PROP_invalid_text_data"));
            }
            index = data.indexOf ('&');
            if ( index != -1 ) {
                throw new InvalidArgumentException (data, Util.getString ("PROP_invalid_text_data"));
            }
        }
 
        /**
         */
        public boolean isValidTextData (String data) {
            try {
                checkTextData (data);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
 

        /**
         */
        public void checkDocumentTypePublicId (String publicId) throws InvalidArgumentException {
            if ( publicId == null ) {
                return;
            }
            checkPublicId (Util.getString ("PROP_DocumentTypePublicId"), publicId);
        }
 
        /**
         */
        public boolean isValidDocumentTypePublicId (String publicId) {
            try {
                checkDocumentTypePublicId (publicId);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
 

        /**
         */
        public void checkElementDeclContentType (TreeElementDecl.ContentType contentType) throws InvalidArgumentException {
            checkNullArgument (Util.getString ("PROP_ElementDeclContentType"), contentType);
            //       Util.debug ("[PENDING]: TreeUtilities::TreeConstraints.checkElementDeclContentType"); // NOI18N
        }
 
        /**
         */
        public boolean isValidElementDeclContentType (TreeElementDecl.ContentType contentType) {
            try {
                checkElementDeclContentType (contentType);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
 

        /**
         */
        public void checkDocumentVersion (String version) throws InvalidArgumentException {
            if ( version == null )
                return;
            if (!!! version.equals ("1.0")) { // NOI18N
                String arg = Util.getString ("PROP_DocumentVersion");
                String msg = Util.getString ("PROP_invalid_version_number", version);
                throw new InvalidArgumentException (arg, msg);
            }
        }
 
        /**
         */
        public boolean isValidDocumentVersion (String version) {
            try {
                checkDocumentVersion (version);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
 

        /**
         */
        public void checkCDATASectionData (String data) throws InvalidArgumentException {
            checkCharacterData (Util.getString ("PROP_CDATASectionData"), data);

            int index = data.indexOf ("]]>"); // NOI18N
            if (index != -1) {
                throw new InvalidArgumentException (data, Util.getString ("PROP_invalid_cdata_section_data"));
            }
        }
 
        /**
         */
        public boolean isValidCDATASectionData (String data) {
            try {
                checkCDATASectionData (data);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
 

        /**
         */
        public void checkNotationDeclPublicId (String publicId) throws InvalidArgumentException {
            if ( publicId == null ) {
                return;
            }
            checkPublicId (Util.getString ("PROP_NotationDeclPublicId"), publicId);
        }
 
        /**
         */
        public boolean isValidNotationDeclPublicId (String publicId) {
            try {
                checkNotationDeclPublicId (publicId);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
 

        /**
         */
        public void checkAttlistDeclAttributeName (String attributeName) throws InvalidArgumentException {
            checkAttributeName (Util.getString ("PROP_AttlistDeclAttributeName"), attributeName);
        }
 
        /**
         */
        public boolean isValidAttlistDeclAttributeName (String attributeName) {
            try {
                checkAttlistDeclAttributeName (attributeName);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
 

        /**
         */
        public void checkCommentData (String data) throws InvalidArgumentException {
            checkCharacterData (Util.getString ("PROP_CommentData"), data);

            int index = data.indexOf ("--"); // NOI18N
            if (index != -1) {
                throw new InvalidArgumentException (data, Util.getString ("PROP_invalid_comment_data"));
            }
            if (data.endsWith ("-")) { // NOI18N
                throw new InvalidArgumentException (data, Util.getString ("PROP_invalid_comment_data_end"));
            }
        }
 
        /**
         */
        public boolean isValidCommentData (String data) {
            try {
                checkCommentData (data);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
 
        /**
         */
        public void checkAttlistDeclAttributeType (short type) throws InvalidArgumentException {
            if (( type != TreeAttlistDeclAttributeDef.TYPE_CDATA ) &&
                ( type != TreeAttlistDeclAttributeDef.TYPE_ID ) &&
                ( type != TreeAttlistDeclAttributeDef.TYPE_IDREF ) &&
                ( type != TreeAttlistDeclAttributeDef.TYPE_IDREFS ) &&
                ( type != TreeAttlistDeclAttributeDef.TYPE_ENTITY ) &&
                ( type != TreeAttlistDeclAttributeDef.TYPE_ENTITIES ) &&
                ( type != TreeAttlistDeclAttributeDef.TYPE_NMTOKEN ) &&
                ( type != TreeAttlistDeclAttributeDef.TYPE_NMTOKENS ) &&
                ( type != TreeAttlistDeclAttributeDef.TYPE_ENUMERATED ) &&
                ( type != TreeAttlistDeclAttributeDef.TYPE_NOTATION ) ) {
                throw new InvalidArgumentException (new Short (type), Util.getString ("PROP_invalid_attribute_list_declaration_type"));
            }
        }
    
        /**
         */
        public boolean isValidAttlistDeclAttributeType (short type) {
            try {
                checkAttlistDeclAttributeType (type);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
    
        /**
         */
        public void checkAttlistDeclAttributeDefaultType (short defaultType) throws InvalidArgumentException {
            if (( defaultType != TreeAttlistDeclAttributeDef.DEFAULT_TYPE_NULL ) &&
                ( defaultType != TreeAttlistDeclAttributeDef.DEFAULT_TYPE_REQUIRED ) &&
                ( defaultType != TreeAttlistDeclAttributeDef.DEFAULT_TYPE_IMPLIED ) &&
                ( defaultType != TreeAttlistDeclAttributeDef.DEFAULT_TYPE_FIXED ) ) {
                throw new InvalidArgumentException (new Short (defaultType), Util.getString ("PROP_invalid_attribute_list_declaration_default_type"));
            }
        }
    
        /**
         */
        public boolean isValidAttlistDeclAttributeDefaultType (short defaultType) {
            try {
                checkAttlistDeclAttributeDefaultType (defaultType);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
    
    } // end: class Constraints

    
    //
    // Encoding
    //

    /**
     */
    public static final Collection getSupportedEncodings () {
        return EncodingUtil.getIANA2JavaMap().keySet();
    }

    /**
     */
    public static final String iana2java (String iana) {
        String java = (String) EncodingUtil.getIANA2JavaMap().get (iana.toUpperCase());
        return java == null ? iana : java;
    }


    /**
     *
     */
    static class EncodingUtil {

        /** IANA to Java encoding mappings */
        protected final static Map encodingIANA2JavaMap = new TreeMap();
 
        /** */
        protected final static Map encodingIANADescriptionMap = new TreeMap();

        /** */
        protected final static Map encodingIANAAliasesMap = new TreeMap();

        //
        // Static initialization
        //
 
        static {
            encodingIANA2JavaMap.put       ("BIG5", "Big5"); // NOI18N
            encodingIANADescriptionMap.put ("BIG5", Util.getString ("NAME_BIG5")); // NOI18N
            encodingIANAAliasesMap.put     ("BIG5", "BIG5"); // NOI18N 

            encodingIANA2JavaMap.put       ("IBM037",       "CP037");  // NOI18N 
            encodingIANADescriptionMap.put ("IBM037",       Util.getString ("NAME_IBM037")); // NOI18N
            encodingIANAAliasesMap.put     ("IBM037",       "IBM037"); // NOI18N      
            encodingIANAAliasesMap.put     ("EBCDIC-CP-US", "IBM037"); // NOI18N 
            encodingIANAAliasesMap.put     ("EBCDIC-CP-CA", "IBM037"); // NOI18N 
            encodingIANAAliasesMap.put     ("EBCDIC-CP-NL", "IBM037"); // NOI18N 
            encodingIANAAliasesMap.put     ("EBCDIC-CP-WT", "IBM037"); // NOI18N 
 
            encodingIANA2JavaMap.put       ("IBM277",       "CP277");  // NOI18N
            encodingIANADescriptionMap.put ("IBM277",       Util.getString ("NAME_IBM277")); // NOI18N
            encodingIANAAliasesMap.put     ("IBM277",       "IBM277"); // NOI18N 
            encodingIANAAliasesMap.put     ("EBCDIC-CP-DK", "IBM277"); // NOI18N 
            encodingIANAAliasesMap.put     ("EBCDIC-CP-NO", "IBM277"); // NOI18N 
 
            encodingIANA2JavaMap.put       ("IBM278",       "CP278");  // NOI18N
            encodingIANADescriptionMap.put ("IBM278",       Util.getString ("NAME_IBM277")); // NOI18N
            encodingIANAAliasesMap.put     ("IBM278",       "IBM278"); // NOI18N 
            encodingIANAAliasesMap.put     ("EBCDIC-CP-FI", "IBM278"); // NOI18N 
            encodingIANAAliasesMap.put     ("EBCDIC-CP-SE", "IBM278"); // NOI18N 
 
            encodingIANA2JavaMap.put       ("IBM280",       "CP280");  // NOI18N
            encodingIANADescriptionMap.put ("IBM280",       Util.getString ("NAME_IBM280")); // NOI18N
            encodingIANAAliasesMap.put     ("IBM280",       "IBM280"); // NOI18N 
            encodingIANAAliasesMap.put     ("EBCDIC-CP-IT", "IBM280"); // NOI18N 
 
            encodingIANA2JavaMap.put       ("IBM284",       "CP284");  // NOI18N
            encodingIANADescriptionMap.put ("IBM284",       Util.getString ("NAME_IBM284")); // NOI18N
            encodingIANAAliasesMap.put     ("IBM284",       "IBM284"); // NOI18N 
            encodingIANAAliasesMap.put     ("EBCDIC-CP-ES", "IBM284"); // NOI18N 
 
            encodingIANA2JavaMap.put       ("IBM285",       "CP285");  // NOI18N
            encodingIANADescriptionMap.put ("IBM285",       Util.getString ("NAME_IBM285")); // NOI18N
            encodingIANAAliasesMap.put     ("IBM285",       "IBM285"); // NOI18N 
            encodingIANAAliasesMap.put     ("EBCDIC-CP-GB", "IBM285"); // NOI18N 
 
            encodingIANA2JavaMap.put       ("IBM297",       "CP297");  // NOI18N
            encodingIANADescriptionMap.put ("IBM297",       Util.getString ("NAME_IBM297")); // NOI18N
            encodingIANAAliasesMap.put     ("IBM297",       "IBM297"); // NOI18N 
            encodingIANAAliasesMap.put     ("EBCDIC-CP-FR", "IBM297"); // NOI18N 
 
            encodingIANA2JavaMap.put       ("IBM424",       "CP424");  // NOI18N
            encodingIANADescriptionMap.put ("IBM424",       Util.getString ("NAME_IBM424")); // NOI18N
            encodingIANAAliasesMap.put     ("IBM424",       "IBM424"); // NOI18N 
            encodingIANAAliasesMap.put     ("EBCDIC-CP-HE", "IBM424"); // NOI18N 
 
            encodingIANA2JavaMap.put       ("IBM500",       "CP500");  // NOI18N
            encodingIANADescriptionMap.put ("IBM500",       Util.getString ("NAME_IBM500")); // NOI18N
            encodingIANAAliasesMap.put     ("IBM500",       "IBM500"); // NOI18N 
            encodingIANAAliasesMap.put     ("EBCDIC-CP-CH", "IBM500"); // NOI18N 
            encodingIANAAliasesMap.put     ("EBCDIC-CP-BE", "IBM500"); // NOI18N 
 
            encodingIANA2JavaMap.put       ("IBM870",   "CP870");  // NOI18N
            encodingIANADescriptionMap.put ("IBM870",   Util.getString ("NAME_IBM870")); // NOI18N
            encodingIANAAliasesMap.put     ("IBM870",   "IBM870"); // NOI18N 
            encodingIANAAliasesMap.put     ("EBCDIC-CP-ROECE", "IBM870"); // NOI18N 
            encodingIANAAliasesMap.put     ("EBCDIC-CP-YU",    "IBM870"); // NOI18N 
 
            encodingIANA2JavaMap.put       ("IBM871",       "CP871");  // NOI18N
            encodingIANADescriptionMap.put ("IBM871",       Util.getString ("NAME_IBM871")); // NOI18N
            encodingIANAAliasesMap.put     ("IBM871",       "IBM871"); // NOI18N 
            encodingIANAAliasesMap.put     ("EBCDIC-CP-IS", "IBM871"); // NOI18N 
 
            encodingIANA2JavaMap.put       ("IBM918", "CP918");  // NOI18N
            encodingIANADescriptionMap.put ("IBM918", Util.getString ("NAME_IBM918")); // NOI18N
            encodingIANAAliasesMap.put     ("IBM918", "IBM918"); // NOI18N 
            encodingIANAAliasesMap.put     ("EBCDIC-CP-AR2", "IBM918"); // NOI18N 

            encodingIANA2JavaMap.put       ("EUC-JP", "EUCJIS"); // NOI18N
            encodingIANADescriptionMap.put ("EUC-JP", Util.getString ("NAME_EUC-JP")); // NOI18N
            encodingIANAAliasesMap.put     ("EUC-JP", "EUC-JP"); // NOI18N 

            encodingIANA2JavaMap.put       ("EUC-KR", "KSC5601"); // NOI18N
            encodingIANADescriptionMap.put ("EUC-KR", Util.getString ("NAME_EUC-KR")); // NOI18N
            encodingIANAAliasesMap.put     ("EUC-KR", "EUC-KR");  // NOI18N 

            encodingIANA2JavaMap.put       ("GB2312", "GB2312"); // NOI18N
            encodingIANADescriptionMap.put ("GB2312", Util.getString ("NAME_GB2312")); // NOI18N
            encodingIANAAliasesMap.put     ("GB2312", "GB2312"); // NOI18N 

            encodingIANA2JavaMap.put       ("ISO-2022-JP", "JIS");  // NOI18N
            encodingIANADescriptionMap.put ("ISO-2022-JP", Util.getString ("NAME_ISO-2022-JP")); // NOI18N
            encodingIANAAliasesMap.put     ("ISO-2022-JP", "ISO-2022-JP"); // NOI18N 
 
            encodingIANA2JavaMap.put       ("ISO-2022-KR", "ISO2022KR");   // NOI18N
            encodingIANADescriptionMap.put ("ISO-2022-KR", Util.getString ("NAME_ISO-2022-KR")); // NOI18N
            encodingIANAAliasesMap.put     ("ISO-2022-KR", "ISO-2022-KR"); // NOI18N 
 
            encodingIANA2JavaMap.put       ("ISO-8859-1", "8859_1");     // NOI18N
            encodingIANADescriptionMap.put ("ISO-8859-1", Util.getString ("NAME_ISO-8859-1")); // NOI18N
            encodingIANAAliasesMap.put     ("ISO-8859-1", "ISO-8859-1"); // NOI18N 
            encodingIANAAliasesMap.put     ("LATIN1",     "ISO-8859-1"); // NOI18N 
            encodingIANAAliasesMap.put     ("L1",  "ISO-8859-1"); // NOI18N 
            encodingIANAAliasesMap.put     ("IBM819",     "ISO-8859-1"); // NOI18N 
            encodingIANAAliasesMap.put     ("CP819",      "ISO-8859-1"); // NOI18N 
 
            encodingIANA2JavaMap.put       ("ISO-8859-2", "8859_2");     // NOI18N
            encodingIANADescriptionMap.put ("ISO-8859-2", Util.getString ("NAME_ISO-8859-2")); // NOI18N
            encodingIANAAliasesMap.put     ("ISO-8859-2", "ISO-8859-2"); // NOI18N 
            encodingIANAAliasesMap.put     ("LATIN2",     "ISO-8859-2"); // NOI18N 
            encodingIANAAliasesMap.put     ("L2",  "ISO-8859-2"); // NOI18N 
 
            encodingIANA2JavaMap.put       ("ISO-8859-3", "8859_3");     // NOI18N
            encodingIANADescriptionMap.put ("ISO-8859-3", Util.getString ("NAME_ISO-8859-3")); // NOI18N
            encodingIANAAliasesMap.put     ("ISO-8859-3", "ISO-8859-3"); // NOI18N 
            encodingIANAAliasesMap.put     ("LATIN3",     "ISO-8859-3"); // NOI18N 
            encodingIANAAliasesMap.put     ("L3",  "ISO-8859-3"); // NOI18N 
 
            encodingIANA2JavaMap.put       ("ISO-8859-4", "8859_4");     // NOI18N
            encodingIANADescriptionMap.put ("ISO-8859-4", Util.getString ("NAME_ISO-8859-4")); // NOI18N
            encodingIANAAliasesMap.put     ("ISO-8859-4", "ISO-8859-4"); // NOI18N 
            encodingIANAAliasesMap.put     ("LATIN4",     "ISO-8859-4"); // NOI18N 
            encodingIANAAliasesMap.put     ("L4",  "ISO-8859-4"); // NOI18N 

            encodingIANA2JavaMap.put       ("ISO-8859-5", "8859_5");     // NOI18N
            encodingIANADescriptionMap.put ("ISO-8859-5", Util.getString ("NAME_ISO-8859-5")); // NOI18N
            encodingIANAAliasesMap.put     ("ISO-8859-5", "ISO-8859-5"); // NOI18N 
            encodingIANAAliasesMap.put     ("CYRILLIC",   "ISO-8859-5"); // NOI18N 
 
            encodingIANA2JavaMap.put       ("ISO-8859-6", "8859_6");     // NOI18N
            encodingIANADescriptionMap.put ("ISO-8859-6", Util.getString ("NAME_ISO-8859-6")); // NOI18N
            encodingIANAAliasesMap.put     ("ISO-8859-6", "ISO-8859-6"); // NOI18N 
 
            encodingIANA2JavaMap.put       ("ISO-8859-7", "8859_7");     // NOI18N
            encodingIANADescriptionMap.put ("ISO-8859-7", Util.getString ("NAME_ISO-8859-7")); // NOI18N
            encodingIANAAliasesMap.put     ("ISO-8859-7", "ISO-8859-7"); // NOI18N 
            encodingIANAAliasesMap.put     ("GREEK",      "ISO-8859-7"); // NOI18N 
            encodingIANAAliasesMap.put     ("GREEK8",     "ISO-8859-7"); // NOI18N 
 
            encodingIANA2JavaMap.put       ("ISO-8859-8", "8859_8");     // NOI18N
            encodingIANADescriptionMap.put ("ISO-8859-8", Util.getString ("NAME_ISO-8859-8")); // NOI18N
            encodingIANAAliasesMap.put     ("ISO-8859-8", "ISO-8859-8"); // NOI18N 
            encodingIANAAliasesMap.put     ("HEBREW",     "ISO-8859-8"); // NOI18N 
 
            encodingIANA2JavaMap.put       ("ISO-8859-9", "8859_9");     // NOI18N
            encodingIANADescriptionMap.put ("ISO-8859-9", Util.getString ("NAME_ISO-8859-9")); // NOI18N
            encodingIANAAliasesMap.put     ("ISO-8859-9", "ISO-8859-9"); // NOI18N 
            encodingIANAAliasesMap.put     ("LATIN5",     "ISO-8859-9"); // NOI18N 
            encodingIANAAliasesMap.put     ("L5",  "ISO-8859-9"); // NOI18N 
 
            encodingIANA2JavaMap.put       ("KOI8-R", "KOI8_R"); // NOI18N
            encodingIANADescriptionMap.put ("KOI8-R", Util.getString ("NAME_KOI8-R")); // NOI18N
            encodingIANAAliasesMap.put     ("KOI8-R", "KOI8-R"); // NOI18N 
 
            encodingIANA2JavaMap.put       ("US-ASCII",     "8859_1"); // NOI18N
            encodingIANADescriptionMap.put ("US-ASCII",     Util.getString ("NAME_ASCII")); // NOI18N
            encodingIANAAliasesMap.put     ("ASCII",     "US-ASCII");  // NOI18N 
            encodingIANAAliasesMap.put     ("US-ASCII",  "US-ASCII");  // NOI18N 
            encodingIANAAliasesMap.put     ("ISO646-US", "US-ASCII");  // NOI18N 
            encodingIANAAliasesMap.put     ("IBM367",    "US-ASCII");  // NOI18N 
            encodingIANAAliasesMap.put     ("CP367",     "US-ASCII");  // NOI18N 
 
            encodingIANA2JavaMap.put       ("UTF-8", "UTF8");  // NOI18N
            encodingIANADescriptionMap.put ("UTF-8", Util.getString ("NAME_UTF-8")); // NOI18N
            encodingIANAAliasesMap.put     ("UTF-8", "UTF-8"); // NOI18N 
 
            encodingIANA2JavaMap.put       ("UTF-16", "Unicode"); // NOI18N
            encodingIANADescriptionMap.put ("UTF-16", Util.getString ("NAME_UTF-16")); // NOI18N
            encodingIANAAliasesMap.put     ("UTF-16", "UTF-16");  // NOI18N 
        }

 
        /**
         */
        public static Map getIANA2JavaMap () {
            return encodingIANA2JavaMap;
        }
        
    } // end: class EncodingUtil

}
