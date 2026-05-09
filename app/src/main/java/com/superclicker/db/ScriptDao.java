package com.superclicker.db;
import androidx.lifecycle.LiveData;import androidx.room.*;import com.superclicker.model.Script;import java.util.List;
@Dao
public interface ScriptDao{@Query("SELECT * FROM scripts ORDER BY updateTime DESC")LiveData<List<Script>> getAll();
@Query("SELECT * FROM scripts ORDER BY updateTime DESC")List<Script> getAllSync();
@Query("SELECT * FROM scripts WHERE id=:id")Script getById(long id);
@Insert long insert(Script s);@Update void update(Script s);@Delete void delete(Script s);
@Query("DELETE FROM scripts WHERE id=:id")void deleteById(long id);}
