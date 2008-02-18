/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 *
 * These imports are default anyway ...
 */

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/** This is a multiline
 *  comment.
 *  I wanna have it folded!
 */

new Test().method()


/** This is a multiline
 *  comment.
 *  I wanna have it folded!
 */

class Test {
    
    /** This is a multiline
     *  comment.
     *  I wanna have it folded!
     */
    
    
    def method (){
        println "Test.method\n"
        myclosure()
    }
    
    def myclosure = { 
        println "print from closure\n" 
        // my
        // homemade 
        // comment
    }
}
