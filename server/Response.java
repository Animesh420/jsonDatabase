package server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

class Response {
    // POJO for sending response back to the client.
    public String response = null;
    public String reason = null;
    public JsonElement value = null;


    public Response(String response, String reason, JsonElement value) {
        this.response = response;
        this.reason = reason;
        this.value = value;
    }

    public Response(String response) {
        this.response = response;
    }

    public String toJsonString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

}

