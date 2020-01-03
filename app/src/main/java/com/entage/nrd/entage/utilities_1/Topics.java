package com.entage.nrd.entage.utilities_1;

public class Topics {

    // THIS TOPICS FOR ALL USERS
    private static String ENTAGE_APP =  "entaji_app";

    // THIS TOPICS FOR USERS HAS ENTAGE PAGE
    private static String USERS_ENTAGE_PAGE =  "users_entage_page";

    // THIS TOPICS FOR ADMIN USERS HAS ENTAGE PAGE
    private static String ADMIN_USERS_ENTAGE_PAGE =  "admin_users_entage_page";

    // THIS TOPICS FOR CUSTOMERS IN ENTAGE PAGE
    private static String CUSTOMER_IN_ENTAGE_PAGE =  "customers_in_entage_page";

    // THIS TOPIC FOR USERS HAS FOLLOW ENTAGE PAGE
    private static String FOLLOWING =  "following_";

    // THIS TOPIC FOR USERS HAS TURN ON NOTIFICATIONS IN ENTAGE PAGE THAT FOLLOW
    private static String NOTIFYING =  "notifying_";

    // THIS TOPIC FOR User Question
    private static final String USER_QUESTION =  "user_question_";


    public static String getTopicsEntajiApp(){
        return ENTAGE_APP;
    }

    public static String getTopicsUsersEntagePage(){
        return USERS_ENTAGE_PAGE ;
    }

    public static String getTopicsAdminInEntagePage(String page_id){ return ADMIN_USERS_ENTAGE_PAGE + page_id ; }

    public static String getTopicsCustomersInEntagePage(String page_id){ return CUSTOMER_IN_ENTAGE_PAGE + page_id ; }

    public static String getTopicsUserQuestion(String question_id){ return USER_QUESTION + question_id ; }

    public static String getTopicsFollowing(String page_id){
        return FOLLOWING + page_id ;
    }

    public static String getTopicsNotifying(String page_id){
        return NOTIFYING + page_id;
    }





}
