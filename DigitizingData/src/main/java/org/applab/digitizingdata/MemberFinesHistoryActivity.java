package org.applab.digitizingdata;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.domain.model.VslaCycle;
import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;
import org.applab.digitizingdata.helpers.EnhancedListView;
import org.applab.digitizingdata.helpers.FineHistoryArrayAdapter;
import org.applab.digitizingdata.helpers.MemberFineRecord;
import org.applab.digitizingdata.helpers.SwipeDetector;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.FineTypeRepo;
import org.applab.digitizingdata.repo.MeetingFineRepo;
import org.applab.digitizingdata.repo.MeetingRepo;
import org.applab.digitizingdata.repo.VslaCycleRepo;

import java.util.ArrayList;
import java.util.Date;

//import de.timroes.android.listview.EnhancedListView;


/**
 * Created by Moses on 7/7/13.
 */
public class MemberFinesHistoryActivity extends SherlockListActivity {
    ActionBar actionBar;
    String meetingDate;
    int memberId;
    int meetingId;
    private MeetingFineRepo fineRepo = null;
    Meeting targetMeeting = null;
    MeetingRepo meetingRepo = null;
    ArrayList<MemberFineRecord> fines;
    int targetCycleId = 0;
    boolean proceedWithSaving = false;
    boolean alertDialogShowing = false;
    SwipeDetector swipeDetector = new SwipeDetector();


    private enum ControlGroup {
        SWIPE_TO_DISMISS
    }

    private static final String PREF_UNDO_STYLE = "de.timroes.android.listviewdemo.UNDO_STYLE";
    private static final String PREF_SWIPE_TO_DISMISS = "de.timroes.android.listviewdemo.SWIPE_TO_DISMISS";
    private static final String PREF_SWIPE_DIRECTION = "de.timroes.android.listviewdemo.SWIPE_DIRECTION";
    private static final String PREF_SWIPE_LAYOUT = "de.timroes.android.listviewdemo.SWIPE_LAYOUT";


    private EnhancedListAdapter mAdapter;
    private EnhancedListView mListView;


    private Bundle mUndoStylePref;
    private Bundle mSwipeDirectionPref;

    FineHistoryArrayAdapter fineHistoryArrayAdapter;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());
        inflateActionBar();

        setContentView(R.layout.activity_member_fines_history);

        // TextView lblMeetingDate = (TextView)findViewById(R.id.lblMSHMeetingDate);
        meetingDate = getIntent().getStringExtra("_meetingDate");
        // lblMeetingDate.setText(meetingDate);

        TextView lblFullName = (TextView) findViewById(R.id.lblFineFullName);
        String fullName = getIntent().getStringExtra("_name");
        lblFullName.setText(fullName);

        //ListView swipeListView= (ListView) findViewById(android.R.id.list);

        // SwipeListView swipeListView = (SwipeListView) findViewById(android.R.id.list);

        // swipeViewAdapter= new SwipeViewAdapter(this, R.layout.swipelistview, countries);

        // swipeListView.setAdapter(swipeViewAdapter);

        mListView = (EnhancedListView) findViewById(android.R.id.list);


        if (getIntent().hasExtra("_meetingId")) {
            meetingId = getIntent().getIntExtra("_meetingId", 0);
        }

        if (getIntent().hasExtra("_memberId")) {
            memberId = getIntent().getIntExtra("_memberId", 0);
        }

        fineRepo = new MeetingFineRepo(MemberFinesHistoryActivity.this);
        meetingRepo = new MeetingRepo(MemberFinesHistoryActivity.this);
        targetMeeting = meetingRepo.getMeetingById(meetingId);

        if (targetMeeting != null && targetMeeting.getVslaCycle() != null) {
            targetCycleId = targetMeeting.getVslaCycle().getCycleId();
            double totalFines = fineRepo.getMemberTotalFinesInCycle(targetCycleId, memberId);
        }

        mListView.setSwipingLayout(R.id.swiping_layout);

        // Set the callback that handles dismisses.
        mListView.setDismissCallback(new EnhancedListView.OnDismissCallback() {
            /**
             * This method will be called when the user swiped a way or deleted it via
             * {@link EnhancedListView#delete(int)}.
             *
             * @param listView The {@link EnhancedListView} the item has been deleted from.
             * @param position The position of the item to delete from your adapter.
             * @return An {@link EnhancedListView.Undoable}, if you want
             *      to give the user the possibility to undo the deletion.
             */
            @Override
            public EnhancedListView.Undoable onDismiss(EnhancedListView listView, final int position) {

                final MemberFineRecord item = (MemberFineRecord) mAdapter.getItem(position);
                mAdapter.remove(position);
                return new EnhancedListView.Undoable() {
                    @Override
                    public void undo() {
                        mAdapter.insert(position, item);
                    }
                };
            }
        });


        applySwipeSettings();
        populateFineHistory();

    }

    private void inflateActionBar() {
        // BEGIN_INCLUDE (inflate_set_custom_view)
        // Inflate a "Done/Cancel" custom action bar view.
        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_back, null);
        /** final View customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_back_done, null);
         customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
         new View.OnClickListener() {
        @Override public void onClick(View v) {
        if(saveMemberFine()) {
        Toast.makeText(MemberFinesHistoryActivity.this,"New Fine entered successfully",Toast.LENGTH_LONG).show();
        Intent i = new Intent(getApplicationContext(), MeetingActivity.class);
        i.putExtra("_tabToSelect", "fines");
        i.putExtra("_meetingDate",meetingDate);
        i.putExtra("_meetingId",meetingId);
        startActivity(i);
        finish();
        }

        }
        }); */
        customActionBarView.findViewById(R.id.actionbar_back).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getApplicationContext(), MeetingActivity.class);
                        i.putExtra("_tabToSelect", "fines");
                        i.putExtra("_meetingDate", meetingDate);
                        i.putExtra("_meetingId", meetingId);
                        startActivity(i);
                        finish();
                    }
                }
        );


        actionBar = getSupportActionBar();
        actionBar.setTitle("Fines");

        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);

        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL)
        );

        actionBar.setDisplayShowCustomEnabled(true);

        /**   actionBar.setDisplayOptions(
         ActionBar.DISPLAY_SHOW_CUSTOM,
         ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
         | ActionBar.DISPLAY_SHOW_TITLE
         );
         actionBar.setCustomView(customActionBarView,
         new ActionBar.LayoutParams(
         ViewGroup.LayoutParams.MATCH_PARENT,
         ViewGroup.LayoutParams.MATCH_PARENT)
         ); */
        // END_INCLUDE (inflate_set_custom_view)
    }

    private void populateFineHistory() {
        if (fineRepo == null) {
            fineRepo = new MeetingFineRepo(MemberFinesHistoryActivity.this);
        }
        fines = fineRepo.getMemberFineHistoryInCycle(targetCycleId, memberId);

        if (fines == null) {
            fines = new ArrayList<MemberFineRecord>();

        }


        /** swipeViewAdapter= new SwipeViewAdapter(this,R.layout.swipelistview, countries);

         swipeListView.setAdapter(swipeViewAdapter); */
        //Now get the data via the adapter
        FineHistoryArrayAdapter adapter = new FineHistoryArrayAdapter(MemberFinesHistoryActivity.this, fines, "fonts/roboto-regular.ttf");
        mAdapter = new EnhancedListAdapter(MemberFinesHistoryActivity.this, fines, "fonts/roboto-regular.ttf");
        // mAdapter.resetItems();

        mListView.setAdapter(mAdapter);
        //Assign Adapter to ListView
        // setListAdapter(adapter);

        /**   getListView().setOnTouchListener(swipeDetector);

         getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
         public void onItemClick(AdapterView<?> parent, View view,
         int position, long id) {
         if (swipeDetector.swipeDetected()) {
         if (swipeDetector.getAction() == SwipeDetector.Action.RL) {
         view.setBackgroundColor(R.color.light_blue_bottom_right);
         } else {

         }
         }
         }

         });
         */

        //Hack to ensure all Items in the List View are visible
        Utils.setListViewHeightBasedOnChildren(getListView());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.member_fine_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent i;
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = new Intent(this, MeetingActivity.class);
                upIntent.putExtra("_tabToSelect", "fines");
                upIntent.putExtra("_meetingDate", meetingDate);
                upIntent.putExtra("_meetingId", meetingId);

                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is not part of the application's task, so
                    // create a new task
                    // with a synthesized back stack.
                    TaskStackBuilder
                            .from(this)
                            .addNextIntent(new Intent(this, MeetingActivity.class))
                            .addNextIntent(upIntent).startActivities();
                    finish();
                } else {
                    // This activity is part of the application's task, so simply
                    // navigate up to the hierarchical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
            case R.id.mnuMFBack:
                i = new Intent(MemberFinesHistoryActivity.this, MeetingActivity.class);
                i.putExtra("_tabToSelect", "fines");
                i.putExtra("_meetingDate", meetingDate);
                i.putExtra("_meetingId", meetingId);
                startActivity(i);
                return true;
            /** case R.id.mnuMSHSave:

             if (saveMemberFine()) {
             Toast.makeText(MemberFinesHistoryActivity.this, "Fines entered successfully", Toast.LENGTH_LONG).show();
             i = new Intent(MemberFinesHistoryActivity.this, MeetingActivity.class);
             i.putExtra("_tabToSelect", "fines");
             i.putExtra("_meetingDate", meetingDate);
             i.putExtra("_meetingId", meetingId);
             startActivity(i);
             } */
        }
        return true;
    }

    /** public void onClick(View view) {
     Toast.makeText(this, "Deletion undone, OKAY?", Toast.LENGTH_LONG).show();
     // fineHistoryArrayAdapter.insert(itemToDelete, position);
     view.setVisibility(View.GONE);

     } */


    /**
     * Applies the settings the user has made to the list view.
     */
    private void applySwipeSettings() {

        // Set the UndoStyle, the user selected.
        EnhancedListView.UndoStyle style = EnhancedListView.UndoStyle.SINGLE_POPUP;

        mListView.setUndoStyle(style);
        // Enable or disable Swipe to Dismiss
        mListView.enableSwipeToDismiss();

        // Set the swipe direction
        EnhancedListView.SwipeDirection direction;
        direction = EnhancedListView.SwipeDirection.BOTH;
        mListView.setSwipeDirection(direction);

    }

    private class EnhancedListAdapter extends BaseAdapter {

        Context context;
        ArrayList<MemberFineRecord> values;
        int position;
        Typeface typeface;
        FineTypeRepo fineTypeRepo;
        MeetingFineRepo finesRepo;
        VslaCycleRepo cycleRepo;
        VslaCycle currentCycle;
        String datePaid;
        private View viewContainer;
        private boolean changedFromCode = false;

        public EnhancedListAdapter(Context context, ArrayList<MemberFineRecord> values, String font) {
            //super(context, R.layout.row_fines_history, values);
            this.context = context;
            this.values = values;
            this.typeface = Typeface.createFromAsset(context.getAssets(), font);

            finesRepo = new MeetingFineRepo(getApplicationContext());
            fineTypeRepo = new FineTypeRepo(getApplicationContext());
            cycleRepo = new VslaCycleRepo(getApplicationContext());
            currentCycle = cycleRepo.getCurrentCycle();
        }

        /**
         * void resetItems() {
         * values.clear();
         * for (int i = 1; i <= 40; i++) {
         * values.add("Item " + i);
         * }
         * notifyDataSetChanged();
         * }
         */

        public void remove(int position) {
            values.remove(position);
            notifyDataSetChanged();
        }

        public void insert(int position, MemberFineRecord fineRecord) {
            values.add(position, fineRecord);
            notifyDataSetChanged();
        }

        /**
         * How many items are in the data set represented by this Adapter.
         *
         * @return Count of items.
         */
        @Override
        public int getCount() {
            return values.size();
        }

        /**
         * Get the data item associated with the specified position in the data set.
         *
         * @param position Position of the item whose data we want within the adapter's
         *                 data set.
         * @return The data at the specified position.
         */
        @Override
        public Object getItem(int position) {
            return values.get(position);
        }

        /**
         * Get the row id associated with the specified position in the list.
         *
         * @param position The position of the item within the adapter's data set whose row id we want.
         * @return The id of the item at the specified position.
         */
        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * Get a View that displays the data at the specified position in the data set. You can either
         * create a View manually or inflate it from an XML layout file. When the View is inflated, the
         * parent View (GridView, ListView...) will apply default layout parameters unless you use
         * {@link android.view.LayoutInflater#inflate(int, android.view.ViewGroup, boolean)}
         * to specify a root view and to prevent attachment to the root.
         *
         * @param position The position of the item within the adapter's data set of the item whose view
         *                 we want.
         * @param rowView  The old view to reuse, if possible. Note: You should check that this view
         *                 is non-null and of an appropriate type before using. If it is not possible to convert
         *                 this view to display the correct data, this method can create a new view.
         *                 Heterogeneous lists can specify their number of view types, so that this View is
         *                 always of the right type (see {@link #getViewTypeCount()} and
         *                 {@link #getItemViewType(int)}).
         * @param parent   The parent that this view will eventually be attached to
         * @return A View corresponding to the data at the specified position.
         */
        @Override
        public View getView(int position, View rowView, ViewGroup parent) {

            final ViewHolder holder;


            if (rowView == null) {
                rowView = getLayoutInflater().inflate(R.layout.row_fines_history, parent, false);

                final View origView = rowView;

                changedFromCode = true;
                holder = new ViewHolder();
                assert rowView != null;
                holder.txtFineMeetingDate = (TextView) rowView.findViewById(R.id.txtFineMeetingDate);
                holder.txtFineType = (TextView) rowView.findViewById(R.id.lblFineType);
                holder.txtFineAmount = (TextView) rowView.findViewById(R.id.txtMemberFineAmount);
                holder.paidStatusCheckBox = (CheckBox) rowView.findViewById(R.id.chkMemberFine);

                // Set typeface
                holder.txtFineMeetingDate.setTypeface(typeface);
                holder.txtFineType.setTypeface(typeface);
                holder.txtFineAmount.setTypeface(typeface);

                rowView.setTag(holder);
            } else {
                holder = (ViewHolder) rowView.getTag();
            }

            //Assign Values to the Widgets
            final MemberFineRecord fineRecord = values.get(position);

            RelativeLayout parentLayout = (RelativeLayout) holder.paidStatusCheckBox.getParent();
            parentLayout.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    if ((fineRecord.getStatus() == 1) && (!(fineRecord.getPaidInMeetingId() == meetingId))) {
                                                        Toast.makeText(context, R.string.fine_is_previously__paid_warning, Toast.LENGTH_LONG).show();
                                                    }
                                                }


                                            }
            );


            // If fines were paid in previous meetings disable editing
            if ((fineRecord.getStatus() == 1) && (fineRecord.getPaidInMeetingId() != meetingId)) {
                holder.paidStatusCheckBox.setEnabled(false);
                holder.paidStatusCheckBox.setClickable(false);
                parentLayout.setEnabled(false);
                parentLayout.setActivated(false);
            } else {
                holder.paidStatusCheckBox.setEnabled(true);
                holder.paidStatusCheckBox.setClickable(true);
                parentLayout.setEnabled(true);
                parentLayout.setActivated(true);
            }

            holder.paidStatusCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                                     @Override
                                                                     public void onCheckedChanged(CompoundButton buttonView,
                                                                                                  boolean isChecked) {
                                                                         if (!changedFromCode) {


                                                                             if (isChecked) {
                                                                                 fineRecord.setStatus(1);
                                                                                 Date date = new Date();
                                                                                 // datePaid = Utils.formatDateToSqlite((date));
                                                                                 datePaid = meetingDate;
                                                                             } else {
                                                                                 fineRecord.setStatus(0);
                                                                                 datePaid = "";
                                                                             }
                                                                             finesRepo.updateMemberFineStatus(meetingId, fineRecord.getFineId(), fineRecord.getStatus(), datePaid);
                                                                         }
                                                                     }
                                                                 }
            );


            holder.position = position;

            if (fineRecord != null)

            {
                holder.txtFineMeetingDate.setText(String.format(Utils.formatDate(fineRecord.getMeetingDate(), Utils.OTHER_DATE_FIELD_FORMAT)));
                holder.txtFineAmount.setText(String.format("%,.0fUGX", fineRecord.getAmount()));

                /** TODO: REMOVE and find a better way how
                 * Meantime fix for QA time */
                switch (fineRecord.getFineTypeId()) {
                    case 1:
                        fineRecord.setFineTypeName(context.getResources().getString(R.string.finetype_other));
                        break;
                    case 2:
                        fineRecord.setFineTypeName(context.getResources().getString(R.string.finetype_latecoming));
                        break;
                    case 3:
                        fineRecord.setFineTypeName(context.getResources().getString(R.string.finetype_disorder));
                        break;
                    default:
                        fineRecord.setFineTypeName("Unknown");
                }
                holder.txtFineType.setText(fineRecord.getFineTypeName());
                holder.paidStatusCheckBox.setChecked(fineRecord.getStatus() != 0);
                changedFromCode = false;
            }

            return rowView;
        }

        private class ViewHolder {
            CheckBox paidStatusCheckBox;
            TextView txtFineAmount;
            TextView txtFineType;
            TextView txtFineMeetingDate;
            int position;
        }

    }
}