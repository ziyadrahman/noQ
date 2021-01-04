package com.example.e_rationqueue;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String DATE_FORMAT ="dd-MM-yyyy";
    GridView gridViewTimeSlot;
    MaterialButton bookBtn;
    ArrayList<String> availableTimeSlotsMap=new ArrayList<>();
    String todayDate;
    String pickedtimeSlot;
    FirebaseDatabase database;
    TextView messageTextView;
    TextView availableSlotsTextView;
    TextView customerNameText;
    TextView cardNoText;
    TextView cardTypeText;
    String userId;

    String currentWeek;
    String currentMonth;
    int dayOfTheWeek;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        database=FirebaseDatabase.getInstance();
        gridViewTimeSlot=findViewById(R.id.timeSlotGridView);
        availableSlotsTextView=findViewById(R.id.text);
        bookBtn=findViewById(R.id.bookBtn);
        messageTextView=findViewById(R.id.messageTextView);
        customerNameText=findViewById(R.id.customerNameText);
        cardTypeText=findViewById(R.id.cardTypeText);
        cardNoText=findViewById(R.id.cardNoText);
        userId=getCurrentUser();
        addValuesToTimeSlotsMap();

        final Calendar calendar = Calendar.getInstance();

        currentWeek=String.valueOf(calendar.get(Calendar.WEEK_OF_MONTH));
        currentMonth=String.valueOf(calendar.get(Calendar.MONTH));
        dayOfTheWeek=calendar.get(Calendar.DAY_OF_WEEK);

        int day=calendar.get(Calendar.DATE);
        int month=calendar.get(Calendar.MONTH);
        int year=calendar.get(Calendar.YEAR);
        Date date=new GregorianCalendar(year,month,day+1).getTime();

        todayDate=dateToString(date);
        if (dayOfTheWeek!=1) {
            loadCurrentWeekBooked();
        }
        else
        {
            availableSlotsTextView.setVisibility(View.INVISIBLE);
            messageTextView.setText("SUNDAY HOLIDAY");
            messageTextView.setVisibility(View.VISIBLE);
        }
        setData();
    }



    private void addValuesToTimeSlotsMap() {
        availableTimeSlotsMap.add("08:00AM-08:15AM");
        availableTimeSlotsMap.add("08:15AM-08:30AM");
        availableTimeSlotsMap.add("08:30AM-08:45AM");
        availableTimeSlotsMap.add("08:45AM-09:00AM");

        availableTimeSlotsMap.add("09:00AM-09:15AM");
        availableTimeSlotsMap.add("09:15AM-09:30AM");
        availableTimeSlotsMap.add("09:30AM-09:45AM");
        availableTimeSlotsMap.add("09:45AM-10:00AM");

        availableTimeSlotsMap.add("10:00AM-10:15AM");
        availableTimeSlotsMap.add("10:15AM-10:30AM");
        availableTimeSlotsMap.add("10:30AM-10:45AM");
        availableTimeSlotsMap.add("10:45AM-11:00AM");

        availableTimeSlotsMap.add("11:00AM-11:15AM");
        availableTimeSlotsMap.add("11:15AM-11:30AM");
        availableTimeSlotsMap.add("11:30AM-11:45AM");
        availableTimeSlotsMap.add("11:45AM-12:00PM");

        availableTimeSlotsMap.add("04:00PM-04:15PM");
        availableTimeSlotsMap.add("04:15PM-04:30PM");
        availableTimeSlotsMap.add("04:30PM-04:45PM");
        availableTimeSlotsMap.add("04:45PM-05:00PM");

        availableTimeSlotsMap.add("05:00PM-05:15PM");
        availableTimeSlotsMap.add("05:15PM-05:30PM");
        availableTimeSlotsMap.add("05:30PM-05:45PM");
        availableTimeSlotsMap.add("05:45PM-06:00PM");

        availableTimeSlotsMap.add("06:00PM-06:15PM");
        availableTimeSlotsMap.add("06:15PM-06:30PM");
        availableTimeSlotsMap.add("06:30PM-06:45PM");
        availableTimeSlotsMap.add("06:45PM-07:00PM");

        availableTimeSlotsMap.add("07:00PM-07:15PM");
        availableTimeSlotsMap.add("07:15PM-07:30PM");
        availableTimeSlotsMap.add("07:30PM-07:45PM");
        availableTimeSlotsMap.add("07:45PM-08:00PM");




    }
    public String dateToString(Date selectedDate)
    {

        SimpleDateFormat dateFormat=new SimpleDateFormat(DATE_FORMAT, Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String dateToString=dateFormat.format(selectedDate);
        Log.d("todayDate",dateToString);
        return dateToString;
    }
    public void loadCurrentWeekBooked(){
        DatabaseReference todayBooked=database.getReference("currentWeekBooked");

        todayBooked.child(currentMonth).child(currentWeek).child(userId).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue()==null){
                            loadBookedTimeSlotsDb();
                        }
                        else
                        {
                            availableSlotsTextView.setVisibility(View.INVISIBLE);
                            messageTextView.setText("Time Slot Booked At:"+
                                    snapshot.getValue());
                            messageTextView.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        loadBookedTimeSlotsDb();

                    }
                });

    }
    public void loadBookedTimeSlotsDb() {


        DatabaseReference databaseBookedTimeSlots=database.getReference("bookedTimeSlot");



//        databaseBookedTimeSlots.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
//
//                Log.d("timeslot","onchildadded() runned");
////                for (DataSnapshot ds : dataSnapshot.getChildren()) {
//
//                    try {
//                        Log.d("timeslot","for loop runned");
//                        String timeSlotkey = dataSnapshot.getKey();
//                        Log.d("timeslot",timeSlotkey);
//                        availableTimeSlotsMap.remove(timeSlotkey);
//
//                    } catch (NullPointerException e) {
//                        Toast.makeText(MainActivity.this, "Suggestions blocked ", Toast.LENGTH_SHORT).show();
//                    }
//
////                }
//                loadAvailableTimeSlots();
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                loadAvailableTimeSlots();
//
//            }
//        });
        databaseBookedTimeSlots.child(todayDate).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren())
                {
                    String bookedTime=ds.getKey();
                    Log.d("bookedTime",bookedTime);
                    availableTimeSlotsMap.remove(bookedTime);
                }
               loadAvailableTimeSlots();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadAvailableTimeSlots() {

        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,availableTimeSlotsMap);
        gridViewTimeSlot.setAdapter(arrayAdapter);
        timeSlotListner();


    }
    private void timeSlotListner()
    {
        gridViewTimeSlot.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               pickedtimeSlot=parent.getItemAtPosition(position).toString();
                bookBtn.setText("TAKE TIME SLOT AT: "+pickedtimeSlot);
                showBookBtn();
                bookBtnListner();

            }
        });
    }
    private void showBookBtn()
    {
        bookBtn.setVisibility(View.VISIBLE);
    }
    private void goneBookBtn()
    {
        bookBtn.setVisibility(View.GONE);
    }
    private void bookBtnListner()
    {
        bookBtn.setOnClickListener(v -> {
            addBookedTimeSlot();
            currentWeekBooked();
            goneBookBtn();
            availableSlotsTextView.setVisibility(View.INVISIBLE);
            messageTextView.setText("Time slot booked at:"+pickedtimeSlot);
            messageTextView.setVisibility(View.VISIBLE);
            gridViewTimeSlot.setVisibility(View.GONE);


        });
    }

    private void currentWeekBooked() {
        DatabaseReference todayBooked=database.getReference("currentWeekBooked");
        todayBooked.child(currentMonth).child(currentWeek).child(userId).setValue(pickedtimeSlot);
    }

    private void addBookedTimeSlot() {
        DatabaseReference databaseBookedTimeSlots=database.getReference("bookedTimeSlot");
        databaseBookedTimeSlots.child(todayDate).child(pickedtimeSlot).setValue(userId);
    }
    private void setData()
    {
        DatabaseReference users=FirebaseDatabase.getInstance().getReference("users");
        users.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String customerName=snapshot.child("customerName").getValue().toString();
                customerNameText.setText(customerName);
                String cardNo=snapshot.child("cardNo").getValue().toString();
                cardNoText.setText(cardNo);
                String cardType=snapshot.child("cardType").getValue().toString();
                cardTypeText.setText(cardType.toUpperCase());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private String getCurrentUser()
    {
        FirebaseAuth mAuth;
        mAuth=FirebaseAuth.getInstance();

        return   Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
    }

}