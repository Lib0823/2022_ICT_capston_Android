package com.example.project2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BattleActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance(); // 파이어베이스 데이터베이스 연동
    private FirebaseDatabase mFirebaseDB;
    private DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser(); // 방금 로그인 성공한 유저의 정보를 가져오는 객체
    private String userId;
    private DatabaseReference mDatabaseRef1 = FirebaseDatabase.getInstance().getReference();      // 파이어베이스 DB에 저장시킬 상위 주소위치
    private LinearLayout llBattle, llresultlayout;
    private int run = 0;

    //ListView list;
    ArrayList data;
    ArrayAdapter adapter;
    EditText battleId;
    Button battleRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);

        final UserAccount[] userInfo = {new UserAccount()};
        llBattle = findViewById(R.id.ll_battlelayout);
        llresultlayout = findViewById(R.id.ll_resultlayout);
        //데이터 읽기
        mDatabaseRef.child("project").child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onCancelled(@NonNull DatabaseError error) { //참조에 액세스 할 수 없을 때 호출
                run = 0;
            }

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userInfo[0] = snapshot.getValue(UserAccount.class);
                if(userInfo[0] == null ||  userInfo[0].equals(null))
                    run = 0;
                else {
                    run = userInfo[0].getRun();
                    if(run == 1) {
                        llBattle.setVisibility(View.GONE);
                        llresultlayout.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        // 리스트 생성
        //list = findViewById(R.id.battleList);
        data = new ArrayList<>();
        //adapter = new ArrayAdapter<>
                //(this, android.R.layout.simple_list_item_1, data);
        //list.setAdapter(adapter);
        //list.setSelection(adapter.getCount() - 1);

        //data.add("leeSoo님이 대결을 신청하였습니다!");
        //adapter.notifyDataSetChanged();
        //list.setSelection(adapter.getCount() - 1);

        // Spinner
        Spinner battleSpinner = (Spinner)findViewById(R.id.battleSpinner);
        ArrayAdapter battleAdapter = ArrayAdapter.createFromResource(this,
                R.array.battleDay, android.R.layout.simple_spinner_item);
        battleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        battleSpinner.setAdapter(battleAdapter);

        String matchDay = battleSpinner.getSelectedItem().toString();
        battleId = findViewById(R.id.battleId);

        // 현재 날짜 가져오기
        long now = System.currentTimeMillis();
        Date date1 = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd");
        String date = sdf.format(date1);

        String str[] = matchDay.split("일");
        String res = "";
        for(int i=0 ; i<str.length ; i++)
        {
            res += str[i];
        }
        Log.d("일수 가져오기", res);
        Calendar cal = Calendar.getInstance();	 //날짜 계산을 위해 Calendar 추상클래스 선언 getInstance()메소드 사용
        cal.setTime(date1);
        cal.add(Calendar.DATE, Integer.parseInt(res));
        String date5 = sdf.format(cal.getTime());
        // 신청하기 버튼 누를 시,
        battleRequest = findViewById(R.id.battleRequest);
        String finalRes = res;
        final String[] opUserToken = new String[1];
        battleRequest.setOnClickListener(new View.OnClickListener() {
            private ArrayList<UserAccount> arrayList = new ArrayList<>();
            @Override
            public void onClick(View v) {
                String opponent = battleId.getText().toString();    // 상대방 신청 id
                if(!opponent.equals("")) {
                    mDatabaseRef1 = mFirebaseDB.getInstance().getReference().child("project").getRef();
                    mDatabaseRef1.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            // 파이어베이스 데이터베이스 데이터를 받아오는 곳
                            arrayList.clear();
                            String compare;
                            int index = 0;
                            int in1 = 0;
                            for(DataSnapshot ss : snapshot.getChildren()){
                                UserAccount userAccount = ss.getValue(UserAccount.class);
                                compare = userAccount.getId();
                                if(compare.contains(opponent)) {
                                    index = 1;
                                    opUserToken[0] = userAccount.getIdToken();  // 상대방 아이디 토큰 가져오기
                                    final UserAccount[] userInfo = {new UserAccount()};
                                    //데이터 읽기
                                    mDatabaseRef.child("project").child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) { //참조에 액세스 할 수 없을 때 호출
                                            Toast toast = Toast.makeText(BattleActivity.this, "회원님의 정보를 가져오지 못했습니다.", Toast.LENGTH_SHORT);
                                            toast.show();
                                        }

                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            userInfo[0] = snapshot.getValue(UserAccount.class);
                                            if(userInfo[0] == null || userInfo[0].getId() == null || userInfo[0].getId().length() == 0 || userInfo[0].equals(null)) {
                                                Toast toast = Toast.makeText(BattleActivity.this, "회원님의 정보를 가져오지 못했습니다.", Toast.LENGTH_SHORT);
                                                toast.show();
                                            }
                                            else {
                                                Toast toast = Toast.makeText(BattleActivity.this, "상대방의 정보를 찾았습니다.", Toast.LENGTH_LONG);
                                                toast.show();
                                                userId = userInfo[0].getId();
                                                BattleInfo battleInfo = new BattleInfo();
                                                battleInfo.setUserId(userId);
                                                Log.d("유저 아이디 정보", userId);
                                                battleInfo.setMatchDay(date5);
                                                Log.d("매치일 정보", date5);
                                                battleInfo.setOpid(opponent);
                                                Log.d("상대방 정보", opponent);
                                                battleInfo.setStartDay(date);
                                                Log.d("시작날짜 정보", date);
                                                battleInfo.setOpToken(opUserToken[0]);
                                                // setValue : DB 하위주소(UserAccount)에 정보를 삽입함. (2022-10-21 이수)
                                                mDatabaseRef.child("battle").child(firebaseUser.getUid()).setValue(battleInfo);
                                                Map<String, Object> taskMap1 = new HashMap<String, Object>();
                                                taskMap1.put("run", 1);
                                                mDatabaseRef.child("project").child(firebaseUser.getUid()).updateChildren(taskMap1);


                                                BattleInfo battleInfo1 = new BattleInfo();
                                                battleInfo1.setUserId(opponent);
                                                //battleInfo1.setBattleId(userId);
                                                battleInfo1.setMatchDay(date5);
                                                battleInfo1.setStartDay(date);
                                                battleInfo1.setOpid(userId);
                                                battleInfo1.setOpToken(firebaseUser.getUid());
                                                // setValue : DB 하위주소(UserAccount)에 정보를 삽입함. (2022-10-21 이수)
                                                mDatabaseRef.child("battle").child(opUserToken[0]).setValue(battleInfo1);

                                                Map<String, Object> taskMap2 = new HashMap<String, Object>();
                                                taskMap2.put("run", 1);
                                                mDatabaseRef.child("project").child(opUserToken[0]).updateChildren(taskMap2);
                                                Toast toast1 = Toast.makeText(BattleActivity.this, "대결이 시작되었습니다.", Toast.LENGTH_LONG);
                                                toast1.show();
                                                llBattle.setVisibility(View.GONE);
                                                llresultlayout.setVisibility(View.VISIBLE);
                                                return;
                                            }
                                        }
                                    });

                                } else {
                                    in1 += 1;
                                    if(in1 == 1) {
                                        Toast toast = Toast.makeText(BattleActivity.this, "상대방을 찾고 있습니다.", Toast.LENGTH_LONG);
                                        toast.show();
                                    }
                                }
                                if(index == 0) {
                                    Toast toast = Toast.makeText(BattleActivity.this, "상대방을 찾지 못했습니다.", Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            }
                        }
                        // DB 에러처리
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast toast = Toast.makeText(BattleActivity.this, "상대방 ID를 찾을 수가 없습니다.", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });
                } else {
                    Toast toast = Toast.makeText(BattleActivity.this, "상대방 ID를 입력해주세요.", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        TextView tv_resultStart = findViewById(R.id.tv_resultStart);
        TextView tv_resultEnd = findViewById(R.id.tv_resultEnd);
        TextView tv_myId = findViewById(R.id.tv_myId);
        TextView tv_resultId = findViewById(R.id.tv_resultId);
        final String[] optoken = new String[1];
        final BattleInfo[] battleInfos = {new BattleInfo()};
        //데이터 읽기
        mDatabaseRef.child("battle").child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onCancelled(@NonNull DatabaseError error) { //참조에 액세스 할 수 없을 때 호출
                tv_resultStart.setText("에러처리");
                tv_resultEnd.setText("에러처리");
                tv_myId.setText("에러처리");
                tv_resultId.setText("에러처리");
                Log.d("여기1", "여기1");
            }

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                battleInfos[0] = snapshot.getValue(BattleInfo.class);
                if (battleInfos[0] == null || battleInfos[0].equals(null)) {
                    tv_resultStart.setText("에러처리");
                    tv_resultEnd.setText("에러처리");
                    tv_myId.setText("에러처리");
                    tv_resultId.setText("에러처리");
                    Log.d("여기2", "여기2");
                } else {
                    tv_resultStart.setText(battleInfos[0].getStartDay());
                    tv_resultEnd.setText(battleInfos[0].getMatchDay());
                    tv_myId.setText(battleInfos[0].getUserId());
                    tv_resultId.setText(battleInfos[0].getOpid());
                    optoken[0] = battleInfos[0].getOpToken();

                    // 밀리세컨드 단위로 차이 결과 도출
                    long resultTime;
                    int resultDay;
                    SimpleDateFormat format = new SimpleDateFormat("yyy-mm-dd");
                    Date startDate = null;
                    Log.d("여기도 좀 지나가줘 ㅠㅠ", "");
                    try {
                        startDate = format.parse(tv_resultStart.getText().toString());
                        Date endDate = format.parse(tv_resultEnd.getText().toString());
                        resultTime = endDate.getTime() - startDate.getTime();
                        resultDay = (int) (resultTime / (24 * 60 * 60 * 1000));
                        Log.d("여기도 좀 지나가줘1222222 ㅠㅠ", String.valueOf(resultDay));
                        int sumpoint = 0;
                        // 내점수
                        for(int i = 0; i <= resultDay; i++) {
                            Log.d("여기도 좀 지나가줘3333333 ㅠㅠ", String.valueOf(resultDay));
                            final PointInfo[] pointInfos = {new PointInfo()};
                            final int[] point = {0};
                            SimpleDateFormat fm = new SimpleDateFormat("yyy-mm-dd");

                            Calendar cal = Calendar.getInstance();	 //날짜 계산을 위해 Calendar 추상클래스 선언 getInstance()메소드 사용
                            cal.setTime(startDate);
                            cal.add(Calendar.DATE, i);
                            Log.d("시작일정", String.valueOf(startDate));
                            String date = fm.format(cal.getTime());
                            //데이터 읽기
                            int finalI = i;
                            Log.d("토큰1",firebaseUser.getUid());
                            Log.d("일정1",date);
                            mDatabaseRef.child("point").child(firebaseUser.getUid()).child(date).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) { //참조에 액세스 할 수 없을 때 호출
                                    point[0] = 0;
                                    Log.d("여기도1","");
                                }

                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    pointInfos[0] = snapshot.getValue(PointInfo.class);
                                    if(pointInfos[0] == null || pointInfos[0].equals(null)) {
                                        point[0] = 0;
                                        Log.d("여기도2", "");
                                    }
                                    else {
                                        point[0] += pointInfos[0].getPoint();
                                        Log.d("내 점수", String.valueOf(point[0]));
                                        // 그래프!X -> 프로그레스
                                        ProgressBar myPoint = findViewById(R.id.myPointBar);
                                        myPoint.setProgress(point[0]);

                                    }
                                }
                            });

                            Log.d("그래프1 - " , String.valueOf(sumpoint));
                        }
                        
                        // 상대점수
                        for(int i = 0; i <= resultDay; i++) {
                            final PointInfo[] pointInfos = {new PointInfo()};
                            final int[] point = {0};
                            SimpleDateFormat fm = new SimpleDateFormat("yyyy-mm-dd");

                            Calendar cal = Calendar.getInstance();	 //날짜 계산을 위해 Calendar 추상클래스 선언 getInstance()메소드 사용
                            cal.setTime(startDate);
                            cal.add(Calendar.DATE, i);
                            String date = fm.format(cal.getTime());
                            //데이터 읽기
                            int finalI = i;
                            Log.d("토큰2",optoken[0]);
                            Log.d("일정2",date);
                            mDatabaseRef.child("point").child(optoken[0]).child(date).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) { //참조에 액세스 할 수 없을 때 호출
                                    point[0] = 0;
                                    Log.d("여기도11","");
                                }

                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    pointInfos[0] = snapshot.getValue(PointInfo.class);
                                    if(pointInfos[0] == null || pointInfos[0].equals(null)) {
                                        point[0] = 0;
                                        Log.d("여기도22","");
                                    }
                                    else {
                                        point[0] += pointInfos[0].getPoint();
                                        Log.d("그래프2 - " + finalI, String.valueOf(point[0]));

                                        // 그래프!X -> 프로그레스
                                        ProgressBar opPoint = findViewById(R.id.opPointBar);
                                        opPoint.setProgress(point[0]);
                                    }
                                }
                            });


                        }
                        
                    } catch (ParseException e) {
                        e.printStackTrace();
                        resultDay = 0;
                    }

                    /*
                    try {
                        Log.d("여기도 좀 지나가줘 ㅠㅠ", "");
                        // 초기화
                        BarChart barChart1 = findViewById(R.id.tab1_chart_1);
                        BarDataSet  barDataSet1 = new BarDataSet(data1(resultDay, tv_resultStart.getText().toString()), "Data1");
                        barDataSet1.setColor(Color.RED);
                        // 바 데이터 생성
                        BarData barData1 = new BarData();
                        //바 데이터에 데이터셋 추가
                        barData1.addDataSet(barDataSet1);
                        barChart1.setData(barData1);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }


                    try {
                    // 초기화
                    BarChart barChart2 = findViewById(R.id.tab1_chart_2);
                    // 데이터 생성
                    BarDataSet barDataSet2 = new BarDataSet(data2(resultDay, tv_resultStart.getText().toString(), opUserToken[0]), "Data2");
                        barDataSet2.setColor(Color.BLUE);
                        // 바 데이터 생성
                        BarData barData2 = new BarData();
                        //바 데이터에 데이터셋 추가
                        barData2.addDataSet(barDataSet2);
                        barChart2.setData(barData2);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    */
                }
            }
        });
    }

    private ArrayList<BarEntry> data1(int resultDay, String startDay) throws ParseException {
        ArrayList<BarEntry> dataList = new ArrayList<>();
        for(int i = 0; i < resultDay; i++) {
            final PointInfo[] pointInfos = {new PointInfo()};
            final int[] point = {0};
            SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd");
            Date to = fm.parse(startDay);

            Calendar cal = Calendar.getInstance();	 //날짜 계산을 위해 Calendar 추상클래스 선언 getInstance()메소드 사용
            cal.setTime(to);
            cal.add(Calendar.DATE, i);
            String date = fm.format(cal.getTime());
            //데이터 읽기
            int finalI = i;
            mDatabaseRef.child("point").child(firebaseUser.getUid()).child(date).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onCancelled(@NonNull DatabaseError error) { //참조에 액세스 할 수 없을 때 호출
                    point[0] = 0;
                }

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    pointInfos[0] = snapshot.getValue(PointInfo.class);
                    if(pointInfos[0] == null || pointInfos[0].equals(null))
                        point[0] = 0;
                    else {
                        point[0] = pointInfos[0].getPoint();
                        Log.d("그래프1 - " + finalI, String.valueOf(point[0]));
                        dataList.add(new BarEntry(finalI, point[0]));
                    }
                }
            });

        }
        return dataList;
    }

    private ArrayList<BarEntry> data2(int resultDay, String startDay, String opid) throws ParseException {
        ArrayList<BarEntry> dataList = new ArrayList<>();

        // 상대방에 아이디를 통해서 상대방 토큰을 가져온다.


        for(int i = 0; i < resultDay; i++) {
            final PointInfo[] pointInfos = {new PointInfo()};
            final int[] point = {0};
            SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd");
            Date to = fm.parse(startDay);

            Calendar cal = Calendar.getInstance();	 //날짜 계산을 위해 Calendar 추상클래스 선언 getInstance()메소드 사용
            cal.setTime(to);
            cal.add(Calendar.DATE, i);
            String date = fm.format(cal.getTime());
            //데이터 읽기
            int finalI = i;
            mDatabaseRef.child("point").child(opid).child(date).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onCancelled(@NonNull DatabaseError error) { //참조에 액세스 할 수 없을 때 호출
                    point[0] = 0;
                }

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    pointInfos[0] = snapshot.getValue(PointInfo.class);
                    if(pointInfos[0] == null || pointInfos[0].equals(null))
                        point[0] = 0;
                    else {
                        point[0] = pointInfos[0].getPoint();
                        Log.d("그래프2 - " + finalI, String.valueOf(point[0]));
                        dataList.add(new BarEntry(finalI, point[0]));
                    }
                }
            });

        }
        return dataList;
    }

    // 이름 변경을 위한 메소드
    private void read() {

        final UserAccount[] userInfo = {new UserAccount()};
        //데이터 읽기
        mDatabaseRef.child("project").child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onCancelled(@NonNull DatabaseError error) { //참조에 액세스 할 수 없을 때 호출
                userId = "아이디 에러";
            }

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userInfo[0] = snapshot.getValue(UserAccount.class);
                if(userInfo[0] == null || userInfo[0].getId() == null || userInfo[0].getId().length() == 0 || userInfo[0].equals(null))
                    userId = "아이디 에러";
                else
                    userId = userInfo[0].getId();
            }
        });
        /*if(userInfo[0].getName() == null || userInfo[0].getName().length() == 0)
            welcome.setText("회원정보를 불러오지 못했습니다.");
        else if (userInfo[0].getDogName().equals(""))
            tvDogName.setText(userInfo[0].getDogName());*/
    }
}