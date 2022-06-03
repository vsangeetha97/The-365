package com.ucdenver.csci.project.the365;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;


public class The365Dialog extends LinearLayout {
    ImageButton prevButton;
    ImageButton nextButton;
    TextView currentdate;
    GridView gridview;
    Context context;
    MyGridAdapter myGridAdapter;
    AlertDialog alertDialog;

    int alarmYear,alarmMonth,alarmDay,alarmHour, alarmminute;

    EditText eventName;
    TextView eventTime;
    ImageButton setTime;
    Button addEvent;
    CheckBox alarmMe;




    private static final int MAX_CALENDAR_DAYS=42;
    Calendar calendar= Calendar.getInstance(Locale.ENGLISH);
    SimpleDateFormat dateFormat=new SimpleDateFormat("MMMM yyyy",Locale.ENGLISH);
    SimpleDateFormat monthFormat=new SimpleDateFormat("MMMM",Locale.ENGLISH);
    SimpleDateFormat yearFormat=new SimpleDateFormat("yyyy",Locale.ENGLISH);
    SimpleDateFormat eventDateFormat=new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);


    List<Date> dates=new ArrayList<>();
    List<Events> events=new ArrayList<>();





    public The365Dialog(Context context) {
        super(context);

    }

    public The365Dialog(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context=context;

        LayoutInflater inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflator.inflate(R.layout.calendar_layout, this);
        prevButton = view.findViewById(R.id.prevButton);
        nextButton = view.findViewById(R.id.nextButton);
        currentdate= view.findViewById(R.id.currentdateTextView);
        gridview   = view.findViewById(R.id.gridView);
        setupCalendar();

        prevButton.setOnClickListener(v -> {

                calendar.add(Calendar.MONTH, -1);
                setupCalendar();

        });

        nextButton.setOnClickListener(v -> {

                calendar.add(Calendar.MONTH, 1);
                setupCalendar();

        });

         gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view , int position , long id) {
                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setCancelable(true);

                final View addView =LayoutInflater.from(parent.getContext()).inflate(R.layout.add_newevent_layout,null);

                 eventName=addView.findViewById(R.id.eventname);
                 eventTime=addView.findViewById(R.id.eventtime);
                 setTime=addView.findViewById(R.id.seteventtime);
                 addEvent= addView.findViewById(R.id.addevent);
                 alarmMe=addView.findViewById(R.id.alarmme);
                 Calendar dateCalendar= Calendar.getInstance();
                 dateCalendar.setTime(dates.get(position));
                 alarmYear = dateCalendar.get(Calendar.YEAR);
                 alarmMonth=dateCalendar.get(Calendar.MONTH);
                 alarmDay=dateCalendar.get(Calendar.DAY_OF_MONTH);


                setTime.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Calendar calendar= Calendar.getInstance();
                        int hours=calendar.get(Calendar.HOUR_OF_DAY);
                        int minutes = calendar.get(Calendar.MINUTE);
                        TimePickerDialog timePickerDialog=new TimePickerDialog(addView.getContext(), R.style.Theme_AppCompat_Dialog,
                            new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                  Calendar c=Calendar.getInstance();
                                  c.set(Calendar.HOUR_OF_DAY,hourOfDay);
                                  c.set(Calendar.MINUTE, minute);
                                  c.setTimeZone(TimeZone.getDefault());
                                  SimpleDateFormat hformate=new SimpleDateFormat("K:mm a", Locale.ENGLISH);
                                  String EventTime=hformate.format(c.getTime());
                                  eventTime.setText(EventTime);
                                    alarmHour=c.get(Calendar.HOUR_OF_DAY);
                                    alarmminute=c.get(Calendar.MINUTE) ;
                                }


                            },hours,minutes,false);
                    timePickerDialog.show();
                }
            });
            final  String date=eventDateFormat.format(dates.get(position));
            final  String month=monthFormat.format(dates.get(position));
            final  String year=yearFormat.format(dates.get(position));


        addEvent.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String event=eventName.getText().toString();
                String time=eventTime.getText().toString();
                if (alarmMe.isChecked()){
                    SaveEvent(event,time,date,month,year,"on");
                    setupCalendar();
                    Calendar calendar=Calendar.getInstance();
                    calendar.set(alarmYear,alarmMonth,alarmDay,alarmHour, alarmminute);
                    setAlarm(calendar,event, time,getrequestCode(date,event,time));
                    alertDialog.dismiss();
                }else{
                    SaveEvent(event,time,date,month,year,"off");
                    setupCalendar();
                    alertDialog.dismiss();
                }



            }

        });
                builder.setView(addView);
                alertDialog = builder.create();
                alertDialog.show();
        }
         });

         gridview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
             @Override
             public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                 String date=eventDateFormat.format(dates.get(position));

                 AlertDialog.Builder builder=new AlertDialog.Builder(context);
                 builder.setCancelable(true);
                 View showView =LayoutInflater.from(parent.getContext()).inflate(R.layout.show_event_layout,null);

                RecyclerView recyclerView= showView.findViewById(R.id.EventsRV);
                RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(showView.getContext());
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setHasFixedSize(true);
                EventRecyclerAdapter eventRecyclerAdapter=new EventRecyclerAdapter(showView.getContext(),CollecteventsbyDate(date) );
                recyclerView.setAdapter(eventRecyclerAdapter);
                eventRecyclerAdapter.notifyDataSetChanged();

                builder.setView(showView);
                alertDialog= builder.create();
                alertDialog.show();
                alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        setupCalendar();
                    }
                });

                 return true;
             }
         });

    }
    public int getrequestCode(String date, String event, String time) {
        int code=0;

        DataManager db=new DataManager(context);
        SQLiteDatabase database=db.getReadableDatabase();
        Cursor cursor=db.readIDEvents(date,event,time,database);

        while(cursor.moveToNext()){
             code=cursor.getInt(cursor.getColumnIndex(DBStructure.ID));

        }

        cursor.close();
        db.close();


        return code;
    }


    public The365Dialog(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setAlarm(Calendar calendar,String event, String time, int requestcode){
        Intent intent=new Intent(context.getApplicationContext(),AlarmReceiver.class);
        intent.putExtra("event",event);
        intent.putExtra("time",time);
        intent.putExtra("id",requestcode);
        PendingIntent pendingIntent= PendingIntent.getBroadcast(context,requestcode,intent,PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager= (AlarmManager) context.getApplicationContext().getSystemService(context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);


    }

    private ArrayList<Events> CollecteventsbyDate(String date){
        ArrayList<Events> arrayList=new ArrayList<>();
         DataManager db=new DataManager(context);
        SQLiteDatabase database=db.getReadableDatabase();
        Cursor cursor=db.readEvents(date,database);

        while(cursor.moveToNext()){
            String event=cursor.getString(cursor.getColumnIndex(DBStructure.EVENT));
            String times=cursor.getString(cursor.getColumnIndex(DBStructure.TIME));
            String dates=cursor.getString(cursor.getColumnIndex(DBStructure.DATE));
            String months=cursor.getString(cursor.getColumnIndex(DBStructure.MONTH));
            String Years=cursor.getString(cursor.getColumnIndex(DBStructure.YEAR));
            Events eventlist=new Events(event,times,dates,months,Years);
            arrayList.add(eventlist);
        }

        cursor.close();
        db.close();
    return arrayList;
    }

    private void SaveEvent(String event, String time,String date, String month,String year, String notify){
        DataManager db = new DataManager(context);
        SQLiteDatabase database= db.getWritableDatabase();
        db.saveEvents(event,time,date,month,year,notify,database);
        Toast.makeText(context,"Event Saved",Toast.LENGTH_SHORT).show();
    }


    private void setupCalendar(){
        String current_Date=dateFormat.format(calendar.getTime());
        currentdate.setText(current_Date);
        dates.clear();
        Calendar monthCalendar= (Calendar) calendar.clone();
        monthCalendar.set(Calendar.DAY_OF_MONTH,1);
        int firstDayofMonth=monthCalendar.get(Calendar.DAY_OF_WEEK)-1;
        monthCalendar.add(Calendar.DAY_OF_MONTH, -firstDayofMonth);
        String month= monthFormat.format(calendar.getTime());
        String year= yearFormat.format((calendar.getTime()));

       CollectEventsperMonth(month,year);

        while (dates.size() < MAX_CALENDAR_DAYS){
            dates.add(monthCalendar.getTime());
            monthCalendar.add(Calendar.DAY_OF_MONTH,1);
        }

        myGridAdapter =new MyGridAdapter(context,dates,calendar,events);
        gridview.setAdapter(myGridAdapter);
    }

    private void CollectEventsperMonth(String Month,String year){

         events.clear();
        DataManager db=new DataManager(context);
         SQLiteDatabase database=db.getReadableDatabase();

         Cursor cursor=db.readEventspermonth(Month,year,database);

         while(cursor.moveToNext()){
                String event=cursor.getString(cursor.getColumnIndex(DBStructure.EVENT));
                String times=cursor.getString(cursor.getColumnIndex(DBStructure.TIME));
                String dates=cursor.getString(cursor.getColumnIndex(DBStructure.DATE));
                String months=cursor.getString(cursor.getColumnIndex(DBStructure.MONTH));
                String Years=cursor.getString(cursor.getColumnIndex(DBStructure.YEAR));
                Events eventlist=new Events(event,times,dates,months,Years);
                events.add(eventlist);
            }

            cursor.close();
            db.close();


    }


}
