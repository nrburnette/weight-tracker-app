package com.zybooks.projecttwonickburnetteweightlossoption;
// UPDATED Enhancement 3
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;
@Dao
public interface WeightDao {
    @Query("SELECT * FROM weights ORDER BY date ASC")
    List<WeightEntryEntity> getAll();

    @Insert
    void insert(WeightEntryEntity e);

    // replicate existing delete-last entry
    @Query("DELETE FROM weights WHERE id = (SELECT MAX(id) FROM weights)")
    void deleteLast();

}
