package com.coding4fun.utils;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by coding4fun on 17-Apr-17.
 */

public class Constants {

    public static final String TAG = "HandRider_UniBus";
    public static final String UPDATE_CURRENT_LOCATION_BROADCAST = "updateCurrentLocationBroadcast";
    public static final LatLng LOCATION_BAU_BEIRUT = new LatLng(33.87177737417301,35.49630884081125); // real
    //public static final LatLng LOCATION_BAU_BEIRUT = new LatLng(33.68305525065856 ,35.46291500329971);
    //public static final LatLng LOCATION_BAU_BEIRUT = new LatLng(33.69366316644295,35.42533993721008); //sa3diyyet
    public static final LatLng LOCATION_BAU_DEBBIEH = new LatLng(33.67607214239718,35.46580508351326); // real
    //public static final LatLng LOCATION_BAU_DEBBIEH = new LatLng(33.672673708845565,35.46635828912258);
    public static final String ENCODED_PATH = "wqvmE}xswElFTvETGjC@p@Dj@FXBHH?nAHpOr@dRp@dCJVC|BBV?v@KjFV~BD`GV|EPbEVhDLrC?pII|R]lIO~Za@jBGpEOpEa@|Eu@pAOrAIpACrABvBRdCLfAD~@BxAOZKZKt@_@z@q@|@cAt@mA`@_ApAyCl@qAZcAXw@~@oDd@gBt@qBp@mAt@cAdAgAV]d@o@`F_JjAuBt@eA|@eAv@m@jDcC|@[|@QP?N@`@Fh@Pb@RbW|Pz]jVzPrLdHfFvAz@lA`AlD`CzDnCbD~BlDdCxCvBxJfH`LfIvO`LxJfHl@f@dHfFhAv@jAp@ZLv@Rl@H`AB`D[~C[z@IhACjA@z@F|@Lv@PtA`@n@Xz@d@jAn@xDvBdGfD~CjB`@ZfCtApItEjNzHfFrCnRlK~MzHbHxDXNfXhO|FjDfB~@dBdA|ApAtG|GVVvNxO\\XxEdFhEpEhAfAnA`ArA|@jKvF`I~DtLlG`PzIfIpEn@ZdBf@r@NjALtBJrA@pAE`OgArOmA|OoAxIk@rDSxAEjBAtABnGHtKh@fMr@nQdApZdB|H`@`X~AtDZlAPzATpB\\dFdA`Cf@pAX~H~BvCnAdCtA|C|B|CjCtHjIfBlBhE~EpFpGz@x@bEdEpMpHt@d@fAz@pAjApCnDpAxBv@`BhA|Ct@jCpCtLlCzL~AvGz@rC@PBR~AnFxBbHh@nB`@xBlDe@z@HNJJJZd@pA`Br@n@`Af@j@Nv@Jl@?z@GnA]bAg@VWRa@Fe@HiFLiHEsAQcAiAqCcBmESy@Ec@C{ECeECaGCmDGkAgAaJKoBDoAz@mF|@uFLcAb@{Fn@uHPgCHq@XaATu@hAqC`DgIpA_DLO\\SVC`@@j@PlGfE|AdATJ`@BXGTIVQdAeA|AaBVe@^mAXiAN[LOPG\\ARD`BdBhAhAXLTFV@b@CzCaAb@UXc@Fk@IiAQoAa@}Am@{Bk@iCu@cEo@wDeAaH_AmI[aDE{@GsEKiMQiLGeAKu@aCsMc@_Ck@{ByC{Jm@uBGg@?U@SLg@Zm@v@gAh@w@h@{@L_@Jm@@eC?wAJiAL}@R}@\\u@n@m@b@UXGdDs@bBc@d@WfA{@tAwAt@eB~C{HR]\\]NKhA]pI{B~@Wd@QDEDCf@b@XbBr@dHd@zENbBA|AIdBc@rDeArH]tB_@`DQ`CAlANtFH~D^xH?`@EBGJEN?LDPJHJDH@";
    /**** FOR TESTING ****/
    //public static final LatLng LOCATION_BAU_BEIRUT = new LatLng(34.46050408744363,35.90884432196617);
    //public static final LatLng LOCATION_BAU_DEBBIEH = new LatLng(34.43463546245568,35.83613332360983); //BAU mina
    //public static final LatLng LOCATION_BAU_DEBBIEH = new LatLng(34.45116180503053,35.861612632870674); //baddawi
    //public static final String ENCODED_PATH = "{piqEsldzEkBt@o@\\BNQ@sDnAcDrAmB|@w@PICEAMDILAL@LBHXvAh@tEXdEPjBDrA^`VHzFFzMBvR?`AJtD^nCp@xCt@tBv@~A`NfXvArCfBrDl@~AzBbI|A~F^jBjBtLp@tEZxB~D`VRlB^xBdBhIh@nB\\bAf@|AzAbFfB`FjCzHz@dCx@jBxBtErFrLh@bA`BfC`B~CPp@FVh@~Cr@~C|A`JxAnInAvHpA`HJfAHbDENCl@H|@Vv@Zh@\\ZDBTr@Tl@jAfG^`AvSjYjBhCd@h@h@b@tAdAdDvB|B`B\\Rd@R~A^lGdBhKtC";
    /********************/
    public static final String GEOFENCE_ID_BAU_BEIRUT = "BAU_BEIRUT";
    public static final String GEOFENCE_ID_BAU_DEBBIEH = "BAU_DEBBIEH";
    public static final String GEOFENCE_ID_PASSENGER = "PASSENGER";
    public static final float GEOFENCE_RADIUS_IN_METERS = 200f;
    public static final String ACTION_START = "start_service";
    public static final String ACTION_STOP = "stop_service";
    public static final String ACTION_BUS_IS_FULL = "bus_is_full";
    public static final String ACTION_GEOFENCE_ENTER_BEIRUT = "GEOFENCE_ENTER_BEIRUT";
    public static final String ACTION_GEOFENCE_EXIT_BEIRUT = "GEOFENCE_EXIT_BEIRUT";
    public static final String ACTION_GEOFENCE_ENTER_DEBBIEH = "GEOFENCE_ENTER_DEBBIEH";
    public static final String ACTION_GEOFENCE_EXIT_DEBBIEH = "GEOFENCE_EXIT_DEBBIEH";
    public static final String ACTION_NEW_PASSENGER = "NEW_PASSENGER";
    public static final String ACTION_CANCEL_PASSENGER = "CANCEL_PASSENGER";
    public static final String ACTION_GEOFENCE_ENTER_PASSENGER = "GEOFENCE_ENTER_PASSENGER";
    public static final String ACTION_GEOFENCE_EXIT_PASSENGER = "GEOFENCE_EXIT_PASSENGER";
    public static final String INTENT_EXTRA_FINISH_RIDE_AND_STOP_SERVICE = "FINISH_RIDE_AND_STOP_SERVICE";
    public static final String INTENT_EXTRA_NEW_LAT = "newLat";
    public static final String INTENT_EXTRA_NEW_LNG = "newLng";
    public static final String INTENT_EXTRA_NEW_BEARING = "newBearing";
    public static final String DRIVING_STATUS_WAITING_IN_BEIRUT = "Waiting in Beirut carage";
    public static final String DRIVING_STATUS_WAITING_IN_DEBBIEH = "Waiting in Debbieh carage";
    public static final String DRIVING_STATUS_GOING_TO_BEIRUT = "On my way to Beirut campus";
    public static final String DRIVING_STATUS_GOING_TO_DEBBIEH = "On my way to Debbieh campus";


}