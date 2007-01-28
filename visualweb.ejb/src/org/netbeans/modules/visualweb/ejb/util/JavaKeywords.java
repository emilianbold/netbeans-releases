/*
 * JavaKeywords.java
 *
 * Created on February 9, 2005, 1:05 PM
 */

package org.netbeans.modules.visualweb.ejb.util;

import java.util.ArrayList;

/**
 * List all the Java keywords and reserved words
 * @author  cao
 */
public class JavaKeywords {
    
    private static ArrayList keywords;
    
    static {
        // NOI18N
        keywords = new ArrayList();
        keywords.add( "abstract" );
        keywords.add( "assert" );
        keywords.add( "boolean" );
        keywords.add( "byte" );
        keywords.add( "case" );
        keywords.add( "catch" );
        keywords.add( "char" );
        keywords.add( "class" );
        keywords.add( "const" );
        keywords.add( "continue" );
        keywords.add( "default" );
        keywords.add( "do" );
        keywords.add( "double" );
        keywords.add( "else" );
        keywords.add( "enum" );
        keywords.add( "extends" );
        keywords.add( "final" );
        keywords.add( "float" );
        keywords.add( "for" );
        keywords.add( "goto" );
        keywords.add( "if" );
        keywords.add( "implements" );
        keywords.add( "import" );
        keywords.add( "instanceof" );
        keywords.add( "int" );
        keywords.add( "interface" );
        keywords.add( "long" );
        keywords.add( "native" );
        keywords.add( "new" );
        keywords.add( "package" );
        keywords.add( "private" );
        keywords.add( "protected" );
        keywords.add( "public" );
        keywords.add( "return" );
        keywords.add( "short" );
        keywords.add( "static" );
        keywords.add( "strictfp" );
        keywords.add( "super" );
        keywords.add( "switch" );
        keywords.add( "synchronized" );
        keywords.add( "this" );
        keywords.add( "throw" );
        keywords.add( "throws" );
        keywords.add( "transient" );
        keywords.add( "try" );
        keywords.add( "void" );
        keywords.add( "volatile" );
        keywords.add( "while" );
        // reserved words
        keywords.add( "true" );
        keywords.add( "false" );
        keywords.add( "null" );
    }
    
    public static boolean isKeyword( String name )
    {
        if( keywords.contains( name ) )
            return true;
        else
            return false;
    }
    
}
