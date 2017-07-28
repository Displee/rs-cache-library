package org.displee.progress;

public interface ProgressListener {

    void notify(double progress, String message);

    void finish(String title, String message);

}
