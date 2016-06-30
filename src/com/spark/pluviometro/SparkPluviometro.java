package com.spark.pluviometro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.StorageLevels;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.mqtt.MQTTUtils;

public class SparkPluviometro{
	
	public static void main(String[] args){
		
		Logger.getLogger("org").setLevel(Level.OFF);
		Logger.getLogger("akka").setLevel(Level.OFF);
		
		SparkConf conf = new SparkConf();
		conf.setAppName("Pluviômetro");
		conf.setMaster("spark://edvan-jr:7077");
		conf.set("spark.driver.allowMultipleContexts", "true");
		String[] teste = {"/home/edvanjr/Downloads/mysql-connector-java-5.1.39.jar", "/home/edvanjr/teste.jar","/home/edvanjr/spark-1.6.1-bin-hadoop2.6/lib/spark-streaming-mqtt-assembly_2.11-1.6.1.jar" };
		conf.setJars(teste);
		
		JavaSparkContext jsc = new JavaSparkContext(conf);
		
		JavaStreamingContext ssc = new JavaStreamingContext(jsc, Durations.seconds(1));
		
		String brokerUrl = "tcp://52.67.19.49:1883";
		String topic = "simulador";
		
		JavaReceiverInputDStream<String> lines = MQTTUtils.createStream(ssc, brokerUrl, topic, StorageLevels.MEMORY_AND_DISK_SER);
		
		lines.map(new Function<String, String[]>() {

			@Override
			public String[] call(String arg0) throws Exception {
				String[] dados = arg0.split("---");
				return dados;
			}
		}).map(new Function<String[], Double>() {

			@Override
			public Double call(String[] arg0) throws Exception {
				
				Double altura = Double.valueOf(arg0[1]);
				Double volumeChuva = altura * 0.4;
				Double qtdLitros = volumeChuva * 1000.0;
				
				Connection conexao = new ConexaoMySQL().getConexaoMySQL();
				
				String query = "insert into medidas (data_medida, altura, volume, litros, pluviometro)"
				        + " values (?, ?, ?, ?, ?)";
				
				PreparedStatement prepare = conexao.prepareStatement(query);
				prepare.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
				prepare.setDouble(2, altura);
				prepare.setDouble(3, volumeChuva);
				prepare.setDouble(4, qtdLitros);
				prepare.setString(5, arg0[0]);
				
				prepare.execute();
				
				conexao.close();
				
				System.out.println("Salvo no banco de dados: " + arg0);
				
				return altura;
			}
		}).print();
		
		ssc.start();
		ssc.awaitTermination();
	}
}
