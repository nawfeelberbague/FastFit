package com.example.fastfit;

/**
 * Represents an exercise recommendation for a specific time window.
 */
public class ExerciseSlot {

    public enum Status { BEST, GOOD, AVOID }

    private final String timeRange;      // e.g. "9:00 PM – 11:00 PM"
    private final String period;         // e.g. "After Iftar"
    private final String exerciseType;   // e.g. "Strength Training / HIIT"
    private final String tip;            // short advice
    private final Status status;         // BEST | GOOD | AVOID

    public ExerciseSlot(String timeRange, String period,
                        String exerciseType, String tip, Status status) {
        this.timeRange    = timeRange;
        this.period       = period;
        this.exerciseType = exerciseType;
        this.tip          = tip;
        this.status       = status;
    }

    public String getTimeRange()     { return timeRange;    }
    public String getPeriod()        { return period;       }
    public String getExerciseType()  { return exerciseType; }
    public String getTip()           { return tip;          }
    public Status getStatus()        { return status;       }
}