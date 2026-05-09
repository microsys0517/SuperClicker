package com.superclicker.db;
import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.superclicker.model.Step;
import java.util.List;

@Dao
public interface StepDao {
    @Query("SELECT * FROM steps WHERE scriptId = :scriptId ORDER BY `order` ASC")
    LiveData<List<Step>> getByScriptId(long scriptId);

    @Query("SELECT * FROM steps WHERE scriptId = :scriptId ORDER BY `order` ASC")
    List<Step> getByScriptIdSync(long scriptId);

    @Query("SELECT * FROM steps WHERE id = :id")
    Step getById(long id);

    @Query("SELECT MAX(`order`) FROM steps WHERE scriptId = :scriptId")
    int getMaxOrder(long scriptId);

    @Insert
    long insert(Step step);

    @Update
    void update(Step step);

    @Delete
    void delete(Step step);

    @Query("DELETE FROM steps WHERE id IN (:ids)")
    void deleteByIds(List<Long> ids);
}
