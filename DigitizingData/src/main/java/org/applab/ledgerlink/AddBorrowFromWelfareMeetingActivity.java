package org.applab.ledgerlink;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.domain.model.MeetingOutstandingWelfare;
import org.applab.ledgerlink.domain.model.Member;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.repo.MeetingOutstandingWelfareRepo;

import java.util.Calendar;
import java.util.Date;

public class AddBorrowFromWelfareMeetingActivity extends SherlockActivity {

    LedgerLinkApplication ledgerLinkApplication;
    private int selectedMemberId;
    private int meetingId;
    private MeetingOutstandingWelfare meetingOutstandingWelfare;
    private boolean selectedFinishButton = false;

    private TextView txtOutstandingWelfareDueDate;

    int mYear;
    int mMonth;
    int mDay;

    protected TextView viewClicked;

    protected final DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            updateDisplay();
        }
    };

    protected void updateDisplay() {
        if (viewClicked != null) {
            viewClicked.setText(String.format("%02d", mDay) + "-" + Utils.getMonthNameAbbrev(mMonth + 1) + "-" + mYear);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ledgerLinkApplication = (LedgerLinkApplication) getApplication();
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());

        if (getIntent().hasExtra("_memberId")) {
            this.selectedMemberId = getIntent().getIntExtra("_memberId", 0);
        }

        if (getIntent().hasExtra("_meetingId")) {
            this.meetingId = getIntent().getIntExtra("_meetingId", 0);
        }

        String fullName = getIntent().getStringExtra("_name");

        inflateCustomActionBar();

        setContentView(R.layout.activity_add_borrow_from_welfare_meeting);

        TextView lblMemberBorrowFromWelfareFullName = (TextView) findViewById(R.id.lblMemberBorrowFromWelfareFullName);
        lblMemberBorrowFromWelfareFullName.setText(fullName);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);

        txtOutstandingWelfareDueDate = (TextView) findViewById(R.id.txtOutstandingWelfareDueDate);
        txtOutstandingWelfareDueDate.setText(Utils.formatDate(cal.getTime(), "dd-MMM-yyyy"));
        txtOutstandingWelfareDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date nextOutstandingWelfareDueDate = Utils.stringToDate(txtOutstandingWelfareDueDate.getText().toString(), "dd-MMM-yyyy");
                Calendar cal = Calendar.getInstance();
                cal.setTime(nextOutstandingWelfareDueDate);
                mYear = cal.get(Calendar.YEAR);
                mMonth = cal.get(Calendar.MONTH);
                mDay = cal.get(Calendar.DAY_OF_MONTH);

                viewClicked = (TextView) view;
                DatePickerDialog datePickerDialog = new DatePickerDialog(AddBorrowFromWelfareMeetingActivity.this, mDateSetListener, mYear, mMonth, mDay);
                datePickerDialog.setTitle("Set the outstanding welfare due date");
                datePickerDialog.show();
            }
        });

        populateMemberOutstandingWelfare();
    }

    protected void populateMemberOutstandingWelfare(){
        MeetingOutstandingWelfare meetingOutstandingWelfare = new MeetingOutstandingWelfareRepo(getApplicationContext()).getMemberOutstandingWelfare(meetingId, selectedMemberId);
        if(meetingOutstandingWelfare != null){
            EditText txtIssueMemberWelfareAmount = (EditText) findViewById(R.id.txtIssueMemberWelfareAmount);
            txtIssueMemberWelfareAmount.setText(Utils.formatRealNumber(meetingOutstandingWelfare.getAmount()));

            txtOutstandingWelfareDueDate.setText(Utils.formatDate(meetingOutstandingWelfare.getExpectedDate()));
        }
    }

    private void inflateCustomActionBar() {

        // Inflate a "Done/Cancel" custom action bar view.
        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View customActionBarView;

        customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_cancel_done, null);
        customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedFinishButton = true;
                        Member member = new Member();
                        member.setMemberId(selectedMemberId);
                        if(saveMemberOutstandingWelfare(member)){
                            finish();
                        }
                    }
                }
        );
        customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }
        );

        // actionbar with logo
        ActionBar actionBar = getSupportActionBar();

        // Swap in training mode icon if in training mode
        if (Utils.isExecutingInTrainingMode()) {
            actionBar.setIcon(R.drawable.icon_training_mode);
        }
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setTitle("Borrow From Welfare");

        // Set to false to remove caret and disable its function; if designer decides otherwise set both to true
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);

        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL)
        );

        actionBar.setDisplayShowCustomEnabled(true);
    }

    protected boolean saveMemberOutstandingWelfare(Member member){
        boolean saveOutstandingWelfare = false;
        if(validateData(member)){
            ledgerLinkApplication.getMeetingOutstandingWelfareRepo().saveMemberOutstandingWelfare(this.meetingOutstandingWelfare);
            saveOutstandingWelfare = true;
        }
        return saveOutstandingWelfare;
    }

    protected boolean validateData(Member member){
        if(member == null){
            return false;
        }
        this.meetingOutstandingWelfare = new MeetingOutstandingWelfare();

        String dlgTitle = "Borrow From Welfare";

        EditText txtIssueMemberWelfareAmount = (EditText) findViewById(R.id.txtIssueMemberWelfareAmount);
        String outstandingWelfareAmount = txtIssueMemberWelfareAmount.getText().toString().trim();
        if(outstandingWelfareAmount.length() == 0){
            Utils.createAlertDialogOk(this, dlgTitle, "The welfare amount is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
            txtIssueMemberWelfareAmount.requestFocus();
            return false;
        }else{
            double disbursedWelfareAmount = Double.parseDouble(outstandingWelfareAmount);
            if(disbursedWelfareAmount < 1){
                Utils.createAlertDialogOk(this, dlgTitle, "The welfare amount is invalid", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtIssueMemberWelfareAmount.requestFocus();
                return false;
            }else{
                this.meetingOutstandingWelfare.setAmount(disbursedWelfareAmount);
                this.meetingOutstandingWelfare.setMember(member);
                Meeting issuedInMeeting = new Meeting();
                issuedInMeeting.setMeetingId(this.meetingId);
                this.meetingOutstandingWelfare.setMeeting(issuedInMeeting);

                Meeting paidInMeeting = new Meeting();
                paidInMeeting.setMeetingId(0);
                this.meetingOutstandingWelfare.setPaidInMeeting(paidInMeeting);
                this.meetingOutstandingWelfare.setIsCleared(0);
                Date outstandingDueDate = Utils.stringToDate(txtOutstandingWelfareDueDate.getText().toString(), "dd-MMM-yyyy");
                this.meetingOutstandingWelfare.setExpectedDate(outstandingDueDate);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.mnuFFinished:
                return false;
        }
        return true;
    }
}
