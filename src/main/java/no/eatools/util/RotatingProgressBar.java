package no.eatools.util;

import org.apache.commons.lang.time.StopWatch;

/**
 * @author ohs
 */
public class RotatingProgressBar extends Thread {
    private boolean showProgress = true;
    public void run() {
        final String anim= "|/-\\oO";
        int x = 0;
        final StopWatch sw = new StopWatch();
        sw.start();
        while (showProgress) {
            System.out.printf("%s %s %5.2f sec", "\r Processing ", anim.charAt(x++ % anim.length()), + sw.getTime()/1000.0);
            if (getPriority() != MIN_PRIORITY) {
                setPriority(MIN_PRIORITY);
            }
            try { Thread.sleep(100); }
            catch (final Exception e) {};
        }
        sw.stop();
    }

    public void setShowProgress(final boolean showProgress) {
        this.showProgress = showProgress;
    }
}
