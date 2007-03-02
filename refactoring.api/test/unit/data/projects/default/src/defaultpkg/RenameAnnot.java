package defaultpkg;

/**
 * @author 
 */
public @interface RenameAnnot {
    //comments
    String text() default "n/a";
    /** javadoc */
    String[] texts();
    int number();
    
}
