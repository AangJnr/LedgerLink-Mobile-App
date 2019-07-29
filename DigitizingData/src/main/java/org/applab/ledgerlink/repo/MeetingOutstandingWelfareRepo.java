package org.applab.ledgerlink.repo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.applab.ledgerlink.datatransformation.OutstandingWelfareDataTransferRecord;
import org.applab.ledgerlink.domain.model.MeetingOutstandingWelfare;
import org.applab.ledgerlink.domain.schema.MeetingSchema;
import org.applab.ledgerlink.domain.schema.OutstandingWelfareSchema;
import org.applab.ledgerlink.helpers.DatabaseHandler;
import org.applab.ledgerlink.helpers.Utils;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by JCapito on 11/5/2018.
 */

public class MeetingOutstandingWelfareRepo {

    private Context context;
    private int outstandingWelfareId;
    private MeetingOutstandingWelfare meetingOutstandingWelfare;

    public MeetingOutstandingWelfareRepo(Context context) {
        this.context = context;
        this.meetingOutstandingWelfare = null;
    }

    public MeetingOutstandingWelfareRepo(Context context, int outstandingWelfareId) {
        this.context = context;
        this.outstandingWelfareId = outstandingWelfareId;
        this.__load();
    }

    protected void __load() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sql = String.format("SELECT * FROM %s WHERE %s=%d", OutstandingWelfareSchema.TBL_OUTSTANDING_WELFARE, OutstandingWelfareSchema.COL_OW_ID, this.outstandingWelfareId);
            cursor = db.rawQuery(sql, null);
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    this.meetingOutstandingWelfare = new MeetingOutstandingWelfare();
                    this.meetingOutstandingWelfare.setOutstandingWelfareId(cursor.getInt(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_ID)));
                    this.meetingOutstandingWelfare.setExpectedDate(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_EXPECTED_DATE))));
                    this.meetingOutstandingWelfare.setAmount(cursor.getDouble(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_AMOUNT)));
                    this.meetingOutstandingWelfare.setIsCleared(cursor.getInt(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_IS_CLEARED)));
                    this.meetingOutstandingWelfare.setDateCleared(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_DATE_CLEARED))));
                    this.meetingOutstandingWelfare.setComment(cursor.getString(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_COMMENT)));

                    MeetingRepo meetingRepo = new MeetingRepo(this.context, cursor.getInt(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_MEETING_ID)));
                    this.meetingOutstandingWelfare.setMeeting(meetingRepo.getMeeting());

                    MeetingRepo paidInMeetingRepo = new MeetingRepo(this.context, cursor.getInt(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_PAID_IN_MEETING_ID)));
                    this.meetingOutstandingWelfare.setPaidInMeeting(paidInMeetingRepo.getMeeting());

                    MemberRepo memberRepo = new MemberRepo(this.context);
                    this.meetingOutstandingWelfare.setMember(memberRepo.getMemberById(cursor.getInt(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_MEMBER_ID))));
                }
            }
        } catch (Exception e) {
            Log.e("OutstandingWelfareRepo", e.getMessage());
        } finally {
            cursor.close();
            db.close();
        }
    }

    public MeetingOutstandingWelfare getMeetingOutstandingWelfare() {
        return this.meetingOutstandingWelfare;
    }

    public double getMemberTotalWelfareOutstandingInCycle(int cycleId, int memberId) {

        double totalFines = 0.00;
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sql = String.format("SELECT SUM(%s)AS TotalWelfareOutstanding FROM %s WHERE %s=%d AND %s=%d AND %s IN (SELECT %s FROM %s WHERE %s=%d)",
                    OutstandingWelfareSchema.COL_OW_AMOUNT,
                    OutstandingWelfareSchema.TBL_OUTSTANDING_WELFARE,
                    OutstandingWelfareSchema.COL_OW_MEMBER_ID, memberId,
                    OutstandingWelfareSchema.COL_OW_IS_CLEARED, 0,
                    OutstandingWelfareSchema.COL_OW_MEETING_ID,
                    MeetingSchema.COL_MT_MEETING_ID,
                    MeetingSchema.TBL_MEETINGS,
                    MeetingSchema.COL_MT_CYCLE_ID, cycleId);
            cursor = db.rawQuery(sql, null);
            if (cursor != null && cursor.moveToFirst()) {
                totalFines = cursor.getDouble(cursor.getColumnIndex("TotalWelfareOutstanding"));
            }
        } catch (Exception ex) {
            Log.e("TotalWelfareOutstanding", ex.getMessage());
            return 0.0;
        } finally {
            cursor.close();
            db.close();
        }

        return totalFines;
    }

    public MeetingOutstandingWelfare getMemberOutstandingWelfare(int meetingId, int memberId) {

        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sql = String.format("SELECT * FROM %s WHERE %s=%d AND %s=%d AND %s=%d",
                    OutstandingWelfareSchema.TBL_OUTSTANDING_WELFARE,
                    OutstandingWelfareSchema.COL_OW_MEETING_ID, meetingId,
                    OutstandingWelfareSchema.COL_OW_MEMBER_ID, memberId,
                    OutstandingWelfareSchema.COL_OW_IS_CLEARED, 0);
            cursor = db.rawQuery(sql, null);
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    this.meetingOutstandingWelfare = new MeetingOutstandingWelfare();
                    this.meetingOutstandingWelfare.setOutstandingWelfareId(cursor.getInt(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_ID)));
                    if (cursor.getString(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_EXPECTED_DATE)) != null) {
                        this.meetingOutstandingWelfare.setExpectedDate(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_EXPECTED_DATE))));
                    }
                    this.meetingOutstandingWelfare.setAmount(cursor.getDouble(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_AMOUNT)));
                    this.meetingOutstandingWelfare.setIsCleared(cursor.getInt(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_IS_CLEARED)));
                    if (cursor.getString(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_DATE_CLEARED)) != null) {
                        this.meetingOutstandingWelfare.setDateCleared(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_DATE_CLEARED))));
                    }
                    this.meetingOutstandingWelfare.setComment(cursor.getString(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_COMMENT)));

                    MeetingRepo meetingRepo = new MeetingRepo(this.context, cursor.getInt(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_MEETING_ID)));
                    this.meetingOutstandingWelfare.setMeeting(meetingRepo.getMeeting());

                    MeetingRepo paidInMeetingRepo = new MeetingRepo(this.context, cursor.getInt(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_PAID_IN_MEETING_ID)));
                    this.meetingOutstandingWelfare.setPaidInMeeting(paidInMeetingRepo.getMeeting());

                }
            }
        } catch (Exception e) {
            Log.e("OutstandingWelfareId", e.getMessage());
        } finally {
            cursor.close();
            db.close();
        }
        return this.meetingOutstandingWelfare;
    }

    public void updateMemberOutstandingWelfare(int outstandingWelfareId, int paidInMeetingId, int isCleared, Date dateCleared) {
        SQLiteDatabase db = null;
        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sql = "UPDATE " +
                    OutstandingWelfareSchema.TBL_OUTSTANDING_WELFARE + " " +
                    "SET " +
                    OutstandingWelfareSchema.COL_OW_PAID_IN_MEETING_ID + " = ?," +
                    OutstandingWelfareSchema.COL_OW_IS_CLEARED + " = ?," +
                    OutstandingWelfareSchema.COL_OW_DATE_CLEARED + " = ?" +
                    " WHERE " + OutstandingWelfareSchema.COL_OW_ID + " = ?";

            db.execSQL(sql, new String[]{String.valueOf(paidInMeetingId), String.valueOf(isCleared), Utils.formatDateToSqlite(dateCleared), String.valueOf(outstandingWelfareId)});
        } catch (Exception e) {
            Log.e("UpdateOutstanding", e.getMessage());
        } finally {
            db.close();
        }
    }

    public ArrayList<MeetingOutstandingWelfare> getMemberOutstandingWelfareHistory(int cycleId, int memberId) {

        ArrayList<MeetingOutstandingWelfare> meetingOutstandingWelfares = new ArrayList<MeetingOutstandingWelfare>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sql = String.format("SELECT * FROM %s WHERE %s=%d AND %s=%d AND %s IN (SELECT %s FROM %s WHERE %s=%d)",
                    OutstandingWelfareSchema.TBL_OUTSTANDING_WELFARE,
                    OutstandingWelfareSchema.COL_OW_MEMBER_ID, memberId,
                    OutstandingWelfareSchema.COL_OW_IS_CLEARED, 0,
                    OutstandingWelfareSchema.COL_OW_MEETING_ID,
                    MeetingSchema.COL_MT_MEETING_ID,
                    MeetingSchema.TBL_MEETINGS,
                    MeetingSchema.COL_MT_CYCLE_ID, cycleId);
            cursor = db.rawQuery(sql, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    MeetingOutstandingWelfare meetingOutstandingWelfare = new MeetingOutstandingWelfare();
                    meetingOutstandingWelfare.setOutstandingWelfareId(cursor.getInt(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_ID)));
                    if (cursor.getString(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_EXPECTED_DATE)) != null) {
                        meetingOutstandingWelfare.setExpectedDate(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_EXPECTED_DATE))));
                    }
                    meetingOutstandingWelfare.setAmount(cursor.getDouble(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_AMOUNT)));
                    meetingOutstandingWelfare.setIsCleared(cursor.getInt(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_IS_CLEARED)));
                    if (cursor.getString(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_DATE_CLEARED)) != null) {
                        meetingOutstandingWelfare.setDateCleared(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_DATE_CLEARED))));
                    }
                    meetingOutstandingWelfare.setComment(cursor.getString(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_COMMENT)));

                    MeetingRepo meetingRepo = new MeetingRepo(this.context, cursor.getInt(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_MEETING_ID)));
                    meetingOutstandingWelfare.setMeeting(meetingRepo.getMeeting());

                    MeetingRepo paidInMeetingRepo = new MeetingRepo(this.context, cursor.getInt(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_PAID_IN_MEETING_ID)));
                    meetingOutstandingWelfare.setPaidInMeeting(paidInMeetingRepo.getMeeting());

                    meetingOutstandingWelfares.add(meetingOutstandingWelfare);

                }
            }
        } catch (Exception e) {
            Log.e("OutstandingWelfareId", e.getMessage());
        } finally {
            cursor.close();
            db.close();
        }
        return meetingOutstandingWelfares;
    }

    public int getMemberOutstandingWelfareId(int meetingId, int memberId) {
        int welfareId = 0;
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sql = String.format("SELECT %s FROM %s WHERE %s=%d AND %s=%d AND %s=%d",
                    OutstandingWelfareSchema.COL_OW_ID, OutstandingWelfareSchema.TBL_OUTSTANDING_WELFARE,
                    OutstandingWelfareSchema.COL_OW_MEETING_ID, meetingId,
                    OutstandingWelfareSchema.COL_OW_MEMBER_ID, memberId,
                    OutstandingWelfareSchema.COL_OW_IS_CLEARED, 0);
            cursor = db.rawQuery(sql, null);
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    welfareId = cursor.getInt(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_ID));
                }
            }
        } catch (Exception e) {
            Log.e("OutstandingWelfareId", e.getMessage());
        } finally {
            cursor.close();
            db.close();
        }
        return welfareId;
    }

    public void saveMemberOutstandingWelfare(MeetingOutstandingWelfare meetingOutstandingWelfare) {
        boolean performUpdate = false;
        SQLiteDatabase db = null;
        try {
            int outstandingWelfareId = getMemberOutstandingWelfareId(meetingOutstandingWelfare.getMeeting().getMeetingId(), meetingOutstandingWelfare.getMember().getMemberId());
            if (outstandingWelfareId > 0) {
                performUpdate = true;
            }
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            if (performUpdate) {
                String sql = "UPDATE " + OutstandingWelfareSchema.TBL_OUTSTANDING_WELFARE + " SET " + OutstandingWelfareSchema.COL_OW_MEETING_ID + " = ?, " +
                        OutstandingWelfareSchema.COL_OW_MEMBER_ID + " = ?, " +
                        OutstandingWelfareSchema.COL_OW_AMOUNT + " = ?, " +
                        OutstandingWelfareSchema.COL_OW_EXPECTED_DATE + " = ?, " +
                        OutstandingWelfareSchema.COL_OW_COMMENT + " = ? WHERE " +
                        OutstandingWelfareSchema.COL_OW_ID + " = ?";
                db.execSQL(sql, new String[]{
                        String.valueOf(meetingOutstandingWelfare.getMeeting().getMeetingId()),
                        String.valueOf(meetingOutstandingWelfare.getMember().getMemberId()),
                        String.valueOf(meetingOutstandingWelfare.getAmount()),
                        Utils.formatDateToSqlite(meetingOutstandingWelfare.getExpectedDate()),
                        meetingOutstandingWelfare.getComment(),
                        String.valueOf(outstandingWelfareId)});
            } else {
                String sql = "INSERT INTO " + OutstandingWelfareSchema.TBL_OUTSTANDING_WELFARE + " (" +
                        OutstandingWelfareSchema.COL_OW_MEETING_ID + "," +
                        OutstandingWelfareSchema.COL_OW_MEMBER_ID + "," +
                        OutstandingWelfareSchema.COL_OW_AMOUNT + "," +
                        OutstandingWelfareSchema.COL_OW_EXPECTED_DATE + "," +
                        OutstandingWelfareSchema.COL_OW_IS_CLEARED + "," +
                        OutstandingWelfareSchema.COL_OW_DATE_CLEARED + "," +
                        OutstandingWelfareSchema.COL_OW_PAID_IN_MEETING_ID + "," +
                        OutstandingWelfareSchema.COL_OW_COMMENT + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                db.execSQL(sql, new String[]{
                        String.valueOf(meetingOutstandingWelfare.getMeeting().getMeetingId()),
                        String.valueOf(meetingOutstandingWelfare.getMember().getMemberId()),
                        String.valueOf(meetingOutstandingWelfare.getAmount()),
                        Utils.formatDateToSqlite(meetingOutstandingWelfare.getExpectedDate()),
                        String.valueOf(meetingOutstandingWelfare.getIsCleared()),
                        Utils.formatDateToSqlite(meetingOutstandingWelfare.getDateCleared()),
                        String.valueOf(meetingOutstandingWelfare.getPaidInMeeting()),
                        meetingOutstandingWelfare.getComment()});
            }
        } catch (Exception e) {
            Log.e("SaveOutstandingWelfare", e.getMessage());
        } finally {
            db.close();
        }
    }

    public ArrayList<OutstandingWelfareDataTransferRecord> getMeetingOutstandingWelfareForAllMembers(int meetingId) {
        ArrayList<OutstandingWelfareDataTransferRecord> outstandingWelfareDataTransferRecords = new ArrayList<OutstandingWelfareDataTransferRecord>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try{
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sql = String.format("SELECT * FROM %s WHERE %s=%d",
                    OutstandingWelfareSchema.TBL_OUTSTANDING_WELFARE,
                    OutstandingWelfareSchema.COL_OW_MEETING_ID, meetingId);
            cursor = db.rawQuery(sql, null);
            if(cursor != null){
                while(cursor.moveToNext()){
                    OutstandingWelfareDataTransferRecord outstandingWelfareDataTransferRecord = new OutstandingWelfareDataTransferRecord();
                    outstandingWelfareDataTransferRecord.setOutstandingWelfareId(cursor.getInt(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_ID)));
                    outstandingWelfareDataTransferRecord.setMeetingId(cursor.getInt(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_MEETING_ID)));
                    outstandingWelfareDataTransferRecord.setMemberId(cursor.getInt(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_MEMBER_ID)));
                    outstandingWelfareDataTransferRecord.setAmount(cursor.getDouble(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_AMOUNT)));
                    if(cursor.getString(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_EXPECTED_DATE)) != null) {
                        outstandingWelfareDataTransferRecord.setExpectedDate(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_EXPECTED_DATE))));
                    }
                    outstandingWelfareDataTransferRecord.setIsCleared(cursor.getInt(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_IS_CLEARED)));
                    if(cursor.getString(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_DATE_CLEARED)) != null) {
                        outstandingWelfareDataTransferRecord.setDateCleared(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_DATE_CLEARED))));
                    }
                    outstandingWelfareDataTransferRecord.setPaidInMeetingId(cursor.getInt(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_PAID_IN_MEETING_ID)));
                    outstandingWelfareDataTransferRecord.setComment(cursor.getString(cursor.getColumnIndex(OutstandingWelfareSchema.COL_OW_COMMENT)));
                    outstandingWelfareDataTransferRecords.add(outstandingWelfareDataTransferRecord);
                }
            }
        }catch (Exception e){
            Log.e("OutstandingWelfare", e.getMessage());
        }finally {
            cursor.close();
            db.close();
        }

        return outstandingWelfareDataTransferRecords;
    }

    public double getTotalOutstandingWelfareInMeeting(int meetingId){
        double totalOutstandingWelfare = 0.0;
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try{
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sql = String.format("SELECT SUM(%s) TotalOutstandingWelfare FROM %s WHERE %s=%d AND %s=%d",
                    OutstandingWelfareSchema.COL_OW_AMOUNT, OutstandingWelfareSchema.TBL_OUTSTANDING_WELFARE,
                    OutstandingWelfareSchema.COL_OW_IS_CLEARED, 0,
                    OutstandingWelfareSchema.COL_OW_MEETING_ID, meetingId);
            cursor = db.rawQuery(sql, null);
            if(cursor != null){
                if(cursor.moveToNext()){
                    totalOutstandingWelfare = cursor.getDouble(cursor.getColumnIndex("TotalOutstandingWelfare"));
                }
            }
        }catch (Exception e){
            Log.e("OutstandingWelfare", e.getMessage());
        }finally {
            cursor.close();
            db.close();
        }
        return totalOutstandingWelfare;
    }

}
