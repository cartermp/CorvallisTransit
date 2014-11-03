package phillipcarter.com.mapsthing.util;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.List;

import phillipcarter.com.mapsthing.model.Route;
import phillipcarter.com.mapsthing.model.Tuple;

/**
 * Contains various generic utilities specific enough to be classified as "system".
 */
public class SystemUtil {
    /**
     * Attempts to get routes from the application's cache file.
     */
    public static Tuple<Boolean, List<Route>> getRoutes(Context ctx, String fileName) {
        Gson gson = new Gson();
        Type routeType = new TypeToken<List<Route>>() {
        }.getType();

        String json = "";

        try {
            FileInputStream fis = ctx.openFileInput(fileName);
            json = readString(fis);
            fis.close();
        } catch (IOException e) {
            return Tuple.create(false, null);
        }

        List<Route> routes = gson.fromJson(json, routeType);

        return Tuple.create(true, routes);
    }

    /**
     * Attempts to write a list of routes to the application's cache file.
     */
    public static boolean writeRoutesToFile(Context ctx, List<Route> routes, String fileName) {
        Gson gson = new Gson();
        Type routeType = new TypeToken<List<Route>>() {
        }.getType();

        String json = gson.toJson(routes, routeType);

        try {
            File f = ctx.getCacheDir();
            FileOutputStream fos = new FileOutputStream(f);
            writeString(fos, json);
            fos.close();
        } catch (FileNotFoundException ex) {
            return false;
        } catch (IOException ex) {
            return false;
        }

        return true;
    }

    /**
     * Reads JSON-encoded routes from an input stream.
     */
    private static String readString(InputStream is)
            throws IOException {
        InputStreamReader reader = new InputStreamReader(is);
        BufferedReader bf = new BufferedReader(reader);

        return bf.readLine();
    }

    /**
     * Writes given json to a file stream.
     */
    private static void writeString(OutputStream os, String json)
            throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(os);
        BufferedWriter bf = new BufferedWriter(writer);

        bf.write(json);
    }
}
