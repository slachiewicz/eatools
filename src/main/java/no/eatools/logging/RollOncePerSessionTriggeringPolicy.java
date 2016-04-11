package no.eatools.logging;

/**
 * @author ohs
 */

import java.io.File;

import ch.qos.logback.core.rolling.TriggeringPolicyBase;

public class RollOncePerSessionTriggeringPolicy<E> extends TriggeringPolicyBase<E> {
    private boolean doRolling = true;

    @Override
    public boolean isTriggeringEvent(final File activeFile, final E event) {
        // roll the first time when the event gets called
        if (doRolling) {
            doRolling = false;
            return true;
        }
        return false;
    }
}
