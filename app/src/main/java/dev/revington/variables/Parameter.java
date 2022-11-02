package dev.revington.variables;
 
import net.minidev.json.JSONObject;

public class Parameter {

    public static String CODE = "code";
    public static String QUERY = "query";

    public static String AUTH = "jk_token";

    public static String DUP_EMAIL = "An account already exists linked to this email.";
    public static String NO_ACCOUNT = "Incorrect email or password.";
    public static String ACCOUNT_DISABLED = "Account is disabled. Please check your emails.";
    public static String INACTIVE_ACC = "Account is not verified yet. Please check your emails.";
    public static String NEW_ACCOUNT_CREATED = "Your account has been successfully created and an activation link has been send to your email address.";
    public static String WENT_WRONG = "Something went wrong.";
    public static String EXPIRED_TOKEN = "Token expired.";
    public static String LINK_SENT_SUCCESSFUL = "An activation link successfully send.";
    public static String TRY_AGAIN = "Please try again later.";
    
    public static String RESULTS = "results";
    public static String DESCRIPTION = "des";
    
    public static int ACTIVE = 0;
    public static int DISABLED = 1;

    public static int ALL_GRANTS = 1;
    public static int GRANT_ACTIVATION = 2;
    public static int GRANT_REACTIVATION = 3;
    public static int GRANT_RESET = 4;

    public static int INACTIVE = 0;
    public static int ACTIVATED = 1;

    public enum Category {
        FRIEND_REQUEST("F_R_"), REQUEST_ACCEPT("F_R_A");
        public final String label;

        private Category(String label) {
            this.label = label;
        }
    };

    public static String TITLE = "title";
    public static String CONTENT = "content";

    public static JSONObject setNotificationMessage(String title, String content) {
        JSONObject msg = new JSONObject();
        msg.put(TITLE, title);
        msg.put(CONTENT, content);
        return msg;
    }

    public static String REQUEST = "request";

    public static String COOKIE = "cookie";

    public static int COOKIE_TIMEOUT = 60 * 60 * 24 * 30 * 6;
    public static int ACTIVATION_TOKEN_TIMEOUT = 60 * 60 * 6;
    public static int RESET_TOKEN_TIMEOUT = 60 * 15;

    public static String CLIENT_ID = "client_id";
    public static String SESSION_ID = "session_id";
    public static final String SIMP_SESSION_ID = "simpSessionId";
    public static final String SIMP_SESSION_ATTRIBUTES = "simpSessionAttributes";

    public static int OFFLINE = 0;
    public static int ONLINE = 1;
    // public static int BUSY = 2; 

    public static JSONObject PULL_NOTIFICATIONS = new JSONObject().appendField("pull", true);

    public static String ELEMENTS = "total_elements";
    public static String PAGES = "total_pages";
    public static String PAGE = "current_page";

    public static final String ACTION = "action";
    public static final String DATA = "data";

    public static final String WS_NOTIFY = "/app/notify";
    public static final String WS_QUEUE = "/app/queue";

    public static String WS_ACTION_PULL = "pull";
    public static String WS_ACTION_REFRESH = "refresh";
    public static String WS_ACTION_MESSAGE_RECIEVE = "message_recieve";
    public static String WS_ACTION_MESSAGE_DELETE = "message_delete";
    public static String WS_ACTION_MESSAGE_SEND = "message_send";
    public static String WS_ACTION_DELETE_USER = "delete_friend";
    public static String WS_ACTION_ADD_USER = "add_friend";
    public static String WS_ACTION_RECIEVE = "recieve";
    public static String WS_ACTION_ACTIVE = "active";
    public static String WS_ACTION_NOTIFY = "notify";

    public static String WS_ID = "id";
    public static String WS_MESSAGE_ID = "msg_id";
    public static String WS_CONTENT = "content";
    public static String WS_TIME = "time";
    public static String WS_SENT = "sent";
    public static String WS_DATA = DATA;
    public static String WS_ACTION = ACTION;

    public static String CLIENT_ONLINE = "make_online";
    public static String CLIENT_OFFLINE = "make_offline";
    public static String CLIENT_TYPING = "make_typing";
    
}
