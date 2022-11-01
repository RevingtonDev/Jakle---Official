package dev.revington.variables;

import net.minidev.json.JSONObject;

public class StatusHandler {

    public static JSONObject S200 = append(200, "Query successful.");

    public static JSONObject E500 = append(500, "Internal server error.");
    
    public static JSONObject E1020 = append(1020, "Spam protected.");
    
    public static JSONObject E1021 = append(1021, "Please try again later.");

    public static JSONObject append(int code, String query) {
        JSONObject result = new JSONObject();
        result.put("code", code);
        result.put("query", query);
        return result;
    }

}
