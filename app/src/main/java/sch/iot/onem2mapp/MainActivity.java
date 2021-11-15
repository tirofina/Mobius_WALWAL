package sch.iot.onem2mapp;

import android.app.NotificationChannel; // added by S. Lee, SCH Univ.
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;

import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import fr.arnaudguyon.xmltojsonlib. XmlToJson;


import static sch.iot.onem2mapp.R.layout.activity_main;

public class MainActivity extends AppCompatActivity implements Button.OnClickListener, CompoundButton.OnCheckedChangeListener {
    public Button btnRetrieve;
    public Switch Switch_MQTT;
    public TextView textViewData;

    // added by by S. Lee, SCH Univ.
    public TextView textTrain;
    public TextView textRemains;
    public TextView textEat;
    public TextView textAlert;
    public TextView textSuccess;

    public ToggleButton btn_train;
    public ToggleButton btnSitdown;
    public ToggleButton btnWait;

    public Handler handler;
    public ToggleButton btnAddr_Set;

    private static CSEBase csebase = new CSEBase();
    private static AE ae = new AE();
    private static String TAG = "MainActivity";
    private String MQTTPort = "1883";

    // Modify this variable associated with your AE name in Mobius, by J. Yun, SCH Univ.
    private String ServiceAEName = "walwal";

    private String MQTT_Req_Topic = "";
    private String MQTT_Resp_Topic = "";
    private MqttAndroidClient mqttClient = null;
    private EditText EditText_Address =null;
    private String Mobius_Address ="";

    // added b

    private static final String PRIMARY_CHANNEL_ID_1 = "primary_notification_channel_1";
    private static final String PRIMARY_CHANNEL_ID_2 = "primary_notification_channel_2";
    private static final String PRIMARY_CHANNEL_ID_3 = "primary_notification_channel_3";
    private static final String PRIMARY_CHANNEL_ID_4 = "primary_notification_channel_4";
    private static final String PRIMARY_CHANNEL_ID_5 = "primary_notification_channel_5";
    private static final String PRIMARY_CHANNEL_ID_6 = "primary_notification_channel_6";

    private NotificationManager mNotificationManager;

    private static final int NOTIFICATION_ID_1 = 0;
    private static final int NOTIFICATION_ID_2 = 0;
    private static final int NOTIFICATION_ID_3 = 0;
    private static final int NOTIFICATION_ID_4 = 0;
    private static final int NOTIFICATION_ID_5 = 0;
    private static final int NOTIFICATION_ID_6 = 0;

    public Button button_notify_1;
    public Button button_notify_2;
    public Button button_notify_3;
    public Button button_notify_4;
    public Button button_notify_5;
    public Button button_notify_6;

    // Main
    public MainActivity() {
        handler = new Handler();
    }

    /* onCreate */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_main);

        btnRetrieve = findViewById(R.id.btnRetrieve);
        Switch_MQTT = findViewById(R.id.switch_mqtt);
        textViewData = findViewById(R.id.textViewData);
        EditText_Address = findViewById(R.id.editText);
        btnAddr_Set = findViewById(R.id.toggleButton_Addr);

        btn_train = findViewById(R.id.btn_train);
        btnSitdown =  findViewById(R.id.btnSitdown);
        btnWait =  findViewById(R.id.btnWait);

        textRemains = findViewById(R.id.textRemains);
        textEat = findViewById(R.id.textEat);
        textAlert = findViewById(R.id.textAlert);
        textSuccess = findViewById(R.id.textSuccess);


        btnRetrieve.setOnClickListener(this);
        Switch_MQTT.setOnCheckedChangeListener(this);
        btn_train.setOnClickListener(this);
        btnWait.setOnClickListener(this);
        btnSitdown.setOnClickListener(this);
        btnAddr_Set.setOnClickListener(this);


        // added by by S. Lee, SCH Univ.
        button_notify_1 = findViewById(R.id.notify_1);
        button_notify_2 = findViewById(R.id.notify_2);
        button_notify_3 = findViewById(R.id.notify_3);
        button_notify_4 = findViewById(R.id.notify_4);
        button_notify_5 = findViewById(R.id.notify_5);
        button_notify_6 = findViewById(R.id.notify_6);
        btnAddr_Set.setFocusable(true);

        btnSitdown.setVisibility(View.INVISIBLE);
        btnWait.setVisibility(View.INVISIBLE);

        // Create AE and Get AEID

        button_notify_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendNotification_1();
            }
        });
        button_notify_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendNotification_2();
            }
        });
        button_notify_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendNotification_3();
            }
        });
        button_notify_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendNotification_4();
            }
        });
        button_notify_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendNotification_5();
            }
        });
        button_notify_6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendNotification_6();
            }
        });
        createNotificationChannel();

        btnAddr_Set.performClick();
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                btnRetrieve.performClick();

                new Handler().postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Switch_MQTT.performClick();
                    }
                }, 600);
            }
        }, 600);

    }
    /* AE Create for Androdi AE */
    public void GetAEInfo() {

        // You can put the IP address directly in code,
        // but also get it from EditText window
        Mobius_Address = EditText_Address.getText().toString();
//         csebase.setInfo(Mobius_Address,"7579","Mobius","1883");
        csebase.setInfo("203.253.128.177","7579","Mobius","1883");

        // AE Create for Android AE
        ae.setAppName("ncubeapp");
        aeCreateRequest aeCreate = new aeCreateRequest();
        aeCreate.setReceiver(new IReceived() {
            public void getResponseBody(final String msg) {
                handler.post(new Runnable() {
                    public void run() {
                        Log.d(TAG, "** AE Create ResponseCode[" + msg +"]");
                        if( Integer.parseInt(msg) == 201 ){
                            MQTT_Req_Topic = "/oneM2M/req/Mobius2/"+ae.getAEid()+"_sub"+"/#";
                            MQTT_Resp_Topic = "/oneM2M/resp/Mobius2/"+ae.getAEid()+"_sub"+"/json";
                            Log.d(TAG, "ReqTopic["+ MQTT_Req_Topic+"]");
                            Log.d(TAG, "ResTopic["+ MQTT_Resp_Topic+"]");
                        }
                        else { // If AE is Exist , GET AEID
                            aeRetrieveRequest aeRetrive = new aeRetrieveRequest();
                            aeRetrive.setReceiver(new IReceived() {
                                public void getResponseBody(final String resmsg) {
                                    handler.post(new Runnable() {
                                        public void run() {
                                            Log.d(TAG, "** AE Retrive ResponseCode[" + resmsg +"]");
                                            MQTT_Req_Topic = "/oneM2M/req/Mobius2/"+ae.getAEid()+"_sub"+"/#";
                                            MQTT_Resp_Topic = "/oneM2M/resp/Mobius2/"+ae.getAEid()+"_sub"+"/json";
                                            Log.d(TAG, "ReqTopic["+ MQTT_Req_Topic+"]");
                                            Log.d(TAG, "ResTopic["+ MQTT_Resp_Topic+"]");
                                        }
                                    });
                                }
                            });
                            aeRetrive.start();
                        }
                    }
                });
            }
        });
        aeCreate.start();
    }

    // Switch - Get MQTT, by J. Yun, SCH Univ.

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if (isChecked) {
            Log.d(TAG, "MQTT Create");
            MQTT_Create(true);
        } else {
            Log.d(TAG, "MQTT Close");
            MQTT_Create(false);
        }
    }



    /* MQTT Subscription */
    public void MQTT_Create(boolean mtqqStart) {
        if (mtqqStart && mqttClient == null) {
            /* Subscription Resource Create to Yellow Turtle */
            // added by S. Lee, SCH Univ.

            SubscribeResource

            subcribeResource = new SubscribeResource("remains");
            subcribeResource.setReceiver(new IReceived() {
                public void getResponseBody(final String msg) {
                    handler.post(new Runnable() {
                        public void run() {
                            textViewData.setText("**** Subscription Resource Creation Response ****\r\n\r\n" + msg);
                        }
                    });
                }
            });
            subcribeResource.start();
            
            subcribeResource = new SubscribeResource("eat");
            subcribeResource.setReceiver(new IReceived() {
                public void getResponseBody(final String msg) {
                    handler.post(new Runnable() {
                        public void run() {
                            textViewData.setText("**** Subscription Resource Creation Response ****\r\n\r\n" + msg);
                        }
                    });
                }
            });
            subcribeResource.start();
            
            subcribeResource = new SubscribeResource("alert");
            subcribeResource.setReceiver(new IReceived() {
                public void getResponseBody(final String msg) {
                    handler.post(new Runnable() {
                        public void run() {
                            textViewData.setText("**** Subscription Resource Creation Response ****\r\n\r\n" + msg);
                        }
                    });
                }
            });
            subcribeResource.start();
            
            subcribeResource = new SubscribeResource("success");
            subcribeResource.setReceiver(new IReceived() {
                public void getResponseBody(final String msg) {
                    handler.post(new Runnable() {
                        public void run() {
                            textViewData.setText("**** Subscription Resource Creation Response ****\r\n\r\n" + msg);
                        }
                    });
                }
            });
            subcribeResource.start();

            /* MQTT Subscribe */
            mqttClient = new MqttAndroidClient(this.getApplicationContext(), "tcp://" + csebase.getHost() + ":" + csebase.getMQTTPort(), MqttClient.generateClientId());
            mqttClient.setCallback(mainMqttCallback);
            try {
                // added by J. Yun, SCH Univ.
                // Modified by S. Lee, SCH Univ.
                MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
                mqttConnectOptions.setKeepAliveInterval(600);
                mqttConnectOptions.setCleanSession(false);

                IMqttToken token = mqttClient.connect(mqttConnectOptions);
                token.setActionCallback(mainIMqttActionListener);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        } else {
            /* MQTT unSubscribe or Client Close */
            mqttClient.setCallback(null);
            mqttClient.close();
            mqttClient = null;
        }
    }

    /* MQTT Listener */
    private IMqttActionListener mainIMqttActionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            Log.d(TAG, "onSuccess");
            String payload = "";
            int mqttQos = 1; /* 0: NO QoS, 1: No Check , 2: Each Check */

            MqttMessage message = new MqttMessage(payload.getBytes());
            try {
                mqttClient.subscribe(MQTT_Req_Topic, mqttQos);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            Log.d(TAG, "onFailure");
        }
    };

    /* MQTT Broker Message Received */
    private MqttCallback mainMqttCallback = new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
            Log.d(TAG, "connectionLost");
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {

            Log.d(TAG, "messageArrived");

            textViewData.setText("");
            textViewData.setText("MQTT data received\r\n\r\n" + message.toString().replaceAll(",", "\n"));
            Log.d(TAG, "Notify ResMessage:" + message.toString());

            // Added by J. Yun, SCH Univ.
            // Modified by S. Lee, SCH Univ.
            String cnt = getContainerName(message.toString());
            Log.d(TAG, "Received container name is " + cnt);
            //textViewData.setText(cnt);

            if (cnt.indexOf("eat") != -1)
            {
                if(Integer.parseInt(getContainerContentJSON(message.toString())) == 1)
                {
                    textEat.setText("Ate");
                    sendNotification_3();
                }
                else
                {
                    textEat.setText("Not");
                }

            }

            if (cnt.indexOf("success") != -1)
            {
                if(Integer.parseInt(getContainerContentJSON(message.toString())) == 1)
                {
                    textSuccess.setText("ASDFASDF");
                    sendNotification_4();

                    new Handler().postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            btn_train.performClick();
                            btnSitdown.setChecked(false);
                            btnWait.setChecked(false);
                        }
                    }, 600);
                }
                else
                {
                    sendNotification_6();
                    btn_train.performClick();
                    btnSitdown.setChecked(false);
                    btnWait.setChecked(false);

                }

            }


            if (cnt.indexOf("alert") != -1)
            {
                if(Integer.parseInt(getContainerContentJSON(message.toString())) == 1)
                {
                    textAlert.setText("Alert!");
                    sendNotification_5();
                }
                else
                {
                    textAlert.setText("Nope Alert :)");
                }

            }

            else
                ;

            /* Json Type Response Parsing */
            String retrqi = MqttClientRequestParser.notificationJsonParse(message.toString());
            Log.d(TAG, "RQI["+ retrqi +"]");

            String responseMessage = MqttClientRequest.notificationResponse(retrqi);
            Log.d(TAG, "Recv OK ResMessage ["+responseMessage+"]");

            /* Make json for MQTT Response Message */
            MqttMessage res_message = new MqttMessage(responseMessage.getBytes());

            try {
                mqttClient.publish(MQTT_Resp_Topic, res_message);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            Log.d(TAG, "deliveryComplete");
        }

    };

    // Added by J. Yun, SCH Univ.
    // Modified by S. Lee, SCH Univ.
    private String getContainerName(String msg) {
        String cnt = "";
        try {
            JSONObject jsonObject = new JSONObject(msg);
            cnt = jsonObject.getJSONObject("pc").
                    getJSONObject("m2m:sgn").getString("sur");
            // Log.d(TAG, "Content is " + cnt);
        } catch (JSONException e) {
            Log.e(TAG, "JSONObject error!");
        }
        return cnt;
    }

    // Added by J. Yun, SCH Univ.
    private String getContainerContentJSON(String msg) {
        String con = "";
        try {
            JSONObject jsonObject = new JSONObject(msg);
            con = jsonObject.getJSONObject("pc").
                    getJSONObject("m2m:sgn").
                    getJSONObject("nev").
                    getJSONObject("rep").
                    getJSONObject("m2m:cin").
                    getString("con");
//            Log.d(TAG, "Content is " + con);
        } catch (JSONException e) {
            Log.e(TAG, "JSONObject error!");
        }
        return con;
    }

    // Added by J. Yun, SCH Univ.
    private String getContainerContentXML(String msg) {
        String con = "";
        try {
            XmlToJson xmlToJson = new XmlToJson.Builder(msg).build();
            JSONObject jsonObject = xmlToJson.toJson();
            con = jsonObject.getJSONObject("m2m:cin").getString("con");
//            Log.d(TAG, "Content is " + con);
        } catch (JSONException e) {
            Log.e(TAG, "JSONObject error!");
        }
        return con;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnSitdown: {
                if (((ToggleButton) v).isChecked()) {

                    btnSitdown.setVisibility(View.VISIBLE);
                    btnWait.setVisibility(View.INVISIBLE);
                    ControlRequest req = new ControlRequest("1");
                    req.setReceiver(new IReceived() {
                        public void getResponseBody(final String msg) {
                            handler.post(new Runnable() {
                                public void run() {
                                    textViewData.setText(msg);
                                }
                            });
                        }
                    });
                    req.start();
                }
                else
                {
                    btnSitdown.setVisibility(View.VISIBLE);
                    btnWait.setVisibility(View.VISIBLE);

                }

                break;
            }


            case R.id.btnWait: {

                if (((ToggleButton) v).isChecked()) {

                    btnSitdown.setVisibility(View.INVISIBLE);
                    btnWait.setVisibility(View.VISIBLE);
                    ControlRequest req = new ControlRequest("2");
                    req.setReceiver(new IReceived() {
                        public void getResponseBody(final String msg) {
                            handler.post(new Runnable() {
                                public void run() {
                                    textViewData.setText(msg);
                                }
                            });
                        }
                    });
                    req.start();
                }
                else
                {
                    btnSitdown.setVisibility(View.VISIBLE);
                    btnWait.setVisibility(View.VISIBLE);

                }

                break;
            }


            case R.id.btnVideo:
                // for URL
                Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse("http://wnsgml4.ddns.net:8081"));
//                Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse("http://google.com"));
                startActivity(intent);
                break;

            case R.id.btnRetrieve: {
                RetrieveRequest

                //MQTT
                req = new RetrieveRequest("success");
                req.setReceiver(new IReceived() {
                    public void getResponseBody(final String msg) {
                        handler.post(new Runnable() {
                            public void run() {
                                if(Integer.parseInt(getContainerContentXML(msg)) == 1)
                                {
                                    textSuccess.setText("YES");
                                    new Handler().postDelayed(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            btn_train.performClick();
                                            btnSitdown.setChecked(false);
                                            btnWait.setChecked(false);

                                        }
                                    }, 600);
                                }
                                else
                                    textSuccess.setText("NO");
                            }
                        });
                    }
                });
                req.start();


                req = new RetrieveRequest("eat");
                req.setReceiver(new IReceived() {
                    public void getResponseBody(final String msg) {
                        handler.post(new Runnable() {
                            public void run() {
                                if(Integer.parseInt(getContainerContentXML(msg)) == 1)
                                {
                                    textEat.setText("Ate");
                                    sendNotification_3();
                                }
                                else
                                    textEat.setText("Not");
                            }
                        });
                    }
                });
                req.start();


                req = new RetrieveRequest("alert");
                req.setReceiver(new IReceived() {
                    public void getResponseBody(final String msg) {
                        handler.post(new Runnable() {
                            public void run() {
                                if(Integer.parseInt(getContainerContentXML(msg)) == 1)
                                {
                                    textAlert.setText("Alert!");
                                    sendNotification_5();
                                }
                                else
                                    textAlert.setText("Nope Alert :)");
                            }
                        });
                    }
                });
                req.start();

                req = new RetrieveRequest("remains");
                req.setReceiver(new IReceived() {
                    public void getResponseBody(final String msg) {
                        handler.post(new Runnable() {
                            public void run() {

                                textRemains.setText(getContainerContentXML(msg)+"%");
                                switch (getContainerContentXML(msg)) {
                                    case "0":
                                    case "1":
                                    case "2":
                                    case "3":
                                    case "4":
                                    case "5":
                                    case "6":
                                    case "7":
                                    case "8":
                                    case "9":
                                    case "10":
                                    case "11":
                                    case "12":
                                    case "13":
                                    case "14":
                                    case "15":
                                    case "16":
                                    case "17":
                                    case "18":
                                    case "19":
                                    case "20":
                                    case "21":
                                    case "22":
                                    case "23":
                                    case "24":
                                    case "25":
                                    case "26":
                                    case "27":
                                    case "28":
                                    case "29":
                                    case "30":
                                    case "31":
                                    case "32":
                                    case "33":
                                        sendNotification_2();
                                        break;
                                    case "34":
                                    case "35":
                                    case "36":
                                    case "37":
                                    case "38":
                                    case "39":
                                    case "40":
                                    case "41":
                                    case "42":
                                    case "43":
                                    case "44":
                                    case "45":
                                    case "46":
                                    case "47":
                                    case "48":
                                    case "49":
                                    case "50":
                                    case "51":
                                    case "52":
                                    case "53":
                                    case "54":
                                    case "55":
                                    case "56":
                                    case "57":
                                    case "58":
                                    case "59":
                                    case "60":
                                    case "61":
                                    case "62":
                                    case "63":
                                    case "64":
                                    case "65":
                                    case "66":
                                        sendNotification_1();
                                        break;
                                    default:
                                        break;
                                }

                            }
                        });
                    }
                });
                req.start();



                break;
            }

            case R.id.btn_train: {
                if (((ToggleButton) v).isChecked()) {

                    btnSitdown.setVisibility(View.VISIBLE);
                    btnWait.setVisibility(View.VISIBLE);
                    ControlRequest req = new ControlRequest("0");
                    req.setReceiver(new IReceived() {
                        public void getResponseBody(final String msg) {
                            handler.post(new Runnable() {
                                public void run() {
                                    textViewData.setText(msg);
                                }
                            });
                        }
                    });
                    req.start();
                }
                else
                {
                    btnSitdown.setVisibility(View.VISIBLE);
                    btnWait.setVisibility(View.VISIBLE);
                    ControlRequest req = new ControlRequest("0");
                    req.setReceiver(new IReceived() {
                        public void getResponseBody(final String msg) {
                            handler.post(new Runnable() {
                                public void run() {
                                    textViewData.setText(msg);
                                }
                            });
                        }
                    });
                    req.start();
                }

                break;
            }

            case R.id.toggleButton_Addr: {

                    btnRetrieve.setVisibility(View.VISIBLE);
                    Switch_MQTT.setVisibility(View.VISIBLE);

                    EditText_Address.setHintTextColor(Color.BLUE);
                    EditText_Address.setBackgroundColor(Color.LTGRAY);
                    EditText_Address.setFocusable(false);

                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(EditText_Address.getWindowToken(), 0);//hide keyboard

                    GetAEInfo();

                break;
            }


        }
    }
    @Override
    public void onStart() {
        super.onStart();

    }
    @Override
    public void onStop() {
        super.onStop();

    }

    /* Response callback Interface */
    public interface IReceived {
        void getResponseBody(String msg);
    }

    // Retrieve , added by J. Yun, SCH Univ, Modified by S. Lee, SCH Univ.
    class RetrieveRequest extends Thread {
        private final Logger LOG = Logger.getLogger(RetrieveRequest.class.getName());
        private IReceived receiver;
        //        private String ContainerName = "cnt-co2";
        private String ContainerName = "";


        public RetrieveRequest(String containerName) {
            this.ContainerName = containerName;
        }
        public RetrieveRequest() {}
        public void setReceiver(IReceived hanlder) { this.receiver = hanlder; }

        @Override
        public void run() {
            try {
                String sb = csebase.getServiceUrl() + "/" + ServiceAEName + "/" + ContainerName + "/" + "latest";

                URL mUrl = new URL(sb);

                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(false);

                conn.setRequestProperty("Accept", "application/xml");
                conn.setRequestProperty("X-M2M-RI", "12345");
                conn.setRequestProperty("X-M2M-Origin", ae.getAEid() );
                conn.setRequestProperty("nmtype", "long");
                conn.connect();

                String strResp = "";
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String strLine= "";
                while ((strLine = in.readLine()) != null) {
                    strResp += strLine;
                }

                if ( strResp != "" ) {
                    receiver.getResponseBody(strResp);
                }
                conn.disconnect();

            } catch (Exception exp) {
                LOG.log(Level.WARNING, exp.getMessage());
            }
        }
    }

    /* Request Control train */
    class ControlRequest extends Thread {
        private final Logger LOG = Logger.getLogger(ControlRequest.class.getName());
        private IReceived receiver;
        private String container_name = "training";


        public ContentInstanceObject contentinstance;
        public ControlRequest(String comm) {
            contentinstance = new ContentInstanceObject();
            contentinstance.setContent(comm);
        }
        public void setReceiver(IReceived hanlder) { this.receiver = hanlder; }

        @Override
        public void run() {
            try {
                String sb = csebase.getServiceUrl() +"/" + ServiceAEName + "/" + container_name;

                URL mUrl = new URL(sb);

                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setInstanceFollowRedirects(false);

                conn.setRequestProperty("Accept", "application/xml");
                conn.setRequestProperty("Content-Type", "application/vnd.onem2m-res+xml;ty=4");
                conn.setRequestProperty("locale", "ko");
                conn.setRequestProperty("X-M2M-RI", "12345");
                conn.setRequestProperty("X-M2M-Origin", ae.getAEid() );

                String reqContent = contentinstance.makeXML();
                conn.setRequestProperty("Content-Length", String.valueOf(reqContent.length()));

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                dos.write(reqContent.getBytes());
                dos.flush();
                dos.close();

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String resp = "";
                String strLine="";
                while ((strLine = in.readLine()) != null) {
                    resp += strLine;
                }
                if (resp != "") {
                    receiver.getResponseBody(resp);
                }
                conn.disconnect();

            } catch (Exception exp) {
                LOG.log(Level.SEVERE, exp.getMessage());
            }
        }
    }
    /* Request AE Creation */
    class aeCreateRequest extends Thread {
        private final Logger LOG = Logger.getLogger(aeCreateRequest.class.getName());
        String TAG = aeCreateRequest.class.getName();
        private IReceived receiver;
        int responseCode=0;
        public ApplicationEntityObject applicationEntity;
        public void setReceiver(IReceived hanlder) { this.receiver = hanlder; }
        public aeCreateRequest(){
            applicationEntity = new ApplicationEntityObject();
            applicationEntity.setResourceName(ae.getappName());
            Log.d(TAG, ae.getappName() + "JJjj");
        }
        @Override
        public void run() {
            try {

                String sb = csebase.getServiceUrl();
                URL mUrl = new URL(sb);

                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setInstanceFollowRedirects(false);

                conn.setRequestProperty("Content-Type", "application/vnd.onem2m-res+xml;ty=2");
                conn.setRequestProperty("Accept", "application/xml");
                conn.setRequestProperty("locale", "ko");
                conn.setRequestProperty("X-M2M-Origin", "S"+ae.getappName());
                conn.setRequestProperty("X-M2M-RI", "12345");
                conn.setRequestProperty("X-M2M-NM", ae.getappName() );

                String reqXml = applicationEntity.makeXML();
                conn.setRequestProperty("Content-Length", String.valueOf(reqXml.length()));

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                dos.write(reqXml.getBytes());
                dos.flush();
                dos.close();

                responseCode = conn.getResponseCode();

                BufferedReader in = null;
                String aei = "";
                if (responseCode == 201) {
                    // Get AEID from Response Data
                    in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    String resp = "";
                    String strLine;
                    while ((strLine = in.readLine()) != null) {
                        resp += strLine;
                    }

                    ParseElementXml pxml = new ParseElementXml();
                    aei = pxml.GetElementXml(resp, "aei");
                    ae.setAEid( aei );
                    Log.d(TAG, "Create Get AEID[" + aei + "]");
                    in.close();
                }
                if (responseCode != 0) {
                    receiver.getResponseBody( Integer.toString(responseCode) );
                }
                conn.disconnect();
            } catch (Exception exp) {
                LOG.log(Level.SEVERE, exp.getMessage());
            }

        }
    }
    /* Retrieve AE-ID */
    class aeRetrieveRequest extends Thread {
        private final Logger LOG = Logger.getLogger(aeCreateRequest.class.getName());
        private IReceived receiver;
        int responseCode=0;

        public aeRetrieveRequest() {
        }
        public void setReceiver(IReceived hanlder) {
            this.receiver = hanlder;
        }

        @Override
        public void run() {
            try {
                String sb = csebase.getServiceUrl()+"/"+ ae.getappName();
                URL mUrl = new URL(sb);

                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(false);

                conn.setRequestProperty("Accept", "application/xml");
                conn.setRequestProperty("X-M2M-RI", "12345");
                conn.setRequestProperty("X-M2M-Origin", "Sandoroid");
                conn.setRequestProperty("nmtype", "short");
                conn.connect();

                responseCode = conn.getResponseCode();

                BufferedReader in = null;
                String aei = "";
                if (responseCode == 200) {
                    // Get AEID from Response Data
                    in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    String resp = "";
                    String strLine;
                    while ((strLine = in.readLine()) != null) {
                        resp += strLine;
                    }

                    ParseElementXml pxml = new ParseElementXml();
                    aei = pxml.GetElementXml(resp, "aei");
                    ae.setAEid( aei );
                    //Log.d(TAG, "Retrieve Get AEID[" + aei + "]");
                    in.close();
                }
                if (responseCode != 0) {
                    receiver.getResponseBody( Integer.toString(responseCode) );
                }
                conn.disconnect();
            } catch (Exception exp) {
                LOG.log(Level.SEVERE, exp.getMessage());
            }
        }
    }
    /* Subscribe Co2 Content Resource */
    class SubscribeResource extends Thread {
        private final Logger LOG = Logger.getLogger(SubscribeResource.class.getName());
        private IReceived receiver;
        //        private String container_name = "cnt-co2"; //change to control container name
        private String container_name; //change to control container name

        public ContentSubscribeObject subscribeInstance;
        public SubscribeResource(String containerName) {
            subscribeInstance = new ContentSubscribeObject();
            subscribeInstance.setUrl(csebase.getHost());
            subscribeInstance.setResourceName(ae.getAEid()+"_rn");
            subscribeInstance.setPath(ae.getAEid()+"_sub");
            subscribeInstance.setOrigin_id(ae.getAEid());

            // added by J. Yun, SCH Univ.
            this.container_name = containerName;
        }

        public void setReceiver(IReceived hanlder) { this.receiver = hanlder; }

        @Override
        public void run() {
            try {
                String sb = csebase.getServiceUrl() + "/" + ServiceAEName + "/" + container_name;

                URL mUrl = new URL(sb);

                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setInstanceFollowRedirects(false);

                conn.setRequestProperty("Accept", "application/xml");
                conn.setRequestProperty("Content-Type", "application/vnd.onem2m-res+xml; ty=23");
                conn.setRequestProperty("locale", "ko");
                conn.setRequestProperty("X-M2M-RI", "12345");
                conn.setRequestProperty("X-M2M-Origin", ae.getAEid());

                String reqmqttContent = subscribeInstance.makeXML();
                conn.setRequestProperty("Content-Length", String.valueOf(reqmqttContent.length()));

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                dos.write(reqmqttContent.getBytes());
                dos.flush();
                dos.close();

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String resp = "";
                String strLine="";
                while ((strLine = in.readLine()) != null) {
                    resp += strLine;
                }

                if (resp != "") {
                    receiver.getResponseBody(resp);
                }
                conn.disconnect();

            } catch (Exception exp) {
                LOG.log(Level.SEVERE, exp.getMessage());
            }
        }
    }


    // Generate Push, added by S. Lee, SCH Univ.
    public void sendNotification_1(){
        NotificationCompat.Builder notifyBuilder_1 = getNotificationBuilder_1();
        mNotificationManager.notify(NOTIFICATION_ID_1, notifyBuilder_1.build());
    }
    private NotificationCompat.Builder getNotificationBuilder_1() {
        // Create an Implicit Intent that allows you to start MainActivity when you click Notification.
        Intent notificationIntent = new Intent(this, MainActivity.class);
        // The definition of Pending Intent that covers and delivers Notification.
        PendingIntent notificationPendingIntent = android.app.PendingIntent.getActivity(this,
                NOTIFICATION_ID_1, notificationIntent, android.app.PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notifyBuilder_1 = new NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID_1)
                .setContentTitle("Don't have enough feed")
                .setContentText("Don't have enough feed\n[There's less than two-thirds left.]")
                .setSmallIcon(R.drawable.ic_android)
                .setContentIntent(notificationPendingIntent)
                .setAutoCancel(true)
                ;
        return notifyBuilder_1;
    }

    public void sendNotification_2(){
        NotificationCompat.Builder notifyBuilder = getNotificationBuilder_2();
        mNotificationManager.notify(NOTIFICATION_ID_2, notifyBuilder.build());
    }
    private NotificationCompat.Builder getNotificationBuilder_2() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent notificationPendingIntent = android.app.PendingIntent.getActivity(this,
                NOTIFICATION_ID_2, notificationIntent, android.app.PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notifyBuilder_2 = new NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID_2)
                .setContentTitle("Don't have enough feed")
                .setContentText("You need to replenish the feed.\n[There's less than a third left.]")
                .setSmallIcon(R.drawable.ic_android)
                .setContentIntent(notificationPendingIntent)
                .setAutoCancel(true);
        return notifyBuilder_2;
    }

    public void sendNotification_3(){
        NotificationCompat.Builder notifyBuilder_3 = getNotificationBuilder_3();
        mNotificationManager.notify(NOTIFICATION_ID_3, notifyBuilder_3.build());
    }
    private NotificationCompat.Builder getNotificationBuilder_3() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent notificationPendingIntent = android.app.PendingIntent.getActivity(this,
                NOTIFICATION_ID_3, notificationIntent, android.app.PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notifyBuilder_3 = new NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID_3)
                .setContentTitle("My pet ate.")
                .setContentText("It's great.")
                .setSmallIcon(R.drawable.ic_android)
                .setContentIntent(notificationPendingIntent)
                .setAutoCancel(true);
        return notifyBuilder_3;
    }

    public void sendNotification_4(){
        NotificationCompat.Builder notifyBuilder = getNotificationBuilder_4();
        mNotificationManager.notify(NOTIFICATION_ID_4, notifyBuilder.build());
    }
    private NotificationCompat.Builder getNotificationBuilder_4() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent notificationPendingIntent = android.app.PendingIntent.getActivity(this,
                NOTIFICATION_ID_4, notificationIntent, android.app.PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notifyBuilder_4 = new NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID_4)
                .setContentTitle("My pet has succeeded in training.")
                .setContentText("It's great.")
                .setSmallIcon(R.drawable.ic_android)
                .setContentIntent(notificationPendingIntent)
                .setAutoCancel(true);
        return notifyBuilder_4;
    }

    public void sendNotification_5(){
        NotificationCompat.Builder notifyBuilder = getNotificationBuilder_5();
        mNotificationManager.notify(NOTIFICATION_ID_5, notifyBuilder.build());
    }
    private NotificationCompat.Builder getNotificationBuilder_5() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent notificationPendingIntent = android.app.PendingIntent.getActivity(this,
                NOTIFICATION_ID_5, notificationIntent, android.app.PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notifyBuilder_5 = new NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID_5)
                .setContentTitle("My pet ran away.")
                .setContentText("Please check the video to see if pet's doing well.")
                .setSmallIcon(R.drawable.ic_android)
                .setContentIntent(notificationPendingIntent)
                .setAutoCancel(true);
        return notifyBuilder_5;
    }

    public void sendNotification_6(){
        NotificationCompat.Builder notifyBuilder = getNotificationBuilder_6();
        mNotificationManager.notify(NOTIFICATION_ID_6, notifyBuilder.build());
    }
    private NotificationCompat.Builder getNotificationBuilder_6() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent notificationPendingIntent = android.app.PendingIntent.getActivity(this,
                NOTIFICATION_ID_6, notificationIntent, android.app.PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notifyBuilder_6 = new NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID_6)
                .setContentTitle("My pet failed to train.")
                .setContentTitle("My pet failed to train.")
                .setSmallIcon(R.drawable.ic_android)
                .setContentIntent(notificationPendingIntent)
                .setAutoCancel(true);
        return notifyBuilder_6;
    }

    public void createNotificationChannel() {
        mNotificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            //----for channel_2
            NotificationChannel notificationChannel_1 = new NotificationChannel(PRIMARY_CHANNEL_ID_1,
                    "Don't have enough feed", mNotificationManager.IMPORTANCE_HIGH);
            notificationChannel_1.enableLights(true);
            notificationChannel_1.setLightColor(Color.RED);
            notificationChannel_1.enableVibration(true);
            notificationChannel_1.setDescription("Notification from Mascot_1");
            mNotificationManager.createNotificationChannel(notificationChannel_1);

            //----for channel_2
            NotificationChannel notificationChannel_2 = new NotificationChannel(PRIMARY_CHANNEL_ID_2,
                    "Don't have enough feed!", mNotificationManager.IMPORTANCE_HIGH);
            notificationChannel_2.enableLights(true);
            notificationChannel_2.setLightColor(Color.BLUE);
            notificationChannel_2.enableVibration(true);
            notificationChannel_2.setDescription("Notification from Mascot_2");
            mNotificationManager.createNotificationChannel(notificationChannel_2);

            //----for channel_3
            NotificationChannel notificationChannel_3 = new NotificationChannel(PRIMARY_CHANNEL_ID_3,
                    "My pet ate.", mNotificationManager.IMPORTANCE_HIGH);
            notificationChannel_3.enableLights(true);
            notificationChannel_3.setLightColor(Color.RED);
            notificationChannel_3.enableVibration(true);
            notificationChannel_3.setDescription("Notification from Mascot_3");
            mNotificationManager.createNotificationChannel(notificationChannel_3);

            //----for channel_4
            NotificationChannel notificationChannel_4 = new NotificationChannel(PRIMARY_CHANNEL_ID_4,
                    "My pet has succeeded in training.", mNotificationManager.IMPORTANCE_HIGH);
            notificationChannel_4.enableLights(true);
            notificationChannel_4.setLightColor(Color.BLUE);
            notificationChannel_4.enableVibration(true);
            notificationChannel_4.setDescription("Notification from Mascot_4");
            mNotificationManager.createNotificationChannel(notificationChannel_4);

            //----for channel_5
            NotificationChannel notificationChannel_5 = new NotificationChannel(PRIMARY_CHANNEL_ID_5,
                    "My pet ran away.", mNotificationManager.IMPORTANCE_HIGH);
            notificationChannel_5.enableLights(true);
            notificationChannel_5.setLightColor(Color.BLUE);
            notificationChannel_5.enableVibration(true);
            notificationChannel_5.setDescription("Notification from Mascot_5");
            mNotificationManager.createNotificationChannel(notificationChannel_5);

            //----for channel_6
            NotificationChannel notificationChannel_6 = new NotificationChannel(PRIMARY_CHANNEL_ID_6,
                    "My pet failed to train.", mNotificationManager.IMPORTANCE_HIGH);
            notificationChannel_6.enableLights(true);
            notificationChannel_6.setLightColor(Color.BLUE);
            notificationChannel_6.enableVibration(true);
            notificationChannel_6.setDescription("Notification from Mascot_6");
            mNotificationManager.createNotificationChannel(notificationChannel_6);

        }
    }
}