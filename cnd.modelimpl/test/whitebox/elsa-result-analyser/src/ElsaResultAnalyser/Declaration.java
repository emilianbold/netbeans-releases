/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ElsaResultAnalyser;

import java.util.ArrayList;

/**
 *
 * @author nk220367
 */
public class Declaration {

    public enum TYPE {VARIABLE, FUNCTION};
    
    public String name;
    public Offset namePos;
    public Offset pos;
    public TYPE type;
    
    ArrayList<Offset> usages = new ArrayList<Offset>();
    
    public Offset qualifierPos;
    public String fullName;
    ArrayList<Declaration> declarations = new ArrayList<Declaration>();
    
    public Declaration(String name, String nameLoc, String loc, TYPE type, int nameElsaLine) {
        this.name = name;
        namePos = new Offset(nameLoc, nameElsaLine);
        pos = new Offset(loc, 0);
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        Declaration d = (Declaration) obj;
        return (name.equals(d.name) &&
                pos.equals(d.pos) 
                
                //test
                 && namePos.elsaLine == d.namePos.elsaLine
                
                );
    }
}
