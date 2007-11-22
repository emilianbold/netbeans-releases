/*
 * Card.java
 *
 * Created on November 7, 2006, 3:57 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.test;

/**
 *
 * @author jindra
 */
public class Card {
    public @interface RequestForEnhancement {
        int    id();
        String synopsis();
        String engineer() default "[unassigned]";
        String date() default "[unimplemented]";
    }

    public @interface Second {
        
    }
    
    public enum Suit { CLUBS, DIAMONDS, HEARTS, SPADES }
    
    /** Creates a new instance of Card */
    public Card() {
    }
    
}
