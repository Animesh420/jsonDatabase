package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class DbMemoryToFile {
    private Gson gson = null;
    private String dbJsonFile = null;

    // File to save/load database in memory
    public JsonObject jsonFileToMap(String filePath) throws IOException {
        System.out.println("Reading data from " + filePath + " in server.");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));
        JsonObject dbMap = gson.fromJson(bufferedReader.readLine(), JsonObject.class);
        if (dbMap == null) {
            dbMap = new JsonObject();
        }
        bufferedReader.close();
        return dbMap;
    }

    public void mapToJsonFile(JsonObject dbMap) throws IOException {
        String mapToJson = gson.toJson(dbMap);
        FileWriter fileWriter = new FileWriter(dbJsonFile);
        fileWriter.write(mapToJson);
        fileWriter.close();
    }

    public DbMemoryToFile(String dbJsonFile) {
        gson = new Gson();
        this.dbJsonFile = dbJsonFile;
    }

}
