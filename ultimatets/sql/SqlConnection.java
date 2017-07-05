package ultimatets.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import ultimatets.UltimateTs;
import ultimatets.utils.Utils;
import ultimatets.utils.enums.LogEnum;
import ultimatets.utils.enums.UtilsStorage;

public class SqlConnection {
	
	public Connection connection;
    private String urlbase, host, database, user, pass;
   
    public SqlConnection(String urlbase, String host, String database, String user, String pass) {
        this.urlbase = urlbase;
        this.host = host;
        this.database = database;
        this.user = user;
        this.pass = pass;
    }

    // DATABASE FONCTIONS:
    
    public void connection(){
        if(!isConnected()){
            try {
                connection = DriverManager.getConnection(urlbase + host + "/" + database, user, pass);
                UltimateTs.main().log(LogEnum.Sql, Level.INFO, UltimateTs.messages.getString("messages.database.online"));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
   
    public void disconnect(){
        if(isConnected()){
            try {
                connection.close();
                UltimateTs.main().log(LogEnum.Sql, Level.INFO, UltimateTs.messages.getString("messages.database.offline"));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        Utils.setStorageType(UtilsStorage.FILE);
    }
   
    public boolean isConnected(){
        return connection != null;
    }
    
    public void createTables(){
    	try {
			PreparedStatement q = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `"+database+"`.`UltimateTS_linkedplayers` ( `id` INT(16) NOT NULL AUTO_INCREMENT , `uuid` VARCHAR(255) NOT NULL , `dbId` INT(16) NOT NULL, PRIMARY KEY `id`(`id`) ) ENGINE = InnoDB;");
			q.executeUpdate();
			q.close();
			UltimateTs.main().log(LogEnum.Sql, Level.INFO, "Table UltimateTS_linkedplayers created succesfully!");
		}catch (SQLException e){
			e.printStackTrace();
		}
    }
    
    //FUNCTIONS:
    
    public boolean useDataBase(){
    	if((UltimateTs.main().getConfig().getBoolean("database.enable") == true) && (connection != null)){
    		Utils.setStorageType(UtilsStorage.SQL);
    		return true;
    	}
    	return false;
    }
    
    public void validLink(String uuid, int dbId){
    	if(!isUUIDLinked(uuid)){
    		try {
    			PreparedStatement q = connection.prepareStatement("INSERT INTO UltimateTS_linkedplayers(uuid,dbId) VALUES (?,?)");
    			q.setString(1, uuid);
    			q.setInt(2, dbId);
    			q.execute();
    			q.close();
    		} catch (SQLException e) {
    			e.printStackTrace();
    		}
    	}
    }
    
    public void unlink(String uuid){
    	if(isUUIDLinked(uuid)){	
    		try {
				PreparedStatement rs = connection.prepareStatement("UPDATE UltimateTS_linkedplayers SET dbId = ? WHERE uuid = ?");
				rs.setInt(1, 0);
				rs.setString(2, uuid);
				rs.executeUpdate();
				rs.close();
    		} catch (SQLException e) {
				e.printStackTrace();
			}
    	}
    }
    
    public boolean isUUIDLinked(String uuid){
    	try {
            PreparedStatement q = connection.prepareStatement("SELECT dbId FROM UltimateTS_linkedplayers WHERE uuid = ?");
            q.setString(1, uuid);
            ResultSet rs = q.executeQuery();
            int id = 0;
            while(rs.next()){
                id = rs.getInt("dbId");
            }
            q.close();
            if(id > 0) return true;
        } catch (SQLException e) {}
    	return false;
    }
    
    public int getLinkedId(String uuid){
    	if(isUUIDLinked(uuid)){
	    	try {
	            PreparedStatement q = connection.prepareStatement("SELECT dbId FROM UltimateTS_linkedplayers WHERE uuid = ?");
	            q.setString(1, uuid);
	            ResultSet rs = q.executeQuery();
	            int id = 0;
	            while(rs.next()){
	                id = rs.getInt("dbId");
	            }
	            q.close();
	            if(id > 0) return id;
	        } catch (SQLException e) {}
    	}
    	return 0;
    }

}
