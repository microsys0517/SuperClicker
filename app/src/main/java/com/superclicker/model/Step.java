package com.superclicker.model;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.superclicker.db.Converters;
@Entity(tableName = "steps",
    foreignKeys = @ForeignKey(entity = Script.class, parentColumns = "id", childColumns = "scriptId", onDelete = ForeignKey.CASCADE),
    indices = @Index("scriptId"))
@TypeConverters(Converters.class)
public class Step {
    @PrimaryKey(autoGenerate = true) public long id;
    public long scriptId;
    public int order;
    public StepType type;
    public String label;
    public long waitBefore;
    public long waitAfter;
    public int retryCount;
    public long retryDelay;
    public boolean enabled;
    public int x1, y1, x2, y2;
    public long duration;
    public String multiPoints;
    public String matchImagePath;
    public int matchColor;
    public int colorTolerance;
    public String matchText;
    public String regionJson;
    public String compareValue1;
    public String compareValue2;
    public String compareOperator;
    public String counterName;
    public int counterValue;
    public String counterOp;
    public String inputText;
    public int jumpTarget;
    public String jumpCondition;
    public String subScriptId;
    public String errorRule;
    public transient boolean lastResult;
    public Step() {
        this.enabled = true; this.waitBefore = 0; this.waitAfter = 500;
        this.retryCount = 0; this.retryDelay = 1000; this.duration = 300; this.colorTolerance = 10;
    }
    public Step copy() {
        Step s = new Step();
        s.scriptId = this.scriptId; s.order = this.order; s.type = this.type; s.label = this.label;
        s.waitBefore = this.waitBefore; s.waitAfter = this.waitAfter; s.retryCount = this.retryCount;
        s.retryDelay = this.retryDelay; s.enabled = this.enabled;
        s.x1 = this.x1; s.y1 = this.y1; s.x2 = this.x2; s.y2 = this.y2; s.duration = this.duration;
        s.multiPoints = this.multiPoints; s.matchImagePath = this.matchImagePath;
        s.matchColor = this.matchColor; s.colorTolerance = this.colorTolerance; s.matchText = this.matchText;
        s.regionJson = this.regionJson; s.compareValue1 = this.compareValue1;
        s.compareValue2 = this.compareValue2; s.compareOperator = this.compareOperator;
        s.counterName = this.counterName; s.counterValue = this.counterValue; s.counterOp = this.counterOp;
        s.inputText = this.inputText; s.jumpTarget = this.jumpTarget; s.jumpCondition = this.jumpCondition;
        s.subScriptId = this.subScriptId; s.errorRule = this.errorRule;
        return s;
    }
}
