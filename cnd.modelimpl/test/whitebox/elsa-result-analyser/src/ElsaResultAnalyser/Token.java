/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ElsaResultAnalyser;

/**
 *
 * @author nk220367
 */
public class Token {
    String name;
    TT type;
    int line;
    int row;
    boolean defined;
    
    
    enum TT {
	TOKEN_ID, 
	TOKEN_ROUND_BRACKET,
	TOKEN_BRACE,
	TOKEN_EOF,
	TOKEN_DIGIT,
        TOKEN_NULL
    };
    
    Token() {
        name = "";
	line = 0;
	defined = false;        
    }
}
