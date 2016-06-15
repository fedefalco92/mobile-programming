package it.unibs.appwow.models;

import org.json.JSONException;
import org.json.JSONObject;

import it.unibs.appwow.utils.DateUtils;

/**
 * Created by Alessandro on 15/06/2016.
 */
public class CostModel {
    private int mId;
    private int mIdGroup;
    private int mIdUser;
    private double mAmount;
    private String mName;
    private String mNotes;
    private long mCreatedAt;
    private long mUpdatedAt;
    private long mArchivedAt;
    private String mPosition;
    private String mAmountDetails; // FIXME: 06/05/2016 da sostituire con un vector da riempire al momento dell'importazione dal DB

    public CostModel(int id, int idGroup, int idUser, double amount, String name, String notes, long createdAt, long updatedAt, long archivedAt, String position, String amountDetails) {
        mId = id;
        mIdGroup = idGroup;
        mIdUser = idUser;
        mAmount = amount;
        mName = name;
        mNotes = notes;
        mCreatedAt = createdAt;
        mUpdatedAt = updatedAt;
        mArchivedAt = archivedAt;
        mPosition = position;
        mAmountDetails = amountDetails;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public int getIdGroup() {
        return mIdGroup;
    }

    public void setIdGroup(int idGroup) {
        mIdGroup = idGroup;
    }

    public int getIdUser() {
        return mIdUser;
    }

    public void setIdUser(int idUser) {
        mIdUser = idUser;
    }

    public double getAmount() {
        return mAmount;
    }

    public void setAmount(double amount) {
        mAmount = amount;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getNotes() {
        return mNotes;
    }

    public void setNotes(String notes) {
        mNotes = notes;
    }

    public long getCreatedAt() {
        return mCreatedAt;
    }

    public void setCreatedAt(long createdAt) {
        mCreatedAt = createdAt;
    }

    public long getUpdatedAt() {
        return mUpdatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        mUpdatedAt = updatedAt;
    }

    public long getArchivedAt() {
        return mArchivedAt;
    }

    public void setArchivedAt(long archivedAt) {
        mArchivedAt = archivedAt;
    }

    public String getPosition() {
        return mPosition;
    }

    public void setPosition(String position) {
        mPosition = position;
    }

    public String getAmountDetails() {
        return mAmountDetails;
    }

    public void setAmountDetails(String amountDetails) {
        mAmountDetails = amountDetails;
    }

    public static CostModel create(JSONObject costJs) throws JSONException {
        int id = costJs.getInt("id");
        int idGroup = costJs.getInt("idGroup");
        int idUser = costJs.getInt("idUser");
        double amount = costJs.getDouble("amount");
        String name = costJs.getString("name");
        String notes = costJs.getString("notes");
        long createdAt = DateUtils.dateToLong(costJs.getString("created_at"));
        long updatedAt = DateUtils.dateToLong(costJs.getString("updated_at"));
        long archivedAt = DateUtils.dateToLong(costJs.getString("archived_at"));
        String position = costJs.getString("position");
        String amountDetails = costJs.getString("amount_details");

        return new CostModel(id, idGroup, idUser, amount, name, notes, createdAt, updatedAt, archivedAt, position, amountDetails);
    }
}