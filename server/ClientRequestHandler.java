package server;

import com.google.gson.*;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ClientRequestHandler implements Callable {
    public JsonObject db = new JsonObject(); // Memory representation for database holding JSON Objects

    // Status CODES
    public final String OK_STATUS = "OK";
    public final String ERROR_STATUS = "ERROR";
    public final String NO_SUCH_KEY = "No such key";

    // File to store the "db" variable on disk
    public static final String JSON_DB_PATH = System.getProperty("user.dir") + "\\src\\server\\data\\db.json";
    // Managing loading and downloading of db memory object in file and back.
    DbMemoryToFile dbToFileMgr = null;

    // Socket initializing variables
    private final Socket socket;
    private DataInputStream input;
    private DataOutputStream output;

    // Variable to close the running server
    static volatile Boolean exit = false;

    // Lock to manage data read in a multi threaded environment
    static final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public ClientRequestHandler(Socket socket) {
        // Constructor
        /*
         * ClientRequestHandler is a Task called from the main server via the ExecutorServices API.
         * The Task process the requests and sends back appropriate response (JSON in JSON out)
         */
        this.socket = socket;
        this.dbToFileMgr = new DbMemoryToFile(JSON_DB_PATH);

    }


    public String get(JsonElement key) {
        // The get <data> function
        // It accepts a JSONElement object which can be a key or Array of keys
        // When a key is passed its value is directly returned if it exists else an ERROR_STATUS.
        // When a list of keys is passed the list is traversed reaching to the end key hierarchy by hierarchy

        Response getResponse = null;
        try {
            readWriteLock.readLock().lock();
            db = this.dbToFileMgr.jsonFileToMap(JSON_DB_PATH);
            if (key.isJsonArray()) {
                // Travelling hierarchies when the key is an array.
                getResponse = this.travelDown(key, null, "get");

            } else {
                if (db.has(key.getAsString())) {
                    getResponse = new Response(OK_STATUS, null, db.get(key.getAsString()));
                } else {
                    getResponse = new Response(ERROR_STATUS, NO_SUCH_KEY, null);
                }
            }
            return getResponse.toJsonString();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            readWriteLock.readLock().unlock();
        }
        return ERROR_STATUS;
    }

    public String set(JsonElement key, JsonElement val) throws IOException {
        // Similar to get data function this sets data in database
        Response setResponse = null;
        try {
            readWriteLock.writeLock().lock();
            db = this.dbToFileMgr.jsonFileToMap(JSON_DB_PATH);

            if (key.isJsonArray()) {
                setResponse = travelDown(key, val, "set");
            } else {
                db.add(key.getAsString(), val);
            }

            this.dbToFileMgr.mapToJsonFile(db);
            return new Response(OK_STATUS, null, null).toJsonString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            readWriteLock.writeLock().unlock();
        }
        return ERROR_STATUS;
    }

    public String delete(JsonElement key) throws IOException {
        // similar to get, it deletes key from the JSON
        Response delResponse = null;
        try {
            readWriteLock.writeLock().lock();
            db = this.dbToFileMgr.jsonFileToMap(JSON_DB_PATH);
            if (key.isJsonArray()) {
                delResponse = travelDown(key, null, "delete");

            } else {
                String keyString = key.getAsString();
                if (db.has(keyString)) {
                    db.remove(keyString);
                    delResponse = new Response(OK_STATUS, null, null);
                } else {
                    delResponse = new Response(ERROR_STATUS, NO_SUCH_KEY, null);
                }
            }
            this.dbToFileMgr.mapToJsonFile(db);
            return delResponse.toJsonString();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            readWriteLock.writeLock().unlock();
        }
        return ERROR_STATUS;

    }

    public String setExit() {
        exit = true;
        return new Response(OK_STATUS, null, null).toJsonString();
    }

    public Response travelDown(JsonElement key, JsonElement value, String operation) {
        JsonArray keyArray = key.getAsJsonArray();
        JsonObject memory = db;
        for (int i = 0; i < keyArray.size(); i++) {
            String keyString = keyArray.get(i).getAsString();
            if (i == keyArray.size() - 1) {
                switch (operation) {
                    case "set":
                        memory.add(keyString, value);
                        return new Response(OK_STATUS);
                    case "get":
                        if (memory.has(keyString)) {
                            return new Response(OK_STATUS, null, memory.get(keyString));

                        } else {
                            return new Response(ERROR_STATUS, NO_SUCH_KEY, null);
                        }

                    case "delete":
                        String status = memory.has(keyString) ? OK_STATUS : ERROR_STATUS;
                        String reason = status == OK_STATUS ? null : NO_SUCH_KEY;
                        if (memory.has(keyString)) {
                            memory.remove(keyString);
                        }
                        return new Response(status, reason, null);
                }

            } else {

                JsonObject valueJson;
                if (operation.equals("set") && !memory.has(keyString)) {
                    valueJson = new JsonObject();
                    memory.add(keyString, valueJson);
                } else {
                    valueJson = memory.get(keyString).getAsJsonObject();
                }

                memory = valueJson;
            }
        }
        return null;
    }


    public String handleInteractions(String line) throws IOException {

        ServerCommand obj = ServerCommand.getObjFromCommand(line);

        switch (obj.type) {
            case "get":
                return this.get(obj.key);

            case "set":
                return this.set(obj.key, obj.value);

            case "delete":
                return this.delete(obj.key);

            case "exit":
                return this.setExit();

            default:
                return "";
        }

    }

    @Override
    public Boolean call() throws Exception {
        try {
            this.input = new DataInputStream(this.socket.getInputStream());
            this.output = new DataOutputStream(this.socket.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            String msg = this.input.readUTF();
            String out = this.handleInteractions(msg);
            this.output.writeUTF(out);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return exit;
    }
}

