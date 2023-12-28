package com.example.cvrp;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import java.io.FileReader;
import java.io.IOException;

import java.io.FileReader;
import java.io.IOException;


@SpringBootApplication
public class CvrpApplication {

	public static void main(String[] args) {
		SpringApplication.run(CvrpApplication.class, args);
/*
		String csvFilePath = "C:\\Users\\Önder\\Documents\\Germany-Berlin.csv";
		String apiUrl = "http://localhost:8080/address/add"; // Replace with your API URL

		try (CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(new FileReader(csvFilePath))) {
			for (CSVRecord record : parser) {
				String lat = record.get("LAT");
				String lon = record.get("LON");

				sendPostRequest(apiUrl, lat, lon);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
*/
	}
/*
	private static void sendPostRequest(String apiUrl, String lat, String lon) {
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			HttpPost httpPost = new HttpPost(apiUrl);

			String json = "{\"latitude\": \"" + lat + "\", \"longitude\": \"" + lon + "\"}";
			StringEntity entity = new StringEntity(json);
			httpPost.setEntity(entity);
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");

			CloseableHttpResponse response = client.execute(httpPost);
			System.out.println("Response Status: " + response.getStatusLine().getStatusCode());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
*/
}
