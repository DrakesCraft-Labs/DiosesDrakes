package cl.drakescraft.diosesdrakes.api;

/** Result of an idempotent boss reward request. */
public record DivineBossReward(Status status, int favorGranted, int totalFavor) {
    public enum Status {
        GRANTED,
        ALREADY_GRANTED,
        NO_ACTIVE_GOD,
        UPKEEP_SUSPENDED,
        DISABLED,
        FAILED
    }

    public boolean granted() {
        return status == Status.GRANTED;
    }
}
