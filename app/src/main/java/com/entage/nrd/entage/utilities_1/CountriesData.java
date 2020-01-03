package com.entage.nrd.entage.utilities_1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;

public class CountriesData {
    private static final String TAG = "CountriesData";


    private static HashMap<String,String> countriesNameCode;
    private static HashMap<String,String> countriesNameId;
    public static HashMap<String, String> data;
    private static ArrayList<String> countriesNames ;
    private static String countryUserId;

    public CountriesData() {

    }

    public static ArrayList<String> getCountriesNames(String _countryUserId){
        countryUserId = _countryUserId;
        if (countriesNames == null){
            setupData();
        }
        return countriesNames;
    }

    public static String getCountryId(String nameCountry){
        if(countriesNameId == null){
            setupData();
        }
        return countriesNameId.get(nameCountry);
    }

    public static String getCountryCode(String nameCountry){
        if(countriesNameCode == null){
            setupData();
        }
        return countriesNameCode.get(nameCountry);
    }

    public static String getCountryIdByCode(String codeCountry){
        if(countriesNameId == null){
            setupData();
        }
        return data.get(codeCountry);
    }

    private static void setupData(){
        setupIdCountries();

        String[] locales = Locale.getISOCountries();
        countriesNameCode = new HashMap<>();
        countriesNameId = new HashMap<>();

        countriesNames = new ArrayList<>();
        String userCountry = "" ;
        for (String locale : locales) {
            Locale obj = new Locale(Locale.getDefault().getDisplayLanguage(), locale);
            if (data.containsKey(obj.getCountry())) {
                countriesNameCode.put(obj.getDisplayCountry(), obj.getCountry());
                countriesNameId.put(obj.getDisplayCountry(), data.get(obj.getCountry()));
                countriesNames.add(obj.getDisplayCountry());

                if(countryUserId!=null && countryUserId.equals(obj.getCountry())){
                    userCountry = obj.getDisplayCountry();
                }
            }
        }

        /* Sort statement*/
        Collections.sort(countriesNames);
        if(countriesNames.contains(userCountry)){
            countriesNames.remove(userCountry);
            countriesNames.add(0, userCountry);
        }
    }

    private static void setupIdCountries(){
        //geonamesSearch.setFormatSearch("cv", "", "", "A", "PCLI");
        data = new HashMap<>();
        data.put("IT","3175395");
        data.put("CN","1814991");
        data.put("IN","1269750");
        data.put("US","6252001");
        data.put("BR","3469034");
        data.put("JP","1861060");
        data.put("VN","1562822");
        data.put("DE","2921044");
        data.put("EG","357994");
        data.put("TR","298795");
        data.put("IR","130758");
        data.put("TH","1605651");
        data.put("FR","3017382");
        data.put("GB","2635167");
        data.put("MM","1327865");
        data.put("ZA","953987");
        data.put("KR","1835841");
        data.put("KE","192950");
        data.put("PL","798544");
        data.put("SD","366755");
        data.put("PE","3932488");
        data.put("NP","1282988");
        data.put("GH","2300660");
        data.put("YE","69543");
        data.put("KP","1873107");
        data.put("TW","1668284");
        data.put("AU","2077456");
        data.put("CI","2287781");
        data.put("CL","3895114");
        data.put("ML","2453866");
        data.put("CU","3562981");
        data.put("GR","390903");
        data.put("TD","2434508");
        data.put("GN","2420477");
        data.put("SE","2661886");
        data.put("LA","1655842");
        data.put("FI","660013");
        data.put("NO","3144096");
        data.put("CF","239880");
        data.put("MN","2029969");
        data.put("CG","2260494");
        data.put("OM","286963");
        data.put("GA","2400553");
        data.put("CZ","3077311");
        data.put("BJ","2395170");
        data.put("AT","2782113");
        data.put("JO","248816");
        data.put("FJ","2205218");
        data.put("NL","2750405");
        data.put("BE","2802361");
        data.put("TG","2363686");
        data.put("DK","2623032");
        data.put("BA","3277605");
        data.put("IS","2629691");
        data.put("TO","4032283");
        data.put("HT","3723988");
        data.put("CH","2658434");
        data.put("BT","1252634");
        data.put("KM","921929");
        data.put("ME","3194884");
        data.put("BN","1820814");
        data.put("BH","290291");
        data.put("MT","2562770");
        data.put("NR","2110425");
        data.put("BD","1210997");
        data.put("RU","2017370");
        data.put("MX","3996063");
        data.put("PH","1694008");
        data.put("CD","203312");
        data.put("ES","2510769");
        data.put("UA","690791");
        data.put("AR","3865483");
        data.put("DZ","2589581");
        data.put("MA","2542007");
        data.put("CA","6251999");
        data.put("UG","226074");
        data.put("IQ","99237");
        data.put("AF","1149361");
        data.put("SY","163843");
        data.put("CM","2233387");
        data.put("NE","2440476");
        data.put("MW","927384");
        data.put("KH","1831722");
        data.put("ZM","895949");
        data.put("AO","3351879");
        data.put("SN","2245662");
        data.put("PT","2264397");
        data.put("TN","2464461");
        data.put("SO","51537");
        data.put("SS","7909807");
        data.put("LY","2215636");
        data.put("PY","3437598");
        data.put("TM","1218197");
        data.put("NZ","2186224");
        data.put("UY","3439705");
        data.put("BW","933860");
        data.put("MU","934292");
        data.put("HU","719819");
        data.put("AE","290557");
        data.put("PA","3703430");
        data.put("GY","3378535");
        data.put("SB","2103350");
        data.put("BS","3572887");
        data.put("RW","49518");
        data.put("RS","6290252");
        data.put("GE","614540");
        data.put("IE","2963597");
        data.put("MD","617790");
        data.put("LT","597427");
        data.put("SR","3382998");
        data.put("SC","241170");
        data.put("BI","433561");
        data.put("IL","294640");
        data.put("LS","932692");
        data.put("GW","2372248");
        data.put("TL","1966436");
        data.put("MV","1282028");
        data.put("TV","2110297");
        data.put("LB","272103");
        data.put("JM","3489940");
        data.put("KW","285570");
        data.put("XK","831053");
        data.put("GM","2413451");
        data.put("CY","146669");
        data.put("CV","3374766");
        data.put("BZ","3582678");
        data.put("SG","1880251");
        data.put("QA","289688");
        data.put("DJ","223816");
        data.put("WS","4034894");
        data.put("LU","2960313");
        data.put("GD","3580239");
        data.put("AD","3041565");
        data.put("DM","3575830");
        data.put("MC","2993457");
        data.put("VA","3164670");
        data.put("PK","1168579");
        data.put("NG","2328926");
        data.put("ET","337996");
        data.put("CO","3686110");
        data.put("TZ","149590");
        data.put("MY","1733045");
        data.put("VE","3625428");
        data.put("MZ","1036973");
        data.put("RO","798549");
        data.put("EC","3658394");
        data.put("ZW","878675");
        data.put("GT","3595528");
        data.put("BO","3923057");
        data.put("BY","630336");
        data.put("HN","3608932");
        data.put("NI","3617476");
        data.put("ER","338010");
        data.put("KG","1527747");
        data.put("MR","2378080");
        data.put("NA","3355338");
        data.put("LK","1227603");
        data.put("AZ","587116");
        data.put("BG","732800");
        data.put("HR","3202326");
        data.put("LR","2275384");
        data.put("VU","2134431");
        data.put("DO","3508796");
        data.put("SL","2403846");
        data.put("LV","458258");
        data.put("EE","453733");
        data.put("KI","4030945");
        data.put("SK","3057568");
        data.put("AL","783754");
        data.put("AM","174982");
        data.put("SV","3585968");
        data.put("MK","718075");
        data.put("SZ","934841");
        data.put("BB","3374084");
        data.put("LC","3576468");
        data.put("SM","3168068");
        data.put("ID","1643084");
        data.put("SA","102358");
        data.put("MG","1062947");
        data.put("BF","2361809");
        data.put("KZ","1522867");
        data.put("TJ","1220409");
        data.put("PG","2088628");
        data.put("CR","3624060");
        data.put("GQ","2309096");
        data.put("SI","3190538");
        data.put("LI","3042058");
        data.put("UZ","1512440");
        data.put("TT","3573591");
        data.put("ST","2410758");
        data.put("AG","3576396");
        data.put("KN","3575174");
        data.put("VC","3577815");
    }
}
