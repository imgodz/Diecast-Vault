package com.imgodz.diecastvault;

import androidx.lifecycle.LiveData;
import androidx.room.Dao; // Import the Dao annotation
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao // Add the @Dao annotation here
public interface DiecastDAO {

    @Insert
    void insert(Diecast diecast);

    @Update
    void update(Diecast diecast);

    @Delete
    void delete(Diecast diecast);

    @Query("SELECT * FROM diecast_table")
    LiveData<List<Diecast>> getAllDiecast();

    @Query("SELECT COUNT(*) FROM diecast_table")
    LiveData<Integer> getDiecastCount();

    @Query("SELECT model_maker FROM diecast_table GROUP BY model_maker ORDER BY COUNT(*) DESC LIMIT 1")
    LiveData<String> getMostCommonModelMaker();

    @Query("SELECT category, COUNT(*) as count FROM diecast_table GROUP BY category")
    LiveData<List<CategoryCount>> getCategoryBreakdown();

    @Query("SELECT SUM(price) FROM diecast_table")
    LiveData<Integer> getTotalSpent();

    @Query("SELECT * FROM diecast_table ORDER BY price DESC LIMIT 1")
    LiveData<Diecast> getMostExpensive();

    @Query("SELECT * FROM diecast_table WHERE id = :id LIMIT 1")
    LiveData<Diecast> getDiecastById(int id);


}