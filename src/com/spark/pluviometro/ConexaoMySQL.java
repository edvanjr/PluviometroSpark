package com.spark.pluviometro;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoMySQL {
	
	public ConexaoMySQL(){
		
	}
	
	public Connection getConexaoMySQL(){
		
		Connection conexao = null;
		
		try{
			String driverName = "com.mysql.jdbc.Driver";                        
			Class.forName(driverName);
			
			String serverName = "54.183.1.153";
            String mydatabase ="pluviometro";        
            String url = "jdbc:mysql://" + serverName + "/" + mydatabase;
            String username = "edvanxp";
            String password = "edvanxp";
 
            conexao = DriverManager.getConnection(url, username, password);
 
            return conexao;
		}catch(ClassNotFoundException e){
			return null;
		}catch (SQLException e) {
			return null;
		}
	}

}
