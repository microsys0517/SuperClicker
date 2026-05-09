package com.superclicker.model;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.superclicker.db.Converters;
@Entity(tableName = "scripts")
@TypeConverters(Converters.class)
public class Script {
    @PrimaryKey(autoGenerate = true) public long id;
    public String name;
    public String description;
    public long createTime;
    public long updateTime;
    public ExecutionConfig config;
    public Script() {
        this.createTime = System.currentTimeMillis();
        this.updateTime = System.currentTimeMillis();
        this.config = new ExecutionConfig();
    }
}
