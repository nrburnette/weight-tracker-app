package com.zybooks.projecttwonickburnetteweightlossoption;
// UPDATED Enhancement 3
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "weights")
public class WeightEntryEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String date; // formated as "yyyy-MM-dd"

    @NonNull
    @ColumnInfo(name = "WEIGHT") // <-- match actual column name in db
    public String weight;  // other db is kept as a string, initial convenience
}
