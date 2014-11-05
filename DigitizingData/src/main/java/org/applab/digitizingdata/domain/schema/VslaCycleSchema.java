package org.applab.digitizingdata.domain.schema;

/**
 * Created by Moses on 7/13/13.
 */

public class VslaCycleSchema {
    // Table: VslaCycles
    public static final String TBL_VSLA_CYCLES = "VslaCycles";
    public static final String COL_VC_CYCLE_ID = "_id";
    public static final String COL_VC_CYCLE_CODE = "CycleCode";
    public static final String COL_VC_START_DATE = "StartDate";
    public static final String COL_VC_END_DATE = "EndDate";
    public static final String COL_VC_SHARE_PRICE = "SharePrice";
    public static final String COL_VC_MAX_SHARE_QTY = "MaxShareQuantity";
    public static final String COL_VC_MAX_START_SHARE = "MaxStartShare";
    public static final String COL_VC_INTEREST_RATE = "InterestRate";
    public static final String COL_VC_IS_ACTIVE = "IsActive";
    public static final String COL_VC_IS_ENDED = "IsEnded";
    public static final String COL_VC_DATE_ENDED = "DateEnded";
    public static final String COL_VC_SHARED_AMOUNT = "SharedAmount";
    public static final String COL_VC_INTEREST_AT_SETUP = "InterestAtSetup";    //Interest in case of Mid-CYcle Setup
    public static final String COL_VC_FINES_AT_SETUP = "FinesAtSetup";    //Fines collected so far in case of Mid-CYcle Setup
    public static final String COL_VC_INTEREST_AT_SETUP_COMMENT = "InterestAtSetupComment";    //Interest in case of Mid-CYcle Setup
    public static final String COL_VC_FINES_AT_SETUP_COMMENT = "FinesAtSetupComment";    //Fines collected so far in case of Mid-CYcle Setup

    public static String getCreateTableScript() {
        StringBuffer sb = null;

        // Create Table: VslaInfo
        sb = new StringBuffer();
        sb.append("CREATE TABLE " + TBL_VSLA_CYCLES + " (");
        sb.append(COL_VC_CYCLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,");
        sb.append(COL_VC_CYCLE_CODE + " TEXT ,");
        sb.append(COL_VC_START_DATE + " TEXT ,");
        sb.append(COL_VC_END_DATE + " TEXT ,");
        sb.append(COL_VC_SHARE_PRICE + " NUMERIC ,");
        sb.append(COL_VC_MAX_SHARE_QTY + " NUMERIC ,");
        sb.append(COL_VC_MAX_START_SHARE + " NUMERIC ,");
        sb.append(COL_VC_INTEREST_RATE + " NUMERIC ,");
        sb.append(COL_VC_IS_ACTIVE + " INTEGER ,");
        sb.append(COL_VC_IS_ENDED + " INTEGER ,");
        sb.append(COL_VC_DATE_ENDED + " TEXT ,");
        sb.append(COL_VC_SHARED_AMOUNT + " NUMERIC ,");
        sb.append(COL_VC_INTEREST_AT_SETUP + " NUMERIC ,");
        sb.append(COL_VC_INTEREST_AT_SETUP_COMMENT + " TEXT ,");
        sb.append(COL_VC_FINES_AT_SETUP + " NUMERIC ,");
        sb.append(COL_VC_FINES_AT_SETUP_COMMENT + " TEXT");
        sb.append(")");

        return sb.toString();
    }

    public static String getDropTableScript() {
        return "DROP TABLE IF EXISTS " + TBL_VSLA_CYCLES;
    }

    public static String getAlterTableScript() {
        StringBuffer sbAlter = null;

        // Create Table: VslaInfo
        sbAlter = new StringBuffer();
        sbAlter.append("ALTER TABLE " + TBL_VSLA_CYCLES + " ");
        //sbAlter.append("ADD COLUMN " + COL_VC_FINES_AT_SETUP_COMMENT + " TEXT AFTER " + COL_VC_FINES_AT_SETUP + ", ");
      //  sbAlter.append("ADD IF NOT EXISTS COLUMN " + COL_VC_NARA_AT_SETUP_COMMENT + " TEXT AFTER " + COL_VC_INTEREST_AT_SETUP_COMMENT + ";" );
        return sbAlter.toString();
    }

    public static String getTableName() {
        return TBL_VSLA_CYCLES;
    }

    public static String getColumnList() {
        StringBuffer sb = new StringBuffer();
        sb.append(COL_VC_CYCLE_ID + ",");
        sb.append(COL_VC_CYCLE_CODE + ",");
        sb.append(COL_VC_START_DATE + ",");
        sb.append(COL_VC_END_DATE + ",");
        sb.append(COL_VC_SHARE_PRICE + ",");
        sb.append(COL_VC_MAX_SHARE_QTY + ",");
        sb.append(COL_VC_MAX_START_SHARE + ",");
        sb.append(COL_VC_INTEREST_RATE + ",");
        sb.append(COL_VC_IS_ACTIVE + ",");
        sb.append(COL_VC_IS_ENDED + ",");
        sb.append(COL_VC_DATE_ENDED + ",");
        sb.append(COL_VC_SHARED_AMOUNT + ",");
        sb.append(COL_VC_INTEREST_AT_SETUP + ",");
        sb.append(COL_VC_INTEREST_AT_SETUP_COMMENT + ",");
        sb.append(COL_VC_FINES_AT_SETUP + ",");
        sb.append(COL_VC_FINES_AT_SETUP_COMMENT);

        return sb.toString();
    }

    public static String[] getColumnListArray() {
        String[] columns = getColumnList().split(",");
        return columns;
    }


}

