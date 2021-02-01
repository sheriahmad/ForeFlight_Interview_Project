/*
 * This programs takes in airport identifier, makes the call to two APIs
 * and outputs the data in resource.json file.
 */
package com.company.foreflightInterviewProject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.InputStream;

/**
 * @author sahmad
 * @version 1.0
 */

public class ForeFlightProject {

    private static StringBuffer AIRPORT_RESPONSE;
    private static StringBuffer WEATHER_RESPONSE;
    private static int RESPONSE_CODE;
    private static Properties prop;
    private static String airportCode;
    private static JSONObject jsonResponseObject;


    /**
     * @param args
     * @throws IOException
     * @throws JSONException
     */
    public static void main(String[] args) throws JSONException, ParseException, IOException {

        // Read config properties
        try (InputStream input = new FileInputStream("src/resources/config.properties")) {
            // load a properties file
            prop = new Properties();
            prop.load(input);

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // Create a bufferReader
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter Airport Code: ");

        airportCode = bufferedReader.readLine();

        WEATHER_RESPONSE = new StringBuffer();
        AIRPORT_RESPONSE = new StringBuffer();


        //API call to the airports
        callAPI(airportCode);


        //Read JSON response and print to console
        JSONObject theAirportResponse = new JSONObject(AIRPORT_RESPONSE.toString());
        JSONObject theWeatherResponse = new JSONObject(WEATHER_RESPONSE.toString());

        double temp = (double) theWeatherResponse.getJSONObject("report").getJSONObject("conditions").get("tempC");
        double mphSpeed = (double) theWeatherResponse.getJSONObject("report").getJSONObject("conditions").
                getJSONObject("wind").get("speedKts");
        int windDegrees = (int) theWeatherResponse.getJSONObject("report").getJSONObject("conditions").
                getJSONObject("wind").get("from");

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+SSSS");
        Date firstPeriod = inputFormat.parse(String.valueOf(theWeatherResponse.getJSONObject("report").
                getJSONObject("forecast").getJSONArray("conditions").getJSONObject(1).getJSONObject("period").
                get("dateStart")));

        Date secondPeriod = inputFormat.parse(String.valueOf(theWeatherResponse.getJSONObject("report").
                getJSONObject("forecast").getJSONArray(
                "conditions").getJSONObject(2).getJSONObject("period").get("dateStart")));

        System.out.println("Result after Reading JSON Response"

                + "\n" +
                "The Airport Identifier : " + theAirportResponse.getString("icao")

                + "\n" +
                "Name : " + theAirportResponse.getString("name")

                + "\n" +
                "Runways : " + theAirportResponse.get("runways")

                + "\n" +
                "Latitude : " + theAirportResponse.get("latitudeSecsNorth")

                + "\n" +
                "Longitude : " + theAirportResponse.get("longitudeSecsEast")

                + "\n" +
                "Temperature in Fahrenheit: " + convertTemp(temp)

                + "\n" +
                "Relative Humidity: " + theWeatherResponse.getJSONObject("report").getJSONObject("conditions").
                get("relativeHumidity")

                + "\n" +
                "Cloud Coverage: " + theWeatherResponse.getJSONObject("report").getJSONObject("conditions").
                get("cloudLayers")

                + "\n" +
                "Distance SM: " + theWeatherResponse.getJSONObject("report").getJSONObject("conditions").
                getJSONObject("visibility").get("distanceSm")

                + "\n" +
                "Wind Speed MPH: " + String.format("%.2f", convertSpeed(mphSpeed))

                + "\n" +
                "Wind direction: " + convertDegrees(windDegrees)

                + "\n" +
                "First Forecast Report Time: " + firstPeriod

                + "\n" +
                "Forecasted Wind: " + convertSpeed((Double) theWeatherResponse.getJSONObject("report").
                getJSONObject("forecast").getJSONArray("conditions").getJSONObject(1).getJSONObject("wind").
                get("speedKts"))

                + "\n" +
                "Wind Direction: " + theWeatherResponse.getJSONObject("report").getJSONObject(
                "forecast").getJSONArray(
                "conditions").getJSONObject(1).getJSONObject("wind").get("direction")

                + "\n" +
                "Second Forecast Report Time: " + secondPeriod

                + "\n" + "Forecasted Wind: " + convertSpeed((Double) theWeatherResponse.getJSONObject("report")
                .getJSONObject("forecast").getJSONArray("conditions").getJSONObject(2).getJSONObject("wind").
                        get("speedKts"))

                + "\n" +
                "Wind Direction: " + theWeatherResponse.getJSONObject("report").getJSONObject("forecast").
                getJSONArray("conditions").getJSONObject(2).getJSONObject("wind").get("direction")

        );

        //call to writeJson method that writes to response.json
        writeJson(theWeatherResponse, theAirportResponse, windDegrees, mphSpeed, firstPeriod, secondPeriod, temp);
    }

    /**
     * @param theWeatherResponse
     * @param theAirportResponse
     * @param windDegrees
     * @param mphSpeed
     * @param firstPeriod
     * @param secondPeriod
     * @param temp
     * @throws JSONException
     */
    public static void writeJson(JSONObject theWeatherResponse, JSONObject theAirportResponse, int windDegrees,
                                 double mphSpeed, Date firstPeriod, Date secondPeriod, double temp)
            throws JSONException {
        //Create json object.
        jsonResponseObject = new JSONObject();

        //Overwrite random JSON order
        try {
            Field changeMap = jsonResponseObject.getClass().getDeclaredField("map");
            changeMap.setAccessible(true);
            changeMap.set(jsonResponseObject, new LinkedHashMap<>());
            changeMap.setAccessible(false);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }

        //Current weather
        JSONObject currentWeather = new JSONObject();

        //Current Weather Node
        currentWeather.put("fahrenheitTemperature", convertTemp(temp));
        currentWeather.put("relativeHumidity", theWeatherResponse.getJSONObject("report").getJSONObject(
                "conditions").get("relativeHumidity"));
        currentWeather.put("cloudCoverage", theWeatherResponse.getJSONObject("report").getJSONObject("conditions").
                get("cloudLayers"));

        JSONObject forecastWeather = new JSONObject();
        try {
            Field changeMap = forecastWeather.getClass().getDeclaredField("map");
            changeMap.setAccessible(true);
            changeMap.set(forecastWeather, new LinkedHashMap<>());
            changeMap.setAccessible(false);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }

        //Forecast
        //First Forecast Node
        JSONObject firstForecastNode = new JSONObject();
        firstForecastNode.put("time", firstPeriod);
        firstForecastNode.put("windSpeed",
                convertSpeed((Double) theWeatherResponse.getJSONObject("report").getJSONObject("forecast").getJSONArray(
                        "conditions").getJSONObject(1).getJSONObject("wind").get("speedKts")));
        firstForecastNode.put("windDirection", theWeatherResponse.getJSONObject("report").getJSONObject(
                "forecast").getJSONArray(
                "conditions").getJSONObject(1).getJSONObject("wind").get("direction"));
        forecastWeather.put("firstForecast", firstForecastNode);


        //Second Forecast Node
        JSONObject secondForecastNode = new JSONObject();
        secondForecastNode.put("time", secondPeriod);
        secondForecastNode.put("windSpeed",
                convertSpeed((Double) theWeatherResponse.getJSONObject("report").getJSONObject("forecast").getJSONArray(
                        "conditions").getJSONObject(2).getJSONObject("wind").get("speedKts")));
        secondForecastNode.put("windDirection", theWeatherResponse.getJSONObject("report").getJSONObject(
                "forecast").getJSONArray(
                "conditions").getJSONObject(2).getJSONObject("wind").get("direction"));

        forecastWeather.put("secondForecast", secondForecastNode);


        //Json schema
        jsonResponseObject.put("Identifier", theAirportResponse.getString("icao"));
        jsonResponseObject.put("name", theAirportResponse.getString("name"));
        jsonResponseObject.put("runways", theAirportResponse.get("runways"));
        jsonResponseObject.put("Latitude", theAirportResponse.get("latitudeSecsNorth"));
        jsonResponseObject.put("Longitude", theAirportResponse.get("longitudeSecsEast"));
        jsonResponseObject.put("fahrenheitTemperature", convertTemp(temp));
        jsonResponseObject.put("relativeHumidity", theWeatherResponse.getJSONObject("report").getJSONObject(
                "conditions").get("relativeHumidity"));
        jsonResponseObject.put("currentWeatherReport", currentWeather);

        jsonResponseObject.put("forecastWeatherReport", forecastWeather);


        //Write JSON file
        try (FileWriter file = new FileWriter("response.json")) {

            file.write(String.valueOf(jsonResponseObject));
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     *
     * @param ac
     */
    public static void callAPI(String ac) {
        //call to airport API
        try {
            URL urlForAirport = new URL("https://qa.foreflight.com/airports/" + ac);
            HttpURLConnection con = (HttpURLConnection) urlForAirport.openConnection();

            //add request header
            con.setRequestProperty(prop.getProperty("headerKay"), prop.getProperty("headerValue"));
            con.setRequestProperty("Authorization", "Basic " +
                    "ZmYtaW50ZXJ2aWV3OkAtKkt6VS4qZHRQOWRrb0U3UHJ5TDJvalkhdURWLjZKSkdDOQ==");
            con.setRequestProperty(prop.getProperty("username"), prop.getProperty("password"));

            con.setRequestMethod("GET");

            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            RESPONSE_CODE = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + urlForAirport);
            System.out.println("Response Code : " + RESPONSE_CODE);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                AIRPORT_RESPONSE.append(inputLine);
            }
            in.close();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Airport API call not reachable.");
        }

        //Call to weather API
        try {
            URL urlForWeather = new URL("https://qa.foreflight.com/weather/report/" + ac);
            HttpURLConnection con = (HttpURLConnection) urlForWeather.openConnection();

            //add request header
            con.setRequestProperty(prop.getProperty("headerKay"), prop.getProperty("headerValue"));
            con.setRequestProperty("Authorization", "Basic " +
                    "ZmYtaW50ZXJ2aWV3OkAtKkt6VS4qZHRQOWRrb0U3UHJ5TDJvalkhdURWLjZKSkdDOQ==");
            con.setRequestProperty(prop.getProperty("username"), prop.getProperty("password"));

            con.setRequestMethod("GET");

            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            RESPONSE_CODE = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + urlForWeather);
            System.out.println("Response Code : " + RESPONSE_CODE);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                WEATHER_RESPONSE.append(inputLine);
            }
            in.close();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Weather API call not reachable.");
        }
    }


    /**
     * @param celciusTemp
     * @return fahrenheitTemp
     */
    public static Double convertTemp(Double celciusTemp) {

        double fahrenheitTemp = ((celciusTemp * 9) / 5) + 32;
        return fahrenheitTemp;
    }

    /**
     * @param ktsSpeed
     * @return miles per hour
     */
    public static double convertSpeed(double ktsSpeed) {
        double mphSpeed = ktsSpeed * 1.15;
        return mphSpeed;
    }

    /**
     * @param degrees
     * @return cardinal direction
     */
    public static String convertDegrees(int degrees) {
        String directions[] = {"North", "NorthEast", "East", "SouthEast", "South", "SouthWest", "West", "NorthWest",
                "North"};
        return directions[(int) Math.round(((double) degrees % 360) / 45)];
    }
}

