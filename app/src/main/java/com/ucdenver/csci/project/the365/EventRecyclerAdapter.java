package com.ucdenver.csci.project.the365;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EventRecyclerAdapter extends RecyclerView.Adapter<EventRecyclerAdapter.MyviewHolder>{

    Context context;
    ArrayList<Events> arrayList;
    DataManager dataManager;

    public EventRecyclerAdapter(Context context, ArrayList<Events> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }


    @NonNull
    @Override
    public MyviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

       View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.show_event_row_layout, parent, false);
        return new MyviewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull  MyviewHolder holder, int position) {
    Events events= arrayList.get(position);
    holder.event.setText(events.getEVENT());
    holder.date.setText((events.getDATE()));
    holder.time.setText(events.getTIME());
    holder.delete.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            deleteCalendarEvent(events.getEVENT(),events.getDATE(), events.getTIME());
            arrayList.remove(position);
            notifyDataSetChanged();
        }
    });

    if (isAlarmed(events.getDATE(),events.getEVENT(),events.getTIME())){

        holder.setAlarm.setImageResource(R.drawable.ic_baseline_notifications_active_24);
       // notifyDataSetChanged();
    }else{
        holder.setAlarm.setImageResource(R.drawable.ic_baseline_notifications_off_24);
      //  notifyDataSetChanged();
    }
        Calendar datecalendar=Calendar.getInstance();
        datecalendar.setTime(convertStringToDate(events.getDATE()));
        final int alarmYear=datecalendar.get(Calendar.YEAR);
        final int alarmMonth=datecalendar.get(Calendar.MONTH);
        final int alarmDay=datecalendar.get(Calendar.DAY_OF_MONTH);
        Calendar timecalendar=Calendar.getInstance();
        timecalendar.setTime(convertStringToTime(events.getTIME()));
        final int alarmHour=timecalendar.get(Calendar.HOUR_OF_DAY);
        final int alarmMinute=timecalendar.get(Calendar.MINUTE);



    holder.setAlarm.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(isAlarmed(events.getDATE(),events.getEVENT(),events.getTIME())){
                holder.setAlarm.setImageResource(R.drawable.ic_baseline_notifications_off_24);
                cancelAlarm(getrequestCode(events.getDATE(),events.getEVENT(),events.getTIME()));
                updateEvent(events.getDATE(),events.getEVENT(),events.getTIME(),"off");
                notifyDataSetChanged();
            }else{
                holder.setAlarm.setImageResource(R.drawable.ic_baseline_notifications_active_24);

                Calendar alarCalendar= Calendar.getInstance();
                alarCalendar.set(alarmYear,alarmMonth,alarmDay,alarmHour,alarmMinute);
                setAlarm(alarCalendar,events.getEVENT(), events.TIME, getrequestCode(events.getDATE(), events.getEVENT(),events.getTIME()));
                updateEvent(events.getDATE(),events.getEVENT(),events.getTIME(),"on");
                notifyDataSetChanged();

            }
        }
    });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class MyviewHolder extends RecyclerView.ViewHolder{

        TextView date, event, time;
        Button delete;

        ImageButton setAlarm;

        public MyviewHolder(@NonNull  View itemView) {
            super(itemView);

            date= itemView.findViewById(R.id.eventdate);
            event= itemView.findViewById(R.id.eventname);
            time= itemView.findViewById(R.id.eventime);
            delete=itemView.findViewById(R.id.delete);
            setAlarm=itemView.findViewById(R.id.alarmmeBtn);
        }
    }

    public void setAlarm(Calendar calendar, String event, String time, int requestcode){
        Intent intent=new Intent(context.getApplicationContext(),AlarmReceiver.class);
        intent.putExtra("event",event);
        intent.putExtra("time",time);
        intent.putExtra("id",requestcode);
        PendingIntent pendingIntent= PendingIntent.getBroadcast(context,requestcode,intent,PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager= (AlarmManager) context.getApplicationContext().getSystemService(context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);


    }

    public void cancelAlarm( int requestcode){
        Intent intent=new Intent(context.getApplicationContext(),AlarmReceiver.class);
        intent.putExtra("id",requestcode);
        PendingIntent pendingIntent= PendingIntent.getBroadcast(context,requestcode,intent,PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager= (AlarmManager) context.getApplicationContext().getSystemService(context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);


    }
    public int getrequestCode(String date, String event, String time) {
        int code=0;

        dataManager=new DataManager(context);
        SQLiteDatabase database=dataManager.getReadableDatabase();
        Cursor cursor=dataManager.readIDEvents(date,event,time,database);

        while(cursor.moveToNext()){
            code=cursor.getInt(cursor.getColumnIndex(DBStructure.ID));

        }

        cursor.close();
        dataManager.close();


        return code;
    }

    public void updateEvent(String date, String event, String time,String notify){
        dataManager =new DataManager(context);
        SQLiteDatabase database=dataManager.getWritableDatabase();
        dataManager.updateEvent(date,event,time ,notify,database);
        dataManager.close();
    }

    private void deleteCalendarEvent(String event, String date, String time){
        dataManager =new DataManager(context);
        SQLiteDatabase database=dataManager.getWritableDatabase();
        dataManager.deleteEvent(event,date,time,database);
        dataManager.close();
    }
    private Date convertStringToTime (String eventDate){
        SimpleDateFormat format=new SimpleDateFormat("kk:mm", Locale.ENGLISH);
        Date date=null;

        try{
            date=format.parse(eventDate);

        }catch (ParseException e){
            e.printStackTrace();
        }

        return date;
    }
    private Date convertStringToDate (String eventDate){
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Date date=null;

        try{
            date=format.parse(eventDate);

        }catch (ParseException e){
            e.printStackTrace();
        }

        return date;
    }
    private boolean isAlarmed(String date, String event, String time){

        boolean alarmed=false;
        dataManager =new DataManager(context);
        SQLiteDatabase database=dataManager.getReadableDatabase();
        Cursor cursor=dataManager.readIDEvents(date,event, time, database);

        while(cursor.moveToNext()){
           String notify=cursor.getString(cursor.getColumnIndex(DBStructure.Notify));

           if (notify.equals("on")) {
               alarmed = true;
           }
               else{
                   alarmed=false;
                }


        }

        cursor.close();
        dataManager.close();

        return alarmed;

    }

}
