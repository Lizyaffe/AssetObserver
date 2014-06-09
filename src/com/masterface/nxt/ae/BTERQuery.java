package com.masterface.nxt.ae;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class BTERQuery {

    public static void main( String[] args ) throws Exception {
		/*
		 * Try it out! doRequest needs the request type ( post or get ), the API URL, and arguments if necessary (otherwise uses the stub method, as shown here)
		 */
        BTERQuery request = new BTERQuery( );

		/*
		 * Example 1 -- GET method, no arguments
		 */
        StringBuffer response1 = request.doRequest("http://data.bter.com/api/1/ticker/NXT_BTC" );
        System.out.println( response1 );
    }

    public StringBuffer doRequest(String url) throws Exception {
        // Now do the actual request
        HttpClient client = HttpClientBuilder.create( ).build( );
        HttpGet get = new HttpGet(url);
        HttpResponse response = client.execute(get);

        BufferedReader rd = new BufferedReader( new InputStreamReader( response.getEntity( ).getContent( ) ) );
        StringBuffer buffer = new StringBuffer( );
        String line;
        while ( ( line = rd.readLine( ) ) != null ) {
            buffer.append( line );
        }
        return buffer;
    }
}