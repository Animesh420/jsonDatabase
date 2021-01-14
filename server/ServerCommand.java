package server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

public class ServerCommand {
    public String type = null;
    public JsonElement key = null;
    public JsonElement value = null;

    public static ServerCommand getObjFromCommand(String json) {
        Gson gson = new Gson();
        ServerCommand obj = gson.fromJson(json, ServerCommand.class);
        return obj;
    }

}
