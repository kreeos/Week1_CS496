package com.example.week1;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;

public class translator extends Fragment implements TextToSpeech.OnInitListener{


    //번역기 입력박스, 출력박스, 번역기타이틀, 음성인식텍스트뷰
    EditText etSource;
    TextView tvResult, title, voiceTextView;
    /// 인텐트, SpeechRecognizer, 텍스트뷰 삽입
    Intent i;
    SpeechRecognizer mRecognizer;
    String item;
    /// 음성언어/받는언어/리턴언어
    String voiceLang = "ko-KR";
    String sourceLang = "ko";
    String targetLang = "en";
    private TextToSpeech tts;
    int language;

    String name;

    //언어선택버튼 , 번역실행버튼
    Button  test_button, btTranslate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.tab3_translator, container, false);

        title = (TextView) rootView.findViewById(R.id.title_translator);
        etSource = (EditText) rootView.findViewById(R.id.et_source);
        tvResult = (TextView) rootView.findViewById(R.id.tv_result);
        btTranslate = (Button) rootView.findViewById(R.id.bt_translate);
        test_button = (Button) rootView.findViewById(R.id.title_translator);

        //Speech-To-Text, Text-To-Speech 버튼
        FloatingActionButton button_speak = rootView.findViewById(R.id.text_to_speach);
        FloatingActionButton button_record = rootView.findViewById(R.id.voice_recognition);

        //각 FAB버튼에 마이크, 스피커 아이콘 넣어주기
        button_speak.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.speaker));
        button_record.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.microphone));

        //인텐드 for 음성인식
        i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getActivity().getPackageName());

        //음성인식
        tts = new TextToSpeech(getActivity(), this);

        //이하 추가 코드
        voiceTextView = (TextView) rootView.findViewById(R.id.et_source);

        mRecognizer = SpeechRecognizer.createSpeechRecognizer(getActivity());
        mRecognizer.setRecognitionListener(listener);

        button_speak.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                speakOutNow();
            }
        });




        //음성인식버튼 리스너

        button_record.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, voiceLang);
                mRecognizer.startListening(i);
            }
        });


//번역 실행버튼 클릭이벤트
        btTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//소스에 입력된 내용이 있는지 체크하고 넘어가자.
                if (etSource.getText().toString().length() == 0) {
                    Toast.makeText(getActivity(), "번역할 내용을 입력하세요.", Toast.LENGTH_SHORT).show();
                    etSource.requestFocus();
                    return;
                }
                //실행버튼을 클릭하면 AsyncTask를 이용 요청하고 결과를 반환받아서 화면에 표시하도록 해보자.
                NaverTranslateTask asyncTask = new NaverTranslateTask();
                String sText = etSource.getText().toString();
                asyncTask.execute(sText);
            }
        });

        //언어선택버튼 클릭이벤트
        test_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String[] items = new String[]{"한국어 → 영어", "영어 → 한국어", "한국어 → 일본어", "일본어 → 한국어", "한국어 → 중국어", "중국어 → 한국어"};
                final int[] selectedIndex = {0};


                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog .setTitle("번역 언어를 선택하세요")
                        .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                selectedIndex[0] = which;
                                //선택 시 스트링 내의 선택사항의 포지션을 지정
                            }
                        })

                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(selectedIndex[0] == 0){
                                    setKoEn();
                                }
                                if(selectedIndex[0] == 1){
                                    setEnKo();
                                }
                                if(selectedIndex[0] == 2){
                                    setKoJp();
                                }
                                if(selectedIndex[0] == 3){
                                    setJpKo();
                                }
                                if(selectedIndex[0] == 4){
                                    setKoCn();
                                }
                                if(selectedIndex[0] == 5){
                                    setCnKo();

                                }
                            }

                        }).create().show();

            }
        });

        return rootView;
    }




    //fragment static method의 객체 생성용 instance
    public static translator newInstance() {
        Bundle args = new Bundle();

        translator fragment = new translator();
        fragment.setArguments(args);
        return fragment;
    }


    public void setKoEn(){
        sourceLang = "ko";
        targetLang = "en";
        voiceLang = "ko-KR";
        etSource.setText("");
        tvResult.setText("");
        language = tts.setLanguage(Locale.ENGLISH);
        test_button.setText("한국어 → 영어");
    }

    public void setEnKo(){
        sourceLang = "en";
        targetLang = "ko";
        voiceLang = "en-US";
        etSource.setText("");
        tvResult.setText("");
        language = tts.setLanguage(Locale.KOREAN);
        test_button.setText("영어 → 한국어");
    }

    public void setKoJp(){
        sourceLang = "ko";
        targetLang = "ja";
        voiceLang = "ko-KR";
        etSource.setText("");
        tvResult.setText("");
        language = tts.setLanguage(Locale.JAPANESE);
        title.setText("한국어 → 일본어");
    }

    public void setJpKo(){
            sourceLang = "ja";
            targetLang = "ko";
            voiceLang = "ja-JP";
            etSource.setText("");
            tvResult.setText("");
            language = tts.setLanguage(Locale.KOREAN);
            title.setText("일본어 → 한국어");
    }

    public void setKoCn(){
        sourceLang = "ko";
        targetLang = "zh-CN";
        voiceLang = "ko-KR";
        etSource.setText("");
        tvResult.setText("");
        language = tts.setLanguage(Locale.CHINESE);
        title.setText("한국어 → 중국어");
    }

    public void setCnKo(){
        sourceLang = "zh-CN";
        targetLang = "ko";
        voiceLang = "cmn-Hans-CN";
        etSource.setText("");
        tvResult.setText("");
        language = tts.setLanguage(Locale.KOREAN);
        title.setText("중국어 → 한국어");
    }

    public class NaverTranslateTask extends AsyncTask<String, Void, String> {

            public String resultText;
            //Naver
            String clientId = "FW1xTS6RYZbQtuQ5NAnJ";//애플리케이션 클라이언트 아이디값";
            String clientSecret = "D1261Fyhww";//애플리케이션 클라이언트 시크릿값";
            //언어선택도 나중에 사용자가 선택할 수 있게 옵션 처리해 주면 된다.


            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            //AsyncTask 메인처리
            @Override
            protected String doInBackground(String... strings) {
//네이버제공 예제 복사해 넣자.
//Log.d("AsyncTask:", "1.Background");

                String sourceText = strings[0];

                try {
//String text = URLEncoder.encode("만나서 반갑습니다.", "UTF-8");
                    String text = URLEncoder.encode(sourceText, "UTF-8");
                    String apiURL = "https://openapi.naver.com/v1/language/translate";
                    URL url = new URL(apiURL);
                    HttpURLConnection con = (HttpURLConnection)url.openConnection();
                    con.setRequestMethod("POST");
                    con.setRequestProperty("X-Naver-Client-Id", clientId);
                    con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
// post request
                    String postParams = "source="+sourceLang+"&target="+targetLang+"&text=" + text;
                    con.setDoOutput(true);
                    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                    wr.writeBytes(postParams);
                    wr.flush();
                    wr.close();
                    int responseCode = con.getResponseCode();
                    BufferedReader br;
                    if(responseCode==200) { // 정상 호출
                        br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    } else { // 에러 발생
                        br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                    }
                    String inputLine;
                    StringBuffer response = new StringBuffer();
                    while ((inputLine = br.readLine()) != null) {
                        response.append(inputLine);
                    }
                    br.close();
//System.out.println(response.toString());
                    return response.toString();

                } catch (Exception e) {
//System.out.println(e);
                    Log.d("error", e.getMessage());
                    return null;
                }
            }

            //번역된 결과를 받아서 처리
            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
//최종 결과 처리부
//Log.d("background result", s.toString()); //네이버에 보내주는 응답결과가 JSON 데이터이다.

//JSON데이터를 자바객체로 변환해야 한다.
//Gson을 사용할 것이다.

                Gson gson = new GsonBuilder().create();
                JsonParser parser = new JsonParser();
                JsonElement rootObj = parser.parse(s.toString())
//원하는 데이터 까지 찾아 들어간다.
                        .getAsJsonObject().get("message")
                        .getAsJsonObject().get("result");
//안드로이드 객체에 담기
                TranslatedItem items = gson.fromJson(rootObj.toString(), TranslatedItem.class);
//Log.d("result", items.getTranslatedText());
//번역결과를 텍스트뷰에 넣는다.
                tvResult.setText(items.getTranslatedText());
            }



            //자바용 그릇
            private class TranslatedItem {
                String translatedText;

                public String getTranslatedText() {
                    return translatedText;
                }
            }
        }
//음성인식프로그램 시작

//voice listener
    private RecognitionListener listener = new RecognitionListener() {

        @Override
        public void onRmsChanged(float rmsdB) {
            // TODO Auto-generated method stub

        }

        //인식 후 string으로 바꿔 textview에 담기
        @Override public void onResults(Bundle results) {
            ArrayList<String> mResult = (ArrayList<String>) results.get(SpeechRecognizer.RESULTS_RECOGNITION);
            String[] rs = new String[mResult.size()];
            mResult.toArray(rs);
            voiceTextView.append(" "+rs[0]);
        }

        //준비되면 toast로 메세지띄우기
        @Override
        public void onReadyForSpeech(Bundle params) {
            Toast.makeText(getActivity(), "음성인식 시작", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            // TODO Auto-generated method stub

        }

        //에러발생시 에러코드와 함께 에러를 토스트로 띄움 + 로그에 기록
        @Override
        public void onError(int error) {

            Toast.makeText(getActivity(), "오류 발생: "+error, Toast.LENGTH_LONG).show();
            Log.d("error", "Error has occured : "+error);
        }

        //종료메시지 toast로 띄우기
        @Override
        public void onEndOfSpeech() {
            Toast.makeText(getActivity(), "음성인식 종료", Toast.LENGTH_LONG).show();

        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onBeginningOfSpeech() {
            // TODO Auto-generated method stub

        }
    };

    //TTS functions
    //앱종료시 tts를 같이 종료해 준다.
    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    //TTS 구현 기능
    @Override
    public void onInit(int status) {
        language= tts.setLanguage(Locale.ENGLISH);
        if (status == TextToSpeech.SUCCESS) {
            if (language == TextToSpeech.LANG_MISSING_DATA || language == TextToSpeech.LANG_NOT_SUPPORTED) {

                Toast.makeText(getActivity(), "지원하지 않는 언어입니다.", Toast.LENGTH_SHORT).show();
            } else {


                speakOutNow();
            }
        } else {
            Toast.makeText(getActivity(), "TTS 실패!", Toast.LENGTH_SHORT).show();
        }
    }

    //Speak out...
    private void speakOutNow() {
        String text = tvResult.getText().toString();
        //tts.setPitch((float) 0.1); //음량
        //tts.setSpeechRate((float) 0.5); //재생속도
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }


}









