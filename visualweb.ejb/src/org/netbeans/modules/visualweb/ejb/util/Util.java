/*
 * Util.java
 *
 * Created on June 9, 2004, 2:56 PM
 */

package org.netbeans.modules.visualweb.ejb.util;

import java.io.File;

/**
 * This class contains some utility methods
 *
 * @author  cao
 */
public class Util {
    
    /**
     * Get the file name out of the path
     */
    public static String getFileName( String path )
    {
        return new File(path).getName();
    }
    
    public static String getClassName( String fullPackageClassName )
    {
        int i = fullPackageClassName.lastIndexOf( "." );
        String beanClassName = fullPackageClassName.substring( i+1 );
            
        return beanClassName;
    }
    
    public static String getPackageName( String fullPackageClassName )
    {
        int i = fullPackageClassName.lastIndexOf( "." );
        
        if( i == -1 ) // No package
            i = 0;
            
        String packageName = fullPackageClassName.substring( 0, i );
            
        return packageName;
    }
    
    /*
     * Utility routine to paper over array type names
     */
    public static String getTypeName(Class type) {
        if (type.isArray()) {
            try {
                Class cl = type;
                int dimensions = 0;
                while (cl.isArray()) {
                    dimensions++;
                    cl = cl.getComponentType();
                }
                StringBuffer sb = new StringBuffer();
                sb.append(cl.getName());
                for (int i = 0; i < dimensions; i++) {
                    sb.append("[]");
                }
                return sb.toString();
            } catch (Throwable e) { /*FALLTHRU*/ }
        }
        return type.getName();
    }
    
    public static String capitalize(String name) 
    {
        if( name == null || name.length() == 0 ) {
            return name;
        }
        
        char chars[] = name.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }
    
    public static String decapitalize(String name) 
    {
        if( name == null || name.length() == 0 ) {
            return name;
        }
        
        char chars[] = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }
    
    public static boolean isAcronyn( String name ) 
    {
        // The name is consider as acronyn if it starts with two or more upper-case letters in a row
        
        if( name == null || name.length() == 0 ) {
            return false;
        }
        
        char chars[] = name.toCharArray();
        if( Character.isUpperCase( chars[0] ) && Character.isUpperCase( chars[1] ) )
            return true;
        else
            return false;
    }
}
