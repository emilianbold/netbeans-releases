
package team;


/**
 * This is the local-home interface for Player enterprise bean.
 */
public interface PlayerLocalHome extends javax.ejb.EJBLocalHome {
    
    
    
    /**
     *
     */
    team.PlayerLocal findByPrimaryKey(java.lang.String key)  throws javax.ejb.FinderException;

    public team.PlayerLocal create(java.lang.String id, java.lang.String name, java.lang.String position, java.lang.Double salary) throws javax.ejb.CreateException;

    java.util.Collection findByName(java.lang.String name) throws javax.ejb.FinderException;

    java.util.Collection findByPosition(java.lang.String position) throws javax.ejb.FinderException;

    java.util.Collection findBySalary(java.lang.Double salary) throws javax.ejb.FinderException;

    java.util.Collection findAll() throws javax.ejb.FinderException;

         java.util.Collection findByHigherSalary(java.lang.String name) throws javax.ejb.FinderException;

        java.util.Collection findByPositionAndName(java.lang.String position, java.lang.String name) throws javax.ejb.FinderException;

     java.util.Collection findBySalaryRange(double low, double high) throws javax.ejb.FinderException;

      java.util.Collection findBySport(java.lang.String sport) throws javax.ejb.FinderException;

       java.util.Collection findByTest(java.lang.String param1, java.lang.String param2, java.lang.String param3) throws javax.ejb.FinderException;

     java.util.Collection findNotOnTeam() throws javax.ejb.FinderException;
    
     java.util.Collection findByCity(java.lang.String city) throws javax.ejb.FinderException;

     java.util.Collection findByLeague(team.LeagueLocal league) throws javax.ejb.FinderException;

    
    
}
