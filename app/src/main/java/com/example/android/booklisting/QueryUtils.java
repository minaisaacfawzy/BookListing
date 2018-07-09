package com.example.android.booklisting;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by M on 09/07/2018.
 * <p>
 * Helper methods relating to requesting and receiving book data from Google books Api
 */

class QueryUtils {

    private static final String TAG = "QueryUtils";

    /**
     * private  constructor because no one should ever need to create an object of {@link QueryUtils}
     * this is a class which contains only static variables and methods which can be accessed only through class name
     * and an object of this class is not needed
     */
    private QueryUtils() {
    }

    public static List<Book> fetchBooksList(String url) {

        URL urlObject = createUrl(url);

        String jsonResponse = "";

        try {
            jsonResponse = makeHttpRequeat(urlObject);
        } catch (IOException e) {
            Log.i(TAG, "fetchBooksList: problems in closiing input stream");
        }

        List<Book> booksList = extractBooksFromJson(jsonResponse);
        return booksList;
    }

    /**
     * @return URL object from the given url string*
     */
    private static URL createUrl(String url) {
        URL urlObject = null;
        try {
            urlObject = new URL(url);
        } catch (MalformedURLException e) {
            Log.i(TAG, "createUrl: ");
        }
        return urlObject;
    }

    /**
     * @return String represents the json response after http request
     */
    private static String makeHttpRequeat(URL urlObject) throws IOException {
        String jsonResponse = "";

        if (urlObject == null)
            return jsonResponse;

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) urlObject.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(15000);
            urlConnection.setReadTimeout(1000);
            urlConnection.connect();
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.i(TAG, "makeHttpRequeat: Error in response code" + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.i(TAG, "makeHttpRequeat: problems retrieving book results");
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * @return String json response after reading from {@link InputStream}
     */

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder jsonResponse = new StringBuilder();
        //inputStreamReader to read data in form of byew
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String line = bufferedReader.readLine();
        while (line != null) {
            jsonResponse.append(line);
            line = bufferedReader.readLine();
        }

        return jsonResponse.toString();
    }

    /**
     * @return List for the {@link Book} queried after parsing json response
     */

    private static List<Book> extractBooksFromJson(String jsonResponse) {
        List<Book> books = new ArrayList<>();
        if (TextUtils.isEmpty(jsonResponse))
            return null;

        try {
            JSONObject root = new JSONObject(jsonResponse);
            JSONArray items = root.getJSONArray("items");
            for (int i = 0, n = items.length(); i < n; i++) {
                JSONObject bookInfo = (JSONObject) items.get(i);
                JSONObject volumeInfo = bookInfo.getJSONObject("volumeInfo");
                String title = volumeInfo.getString("title");
                JSONArray authors = volumeInfo.getJSONArray("authors");
                List<String> authorsList = Arrays.asList(authors.toString());
                Book book = new Book(title, authorsList);
                books.add(book);

            }

        } catch (JSONException e) {
            Log.i(TAG, "extractBooksFromJson: problems in parsing books from json reponse");
        }
        return books;
    }

}