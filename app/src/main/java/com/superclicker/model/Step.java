package com.superclicker.model;
import androidx.room.Entity;import androidx.room.ForeignKey;import androidx.room.Index;
import androidx.room.PrimaryKey;import androidx.room.TypeConverters;
import com.superclicker.db.Converters;
@Entity(tableName="steps",foreignKeys=@ForeignKey(entity=Script.class,parentColumns="id",childColumns="scriptId",onDelete=ForeignKey.CASCADE),indices=@Index("scriptId"))
@TypeConverters(Converters.class)
public class Step{@PrimaryKey(autoGenerate=true)public long id;public long scriptId;public int order;public StepType type;
public String label;public long waitBefore;public long waitAfter;public int retryCount;public long retryDelay;public boolean enabled;
public int x1,y1,x2,y2;public long duration;public String multiPoints;public String matchImagePath;public int matchColor;public int colorTolerance;
public String matchText;public String regionJson;public String compareValue1;public String compareValue2;public String compareOperator;
public String counterName;public int counterValue;public String counterOp;public String inputText;public int jumpTarget;
public String jumpCondition;public String subScriptId;public String errorRule;public transient boolean lastResult;
public Step(){enabled=true;waitBefore=0;waitAfter=500;retryCount=1;retryDelay=1000;duration=300;colorTolerance=10;compareOperator="==";counterOp="set";}
}
