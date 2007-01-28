/*
 * MethodParamValiator.java
 *
 * Created on February 9, 2005, 10:33 AM
 */

package org.netbeans.modules.visualweb.ejb.util;
import org.openide.util.NbBundle;

/**
 * This class is used to make sure the parameter name is valid in Java sense 
 *
 * @author  cao
 */
public class MethodParamValidator {
    
    public MethodParamValidator() {
    }
    
    public static void validate( String paramName ) throws InvalidParameterNameException
    {
        // A legal parameter name 
        // - start with a letter
        // - no space
        // - not keyword
        
        // Make sure it is not one of the keywords
        if( JavaKeywords.isKeyword( paramName ) )
        {
            throw new InvalidParameterNameException( NbBundle.getMessage(MethodParamValidator.class, "PARAMETER_NAME_NOT_UNIQUE", paramName ) );
        }
        
        for( int i = 0; i < paramName.length(); i ++ )
        {
            char theChar =paramName.charAt( i );
            
            // The first character must be a letter, underscore, or dollar sign
            if( i == 0 )
            {
                if( !Character.isJavaIdentifierStart( theChar ) ) {
                    throw new InvalidParameterNameException( NbBundle.getMessage(MethodParamValidator.class, "INVALID_FIRST_CHAR", paramName ) );
                }
            }
            else
            {
                if( !Character.isJavaIdentifierPart( theChar ) )
                {
                    if( Character.isSpaceChar( theChar ) )
                        throw new InvalidParameterNameException( NbBundle.getMessage(MethodParamValidator.class, "NO_SPACE_IN_PARAMETER_NAME", paramName ) );
                    else
                        throw new InvalidParameterNameException( NbBundle.getMessage(MethodParamValidator.class, "INVALID_CHAR", paramName, new Character(theChar).toString() ) );
                }
            }
            
            
        }
            
    }
    
    public static void main( String[] args )
    {
        MethodParamValidator v = new MethodParamValidator();
        String name = "goto";
        
        try {
            
            v.validate( name );
            System.out.println( "Good parameter name: " + name );
        } catch( InvalidParameterNameException e ) {
            System.out.println( e.getMessage() );
        }
        
    }
    
}
