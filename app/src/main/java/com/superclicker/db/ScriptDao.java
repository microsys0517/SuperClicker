package com.superclicker.db;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.superclicker.model.Script;
import java.util.List;
@Dao
public interface ScriptDao {
    @Query("SELECT * FROM scripts ORDER BY updateTime DESC") LiveData<List<Script>> getAll();
    @Query("SELECT * FROM scripts ORDER BY updateTime DESC") List<Script> getAllSync();
    @Query("SELECT * FROM scripts WHERE id = :id") Script getById(long id);
    @Insert long insert(Script script);
    @Update void update(Script script);
    @Delete void delete(Script script);
    @Query("DELETE FROM scripts WHERE id = :id") void deleteById(long id);
}
