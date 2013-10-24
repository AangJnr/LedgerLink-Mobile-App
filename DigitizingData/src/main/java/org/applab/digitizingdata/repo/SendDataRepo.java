package org.applab.digitizingdata.repo;

import android.content.Context;
import android.telephony.TelephonyManager;

import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.domain.model.Member;
import org.applab.digitizingdata.domain.model.VslaCycle;
import org.applab.digitizingdata.domain.model.VslaInfo;
import org.applab.digitizingdata.helpers.DatabaseHandler;
import org.applab.digitizingdata.helpers.Utils;
import org.json.*;

import java.util.ArrayList;

/**
 * Created by Moses on 10/24/13.
 */
public class SendDataRepo {

    private static String vslaCode = null;
    private static String phoneImei = null;

    private static String getVslaCode() {
        try {
            if(vslaCode == null || vslaCode.length() < 1) {
                VslaInfoRepo vslaInfoRepo = new VslaInfoRepo(DatabaseHandler.databaseContext);
                VslaInfo vslaInfo = vslaInfoRepo.getVslaInfo();
                if(null != vslaInfo) {
                    vslaCode = vslaInfo.getVslaCode();
                }
            }
            return vslaCode;
        }
        catch(Exception ex) {
            return null;
        }
    }

    private static String getPhoneImei() {
        try {
            if(phoneImei == null || phoneImei.length()<1){
                TelephonyManager tm = (TelephonyManager)DatabaseHandler.databaseContext.getSystemService(Context.TELEPHONY_SERVICE);
                phoneImei = tm.getDeviceId();
            }
            return phoneImei;
        }
        catch(Exception ex) {
            return null;
        }
    }

    public static String getVslaCycleJson(VslaCycle cycle) {

        if(cycle == null) {
            return null;
        }

        //Build JSON input string
        JSONStringer js = new JSONStringer();
        String jsonRequest = null;

        try {
            jsonRequest = js
                    .object()
                    .key("VslaCode").value(getVslaCode())
                    .key("PhoneImei").value(getPhoneImei())
                    .key("VslaCycle").object()
                        .key("CycleId").value(cycle.getCycleId())
                        .key("StartDate").value(Utils.formatDate(cycle.getStartDate(),"yyyy-MM-dd"))
                        .key("EndDate").value(Utils.formatDate(cycle.getEndDate(),"yyyy-MM-dd"))
                        .key("SharePrice").value(cycle.getSharePrice())
                        .key("MaxShareQty").value(cycle.getMaxSharesQty())
                        .key("MaxStartShare").value(cycle.getMaxStartShare())
                        .key("InterestRate").value(cycle.getInterestRate())
                        .endObject()
                    .endObject()
                    .toString();

        }
        catch(JSONException ex) {
            return null;
        }
        catch(Exception ex) {
            return null;
        }

        return jsonRequest;
    }

    public static String getMembersJson(ArrayList<Member> members) {

        if(members == null) {
            return null;
        }

        //Build JSON input string
        JSONStringer js = new JSONStringer();
        String jsonRequest = null;

        try {
            js.object()
                .key("VslaCode").value(getVslaCode())
                .key("PhoneImei").value(getPhoneImei())
                .key("MemberCount").value(members.size())
                .key("Members").array();
            for(Member member : members) {
                js.object()
                        .key("MemberId").value(member.getMemberId())
                        .key("MemberNo").value(member.getMemberNo())
                        .key("Surname").value(member.getSurname())
                        .key("OtherNames").value(member.getOtherNames())
                        .key("Gender").value(member.getGender())
                        .key("DateOfBirth").value(Utils.formatDate(member.getDateOfBirth(), "yyyy-MM-dd"))
                        .key("Occupation").value(member.getOccupation())
                        .key("PhoneNumber").value(member.getPhoneNumber())
                        .key("CyclesCompleted").value(member.getCyclesCompleted())
                        .key("IsActive").value(member.isActive())
                        .key("IsArchived").value(false)
                    .endObject();
            }

            js.endArray()
            .endObject();

            jsonRequest = js.toString();

        }
        catch(JSONException ex) {
            return null;
        }
        catch(Exception ex) {
            return null;
        }

        return jsonRequest;
    }

    public static String getMeetingJson(Meeting meeting) {

        if(meeting == null) {
            return null;
        }

        //Build JSON input string
        JSONStringer js = new JSONStringer();
        String jsonRequest = null;

        try {
            MeetingRepo meetingRepo = new MeetingRepo(DatabaseHandler.databaseContext);
            MeetingSavingRepo savingRepo = new MeetingSavingRepo(DatabaseHandler.databaseContext);
            MeetingAttendanceRepo attendanceRepo = new MeetingAttendanceRepo(DatabaseHandler.databaseContext);
            MeetingLoanIssuedRepo loanIssuedRepo = new MeetingLoanIssuedRepo(DatabaseHandler.databaseContext);
            MeetingLoanRepaymentRepo loanRepaymentRepo = new MeetingLoanRepaymentRepo(DatabaseHandler.databaseContext);

            double membersPresent = attendanceRepo.getAttendanceCountByMeetingId(meeting.getMeetingId(), 1);
            double savings = savingRepo.getTotalSavingsInMeeting(meeting.getMeetingId());
            double loansRepaid = loanRepaymentRepo.getTotalLoansRepaidInMeeting(meeting.getMeetingId());
            double loansIssued = loanIssuedRepo.getTotalLoansIssuedInMeeting(meeting.getMeetingId());

            jsonRequest = js
                .object()
                    .key("VslaCode").value(getVslaCode())
                    .key("PhoneImei").value(getPhoneImei())
                    .key("CycleId").value((meeting.getVslaCycle() != null) ? meeting.getVslaCycle().getCycleId(): 0)
                    .key("MeetingId").value(meeting.getMeetingId())
                    .key("MeetingDate").value(Utils.formatDate(meeting.getMeetingDate(),"yyyy-MM-dd"))
                    .key("OpeningBalanceBox").value(meeting.getOpeningBalanceBox())
                    .key("OpeningBalanceBank").value(meeting.getOpeningBalanceBank())
                    .key("Fines").value(meeting.getFines())
                    .key("MembersPresent").value(membersPresent)
                    .key("Savings").value(savings)
                    .key("LoansRepaid").value(loansRepaid)
                    .key("LoansIssued").value(loansIssued)
                    .key("ClosingBalanceBox").value(meeting.getClosingBalanceBox())
                    .key("ClosingBalanceBank").value(meeting.getClosingBalanceBank())
                    .key("IsCashBookBalanced").value(meeting.isCashBookBalanced())
                    .key("IsDataSent").value(meeting.isMeetingDataSent())
                .endObject()
                .toString();
        }
        catch(JSONException ex) {
            return null;
        }
        catch(Exception ex) {
            return null;
        }
        return jsonRequest;
    }
}
