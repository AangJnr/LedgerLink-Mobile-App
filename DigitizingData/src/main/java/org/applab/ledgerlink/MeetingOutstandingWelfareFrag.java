package org.applab.ledgerlink;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;

import org.applab.ledgerlink.domain.model.Member;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.LongTaskRunner;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.helpers.adapters.BorrowFromWelfareArrayAdapter;
import org.applab.ledgerlink.repo.MemberRepo;

import java.util.ArrayList;


public class MeetingOutstandingWelfareFrag extends Fragment {
    ActionBar actionBar;
    ArrayList<Member> members;
    String meetingDate;
    int meetingId;
    private MeetingActivity parentActivity;
    private RelativeLayout fragmentView;
    LedgerLinkApplication ledgerLinkApplication;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = (MeetingActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container == null) {
            // We have different layouts, and in one of them this
            // fragment's containing frame doesn't exist.  The fragment
            // may still be created from its saved state, but there is
            // no reason to try to create its view hierarchy because it
            // won't be displayed.  Note this is not needed -- we could
            // just run the code below, where we would create and return
            // the view hierarchy; it would just never be used.
            return null;
        }
        fragmentView =  (RelativeLayout)inflater.inflate(R.layout.fragment_meeting_outstanding_welfare, container, false);
        initializeFragment();
        return fragmentView;
    }

    private void initializeFragment() {

        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());
        actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();
        meetingDate = getActivity().getIntent().getStringExtra("_meetingDate");
        String title = getString(R.string.meeting);
        switch(Utils._meetingDataViewMode) {
            case VIEW_MODE_REVIEW:
                title = getString(R.string.send_data);
                break;
            case VIEW_MODE_READ_ONLY:
                title = getString(R.string.send_data);
                break;
            default:
                //title="Meeting";
                break;
        }
        actionBar.setTitle(title);
        actionBar.setSubtitle(meetingDate);
        /**TextView lblMeetingDate = (TextView)getActivity()().findViewById(R.id.lblMSavFMeetingDate);
         meetingDate = getActivity()().getIntent().getStringExtra("_meetingDate");
         lblMeetingDate.setText(meetingDate); */
        meetingId = getActivity().getIntent().getIntExtra("_meetingId", 0);
        //Wrap long task in runnable an run asynchronously
        Runnable populateRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                //Populate the Members
                populateMembersList();
            }
        };
        LongTaskRunner.runLongTask(populateRunnable, getString(R.string.please_wait), getString(R.string.loading_outstanding_welfare_info), parentActivity);
    }

    private void populateMembersList() {
        // Load the Main Menu
        MemberRepo memberRepo = new MemberRepo(parentActivity.getBaseContext());
        members = memberRepo.getActiveMembers();

        // Now get the data via the adapter
        final BorrowFromWelfareArrayAdapter adapter = new BorrowFromWelfareArrayAdapter(parentActivity.getBaseContext(), members);
        adapter.setMeetingId(meetingId);


        // Assign Adapter to ListView
        final ListView lvwMembers = (ListView)fragmentView.findViewById(R.id.lvwBorrowFromWelfareMembers);
        final TextView txtEmpty = (TextView)fragmentView.findViewById(R.id.txtMBorrowFromWelfareEmpty);

        Runnable runOnUiRunnable = new Runnable() {
            @Override
            public void run() {
                lvwMembers.setEmptyView(txtEmpty);
                lvwMembers.setAdapter(adapter);
            }
        };

        parentActivity.runOnUiThread(runOnUiRunnable);

        // listening to single list item on click
        lvwMembers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //Do not invoke the event when in Read only Mode
                if (Utils._meetingDataViewMode != Utils.MeetingDataViewMode.VIEW_MODE_READ_ONLY) {
                    Member selectedMember = members.get(position);
                    Intent i = new Intent(view.getContext(), MemberOutstandingWelfareHistoryActivity.class);

                    // Pass on data
                    i.putExtra("_meetingDate", meetingDate);
                    i.putExtra("_memberId", selectedMember.getMemberId());
                    i.putExtra("_name", selectedMember.getFullName());
                    i.putExtra("_meetingId", meetingId);

                    startActivity(i);
                    //finish this list so that it doesnt show up after fining
                }
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        this.populateMembersList();
    }
}
